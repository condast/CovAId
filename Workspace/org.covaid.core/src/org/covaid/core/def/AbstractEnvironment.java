package org.covaid.core.def;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.condast.commons.data.latlng.LatLng;
import org.condast.commons.data.plane.Field;
import org.condast.commons.data.plane.IField;
import org.covaid.core.model.Hub;
import org.covaid.core.model.Person;
import org.covaid.core.model.EnvironmentEvent;

public abstract class AbstractEnvironment implements IEnvironment {

	protected static final double LONGITUDE = 4.00f;
	protected static final double LATITUDE  = 52.000f;
	protected static final int DEFAULT_LENGTH  = 1000; //1 km
	protected static final int DEFAULT_WIDTH  = 1000; //1 km
	private IField field;//metres
	private int population;
	private String contagion;
	private int days;
	
	private int activity;//the amount of activities in a day
	private int index;
	private boolean started;
	private boolean pause;
	
	private Collection<Person> persons;
	private Map<String, Hub> hubs;

	protected abstract void onCreatePerson( int index, IPerson person );

	protected abstract void onMovePerson( Date date, Person person );
	
	private TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if(!started || pause )
				return;
			try {
				Collection<Person> temp = new ArrayList<Person>(persons);
				Date date = getDate();
				for( Person person: temp ) {
					onMovePerson( date, person);
					notifyListeners( new EnvironmentEvent( environment, person, days ));
				}
				index++;
				index %= activity;
				
				notifyListeners( new EnvironmentEvent( environment, Events.ACTIVITY, days ));
				if( index != 0 )
					return;

				days++;
				for( IHub hub: hubs.values())
					hub.updateHub(date);
				for( IPerson person: temp ) {
					person.updatePerson(date);
				}
				notifyListeners( new EnvironmentEvent( environment, days ));

			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
	};
	
	private Timer timer;

	private Collection<IEnvironmentListener> listeners;

	private Date start;
	
	private String name;

	private IEnvironment environment;

	public AbstractEnvironment( String name ) {
		this( name, DEFAULT_LENGTH, DEFAULT_WIDTH, DEFAULT_POPULATION );
	}
	
	public AbstractEnvironment( String name, int length, int width, int population ) {
		this( new LatLng( name, LATITUDE, LONGITUDE), length, width, population, DEFAULT_SPEED);
	}

	public AbstractEnvironment( LatLng location, int length, int width, int population, int speed ) {
		this.environment = this;
		this.name = location.getId();
		this.started = false;
		persons = new ConcurrentSkipListSet<>();
		hubs = new TreeMap<>();
		this.listeners = new ArrayList<>();
		timer = new Timer();
	    timer.scheduleAtFixedRate(timerTask, 0, speed);
		field = new Field( location, length, width);
		this.population = population;
	    this.contagion = IContagion.SupportedContagion.COVID_19.name();
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void init( int population ) {
		init( DEFAULT_ACTIVITY, population );
	}
	
	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	@Override
	public void init( int activity, int population ) {
		this.clear();
		this.start = Calendar.getInstance().getTime();
		this.days = 0;
		this.activity = activity;
		this.pause = false;
		this.population = population;
		int index = 0;
		while( persons.size() < population ) {
			Person person = createPerson();
			onCreatePerson(index++, person);
			persons.add(person);
		}
	}

	@Override
	public void addListener( IEnvironmentListener listener ) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener( IEnvironmentListener listener ) {
		this.listeners.remove(listener);
	}

	protected void notifyListeners( EnvironmentEvent event ) {
		for( IEnvironmentListener listener: this.listeners )
			listener.notifyPersonChanged(event);
	}

	@Override
	public Collection<Person> getPersons() {
		return persons;
	}

	protected Map<String, Hub> getHubs() {
		return hubs;
	}

	@Override
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

	@Override
	public String getContagion() {
		return contagion;
	}

	@Override
	public void setContagion(String contagion) {
		this.contagion = contagion;
	}

	@Override
	public int getPopulation() {
		return population;
	}

	@Override
	public IField getField() {
		return field;
	}

	@Override
	public void setField( IField field) {
		this.field = field;
	}

	@Override
	public void zoomIn() {
		int length = (int) (field.getLength()/2);
		int width = (int) (field.getWidth()/2);
		field = new Field( field.getCoordinates(), length, width );
	}

	@Override
	public void zoomOut() {
		int length = (int) (field.getLength()*2);
		int width = (int) (field.getWidth()*2);
		field = new Field( field.getCoordinates(), length, width );
	}

	@Override
	public int getDays() {
		return days;
	}
	
	/**
	 * Get the simulated date
	 * @return
	 */
	@Override
	public Date getDate() {
		Calendar calendar = Calendar.getInstance();
		long time = start.getTime() + days + (long)( 24*3600*1000*index/activity);
		calendar.setTimeInMillis( time);
		return calendar.getTime();
	}
	
	@Override
	public void start() {
		this.index = 0;
		this.started = true;
 	}
	
	@Override
	public void stop() {
		this.started = false;;
	}
	
	@Override
	public void clear() {
		this.persons.clear();
		this.hubs.clear();
	}

	@Override
	public void dispose() {
		stop();
		this.clear();
		if( timer == null )
			return;
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
			int x = (int)( field.getWidth() * Math.random());
			int y = (int)( field.getLength() * Math.random());
			double risk = 100*Math.random();
			double safety = 100*Math.random();
			String identifier = "id("+ x + "," + y + "):{" + risk + ", " + safety + "}";  
			person = new Person(identifier, x, y, safety, risk );
		}
		while(persons.contains(person));
		return person;
	}
}