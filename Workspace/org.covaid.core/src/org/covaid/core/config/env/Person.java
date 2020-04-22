package org.covaid.core.config.env;

import java.util.Calendar;
import java.util.Date;

public class Person implements Comparable<Person>{

	private Point location;
	
	private History history;
	
	public Person(){
		this.history = new History();
	}

	public Person( int xpos, int ypos) {
		this();
		location = new Location(xpos, ypos);
	}

	public Person( int xpos, int ypos, Contagion contagion) {
		this( xpos, ypos );
		Location loc = new Location( location );
		loc.addContagion(contagion);
		this.putHistory( Calendar.getInstance().getTime(), loc);
	}

	public void setPosition(int xpos, int ypos) {
		location.setPosition(xpos, ypos);
	}

	public Point getLocation() {
		return location;
	}

	public void putHistory( Date date, Location location ) {
		this.history.put( date, location);
	}

	public History getHistory() {
		return this.history;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if( super.equals(obj))
			return true;
		if(!( obj instanceof Person))
			return false;
		Person test = (Person) obj;
		return location.equals(test.getLocation());
	}

	@Override
	public int compareTo(Person o) {
		return this.location.compareTo(o.getLocation());
	}
}