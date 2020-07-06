/**
 * @file CRDomain.java
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
package core.solver;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import core.domains.Instance;
import core.domains.State;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.solver.model.crg.CRGInfluence;
import core.solver.model.crg.CRGReward;
import core.solver.model.crg.CRGRewardHeuristic;
import core.solver.model.crg.CRGRewards;
import core.solver.model.crg.CRGState;
import core.solver.model.crg.CRGTransition;
import core.solver.model.policy.CRGJState;



/**
 * Base implementation of a CoRe domain
 * 
 * @author Joris Scharpff
 */
public abstract class CRDomain {
	/** Provide a reference to the CoRe solver */
	protected CoRe core;

	/**
	 * Sets the reference to the CoRe solver
	 * 
	 * @param core The solver
	 */
	protected void setCoRe( CoRe core ) {
		this.core = core;
	}
	
	/**
	 * Creates the CRGReward functions of this domain
	 * 
	 * @return A list of all reward functions
	 */
	protected abstract List<CRGReward> createRewards( );
	
	/**
	 * Assigns the reward functions to the agents using the specified heuristic,
	 * can be overwritten for domain-specific behaviour
	 * 
	 * @param rewards The rewards to assign
	 * @param heur The assignment heuristic
	 * @return Mapping of reward functions per agent
	 */
	protected Map<Agent, CRGRewards> assignRewards( List<CRGReward> rewards ) {
		final CRGRewardHeuristic heur = core.getSettings( ).getAssignHeuristic( );
		assert heur != null : "No reward assignment heuristic chosen";
		core.dbg_msg( "Using reward heuristic " + heur.getClass( ).getSimpleName( ) );
		return heur.assign( getI( ).getAgents( ), rewards );
	}

	/**
	 * Determines the set of actions that is available from the specified local
	 * state
	 * 
	 * @param state The local state
	 * @return The set of actions that can (still) be performed from the current
	 * state(without making the state infeasible)
	 */
	public abstract Collection<Action> getAvailableActions( CRGState state );
	
	
	/**
	 * Determines the set of new states that may result from taking an action in
	 * the specified state
	 * 
	 * @param state The current state
	 * @param action The action to take
	 * @return The collection of new states
	 */
	public abstract Collection<CRGState> getNewStates( CRGState state, Action action );
	
	/**
	 * Returns the transition probability of the given transition
	 * 
	 * @param trans The transition
	 * @return The transition probability
	 */
	public abstract double getTransitionProbability( CRGTransition trans );
	
	/**
	 * Determines the set of reward-dependent actions of the agent given the
	 * current local transaction
	 *  
	 * @param R The CRG rewards
	 * @param trans The local transaction
	 * @param agent The agent that may have action interactions due to the local
	 * transition
	 * @return The collection of dependent actions, empty if no dependency occurs
	 */
	public abstract Collection<Action> getDependentActions( CRGRewards R, CRGTransition trans, Agent agent );
	
	/**
	 * Determines the set of transition influences of the agent given the local
	 * transition (including dependent actions)
	 * 
	 * @param R The CRG rewards
	 * @param trans The local transition
	 * @param agent The agent to get the influence of
	 * @return The collection of influences, empty if no influence occurs
	 */
	public abstract Collection<CRGInfluence> getTransitionInfluence( CRGRewards R, CRGTransition trans, Agent agent );

	/**
	 * Creates a factored state from the global state
	 * 
	 * @param state The global state to factor
	 * @return The factored CRGStates object
	 */
	public abstract CRGJState factorState( State state );
	
	/**
	 * Checks if the state is a terminal state according to the domain, by 
	 * default implemented as time == horizon
	 * 
	 * @param state The state
	 * @return True iff the state is a terminal state
	 */
	public boolean isTerminal( CRGState state ) {
		return state.getTime( ) == getI( ).getHorizon( );
	}
	
	/** @return The CoRe solver instance */
	protected CoRe getCoRe( ) { return core; }
	
	/** @return The current instance */
	protected Instance getI( ) { return core.getI( ); }

}
