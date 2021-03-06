package org.covaid.core.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.condast.commons.Utils;
import org.covaid.core.contagion.IntegerContagionOperator;
import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.hub.AbstractHub;
import org.covaid.core.hub.trace.AbstractTrace;

public class Hub extends AbstractHub<Integer> {

	public Hub(IPoint location) {
		this( location, 0, DEFAULT_HISTORY );
	}
	
	public Hub(IPoint location, int current, int history) {
		super(new Location( location.getIdentifier(), location.getXpos(), location.getYpos()), current, history, new Trace( current, false));
		super.getTrace().setHub(this);
	}

	/**
	 * convenience method to 
	 * @param person
	 */
	public Hub( IPerson<Integer> person ) {
		this( new Location( person.getLocation().getXpos(), person.getLocation().getYpos()));
	}

	/**
	 * convenience method to 
	 * @param person
	 */
	public Hub( IPerson<Integer> person, int timeStep, int history ) {
		this( person.getLocation(), timeStep, history );
	}

	/**
	 * Respond to an encounter with a person. This happens when a person enters the location of this hub
	 * The snapshots of the person and the location are compared, and the person is alerted
	 * if the risk of infection has increased. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	@Override
	public boolean encounter( IPerson<Integer> person, Integer step ) {
		if( !person.getLocation().equals( super.getLocation()))
			return false;
		ILocation<Integer> check = person.createSnapshot();

		//Determine the worst case situation for the encounter
		ILocation<Integer> worst = this.getLocation().createWorst( check );
		
		//Check if the person is worse off, and if so generate an alert		
		IContagion[] worse = check.getWorse( worst );
		if( !Utils.assertNull(worse)) {
			for( IContagion contagion: worse)
				person.alert( step, step, worst, contagion );
		}
		
		//If the hub has deteriorated, then add the person 
		worse = super.getLocation().getWorse( worst );
		if( !Utils.assertNull(worse)) {
			super.setLocation( worst );
		}
			
		super.put( person, step );
		return true;
	}
	
	@Override
	protected boolean onPersonAlert(IPerson<Integer> person, Integer moment, Integer timeStep, Integer history, Integer encountered) {
		return ( timeStep - moment )<history ;
	}

	@Override
	protected boolean onRemovePersons(IPerson<Integer> person, Integer timeStep, Integer history, Integer encountered) {
		return ( timeStep - encountered ) > history;
	}

	
	@Override
	public Map<Integer, Double> getPrediction(IContagion contagion, Integer range) {
		Map<Integer, Double> base = super.getPrediction(contagion, range);
		return base;
	}

	@Override
	public Hub clone(){
		Hub hub = new Hub( super.getLocation() );
		Iterator<Map.Entry<IPerson<Integer>, Integer>> iterator = super.getPersons().entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<IPerson<Integer>, Integer> entry = iterator.next();
			hub.put(entry.getKey(), entry.getValue());
		}
		return hub;
	}
	
	private static class Trace extends AbstractTrace<Integer>{

		public Trace( Integer current, boolean enabled ) {
			super( current, new IntegerContagionOperator(), enabled );
		}

		@Override
		protected Integer onGetAverage(Integer first, Integer second) {
			int f = ( first == null )?0: first;
			int l = ( second == null )?0: second;
			return (l+f)/2;
		}

		public Map<Integer, Double> getTraces(IContagion contagion, Integer range) {
			Map<IPoint, ContagionData<Integer>> map = super.getTraceMap(contagion, range);
			Map<Integer, Double> results = new HashMap<>();
			for( Map.Entry<IPoint, ContagionData<Integer>> entry: map.entrySet()) {
				int step = entry.getKey().getYpos();
				if( step < getCurrent() - range )
					continue;
				Double risk = results.get(step);
				if( risk == null )
					risk = entry.getValue().getRisk();
				else
					risk = ( risk + entry.getValue().getRisk())/2;
				results.put(step, risk);
			}
			return results;
		}
	}
}