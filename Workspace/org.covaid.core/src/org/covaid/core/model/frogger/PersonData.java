package org.covaid.core.model.frogger;

import org.covaid.core.def.IPerson;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Mobile;
import org.covaid.core.model.Point;
import org.covaid.core.def.IPerson.States;

public class PersonData {

	private States state;
	private Contagion monitor;
	
	private Point location;
	
	private Mobile mobile;

	public PersonData( IPerson<Integer> person ) {
		this.state = person.getState();
		this.monitor = (Contagion) person.getMonitor();
		this.location = (Point) person.getLocation();
	}

	public States getState() {
		return state;
	}

	public Contagion getMonitor() {
		return monitor;
	}

	public Point getLocation() {
		return location;
	}

	public Mobile<Integer> getMobile() {
		return mobile;
	}
}
