package org.covaid.mobile.model;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.condast.commons.data.plane.Field;
import org.condast.commons.data.plane.IField;
import org.covaid.core.contagion.ContagionManager;
import org.covaid.core.contagion.IContagionManager;
import org.covaid.core.data.SharedData;
import org.covaid.core.data.StoredData;
import org.covaid.core.data.StoredNode;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistory;
import org.covaid.core.def.IHistoryListener;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IMobile;
import org.covaid.core.def.IPoint;
import org.covaid.core.field.FieldChangeEvent;
import org.covaid.core.field.IFieldListener;
import org.covaid.core.model.Contagion;

@Entity(name="MOBILE")
public class Mobile implements IMobile, IFieldListener {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	//An anonymous id used for communication with the serrver
	@Column(nullable=false)
	private String identifier;
	
	private double risk, safety;
	
	@OneToOne
	private Point location;

	private StoredNode node;
	
	private History history;
	
	//LatLng is transformed internally to (x,y) coordinates in the simulation
	private transient IField field; 

	private IHistoryListener listener = (e)->{
		//history.alert(e.getDate(), e.getLocation(), e.getContagion());
	};

	
	public Mobile() {
		super();
		this.identifier = "null";
	}

	/**
	 * The Safety is the extent in which the bubble should protect you, e.g. for vulnerable people
	 * The Risk is the amount of risk you are willing to take
	 * @param id
	 * @param safety (0-100)
	 * @param risk (0-100)
	 * @param location
	 */
	public Mobile( String identifier, IField field) {
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
	public Mobile( String identifier, Point location) {
		this( identifier, 50, 50, location );
	}
	
	/**
	 * The Safety is the extent in which the bubble should protect you, e.g. for vulnerable people
	 * The Risk is the amount of risk you are willing to take
	 * @param id
	 * @param safety (0-100)
	 * @param risk (0-100)
	 * @param location
	 */
	public Mobile( String identifier, double safety, double risk, Point location) {
		super();
		this.identifier = identifier;
		this.safety = safety;
		this.risk = risk;
		this.node = new StoredNode(identifier );
		this.location = location;
		this.history = new History();
		this.history.addListener(listener);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public IField getField() {
		return field;
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
	public double getSafety() {
		return safety;
	}

	@Override
	public void setSafety(double safety) {
		this.safety = safety;
	}

	@Override
	public Point getLocation() {
		return location;
	}

	@Override
	public void setLocation( IPoint location) {
		this.location = (Point) location;
	}

	@Override
	public void alert( Date date, ILocation location, IContagion contagion ) {
		this.history.alert( date, location, contagion);
	}

	@Override
	public IHistory getHistory() {
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

	public StoredNode getNode() {
		return node;
	}
	
	@Override
	public boolean addContact( SharedData data ) {
		StoredData current = this.node.get(data.getIdentifier());
		if( current == null )
			this.node.addChild( new StoredData( data.getIdentifier()), data.getDistance());
		else {
			for( IContagion cd: data.getContagion()) {
				IContagion ccd = current.getContagiousness(cd.getIdentifier());
				if( ccd == null ) {
					current.addContagion( (Contagion) cd, Calendar.getInstance().getTime());
				}else {
					IContagionManager manager = new ContagionManager();
					if( manager.calculateContagiousness( cd, cd.getContagiousness(), data.getDistance() ) > ccd.getContagiousness() ) {
						current.addContagion((Contagion) cd, Calendar.getInstance().getTime());
					}
				}
			}
		}
		return true;
	}

	@Override
	public void notifyFieldChange(FieldChangeEvent event) {
		double scaleX = (double)location.getXpos()/this.field.getLength();
		double scaleY = (double)location.getYpos()/this.field.getWidth();
		IField field = event.getField();
		this.location.setXpos((int) (scaleX * field.getLength()));
		this.location.setYpos((int) (scaleY * field.getWidth()));
		field = new Field( field.getCoordinates(), field.getLength(), field.getWidth());
	}	
}
