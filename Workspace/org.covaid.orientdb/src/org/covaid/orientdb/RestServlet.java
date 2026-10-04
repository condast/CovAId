package org.covaid.orientdb;

import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;

import org.covaid.orientdb.rest.DatabaseRestService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class RestServlet extends ServletContainer {
	private static final long serialVersionUID = -619292608072097888L;
	
	//Same as alias in plugin.xml
	public static final String S_CONTEXT_PATH = "aieonf/orientdb";

	private Logger logger = Logger.getLogger(RestServlet.class.getName());

	public RestServlet() {
		super( new RestApplication());
		logger.info("STARTING SERVLET: " + this.getClass().getName()); 
	}

	@ApplicationPath(S_CONTEXT_PATH)
	private static class RestApplication extends ResourceConfig {

		//Loading classes is the safest way...
		//in equinox the scanning of packages may not work
		private RestApplication() {
			register( DatabaseRestService.class );
		}
	}
}
