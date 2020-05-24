package org.covaid.core.hub.trace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.hub.IHub;

public abstract class AbstractTrace<T extends Object> implements ITrace<T>{

	private IHub<T> hub;
	
	private Map<IHub<T>, IHubTrace<T>> traces;

	public AbstractTrace() {
		traces = new HashMap<>();
	}

	public AbstractTrace( IHub<T> hub) {
		this();
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
	public Map<T, Double> getPrediction( IContagion<T> contagion, T range ){
		Map<T, Double> results = new TreeMap<>();
		Iterator<Map.Entry<IHub<T>, IHubTrace<T>>> iterator = this.traces.entrySet().iterator();
		while( iterator.hasNext() ) {
			Map.Entry<IHub<T>, IHubTrace<T>> entry = iterator.next();
			ContagionData<T> data = entry.getValue().get(contagion);
			Double check = results.get(data.getTimeStep());
			double risk = ( check==null)?0: check;
			if( data.getRisk() > risk)
				results.put(data.getTimeStep(), data.getRisk());
		}
		return results;
	}

	@Override
	public boolean update( IContagion<T> contagion, T timeStep, ITrace<T> guest ) {
		ILocation<T> location = hub.getLocation();
		if( !location.isInfected(contagion))
			return false;
		IHubTrace<T> ct = guest.getHubTrace(this.hub);
		if( ct == null )
			ct = guest.addHubTrace( this.hub );
		ct.update(contagion, timeStep, ct);
		return true;
	}
	
	protected abstract T onGetAverage( T first, T second );
	
	/*
	public boolean alert( IHub<T> hub, T timeStep, IContagion<T> contagion, double contagiousness ) {
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
		
		private Map<IContagion<T>,ContagionData<T>> data;

		public HubTrace() {
			this.data = new HashMap<>();
		}
		
		@Override
		public Collection<IContagion<T>> getContagions(){
			return this.data.keySet();
		}
		
		@Override
		public ContagionData<T> get( IContagion<T> contagion ) {
			return this.data.get(contagion);
		}
		
		public void put( IContagion<T> contagion, ContagionData<T> data ) {
			this.data.put(contagion, data);
		}

		@Override
		public void update( IContagion<T> contagion, T timeStep, IHubTrace<T> ct) {
			ContagionData<T> guest = ct.get(contagion);
			if( guest == null ) {
				guest = new ContagionData<T>( timeStep );
				ct.put(contagion, guest);
			}
			ContagionData<T> source = this.data.get(contagion);
			double risk = 0;
			T step = timeStep;
			if( source != null ) {
				risk = ( source.getRisk() + guest.getRisk() )/2;
				step = onGetAverage(source.getTimeStep(), guest.getTimeStep());
			}else{
				risk = guest.getRisk()/2;
			}
			guest.setRisk(risk);
			guest.setTimeStep(step);
		}
	}
}
