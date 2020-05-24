package org.covaid.core.data;

import java.util.Map;
import java.util.TreeMap;

public class TimelineData<K,V extends Object> {

	private String identifier;
	
	private Map<K,V> data;
	
	public TimelineData( String identifier ) {
		this.identifier = identifier;
		data = new TreeMap<>();
	}

	public String getIdentifier() {
		return identifier;
	}

	public V get( K key ) {
		return this.data.get(key);
	}
	
	public void put( K key, V value ) {
		this.data.put(key, value);
	}

	public Map<K, V> getData() {
		return data;
	}
	
	public int size() {
		return this.data.size();
	}

	@Override
	public String toString() {
		return this.data.toString();
	}
	
	
}
