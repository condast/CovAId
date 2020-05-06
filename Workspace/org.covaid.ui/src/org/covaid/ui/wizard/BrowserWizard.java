package org.covaid.ui.wizard;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.condast.commons.Utils;
import org.condast.commons.messaging.http.AbstractHttpRequest;
import org.condast.commons.messaging.http.ResponseEvent;
import org.condast.js.commons.wizard.AbstractHtmlParser;
import org.condast.js.commons.wizard.AbstractHtmlParser.Authentication;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;

public class BrowserWizard extends Composite {
	private static final long serialVersionUID = 1L;

	public static final String S_PATH = "http://www.condast.com:8080/covaid/mobile";
	private enum Requests{

		CREATE,
		GET_SAFETY,
		GET_RISK;
		
		@Override
		public String toString() {
			return super.toString();
		}
	}
	
	public enum Links{
		DOWNLOAD,
		INDEX,
		HEALTH,
		DOCTOR,
		SETTINGS,
		ACKNOWLEDGEMENTS;

		@Override
		public String toString() {
			return super.toString();
		}
		
		public String toFile() {
			return "/resources/" + super.toString().toLowerCase() + ".ht";
		}
	}

	public enum Labels{
		SAFETY,
		RISK,
		TEMP;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}		
	}

	private Browser browser;
	private Group grpIndication;

	private AbstractHtmlParser wizard;
	
	private Links link;
	
	private Map<Authentication,String> auth;
	
	private PaintListener listener = (e)->{
		Canvas canvas = (Canvas) e.getSource();
		Rectangle rect = canvas.getBounds();
		int radius = Math.min(rect.width, rect.height)/3;
		int riskRadius = radius;
		int safetyRadius = (int) (1.5*radius);
		GC gc = e.gc;
		gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN));
		gc.fillOval((rect.width-riskRadius)/2, (rect.height-riskRadius)/2, riskRadius, riskRadius);
		gc.setLineWidth(4);
		//gc.setLineStyle(SWT.LINE_DOT);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.drawOval((rect.width-safetyRadius)/2, (rect.height-safetyRadius)/2, safetyRadius, safetyRadius);
	};
			
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public BrowserWizard(Composite parent, int style) {
		super(parent, style);
		this.link = Links.DOWNLOAD;
		this.auth = new HashMap<>();
		createPage(parent, style);
	}
	
	private String createVariables( String args ) {
		StringBuilder builder = new StringBuilder();
		String[] split = args.split("[&]");
		for( String str: split ) {
			builder.append("var " + str + ";\n");
			String[] split1 = str.split("[=]");
			auth.put(Authentication.valueOf(split1[0].toUpperCase()), split1[1]);
		}
		return builder.toString();
	}

	protected void createPage( Composite parent, int style ) {
		super.setLayout(new FillLayout());
		setLayout(new GridLayout(1, false));
		super.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		browser = new Browser( this, SWT.BORDER );
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browser.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		wizard = new AbstractHtmlParser(browser, BrowserWizard.class) {

			@Override
			protected void onHandleLinks( String linkStr) {
				String[] split = linkStr.split("[?]");
				if( split.length>1)
					createVariables(split[1]);
				link = Links.valueOf(split[0].toUpperCase());
				grpIndication.setVisible( !Links.DOWNLOAD.equals(link));
				super.createPage( BrowserWizard.class.getResourceAsStream(link.toFile()));
			}
			
			@Override
			protected String onHandleLabel(String id, Attributes attr) {
				String result = attr.toString();
				switch( attr ) {
				default:
					break;
				}
				return result;
			}

			
			@Override
			protected String onHandleScript( Class<?> clss, String path) {
				StringBuilder builder = new StringBuilder();
				try {
					builder.append( parse( clss.getResourceAsStream( path)));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return builder.toString();
			}

			@Override
			protected String onHandleAuthentication( String id, Authentication authentication) {
				StringBuilder builder = new StringBuilder();
				if( Utils.assertNull(auth))
					return builder.toString();
				builder.append("=");
				switch( authentication ) {
				case IDENTIFIER:
					builder.append("'");
					builder.append( auth.get(authentication));
					builder.append("'");
					break;
				default:
					builder.append( auth.get(authentication));
					break;
				}
				return builder.toString();
			}

			@Override
			protected String onHandleValues(Functions function, String id, Attributes attr) {
				int result = 0;
				Labels label = Labels.valueOf(id.toUpperCase());
				switch( attr ) {
				case MIN:
					result = Labels.TEMP.equals(label)?30: 0;
					break;
				case MAX:
					result = Labels.TEMP.equals(label)?45: 100;
					break;
				default:
					break;
				}
				return String.valueOf(result);
			}
			
			@Override
			protected String onHandleFunction(Functions function, String id, Attributes attr) {
				String result = null;
				try {
					switch( function ) {
					case SCRIPT:
						StringBuilder builder = new StringBuilder();
						builder.append( "var mobile-id = ");
						WebClient client = new WebClient();
						Map<String, String> params = new HashMap<>();
						client.sendGet( Requests.CREATE, params, builder);
						return builder.toString();
					default:
						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return result;
			}	
		};

		grpIndication = new Group(this, SWT.NONE);
		grpIndication.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		grpIndication.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpIndication.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpIndication.setText("Indication");
		grpIndication.setVisible(false);
		
		Canvas canvas = new Canvas(grpIndication, SWT.NONE);
		canvas.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
		canvas.addPaintListener(listener);
		
		wizard.createPage( BrowserWizard.class.getResourceAsStream( link.toFile()));	
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	private class WebClient extends AbstractHttpRequest<Requests, StringBuilder>{
	
		public WebClient() {
			super( S_PATH );
		}

		
		@Override
		protected void sendGet(Requests request, Map<String, String> parameters, StringBuilder data) throws Exception {
			super.sendGet(request, parameters, data);
		}
	
		@Override
		protected String onHandleResponse(ResponseEvent<Requests, StringBuilder> event, StringBuilder data)
				throws IOException {
			switch( event.getRequest()) {
			case CREATE:
				Gson gson = new Gson();
				break;
			default:
				break;// TODO Auto-generated method stub
			}
			return null;
		}
		
	}
}