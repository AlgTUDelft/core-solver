/**
 * @file GeneratorException.java
 * @brief Short description of file
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2014 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         10 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.exceptions;

/**
 * Exception that is thrown when generation fails, does not have to be caught
 *
 * @author Joris Scharpff
 */
@SuppressWarnings("serial")
public class GeneratorException extends RuntimeException {
	/**
	 * Creates a new GeneratorException
	 * 
	 * @param msg The error message
	 */
	public GeneratorException( String msg ) {
		super( msg );
	}
}
