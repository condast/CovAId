package org.covaid.core.operators;

import java.util.HashMap;
import java.util.Map;

import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;

public class ContagionMapOperator<T extends Object> {

	private Map<IContagion, ContagionData<T>> contagions;

	private IContagionOperator<T> operator;

	public ContagionMapOperator( IContagionOperator<T> operator ) {
		this( operator, new HashMap<>());
	}

	public ContagionMapOperator( IContagionOperator<T> operator, Map<IContagion, ContagionData<T>> contagions ) {
		this.operator = operator;
		this.contagions = contagions;
	}

	public Map<IContagion, ContagionData<T>> getContagions() {
		return contagions;
	}

	public IContagionOperator<T> getOperator() {
		return operator;
	}

}
