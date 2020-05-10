package org.covaid.core.model.date;

import java.util.Date;

import org.covaid.core.model.AbstractContagion;

public class DateContagion extends AbstractContagion<Date>{
	

	public DateContagion(String identifier, double contagiousness, double threshold, int distance, int maxDays,
			int halftime, double dispersion, boolean monitored) {
		super(identifier, contagiousness, threshold, distance, maxDays, halftime, dispersion, monitored);
	}

	public DateContagion(String identifier, double contagiousness, int distance, int maxDays) {
		super(identifier, contagiousness, distance, maxDays);
	}

	public DateContagion(String identifier, double contagiousness) {
		super(identifier, contagiousness);
	}

	public DateContagion(SupportedContagion identifier, double contagiousness) {
		super(identifier, contagiousness);
	}

	@Override
	public boolean isContagious(Date date) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected long getDifference(Date first, Date last) {
		return Math.abs( first.getTime()- last.getTime());
	}
}
