package org.covaid.dashboard.service;

import org.condast.commons.data.plane.IField;
import org.covaid.core.field.IFieldListener;
import org.covaid.core.field.IFieldProvider;
import org.covaid.dashboard.core.Dispatcher;
import org.osgi.service.component.annotations.Component;

@Component(
		name = "org.covaid.dashboard.field.provider"
)
public class FieldProvider implements IFieldProvider{

	private Dispatcher dispatcher = Dispatcher.getInstance();
	
	public FieldProvider() {
		super();
	}

	@Override
	public IField getField() {
		return dispatcher.getField();
	}

	@Override
	public void addFieldListener(IFieldListener listener) {
		dispatcher.addFieldListener(listener);
	}

	@Override
	public void removeFieldListener(IFieldListener listener) {
		dispatcher.removeFieldListener(listener);
	}

	
	
}
