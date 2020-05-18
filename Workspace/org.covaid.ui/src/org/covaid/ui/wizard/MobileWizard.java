package org.covaid.ui.wizard;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.condast.commons.Utils;
import org.condast.commons.auth.AuthenticationData;
import org.condast.commons.auth.AuthenticationData.Authentication;
import org.condast.commons.config.Config;
import org.condast.commons.messaging.http.AbstractHttpRequest;
import org.condast.commons.messaging.http.IHttpClientListener;
import org.condast.commons.messaging.http.ResponseEvent;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.ui.session.AbstractSessionHandler;
import org.condast.commons.ui.session.SessionEvent;
import org.condast.js.commons.controller.AbstractJavascriptController;
import org.condast.js.commons.eval.IEvaluationListener;
import org.condast.js.commons.wizard.AbstractHtmlParser;
import org.covaid.core.data.frogger.LocationData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IMobile;
import org.covaid.core.def.IPoint;
import org.covaid.core.def.IContagion.SupportedContagion;
import org.covaid.core.mobile.IMobileRegistration;
import org.covaid.core.mobile.RegistrationEvent;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Point;
import org.covaid.core.model.date.DateMobile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;

public class MobileWizard extends Composite {
	private static final long serialVersionUID = 1L;

	//public static final String S_PATH = "http://localhost:10080/covaid/mobile/rest";
	//public static final String S_PATH = "http://www.condast.com:8080/covaid/mobile/rest";
	public static final String S_COVAID_CONTEXT = "covaid/rest";
	public static final String S_COVAID_MOBILE_CONTEXT = "covaid/mobile/rest";

	public static final int DEFAULT_WIDTH = 100;//metres
	public static final int DEFAULT_HISTORY = 16;//day, looking ahead of what is coming
	public static final int DEFAULT_RADIUS = 5;//day, looking ahead of what is coming

	private enum Requests{

		CREATE,
		REMOVE,
		GET,
		GET_SAFETY,
		GET_RISK,
		SURROUNDINGS;
		
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

	private enum Attributes{
		XPOS,
		YPOS,
		RADIUS,
		STEP;
		
		@Override
		public String toString() {
			return StringStyler.xmlStyleString( super.toString());
		}
	}

	private Browser browser;
	private Group grpIndication;
	private Canvas canvasSafety;
	private Canvas canvasForecast;
	
	private AbstractHtmlParser wizard;
	
	private Links link;
		
	private CanvasController controller;
	
	private IMobile<Date> mobile;
	private AuthenticationData authData;
	private Config config;

	private Map<IPoint,LocationData> hubs;	
	private int timeStep;

	private Collection<IMobileRegistration<Date>> listeners;

	private SessionHandler session;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private PaintListener safetyListener = (e)->{
		Canvas canvas = (Canvas) e.getSource();
		Rectangle rect = canvas.getBounds();
		int radius = Math.min(rect.width, rect.height)/3;
		int riskRadius = radius;
		double safety = ( this.mobile == null )?100:this.mobile.getSafety();
		double amplify = ( this.mobile == null )? 1.5: 2*(1.01-safety/100);
		int safetyRadius = (int) (amplify*radius);
		GC gc = e.gc;
		Color base = getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN);
		gc.setBackground(base);
		
		int topX = (rect.width-riskRadius)/2;
		int topY = (rect.height-riskRadius)/2;
		int centreX = rect.width/2;
		int centreY = rect.height/2;
		gc.fillOval(topX, topY, riskRadius, riskRadius);
		gc.setLineWidth(4);
		//gc.setLineStyle(SWT.LINE_DOT);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.drawOval((rect.width-safetyRadius)/2, (rect.height-safetyRadius)/2, safetyRadius, safetyRadius);

		//Fill in the hubs
		if( !Utils.assertNull(hubs)) {
			double step = riskRadius/DEFAULT_RADIUS;
			int[] arr = {centreX,centreY,0,0,0,0,centreX,centreY};
			for( int angle=0; angle<359; angle++ ) {
				double phi = Math.toRadians( angle );
				for(int length=DEFAULT_RADIUS; length>=0; length--) {
					int xpos = (int) (length * Math.sin(phi));
					int ypos = (int) (length * Math.cos(phi));
					LocationData data = hubs.get( new Point( xpos, ypos)); 
					if( data == null )
						continue;
					IContagion<Integer> contagion = new Contagion(SupportedContagion.COVID_19);
					Map<Contagion, Double> contagions = data.getContagions();
					if( Utils.assertNull(contagions))
						continue;
					Color colour = getColour(base, contagion, contagions.get(contagion));
					gc.setBackground(colour);

					int x = (int) (length * step/2 * Math.sin(phi));
					int y = (int) (length * step/2 * Math.cos(phi));
					arr[2] = (int) (centreX + x); arr[3] = (int) (centreY + y); 
					phi = Math.toRadians(angle+1);
					x = (int) (length * step/2 * Math.sin(phi));
					y = (int) (length * step/2 * Math.cos(phi));
					arr[4] = (int) (centreX + x); arr[5] = (int) (centreY + y); 
					gc.fillPolygon(arr);
				}
			}
		}
		gc.dispose();
		requestLayout();
	};

