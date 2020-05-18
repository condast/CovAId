package org.covaid.ui.mobile;

import java.util.Arrays;

import org.condast.commons.strings.StringStyler;
import org.condast.js.commons.controller.AbstractView;
import org.condast.js.commons.controller.IJavascriptController;
import org.covaid.core.def.IMobile;

public class MobileView extends AbstractView<MobileView.Commands>{
	
	public enum Commands{
		SET_HEALTH,
		SET_SAFETY;

		@Override
		public String toString() {
			return StringStyler.toMethodString(this.name());
		}
	}
	
	public MobileView( IJavascriptController controller) {
		super( controller );
	}

	/**
	 * Set the health slider
	 * @param name
	 * @param latlng
	 * @param opacity
	 * @return
	 */
	public String setHealthSlider( IMobile<?> mobile ){
		String[] params = new String[1];
		params[0] = String.valueOf( mobile.getHealth());
		return perform( Commands.SET_HEALTH, Arrays.asList( params ));
	}

	/**
	 * Set the safety slider
	 * @param name
	 * @param latlng
	 * @param opacity
	 * @return
	 */
	public String setSafetySlider( IMobile<?> mobile ){
		String[] params = new String[1];
		params[0] = String.valueOf( mobile.getHealth());
		return perform( Commands.SET_SAFETY, Arrays.asList( params ));
	}
}
