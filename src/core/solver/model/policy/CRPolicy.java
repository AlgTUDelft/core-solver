/**
 * @file CRPolicy.java
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
 * @date         17 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.model.policy;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import core.domains.Instance;
import core.domains.State;
import core.domains.StateValue;
import core.exceptions.SolverException;
import core.model.policy.Agent;
import core.model.policy.JointAction;
import core.model.policy.Policy;
import core.solver.CRDomain;
import core.solver.CoRe;


/**
 * Policy resulting from a CoRe solve run
 * 
 * @author Joris Scharpff
 */
public class CRPolicy extends Policy {	
	/** The domain that is solved by this policy */
	protected final CRDomain domain;
	
	/** The initial joint state */
	protected CRGJState initstate;
	
	/** The optimal actions in each state */
	protected Map<CRGJState, CROptTransition> statemap;
	
	/**
	 * Creates a new CRPolicy that was solved for the specified CoRe domain
	 * 
	 * @param instance The instance corresponding to the problem
	 * @param domain The CRDomain of this instance
	 * @param initstate The initial joint state
	 */
	private CRPolicy( Instance instance, CRDomain domain ) {
		super( instance );
		
		this.domain = domain;
		this.initstate = null;
		this.statemap = new HashMap<CRGJState, CROptTransition>( );
	}
	
	/**
	 * @see core.sim.oracle.ActionOracle#query(core.domains.State)
	 */
	@Override
	public JointAction query( State state ) throws SolverException {
		// get the factored local state
		final CRGJState s = domain.factorState( state );
		if( !statemap.containsKey( s ) ) {
			System.err.println( "CRGJState: " + s );
			throw new SolverException( "Policy contains no optimal action for state " + state );
		}

		return statemap.get( s ).getOptimalAction( );
	}
	
	@Override
	public StateValue getExpectedValue() throws SolverException {
		return statemap.get( initstate ).getValue();
	}
	
	/**
	 * Creates a policy from the specified CoReSolver result, described by a
	 * map of transitions and the initial state
	 * 
	 * @param core The CoRe solver that found this policy
	 * @param polmap The policy transition map
	 * @param initstate The initial state of the policy
	 * @return The policy
	 */
	public static CRPolicy fromCoRe( CoRe core, Map<CRGJState, CROptTransition> polmap, Map<CRGJState, Collection<CRGJState>> decouplemap, Collection<CRGJState> initstate ) {
		final CRPolicy p = new CRPolicy( core.getI( ), core.getDomain( ) );
		p.initstate = initstate.iterator( ).next( );
		p.statemap.put( p.initstate, polmap.get( p.initstate ) );		
		p.buildPolicy( polmap, decouplemap, new ArrayList<CRGJState>( initstate ) );		
		return p;
	}
	
	/**
	 * Recursively builds the fully-specified policy mapping from the specified
	 * joint state collection
	 * 
	 * @param polmap The solver policy mapping
	 * @param decouplemap The solver decoupling mapping
	 * @param states The collection of (decoupled) joint states
	 */
	protected void buildPolicy( Map<CRGJState, CROptTransition> polmap, Map<CRGJState, Collection<CRGJState>> decouplemap, Collection<CRGJState> states ) {
		// combine all local states into a single joint state
		final List<CRGJState> S = new ArrayList<CRGJState>( states );
		CRGJState state = S.remove( states.size( ) - 1 );
		while( S.size( ) > 0 ) state = state.combine( S.remove( S.size( ) - 1 ) );
		if( initstate == null ) initstate = state;
		
		// check if the horizon has been reached
		if( state.getTime( ) == getInstance( ).getHorizon( ) ) {
			statemap.put( state, null );
			return;
		}
				
		// get the optimal transitions for every state
		final Collection<CROptTransition> transitions = new ArrayList<CROptTransition>( states.size( ) );
		for( CRGJState s : states ) {			
			// get all (potentially decoupled) states
			final Collection<CRGJState> decstates = new ArrayList<CRGJState>( );
			if( decouplemap.containsKey( s ) ) 
				decstates.addAll( decouplemap.get( s ) );
			else
				decstates.add( s );

			// for every decoupled state, get the optimal transition(s)
			for( CRGJState decstate : decstates ) {
				// check for terminal state or transition leading to terminal state
				assert polmap.get( decstate ) != null && !polmap.get( decstate ).isTerminal( ) : "Missing/invalid transition for state " + decstate;

				// add the optimal transition of this state
				transitions.add( polmap.get( decstate ) );
			}
		}
		
		// combine them into a single optimal transition
		final CROptTransition opt_trans = combineTransitions( transitions );
		statemap.put( state, opt_trans );

		// build all combinations of new states, and continue policy build
		final Collection<Collection<CRGJState>> newstates = enumNextStates( transitions );
		for( Collection<CRGJState> s : newstates )
			buildPolicy( polmap, decouplemap, s );
	}

