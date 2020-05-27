package org.covaid.ui.simulator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.condast.commons.data.plane.Field;
import org.condast.commons.data.plane.IField;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.ui.session.PushSession;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IFieldEnvironment;
import org.covaid.core.environment.DomainEvent;
import org.covaid.core.environment.IDomain;
import org.covaid.core.environment.IEnvironmentListener;
import org.covaid.core.environment.field.CovaidDomain;
import org.covaid.core.environment.field.IFieldDomain;
import org.covaid.core.environment.field.RawDomain;
import org.covaid.core.model.Contagion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;

public class SimulatorComposite extends Composite {
	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_POPULATION = 200;
	private static final int THOUSAND= 1000;

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

	private Map<String, DomainComposite> canvases;
	private GraphCanvas graphCanvas;

	private Label horizontalMetres;
	private Label verticalMetres;
	private Label lblPopulationValue;
	private Label lblSafetyValue;
	private Slider sliderSafety;
	private Label lblMaxRiskValue;
	private Slider sliderRisk;
	private Button btnStartButton;

	private PushSession<DomainEvent<Date>> session;

	private IFieldEnvironment environment;

	private Label lblDay;
	private Label lblDayValue;
	private Combo comboContagion;

	private IEnvironmentListener<Date> covaidListener = (e)->{
		if( getDisplay().isDisposed())
			return;
		
		getDisplay().asyncExec(()->{
			try {
				if( environment != null )
					lblDayValue.setText(String.valueOf( environment.getDayString(false)));
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		});
	};

	private Label lblMinRisk;
	private Label lblMaxSafety;
	private Group epidemicGroup;
	private Group settingsGroup;
	private Label lblLength;
	private Label lblWidth;
	private Group grpControl;
	private Label lblContagiousness;
	private Label lblContagiousnessValue;
	private Label lblDistance;
	private Label lblDistanceValue;
	private Label lblIncubation;
	private Label lblIncubationValue;
	private Label lblDispersion;
	private Label lblDispersionValue;
	private Label lblHalfTime;
	private Label lblHalfTimeValue;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public SimulatorComposite(Composite parent, int style) {
		super(parent, style);
		this.canvases = new HashMap<>();
		this.createComposite(parent, style);
		session = new PushSession<DomainEvent<Date>>();
		session.start();
	}

	protected void createComposite( Composite parent, int style ) {
		setLayout(new GridLayout(3, false));

		Composite composite = new Composite(this, SWT.BORDER);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		composite.setLayout(new GridLayout(3, false));

		Group rawGroup = new Group(composite, SWT.BORDER);
		rawGroup.setLayout(new FillLayout());
		rawGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		rawGroup.setText(RawDomain.NAME);
		DomainComposite canvas = new DomainComposite(rawGroup, SWT.BORDER);
		canvases.put(RawDomain.NAME, canvas);

		Group covaidGroup = new Group(composite, SWT.BORDER);
		covaidGroup.setLayout(new GridLayout(4, false));
		covaidGroup.setText(CovaidDomain.NAME);
		covaidGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		canvas = new DomainComposite(covaidGroup, SWT.BORDER);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		canvases.put(CovaidDomain.NAME, canvas);

		Label lblSafety = new Label(covaidGroup, SWT.NONE);
		lblSafety.setText("Safety");

		lblSafetyValue = new Label(covaidGroup, SWT.NONE);
		GridData gd_lblSafetyValue = new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1);
		lblSafetyValue.setLayoutData(gd_lblSafetyValue);
		lblSafetyValue.setText("0%");

		sliderSafety = new Slider(covaidGroup, SWT.NONE);
		sliderSafety.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sliderSafety.setMaximum(101);
		sliderSafety.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				sliderRisk.setMaximum( 101 - slider.getSelection());	
				if( sliderRisk.getSelection() < slider.getSelection())
					sliderRisk.setSelection(slider.getSelection());
				lblMaxRiskValue.setText( 101 - slider.getSelection() + "%" );
				super.widgetSelected(e);
			}	
		});

		lblMaxSafety = new Label(covaidGroup, SWT.NONE);
		lblMaxSafety.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMaxSafety.setText("100%");

		Label lblRisk = new Label(covaidGroup, SWT.NONE);
		lblRisk.setText("Risk");

		lblMinRisk = new Label(covaidGroup, SWT.NONE);
		lblMinRisk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		lblMinRisk.setText("0");

		sliderRisk = new Slider(covaidGroup, SWT.NONE);
		sliderRisk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		sliderRisk.setMaximum(101);
		sliderRisk.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				sliderSafety.setMaximum(slider.getSelection());	
				if( sliderSafety.getSelection() > slider.getSelection())
					sliderSafety.setSelection(slider.getSelection());
				lblMaxSafety.setText( slider.getSelection() + "%");
				super.widgetSelected(e);
			}	
		});

		lblMaxRiskValue = new Label(covaidGroup, SWT.NONE);
		lblMaxRiskValue.setText("100%");

		Group graphGroup = new Group(composite, SWT.BORDER);
		graphGroup.setLayout(new GridLayout(1, false));
		GridData gd_graphGroup = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_graphGroup.widthHint = 308;
		graphGroup.setLayoutData(gd_graphGroup);
		graphGroup.setText("Infection");
		graphCanvas = new GraphCanvas(graphGroup, SWT.NONE);
		graphCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		epidemicGroup = new Group(this, SWT.NONE);
		epidemicGroup.setText("Epidemic");
		epidemicGroup.setLayout(new GridLayout(3, false));
		epidemicGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		comboContagion = new Combo(epidemicGroup, SWT.NONE);
		comboContagion.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		comboContagion.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				Combo combo = (Combo) e.widget;
				IContagion contagion = new Contagion( IContagion.SupportedContagion.valueOf( combo.getText()), 100);
				lblContagiousnessValue.setText( String.format("%.2f", contagion.getContagiousness()) + "%");
				lblDistanceValue.setText( contagion.getDistance() + " m" );
				lblIncubationValue.setText( contagion.getIncubation() + " days" );
				lblDispersionValue.setText( String.format("%.2f", contagion.getDispersion()) + " m"); 
				lblHalfTimeValue.setText( contagion.getHalfTime() + " days"); 
				super.widgetSelected(e);
			}	

			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo) e.widget;
				IContagion contagion = new Contagion( IContagion.SupportedContagion.valueOf( combo.getText()), 100);
				lblContagiousnessValue.setText( String.format("%.2f", contagion.getContagiousness()) + "%");
				lblDistanceValue.setText( contagion.getDistance() + " m" );
				lblIncubationValue.setText( contagion.getIncubation() + " days" );
				lblDispersionValue.setText( String.format("%.2f", contagion.getDispersion()) + " m"); 
				lblHalfTimeValue.setText( contagion.getHalfTime() + " days"); 
				super.widgetSelected(e);
			}	
		});
		comboContagion.setItems(IContagion.SupportedContagion.getItems());
		comboContagion.select( IContagion.SupportedContagion.COVID_19.ordinal());
		
		lblContagiousness = new Label(epidemicGroup, SWT.NONE);
		lblContagiousness.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblContagiousness.setText("Contagiousness:");
		
		lblContagiousnessValue = new Label(epidemicGroup, SWT.NONE);
		lblContagiousnessValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblContagiousnessValue.setText("New Label");
		new Label(epidemicGroup, SWT.NONE);
		
		lblDistance = new Label(epidemicGroup, SWT.NONE);
		lblDistance.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDistance.setText("Distance:");
		
		lblDistanceValue = new Label(epidemicGroup, SWT.NONE);
		lblDistanceValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblDistanceValue.setText("New Label");
		new Label(epidemicGroup, SWT.NONE);
		
		lblIncubation = new Label(epidemicGroup, SWT.NONE);
		lblIncubation.setText("Incubation period:");
		
		lblIncubationValue = new Label(epidemicGroup, SWT.NONE);
		lblIncubationValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblIncubationValue.setText("New Label");
		new Label(epidemicGroup, SWT.NONE);
		
		lblDispersion = new Label(epidemicGroup, SWT.NONE);
		lblDispersion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDispersion.setText("Dispersion:");
		
		lblDispersionValue = new Label(epidemicGroup, SWT.NONE);
		lblDispersionValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblDispersionValue.setText("New Label");
		new Label(epidemicGroup, SWT.NONE);
		
		lblHalfTime = new Label(epidemicGroup, SWT.NONE);
		lblHalfTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHalfTime.setText("Half Time:");
		
		lblHalfTimeValue = new Label(epidemicGroup, SWT.NONE);
		lblHalfTimeValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblHalfTimeValue.setText("New Label");

		settingsGroup = new Group(this, SWT.NONE);
		settingsGroup.setText("Environment");
		settingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		settingsGroup.setLayout(new GridLayout(5, false));

		lblLength = new Label(settingsGroup, SWT.NONE);
		lblLength.setText("Length:");

		horizontalMetres = new Label(settingsGroup, SWT.NONE);
		horizontalMetres.setText("0 km");

		lblWidth = new Label(settingsGroup, SWT.NONE);
		lblWidth.setText("width");

		verticalMetres = new Label(settingsGroup, SWT.NONE);
		verticalMetres.setText("0 km");

		Button btnZoomIn = new Button(settingsGroup, SWT.NONE);
		btnZoomIn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnZoomIn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				environment.zoomIn();
				for( DomainComposite canvas: canvases.values())
					resize( canvas.getBounds());
				IField field = environment.getField();
				horizontalMetres.setText( getAreaText( field.getLength() ));
				verticalMetres.setText( getAreaText( field.getWidth() ));
			}
		});
		btnZoomIn.setText("Zoom in");

		Label lblPopulation = new Label(settingsGroup, SWT.NONE);
		lblPopulation.setText("Population");

		Slider sliderPopulation = new Slider(settingsGroup, SWT.NONE);
		sliderPopulation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		sliderPopulation.setMinimum(1);
		sliderPopulation.setMaximum(100 * THOUSAND);
		sliderPopulation.setSelection(DEFAULT_POPULATION);
		sliderPopulation.setIncrement(1);
		sliderPopulation.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				lblPopulationValue.setText( String.valueOf( slider.getSelection() ));
				super.widgetSelected(e);
			}	
		});

		lblPopulationValue = new Label(settingsGroup, SWT.NONE);
		lblPopulationValue.setText(String.valueOf( sliderPopulation.getSelection()));

		Button btnZoomOut = new Button(settingsGroup, SWT.NONE);
		btnZoomOut.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnZoomOut.setText("Zoom Out");
		btnZoomOut.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				environment.zoomOut();
				for( DomainComposite canvas: canvases.values())
					resize( canvas.getBounds());
				IField field = environment.getField();
				horizontalMetres.setText( getAreaText( field.getLength() ));
				verticalMetres.setText( getAreaText( field.getWidth() ));
			}
		});

		grpControl = new Group(this, SWT.NONE);
		grpControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpControl.setText("Control");
		grpControl.setLayout(new GridLayout(2, false));

		lblDay = new Label(grpControl, SWT.NONE);
		lblDay.setText("Day:");

		lblDayValue = new Label(grpControl, SWT.NONE);
		lblDayValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblDayValue.setText("0");

		Button btnClear = new Button(grpControl, SWT.NONE);
		btnClear.setText("Clear");
		btnClear.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				graphCanvas.clear();
				super.widgetSelected(e);
			}
		});
		
		btnStartButton = new Button(grpControl, SWT.NONE);
		btnStartButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
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
						environment.init( sliderPopulation.getSelection());
						environment.start();
						break;
					case STOP:
						comboContagion.setEnabled(true);
						environment.stop();
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
	}


	public void setInput( IFieldEnvironment environment) {
		if( this.environment != null ) {
				environment.removeListener(covaidListener);
			this.graphCanvas.removeInput(environment);
		}
		this.environment = environment;
		if( this.environment != null ){
			this.environment.addListener(covaidListener);
			IField field = environment.getField();
			for( IDomain<Date> domain: environment.getDomains()) {
				DomainComposite canvas = canvases.get( domain.getName());
				canvas.setInput((IFieldDomain) domain);
				Point point = canvas.computeSize((int)field.getLength(), (int)field.getWidth());
				canvas.resize( new Rectangle( 0, 0, point.x, point.y));
			}
			this.graphCanvas.addInput(environment);
			horizontalMetres.setText( getAreaText( field.getLength() ));
			verticalMetres.setText( getAreaText( field.getWidth() ));
			lblPopulationValue.setText(String.valueOf(environment.getPopulation()));
			lblDayValue.setText(String.valueOf( environment.getDays()));
			comboContagion.select( IContagion.SupportedContagion.valueOf( environment.getContagion()).ordinal());
			IContagion contagion = new Contagion( IContagion.SupportedContagion.valueOf( comboContagion.getText()), 100);
			lblContagiousnessValue.setText( String.format("%.2f", contagion.getContagiousness()) + "%");
			lblDistanceValue.setText( contagion.getDistance() + " m" );
			lblIncubationValue.setText( contagion.getIncubation() + " days" );
			lblDispersionValue.setText( String.format("%.2f", contagion.getDispersion()) + " m"); 
			lblHalfTimeValue.setText( contagion.getHalfTime() + " days"); 
		}
		requestLayout();
	}

	public Color getColour( double contagion ) {
		Device device = getDisplay();
		double scale = contagion * 2.5;
		Color colour = new Color (device, (int)(scale), 0, (int)(255-scale));
		return colour;
	}

	protected void resize( Rectangle rectangle ) {
		if(( environment == null ) || ( rectangle.height == 0 ) || (rectangle.width == 0 ))
			return;
		IField field = environment.getField();
		float scale = (float)rectangle.height/rectangle.width;
		field = new Field( field.getCoordinates(), field.getLength(),  (int) (field.getLength()*scale ));
		environment.setField(field);
		horizontalMetres.setText(String.valueOf( field.getLength()));
		verticalMetres.setText(String.valueOf( field.getWidth()));
	}

	private String getAreaText( long length ) {
		if( length > 1000 )
			return String.format("%.2f", (double)(length/1000)) + " km";
		else
			return length + " m";
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void dispose() {
		this.session.stop();
		environment.dispose();
	}
}
