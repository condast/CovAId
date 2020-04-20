package org.covaid.ui.map;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;
import org.condast.commons.Utils;
import org.condast.commons.authentication.user.ILoginUser;
import org.condast.commons.data.latlng.LatLng;
import org.condast.commons.data.latlng.Polygon;
import org.condast.commons.data.plane.FieldData;
import org.condast.commons.data.plane.IPolygon;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.strings.StringUtils;
import org.condast.js.commons.eval.EvaluationEvent;
import org.condast.js.commons.eval.IEvaluationListener;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Composite;
import org.openlayer.map.control.GeoView;
import org.openlayer.map.control.ShapesView;
import org.openlayer.map.controller.OpenLayerController;

public class MapBrowser extends Browser {
	private static final long serialVersionUID = 1L;

	public static String S_ERR_NO_FIELD_DATA = "The vessel does not have any field data: ";
	public static String S_ERR_NO_GPS_SIGNAL = "NO GPS SIGNAL";
	
	public static final int DEFAULT_SCAN_DELAY = 20;//20 update pulses
	
	private enum CallBacks{
		POINT,
		POLYGON;
		
		@Override
		public String toString() {
			return StringStyler.xmlStyleString(name());
		}
	}

	private OpenLayerController mapController;
	
	private boolean drawing;
	
	private LatLng clickedLocation;
	
	
	private ILoginUser user;

	private GeoView geo;
	
	
	private ProgressListener plistener = new ProgressListener() {
		private static final long serialVersionUID = 1L;
		@Override
		public void completed(ProgressEvent event) {
			try{
				logger.info("Browser activated" );
			}
			catch( Exception ex ){
				ex.printStackTrace();
			}
		}

		@Override
		public void changed(ProgressEvent event) {
		}
	};

	private IEvaluationListener<Object> mapListener = new IEvaluationListener<Object>(){

		@Override
		public void notifyEvaluation(EvaluationEvent<Object> event) {
			try {
				if(!OpenLayerController.S_CALLBACK_ID.equals(event.getId()))
					return;
				if( Utils.assertNull( event.getData()))
					return;
				Collection<Object> eventData = Arrays.asList(event.getData());
				StringBuilder builder = new StringBuilder();
				builder.append("Map data: ");
				for( Object obj: eventData ) {
					if( obj != null )
						builder.append(obj.toString());
					builder.append(", ");
					}
				logger.fine(builder.toString());
				String str = (String) event.getData()[1];
				if( !StringUtils.isEmpty(str) && str.startsWith( IPolygon.Types.POINT.name())) {
					Object[] loc = ( Object[])event.getData()[2];
					clickedLocation = new LatLng((String) event.getData()[1], (double)loc[1], (double)loc[0] );
					if(( geo.getFieldData() != null ) && !( geo.getFieldData().isPersisted())) {
						geo.getFieldData().setCoordinates(clickedLocation);
						geo.zoomin();
						geo.jump();
					}
				}
				if( IEvaluationListener.EventTypes.SELECTED.equals( event.getEventType())) {
					logger.info(event.getData()[2].toString());
				}else {
					String data = (String) event.getData()[1];
					if( !StringUtils.isEmpty(data) && data.startsWith( CallBacks.POLYGON.name() )) {
						String wkt = (String )event.getData()[1];
						if( StringUtils.isEmpty( wkt ))
							return;
						String tp = (String) event.getData()[0];
						ShapesView.Commands command = ShapesView.Commands.valueOf( StringStyler.styleToEnum(tp));
						if( !ShapesView.Commands.ADDEND_SHAPE.equals(command))
							return;
						StringBuffer buffer = new StringBuffer();
						buffer.append(tp);
						buffer.append(": ");
						if( wkt.startsWith( IPolygon.Types.POLYGON.name())) {
							buffer.append(wkt + "\n");
							//polygon = Polygon.fromWKT( name, wkt);
						}
						logger.fine( buffer.toString());
					}else {
						if( drawing )
							return;
						Object[] coords = (Object[]) event.getData()[2];
						LatLng latlng = new LatLng(( Double) coords[1], (Double)coords[0]);				
					}
				}
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
	};
		
	private Logger logger = Logger.getLogger( this.getClass().getName() );

	public MapBrowser(Composite parent, int style) {
		super(parent, style);
		this.drawing = false;
		this.mapController = new OpenLayerController( this );
		this.mapController.addEvaluationListener(mapListener);
		this.geo = new GeoView( mapController );
		this.addProgressListener(plistener);
	}


	public void setClickedLocation( FieldData fieldData) {
		if( clickedLocation == null )
			return;
		LatLng location = clickedLocation;
		location.setId( fieldData.getName());
		fieldData.setZoom(geo.getZoom());
		fieldData.setCoordinates(location);
	}

	protected void setFeature( FieldData fieldData ) {
		//Only polygons after logging in
		if( fieldData == null )
			return;
		//NOTE: System crashed on null WKT string!
		if( !StringUtils.isEmpty( fieldData.getWtkString() ))
			logger.fine(fieldData.getWtkString());
		ShapesView shapes = new ShapesView( mapController );
		shapes.clear();
		Polygon pg = new Polygon( fieldData );
		pg.setFieldData();
		shapes.addShape( pg );
		for( IPolygon child: fieldData.getChildren() )
			shapes.addShape(child.toWKT());
		geo.setFieldData(fieldData);
		geo.jump();
	}

	public void setInput( String context, ILoginUser user ){
		this.user = user;
	}
	

	protected void request( LatLng latlng, boolean isInField ) throws Exception {
		if( user == null )
			return;
		/*
		Map<String, String> params = waypointController.getUserParams(user.getId(), user.getToken());
		switch( request ) {
		case ADD:
			params.put(WaypointData.Parameters.NAME.toString(), String.valueOf( latlng.getId() ));
			params.put(WaypointData.Parameters.LATITUDE.toString(), String.valueOf( latlng.getLatitude() ));
			params.put(WaypointData.Parameters.LONGITUDE.toString(), String.valueOf( latlng.getLongitude() ));
			params.put(WaypointData.Parameters.ACTIVE.toString(), String.valueOf( isInField ));
			break;
		default:
			break;
		}
		*/
	}

	public void refresh( long vesselId ) {
		if( user == null )
			return;
		//vesselController.refresh();
	}

	public void clear() {
	}

	
	public void dispose() {
		this.mapController.removeEvaluationListener( mapListener);
		this.mapController.dispose();
		this.removeProgressListener(plistener);
		super.dispose();
	}
}
