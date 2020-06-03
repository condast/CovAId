package org.covaid.ui.mobile;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import org.condast.commons.strings.StringUtils;
import org.condast.commons.ui.session.AbstractSessionHandler;
import org.condast.commons.ui.session.SessionEvent;
import org.condast.commons.ui.xy.AbstractMultiXYGraph;
import org.condast.js.commons.controller.AbstractJavascriptController;
import org.condast.js.commons.eval.IEvaluationListener;
import org.condast.js.commons.wizard.AbstractHtmlParser;
import org.covaid.core.data.ContagionData;
import org.covaid.core.data.TimelineCollection;
import org.covaid.core.data.TimelineData;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Button;

public class MobileWizard extends Composite {
	private static final long serialVersionUID = 1L;

	public static final String S_COVAID_CONTEXT = "covaid/rest";
	public static final String S_COVAID_MOBILE_CONTEXT = "covaid/mobile/rest";

	public static final int DEFAULT_WIDTH = 100;//metres
	public static final int DEFAULT_HISTORY = 16;//day, looking ahead of what is coming
	public static final int DEFAULT_RADIUS = 5;//day, looking ahead of what is coming

	private static final String S_FORECAST = "14 Day Forecast";

	public enum Hubs{
		CENTRE,
		PROTECTED;
		
		public int getColorCode() {
			int colour = SWT.COLOR_WHITE;
			switch( this ) {
			case CENTRE:
				colour = SWT.COLOR_RED;
				break;
			case PROTECTED:
				colour = SWT.COLOR_DARK_GREEN;
				break;
			default:
				break;
			}
			return colour;
		}
	}

	public enum Contexts{
		REST,
		MOBILE;
		
		public String getPath() {
			return MOBILE.equals(this)? S_COVAID_MOBILE_CONTEXT: S_COVAID_CONTEXT;
		}
	}
	
	private enum Requests{

		REGISTER,
		CREATE,
		REMOVE,
		GET,
		GET_SAFETY,
		GET_RISK,
		SURROUNDINGS,
		SET_PROTECTION,
		PROTECTED,
		PREDICTION,
		AVERAGE;
		
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
		STEP,
		PROTECTION,
		RANGE;
		
		@Override
		public String toString() {
			return StringStyler.xmlStyleString( super.toString());
		}
	}

	private Browser browser;
	private Group grpIndication;
	private Canvas canvasSafety;
	private Canvas canvasForecast;
	private Group grpDaysForecast;
	private Combo contagionCombo;
	private Button btnSafety;
	private Label lblRisk;
	private Label lblProtected;		
	private XYGraph graph;

	private AbstractHtmlParser wizard;
	
	private Links link;
		
	private CanvasController controller;
	
	private IMobile<Date> mobile;
	private AuthenticationData authData;
	private Config config;

	private Map<IPoint,LocationData<Integer>> hubs;	
	
	private Map<Integer,Double> prediction;	

	private boolean protection;
	
	private Collection<IMobileRegistration<Date>> listeners;

	private SessionHandler session;
	
	private boolean busy;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private PaintListener safetyListener = (e)->{
		busy = true;
		GC gc = e.gc;
		try {
			Canvas canvas = (Canvas) e.getSource();
			Rectangle rect = canvas.getBounds();
			int radius = Math.min(rect.width, rect.height)/3;
			int riskRadius = radius;
			double safety = ( this.mobile == null )?100:this.mobile.getRisk();
			double amplify = ( this.mobile == null )? 1.5: 2*(1.01-safety/100);
			int safetyRadius = (int) (amplify*radius);
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
						LocationData<Integer> data = hubs.get( new Point( xpos, ypos)); 
						if( data == null )
							continue;
						IContagion contagion = new Contagion(SupportedContagion.COVID_19);
						Map<Contagion, ContagionData<Integer>> contagions = data.getContagions();
						if( Utils.assertNull(contagions))
							continue;
						Color colour = getColour(base, contagion, contagions.get(contagion).getRisk());
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
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			busy = false;
		}
		gc.dispose();
	};

