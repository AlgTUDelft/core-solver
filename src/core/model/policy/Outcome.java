/**
 * @file Outcome.java
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
 * @date         19 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.policy;

import java.text.NumberFormat;

import core.util.Util;


/**
 * Outcome of an action
 * 
 * @author Joris Scharpff
 */
public abstract class Outcome {
	/** The probability of this outcome */
	protected final double prob;
	
	/**
	 * Creates an action outcome
	 * 
	 * @param prob The probability
	 * @throws IllegalArgumentException if the probability is not valid
	 */
	public Outcome( double prob ) {
		if( prob < 0 || prob > 1 ) throw new IllegalArgumentException( "Invalid probability for outcome: " + prob );
		this.prob = prob;
	}
	
	/**
	 * @return The outcome probability
	 */
	public double getProbability( ) {
		return prob;
	}
	
	/**
	 * @return The human-readable description of this outcome, empty by default
	 */
	public abstract String getDescription( );
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		final NumberFormat pf = Util.perc( );
		return "O: " + getDescription( ) + "(" + pf.format( prob ) + ")";
	}
	
	/**
	 * Compares two outcomes, this enforces that all outcomes implement an equals function
	 * 
	 * @param out The outcome
	 * @return True if the two outcomes are equal
	 */
	protected abstract boolean equalsOutcome( Outcome out );
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof Outcome) ) return false;
		
		return this.equalsOutcome( (Outcome) obj );
	}
}
