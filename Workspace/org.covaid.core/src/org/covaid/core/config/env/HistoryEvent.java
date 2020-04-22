package org.covaid.core.config.env;

import java.util.Date;
import java.util.EventObject;

public class HistoryEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private Date date;
	private Location location;
	
	public HistoryEvent( History source, Date date, Location location) {
		super(source);
		this.date = date;
		this.location = location;
	}

	public History getHistory( ) {
		return (History) getSource();
	}

	public Date getDate() {
		return date;
	}

	public Location getLocation() {
		return location;
	}
}
