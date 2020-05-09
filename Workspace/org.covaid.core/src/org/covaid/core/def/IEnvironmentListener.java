package org.covaid.core.def;

@FunctionalInterface
public interface IEnvironmentListener<T extends Object> {

	public void notifyChanged( EnvironmentEvent<T> event );
}
