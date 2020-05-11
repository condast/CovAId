package org.covaid.core.environment.field;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.condast.commons.data.latlng.LatLng;
import org.condast.commons.data.plane.Field;
import org.condast.commons.data.plane.IField;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IFieldEnvironment;
import org.covaid.core.def.IHub;
import org.covaid.core.def.IPerson;
import org.covaid.core.environment.AbstractEnvironment;

public class FieldEnvironment extends AbstractEnvironment<Date> implements IFieldEnvironment{

	private IField field;//metres
	private String contagion;
	private int index;
	
	private Date start;
	
	public FieldEnvironment() {
		this( new LatLng( LATITUDE, LONGITUDE), DEFAULT_LENGTH, DEFAULT_WIDTH, DEFAULT_POPULATION, DEFAULT_SPEED );
	}
	
	public FieldEnvironment( String name, int length, int width, int population ) {
		this( new LatLng( name, LATITUDE, LONGITUDE), length, width, population, DEFAULT_SPEED);
	}

	public FieldEnvironment( LatLng location, int length, int width, int population, int speed ) {
		super( location.getId(), length, width, population, speed );
		field = new Field( location, length, width);
	    this.contagion = IContagion.SupportedContagion.COVID_19.name();
	}

	@Override
	public String getName() {
		return this.field.getName();
	}
	
	@Override
	public void init( int population ) {
		super.init(population);
		init( DEFAULT_ACTIVITY, population );
	}
	
	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	@Override
	public void init( int activity, int population ) {
		super.setActivity(activity);
	}


	public Collection<IPerson<Date>> getPersons( IFieldDomain domain ) {
		return domain.getPersons();
	}

	protected Map<String, IHub<Date>> getHubs( IFieldDomain domain ) {
		return domain.getHubs();
	}

	@Override
	public String getDayString( boolean trunc ) {
		StringBuilder builder = new StringBuilder();
		builder.append( super.getDays() );
		if(!trunc) {
			builder.append(": ");
			double step = (double)index/super.getActivity();
			builder.append(String.format("%.2f", step));
		}
		return builder.toString();
	}

	@Override
	public String getContagion() {
		return contagion;
	}

	@Override
	public void setContagion(String contagion) {
		this.contagion = contagion;
	}

	@Override
	public IField getField() {
		return field;
	}

	@Override
	public void setField( IField field) {
		this.field = field;
	}

	@Override
	public void zoomIn() {
		int length = (int) (field.getLength()/2);
		int width = (int) (field.getWidth()/2);
		field = new Field( field.getCoordinates(), length, width );
	}

	@Override
	public void zoomOut() {
		int length = (int) (field.getLength()*2);
		int width = (int) (field.getWidth()*2);
		field = new Field( field.getCoordinates(), length, width );
	}

	
	/**
	 * Get the simulated date
	 * @return
	 */
	@Override
	public Date getTimeStep( long days ) {
		Calendar calendar = Calendar.getInstance();
		long time = start.getTime() + days + (long)( 24*3600*1000*index/super.getActivity());
		calendar.setTimeInMillis( time);
		return calendar.getTime();
	}
	
	@Override
	public void start() {
		this.index = 0;
		super.start();
 	}
}