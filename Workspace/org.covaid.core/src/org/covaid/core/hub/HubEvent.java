package org.covaid.core.hub;

import java.util.EventObject;

public class HubEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;

	private T step;
	
	public HubEvent( IHub<T> source, T step) {
		super(source);
		this.step = step;
	}

	@SuppressWarnings("unchecked")
	public IHub<T> getHistory( ) {
		return (IHub<T>) getSource();
	}

	public T getStep() {
		return step;
	}
}