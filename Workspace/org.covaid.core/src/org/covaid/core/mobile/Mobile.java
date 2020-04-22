package org.covaid.core.mobile;

import java.util.Calendar;

import org.condast.commons.data.latlng.LatLng;
import org.covaid.core.config.env.Contagion;
import org.covaid.core.contagion.ContagionManager;
import org.covaid.core.contagion.IContagionManager;
import org.covaid.core.data.SharedData;
import org.covaid.core.data.StoredData;
import org.covaid.core.data.StoredNode;

public class Mobile {
	
	public enum Risks{
		DONT_CARE(0),
		ADVENTUROUS(80),
		AVERAGE(50),
		PLAY_SAFE(20),
		NO_RISC(100);
		
		private int index;
		
		private Risks(int index ) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}	
	}
	
	private float risk, safety;
	private StoredNode node;
	private LatLng location;
	
	/**
	 * The Safety is the extent in which the bubble should protect you, e.g. for vulnerable people
	 * The Risk is the amount of risk you are willing to take
	 * @param id
	 * @param safety
	 * @param risk
	 * @param location
	 */
	public Mobile( String identifier, float safety, float risk, LatLng location) {
		super();
		this.safety = safety;
		this.risk = risk;
		this.node = new StoredNode(identifier );
		this.location = location;
	}

	public float getRisk() {
		return risk;
	}

	public void setRisk(float risk) {
		this.risk = risk;
	}

	public float getSafety() {
		return safety;
	}

	public void setSafety(float safety) {
		this.safety = safety;
	}

	public LatLng getLocation() {
		return location;
	}

	public void setLocation(LatLng location) {
		this.location = location;
	}

	public StoredNode getNode() {
		return node;
	}
	
	public boolean addContact( SharedData data ) {
		StoredData current = this.node.get(data.getIdentifier());
		if( current == null )
			this.node.addChild( new StoredData( data.getIdentifier()), data.getDistance());
		else {
			for( Contagion cd: data.getContagion()) {
				Contagion ccd = current.getContagiousness(cd.getIdentifier());
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
}
