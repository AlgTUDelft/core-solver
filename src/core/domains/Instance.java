/**
 * @file Instance.java
 * @brief Short description of file
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2013 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         27 jun. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains;

import java.util.HashSet;
import java.util.Set;

import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.policy.OutcomeRealisation;
import core.model.policy.Plan;
import core.model.policy.SimplePlan;
import core.model.sharedreward.SharedReward;

/**
 * Container for stochastic planning problem instances
 *
 * @author Joris Scharpff
 */
public abstract class Instance {
	/** The planning horizon */
	protected final int horizon;
	
	/** The set of agents */
	protected final Set<Agent> agents;
	
	/** List of all actions */
	protected final Set<Action> actions;
	
	/** The initial problem state */
	protected State initstate;
	
	/** The shared reward model */
	protected SharedReward sharedreward;

	/** Instance generation seed (-1 for manual) */
	protected long seed;
	
	/**
	 * Creates a custom instance
	 * 
	 * @param agents The agents in the instance
	 * @param horizon The instance horizon
	 */
	public Instance( Set<Agent> agents, int horizon ) {
		this.horizon = horizon;
		this.seed = -1;

		this.agents = new HashSet<Agent>( agents );

		// build global actions list
		this.actions = new HashSet<Action>( );
		for( Agent a : this.agents )
			actions.addAll( a.getActions( ) );
	}
	
	/**
	 * @return True if the I has an associated shared reward model
	 */
	public boolean hasSharedReward( ) {
		return sharedreward != null;
	}
	
	/**
	 * Sets the shared reward
	 * 
	 * @param rewardmodel The shared reward model
	 */
	public void setSharedReward( SharedReward rewardmodel ) {
		this.sharedreward = rewardmodel;
	}
	
	/**
	 * @return The shared reward model used in this instance
	 */
	public SharedReward getSharedReward( ) {
		return sharedreward;
	}
	
	/**
	 * @return The number of agents
	 */
	public int getN( ) {
		return agents.size( );
	}
	
	/**
	 * @return The set of agents
	 */
	public Set<Agent> getAgents( ) {
		return agents;
	}
	
	/**
	 * Retrieves the agent by its ID
	 * 
	 * @param ID The agent ID
	 * @return The agent or null if not found
	 */
	public Agent getAgent( int ID ) {
		for( Agent a : agents )
			if( a.ID == ID )
				return a;
		
		return null;
	}
	
	/**
	 * @return The total number of actions
	 */
	public int getA( ) {
		return actions.size( );
	}

	/**
	 * @return The list of all actions in the instance
	 */
	public Set<Action> getActions( ) {
		return actions;
	}
	
	/**
	 * @return The problem horizon
	 */
	public int getHorizon( ) {
		return horizon;
	}
	
	/**
	 * @return The seed used to generate the instance
	 */
	public long getGenerationSeed( ) {
		return seed;
	}
	
	/**
	 * Sets the generation seed used to generate this instance
	 * 
	 * @param seed The seed
	 */
	public void setGenerationSeed( long seed ) {
		this.seed = seed;
	}
		
	/**
	 * Checks if the instance contains no stochastic elements
	 * 
	 * @return True if the instance is deterministic
	 */
	public boolean isDeterministic( ) {
		for( Action a : getActions( ) )
			if( a.getOutcomes( ).size( ) > 1 )
				return false;
		
		return true;
	}
	
	/**
	 * Checks if the instance contains stochastic elements
	 * 
	 * @return True if the instance is stochastic
	 */
	public boolean isStochastic( ) {
		return !isDeterministic( );
	}
	
	/**
	 * Sets the initial state for the instance
	 * 
	 * @param initstate The initial state
	 */
	protected void setInitialState( State initstate ) {
		this.initstate = initstate;
	}
	
	/**
	 * Returns the initial state for this instance
	 * 
	 * @return The initial state
	 */
	public State getInitialState( ) {
		return initstate;
	}
	
	/**
	 * Returns an empty state value for this instance
	 * 
	 * @return The empty state value
	 */
	public abstract StateValue getEmptyValue( );
	
	/**
	 * Returns an empty plan for this instance
	 * 
	 * @return The empty plan
	 */
	public Plan getEmptyPlan( ) {
		return new SimplePlan( this );
	}
	
	/**
	 * Transforms the instance to a new instance where all stochastic elements
	 * have been replaced by deterministic ones, using the specified outcome
	 * realisation
	 * 
	 * @param outcomes The outcome realisation
	 * @return The new instance
	 * @throws IllegalArgumentException if the transform failed
	 */
	public abstract Instance toDeterministicInstance( OutcomeRealisation outcomes ) throws IllegalArgumentException;
	
	/**
	 * Transforms the instance using the specified weights
	 * 
	 * @param w The transform weights
	 * @return The new instance
	 * @throws IllegalArgumentException if the weights are incorrect for the instance
	 */
	public abstract Instance getWeightedInstance( double[] w ) throws IllegalArgumentException;
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		// output I details
		String str = "=====[ Instance ]=====\n";
		for( Agent a : agents )
			str += a.toLongString( ) + "\n";
		
		// output social cost model
		if( sharedreward != null ) {
			str += "\n" + sharedreward.getDescription( );
		}
		return str;
	}
}
