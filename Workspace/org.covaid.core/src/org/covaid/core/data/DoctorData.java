package org.covaid.core.data;

import org.covaid.core.def.IMobile;

public class DoctorData {

	private long id;
	private boolean covaid;

	public DoctorData( IMobile<?> mobile, boolean covaid) {
		this( Long.parseLong(mobile.getIdentifier()), covaid );
	}
	
	public DoctorData(long id, boolean covaid) {
		super();
		this.id = id;
		this.covaid = covaid;
	}

	public long getId() {
		return id;
	}

	public boolean hasCovaid() {
		return covaid;
	}
}
