package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Location extends Point implements Comparable<Point>{
	
	private Map<Contagion, Double> contagions;

	public Location( Point point) {
		this( point.getXpos(), point.getYpos());
	}

	public Location( int xpos, int ypos) {
		this( createIdentifier(xpos, ypos), xpos, ypos);
	}
	
	/**
	 * A location needs a unique identifier in order for it to distinguish itself from
	 * other locations. A postcode could work, or a string representation of LatLng coordinates 
	 * @param identifier
	 * @param xpos
	 * @param ypos
	 */
	public Location( String identifier, int xpos, int ypos) {
		super( identifier, xpos, ypos);
		contagions = new HashMap<>();
	}

	public void addContagion( Contagion contagion ) {
		contagions.put(contagion, contagion.getContagiousness());
	}

	public boolean removeContagion( Contagion contagion ) {
		return ( contagions.remove(contagion) != null );
	}

	public double getContagion( Contagion contagion ) {
		Double result = this.contagions.get(contagion);
		return ( result == null )?0: result;
	}

	public Contagion[] getContagion() {
		return this.contagions.keySet().toArray( new Contagion[ this.contagions.size()]);
	}

	public boolean isContagious( Date date ) {
		for( Contagion contagion: this.contagions.keySet() ) {
			if( contagion.isContagious( date ))
				return true;
		}
		return false;
	}
	
	public boolean isContagious( long days ) {
		for( Contagion contagion: this.contagions.keySet() ) {
			if( contagion.isContagious(days))
				return true;
		}
		return false;
	}
	
	public boolean updateContagion( Contagion contagion ) {
		boolean result = false;
		if( contagion == null )
			return result;
		this.contagions.put(contagion, contagion.getContagiousness());
		return true;
	}

	/**
	 * Return the list of contagions that are worse in the given location.
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	public Contagion[] isWorse( Location location ) {
		Collection<Contagion> results = new ArrayList<>();
		if( location.compareTo(this) != 0 )
			return new Contagion[0];//invalid comparison
		
		for( Contagion contagion: this.contagions.keySet() ) {
			double compare = location.getContagion( contagion);
			if( compare  > contagion.getContagiousness())
				results.add(contagion);
		}
		return results.toArray( new Contagion[ results.size()]);
	}

	public Point toPoint() {
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
		if(!( obj instanceof Location))
			return false;
		Location test = (Location) obj;
		return (( test.getXpos() - getXpos() == 0 ) && ( test.getYpos() - getYpos() == 0 ));
	}
}
