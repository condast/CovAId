package org.covaid.core.data.frogger;

import java.util.HashMap;
import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Point;

public class LocationData {

	private Map<Contagion, Double> contagions;
	
	private Point point;

	public LocationData( ILocation<Integer> location, int step ) {
		this.point = (Point) location.toPoint();
		contagions = new HashMap<>();
		for( IContagion<Integer> contagion: location.getContagion())
			contagions.put((Contagion) contagion, contagion.getContagiousness( location.getInfectionDate(contagion), step ));
	}

	public Map<Contagion, Double> getContagions() {
		return contagions;
	}

	public Point getPoint() {
		return point;
	}
}
