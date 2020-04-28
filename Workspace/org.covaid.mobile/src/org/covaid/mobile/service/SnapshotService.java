package org.covaid.mobile.service;

import java.util.Collection;

import javax.persistence.TypedQuery;

import org.condast.commons.persistence.service.AbstractEntityService;
import org.condast.commons.persistence.service.IPersistenceService;
import org.covaid.core.def.ILocation;
import org.covaid.core.model.Location;

public class SnapshotService extends AbstractEntityService<ILocation>{

	public static final String S_QUERY_FIND_ALL = "Location l ";
	public static final String S_QUERY_FIND_LOCATION = S_QUERY_FIND_ALL + " WHERE l.identifier = :identifier";
	public static final String S_QUERY_FIND_LOCATION_IN_RANGE = 
			"SELECT l FROM Location l WHERE l.latitude >= :latmin AND l.latitude <= :latmax AND "
			+ "l.longitude >= :lonmin AND l.longitude <= :lonmax ";

	public SnapshotService( IPersistenceService service ) {
		super( ILocation.class, service );
	}

	public ILocation create( String name, int x, int y ) {
		ILocation Location = new Location( name, x, y );
		super.create(Location);
		return Location;
	}

	public Collection<ILocation> findLocation( String identifier ){
		TypedQuery<ILocation> query = super.getTypedQuery( S_QUERY_FIND_LOCATION );
		query.setParameter("identifier", identifier);
		return query.getResultList();
	}

}
