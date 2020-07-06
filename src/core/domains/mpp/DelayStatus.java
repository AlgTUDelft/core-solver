/**
 * @file DelayStatus.java
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
 * @date         4 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains.mpp;


/**
 * Enumerates delay states for tasks
 *
 * @author Joris Scharpff
 */
public enum DelayStatus {
	/** Pending delay, not sure yet */
	Pending,
	/** The task is delayed */
	Delayed,
	/** The task is not delayed */
	NotDelayed;
	
	/**
	 * @return True if the delay status is delayed
	 */
	public boolean isDelayed( ) {
		return (this == Delayed);
	}

	/**
	 * @return True if the status is delayed or pending
	 */
	public boolean isPossiblyDelayed( ) {
		return (this == Pending | this == Delayed);
	}
	
	/**
	 * Returns the correct delay status from a boolean value
	 * 
	 * @param bool The boolean
	 * @return (bool ? Delayed : NotDelayed)
	 */
	public static DelayStatus fromBool( boolean bool ) {
		return (bool ? DelayStatus.Delayed : DelayStatus.NotDelayed );
	}
	
	/**
	 * Returns the delay status that corresponds to an outcome
	 * 
	 * @param outcome The action outcome
	 * @return The delay status
	 */
	public static DelayStatus fromOutcome( MPPOutcome outcome ) {
		return fromBool( outcome.isDelayed( ) );
	}
	
	/**
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString( ) {
		switch( this ) {
			default:
			case Pending: return "P";
			case Delayed: return "D";
			case NotDelayed: return "N";
		}
	}
}
