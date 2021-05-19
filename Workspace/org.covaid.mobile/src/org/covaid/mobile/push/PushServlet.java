package org.covaid.mobile.push;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.condast.commons.config.Config;
import org.condast.commons.strings.StringStyler;
import org.condast.js.commons.parser.AbstractResourceParser;
import org.condast.js.push.core.AbstractPushServlet;
import org.condast.js.push.core.IPushListener;
import org.condast.js.push.core.IPushListener.Calls;
import org.condast.js.push.core.advice.IAdvice;
import org.covaid.core.data.DoctorData;
import org.covaid.core.def.IMobile;
import org.covaid.core.doctor.DoctorDataEvent;
import org.covaid.core.doctor.IDoctorDataListener.DocterDataEvents;
import org.covaid.mobile.core.Dispatcher;

public class PushServlet extends AbstractPushServlet {
	private static final long serialVersionUID = 1L;

	private static String TITLE = "CovAID Push Services";
	private static String COVAID = "covaid/";
	private static String CONTEXT = COVAID + "mobile";

	private enum Contexts{
		PUSH,
		MOBILE,
		LOCAL,
		HOME;	
	}
	private Config config;
	
	private Push push = Push.getInstance();
	
	public PushServlet() {
		super( TITLE );
		config = new Config();
		this.addPushListener(push);
	}
	
	@Override
	protected String onSetContext(String context, String application, String service) {
		String serverContext = config.getServerContext();
		//serverContext = serverContext.replace("localhost", "192.168.178.41");
		String path = null;
		switch( Contexts.valueOf(StringStyler.styleToEnum(service))) {
		case LOCAL:
			path = CONTEXT;
			break;
		case HOME:
			path = serverContext + COVAID + service;
			break;
		default:
			path = serverContext+ CONTEXT +"/" + service;
			break;
		}
		return path ;
	}

	@Override
	protected String onGetPublicKey(String id, AbstractResourceParser.Attributes attr) {
		return push.getPublicKey();
	}
	
	protected boolean onUpdate( Calls call, HttpServletRequest req, HttpServletResponse resp ) {
		String type = req.getParameter(IPushListener.Attributes.NOTIFICATION.toString());
		IAdvice.Notifications notification = IAdvice.Notifications.valueOf( StringStyler.styleToEnum(type)); 
		boolean result = false;
		switch( notification ) {
		case DONT_CARE:
			try {
				Dispatcher dispatcher = Dispatcher.getInstance(); 
				String mobileId = req.getParameter(IPushListener.Attributes.IDENTIFIER.toString());
				IMobile<Date> mobile = dispatcher.getMobile(mobileId);
				DoctorData data = new DoctorData(mobile);
				dispatcher.notifyDoctorDoctorChanged( new DoctorDataEvent(this,  DocterDataEvents.ADD, data));
			}
			catch( Exception ex) {
				ex.printStackTrace();
			}
			result = true;
			break;
		default:
			result = super.onUpdate(call, req, resp);
			break;
		}
		return result;
	}
}