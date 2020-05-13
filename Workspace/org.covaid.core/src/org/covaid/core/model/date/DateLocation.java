package org.covaid.core.model.date;

import java.util.Date;

import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.AbstractLocation;

public class DateLocation extends AbstractLocation<Date> implements ILocation<Date>{
	
	public DateLocation( IPoint point) {
		this( point.getXpos(), point.getYpos());
	}

	public DateLocation( String identifier, IPoint point) {
		this( identifier, point.getXpos(), point.getYpos());
	}

	public DateLocation( int xpos, int ypos) {
		this( createIdentifier(xpos, ypos), xpos, ypos);
	}
	
	/**
	 * A location needs a unique identifier in order for it to distinguish itself from
	 * other locations. A postcode could work, or a string representation of LatLng coordinates 
	 * @param identifier
	 * @param xpos
	 * @param ypos
	 */
	public DateLocation( String identifier, int xpos, int ypos) {
		super( identifier, xpos, ypos);
	}

	
	@Override
	protected ILocation<Date> createLocation(String identifier, IPoint point) {
		return new DateLocation( identifier, point );
	}
}
