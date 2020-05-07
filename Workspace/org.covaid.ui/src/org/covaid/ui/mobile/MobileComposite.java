package org.covaid.ui.mobile;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.covaid.ui.wizard.MobileWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

public class MobileComposite extends Composite {
	private static final long serialVersionUID = 1L;
	private MobileWizard wizard;
	private Label label_1;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MobileComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(3, true));
		
		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		wizard = new MobileWizard(this, SWT.BORDER);
		wizard.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		label_1 = new Label(this, SWT.NONE);
		label_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
