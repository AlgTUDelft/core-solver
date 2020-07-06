/**
 * @file StateReward.java
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
 * State value with only a single reward
 * 
 * @author Joris Scharpff
 */
public class StateReward extends StateValue {
	/**
	 * Creates a new state reward value
	 * 
	 * @param reward The initial reward
	 */
	public StateReward( double reward ) {
		super( new ObjectiveValue( "Reward", reward ) );
	}

	/**
	 * @see core.domains.StateValue#copy()
	 */
	@Override
	public StateValue copy( ) {
		return new StateReward( getReward( ) );
	}
	
	/**
	 * Sets the reward
	 * 
	 * @param reward The new reward
	 */
	public void setReward( double reward ) {
		objectives[0].setValue( reward );
	}
	
	/**
	 * @return The state reward
	 */
	public double getReward( ) {
		return objectives[0].getValue( );
	}
	
	/**
	 * @see core.domains.StateValue#toDoubleArray()
	 */
	@Override
	public double[] toDoubleArray( ) {
		return new double[] { getReward( ) };
	}
}
