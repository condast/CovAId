package org.covaid.core.model;

import java.util.Date;
import java.util.EventObject;

public class HistoryEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private Date date;
	private Point location;
	private Contagion contagion;

	public HistoryEvent( History source, Date date, Point location) {
		this( source, date, location, null );
	}
	
	public HistoryEvent( History source, Date date, Point location, Contagion contagion) {
		super(source);
		this.date = date;
		this.location = location;
		this.contagion = contagion;
	}

	public History getHistory( ) {
		return (History) getSource();
	}

	public Date getDate() {
		return date;
	}

	public Point getLocation() {
		return location;
	}

	public Contagion getContagion() {
		return contagion;
	}
}
