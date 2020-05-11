package org.covaid.ui.simulator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Canvas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.condast.commons.ui.session.PushSession;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IFieldEnvironment;
import org.covaid.core.def.IDomainListener;
import org.covaid.core.def.IPerson;
import org.covaid.core.environment.IDomain.DomainEvents;
import org.covaid.core.environment.field.IFieldDomain;
import org.covaid.core.model.date.DateContagion;
import org.covaid.core.environment.DomainEvent;
import org.covaid.core.environment.IDomain;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class GraphCanvas extends Canvas {
	private static final long serialVersionUID = 1L;

	private PushSession<DomainEvent<Date>> session;

	private Map<IFieldDomain, List<Data>> domains;

	protected Logger logger = Logger.getLogger(this.getClass().getName());

	private IDomainListener<Date> listener = (e)->{
		if( getDisplay().isDisposed() || DomainEvents.UPDATE_PERSON.equals(e.getEvent()))
			return;
		IFieldDomain domain = (IFieldDomain) e.getSource();
		IFieldEnvironment env = (IFieldEnvironment) domain.getEnvironment();
		List<Data> list = domains.get( domain );
		Data data = list.get(list.size()-1);
		int infected = 0;
		IContagion<Date> contagion = new DateContagion( IContagion.SupportedContagion.valueOf( env.getContagion()), 100);
		for( IPerson<Date> person: domain.getPersons() ) {
			if( person.getContagiousness( contagion ) > 10 )
				infected++;
		}

		if( infected > data.infected) {
			list.add( new Data( infected ));
		}
				
		getDisplay().asyncExec(()->{
			try {
				redraw();
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
			requestLayout();
		});
	};

	private PaintListener paintListener = (event)->{
		try{
			updateCanvas(event.gc );
		}
		catch( Exception ex ){
			ex.printStackTrace();
		}
	};
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public GraphCanvas(Composite parent, int style) {
		super(parent, style);
		this.createComposite(parent, style);
		this.domains = new HashMap<>();
		session = new PushSession<DomainEvent<Date>>();
		session.start();
	}

	protected void createComposite( Composite parent, int style ) {
		addPaintListener(paintListener);
	}

	public void addInput( IFieldEnvironment environment) {
		for( IDomain<Date> domain: environment.getDomains()) {
			if( !this.domains.containsKey(domain) ) {
				List<Data> list = Collections.synchronizedList( new ArrayList<>());
				list.add( new Data( 0 ));
				this.domains.put( (IFieldDomain) domain, list);
			}
			domain.addListener(listener);
		}
		requestLayout();
	}

	public void removeInput( IFieldEnvironment environment ) {
		for( IDomain<Date> domain: environment.getDomains()) {
			if( this.domains.containsKey(domain) ) {
				this.domains.remove(domain);
				domain.removeListener(listener);
			}
		}
	}

	protected void updateCanvas( GC gc ) {
		try {
			Rectangle rectangle = getBounds();
			gc.drawLine(0, rectangle.height - 20, rectangle.width, rectangle.height - 20);
			gc.drawLine(0, 0, 0, rectangle.height - 20);
			setData(null);
			int colourIndex = 0;
			for( IFieldDomain domain: this.domains.keySet() ) { 
				Collection<Data> progress = domains.get(domain);
				if( progress.isEmpty())
					continue;
				Iterator<Data> iterator = new ArrayList<Data>( progress).iterator();
				Data first = iterator.next();
				int index = 0;
				int base = rectangle.height - 20;
				gc.setForeground( getColour(colourIndex++));				
				while( iterator.hasNext() ) {
					Data next = iterator.next();
					gc.drawLine(index, base - first.infected, index++, base - next.infected );
					first = next;
				}
			}
			gc.dispose();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
	}

	public void clear() {
		for( IDomain<Date> domain: this.domains.keySet()) {
			domain.clear();
			this.domains.get(domain).add(new Data( 0 ));
		}
	}

	public Color getColour( int index ) {
		Device device = getDisplay();
		Color colour = device.getSystemColor( SWT.COLOR_BLACK);	
		switch( index ) {
		case 0:
			colour = device.getSystemColor( SWT.COLOR_BLUE);	
			break;
		case 1:
			colour = device.getSystemColor( SWT.COLOR_MAGENTA);	
			break;			
		default:
			break;
		}
		return colour;
	}

	public Color getColour( double contagion ) {
		Device device = getDisplay();
		double scale = contagion * 2.5;
		Color colour = new Color (device, (int)(scale), 0, (int)(255-scale));
		return colour;
	}
	

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void dispose() {
		removePaintListener(paintListener);
		this.session.stop();
	}
	
	private static class Data{
		
		private int activity;
		private int infected;
		
		public Data( int population) {
			super();
			this.infected = population;
		}
	}
}
