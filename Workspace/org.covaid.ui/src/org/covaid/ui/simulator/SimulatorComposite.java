package org.covaid.ui.simulator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.condast.commons.data.util.Vector;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.ui.session.PushSession;
import org.covaid.core.config.env.Contagion;
import org.covaid.core.config.env.Contagion.SupportedContagion;
import org.covaid.core.config.env.History;
import org.covaid.core.config.env.ISimulationListener;
import org.covaid.core.config.env.Location;
import org.covaid.core.config.env.Person;
import org.covaid.core.config.env.Simulation;
import org.covaid.core.config.env.SimulationEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;

public class SimulatorComposite extends Composite {
	private static final long serialVersionUID = 1L;
			
	private static final int THOUSAND= 1000;
	private static final int MILLION= THOUSAND*THOUSAND;
	
	private enum States{
		STOP(false),
		START(true);
		
		private boolean started;
		
		private States( boolean started ) {
			this.started = started;
		}
		
		public boolean isStarted() {
			return this.started;
		}
		
		public static States toggle( States state ) {
			boolean toggle = !state.started;
			return getState( toggle );
		}
		
		public static States getState( boolean value ) {
			return value?States.START: States.STOP;
		}

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString() );
		}
	}
	
	private Canvas canvas;
	private Label horizontalMetres;
	private Label verticalMetres;
	private Label lblPopulationValue;
	private Label lblMinRiskValue;
	private Slider sliderMinRisk;
	private Label lblMaxRiskValue;
	private Slider sliderMaxRisk;
	private Button btnStartButton;
	
	private PushSession<SimulationEvent> session;
	
	private Simulation simulation;

	private Label lblDay;
	private Label lblDayValue;
	private Label lblEpidemic;
	private Combo comboContagion;

	private int counter;
	private ISimulationListener listener = (e)->{
		if( getDisplay().isDisposed())
			return;
		counter++;
		counter%=100;
		if( counter != 1 )
			return;
		getDisplay().asyncExec(()->{
			try {
				if( this.simulation != null )
					lblDayValue.setText(String.valueOf( this.simulation.getDayString(false)));
				canvas.setData(e);
				canvas.redraw();
				requestLayout();
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		});
	};

	private PaintListener paintListener = (event)->{
		try{
			updateCanvas( event.gc );
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
	};

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public SimulatorComposite(Composite parent, int style) {
		super(parent, style);
		this.createComposite(parent, style);
		this.counter = 0;
		comboContagion.setItems(Contagion.SupportedContagion.getItems());
		comboContagion.select( SupportedContagion.COVID_19.ordinal());
		session = new PushSession<SimulationEvent>();
		session.start();
	}
		
	protected void createComposite( Composite parent, int style ) {
		setLayout(new GridLayout(4, false));
		
		canvas = new Canvas(this, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		canvas.addPaintListener(paintListener);
		canvas.addListener(SWT.RESIZE, (elistener)->{ resize( canvas.getBounds()); });
		
		lblDay = new Label(this, SWT.NONE);
		lblDay.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDay.setText("Day:");
		
		lblDayValue = new Label(this, SWT.NONE);
		lblDayValue.setText("0");
		
		Button btnZoomIn = new Button(this, SWT.NONE);
		btnZoomIn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				simulation.zoomIn();
				resize(canvas.getClientArea());
				horizontalMetres.setText(String.valueOf( simulation.getLength()));
				verticalMetres.setText(String.valueOf( simulation.getWidth()));
			}
		});
		btnZoomIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnZoomIn.setText("Zoom in");
		
		Button btnZoomOut = new Button(this, SWT.NONE);
		btnZoomOut.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		btnZoomOut.setText("Zoom Out");
		btnZoomOut.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				simulation.zoomOut();
				resize(canvas.getBounds());
				horizontalMetres.setText(String.valueOf( simulation.getLength()));
				verticalMetres.setText(String.valueOf( simulation.getWidth()));
			}
		});
		
		lblEpidemic = new Label(this, SWT.NONE);
		lblEpidemic.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEpidemic.setText("Epidemic:");

		comboContagion = new Combo(this, SWT.NONE);
		comboContagion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboContagion.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				lblPopulationValue.setText( String.valueOf( slider.getSelection() ));
				super.widgetSelected(e);
			}	
		});

		horizontalMetres = new Label(this, SWT.NONE);
		horizontalMetres.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		horizontalMetres.setText("0 km");

		verticalMetres = new Label(this, SWT.NONE);
		verticalMetres.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		verticalMetres.setText("0 km");

		Label lblPopulation = new Label(this, SWT.NONE);
		lblPopulation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPopulation.setText("Population");

		Slider sliderPopulation = new Slider(this, SWT.NONE);
		sliderPopulation.setMinimum(1);
		sliderPopulation.setMaximum(20 * MILLION);
		sliderPopulation.setSelection(1);
		sliderPopulation.setIncrement(THOUSAND);
		sliderPopulation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sliderPopulation.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				lblPopulationValue.setText( String.valueOf( slider.getSelection() ));
				super.widgetSelected(e);
			}	
		});
		
		lblPopulationValue = new Label(this, SWT.NONE);
		lblPopulationValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPopulationValue.setText("0");
		new Label(this, SWT.NONE);
		
		Label lblMinRisk = new Label(this, SWT.NONE);
		lblMinRisk.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMinRisk.setText("Min. Risk");
		
		sliderMinRisk = new Slider(this, SWT.NONE);
		sliderMinRisk.setMaximum(101);
		sliderMinRisk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sliderMinRisk.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				sliderMaxRisk.setMinimum(slider.getSelection());	
				if( sliderMaxRisk.getSelection() < slider.getSelection())
					sliderMaxRisk.setSelection(slider.getSelection());
				lblMinRiskValue.setText( String.valueOf( slider.getSelection()));
				super.widgetSelected(e);
			}	
		});
		
		lblMinRiskValue = new Label(this, SWT.NONE);
		lblMinRiskValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMinRiskValue.setText("0");
		new Label(this, SWT.NONE);
		
		Label lblMaxRisk = new Label(this, SWT.NONE);
		lblMaxRisk.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMaxRisk.setText("Max. Risk");
		
		sliderMaxRisk = new Slider(this, SWT.NONE);
		sliderMaxRisk.setMaximum(101);
		sliderMaxRisk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sliderMaxRisk.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				sliderMinRisk.setMaximum(slider.getSelection());	
				if( sliderMinRisk.getSelection() > slider.getSelection())
					sliderMinRisk.setSelection(slider.getSelection());
				lblMaxRiskValue.setText( String.valueOf( slider.getSelection()));
				super.widgetSelected(e);
			}	
		});
		
		lblMaxRiskValue = new Label(this, SWT.NONE);
		lblMaxRiskValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMaxRiskValue.setText("100");
		new Label(this, SWT.NONE);
		
		Button btnCovaid = new Button(this, SWT.CHECK);
		btnCovaid.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnCovaid.setText("CovAID");
		new Label(this, SWT.NONE);
		
		Button btnClear = new Button(this, SWT.NONE);
		btnClear.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		btnClear.setText("Clear");
		
		btnStartButton = new Button(this, SWT.NONE);
		btnStartButton.setData(States.STOP);
		btnStartButton.setText(States.getState(!States.STOP.isStarted()).toString());
		btnStartButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Button button = (Button) e.widget;
					States state = (States) button.getData();
					btnStartButton.setText( state.toString());
					state = States.toggle( state);
					btnStartButton.setData( state );
					switch( state ) {
					case START:
						comboContagion.setEnabled(false);
						simulation.init( 1);//sliderPopulation.getSelection());
						simulation.start();
						break;
					case STOP:
						comboContagion.setEnabled(true);
						simulation.stop();
						break;
					default:
						break;
					}
				}
				catch( Exception ex ) {
					ex.printStackTrace();
				}
			}
		});
		btnStartButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
	}

	public void setInput(Simulation simulation) {
		if( this.simulation != null ) {
			this.simulation.removeListener(listener);
		}
		this.simulation = simulation;
		if( this.simulation != null ) {
			this.simulation.addListener(listener);
			Point point = canvas.computeSize(this.simulation.getLength(), this.simulation.getWidth());
			resize( new Rectangle( 0, 0, point.x, point.y));
			lblDayValue.setText(String.valueOf( this.simulation.getDays()));
			comboContagion.select( SupportedContagion.valueOf(simulation.getContagion().getIdentifier()).ordinal());
		}
		canvas.requestLayout();
	}

	public Color getColour( double contagion ) {
		Device device = getDisplay();
		double scale = contagion * 2.5;
		Color colour = new Color (device, (int)(scale), 0, (int)(255-scale));
		return colour;
	}
	
	protected void updateCanvas( GC gc ) {
		try {
			SimulationEvent event = (SimulationEvent) canvas.getData();
			if( event == null )
				return;
			Person person = event.getPerson();
			Contagion contagion = this.simulation.getContagion();
			Date date = simulation.getDate();
			
			double safety = person.getSafetyBubble(contagion, Calendar.getInstance().getTime());

			Map<Date, Location> history = person.getHistory().get();
			Iterator<Map.Entry<Date, Location>> iterator = new ArrayList<Map.Entry<Date, Location>>( history.entrySet()).iterator();
			while( iterator.hasNext() ) {
				Map.Entry<Date, Location> entry = iterator.next();
				double cont = History.getContagion(contagion, person.getLocation(), date, entry);
				Vector<Double,Double> vector = contagion.getContagion(date);
				gc.setBackground( getColour( vector.getKey().doubleValue() ));
				gc.fillOval(entry.getValue().getXpos(), entry.getValue().getYpos(), 
						scaleX( vector.getValue().doubleValue()), scaleY( vector.getValue().doubleValue()));
			}

			gc.setBackground(getDisplay().getSystemColor( SWT.COLOR_DARK_BLUE));
			gc.fillOval(person.getLocation().getXpos(), person.getLocation().getYpos(), scaleX( safety), scaleY( safety));
			gc.dispose();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
	}

	protected int scaleX( double size ) {
		if( simulation == null )
			return 0;
		return (int)canvas.getBounds().width/simulation.getLength();
	}

	protected int scaleY( double size ) {
		if( simulation == null )
			return 0;
		return (int)canvas.getBounds().height/simulation.getWidth();
	}

	protected void resize( Rectangle rectangle ) {
		if(( simulation == null ) || ( rectangle.height == 0 ) || (rectangle.width == 0 ))
			return;
		float scale = (float)rectangle.height/rectangle.width;
		simulation.setWidth( (int) (simulation.getLength()*scale));		
		horizontalMetres.setText(String.valueOf( simulation.getLength()));
		verticalMetres.setText(String.valueOf( simulation.getWidth()));
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void dispose() {
		canvas.removePaintListener(paintListener);
		this.session.stop();
		this.simulation.dispose();
	}
}
