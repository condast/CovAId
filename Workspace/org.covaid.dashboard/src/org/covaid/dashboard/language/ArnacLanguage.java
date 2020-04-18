package org.covaid.dashboard.language;

import org.condast.commons.i18n.Language;

public class ArnacLanguage extends Language {

	private static final String S_VG_LANGUAGE = "VGLanguage";
		
	public enum SupportedText{
		GUEST_MEMBERS;
	}
	
	public ArnacLanguage() {
		super( S_VG_LANGUAGE, "NL", "nl");
	}
}
