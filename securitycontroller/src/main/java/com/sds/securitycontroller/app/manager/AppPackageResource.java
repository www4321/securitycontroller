package com.sds.securitycontroller.app.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.app.AppPackage;
import com.sds.securitycontroller.utils.OutputMessage;

public class AppPackageResource extends ServerResource {
	protected static Logger log = LoggerFactory
			.getLogger(AppManagerResource.class);
	String appId = null;
	App app = null;
	IAppManagementService appmanager = null;
	OutputMessage response = null;
	protected String dir = null;

	@Override
	public void doInit() {
		appmanager = (IAppManagementService) getContext().getAttributes().get(
				IAppManagementService.class.getCanonicalName());
    	response=new OutputMessage(true, this);
		appId = (String) getRequestAttributes().get("id");
		if (appId != null) {
			app = appmanager.getApp(appId);
		}
		this.dir = appmanager.getPackagedir();
	}

	@Get
	public Representation handleGet() {
		Representation rep = null;
		if (null != appId) {// upgrade
			rep=this.downloadPackage();
		} else {			 
			rep=this.showPackage();
		}
		return rep;
	}

	/*
	 * the directory will be like this: /package/[guid]/[newest-version].xxx
	 * "post http://10.65.100.199:8888/sc/apps/1402472535178/package?guid=what&ver=1.1.1.1"
	 */
	@Post
	public Representation handlePost(Representation entity) {

		String guid = this.getQueryValue("guid");
		String version = this.getQueryValue("ver");
		Representation rep = null;
 
		do {
			if(null != appId){
				response.setResult(404, 404,"not supported");
				break;
			}
			if (guid == null || version == null) {
				response.setResult(404, 404,"query string missed");
				break;
			}

			if (!MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(),
					true)) {
				response.setResult(404, 404,"content type error [RFC 1867].");
				break;
			}
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(1000240);
			RestletFileUpload upload = new RestletFileUpload(factory);

			List<FileItem> items = null;
			try {
				items = upload.parseRepresentation(entity);
			} catch (FileUploadException e) {
				response.setResult(404, 404,"please follow the RFC 1867.");
				break;
			}

			for (final Iterator<FileItem> it = items.iterator(); it.hasNext();) {
				FileItem fi = it.next();
				String filename = fi.getName();
				if (fi.isFormField() || null == filename
						|| "" == filename.trim()) {
					continue;
				}

				String ext = filename.substring(filename.lastIndexOf("."));

				if (!this.savePackage(guid, version, ext, fi)) {
					response.setResult(404, 404,"save package failed.");
					break;
				}
				break;
			}
			response.setResult(200, 200,"upload succeed"); 
		} while (false);
		rep = new StringRepresentation(response.toString(),	MediaType.TEXT_PLAIN);
		return rep;
	}

	private boolean savePackage(String guid, String version, String ext,
			FileItem fi) {
		File fdir = new File(dir + guid);
		boolean ret = false;
		do {
			if (!fdir.exists()) {
				if (!fdir.mkdirs()) {
					break;
				}
			}
			File[] files = fdir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					files[i].delete();
				}
			}
			String path=dir+guid+"/" + version + ext;
			File file = new File(path);
			try {
				fi.write(file);
			} catch (Exception e) {
				break;
			}
			ret = true;
		} while (false);

		return ret;
	}
	
    private Representation downloadPackage(){    	
    	Representation rep = null;
    	do {

			if (null == app) {
				response.setResult(404, 404,"no such app");
				break;
			}
			String dpath=dir + app.getGuid();
			File d = new File(dpath);
			if (!d.isDirectory()) {
				response.setResult(404, 404,"no such package");
				break;
			}

			File[] files = d.listFiles();
			if (0 == files.length) {
				response.setResult(404, 404,"no such package");
				break;
			}

			String path = dpath+"/"+files[0].getName();
			File f = new File(path);			
			rep = new FileRepresentation(f, MediaType.TEXT_PLAIN);
			Disposition disp = new Disposition(Disposition.TYPE_ATTACHMENT);
			disp.setFilename(f.getName());
			disp.setSize(f.length());
			rep.setDisposition(disp);
			setStatus(Status.SUCCESS_OK);
		} while (false);
    	if(null == rep){
    		return new StringRepresentation(response.toString(), MediaType.TEXT_PLAIN);
    	}
    	return rep;
    }
	
	
	 private StringRepresentation showPackage(){  
	    	List<AppPackage> list = new ArrayList<AppPackage>();
			File d = new File(this.dir);
			File[] dirs = d.listFiles();
		 			
			do {
				if (dirs == null) {
					  response.setResult(404, 404,"no snapshot found.");
					break;
				}

				for (int i = 0; i < dirs.length; i++) {
					if (!dirs[i].isDirectory()) {
						continue;
					}
					File[] files=dirs[i].listFiles();
					if(0 == files.length){
						log.warn("no package for app "+dirs[i].getName());
						continue;
					}
					String version=files[0].getName().toLowerCase();
					String guid=dirs[i].getName();
					long time = files[0].lastModified() / 1000;
					long length = files[0].length();
					 
					int k=version.lastIndexOf(".");
					if(k>0){
						version=version.substring(0, k);
					}
					list.add(new AppPackage(guid, version, (int) time,length));
				}
			 
				response.putData("snapshots",list);
			} while (false); 
			
			return  new StringRepresentation(response.toString(), MediaType.TEXT_PLAIN); 
	    } 
	 
}
