package org.covaid.core.def;

import java.util.Date;
import java.util.EventObject;

public class HistoryEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private Date date;
	private IPoint location;
	private IContagion contagion;

	public HistoryEvent( IHistory source, Date date, IPoint location) {
		this( source, date, location, null );
	}
	
	public HistoryEvent( IHistory source, Date date, IPoint location, IContagion contagion) {
		super(source);
		this.date = date;
		this.location = location;
		this.contagion = contagion;
	}

	public IHistory getHistory( ) {
		return (IHistory) getSource();
	}

	public Date getDate() {
		return date;
	}

	public IPoint getLocation() {
		return location;
	}

	public IContagion getContagion() {
		return contagion;
	}
}
