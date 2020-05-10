package org.covaid.core.def;

import java.util.EventObject;

public class HistoryEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;

	private T date;
	private IPoint location;
	private IContagion<T> contagion;

	public HistoryEvent( IHistory<T> source, T date, IPoint location) {
		this( source, date, location, null );
	}
	
	public HistoryEvent( IHistory<T> source, T date, IPoint location, IContagion<T> contagion) {
		super(source);
		this.date = date;
		this.location = location;
		this.contagion = contagion;
	}

	@SuppressWarnings("unchecked")
	public IHistory<T> getHistory( ) {
		return (IHistory<T>) getSource();
	}

	public T getStep() {
		return date;
	}

	public IPoint getLocation() {
		return location;
	}

	public IContagion<T> getContagion() {
		return contagion;
	}
}
