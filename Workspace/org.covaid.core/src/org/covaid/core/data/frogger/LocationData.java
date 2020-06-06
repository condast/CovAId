package org.covaid.core.data.frogger;

import java.util.HashMap;
import java.util.Map;

import org.condast.commons.Utils;
import org.covaid.core.contagion.IntegerContagionOperator;
import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Point;

public class LocationData {

	private Map<Contagion, ContagionData<Integer>> contagions;
	
	private Point point;

	public LocationData( ILocation<Integer> location ) {
		this.point = (Point) location.toPoint();
		this.contagions = new HashMap<>();
		for( Map.Entry<IContagion, ContagionData<Integer>> entry: location.getContagions().entrySet())
			contagions.put((Contagion) entry.getKey(), entry.getValue());
	}

	public LocationData( Point point, Map<Contagion, ContagionData<Integer>> contagions ) {
		this.point = point;
		this.contagions = contagions;
	}

	public Map<Contagion, ContagionData<Integer>> getContagions() {
		return contagions;
	}

	public Point getPoint() {
		return point;
	}

	public double getRisk(IContagion contagion, int timeStep) {
		if( Utils.assertNull(this.contagions))
			return 0;
		IntegerContagionOperator operator = new IntegerContagionOperator(timeStep, contagion);
		return operator.getContagiousness( this.contagions.get(contagion));
	}
}
