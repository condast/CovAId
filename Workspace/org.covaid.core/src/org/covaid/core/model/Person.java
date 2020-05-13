package org.covaid.core.model;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.IContagion.SupportedContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;

public class Person extends AbstractPerson<Integer>{

	public Person( Point point, double safety, double health) {
		this( point.getIdentifier(), point.getXpos(), point.getYpos(), safety, health, new Contagion( SupportedContagion.COVID_19, 100-health ));
	}

	public Person( String identifier, int xpos, int ypos, double safety) {
		this( new Point( identifier, xpos, ypos ), safety, 100 );
	}
	
	/**
	 * The YPosition is a measure of the time
	 * @param identifier
	 * @param xpos
	 * @param ypos
	 * @param safety
	 * @param health
	 * @param contagion
	 */
	public Person( String identifier, int xpos, int ypos, double safety, double health, IContagion<Integer> contagion) {
		super( xpos, ypos, contagion, new Mobile<Integer>(identifier, safety, health, new Point( identifier, xpos, ypos), new History()));
	}

	@Override
	protected long getDifference(Integer first, Integer last) {
		return first-last;
	}

	@Override
	protected IContagion<Integer> createContagion(String identifier, double safety) {
		return new Contagion( identifier, safety );
	}

	@Override
	protected ILocation<Integer> createLocation(String identifier, IPoint point) {
		return new Location( identifier, point );
	}
	
	

}