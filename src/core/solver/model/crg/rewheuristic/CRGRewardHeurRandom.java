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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import core.model.policy.Agent;
import core.solver.model.crg.CRGReward;
import core.solver.model.crg.CRGRewardHeuristic;
import core.solver.model.crg.CRGRewards;
import core.util.RandGenerator;


/**
 * @author Joris Scharpff
 */
public class CRGRewardHeurRandom extends CRGRewardHeuristic {
	/** The random generator */
	protected final RandGenerator rand;
	
	/**
	 * Creates a new random assignment heuristic with the specified random seed
	 * 
	 * @param seed The seed to initialise the random generator
	 */
	public CRGRewardHeurRandom( long seed ) {
		rand = new RandGenerator( seed );
	}

	/**
	 * @see core.solver.model.crg.CRGRewardHeuristic#assign(java.util.Collection, java.util.List)
	 */
	@Override
	public Map<Agent, CRGRewards> assign( Collection<Agent> agents, List<CRGReward> rewards ) {
		// initialise mapping & assign single-agent rewards
		final Map<Agent, CRGRewards> map = initMapping( agents );
		assignSingle( map, rewards );
		
		// randomly assign all multi-agent rewards
		for( CRGReward r : rewards ) {
			if( r.size( ) == 1 ) continue;
			
			final Agent a = (new ArrayList<Agent>( r.getScope( ) )).get( rand.randInt( r.getScope( ).size( ) - 1 ) );
			map.get( a ).addReward( r );
		}
		
		return map;
	}
}
