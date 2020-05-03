package org.covaid.mobile.ds;

import org.covaid.core.field.IFieldProvider;
import org.covaid.mobile.core.Dispatcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component( name="org.covaid.mobile.service.field",
			immediate=true)
public class FieldService {

	private Dispatcher dispatcher = Dispatcher.getInstance();
	
	@Reference( cardinality = ReferenceCardinality.MANDATORY,
			policy=ReferencePolicy.DYNAMIC)
	public void bind( IFieldProvider provider){
		this.dispatcher.setFieldProvider( provider );
	}

	public void unbind( IFieldProvider provider){
		this.dispatcher.removeFieldProvider(provider);
	}
}
