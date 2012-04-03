package net.shortround.rose;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

import android.content.res.AssetManager;
import android.os.Handler;

public class WebServer {
	
	private static WebServer sharedInstance = null;
	protected Server server;
	protected static int serverPort = 8080;
	
	protected RoseService roseService;
	protected StaticProxy staticProxy;

	protected WebServer(int serverPort) {
		server = new Server(serverPort);
		
		roseService = new RoseService();
		staticProxy = new StaticProxy();
		
		ServletHandler handler = new ServletHandler();
		handler.addServletWithMapping(new ServletHolder(roseService), "/rose/*");
		handler.addServletWithMapping(new ServletHolder(staticProxy), "/*");
		server.addHandler(handler);
		
		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static WebServer getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new WebServer(serverPort);
		}
		
		return sharedInstance;
	}
	
	public void setAssetManager(AssetManager assetManager) {
		staticProxy.setAssetManager(assetManager);
	}
	
	public void setHandler(Handler handler) {
		roseService.setHandler(handler);
	}
	
	public void setView(RoseView view) {
		roseService.setView(view);
	}
}
