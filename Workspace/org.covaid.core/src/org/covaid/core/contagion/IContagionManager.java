package org.covaid.core.contagion;

import java.util.Date;

import org.covaid.core.data.ContagionData;

public interface IContagionManager {

	public float calculateContagiousness( ContagionData data, float detectedContagion, float distance );

	public float calculateContagiousness(ContagionData data, Date previous, Date current);

}