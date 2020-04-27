package org.covaid.core.model;

@FunctionalInterface
public interface IEnvironmentListener {

	public void notifyPersonChanged( EnvironmentEvent event );
}
