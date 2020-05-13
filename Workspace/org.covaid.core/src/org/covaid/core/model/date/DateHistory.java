package org.covaid.core.model.date;

import java.util.Date;
import java.util.Map;

import org.condast.commons.date.DateUtils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.AbstractHistory;

public class DateHistory extends AbstractHistory<Date> {
	
	public DateHistory() {
		super();
	}
	
	@Override
	protected long getDifference(Date first, Date last) {
		return first.getTime() - last.getTime();
	}

	@Override
	protected IContagion<Date> createContagion(String identifier) {
		return new DateContagion( identifier, 100 );
	}

	@Override
	protected ILocation<Date> createLocation(String identifier, IPoint point) {
		return new DateLocation(identifier, point);
	}

	/**
	 * Calculate the maximum contagion of the given test object for the reference
	 * @param contagion
	 * @param reference
	 * @param test
	 * @return
	 */
	public static double getContagion( IContagion<Date> contagion, IPoint location, Date date, Map.Entry<Date, DateLocation> test) {
		long days = DateUtils.getDifferenceDays( date, test.getKey());
		double distance = location.getDistance( test.getValue());
		return contagion.getSpread( days, distance );
	}
}
