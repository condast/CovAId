package org.covaid.core.hub.trace;

import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.hub.IHub;

public interface ITrace<T extends Object> {

	IHub<T> getHub();

	void setHub(IHub<T> hub);

	IHubTrace<T> getHubTrace(IHub<T> hub);

	IHubTrace<T> addHubTrace(IHub<T> hub);

	/**
	 * Get the prediction from the given range towards the current time
	 * @param contagion
	 * @param range
	 * @return
	 */
	Map<T, Double> getPrediction(IContagion contagion, T range);

	boolean update(IContagion contagion, T current, ITrace<T> guest);
}