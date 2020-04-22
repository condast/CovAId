package org.covaid.core.contagion;

import java.util.Date;

import org.covaid.core.config.env.Contagion;

public interface IContagionManager {

	public float calculateContagiousness( Contagion data, float detectedContagion, float distance );

	public float calculateContagiousness(Contagion data, Date previous, Date current);

}