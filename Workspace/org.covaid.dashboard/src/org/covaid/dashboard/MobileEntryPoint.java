package org.covaid.dashboard;

import java.util.Locale;
import org.covaid.ui.mobile.MobileComposite;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class MobileEntryPoint extends AbstractEntryPoint {
	private static final long serialVersionUID = 1L;

	public static final String S_COVAID = "covaid";

	@Override
	protected void createContents(Composite parent) {
		try{
			//Set the RWT Locale
			parent.setData( RWT.CUSTOM_VARIANT, S_COVAID );
			Locale locale = new Locale( "nl", "NL" );
			RWT.setLocale(locale);
			RWT.getUISession().setLocale(locale);
			Locale.setDefault( locale );
			parent.setLayout( new GridLayout(2, false ));

	        parent.setLayout(new FillLayout());       
	        Composite root = new MobileComposite( parent, SWT.None );
			root.setData( RWT.CUSTOM_VARIANT, S_COVAID );
			
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		//evc.setVesselService(null);
		super.finalize();
	}
}