package net.shortround.rose;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.content.res.AssetManager;

public class StaticProxy extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	protected static HashMap<String, String>extensions;
	protected AssetManager assetManager;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		extensions = new HashMap<String, String>();
		extensions.put("css",  "text/css");
		extensions.put("html", "text/html");
		extensions.put("js",   "text/javascript");
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String requestUri = req.getRequestURI();
		if ((requestUri == null) || (requestUri.equals(""))) {
			resp.sendError(400, "Missing resource string");
		} else {
			if (requestUri.equals("/")) {
				requestUri = requestUri + "index.html";
			}
			
			String[] requestUriArray = requestUri.split("/");
			String requestFileName = requestUriArray[(requestUriArray.length - 1)];
			String[] requestFileNameArray = requestFileName.split("\\.");
			String requestFileExtension = requestFileNameArray[(requestFileNameArray.length - 1)];
			
			String requestAssetsPath = requestUri.substring(1);
			InputStream resourceInputStream = assetManager.open(requestAssetsPath);
			
			resp.setContentType(extensions.get(requestFileExtension));
			
			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			while ((bytesRead = resourceInputStream.read(buffer)) != -1) {
				resp.getOutputStream().write(buffer, 0, bytesRead);
			}
			
			resourceInputStream.close();
			resp.getOutputStream().flush();
			resp.getOutputStream().close();
		}
	}
	
	public void setAssetManager(AssetManager value) {
		assetManager = value;
	}
}
