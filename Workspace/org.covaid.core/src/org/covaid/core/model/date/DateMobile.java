package org.covaid.core.model.date;

import java.util.Calendar;
import java.util.Date;

import org.condast.commons.data.plane.IField;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistoryListener;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IMobile;
import org.covaid.core.def.IPoint;
import org.covaid.core.field.FieldChangeEvent;
import org.covaid.core.field.IFieldListener;
import org.covaid.core.model.Point;

public class DateMobile implements IMobile<Date>, IFieldListener {

		
	//An anonymous id used for communication with the serrver
	
	private String identifier;
	
	private double health, risk;
	private String email;//doctor email
	private IPoint location;

	private DateHistory history;
	
	private Date timestamp;
	
	private transient IField field;

	private transient IHistoryListener<Date> listener = (e)->{
		//history.alert(e.getDate(), e.getLocation(), e.getContagion());
	};

	public DateMobile() {
		super();
		this.timestamp = Calendar.getInstance().getTime();
	}

	/**
	 * The Safety is the extent in which the bubble should protect you, e.g. for vulnerable people
	 * The Risk is the amount of risk you are willing to take
	 * @param id
	 * @param risk (0-100)
	 * @param risk (0-100)
	 * @param location
	 */
	public DateMobile( String identifier, IField field) {
		this( identifier, 50, 50, new Point((int)field.getLength()/2, (int)field.getWidth()/2 ));
		this.field = field;
	}

	/**
	 * The Safety is the extent in which the bubble should protect you, e.g. for vulnerable people
	 * The Risk is the amount of risk you are willing to take
	 * @param id
	 * @param safety (0-100)
	 * @param risk (0-100)
	 * @param location
	 */
	public DateMobile( String identifier, double safety, double health, IPoint location) {
		super();
		this.identifier = identifier;
		this.risk = safety;
		this.health = health;
		this.location = location;
		this.history = new DateHistory();
		this.history.addListener(listener);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	public IField getField() {
		return field;
	}

	public void setField(IField field) {
		this.field = field;
	}

	@Override
	public double getHealth() {
		return health;
	}

	@Override
	public void setHealth(double health) {
		this.health = health;
		if( this.health < this.risk )
			this.risk=  this.health;
	}

	@Override
	public double getRisk() {
		return risk;
	}

	@Override
	public void setRisk(double risk) {
		this.risk = risk;
	}

	@Override
	public IPoint getLocation() {
		return location;
	}

	@Override
	public void setLocation( IPoint location) {
		this.location = location;
	}

	@Override
	public void alert( Date date, ILocation<Date> location, IContagion<Date> contagion ) {
		this.history.alert( date, location, contagion, 100);
	}

	@Override
	public DateHistory getHistory() {
		return this.history;
	}

	/**
	 * Returns true if the risk assessment shows that the owner is healthy
	 * @return
	 */
	@Override
	public boolean isHealthy() {
		return this.history.isEmpty();
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public void notifyFieldChange(FieldChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Set the health and safety based on a questionnaire
	 * @param cough
	 * @param fever
	 * @param lackoftaste
	 * @param soreThroat
	 * @param nasalCold
	 * @param temperature
	 * @return
	 */
	@Override
	public double getHealthAdvice( boolean cough, boolean fever, boolean lackoftaste, boolean soreThroat, boolean nasalCold,  double temperature) {
		this.health = 100;
		this.risk = 50;
		if( cough || fever || lackoftaste ) { 
			health = 5;
			risk = 0;
		}else if( soreThroat || nasalCold) {
			health = 40;
			risk = 20;
		}
		if(( temperature < 36.5 )|| ( temperature > 39.5)) {
			health = 0;
			risk = 0;
		}
		return health;
	}
}
