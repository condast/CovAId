package org.covaid.dashboard;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.Application.OperationMode;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;

public class BasicApplication implements ApplicationConfiguration {

	private static final String S_ENTRY_POINT = "/home";
	private static final String S_COVAID_THEME = "covaid.theme";
	private static final String S_THEME_CSS = "themes/theme.css";

    @Override
	public void configure(Application application) {
        //application.addStyleSheet( S_COVAID_THEME, S_THEME_CSS );
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(WebClient.PAGE_TITLE, "Hello CovAId");
        //properties.put( WebClient.THEME_ID, S_COVAID_THEME );
        
        application.setOperationMode( OperationMode.SWT_COMPATIBILITY );       
        application.addEntryPoint( S_ENTRY_POINT, BasicEntryPoint.class, properties);
     }
}