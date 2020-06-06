package org.covaid.core.hub.trace;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Logger;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;
import org.covaid.core.hub.IHub;
import org.covaid.core.operators.IContagionOperator;

public abstract class AbstractTrace<T extends Object> implements ITrace<T>{

	private IHub<T> hub;
	private T current;
	private IContagionOperator<T> operator;
	
	private Map<IPoint, HubTrace> traces;
	
	private boolean enabled;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	protected AbstractTrace( T current, IContagionOperator<T> operator, boolean enabled) {
		traces = new ConcurrentSkipListMap<>();
		this.operator = operator;
		this.current = current;
		this.enabled = enabled;
	}

	protected AbstractTrace( IHub<T> hub, T current, IContagionOperator<T> operator) {
		this( current, operator, false );
		this.hub = hub;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void setHub(IHub<T> hub) {
		this.hub = hub;
	}
	
	protected T getCurrent() {
		return current;
	}

	protected HubTrace addHubTrace( ILocation<T> location ) {
		HubTrace hubTrace = new HubTrace();
		this.traces.put(location, hubTrace);
		return hubTrace;
	}
	
	@Override
	public synchronized Map<T, Double> getPrediction( IContagion contagion, T range ){
		Map<T, Double> results = new TreeMap<>( new TimeComparator());
		this.operator.setCurrent(current);
		for( Map.Entry<IPoint, HubTrace> entry: this.traces.entrySet()) {
			ContagionData<T> data = entry.getValue().get(contagion);
			if(( data == null ) || !operator.isInRange(range, data.getMoment()))
				continue;
			Double check = results.get(data.getMoment());
			double risk = ( check==null)?0: check;
			if( data.getRisk() > risk)
				results.put( operator.subtract(current, data.getMoment()), data.getRisk());
		}
		if( results.isEmpty())
			return results;
		return results;
	}

	@Override
	public synchronized Map<T, Double> getPrediction( IContagion contagion, T range, Map<IPoint,? extends IHub<T>> hubs ){
		Map<T, Double> results = new TreeMap<>( new TimeComparator());
		this.operator.setCurrent(current);
		for( Map.Entry<IPoint, HubTrace> entry: this.traces.entrySet()) {
			ContagionData<T> data = entry.getValue().get(contagion);
			if(( data == null ) || !operator.isInRange(range, data.getMoment()))
				continue;
			IHub<T> hub = hubs.get(entry.getKey());
			if(( hub == null ) || hub.getLocation().isHealthy(contagion, current))
				continue;
			Double drisk = hub.getLocation().getRisk(contagion, current);
			double risk = ( drisk == null )?0: drisk*data.getRisk()/100;
			T time = operator.subtract(current, data.getMoment());
			time = operator.subtract(range, time);
			results.put( time,risk);
		}
		return results;
	}

	protected Map<IPoint, ContagionData<T>> getTraceMap( IContagion contagion, T range ){
		Map<IPoint, ContagionData<T>> results = new TreeMap<>();
		this.operator.setCurrent(current);
		for( Map.Entry<IPoint, HubTrace> entry: this.traces.entrySet()) {
			ContagionData<T> data = entry.getValue().get(contagion);
			if( !operator.isInRange(range, data.getMoment()))
				continue;
			results.put(entry.getKey(), data);
		}
		return results;
	}

	/**
	 * This trace has an increased risk of contagion. One of the guests is a possible source
	 */
	@Override
	public boolean update( IContagion contagion, T current, ILocation<T> guest ) {
		//If the guest is not infected, then ignore it
		if( !enabled )
			return false;
		if( !guest.isInfected(contagion, current))
			return false;
		this.current = current;
		ILocation<T> location = hub.getLocation();
		if( !location.isInfected(contagion, current))
			return false;
		
		//both hubs are infected, so see if a trace is already present
		ContagionData<T> data = hub.getLocation().get(contagion);
		
		//Check to see if the guest is more recent than this one, for then it can't be a source
		ContagionData<T> compare = guest.get(contagion);
		if( operator.isSmaller(data.getMoment(), compare.getMoment()))
			return false;
		
		logger.fine("compare guest " + guest.toString() + ": " + data.toString() + "-" + compare.toString());
		HubTrace ct = this.traces.get( guest.toPoint());
		if( ct == null )
			ct = addHubTrace( guest );
		ct.update(contagion, current, guest);
		
		logger.fine(toString());
		return true;
	}
	
	protected abstract T onGetAverage( T first, T second );
	
	@Override
	public String toString() {
		return this.hub.toString() + ": " + traces;
	}

	private class HubTrace{
		
		private Map<IContagion,ContagionData<T>> data;

		public HubTrace() {
			this.data = new HashMap<>();
		}
		
		public ContagionData<T> get( IContagion contagion ) {
			return this.data.get(contagion);
		}
		
		private void update( IContagion contagion, T current, ILocation<T> compare) {
			double risk = 0;
			operator.setCurrent(current);
			operator.setContagion(contagion);
			T calc = current;
			ContagionData<T> source = this.data.get(contagion);
			ContagionData<T> data = compare.get(contagion);
			long diff = Math.abs( operator.getDifference(current, data.getMoment()));
			if( source == null ) {
				source = data.clone();
				this.data.put(contagion, source);
				risk = operator.getTransferContagiousness( data )/2;				
				calc = onGetAverage(current, data.getMoment());
			}else{
				diff = Math.abs( operator.getDifference(source.getMoment(), data.getMoment()));
				if( diff == 0 )
					diff = 1;
				risk = ( source.getRisk() + operator.getTransferContagiousness( data ) )/diff;
				calc = onGetAverage(source.getMoment(), data.getMoment());
			}
			source.setRisk(risk);
			source.setTimeStep(calc);
		}

		@Override
		public String toString() {
			return this.data.toString();
		}
	}
	
	private class TimeComparator implements Comparator<T>{

		@Override
		public int compare(T o1, T o2) {
			return (int) operator.getDifference(o1, o2);
		}
		
	}
}
