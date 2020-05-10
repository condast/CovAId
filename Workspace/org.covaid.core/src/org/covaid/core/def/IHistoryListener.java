package org.covaid.core.def;

@FunctionalInterface
public interface IHistoryListener<T extends Object> {

	public void notifyContagionChanged( HistoryEvent<T> event );
}
