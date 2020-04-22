package org.covaid.core.config.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

public class Hub extends Point {

	private Collection<Person> persons;
	
	private IHistoryListener listener = (e)->{
		updateHub(e);
	};
	
	public Hub(int xpos, int ypos) {
		super(xpos, ypos);
		this.persons = new TreeSet<>();
	}

	public Hub(String identifier, int xpos, int ypos) {
		super(identifier, xpos, ypos);
		this.persons = new TreeSet<>();
	}

	public Hub( Person person ) {
		this( person.getLocation().getIdentifier(), person.getLocation().getXpos(), person.getLocation().getYpos());
	}
	
	public boolean addPerson( Person person ) {
		if( person.getLocation().compareTo(this) != 0 )
			return false;
		person.getHistory().addListener(listener);
		return this.persons.add(person);
	}

	public boolean removePerson( Person person ) {
		person.getHistory().removeListener(listener);		
		return this.persons.remove(person);
	}
	
	public void updateHub( HistoryEvent event ) {
		Collection<Person> temp = new ArrayList<Person>( this.persons );
		for( Person person: temp ) {
			person.getHistory().update(event.getDate(), event.getLocation());
		}
	}
	
	public void updateHub( long days ) {
		Collection<Person> temp = new ArrayList<Person>( this.persons );
		for( Person person: temp ) {
			if(!person.getHistory().isContagious(days))
				this.persons.remove(person);
		}
	}
}
