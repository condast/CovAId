package org.covaid.ui.frogger;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.condast.commons.Utils;
import org.condast.commons.auth.AuthenticationData;
import org.condast.commons.messaging.http.AbstractHttpRequest;
import org.condast.commons.messaging.http.IHttpClientListener;
import org.condast.commons.messaging.http.ResponseEvent;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.ui.session.AbstractSessionHandler;
import org.condast.commons.ui.session.SessionEvent;
import org.covaid.core.data.frogger.HubData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Point;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Slider;

public class FroggerComposite extends Composite {
	private static final long serialVersionUID = 1L;

	//public static final String S_PATH = "http://localhost:10080/covaid/rest";
	public static final String S_PATH = "http://www.condast.com:8080/covaid/mobile/rest";

	public static final int DEFAULT_WIDTH = 100;//metres
	public static final int DEFAULT_HISTORY = 16;//day, looking ahead of what is coming

	private enum Requests{

		REGISTER,
		START,
		PAUSE,
		STOP,
		CLEAR,
		GET_DAY,
		GET_HUBS, 
		UPDATE;
		
		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}
	}
	
	private enum Attributes{

		WIDTH,
		DENSITY,
		INFECTED,
		STEP;
		
		@Override
		public String toString() {
			return StringStyler.xmlStyleString( super.toString());
		}
	}
	private Group grpIndication;
	private Canvas canvas;
	private Slider sliderDensity;
	private Label lblDensityValue;
	
	private Slider sliderInfections;
	private Label lblInfectionsValue;
	private Button btnStart;
	private Button btnPause;
	private Button btnClear;

	private AuthenticationData data;
	private Map<IPoint,HubData> hubs;
	
	private boolean started;
	private boolean paused;
	
	private Timer timer;
	private TimerTask timerTask = new TimerTask(){

		@Override
		public void run() {
			try {
				if(!started || ( data == null ))
					return;
				WebClient client = new WebClient();
				client.addListener(session);
				Map<String, String> params = data.toMap();
				params.put(Attributes.STEP.toString(), String.valueOf( DEFAULT_HISTORY+1 ));
				client.sendGet(Requests.UPDATE, params);
				client.removeListener(session);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	};
	
	private SessionHandler session;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private PaintListener listener = (e)->{
		Canvas canvas = (Canvas) e.getSource();
		Rectangle rect = canvas.getBounds();
		GC gc = e.gc;
		
		//Surroundings
		int horizon = rect.height/4;
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		gc.fillRectangle(0, 0, rect.width, horizon);
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
		gc.fillRectangle(0, horizon, rect.width, rect.height);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.setLineWidth(4);
		gc.drawLine(0, horizon, rect.width, horizon);
		
		//Road
		int half = rect.width/2;
		double offset_bottom = rect.width/2;
		double offset_top = rect.width/30;
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		int[] pg = { (int) (half-offset_top), horizon, (int) (half-offset_bottom), rect.height, (int) (half+offset_bottom), rect.height, (int) (half+offset_top), horizon};
		gc.fillPolygon(pg);
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
		gc.drawLine((int)(half-offset_top), horizon, (int)(half-offset_bottom), rect.height);
		gc.drawLine((int)(half+offset_top), horizon, (int)(half+offset_bottom), rect.height);
		//gc.setLineStyle(SWT.LINE_DOT);
		
		//Fill in the hubs
		double scaleX = (offset_bottom - offset_top)/(rect.height - horizon);
		double scaleY = (rect.height - horizon)/( DEFAULT_HISTORY-1);
		if( !Utils.assertNull(hubs)) {
			Collection<HubData> test = new ArrayList<HubData>( this.hubs.values());
			for( HubData hub: test) {
				for( IPoint previous: hub.getPrevious() ) {
					draw(gc, hub, previous.clone(), 1, half, offset_top, horizon, scaleX, scaleY);
				}
			}
		}
		gc.dispose();
		requestLayout();
	};
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public FroggerComposite(Composite parent, int style) {
		super(parent, style | SWT.NO_SCROLL);
		createPage(parent, style | SWT.NO_SCROLL);
		this.started = false;
		this.paused = false;
		this.hubs = new TreeMap<>();
		session = new SessionHandler(getDisplay());
	    this.timer = new Timer(true);
	    timer.scheduleAtFixedRate(timerTask, 0, 1000);
	}

	protected void createPage( Composite parent, int style ) {
		super.setLayout(new GridLayout(1, true));
		setLayout(new GridLayout(3, false));
		grpIndication = new Group(this, SWT.BORDER);
		grpIndication.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpIndication.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		grpIndication.setText("Surroundings");

		canvas = new Canvas(grpIndication, SWT.NONE);
		canvas.addPaintListener(listener);

		Group grpPlay = new Group(this, SWT.NONE);
		grpPlay.setLayout(new GridLayout(4, false));
		grpPlay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpPlay.setText("Play");
		
		btnStart = new Button(grpPlay, SWT.NONE);
		btnStart.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Requests request = started? Requests.STOP: Requests.START;
					paused = false;
					WebClient client = new WebClient();
					Map<String, String> params = data.toMap();
					params.put( Attributes.DENSITY.toString(), String.valueOf( sliderDensity.getSelection()));
					params.put( Attributes.INFECTED.toString(), String.valueOf( sliderInfections.getSelection()));
					params.put( Attributes.WIDTH.toString(), String.valueOf( DEFAULT_WIDTH ));
					client.sendGet(request, params);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnStart.setText(Requests.START.toString());
		btnStart.setEnabled(this.data != null );
		
		btnPause = new Button(grpPlay, SWT.NONE);
		btnPause.setText("Pause");
		btnPause.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					WebClient client = new WebClient();
					Map<String, String> params = data.toMap();
					client.sendGet(Requests.PAUSE, params);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnPause.setEnabled(this.data != null );
		
		btnClear = new Button(grpPlay, SWT.NONE);
		btnClear.setText( Requests.CLEAR.toString());
		btnClear.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					WebClient client = new WebClient();
					Map<String, String> params = data.toMap();
					client.sendGet(Requests.CLEAR, params);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnClear.setEnabled(false);
		new Label(grpPlay, SWT.NONE);
		
		Group grpSettings = new Group(this, SWT.NONE);
		grpSettings.setLayout(new GridLayout(3, false));
		grpSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpSettings.setText("Population");
		
		Label lblDensity = new Label(grpSettings, SWT.NONE);
		lblDensity.setText("Density:");
		
		sliderDensity = new Slider(grpSettings, SWT.NONE);
		sliderDensity.setSelection(1);
		sliderDensity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sliderDensity.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				lblDensityValue.setText(String.valueOf( slider.getSelection()));
				super.widgetSelected(e);
			}	
		});
		lblDensityValue = new Label(grpSettings, SWT.NONE);
		lblDensityValue.setText(String.valueOf( sliderDensity.getSelection()));
		
		Label lblInfected = new Label(grpSettings, SWT.NONE);
		lblInfected.setText("Infected:");
		
		sliderInfections = new Slider(grpSettings, SWT.NONE);
		sliderInfections.setSelection(10);
		sliderInfections.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sliderInfections.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				lblInfectionsValue.setText(String.valueOf( slider.getSelection()));
				super.widgetSelected(e);
			}	
		});
		
		lblInfectionsValue = new Label(grpSettings, SWT.NONE);
		lblInfectionsValue.setText(String.valueOf( sliderInfections.getSelection()));

		Group grpControls = new Group(this, SWT.NONE);
		grpControls.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		grpControls.setLayout(new GridLayout(3, false));
		grpControls.setText("Controls");
		new Label(grpControls, SWT.NONE);

		Button btnNewButton = new Button(grpControls, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnNewButton.setText("^");
		new Label(grpControls, SWT.NONE);

		Button button_1 = new Button(grpControls, SWT.NONE);
		button_1.setText("<-");
		new Label(grpControls, SWT.NONE);

		Button button = new Button(grpControls, SWT.NONE);
		button.setText("->");
		new Label(grpControls, SWT.NONE);

		Button btnV = new Button(grpControls, SWT.NONE);
		btnV.setText("V");
		new Label(grpControls, SWT.NONE);

	}

	public void setInput( AuthenticationData data ) {
		this.data = data;
		btnStart.setEnabled(this.data != null );
		btnPause.setEnabled(this.data != null );
	}

	protected IPoint transform( IPoint hub, int half, double offset_top, int horizon, double scaleX, double scaleY ) {
		int locy = hub.getYpos();
		double width = 2 * ( offset_top + 35 * scaleX*locy);
		double xStart = half - 0.5 * width; 
		double xpos = xStart + width * hub.getXpos()/DEFAULT_WIDTH;
		double ypos = horizon + scaleY * locy;
		return new Point((int) xpos, (int)ypos );	
	}

	protected Color getColour( Color colour, IContagion<?> contagion, double value ) {
		double green = colour.getGreen() * (1f-value/100);
		return new Color( getDisplay(), colour.getRed(), (int)(green), colour.getBlue() );
	}

	protected void draw( GC gc, HubData hub, HubData previous, int step, int half, double offset_top, int horizon, double scaleX, double scaleY ) {
		if( previous == null)
			return;
		IPoint prev = transform(previous.getLocation().getPoint(), half, offset_top, horizon, scaleX, scaleY);
		IPoint next = transform(hub.getLocation().getPoint(), half, offset_top, horizon, scaleX, scaleY);
		
		Iterator<Map.Entry<Contagion, Double>> iterator = previous.getLocation().getContagions().entrySet().iterator();
		Color base = getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW);
		Color colour = base;
		gc.setForeground(colour);
		while( iterator.hasNext() ) {
			Map.Entry<Contagion, Double> entry = iterator.next();
			double cont1 = entry.getValue();
			double cont2 = hub.getLocation().getContagions().get( entry.getKey()).doubleValue();
			colour = getColour( base, entry.getKey(), cont2);
			gc.setForeground( colour);
			gc.drawLine(prev.getXpos(), prev.getYpos(), next.getXpos(), next.getYpos());
		}
		gc.drawLine(prev.getXpos(), prev.getYpos(), next.getXpos(), next.getYpos());
	}

	protected void draw( GC gc, HubData hub, IPoint previous, int step, int half, double offset_top, int horizon, double scaleX, double scaleY ) {
		if( previous == null)
			return;
		IPoint prev = transform(previous, half, offset_top, horizon, scaleX, scaleY);
		IPoint next = transform(hub.getLocation().getPoint(), half, offset_top, horizon, scaleX, scaleY);
		
		Iterator<Map.Entry<Contagion, Double>> iterator = hub.getLocation().getContagions().entrySet().iterator();
		Color base = getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW);
		Color colour = base;
		gc.setForeground(colour);
		while( iterator.hasNext() ) {
			Map.Entry<Contagion, Double> entry = iterator.next();
			double cont1 = entry.getValue();
			double cont2 = hub.getLocation().getContagions().get( entry.getKey()).doubleValue();
			colour = getColour( base, entry.getKey(), cont2);
			gc.setForeground( colour);
			gc.drawLine(prev.getXpos(), prev.getYpos(), next.getXpos(), next.getYpos());
		}
		gc.drawLine(prev.getXpos(), prev.getYpos(), next.getXpos(), next.getYpos());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void dispose() {
		session.dispose();
		super.dispose();
	}
	
	private class WebClient extends AbstractHttpRequest<Requests, StringBuilder>{
	
		public WebClient() {
			super( S_PATH );
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
			case REGISTER:
				break;
			case START:
				started = true;
				paused = false;
				btnStart.setText( Requests.STOP.toString());
				btnClear.setEnabled(true);
				break;
			case STOP:
				started = false;
				paused = false;
				btnStart.setText(Requests.START.toString());
				break;
			case PAUSE:
				paused = !paused;
				btnPause.setText(paused? "Resume": "Pause");
				break;
			case CLEAR:
				btnClear.setEnabled(false);
				break;
			case UPDATE:
				setHubs( gson.fromJson(event.getResponse(), HubData[].class));
				break;
			case GET_DAY:
				break;
			default:
				break;
			}
			return null;
		}
	}
	
	protected void setHubs( HubData[] hubData ) {
		this.hubs.clear();
		for( HubData hd: hubData ) {
			if(( hd == null ) || ( hd.getLocation() == null ) || ( hd.getLocation().getPoint() == null )) {
				System.err.println("Error");
				continue;
			}
			hubs.put(hd.getLocation().getPoint(), hd);
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
			canvas.redraw();	
		}

		@Override
		public void notifyResponse(ResponseEvent<Requests, StringBuilder> event) {
			addData(event);
		}
		
	}
}