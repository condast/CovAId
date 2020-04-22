package org.covaid.ui.simulator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.condast.commons.strings.StringStyler;
import org.covaid.core.config.env.Contagion;
import org.covaid.core.config.env.ISimulationListener;
import org.covaid.core.config.env.Person;
import org.covaid.core.config.env.Simulation;
import org.covaid.core.config.env.SimulationEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Button;

public class SimulatorComposite extends Composite {
	private static final long serialVersionUID = 1L;
	
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
	
	private Simulation simulation;
	
	private ISimulationListener listener = (e)->{
		if( getDisplay().isDisposed())
			return;
		getDisplay().asyncExec(()->{
			canvas.setData(e);
			canvas.redraw();
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
		setLayout(new GridLayout(5, false));
		
		canvas = new Canvas(this, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		Slider horizontalSlider = new Slider(this, SWT.VERTICAL);
		horizontalSlider.setMaximum(310);
		horizontalSlider.setSelection(1);
		horizontalSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		horizontalSlider.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				verticalMetres.setText( slider.getSelection() + " km");
				super.widgetSelected(e);
			}	
		});
		
		Slider verticalSlider = new Slider(this, SWT.NONE);
		verticalSlider.setMaximum(210);
		verticalSlider.setSelection(1);
		verticalSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		verticalSlider.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				horizontalMetres.setText( slider.getSelection() + " km");
				super.widgetSelected(e);
			}	
		});
		new Label(this, SWT.NONE);
		
		horizontalMetres = new Label(this, SWT.NONE);
		horizontalMetres.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		horizontalMetres.setText("0 km");

		
		verticalMetres = new Label(this, SWT.NONE);
		verticalMetres.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		verticalMetres.setText("0 km");
		new Label(this, SWT.NONE);
		
		Label lblPopulation = new Label(this, SWT.NONE);
		lblPopulation.setText("Population");
		
		Slider sliderPopulation = new Slider(this, SWT.NONE);
		sliderPopulation.setMaximum(10000);
		sliderPopulation.setSelection(100);
		sliderPopulation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		sliderPopulation.addSelectionListener( new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				Slider slider = (Slider) e.widget;
				lblPopulationValue.setText( slider.getSelection() + " /km2");
				super.widgetSelected(e);
			}	
		});
		
		lblPopulationValue = new Label(this, SWT.NONE);
		lblPopulationValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPopulationValue.setText("0 /km2");
		new Label(this, SWT.NONE);
		
		Label lblMinRisk = new Label(this, SWT.NONE);
		lblMinRisk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMinRisk.setText("Min. Risk");
		
		sliderMinRisk = new Slider(this, SWT.NONE);
		sliderMinRisk.setMaximum(101);
		sliderMinRisk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
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
		lblMaxRisk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblMaxRisk.setText("Max. Risk");
		
		sliderMaxRisk = new Slider(this, SWT.NONE);
		sliderMaxRisk.setMaximum(101);
		sliderMaxRisk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
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
						simulation.init(Contagion.SupportedContagion.COVID_19.getContagion(), horizontalSlider.getSelection(), verticalSlider.getSelection(), 
								sliderPopulation.getSelection());
						simulation.start();
						break;
					case STOP:
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
		new Label(this, SWT.NONE);
	}

	public void setInput(Simulation simulation) {
		if( this.simulation != null ) {
			this.simulation.removeListener(listener);
		}
		this.simulation = simulation;
		if( this.simulation != null )
			this.simulation.addListener(listener);
	}

	protected void updateCanvas( GC gc ) {
		SimulationEvent event = (SimulationEvent) canvas.getData();
		Person person = event.getPerson();
		gc.setForeground(getDisplay().getSystemColor( SWT.COLOR_DARK_BLUE));
		gc.fillOval(person.getLocation().getXpos(), person.getLocation().getYpos(), 10, 10);
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void dispose() {
		this.simulation.dispose();
	}
}
