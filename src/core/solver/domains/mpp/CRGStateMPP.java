/**
 * @file CRGStateMPP.java
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
 * @date         16 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.domains.mpp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.domains.mpp.PTask;
import core.domains.mpp.Task;
import core.model.policy.Agent;
import core.solver.model.crg.CRGState;


/**
 * Local agent state of the MPP
 * 
 * @author Joris Scharpff
 */
public class CRGStateMPP extends CRGState {
	/** The current task */
	protected Task ctask;
	
	/** The state history */
	protected final Map<Task, PTask> hist;
	
	/**
	 * Creates a new MPP state
	 * 
	 * @param agent The agent
	 * @param time The current state time
	 */
	public CRGStateMPP( Agent agent, int time ) {
		super( agent, time );
		
		hist = new HashMap<Task, PTask>( agent.getNumActions( ) );
		ctask = null;
	}
		
	/**
	 * Creates a copy of the specified state and increases the state time by one
	 * 
	 * @param state The state to copy
	 */
	private CRGStateMPP( CRGStateMPP state ) {
		this( state, null, false );
	}		
	
	/**
	 * Creates a copy of the specified state and adds the task to the history
	 * 
	 * @param state The state to copy
	 * @param newtask The task to add to the history (null for NoOp / continue)
	 * @param delayed 
	 */
	private CRGStateMPP( CRGStateMPP state, Task newtask, boolean delayed ) {
		this( state.getAgent( ), state.getTime( ) + 1 );
		
		// copy the history and current task
		hist.putAll( state.hist );
		ctask = state.ctask;
		
		// add new task if not null
		if( newtask == null ) return;
		assert getCurrentTask( ) == null : "Cannot start new action " + newtask + ", still busy with " + getCurrentTask( );
		final PTask pt = new PTask( newtask, getTime( ) - 1, delayed );
		hist.put( newtask, pt );
		
		// and set it as the current task if it requires at least one more time step
		if( pt.getDuration( ) > 1 )
			setCurrentTask( newtask );
	}	
	
	/**
	 * Creates a new state that results from executing a NoOp
	 * 
	 * @return The new state
	 */
	protected CRGStateMPP execNoOp( ) {
		assert isIdle( ) : "Cannot execute NoOp, the agent is still busy with " + getCurrentTask( );
		
		return new CRGStateMPP( this );
	}
	
	/**
	 * Creates a new state that result from continuing the action
	 * 
	 * @return The new state
	 */
	protected CRGStateMPP execContinue( ) {
		assert !isIdle( ) : "There is no current action to continue";
		
		// create new state
		final CRGStateMPP newstate = new CRGStateMPP( this );
		
		// check if the current task should now be completed
		final PTask pt = hist.get( getCurrentTask( ) );
		if( pt.getEndTime( ) < newstate.getTime( ) )
			newstate.clearCurrentTask( );
		
		return newstate;
	}
	
	/**
	 * Creates a set of all new states that can be encountered when executing the
	 * given task from this state
	 * 
	 * @param task The task that is started
	 * @return The new states
	 */
	protected Collection<CRGState> execTask( Task task ) {
		assert isIdle( ) : "Cannot execute " + task + ", the agent is still busy with " + getCurrentTask( );
		
		final Set<CRGState> newstates = new HashSet<CRGState>( task.canDelay( ) ? 2 : 1 );
		
		// add non-delayed execution
		newstates.add( new CRGStateMPP( this, task, false ) );
		
		// and delayed if possible
		if( task.canDelay( ) ) newstates.add( new CRGStateMPP( this, task, true ) );
		
		return newstates;
	}
	
	/**
	 * Checks if the specified task has been completed
	 * 
	 * @param task The task
	 * @return True iff the task has been completed
	 */
	public boolean hasCompleted( Task task ) {
		assert task.getAgent( ).equals( getAgent() ) : "Task does not belong to the agent";
		
		// check if currently working on this task
		if( !isIdle( ) && getCurrentTask( ).equals( task ) ) return false;
		
		return hist.containsKey( task );
	}
	
	/**
	 * @return True iff the agent is idle
	 */
	protected boolean isIdle( ) {
		return ctask == null;
	}
	
	/**
	 * @return The current task, null if the agent is idle
	 */
	protected Task getCurrentTask( ) {
		return ctask;
	}
	
	/**
	 * Sets the current task
	 * 
	 * @param task The new current task
	 */
	private void setCurrentTask( Task task ) {
		assert ctask == null : "Cannot set " + task + " as current because " + getCurrentTask( ) + " is still busy";
		ctask = task;
	}
	
	/**
	 * Clears the current task variable
	 */
	private void clearCurrentTask( ) {
		assert ctask != null : "Task was already cleared";
		ctask = null;
	}
	
	/**
	 * Returns the stored start time and outcome for the task
	 * 
	 * @param task The task
	 * @return The PTask
	 */
	protected PTask getPTask( Task task ) {
		assert hist.containsKey( task ) : "Task not in state history";
		return hist.get( task );
	}

	/**
	 * @return The planned task map
	 * FIXME: delete this!
	 */
	public Map<Task, PTask> getPlanned( ) {
		return hist;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "S_" + getAgent( ) + "_" + getTime( ) + ":" + hist.values( ).toString( ) +
				(isIdle( ) ? "" : " {" +getCurrentTask( ) + "}");
	}
	
	/**
	 * @see core.solver.model.crg.CRGState#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return super.hashCode( ) + 100000 * hist.size( );
	}
	
	/**
	 * @see core.solver.model.crg.CRGState#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof CRGStateMPP) ) return false;
		final CRGStateMPP s = (CRGStateMPP)obj;
		
		// do simple parent state checks first
		if( !super.equals( obj ) ) return false;
		
		// do MPP specific checks
		if( ctask == null ) { if( s.ctask != null ) return false; }
		else if( !ctask.equals( s.ctask ) ) return false;
		if( !hist.equals( s.hist ) ) return false;
		
		return true;
	}
}
