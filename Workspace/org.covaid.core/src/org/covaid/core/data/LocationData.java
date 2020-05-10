package org.covaid.core.data;

import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import org.condast.commons.data.latlng.ILocation;
import org.condast.commons.data.latlng.LatLng;
import org.covaid.core.def.IContagion;
import org.covaid.core.model.date.DateContagion;

public class LocationData implements ILocation, org.condast.commons.IUpdateable {

	private long id;

	private String name;
	
	private double latitude;
	private double longitude;
	
	private Collection<DateContagion> contagion;
		
	private Date createDate;
	
	private Date updateDate;

	public LocationData() {
		super();
	}

	public LocationData( LatLng latlng ) {
		this();
		this.name = latlng.getId();
		this.latitude = latlng.getLatitude();
		this.longitude = latlng.getLongitude();
		contagion = new TreeSet<>();
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public LatLng getLocation() {
		return new LatLng( this.name, this.latitude, this.longitude );
	}

	public boolean addContagion( DateContagion contagion) {
		return this.contagion.add(contagion);
	}

	public boolean removeContagion( IContagion contagion) {
		return this.contagion.remove(contagion);
	}

	@Override
	public Date getCreateDate() {
		return createDate;
	}

	@Override
	public void setCreateDate(Date create) {
		this.createDate = create;
	}

	@Override
	public Date getUpdateDate() {
		return updateDate;
	}

	@Override
	public void setUpdateDate(Date update) {
		this.updateDate = update; 
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(ILocation arg0) {
		LatLng latlng = getLocation();
		return latlng.compareTo(arg0.getLocation());
	}	
}
