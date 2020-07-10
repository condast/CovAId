package org.covaid.core.doctor;

import java.util.EventObject;

import org.covaid.core.data.DoctorData;

public class DoctorDataEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	private IDoctorDataListener.DocterDataEvents type;
	
	private DoctorData data;
	
	public DoctorDataEvent(Object source, IDoctorDataListener.DocterDataEvents type, DoctorData data ) {
		super(source);
		this.type = type;
		this.data = data;
	}

	public DoctorData getData() {
		return data;
	}

	public IDoctorDataListener.DocterDataEvents getType() {
		return type;
	}
}
