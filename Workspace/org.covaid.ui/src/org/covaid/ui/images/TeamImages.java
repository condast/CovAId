/*******************************************************************************
 * Copyright (c) 2016 Condast and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Contributors:
 *     Condast                - EetMee
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.covaid.ui.images;

import org.condast.commons.strings.StringStyler;
import org.condast.commons.ui.image.AbstractImages;
import org.condast.js.push.core.advice.IAdvice;
import org.eclipse.swt.graphics.Image;

public class TeamImages extends AbstractImages {

	public static final String S_BUNDLE_ID = "org.covaid.ui";

	public static final String S_DEFAULT_LOCATION = "/covaid/images/team/";
	public enum Team{
		AMANDA,
		GINO,
		CHARLES,
		RUBEN,
		NELLY,
		PLUSKLAS;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString() );
		}
		
		public static String getFileName( Team member, IAdvice.Mood mood ){
			String str = member.name().toLowerCase() + "/" + member.toString() + 
					"_" + mood.toString() + ".png";
			return str;
		}

		public static String getPath( Team member ){
			String name = member.name().toLowerCase();
			String str = name + "/" + name + "_Happy.png";
			return S_DEFAULT_LOCATION + str;
		}

		public static String getPath( Team member, IAdvice.Mood mood ){
			String str = member.name().toLowerCase() + "/" + member.toString() + 
					"_" + mood.toString() + ".png";
			return S_DEFAULT_LOCATION + str;
		}

		public static String getPath( IAdvice advice ){
			Team member = Team.valueOf(advice.getTitle().toUpperCase());
			String str;
			switch( member ) {
			case AMANDA:
			case GINO:
			case NELLY:
				str = member.name().toLowerCase() + "/" + member.toString() + ".png";
				break;
			default:
				str = member.name().toLowerCase() + "/" + member.toString() + ".png";
			}
			return S_DEFAULT_LOCATION + str;
		}

		/**
		 * 
		 * @param image, enum images like Images.ADD, DELETE and so on
		 * @param imageSize, 
		 * @return
		 */
		public static String getFileName( Team image, ImageSize imageSize ){
			String str = ImageSize.getLocation( image.name(), imageSize );//get location, filename and size from super
			return str;
		}

	}
	
	private static TeamImages images = new TeamImages();
	
	private TeamImages() {
		super( S_RESOURCES, S_BUNDLE_ID );
	}

	/**
	 * Get an instance of this map
	 * @return
	 */
	public static TeamImages getInstance(){
		return images;
	}
	
	@Override
	public void initialise(){
		for( Team img: Team.values() ) {
			for( IAdvice.Mood mood: IAdvice.Mood.values()){
				setImage( Team.getFileName( img, mood ));
			}
		}
	}

	/**
	 * Get the image
	 * @param desc
	 * @return
	 */
	public static Image getImage( Team desc, IAdvice.Mood mood ){
			return images.getImageFromName( Team.getFileName( desc, mood  ));				
	}

	/**
	 * 
	 * @param desc, an enumimage like Images.ADD, DELETE and so on
	 * @param imageSize, a size like Abstractimages.ImageSizes.SMALL, NORMAL, LARGE, TILE
	 * @return the image in the wanted size
	 */
	public static Image getImage( Team desc, ImageSize imageSize ){
			return images.getImageFromName( Team.getFileName( desc, imageSize ) );				
	}

}