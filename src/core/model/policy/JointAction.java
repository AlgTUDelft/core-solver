/**
 * @file JointAction.java
 * @brief [brief description]
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2013 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         31 okt. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.policy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * A joint action is a set of actions that describes for each agent the task to
 * take.
 * 
 * @author Joris Scharpff
 */
public class JointAction {
	/** The agents in this map */
	protected final Map<Agent, Action> actions;
	
	/** The time at which this joint action is executed */
	protected final int time;
	
	/**
	 * Creates a new empty joint action for the specified time
	 * 
	 * @param time The time at which this joint action is taken
	 */
	public JointAction( int time ) {
		this.actions = new HashMap<Agent, Action>( );
		this.time = time;
	}
	
	/**
	 * Creates a new JointAction for the agents
	 * 
	 * @param agents The agents involved in this joint action
	 * @param time The time at which this joint action is taken
	 */
	public JointAction( Collection<Agent> agents, int time ) {
		// create mapping and add all agent keys
		this.actions = new HashMap<Agent, Action>( agents.size( ) );
		for( Agent agent : agents )
			this.actions.put( agent, null );
		

		// set the time of this joint action
		this.time = time;
	}
	
	/**
	 * Creates a new joint action containing only one agent and sets the
	 * specified action for the agent
	 * 
	 * @param agent The agent
	 * @param time The time of taking the action
	 * @param action The action (null for continue)
	 */
	public JointAction( Agent agent, int time, Action action ) {
		this.actions = new HashMap<Agent, Action>( );
		actions.put( agent, action );
			
		// set the time and action
		this.time = time;
		setAction( agent, action );
	}
	
	/**
	 * Copies a joint action
	 * 
	 * @param ja The joint action to copy
	 */
	public JointAction( JointAction ja ) {
		actions = new HashMap<Agent, Action>( ja.actions );
		
		time = ja.time;
	}
		
	/**
	 * Sets the action for the agent
	 * 
	 * @param agent The agent
	 * @param action The action it is performing
	 * @throws NullPointerException if the action is null
	 */
	public void setAction( Agent agent, Action action ) {
		if( action == null )
			throw new NullPointerException( "Setting NULL action for agent " + agent.toString( ) );
		
		actions.put( agent, action );
	}
	
	/**
	 * Retrieves the action for the specified agent
	 * 
	 * @param agent The agent
	 * @return The task for the specified agent
	 * @throws NullPointerException if no task is set
	 */
	public Action getAction( Agent agent ) {
		final Action act = actions.get( agent );
		if( act == null )
			throw new NullPointerException( "No action for agent " + agent.toString( ) );
		
		return act;
	}
	
	/**
	 * Checks if an action is set for the agent
	 * 
	 * @param agent The agent
	 * @return True if an action is set
	 */
	public boolean hasAction( Agent agent ) {
		return actions.get( agent ) != null;
	}
	
	/**
	 * @return The set of all actions in this joint action
	 */
	public Set<Action> toSet( ) {
		final Set<Action> actions = new HashSet<Action>( );
		for( Agent a : getAgents( ) )
			if( getAction( a ) != null )
				actions.add( getAction( a ) );
		
		return actions;
	}

	/**
	 * @return The set of agents in this joint action
	 */
	public Collection<Agent> getAgents( ) {
		return actions.keySet( );
	}
	
	/**
	 * @return The set of actions in this joint action
	 */
	public Collection<Action> getActions( ) {
		final Set<Action> actions = new HashSet<Action>( );
		for( Agent a : getAgents( ) )
			actions.add( getAction( a ) );
		return actions;
	}
	
	/**
	 * @return The time of this joint action
	 */
	public int getTime( ) {
		return time;
	}
	
	/**
	 * Adds the agent to this joint action and sets the action.
	 * 
	 * @param agent The agent
	 * @param action The action
	 * @throws RuntimeException if the agent was already in this joint action
	 */
	public void addAgent( Agent agent, Action action ) throws RuntimeException {
		if( actions.containsKey( agent ) )
			throw new RuntimeException( "Agent already in joint action" );

		// add the agent and set the action
		setAction( agent, action );
	}
	
	/**
	 * Removes the agent from this joint action
	 * 
	 * @param agent The agent
	 * @return The action that was planned
	 * @throws RuntimeException if the agent was not in the joint action
	 */
	public Action removeAgent( Agent agent ) throws RuntimeException {
		if( !actions.containsKey( agent ) )
			throw new RuntimeException( "Agent not in the joint action" );
		
		return actions.remove( agent );
	}
	
	/**
	 * Combines this joint action with the specified other joint action. When the
	 * disjoint parameter is true, agents cannot occur in both joint action. If
	 * the parameter is false, the action from the other joint action is set for
	 * the agent.
	 * 
	 * @param ja The joint action to combine with
	 * @param disjoint True to force that the agent sets should be disjoint
	 * @return The new joint action that is the combination of both
	 * @throws RuntimeException if the set is not disjoint but the parameter
	 * value is true
	 */
	public JointAction combineWith( JointAction ja, boolean disjoint ) throws RuntimeException {
		// check if disjoint (for debug purpose)
		if( disjoint ) {
			for( Agent a : actions.keySet( ) )
				if( ja.actions.containsKey( a ) && ja.hasAction( a ) && hasAction( a ) )
					throw new RuntimeException( "Agent A" + a.ID + " is in both joint actions\nJoint actions: " + toString( ) + " | " + ja.toString( ) );
		}
		
		// create a new joint action for the combined set of agents
		final Map<Agent, Action> actions = new HashMap<Agent, Action>( this.actions );
		final JointAction comb = new JointAction( actions.keySet( ), time );
		
		// fill in actions from both joint action
		for( Agent a : this.actions.keySet( ) ) if( hasAction( a ) ) comb.setAction( a, getAction( a ) );
		for( Agent a : ja.actions.keySet( ) ) if( ja.hasAction( a ) ) comb.setAction( a, ja.getAction( a ) );
		
		return comb;
	}
	
	/**
	 * Checks if the specified action is starting through this joint action
	 * 
	 * @param action The action
	 * @return True if the action is starting
	 */
	public boolean contains( Action action ) {
		return getAction( action.getAgent( ) ).equals( action );
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return getTime( );
	}
	
	/**
	 * Joint actions equal if all tasks are equal
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof JointAction) ) return false;
		final JointAction ja = (JointAction) obj;

		// compare action maps
		if( !actions.equals( ja.actions ) ) return false;
		
		return true;
	}
	
	/**
	 * @return The number of agents in the joint action
	 */
	public int size( ) {
		return actions.size( );
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		String str = "{";
		for( Agent a : actions.keySet( ) ) {
			
			final Action act = getAction( a );	
			str += (act != null ? act.toString( ) : "!" + a.toString( ) ) + ", ";
		}
		str = str.substring( 0, str.length( ) - 2 );
		str += ", time: " + time + "}";
		return str;
	}
}
