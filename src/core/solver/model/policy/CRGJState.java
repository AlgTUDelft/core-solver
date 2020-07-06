/**
 * @file CRGStates.java
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
package core.solver.model.policy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import core.model.policy.Agent;
import core.model.policy.JointAction;
import core.solver.CoRe;
import core.solver.base.Solver;
import core.solver.model.crg.CRGState;


/**
 * Collection of local states
 * 
 * @author Joris Scharpff
 */
public class CRGJState {
	/** The current state per agent */
	protected final Map<Agent, CRGState> states;
	
	/** The time in the joint state */
	protected int time;
	
	/**
	 * Creates a new CRGStates collection
	 * 
	 * @param agents The agents in the local state
	 */
	public CRGJState( Collection<Agent> agents ) {
		states = new HashMap<Agent, CRGState>( agents.size( ) );
		for( Agent a : agents )
			states.put( a, null );
		
		time = -1;
	}
	
	/**
	 * Creates a copy of the specified state
	 * 
	 * @param state The state to copy
	 */
	public CRGJState( CRGJState state ) {
		states = new HashMap<Agent, CRGState>( state.states );
		
		time = state.time;
	}

	/**
	 * Retrieves the current state of the agent
	 * 
	 * @param agent The agent
	 * @return The current state
	 */
	public CRGState get( Agent agent ) {
		assert states.containsKey( agent ) : "Agent " + agent + " not part of state " + this;
		return states.get( agent ); 
	}
	
	/**
	 * Sets the state in the factored state, the agent is derived from the local
	 * state object
	 * 
	 * @param localstate The local state to set
	 */
	public void set( CRGState localstate ) {
		assert !(time != -1 && localstate.getTime( ) != time) : "All states must have the same state time";
		assert states.containsKey( localstate.getAgent( ) ) : "Agent not in the scope of the factored state: " + localstate.getAgent( );

		// add the state and make sure the time is set correctly
		states.put( localstate.getAgent( ), localstate );
		time = localstate.getTime( );
	}
	
	/**
	 * Removes the state from the joint state
	 * 
	 * @param localstate The local state to remove
	 */
	public void unset( CRGState localstate ) {
		assert states.containsKey( localstate.getAgent( ) ) : "Agent not in joint state scope";
		assert states.get( localstate.getAgent( ) ).equals( localstate ) : "Local state not in joint state";
		
		states.put( localstate.getAgent( ), null );
	}
	
	
	/**
	 * Checks whether the agent is part of the joint state
	 * 
	 * @param agent The agent
	 * @return True iff the agent is part of this joint state
	 */
	public boolean has( Agent agent ) {
		return states.keySet( ).contains( agent );
	}
		
	/**
	 * Checks if the state is valid by testing if all agents have an assigned
	 * local state
	 * 
	 * @return True iff all agents have a local state 
	 */
	public boolean isValid( ) {
		for( Agent a : states.keySet( ) )
			if( states.get( a ) == null ) return false;
		
		return true;
	}
	
	/**
	 * Determines the set of all new states that can result from executing the
	 * joint action from the state
	 * 
	 * @param state The current joint state
	 * @param ja The joint action
	 * @return Collection of all new states
	 */
	public Collection<CRGJState> getNewStates( CRGJState state, JointAction ja ) {
		final Stack<Agent> agents = new Stack<Agent>( );
		agents.addAll( getAgents( ) );
		
		final Collection<CRGJState> states = new HashSet<CRGJState>( );
		getNewStates( state, ja, states, agents, new CRGJState( getAgents( ) ) );
		return states;
	}
	
	/**
	 * Recursively generates all new states for the remaining agents and adds the
	 * joint state
	 * 
	 * @param state The current joint state
	 * @param ja The joint action
	 * @param states The set of all result states
	 * @param agents The agents remaining
	 * @param curr The joint state that is being constructed
	 */
	protected void getNewStates( CRGJState state, JointAction ja, Collection<CRGJState> states, Stack<Agent> agents, CRGJState curr ) {
		// add state
		if( agents.size( ) == 0 ) {
			states.add( new CRGJState( curr ) );
			return;
		}
		
		// add all new states of the next agent
		final Agent a = agents.pop( );
		for( CRGState s :  ((CoRe)Solver.getRunning( )).getDomain( ).getNewStates( state.get( a ), ja.getAction( a ) ) ) {
			curr.set( s );
			getNewStates( state, ja, states, agents, curr );
			curr.unset( s );
		}
		agents.push( a );
	}
	
	/**
	 * Combines this state with the specified other state
	 * 
	 * @param state The state to combine with'
	 * @return A new state that is the combination of both
	 */
	public CRGJState combine( CRGJState state ) {
		// determine agent set
		final Collection<Agent> agents = new HashSet<Agent>( this.getAgents( ) );
		agents.addAll( state.getAgents( ) );
		
		final CRGJState s = new CRGJState( agents  );
		
		// set states from both states
		for( CRGState local : getStates( ) )
			s.set( local );
		for( CRGState local : state.getStates( ) )
			s.set( local );
		
		assert s.isValid( ) : "The joint state does not contain a state for all agents: " + state;
		return s;
	}
	
	/**
	 * @return The agents in this state
	 */
	public Collection<Agent> getAgents( ) {
		return states.keySet( );
	}
	
	/**
	 * @return The states in this joint state
	 */
	public Collection<CRGState> getStates( ) {
		return states.values( );
	}
	
	/**
	 * @return The time in the state
	 */
	public int getTime( ) {
		return time;
	}
	
	/**
	 * @return The size of the state (number of agents)
	 */
	public int size( ) {
		return states.size( );
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return "{" + states.values( ).toString( ) + "}";
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return time * size( );
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof CRGJState) ) return false;
		final CRGJState s = (CRGJState)obj;
		
		// compare agent sets
		if( !getAgents( ).equals( s.getAgents( ) ) ) return false;
		
		// compare states
		for( Agent a : getAgents( ) )
			if( !get( a ).equals( s.get( a ) ) )
				return false;
		
		return true;
	}
}
