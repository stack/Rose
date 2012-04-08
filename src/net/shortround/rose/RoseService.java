package net.shortround.rose;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.os.Handler;

public class RoseService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private Handler handler;
	private RoseView view;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String requestUri = req.getRequestURI();
		if (requestUri.equals("/rose/data")) {
			resp.setContentType("application/json");
			resp.getWriter().println(view.getSerializedData().toString());
		} else if (requestUri.equals("/rose/decay")) {
			handler.sendMessage(handler.obtainMessage(RoseActivity.MESSAGE_DECAY));
			
			resp.setContentType("text/plain");
			resp.setStatus(200);
		} else if (requestUri.equals("/rose/revert")) {
			handler.sendMessage(handler.obtainMessage(RoseActivity.MESSAGE_REVERT));
			
			resp.setContentType("text/plain");
			resp.setStatus(200);
		} else if (requestUri.equals("/rose/toggle")) {
			handler.sendMessage(handler.obtainMessage(RoseActivity.MESSAGE_TOGGLE_DISPLAY));
			
			resp.setContentType("text/plain");
			resp.setStatus(200);
		} else {
			resp.sendError(404, "Forbidden");
		}
	}
	
	public void setHandler(Handler value) {
		handler = value;
	}
	
	public void setView(RoseView view) {
		this.view = view;
	}
}
