package org.covaid.core.field;

import org.condast.commons.data.plane.IField;

public interface IFieldProvider {

	public IField getField();
	
	public void addFieldListener( IFieldListener listener);

	public void removeFieldListener( IFieldListener listener);

}
