package org.covaid.mobile.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.condast.commons.messaging.http.IHttpRequest.HttpStatus;
import org.covaid.core.def.ILocation;
import org.covaid.mobile.core.Dispatcher;
import org.covaid.mobile.service.SnapshotService;

@Path("/push")
public class SnapshotResource {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public SnapshotResource() {}

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
}