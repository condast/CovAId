package org.covaid.dashboard.core;

import java.util.HashMap;
import java.util.Map;

import org.condast.commons.strings.StringStyler;
import org.covaid.core.environment.CovaidEnvironment;
import org.covaid.core.environment.IEnvironment;
import org.covaid.core.environment.RawEnvironment;
import org.covaid.ui.simulator.SimulatorComposite;
import org.eclipse.swt.widgets.Composite;

public class Dispatcher {

	public static final String S_ARNAC_URL = "arnac/";
	public static final String S_REST_CONTEXT = S_ARNAC_URL + "rest/";
	public static final String S_OPTIONS_CONTEXT = S_REST_CONTEXT + "options";

	public enum Composites{
		COVAID_COMPOSITE,
		SIMULATOR_COMPOSITE,
		MAINTENANCE_COMPOSITE,
		HTML_WIZARD,
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

	private Map<String, IEnvironment> environments;
	
	private static Dispatcher dispatcher = new Dispatcher();

	private Dispatcher() {
		this.environments = new HashMap<>();
		environments.put(CovaidEnvironment.NAME, new CovaidEnvironment());
		environments.put( RawEnvironment.NAME, new RawEnvironment() );
	}
	
	public static Dispatcher getInstance() {
		return dispatcher;
	}

	
	public void addComposite( Composites type, Composite composite ) {
		switch( type ){
		case COVAID_COMPOSITE:
			break;
		case HTML_WIZARD:
			//NmeaComposite nmea = ( NmeaComposite ) composite;
			//nmea.setInput(this);
			break;
		case SYSTEM_COMPOSITE:
			//VesselSystemView vsc = ( VesselSystemView ) composite;
			//vsc.setInput(this);
			break;
		case SIMULATOR_COMPOSITE:
			SimulatorComposite sc = ( SimulatorComposite) composite;
			sc.setInput(environments);
			break;
		default:
			break;
		}
	}
}