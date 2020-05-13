package org.covaid.core.data.frogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import org.covaid.core.def.IHub;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.Hub;
import org.covaid.core.model.Point;

public class HubData{

	private LocationData location;
		
	//The previous hubs, that usually imply that a person had moved to this hub through the other
	private Collection<Point> previous;

	public HubData( IHub<Integer> hub, int step) {
		this.location = new LocationData( hub.getLocation(), step );
		this.previous = new ArrayList<>();
	}

	public boolean addPrevious( IPoint point ) {
		return this.previous.add( (Point) point);
	}

	public boolean removePrevious( IPoint point ) {
		return this.previous.remove( point );
	}

	public LocationData getLocation() {
		return location;
	}
	
	public IPoint[] getPrevious() {
		Collection<Point> results = new ArrayList<Point>( this.previous);
		return results.toArray( new IPoint[ results.size()]);
	}

	public synchronized static HubData[] getHubs( Collection<Hub> hubs, int step ){
		Collection<HubData> results = new ArrayList<>();
		for( IHub<Integer> hub: hubs ) {
			HubData hd = new HubData(hub, step );
			if(( hd == null ) || ( hd.getLocation() == null ) || hd.getLocation().getPoint() == null )
				System.err.println("Error");
			for( IPoint point: hub.getPrevious()) {
				hd.addPrevious(point.clone());//use clone or otherwise a concurrent modification exception occurs
			}
			results.add( hd);
		}
		return results.toArray( new HubData[ results.size()]);
	}
}