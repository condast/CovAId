package org.covaid.core.field;

import java.util.EventObject;

import org.condast.commons.data.plane.IField;

public class FieldChangeEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	public IField field;
	
	public FieldChangeEvent(Object source, IField field) {
		super(source);
		this.field = field;
	}

	public IField getField() {
		return field;
	}
}
