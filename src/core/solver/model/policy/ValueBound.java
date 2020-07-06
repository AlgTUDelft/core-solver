/**
 * @file CRGBound.java
 * @brief [brief description]
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2016 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         17 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.model.policy;

import core.domains.StateValue;
import core.util.Util;


/**
 * Container for lower and upper return bounds, immutable to prevent shared
 * access/update issues
 * 
 * @author Joris Scharpff
 */
public class ValueBound {
	/** The lower bound */
	protected final StateValue lb;
	
	/** The upper bound */
	protected final StateValue ub;
	
	/**
	 * Creates an empty bound, used when building bounds
	 */
	private ValueBound( ) {
		lb = null;
		ub = null;
	}
	
	/** @return A new, empty bound object */
	public static ValueBound emptyBound( ) {
		return new ValueBound( );
	}
	
	/**
	 * Copies the bound
	 * 
	 * @param bound The bound to copy
	 */
	private ValueBound( ValueBound bound ) {
		this( bound.lb, bound.ub );
	}
	
	/**
	 * Creates a bounds object for the single state value
	 *
	 * @param sv The state value containing the expected return
	 */
	public ValueBound( StateValue sv ) {
		this( sv, sv );
	}
	
	/**
	 * Creates a new bound from the previous bound with the value added to it
	 * 
	 * @param bound The previous bound
	 * @param value The value to add
	 */
	public ValueBound( ValueBound bound, StateValue value ) {
		this( bound );
		
		lb.add( value );
		ub.add( value );
	}
	
	
	/**
	 * Creates a new bound with specified lower and upper bound, StateValue
	 * objects are copied
	 * 
	 * @param lb The lower bound
	 * @param ub The upper bound
	 */
	private ValueBound( StateValue lb, StateValue ub ) {
		assert lb != null && ub != null : "Cannot create empty bound";

		this.lb = lb.copy( );
		this.ub = ub.copy( );		
	}
	
	/**
	 * @return The lower bound
	 */
	public StateValue getLower( ) {
		return lb;
	}
	
	/**
	 * @return The upper bound
	 */
	public StateValue getUpper( ) {
		return ub;
	}
	
	/**
	 * Updates the bound by setting the LB (resp. UB) to the minimum (maximum) of
	 * both bound object values
	 * 
	 * @param bound The bound to update with
	 * @return The updated bound object
	 */
	public ValueBound update( ValueBound bound ) {
		assert bound.lb != null && bound.ub != null : "Empty bounds";

		
		final StateValue L = (lb == null || lb.getTotal( ) > bound.lb.getTotal( )) ? bound.lb : lb;
		final StateValue U = (ub == null || ub.getTotal( ) < bound.ub.getTotal( )) ? bound.ub : ub;
		
		return new ValueBound( L, U );
	}
	
	/**
	 * Creates a new bound that contains the sum of this and the other bound's L
	 * and U values
	 * 
	 * @param bound The bound to add
	 * @return A new bound with L = this.L + bound.L and U = this.U + bound.U 
	 */
	public ValueBound add( ValueBound bound ) {
		assert bound.lb != null && bound.ub != null : "Empty bounds";

		// not initialised?
		if( lb == null || ub == null ) {
			// return the other bound
			assert lb == null && ub == null : "Cannot have only one of the bounds initialised";
			return new ValueBound( bound );
		} 
		
	
		// add the bound values
		final StateValue L = bound.lb.copy( );
		L.add( lb );
		final StateValue U = bound.ub.copy( );
		U.add( ub );
			
		return new ValueBound( L, U );
	}
	
	/**
	 * Scales the bound by the specified factor
	 * 
	 * @param factor The scale factor
	 * @return The scaled bound
	 */
	public ValueBound scale( double factor ) {
		assert lb != null && ub != null : "Empty bounds";

		final StateValue L = lb.copy( );
		L.scale( factor );
		final StateValue U = ub.copy( );
		U.scale( factor );
		
		return new ValueBound( L, U );
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "[" + Util.dec( ).format( lb.getTotal( ) ) + ", " + Util.dec( ).format( ub.getTotal( ) ) + "]";
	}
}
