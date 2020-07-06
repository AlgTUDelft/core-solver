/**
 * @file Function.java
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
 * @date         27 jun. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.function;

import java.lang.reflect.InvocationTargetException;

import core.exceptions.IIOException;

/**
 * Interface for all cost functions
 *
 * @author Joris Scharpff
 */
public abstract class Function {
	/**
	 * Evaluates the function given current week and the horizon
	 * 
	 * @param week The current week
	 * @param horizon The plan problem horizon
	 * @return The result from evaluating this function
	 */
	public abstract double eval( int week, int horizon );
	
	/**
	 * Evaluates a function that is not time dependent
	 * 
	 * @return The function result
	 */
	public double eval( ) {
		return eval( 0, 1 );
	}

	/**
	 * String describing the cost function (for print purpose)
	 * 
	 * @return The function description
	 */
	public abstract String getFunction( );
	
	/**
	 * Creates a copy of the function
	 * 
	 * @return A copy of the function
	 */
	public Function copy( ) {
		return copy( 1.0 );
	}
	
	/**
	 * Copies the function and applies the scalar weight
	 * 
	 * @param weight The scalar
	 * @return A copy g(x) of the function such that g(x) = f(x) * weight
	 */
	public abstract Function copy( double weight );
	
	/**
	 * Converts the function to a serialisable string
	 * 
	 * @return The string that can be serialised
	 */
	protected abstract String doSerialise( );
	
	/**
	 * Serialises the function
	 * 
	 * @return The serialised function 
	 */
	public String serialise( ) {
		return this.getClass( ).getName( ) + ";" + doSerialise( );
	}
	
	/**
	 * Reads the function from a serialised string
	 * 
	 * @param serialised The serialised social cost model string
	 * @return The social cost model
	 * @throws IIOException
	 */
	public static Function deserialise( String serialised ) throws IIOException {
		try {
			final int idx = serialised.indexOf( ';' );
			final String classname = serialised.substring( 0, idx );
			final String s = serialised.substring( idx + 1 );
			return (Function) Class.forName( classname ).getConstructor( String.class ).newInstance( s );
		} catch( Exception e ) {
			if( e instanceof NoSuchMethodException )
				throw new IIOException( "Function does not implement a deserialisable constructor (constructor(String) throws IIOException)" );
			if( e instanceof InvocationTargetException )
				throw ((IIOException) ((InvocationTargetException) e).getTargetException( ) );
				
			throw new IIOException( "Failed to create function", e );
		}
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return getFunction( );
	}
}
