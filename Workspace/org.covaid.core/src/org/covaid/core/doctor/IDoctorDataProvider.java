package org.covaid.core.doctor;

public interface IDoctorDataProvider {

	public void addDoctorDataListener( IDoctorDataListener listener );

	public void removeDoctorDataListener( IDoctorDataListener listener );

}
