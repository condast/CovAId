package org.covaid.mobile.service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;

import org.condast.commons.data.plane.IField;
import org.covaid.core.def.IMobile;
import org.covaid.core.model.Mobile;
import org.covaid.orientdb.def.IOrientPersistenceService;
import org.covaid.orientdb.object.AbstractEntityService;

public class MobileService extends AbstractEntityService<IMobile>{

	public static final String S_QUERY_FIND_ALL = "SELECT FROM Mobile WHERE ";
	public static final String S_QUERY_FIND_MOBILE = S_QUERY_FIND_ALL + " identifier = '";

	public MobileService( IOrientPersistenceService service ) {
		super( IMobile.class, service );
	}

	public IMobile create( String name, IField field ) {
		Calendar calendar = Calendar.getInstance();
		String identifier = name + ":" + calendar.getTimeInMillis();
		Mobile mobile = null;
		try{
			mobile = (Mobile) super.create( Mobile.class );
			mobile.setIdentifier( toHex( identifier, "UTF-8" ));
			mobile.setField( field );
			super.update(mobile);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		return mobile;
	}

	public Collection<IMobile> find( String identifier ){
		Collection<IMobile> results = super.query( S_QUERY_FIND_MOBILE + identifier + "'");
		return results;
	}

	public static String toHex(String arg, String charset) throws UnsupportedEncodingException {
	    return String.format("%040x", new BigInteger(1, arg.getBytes( charset )));
	}
}
