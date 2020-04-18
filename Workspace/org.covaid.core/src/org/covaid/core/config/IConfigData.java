package org.covaid.core.config;

import org.condast.commons.strings.StringStyler;

public interface IConfigData{

	String S_REGISTER_ID = "org.satr.arnac.vessel.register";
	String S_REGISTER = "Register Vessel";
	String S_CONFIG_ID = "org.satr.arnac.vessel.config";
	String S_CONFIG = "Configure vessel";
	String S_FIELD_ID = "org.satr.arnac.field";
	String S_FIELD = "Get Field";
	String S_VESSEL_ID = "org.satr.arnac.vessel";
	String S_VESSEL = "vessel";
	String S_WEB_CLIENT_ID = "org.satr.arnac.web.client";
	String S_WEB_CLIENT = "Web Client";
	String S_GPS_ID = "org.satr.arnac.gps";
	String S_GPS = "GPS Unit";
	String S_NMEA_ID = "org.satr.arnac.nmea";
	String S_NMEA = "NMEA Transponder";

	public static final String S_COMPASS_ID ="org.satr.arnac.gps";
	public static final String S_COMPASS = "Compass Sensor";

	public static final String S_SERVO_ID ="org.satr.arnac.servo";
	public static final String S_SERVO = "Servo Unit";

	public enum Attributes{
		ID,
		NAME,
		DESCRIPTION,
		TYPE,
		STATUS,
		UNIQUE;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString() );
		}
	}

	public enum Status{
		UNKNOWN,
		INIT,
		TESTING,
		FAILED,
		PASSED;
	}

	public enum ConfigIDs{
		REGISTER,
		CONFIG,
		FIELD,
		COMPASS,
		GPS,
		NMEA,
		SERVO,
		VESSEL,
		WEB_CLIENT;
		
		public String getId() {
			String id = null;
			switch( this ) {
			case REGISTER:
				id = S_REGISTER_ID;
				break;
			case CONFIG:
				id = S_CONFIG_ID;
				break;
			case FIELD:
				id = S_FIELD_ID;
				break;
			case COMPASS:
				id = S_COMPASS_ID;
				break;
			case GPS:
				id = S_GPS_ID;
				break;
			case NMEA:
				id = S_NMEA_ID;
				break;
			case SERVO:
				id = S_SERVO_ID;
				break;
			case VESSEL:
				id = S_VESSEL_ID;
				break;
			case WEB_CLIENT:
				id = S_WEB_CLIENT_ID;
				break;
			default:
				break;
			}
			return id;
		}

		public String getName() {
			String id = null;
			switch( this ) {
			case REGISTER:
				id = S_REGISTER;
				break;
			case CONFIG:
				id = S_CONFIG;
				break;
			case FIELD:
				id = S_FIELD;
				break;
			case COMPASS:
				id = S_COMPASS;
				break;
			case GPS:
				id = S_GPS;
				break;
			case NMEA:
				id = S_NMEA;
				break;
			case SERVO:
				id = S_SERVO;
				break;
			case VESSEL:
				id = S_VESSEL;
				break;
			case WEB_CLIENT:
				id = S_WEB_CLIENT;
				break;
			default:
				break;
			}
			return id;
		}
	}

	IConfigData getParent();

	String getId();

	String getType();

	String getName();

	boolean isUnique();

	String getDescription();

	Status getStatus();

	boolean setStatus(Status status);

	void addAttribute(String key, String value);

	void removeAttribute(String key);

	IConfigData addChild( String id );

	boolean addChild( IConfigData data);

	boolean removeChild( IConfigData data);

	IConfigData getChild(String configId);

	boolean hasChild(String configId);

	boolean hasChildren();

	IConfigData[] getChildren();

}