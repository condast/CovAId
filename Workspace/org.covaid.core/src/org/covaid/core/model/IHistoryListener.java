package org.covaid.core.model;

@FunctionalInterface
public interface IHistoryListener {

	public void notifyContagionChanged( HistoryEvent event );
}
