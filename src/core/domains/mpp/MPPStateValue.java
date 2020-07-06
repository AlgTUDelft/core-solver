/**
 * @file MPPStateValue.java
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
package core.domains.mpp;

import core.domains.ObjectiveValue;
import core.domains.StateValue;


/**
 * Value of an MPP state
 *  
 * @author Joris Scharpff
 */
public class MPPStateValue extends StateValue {
	/**
	 * Creates a new (empty) MPPStateValue
	 * 
	 */
	public MPPStateValue( ) {
		this( 0, 0, 0 );
	}
	
	/**
	 * Creates a new MPP state value
	 * 
	 * @param revenue The revenue
	 * @param cost The maintenance costs
	 * @param network The network cost
	 */
	public MPPStateValue( double revenue, double cost, double network ) {
		super( new ObjectiveValue[] {
				new ObjectiveValue( "Revenue", revenue ),
				new ObjectiveValue( "Costs", cost ),
				new ObjectiveValue( "Network", network )
		} );
	}
	
	/**
	 * @see core.domains.StateValue#copy()
	 */
	@Override
	public StateValue copy( ) {
		return new MPPStateValue( getRevenue( ), getCost( ), getNetworkReward( ) );
	}
	
	/**
	 * @return The total revenue due to executed tasks
	 */
	public double getRevenue( ) {
		return objectives[0].getValue( );
	}
	
	/**
	 * @return The total maintenance costs
	 */
	public double getCost( ) {
		return objectives[1].getValue( );
	}
	
	/**
	 * @return The total network cost
	 */
	public double getNetworkReward( ) {
		return objectives[2].getValue( );
	}
	
	/**
	 * Creates a 2D double array of the values
	 * 
	 * @return The values as double array
	 */
	@Override
	public double[] toDoubleArray( ) {
		return new double[ ] { getRevenue( ) + getCost( ), getNetworkReward( ) };
	}
	
	/**
	 * @see core.domains.StateValue#toString()
	 */
	@Override
	public String toString( ) {
		if( getRevenue( ) == 0 ) {
			return super.toString( new double[] { getCost( ), getNetworkReward( ) } );
		} else {
			return super.toString( );
		}
	}
}
