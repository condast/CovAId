package org.covaid.core.model.frogger;

import java.util.ArrayList;
import java.util.Collection;

import org.covaid.core.def.IHub;

public class HubData{

	private LocationData location;

	public HubData( IHub<Integer> hub) {
		this.location = new LocationData( hub.getLocation());
	}

	public LocationData getLocation() {
		return location;
	}
	
	public static HubData[] getHubs( IHub<Integer>[] hubs ){
		Collection<HubData> results = new ArrayList<>();
		for( IHub<Integer> hub: hubs )
			results.add( new HubData( hub ));
		return results.toArray( new HubData[ results.size()]);
	}
}