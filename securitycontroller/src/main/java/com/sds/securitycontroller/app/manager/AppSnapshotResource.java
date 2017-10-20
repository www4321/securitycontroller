package com.sds.securitycontroller.app.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.*;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.app.App;
import com.sds.securitycontroller.utils.InputMessage;
import com.sds.securitycontroller.utils.OutputMessage;
import com.sds.securitycontroller.utils.Snapshot;
import com.sds.securitycontroller.utils.http.HTTPHelper;
import com.sds.securitycontroller.utils.http.HTTPHelperResult;

public class AppSnapshotResource extends ServerResource {
	protected static Logger log = LoggerFactory
			.getLogger(AppManagerResource.class);
	OutputMessage response = null;
	String appId = null;
	App app = null;
	IAppManagementService appmanager = null;
	protected String dir = null;

	@Override
	public void doInit() {
		appmanager = (IAppManagementService) getContext().getAttributes().get(
				IAppManagementService.class.getCanonicalName());
		appId = (String) getRequestAttributes().get("id");
		if (appId != null) {
			app = appmanager.getApp(appId);
		}
		this.response= new OutputMessage(true,this);
		this.dir=appmanager.getSnapshotdir();
	}

	@Get
	public Representation handleGet() {
		Representation rep = null;
		do {
			if (null != appId) {// download
				rep = this.downloadSnapshot();
			} else {// show
				rep = this.showSnapshot();
			}
		} while (false);
		return rep;
	}

	// this method is for ui
	@Put
	public String handlePutRequest(String fmJson) {
		do {
			if (null == appId) {
				response.setResult(404, 404,"appid is null");
				break;
			}
			if (null == app) {
				response.setResult(404, 404,"no such app");
				break;
			}

			String url = "http://" + app.getHost() + ":" + app.getPort()
					+ "/snapshot";
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
			HTTPHelperResult result = HTTPHelper.httpGet(url, headers);
			
  			if(-1 == result.getCode()){
				response.setResult(500, 500,"sc error "+result.getMsg());
				break;
			}
			
			InputMessage imsg=null;
			try {
				imsg=new InputMessage(true,result.getMsg());
			} catch (IOException e) {
				response.setResult(result.getCode(),result.getCode(), result.getMsg());
				break;
			}
			

			 
			if (200 != result.getCode()) {
				response.setResult(500, 500,"sc error "+imsg.getData().path("result").asText());
				break;
			}
			response.setResult(200,200, "operation succeed");
		} while (false);

		return response.toString();
	}

	@Post
	public Representation handlePost(Representation entity) {

		Representation rep = null;
		response.setResult(200, 200,"upload succeed.");
		do {
			if (appId == null) {
				response.setResult(404,404, "appid is null");
				break;
			}
			if (app == null) {
				response.setResult(404,404, "no such app");
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
				response.setResult(404,404, "please follow the RFC 1867.");
			}

			for (final Iterator<FileItem> it = items.iterator(); it.hasNext();) {
				FileItem fi = it.next();
				String filename = fi.getName();
				if (fi.isFormField() || null == filename
						|| "" == filename.trim()) {
					continue;
				}

				String extName = filename.substring(filename.lastIndexOf("."));
				String newName = "snapshot_app_" + appId + extName;

				try {
					File fdir = new File(dir);
					if (!fdir.exists()) {
						if (!fdir.mkdirs()) {
							response.setResult(404,404, "create dir error");
							break;
						}
					}

					String oldf = this.getFileName(appId);
					if (null != oldf) {
						File of = new File(dir + oldf);
						if (of.exists()) {
							of.delete();
						}
					}

					File file = new File(dir + newName);
					fi.write(file);

				} catch (Exception e) {
					response.setResult(404,404,
							"write file error " + e.getMessage());
					break;
				}
			}
		} while (false);
		rep = new StringRepresentation(response.toString(),
				MediaType.TEXT_PLAIN);
		return rep;
	}

	// --------------functions-----------

	private String getFileName(String id) {
		String pat = "snapshot_app_" + id.toLowerCase() + ".";
		File d = new File(this.dir);
		File[] files = d.listFiles();

		if (files == null) {
			return null;
		}

		for (int i = 0; i < files.length; i++) {
			if (!files[i].isFile()) {
				continue;
			}
			String filename = files[i].getName().toLowerCase();
			if (filename.startsWith(pat)) {// got the file
				return filename;
			}
		}
		return null;
	}

	private Representation downloadSnapshot() {
		Representation rep = null;
		do {
			String filename = getFileName(appId);
			if (null == filename) {
				response.setResult(404,404, "snapshot not found.");
				rep = new StringRepresentation(response.toString(),
						MediaType.TEXT_PLAIN);
				break;
			}
			String path = dir + filename;
			File f = new File(path);
			rep = new FileRepresentation(f, MediaType.TEXT_PLAIN);
			Disposition disp = new Disposition(Disposition.TYPE_ATTACHMENT);
			disp.setFilename(f.getName());
			disp.setSize(f.length());
			rep.setDisposition(disp);
			setStatus(Status.SUCCESS_OK);
		} while (false);

		return rep;
	}

	/*
	 * 快照文件名类似：snapshot_app_[appid].xxxx 遍历快照目录取出appid，生成AppSnapshot对象列表，展示出来
	 */
	private StringRepresentation showSnapshot() {
		List<Snapshot> list = new ArrayList<Snapshot>();

		File d = new File(this.dir);
		File[] files = d.listFiles();
		App tmpapp = new App();
		do {
			if (files == null) {
				response.setResult(404,404, "no snapshot found");
				break;
			}
			/*
			 * traverse the 'snapshot' dir the 'snapshot file' itself contains
			 * appid
			 */
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isFile()) {
					continue;
				}

				String filename = files[i].getName().toLowerCase();
				String fields[] = filename.split("[._]", 5);
				if (4 != fields.length) {
					log.warn("filename not ok.");
					continue;
				}
				if(!fields[1].equalsIgnoreCase("app")){//filter the dev snapshot
					continue;
				}
				String appid = fields[2];
				String appname = "NOT FOUND";
				long time = files[i].lastModified() / 1000;
				long length = files[i].length();

				tmpapp = appmanager.getApp(fields[2]);
				if (null != tmpapp)
					appname = tmpapp.getName();
				list.add(new Snapshot(appid, "APP",appname, filename, (int) time,
						length));
			} 
			response.putData("snapshots", list); 
		} while (false);

		return new StringRepresentation(response.toString(),
				MediaType.TEXT_PLAIN);
	}

}
