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
	protected IContagion createContagion(String identifier) {
		return new Contagion( identifier, 100 );
	}

	@Override
	protected ILocation<Integer> createLocation(String identifier, IPoint point) {
		return new Location( identifier, point );
	}

}
