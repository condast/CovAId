package org.covaid.core.operators;

import org.condast.commons.data.util.Vector;
import org.condast.commons.number.NumberUtils;
import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;

public abstract class AbstractContagionOperator<T extends Object> implements IContagionOperator<T> {

	private T current;
	
	private IContagion contagion;
	
	protected AbstractContagionOperator() {
		super();
	}
	
	protected AbstractContagionOperator( T current, IContagion contagion ) {
		this.current = current;
		this.contagion = contagion;
	}
	
	/**
	 * Returns true if the first is smaller than the last
	 * @param first
	 * @param last
	 * @return
	 */
	public boolean isSmaller(T first, T last) {
		return getDifference(first, last)<=0;
	}

	@Override
	public T getCurrent() {
		return current;
	}

	@Override
	public void setCurrent(T init) {
		this.current = init;
	}

	@Override	
	public IContagion getContagion() {
		return contagion;
	}

	@Override
	public void setContagion(IContagion contagion) {
		if( contagion == null )
			throw new NullPointerException();
		this.contagion = contagion;
	}
	
	/**
	 * Get the contagion as it spreads in the seconds after contact
	 * <contagion, radius>
	 * 
	 * @param contagion
	 * @param current
	 * @param distance
	 * @return
	 */
	@Override
	public Vector<Double,Double> getContagion( T current) {
		long diff = Math.abs( getDifference( current, current ));
		double newContagion = NumberUtils.clipRange(0, 100, contagion.getContagiousness() * contagion.getHalfTime() * IContagion.DAY/diff);
		double radius = contagion.getDistance() + contagion.getDispersion()* diff*1000;
		return new Vector<Double, Double>(newContagion, radius );	
	}

	/**
	 * Get the contagion as it spreads in the seconds after contact
	 * <contagion, radius>
	 * 
	 * @param contagion
	 * @param step
	 * @param distance
	 * @return
	 */
	@Override
	public Vector<Double,Double> getContagionRange( T start) {
		long diff = Math.abs( getDifference( start, current ));
		double newContagion = NumberUtils.clipRange(0, 100, contagion.getContagiousness() * contagion.getHalfTime() * IContagion.DAY/diff);
		double radius = contagion.getDistance() + contagion.getDispersion()* diff*1000;
		return new Vector<Double, Double>(newContagion, radius );	
	}

	/**
	 * Returns true if the this contagiousness is larger than the given one
	 * @param contagion
	 * @return
	 */
	@Override
	public boolean isLarger( IContagion contagion ) {
		return contagion.getContagiousness() < contagion.getContagiousness();
	}

	@Override
	public boolean isContagious( T init, double risk ) {
		double contagiousness = risk * getContagiousness( init );
		return ( contagiousness > contagion.getThreshold() );
	}

	@Override
	public boolean isContagious( ContagionData<T> data ) {
		if( data == null )
			return false;
		double contagiousness = getContagiousness( data );
		return ( contagiousness > contagion.getThreshold() );
	}

	@Override
	public boolean isInfected( ContagionData<T> data ) {
		return isContagious( data );
	}

	@Override
	public boolean isHealthy( ContagionData<T> data ) {
		return !isContagious( data);
	}

	@Override
	public double getContagiousness( T init) {
		long diff = getDifference( init, current);
		if( contagion == null )
			return 0;
		return 100*(( diff < contagion.getIncubation() )? 1: (double)contagion.getIncubation()/diff);
	}

	@Override
	public double getContagiousness( ContagionData<T> data) {
		if( data == null )
			return 0;
		long diff = getDifference( current, data.getMoment());
		return data.getRisk()*(( diff < contagion.getIncubation() )? 1: (double)contagion.getIncubation()/diff);
	}

	@Override
	public double getTransferContagiousness( ContagionData<T> data) {
		if( data == null )
			return 0;
		long diff = getDifference( current, data.getMoment());
		return ( diff == 0 )?data.getRisk(): data.getRisk()/(2 + diff );
	}

	@Override
	public int compareTo(T o) {
		long diff = getDifference(current, o);
		return (int) diff;
	}	
}
