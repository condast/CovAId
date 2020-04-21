package org.covaid.ui.wizard;

import java.io.IOException;

import org.condast.js.commons.wizard.AbstractHtmlWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class BrowserWizard extends Composite {
	private static final long serialVersionUID = 1L;

	public enum Resources{
		HEADER,
		INDEX,
		HEALTH,
		DOCTOR,
		SETTINGS;

		@Override
		public String toString() {
			return super.toString();
		}
		
		public String toFile() {
			return "/resources/" + super.toString().toLowerCase() + ".ht";
		}
	}

	public enum Labels{
		RISK,
		TEMP;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}		
	}

	private Browser browser;
	
	private AbstractHtmlWizard wizard;
			
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public BrowserWizard(Composite parent, int style) {
		super(parent, style);
		super.setLayout(new FillLayout());
		browser = new Browser( this, SWT.BORDER );
		wizard = new AbstractHtmlWizard(browser) {

			@Override
			protected void onHandleLinks( String link) {
				Resources resource = Resources.valueOf(link.toUpperCase());
				try {
					super.createPage( BrowserWizard.class.getResourceAsStream(resource.toFile()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	
			
			@Override
			protected String onHandleLabel(String id, Attributes attr) {
				System.err.println( id + ", " + attr );
				String result = attr.toString();
				switch( attr ) {
				default:
					break;
				}
				return result;
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
			
		};
		try {
			wizard.createHeader(BrowserWizard.class.getResourceAsStream(Resources.HEADER.toFile()));
			wizard.createPage( BrowserWizard.class.getResourceAsStream(Resources.INDEX.toFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
