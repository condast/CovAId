package org.covaid.core.model;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;

public class History extends AbstractHistory<Integer> {

	@Override
	protected long getDifference(Integer first, Integer last) {
		return first-last;
	}

	@Override
	protected IContagion<Integer> createContagion(String identifier, double safety) {
		return new Contagion( identifier, safety );
	}

	@Override
	protected ILocation<Integer> createLocation(String identifier, IPoint point) {
		return new Location( identifier, point );
	}

}
