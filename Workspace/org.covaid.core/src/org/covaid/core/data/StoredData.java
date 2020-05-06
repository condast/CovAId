package org.covaid.core.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.condast.commons.strings.StringStyler;
import org.covaid.core.def.IContagion;

public class StoredData {

	public enum Attributes{
		ID,
		IDENTIFIER,
		CONTAGION,
		TIMESTAMP;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}	
	}
	
	private String id;
	private String identifier;
	private Map<IContagion, Date> contagiousness;
	
	public StoredData(String identifier, Map<IContagion, Date> contagiousness ) {
		super();
		this.identifier = identifier;
		this.contagiousness = contagiousness;
	}
	
	public StoredData(String identifier ) {
		this( identifier, new HashMap<IContagion, Date>());
	}

	public StoredData(SharedData shared ) {
		this( shared.getIdentifier(), SharedData.toTimeStamped( shared ));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public IContagion getContagiousness( String identifier ) {
		Iterator<Map.Entry<IContagion, Date>> iterator = contagiousness.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<IContagion, Date> entry = iterator.next();
			if( entry.getKey().getIdentifier().equals(identifier))
				return entry.getKey();
		}
		return null;
	}

	public Map<IContagion, Date> getContagiousness() {
		return contagiousness;
	}

	public void addContagion( IContagion data, Date timestamp ) {
		if( !this.contagiousness.containsKey(data))
			this.contagiousness.put(data, timestamp);
		else
			this.contagiousness.replace(data, timestamp);
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if( super.equals(obj))
			return true;
		if(!( obj instanceof StoredData))
			return false;
		StoredData data = (StoredData) obj;
		if( data.getId().equals(getId()))
			return true;
		return data.getIdentifier().equals(identifier);
	}
	
	
}
