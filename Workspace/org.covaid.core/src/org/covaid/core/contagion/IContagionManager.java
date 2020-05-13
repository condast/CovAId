package org.covaid.core.contagion;

import java.util.Date;

import org.covaid.core.def.IContagion;

public interface IContagionManager<T extends Object> {

	public double calculateContagiousness( IContagion<T> data, double detectedContagion, double distance );

	public double calculateContagiousness(IContagion<T> data, Date previous, Date current);

}