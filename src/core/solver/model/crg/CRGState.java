/**
 * @file CRGState.java
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
 * State in the Conditional Return graph
 * 
 * @author Joris Scharpff
 */
public abstract class CRGState {
	/** The agent to which the state belongs */
	final Agent agent;
	
	/** The time in the current state */
	protected final int time;
	
	/**
	 * Creates a new state
	 * 
	 * @param agent The agent to which the state belongs
	 * @param time The time in the state
	 */
	protected CRGState( Agent agent, int time ) {
		this.agent = agent;
		this.time = time;
	}
	
	/** 
	 * @return The agent to which the state belongs
	 */
	public Agent getAgent( ) {
		return agent;
	}
	
	/**
	 * @return The time in the state
	 */
	public int getTime( ) {
		return time;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return agent.ID + 100 * time;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof CRGState) ) return false;
		final CRGState s = (CRGState)obj;
		
		if( !agent.equals( s.agent ) ) return false;
		if( time != s.time ) return false;
		
		return true;
		
	}
}
