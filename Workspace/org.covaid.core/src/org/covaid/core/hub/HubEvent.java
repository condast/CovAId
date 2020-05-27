package org.covaid.core.hub;

import java.util.EventObject;

import org.covaid.core.def.IContagion;
import org.covaid.core.hub.trace.ITrace;

public class HubEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;

	private IContagion contagion;
	private ITrace<T> trace;
	private T current;
	
	public HubEvent( IHub<T> source, ITrace<T> trace, T current, IContagion contagion) {
		super(source);
		this.trace = trace;
		this.current = current;
		this.contagion = contagion;
	}

	@SuppressWarnings("unchecked")
	public IHub<T> getHub( ) {
		return (IHub<T>) getSource();
	}

	public IContagion getContagion() {
		return contagion;
	}

	public ITrace<T> getTrace() {
		return trace;
	}

	public T getCurrent() {
		return current;
	}
}