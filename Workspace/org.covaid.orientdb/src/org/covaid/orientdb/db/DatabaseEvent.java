package org.covaid.orientdb.db;

import java.util.EventObject;

public class DatabaseEvent<T> extends EventObject{
	private static final long serialVersionUID = 1L;

	public DatabaseEvent(Object source) {
		super(source);
	}

}
