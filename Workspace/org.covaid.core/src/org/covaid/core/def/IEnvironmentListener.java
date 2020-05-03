package org.covaid.core.def;

@FunctionalInterface
public interface IEnvironmentListener {

	public void notifyChanged( EnvironmentEvent event );
}
