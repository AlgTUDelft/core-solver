/**
 * @file CRGRewards.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import core.model.policy.Agent;


/**
 * Container for a collection of rewards
 * 
 * @author Joris Scharpff
 */
public class CRGRewards {
	/** The agent to which all rewards belong */
	protected final Agent agent;
	
	/** The reward functions in the container */
	protected final Collection<CRGReward> rewards;
	
	/** The agents in the scope of the reward functions */
	protected final Collection<Agent> scope;
	
	/**
	 * Creates a new CRGReward container
	 * 
	 * @param agent The agent that is responsible for the rewards
	 */
	public CRGRewards( Agent agent ) {
		this.agent = agent;
		this.rewards = new ArrayList<CRGReward>( );
		this.scope = new HashSet<Agent>( );
	}
	
	/**
	 * Adds a reward function to this reward container
	 * 
	 * @param reward The reward function
	 */
	public void addReward( CRGReward reward ) {
		rewards.add( reward );
		
		// update scope if required
		scope.addAll( reward.getScope( ) );
	}
	
	/**
	 * @return The collection of rewards
	 */
	public Collection<CRGReward> getRewards( ) {
		return rewards;
	}
	
	/** @return The agent responsible for the rewards */
	public Agent getAgent( ) {
		return agent;
	}
	
	/**
	 * @return The scope of the set of rewards
	 */
	public Collection<Agent> getScope( ) {
		return scope;
	}
	

	/**
	 * @return The number of reward functions in this collection
	 */
	public int size( ) {
		return rewards.size( );
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return rewards.toString( );
	}
}
