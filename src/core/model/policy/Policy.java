/**
 * @file Policy.java
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
 * @date         1 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.policy;

import java.text.NumberFormat;
import java.util.Set;

import core.domains.Instance;
import core.domains.State;
import core.domains.StateValue;
import core.exceptions.InfeasibleStateException;
import core.exceptions.SolverException;
import core.util.CachedVar;
import core.util.Debugable;
import core.util.Util;

/**
 * Policies are contingent plans that specify for each state the best action to
 * take. This class is abstract because the implementation of policies can be
 * done with various different techniques, mostly best exploited in combination
 * with the stochastic solver of choice. 
 *
 * @author Joris Scharpff
 */
public abstract class Policy extends Debugable {
	/** The I associated with the plan */
	protected final Instance I;
	
	/** The number of states in the policy */
	protected int numreachable;
	
	/** True to debug policy evaluation */
	protected boolean evaldebug;
	
	/** True to show evaluated terminal states */
	protected boolean showterminal;
	
	/** Cached expected policy value */
	protected final CachedVar<StateValue> expval;
	
	/**
	 * Creates a new policy
	 * 
	 * @param instance The instance
	 * @param horizon The policy horizon
	 */
	public Policy( Instance instance ) {
		this.I = instance;
		
		// initialise cached value and reachable to -1 to signal unknown, is lazily
		// evaluated by getExpectedValue
		expval = new CachedVar<StateValue>( );
		numreachable = -1;
		
		// initialise policy debug parameters
		setDebugEvaluation( false );
		setShowTerminalStates( false );
	}
	
	/**
	 * Enables/disables the debugging info of policy evaluation
	 * 
	 * @param enable True to enable
	 */
	public void setDebugEvaluation( boolean enable ) {
		this.evaldebug = enable;
	}
	
	/**
	 * Enables/disables debug display of terminal states
	 * 
	 * @param enable True to enable
	 */
	public void setShowTerminalStates( boolean enable ) {
		this.showterminal = enable;
	}
	
	/**
	 * @return The instance associated with the policy
	 */
	public Instance getInstance( ) {
		return I;
	}
	
	/**
	 * @return The number of states in this policy
	 */
	public int getNumReachable( ) {
		return numreachable;
	}	
	
	/**
	 * Returns the actions that should be started from the current state and time.
	 * 
	 * @param state The current state
	 * @return The joint action
	 * @throws SolverException if the solver failed to query correctly
	 */
	public abstract JointAction query( State state ) throws SolverException;

			
	/**
	 * @return The problem horizon
	 */
	public int getHorizon( ) {
		return I.getHorizon( );
	}
	
	/**
	 * @return The total expected value of the policy over all objectives
	 * @throws SolverException
	 */
	public double getExpectedTotalValue( ) throws SolverException {
		if( !expval.isValid( ) )
			expval.setValue( getExpectedValue( ) );
			
		return expval.getValue( ).getTotal( );
	}
	
	/**
	 * @return The expected value of the policy per objective
	 * @throws SolverException 
	 */
	public StateValue getExpectedValue( ) throws SolverException {
		if( !expval.isValid( ) )
			expval.setValue( getExpectedValue( I ) );
		
		return expval.getValue( );
	}
	
	/**
	 * Computes the expected value of the policy, using the given instance to
	 * build and evaluate states. This assumes that the tasks of both instances
	 * agree on duration and delay duration.
	 * 
	 * @param instance The instance to use
	 * @return The expected value of the policy for the specified instance
	 * @throws SolverException if the evaluation failed
	 */
	public StateValue getExpectedValue( Instance instance ) throws SolverException {
		numreachable = 0;
		return getExpectedValue( instance, instance.getInitialState( ).copy( ), true );
	}
	
