package org.covaid.mobile.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.Contagion;

@Entity(name="LOCATION")
public class Location extends Point implements ILocation{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@OneToMany(mappedBy="contagion", cascade = CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval = true)
	private Map<Contagion, Double> contagions;

	public Location( IPoint point) {
		this( point.getXpos(), point.getYpos());
	}

	public Location( String identifier, IPoint point) {
		this( identifier, point.getXpos(), point.getYpos());
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

	public void addContagion( IContagion contagion ) {
		contagions.put((Contagion)contagion, contagion.getContagiousness());
	}

	public boolean removeContagion( IContagion contagion ) {
		return ( contagions.remove(contagion) != null );
	}

	public double getContagion( IContagion contagion ) {
		Double result = this.contagions.get(contagion);
		return ( result == null )?0: result;
	}

	@Override
	public IContagion getContagion( String identifier ) {
		for( IContagion contagion: this.contagions.keySet() ){
			if( contagion.getIdentifier().equals(identifier))
				return contagion;
		}
		return null;
	}

	public IContagion[] getContagion() {
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
	
	public boolean updateContagion( IContagion contagion ) {
		boolean result = false;
		if( contagion == null )
			return result;
		this.contagions.put((Contagion)contagion, contagion.getContagiousness());
		return true;
	}

	/**
	 * Return the list of contagions that are worse in the given location.
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	public IContagion[] getWorse( ILocation location ) {
		Collection<IContagion> results = new ArrayList<>();
		if( location.compareTo((IPoint)this) != 0 )
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

	/**
	 * Return true if the given location is more contagious than this one. This
	 * means that there is at least one more contagious infection
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	@Override
	public boolean isWorse( ILocation location ) {
		for( IContagion contagion: this.contagions.keySet() ) {
			double compare = location.getContagion( contagion);
			if( compare  > contagion.getContagiousness())
				return true;
		}
		return false;
	}

	/**
	 * Returns the worst possible situation when combining the contagiousness of both locations
	 * Returns null if the locations are not the same
	 * @param location
	 * @return
	 */
	@Override
	public ILocation createWorst( ILocation location ) {
		if( location.compareTo(this) != 0 )
			return null;//invalid comparison
		
		ILocation worst = new Location( location.getIdentifier(), location.toPoint());
		for( IContagion contagion: this.contagions.keySet() ) {
			double compare = location.getContagion( contagion);
			if( compare  > contagion.getContagiousness())
				worst.addContagion(contagion);
			else
				worst.addContagion(location.getContagion( contagion.getIdentifier()));
		}
		return worst;
	}
}
