package org.covaid.core.model;

import org.covaid.core.data.StoredNode;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistory;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IMobile;
import org.covaid.core.def.IPoint;

public class Mobile<T extends Object> implements IMobile<T> {

		
	//An anonymous id used for communication with the server
	
	private String identifier;
	
	private double health, safety;
	private String email;//doctor email
	private transient StoredNode<T> node;
	private IPoint location;

	private IHistory<T> history;
	
	private T timestamp;
	
	public Mobile() {
		super();
	}

	/**
	 * The Safety is the extent in which the bubble should protect you, e.g. for vulnerable people
	 * The Risk is the amount of risk you are willing to take
	 * @param id
	 * @param safety (0-100)
	 * @param risk (0-100)
	 * @param location
	 */
	public Mobile( String identifier, double safety, double health, IPoint location, IHistory<T> history) {
		super();
		this.identifier = identifier;
		this.safety = safety;
		this.health = health;
		this.node = new StoredNode<T>(identifier );
		this.location = location;
		this.history = history;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public double getHealth() {
		return health;
	}

	@Override
	public void setHealth(double health) {
		this.health = health;
		if( this.health < this.safety )
			this.safety=  this.health;
	}

	@Override
	public double getSafety() {
		return safety;
	}

	@Override
	public void setSafety(double safety) {
		this.safety = safety;
	}

	@Override
	public IPoint getLocation() {
		return location;
	}

	@Override
	public void setLocation( IPoint location) {
		this.location = location;
	}

	@Override
	public void alert( T date, ILocation<T> location, IContagion<T> contagion ) {
		this.history.alert( date, location, contagion, 100);
	}

	@Override
	public IHistory<T> getHistory() {
		return this.history;
	}

	/**
	 * Returns true if the risk assessment shows that the owner is healthy
	 * @return
	 */
	@Override
	public boolean isHealthy() {
		return this.history.isEmpty();
	}

	public StoredNode<T> getNode() {
		return node;
	}
	
	@Override
	public T getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(T timestamp) {
		this.timestamp = timestamp;
	}
}
