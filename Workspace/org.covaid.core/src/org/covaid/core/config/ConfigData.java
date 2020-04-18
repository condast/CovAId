package org.covaid.core.config;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.condast.commons.Utils;
import org.condast.commons.strings.StringUtils;

public class ConfigData implements IConfigData {

	@SuppressWarnings("unused")
	private String id;

	private Map<String, String> attributes;

	private transient IConfigData parent;
	private transient Map<String, ConfigData> childrenMap;
	

	public ConfigData() {
		this(null, null, null, null, null, true );
	}

	public ConfigData( String id ) {
		this(null, id );
	}

	protected ConfigData( IConfigData parent, String id ) {
		this(parent, id, null, null, null, true );
	}

	public ConfigData( ConfigIDs configId, String type, String description ) {
		this(null, configId.getId(), type, configId.getName(), description, true );
	}

	public ConfigData( ConfigIDs configId, String type, String description, boolean unique ) {
		this(null, configId.getId(), type, configId.getName(), description, unique );
	}

	public ConfigData(String id, String name, String type, String description) {
		this(null, id, name, type, description );
	}

	public ConfigData(IConfigData parent, String id, String name, String type, String description) {
		this( parent, id, name, type, description, false );
	}

	public ConfigData(IConfigData parent, String id, String name, String type, String description, boolean unique) {
		super();
		this.id = id;
		this.parent = parent;
		this.attributes = new HashMap<>();
		addAttribute(IConfigData.Attributes.ID.name(), id);
		addAttribute(IConfigData.Attributes.NAME.name(), name);
		addAttribute(IConfigData.Attributes.DESCRIPTION.name(), description);
		addAttribute(IConfigData.Attributes.TYPE.name(), type);
		addAttribute(IConfigData.Attributes.UNIQUE.name(), String.valueOf(unique));
		addAttribute(IConfigData.Attributes.STATUS.name(), Status.INIT.name());
		this.childrenMap = new TreeMap<>();
	}
	
	@Override
	public IConfigData getParent() {
		return parent;
	}

	private void setParent(IConfigData parent) {
		this.parent = parent;
	}

	@Override
	public String getId() {
		return this.attributes.get(IConfigData.Attributes.ID.name());
	}

	@Override
	public String getType() {
		return this.attributes.get(IConfigData.Attributes.TYPE.name());
	}

	@Override
	public String getName() {
		return this.attributes.get(IConfigData.Attributes.NAME.name());
	}

	@Override
	public boolean isUnique() {
		String str = this.attributes.get(IConfigData.Attributes.UNIQUE.name());
		return StringUtils.isEmpty(str)?false: Boolean.parseBoolean(str);
	}

	@Override
	public String getDescription() {
		return this.attributes.get(IConfigData.Attributes.DESCRIPTION.name());
	}

	@Override
	public Status getStatus() {
		String str = this.attributes.get(IConfigData.Attributes.STATUS.name());
		return StringUtils.isEmpty(str)?Status.INIT: Status.valueOf(str);
	}

	@Override
	public boolean setStatus(Status status) {
		if( status == null )
			return false;
		addAttribute(IConfigData.Attributes.STATUS.name(), status.name());
		return true;
	}

	@Override
	public void addAttribute( String key, String value ) {
		this.attributes.put(key, value);
	}
	
	@Override
	public void removeAttribute( String key) {
		this.attributes.remove(key);
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public synchronized IConfigData addChild( String id ) {
		ConfigData child = new ConfigData( this, id );
		this.childrenMap.put(child.getId(), child);
		return child;
	}

	@Override
	public synchronized boolean addChild( IConfigData data ) {
		if( data.isUnique() && hasChild(data.getId()))
			removeChild(data.getId());
		ConfigData child = (ConfigData) data;
		child.setParent(this);
		this.childrenMap.put(data.getId(), (ConfigData) data);
		return true;
	}

	@Override
	public synchronized boolean removeChild( IConfigData data ) {
		ConfigData child = (ConfigData) data;
		child.setParent(null);
		this.childrenMap.remove(data.getId());
		return true;
	}

	public synchronized boolean removeChild( String configId ) {
		IConfigData child = this.childrenMap.remove(configId);
		return ( child != null );
	}

	@Override
	public boolean hasChild( String configId ) {
		IConfigData child = this.childrenMap.get(configId);
		return ( child != null );
	}
	
	@Override
	public boolean hasChildren() {
		return !Utils.assertNull(childrenMap);
	}
	
	@Override
	public IConfigData getChild(String configId) {
		return this.childrenMap.get(configId);
	}

	public ConfigData[] getChildren() {
		return this.childrenMap.values().toArray( new ConfigData[ this.childrenMap.size()] );
	}

	@Override
	public int hashCode() {
		return ( getId() == null )? super.hashCode(): getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!( obj instanceof ConfigData ))
			return false;
		IConfigData arg0 = (IConfigData) obj; 
		return getId().equals( arg0.getId());
	}
	
	
}
