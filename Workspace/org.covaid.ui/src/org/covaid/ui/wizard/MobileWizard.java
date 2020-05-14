package org.covaid.ui.wizard;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.condast.commons.auth.AuthenticationData;
import org.condast.commons.auth.AuthenticationData.Authentication;
import org.condast.commons.config.Config;
import org.condast.commons.messaging.http.AbstractHttpRequest;
import org.condast.commons.messaging.http.ResponseEvent;
import org.condast.js.commons.controller.AbstractJavascriptController;
import org.condast.js.commons.eval.IEvaluationListener;
import org.condast.js.commons.wizard.AbstractHtmlParser;
import org.covaid.core.def.IMobile;
import org.covaid.core.mobile.IMobileRegistration;
import org.covaid.core.mobile.RegistrationEvent;
import org.covaid.core.model.date.DateMobile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;

public class MobileWizard extends Composite {
	private static final long serialVersionUID = 1L;

	//public static final String S_PATH = "http://localhost:10080/covaid/mobile/rest";
	//public static final String S_PATH = "http://www.condast.com:8080/covaid/mobile/rest";
	public static final String S_COVAID_CONTEXT = "covaid/mobile/rest";

	private enum Requests{

		CREATE,
		REMOVE,
		GET,
		GET_SAFETY,
		GET_RISK;
		
		@Override
		public String toString() {
			return super.toString();
		}
	}
	
	public enum Links{
		DOWNLOAD,
		INSTALLING,
		INDEX,
		HEALTH,
		DOCTOR,
		SETTINGS,
		ACKNOWLEDGEMENTS;

		@Override
		public String toString() {
			return super.toString();
		}
		
		public String toFile() {
			return "/resources/" + super.toString().toLowerCase() + ".ht";
		}
	}

