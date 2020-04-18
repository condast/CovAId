package org.covaid.orientdb.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.condast.commons.data.filter.AbstractAttributeFilter;
import org.condast.commons.data.filter.AbstractFilter;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public abstract class AbstractGraphFilter extends AbstractFilter<Vertex> implements IGraphFilter {

	public String SQL_BASE = "SELECT FROM ";

	private OrientGraph graph;

	protected AbstractGraphFilter(OrientGraph graph, String name, String rule) {
		super(name, rule);
		this.graph = graph;
	}

	protected OrientGraph getGraph() {
		return graph;
	}

	@Override
	protected String[] getRules() {
		return AbstractAttributeFilter.Rules.items();
	}

	/**
	 * If true, the given rule is accepted by this filter
	 *
	 * @param rule String
	 * @return boolean
	 */
	@Override
	protected boolean acceptRule( String rule )
	{
		return org.condast.commons.data.filter.AbstractAttributeFilter.checkRule( rule );
	}

	protected Iterator<Vertex> getIterator( OrientGraph graph ){
		return graph.getVertices().iterator();
	}
	
	@Override
	public Collection<Vertex> doFilter() {
		Iterator<Vertex> iterator = getIterator( this.graph);
		Collection<Vertex> results = new ArrayList<>();
		while( iterator.hasNext() ) {
			Vertex vertex = iterator.next();
			if( acceptEnabled(vertex))
				results.add(vertex);
		}
		return results;
	}
	
}
