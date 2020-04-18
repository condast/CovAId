package org.covaid.core.config;

public interface IConfigurationChangeListener {

	public enum ConfigurationEvents{
		SET_ROOT,
		CONFIG_ADDED,
		CONFIG_REMOVED,
		STATUS_CHANGED
	}
	public void notifyConfigurationChanged( ConfigurationEvent event );
}
