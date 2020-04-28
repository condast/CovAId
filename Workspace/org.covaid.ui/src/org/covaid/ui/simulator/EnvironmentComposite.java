package org.covaid.ui.simulator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.condast.commons.data.plane.Field;
import org.condast.commons.data.plane.IField;
import org.condast.commons.data.util.Vector;
import org.condast.commons.ui.session.PushSession;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IEnvironmentListener;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.EnvironmentEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;

public class EnvironmentComposite extends Composite {
	private static final long serialVersionUID = 1L;

	private Canvas canvas;

	private PushSession<EnvironmentEvent> session;

	private IEnvironment environment;

	private int counter;
	private IEnvironmentListener listener = (e)->{
		if( getDisplay().isDisposed())
			return;
		session.addData(e);
		counter++;
		counter%=environment.getPopulation();
		if( counter != 1 )
			return;
		getDisplay().asyncExec(()->{
			try {
				canvas.setData(e);
				canvas.redraw();
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
			requestLayout();
		});
	};

	private PaintListener paintListener = (event)->{
		try{
			EnvironmentEvent ee = (EnvironmentEvent) canvas.getData();
			if(( ee == null ) || ( ee.getPerson() == null ))
				return;
			updateCanvas(event.gc );
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
	};

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public EnvironmentComposite(Composite parent, int style) {
		super(parent, style);
		this.createComposite(parent, style);
		this.counter = 0;
		session = new PushSession<EnvironmentEvent>();
		session.start();
	}

	protected void createComposite( Composite parent, int style ) {
		setLayout(new GridLayout(1, false));

		canvas = new Canvas(this, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		canvas.addPaintListener(paintListener);
		canvas.addListener(SWT.RESIZE, (elistener)->{ resize( canvas.getBounds()); });

	}

	public void setInput( IEnvironment environment) {
		if( this.environment != null ) {
			this.environment.removeListener(listener);
		}
		this.environment = environment;
		if( this.environment != null ) {
			this.environment.addListener(listener);
		}
		canvas.requestLayout();
	}

	protected void updateCanvas( GC gc ) {
		try {
			EnvironmentEvent event = (EnvironmentEvent) canvas.getData();
			if( event == null )
				return;
			for( IPerson person: this.environment.getPersons())
				updatePerson(gc, person);
			gc.dispose();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
	}

	protected void updatePerson( GC gc, IPerson person ) {
		try {
			EnvironmentEvent event = (EnvironmentEvent) canvas.getData();
			if( event == null )
				return;
			IContagion contagion = IContagion.SupportedContagion.getContagion( this.environment.getContagion());
			Date date = environment.getDate();

			double safety = person.getSafetyBubble(contagion, Calendar.getInstance().getTime());

			Map<Date, ILocation> history = person.getHistory().get();
			Iterator<Map.Entry<Date, ILocation>> iterator = new ArrayList<Map.Entry<Date, ILocation>>( history.entrySet()).iterator();
			while( iterator.hasNext() ) {
				Map.Entry<Date, ILocation> entry = iterator.next();
				Vector<Double,Double> vector = contagion.getContagion(date);
				gc.setBackground( getColour( vector.getKey().doubleValue() ));
				//drawPerson( gc, entry.getValue(), vector.getValue().doubleValue());
			}

			gc.setBackground( getColour( person.getContagiousness(contagion)));
			drawPerson( gc, person.getLocation(), safety );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
	}

	public Color getColour( double contagion ) {
		Device device = getDisplay();
		double scale = contagion * 2.5;
		Color colour = new Color (device, (int)(scale), 0, (int)(255-scale));
		return colour;
	}

	protected void drawPerson( GC gc, IPoint location, double safety ) {
		Rectangle bounds = canvas.getBounds();
		gc.fillOval(bounds.x + scaleX(location.getXpos()), 
				bounds.y + scaleY(location.getYpos()), scaleX( safety), 10 );		
	}
	
	protected int scaleX( double size ) {
		if( environment == null )
			return 0;
		IField field = environment.getField();
		double result = (double)size * canvas.getBounds().width/field.getLength();
		return (int) (result);
	}

	protected int scaleY( double size ) {
		if( environment == null )
			return 0;
		IField field = environment.getField();
		return (int)( size * canvas.getBounds().height/field.getWidth());
	}

	protected void resize( Rectangle rectangle ) {
		if(( environment == null ) || ( rectangle.height == 0 ) || (rectangle.width == 0 ))
			return;
		IField field = environment.getField();
		float scale = (float)rectangle.height/rectangle.width;
		field = new Field( field.getCoordinates(), field.getLength(),  (int) (field.getLength()*scale ));
		environment.setField(field);
		environment.setField(field);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void dispose() {
		canvas.removePaintListener(paintListener);
		this.session.stop();
		this.environment.dispose();
		this.environment.dispose();
	}
}
