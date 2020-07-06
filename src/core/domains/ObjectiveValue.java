/**
 * @file ObjectiveValue.java
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
 * @date         20 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains;


/**
 * Represents one objective in the state value
 * 
 * @author Joris Scharpff
 */
public class ObjectiveValue {
	/** The objective name */
	protected final String name;
	
	/** The objective value */
	protected double value;
	
	/**
	 * Creates a new objective
	 * 
	 * @param name The objective name
	 */
	public ObjectiveValue( String name ) {
		this( name, 0 );
	}
	
	/**
	 * Creates a new objective with a specified initial value
	 * 
	 * @param name The objective name
	 * @param value The initial value
	 */
	public ObjectiveValue( String name, double value ) {
		this.name = name;
		setValue( value );
	}
	
	/**
	 * Sets the value of the objective
	 * 
	 * @param value The value
	 */
	public void setValue( double value ) {
		this.value = value;
	}
	
	/**
	 * @return The value
	 */
	public double getValue( ) {
		return value;
	}
	
	/**
	 * Adds the specified amount
	 * 
	 * @param amount The amount to add
	 */
	public void add( double amount ) {
		value += amount;
	}
	
	/**
	 * Scales the objective value by the specified weight
	 * 
	 * @param w The scale weight
	 */
	public void scale( double w ) {
		value *= w;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof ObjectiveValue) ) return false;
		
		return name.equals( ((ObjectiveValue)obj).name );
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return name.hashCode( );
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return name + ": " + value;
	}
}
