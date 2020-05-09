package org.covaid.core.environment.field;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.condast.commons.data.plane.IField;
import org.covaid.core.def.IHub;
import org.covaid.core.def.IPerson;
import org.covaid.core.environment.IDomain;

public interface IFieldDomain extends IDomain<Date>{

	Collection<IPerson> getPersons();

	Map<String, IHub> getHubs();

	IField getField();

	void setField(IField field);

}