	public enum Labels{
		SAFETY,
		RISK,
		TEMP;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}		
	}

	private Browser browser;
	private Group grpIndication;
	private Canvas canvas;

	private AbstractHtmlParser wizard;
	
	private Links link;
		
	private CanvasController controller;
	
	private IMobile<Date> mobile;
	private AuthenticationData authData;
	private Config config;
	
	private Collection<IMobileRegistration<Date>> listeners;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private PaintListener listener = (e)->{
		Canvas canvas = (Canvas) e.getSource();
		Rectangle rect = canvas.getBounds();
		int radius = Math.min(rect.width, rect.height)/3;
		int riskRadius = radius;
		double safety = ( this.mobile == null )?100:this.mobile.getSafety();
		double amplify = ( this.mobile == null )? 1.5: 2*(1.01-safety/100);
		int safetyRadius = (int) (amplify*radius);
		GC gc = e.gc;
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN));
		gc.fillOval((rect.width-riskRadius)/2, (rect.height-riskRadius)/2, riskRadius, riskRadius);
		gc.setLineWidth(4);
		//gc.setLineStyle(SWT.LINE_DOT);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.drawOval((rect.width-safetyRadius)/2, (rect.height-safetyRadius)/2, safetyRadius, safetyRadius);
		gc.dispose();
		requestLayout();
	};
	
	private IEvaluationListener<Object> elistener = (event) ->{
		try {
			logger.fine("CALLBACK");
			if(!CanvasController.S_INITIALISTED_ID.equals(event.getId()) || ( this.authData == null ))
				return;
			
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}	
	};
			
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MobileWizard(Composite parent, int style) {
		super(parent, style | SWT.NO_SCROLL);
		config = new Config();
		this.link = Links.DOWNLOAD;
		createPage(parent, style | SWT.NO_SCROLL);
		this.controller = new CanvasController( this.browser );
		this.controller.addEvaluationListener(elistener);
		this.listeners = new ArrayList<>();
	}
	
	private String createVariables( String args ) throws Exception {
		StringBuilder builder = new StringBuilder();
		String[] split = args.split("[&]");
		Map<Authentication, String> auth = new HashMap<>();
		for( String str: split ) {
			builder.append("var " + str + ";\n");
			String[] split1 = str.split("[=]");
			auth.put(AuthenticationData.Authentication.valueOf(split1[0].toUpperCase()), split1[1]);
		}
		this.authData = new AuthenticationData(auth);
		if( mobile == null ) {
			WebClient client = new WebClient();
			Map<String, String> params = authData.toMap();
			client.sendGet(Requests.GET, params);
		}
		return builder.toString();
	}

	protected void createPage( Composite parent, int style ) {
		super.setLayout(new GridLayout(1, false));
		super.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		browser = new Browser( this, SWT.BORDER | SWT.NO_SCROLL);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browser.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		wizard = new AbstractHtmlParser(browser, MobileWizard.class) {
		
			@Override
			protected String onHandleContext(String context, String application, String service) {
				return config.getServerContext() + S_COVAID_CONTEXT;
			}

			@Override
			protected void onHandleLinks( String linkStr) {
				try {
					String[] split = linkStr.split("[?]");
					if( split.length>1)
						createVariables(split[1]);
					link = Links.valueOf(split[0].toUpperCase());
					grpIndication.setVisible( !Links.DOWNLOAD.equals(link));
					super.createPage( MobileWizard.class.getResourceAsStream(link.toFile()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			protected String onHandleLabel(String id, Attributes attr) {
				String result = attr.toString();
				switch( attr ) {
				default:
					break;
				}
				return result;
			}
			
			@Override
			protected String onHandleScript( Class<?> clss, String path) {
				StringBuilder builder = new StringBuilder();
				switch( link) {
				case DOWNLOAD:
					path = path.replace("mobile", "download" );
					break;
				case INSTALLING:
					path = path.replace("mobile", "installing" );
					break;
				default:
					break;
				}
				try {
					builder.append( parse( clss.getResourceAsStream( path)));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return builder.toString();
			}

			@Override
			protected String onHandleAuthentication( String id, AuthenticationData.Authentication authentication) {
				StringBuilder builder = new StringBuilder();
				if( authData == null )
					return builder.toString();
				builder.append("=");
				switch( authentication ) {
				case IDENTIFIER:
					builder.append("'");
					builder.append( authData.getIdentifier());
					builder.append("'");
					break;
				default:
					builder.append( authData.get(authentication));
					break;
				}
				return builder.toString();
			}

			@Override
			protected String onHandleValues(Functions function, String id, Attributes attr) {
				int result = 0;
				Labels label = Labels.valueOf(id.toUpperCase());
				switch( attr ) {
				case MIN:
					result = Labels.TEMP.equals(label)?35: 0;
					break;
				case MAX:
					result = Labels.TEMP.equals(label)?40: 100;
					break;
				default:
					break;
				}
				return String.valueOf(result);
			}
			
			@Override
			protected String onHandleFunction(Functions function, String id, Attributes attr) {
				String result = null;
				try {
					switch( function ) {
					case SCRIPT:
						StringBuilder builder = new StringBuilder();
						builder.append( "var mobile-id = ");
						WebClient client = new WebClient();
						Map<String, String> params = new HashMap<>();
						client.sendGet( Requests.CREATE, params, builder);
						return builder.toString();
					default:
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return result;
			}	
		};

		grpIndication = new Group(this, SWT.NONE);
		grpIndication.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		grpIndication.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpIndication.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpIndication.setText("Indication");
		grpIndication.setVisible(false);
		
		canvas = new Canvas(grpIndication, SWT.NONE);
		canvas.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		canvas.addPaintListener(listener);
		
		wizard.createPage( MobileWizard.class.getResourceAsStream( link.toFile()));	
	}

	public void addRegistrationListener( IMobileRegistration<Date> listener ) {
		this.listeners.add(listener);
	}

	public void removeRegistrationListener( IMobileRegistration<Date> listener ) {
		this.listeners.remove(listener);
	}
	
	protected void notifyRegistrationEvent( RegistrationEvent<Date> event ){
		for( IMobileRegistration<Date> listener: this.listeners )
			listener.notifyMobileRegistration(event);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void dispose() {
		if( this.mobile != null ) {
			WebClient client = new WebClient();
			Map<String, String> params = authData.toMap();
			try {
				client.sendGet(Requests.REMOVE, params);
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.mobile = null;
		}
		this.controller.removeEvaluationListener(elistener);
		this.controller.dispose();
		super.dispose();
	}
	
	private class WebClient extends AbstractHttpRequest<Requests, StringBuilder>{
	
		public WebClient() {
			super( S_COVAID_CONTEXT );
			super.setContextPath( config.getServerContext() + S_COVAID_CONTEXT);
		}

		@Override
		protected void sendGet(Requests request, Map<String, String> parameters ) throws Exception {
			super.sendGet(request, parameters);
		}

		@Override
		protected void sendGet(Requests request, Map<String, String> parameters, StringBuilder data) throws Exception {
			super.sendGet(request, parameters, data);
		}
	
		@Override
		protected String onHandleResponse(ResponseEvent<Requests, StringBuilder> event, StringBuilder data)
				throws IOException {
			Gson gson = new Gson();
			switch( event.getRequest()) {
			case CREATE://is usually not called, but done directly from html
				mobile = gson.fromJson( event.getResponse(), DateMobile.class );
				break;
			case GET:
				IMobile<Date> newMobile = gson.fromJson( event.getResponse(), DateMobile.class );
				if(( mobile == null ) || !mobile.getIdentifier().equals( newMobile.getIdentifier()))
					notifyRegistrationEvent( new RegistrationEvent<Date>(browser, IMobileRegistration.RegistrationTypes.REGISTER, authData, mobile));
				canvas.redraw();
				break;
			case REMOVE:
				notifyRegistrationEvent( new RegistrationEvent<Date>(browser, IMobileRegistration.RegistrationTypes.UNREGISTER, authData, mobile));
				break;
			default:
				break;// TODO Auto-generated method stub
			}
			return null;
		}

		@Override
		protected void onHandleResponseFail(HttpStatus status, ResponseEvent<Requests, StringBuilder> event)
				throws IOException {
			logger.info("REQUEST: " + event.getRequest() + ", STATUS: " + status);
			super.onHandleResponseFail(status, event);
		}	
	}
	
	private class CanvasController extends AbstractJavascriptController{
		public static final String S_INITIALISTED_ID = "CanvasInitialisedId";

		public static final String S_CALLBACK_ID = "CallBackId";
		private String S_REFRESH_CANVAS = "refreshCanvas";

		private BrowserFunction callback;

		public CanvasController(Browser browser) {
			super( browser, S_INITIALISTED_ID );
			this.callback = createCallBackFunction( S_CALLBACK_ID, S_REFRESH_CANVAS );	
		}

		@Override
		protected void onLoadCompleted() {
			logger.info("COMPLETED");
		}
		
		public void dispose(){
			this.callback.dispose();
		}
	}
}