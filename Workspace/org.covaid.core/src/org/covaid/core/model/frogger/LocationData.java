package org.covaid.core.model.frogger;

import java.util.HashMap;
import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Point;

public class LocationData {

	private Map<Contagion, Double> contagions;
	
	private Point point;

	public LocationData( ILocation<Integer> location ) {
		this.point = (Point) location.toPoint();
		contagions = new HashMap<>();
		for( IContagion<Integer> contagion: location.getContagion())
			contagions.put((Contagion) contagion, location.getContagion(contagion));
	}

	public Map<Contagion, Double> getContagions() {
		return contagions;
	}

	public Point getPoint() {
		return point;
	}
}
