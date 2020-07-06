/**
 * @file State.java
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
 * @date         3 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.exceptions.InfeasibleStateException;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.policy.JointAction;
import core.model.policy.Plan;
import core.model.policy.RealisedJAction;
import core.util.CachedVar;
import core.util.Util;

/**
 * Encodes a state of the MPP
 *
 * @author Joris Scharpff
 */
public abstract class State {
	/** The instance */
	protected final Instance I;
	
	/** The global state time */
	protected int time;
	
	/** The probability of reaching the state */
	protected double probabilty;
	
	/** The state history */
	protected final Plan history;
	
	/** Cached hash code */
	protected final CachedVar<Integer> hashCode;
	
	/**
	 * Creates a new (initial) state for the instance
	 * 
	 * @param instance The instance
	 * @param time The (initial) time
	 * @param prob The state probability
	 * @param history The state history
	 */
	protected State( Instance instance, int time, double prob, Plan history ) {
		this.I = instance;
		this.time = time;
		this.history = history;
		
		probabilty = prob;
		hashCode = new CachedVar<Integer>( );
	}
	
	/**
	 * Makes a copy of the state
	 * 
	 * @return The new state
	 */
	public abstract State copy( );
	
	/**
	 * Copies the state properties
	 * 
	 * @param state The state to copy from
	 */
	protected void copyState( State state ) {		
		// copy hash code
		if( state.hashCode.isValid( ) )
			hashCode.setValue( state.hashCode.getValue( ) );
		else
			hashCode.invalidate( );
	}
			
	/**
	 * @return The associated I
	 */
	public Instance getI( ) {
		return I;
	}
	
	/**
	 * @return The horizon 
	 */
	public int getHorizon( ) {
		return I.getHorizon( );
	}
	
	/**
	 * @return The state history
	 */
	public Plan getHistory( ) {
		return history;
	}
	
	/**
	 * @return The initial state time
	 */
	protected int getInitT( ) {
		return getI( ).getInitialState( ).getTime( );
	}
	
	/**
	 * Retrieves the state value
	 * 
	 * @return The value that is obtained by the plan
	 * @throws InfeasibleStateException if the state is infeasible
	 */
	public abstract StateValue getValue( ) throws InfeasibleStateException;


	/**
	 * Retrieves the shared reward in this state
	 * 
	 * @return The sum of shared rewards in this state
	 */
	public double getSharedReward( ) {
		if( !getI( ).hasSharedReward( ) )
			return 0;
		else
			return getI( ).getSharedReward( ).getReward( this );
	}
	
	/**
	 * @return The probability of reaching this state
	 */
	public double getProbability( ) {
		return probabilty;
	}
	
	/**
	 * @return The global time in the state
	 */
	public int getTime( ) {
		return time;
	}
	
	/**
	 * Adds the specified amount of time
	 * 
	 * @param t The amount to add
	 */
	protected void addTime( int t ) {
		time += t;
		hashCode.invalidate( );
	}
	
	/**
	 * Determines the set of actions that can be executed by the agent from the
	 * current state. By default it returns all actions.
	 * 
	 * @param agent The agent
	 * @return The actions available to the agent in this state
	 */
	public Set<Action> getAvailableActions( Agent agent ) {
		return new HashSet<Action>( agent.getActions( ) );
	}
	
	/**
	 * Determines the list of all unique joint actions that can be executed by
	 * the agent from the current state. By default it enumerates the list of
	 * joint actions that result from getAvailableActions( agent ).
	 * 
	 * @return The list of all joint actions
	 */
	public List<JointAction> getAvailableJointActions( ) {
		final List<JointAction> actions = new ArrayList<JointAction>( );
		enumJointActions( actions, new ArrayList<Agent>( getI( ).getAgents( ) ), new JointAction( getTime( ) ) );
		return actions;
	}
	
	/**
	 * Enumerates all possible joint actions
	 * 
	 * @param actions The list of all joint actions so far
	 * @param agents The remaining agents to make a decision for
	 * @param curr The current joint action so far
	 */
	private void enumJointActions( List<JointAction> actions, List<Agent> agents, JointAction curr ) {
		// check if there are more agents to decide for
		if( agents.size( ) == 0 ) {
			// add the current joint action
			if( canExecute( curr ) )
				actions.add( new JointAction( curr ) );
			return;
		}
		
		// decide for the current agent
		final Agent a = agents.remove( agents.size( ) - 1 );
		for( Action act : getAvailableActions( a ) ) {
			curr.addAgent( a, act );
			enumJointActions( actions, agents, curr );
			curr.removeAgent( a );
		}
		
		// re-add agent
		agents.add( a );
	}
	
	/**
	 * Fast check whether the state is a terminal state
	 * 
	 * @return True iff the state is a terminal state of the problem
	 */
	public boolean isTerminal( ) {
		return time == getHorizon( );
	}
	
