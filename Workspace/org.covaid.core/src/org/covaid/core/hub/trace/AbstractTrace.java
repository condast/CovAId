package org.covaid.core.hub.trace;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.hub.IHub;
import org.covaid.core.operators.IContagionOperator;

public abstract class AbstractTrace<T extends Object> implements ITrace<T>{

	private IHub<T> hub;
	private T current;
	private IContagionOperator<T> operator;
	
	private Map<IHub<T>, IHubTrace<T>> traces;

	protected AbstractTrace( T current, IContagionOperator<T> operator) {
		traces = new TreeMap<>();
		this.operator = operator;
		this.current = current;
	}

	protected AbstractTrace( IHub<T> hub, T current, IContagionOperator<T> operator) {
		this( current, operator );
		this.hub = hub;
	}
	
	@Override
	public IHub<T> getHub() {
		return hub;
	}

	@Override
	public void setHub(IHub<T> hub) {
		this.hub = hub;
	}

	@Override
	public IHubTrace<T> getHubTrace( IHub<T> hub ){
		return this.traces.get(hub);
	}
	
	@Override
	public IHubTrace<T> addHubTrace( IHub<T> hub ) {
		IHubTrace<T> hubTrace = new HubTrace();
		this.traces.put(hub, hubTrace);
		return hubTrace;
	}
	
	@Override
	public Map<T, Double> getPrediction( IContagion contagion, T range ){
		Map<T, Double> results = new TreeMap<>( new TimeComparator());
		this.operator.setCurrent(current);
		for( Map.Entry<IHub<T>, IHubTrace<T>> entry: this.traces.entrySet()) {
			ContagionData<T> data = entry.getValue().get(contagion);
			if( !operator.isLastEntry(range, data.getMoment()))
				continue;
			Double check = results.get(data.getMoment());
			double risk = ( check==null)?0: check;
			if( data.getRisk() > risk)
				results.put( operator.subtract(data.getMoment(), range), data.getRisk());
		}
		return results;
	}

	@Override
	public boolean update( IContagion contagion, T current, ITrace<T> guest ) {
		this.current = current;
		ILocation<T> location = hub.getLocation();
		if( !location.isInfected(contagion, current))
			return false;
		IHubTrace<T> ct = guest.getHubTrace(this.hub);
		if( ct == null )
			ct = guest.addHubTrace( this.hub );
		ct.update(contagion, current, ct);
		return true;
	}
	
	protected abstract T onGetAverage( T first, T second );
	
	/*
	public boolean alert( IHub<T> hub, T timeStep, IContagion contagion, double contagiousness ) {
		if( hub.isHealthy( contagion))
			return false;
		ContagionTrace trace = this.traces.get(hub);
		ContagionData<T> data = new ContagionData<T>( timeStep, contagion.getContagiousness()/2);
		if( trace == null ) {
			trace = new ContagionTrace();
			traces.put(hub, trace );
			trace.put( contagion, data );
		}else {
			data = trace.get(contagion);
			if( data == null ) {
				trace.put( contagion, data );
			}else {
				onUpdateSymbiot(timeStep, data, contagiousness);
			}
		}
		return true;
	}
*/
	
	private class HubTrace implements IHubTrace<T>{
		
		private Map<IContagion,ContagionData<T>> data;

		public HubTrace() {
			this.data = new HashMap<>();
		}
		
		@Override
		public Collection<IContagion> getContagions(){
			return this.data.keySet();
		}
		
		@Override
		public ContagionData<T> get( IContagion contagion ) {
			return this.data.get(contagion);
		}
		
		public void put( IContagion contagion, ContagionData<T> data ) {
			this.data.put(contagion, data);
		}

		@Override
		public void update( IContagion contagion, T current, IHubTrace<T> ct) {
			ContagionData<T> guest = ct.get(contagion);
			if( guest == null ) {
				guest = new ContagionData<T>( current );
				ct.put(contagion, guest);
			}
			ContagionData<T> source = this.data.get(contagion);
			double risk = 0;
			operator.setCurrent(current);
			operator.setContagion(contagion);
			T calc = current;
			if( source != null ) {
				risk = ( source.getRisk() + operator.getContagionRange( guest.getMoment() ).getValue() )/2;
				calc = onGetAverage(source.getMoment(), guest.getMoment());
			}else{
				risk = guest.getRisk()/2;
			}
			guest.setRisk(risk/100);
			guest.setTimeStep(calc);
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
