package org.covaid.core.hub.trace;

import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;
import org.covaid.core.hub.IHub;

public interface ITrace<T extends Object> {

	void setHub(IHub<T> hub);

	/**
	 * Get the prediction from the given range towards the current time
	 * @param contagion
	 * @param range
	 * @return
	 */
	Map<T, Double> getPrediction(IContagion contagion, T range);

	/**
	 * Create a prediction of the risk of contagion by looking at the current state
	 * of the given hubs
	 * @param contagion
	 * @param range
	 * @param hubs
	 * @return
	 */
	Map<T, Double> getPrediction(IContagion contagion, T range, Map<IPoint, ? extends IHub<T>> hubs);

	/**
	 * This trace has an increased risk of contagion. One of the guests is a possible source
	 */
	boolean update(IContagion contagion, T current, ILocation<T> guest);

	Map<T, Double> getTraces(IContagion contagion, T range);

	boolean isEnabled();

	void setEnabled(boolean enabled);
}