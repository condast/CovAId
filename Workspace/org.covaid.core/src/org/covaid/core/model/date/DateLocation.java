package org.covaid.core.model.date;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
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
	public ILocation<Date> clone() {
		IPoint point = super.toPoint();
		ILocation<Date> result = new DateLocation( point );
		Iterator<Map.Entry<IContagion<Date>, ContagionData<Date>>> iterator = super.getContagions().entrySet().iterator();
		while( iterator.hasNext() ) {
			Map.Entry<IContagion<Date>, ContagionData<Date>> entry = iterator.next();
			result.addContagion(entry.getValue().getTimeStep(), entry.getKey());
		}
		return result;
	}

}
