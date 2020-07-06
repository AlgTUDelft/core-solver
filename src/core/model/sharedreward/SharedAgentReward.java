/**
 * @file SharedAgentReward.java
 * @brief Short description of file
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright © 2014 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         10 apr. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.sharedreward;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.domains.Instance;
import core.domains.State;
import core.exceptions.IIOException;
import core.model.function.Function;
import core.model.policy.Agent;

/**
 * Shared reward based on agent features
 *
 * @author Joris Scharpff
 */
public abstract class SharedAgentReward extends SharedReward {
	/** Sets of agents that of which we check some state feature in order to
	 * obtain a reward. The state feature depends on the domain */
	private final Map<Set<Agent>, Function> rewards;
	
	/**
	 * Creates a new reward model based on agent features
	 * 
	 * @param inst The instance associated with the model
	 */
	public SharedAgentReward( Instance inst ) {
		super( inst );
		
		// create an initial agent set mapping
		rewards = new HashMap<Set<Agent>, Function>( );
	}

	/**
	 * @see core.model.sharedreward.SharedReward#copy(core.model.sharedreward.SharedReward)
	 */
	@Override
	protected void copy( SharedReward sr ) {
		// first copy all abstract super-class properties 
		super.copy( sr );

		// copy the reward set
		final SharedAgentReward sar = (SharedAgentReward) sr;
		for( Set<Agent> agents : sar.rewards.keySet( ) )
			rewards.put( agents, sar.rewards.get( agents ).copy( ) );
	}

	/**
	 * @see core.model.sharedreward.SharedReward#computeReward(core.domains.State)
	 */
	@Override
	protected abstract double computeReward( State state );
	
	/**
	 * Adds a rule for the set of agents. If there was already a rule present
	 * with the same set of agents, the function does not add the agents and
	 * returns false;
	 * 
	 * @param agents The set of agents to add
	 * @param reward The reward function
	 * @return True if the rules was added successfully
	 */
	public boolean addRule( Set<Agent> agents, Function reward ) {
		if( rewards.containsKey( agents ) ) return false;
		
		rewards.put( agents, reward );
		return true;
	}
	
	/**
	 * @return The set of rules
	 */
	public Set<Set<Agent>> getRules( ) {
		return rewards.keySet( );
	}
	
	/**
	 * @param agents The agents
	 * @return The reward function for the specified rule or null if there is no
	 * such rule 
	 */
	public Function getReward( Set<Agent> agents ) {
		return rewards.get( agents );
	}
	
	/**
	 * @see core.model.sharedreward.SharedReward#getDescription()
	 */
	@Override
	public String getDescription( ) {
		String str = "[Shared agent rewards]\n";
		for( Set<Agent> agents : rewards.keySet( ) ) {
			str += "[";
			for( Agent a : agents )
				str += a + "&";
			str = str.substring( 0, str.length( ) - 1 ) + "] => ";
			str += rewards.get( agents ).toString( );
			str += "\n";
		}
		
		return str;
	}

	/**
	 * @see core.model.sharedreward.SharedReward#doSerialise()
	 */
	@Override
	protected String doSerialise( ) {
		String str = "";
		
		for( Set<Agent> agents : rewards.keySet( ) ) {
			str += "[";
			for( Agent a : agents )
				str += a.ID + ";";
			str = str.substring( 0, str.length( ) - 1 ) + "] => " + rewards.get( agents ).serialise( ) + " ^ ";
		}
		
		return str.substring( 0, str.length( ) - 4 );
	}

	/**
	 * @see core.model.sharedreward.SharedReward#deserialise(java.lang.String)
	 */
	@Override
	protected void deserialise( String serialised ) throws IIOException {
		// read all rules
		for( String rule : serialised.split( " \\^ " ) ) {
			final String[] r = rule.split( " => " );
			
			// read tasks
			final Set<Agent> agents = new HashSet<Agent>( );
			for( String agent : r[0].substring( 1, r[0].length( ) - 1 ).split( ";" ) ) {
				
				try {
					final int aID = Integer.parseInt( agent );
					
					final Agent a = getI( ).getAgent( aID );
					if( a == null ) throw new NullPointerException( );
					agents.add( a );
				} catch( NumberFormatException nfe ) {
					throw new IIOException( "Invalid agent ID in rule: " + rule );
				} catch( NullPointerException npe ) {
					throw new IIOException( "Agent ID unknown in rule: " + rule );
				}
			}
			
			// read function
			final Function f = Function.deserialise( r[1] );
			
			addRule( agents, f );
		}
	}
}
