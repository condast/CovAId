package org.covaid.core.data;

public class ContagionData<T extends Object> implements Cloneable{

	public static final int MAX_CONTAGIOUSNESS = 100;
	private T timeStep;
	private double risk;

	public ContagionData( T timeStep) {
		this( timeStep, MAX_CONTAGIOUSNESS );
	}
	
	public ContagionData( T timeStep, double contagiousness) {
		super();
		this.timeStep = timeStep;
		this.risk = contagiousness;
	}

	public T getMoment() {
		return timeStep;
	}

	public void setTimeStep(T timeStep) {
		this.timeStep = timeStep;
	}

	public double getRisk() {
		return risk;
	}

	public void setRisk(double contagiousness) {
		this.risk = contagiousness;
	}
	
	@Override
	public ContagionData<T> clone(){
		return new ContagionData<T>( this.timeStep, this.risk );
	}

	@Override
	public String toString() {
		return "{" + (( timeStep == null )? "null": timeStep.toString()) + ", " + risk + "}";
	}
}
