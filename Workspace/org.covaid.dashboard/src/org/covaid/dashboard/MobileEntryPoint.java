package org.covaid.dashboard;

import java.util.Locale;

import org.condast.commons.strings.StringUtils;
import org.covaid.ui.mobile.MobileComposite;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.StartupParameters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class MobileEntryPoint extends AbstractEntryPoint {
	private static final long serialVersionUID = 1L;

	public static final String S_COVAID = "covaid";

	@Override
	protected void createContents(Composite parent) {
		try{
			//Set the RWT Locale
			StartupParameters service = RWT.getClient().getService( StartupParameters.class );
			String subscription = service.getParameter("id");
			long subscriptionId = StringUtils.isEmpty(subscription)?0:Long.parseLong(subscription);
			parent.setData( RWT.CUSTOM_VARIANT, S_COVAID );
			Locale locale = new Locale( "nl", "NL" );
			RWT.setLocale(locale);
			RWT.getUISession().setLocale(locale);
			Locale.setDefault( locale );
	        parent.setLayout(new FillLayout());       
	        MobileComposite root = new MobileComposite( parent, SWT.None );
	        root.setSubscriptionId(subscriptionId);
			root.setData( RWT.CUSTOM_VARIANT, S_COVAID );
			//ClientFileLoader loader = RWT.getClient().getService( ClientFileLoader.class );
			//loader.requireJs("http://localhost:10080/covaid/mobile/push/js/push/index.js");
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