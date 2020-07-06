/**
 * @file CRGRewardHeurRandom.java
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
package core.solver.model.crg.rewheuristic;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.model.policy.Agent;
import core.solver.model.crg.CRGReward;
import core.solver.model.crg.CRGRewardHeuristic;
import core.solver.model.crg.CRGRewards;


/**
 * Assigns the rewards to the agent with the lowest degree of dependency
 * 
 * @author Joris Scharpff
 */
public class CRGRewardHeurLowestDegree extends CRGRewardHeuristic {

	/**
	 * @see core.solver.model.crg.CRGRewardHeuristic#assign(java.util.Collection, java.util.List)
	 */
	@Override
	public Map<Agent, CRGRewards> assign( Collection<Agent> agents, List<CRGReward> rewards ) {
		// initialise mapping & assign single-agent rewards
		final Map<Agent, CRGRewards> map = initMapping( agents );

		// count the degree in each agent
		final Map<Agent, Integer> degree = new HashMap<Agent, Integer>( agents.size( ) );
		for( Agent a : agents )
			degree.put( a, 0 );

		// determine degrees
		for( CRGReward r : rewards )
			for( Agent a : r.getScope( ) )
				degree.put( a, degree.get( a ) + 1 );
		
		// assign to the agent with the lowest degree in the scope
		for( CRGReward r : rewards ) {
			Agent ag = null;
			for( Agent a : r.getScope( ) )
				if( ag == null || degree.get( ag ) > degree.get( a ) )
					ag = a;
			
			// assign it
			map.get( ag ).addReward( r );
		}
		
		return map;
	}
}
