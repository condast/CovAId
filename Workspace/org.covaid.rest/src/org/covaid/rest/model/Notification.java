package org.covaid.rest.model;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.condast.commons.strings.StringStyler;
import org.condast.commons.strings.StringUtils;

public class Notification {

	public enum Events{
		IDLE,
		ILLNESS,
		APPOINTMENT,
		CONFIRMATION;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}	
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column( nullable=true)
	private String anonymous; //anonymous id

	@Column( nullable=true)
	private String notification; //could be an illness

	@ElementCollection(fetch=FetchType.EAGER)
	private Map<Long,String> events;
	
	@JoinColumn(name="DOCTOR_ID", nullable=false)
	@OneToOne
	private Doctor doctor;

	@Basic(optional = false)
	@Column( nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;
	
	@Basic(optional = false)
	@Column( nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;

	public Notification() {
		events = new HashMap<>();
		this.createDate = Calendar.getInstance().getTime();
		this.updateDate = Calendar.getInstance().getTime();
	}

	public Notification(String anonymous, Doctor doctor, String notification) {
		this();
		this.anonymous = anonymous;
		this.doctor = doctor;
		this.notification = notification;
	}

	public long getId() {
		return id;
	}

	public String getAnonymous() {
		return anonymous;
	}

	public String getNotification() {
		return notification;
	}

	public Doctor getDoctor() {
		return doctor;
	}

	public Events getEvent( Date date ) {
		String str = this.events.get(date.getTime());
		return StringUtils.isEmpty(str)?Events.IDLE: Events.valueOf( str );
	}

	public void putEvent( Date date, Events event ) {
		this.events.put(date.getTime(), event.name());
	}

	protected Map<Long, String> getEvents() {
		return events;
	}
}
