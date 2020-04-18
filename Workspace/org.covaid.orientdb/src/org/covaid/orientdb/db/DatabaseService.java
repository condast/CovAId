package org.covaid.orientdb.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.condast.commons.authentication.core.AuthenticationEvent;
import org.condast.commons.authentication.user.ILoginUser;
import org.covaid.core.data.StoredNode;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * Handles the Orient Databae
 * @See :https://orientdb.com/docs/2.2/documenttx-Database-Tinkerpop.html
 * @author Kees
 *
 * @param <D>
 * @param <Descriptor>
 */
public class DatabaseService {
	
	public static final String S_BUNDLE_ID = "org.aieonf.orientdb";
	public static final String S_IDENTIFIER = "documenttxModel";
	
	protected static final String S_ROOT = "Root";
	protected static final String S_DESCRIPTORS = "Descriptors";

	private Collection<IDatabaseListener> listeners;
	
	private static DatabaseService service = new DatabaseService();
	
	private static DatabasePersistenceService persistence = DatabasePersistenceService.getInstance();

	private OrientGraph graph;
	
	private DatabaseService() {
		listeners = new ArrayList<>();
	}

	public static DatabaseService getInstance(){
		return service;
	}
	
	public OrientGraph getGraph() {
		return graph;
	}

	/**
	 * Register a new user to the database
	 * @param domain
	 * @param login
	 * @return
	 */
	protected boolean register( ODatabaseDocumentTx dbdoc, AuthenticationEvent login ) {
		OSecurity sm = graph.getRawGraph().getMetadata().getSecurity();
		ILoginUser loginUser = login.getUser();
		switch( login.getEvent() ) {
		case REGISTER:
			OUser user = sm.createUser( loginUser.getUserName(), login.getPassword(), new String[] { ILoginUser.Roles.ADMIN.name().toLowerCase() });
			return true;
		case LOGIN:
			List<ODocument> users = sm.getAllUsers();
			for( ODocument doc: users ) {
				OUser ouser = new OUser( doc );
				if(ouser.getName().equals( loginUser.getUserName() ))
					return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	/**
	 * Connect to the database
	 * 
	 * @param loader
	 */
	public boolean open( ){
		if( !persistence.isConnected() )
			return false;
		graph = persistence.createDatabase();
		ODatabaseDocumentTx database = graph.getRawGraph();
		if(!database.isActiveOnCurrentThread())
			database.activateOnCurrentThread();
		//graph.begin();
		return true;
	}
	
	public String getIdentifier(){
		return S_IDENTIFIER;
	}
	
	public void addListener(IDatabaseListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(IDatabaseListener listener) {
		this.listeners.remove(listener);
	}

	protected final void notifyListeners( DatabaseEvent<StoredNode> event ){
		for( IDatabaseListener listener: this.listeners )
			listener.notifyChange(event);
	}
	
	public boolean isOpen(){
		return (this.graph != null ) && !this.graph.isClosed();
	}

	public boolean close() {
		this.graph.commit();
		return this.graph.isClosed();
	}
	
	public boolean remove( String id ) {
		Vertex vertex = this.graph.getVertex(id);
		if( vertex == null )
			return false;
		this.graph.removeVertex(vertex);
		return true;
	}

	public boolean remove( String parent, String[] children ) {
		boolean result = false;
		Vertex vertex = this.graph.getVertex(parent);
		if( vertex == null )
			return false;
		
		Iterator<Edge> iterator = vertex.getEdges(Direction.BOTH).iterator();
		while( iterator.hasNext()) {
			Edge child = iterator.next();
			for( String childId: children ) {
				if( child.getVertex( Direction.OUT).getId().equals( childId )) {
					this.graph.removeEdge(child);
					result = true;
				}
			}
		}
		return result;
	}

	public void sync(){
		try{
			this.graph.commit();
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
		finally{
			if( this.graph != null )
				this.graph.rollback();
		}
	}

}