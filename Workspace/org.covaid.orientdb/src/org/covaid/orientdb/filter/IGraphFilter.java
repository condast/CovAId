package org.covaid.orientdb.filter;

import java.util.Collection;

import org.condast.commons.data.filter.IFilter;

import com.tinkerpop.blueprints.Vertex;

public interface IGraphFilter extends IFilter<Vertex>{

	Collection<Vertex> doFilter();

}