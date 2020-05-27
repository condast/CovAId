package org.covaid.core.model.date;

import java.util.Date;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IContagion.SupportedContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.AbstractPerson;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Mobile;
import org.covaid.core.model.Point;

public class DatePerson extends AbstractPerson<Date>{

	public DatePerson( IPoint point, double safety, double health, IContagion contagion) {
		this( point.getIdentifier(), point.getXpos(), point.getYpos(), safety, health, contagion );
	}

	public DatePerson( String identifier, int xpos, int ypos, double safety, double health, IContagion contagion) {
		super( xpos, ypos, contagion, new Mobile<Date>( identifier, safety, health, new Point( xpos, ypos ), new DateHistory()));
	}

	public DatePerson(String identifier, int x, int y, double safety, double health) {
		this( identifier, x, y, safety, health, new Contagion( SupportedContagion.COVID_19, 100-health ));
	}

	@Override
	protected long getDifference(Date first, Date last) {
		return first.getTime() - last.getTime();
	}

	@Override
	protected IContagion createContagion(String identifier, double contagiousness) {
		return new Contagion(identifier, contagiousness);
	}

	@Override
	protected ILocation<Date> createLocation(String identifier, IPoint point) {
		return new DateLocation( identifier, point );
	}
}