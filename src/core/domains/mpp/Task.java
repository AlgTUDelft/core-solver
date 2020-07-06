/**
 * @file Task.java
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
package core.domains.mpp;

import java.text.NumberFormat;

import core.model.function.ConstFunction;
import core.model.function.Function;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.policy.Outcome;
import core.util.Util;

/**
 * Representation of a task.
 *
 * @author Joris Scharpff
 */
public class Task extends Action {
	/** The revenue for completing the task */
	protected final double revenue;
	
	/** The additional duration when the task is delayed */
	protected final int delduration;
	
	/** In case of continue, this is the task we are continuing */
	protected Task conttask;

	/**
	 * Creates a continue task for the agent
	 * 
	 * @param task The task we are continuing
	 * @return The NoOp
	 */
	public static Task CONT( Task task ) {
		return new Task( task );
	}
	
	/**
	 * Creates a NOOP task for the agent
	 * 
	 * @param agent The agent
	 * @return The NoOp
	 */
	public static Task NoOp( Agent agent ) {
		return new Task( agent, -1, 0, new ConstFunction( 0 ), 1 );
	}
	
	/**
	 * Create a new task that has no probability of delay
	 * 
	 * @param agent The agent to create the task for
	 * @param ID The task ID
	 * @param revenue The revenue for completing the task
	 * @param costfunc The cost function
	 * @param duration The task duration in week
	 */
	public Task( Agent agent, int ID, double revenue, Function costfunc, int duration ) {
		this( agent, ID, revenue, costfunc, duration, 0.0, 0 );
	}
	
	/**
	 * Creates a new task with probability of delay
	 * 
	 * @param agent The agent to create the task for
	 * @param ID The task ID
	 * @param revenue The revenue for completing the task
	 * @param costfunc The task cost function
	 * @param duration The regular duration in weeks
	 * @param delprob The probability of delay [0,1]
	 * @param deldur The additional duration if delayed
	 */
	public Task( Agent agent, int ID, double revenue, Function costfunc, int duration, double delprob, int deldur ) {
		super( agent, ID, ID == -1 ? "NoOp" : "Task " + ID, costfunc, duration );

		this.revenue = revenue;
		this.delduration = (delprob > 0 ? deldur : 0);
		this.conttask = null;
		
		// set multiple outcomes if it can be delayed
		if( deldur > 0 && delprob > 0.0 )
			setOutcomes( new Outcome[] { new MPPOutcome( false, 1 - delprob ), new MPPOutcome( true, delprob ) } );
		else
			setOutcome( new MPPOutcome( false, 1.0 ) );
	}
	
	/**
	 * Copies an existing task and sets durations according to the realisation
	 * 
	 * @param task The task to copy
	 * @param delay The delay realisation
	 */
	public Task( Task task, DelayStatus delay ) {
		super( task.agent, task.ID, task.desc, task.reward, (delay == null ? task.duration : task.duration + (delay.isDelayed( ) ? task.delduration : 0 )) );

		this.revenue = task.revenue;
		this.delduration = (delay == null ? task.delduration : 0);
		this.conttask = null;
	}
	
	/**
	 * Constructs a new continue action
	 * 
	 * @param task The task to continue
	 */
	private Task( Task task ) {
		this( task.getAgent( ), -2, 0, new ConstFunction( 0 ), 1, 0, 0 );
		
		this.conttask = task;
	}
	
	/**
	 * @return True if this action is a NOOP 
	 */
	public boolean isNoOp( ) {
		return (ID == -1);
	}
	
	/**
	 * @return True if this action is a continue action
	 */
	public boolean isContinue( ) {
		return (ID == -2);
	}
	
	/**
	 * @return The continue task
	 */
	public Task getContinueTask( ) {
		return conttask;
	}
	
	/**
	 * @return The task or the continue task if the task is a continue action
	 */
	public Task getRealTask( ) {
		return isContinue( ) ? getContinueTask( ) : this;
	}
	
	/**
	 * @return True if the task can be delayed
	 */
	public boolean canDelay( ) {
		return delduration > 0;
	}
		
	/**
	 * @return The revenue for completing the task
	 */
	public double getRevenue( ) {
		return revenue;
	}

	/**
	 * Returns the total task duration
	 * 
	 * @param delay Include delay time
	 * @return The task duration optionally including delay
	 */
	public int getDuration( boolean delay ) {
		return duration + (delay ? delduration : 0);
	}
	
	/**
	 * @return The delay probability of the task
	 */
	public double getDelayProb( ) {
		return canDelay( ) ? outcomes.get( 1 ).getProbability( ) : 0;		
	}
	
	/**
	 * @return The additional duration when delayed
	 */
	public int getDelayDur( ) {
		return delduration;
	}
	
	/**
	 * Computes the cost for the task when starting at the given time
	 * 
	 * @param starttime The time it starts at
	 * @param horizon The problem horizon
	 * @param delay Include the delay duration?
	 * @return The total cost of the task over the specified period
	 */
	public double getCost( int starttime, int horizon, boolean delay ) {
		// compute end time
		final int endtime = starttime + duration + (delay ? delduration : 0);
		
		// sum cost over that time
		double cost = 0;
		for( int t = starttime; t < endtime; t++ )
			cost += getStepCost( t, horizon );
		
		return cost;
	}
	
	/**
	 * Computes the cost of executing the task at the specified time step
	 * 
	 * @param time The time step
	 * @param horizon The plan horizon
	 * @return The cost of executing the task at the given time
	 */
	public double getStepCost( int time, int horizon ) {
		return -reward.eval( time, horizon );
	}
		
	/**
	 * Get full task ID including agent
	 *
	 * @return String AiTj with i = agent and j = task ID or NOOP for the NoOp
	 * task
	 */
	public String getFullID( ) {
		if( isNoOp( ) ) return "NOP" + agent.ID;
		if( isContinue( ) ) return "C" + getContinueTask( ).getFullID( );
		return "A" + agent.ID + "T" + ID;
	}
	
	/**
	 * @return The short name of the task
	 */
	public String toLongString( ) {
		final NumberFormat df = Util.dec( 2 );
		final NumberFormat pf = Util.perc( );
		return "[Agent " + agent.ID + "] Task " + ID + ": P = " + df.format( revenue ) + ", C(t) = " + reward.toString( ) + ", d = " + duration + ", d' = " + delduration + " (" + pf.format( getDelayProb( ) ) + ")"; 
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "[" + getFullID( ) + "]";
	}
	
	/**
	 * Tasks equal if they are both no-ops or their agents & IDs match
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof Task) ) return false;
		final Task t = (Task) obj;
		
		if( isNoOp( ) && t.isNoOp( ) ) return true;
		if( isContinue( ) && t.isContinue( ) ) {
			if( !getContinueTask( ).equals( t.getContinueTask( ) ) ) {
				System.out.println( getContinueTask( ).getDescription( ) );
				System.out.println( t.getContinueTask( ).getDescription( ) );
				return false;
			}
		}
		
		return super.equals( t );
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return getFullID( ).hashCode( );
	}
}
