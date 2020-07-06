/**
 * @file CRDomainMPP.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.domains.State;
import core.domains.mpp.MPPInstance;
import core.domains.mpp.MPPOutcome;
import core.domains.mpp.MPPState;
import core.domains.mpp.PTask;
import core.domains.mpp.Task;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.policy.RealisedJAction;
import core.model.sharedreward.SharedActionReward;
import core.solver.CRDomain;
import core.solver.model.crg.CRGInfluence;
import core.solver.model.crg.CRGReward;
import core.solver.model.crg.CRGRewards;
import core.solver.model.crg.CRGState;
import core.solver.model.crg.CRGTransition;
import core.solver.model.policy.CRGJState;


/**
 * @author Joris Scharpff
 */
public class CRDomainMPP extends CRDomain {
	
	/**
	 * @see core.solver.CRDomain#createRewards()
	 */
	@Override
	protected List<CRGReward> createRewards( ) {
		// build list of all reward functions
		final List<CRGReward> rewards = new ArrayList<CRGReward>( );
		
		// create local reward functions
		for( Agent a : getI( ).getAgents( ) )
			rewards.add( new CRGRewardMPP( a, getI( ).getHorizon( ) ) );
		
		// create network cost functions
		final SharedActionReward sc = (SharedActionReward)getI( ).getSharedReward( );
		if( sc != null ) {
			for( Set<Action> actions : sc.getRuleSet( ) )
				rewards.add( new CRGRewardMPP( actions, sc.getReward( actions, 0 ) ) );
		}
		
		return rewards;
	}

	
	/**
	 * @see core.solver.CRDomain#factorState(core.domains.State)
	 */
	@Override
	public CRGJState factorState( State state ) {
		// get global MPP state and create new factored state
		final MPPState s = (MPPState)state;
		final CRGJState states = new CRGJState( getI( ).getAgents( ) );
		
		// get local state of each agent
		for( Agent a : states.getAgents( ) ) {
			final CRGStateMPP st = new CRGStateMPP( a, s.getTime( ) );
		
			// get completed tasks
			for( int t = 0; t < s.getTime( ); t++ ) {
				final RealisedJAction ra = s.getExecuted( t );
				final Task task = (Task)ra.getAction( a );
				if( !task.isNoOp( ) && !task.isContinue( ) )
					st.hist.put( task, new PTask( task, t, ((MPPOutcome)ra.getOutcome( a )).isDelayed( ) ) );
			}
			
			// determine current task (if any)
			for( PTask pt : st.hist.values( ) )
				if( pt.getEndTime( ) >= s.getTime( ) )
					st.ctask = pt.getTask( );
			
			// set the local state
			states.set( st );
		}
		
		
		return states;
	}
	
	/**
	 * @see core.solver.CRDomain#getAvailableActions(core.solver.model.crg.CRGState)
	 */
	@Override
	public Collection<Action> getAvailableActions( CRGState state ) {
		final CRGStateMPP s = (CRGStateMPP)state;
		
		// if busy, the agent can only continue the current task
		if( !s.isIdle( ) ) {
			final Set<Action> actions = new HashSet<Action>( 1 );
			actions.add( Task.CONT( s.getCurrentTask( ) ) );
			return actions; 			
		}
		
		// get the actions of the agent
		final Agent a = state.getAgent( );
		final Set<Action> actions = new HashSet<Action>( a.getActions( ) );
		actions.add( Task.NoOp( a ) );

		// remove completed actions and those that cannot be completed anymore
		final Set<Action> available = new HashSet<Action>( actions.size( ) );
		for( Action act : actions ) {
			final Task t = (Task)act;
			if( s.hasCompleted( t ) ) continue;
			if( !canComplete( t, s.getTime( ) ) ) continue;
			
			// do not add action if it may lead to an invalid state
			if( getI( ).mustCompleteAll( ) && !canCompleteAll( t, s ) ) continue;
			
			available.add( t );
		}
		
		return available;
	}
	
	/**
	 * @see core.solver.CRDomain#getNewStates(core.solver.model.crg.CRGState, core.model.policy.Action)
	 */
	@Override
	public Collection<CRGState> getNewStates( CRGState state, Action action ) {
		final Set<CRGState> newstates = new HashSet<CRGState>( );
		
		// check the state for the results this action may have
		final CRGStateMPP s = (CRGStateMPP)state;
		final Task t = (Task)action;
		
		// busy agent?
		if( !s.isIdle( ) )
			newstates.add( s.execContinue( ) );
		else if( t.isNoOp( ) )
			newstates.add( s.execNoOp( ) );
		else
			newstates.addAll( s.execTask( t ) );
		
		return newstates;		
	}
	
