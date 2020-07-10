package org.covaid.ui.mobile;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.covaid.core.mobile.IMobileRegistration;
import org.covaid.ui.frogger.FroggerComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;

public class MobileComposite extends Composite {
	private static final long serialVersionUID = 1L;
	private MobileWizard wizard;
	private FroggerComposite froggerComposite;
	
	private IMobileRegistration<Date> listener = (e)->{
		froggerComposite.setInput(e.getAuthenticationData());
	};

	private IUpdateListener updateListener = (e)->{
		if( "CLEAR".equals( e.getIdentifier())){
			wizard.clear();
		}
		//froggerComposite.setInput(e.getAuthenticationData());
	};

	private Timer timer;
	private TimerTask timerTask = new TimerTask(){

		@Override
		public void run() {
			if(!froggerComposite.isStarted() || froggerComposite.isPaused())
				return;
			froggerComposite.poll();
			wizard.poll();
		}
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
		froggerComposite.addUpdateListener(updateListener);
		
		wizard.addRegistrationListener(listener);
	    this.timer = new Timer(true);
	    timer.scheduleAtFixedRate(timerTask, 0, 1000);
	}

	public void setSubscriptionId(long subscriptionId) {
		this.wizard.setSubscriptionId(subscriptionId);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void dispose() {
		froggerComposite.removeUpdateListener(updateListener);
		wizard.removeRegistrationListener(listener);
		super.dispose();
	}
}
