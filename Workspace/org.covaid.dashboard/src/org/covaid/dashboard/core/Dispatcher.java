package org.covaid.dashboard.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.condast.commons.Utils;
import org.condast.commons.authentication.user.ILoginUser;
import org.condast.commons.data.latlng.LatLng;
import org.condast.commons.data.latlng.Motion;
import org.condast.commons.data.plane.IField;
import org.condast.commons.strings.StringStyler;
import org.covaid.core.user.IUserRegistration;
import org.covaid.dashboard.authentication.AuthenticationManager;
import org.eclipse.swt.widgets.Composite;

public class Dispatcher {

	public static final String S_ARNAC_URL = "arnac/";
	public static final String S_REST_CONTEXT = S_ARNAC_URL + "rest/";
	public static final String S_OPTIONS_CONTEXT = S_REST_CONTEXT + "options";

	public enum Composites{
		ARNAC_COMPOSITE,
		ENVIRONMENT_COMPOSITE,
		DEPTH_COMPOSITE,
		MAINTENANCE_COMPOSITE,
		OPENROV_COMPOSITE,
		NMEA_COMPOSITE,
		SYSTEM_COMPOSITE,
		LOG_COMPOSITE;
	}
	
	public enum Options{
		OPTIONS,
		LOG,
		SYSTEM;
		
		public static boolean isValid( String str ) {
			for( Options option: values()) {
				if( option.name().equals( StringStyler.styleToEnum( str )))
					return true;
			}
			return false;
		}
	}
			
	private static Dispatcher dispatcher = new Dispatcher();

	private Dispatcher() {
	}
	
	public static Dispatcher getInstance() {
		return dispatcher;
	}

	public void addComposite( Composites type, Composite composite ) {
		switch( type ){
		case ARNAC_COMPOSITE:
			//ArnacComposite ac = ( ArnacComposite) composite;
			//ac.setUserManager( AuthenticationManager.getInstance());
			//ac.setInput(this);
			break;
		case NMEA_COMPOSITE:
			//NmeaComposite nmea = ( NmeaComposite ) composite;
			//nmea.setInput(this);
			break;
		case SYSTEM_COMPOSITE:
			//VesselSystemView vsc = ( VesselSystemView ) composite;
			//vsc.setInput(this);
			break;
		case ENVIRONMENT_COMPOSITE:
			//EnvironmentComposite evc = (EnvironmentComposite) composite;
			//evc.setVesselService( dispatcher );
			break;
		case DEPTH_COMPOSITE:
			//DepthComposite dc = (DepthComposite) composite;
			//dc.setInput( MapLocation.Location.WORLD, false);
			break;
		case OPENROV_COMPOSITE:
			//OpenROVComposite rovc = (OpenROVComposite) composite;
			break;
		default:
			break;
		}
	}
}