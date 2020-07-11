package org.covaid.dashboard;

import java.util.Locale;
import org.condast.js.commons.session.AbstractSessionHandler;
import org.condast.js.commons.session.SessionEvent;
import org.covaid.core.doctor.DoctorDataEvent;
import org.covaid.core.doctor.IDoctorDataListener;
import org.covaid.dashboard.core.Dispatcher;
import org.covaid.ui.doctor.CovaidTestTableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class DoctorEntryPoint extends AbstractEntryPoint {
	private static final long serialVersionUID = 1L;

	public static final String S_COVAID = "covaid";

	private CovaidTestTableViewer root;
	private Dispatcher dispatcher = Dispatcher.getInstance();

	private SessionHandler session;

	@Override
	protected void createContents(Composite parent) {
		try{
			//Set the RWT Locale
			parent.setData( RWT.CUSTOM_VARIANT, S_COVAID );
			Locale locale = new Locale( "nl", "NL" );
			RWT.setLocale(locale);
			RWT.getUISession().setLocale(locale);
			Locale.setDefault( locale );
	        parent.setLayout(new FillLayout());       
	        root = new CovaidTestTableViewer( parent, SWT.None );
	        root.setInput(dispatcher.getData());
			root.setData( RWT.CUSTOM_VARIANT, S_COVAID );
			session = new SessionHandler( root.getDisplay());
			dispatcher.addDoctorListener(session);
			root.addDoctorListener(session);
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispatcher.removeDoctorListener(session);
		root.removeDoctorListener(session);
		super.finalize();
	}
	
	private class SessionHandler extends AbstractSessionHandler<DoctorDataEvent> implements IDoctorDataListener{

		protected SessionHandler( Display display) {
			super( display );
		}

		@Override
		public void notifyDoctorDoctorChanged(DoctorDataEvent event) {
			addData( event);
		}

		@Override
		protected void onHandleSession(SessionEvent<DoctorDataEvent> sevent) {
			if( sevent.getData() == null )
				return;
			DoctorDataEvent event = sevent.getData();
			switch( event.getType()) {
			case ADD:
				root.setInput(dispatcher.getData());
				break;
			case REMOVE:
				dispatcher.getData().remove(event.getData());
				break;
			}
		}
		
	}
}