package org.covaid.core.def;

public interface IPoint extends Comparable<IPoint>, Cloneable{

	String getIdentifier();

	int getXpos();

	int getYpos();

	double getDistance(IPoint point);

	IPoint clone();

	int compareTo(IPoint o);
}