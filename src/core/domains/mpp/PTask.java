/**
 * @file PTask.java
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
 * @date         21 feb. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains.mpp;




/**
 * Container for a task + realisation
 * 
 * @author Joris Scharpff
 */
public class PTask {
	/** The task */
	protected final Task task;
	
	/** The start time */
	protected final int starttime;
	
	/** The delay realisation */
	protected final boolean delay;
	
	/**
	 * Creates a new PTask
	 * 
	 * @param task The task
	 * @param starttime The start time of the task
	 * @param delay The delay realisation
	 */
	public PTask( Task task, int starttime, boolean delay ) {
		this.task = task;
		this.starttime = starttime;
		this.delay = delay;
	}
	
	/**
	 * @return The task
	 */
	public Task getTask( ) {
		return task;
	}
	
	/**
	 * Computes the total revenue for this task
	 * 
	 * @param horizon The instance horizon
	 * @return The task revenue
	 */
	public double getProfit( int horizon ) {
		return task.getRevenue( ) - getCost( horizon );
	}
	
	/**
	 * Computes the cost of the task
	 * 
	 * @param horizon The instance horizon
	 * @return The cost of this task
	 */
	public double getCost( int horizon ) {
		return task.getCost( starttime, horizon, delay );
	}
	
	/**
	 * @return The duration of this task given its realisation
	 */
	public int getDuration( ) {
		return task.getDuration( ) + (delay ? task.getDelayDur( ) : 0);
	}
	
	/**
	 * @return The start time
	 */
	public int getStartTime( ) {
		return starttime;
	}
	
	/**
	 * @return The end time of this planned task
	 */
	public int getEndTime( ) {
		return starttime + getDuration( ) - 1;
	}
	
	/**
	 * @return True if this realisation is delayed
	 */
	public boolean isDelayed( ) {
		return delay;
	}
	
	/**
	 * @return The probability of this realisation
	 */
	public double getProbability( ) {
		return (delay ? task.getDelayProb( ) : 1 - task.getDelayProb( ));
	}
	
	/**
	 * Checks if the planned task overlaps with me
	 * 
	 * @param ptask The planned task
	 * @return True if the task is executed within my execution period
	 */
	public boolean overlaps( PTask ptask ) {
		if( ptask.getStartTime( ) >= getStartTime( ) && ptask.getStartTime( ) <= getEndTime( ) ) return true;
		if( ptask.getStartTime( ) >= getEndTime( ) && ptask.getEndTime( ) <= getEndTime( ) ) return true;
		
		return false;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof PTask) ) return false;
		final PTask pt = (PTask) obj;
		
		return task.equals( pt.task ) && starttime == pt.starttime && delay == pt.delay;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return task.toString( ) + "|" + starttime + "-" + (starttime + getDuration( ) - 1);  
	}
}