/**
 * @file InstanceIOException.java
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
 * @date         16 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.exceptions;

/**
 * Exception on reading/writing an instance
 *
 * @author Joris Scharpff
 */
@SuppressWarnings("serial")
public class IIOException extends Exception {
	/**
	 * Creates a new IO exception with the specified message
	 * 
	 * @param msg The message
	 */
	public IIOException( String msg ) {
		super( msg );
	}
	
	/**
	 * Creates a new IO exception with the specified message and originating exception
	 * 
	 * @param msg The error message
	 * @param e The original exception causing this IO exception
	 */
	public IIOException( String msg, Exception e ) {
		super( msg, e );
	}
}