	private PaintListener forecastListener = (e)->{
		busy = true;
		GC gc = e.gc;
		try {
			Canvas canvas = (Canvas) e.getSource();
			Rectangle rect = canvas.getBounds();
			Color base = getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN);
			gc.setBackground(base);
			int xStart = 10;
			int yStart = rect.height/2;
			int panning = 10;
			int scaleX = (rect.width-2*panning)/DEFAULT_HISTORY;
			int scaleY = (rect.height-2*panning)/100;
			int step = (rect.width-2*panning)/DEFAULT_HISTORY;
			gc.drawLine( xStart, panning, xStart, rect.height - panning );
			gc.drawLine( xStart, yStart, rect.width, yStart );
			for( int i=0; i<DEFAULT_HISTORY; i++ ) {
				int x = xStart + i * step;
				gc.drawLine(x, yStart-3, x, yStart+3);
			}
			gc.setForeground( base );
			if( !Utils.assertNull(prediction )) {
				Map.Entry<Integer, Double> current = null;
				for( Map.Entry<Integer, Double> entry: prediction.entrySet()) {
					int xpos = xStart+scaleX*entry.getKey();
					int ypos = (int) ((current == null )?yStart: yStart -scaleY * current.getValue());
					gc.drawLine(xpos, ypos, xpos+step, (int) (yStart + entry.getValue()));
					current = entry;
				}
			}
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			busy = false;
		}
		gc.dispose();
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
		this.contagionCombo.setItems(SupportedContagion.getItems());
		this.contagionCombo.select(SupportedContagion.COVID_19.ordinal());
		this.controller = new CanvasController( this.browser );
		this.controller.addEvaluationListener(elistener);

