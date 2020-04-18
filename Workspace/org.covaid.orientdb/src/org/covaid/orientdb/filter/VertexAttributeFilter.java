package org.covaid.orientdb.filter;

import org.condast.commons.data.filter.AbstractAttributeFilter;
import org.condast.commons.data.filter.FilterException;
import org.condast.commons.data.filter.WildcardFilter;
import org.condast.commons.strings.StringUtils;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public class VertexAttributeFilter extends AbstractGraphFilter {

	public String SQL_BASE = "SELECT FROM ";

	private String refKey;
	private String refVal;

	protected VertexAttributeFilter(OrientGraph graph, String name, AbstractAttributeFilter.Rules rule, String key) {
		this( graph, name, rule, key, null );
	}
	
	protected VertexAttributeFilter(OrientGraph graph, String name, AbstractAttributeFilter.Rules rule, String key, String refVal) {
		super(graph, name, rule.toString());
		this.refKey = key;
		this.refVal = refVal;
	}

	@Override
	protected boolean acceptEnabled(Vertex vertex) throws FilterException {
		if( vertex == null )
			return false;
		boolean contains = false;
		String val = vertex.getProperty(this.refKey);
		contains = !StringUtils.isEmpty(val);
		switch( AbstractAttributeFilter.Rules.valueOf( super.getRule() )){
		case CONTAINS:
			return contains;
		case CONTAINS_NOT:
			return !contains;
		case EQUALS:
			if( contains && val.equals( this.refVal ))
				return true;
			break;
		case EQUALS_NOT:
			if( !val.equals( this.refVal ))
				return true;
			break;

		default:
			WildcardFilter filter = new WildcardFilter( val);
			return filter.accept( this.refVal );
		}
		return false;
	}
}
