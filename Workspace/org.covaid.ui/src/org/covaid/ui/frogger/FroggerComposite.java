package org.covaid.ui.frogger;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.condast.commons.auth.AuthenticationData;
import org.condast.commons.messaging.http.AbstractHttpRequest;
import org.condast.commons.messaging.http.ResponseEvent;
import org.condast.commons.strings.StringStyler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
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

	public static final String S_PATH = "http://localhost:10080/covaid/rest";
	//public static final String S_PATH = "http://www.condast.com:8080/covaid/mobile/rest";

	private enum Requests{

		REGISTER,
		START,
		PAUSE,
		STOP,
		GET_DAY,
		GET_HUBS;
		
		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}
	}
	
	private Group grpIndication;
	private Canvas canvas;
	private Button btnStart;
	private Button btnPause;

	private AuthenticationData data;
	
	private boolean started;
	
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
		int offset_bottom = rect.width/2;
		int offset_top = rect.width/30;
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		int[] pg = { half-offset_top, horizon, half-offset_bottom, rect.height, half+offset_bottom, rect.height, half+offset_top, horizon};
		gc.fillPolygon(pg);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
		gc.drawLine(half-offset_top, horizon, half-offset_bottom, rect.height);
		gc.drawLine(half+offset_top, horizon, half+offset_bottom, rect.height);
		//gc.setLineStyle(SWT.LINE_DOT);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
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
					WebClient client = new WebClient();
					Map<String, String> params = data.toMap();
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
		
		Button btnClear = new Button(grpPlay, SWT.NONE);
		btnClear.setText("Clear");
		new Label(grpPlay, SWT.NONE);
		
		Group grpSettings = new Group(this, SWT.NONE);
		grpSettings.setLayout(new GridLayout(3, false));
		grpSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpSettings.setText("Population");
		
		Label lblDensity = new Label(grpSettings, SWT.NONE);
		lblDensity.setText("Density:");
		
		Slider slider = new Slider(grpSettings, SWT.NONE);
		slider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblDensityValue = new Label(grpSettings, SWT.NONE);
		lblDensityValue.setText("0");
		
		Label lblInfected = new Label(grpSettings, SWT.NONE);
		lblInfected.setText("Infected:");
		
		Slider slider_1 = new Slider(grpSettings, SWT.NONE);
		slider_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblInfectedValue = new Label(grpSettings, SWT.NONE);
		lblInfectedValue.setText("0");
		
				Group grpControls = new Group(this, SWT.NONE);
				grpControls.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
				grpControls.setLayout(new GridLayout(3, false));
				grpControls.setText("Controls");
				new Label(grpControls, SWT.NONE);
				
				Button btnNewButton = new Button(grpControls, SWT.NONE);
				btnNewButton.addSelectionListener(new SelectionAdapter() {
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
	
	protected void createPage( Composite parent, int style ) {
		super.setLayout(new GridLayout(1, true));
		setLayout(new GridLayout(3, false));
		grpIndication = new Group(this, SWT.BORDER);
		grpIndication.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpIndication.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		grpIndication.setText("Surroundings");
		
		canvas = new Canvas(grpIndication, SWT.NONE);
		canvas.addPaintListener(listener);
	}

	public void setInput( AuthenticationData data ) {
		this.data = data;
		btnStart.setEnabled(this.data != null );
		btnPause.setEnabled(this.data != null );
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void dispose() {
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
				btnStart.setText( Requests.STOP.toString());
				break;
			case STOP:
				started = false;
				btnStart.setText(Requests.START.toString());
				break;
			case GET_DAY:
				break;
			default:
				break;
			}
			canvas.redraw();
			return null;
		}
	}
}