/**
 * @file Agent.java
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
package core.model.policy;

import java.util.HashSet;
import java.util.Set;

/**
 * Agent of the problem, responsible for a set of tasks
 *
 * @author Joris Scharpff
 */
public class Agent {
	/** The agent ID number */
	public final int ID;
	
	/** The actions it can perform */
	protected Set<Action> actions;
	
	/**
	 * Creates a new agent, the agent is assigned a unique ID automatically that
	 * can also be used for array indexing
	 * 
	 * @param ID The ID of the agent. The ID's MUST be numbered from 0 to N - 1,
	 * because the implementation relies on these for array indexing.
	 */
	public Agent( int ID ) {
		this.ID = ID;
		actions = new HashSet<Action>( );
	}
		
	/**
	 * Adds an action to this agent
	 * 
	 * @param action The action to add 
	 */
	public void addAction( Action action ) {
		actions.add( action );
	}
	
	/**
	 * @return The set of actions
	 */
	public Set<Action> getActions( ) {
		return actions;
	}
	
	/**
	 * Retrieves the action by ID
	 * 
	 * @param ID The action ID
	 * @return The action
	 */
	public Action getAction( int ID ) {
		for( Action a : actions )
			if( a.ID == ID )
				return a;
		
		return null;
	}
	
	/**
	 * @return The number of tasks
	 */
	public int getNumActions( ) {
		return actions.size( );
	}
	
	/**
	 * @return The long description of the agent
	 */
	public String toLongString( ) {
		String str = "|Agent " + ID + "|:";
		for( Action a : actions )
			str += "\n->" + a.toString( );
		return str;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "A" + ID;
	}
	
	/**
	 * Agent equal if their IDs equal
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof Agent) ) return false;
		final Agent a = (Agent) obj;
		
		return ID == a.ID; 
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return ID;
	}
}
