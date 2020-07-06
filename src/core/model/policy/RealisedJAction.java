/**
 * @file RealisedAction.java
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
 * @date         27 feb. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.policy;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.util.Util;


/**
 * Joint action that also includes the outcome for all actions 
 * 
 * @author Joris Scharpff
 */
public class RealisedJAction extends JointAction {
	/** The outcomes for all of the actions */
	protected Map<Agent, Outcome> outcomes;
	
	/**
	 * Creates a new action realisation
	 * 
	 * @param agents The agents
	 * @param time The time of taking this joint action
	 */
	public RealisedJAction( Set<Agent> agents, int time ) {
		super( agents, time );
		
		outcomes = new HashMap<Agent, Outcome>( agents.size( ) );
	}
	
	/**
	 * Creates a new realised action that copies the joint actions but sets
	 * the outcome to null by default
	 * 
	 * @param ja The joint action to copy
	 */
	public RealisedJAction( JointAction ja ) {
		// copy joint action
		super( ja );
		
		// initialise delay and cached probability as invalid
		outcomes = new HashMap<Agent, Outcome>( ja.actions.size( ) );
	}
	
	/**
	 * Copies a realised action
	 * 
	 * @param ra The realised action
	 */
	public RealisedJAction( RealisedJAction ra ) {
		// copy other stuff
		super( ra );
		
		// copy delay values
		outcomes = new HashMap<Agent, Outcome>( ra.outcomes );
	}
	
	/**
	 * Sets the action and outcome for the agent
	 * 
	 * @param agent The agent
	 * @param action The action
	 * @param outcome The outcome in this realisation
	 */
	public void setAction( Agent agent, Action action, Outcome outcome ) {
		setAction( agent, action );
		setOutcome( agent, outcome );
	}
	
	/**
	 * Sets the outcome for the action
	 * 
	 * @param agent The agent
	 * @param outcome The outcome of its action
	 */
	public void setOutcome( Agent agent, Outcome outcome ) {
		outcomes.put( agent, outcome );
	}
	
	/**
	 * @param agent The agent
	 * @return The outcome for the action of the agent
	 * @throws NullPointerException if the outcome is not set
	 */
	public Outcome getOutcome( Agent agent ) throws NullPointerException {
		if( !hasOutcome( agent ) )
			throw new NullPointerException( "No outcome specified for agent " + agent + " (action: " + getAction( agent ) + ")" );
		return outcomes.get( agent );
	}
	
	/**
	 * @param agent The agent to check for
	 * @return True iff there is an outcome specified for the agent
	 */
	public boolean hasOutcome( Agent agent ) {
		return outcomes.containsKey( agent ) && outcomes.get( agent ) != null;
	}
	
	/**
	 * @return The probability of this realisation
	 */
	public double getProbability( ) {
		double p = 1.0;
		for( Agent a : getAgents( ) )
			p *= getOutcome( a ).getProbability( );

		return p;
	}
	
	/**
	 * @see core.model.policy.JointAction#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return time * actions.size( );
	}

	/**
	 * @see core.model.policy.JointAction#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( !super.equals( obj ) ) return false;
		if( !(obj instanceof RealisedJAction) ) return false;
		final RealisedJAction ra = (RealisedJAction) obj;
		
		// check if all outcomes match
		if( !outcomes.equals( ra.outcomes ) ) return false;
		
		return true;
	}
	
	/**
	 * Constructs all possible realisations for the given joint action
	 * 
	 * @param ja The joint action
	 * @return The list of all possible realisation combinations for each of the
	 * actions in the joint action
	 */
	public static List<RealisedJAction> allForJointAction( JointAction ja ) {
		final List<RealisedJAction> actions = new ArrayList<RealisedJAction>( );
		enumRealisations( ja, new ArrayList<Agent>( ja.getAgents( ) ), new RealisedJAction( ja ), actions );
		return actions;
	}
	
	/**
	 * Enumerates all possible realisations for the given set of agents and the
	 * joint action
	 * 
	 * @param ja The joint action
	 * @param agents The set of (remaining) agents
	 * @param actions The result list container
	 */
	protected static void enumRealisations( JointAction ja, List<Agent> agents, RealisedJAction curr, List<RealisedJAction> actions ) {
		// realisation for all agents?
		if( agents.size( ) == 0 ) {
			actions.add( new RealisedJAction( curr ) );
			return;
		}
		
		// try all outcomes realisations for my task
		final Agent agent = agents.remove( agents.size( ) - 1 );
		for( Outcome o : ja.getAction( agent ).getOutcomes( ) ) {
			curr.setOutcome( agent, o );
			enumRealisations( ja, agents, curr, actions );
		}
		
		// and add the agent again
		agents.add( agent );
	}
	
	/**
	 * @see core.model.policy.JointAction#toString()
	 */
	@Override
	public String toString( ) {
		final NumberFormat p = Util.perc( );
		
		String str = "{";
		for( Agent a : actions.keySet( ) ) {
			
			final Action act = getAction( a );
			final Outcome o = getOutcome( a );
			str += act.toString( ) + ": " + o.toString( ) + ", ";
		}
		str = str.substring( 0, str.length( ) - 2 );
		str += ", p: " + p.format( getProbability( ) ) + ", t = " + time + "}";
		return str;
	}
}
