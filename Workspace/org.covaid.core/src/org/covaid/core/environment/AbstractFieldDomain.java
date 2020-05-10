package org.covaid.core.environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.condast.commons.data.plane.Field;
import org.condast.commons.data.plane.IField;
import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IDomainListener;
import org.covaid.core.def.IFieldEnvironment;
import org.covaid.core.def.IHub;
import org.covaid.core.def.IPerson;
import org.covaid.core.environment.field.IFieldDomain;
import org.covaid.core.model.date.DatePerson;

public abstract class AbstractFieldDomain implements IFieldDomain{

	public static final int DEFAULT_RADIUS = 10;//metres, the maximum movement that a person can make during one step in activity

	private String name;
	private int population;
	private Collection<IPerson> persons;
	private Map<String, IHub> hubs;
	//private int index;
	private IField field;
	private IFieldEnvironment environment;
	
	private Collection<IDomainListener> listeners;

	public AbstractFieldDomain( String name ) {
		super();
		this.name = name;
		this.population = 1;
		persons = new ConcurrentSkipListSet<>();
		hubs = new TreeMap<>();
		this.listeners = new ArrayList<>();
	}
	
	public void init( int population ) {
		this.population = population;
		while( persons.size() < population ) {
			DatePerson person = createPerson();
			onCreatePerson(this, person);
			persons.add(person);
		}
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<IPerson> getPersons() {
		return persons;
	}

	public int getPopulation() {
		return population;
	}

	@Override
	public IEnvironment<Date> getEnvironment() {
		return environment;
	}
	
	public void setEnvironment(IEnvironment<Date> environment) {
		this.environment = (IFieldEnvironment) environment;
		this.population = 1;
		this.field = new Field( this.environment.getField().toFieldData(1));
	}

	public IField getField() {
		return field;
	}

	public void setField(IField field) {
		this.field = field;
	}

	@Override
	public Map<String, IHub> getHubs() {
		return hubs;
	}

	@Override
	public void addListener( IDomainListener listener ) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener( IDomainListener listener ) {
		this.listeners.remove(listener);
	}

	void notifyListeners( DomainEvent event ) {
		for( IDomainListener listener: this.listeners )
			listener.notifyPersonChanged(event);
	}

	protected abstract void onCreatePerson( IFieldDomain domain, IPerson person );

	protected abstract void onMovePerson( IFieldDomain domain, Date date, IPerson person );
	

	public void movePerson( Date date ) {
		Collection<IPerson> temp = new ArrayList<IPerson>(persons);
		for( IPerson person: temp ) {
			onMovePerson( this, date, person);
			notifyListeners( new DomainEvent( this, DomainEvents.UPDATE_PERSON ));
		}
		notifyListeners( new DomainEvent( this, DomainEvents.PERSONS_MOVED ));
	}
	public void updatePerson() {
		while( persons.size() < population ) {
			DatePerson person = createPerson();
			onCreatePerson( this, person);
			persons.add(person);
		}

	}
	
	public void update( Date date ) {
		for( IHub hub: hubs.values())
			hub.update(date);
		Collection<IPerson> temp = new ArrayList<IPerson>(persons);
		for( IPerson person: temp ) {
			person.updatePerson(date);
		}
		notifyListeners( new DomainEvent( this, DomainEvents.UPDATE ));
	}
	
	@Override
	public void clear() {
		this.persons.clear();
		this.hubs.clear();
	}

	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	protected DatePerson createPerson() {
		DatePerson person = null;
		do {
			int x = (int)( field.getWidth() * Math.random());
			int y = (int)( field.getLength() * Math.random());
			double risk = 100*Math.random();
			double safety = 100*Math.random();
			String identifier = "id("+ x + "," + y + "):{" + risk + ", " + safety + "}";  
			person = new DatePerson(identifier, x, y, safety, risk );
		}
		while(persons.contains(person));
		return person;
	}
}
