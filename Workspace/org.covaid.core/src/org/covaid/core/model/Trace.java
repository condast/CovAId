package org.covaid.core.model;

import org.covaid.core.data.ContagionData;
import org.covaid.core.hub.AbstractTrace;
import org.covaid.core.hub.IHub;

public class Trace extends AbstractTrace<Integer> {

	public Trace(IHub<Integer> hub) {
		super(hub);
	}

	@Override
	protected void onUpdateSymbiot(Integer timeStep, ContagionData<Integer> symbiot, double contagiousness) {
		symbiot.setRisk( symbiot.getRisk() + contagiousness /2);
		symbiot.setTimeStep(( symbiot.getTimeStep() + timeStep )/2);
	}
}
