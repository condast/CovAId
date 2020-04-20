package org.covaid.rest.core;

import java.util.Collection;
import java.util.TreeSet;

public class Dispatcher {

	private static Dispatcher dispatcher = new Dispatcher();
	
	private Collection<Long> users;

	private Dispatcher() {
		users = new TreeSet<>();
	}

	public static Dispatcher getInstance() {
		return dispatcher;
	}

	public boolean isregistered( long userId ) {
		return this.users.contains(userId );
	}

	public void start(long userId, int i) {
		this.users.add(userId);
	}

	
}
