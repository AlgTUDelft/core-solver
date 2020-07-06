/**
 * @file SolverException.java
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
 * @date         1 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.exceptions;

/**
 * Base class for all solver exceptions
 *
 * @author Joris Scharpff
 */
@SuppressWarnings ("serial" )
public class SolverException extends Exception {
	/**
	 * Creates a new solver exception 
	 */
	public SolverException( ) {
		super( );
	}
	
	/**
	 * Creates a new solver exception
	 * 
	 * @param msg The exception message
	 */
	public SolverException( String msg ) {
		super( msg );		
	}
	
	/**
	 * Creates a new solver exception with the specified message and originating
	 * exception
	 *  
	 * @param msg The error message
	 * @param e The originating exception 
	 */
	public SolverException( String msg, Exception e ) {
		super( msg, e );
	}
}
