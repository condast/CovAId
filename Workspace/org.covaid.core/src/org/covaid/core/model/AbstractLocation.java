package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;

public abstract class AbstractLocation<T extends Object> extends Point implements ILocation<T>{
	
	private Map<IContagion<T>, Double> contagions;

	public AbstractLocation( IPoint point) {
		this( point.getXpos(), point.getYpos());
	}

	public AbstractLocation( String identifier, IPoint point) {
		this( identifier, point.getXpos(), point.getYpos());
	}

	public AbstractLocation( int xpos, int ypos) {
		this( createIdentifier(xpos, ypos), xpos, ypos);
	}
	
	/**
	 * A location needs a unique identifier in order for it to distinguish itself from
	 * other locations. A postcode could work, or a string representation of LatLng coordinates 
	 * @param identifier
	 * @param xpos
	 * @param ypos
	 */
	public AbstractLocation( String identifier, int xpos, int ypos) {
		super( identifier, xpos, ypos);
		contagions = new HashMap<>();
	}

	@Override
	public void addContagion( IContagion<T> contagion ) {
		contagions.put( contagion, contagion.getContagiousness());
	}

	@Override
	public boolean removeContagion( IContagion<T> contagion ) {
		return ( contagions.remove(contagion) != null );
	}

	@Override
	public double getContagion( IContagion<T> contagion ) {
		Double result = this.contagions.get(contagion);
		return ( result == null )?0: result;
	}

	protected Map<IContagion<T>, Double> getContagions() {
		return contagions;
	}

	@Override
	public IContagion<T> getContagion( String identifier ) {
		for( IContagion<T> contagion: this.contagions.keySet() ){
			if( contagion.getIdentifier().equals(identifier))
				return contagion;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IContagion<T>[] getContagion() {
		return this.contagions.keySet().toArray( new IContagion[ this.contagions.size()]);
	}

	@Override
	public boolean isContagious( T step ) {
		for( IContagion<T> contagion: this.contagions.keySet() ) {
			if( contagion.isContagious( step ))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isContagious( long days ) {
		for( IContagion<T> contagion: this.contagions.keySet() ) {
			if( contagion.isContagious(days))
				return true;
		}
		return false;
	}
	
	/**
	 * Return true if the given location is more contagious than this one. This
	 * means that there is at least one more contagious infection
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	@Override
	public boolean isWorse( ILocation<T> location ) {
		for( IContagion<T> contagion: this.contagions.keySet() ) {
			double compare = location.getContagion( contagion);
			if( compare  > contagion.getContagiousness())
				return true;
		}
		return false;
	}

	/**
	 * Return the list of contagions that are worse in the given location.
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IContagion<T>[] getWorse( ILocation<T> location ) {
		Collection<IContagion<T>> results = new ArrayList<>();
		if( location.compareTo(this) != 0 )
			return results.toArray( new IContagion[ results.size()]);
		
		for( IContagion<T> contagion: this.contagions.keySet() ) {
			double compare = location.getContagion( contagion);
			if( compare  > contagion.getContagiousness())
				results.add(contagion);
		}
		return results.toArray( new IContagion[ results.size()]);
	}

	protected abstract ILocation<T> createLocation( String identifier, IPoint point );


	/**
	 * Returns the worst possible situation when combining the contagiousness of both locations
	 * Returns null if the locations are not the same
	 * @param location
	 * @return
	 */
	@Override
	public ILocation<T> createWorst( ILocation<T> location ) {
		if( location.compareTo(this) != 0 )
			return null;//invalid comparison
		
		ILocation<T> worst = createLocation( location.getIdentifier(), location.toPoint());
		for( IContagion<T> contagion: this.contagions.keySet() ) {
			double compare = location.getContagion( contagion);
			if( compare  > contagion.getContagiousness())
				worst.addContagion(contagion);
			else
				worst.addContagion(location.getContagion( contagion.getIdentifier()));
		}
		return worst;
	}

	@Override
	public IPoint toPoint() {
		return new Point( this.getXpos(), this.getYpos());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if( super.equals(obj))
			return true;
		if(!( obj instanceof AbstractLocation))
			return false;
		AbstractLocation<?> test = (AbstractLocation<?>) obj;
		return (( test.getXpos() - getXpos() == 0 ) && ( test.getYpos() - getYpos() == 0 ));
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
