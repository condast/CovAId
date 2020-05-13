package org.covaid.orientdb.db;

import java.util.Date;

import org.covaid.core.data.StoredNode;

public interface IDatabaseListener {

	void notifyChange(DatabaseEvent<StoredNode<Date>> event);

}
