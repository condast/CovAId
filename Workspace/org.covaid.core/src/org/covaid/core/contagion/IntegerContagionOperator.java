package org.covaid.core.contagion;

import org.covaid.core.def.IContagion;
import org.covaid.core.operators.AbstractContagionOperator;

public class IntegerContagionOperator extends AbstractContagionOperator<Integer> {

	public IntegerContagionOperator(Integer current, IContagion contagion) {
		super(current, contagion);
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

	/**
	 * Returns true if the reference is within the current time and the range
	 * reference >= current-range AND reference <= current
	 */
	@Override
	public boolean isInRange(Integer range, Integer reference) {
		return ( reference >= ( super.getCurrent() - range )) && ( reference <= super.getCurrent());
	}
}
