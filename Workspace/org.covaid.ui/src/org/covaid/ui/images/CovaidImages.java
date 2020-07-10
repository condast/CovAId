package org.covaid.ui.images;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.condast.commons.ui.image.AbstractImages;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * @see: https://www.iconfinder.com/savlon
 * @author Condast
 *
 */
public class CovaidImages extends AbstractImages{

	public static final String BUNDLE_ID = "org.covaid.ui";
	
	public static final String S_DOUBLE_ARROW = "DOUBLE_ARROW";

	public static final String S_ZOOM_IN = "zoom-in";
	public static final String S_ZOOM_OUT = "zoom-out";

	public static final String S_PNG = ".png";

	public static final String S_DEFAULT_LOCATION = "/covaid/images/";

	public enum Images{
		CHECK,
		UNCHECK,
		COVID_SEARCH,
		COVID_19,
		DOCTOR,
		DOCTOR_CORONAVIRUS;

		public static String getResource( Images image ){
			return getResource( image, ImageSize.NORMAL );
		}
		
		public static String getResource( Images image, ImageSize size ){
			StringBuffer buffer = new StringBuffer();
			switch( image ){		
			case COVID_SEARCH:
				buffer.append( image.name().toLowerCase());
				buffer.append( S_PNG);
				break;
			case COVID_19:
				buffer.append( image.name().toLowerCase());
				buffer.append( S_PNG);
				break;
			case DOCTOR:
				buffer.append( image.name().toLowerCase());
				buffer.append( S_PNG);
				break;
			case DOCTOR_CORONAVIRUS:
				buffer.append( image.name().replace("_", "-").toLowerCase());
				buffer.append( S_PNG);
				break;
			default:
				buffer.append( ImageSize.getLocation(image.name(), size));
				break;
			}
			return buffer.toString();
		}
		
		public static String getPath( Images image ){
			String name = image.name().replace("_", "-").toLowerCase();
			String str = "huge/" + name + "-128.png";
			return S_DEFAULT_LOCATION + str;
		}
	}
	
	private static Logger logger = Logger.getLogger( CovaidImages.class.getName() );
	
	private static CovaidImages images = new CovaidImages();
	
	private CovaidImages() {
		super( S_RESOURCES, BUNDLE_ID);
	}

	public static CovaidImages getInstance(){
		return images;
	}
	
	@Override
	public void initialise(){
		for( Images image: Images.values())
			setImage( Images.getResource(image) );
	}

	public Image getImage( Images image ){
		return super.getImageFromName( Images.getResource( image ));
	}

	protected void setImage( Images image ){
		super.setImage( Images.getResource(image));
	}
	
	/**
	 * Register the resource with the given name
	 * @param name
	 */
	public static void registerImage( Images image ){
		registerImage( image.name().toLowerCase(), Images.getResource(image));
	}
	
	/**
	 * Register the resource with the given name
	 * @param name
	 */
	public static void registerImage( String name, String file ){
		ResourceManager resourceManager = RWT.getResourceManager();
		if( !resourceManager.isRegistered( name ) ) {
			InputStream inputStream = CovaidImages.class.getClassLoader().getResourceAsStream( file );
			try {
				resourceManager.register( name, inputStream );
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.log( Level.SEVERE, name + ": " + file );
					e.printStackTrace();
				}
			}
		}		
	}
	
	/**
	 * Get the image with the given name
	 * @param name
	 * @return
	 */
	public static String getImageString( ImageSize size, Images image ){
		return Images.getResource(image); 
	}
	
	/**
	 * Set the image for the given control
	 * @param widget
	 * @param name
	 */
	public static void setImage( Control widget, Images image ){
		widget.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
		//registerImage( image );
		if( widget instanceof Label ){
			  Label label = (Label) widget;
			  String src = getImageString( ImageSize.NORMAL, image );
			  label.setText( "Hello<img width='24' height='24' src='" + src + "'/> there " );
			}
		if( widget instanceof Button ){
		  Button button = (Button) widget;
		  String src = getImageString( ImageSize.NORMAL, image );
		  button.setText( "<img width='24' height='24' src='" + src + "'/>" );
		}
	}
	
	/**
	 * Get the screen aver and size it to fit the parent
	 * @param parent
	 * @return
	 */
	public static Image getScreenSaver( Composite parent ){
		ImageData imageData = new ImageData( CovaidImages.class.getResourceAsStream( S_RESOURCES + Images.getResource( Images.COVID_SEARCH ) ));
		ImageData scaledData = imageData.scaledTo( parent.getBounds().width, parent.getBounds().height );
		return new Image(Display.getCurrent(), scaledData );
	}
}