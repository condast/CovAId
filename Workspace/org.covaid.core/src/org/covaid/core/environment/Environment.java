package org.covaid.core.environment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.condast.commons.data.latlng.LatLng;
import org.condast.commons.data.plane.Field;
import org.condast.commons.data.plane.IField;
import org.covaid.core.def.EnvironmentEvent;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IEnvironmentListener;
import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IHub;
import org.covaid.core.def.IPerson;

public class Environment implements IEnvironment {

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
	
	private Collection<AbstractDomain> domains;

	private Collection<IEnvironmentListener> listeners;

	private Timer timer;

	private Date start;
	
	private IEnvironment env;

	private TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if(!started || pause )
				return;
			try {
				Date date = getDate();
				for( AbstractDomain domain: domains ) {
					domain.movePerson( date );
				}		
				notifyListeners( new EnvironmentEvent(env, Events.ACTIVITY, days));
				index++;
				index %= activity;
					
				if( index != 0 )
					return;

				days++;
				for( AbstractDomain domain: domains ) {
					domain.update(date );
				}
				notifyListeners( new EnvironmentEvent(env, Events.NEW_DAY, days));
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
	};
	
	
	public Environment() {
		this( new LatLng( LATITUDE, LONGITUDE), DEFAULT_LENGTH, DEFAULT_WIDTH, DEFAULT_POPULATION, DEFAULT_SPEED );
	}
	
	public Environment( String name, int length, int width, int population ) {
		this( new LatLng( name, LATITUDE, LONGITUDE), length, width, population, DEFAULT_SPEED);
	}

	public Environment( LatLng location, int length, int width, int population, int speed ) {
		this.env = this;
		this.started = false;
		this.domains = new ArrayList<>();
		timer = new Timer();
	    timer.scheduleAtFixedRate(timerTask, 0, speed);
		field = new Field( location, length, width);
		this.population = population;
	    this.contagion = IContagion.SupportedContagion.COVID_19.name();
		this.listeners = new ArrayList<>();
	}

	@Override
	public String getName() {
		return this.field.getName();
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
		for( AbstractDomain domain: domains ) {
			domain.init( this.population);
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

	void notifyListeners( EnvironmentEvent event ) {
		for( IEnvironmentListener listener: this.listeners )
			listener.notifyChanged(event);
	}

	@Override
	public void addDomain( AbstractDomain domain ) {
		this.domains.add(domain);
		domain.setEnvironment(this);
	}

	@Override
	public void removeDomain( AbstractDomain domain ) {
		domain.setEnvironment(null);
		this.domains.remove(domain);
	}

	@Override
	public AbstractDomain[] getDomains() {
		return domains.toArray( new AbstractDomain[ this.domains.size() ]);
	}

	@Override
	public int getPopulation() {
		return population;
	}

	public Collection<IPerson> getPersons( AbstractDomain domain ) {
		return domain.getPersons();
	}

	protected Map<String, IHub> getHubs( AbstractDomain domain ) {
		return domain.getHubs();
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
		for( AbstractDomain domain: this.domains)
			domain.clear();
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
}