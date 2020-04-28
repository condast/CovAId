package org.covaid.rest.resources;

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
import org.covaid.rest.core.Dispatcher;
import org.covaid.rest.model.Doctor;
import org.covaid.rest.model.Notification;
import org.covaid.rest.service.DoctorService;
import org.covaid.rest.service.NotificationService;

@Path("/push")
public class NotificationResource {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public NotificationResource() {}

	/**
	 * Find doctors in your area
	 * The system should return by giving the advice to contact a doctor
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/doctor")
	public Response findDoctor( @QueryParam("id") long id, @QueryParam("token") String token,  @QueryParam("location") String location ) {
		logger.info( "Select your doctor for: " + id );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		try{
			if( !dispatcher.isregistered(id, token))
				result = Response.status( HttpStatus.UNAUTHORISED.getStatus() ).build();
			DoctorService service = new DoctorService(dispatcher);
			Collection<Doctor> doctors = service.findDoctor(location);
			if( Utils.assertNull(doctors))
				result = Response.noContent().build();
			Gson gson = new Gson();
			String str = gson.toJson(doctors, Notification[].class );
			result = Response.ok( str ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
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
	@Path("/report")
	public Response report( @QueryParam("id") long id, @QueryParam("token") String token,  
			@QueryParam("identifier") String identifier, @QueryParam("remarks") String remarks,
			@QueryParam("doctor-id") long doctorId, String history ) {
		logger.info( "Report for " + id + ": " + history );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		NotificationService service = null;
		try{
			if( !dispatcher.isregistered(id, token))
				result = Response.status( HttpStatus.UNAUTHORISED.getStatus() ).build();
			DoctorService ds = new DoctorService(dispatcher);
			Doctor doctor = ds.find(doctorId);
			service = new NotificationService(dispatcher);
			service.open();
			Notification notification = service.create( identifier, remarks, doctor);
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
	 * Update the state of the notification
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
	@Path("/update")
	public Response update( @QueryParam("id") long id, @QueryParam("token") String token,  @QueryParam("identifier") String identifier, String history ) {
		logger.info( "Subscription request for " + id + ": " + history );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		try{
			if( !dispatcher.isregistered(id, token))
				result = Response.status( HttpStatus.UNAUTHORISED.getStatus() ).build();
			NotificationService service = new NotificationService(dispatcher);
			Collection<Notification> notifications = service.findNotification(identifier);
			if( Utils.assertNull(notifications))
				result = Response.noContent().build();
			Gson gson = new Gson();
			String str = gson.toJson(notifications, Notification[].class );
			result = Response.ok( str ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return result;
	}

	/**
	 * Confirm the illness or ailment. This is done by the doctor.
	 * The system should return by giving the advice to contact a doctor
	 * @param id
	 * @param token
	 * @param identifier
	 * @param history
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/confirm")
	public Response confirm( @QueryParam("id") long id, @QueryParam("token") String token,  @QueryParam("doctorId") String doctorId, 
			@QueryParam("identifier") String identifier, String history ) {
		logger.info( "Subscription request for " + id + ": " + history );
		Dispatcher dispatcher = Dispatcher.getInstance();
		Response result = null;
		try{
			if( !dispatcher.isregistered(id, token))
				result = Response.status( HttpStatus.UNAUTHORISED.getStatus() ).build();
			NotificationService service = new NotificationService(dispatcher);
			Collection<Notification> notifications = service.findNotification(identifier);
			if( Utils.assertNull(notifications))
				result = Response.noContent().build();
			Gson gson = new Gson();
			String str = gson.toJson(notifications, Notification[].class );
			result = Response.ok( str ).build();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			return Response.serverError().build();
		}
		return result;
	}
}