	/**
	 * Combines the set of decoupled optimal transitions into a single joint
	 * transition
	 * 
	 * @param transitions The collection of transitions
	 * @return The optimal joint transition
	 */
	protected CROptTransition combineTransitions( Collection<CROptTransition> transitions ) {
		// go over all transitions and combine them, also sum rewards
		Collection<CRGJTransition> T = new HashSet<CRGJTransition>( );
		//System.out.println( transitions );
		JointAction jact = new JointAction( transitions.iterator( ).next( ).getOptimalAction( ).getTime( ) );
		StateValue value = getInstance( ).getEmptyValue( );		
		for( CROptTransition opt : transitions ) {
			if( opt.isTerminal( ) ) continue;
			
			// add actions and value
			for( Agent a : opt.getOptimalAction( ).getAgents( ) )
				jact.addAgent( a, opt.getOptimalAction( ).getAction( a ) );
			value.add( opt.getValue( ) );
			
			// first collection?
			if( T.size( ) == 0 ) {
				T.addAll( opt.getTransitions( ) );
				continue;
			}
			
			// no, combine with previous
			final Collection<CRGJTransition> Tnew = new HashSet<CRGJTransition>( );
			for( CRGJTransition t : T )
				for( CRGJTransition t2 : opt.getTransitions( ) )
					Tnew.add( t.combine( t2 ) );
			
			T = Tnew;
		}
		
		return new CROptTransition( value.copy( ), jact, T );
	}
	
	/**
	 * Enumerates all combination of states for the set of decoupled states
	 * 
	 * @param transitions The available transitions
	 */
	protected Collection<Collection<CRGJState>> enumNextStates( Collection<CROptTransition> transitions ) {
		// the set of next states per optimal transition
		final List<Collection<CRGJState>> S = new ArrayList<Collection<CRGJState>>( );
		for( CROptTransition t : transitions ) {
			final Collection<CRGJState> s = new HashSet<CRGJState>( );
			for( CRGJTransition tr : t.getTransitions( ) )
				s.add( tr.getNewState( ) );
			S.add( s );
		}
		
		// recursively enumerate all combinations
		final List<Collection<CRGJState>> newstates = new ArrayList<Collection<CRGJState>>( );
		enumNextStates( S, 0, newstates, new HashSet<CRGJState>( ) );		
		return newstates;
	}
	
	/**
	 * Recursively enumerates all sets of new states
	 */
	protected void enumNextStates( List<Collection<CRGJState>> S, int idx, List<Collection<CRGJState>> newstates, Collection<CRGJState> curr ) {
		if( idx == S.size( ) ) {
			newstates.add( new HashSet<CRGJState>( curr ) );
			return;
		}
		
		// enumerate all combinations for the current index
		for( CRGJState state : S.get( idx ) ) {
			curr.add( state );
			enumNextStates( S, idx + 1, newstates, curr );
			curr.remove( state );
		}
	}
	

	/**
	 * Dumps the policy using the print writer
	 *
	 * @param pw The print writer
	 * @param horizon The planning horizon
	 * @param time Recursively dumps the policy
	 */
	public void dumpPolicy( PrintStream pw, int horizon, int time ) {
		if( time > horizon ) return;
	
		pw.println( "States at t = " + time );
		for( CRGJState s : statemap.keySet( ) ) {
			
			if( s.getStates( ).iterator( ).next( ).getTime( ) != time ) continue;
			
			// get the state info
			pw.println( s );
			final CROptTransition info = statemap.get( s );
			pw.println( "> " + info );
		}
		
		pw.println( );
		
		dumpPolicy( pw, horizon, time + 1 );
	}
}
