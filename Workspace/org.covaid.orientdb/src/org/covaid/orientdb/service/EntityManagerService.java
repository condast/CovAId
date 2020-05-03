package org.covaid.orientdb.service;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.object.jpa.OJPAEntityManagerFactory;

import javax.persistence.EntityManagerFactory;

import org.osgi.service.component.annotations.Component;

public class EntityManagerService extends OJPAEntityManagerFactory implements EntityManagerFactory{

	private OObjectDatabaseTx db;


	public EntityManagerService() {
		super(null);
		// TODO Auto-generated constructor stub
	}
}
