package org.covaid.core.model;

public class Contagion extends AbstractContagion<Integer>{
	
	public Contagion(String identifier, double contagiousness, double threshold, int distance, int maxDays,
			int halftime, double dispersion, boolean monitored) {
		super(identifier, contagiousness, threshold, distance, maxDays, halftime, dispersion, monitored);
	}

	public Contagion(String identifier, double contagiousness, int distance, int maxDays) {
		super(identifier, contagiousness, distance, maxDays);
	}

	public Contagion(SupportedContagion contagion) {
		super(contagion.name(), 100f);
	}

	public Contagion(String identifier, double contagiousness) {
		super(identifier, contagiousness);
	}

	public Contagion(SupportedContagion identifier, double contagiousness) {
		super(identifier, contagiousness);
	}

	@Override
	protected long getDifference(Integer first, Integer last) {
		return first-last;
	}

	@Override
	public boolean isContagious( Integer step) {
		return this.isContagious(  step - getTimestamp());
	}
}
