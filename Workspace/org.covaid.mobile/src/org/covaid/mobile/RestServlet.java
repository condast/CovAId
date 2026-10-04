package org.covaid.mobile;

import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.ws.rs.ApplicationPath;
import org.condast.commons.jpa.rest.CorsFilter;
import org.covaid.mobile.resources.MobileResource;
import org.covaid.mobile.resources.NotificationResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(service = Servlet.class, 
scope=ServiceScope.PROTOTYPE,
property= "osgi.http.whiteboard.servlet.pattern=/" + RestServlet.S_CONTEXT_PATH + "/*")
public class RestServlet extends ServletContainer {
	private static final long serialVersionUID = -7407460118171263754L;
	
	public static final String S_CONTEXT_PATH = "covaid";

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
			register( CorsFilter.class );
			register( MobileResource.class );
			register( NotificationResource.class );
		}
	}
}
