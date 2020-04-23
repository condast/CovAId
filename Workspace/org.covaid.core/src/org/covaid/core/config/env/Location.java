package org.covaid.core.config.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.covaid.core.config.env.Contagion.SupportedContagion;

public class Location extends Point implements Comparable<Point>{
	
	private Map<String, Contagion> contagions;

	public Location( Point point) {
		this( point.getXpos(), point.getYpos());
	}

	public Location( int xpos, int ypos) {
		this( createIdentifier(xpos, ypos), xpos, ypos);
	}
	
	public Location( String identifier, int xpos, int ypos) {
		super( identifier, xpos, ypos);
		contagions = new HashMap<>();
	}

	public void addContagion( Contagion contagion ) {
		contagions.put(contagion.getIdentifier(), contagion);
	}

	public boolean removeContagion( Contagion contagion ) {
		return ( contagions.remove(contagion.getIdentifier()) != null );
	}

	public Contagion getContagion( String identifier ) {
		return this.contagions.get(identifier);
	}

	public Contagion[] getContagion() {
		return this.contagions.values().toArray( new Contagion[ this.contagions.size()]);
	}

	public boolean isContagious( Date date ) {
		for( Contagion contagion: this.contagions.values() ) {
			if( contagion.isContagious( date ))
				return true;
		}
		return false;
	}
	
	public boolean isContagious( long days ) {
		for( Contagion contagion: this.contagions.values() ) {
			if( contagion.isContagious(days))
				return true;
		}
		return false;
	}
	
	public boolean updateContagion( Contagion contagion ) {
		boolean result = false;
		if( contagion == null )
			return result;
		Contagion current = this.contagions.get(contagion.getIdentifier());
		if( current == null) {
			this.addContagion(contagion);
			result = true;
		}else {
			if( contagion.isLarger(current)) {
				this.addContagion(contagion);
				result = true;
			}		
		}
		return result;
	}

	/**
	 * updates the contagion for the given date and location
	 * @param date
	 * @param location
	 * @return
	 */
	public boolean updateContagion( Contagion contagion, Date date, double distance ) {
		if( contagion == null )
			return false;
		boolean result = false;
		for( Contagion cg: this.contagions.values() ) {
			result |= contagion.update(cg , date, distance);
		}
		return result;
	}

	/**
	 * updates the contagion for the given date and location
	 * @param date
	 * @param location
	 * @return
	 */
	public boolean updateContagion( Date date, Location location ) {
		boolean result = false;
		double distance = getDistance(location);
		for( Contagion cg: this.contagions.values() ) {
			result |= cg.update(location.getContagion(cg.getIdentifier()) , date, distance);
		}
		return result;
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
		
		for( Contagion contagion: this.contagions.values() ) {
			Contagion compare = location.getContagion( contagion.getIdentifier());
			if( compare.isLarger(contagion))
				results.add(contagion);
		}
		return results.toArray( new Contagion[ results.size()]);
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
