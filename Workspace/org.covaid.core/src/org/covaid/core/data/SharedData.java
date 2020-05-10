package org.covaid.core.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.condast.commons.data.latlng.LatLng;
import org.condast.commons.strings.StringStyler;
import org.covaid.core.def.IContagion;

public class SharedData<T extends Object>{

	public enum Attributes{
		IDENTIFIER,
		DISTANCE,
		CONTAGION,
		LATITUDE,
		LONGITUDE;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}	
	}
	
	private String identifier;
	private Collection<IContagion<T>> contagion;
	private float distance;
	
	private LatLng latlng;

	public SharedData(String identifier, IContagion<T> disease, float distance, LatLng latlng) {
		this( identifier, new ArrayList<IContagion<T>>(), distance, latlng );
		this.contagion.add(disease);
	}

	public SharedData(String identifier, Collection<IContagion<T>> contagion, float distance, LatLng latlng) {
		super();
		this.identifier = identifier;
		this.contagion = contagion;
		this.distance = distance;
		this.latlng = latlng;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	@SuppressWarnings("unchecked")
	public IContagion<T>[] getContagion() {
		return contagion.toArray( new IContagion[ this.contagion.size()] );
	}

	
	public float getDistance() {
		return distance;
	}

	public LatLng getLatlng() {
		return latlng;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if( super.equals(obj))
			return true;
		if(!( obj instanceof SharedData))
			return false;
		SharedData<?> data = (SharedData<?>) obj;
		return data.getIdentifier().equals(identifier);
	}
	
	public static Map<IContagion<Date>, Date> toTimeStamped( SharedData<Date> data ){
		Map<IContagion<Date>, Date> results = new HashMap<>();
		for( IContagion<Date> cd: data.contagion )
			results.put(cd, Calendar.getInstance().getTime());
		return results;
	}
}
