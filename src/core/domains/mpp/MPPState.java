/**
 * @file MPPState.java
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
package core.domains.mpp;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.domains.State;
import core.domains.StateValue;
import core.exceptions.InfeasibleStateException;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.policy.JointAction;
import core.model.policy.RealisedJAction;
import core.util.Util;


/**
 * Global state of the MPP
 * 
 * @author Joris Scharpff
 */
public class MPPState extends State {
	/**
	 * Creates a new state
	 * 
	 * @param inst The MPP instance
	 * @param time The global state time
	 * @param prob The state probability
	 * @param plan The history so far (as a plan)
	 */
	private MPPState( MPPInstance inst, int time, double prob, MPPPlan plan ) {
		super( inst, time, prob, plan );
	}
	
	/**
	 * Creates new initial MPP state
	 * 
	 * @param inst The MPP instance
	 */
	public MPPState( MPPInstance inst ) {
		this( inst, 0, 1.0, new MPPPlan( inst ) );
	}
	
	/**
	 * @see core.domains.State#copy()
	 */
	@Override
	public MPPState copy( ) {
		final MPPState s = new MPPState( getI( ), getTime( ), getProbability( ), getHistory( ).copy( ) );
		s.copyState( this );
		
		return s;
	}
	
	/**
	 * Builds a state from a given history
	 * 
	 * @param instance The instance this state is for
	 * @param history The plan describing the history
	 * @param time Last time point to use from history
	 */
	public MPPState( MPPInstance instance, MPPPlan history, int time ) {
		// create new initial state
		this( instance );
		this.time = time;
		
		// build state incrementally from history up to the time
		for( int t = 0; t < time; t++ ) {
			// get tasks active at this time
			final RealisedJAction action = history.getRealised( t );
			
			for( Agent agent : I.getAgents( ) ) {
				// get the task and its outcome
				final Task task = (Task)action.getAction( agent );
				final MPPOutcome outcome = (MPPOutcome) action.getOutcome( agent );

				// check what task this is
				if( !task.isContinue( ) && !task.isNoOp( )) {
					// add the task if it starts at this time
					if( history.getTime( task ) == t  ) {
						getHistory( ).add( task, t, DelayStatus.fromOutcome( outcome ) );
						probabilty *= outcome.getProbability( );
					}
				}
			}
		}
	}	

	/**
	 * @see core.domains.State#canExecute(core.model.policy.JointAction)
	 */
	@Override
	public boolean canExecute( JointAction ja ) {
		if( !super.canExecute( ja ) ) return false;

		// check if the add operation is successful for all tasks when delayed
		copy( );
		for( Agent a : ja.getAgents( ) ) {
			final Task t = (Task) ja.getAction( a );
			
			if( !canComplete( t ) ) return false;
		}
		
		return true;
	}

	/**
	 * @see core.domains.State#getExecuting(int)
	 */
	@Override
	public Set<Action> getExecuting( int time ) {
		final Set<Action> executing = new HashSet<Action>( );
		for( Task t : getHistory( ).getExecuting( time ) )
			executing.add( t );
		
		return executing;
	}
	
	/**
	 * @see core.domains.State#getI()
	 */
	@Override
	public MPPInstance getI( ) {
		return (MPPInstance)super.getI( );
	}
		
	/**
	 * @return The plan that contains the state's history
	 */
	@Override
	public MPPPlan getHistory( ) {
		return (MPPPlan)super.getHistory( );
	}
	
	/**
	 * @see core.domains.State#getValue()
	 */
	@Override
	public StateValue getValue( ) throws InfeasibleStateException {
		if( !isFeasible( ) ) throw new InfeasibleStateException( this );
		
		final MPPStateValue sv = getHistory( ).getValue( );
		return new MPPStateValue( sv.getRevenue( ), sv.getCost( ), sv.getNetworkReward( ) );
	}
	
	/**
	 * Retrieves the profits obtained in the state
	 * 
	 * @return The sum of profits minus cost in this state
	 */
	public double getProfits( ) {
		return getHistory( ).getProfit( );
	}
			
	/**
	 * Checks if the agent is 'idle' in the current state given the time. An
	 * agent is not idle if his current time in the state is later then the given
	 * time.
	 * 
	 * @param agent The agent to check
	 * @param time The time to check whether an agent is idle
	 * @return True if the time variable in the state is equal or smaller than
	 * time
	 */
	public boolean isIdle( Agent agent, int time ) {
		return getTask( agent, time ) == null;
	}
	
