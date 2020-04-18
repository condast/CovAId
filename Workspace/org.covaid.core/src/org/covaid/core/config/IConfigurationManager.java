package org.covaid.core.config;


public interface IConfigurationManager {

	IConfigData getRoot();

	boolean addConfigData(String parentId, IConfigData child);

	IConfigData getConfigData(String configId);

	void addTestListener(IConfigurationChangeListener listener);

	void removeTestListener(IConfigurationChangeListener listener);

	IConfigData.Status getStatus(String id);

	boolean addConfigData(IConfigData parent, IConfigData child);

	boolean removeConfigData(IConfigData parent, IConfigData child);

	boolean setStatus(String configId, IConfigData.Status status);

	void setRoot(IConfigData root);

	boolean isEnabled();

	void setEnabled(boolean enabled);
}