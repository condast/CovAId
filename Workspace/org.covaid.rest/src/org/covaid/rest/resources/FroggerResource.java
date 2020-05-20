package org.covaid.rest.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.condast.commons.Utils;
import org.condast.commons.strings.StringUtils;
import org.covaid.core.data.frogger.HubData;
import org.covaid.core.data.frogger.LocationData;
import org.covaid.core.environment.IEnvironment;
import org.covaid.rest.core.Dispatcher;

@Path("/")
public class FroggerResource {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Lock lock;

	public FroggerResource() {
		lock = new ReentrantLock();
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
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/register")
	public Response create( @QueryParam("identifier") String identifier ) {
		logger.info( "Create " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			IEnvironment<Integer> env = dispatcher.register(identifier);
			Gson gson = new Gson();
			String str = gson.toJson( new CreateRegisterData(env), CreateRegisterData.class);
			result = Response.ok( str ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return result;
	}

	/**
	 * Start the frogger environment
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/start")
	public Response start( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier,
			@QueryParam("width") int width, @QueryParam("density") int density,  @QueryParam("infected") int infected) {
		logger.info( "Start mobile " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			boolean result = dispatcher.start(identifier, width, density, infected );
			response = Response.ok( result ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return response;
	}

	/**
	 * Pause the frogger environment
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/pause")
	public Response pause( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier ) {
		logger.info( "Get mobile " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			boolean result = dispatcher.pause(identifier);
			response = Response.ok( result ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return response;
	}

	/**
	 * Start the frogger environment
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/stop")
	public Response stop( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier ) {
		logger.info( "Get mobile " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			boolean result = dispatcher.stop(identifier);
			response = Response.ok( result ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return response;
	}

	/**
	 * Start the frogger environment
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/clear")
	public Response clear( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier ) {
		logger.info( "Clear environment " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			boolean result = dispatcher.stop(identifier);
			result &= dispatcher.clear(identifier);
			response = Response.ok( result ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return response;
	}

	/**
	 * Start the frogger environment
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/set-infected")
	public Response setInfected( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier,
			@QueryParam("infected") int infected) {
		logger.info( "Set infected environment " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			boolean result = dispatcher.setInfected(identifier, infected);
			response = Response.ok( result ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return response;
	}

	/**
	 * Set the protection for the frogger environment
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/set-protection")
	public Response setProtection( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier,
			@QueryParam("protection") boolean protection) {
		logger.fine( "Set protection environment " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			boolean result = dispatcher.setProtection(identifier, protection);
			response = result? Response.ok( protection ).build(): Response.noContent().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return response;
	}

	/**
	 * Get the protected locations
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/protected")
	public Response getProtected( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier) {
		logger.fine( "Get protected " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			LocationData<Integer>[] results = dispatcher.getProtected(identifier);
			if( Utils.assertNull(results))
				return Response.noContent().build();
			GsonBuilder builder = new GsonBuilder();
			builder.enableComplexMapKeySerialization();
			Gson gson = builder.create();
			String str=  gson.toJson(results, LocationData[].class);
			response = Response.ok(str).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			response = Response.serverError().build();
		}
		return response;
	}

	/**
	 * dispose of the frogger environment
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/dispose")
	public Response dispose( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier ) {
		logger.info( "Clear environment " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			boolean result = dispatcher.stop(identifier);
			dispatcher.dispose( identifier );
			response = Response.ok( result ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return response;
	}

	/**
	 * Get the updated information about the hubs
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/update")
	public synchronized Response getUpdate( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier,
			@QueryParam("step") int step ) {
		logger.fine( "Update information " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			lock.lock();
			try {
				Collection<HubData> hubs = dispatcher.getUpdate(identifier, step);
				HubData[] results = hubs.toArray( new HubData[ hubs.size()]);
				if( Utils.assertNull(results)) 
					return Response.noContent().build();
				GsonBuilder builder = new GsonBuilder();
				Gson gson = builder.enableComplexMapKeySerialization().create();
				String str = gson.toJson( results, HubData[].class);
				response = Response.ok( str ).build();
			}
			finally {
				lock.unlock();
			}
		}
		catch( Exception ex ){
			logger.warning(ex.getMessage());
			return Response.serverError().build();
		}
		return response;
	}

	/**
	 * Get the updated information about the hubs
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/surroundings")
	public synchronized Response getSurroundings( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier,
			@QueryParam("radius") int radius, @QueryParam("step") int step  ) {
		logger.fine( "Get surroundings " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response response = null;
		try{
			if( StringUtils.isEmpty(identifier))
				return Response.serverError().build();
			lock.lock();
			try {
				LocationData<Integer>[] results = dispatcher.getSurroundings(identifier, radius, step);
				if( Utils.assertNull(results)) 
					return Response.noContent().build();
				GsonBuilder builder = new GsonBuilder();
				Gson gson = builder.enableComplexMapKeySerialization().create();
				String str = gson.toJson( results, HubData[].class);
				response = Response.ok( str ).build();
			}
			finally {
				lock.unlock();
			}
		}
		catch( Exception ex ){
			logger.warning(ex.getMessage());
			return Response.serverError().build();
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
	@Path("/status")
	public Response getStatus( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier ) {
		logger.info( "Get status " + identifier );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		try{
			Gson gson = new Gson();
			String str = gson.toJson( null, CreateRegisterData.class);
			result = Response.ok( str ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
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
	@Path("/move")
	public Response setSafety( @QueryParam("id") long id, @QueryParam("token") long token, @QueryParam("identifier") String identifier,
			@QueryParam("angle") int angle) {
		logger.info( "Set safety " + angle );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		try{
			//dispatcher.move( identifier );
			result = Response.ok().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return result;
	}
	
	@SuppressWarnings("unused")
	private static class CreateRegisterData{
		
		private long id;
		private long token;
		private String identifier;
		public CreateRegisterData( IEnvironment<Integer> environment) {
			super();
			this.identifier = environment.getName();
			this.id = environment.hashCode();
			this.token = Calendar.getInstance().getTime().getTime();
		}	
	}
}