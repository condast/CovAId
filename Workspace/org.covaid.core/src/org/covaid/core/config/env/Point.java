package org.covaid.core.config.env;

public class Point implements Comparable<Point>{

	private String identifier;
	private int xpos, ypos;
	
	public Point( int xpos, int ypos) {
		this( createIdentifier(xpos, ypos), xpos, ypos);
	}
	
	public Point( String identifier, int xpos, int ypos) {
		this.xpos = xpos;
		this.ypos = ypos;
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getXpos() {
		return xpos;
	}

	protected void setXpos(int xpos) {
		this.xpos = xpos;
	}

	public int getYpos() {
		return ypos;
	}

	protected void setYpos(int ypos) {
		this.ypos = ypos;
	}

	protected void setPosition(int xpos, int ypos) {
		this.xpos = xpos;
		this.ypos = ypos;
	}
	
	public double getDistance( Point point ) {
		return Math.sqrt( Math.pow( xpos - point.getXpos(), 2) + Math.pow( ypos - point.getYpos(),2 )); 
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if( super.equals(obj))
			return true;
		if(!( obj instanceof Point))
			return false;
		Point test = (Point) obj;
		return (( test.getXpos() - xpos == 0 ) && ( test.getYpos() - ypos == 0 ));
	}

	@Override
	public String toString() {
		return createIdentifier(xpos, ypos);
	}

	@Override
	public int compareTo(Point o) {
		int compare = xpos - o.getXpos();
		if( compare != 0 )
			return compare;
		return ypos - o.getYpos();	
	}
	
	public static String createIdentifier( int xpos, int ypos ) {
		return "{" + xpos + "," + ypos + "}";
	}
}
