/**
 * @file OutcomeRealisation.java
 * @brief [brief description]
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2014 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         20 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.policy;

import java.util.HashMap;
import java.util.Map;

import core.domains.Instance;


/**
 * Container for 'known' outcome realisations used in simulation and plan
 * synthesis
 * 
 * @author Joris Scharpff
 */
public class OutcomeRealisation {
	/** The instance */
	protected final Instance I;
	
	/** The array containing an outcome for each action at every time step */
	protected Map<Action, Outcome[]> outcomes;
	
	/**
	 * Creates a new outcome realisation for the specified instance
	 * 
	 * @param inst The instance
	 */
	public OutcomeRealisation( Instance inst ) {
		this.I = inst;
		
		// generate empty outcome mapping
		outcomes = new HashMap<Action, Outcome[]>( inst.getA( ) );
	}
	
	/**
	 * Sets the outcome for the action in the specified time step
	 * 
	 * @param action The action
	 * @param time The state time
	 * @param outcome The outcome 
	 */
	public void setOutcome( Action action, int time, Outcome outcome ) {
		if( outcomes.get( action ) == null )
			outcomes.put( action, new Outcome[ I.getHorizon( ) ] );

		outcomes.get( action )[ time ] = outcome;
	}
	
	/**
	 * Returns the outcome for the action in the specified time step
	 * 
	 * @param action The action
	 * @param time The state time
	 * @return The outcome that is 'known' for the action
	 */
	public Outcome getOutcome( Action action, int time ) {
		/*
		 * JS 1-9-2016: I don't think this is actually required anymore? Anyway,
		 *   it seems no longer in use, therefore I commented this (domain
		 *   dependent) code out.
		 
		// FIXME domain dependent
		if( action instanceof Task ) {
			final Task t = (Task) action;
			if( t.isNoOp( ) || t.isContinue( ) )
				return new MPPOutcome( (Task) action, false );
		}*/
		
		if( outcomes.get( action ) == null || outcomes.get( action )[ time ] == null )
			throw new NullPointerException( "No outcome for action " + action + " (time: " + time + ")" );
		
		return outcomes.get( action )[ time ];
	}
	
	/**
	 * Returns the joint outcome as realised action for the specified joint
	 * action and time
	 * 
	 * @param ja The joint action
	 * @param time The state time
	 * @return The realised outcomes as a realised action
	 */
	public RealisedJAction getOutcomes( JointAction ja, int time ) {
		final RealisedJAction ra = new RealisedJAction( ja );
		
		// get all individual outcomes
		for( Agent a : ra.getAgents( ) )
			ra.setOutcome( a, getOutcome( ja.getAction( a ), time ) );
		
		return ra;
	}
}
