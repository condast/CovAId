package org.covaid.core.environment.field;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.condast.commons.data.plane.IField;
import org.covaid.core.def.IPerson;
import org.covaid.core.environment.IDomain;
import org.covaid.core.hub.IHub;

public interface IFieldDomain extends IDomain<Date>{

	Collection<IPerson<Date>> getPersons();

	Map<String, IHub<Date>> getHubs();

	IField getField();

	void setField(IField field);

}