package org.covaid.core.def;

import java.util.Date;

import org.condast.commons.authentication.user.ILoginUser;

public interface IUserData {

	long getId();

	long getUserId();

	ILoginUser getLoginUser();

	String getUserName();

	/**
	 * A feature is a location, augmented with additional characteristics, such as a zoom
	 * @return
	 */
	
	String getDescription();

	void setDescription(String description);

	Date getCreateDate();

	int getSelectedLocationIndex();
	
	void setSelectedLocation(int selection);

	/**
	 * Options are stored as a json string of a corresponding object
	 * @return
	 */
	String getOptions();

	void setOptions(String options);
}