package org.covaid.ui.mobile;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.covaid.core.mobile.IMobileRegistration;
import org.covaid.ui.frogger.FroggerComposite;
import org.covaid.ui.wizard.MobileWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

public class MobileComposite extends Composite {
	private static final long serialVersionUID = 1L;
	private MobileWizard wizard;
	private FroggerComposite froggerComposite;
	
	
	private IMobileRegistration listener = (e)->{
		froggerComposite.setInput(e.getAuthenticationData());
	};
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MobileComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(4, true));
		
		wizard = new MobileWizard(this, SWT.BORDER);
		wizard.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		froggerComposite = new FroggerComposite(this, SWT.NONE);
		froggerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		wizard.addRegistrationListener(listener);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void dispose() {
		wizard.removeRegistrationListener(listener);
		super.dispose();
	}
}
