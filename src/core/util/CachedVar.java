/**
 * @file CachedVar.java
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
 * Container for a cached variable, includes valid flag
 * 
 * @param <T> The value type
 * @author Joris Scharpff
 */
public class CachedVar<T> {
	/** Indicates whether the cached value is still valid */
	protected boolean valid;
	
	/** The value */
	protected T value;
	
	/**
	 * Creates the variable
	 */
	public CachedVar( ) {
		value = null;
		valid = false;
	}
	
	/**
	 * Creates the variable with initial value
	 * 
	 * @param value The initial value
	 */
	public CachedVar( T value ) {
		this.value = value;
		valid = true;
	}
	
	/**
	 * Makes a copy of the cached variable
	 * 
	 * @return The copy
	 */
	public CachedVar<T> copy( ) {
		final CachedVar<T> c = new CachedVar<T>( );
		if( isValid( ) )
			c.setValue( getValue( ) );
		return c;
	}
		
	/**
	 * Sets the value of the cached variable and marks it valid
	 * 
	 * @param value The value to set
	 */
	public void setValue( T value ) {
		this.value = value;
		valid = true;
	}
	
	/**
	 * @return The cached value
	 * @throws InvalidCacheException if the value is invalid
	 */
	public T getValue( ) throws InvalidCacheException {
		if( !valid ) throw new InvalidCacheException( );
		return value;
	}
	
	/**
	 * Invalidates the cached value
	 */
	public void invalidate( ) {
		valid = false;
	}
	
	/**
	 * @return True if the value is valid
	 */
	public boolean isValid( ) {
		return valid;
	}
}