		this.protection = false;
		this.hubs = new TreeMap<>();
		this.prediction = new HashMap<>();
		
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
		this.btnSafety.setEnabled(this.authData !=null);
		if( mobile == null ) {
			MobileWebClient client = new MobileWebClient();
			Map<String, String> params = authData.toMap();
			client.sendGet(Requests.GET, params);
			WebClient webClient = new WebClient();
			webClient.sendGet(Requests.REGISTER, params);
		}
		return builder.toString();
	}

	protected void createPage( Composite parent, int style ) {
		super.setLayout(new GridLayout(1, false));
		super.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		browser = new Browser( this, SWT.BORDER | SWT.NO_SCROLL);
		GridData gd_browser = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_browser.heightHint = 152;
		browser.setLayoutData(gd_browser);
		browser.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		wizard = new AbstractHtmlParser(browser, MobileWizard.class) {
		
			@Override
			protected String onHandleContext(String context, String application, String service) {
				Contexts cxt = Contexts.valueOf( StringStyler.styleToEnum(service));
				String path = cxt.getPath();
				return config.getServerContext() +path;
			}
			
			@Override
			protected String onCreateLink(String id, String type, String url) {
				String result = super.onCreateLink(id, type, url);
				switch( link ) {
				case DOWNLOAD:
				case INSTALLING:
					Links ref = Links.valueOf(type.toUpperCase());
					if( Links.HEALTH.equals(ref))
						result = "#";
					break;
				default:
					break;
				}
				return result;
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
		grpDaysForecast.setText( S_FORECAST );
		grpDaysForecast.setLayout(new GridLayout(2, false));
		grpDaysForecast.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		contagionCombo = new Combo(grpDaysForecast, SWT.NONE);
		contagionCombo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		contagionCombo.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Combo combo = (Combo) e.widget;
					SupportedContagion sc = SupportedContagion.values()[ combo.getSelectionIndex()];
					if( !SupportedContagion.COVID_19.equals(sc)) {
						MessageBox messageBox = new MessageBox( getDisplay().getActiveShell());
						messageBox.setMessage( "Option " + sc.toString() + " is currently not supported in this demo.");
						combo.select(SupportedContagion.COVID_19.ordinal());
						messageBox.open();
					}
				}
				catch( Exception ex ) {
					ex.printStackTrace();
				}
				super.widgetSelected(e);
			}		
		});
		canvasForecast = new Canvas(grpDaysForecast, SWT.NONE);
		canvasForecast.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		canvasForecast.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		canvasForecast.addPaintListener(forecastListener);

		grpIndication = new Group(this, SWT.NONE);
		grpIndication.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		grpIndication.setLayout(new GridLayout(2, false));
		grpIndication.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpIndication.setText("Indication");
		grpIndication.setVisible(false);
		
		canvasSafety = new Canvas(grpIndication, SWT.NONE);
		canvasSafety.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		canvasSafety.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		canvasSafety.addPaintListener(safetyListener);
				
		btnSafety = new Button(grpIndication, SWT.CHECK);
		btnSafety.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		btnSafety.setBackground( getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		btnSafety.setForeground( getDisplay().getSystemColor(SWT.COLOR_WHITE));
		btnSafety.setText("Enable Protection");
		btnSafety.setEnabled(this.authData != null );
		btnSafety.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				boolean protect = button.getSelection();
				setProtection(protect);
				super.widgetSelected(e);
			}
		});
		
		
		lblRisk = new Label(grpIndication, SWT.NONE);
		lblRisk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblRisk.setEnabled(this.protection);
		lblRisk.setBackground( getDisplay().getSystemColor(SWT.COLOR_BLACK));
		lblRisk.setForeground( getDisplay().getSystemColor( Hubs.CENTRE.getColorCode()));
		lblRisk.setText("Risk without:");

		graph = new XYGraph(grpIndication, SWT.BORDER);
		graph.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
		graph.setEnabled(this.protection);
		graph.setBackground(getDisplay().getSystemColor( SWT.COLOR_TRANSPARENT));
		lblProtected = new Label(grpIndication, SWT.NONE);
		lblProtected.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		lblProtected.setBackground( getDisplay().getSystemColor(SWT.COLOR_BLACK));
		lblProtected.setForeground( getDisplay().getSystemColor( Hubs.PROTECTED.getColorCode()));
		lblProtected.setText("With: ");
		lblProtected.setEnabled(this.protection);		
		
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
	
	public void clear() {
		this.busy = false;
		this.graph.clear();
		this.hubs.clear();
		this.prediction.clear();
	}

	public void poll() {
		try {
			if( busy )
				return;
			WebClient client = new WebClient();
			client.addListener(session);
			Map<String, String> params = authData.toMap();
			params.put(Attributes.RADIUS.toString(), String.valueOf( DEFAULT_RADIUS ));
			params.put(Attributes.STEP.toString(), String.valueOf( DEFAULT_HISTORY+1 ));
			params.put(Attributes.RANGE.toString(), String.valueOf( DEFAULT_HISTORY+1 ));
			client.sendGet(Requests.SURROUNDINGS, params);
			client.sendGet(Requests.PREDICTION, params);
			client.sendGet(Requests.AVERAGE, params);
			client.removeListener(session);
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
	}
	
	protected Color getColour( Color colour, IContagion contagion, double value ) {
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
	
	private class XYGraph extends AbstractMultiXYGraph<Integer, Double>{
		private static final long serialVersionUID = 1L;

		public XYGraph(Composite parent, int style) {
			super(parent, style|SWT.BOTTOM);
		}
	
		@Override
		protected Color onSetForeground(GC gc, String identifier) {
			Color color = getDisplay().getSystemColor( Hubs.valueOf(identifier).getColorCode());
			return color;
		}

		@Override
		protected Color onSetBackground(GC gc, String identifier) {
			Color color = getDisplay().getSystemColor( Hubs.valueOf(identifier).getColorCode());
			return color;
		}

		@Override
		protected int onPaint(GC gc, String identifier, int xZero, int yZero, Entry<Integer, Double> prev, Entry<Integer, Double> value) {
			double scaleX = ((double)getArea().width)/DEFAULT_HISTORY;
			double scaleY = ((double)getArea().height)/100;
			int prevY = (int) ((prev == null )? 0: (int)(scaleY*prev.getValue()));
			int ypos = (int) ((value == null )? 0: (int)(scaleY*value.getValue()));
			gc.drawLine(xZero, yZero - prevY, (int)(xZero+scaleX), yZero - ypos);	
			int xpos = (value == null )? xZero: xZero + (int)(scaleX * value.getKey());
			return xpos;
		}	
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
				break;
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
			case PREDICTION:
			case SURROUNDINGS:
				break;
			default:
				super.onHandleResponseFail(status, event);
				break;
			}	
		}
	}
	
	protected void setHubs( LocationData<Integer>[] hubData ) {
		this.hubs.clear();
		if( hubData == null )
			return;
		for( LocationData<Integer> ld: hubData ) {
			if( ld == null ) {
				System.err.println("Error");
				continue;
			}
			hubs.put(ld.getPoint(), ld);
		}
		handleProtection( );
	}

	protected void setProtection( boolean protection ) {
		try {
			WebClient client = new WebClient();
			client.addListener(session);
			Map<String, String> params = authData.toMap();
			params.put(Attributes.PROTECTION.toString(), String.valueOf( protection ));
			client.sendGet(Requests.SET_PROTECTION, params);
			client.removeListener(session);
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
	}

	protected void handleProtection() {
		try {
			if(!protection)
				return;
			WebClient client = new WebClient();
			client.addListener(session);
			Map<String, String> params = authData.toMap();
			client.sendGet(Requests.PROTECTED, params);
			client.removeListener(session);
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
	}

	private class SessionHandler extends AbstractSessionHandler<ResponseEvent<Requests, StringBuilder>> implements IHttpClientListener<Requests, StringBuilder>{

		protected SessionHandler(Display display) {
			super(display);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onHandleSession(SessionEvent<ResponseEvent<Requests, StringBuilder>> sevent) {
			if( sevent.getData() == null )
				return;
			Gson gson = new Gson();
			ResponseEvent<Requests, StringBuilder> response = sevent.getData();
			switch( sevent.getData().getRequest()) {
			case GET:
				btnSafety.setEnabled(authData != null );
				break;
			case SET_PROTECTION:
				protection = Boolean.valueOf(response.getResponse());
				lblRisk.setEnabled(protection);
				lblProtected.setEnabled(protection);
				//lblWith.setEnabled(protection);
				//lblWithValue.setEnabled(protection);
				break;
			case PROTECTED:
				LocationData<Integer>[] results = gson.fromJson(response.getResponse(), LocationData[].class);
				//lblWithoutValue.setText( String.format("%3f", results[0].getRisk(contagion, timeStep)));
				//lblWithValue.setText( String.format("%3f", results[1].getRisk(contagion, timeStep)));
				break;
			case AVERAGE:
				TimelineCollection<Integer, Double> timeLine = convert( gson, response.getResponse());	
				graph.setInput(timeLine.getTimelineData());
				graph.requestLayout();
				break;
			case PREDICTION:
				prediction = convertToType(gson, response.getResponse());				
				break;
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

		@SuppressWarnings("unchecked")
		private Map<Integer, Double> convertToType( Gson gson, String str ){
			Map<Integer, Double> results = new HashMap<>();
			if( StringUtils.isEmpty(str))
				return results;
			Map<String, Double> map = gson.fromJson( str, Map.class);
			for( Map.Entry<String, Double> entry: map.entrySet()) {
				results.put( Integer.parseInt(entry.getKey()), entry.getValue());
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		private TimelineCollection<Integer, Double> convert( Gson gson, String str ){
			TimelineCollection<Integer, Double> collection = new TimelineCollection<Integer, Double>();
			if(StringUtils.isEmpty(str))
				return collection;
			TimelineCollection<String, Double> timeLine = gson.fromJson( str, TimelineCollection.class);
			for( Map.Entry<String, Map<String, Double>> entry: timeLine.getTimelineData().entrySet()) {
				TimelineData<Integer, Double> td = new TimelineData<Integer, Double>(entry.getKey());
				for( Map.Entry<String, Double> data: entry.getValue().entrySet()) {
					td.put( Integer.parseInt(data.getKey()), data.getValue());
					collection.put(entry.getKey(), td);
				}
			}
			return collection;
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