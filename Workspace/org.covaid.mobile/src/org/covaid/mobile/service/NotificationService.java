package org.covaid.mobile.service;

import java.util.Collection;

import javax.persistence.TypedQuery;

import org.covaid.core.model.Doctor;
import org.covaid.mobile.model.Notification;
import org.covaid.orientdb.def.IOrientPersistenceService;
import org.covaid.orientdb.object.AbstractEntityService;

public class NotificationService extends AbstractEntityService<Notification>{

	public static final String S_QUERY_FIND_ALL = "Notification n ";
	public static final String S_QUERY_FIND_NOTIFICATION = S_QUERY_FIND_ALL + " WHERE n.identifier = :identifier";
	public static final String S_QUERY_FIND_NOTIFICATION_IN_RANGE = 
			"SELECT l FROM Notification l WHERE l.latitude >= :latmin AND l.latitude <= :latmax AND "
			+ "l.longitude >= :lonmin AND l.longitude <= :lonmax ";

	public NotificationService(  IOrientPersistenceService service ) {
		super( Notification.class, service );
	}

	public Notification create( String name, String identifier, Doctor doctor ) {
		Notification Notification = new Notification( name, doctor, identifier );
		//super.create(Notification);
		return Notification;
	}

	public Collection<Notification> findNotification( String identifier ){
		TypedQuery<Notification> query = super.getTypedQuery( S_QUERY_FIND_NOTIFICATION );
		query.setParameter("identifier", identifier);
		return query.getResultList();
	}

}
