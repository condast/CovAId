package org.covaid.core.def;

import java.util.Date;

import org.condast.commons.data.plane.IField;
import org.covaid.core.environment.IEnvironment;

public interface IFieldEnvironment extends IEnvironment<Date>{

	double LONGITUDE = 4.00f;
	double LATITUDE  = 52.000f;

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