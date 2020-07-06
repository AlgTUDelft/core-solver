/**
 * @file Action.java
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
 * @date         19 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.policy;

import java.util.ArrayList;
import java.util.List;

import core.model.function.ConstFunction;
import core.model.function.Function;


/**
 * Represent an action that can be taken by an agent
 * 
 * @author Joris Scharpff
 */
public class Action {
	/** The agent responsible for the task */
	protected final Agent agent;
		
	/** The action ID */
	public final int ID;
	
	/** The action description */
	protected final String desc;
	
	/** The action reward (can be time dependent) */
	protected Function reward;
	
	/** The duration of the action */
	protected final int duration;
	
	/** Possible outcomes of the actions, stored with probabilities */
	protected List<Outcome> outcomes;
	
	/**
	 * Creates a new action
	 * 
	 * @param agent The responsible agent
	 * @param ID The action ID
	 * @param desc The action description
	 * @param reward The action reward function
	 * @param duration The action duration 
	 */
	public Action( Agent agent, int ID, String desc, Function reward, int duration ) {
		this.agent = agent;
		this.ID = ID;
		this.desc = desc;
		this.duration = duration;
		
		setReward( reward );
		
		// create outcome list
		outcomes = new ArrayList<Outcome>( );
	}
	
	/**
	 * @return The responsible agent
	 */
	public Agent getAgent( ) {
		return agent;
	}

	/**
	 * @return The action description
	 */
	public String getDescription( ) {
		return desc;
	}
	
	/**
	 * @return The action duration
	 */
	public int getDuration( ) {
		return duration;
	}
	
	/**
	 * Set the task reward function
	 * 
	 * @param reward The reward function
	 */
	public void setReward( Function reward ) {
		if( reward != null )
			this.reward = reward;
		else
			this.reward = new ConstFunction( 0 );
	}
	
	/**
	 * Returns the cost function, used only to save/load tasks
	 * 
	 * @return The cost function
	 */
	public Function getRewardFunction( ) {
		return reward;
	}	

	/**
	 * Sets the outcome for this action
	 * 
	 * @param outcome The outcome
	 */
	public void setOutcome( Outcome outcome ) {
		outcomes.clear( );
		outcomes.add( outcome );
	}
	
	/**
	 * Sets the possible outcomes for the action
	 * 
	 * @param outcomes The outcomes
	 * @param probs The associated probabilities
	 * @throws IllegalArgumentException if the probabilities do not sum to 1
	 */
	public void setOutcomes( Outcome... outcomes ) {		
		// clear previous outcomes and add all
		double prob = 0.0;
		this.outcomes.clear( );
		for( Outcome o : outcomes ) {
			// add the outcome
			this.outcomes.add( o );
			prob += o.getProbability( );
		}
		if( Math.abs( 1 - prob ) > 0.0001 )
			throw new IllegalArgumentException( "Outcome probabilities should sum to 1" );
	}
	
	/**
	 * @return The list of possible outcomes
	 */
	public List<Outcome> getOutcomes( ) {
		return outcomes;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "[A" + agent.ID + "-" + ID + "]";
	}
	
	/**
	 * Two actions equal if their agent's equal and their IDs are the same
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof Action) ) return false;
		final Action a = (Action) obj;
		
		if( ID != a.ID ) return false;
		if( !getAgent( ).equals( a.getAgent( ) ) ) return false;
		
		return true;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return (int)Math.pow( 2, agent.ID ) + ID; 
	}
}
