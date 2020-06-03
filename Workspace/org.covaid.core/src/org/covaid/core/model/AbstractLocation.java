package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.covaid.core.data.ContagionData;
import org.covaid.core.data.frogger.LocationData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;
import org.covaid.core.operators.IContagionOperator;

public abstract class AbstractLocation<T extends Object> extends Point implements ILocation<T>{
	
	public static final String S_ERR_ILLEGAL_RISK_VALUE = "The risk should be inbetween <0,100>: ";
	
	private Map<IContagion, ContagionData<T>> contagions;
	
	private IContagionOperator<T> operator;
	
	protected AbstractLocation( IPoint point, IContagionOperator<T> operator) {
		this( point.getXpos(), point.getYpos(), operator);
	}

	protected AbstractLocation( IPoint point, Map<IContagion, ContagionData<T>> contagions, IContagionOperator<T> operator) {
		this( point.getXpos(), point.getYpos(), operator );
		this.contagions = contagions;
	}

	protected AbstractLocation( String identifier, IPoint point, IContagionOperator<T> operator) {
		this( identifier, point.getXpos(), point.getYpos(), operator);
	}

	protected AbstractLocation( int xpos, int ypos, IContagionOperator<T> operator) {
		this( createIdentifier(xpos, ypos), xpos, ypos, operator);
	}
	
	/**
	 * A location needs a unique identifier in order for it to distinguish itself from
	 * other locations. A postcode could work, or a string representation of LatLng coordinates 
	 * @param identifier
	 * @param xpos
	 * @param ypos
	 */
	public AbstractLocation( String identifier, int xpos, int ypos, IContagionOperator<T> operator) {
		super( identifier, xpos, ypos);
		this.operator = operator;
		contagions = new HashMap<>();
	}

	@Override
	public IContagionOperator<T> getOperator() {
		return operator;
	}

	@Override
	public void addContagion( T timestamp, IContagion contagion ) {
		contagions.put( contagion, new ContagionData<T>( timestamp ));
	}

	@Override
	public boolean addContagion( T timestamp, IContagion contagion, double risk ) {
		if(( risk < 0 ) || ( risk > ContagionData.MAX_CONTAGIOUSNESS )) {
			throw new IllegalArgumentException( S_ERR_ILLEGAL_RISK_VALUE + risk);
		}
		if( risk < Double.MIN_VALUE)
			return true;
		contagions.put( contagion, new ContagionData<T>( timestamp, risk ));
		return true;
	}

	@Override
	public boolean removeContagion( IContagion contagion ) {
		return ( contagions.remove(contagion) != null );
	}

	@Override
	public boolean isInfected( IContagion contagion, T step ) {
		ContagionData<T> data = this.contagions.get( contagion);
		operator.setCurrent(step);
		operator.setContagion(contagion);
		return operator.isInfected(data);
	}

	@Override
	public boolean isHealthy( T step ) {
		if( this.contagions.isEmpty())
			return true;
		for( Map.Entry<IContagion, ContagionData<T>> entry: this.contagions.entrySet()) {
			operator.setCurrent(step);
			operator.setContagion(entry.getKey());
			if( !operator.isHealthy(entry.getValue()))
				return false;
		}
		return true;
	}

	@Override
	public boolean isHealthy( IContagion contagion, T step) {
		if( this.contagions.isEmpty())
			return true;
		ContagionData<T> data = this.contagions.get( contagion);
		operator.setCurrent(step);
		operator.setContagion(contagion);
		return (data == null )?true: !operator.isContagious( data.getMoment(), data.getRisk() );
	}

	@Override
	public T getInfectionDate( IContagion contagion ) {
		ContagionData<T> data = this.contagions.get( contagion);
		return ( data == null )?null: data.getMoment();
	}

	protected Map<IContagion, ContagionData<T>> getContagions() {
		return contagions;
	}

	@Override
	public synchronized double getContagion( IContagion contagion, T step ) {
		ContagionData<T> data = this.contagions.get( contagion);
		if ( data == null )
			return 0;
		T infection = getInfectionDate(contagion);
		operator.setCurrent(step);
		operator.setContagion(contagion);
		return ( infection == null )?0: data.getRisk() * operator.getContagiousness(infection)/100;
	}

