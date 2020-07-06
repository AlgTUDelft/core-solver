/**
 * @file SharedNetworkCost.java
 * @brief [brief description]
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2014 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         25 mrt. 2014
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
import core.model.function.ConstFunction;
import core.model.function.Function;
import core.model.policy.Action;
import core.model.policy.Agent;


/**
 * Shared reward for executing actions concurrently
 * 
 * @author Joris Scharpff
 */
public class SharedActionReward extends SharedReward {
	/** Mapping of action combinations to a reward function */
	protected Map<Set<Action>, Function> rewards;
	
	/** Maximum constraint cardinality */
	protected int maxK;
	
	/**
	 * Creates a new shared network cost model for the instance
	 * 
	 * @param instance The MPP instance
	 */
	public SharedActionReward( Instance instance ) {
		super( instance );
		
		// create new mapping
		rewards = new HashMap<Set<Action>, Function>( );
		maxK = 0;
	}
	
	/**
	 * @see core.model.sharedreward.SharedReward#copyCreate()
	 */
	@Override
	protected SharedReward copyCreate( Instance instance ) {
		final SharedActionReward sr = new SharedActionReward( instance );
		
		// copy mapping
		for( Set<Action> actions : rewards.keySet( ) )
			sr.rewards.put( actions, rewards.get( actions ).copy( ) );
		sr.maxK = maxK;
		
		return sr;
	}

	/**
	 * @see core.model.sharedreward.SharedReward#computeReward(core.domains.State)
	 */
	@Override
	protected double computeReward( State state ) {
		// determine shared reward up to this state
		double rew = 0;
		for( int t = 0; t < state.getTime( ); t++ ) {
			// get all actions executed at the time
			rew += getReward( state.getExecuting( t ), t );
		}
		
		return rew;
	}
	
	/**
	 * Computes the reward that is due to the combination of actions at the
	 * specified time
	 * 
	 * @param actions The actions
	 * @param time The time
	 * @return The shared reward for that time and action combination
	 */
	public double getReward( Set<Action> actions, int time ) {
		double rew = 0;
		for( Set<Action> rule : rewards.keySet( ) ) {
			if( actions.containsAll( rule ) )
				rew += rewards.get( rule ).eval( time, getI( ).getHorizon( ) );
		}
		
		return rew * weight;
	}
	
	/**
	 * Computes the reward that is due to the combination of actions at the
	 * specified time, only counting if it involves one of the specified agents
	 * 
	 * @param actions The actions
	 * @param time The time
	 * @param agents The agents
	 * @return The shared reward for that time and action combination
	 */
	public double getReward( Set<Action> actions, int time, Set<Agent> agents ) {
		double rew = 0;
		for( Set<Action> rule : rewards.keySet( ) ) {
			if( actions.containsAll( rule ) ) {
				boolean contained = false;
				for( Action a : rule )
					contained |= agents.contains( a.getAgent( ) );
				
				if( contained )
					rew += rewards.get( rule ).eval( time, getI( ).getHorizon( ) );
			}
		}
		
		return rew * weight;
	}
	
	/**
	 * Adds a rule to the network reward model
	 * 
	 * @param actions The set of actions to match
	 * @param function The cost function
	 * @return True if no such rule was present
	 */
	public boolean addRule( Set<Action> actions, Function function ) {
		if( rewards.containsKey( actions ) )
			return false;
		
		rewards.put( actions, function );
		if( actions.size( ) > maxK )
			maxK = actions.size( );
		
		return true;
	}
	
	/**
	 * Adds a binary rule to the network reward model with a constant reward
	 * 
	 * @param a1 Action 1
	 * @param a2 Action 2
	 * @param reward The reward
	 * @return True if no such rule previously existed
	 */
	public boolean addRule( Action a1, Action a2, double reward ) {
		return addRule( a1, a2, new ConstFunction( reward ) );
	}
	
	
	/**
	 * Adds a binary rule to the network reward model
	 * 
	 * @param a1 Action 1
	 * @param a2 Action 2
	 * @param function The reward function
	 * @return True if no such rule previously existed
	 */
	public boolean addRule( Action a1, Action a2, Function function ) {
		final Set<Action> actions = new HashSet<Action>( );
		actions.add( a1 );
		actions.add( a2 );
		return addRule( actions, function );
	}
	
	/**
	 * @return The set of rules
	 */
	public Set<Set<Action>> getRuleSet( ) {
		return rewards.keySet( );
	}
	
	/**
	 * @return The maximum constraint cardinality in this model
	 */
	public int getMaxCardinality( ) {
		return maxK;
	}

	/**
	 * @see core.model.sharedreward.SharedReward#getDescription()
	 */
	@Override
	public String getDescription( ) {
		String str = "[Network costs]\n";
		if( weight != 1.0 )
			str += "(weight " + weight + ")\n";
		for( Set<Action> actions : rewards.keySet( ) ) {
			str += "[";
			for( Action a : actions )
				str += a + "&";
			str = str.substring( 0, str.length( ) - 1 ) + "] => ";
			str += rewards.get( actions ).toString( );
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
		
		for( Set<Action> actions : rewards.keySet( ) ) {
			str += "[";
			for( Action a : actions )
				str += a.getAgent( ).ID + "_" + a.ID + ";";
			str = str.substring( 0, str.length( ) - 1 ) + "] => " + rewards.get( actions ).serialise( ) + " ^ ";
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
			final Set<Action> actions = new HashSet<Action>( );
			for( String task : r[0].substring( 1, r[0].length( ) - 1 ).split( ";" ) ) {
				final String[] IDs = task.split( "_" );
				
				try {
					final int aID = Integer.parseInt( IDs[0] );
					final int tID = Integer.parseInt( IDs[1] );
					
					final Action act = getI( ).getAgent( aID ).getAction( tID );
					if( act == null ) throw new NullPointerException( );
					actions.add( act );
				} catch( NumberFormatException nfe ) {
					throw new IIOException( "Invalid agent / task ID in rule: " + rule );
				} catch( NullPointerException npe ) {
					throw new IIOException( "Agent / task ID unknown in rule: " + rule );
				}
			}
			
			// read function
			final Function f = Function.deserialise( r[1] );
			
			addRule( actions, f );
		}
	}
}
