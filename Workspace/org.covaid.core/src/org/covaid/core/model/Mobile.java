package org.covaid.core.model;

import java.util.Calendar;
import java.util.Date;

import org.condast.commons.data.plane.IField;
import org.covaid.core.contagion.ContagionManager;
import org.covaid.core.contagion.IContagionManager;
import org.covaid.core.data.SharedData;
import org.covaid.core.data.StoredData;
import org.covaid.core.data.StoredNode;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistoryListener;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IMobile;
import org.covaid.core.def.IPoint;
import org.covaid.core.field.FieldChangeEvent;
import org.covaid.core.field.IFieldListener;

public class Mobile implements IMobile, IFieldListener {

		
	//An anonymous id used for communication with the serrver
	
	private String identifier;
	
	private double risk, safety;
	private transient StoredNode node;
	private IPoint location;

	private History history;
	
	private Date timestamp;
	
	private transient IField field;

	private transient IHistoryListener listener = (e)->{
		//history.alert(e.getDate(), e.getLocation(), e.getContagion());
	};

	
	public Mobile() {
		super();
		this.timestamp = Calendar.getInstance().getTime();
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
	public Mobile( String identifier, double safety, double risk, IPoint location) {
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

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public IField getField() {
		return field;
	}

	public void setField(IField field) {
		this.field = field;
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
	public IPoint getLocation() {
		return location;
	}

	@Override
	public void setLocation( IPoint location) {
		this.location = location;
	}

	@Override
	public void alert( Date date, ILocation location, IContagion contagion ) {
		this.history.alert( date, location, contagion);
	}

	@Override
	public History getHistory() {
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
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public boolean addContact( SharedData data ) {
		StoredData current = this.node.get(data.getIdentifier());
		if( current == null )
			this.node.addChild( new StoredData( data.getIdentifier()), data.getDistance());
		else {
			for( Contagion cd: data.getContagion()) {
				IContagion ccd = current.getContagiousness(cd.getIdentifier());
				if( ccd == null ) {
					current.addContagion( cd, Calendar.getInstance().getTime());
				}else {
					IContagionManager manager = new ContagionManager();
					if( manager.calculateContagiousness( cd, cd.getContagiousness(), data.getDistance() ) > ccd.getContagiousness() ) {
						current.addContagion(cd, Calendar.getInstance().getTime());
					}
				}
			}
		}
		return true;
	}

	@Override
	public void notifyFieldChange(FieldChangeEvent event) {
		// TODO Auto-generated method stub
		
	}	
}
