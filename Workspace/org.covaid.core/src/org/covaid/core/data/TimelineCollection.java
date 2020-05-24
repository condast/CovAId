package org.covaid.core.data;

import java.util.Map;
import java.util.TreeMap;

public class TimelineCollection<K,V extends Object> {
	
	private Map<String,TimelineData<K,V>> data;
	
	public TimelineCollection() {
		data = new TreeMap<>();
	}

	public void clear() {
		this.data.clear();
	}
	
	public boolean isEmpty() {
		return this.data.isEmpty();
	}
	
	public TimelineData<K,V> get( String key ) {
		return this.data.get(key);
	}
	
	public void put( String key, TimelineData<K,V> value ) {
		this.data.put(key, value);
	}

	public Map<String, TimelineData<K, V>> getData() {
		return data;
	}
	
	public Map<String, Map<K,V>> getTimelineData(){
		Map<String, Map<K,V>> results = new TreeMap<>();
		for( Map.Entry<String, TimelineData<K,V>> entry: data.entrySet()) {
			results.put(entry.getKey(), entry.getValue().getData());
		}
		return results;
	}
}
