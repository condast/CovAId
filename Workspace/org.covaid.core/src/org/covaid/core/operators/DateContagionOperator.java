package org.covaid.core.operators;

import java.util.Calendar;
import java.util.Date;

import org.covaid.core.def.IContagion;

public class DateContagionOperator extends AbstractContagionOperator<Date> {

	public DateContagionOperator(Date init, IContagion contagion) {
		super(init, contagion);
	}

	public DateContagionOperator() {
		super();
	}

	@Override
	public long getDifference(Date first, Date last) {
		return Math.abs( first.getTime()- last.getTime());
	}

	
	@Override
	public Date subtract(Date first, Date last) {
		long diff = first.getTime() - last.getTime();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(diff);
		return calendar.getTime();
	}

	@Override
	public boolean isLastEntry(Date from, Date reference) {
		return ( reference.before( from ) && reference.after( super.getCurrent()));
	}

}
