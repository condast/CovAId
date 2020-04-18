package org.covaid.dashboard;

import org.condast.commons.i18n.Language;

public class CovaidLanguage extends Language {

	private static final String S_ARNAC_LANGUAGE = "ArnacLanguage";
	
	private static CovaidLanguage language = new CovaidLanguage();
	
	private CovaidLanguage() {
		super( S_ARNAC_LANGUAGE, "NL", "nl");
	}
	
	public static CovaidLanguage getInstance(){
		return language;
	}	
}
