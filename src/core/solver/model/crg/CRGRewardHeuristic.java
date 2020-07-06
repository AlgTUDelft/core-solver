/**
 * @file CRGRewardHeuristic.java
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.model.policy.Agent;


/**
 * Heuristic to assign reward functions to agents
 * 
 * @author Joris Scharpff
 */
public abstract class CRGRewardHeuristic {
	/**
	 * Assigns reward functions to the agents
	 * 
	 * @param agents The set of agents in the instance
	 * @param rewards The reward functions
	 * @return Mapping of rewards per agent
	 */
	public abstract Map<Agent, CRGRewards> assign( Collection<Agent> agents, List<CRGReward> rewards );
	
	/**
	 * Creates the initial mapping with empty result sets
	 * 
	 * @param agents The agents to create a mapping for
	 * @return The map of (empty) reward function sets per agent
	 */
	protected Map<Agent, CRGRewards> initMapping( Collection<Agent> agents ) {
		final Map<Agent, CRGRewards> map = new HashMap<Agent, CRGRewards>( agents.size( ) );

		for( Agent a : agents )
			map.put( a, new CRGRewards( a ) );
		
		return map;
	}
	
	/**
	 * Assigns all single-agent rewards assigned to their corresponding agents
	 * 
	 * @param rewards The list of all reward functions
	 * @param remove True to remove the single-agent rewards from the reward list
	 * @return Mapping containing all single-agent reward functions
	 */
	protected void assignSingle( Map<Agent, CRGRewards> map, List<CRGReward> rewards ) {
		assert map != null : "Mapping not initialised!";
		
		// assign all single-agent rewards
		for( int i = rewards.size( ) - 1; i >= 0; i-- ) {
			final CRGReward r = rewards.get( i );
			if( r.getScope( ).size( ) != 1 ) continue;

			// add the reward to the agent set, remove it if required
			final Agent a = r.getFirstAgent( );
			final CRGRewards R = map.get( a );
			R.addReward( r );
		}
	}
}
