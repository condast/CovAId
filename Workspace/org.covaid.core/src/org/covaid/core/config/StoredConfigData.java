package org.covaid.core.config;

import java.util.Collection;

import org.condast.commons.preferences.AbstractPreferenceStore;
import org.condast.commons.strings.StringUtils;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class StoredConfigData extends AbstractPreferenceStore<IConfigData> implements IConfigData{
		
	private IConfigData parent;

	/**
	 * Root node
	 * @param id
	 */
	protected StoredConfigData( String id) {
		this( id, null );
	}
	
	protected StoredConfigData( String id, IConfigData parent) {
		super( id);
		this.parent = parent;
	}

	protected StoredConfigData(Preferences preferences) {
		super(preferences);
	}
	
	@Override
	public IConfigData getParent() {
		return parent;
	}

	@Override
	public String getId() {
		return super.getName();
	}

	@Override
	public String getType() {
		return super.getSettings(Attributes.TYPE);
	}

	@Override
	public boolean isUnique() {
		String result = super.getSettings(Attributes.UNIQUE);
		return StringUtils.isEmpty(result)?false: Boolean.parseBoolean(result);
	}

	@Override
	public String getDescription() {
		return super.getSettings(Attributes.DESCRIPTION);
	}

	@Override
	public Status getStatus() {
		String result = super.getSettings(Attributes.STATUS);
		return StringUtils.isEmpty(result)? Status.INIT: Status.valueOf(result);
	}

	@Override
	public boolean setStatus(Status status) {
		if( status == null )
			return false;
		super.putSettings(Attributes.STATUS, status.name());
		return true;
	}

	@Override
	public void addAttribute(String key, String value) {
		super.putSettings(key, value);
	}

	@Override
	public void removeAttribute(String key) {
		super.removeSettings(key);
	}

	@Override
	protected IConfigData onDecorate(Preferences preferences) {
		return new StoredConfigData( preferences );
	}	

	@Override
	public IConfigData addChild( String id) {
		StoredConfigData child  = (StoredConfigData) super.addChild(id );
		return child;
	}

	@Override
	public boolean addChild( IConfigData data) {
		StoredConfigData child  = (StoredConfigData) super.addChild(data.getId());
		return ( child != null );
	}

	@Override
	public boolean removeChild( IConfigData data) {
		super.removeChild( data.getId());
		return true;
	}

	@Override
	public boolean removeChild(String configId) {
		return super.removeChild(configId);
	}

	@Override
	public IConfigData getChild(String configId) {
		return super.getChild(configId);
	}

	@Override
	public IConfigData[] getChildren() {
		Collection<IConfigData>children = super.getStoreChildren();
		return children.toArray( new IConfigData[ children.size()]);
	}
	
	public IConfigData toConfigData() {
		IConfigData config = new ConfigData( getId());
		try {
			toConfigData(this, config);
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		return config;
	}
	
	private final void toConfigData( StoredConfigData sconfig, IConfigData config ) throws BackingStoreException {
		Preferences prefs = sconfig.getSettings();
		for( String str: prefs.childrenNames() ) {
			config.addAttribute(str, getSettings(str));
		}
		for( IConfigData child: sconfig.getChildren()) {
			toConfigData((StoredConfigData) child, new ConfigData());
		}
	}	
}
