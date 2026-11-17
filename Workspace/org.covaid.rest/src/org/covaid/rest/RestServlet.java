package org.covaid.rest;

import java.util.logging.Logger;

import javax.servlet.Servlet;
import org.condast.commons.jpa.rest.CorsFilter;
import org.covaid.rest.resources.FroggerResource;
import org.covaid.rest.resources.PushResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

@Component(service = Servlet.class, 
scope=ServiceScope.PROTOTYPE)
@HttpWhiteboardServletName( RestServlet.S_SERVLET_NAME )
@HttpWhiteboardServletPattern( RestServlet.S_SERVLET_PATTERN)

public class RestServlet extends ServletContainer {
	private static final long serialVersionUID = 671367997395431102L;

	//Same as alias in plugin.xml
	public static final String S_CONTEXT_PATH = "covaid";

	protected static final String S_SERVLET_NAME = S_CONTEXT_PATH + ".rest";
	public static final String S_SERVLET_PATTERN = "/" + RestServlet.S_CONTEXT_PATH +"/*";//DON'T do this in the annotation!!!

	private Logger logger = Logger.getLogger(RestServlet.class.getName());

	public RestServlet() {
		super( new RestApplication());
		logger.info("STARTING SERVLET: " + this.getClass().getName()); 
	}

	private static class RestApplication extends ResourceConfig {

		//Loading classes is the safest way...
		//in equinox the scanning of packages may not work
		private RestApplication() {
			property(ServerProperties.WADL_FEATURE_DISABLE, true);
			register( CorsFilter.class );
			register( FroggerResource.class );
			register( PushResource.class );
		}
	}
}
