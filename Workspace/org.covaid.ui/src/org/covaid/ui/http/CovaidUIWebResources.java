package org.covaid.ui.http;

import java.util.logging.Logger;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;

@Component( service = CovaidUIWebResources.class )
@HttpWhiteboardResource(pattern="/covaid/images/*", prefix="/images")
public class CovaidUIWebResources {

	public static final String S_COVAID_RESOURCE = "covaid-resource";

	public static final String S_HOME_IMAGE = "img src=\"/" + S_COVAID_RESOURCE + "/web/images/aquabots.jpg\" class=\"img-fluid mx-auto d-block\" alt=\"Responsive image\"";

	private Logger logger = Logger.getLogger(CovaidUIWebResources.class.getName());

	public CovaidUIWebResources() {
		logger.info("**** RESOURCES LOADED: " + this.getClass().getName()); 
	}
}
