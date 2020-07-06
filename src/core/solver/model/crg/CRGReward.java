/**
 * @file CRGReward.java
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
package core.solver.model.crg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import core.domains.StateValue;
import core.model.policy.Agent;
import core.solver.model.policy.CRGJState;


/**
 * Models a single CRG Reward function
 * 
 * @author Joris Scharpff
 */
public abstract class CRGReward {
	/** The agents in the scope of this reward */
	protected final Set<Agent> scope;
	
	/** Human-readable description for debug purposes, defaulted to the scope */
	protected String name;
	
	/**
	 * Creates a new single-agent reward
	 * 
	 * @param agent The agent
	 */
	public CRGReward( Agent agent ) {
		scope = new HashSet<Agent>( 1 );
		scope.add( agent );
		
		setName( agent.toString( ) );
	}
	
	
	/**
	 * Creates a new CRGReward with the specified scope
	 * 
	 * @param agents The agents in the scope of this function
	 */
	public CRGReward( Collection<Agent> agents ) {
		scope = new HashSet<Agent>( agents );
		
		setName( scope.toString( ) );
	}
	
	/**
	 * Constructs a new, empty CRGReward for custom creation
	 * 
	 * @param size The initial scope set size
	 */
	public CRGReward( int size ) {
		scope = new HashSet<Agent>( size );
	}
	
	/**
	 * Sets a human-friendly name for the reward, this is never used by the
	 * solver but can greatly improve debugging
	 * 
	 * @param name The name
	 */
	public void setName( String name ) {
		this.name = name;
	}
	
	/**
	 * Checks if an agent is within the scope of this function
	 * 
	 * @param agent The agent
	 * @return True iff the agent is in the scope of this function
	 */
	public boolean inScope( Agent agent ) {
		return scope.contains( agent );
	}
	
	/**
	 * Get all agents in the scope of this reward
	 * 
	 * @return The set of agents
	 */
	public Collection<Agent> getScope( ) {
		return scope;
	}
	
	/**
	 * @return The first agent in the scope, useful for single-agent rewards
	 */
	public Agent getFirstAgent( ) {
		return scope.iterator( ).next( );
	}
	
	/**
	 * Computes the reward for the local transition
	 *
	 * @param trans The local transition
	 * @return The transition value as a StateValue object
	 */
	public abstract StateValue getReward( CRGTransition trans );

	/**
	 * Determines of a given local state is locally CRI
	 * 
	 * @param state The state
	 * @return True iff the state is locally conditional independent
	 */
	public abstract boolean localCRI( CRGState state );
	
	/**
	 * Determines if two agents are (globally) Conditional Reward Independent in
	 * this reward function from the current joint state onward
	 * 
	 * @param a1
	 * @param a2
	 * @param state The joint state
	 * @return True iff the agents are  is CRI from
	 */
	public abstract boolean CRI( Agent a1, Agent a2, CRGJState state );
	
	/**
	 * @return The scope size
	 */
	public int size( ) {
		return scope.size( );
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "R(" + name + ")";
	}
}
