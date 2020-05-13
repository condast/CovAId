package org.covaid.core.def;

public interface IPersonListener<T extends Object> {

	public void notifyPersonChanged( PersonEvent<T> event );
}
