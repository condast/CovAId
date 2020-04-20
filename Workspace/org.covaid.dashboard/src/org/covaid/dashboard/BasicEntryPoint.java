package org.covaid.dashboard;

import java.util.Locale;

import org.condast.commons.strings.StringStyler;
import org.condast.commons.strings.StringUtils;
import org.condast.commons.ui.activate.ActivationEvent;
import org.condast.commons.ui.activate.IActivateListener;
import org.condast.commons.ui.activate.IActivateWidget;
import org.condast.commons.ui.logger.LogComposite;
import org.condast.commons.ui.xml.XMLFactoryBuilder;
import org.condast.commons.xml.AbstractXMLBuilder;
import org.condast.commons.xml.BuildEvent;
import org.condast.commons.xml.IBuildListener;
import org.condast.commons.xml.AbstractXMLBuilder.Selection;
import org.covaid.core.def.Hello2;
import org.covaid.dashboard.authentication.AuthenticationDispatcher;
import org.covaid.dashboard.authentication.AuthenticationManager;
import org.covaid.dashboard.core.Dispatcher;
import org.covaid.ui.map.Hello;
import org.covaid.ui.map.MapBrowser;
import org.covaid.ui.map.TestComposite;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;

public class BasicEntryPoint extends AbstractEntryPoint {
	private static final long serialVersionUID = 1L;

	public static final String S_COVAID = "covaid";

	private TabFolder tabFolder;
	private IActivateWidget lc;
	private IActivateWidget debug;
	private RWTUiSessionHandler handler;
	
	private AuthenticationDispatcher authentication = AuthenticationDispatcher.getInstance(); 

	private Dispatcher dispatcher = Dispatcher.getInstance();
	
	private IActivateListener alistener = new IActivateListener() {

		@Override
		public void notifyActivationChange(ActivationEvent event) {
		}
	};

	private IBuildListener<Widget> listener = new IBuildListener<Widget>(){

		@Override
		public void notifyTestEvent(BuildEvent<Widget> event) {
			try {
				if( !Selection.isOfSelection(event.getName()))
					return;
				switch( Selection.valueOf( event.getName())) {
				case TABFOLDER:
					tabFolder = (TabFolder) event.getData();
					tabFolder.setData(RWT.CUSTOM_VARIANT, S_COVAID);
					tabFolder.addSelectionListener( new SelectionAdapter() {
						private static final long serialVersionUID = 1L;

						@Override
						public void widgetSelected(SelectionEvent e) {
							TabItem item = (TabItem) e.item;
							String text = item.getText().toUpperCase();
							if( !Dispatcher.Options.isValid( text )) {
								if( debug != null )
									debug.activate(false);
								return;
							}
							Dispatcher.Options option = Dispatcher.Options.valueOf( StringStyler.styleToEnum(text));
							switch( option ) {
							case OPTIONS:
								if( lc != null )
									lc.activate(true);
								break;
							case SYSTEM:
								if( debug != null )
									debug.activate(true);
								break;
							default:
								debug.activate(false);
								break;
							}
							super.widgetSelected(e);
						}
					});
					break;
				case COMPOSITE:
					String name = event.getAttribute(AbstractXMLBuilder.AttributeNames.NAME);
					if( StringUtils.isEmpty(name))
						return;
					Dispatcher.Composites cmp = Dispatcher.Composites.valueOf( StringStyler.styleToEnum( name ));
					dispatcher.addComposite( cmp, (Composite) event.getData() );
					switch( cmp ){
					case COVAID_COMPOSITE:
						break;
					case ENVIRONMENT_COMPOSITE:
						break;
					case DEPTH_COMPOSITE:
						break;
					case OPENROV_COMPOSITE:
						break;
					case SYSTEM_COMPOSITE:
						//vsv.setInput( dispatcher.getManagers());
						//vsv.setInput( dispatcher.getActiveManager());
						debug.addActivateListener(alistener);
						break;
					case LOG_COMPOSITE:
						lc = (LogComposite) event.getData();
						lc.addActivateListener(alistener);
						//IVesselManager manager = dispatcher.getActiveManager();
						//if( manager == null )
						//	return;
						//VesselOptions store = manager.getOptions();
						//lc.activate(store.isLogging() );
						break;
					default:
						break;
					}
					break;
				default:
					break;
				}
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
	};
	
	@Override
	protected void createContents(Composite parent) {
		try{
			handler = new RWTUiSessionHandler(parent.getDisplay());
			//Set the RWT Locale
			parent.setData( RWT.CUSTOM_VARIANT, S_COVAID );
			Locale locale = new Locale( "nl", "NL" );
			RWT.setLocale(locale);
			RWT.getUISession().setLocale(locale);
			Locale.setDefault( locale );
			parent.setLayout( new GridLayout(2, false ));

	        parent.setLayout(new FillLayout());       
			XMLFactoryBuilder builder = new XMLFactoryBuilder( parent, this.getClass());
	        builder.addListener(listener);
	        builder.build();
	        builder.removeListener(listener);
	        Composite root = builder.getRoot();
			root.setData( RWT.CUSTOM_VARIANT, S_COVAID );
			
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		//evc.setVesselService(null);
		handler.dispose();
		super.finalize();
	}


	private class RWTUiSessionHandler extends org.condast.commons.ui.rwt.AbstractRWTSessionSupport{

		public RWTUiSessionHandler(Display display) {
			super(display, Integer.MAX_VALUE);
		}

		@Override
		protected void onHandleTimeout(boolean reload) {
			AuthenticationManager.getInstance().logout();
		}
		
	}
}