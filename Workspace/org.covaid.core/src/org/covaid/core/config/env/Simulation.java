package org.covaid.core.config.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;

public class Simulation {

	public static final int MILLION = 1000000;

	public static final int DEFAULT_ACTIVITY = 3600*24;//minutes
	public static final int DEFAULT_RADIUS = 10;//metres, the maximum movement that a person can make during one step in activity
	public static final int DEFAULT_SPEED = 1500;//metres, the maximum movement that a person can make during one step in activity

	private int length, width;//metres
	private int radius;
	private Contagion contagion;
	private int days;
	
	private int activity;//the amount of activities in a day
	private int index;
	private boolean started;
	private boolean pause;
	
	private Collection<Person> persons;
	private Map<Point, Hub> hubs;

	private TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if(!started || pause )
				return;
			try {
				for( Person person: persons ) {
					movePerson(person);
					notifyListeners( new SimulationEvent( simulation, person ));
				}
				index++;
				index %= activity;
				if( index == 0 ) {
					days++;
					for( Hub hub: hubs.values())
						hub.updateHub(days);
				}
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
	};
	private Timer timer;

	private Collection<ISimulationListener> listeners;
	
	public Simulation( ) {
		this( DEFAULT_SPEED);
	}
	
	private Simulation simulation;
	
	public Simulation( int speed ) {
		this.simulation = this;
		this.started = false;
		persons = new TreeSet<>();
		hubs = new TreeMap<>();
		this.listeners = new ArrayList<>();
		timer = new Timer();
	    timer.scheduleAtFixedRate(timerTask, 0, speed);
	}

	public void init( Contagion contagion, int length, int width, int population ) {
		init( contagion, length, width, DEFAULT_RADIUS, DEFAULT_ACTIVITY, population );
	}
	
	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	public void init( Contagion contagion, int length, int width, int radius, int activity, int population ) {
		this.clear();
		this.days = 0;
		this.length = length;
		this.width = width;
		this.radius = radius;
		this.contagion = contagion;
		this.activity = activity;
		this.pause = false;

		int total = length * width * population;
		while( persons.size() < total ) {
			Person person = createPerson();
			persons.add(person);
			Location location = new Location( person.getLocation());
			Hub hub = this.hubs.get(location);
			if( hub == null ) {
				hub = new Hub( person );
				this.hubs.put( location, hub );
			}else {
				hub.addPerson(person);
			}
		}
	}

	public void addListener( ISimulationListener listener ) {
		this.listeners.add(listener);
	}

	public void removeListener( ISimulationListener listener ) {
		this.listeners.remove(listener);
	}

	protected void notifyListeners( SimulationEvent event ) {
		for( ISimulationListener listener: this.listeners )
			listener.notifyPersonChanged(event);
	}

	public int getLength() {
		return length;
	}

	public int getWidth() {
		return width;
	}

	public int getDays() {
		return days;
	}
	
	public void start() {
		this.index = 0;
		this.started = true;
 	}
	
	public void stop() {
		this.started = false;;
	}
	
	public void clear() {
		this.persons.clear();
		this.hubs.clear();
	}

	public void dispose() {
		stop();
		this.clear();
		timer.cancel();
		timer.purge();
		timer = null;
	}

	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	protected Person createPerson() {
		Person person = null;
		do {
			int x = (int)( width * Math.random());
			int y = (int)( length * Math.random());
			person = new Person( x, y, contagion );
		}
		while(!persons.contains(person));
		return person;
	}
	
	/**
	 * Move a person
	 * @param population
	 */
	protected void movePerson( Person person) {
		persons.remove(person);
		do {
			int x = person.getLocation().getXpos() + (int)( radius * (Math.random() - 0.5f));
			int y = person.getLocation().getYpos() + (int)( radius * (Math.random() - 0.5f));
			person.setPosition(x, y);
		}
		while(!persons.contains(person));
		persons.add(person);
		Location location = new Location( person.getLocation());
		Hub hub = this.hubs.get(location);
		if( hub == null ) {
			hub = new Hub( person );
			this.hubs.put( location, hub );
		}else {
			hub.addPerson(person);
		}
	}
}