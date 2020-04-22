package org.covaid.core.config.env;

@FunctionalInterface
public interface IHistoryListener {

	public void notifyContagionChanged( HistoryEvent event );
}
