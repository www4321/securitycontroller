/*
 * info: access https urls, trust any server
 * todo: verify the server license
 * author:zhuerfu, 20140905 
 */

package com.sds.securitycontroller.utils.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPSHelper {
	protected static Logger log = LoggerFactory.getLogger(HTTPHelper.class);
	private static final String METHOD_GET = "GET";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_PUT = "PUT";
	private static final String METHOD_DELETE = "DELETE";
	private static final String DEFAULT_CHARSET = "utf-8";
	private static final int DEFAULT_CNN_TIMEOUT = 30000;
	private static final int DEFAULT_RD_TIMEOUT = 30000;

	public static HTTPHelperResult httpPost(String reqUrl, String content) {
		return httpRequest(reqUrl, METHOD_POST, DEFAULT_CHARSET, content,
				DEFAULT_CNN_TIMEOUT, DEFAULT_RD_TIMEOUT);
	}
	public static HTTPHelperResult httpPut(String reqUrl, String content) {
		return httpRequest(reqUrl, METHOD_PUT, DEFAULT_CHARSET, content,
				DEFAULT_CNN_TIMEOUT, DEFAULT_RD_TIMEOUT);
	}

	public static HTTPHelperResult httpGet(String reqUrl){
		return httpRequest(reqUrl, METHOD_GET, DEFAULT_CHARSET, null,
				DEFAULT_CNN_TIMEOUT, DEFAULT_RD_TIMEOUT);
	}
	
	public static HTTPHelperResult httpDelete(String reqUrl){
		return httpRequest(reqUrl, METHOD_DELETE, DEFAULT_CHARSET, null,
				DEFAULT_CNN_TIMEOUT, DEFAULT_RD_TIMEOUT);
	}
	
	public static HTTPHelperResult httpRequest(String reqUrl, String method,
			String charset, String content, int connectTimeout, int readTimeout) {
		URL url = null;
		byte[] bytesToSend = {};
		DataOutputStream out = null;
		BufferedReader reader = null;
		HttpsURLConnection connection = null;
		HTTPHelperResult result = new HTTPHelperResult();

		try {
			// build sending bytes
			if (null != content) {
				bytesToSend = content.getBytes(charset);
			}
			// build url
			url = new URL(reqUrl);
			// build ssl context
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(new KeyManager[0],
					new TrustManager[] { new DefaultTrustManager() },
					new SecureRandom());
			SSLContext.setDefault(ctx);
			// build ssl connection
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod(method);
			connection.setReadTimeout(readTimeout);
			connection.setConnectTimeout(connectTimeout);
			connection.setRequestProperty("Content-Type", "application/json;charset=" + charset);
			connection.setRequestProperty("User-Agent", "sds");
			connection.setRequestProperty("Accept",
					"text/xml,text/javascript,text/html");
			connection.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			// sending data
			if(bytesToSend.length>0){
				out = new DataOutputStream(connection.getOutputStream());
				out.write(bytesToSend);// The URL-encoded contend
				out.flush();
			}

			int code = 0;
			code = connection.getResponseCode();
			result.setCode(code);
			// ---from now on, we CORRECTly accessed the sever
			// handle response data
			String rspCharset = getResponseCharset(connection.getContentType());
			InputStream inputStream = connection.getErrorStream();
			if (null == inputStream) {
				inputStream = connection.getInputStream();
			}
			reader = new BufferedReader(new InputStreamReader(inputStream,
					rspCharset));
			// build a string from the response data
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			// fill the result
			result.setMsg(sb.toString());
		} catch (Exception e) {
			log.error("error while posting request: {}", e.getMessage());
			result.setCode(-1);
			result.setMsg(e.getMessage());		
		}

		if (null != connection) {
			connection.disconnect();
		}
		try {
			if (null != out) {
				out.close();
			}
			if (null != reader) {
				reader.close();
			}
		} catch (IOException e) {
			//here, we for the request is succeed, we only log this error 
			log.error("error while posting request: {}", e.getMessage());
		}

		return result;
	}
	
	//other functions
	private static class DefaultTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	}

	private static String getResponseCharset(String ctype) {
		String charset = DEFAULT_CHARSET;

		if (!ctype.isEmpty()) {
			String[] params = ctype.split(";");
			for (String param : params) {
				param = param.trim();
				if (param.startsWith("charset")) {
					String[] pair = param.split("=", 2);
					if (pair.length == 2) {
						if (!pair[1].isEmpty()) {
							charset = pair[1].trim();
						}
					}
					break;
				}
			}
		}

		return charset;
	}
}
