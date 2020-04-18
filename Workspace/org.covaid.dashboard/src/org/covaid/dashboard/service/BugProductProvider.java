package org.covaid.dashboard.service;

import org.condast.commons.bugzilla.api.AbstractBugProductFactory;
import org.condast.commons.bugzilla.api.IBugProduct;
import org.covaid.dashboard.Activator;
import org.osgi.service.component.annotations.Component;

@Component(
		name = "org.satr.arnac.bugzilla.provider"
)
public class BugProductProvider extends AbstractBugProductFactory{

	private static final String S_ARNAC_PRODUCT = "Arnac";
	private static final String S_ARNAC_COMPONENT = "VirgoComponent";

	public BugProductProvider() {
		super( true, S_ARNAC_PRODUCT, S_ARNAC_COMPONENT);
	}
	
	@Override
	protected boolean onWildcardCorrect(String wildcard) {
		return wildcard.startsWith( Activator.BUNDLE_ID);
	}

	@Override
	public IBugProduct createProduct(String wildcard) {
		return null;
	}
}
