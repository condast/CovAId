package org.covaid.orientdb.core;

import com.orientechnologies.orient.core.entity.OEntityManagerClassHandler;

public class OrientClassHandler extends OEntityManagerClassHandler{

	public OrientClassHandler() {
	}

	@Override
	public synchronized void registerEntityClass(Class<?> iClass) {
		super.registerEntityClass(iClass.getSimpleName(), iClass);
	}

	@Override
	public synchronized void registerEntityClass(Class<?> iClass, boolean forceSchemaReload) {
		super.registerEntityClass(iClass.getSimpleName(), iClass, forceSchemaReload);
	}	
}