	/**
	 * Returns true if the task is determined to be delayed in the state
	 * 
	 * @param task The task to check
	 * @return True if the task delay status is delayed, false otherwise
	 */
	public boolean isDelayed( Task task ) {
		return getHistory( ).isDelayed( task );
	}
	
	/**
	 * In the MPP domain actions can be executed only once, return only the
	 * actions that have not been planned yet
	 * 
	 * @see core.domains.State#getAvailableActions(core.model.policy.Agent)
	 */
	@Override
	public Set<Action> getAvailableActions( Agent agent ) {
		final Set<Action> actions = new HashSet<Action>( );
		
		// check if the agent is busy with an action
		final Task task = getHistory( ).getTask( agent, time );
		if( task != null ) {
			actions.add( Task.CONT( task ) );
			return actions;
		}
		
		// determine non-completed actions
		actions.add( Task.NoOp( agent ) );
		for( Action act : agent.getActions( ) ) {
			if( !getHistory( ).isPlanned( (Task)act ) && canComplete( (Task)act ) )
				actions.add( act );
		}
				
		return actions;
	}
	
	/**
	 * Checks what task the agent is performing at the specified time
	 * 
	 * @param agent The agent
	 * @param time The time
	 * @return Null if the agent has not yet decided, NoOp if it is performing
	 * the NoOp task or the current task otherwise
	 */
	public Task getTask( Agent agent, int time ) {
		// return the planned action
		return getHistory( ).getTask( agent, time );
	}
	
	/**
	 * Returns the number of completed tasks
	 * 
	 * @return The total number of completed tasks
	 */
	public int getNumCompleted( ) {
		return getHistory( ).getNumCompleted( getHorizon( ) );
	}
	
	/**
	 * Returns the set of tasks (of all agents) that can still be added to the
	 * current state
	 * 
	 * @return The set of tasks or an empty array if there are no more
	 */
	public Set<Task> getRemaining( ) {
		final Set<Task> rem = new HashSet<Task>( getI( ).getTasks( ) );
		rem.removeAll( getHistory( ).getPlanned( ) );
		return rem;
	}
	
	/**
	 * Returns the set of tasks that are still unplanned for the agent
	 * 
	 * @param agent The agent
	 * @return The set of tasks that the agent can still plan
	 */
	public Task[] getRemaining( Agent agent ) {
		final List<Task> tasks = new ArrayList<Task>( );
		for( Task t : getRemaining( ) )
			if( t.getAgent( ).equals( agent ) ) tasks.add( t );
		
		return tasks.toArray( new Task[] { } );
	}
	
	/**
	 * Checks if a task can be completed from the current state
	 * 
	 * @param task The task
	 * @return True if there is enough time left
	 */
	protected boolean canComplete( Task task ) {
		return getTime( ) + task.getDuration( true ) <= getHorizon( );
	}
	
	/**
	 * @see core.domains.State#isTerminal()
	 */
	@Override
	public boolean isTerminal( ) {
		// completed if all tasks are selected
		if( getHistory( ).getPlanned( ).size( ) == getI( ).getA( ) ) return true;
		
		// check horizon
		return super.isTerminal( );
	}
	
	/**
	 * @see core.domains.State#isFeasible()
	 */
	@Override
	public boolean isFeasible( ) {
		// check if all tasks can be completed still
		if( getI( ).mustCompleteAll( ) ) {
			for( Agent a : getI( ).getAgents( ) ) {
				int reqtime = 0;
				
				// compute required time still
				for( Action act : a.getActions( ) ) {
					final Task t = (Task) act;
					if( !getHistory( ).isPlanned( t ) )
						reqtime += t.getDuration( true );					
				}
				
				if( getTime( ) + reqtime > getHorizon( ) )
					return false;
			}
		}
		
		return super.isFeasible( );
	}
		
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		final NumberFormat perc = Util.perc( 3 );
		String str = "[";
		for( Task t : getI( ).getTasks( ) ) {
			final PTask pt = getHistory( ).getPlanned( t );
			if( pt != null ) {
				str += pt.toString( ) + ";";
			}
		}
		return str + "], t = " + time + " (" + perc.format( getProbability( ) ) + ")";
	}	
	
	/**
	 * @see core.domains.State#computeStateHash()
	 */
	@Override
	public int computeStateHash( ) {
		return getHistory( ).getPlanned( ).hashCode( ) * getTime( );
	}

	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof MPPState) ) return false;
		final MPPState s = (MPPState)obj;
		
		// compare super state
		return super.equals( s );
	}	
}
