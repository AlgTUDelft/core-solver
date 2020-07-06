/**
 * @file LinearFunc.java
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
 * @date         3 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.function;

import core.exceptions.IIOException;

/**
 * Linear function in the form Ax + b
 *
 * @author Joris Scharpff
 */
public class LinearFunction extends Function {
	/** The gradient */
	public final double a;
	
	/** The intercept */
	public final double b;
	
	/**
	 * Creates a new linear function ax + b
	 * 
	 * @param a The gradient
	 * @param b The intercept
	 */
	public LinearFunction( double a, double b ) {
		this.a = a;
		this.b = b;
	}
	
	/**
	 * Creates a linear function from a serialised string
	 * 
	 * @param serialised The string
	 * @throws IIOException if the deserialisation failed
	 */
	public LinearFunction( String serialised ) throws IIOException {
		final String[] f = serialised.split( "x+" );
		try {
			a = Double.parseDouble( f[ 0 ] );
			b = Double.parseDouble( f[ 1 ] );
		} catch( NumberFormatException nfe ) {
			throw new IIOException( "Failed to deserialise function '" + f + "'", nfe );
		}
	}
	
	/**
	 * @see core.model.function.Function#eval(int, int)
	 */
	@Override
	public double eval( int week, int horizon ) {
		return a * week + b;
	}
	
	/**
	 * @see core.model.function.Function#getFunction()
	 */
	@Override
	public String getFunction( ) {
		return a + "x + " + b;
	}
	
	/**
	 * Computes the intersect of this function with another linear function
	 * 
	 * @param lf The other linear function
	 * @return The intersect point as a double[2] array or null if no intersect
	 * possible, i.e. parallel lines
	 */
	public double[] getIntersection( LinearFunction lf ) {
		return getIntersection( lf, null );
	}
	
	
	/**
	 * Computes the intersect of this function with another linear function in
	 * the specified range
	 * 
	 * @param lf The other linear function
	 * @param range The range [min, max] to check for intersect, null for
	 * infinite
	 * @return The intersect point as a double[2] array or null if no intersect
	 * possible, i.e. parallel lines
	 */
	public double[] getIntersection( LinearFunction lf, double[] range ) {
		if( a == lf.a ) return null;
		
		// compute intersection x
		final double x = (lf.b - b) / (a - lf.a);
		final double y = a * x + b;
		
		// check if the intersection is in the specified range
		if( range != null )
			if( x < range[0] || x > range[1] )
				return null;
		
		return new double[] { x, y };
	}	
	
	/**
	 * @see core.model.function.Function#copy(double)
	 */
	@Override
	public Function copy( double weight ) {
		return new LinearFunction( a * weight, b * weight );
	}
	
	/**
	 * @see core.model.function.Function#doSerialise()
	 */
	@Override
	public String doSerialise( ) {
		return a + "x+" + b;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "y = " + a + "x + " + b;
	}
}
