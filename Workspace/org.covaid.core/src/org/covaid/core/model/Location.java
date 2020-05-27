package org.covaid.core.model;

import java.util.Iterator;
import java.util.Map;

import org.covaid.core.contagion.IntegerContagionOperator;
import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;

public class Location extends AbstractLocation<Integer>{
	
	public Location(int xpos, int ypos) {
		super(xpos, ypos, new IntegerContagionOperator());
	}

	public Location(IPoint point) {
		super(point, new IntegerContagionOperator());
	}

	public Location(IPoint point, Map<IContagion, ContagionData<Integer>> contagions) {
		super(point, contagions, new IntegerContagionOperator());
	}

	public Location(String identifier, int xpos, int ypos) {
		super(identifier, xpos, ypos, new IntegerContagionOperator());
	}

	public Location(String identifier, IPoint point) {
		super(identifier, point, new IntegerContagionOperator());
	}
	
	@Override
	public ILocation<Integer> clone() {
		IPoint point = super.toPoint();
		ILocation<Integer> result = new Location( point );
		Iterator<Map.Entry<IContagion, ContagionData<Integer>>> iterator = super.getContagions().entrySet().iterator();
		while( iterator.hasNext() ) {
			Map.Entry<IContagion, ContagionData<Integer>> entry = iterator.next();
			result.addContagion(entry.getValue().getMoment(), entry.getKey());
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
		for( IContagion contagion: reference.getContagion() ) {
			if( contagion.getIncubation() > result)
				result = contagion.getIncubation();
		}
		return 2*result;
	}

}
