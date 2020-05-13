package org.covaid.core.contagion;

import java.util.Date;
import org.condast.commons.date.DateUtils;
import org.covaid.core.def.IContagion;
import org.covaid.core.model.date.DateContagion;

public class ContagionManager implements IContagionManager<Date> {

	public static final int DEFAULT_COVID_UNSAFE_DISTANCE = 2;
	public static final int DEFAULT_COVID_SAFE_DISTANCE = 10;
	public static final int DEFAULT_COVID_UNSAFE_TIME = 14;
	public static final int DEFAULT_COVID_SAFE_TIME = 30;
	
	public double calculateContagiousness( DateContagion.SupportedContagion identifier, double detectedContagion, double distance ) {
		double contagion = 0f;
		switch( identifier ) {
		case COVID_19:
			contagion = detectedContagion * ((distance < DEFAULT_COVID_UNSAFE_DISTANCE)?100: 
				(distance > DEFAULT_COVID_SAFE_DISTANCE )? 0: (distance*100)/DEFAULT_COVID_SAFE_DISTANCE);
			break;
		default:
			break;
		}
		return contagion; 
	}

	public double calculateContagiousness( IContagion<Date> data, double detectedContagion, double distance ) {
		double contagion = 0f;
		if( IContagion.SupportedContagion.isSupported(data.getIdentifier()))
			contagion = calculateContagiousness(IContagion.SupportedContagion.valueOf(data.getIdentifier()), detectedContagion, distance);
		return contagion; 
	}

	
	public double calculateContagiousness( DateContagion.SupportedContagion identifier, Date previous, Date current ) {
		double contagion = 0f;
		long diff = DateUtils.getDifferenceDays(current, previous);
		switch( identifier ) {
		case COVID_19:
			contagion = (diff < DEFAULT_COVID_UNSAFE_TIME)?100: (diff > DEFAULT_COVID_SAFE_TIME )? 0: (diff*100)/DEFAULT_COVID_SAFE_TIME;
			break;
		default:
			break;
		}
		return contagion; 
	}

	@Override
	public double calculateContagiousness(IContagion<Date> data, Date previous, Date current) {
		double contagion = 0f;
		if( IContagion.SupportedContagion.isSupported(data.getIdentifier()))
			contagion = calculateContagiousness(IContagion.SupportedContagion.valueOf(data.getIdentifier()), previous, current);
		return contagion; 
	}

}
