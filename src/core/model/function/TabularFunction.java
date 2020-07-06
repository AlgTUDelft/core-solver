/**
 * @file TabularFunction.java
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

import java.text.NumberFormat;

import core.exceptions.IIOException;
import core.util.Util;

/**
 * Function that uses a table to retrieve its values
 *
 * @author Joris Scharpff
 */
public class TabularFunction extends Function {
	/** The value table */
	protected final double[] values;
	
	/**
	 * Creates a new tabular function
	 * 
	 * @param values The table values
	 */
	public TabularFunction( double[] values ) {
		this.values = new double[ values.length ];
		for( int i = 0; i < values.length; i++ )
			this.values[ i ] = values[ i ];
	}

	/**
	 * Creates a new tabular function
	 * 
	 * @param values The table values
	 */
	public TabularFunction( Number... values ) {
		this.values = new double[ values.length ];
		for( int i = 0; i < values.length; i++ )
			this.values[ i ] = values[ i ].doubleValue( );
	}

	/**
	 * Deserialises a table function from string
	 * 
	 * @param serialised The serialised table
	 * @throws IIOException if deserialisation failed
	 */
	public TabularFunction( String serialised ) throws IIOException {
		final String[] v = serialised.split( ";" );
		values = new double[ v.length ];
		
		// parse values
		for( int i = 0; i < v.length; i++ ) {
			try {
				values[ i ] = Double.parseDouble( v[ i ] );
			} catch( NumberFormatException nfe ) {
				throw new IIOException( "Failed to parse table function '" + serialised + "'", nfe );
			}
		}
	}
	
	/**
	 * @see core.model.function.Function#eval(int, int)
	 */
	@Override
	public double eval( int week, int horizon ) {
		return values[ week ];
	}

	/**
	 * @see core.model.function.Function#getFunction()
	 */
	@Override
	public String getFunction( ) {
		String str = "{";
		for( int i = 0; i < values.length; i++ )
			str += values[i] + ( i < values.length - 1 ? "," : "}" );
		return str;
	}
	
	/**
	 * @see core.model.function.Function#copy(double)
	 */
	@Override
	public Function copy( double weight ) {
		final double[] v = new double[ values.length ];
		for( int i = 0; i < v.length; i++ )
			v[i] = values[i] * weight;
		
		return new TabularFunction( v );
	}
	
	/**
	 * @see core.model.function.Function#doSerialise()
	 */
	@Override
	public String doSerialise( ) {
		String str = "";
		for( int i = 0; i < values.length; i++ )
			str += "" + values[ i ] + ";";
		return str.substring( 0, str.length( ) - 1 );
	}
	
	/**
	 * @see core.model.function.Function#toString()
	 */
	@Override
	public String toString( ) {
		final NumberFormat df = Util.dec( 2 );
		String str = "{ " + (values.length > 0 ? df.format( values[0] ) : "");
		for( int i = 1; i < values.length; i++ )
			str += ", " + df.format( values[i] );
		str += " }";
		return str;
	}
}
