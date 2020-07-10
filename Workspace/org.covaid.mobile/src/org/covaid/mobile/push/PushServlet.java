package org.covaid.mobile.push;

import org.condast.commons.config.Config;
import org.condast.commons.strings.StringStyler;
import org.condast.js.commons.parser.AbstractFileParser;
import org.condast.js.push.core.AbstractPushServlet;

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
	protected String onGetPublicKey(String id, AbstractFileParser.Attributes attr) {
		return push.getPublicKey();
	}
}