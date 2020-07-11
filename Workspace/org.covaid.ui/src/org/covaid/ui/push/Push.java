package org.covaid.ui.push;

import java.io.IOException;
import java.util.Properties;

import org.condast.commons.config.Config;
import org.condast.commons.config.ConfigBuilder;
import org.condast.js.push.core.AbstractPush;
import org.condast.js.push.core.advice.Advice;
import org.condast.js.push.core.advice.IAdvice;

public class Push extends AbstractPush{

	private static final String S_COVAID = "COVAID"; 
	private static String CONTEXT = "covaid/mobile";
	private static final String S_PUSH = "push"; 

	private Config config;
	
	private static Push push = new Push(); 
	
	private Properties properties;
	
	private Push() {
		super( S_COVAID );
		config = new Config();
		ConfigBuilder builder = new ConfigBuilder();
		builder.build();
		properties = builder.getParsedUnit(S_COVAID);
		super.setPublicKey(getPublicKey());
		super.setPrivateKey(getPrivateKey());
	}
	
	public static final Push getInstance() {
		return push;
	}

	public void initialise() throws IOException {
		super.initialise(getServerContext(S_PUSH));
	}

	public String getPublicKey() {
		return properties.getProperty(ConfigBuilder.Nodes.PUBLIC_KEY.name());
	}

	public String getPrivateKey() {
		return properties.getProperty(ConfigBuilder.Nodes.PRIVATE_KEY.name());
	}

	public String getServerContext( String service ) {
		String serverContext = config.getServerContext();
		return serverContext+ CONTEXT +"/" + service ;		
	}
	
	public IAdvice createAdvice( long userId, long adviceId, String identifier, String member, IAdvice.AdviceTypes type, String text, String icon, String badge, int repeat) {
		return new Advice( userId, adviceId, identifier, member, type, text, icon, badge, repeat );
	}
}
