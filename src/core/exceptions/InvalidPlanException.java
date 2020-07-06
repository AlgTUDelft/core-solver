/**
 * @file InvalidPlanException.java
 * @brief Short description of file
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2013 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         2 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.exceptions;

import core.domains.mpp.MPPPlan;

/**
 * This exception is thrown whenever any alteration of the plan is refused
 * because it would otherwise result in an invalid plan. Note that this is an
 * unchecked exception and hence the programmer can choose to not handle this
 * type of exception (e.g. in cases when you are sure such an error will never
 * occur)
 *
 * @author Joris Scharpff
 */
@SuppressWarnings ("serial" )
public class InvalidPlanException extends RuntimeException {
	/**
	 * Creates a new exception with error message
	 * 
	 * @param msg The error description
	 */
	public InvalidPlanException( String msg ) {
		super( msg );
	}	
	
	/**
	 * Creates a new exception with error message
	 * 
	 * @param msg The error description
	 * @param plan The current plan
	 */
	public InvalidPlanException( String msg, MPPPlan plan ) {
		super( msg + "\n" + plan.toString( ) );
	}
}
