package org.covaid.core.def;

import java.util.Date;

import org.condast.commons.data.plane.IField;

public interface IFieldEnvironment extends IEnvironment<Date>{

	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	void init(int activity, int population);

	String getContagion();

	void setContagion(String contagion);

	IField getField();

	void setField(IField field);

	void zoomIn();

	void zoomOut();

	int getDays();
	
	String getDayString(boolean trunc);
}