/**
 * @file CRGInfluence.java
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

import core.model.policy.Agent;


/**
 * A single Transition Influence
 * 
 * @author Joris Scharpff
 */
public class CRGInfluence {
	/** The current state */
	protected final CRGState state;
	
	/** The new state */
	protected final CRGState newstate;

	/**
	 * Creates a new transition influence
	 * 
	 * @param state The current state
	 * @param newstate The new state
	 */
	public CRGInfluence( CRGState state, CRGState newstate ) {
		assert state.getAgent( ).equals( newstate.getAgent( ) ) : "States do not belong to the same agent";
		assert state.getTime( ) == newstate.getTime( ) - 1 : "States are not sequential";
		
		this.state = state;
		this.newstate = newstate;
	}
	
	/**
	 * @return The agent corresponding to this influence
	 */
	public Agent getAgent( ) {
		return state.getAgent( );
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
		if( obj == null || !(obj instanceof CRGInfluence) ) return false;
		final CRGInfluence I = (CRGInfluence)obj;
		
		if( !state.equals( I.state ) ) return false;
		if( !newstate.equals( I.newstate ) ) return false;
		
		return true;
	}
}
