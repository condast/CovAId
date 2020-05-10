package org.covaid.core.data;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.condast.commons.strings.StringUtils;

public class StoredNode<T extends Object> {

	private StoredData<T> root;
	
	private Map<StoredData<T>, Float> connections;
	
	private Date timestamp;

	public StoredNode() {
		super();
		connections = new HashMap<>();
		this.timestamp = Calendar.getInstance().getTime();
	}

	public StoredNode(StoredData<T> root) {
		this();
		this.root = root;
	}

	public StoredNode(StoredData<T> root, Map<StoredData<T>, Float> data) {
		this();
		this.root = root;
		this.connections = data;
	}

	public StoredNode( String identifier) {
		this( new StoredData<T>( identifier ));
	}

	public StoredData<T> getRoot() {
		return root;
	}
	
	public void addChild( StoredData<T> data, float distance ) {
		this.connections.put(data, distance);
	}

	public float getDistance( StoredData<T> data ) {
		return this.connections.get(data);
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public StoredData<T> get( String identifier ) {
		if( StringUtils.isEmpty(identifier))
			return null;
		for( StoredData<T> data: this.connections.keySet()) {
			if( data.getIdentifier().equals(identifier))
				return data;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public StoredData<T>[] getConnections() {
		return this.connections.values().toArray( new StoredData[ this.connections.size()]);
	}
}
