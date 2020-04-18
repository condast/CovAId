package org.covaid.orientdb.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.condast.commons.data.filter.WildcardFilter;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.strings.StringUtils;
import org.covaid.core.data.ContagionData;
import org.covaid.core.data.StoredData;
import org.covaid.core.data.StoredNode;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public class OrientDatabase {

	public enum Types{
		CONTAGION,
		STORED;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}

		public String toClassString() {
			return "class:" + this.toString();
		}

	}
	private static DatabaseService service = DatabaseService.getInstance();

	private Collection<IDatabaseListener> listeners;
	
	private String identifier;
	
	public OrientDatabase( String identifier ) {
		this.identifier = identifier;
		listeners = new ArrayList<>();
	}

	public String getIdentifier() {
		return identifier;
	}

	public void open() {
		service.open();
	}

	public boolean isOpen() {
		return service.isOpen();
	}

	public void close() {
		service.close();
	}

	public void addListener(IDatabaseListener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(IDatabaseListener listener) {
		this.listeners.remove(listener);
	}
	
	protected void notifyListeners( DatabaseEvent<StoredNode> event ) {
		for( IDatabaseListener listener: listeners )
			listener.notifyChange(event);
	}

	public void prepare() {
		OrientGraph graph = service.getGraph();		
		for( Types tp: Types.values() ) {
			graph.createVertexType( tp.toString() ); 
		}
	}
	
	public boolean add(StoredNode node) {
		Vertex root = fromTree(node);
		return ( root != null );
	}

	public boolean contains( String id) {
		service.open();
		Vertex root = null;
		try {
			OrientGraph graph = service.getGraph();
			root = graph.getVertex( id );
			return (root != null );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			service.close();
		}
		return false;
	}

	public StoredNode get( String id ) {
		service.open();
		Vertex root = null;
		try {
			OrientGraph graph = service.getGraph();
			root = graph.getVertex( id );
			return ( root == null )?null: toNode( root );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			service.close();
		}
		return null;
	}

	public Collection<StoredNode> search( String attribute, String wildcard) {
		Collection<StoredNode> results = new ArrayList<>();
		service.open();
		try {
			OrientGraph graph = service.getGraph();
			Iterator<Vertex> iterator = graph.getVertices().iterator();
			while( iterator.hasNext() ) {
				Vertex vertex = iterator.next();
				String result = vertex.getProperty(attribute);
				if( StringUtils.isEmpty(result))
					continue;
				WildcardFilter filter = new WildcardFilter( wildcard );
				if( filter.accept(result))
					results.add( toNode( vertex ));
			}
			return results;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			service.close();
		}
		return null;
	}

	public boolean remove(StoredNode node) {
		try {
			OrientGraph graph = service.getGraph();
			Vertex vertex = graph.getVertex( node.getRoot().getId() );
			if( vertex == null )
				return false;
			
			graph.removeVertex(vertex);
			return true;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			service.close();
		}
		return false;
	}

	public boolean update(StoredNode node) {
		Vertex root = fromTree(node);
		return ( root != null );
	}

	public void deactivate() {
		service.close();
	}	

	protected static StoredNode toNode( Vertex root) {
		StoredNode node = null;
		service.open();
		try {
			node = new StoredNode( fill( root ));
			Iterator<Edge> iterator = root.getEdges(Direction.OUT, Types.STORED.name()).iterator();
			while( iterator.hasNext()) {
				Edge edge = iterator.next();
				StoredData data = fill( edge.getVertex(Direction.OUT));
				node.addChild(data, Float.parseFloat( edge.getLabel()));
			}
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			service.close();
		}
		return node;
	}

	public static Vertex fromTree( StoredNode node) {
		service.open();
		Vertex root = null;
		try {
			OrientGraph graph = service.getGraph();
			root = graph.getVertex(node.getRoot().getId());
			if( root == null ) {
				root = graph.addVertex(Types.STORED.name());
				node.getRoot().setId(root.getId().toString());
				fill( root, node.getRoot());
			}
			for( StoredData child: node.getConnections()) {
				Vertex vc = graph.getVertex(child.getId());
				if( vc == null ) {
					vc = graph.addVertex(Types.STORED.name());
					child.setId( vc.getId().toString());
					fill( vc, child);
				}
				graph.addEdge(Types.STORED.name(), root, vc, String.valueOf( node.getDistance( child )));
			}
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			service.close();
		}
		return root;
	}
	
	protected static void fill( Vertex vertex, StoredData data ) {
		vertex.setProperty( StoredData.Attributes.IDENTIFIER.name(), data.getIdentifier());
		Iterator<Map.Entry<ContagionData, Date>> iterator = data.getContagiousness().entrySet().iterator();
		OrientGraph graph = service.getGraph();
		while( iterator.hasNext()) {
			Map.Entry<ContagionData, Date> entry = iterator.next();
			Vertex vc = graph.addVertex( Types.CONTAGION.toString() );
			vc.setProperty( ContagionData.Attributes.IDENTIFIER.name(), entry.getKey().getIdentifier());
			vc.setProperty( ContagionData.Attributes.CONTAGIOUSNESS.name(), String.valueOf( entry.getKey().getContagiousness()));
			graph.addEdge(Types.CONTAGION.toString(), vertex, vc, String.valueOf( entry.getValue().getTime()));
		}
	}

	protected static StoredData fill( Vertex vertex ) {
		String id = vertex.getId().toString();
		String identifier = vertex.getProperty( StoredData.Attributes.IDENTIFIER.name());
		Iterator<Edge> iterator = vertex.getEdges(Direction.OUT, Types.CONTAGION.toString()).iterator();
		StoredData result = new StoredData( identifier );
		while( iterator.hasNext() ) {
			Edge edge = iterator.next();
			Vertex vc = edge.getVertex(Direction.OUT);
			float contagiousness = vc.getProperty(ContagionData.Attributes.CONTAGIOUSNESS.name());
			ContagionData data = new ContagionData((String)vc.getProperty(ContagionData.Attributes.IDENTIFIER.name()), contagiousness);
			result.addContagion( data, new Date( Long.parseLong( edge.getLabel())));
		}
		result.setId(id);
		return result;
	}

}
