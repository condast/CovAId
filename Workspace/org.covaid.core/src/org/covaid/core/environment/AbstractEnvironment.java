package org.covaid.core.environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.covaid.core.def.EnvironmentEvent;
import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IEnvironmentListener;

public abstract class AbstractEnvironment<T extends Object> implements IEnvironment<T> {

	protected static final double LONGITUDE = 4.00f;
	protected static final double LATITUDE  = 52.000f;
	protected static final int DEFAULT_LENGTH  = 1000; //1 km
	protected static final int DEFAULT_WIDTH  = 1000; //1 km
	
	private int population;
	private int days;
	
	private int activity;//the amount of activities in a day
	private int index;
	private boolean started;
	private boolean pause;
	
	private String name;
	
	private Collection<IDomain<T>> domains;

	private Collection<IEnvironmentListener<T>> listeners;

	private Timer timer;

	private IEnvironment<T> env;

	private TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if(!started || pause )
				return;
			try {
				T step = getTimeStep( days );
				for( IDomain<T> domain: domains ) {
					domain.movePerson( step );
				}		
				notifyListeners( new EnvironmentEvent<T>(env, Events.ACTIVITY, days));
				index++;
				index = ( activity > 1 )?index %= activity: 0;
					
				if( index != 0 )
					return;

				days++;
				for( IDomain<T> domain: domains ) {
					domain.update(step );
				}
				notifyListeners( new EnvironmentEvent<T>(env, Events.NEW_DAY, days));
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
	};
	
	protected AbstractEnvironment( String name ) {
		this( name, DEFAULT_LENGTH, DEFAULT_WIDTH, DEFAULT_POPULATION, DEFAULT_SPEED );
	}
	
	protected AbstractEnvironment( String name, int length, int width, int population ) {
		this( name, length, width, population, DEFAULT_SPEED);
	}

	protected AbstractEnvironment( String name, int length, int width, int population, int speed ) {
		this.env = this;
		this.name = name;
		this.started = false;
		this.domains = new ArrayList<>();
		timer = new Timer();
	    timer.scheduleAtFixedRate(timerTask, 0, speed);
		this.population = population;
		this.listeners = new ArrayList<>();
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void init( int population ) {
		this.clear();
		this.days = 0;
		this.pause = false;
		this.population = population;
		for( IDomain<T> domain: domains ) {
			domain.init( this.population);
		}
	}

	@Override
	public void addListener( IEnvironmentListener<T> listener ) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener( IEnvironmentListener<T> listener ) {
		this.listeners.remove(listener);
	}

	void notifyListeners( EnvironmentEvent<T> event ) {
		for( IEnvironmentListener<T> listener: this.listeners )
			listener.notifyChanged(event);
	}

	@Override
	public void addDomain( IDomain<T> domain ) {
		this.domains.add(domain);
		domain.setEnvironment(this);
	}

	@Override
	public void removeDomain( IDomain<T> domain ) {
		domain.setEnvironment(null);
		this.domains.remove(domain);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IDomain<T>[] getDomains() {
		return domains.toArray( new IDomain[ this.domains.size() ]);
	}

	protected int getActivity() {
		return activity;
	}

	protected void setActivity(int activity) {
		this.activity = activity;
	}

	@Override
	public int getPopulation() {
		return population;
	}

	@Override
	public int getDays() {
		return days;
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
	public boolean pause() {
		if( !started)
			return false;
		pause = !pause;
		return pause;
		
	}

	@Override
	public void clear() {
		for( IDomain<T> domain: this.domains)
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