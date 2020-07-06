/**
 * @file CRGTransition.java
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
 * @date         16 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.model.crg;

import core.domains.StateValue;
import core.model.policy.Action;
import core.model.policy.Agent;


/**
 * Represents a local transition with dependent actions and influence
 * 
 * @author Joris Scharpff
 */
public class CRGTransition {
	/** The local start state */
	protected final CRGState state;
	
	/** The local action that is taken */
	protected final Action action;
	
	/** The new local state */
	protected final CRGState newstate;
	
	/** The dependent actions */
	protected CRGDepActions depactions;
	
	/** The state influence */
	protected CRGInfluences influences;
	
	/** The transition value */
	protected StateValue value;
	
	/** The transition probability */
	protected double probability; 
	
	/**
	 * Creates a new purely local transition
	 * 
	 * @param state The local state
	 * @param action The local action
	 * @param newstate The new local state
	 */
	public CRGTransition( CRGState state, Action action, CRGState newstate ) {
		this( state, action, newstate, new CRGDepActions( ), new CRGInfluences( ) );
	}
	
	/**
	 * Creates a new local state transition with dependent action(s) and influence
	 * 
	 * @param state The local state
	 * @param action The local action
	 * @param newstate The new local state
	 * @param depactions The set of action dependencies
	 * @param influences The transition influence
	 */
	public CRGTransition( CRGState state, Action action, CRGState newstate, CRGDepActions depactions, CRGInfluences influences ) {
		this.state = state;
		this.action = action;
		this.newstate = newstate;
		this.depactions = depactions;
		this.influences = influences;
		
		this.probability = -1; // not set
	}
	
	/**
	 * Copies the transition
	 * 
	 * @return The copy
	 */
	public CRGTransition copy( ) {
		final CRGTransition t = new CRGTransition( state, action, newstate, new CRGDepActions( depactions ), new CRGInfluences( influences ) );
		t.probability = this.probability;
		return t;
	}
	
	/** @return The local agent */
	public Agent getAgent( ) { return state.getAgent( ); }
	
	/** @return The local state */
	public CRGState getState( ) { return state; }

	/** @return The local action */
	public Action getAction( ) { return action; }
	
	/** @return The new local state */
	public CRGState getNewState( ) { return newstate; }
	
	/** @return The action dependencies */
	public CRGDepActions getDependencies( ) { return depactions; }
	
	/** @return The transition influence */
	public CRGInfluences getInfluences( ) { return influences; }
	
	/**
	 * Sets the value of the transition
	 * 
	 * @param value The value
	 */
	public void setValue( StateValue value ) {
		this.value = value;
	}
	
	/** @return The transition value */
	public StateValue getValue( ) {
		assert value != null : "No value set for transition";
		
		return value;
	}
	
	/**
	 * Sets the transition probability
	 * 
	 * @param prob The transition probability
	 */
	public void setProbability( double prob ) {
		assert probability == -1 : "Probability already set to " + probability;
		this.probability = prob;
	}
	
	/**
	 * @return The transition probability
	 */
	public double getProbability( ) {
		assert probability != -1 : "Transition probability not set";
		return probability;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		final String A = " A=" + getDependencies( ).toString( );
		final String I = getInfluences( ).size( ) == 0 ? "" : " I=" + getInfluences( ).toString( );
		final String V = value == null ? "" : " V=" + value;
		return "T(" + state + ", " + action + ", " + newstate + ")" + A + I + V;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return state.hashCode( ) + newstate.hashCode( );
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof CRGTransition) ) return false;
		final CRGTransition t = (CRGTransition)obj;
		
		if( !state.equals( t.state ) ) return false;
		if( !action.equals( t.action ) ) return false;
		if( !newstate.equals( t.newstate ) ) return false;
		if( !depactions.equals( t.depactions ) ) return false;
		if( !influences.equals( t.influences ) ) return false;
		
		return true;
	}
}
