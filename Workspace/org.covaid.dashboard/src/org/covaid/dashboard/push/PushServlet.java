package org.covaid.dashboard.push;

import org.condast.js.commons.parser.AbstractFileParser.Attributes;
import org.condast.js.push.core.AbstractPushServlet;
import org.covaid.ui.push.Push;

public class PushServlet extends AbstractPushServlet {
	private static final long serialVersionUID = 1L;

	private static String TITLE = "CovAID Push Services";
	
	private Push push = Push.getInstance();
	
	public PushServlet() {
		super( TITLE );
		this.addPushListener(push);
	}
	
	@Override
	protected String onSetContext(String context, String application, String service) {
		return push.getServerContext( service );
	}

	@Override
	protected String onGetPublicKey(String id, Attributes attr) {
		return push.getPublicKey();
	}
}