	private PaintListener forecastListener = (e)->{
		Canvas canvas = (Canvas) e.getSource();
		Rectangle rect = canvas.getBounds();
		int halfY = rect.height/2;
		GC gc = e.gc;
		Color base = getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN);
		gc.setBackground(base);
		gc.drawLine( 10, 10, 10, rect.height - 10 );
		gc.drawLine( 10, halfY, rect.width, halfY );
		for( int i=0; i<DEFAULT_HISTORY; i++ ) {
			int x = 10 + i * (rect.width-20)/DEFAULT_HISTORY;
			gc.drawLine(x, halfY-3, x, halfY+3);
		}
		gc.dispose();
		requestLayout();
	};

	private IEvaluationListener<Object> elistener = (event) ->{
		try {
			logger.fine("CALLBACK");
			if(!CanvasController.S_CALLBACK_ID.equals(event.getId()) || ( this.authData == null ))
				return;
			MobileWebClient client = new MobileWebClient();
			Map<String, String> params = authData.toMap();
			client.sendGet(Requests.GET, params);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}	
	};
	private Group grpDaysForecast;
			
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

		this.timeStep = 0;
		this.hubs = new TreeMap<>();

		session = new SessionHandler(getDisplay());
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
			MobileWebClient client = new MobileWebClient();
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
				return config.getServerContext() + S_COVAID_MOBILE_CONTEXT;
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
						MobileWebClient client = new MobileWebClient();
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

		grpDaysForecast = new Group(this, SWT.NONE);
		grpDaysForecast.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		grpDaysForecast.setText("14 Days Forecast");
		grpDaysForecast.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpDaysForecast.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		canvasForecast = new Canvas(grpDaysForecast, SWT.NONE);
		canvasForecast.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		canvasForecast.addPaintListener(forecastListener);

		grpIndication = new Group(this, SWT.NONE);
		grpIndication.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		grpIndication.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpIndication.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpIndication.setText("Indication");
		grpIndication.setVisible(false);
		
		
		canvasSafety = new Canvas(grpIndication, SWT.NONE);
		canvasSafety.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		canvasSafety.addPaintListener(safetyListener);
		
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

	public void poll() {
		try {
			WebClient client = new WebClient();
			client.addListener(session);
			int xpos = DEFAULT_WIDTH/2;
			int ypos = DEFAULT_HISTORY;
			Map<String, String> params = authData.toMap();
			params.put(Attributes.XPOS.toString(), String.valueOf( xpos ));
			params.put(Attributes.YPOS.toString(), String.valueOf( ypos ));
			params.put(Attributes.RADIUS.toString(), String.valueOf( DEFAULT_RADIUS ));
			params.put(Attributes.STEP.toString(), String.valueOf( DEFAULT_HISTORY+1 ));
			client.sendGet(Requests.SURROUNDINGS, params);
			client.removeListener(session);
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
	}
	
	protected Color getColour( Color colour, IContagion<?> contagion, double value ) {
		double green = colour.getGreen() * (1f-value/100);
		int red = (int) (255f*value/100);
		return new Color( getDisplay(), red, (int)(green), 0 );
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void dispose() {
		if( this.mobile != null ) {
			MobileWebClient client = new MobileWebClient();
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
	
	private class MobileWebClient extends AbstractHttpRequest<Requests, StringBuilder>{
	
		public MobileWebClient() {
			super( config.getServerContext() + S_COVAID_MOBILE_CONTEXT);
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
				mobile = gson.fromJson( event.getResponse(), DateMobile.class );
				notifyRegistrationEvent( new RegistrationEvent<Date>(browser, IMobileRegistration.RegistrationTypes.REGISTER, authData, mobile));
				break;
			case REMOVE:
				notifyRegistrationEvent( new RegistrationEvent<Date>(browser, IMobileRegistration.RegistrationTypes.UNREGISTER, authData, mobile));
				break;
			default:
				break;// TODO Auto-generated method stub
			}
			canvasSafety.redraw();
			return null;
		}

		@Override
		protected void onHandleResponseFail(HttpStatus status, ResponseEvent<Requests, StringBuilder> event)
				throws IOException {
			logger.info("REQUEST: " + event.getRequest() + ", STATUS: " + status);
			super.onHandleResponseFail(status, event);
		}
	}

	private class WebClient extends AbstractHttpRequest<Requests, StringBuilder>{
		
		public WebClient() {
			//super( S_PATH);
			super( config.getServerContext() + S_COVAID_CONTEXT );
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
			return event.getResponse();
		}
		
		@Override
		protected void onHandleResponseFail(HttpStatus status, ResponseEvent<Requests, StringBuilder> event)
				throws IOException {
			switch( event.getRequest()){
			case SURROUNDINGS:
				break;
			default:
				super.onHandleResponseFail(status, event);
				break;
			}	
		}

	}
	
	protected void setHubs( LocationData[] hubData ) {
		this.hubs.clear();
		if( hubData == null )
			return;
		for( LocationData hd: hubData ) {
			if( hd == null ) {
				System.err.println("Error");
				continue;
			}
			hubs.put(hd.getPoint(), hd);
			timeStep = hd.getPoint().getYpos();
		}
		//System.err.println("WAIT");
	}
	
	private class SessionHandler extends AbstractSessionHandler<ResponseEvent<Requests, StringBuilder>> implements IHttpClientListener<Requests, StringBuilder>{

		protected SessionHandler(Display display) {
			super(display);
		}

		@Override
		protected void onHandleSession(SessionEvent<ResponseEvent<Requests, StringBuilder>> sevent) {
			if( sevent.getData() == null )
				return;
			Gson gson = new Gson();
			ResponseEvent<Requests, StringBuilder> response = sevent.getData();
			switch( sevent.getData().getRequest()) {
			case SURROUNDINGS:
				setHubs( gson.fromJson(response.getResponse(), LocationData[].class));				
				break;
			default:
				break;
			}
			canvasSafety.redraw();
			canvasForecast.redraw();
			requestLayout();
		}

		@Override
		public void notifyResponse(ResponseEvent<Requests, StringBuilder> event) {
			addData(event);
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