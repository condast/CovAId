package org.covaid.core.config.env;

@FunctionalInterface
public interface ISimulationListener {

	public void notifyPersonChanged( SimulationEvent event );
}
