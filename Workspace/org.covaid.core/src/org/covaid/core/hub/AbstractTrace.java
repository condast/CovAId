package org.covaid.core.hub;

import java.util.HashMap;
import java.util.Map;

import org.covaid.core.def.IContagion;

public abstract class AbstractTrace<T extends Object> {

	private IHub<T> hub;
	
	private Map<IHub<T>, Map<IContagion<T>,Symbiot<T>>> traces;
	
	public AbstractTrace( IHub<T> hub) {
		this.hub = hub;
		traces = new HashMap<>();
	}
	
	protected abstract void onUpdateSymbiot( T timeStep, Symbiot<T> symbiot, double contagiousness );
	
	public boolean alert( IHub<T> hub, T timeStep, IContagion<T> contagion, double contagiousness ) {
		if( hub.isHealthy( contagion))
			return false;
		Map<IContagion<T>,Symbiot<T>> trace = this.traces.get(hub);
		Symbiot<T> symbiot = new Symbiot<T>( timeStep, contagion.getContagiousness()/2);
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

	protected static class Symbiot<T extends Object>{
		private T timeStep;
		private double contagiousness;
		
		public Symbiot( T timeStep, double contagiousness) {
			super();
			this.timeStep = timeStep;
			this.contagiousness = contagiousness;
		}

		public T getTimeStep() {
			return timeStep;
		}

		public void setTimeStep(T timeStep) {
			this.timeStep = timeStep;
		}

		public double getContagiousness() {
			return contagiousness;
		}

		public void setContagiousness(double contagiousness) {
			this.contagiousness = contagiousness;
		}
	}
}
