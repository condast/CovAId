package org.covaid.ui.frogger;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.condast.commons.Utils;
import org.condast.commons.config.Config;
import org.condast.commons.messaging.http.AbstractHttpRequest;
import org.condast.commons.messaging.http.IHttpClientListener;
import org.condast.commons.messaging.http.ResponseEvent;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.ui.session.AbstractSessionHandler;
import org.condast.commons.ui.session.SessionEvent;
import org.condast.js.commons.utils.AuthenticationData;
import org.covaid.core.data.ContagionData;
import org.covaid.core.data.frogger.HubData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Point;
import org.covaid.ui.mobile.IUpdateListener;
import org.covaid.ui.mobile.UpdateEvent;
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

	public static final String S_COVAID_CONTEXT = "covaid/rest";

	public static final int DEFAULT_WIDTH = 100;//metres
	public static final int DEFAULT_HISTORY = 16;//day, looking ahead of what is coming

	public static final int DEFAULT_TEST_TIME = 60;//seconds. After this the simulation will stop

	private enum Requests{

		REGISTER,
		START,
		PAUSE,
		STOP,
		CLEAR,
		SET_INFECTED,
		SET_DENSITY,
		GET_DAY,
		GET_HUBS, 
		UPDATE;
		
		@Override
		public String toString() {
			return StringStyler.xmlStyleString( super.toString());
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
	
	private Collection<IUpdateListener> listeners;
	
	private boolean started;
	private boolean paused;

	private int timeStep;
	
	private Config config;
	private WebClient client;
	
	private SessionHandler session;
	
	private boolean busy;
	
	private PaintListener listener = (e)->{
		this.busy = true;
		GC gc = e.gc;
		try {
			Canvas canvas = (Canvas) e.getSource();
			Rectangle rect = canvas.getBounds();

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

			int xpos = -10;
			int span = 2;
			double scaleX = (offset_bottom - offset_top)/(rect.height - horizon);
			double scaleY = (rect.height - horizon)/( DEFAULT_HISTORY-1);

			//Hectometer signs
			for( int i=0; i< DEFAULT_HISTORY/span; i++ ) {
				String text = String.valueOf( DEFAULT_HISTORY - i*span ) + " days";
				IPoint point = transformHect( new Point(text, xpos, i*span), half, offset_top, horizon, scaleX, scaleY); 
				drawHectometerSign(gc, point, 70 );
			}

			//Fill in the hubs
			if( !Utils.assertNull(hubs)) {
				Collection<HubData> test = new ArrayList<HubData>( this.hubs.values());
				for( HubData hub: test) {
					timeStep = hub.getMoment();
					for( IPoint previous: hub.getPrevious() ) {
						draw(gc, hub, previous.clone(), 1, half, offset_top, horizon, scaleX, scaleY);
					}
				}
			}
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
		finally {
			busy = false;
			gc.dispose();
		}
	};

	private Label lblDays;
	private Label lbldayValue;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public FroggerComposite(Composite parent, int style) {
		super(parent, style | SWT.NO_SCROLL);
		config = new Config();
		createPage(parent, style | SWT.NO_SCROLL);
		this.started = false;
		this.paused = false;
		this.timeStep = 0;
		this.hubs = new TreeMap<>();
		this.listeners = new ArrayList<>();
		new Label(this, SWT.NONE);
		client = new WebClient();
		session = new SessionHandler(getDisplay());
		client.addListener( session );
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
		grpPlay.setLayout(new GridLayout(5, false));
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
					if(!started)
						timeStep = 0;
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
		btnStart.setEnabled( this.data != null );
		
		btnPause = new Button(grpPlay, SWT.NONE);
		btnPause.setText("Pause");
		btnPause.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Map<String, String> params = data.toMap();
					client.sendGet(Requests.PAUSE, params);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnPause.setEnabled(false );
		
		btnClear = new Button(grpPlay, SWT.NONE);
		btnClear.setText( Requests.CLEAR.toString());
		btnClear.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					timeStep = 0;
					Map<String, String> params = data.toMap();
					client.sendGet(Requests.CLEAR, params);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnClear.setEnabled(false);
		
		lblDays = new Label(grpPlay, SWT.NONE);
		lblDays.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		lblDays.setText("Days:");
		
		lbldayValue = new Label(grpPlay, SWT.NONE);
		lbldayValue.setText( String.format("%3d", timeStep));
		lbldayValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
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
				try {
					Slider slider = (Slider) e.widget;
					lblDensityValue.setText(String.valueOf( slider.getSelection()));
					Map<String, String> params = data.toMap();
					params.put(Attributes.DENSITY.toString(), String.valueOf( slider.getSelection()));
					client.sendGet(Requests.SET_DENSITY, params);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				super.widgetSelected(e);
			}	
		});
		lblDensityValue = new Label(grpSettings, SWT.NONE);
		lblDensityValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
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
				try {
					Slider slider = (Slider) e.widget;
					Map<String, String> params = data.toMap();
					params.put(Attributes.INFECTED.toString(), String.valueOf( slider.getSelection()));
					client.sendGet(Requests.SET_INFECTED, params);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				super.widgetSelected(e);
			}	
		});

		lblInfectionsValue = new Label(grpSettings, SWT.NONE);
		lblInfectionsValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblInfectionsValue.setText(String.valueOf( sliderInfections.getSelection()));
	}

	public void addUpdateListener( IUpdateListener listener ) {
		this.listeners.add(listener);
	}

	public void removeUpdateListener( IUpdateListener listener ) {
		this.listeners.remove(listener);
	}
	
	protected void notifyListers( UpdateEvent event ) {
		for( IUpdateListener listener: this.listeners)
			listener.notifyViewUpdated(event);
	}

	public void setInput( AuthenticationData data ) {
		this.data = data;
		btnStart.setEnabled(this.data != null );
		btnPause.setEnabled(this.data != null );
	}

	public void poll(){
		try {
			if(!started || ( data == null ))
				return;
			client.addListener(session);
			Map<String, String> params = data.toMap();
			params.put(Attributes.STEP.toString(), String.valueOf( DEFAULT_HISTORY+1 ));
			if( timeStep > DEFAULT_TEST_TIME) {
				client.sendGet(Requests.STOP, params);
				timeStep = 0;
			}else
				client.sendGet(Requests.UPDATE, params);
			client.removeListener(session);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public boolean isStarted() {
		return started;
	}

	public boolean isPaused() {
		return paused;
	}

	protected IPoint transform( IPoint hub, int half, double offset_top, int horizon, double scaleX, double scaleY ) {
		int locy = hub.getYpos();
		double width = 2 * ( offset_top + 35 * scaleX*locy);
		double xStart = half - 0.5 * width; 
		double xpos = xStart + width * hub.getXpos()/DEFAULT_WIDTH;
		double ypos = horizon + scaleY * locy;
		return new Point((int) xpos, (int)ypos );	
	}

	/**
	 * A yellow colour that is slightly darker than the road
	 * @param colour
	 * @param contagion
	 * @param value
	 * @return
	 */
	protected Color getBaseColour(  ) {
		return new Color( getDisplay(), 227, 199, 0 );
	}

	protected Color getColour( Color colour, IContagion contagion, double value ) {
		double green = colour.getGreen() * (1f-value/100);
		return new Color( getDisplay(), colour.getRed(), (int)(green), colour.getBlue() );
	}

	protected void draw( GC gc, HubData hub, HubData previous, int step, int half, double offset_top, int horizon, double scaleX, double scaleY ) {
		if( previous == null)
			return;
		IPoint prev = transform(previous.getLocation().getPoint(), half, offset_top, horizon, scaleX, scaleY);
		IPoint next = transform(hub.getLocation().getPoint(), half, offset_top, horizon, scaleX, scaleY);
		
		Iterator<Map.Entry<Contagion, ContagionData<Integer>>> iterator = previous.getLocation().getContagions().entrySet().iterator();
		Color base = getBaseColour();
		Color colour = base;
		gc.setForeground(colour);
		while( iterator.hasNext() ) {
			Map.Entry<Contagion, ContagionData<Integer>> entry = iterator.next();
			double cont2 = hub.getLocation().getContagions().get( entry.getKey()).getMoment();
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
		
		Iterator<Map.Entry<Contagion, ContagionData<Integer>>> iterator = hub.getLocation().getContagions().entrySet().iterator();
		Color base = getBaseColour();
		Color colour = base;
		gc.setForeground(colour);
		while( iterator.hasNext() ) {
			Map.Entry<Contagion, ContagionData<Integer>> entry = iterator.next();
			ContagionData<Integer> data = hub.getLocation().getContagions().get( entry.getKey()); 
			double cont2 = (data == null )? 0: (data.getMoment() == null )?0: data.getMoment();
			cont2 = (cont2)<0?0:cont2>100?100:cont2;
			colour = getColour( base, entry.getKey(), cont2);
			gc.setForeground( colour);
			gc.drawLine(prev.getXpos(), prev.getYpos(), next.getXpos(), next.getYpos());
		}
		gc.drawLine(prev.getXpos(), prev.getYpos(), next.getXpos(), next.getYpos());
	}

	protected IPoint transformHect( IPoint point, int half, double offset_top, int horizon, double scaleX, double scaleY ) {
		int locy = point.getYpos();
		double width = 2 * ( offset_top + 32 * scaleX*locy) + 100;
		double xStart = half - 0.5 * width; 
		double xpos = xStart + width * point.getXpos()/DEFAULT_WIDTH;
		double ypos = horizon + scaleY * locy;
		return new Point( point.getIdentifier(), (int) xpos, (int)ypos );	
	}

	protected void drawHectometerSign( GC gc, IPoint point, int size ) {
		int correction = 50* size/point.getYpos();
		int xpos = point.getXpos();
		int ydraw = point.getYpos()-size+10+correction;
		int width = size;
		int height = size/2;
		
		gc.setForeground( getDisplay().getSystemColor( SWT.COLOR_DARK_GREEN));
		gc.fillRoundRectangle(xpos, ydraw, width, height, 30, 30);
		gc.setLineWidth(1);
		gc.setForeground( getDisplay().getSystemColor( SWT.COLOR_BLACK));
		gc.drawRoundRectangle(xpos, ydraw, width, height, 30, 30);
		gc.setLineWidth(2);
		gc.setForeground( getDisplay().getSystemColor( SWT.COLOR_WHITE));
		gc.drawRoundRectangle(xpos+2, ydraw+2, width-4, height-4, 30, 30);
		gc.setLineWidth(10);
		gc.drawText(point.getIdentifier(), xpos+width/2-28, ydraw+10);
		gc.setForeground( getDisplay().getSystemColor( SWT.COLOR_GRAY));
		
		int pole = 2 * height - correction;
		gc.drawLine(xpos+2+width/2, ydraw+height, xpos+2+width/2, ydraw+pole);
		gc.setLineWidth(1);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void dispose() {
		client.removeListener(session);
		session.dispose();
		super.dispose();
	}
	
	private class WebClient extends AbstractHttpRequest<Requests, StringBuilder>{
	
		public WebClient() {
			super( config.getServerContext() + S_COVAID_CONTEXT );
		}

		protected void sendGet(Requests request, Map<String, String> parameters ) throws IOException {
			super.sendGet(request, parameters, null);
		}

		@Override
		protected void sendGet(Requests request, Map<String, String> parameters, StringBuilder data) throws IOException {
			super.sendGet(request, parameters, data);
		}
	
		@Override
		protected String onHandleResponse(ResponseEvent<Requests, StringBuilder> event, StringBuilder data)
				throws IOException {
			return event.getResponse();
		}
	}
	
	protected void setHubs( HubData[] hubData ) {
		this.hubs.clear();
		if( hubData == null )
			return;
		for( HubData hd: hubData ) {
			if(( hd == null ) || ( hd.getLocation() == null ) || ( hd.getLocation().getPoint() == null )) {
				System.err.println("Error");
				continue;
			}
			hubs.put(hd.getLocation().getPoint(), hd);
			timeStep = hd.getLocation().getPoint().getYpos();
		}
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
			Requests request = sevent.getData().getRequest(); 
			switch( request) {
			case REGISTER:
				break;
			case START:
				started = true;
				paused = false;
				btnStart.setText( Requests.STOP.toString());
				btnPause.setEnabled(true );
				btnClear.setEnabled(false);
				break;
			case STOP:
				started = false;
				paused = false;
				btnStart.setText(Requests.START.toString());
				btnPause.setEnabled(false );
				btnClear.setEnabled(true);
				break;
			case PAUSE:
				paused = !paused;
				btnPause.setText(paused? "Resume": "Pause");
				break;
			case CLEAR:
				btnClear.setEnabled(false);
				hubs.clear();
				timeStep = 0;
				lbldayValue.setText( String.format("%3d", timeStep));
				break;
			case SET_INFECTED:
				lblInfectionsValue.setText(String.valueOf( sliderInfections.getSelection()));
				break;
			case UPDATE:
				setHubs( gson.fromJson(response.getResponse(), HubData[].class));
				break;
			case GET_DAY:
				break;
			default:
				break;
			}
			notifyListers( new UpdateEvent( this, request.name() ));
			if(!busy )
				canvas.redraw();	
			lbldayValue.setText( String.format("%4d", timeStep));
			requestLayout();
		}

		@Override
		public void notifyResponse(ResponseEvent<Requests, StringBuilder> event) {
			addData(event);
		}		
	}
}