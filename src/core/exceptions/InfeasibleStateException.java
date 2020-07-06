/**
 * @file InfeasiableStateException.java
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
 * @date         3 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.exceptions;

import core.domains.State;

/**
 * Is thrown during the solving process to flag the state as infeasible,
 * meaning that it can never lead to a completing end state.
 * 
 * @author Joris Scharpff
 */
@SuppressWarnings( "serial" )
public class InfeasibleStateException extends Exception {
	/**
	 * Creates the exception
	 * 
	 * @param state The state that is infeasible
	 * @param msg The error message
	 */
	public InfeasibleStateException( State state, String msg ) {
		super( "Infeasible state: " + state + (msg != null ? " (" + msg + ")" : "" ) );
	}
	
	/**
	 * Creates an infeasible state exception
	 * 
	 * @param state The infeasible state
	 */
	public InfeasibleStateException( State state ) {
		this( state, null );
	}
}
