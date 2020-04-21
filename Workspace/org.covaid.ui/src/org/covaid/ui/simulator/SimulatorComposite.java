package org.covaid.ui.simulator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Button;

public class SimulatorComposite extends Composite {
	private static final long serialVersionUID = 1L;
	
	private Label horizontalMetres;
	private Label verticalMetres;
	private Label lblPopulationValue;
	private Label lblMinRiskValue;
	private Slider sliderMinRisk;
	private Label lblMaxRiskValue;
	private Slider sliderMaxRisk;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public SimulatorComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(5, false));
		
		Canvas canvas = new Canvas(this, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		Slider horizontalSlider = new Slider(this, SWT.VERTICAL);
		horizontalSlider.setMaximum(310);
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
		
		Button btnNewButton = new Button(this, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		btnNewButton.setText("Start");
		new Label(this, SWT.NONE);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
