package org.covaid.core.data;

import org.condast.commons.strings.StringStyler;
import org.condast.commons.strings.StringUtils;

public class ContagionData implements Comparable<ContagionData>{

	public enum SupportedContagion{
		OTHER,
		COVID_19,
		SEASONAL,
		EXPLOSION;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}
		
		public static boolean isSupported( String str ) {
			if( StringUtils.isEmpty(str))
				return false;
			for( SupportedContagion sc: values() ) {
				if( sc.name().equals(str))
					return true;
			}
			return false;
		}
	}

	public enum Attributes{
		IDENTIFIER,
		CONTAGIOUSNESS,
		TIMESTAMP;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}
	}


	private String identifier;
	
	private float contagiousness;

	public ContagionData( SupportedContagion identifier, float contagiousness) {
		this( identifier.toString(), contagiousness );
	}
	
	public ContagionData(String identifier, float contagiousness) {
		super();
		this.identifier = identifier;
		this.contagiousness = contagiousness;
	}

	public String getIdentifier() {
		return identifier;
	}

	public float getContagiousness() {
		return contagiousness;
	}

	@Override
	public int compareTo(ContagionData o) {
		return this.identifier.compareTo(o.getIdentifier());
	}
}
