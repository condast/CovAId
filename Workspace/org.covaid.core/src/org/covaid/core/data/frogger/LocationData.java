package org.covaid.core.data.frogger;

import java.util.Map;

import org.condast.commons.Utils;
import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Point;

public class LocationData<T extends Object> {

	private Map<Contagion, ContagionData<T>> contagions;
	
	private Point point;

	public LocationData( Point point, Map<Contagion, ContagionData<T>> contagions ) {
		this.point = point;
		this.contagions = contagions;
	}

	public Map<Contagion, ContagionData<T>> getContagions() {
		return contagions;
	}

	public Point getPoint() {
		return point;
	}

	public Double getRisk(IContagion<T> contagion, int timeStep) {
		return Utils.assertNull(this.contagions)?0: this.contagions.get(contagion).getRisk();
	}

	public double getContagion(IContagion<Integer> contagion, int day) {
		return contagions.get(contagion).getRisk();
	}
}
