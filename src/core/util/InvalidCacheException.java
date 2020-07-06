/**
 * @file InvalidCacheException.java
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
 * @date         14 feb. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.util;


/**
 * Exception thrown whenever a cached variable is read but is invalid
 * 
 * @author Joris Scharpff
 */
@SuppressWarnings("serial")
public class InvalidCacheException extends RuntimeException {
	/**
	 * Creates a new cache exception
	 */
	public InvalidCacheException( ) { super( ); }
}
