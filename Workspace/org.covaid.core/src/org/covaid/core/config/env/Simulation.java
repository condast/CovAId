package org.covaid.core.config.env;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;

public class Simulation {

	public static final int MILLION = 1000000;

	public static final int DEFAULT_ACTIVITY = 60*24;//minutes
	public static final int DEFAULT_RADIUS = 10;//metres, the maximum movement that a person can make during one step in activity
	public static final int DEFAULT_SPEED = 20;//metres, the maximum movement that a person can make during one step in activity

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
				Collection<Person> temp = new ArrayList<Person>(persons);
				for( Person person: temp ) {
					movePerson(person, getDate());
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
	
	private Date start;
	
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
	    this.length = 100;
	    this.width = 100;
	    this.contagion = Contagion.SupportedContagion.COVID_19.getContagion();
	}

	public void init( int population ) {
		init( DEFAULT_RADIUS, DEFAULT_ACTIVITY, population );
	}
	
	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	public void init( int radius, int activity, int population ) {
		this.clear();
		this.days = 0;
		this.radius = radius;
		this.activity = activity;
		this.pause = false;

		while( persons.size() < population ) {
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

	public String getDayString( boolean trunc ) {
		StringBuilder builder = new StringBuilder();
		builder.append( days );
		if(!trunc) {
			builder.append(": ");
			double step = (double)index/activity;
			builder.append(String.format("%.2f", step));
		}
		return builder.toString();
	}

	public Contagion getContagion() {
		return contagion;
	}

	public void setContagion(Contagion contagion) {
		this.contagion = contagion;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	
	public void zoomIn() {
		this.length = length/2;
		this.width = width/2;
	}

	public void zoomOut() {
		this.length = 2*length;
		this.width = 2*width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public int getDays() {
		return days;
	}
	
	/**
	 * Get the simulated date
	 * @return
	 */
	public Date getDate() {
		Calendar calendar = Calendar.getInstance();
		long time = start.getTime() + days + (long)( 24*3600*1000*index/activity);
		calendar.setTimeInMillis( time);
		return calendar.getTime();
	}
	
	public void start() {
		this.index = 0;
		this.started = true;
		this.start = Calendar.getInstance().getTime();
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
			double risk = 100*Math.random();
			double safety = 100*Math.random();
			person = new Person( x, y, safety, risk, this.contagion );
		}
		while(persons.contains(person));
		return person;
	}
	
	/**
	 * Move a person
	 * @param population
	 */
	protected void movePerson( Person person, Date date) {
		persons.remove(person);
		int x = person.getLocation().getXpos();
		int y = person.getLocation().getYpos();
		do {
			x = person.getLocation().getXpos() + (int)( radius * (Math.random() - 0.5f));
			y = person.getLocation().getYpos() + (int)( radius * (Math.random() - 0.5f));
			person.setPosition(x, y);
		}
		while(persons.contains(person));
		persons.add(person);
		person.updatePerson(date, x, y);
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