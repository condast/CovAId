package org.covaid.orientdb.rest;

import java.util.Collection;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.condast.commons.Utils;
import org.covaid.core.data.SharedData;
import org.covaid.core.data.StoredNode;
import org.covaid.orientdb.core.Dispatcher;
import org.covaid.orientdb.db.OrientDatabase;

import com.google.gson.Gson;

//Sets the path to base URL + /rest
@Path("/")
public class DatabaseRestService{

	public static final String S_IDENIFIER = "CovAId";

	public static final String S_ERR_UNKNOWN_REQUEST = "An invalid request was retrieved: ";
	public static final String S_ERR_INVALID_VESSEL = "A request was received from an unknown vessel:";
	
	private Dispatcher dispatcher = Dispatcher.getInstance();

	@SuppressWarnings("unchecked")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/add")
	public Response addModel( @QueryParam("id") long id, @QueryParam("token") long token, 
			String data ) {
		StoredNode<Date> node = null;
		if( !dispatcher.isRegistered(id, token))
			return Response.status( Status.UNAUTHORIZED ).build();
		Gson gson = new Gson();
		node = gson.fromJson(data, StoredNode.class);
		OrientDatabase db = new OrientDatabase( S_IDENIFIER);
		db.open();
		boolean result = false;
		try {
			result = db.add(node);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			db.close();
		}
		return result? Response.ok().build(): Response.notModified().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/contains")
	public Response contains( @QueryParam("id") long id, @QueryParam("token") long token, 
			@QueryParam("identifier") String identifier) {
		if( !dispatcher.isRegistered(id, token))
			return Response.status( Status.UNAUTHORIZED ).build();
		OrientDatabase db = new OrientDatabase( S_IDENIFIER);
		db.open();
		Collection<StoredNode<Date>> result = null;
		try {
			result = db.search( SharedData.Attributes.IDENTIFIER.name(), identifier);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			db.close();
		}
		return Response.ok( Utils.assertNull( result )).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get")
	public Response get( @QueryParam("id") long id, @QueryParam("token") long token, 
			@QueryParam("identifier") String identifier ) {
		if( !dispatcher.isRegistered(id, token))
			return Response.status( Status.UNAUTHORIZED ).build();
		OrientDatabase db = new OrientDatabase( S_IDENIFIER);
		db.open();
		Collection<StoredNode<Date>> result = null;
		try {
			result = db.search( SharedData.Attributes.IDENTIFIER.name(), identifier);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			db.close();
		}
		Gson gson = new Gson();
		String str = gson.toJson(result.toArray( new StoredNode[result.size()]), StoredNode[].class);
		return Response.ok( str ).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/search")
	public Response search( @QueryParam("id") long id, @QueryParam("token") long token, 
			@QueryParam("key") String key, @QueryParam("value") String wildcard) {
		if( !dispatcher.isRegistered(id, token))
			return Response.status( Status.UNAUTHORIZED ).build();
		OrientDatabase db = new OrientDatabase( S_IDENIFIER);
		db.open();
		Collection<StoredNode<Date>> result = null;
		try {
			result = db.search( key, wildcard);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			db.close();
		}
		Gson gson = new Gson();
		String str = gson.toJson(result.toArray( new StoredNode[result.size()]), StoredNode[].class);
		return Response.ok( str ).build();
	}

	@SuppressWarnings("unchecked")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/update")
	public Response update( @QueryParam("id") long id, @QueryParam("token") long token, String data) {
		StoredNode<Date> node = null;
		if( !dispatcher.isRegistered(id, token))
			return Response.status( Status.UNAUTHORIZED ).build();
		Gson gson = new Gson();
		node = gson.fromJson(data, StoredNode.class);
		OrientDatabase db = new OrientDatabase( S_IDENIFIER);
		db.open();
		boolean result = false;
		try {
			result = db.update(node);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			db.close();
		}
		return result? Response.ok().build(): Response.notModified().build();
	}

	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/remove")
	public Response remove( @QueryParam("id") long id, @QueryParam("token") long token,
			@QueryParam("domain") String domainstr, @QueryParam("model-id") String modelId) {
		StoredNode<Date> node = null;
		if( !dispatcher.isRegistered(id, token))
			return Response.status( Status.UNAUTHORIZED ).build();
		OrientDatabase db = new OrientDatabase( S_IDENIFIER);
		db.open();
		boolean result = false;
		try {
			result = db.remove(node);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			db.close();
		}
		return result? Response.ok().build(): Response.notModified().build();
	}
}