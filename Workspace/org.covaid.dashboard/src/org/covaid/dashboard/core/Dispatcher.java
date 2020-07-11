package org.covaid.dashboard.core;

import java.util.ArrayList;
import java.util.Collection;

import org.condast.commons.data.latlng.LatLng;
import org.condast.commons.data.plane.Field;
import org.condast.commons.data.plane.IField;
import org.condast.commons.strings.StringStyler;
import org.covaid.core.data.DoctorData;
import org.covaid.core.def.IFieldEnvironment;
import org.covaid.core.doctor.DoctorDataEvent;
import org.covaid.core.doctor.IDoctorDataListener;
import org.covaid.core.doctor.IDoctorDataProvider;
import org.covaid.core.environment.field.CovaidDomain;
import org.covaid.core.environment.field.FieldEnvironment;
import org.covaid.core.environment.field.RawDomain;
import org.covaid.core.field.FieldChangeEvent;
import org.covaid.core.field.IFieldListener;
import org.covaid.core.field.IFieldProvider;
import org.covaid.ui.simulator.SimulatorComposite;
import org.eclipse.swt.widgets.Composite;

public class Dispatcher implements IFieldProvider{

	public static final String S_ARNAC_URL = "arnac/";
	public static final String S_REST_CONTEXT = S_ARNAC_URL + "rest/";
	public static final String S_OPTIONS_CONTEXT = S_REST_CONTEXT + "options";

	public static final int DEFAULT_LENGTH = 1000;
	public static final int DEFAULT_WIDTH = 1000;

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

	private IFieldEnvironment environment;
	
	private IField field;
	
	private Collection<IFieldListener> fieldListeners;

	private Collection<DoctorData> data;
	
	private Collection<IDoctorDataListener> dlisteners;

	private IDoctorDataListener dlistener = new IDoctorDataListener(){

		@Override
		public void notifyDoctorDoctorChanged(DoctorDataEvent event) {
			switch( event.getType()) {
			case ADD:
				data.add(event.getData());
				break;
			case REMOVE:
				data.remove(event.getData());
				break;
			default:
				break;
			}
			for( IDoctorDataListener listener: dlisteners )
				listener.notifyDoctorDoctorChanged(event);
		}	
	};
	
	private Collection<IDoctorDataProvider> providers;	

	private static Dispatcher dispatcher = new Dispatcher();

	private Dispatcher() {
		this.environment = new FieldEnvironment();
		environment.addDomain( new RawDomain());
		environment.addDomain( new CovaidDomain());
		field = new Field(new LatLng(0,0), DEFAULT_LENGTH, DEFAULT_WIDTH);
		this.fieldListeners = new ArrayList<>();
		this.providers = new ArrayList<>();
		this.data = new ArrayList<>();
		this.dlisteners = new ArrayList<>();
	}
	
	public static Dispatcher getInstance() {
		return dispatcher;
	}

	@Override
	public void addFieldListener(IFieldListener listener) {
		this.fieldListeners.add(listener);
	}

	@Override
	public void removeFieldListener(IFieldListener listener) {
		this.fieldListeners.remove(listener);
	}
	
	protected void notifyFieldChange( FieldChangeEvent event ) {
		for( IFieldListener listener: this.fieldListeners )
			listener.notifyFieldChange(event);
	}

	public IField getField() {
		return field;
	}

	public Collection<DoctorData> getData(){
		return this.data;
	}

	public void addDoctorListener(IDoctorDataListener listener) {
		this.dlisteners.add(listener);
	}

	public void removeDoctorListener(IDoctorDataListener listener) {
		this.dlisteners.remove(listener);
	}
	
	public void addProvider(IDoctorDataProvider provider) {
		this.providers.add(provider);
		provider.addDoctorDataListener(dlistener);
	}

	public void removeProvider(IDoctorDataProvider provider) {
		provider.removeDoctorDataListener(dlistener);
		this.providers.remove(provider);
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
			sc.setInput(environment);
			break;
		default:
			break;
		}
	}
}