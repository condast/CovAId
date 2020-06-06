package org.covaid.ui.mobile;

import java.util.EventObject;

public class UpdateEvent extends EventObject{
	private static final long serialVersionUID = 1L;

	private String identifier;
	
	public UpdateEvent( Object source, String identifier ) {
		super( source );
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}
}
