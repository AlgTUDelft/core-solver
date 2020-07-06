/**
 * @file ConstFunction.java
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

import java.text.NumberFormat;

import core.exceptions.IIOException;
import core.util.Util;

/**
 * Constant cost function
 *
 * @author Joris Scharpff
 */
public class ConstFunction extends Function {
	/** The constant */
	protected double constval;
	
	/**
	 * Creates the constant cost function
	 * 
	 * @param constval The value of the constant
	 */
	public ConstFunction( double constval ) {
		this.constval = constval;
	}
	
	/**
	 * Creates the cost function from a serialised string
	 * 
	 * @param serialised The serialised string
	 * @throws IIOException if the deserialisation failed
	 */
	public ConstFunction( String serialised ) throws IIOException {
		try {
			this.constval = Double.parseDouble( serialised );
		} catch( NumberFormatException nfe ) {
			throw new IIOException( "Failed to deserialise constant function", nfe );
		}
	}
	
	/**
	 * @see core.model.function.Function#eval(int, int)
	 */
	@Override
	public double eval( int week, int horizon ) {
		return constval;
	}
	
	/**
	 * @see core.model.function.Function#getFunction()
	 */
	@Override
	public String getFunction( ) {
		final NumberFormat df = Util.dec( 2 );
		return df.format( constval );
	}
	
	/**
	 * @see core.model.function.Function#copy(double)
	 */
	@Override
	public Function copy( double weight ) {
		return new ConstFunction( constval * weight );
	}
	
	/**
	 * @see core.model.function.Function#doSerialise()
	 */
	@Override
	public String doSerialise( ) {
		return "" + constval;
	}
}