	/**
	 * Computes the expected value of the policy from the current state
	 * 
	 * @param instance The instance to use for value evaluation
	 * @param state The state to start from
	 * @param count Counts the number of reachable states in this policy
	 * @return The value of the policy
	 * @throws SolverException 
	 */
	protected StateValue getExpectedValue( Instance instance, State state, boolean count ) throws SolverException {
		// if probability of reaching this state is zero, return empty value
		if( state.getProbability( ) == 0 ) {
			return instance.getEmptyValue( );
		}
		
		if( !state.isFeasible( ) ) throw new SolverException( "Policy contains an invalid state! " + state.toLongString( ) );
		
		// get formatters
		final NumberFormat dec = Util.dec( );
		final NumberFormat perc = Util.perc( 2 );
		
		// this is a terminal state, evaluate its value
		if( state.isTerminal( ) ) {
			if( evaldebug || showterminal ) {
				System.out.println( "*** Evaluating value for leaf node:" );
				System.out.println( state.toLongString( ) );
			}

			//final Plan hist = state.getHistory( ).forInstance( instance );
			final double p = state.getProbability( );
			final StateValue v;
			try {
			 v = state.getValue( );
			} catch( InfeasibleStateException ifs ) {
				throw new SolverException( "Policy contains an infeasible state!" );
			}
			v.scale( p );
			if( evaldebug || showterminal )
				System.out.println( "v: " + v + " (p: " + perc.format( p ) + ")" );
			return v;
		}
		
		// get actions dictated by the policy from this state
		if( evaldebug ) {
			System.out.println( "*** Policy evaluation" );
			System.out.println( "*** Query" );
			System.out.println( state.toLongString( ) );
		}
		final JointAction ja = query( state );
		if( ja == null ) {
			throw new SolverException( "Joint action is null!\n\n" + state.toLongString( ) );
		}
		
		if( evaldebug ) {
			System.out.println("*** Results");
			System.out.println( ja.toString( ) );
		}

		// build all possible new states based upon delay realisations
		final Set<State> states;
		try {
			states = State.getReachable( state, ja );
		} catch( InfeasibleStateException ise ) {
			throw new SolverException( "Policy evaluation reached an infeasible state", ise );
		}
		final StateValue newval = instance.getEmptyValue( );
		numreachable += states.size( );
		if( evaldebug ) {
			System.out.println( "*** New states");
			for( State s : states )
				System.out.println( s.toLongString( ) );
		}
		double prob = 0;
		for( State s : states ) {			
			final StateValue v = getExpectedValue( instance, s, count );
			newval.add( v );
			prob += s.getProbability( ) / state.getProbability( );
		}
		if( Math.abs( 1 - prob ) > 0.0001 ) {
			System.err.println( "State: " + state );
			System.err.println( "Optimal action: " + ja );
			System.err.println( "Next states:");
			for( State s : states ) 
				System.err.println( s );
			throw new SolverException( "State probabilities should sum to 1! (prob: " + dec.format( prob ) + ")" );
		}
		
		// error if no more state is found but this is not a terminal state
		if( states.size( ) == 0 )
			throw new SolverException( "No successive states found in non-terminal state!\n" + state.toString( ) );
		
		return newval;
	}
	
	/**
	 * Compares the values of this policy with that of the given other policy,
	 * all state discrepancies are printed
	 * 
	 * @param policy The other policy to compare against
	 * @throws SolverException if any of both policies is not valid
	 */
	public void compareTo( Policy policy ) throws SolverException {
		compareTo( policy, getInstance( ).getInitialState( ) );
	}
	
	/**
	 * Recursively compares the complete policy set against the other policy
	 * 
	 * @param policy The policy to compare against
	 * @param state The current state to check
	 * @throws SolverException if any of the policies is not valid
	 */
	protected void compareTo( Policy policy, State state ) throws SolverException {
		// if probability of reaching this state is zero, return empty value
		if( state.getProbability( ) == 0 || state.isTerminal( ) )
			return;
		
		if( !state.isFeasible( ) ) throw new SolverException( "Policy contains an invalid state! " + state.toLongString( ) );
						
		// get actions dictated by the policy from this state
		final JointAction ja = query( state );
		final JointAction ja2 = policy.query( state );
		
		// check for match
		if( !ja.equals( ja2 ) ) {
			System.out.println( state );
			final StateValue e1 = getExpectedValue( getInstance(), state, false );
			final StateValue e2 = policy.getExpectedValue( getInstance(), state, false );
			System.out.print( "P1: " + ja + ", EV: " + e1 );
			e1.scale( 1.0 / state.getProbability( ) );
			System.out.println( " V: " + e1 );
			System.out.print( "P2: " + ja2 + ", EV: " + e2 );
			e2.scale( 1.0 / state.getProbability( ) );
			System.out.println( " V: " + e2 );
			System.out.println( );
			return;
		}

		// build all possible new states based upon delay realisations
		final Set<State> states;
		try {
			states = State.getReachable( state, ja );
		} catch( InfeasibleStateException ise ) {
			throw new SolverException( "Policy evaluation reached an infeasible state", ise );
		}
		for( State s : states ) {			
			compareTo( policy, s );
		}
	}
}
