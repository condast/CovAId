package org.covaid.core.doctor;

public interface IDoctorDataListener {

	public enum DocterDataEvents{
		ADD,
		REMOVE;
	}
	
	public void notifyDoctorDoctorChanged( DoctorDataEvent event );
}
