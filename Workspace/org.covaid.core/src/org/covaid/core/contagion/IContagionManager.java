package org.covaid.core.contagion;

import java.util.Date;

import org.covaid.core.config.env.Contagion;

public interface IContagionManager {

	public double calculateContagiousness( Contagion data, double detectedContagion, double distance );

	public double calculateContagiousness(Contagion data, Date previous, Date current);

}