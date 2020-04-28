package org.covaid.core.contagion;

import java.util.Date;

import org.covaid.core.def.IContagion;

public interface IContagionManager {

	public double calculateContagiousness( IContagion data, double detectedContagion, double distance );

	public double calculateContagiousness(IContagion data, Date previous, Date current);

}