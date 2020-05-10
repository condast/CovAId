package org.covaid.core.model;

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

	/**
	 * Create a new location from the reference by adding the highest contagions
	 * from loc2
	 * @param reference
	 * @param loc2
	 * @return
	 */
	public static ILocation<Integer> createWorseCase( ILocation<Integer> reference, ILocation<Integer> loc2 ) {
		Location worst = new Location( reference );
		for( IContagion<Integer> contagion: reference.getContagion() ) {
			double test = loc2.getContagion(contagion);
			if( contagion.getContagiousness() < test)
				worst.addContagion(contagion);
		}
		return worst;
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
