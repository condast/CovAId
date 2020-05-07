package org.covaid.orientdb;

import org.covaid.orientdb.db.DatabasePersistenceService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	 public static String BUNDLE_ID = "org.covaid.orientdb";

	private static BundleContext context;

	private static DatabasePersistenceService graph = DatabasePersistenceService.getInstance();

	static BundleContext getContext() {
		return context;
	}

	public static DatabasePersistenceService getGraphService() {
		return graph;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext){
		Activator.context = bundleContext;
		//graph.connect();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext){
		try {
			Activator.context = null;
			graph.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
