package org.covaid.dashboard.http;

import java.util.logging.Logger;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;

@Component( service = CovaidWebResources.class )
@HttpWhiteboardResource(pattern="/covaid/web/*", prefix="/WEB-INF")
public class CovaidWebResources {

	public static final String S_COVAID_RESOURCE = "covaid-resource";

	public static final String S_HOME_IMAGE = "img src=\"/" + S_COVAID_RESOURCE + "/web/images/aquabots.jpg\" class=\"img-fluid mx-auto d-block\" alt=\"Responsive image\"";

	private Logger logger = Logger.getLogger(CovaidWebResources.class.getName());

	public CovaidWebResources() {
		logger.info("**** RESOURCES LOADED: " + this.getClass().getName()); 
	}
}
