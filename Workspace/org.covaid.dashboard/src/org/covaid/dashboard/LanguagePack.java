package org.covaid.dashboard;

import java.util.Random;

import org.condast.commons.i18n.Language;
import org.condast.js.push.core.advice.IAdvice;

public class LanguagePack extends Language {
	
	private static final String S_LANGUAGE = "MoodleLanguage";

	public enum Fields{
		TITLE,
		SAYS,
		SUCCESS1;
				
		@Override
		public String toString() {
			return getInstance().getString( this);
		}
		
		public String getMessage() {
			return getInstance().getMessage( this );
		}		

		public static Fields getField( IAdvice.AdviceTypes type ) {
			Random random = new Random();
			int value = 1 + random.nextInt(2);
			String str = type.name() + value;
			return Fields.valueOf(str);
		}		

	}
	
	private static LanguagePack language = new LanguagePack();
	
	private LanguagePack() {
		super( S_LANGUAGE, "NL", "nl");
	}
	
	public static LanguagePack getInstance(){
		return language;
	}	
}
