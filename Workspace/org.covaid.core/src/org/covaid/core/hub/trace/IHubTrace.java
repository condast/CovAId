package org.covaid.core.hub.trace;

import java.util.Collection;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;

interface IHubTrace<T extends Object> {

	Collection<IContagion> getContagions();

	ContagionData<T> get(IContagion contagion);

	void put(IContagion contagion, ContagionData<T> guest);

	void update(IContagion contagion, T current, IHubTrace<T> ct);
}