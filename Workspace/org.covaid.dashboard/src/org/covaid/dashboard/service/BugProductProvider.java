package org.covaid.dashboard.service;

import org.condast.commons.bugzilla.api.AbstractBugProductFactory;
import org.condast.commons.bugzilla.api.IBugProduct;
import org.covaid.dashboard.Activator;
import org.osgi.service.component.annotations.Component;

@Component(
		name = "org.covaid.dashboard.bugzilla.provider"
)
public class BugProductProvider extends AbstractBugProductFactory{

	private static final String S_COVAID_PRODUCT = "CovAID";
	private static final String S_COVAID_COMPONENT = "CovAIDComponent";

	public BugProductProvider() {
		super( true, S_COVAID_PRODUCT, S_COVAID_COMPONENT );
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