	@Override
	public double getRisk( IContagion contagion, T step ) {
		if( step == null )
			return ContagionData.MAX_CONTAGIOUSNESS;
		if( this.contagions.isEmpty() )
			return 0;
		ContagionData<T> data = this.contagions.get( contagion );
		if( data == null )
			return 0;
		operator.setCurrent(step);
		return data.getRisk() * operator.getContagiousness(data.getMoment())/100;
	}

	@Override
	public boolean setRisk( IContagion contagion, T step, double risk ) {
		if(( risk < 0 ) || ( risk > ContagionData.MAX_CONTAGIOUSNESS )) {
			throw new IllegalArgumentException( S_ERR_ILLEGAL_RISK_VALUE + risk);
		}
		ContagionData<T> data = this.contagions.get( contagion );
		if(( data == null) && (risk < Double.MIN_VALUE ))
			return true;
		if( data == null ) {
			data = new ContagionData<T>( step, risk );
			this.contagions.put(contagion, data);
		}else
			data.setRisk(risk);
		return true;
	}
	
	@Override
	public ContagionData<T> get( IContagion contagion ) {
		return this.contagions.get(contagion);
	}

	@Override
	public IContagion[] getContagion() {
		return this.contagions.keySet().toArray( new IContagion[ this.contagions.size()]);
	}

	@Override
	public boolean isContagious( T step ) {
		operator.setCurrent(step);
		for( Map.Entry<IContagion, ContagionData<T>> contagion: this.contagions.entrySet() ) {
			if( operator.isContagious( contagion.getValue().getMoment(), contagion.getValue().getRisk() ))
				return true;
		}
		return false;
	}

	/**
	 * Get the actual state of the contagiousness in this hub
	 * @param step
	 * @return
	 */
	public Map<IContagion, Double> getContagions( T step ){
		Map<IContagion, Double> results = new HashMap<>();
		Iterator<Map.Entry<IContagion, ContagionData<T>>> iterator = this.contagions.entrySet().iterator();
		operator.setCurrent(step);
		while( iterator.hasNext() ) {
			Map.Entry<IContagion, ContagionData<T>> entry = iterator.next();
			results.put(entry.getKey(), operator.getContagiousness(entry.getValue().getMoment()));
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
		Collection<IContagion> results = new TreeSet<>( Arrays.asList( location.getContagion()));
		results.addAll(this.contagions.keySet());
		for( IContagion contagion: results ) {
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
	@Override
	public IContagion[] getWorse( ILocation<T> location ) {
		Collection<IContagion> results = new ArrayList<>();		
		Collection<IContagion> test = new TreeSet<>( this.contagions.keySet());
		test.addAll(Arrays.asList(location.getContagion()));
		for( IContagion contagion: test ) {
			double compare = location.getContagion( contagion, location.getInfectionDate(contagion));
			if( compare  > this.getContagion(contagion, this.getInfectionDate(contagion)))
				results.add(contagion);
		}
		return results.toArray( new IContagion[ results.size()]);
	}

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
		
		ILocation<T> worst = this.clone();
		return createWorst(worst, this, check);
	}

	@Override
	public IPoint toPoint() {
		return new Point( this.getXpos(), this.getYpos());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public LocationData<T> toLocationData() {
		return new LocationData((Point) this.toPoint(), this.contagions );
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
		for( IContagion contagion: reference.getContagion() ) {
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
		Collection<IContagion> contagions = new TreeSet<>( Arrays.asList( source.getContagion()));
		contagions.addAll( Arrays.asList( check.getContagion()));
		for( IContagion contagion: contagions ) {
			double compare = check.getContagion( contagion, check.getInfectionDate(contagion) );
			T infection = source.getInfectionDate(contagion);
			T timestamp = null;
			double risk = 0;
			if( compare  > source.getContagion(contagion, infection)) {
				timestamp = check.getInfectionDate(contagion );
				risk = check.getRisk(contagion, timestamp);
				destination.setRisk(contagion, timestamp, risk);
			}else{
				timestamp = check.getInfectionDate(contagion);
				risk = source.getRisk(contagion, timestamp);
				destination.setRisk(contagion, timestamp, risk);
			}
			destination.addContagion(timestamp, contagion);
		}
		return destination;
	}

}
