package org.covaid.core.hub;

import java.util.HashMap;
import java.util.Map;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;

public abstract class AbstractTrace<T extends Object> {

	private IHub<T> hub;
	
	private Map<IHub<T>, Map<IContagion<T>,ContagionData<T>>> traces;
	
	public AbstractTrace( IHub<T> hub) {
		this.hub = hub;
		traces = new HashMap<>();
	}
	
	protected abstract void onUpdateSymbiot( T timeStep, ContagionData<T> symbiot, double contagiousness );
	
	public boolean alert( IHub<T> hub, T timeStep, IContagion<T> contagion, double contagiousness ) {
		if( hub.isHealthy( contagion))
			return false;
		Map<IContagion<T>,ContagionData<T>> trace = this.traces.get(hub);
		ContagionData<T> symbiot = new ContagionData<T>( timeStep, contagion.getContagiousness()/2);
		if( trace == null ) {
			trace = new HashMap<>();
			traces.put(hub, trace );
			trace.put( contagion, symbiot );
		}else {
			symbiot = trace.get(contagion);
			if( symbiot == null ) {
				trace.put( contagion, symbiot );
			}else {
				onUpdateSymbiot(timeStep, symbiot, contagiousness);
			}
		}
		return true;
	}
}
