package org.covaid.core.data;

import java.util.Calendar;
import java.util.Date;

import org.condast.commons.strings.StringStyler;
import org.covaid.core.def.IMobile;

public class DoctorData implements Comparable<DoctorData>{

	public enum States{
		APPOINTMENT,
		NEGATIVE,
		POSITIVE;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}
		
		public static String[] getItems() {
			String[] results = new String[values().length];
			for( int i=0; i<values().length;i++) {
				results[i] = values()[i].toString();
			}
			return results;
		}
		
		public static States getState( int ordinal ) {
			return values()[ordinal];
		}
	}
	
	private long id;
	private Date date;
	private States state;

	public DoctorData( IMobile<?> mobile, States state) {
		this( mobile.getId(), state );
	}

	public DoctorData( IMobile<?> mobile) {
		this( mobile.getId(), States.APPOINTMENT );
	}
	
	public DoctorData(long id, States state) {
		super();
		this.id = id;
		this.state = state;
		this.date = Calendar.getInstance().getTime();
	}

	public long getId() {
		return id;
	}

	public States getState() {
		return state;
	}

	public void setState(States state) {
		this.state = state;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public int compareTo(DoctorData o) {
		return (this.id==o.getId())?0:this.id<o.getId()?-1:1;
	}
}
