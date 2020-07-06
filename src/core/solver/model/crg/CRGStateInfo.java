/**
 * @file CRGStateInfo.java
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
 * @date         1 feb. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.model.crg;

import java.util.Collection;
import java.util.HashSet;

import core.solver.model.policy.ValueBound;


/**
 * State info that stores characteristics, transitions and bounds for a
 * single CRG state
 * 
 * @author Joris Scharpff
 */
public class CRGStateInfo {
	/** True iff the state is terminal */
	protected final boolean terminal;
	
	/** True iff the state is independent */
	protected final boolean independent;
	
	/** The expected return bounds */
	protected ValueBound bounds;
	
	/** The available transitions from the state */
	protected Collection<CRGTransition> transitions;
	
	/**
	 * Creates a new state info object
	 * 
	 * @param terminal True iff the state is terminal
	 * @param independent True iff the state is independent
	 */
	public CRGStateInfo( boolean terminal, boolean independent ) {
		this.terminal = terminal;
		this.independent = independent;
		
		bounds = null;
		transitions = null;
	}
	
	/**
	 * @return True iff the state is terminal
	 */
	public boolean isTerminal( ) {
		return terminal;
	}
	
	/**
	 * @return True iff the state is independent
	 */
	public boolean isIndependent( ) {
		return independent;
	}
	
	/**
	 * Sets the value bounds of the state
	 * 
	 * @param bounds The bounds
	 */
	public void setBounds( ValueBound bounds ) {
		assert this.bounds == null : "Bounds for the state already set";
		this.bounds = bounds;
	}
	
	/**
	 * @return The state expected return bounds
	 */
	public ValueBound getBounds( ) {
		assert bounds != null : "No bounds set for the state";
		return bounds;
	}
	
	/**
	 * Adds a single transition to the state info
	 * 
	 * @param trans The local transition to add
	 */
	public void addTransition( CRGTransition trans ) {
		// get the available transitions
		if( transitions == null ) transitions = new HashSet<CRGTransition>( );
		assert !transitions.contains( trans ) : "Transtion already added for state " + trans;
		transitions.add( trans );
	}
	
	/**
	 * Sets the transitions to the collection
	 * 
	 * @param transitions The collection of transitions
	 */
	public void setTransitions( Collection<CRGTransition> transitions ) {
		assert this.transitions == null : "(Some) transitions already set for the state";
		this.transitions = transitions;
	}
	
	/**
	 * @return The collection of available transitions
	 */
	public Collection<CRGTransition> getTransitions( ) {
		assert transitions != null : "No transitions for the state";
		return transitions;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return (terminal ? "TERM" : (independent ? "IND" : "" ) ) +
				" B=" + (bounds != null ? getBounds( ) : "[]") +
				" T=" + (transitions != null ? getTransitions( ) : "[]");
	}
}

