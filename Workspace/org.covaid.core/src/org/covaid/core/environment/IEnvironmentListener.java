package org.covaid.core.environment;

@FunctionalInterface
public interface IEnvironmentListener<T extends Object> {

	public void notifyChanged( EnvironmentEvent<T> event );
}
