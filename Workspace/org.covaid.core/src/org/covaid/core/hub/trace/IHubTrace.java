package org.covaid.core.hub.trace;

import java.util.Collection;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;

interface IHubTrace<T extends Object> {

	Collection<IContagion<T>> getContagions();

	ContagionData<T> get(IContagion<T> contagion);

	void update(IContagion<T> contagion, T timeStep, IHubTrace<T> ct);

	void put(IContagion<T> contagion, ContagionData<T> guest);

}