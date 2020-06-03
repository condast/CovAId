package org.covaid.core.model.date;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.hub.AbstractHub;
import org.covaid.core.hub.IHub;
import org.covaid.core.hub.trace.AbstractTrace;
import org.covaid.core.hub.trace.ITrace;
import org.covaid.core.operators.DateContagionOperator;

public class DateHub extends AbstractHub<Date> implements IHub<Date> {

	public DateHub(String identifier, int xpos, int ypos) {
		this(new DateLocation( identifier, xpos, ypos ));
	}

	public DateHub( ILocation<Date> location) {
		this(location, Calendar.getInstance().getTime(),  Calendar.getInstance().getTime() );
	}

	public DateHub( ILocation<Date> location, Date current, Date history) {
		super(location, current, history, new Trace( current ));
		super.getTrace().setHub(this);
	}

	/**
	 * convenience method to 
	 * @param person
	 */
	public DateHub( IPerson<Date> person ) {
		this( person.getLocation().getIdentifier(), person.getLocation().getXpos(), person.getLocation().getYpos());
	}
	
	@Override
	protected boolean onPersonAlert(IPerson<Date> person, Date moment, Date timeStep, Date history, Date encountered) {
		return ( timeStep.getTime() - moment.getTime() )<history.getTime() ;
	}

	@Override
	public ILocation<Date> update( Date current ) {	
		super.setLocation( createSnapShot());
		ILocation<Date> location = super.getLocation();
		int days = (int) (2 * DateLocation.getMaxContagionTime(location));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(current);
		calendar.add(Calendar.DAY_OF_YEAR, -days);
		super.getPersons().entrySet().removeIf(entry -> entry.getValue().before(calendar.getTime()));
		return location;
	}

	@Override
	public DateHub clone(){
		DateHub hub = new DateHub( super.getLocation() );
		Iterator<Map.Entry<IPerson<Date>, Date>> iterator = super.getPersons().entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<IPerson<Date>, Date> entry = iterator.next();
			hub.getPersons().put(entry.getKey(), entry.getValue());
		}
		return hub;
	}

	@Override
	protected boolean onRemovePersons(IPerson<Date> person, Date timeStep, Date history, Date encountered) {
		return ( timeStep.getTime() - encountered.getTime() ) > history.getTime();
	}	
	
	private static class Trace extends AbstractTrace<Date> implements ITrace<Date>{

		public Trace(Date current) {
			super( current, new DateContagionOperator() );
		}

		@Override
		protected Date onGetAverage(Date first, Date second) {
			return null;
		}

		@Override
		public Map<Date, Double> getTraces(IContagion contagion, Date range) {
			Map<ILocation<Date>, ContagionData<Date>> map = super.getTraceMap(contagion, range);
			Map<Date, Double> results = new HashMap<>();
			for( Map.Entry<ILocation<Date>, ContagionData<Date>> entry: map.entrySet()) {
				int step = entry.getKey().getYpos();
				Double risk = results.get(step);
				if( risk == null )
					risk = entry.getValue().getRisk();
				else
					risk = ( risk + entry.getValue().getRisk())/2;
				results.put(entry.getValue().getMoment(), risk);
			}
			return results;
		}

	}
}