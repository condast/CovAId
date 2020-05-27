package org.covaid.core.contagion;

import org.covaid.core.def.IContagion;
import org.covaid.core.operators.AbstractContagionOperator;

public class IntegerContagionOperator extends AbstractContagionOperator<Integer> {

	public IntegerContagionOperator(Integer init, IContagion contagion) {
		super(init, contagion);
	}

	public IntegerContagionOperator() {
		super();
	}

	@Override
	public long getDifference(Integer first, Integer last) {
		int f = ( first == null )?0: first;
		int l = ( last == null )?0: last;
		return f-l;
	}

	@Override
	public Integer subtract(Integer first, Integer last) {
		int result = (int) getDifference(first, last);
		return ( result < 0)? first: result;
	}

	@Override
	public boolean isLastEntry(Integer from, Integer reference) {
		return ( reference >= ( super.getCurrent() - from )) && ( reference <= super.getCurrent());
	}
}
