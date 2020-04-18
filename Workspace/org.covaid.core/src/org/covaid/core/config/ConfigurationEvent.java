package org.covaid.core.config;

import java.util.EventObject;

import org.covaid.core.config.IConfigurationChangeListener.ConfigurationEvents;

public class ConfigurationEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private IConfigData data;
	private ConfigurationEvents configEvent;
	
	public ConfigurationEvent(Object source, ConfigurationEvents configEvent, IConfigData data) {
		super(source);
		this.configEvent = configEvent;
		this.data = data;
	}

	public ConfigurationEvents getConfigEvent() {
		return configEvent;
	}

	public IConfigData getData() {
		return data;
	}
}