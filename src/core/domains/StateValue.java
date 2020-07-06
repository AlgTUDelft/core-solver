/**
 * @file StateValue.java
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
 * @date         4 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains;

import java.text.NumberFormat;

import core.util.Util;


/**
 * Container for (MO) state values
 * 
 * @author Joris Scharpff
 */
public abstract class StateValue {
	/** The objectives */
	protected ObjectiveValue[] objectives;
	
	/**
	 * Creates a new state value container
	 * 
	 * @param obj The objectives
	 */
	protected StateValue( ObjectiveValue... obj ) {
		objectives = obj.clone( );
	}
		
	/**
	 * Copies the state value
	 * 
	 * @return The copy
	 */
	public abstract StateValue copy( );
	
	/**
	 * Creates a new state value combined from two state values
	 * 
	 * @param sv The other state value
	 * @param p The probability of this state value
	 * @return The state value that matches the expected value, i.e. sv1 * p +
	 * sv2 * (1-p)
	 */
	public StateValue expected( StateValue sv, double p ) {
		final StateValue res = copy( );
		res.scale( p );
		final StateValue s2 = sv.copy( );
		s2.scale( 1 - p );
		res.add( s2 );
		return res;
	}
	
	/**
	 * @return The state total
	 */
	public double getTotal( ) {
		// compute weighted total with unit weights
		double sum = 0.0;
		for( ObjectiveValue o : objectives )
			sum += o.getValue( );
		
		return sum;
	}
	
	/**
	 * Computes the weighted total
	 * 
	 * @param w The weight for each objective
	 * @return The weighted total
	 * @throws IllegalArgumentException if the number of weights does not match
	 * the number of objectives
	 */
	public double getTotal( double... w ) {
		if( w.length != objectives.length )
			throw new IllegalArgumentException( "Invalid number of weights specified" );
		
		double sum = 0;
		for( int i = 0; i < objectives.length; i++ )
			sum += w[i] * objectives[i].getValue( );
		return sum;
	}
	
	/**
	 * Adds the specified state value to this state value
	 * 
	 * @param sv The value to add
	 * @throws IllegalArgumentException if the state value is incompatible
	 */
	public void add( StateValue sv ) {
		if( sv.objectives.length != objectives.length )
			throw new IllegalArgumentException( "Incompatible state value: " + sv );
		
		for( int i = 0; i < objectives.length; i++ )
			objectives[i].add( sv.objectives[i].getValue( ) );
	}
	
	
	/**
	 * Scales all the costs by the specified weight
	 * 
	 * @param w The scale weight
	 * @return The new state value
	 */
	public void scale( double w ) {
		for( ObjectiveValue o : objectives )
			o.scale( w );
	}
	
	/**
	 * @return The state value as a double array
	 */
	public abstract double[] toDoubleArray( );
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		if( objectives.length == 0 ) return "Empty";
		
		final double[] values = new double[ objectives.length ];
		for( int i = 0; i < objectives.length; i++ )
			values[i] = objectives[i].getValue( );
		
		return toString( values );
	}
	
	/**
	 * Creates a value string with the specified values
	 * 
	 * @param values The values to include
	 * @return The string
	 */
	protected String toString( double[] values ) {
		final NumberFormat df = Util.dec( );
		double total = 0;
		String str = "";
		for( int i = 0; i < values.length; i++ ) {
			str += df.format( values[i] ) + " + ";
			total += values[i];
		}
		str = str.substring( 0, str.length( ) - 3 ) + " = " + df.format( total );
		
		return str;
	}
}
