package org.covaid.core.def;

public interface IPoint extends Comparable<IPoint>{

	String getIdentifier();

	int getXpos();

	int getYpos();

	double getDistance(IPoint point);

	int compareTo(IPoint o);

	void setPosition(int xpos, int ypos);

}