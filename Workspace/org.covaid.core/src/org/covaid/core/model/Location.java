package org.covaid.core.model;

import java.util.Iterator;
import java.util.Map;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;

public class Location extends AbstractLocation<Integer>{
	
	public Location(int xpos, int ypos) {
		super(xpos, ypos);
	}

	public Location(IPoint point) {
		super(point);
	}

	public Location(IPoint point, Map<IContagion<Integer>, ContagionData<Integer>> contagions) {
		super(point, contagions);
	}

	public Location(String identifier, int xpos, int ypos) {
		super(identifier, xpos, ypos);
	}

	public Location(String identifier, IPoint point) {
		super(identifier, point);
	}

	@Override
	protected ILocation<Integer> createLocation(String identifier, IPoint point) {
		return new Location( identifier, point );
	}
	
	@Override
	public ILocation<Integer> clone() {
		IPoint point = super.toPoint();
		ILocation<Integer> result = new Location( point );
		Iterator<Map.Entry<IContagion<Integer>, ContagionData<Integer>>> iterator = super.getContagions().entrySet().iterator();
		while( iterator.hasNext() ) {
			Map.Entry<IContagion<Integer>, ContagionData<Integer>> entry = iterator.next();
			result.addContagion(entry.getValue().getTimeStep(), entry.getKey());
		}
		return result;
	}

	/**
	 * Create a new location from the reference by adding the highest contagions
	 * from loc2
	 * @param reference
	 * @param loc2
	 * @return
	 */
	public static <T extends Object> long getMaxContagionTime( ILocation<T> reference ) {
		long result = 0;
		for( IContagion<T> contagion: reference.getContagion() ) {
			if( contagion.getIncubation() > result)
				result = contagion.getIncubation();
		}
		return 2*result;
	}

}