	/**
	 * @see core.solver.CRDomain#getTransitionProbability(core.solver.model.crg.CRGTransition)
	 */
	@Override
	public double getTransitionProbability( CRGTransition trans ) {
		final Task task = (Task)trans.getAction( );
		if( task.isNoOp( ) || task.isContinue( ) ) return 1.0;

		// check the ptask stored for the task
		final CRGStateMPP s = (CRGStateMPP)trans.getNewState( );
		final PTask pt = s.getPTask( task );
		
		// return the delay probability
		return pt.isDelayed( ) ? task.getDelayProb( ) : 1.0 - task.getDelayProb( );
	}
	
	/**
	 * @see core.solver.CRDomain#getDependentActions(core.solver.model.crg.CRGRewards, sptk.solvers.stochastic.core.model.CRGTransitionOld, core.model.policy.Agent)
	 */
	@Override
	public Collection<Action> getDependentActions( CRGRewards R, CRGTransition trans, Agent agent ) {
		final Set<Action> actions = new HashSet<Action>( agent.getNumActions( ) );
		
		// check for all reward functions that contain the action of the current
		// transition and the agent
		for( CRGReward crgrew : R.getRewards( ) ) {
			final CRGRewardMPP r = (CRGRewardMPP)crgrew;
			
			// no dependencies in single agent rewards
			if( r.getScope( ).size( ) == 1 ) continue;
			
			// only when agent is in scope
			if( !r.getScope( ).contains( agent ) ) continue;
			
			// and it contains the action
			if( !r.getActions( ).contains( ((Task)trans.getAction( )).getRealTask( ) ) ) continue;
			
			// there might be a dependency, add the action of the agent
			for( Action a : r.getActions( ) )
				if( a.getAgent( ).equals( agent ) ) {
					actions.add( a );
					
					// add also a continue task
					final Task t = (Task)a;
					if( t.getDuration( true ) > 1 ) actions.add( Task.CONT( t ) );
					break;
				}
		}
		
		return actions;
	}
	
	/**
	 * @see core.solver.CRDomain#getTransitionInfluence(core.solver.model.crg.CRGRewards, sptk.solvers.stochastic.core.model.CRGTransitionOld, core.model.policy.Agent)
	 */
	@Override
	public Collection<CRGInfluence> getTransitionInfluence( CRGRewards R, CRGTransition trans, Agent agent ) {
		return new HashSet<CRGInfluence>( 0 );
	}
	
	/**
	 * @see core.solver.CRDomain#isTerminal(core.solver.model.crg.CRGState)
	 */
	@Override
	public boolean isTerminal( CRGState state ) {
		// check current task
		final CRGStateMPP s = (CRGStateMPP)state;
		if( !s.isIdle( ) ) return false;
		
		// check if all actions are completed
		if( s.hist.size( ) < s.getAgent( ).getNumActions( ) ) return false;
		
		// finally, the state is terminal if all actions have been completed
		return state.getTime( ) == getI( ).getHorizon( );
	}
	
	/**
	 * Checks if the specified action can be completed from the given start time
	 * 
	 * @param task The task
	 * @param time The start time of the action
	 * @return True iff the task can be completed from the start time without
	 * invalidating the state
	 */
	protected boolean canComplete( Task task, int time ) {
		return time + task.getDuration( true ) <= getI( ).getHorizon( );
	}
	
	/**
	 * Checks if performing the action causes the agent to fail in completing all
	 * its tasks
	 * 
	 * @param task The task
	 * @param state The current state from which to start the task
	 * @return True iff there is enough time left to complete all other tasks
	 */
	protected boolean canCompleteAll( Task task, CRGStateMPP state ) {		
		// get required time of all uncompleted tasks, if the current task is a
		// NoOp add 1 unit to the minimally required time
		int timereq = task.isNoOp( ) ? 1 : 0;

		// sum the minimally required time to complete all
		final Agent agent = state.getAgent( );
		for( Action a : agent.getActions( ) ) {
			final Task t = (Task)a;
			if( !state.hasCompleted( t ) )
				timereq += t.getDuration( true );
		}
		
		// return true iff there is enough time
		return state.getTime( ) + timereq <= getI( ).getHorizon( );
	}
	
	/**
	 * @see core.solver.CRDomain#getI()
	 */
	@Override
	protected MPPInstance getI( ) {
		return (MPPInstance)super.getI( );
	}

}
