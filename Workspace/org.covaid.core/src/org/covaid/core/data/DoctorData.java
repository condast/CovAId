package org.covaid.core.data;

public class DoctorData {

	private long id;
	private boolean covaid;
	
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
