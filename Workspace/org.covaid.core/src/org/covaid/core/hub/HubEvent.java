package org.covaid.core.hub;

import java.util.EventObject;

import org.covaid.core.def.IContagion;
import org.covaid.core.hub.trace.ITrace;

public class HubEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;

	private IContagion<T> contagion;
	private ITrace<T> trace;
	private T step;
	
	public HubEvent( IHub<T> source, ITrace<T> trace, T step, IContagion<T> contagion) {
		super(source);
		this.trace = trace;
		this.step = step;
		this.contagion = contagion;
	}

	@SuppressWarnings("unchecked")
	public IHub<T> getHub( ) {
		return (IHub<T>) getSource();
	}

	public IContagion<T> getContagion() {
		return contagion;
	}

	public ITrace<T> getTrace() {
		return trace;
	}

	public T getStep() {
		return step;
	}
}