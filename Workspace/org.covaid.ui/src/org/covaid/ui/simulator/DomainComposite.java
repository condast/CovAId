package org.covaid.ui.simulator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
import org.condast.commons.ui.session.AbstractSessionHandler;
import org.condast.commons.ui.session.SessionEvent;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IDomainListener;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.environment.AbstractDomain;
import org.covaid.core.environment.DomainEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;

public class DomainComposite extends Composite {
	private static final long serialVersionUID = 1L;

	private Canvas canvas;

	private SessionHandler handler;

	private AbstractDomain domain;

	private int counter;

	private PaintListener paintListener = (event)->{
		try{
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
	public DomainComposite(Composite parent, int style) {
		super(parent, style);
		this.createComposite(parent, style);
		this.counter = 0;
		handler = new SessionHandler( getDisplay());
	}

	protected void createComposite( Composite parent, int style ) {
		setLayout(new GridLayout(1, false));

		canvas = new Canvas(this, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		canvas.addPaintListener(paintListener);
		canvas.addListener(SWT.RESIZE, (elistener)->{ resize( canvas.getBounds()); });
	}

	public void setInput( AbstractDomain domain ) {
		if( this.domain != null ) {
			domain.removeListener(handler);
		}
		this.domain = domain;
		if( this.domain != null ) {
			domain.addListener(handler);
		}
		canvas.requestLayout();
	}

	protected void updateCanvas( GC gc ) {
		try {
			for( IPerson person: domain.getPersons())
				updatePerson(gc, person);
			gc.dispose();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
	}

	protected void updatePerson( GC gc, IPerson person ) {
		try {
			IEnvironment env = domain.getEnvironment();
			IContagion contagion = IContagion.SupportedContagion.getContagion( env.getContagion());
			Date date = env.getDate();

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
		if( domain == null )
			return 0;
		IField field = domain.getField();
		double result = (double)size * canvas.getBounds().width/field.getLength();
		return (int) (result);
	}

	protected int scaleY( double size ) {
		if( domain == null )
			return 0;
		IField field = domain.getField();
		return (int)( size * canvas.getBounds().height/field.getWidth());
	}

	protected void resize( Rectangle rectangle ) {
		if(( domain == null ) || ( rectangle.height == 0 ) || (rectangle.width == 0 ))
			return;
		IField field = domain.getField();
		float scale = (float)rectangle.height/rectangle.width;
		field = new Field( field.getCoordinates(), field.getLength(),  (int) (field.getLength()*scale ));
		domain.setField(field);
		domain.setField(field);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void dispose() {
		canvas.removePaintListener(paintListener);
		this.handler.dispose();
	}
	
	private class SessionHandler extends AbstractSessionHandler<DomainEvent> 
	implements IDomainListener{

		protected SessionHandler(Display display) {
			super(display);
		}

		@Override
		public void notifyPersonChanged(DomainEvent event) {
			if( getDisplay().isDisposed())
				return;
			
			if(AbstractDomain.DomainEvents.UPDATE_PERSON.equals(event.getEvent()))
				return;
			addData(event);
			counter++;
		}

		@Override
		protected void onHandleSession(SessionEvent<DomainEvent> sevent) {
			try {
				canvas.redraw();
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
			requestLayout();

		}
		
	}
}
