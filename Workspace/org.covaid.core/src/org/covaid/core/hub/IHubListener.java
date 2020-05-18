package org.covaid.core.hub;

@FunctionalInterface
public interface IHubListener<T extends Object> {

	public void notifyHubChanged( HubEvent<T> event );
}
