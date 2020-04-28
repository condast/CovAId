package org.covaid.rest.service;

import java.util.Collection;

import javax.persistence.TypedQuery;

import org.condast.commons.persistence.service.AbstractEntityService;
import org.condast.commons.persistence.service.IPersistenceService;
import org.covaid.rest.model.Doctor;

public class DoctorService extends AbstractEntityService<Doctor>{

	public static final String S_QUERY_FIND_ALL = "Doctor d ";
	
	//Identifier is postcode or latlng, etc.
	public static final String S_QUERY_FIND_DOCTOR = S_QUERY_FIND_ALL + " WHERE d.identifier = :identifier";
	public static final String S_QUERY_FIND_DOCTOR_IN_RANGE = 
			"SELECT l FROM Doctor l WHERE l.latitude >= :latmin AND l.latitude <= :latmax AND "
			+ "l.longitude >= :lonmin AND l.longitude <= :lonmax ";

	public DoctorService( IPersistenceService service ) {
		super( Doctor.class, service );
	}

	public Doctor create( String name, String identifier, String Doctor ) {
		Doctor doctor = new Doctor( );
		super.create(doctor);
		return doctor;
	}

	public Collection<Doctor> findDoctor( String identifier ){
		TypedQuery<Doctor> query = super.getTypedQuery( S_QUERY_FIND_DOCTOR );
		query.setParameter("identifier", identifier);
		return query.getResultList();
	}

}
