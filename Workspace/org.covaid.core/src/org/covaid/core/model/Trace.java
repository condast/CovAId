package org.covaid.core.model;

import org.covaid.core.hub.AbstractTrace;
import org.covaid.core.hub.IHub;

public class Trace extends AbstractTrace<Integer> {

	public Trace(IHub<Integer> hub) {
		super(hub);
	}

	@Override
	protected void onUpdateSymbiot(Integer timeStep, Symbiot<Integer> symbiot, double contagiousness) {
		symbiot.setContagiousness( symbiot.getContagiousness() + contagiousness /2);
		symbiot.setTimeStep(( symbiot.getTimeStep() + timeStep )/2);
	}
}
