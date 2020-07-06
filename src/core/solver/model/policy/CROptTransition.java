/**
 * @file CRPolTrans.java
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
 * @date         27 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.model.policy;

import java.util.Collection;

import core.domains.StateValue;
import core.model.policy.JointAction;


/**
 * A single state info object containing the value of the transition and the
 * optimal joint action
 * 
 * @author Joris Scharpff
 */
public class CROptTransition {
	/** The optimal joint action from this state */
	protected final JointAction jact;
	
	/** The possible transitions */
	protected final Collection<CRGJTransition> transitions;
	
	/** The optimal transition value */
	protected final StateValue value;
	
	/**
	 * Creates a new, empty state value
	 * 
	 * @param emptyval The empty state value
	 */
	public CROptTransition( StateValue emptyval ) {
		this( emptyval, null, null );
	}
	
	/**
	 * Creates a new state info object 
	 * 
	 * @param value The expected value of the state
	 * @param optjact The optimal joint action
	 * @param transitions The joint action transitions 
	 */
	public CROptTransition( StateValue value, JointAction optjact, Collection<CRGJTransition> transitions ) {
		this.value = value.copy( );
		this.jact = optjact;
		this.transitions = transitions;
	}
	
	/**
	 * @return The expected value of this state
	 */
	public StateValue getValue( ) {
		return value;
	}
	
	/** @return The optimal joint action */
	public JointAction getOptimalAction( ) {
		return jact;
	}
	
	/** @return The transitions of the optimal joint action */
	public Collection<CRGJTransition> getTransitions( ) {
		return transitions; 
	}
	
	/**
	 * @return True if this transition is to mark a terminal state of the problem
	 */
	public boolean isTerminal( ) {
		return transitions == null;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		if( isTerminal( ) ) return "Terminal";
		
		String str = "A=" + getOptimalAction( ) + " -> V=" + value;
		for( CRGJTransition t : transitions )
			str += "\n> " + t;

		return str;
	}
}