/**
 * @file CRGJTransition.java
 * @brief [brief description]
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2016 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         17 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.model.policy;

import core.domains.StateValue;
import core.model.policy.Agent;
import core.model.policy.JointAction;
import core.util.Util;


/**
 * Joint transition for policy search. Note that it is possible that a state
 * leads to a result state with less agents as they can be decoupled
 * 
 * @author Joris Scharpff
 */
public class CRGJTransition {
	/** The joint state */
	protected final CRGJState state;
	
	/** The joint action */
	protected final JointAction jact;
	
	/** The joint result state */
	protected final CRGJState newstate;
		
	/** The transition reward */
	protected StateValue reward;
	
	/** The transition probability */
	protected double probability;
	
	/** The bounds on the future reward */
	protected ValueBound bounds;
	
	/**
	 * Creates a new transition
	 * 
	 * @param state The start state
	 * @param jact The joint action
	 * @param newstate The new state
	 */
	public CRGJTransition( CRGJState state, JointAction jact, CRGJState newstate ) {
		super( );
		
		assert state.getAgents( ).containsAll( newstate.getAgents( ) ) : "Cannot add agents to the state in a transition";
		assert jact.getAgents( ).equals( newstate.getAgents( ) ) : "The agents in the transition must equal those in the new state";
		
		this.state = state;
		this.jact = jact;
		this.newstate = newstate;
		
		reward = null;
		probability = -1;
		bounds = null;
	}
	
	/**
	 * Combines the transition with the other transition
	 * 
	 * @param trans The other transition
	 * @return A new transition representing the combination of both
	 */
	public CRGJTransition combine( CRGJTransition trans ) {
		// combine joint actions
		final JointAction ja = new JointAction( jact.getTime( ) );
		for( Agent a : jact.getAgents( ) )
			ja.addAgent( a, jact.getAction( a ) );
		for( Agent a : trans.jact.getAgents( ) )
			ja.addAgent( a, trans.jact.getAction( a ) );
		
		
		return new CRGJTransition( state.combine( trans.state ), ja, newstate.combine( trans.newstate ) );
	}
	
	/**
	 * @return The current state	
	 */
	public CRGJState getState( ) {
		return state;
	}
	
	/**
	 * @return The joint action that is taken
	 */
	public JointAction getJointAction( ) {
		return jact;
	}
	
	/**
	 * @return The new state
	 */
	public CRGJState getNewState( ) {
		return newstate;
	}
	
	/**
	 * Sets the joint transition reward
	 * 
	 * @param reward The transition reward
	 */
	public void setReward( StateValue reward ) {
		assert this.reward == null : "Reward already set for transition " + this;
		this.reward = reward;
	}
	
	/**
	 * @return The transition reward
	 */
	public StateValue getReward( ) {
		assert reward != null : "No reward set for joint transition " + this;
		return reward;
	}
	
	/**
	 * Sets the transition probability
	 * 
	 * @param prob The transition probability
	 */
	public void setProbability( double prob ) {
		assert this.probability == -1 : "Probability already set for joint transition " + this;
		this.probability = prob;
	}
	
	/**
	 * @return The transition probability
	 */
	public double getProbability( ) {
		assert probability != -1 : "No probability set for joint transition " + this;
		return probability;
	}
	
	/**
	 * Sets the expected value bounds of this transition
	 * 
	 * @param bounds The bounds
	 */
	public void setBound( ValueBound bounds ) {
		assert this.bounds == null : "Bounds already set for this transition " + this;
		this.bounds = bounds;
	}
	
	/**
	 * @return The reward bounds
	 */
	public ValueBound getBounds( ) {
		assert bounds != null : "No bounds set for joint transition " + this;
		return bounds;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "T( " + state + ", " + jact + ", " + newstate + " )" +
					(reward != null ? " R=" + reward : "") +
					(probability != -1 ? " p=" + Util.dec( 3 ).format( probability ) : "") +
					(bounds != null ? " B=" + bounds : "");
	}
}