	/**
	 * Checks if the state is a feasible state
	 * 
	 * @return True iff the state is feasible
	 */
	public boolean isFeasible( ) {
		return time <= I.getHorizon( );
	}
	
	/**
	 * Checks if the execution of the joint action can lead to an invalid new
	 * state
	 * 
	 * @param ja The joint action
	 * @return True if the action can always be executed safely
	 */
	public boolean canExecute( JointAction ja ) {
		// simply check whether the time limit has not been reached
		return time < I.getHorizon( );
	}
	
	/**
	 * Creates the new state that corresponds from executing the realised action
	 * from the current state
	 * 
	 * @param actions The realised actions
	 * @return The new state that results
	 * @throws InfeasibleStateException if the execution results in an invalid
	 * new state
	 */
	public final State execute( RealisedJAction actions ) throws InfeasibleStateException {
		// check if the joint action can be executed
		if( !canExecute( actions ) )
			throw new InfeasibleStateException( this, "Executing joint action results in an infeasible state (action: " + actions + ")" );
		
		// first copy the state
		final State newstate = copy( );
		
		// perform the basic state operations
		newstate.addTime( 1 );
		newstate.getHistory( ).setRealised( actions );
		newstate.probabilty *= actions.getProbability( );
//		System.out.println( this + " -> " + newstate );
//		System.out.println( probabilty + ", " + actions.getProbability( ) + ", " + newstate.probabilty );
		
		// perform domain specific operations and return the new state
		if( !execute( actions, newstate ) )
			throw new InfeasibleStateException( this, "New state is not feasible!" );
		return newstate;
	}
	
	/**
	 * Performs domain specific execution of the joint action that results in the
	 * specified new state. Implementing functions can alter the newstate if
	 * necessary, by default this function does nothing.
	 * 
	 * @param actions The realised joint action
	 * @param newstate The new state that results from this joint action
	 * @return True iff the new state is valid
	 */
	public boolean execute( RealisedJAction actions, State newstate ) {
		// do nothing, implementing classes can override this for domain specific
		// execution
		return true;
	}
	
	/**
	 * Retrieves the set of actions that were in execution at the specified time.
	 * By default, this returns the set of actions that is in the history at the
	 * specified time.
	 * 
	 * @param time The time
	 * @return The realised action that was executed
	 */
	public Set<Action> getExecuting( int time ) {
		return getHistory( ).getPlanned( time ).toSet( );
	}
	
	/**
	 * Retrieves the realised action that was executed at the specified time
	 * 
	 * @param time The time
	 * @return The realised joint action or null if this time has not been
	 * executed yet
	 */
	public RealisedJAction getExecuted( int time ) {
		if( time >= getTime( ) )
			return null;
		else
			return getHistory( ).getRealised( time );
	}

	/**
	 * Creates a list of next states that can be reached from the current for all
	 * possible realisations of the specified tasks
	 * 
	 * @param state The start state
	 * @param ja The joint action to take from this state
	 * @return The list of unique states that can be reached by starting these
	 * tasks jointly from this state for all realisations
	 * @throws InfeasibleStateException if the policy can reach an infeasible
	 * state
	 */
	public static Set<State> getReachable( State state, JointAction ja ) throws InfeasibleStateException {
		if( !state.canExecute( ja ) ) return new HashSet<State>( );
			
		// get all realisable actions and recurse on them
		final Set<State> states = new HashSet<State>( );
		for( RealisedJAction ra : RealisedJAction.allForJointAction( ja ) ) {
			states.add( state.execute( ra ) );
		}
		return states;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		final NumberFormat perc = Util.perc( 3 );
		return "[t = " + time + "] (" + perc.format( probabilty ) + ")";
	}
	
	/**
	 * Long description of the state
	 * 
	 * @return The complete state description
	 */
	public String toLongString( ) {
		final NumberFormat perc = Util.perc( 3 );
		String str = "[State]\n";
		str += "Time: " + time + "\n";
		str += "Probability: " + perc.format( probabilty ) + "\n";
		
		str += "\n" + getHistory( ).toString( );
		return str;		
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode( ) {
		if( !hashCode.isValid( ) ) {
			hashCode.setValue( computeStateHash( ) );
		}
		return hashCode.getValue( );
	}
	
	/**
	 * Computes the state hash code, the result is cached
	 * 
	 * @return The hash code
	 */
	public abstract int computeStateHash( );
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof State) ) return false;
		final State s = (State) obj;
		
		if( time != s.time ) return false;
		if( !Util.doubleEq( probabilty, s.probabilty ) ) return false;
		if( !getHistory( ).equals( s.getHistory( ) ) ) return false;
		
		return true;
	}
}
