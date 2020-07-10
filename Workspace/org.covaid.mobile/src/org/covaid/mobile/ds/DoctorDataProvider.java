package org.covaid.mobile.ds;

import org.covaid.core.doctor.IDoctorDataListener;
import org.covaid.core.doctor.IDoctorDataProvider;
import org.covaid.mobile.core.Dispatcher;
import org.osgi.service.component.annotations.Component;

@Component(
		name = "org.covaid.mobile.doctor.data"
)
public class DoctorDataProvider implements IDoctorDataProvider{

	private Dispatcher dispatcher = Dispatcher.getInstance();
	
	public DoctorDataProvider() {
	}


	@Override
	public void addDoctorDataListener(IDoctorDataListener listener) {
		dispatcher.addDoctorDataListener(listener);
	}

	@Override
	public void removeDoctorDataListener(IDoctorDataListener listener) {
		dispatcher.removeDoctorDataListener(listener);
	}
}
