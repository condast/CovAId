package org.covaid.core.def;

@FunctionalInterface
public interface IHistoryListener {

	public void notifyContagionChanged( HistoryEvent event );
}
