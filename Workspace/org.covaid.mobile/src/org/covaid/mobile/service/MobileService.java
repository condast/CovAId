package org.covaid.mobile.service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;

import javax.persistence.TypedQuery;

import org.condast.commons.data.plane.IField;
import org.condast.commons.persistence.service.AbstractEntityService;
import org.condast.commons.persistence.service.IPersistenceService;
import org.covaid.core.def.IMobile;
import org.covaid.mobile.model.Mobile;

public class MobileService extends AbstractEntityService<IMobile>{

	public static final String S_QUERY_FIND_ALL = "Mobile m ";
	public static final String S_QUERY_FIND_MOBILE = S_QUERY_FIND_ALL + " WHERE m.identifier = :identifier";
	public static final String S_QUERY_FIND_MOBILE_IN_RANGE = 
			"SELECT l FROM Location l WHERE l.latitude >= :latmin AND l.latitude <= :latmax AND "
			+ "l.longitude >= :lonmin AND l.longitude <= :lonmax ";

	public MobileService( IPersistenceService service ) {
		super( IMobile.class, service );
	}

	public Mobile create( String name, IField field ) {
		Calendar calendar = Calendar.getInstance();
		String identifier = name + ":" + calendar.getTimeInMillis();
		Mobile mobile = null;
		try{
			mobile = new Mobile( toHex( identifier, "UTF-8" ), field );
			super.create(mobile);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		return mobile;
	}

	public Collection<IMobile> findLocation( String identifier ){
		TypedQuery<IMobile> query = super.getTypedQuery( S_QUERY_FIND_MOBILE );
		query.setParameter("identifier", identifier);
		return query.getResultList();
	}

	public static String toHex(String arg, String charset) throws UnsupportedEncodingException {
	    return String.format("%040x", new BigInteger(1, arg.getBytes( charset )));
	}
}
