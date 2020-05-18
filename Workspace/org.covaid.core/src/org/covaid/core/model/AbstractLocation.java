package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;

public abstract class AbstractLocation<T extends Object> extends Point implements ILocation<T>{
	
	private Map<IContagion<T>, T> contagions;
	
	protected AbstractLocation( IPoint point) {
		this( point.getXpos(), point.getYpos());
	}

	protected AbstractLocation( IPoint point, Map<IContagion<T>, T> contagions) {
		this( point.getXpos(), point.getYpos());
		this.contagions = contagions;
	}

	protected AbstractLocation( String identifier, IPoint point) {
		this( identifier, point.getXpos(), point.getYpos());
	}

	protected AbstractLocation( int xpos, int ypos) {
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
	public void addContagion( T timestamp, IContagion<T> contagion ) {
		contagions.put( contagion, timestamp);
	}

	@Override
	public boolean removeContagion( IContagion<T> contagion ) {
		return ( contagions.remove(contagion) != null );
	}

	@Override
	public boolean isInfected( IContagion<T> contagion ) {
		return contagion.isContagious( this.contagions.get(contagion));
	}

	@Override
	public boolean isHealthy() {
		if( this.contagions.isEmpty())
			return true;
		Iterator<Map.Entry<IContagion<T>, T>> iterator = this.contagions.entrySet().iterator();
		while( iterator.hasNext() ) {
			Map.Entry<IContagion<T>, T> entry = iterator.next();
			if( entry.getKey().isContagious(entry.getValue()))
				return false;
		}
		return true;
	}

	@Override
	public boolean isHealthy( IContagion<T> contagion) {
		if( this.contagions.isEmpty())
			return true;
		return !contagion.isContagious(this.contagions.get(contagion));
	}

	@Override
	public T getInfectionDate( IContagion<T> contagion ) {
		return this.contagions.get(contagion);
	}

	protected Map<IContagion<T>, T> getContagions() {
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

	@Override
	public double getContagion( IContagion<T> contagion, T step ) {
		T infection = getInfectionDate(contagion);
		return ( step == null )?0: contagion.getContagiousness(infection, step);
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

	/**
	 * Get the actual state of the contagiousness in this hub
	 * @param step
	 * @return
	 */
	public Map<IContagion<T>, Double> getContagions( T step ){
		Map<IContagion<T>, Double> results = new HashMap<>();
		Iterator<Map.Entry<IContagion<T>, T>> iterator = this.contagions.entrySet().iterator();
		while( iterator.hasNext() ) {
			Map.Entry<IContagion<T>, T> entry = iterator.next();
			results.put(entry.getKey(), entry.getKey().getContagiousness(entry.getValue(), step));
		}
		return results;
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
		Collection<IContagion<T>> results = new TreeSet<>( Arrays.asList( location.getContagion()));
		results.addAll(this.contagions.keySet());
		for( IContagion<T> contagion: results ) {
			double compare = location.getContagion( contagion, location.getInfectionDate(contagion));
			if( compare < getContagion( contagion, getInfectionDate(contagion) ))
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
			double compare = location.getContagion( contagion, location.getInfectionDate(contagion));
			if( compare  > contagion.getContagiousness())
				results.add(contagion);
		}
		return results.toArray( new IContagion[ results.size()]);
	}

	protected abstract ILocation<T> createLocation( String identifier, IPoint point );

	/**
	 * Returns the worst possible situation when combining the contagiousness of both locations
	 * Returns null if the locations are not the same
	 * @param check
	 * @return
	 */
	@Override
	public ILocation<T> createWorst( ILocation<T> check ) {
		if( check.compareTo(this) != 0 )
			return null;//invalid comparison
		
		ILocation<T> worst = createLocation( this.getIdentifier(), this.toPoint());
		return createWorst(worst, this, check);
	}

	@Override
	public IPoint toPoint() {
		return new Point( this.getXpos(), this.getYpos());
	}
	
	/**
	 * move this location to the given point
	 * @param point
	 * @return
	 */
	@Override
	public void move( IPoint point ) {
		super.setPosition(point.getXpos(), point.getYpos());
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
	
	public abstract ILocation<T> clone();
	
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
	
	/**
	 * Returns the worst possible situation when combining the contagiousness of both locations
	 * Returns null if the locations are not the same
	 * @param check
	 * @return
	 */
	public static <T extends Object> ILocation<T> createWorst( ILocation<T> destination ,ILocation<T> source, ILocation<T> check ) {
		Collection<IContagion<T>> contagions = new TreeSet<>( Arrays.asList( source.getContagion()));
		contagions.addAll( Arrays.asList( check.getContagion()));
		for( IContagion<T> contagion: contagions ) {
			double compare = check.getContagion( contagion, check.getInfectionDate(contagion) );
			T infection = source.getInfectionDate(contagion);
			T timestamp = ( compare  > source.getContagion(contagion, infection))? check.getInfectionDate(contagion): check.getInfectionDate(contagion);
			destination.addContagion(timestamp, contagion);
		}
		return destination;
	}

}
