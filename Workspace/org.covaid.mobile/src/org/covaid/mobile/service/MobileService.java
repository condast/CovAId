package org.covaid.mobile.service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.persistence.EntityManager;

import org.condast.commons.data.plane.IField;
import org.condast.commons.persistence.service.AbstractEntityService;
import org.condast.commons.persistence.service.IPersistenceService;
import org.covaid.core.def.IMobile;
import org.covaid.core.model.date.DateMobile;
import org.covaid.orientdb.def.IOrientPersistenceService;

public class MobileService extends AbstractEntityService<IMobile>{

	public static final String S_QUERY_FIND_ALL = "SELECT FROM DateMobile ";
	public static final String S_QUERY_FIND_MOBILE = S_QUERY_FIND_ALL + "WHERE identifier='";

	
	protected MobileService(Class<IMobile> clss, IPersistenceService<EntityManager> service) {
		super(clss, service);
	}

	public MobileService( IOrientPersistenceService service ) {
		this( IMobile.class,null/*service */ );
	}

	public IMobile<Date> create( String name, IField field ) {
		Calendar calendar = Calendar.getInstance();
		String identifier = name + ":" + calendar.getTimeInMillis();
		DateMobile mobile = null;
		try{
			//mobile = (DateMobile) super.create( DateMobile.class );
			mobile.setIdentifier( toHex( identifier, "UTF-8" ));
			mobile.setField( field );
			super.update(mobile);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		return mobile;
	}

	public Collection<IMobile<Date>> find( String identifier ){
		Collection<IMobile<Date>> results = null;// super.query( S_QUERY_FIND_MOBILE + identifier + "'");
		return results;
	}

	public boolean remove( String identifier ){
		Collection<IMobile<Date>> results = null;//super.query( S_QUERY_FIND_MOBILE + identifier + "'");
		boolean result = false;
		//for( IMobile<Date> mobile: results)
		//	result |= super.remove(mobile);
		return result;
	}

	public static String toHex(String arg, String charset) throws UnsupportedEncodingException {
	    return String.format("%040x", new BigInteger(1, arg.getBytes( charset )));
	}
}
