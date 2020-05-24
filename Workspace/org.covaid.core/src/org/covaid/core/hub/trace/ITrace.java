package org.covaid.core.hub.trace;

import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.hub.IHub;

public interface ITrace<T extends Object> {

	IHub<T> getHub();

	void setHub(IHub<T> hub);

	IHubTrace<T> getHubTrace(IHub<T> hub);

	IHubTrace<T> addHubTrace(IHub<T> hub);

	boolean update(IContagion<T> contagion, T timeStep, ITrace<T> guest);

	Map<T, Double> getPrediction(IContagion<T> contagion, T range);
}