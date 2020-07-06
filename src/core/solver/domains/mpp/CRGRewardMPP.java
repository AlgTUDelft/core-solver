/**
 * @file CRGRewardMPP.java
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
import java.util.HashSet;

import core.domains.StateValue;
import core.domains.mpp.MPPStateValue;
import core.domains.mpp.Task;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.solver.model.crg.CRGReward;
import core.solver.model.crg.CRGState;
import core.solver.model.crg.CRGTransition;
import core.solver.model.policy.CRGJState;


/**
 * Reward functions in the MPP domain
 * 
 * @author Joris Scharpff
 */
public class CRGRewardMPP extends CRGReward {	
	/** The planning horizon */
	protected final int horizon;
	
	/** The actions that cause a network cost */
	protected final Collection<Action> netactions;
	
	/** The network cost */
	protected final double netcost;

	
	/**
	 * Creates a new single-agent MPP reward
	 *  
	 * @param agent The agent
	 * @param horizon The planning horizon
	 */
	public CRGRewardMPP( Agent agent, int horizon ) {
		super( agent );
		
		this.horizon = horizon;
		
		// no need for network stuff
		netactions = null;
		netcost = 0;
	}
	
	/**
	 * Creates a new network reward
	 * 
	 * @param actions The actions in the network cost rule
	 * @param cost The network cost
	 */
	public CRGRewardMPP( Collection<Action> actions, double cost ) {
		super( actions.size( ) );
		
		this.horizon = -1;
		
		// store the actions for future reference
		this.netactions = new HashSet<Action>( actions );
		this.netcost = cost;
		
		// get the collection of agents in this rule
		for( Action a : actions ) scope.add( a.getAgent( ) );
		
		// set human-friendly name and return
		setName( actions.toString( ) );
	}
	
	/**
	 * @see core.solver.model.crg.CRGReward#getReward(core.solver.model.crg.CRGTransition)
	 */
	@Override
	public StateValue getReward( CRGTransition trans ) {
		// single-agent?
		if( size( ) == 1 ) {
			final CRGStateMPP s = (CRGStateMPP)trans.getState( );
			return getIndividualReward( s, (Task)trans.getAction( ) );
		} 
		
		// network reward
		final Collection<Action> actions = new HashSet<Action>(  );
		actions.add( ((Task)trans.getAction( )).getRealTask( ) );
		
		for( Action a : trans.getDependencies( ).get( ) )
			actions.add( ((Task)a).getRealTask( ) );
		
		return getNetworkReward( actions );
	}
	
	/**
	 * Computes the individual reward of the agent associated with this function
	 * 
	 * @param state The local agent state
	 * @param ptask The task and its outcome
	 * @return The reward
	 */
	protected StateValue getIndividualReward( CRGState state, Task task ) {
		// no reward for NoOps
		if( task.isNoOp( ) ) return new MPPStateValue( );
		
		// determine revenue and cost
		final double rev = !task.isContinue( ) ? task.getRevenue( ) : 0;
		final Task t = task.isContinue( ) ? task.getContinueTask( ) : task;
		final double cost = t.getStepCost( state.getTime( ), horizon );
		
		// return the reward
		return new MPPStateValue( rev, cost, 0 );
	}
	
	/**
	 * Computes the network reward for a joint transition 
	 * 
	 * @param actions The set of actions performed in this transition
	 * @return The network cost of the transition
	 */
	protected MPPStateValue getNetworkReward( Collection<Action> actions ) {
		assert netactions != null : "Not a network reward function";
		
		// check if rule matches
		for( Action a : netactions ) {
			if( !actions.contains( a ) )
				return new MPPStateValue( );
		}
		
		return new MPPStateValue( 0, 0, netcost );
	}
	
	/**
	 * @see core.solver.model.crg.CRGReward#localCRI(core.solver.model.crg.CRGState)
	 */
	@Override
	public boolean localCRI( CRGState state ) {
		if( size( ) == 1 ) return true;
		
		// checks if my dependent action is completed
		final CRGStateMPP s = (CRGStateMPP)state;
		final Task task = getAction( state.getAgent( ) );		
		return s.hasCompleted( task );
	}
	
	/**
	 * @see core.solver.model.crg.CRGReward#CRI(core.model.policy.Agent, core.model.policy.Agent, core.solver.model.policy.CRGJState)
	 */
	@Override
	public boolean CRI( Agent a1, Agent a2, CRGJState state ) {
		assert inScope( a1 ) : "Agent " + a1 + " not in scope of reward";
		assert inScope( a2 ) : "Agent " + a2 + " not in scope of reward";
		
		// get the network action of both agents and check if it is completed
		return localCRI( state.get( a1 ) ) || localCRI( state.get( a2 ) ); 
	}
	
	/**
	 * Retrieves the task corresponding to the agent
	 * 
	 * @param agent The agent
	 * @return The action for the agent
	 */
	protected Task getAction( Agent agent ) {
		assert inScope( agent ) : "Agent " + agent + " not in scope of this reward function";
		
		for( Action a : getActions( ) )
			if( a.getAgent( ).equals( agent ) )
				return (Task)a;

		// silences the compiler
		throw new AssertionError( "No action for agent!" );
	}
	
	/**
	 * @return The set of actions in this network rule
	 */
	protected Collection<Action> getActions( ) {
		assert getScope( ).size( ) > 1 : "No actions in a single-agent reward";
		return netactions;
	}
}
