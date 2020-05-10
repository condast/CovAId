package org.covaid.core.model.date;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.Point;

public class DateLocation extends Point implements ILocation<Date>{
	
	private Map<IContagion<Date>, Double> contagions;

	public DateLocation( IPoint point) {
		this( point.getXpos(), point.getYpos());
	}

	public DateLocation( String identifier, IPoint point) {
		this( identifier, point.getXpos(), point.getYpos());
	}

	public DateLocation( int xpos, int ypos) {
		this( createIdentifier(xpos, ypos), xpos, ypos);
	}
	
	/**
	 * A location needs a unique identifier in order for it to distinguish itself from
	 * other locations. A postcode could work, or a string representation of LatLng coordinates 
	 * @param identifier
	 * @param xpos
	 * @param ypos
	 */
	public DateLocation( String identifier, int xpos, int ypos) {
		super( identifier, xpos, ypos);
		contagions = new HashMap<>();
	}

	@Override
	public void addContagion( IContagion<Date> contagion ) {
		contagions.put((DateContagion)contagion, contagion.getContagiousness());
	}

	@Override
	public boolean removeContagion( IContagion<Date> contagion ) {
		return ( contagions.remove(contagion) != null );
	}

	@Override
	public double getContagion( IContagion<Date> contagion ) {
		Double result = this.contagions.get(contagion);
		return ( result == null )?0: result;
	}

	protected Map<IContagion<Date>, Double> getContagions() {
		return contagions;
	}

	@Override
	public IContagion<Date> getContagion( String identifier ) {
		for( IContagion<Date> contagion: this.contagions.keySet() ){
			if( contagion.getIdentifier().equals(identifier))
				return contagion;
		}
		return null;
	}

	@Override
	public IContagion<Date>[] getContagion() {
		return this.contagions.keySet().toArray( new DateContagion[ this.contagions.size()]);
	}

	@Override
	public boolean isContagious( Date date ) {
		for( IContagion<Date> contagion: this.contagions.keySet() ) {
			if( contagion.isContagious( date ))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isContagious( long days ) {
		for( IContagion<Date> contagion: this.contagions.keySet() ) {
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
	public boolean isWorse( ILocation<Date> location ) {
		for( IContagion<Date> contagion: this.contagions.keySet() ) {
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
	public IContagion<Date>[] getWorse( ILocation<Date> location ) {
		Collection<IContagion<Date>> results = new ArrayList<>();
		if( location.compareTo(this) != 0 )
			return new IContagion[0];//invalid comparison
		
		for( IContagion<Date> contagion: this.contagions.keySet() ) {
			double compare = location.getContagion( contagion);
			if( compare  > contagion.getContagiousness())
				results.add(contagion);
		}
		return results.toArray( new DateContagion[ results.size()]);
	}

	/**
	 * Returns the worst possible situation when combining the contagiousness of both locations
	 * Returns null if the locations are not the same
	 * @param location
	 * @return
	 */
	@Override
	public ILocation<Date> createWorst( ILocation<Date> location ) {
		if( location.compareTo(this) != 0 )
			return null;//invalid comparison
		
		ILocation<Date> worst = new DateLocation( location.getIdentifier(), location.toPoint());
		for( IContagion<Date> contagion: this.contagions.keySet() ) {
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
		if(!( obj instanceof DateLocation))
			return false;
		DateLocation test = (DateLocation) obj;
		return (( test.getXpos() - getXpos() == 0 ) && ( test.getYpos() - getYpos() == 0 ));
	}
	
	/**
	 * Create a new location from the reference by adding the highest contagions
	 * from loc2
	 * @param reference
	 * @param loc2
	 * @return
	 */
	public static ILocation<Date> createWorseCase( ILocation<Date> reference, ILocation<Date> loc2 ) {
		DateLocation worst = new DateLocation( reference );
		for( IContagion<Date> contagion: reference.getContagion() ) {
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
	public static long getMaxContagionTime( ILocation<Date> reference ) {
		long result = 0;
		for( IContagion<Date> contagion: reference.getContagion() ) {
			if( contagion.getIncubation() > result)
				result = contagion.getIncubation();
		}
		return 2*result;
	}

}
