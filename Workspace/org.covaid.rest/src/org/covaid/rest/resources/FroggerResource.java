package org.covaid.rest.resources;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.covaid.core.def.IEnvironment;
import org.covaid.rest.core.Dispatcher;

@Path("/")
public class FroggerResource {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public FroggerResource() {}

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