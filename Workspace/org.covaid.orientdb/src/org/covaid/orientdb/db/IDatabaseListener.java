package org.covaid.orientdb.db;

import org.covaid.core.data.StoredNode;

public interface IDatabaseListener {

	void notifyChange(DatabaseEvent<StoredNode> event);

}
