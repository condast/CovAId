package org.covaid.mobile.resources;

import com.google.gson.Gson;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.condast.commons.Utils;
import org.condast.commons.messaging.http.IHttpRequest.HttpStatus;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IMobile;
import org.covaid.core.model.Mobile;
import org.covaid.mobile.core.Dispatcher;
import org.covaid.mobile.service.MobileService;
import org.covaid.mobile.service.SnapshotService;

@Path("/")
public class MobileResource {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public MobileResource() {}

	/**
	 * First report of an illness. Add the history so that the system can inform the network.
	 * The system should return by giving the advice to contact a doctor
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/create")
	public Response create( @QueryParam("identifier") String identifier ) {
		logger.info( "Create " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		MobileService service = null;
		try{
			service = new MobileService(dispatcher);
			service.open();
			IMobile mobile = service.create( identifier, dispatcher.getField());
			//dispatcher.addFieldListener(mobile);
			Gson gson = new Gson();
			String str = gson.toJson( new CreateMobileData(mobile), CreateMobileData.class);
			result = Response.ok( str ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			service.close();			
		}
		return result;
	}

	/**
	 * Set the health of the owner
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get")
	public Response getMobile( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier ) {
		logger.info( "Get mobile " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		MobileService service = null;
		try{
			service = new MobileService(dispatcher);
			service.open();
			Collection<IMobile> results = service.find(identifier);
			if( Utils.assertNull( results ))
				return Response.noContent().build();
			IMobile mobile = results.iterator().next();
			Gson gson = new Gson();
			String str = gson.toJson(mobile, Mobile.class);
			result = Response.ok( str ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			service.close();			
		}
		return result;
	}

	/**
	 * Set the health of the owner
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/health")
	public Response setHealth( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier,
			@QueryParam("health") int health) {
		logger.info( "Set health " + health );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		MobileService service = null;
		try{
			service = new MobileService(dispatcher);
			service.open();
			Collection<IMobile> results = service.find(identifier);
			if( Utils.assertNull( results ))
				return Response.noContent().build();
			IMobile mobile = results.iterator().next();
			mobile.setHealth(health);
			service.update(mobile);
			result = Response.ok().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			service.close();			
		}
		return result;
	}

	/**
	 * Set the safety of the owner
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/safety")
	public Response setSafety( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier,
			@QueryParam("safety") int safety) {
		logger.info( "Set safety " + safety );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		MobileService service = null;
		try{
			service = new MobileService(dispatcher);
			service.open();
			Collection<IMobile> results = service.find(identifier);
			if( Utils.assertNull( results ))
				return Response.noContent().build();
			IMobile mobile = results.iterator().next();
			mobile.setSafety( safety );
			service.update(mobile);
			result = Response.ok().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			service.close();			
		}
		return result;
	}

	/**
	 * Set the safety of the owner
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/email")
	public Response setEmail( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier,
			@QueryParam("email") String email) {
		logger.info( "Set email of general practitioner " + email );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		MobileService service = null;
		try{
			service = new MobileService(dispatcher);
			service.open();
			Collection<IMobile> results = service.find(identifier);
			if( Utils.assertNull( results ))
				return Response.noContent().build();
			IMobile mobile = results.iterator().next();
			mobile.setEmail( email );
			service.update(mobile);
			result = Response.ok().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			service.close();			
		}
		return result;
	}

	/**
	 * First report of an illness. Add the history so that the system can inform the network.
	 * The system should return by giving the advice to contact a doctor
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/snapshot")
	public Response shareSnapshot( @QueryParam("id") long id, @QueryParam("token") String token,  
			@QueryParam("identifier") String identifier, @QueryParam("xpos") int x, @QueryParam("ypos") int y,
			@QueryParam("doctor-id") long doctorId, String history ) {
		logger.info( "Report for " + id + ": " + history );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		SnapshotService service = null;
		try{
			if( !dispatcher.isregistered(id, token))
				result = Response.status( HttpStatus.UNAUTHORISED.getStatus() ).build();
			service = new SnapshotService(dispatcher);
			service.open();
			ILocation notification = service.create( identifier, x, y);
			result = Response.ok().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			service.close();			
		}
		return result;
	}
	
	@SuppressWarnings("unused")
	private static class CreateMobileData{
		
		private long id;
		private long token;
		private String identifier;
		public CreateMobileData( IMobile mobile) {
			super();
			this.identifier = mobile.getIdentifier();
			this.id = mobile.hashCode();
			this.token = mobile.getTimestamp().getTime();
		}	
	}
}