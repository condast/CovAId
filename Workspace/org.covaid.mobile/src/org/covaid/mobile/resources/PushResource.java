package org.covaid.mobile.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.condast.commons.strings.StringUtils;
import org.covaid.mobile.core.Dispatcher;

//Sets the path to alias + path
@Path("/push/rest")
public class PushResource{

	public static final String S_ERR_UNKNOWN_REQUEST = "An invalid request was rertrieved: ";
	public static final String S_ERR_INVALID_VESSEL = "A request was received from an unknown vessel:";
	
	public static final String S_RES_INDEX_JS = "/resources/html/push.html";

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private Dispatcher service = Dispatcher.getInstance();

	public PushResource() {
	}

	@GET
	@Path("push")
	public Response getIndex(@QueryParam("id") long userId, @QueryParam("token") String token){
		Response response = Response.noContent().build();
		Scanner scanner = new Scanner( PushResource.class.getResourceAsStream( S_RES_INDEX_JS ));
		try {   
			if( userId < 0 ) {
				response = Response.status( Status.UNAUTHORIZED).build();
				return response;
			}
			Map<String, String> params = new HashMap<>();
			params.put("user-id", String.valueOf(userId));
			StringBuilder builder = new StringBuilder();
			while( scanner.hasNextLine()) {
				String line = scanner.nextLine();
				builder.append( StringUtils.replace(line, params));
			}
			response = Response.ok( builder.toString()).build();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			response = Response.serverError().build();
		}
		finally {
			scanner.close();
		}
		return response;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/subscribe")
	public Response subscribe( @QueryParam("id") long id, @QueryParam("token") String token, String subscription ) {
		try{
			logger.info( "Subscription request for " + id + ": " + subscription );
			//ISubscription sub = Push.subscribe(id, token, subscription);
			//service.start(id, 0);
			return /*( sub == null )? Response.status( Status.BAD_REQUEST).build():*/ 
				Response.ok().build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
	}
}