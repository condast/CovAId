package org.covaid.rest.push;

import java.util.Iterator;
import java.util.Map;

import org.condast.commons.strings.StringUtils;
import org.condast.js.push.core.builder.PushOptionsBuilder;
import org.covaid.rest.LanguagePack;
import org.covaid.rest.LanguagePack.Fields;
import org.covaid.rest.core.IAdvice;

public class PushOptionsAdviceBuilder extends PushOptionsBuilder {

	public static final String S_ADVICE_TAG = "advice-tag";
	
	public PushOptionsAdviceBuilder() {
		super();
	}

	public byte[] createPayLoad( IAdvice advice, boolean renotify ) {
		LanguagePack language = LanguagePack.getInstance();
		String description = advice.getDescription();
		String body = StringUtils.isEmpty(description)? LanguagePack.Fields.SUCCESS1.name(): description;

		addOption( Options.TITLE, language.getString( advice.getMember()) + " " + Fields.SAYS.toString() + ":") ;		
		addOption( Options.BODY, language.getString(body));
		addOption( Options.DATA, advice );
		//addOption( Options.ICON, TeamImages.Team.getPath(advice));
		//addOption( Options.BADGE, TeamImages.Team.getPath(Team.PLUSKLAS));
		addOption( Options.TAG, S_ADVICE_TAG);
			
		addOption( Options.VIBRATE, new int[]{500,110,500,110,450,110,200,110,170,40,450,110,200,110,170,40,500});
		Iterator<Map.Entry<IAdvice.Notifications, String>>iterator = advice.getNotifications().entrySet().iterator();
		while( iterator.hasNext() ) {
			Map.Entry<IAdvice.Notifications, String> entry = iterator.next();
			addAction( entry.getKey().name(), language.getString( entry.getKey()), entry.getValue());
		}
		return createPayLoad(renotify, false );
	}
}
