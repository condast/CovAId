package org.covaid.core.data;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.condast.commons.strings.StringUtils;

public class StoredNode {

	private StoredData root;
	
	private Map<StoredData, Float> connections;
	
	private Date timestamp;

	public StoredNode() {
		super();
		connections = new HashMap<>();
		this.timestamp = Calendar.getInstance().getTime();
	}

	public StoredNode(StoredData root) {
		this();
		this.root = root;
	}

	public StoredNode(StoredData root, Map<StoredData, Float> data) {
		this();
		this.root = root;
		this.connections = data;
	}

	public StoredNode( String identifier) {
		this( new StoredData( identifier ));
	}

	public StoredData getRoot() {
		return root;
	}
	
	public void addChild( StoredData data, float distance ) {
		this.connections.put(data, distance);
	}

	public float getDistance( StoredData data ) {
		return this.connections.get(data);
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public StoredData get( String identifier ) {
		if( StringUtils.isEmpty(identifier))
			return null;
		for( StoredData data: this.connections.keySet()) {
			if( data.getIdentifier().equals(identifier))
				return data;
		}
		return null;
	}

	public StoredData[] getConnections() {
		return this.connections.values().toArray( new StoredData[ this.connections.size()]);
	}
}
