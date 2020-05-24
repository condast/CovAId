package org.covaid.mobile.resources;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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
import org.covaid.core.model.date.DateMobile;
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
	public Response create() {
		logger.info( "Create New Mobile" );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		MobileService service = null;
		try{
			service = new MobileService(dispatcher);
			service.open();
			IMobile<Date> mobile = service.create( DateFormat.getTimeInstance().format( Calendar.getInstance().getTime()), dispatcher.getField());
			dispatcher.addMobile(mobile);
			Gson gson = new Gson();
			String str = gson.toJson( new CreateMobileData(mobile), CreateMobileData.class);
			result = Response.ok( str ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			if( service != null )
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
	@Path("/remove")
	public Response remove( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier ) {
		logger.info( "Remove mobile " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		MobileService service = null;
		try{
			service = new MobileService(dispatcher);
			service.open();
			dispatcher.removeMobile(identifier);
			boolean result = service.remove(identifier);
			if( !result )
				return Response.noContent().build();
			response = Response.ok().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			if( service != null )
				service.close();			
		}
		return response;
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
			IMobile<Date> mobile = dispatcher.getMobile(identifier);
			if( mobile == null ) {
				service = new MobileService(dispatcher);
				service.open();
				Collection<IMobile<Date>> results = service.find(identifier);
				if( Utils.assertNull( results ))
					return Response.noContent().build();
				mobile = results.iterator().next();
			}
			Gson gson = new Gson();
			String str = gson.toJson(mobile, DateMobile.class);
			result = Response.ok( str ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			if( service != null )
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
			Collection<IMobile<Date>> results = service.find(identifier);
			if( Utils.assertNull( results ))
				return Response.noContent().build();
			IMobile<Date> mobile = results.iterator().next();
			mobile.setHealth(health);
			service.update(mobile);
			dispatcher.addMobile(mobile);
			result = Response.ok( "ok").build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			if( service != null )
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
			Collection<IMobile<Date>> results = service.find(identifier);
			if( Utils.assertNull( results ))
				return Response.noContent().build();
			IMobile<Date> mobile = results.iterator().next();
			mobile.setSafety( safety );
			service.update(mobile);
			dispatcher.addMobile(mobile);
			result = Response.ok("ok").build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			if( service != null )
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
	@Produces(MediaType.TEXT_PLAIN)
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
			Collection<IMobile<Date>> results = service.find(identifier);
			if( Utils.assertNull( results ))
				return Response.noContent().build();
			IMobile<Date> mobile = results.iterator().next();
			mobile.setEmail( email );
			service.update(mobile);
			dispatcher.addMobile(mobile);
			result = Response.ok().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			if( service != null )
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
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
			ILocation<Date> notification = service.create( identifier, x, y);
			result = Response.ok().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		finally {
			if( service != null )
				service.close();			
		}
		return result;
	}
	
	@SuppressWarnings("unused")
	private static class CreateMobileData{
		
		private long id;
		private long token;
		private String identifier;
		public CreateMobileData( IMobile<Date> mobile) {
			super();
			this.identifier = mobile.getIdentifier();
			this.id = mobile.hashCode();
			this.token = mobile.getTimestamp().getTime();
		}	
	}
}