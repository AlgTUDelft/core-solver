/**
 * @file CoReSolver.java
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
 * @date         27 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import core.domains.Instance;
import core.domains.StateValue;
import core.exceptions.SolverTimeOut;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.policy.JointAction;
import core.solver.model.crg.CRG;
import core.solver.model.crg.CRGReward;
import core.solver.model.crg.CRGState;
import core.solver.model.crg.CRGTransition;
import core.solver.model.policy.CRGJState;
import core.solver.model.policy.CRGJTransition;
import core.solver.model.policy.CROptTransition;
import core.solver.model.policy.CRPolicy;
import core.solver.model.policy.CoordGraph;
import core.solver.model.policy.ValueBound;
import core.util.Debugable;
import core.util.ProgressBar;
import core.util.Util;


/**
 * Uses the CRGs to find optimal policies
 * 
 * @author Joris Scharpff
 */
public class CoReSolver extends Debugable {
	/** Reference to the CoRe solver */
	protected final CoRe core;
	
	/** The domain that is solved by this policy */
	protected final CRDomain domain;
	
	/** The optimal actions in each state */
	protected Map<CRGJState, CROptTransition> transmap;
	
	/** The decoupling map, keeps track of potential state splits */
	protected Map<CRGJState, Collection<CRGJState>> decouplemap;
	
	/** The (possibly decoupled) initial state */
	protected Collection<CRGJState> initstate;
	
	/** The current coordination graph */
	protected CoordGraph CG;
	
	/** The progress bar showing solving progress */
	protected ProgressBar progress;
	
	/** The current instance (available for debugging purposes) */
	protected static CoReSolver instance;
	
	/**
	 * Creates a new CRPolicy that is (being) solved by the specified CoRe
	 * instance
	 * 
	 * @param core The CoRe solver
	 */
	public CoReSolver( CoRe core ) {
		this.core = core;
		this.domain = core.getDomain( );
		transmap = new HashMap<CRGJState, CROptTransition>( );
		decouplemap = new HashMap<CRGJState, Collection<CRGJState>>( );
		
		CoReSolver.instance = this;
	}
	
	/**
	 * Solves the problem using the Conditional Return policy search method as
	 * described in the paper. Basically, the procedure is a branch-and-bound 
	 * style search through the policy space but rewards are retrieved from the
	 * previously constructed CRGs and solving is decoupled whenever execution
	 * sequences become independent
	 * 
	 * @return The optimal policy value
	 */
	public StateValue findOptimal( ) throws SolverTimeOut {
		dbg_msg( "Starting CoRe Algorithm at " + (new Date()) );
		
		// build initial coordination graph from rewards
		if( getSettings( ).useDecoupleCRI( ) ) {
			final Collection<Agent> agents = getInstance( ).getAgents( );
			final Collection<CRGReward> rewards = new ArrayList<CRGReward>( );
			for( Agent a : agents )
				rewards.addAll( crg( a ).getRewards( ) );
			CG = new CoordGraph( rewards );
			dbg_msg( "Created coordination graph" );
		}
		
		// get initial state from the domain and factor it
		this.initstate = new HashSet<CRGJState>( );
		final CRGJState inits = domain.factorState( getInstance( ).getInitialState( ) );
		if( !getSettings( ).useDecoupleCRI( ) )
			initstate.add( inits );
		
		// start the solve
		final StateValue value = decoupleCRI( inits );

		// done! update states
		dbg_msg( "Completed! Expected policy value: " + value );
		getStats( ).decoupled = decouplemap.size( );
		long size = 0;
		for( Collection<CRGJState> states : decouplemap.values( ) )
			size += states.size( );
		getStats( ).decouple_size = size / (double) getStats( ).decoupled;
		
		// return the optimal value
		return value;
	}
	
	/**
	 * Updates the coordination graph according to the new joint state, splits
	 * the optimal policy search if possible
	 */
	protected StateValue decoupleCRI( CRGJState state ) throws SolverTimeOut {
		if( !getSettings( ).useDecoupleCRI( ) )
			return findOptimal( state );
		
		// detect possible new CRI
		final int components = CG.size( );
		final Collection<?> removed = CG.update( state, initstate.size( ) == 0 );
		
		// do optimal policy search over all new connected components
		final StateValue sv = getInstance( ).getEmptyValue( );
		final Collection<CRGJState> states = CG.getConnectedComponents( state );
		if( CG.size( ) > components ) {
			dbg_msg( "Decoupled search into components " + CG.getComponents( ) );
			decouplemap.put( state, states );
		}

		if( initstate.size( ) == 0 ) initstate.addAll( states );
		for( CRGJState s : states )
			sv.add( findOptimal( s ) );
		
		// add the edges back again
		CG.restore( removed );
		
		// return combined state value
		return sv;
	}
	
	/**
	 * Finds the best joint transition from the current joint state
	 * 
	 * @param state The current state
	 * @return The optimal expected value from the current state
	 */
	protected StateValue findOptimal( CRGJState state ) throws SolverTimeOut {
		core.CHECK_TIMEOUT( );
		
		// get the current state from the sequence
		dbg_msg( "Finding optimal for state " + state );
		getStats( ).states++;
		getStats( ).statesize += state.size( );
		
		// show progress in the search from the initial state
		final boolean showprog = getSettings( ).showProgress( ) && progress == null;
				
		// check if this is a known state
		if( transmap.containsKey( state ) ) {
			assert transmap.get( state ) != null : "State has been visited before but has no value";
			final CROptTransition info = transmap.get( state );
			getStats( ).visited ++;
			dbg_msg( "Previously optimised state " + info );
			return info.getValue( );
		}
		
		// check if this sequence leads to a terminal joint state
		if( isTerminal( state ) ) { 
			dbg_msg( "Terminal joint state: " + state );
			getStats( ).terminal++;
			transmap.put( state, new CROptTransition( getInstance( ).getEmptyValue( ) ) );
			return getInstance( ).getEmptyValue( );
		}
		
		// add the state to the policy state map
		dbg_msg( "# New state" );
		transmap.put( state, null );
		
		
		// generate all joint actions available from the state
		final Collection<JointAction> jactions = new ArrayList<JointAction>( );
		final Stack<Agent> agents = new Stack<Agent>( );
		agents.addAll( state.getAgents( ) );
		enumJointActions( jactions, state, agents, new JointAction( state.getAgents( ), state.getTime( ) ) );
		assert jactions.size( ) > 0 : "No joint actions for state " + state;
		dbg_msg( "Generated joint actions: " + jactions );
		
		
		// determine all available transitions for the joint actions
		final Map<JointAction, Collection<CRGJTransition>> transitions = getTransitions( state, jactions );
		assert transitions.size( ) > 0 : "No transitions available from state " + state;
		
		// compute bounds on all joint actions
		final Map<JointAction, ValueBound> bounds = new HashMap<JointAction, ValueBound>( );
		StateValue Lmax = computeBounds( transitions, bounds );
		
		// and prune joint actions that are not optimal
		getStats( ).prunedactions_outer += prune( transitions, bounds, Lmax );
		

		// now copy (remaining) available action set so that future prunes may be
		// done while iterating over joint actions
		final Collection<JointAction> available_jact = new HashSet<JointAction>( transitions.keySet( ) );
		
		// initialise progress bar
		if( showprog ) progress = new ProgressBar( "CoRe", 0, available_jact.size( ) );
		
		// determine the optimal transition
		CROptTransition opt_trans = null;
		for( final Iterator<JointAction> ja_iter = available_jact.iterator( ); ja_iter.hasNext( ); ) {
			final JointAction ja = ja_iter.next( );
			if( showprog ) progress.step( 1 );
			
			// check if this action is still available
			final Collection<CRGJTransition> available_trans = transitions.get( ja );
			if( available_trans == null ) {
				// pruned
				continue;
			}

			// sum joint action value over all possible outcomes
			final StateValue jvalue = getInstance( ).getEmptyValue( );
			
			dbg_msg( "> Trying joint action: " + ja );
			getStats( ).jactions++;
			
			
			// generate all new states as a result of this joint action
			for( CRGJTransition trans : available_trans ) {
				// determine the transition value
				final StateValue trvalue = trans.getReward( );
				
				// continue the search
				dbg_incSpacing( );
				trvalue.add( decoupleCRI( trans.getNewState( ) ) );
				dbg_decSpacing( );
				
				// scale the current reward by the transition probability
				trvalue.scale( trans.getProbability( ) );
				
				jvalue.add( trvalue );
			}
			
			dbg_msg( "> Value for " + ja + " is " + jvalue );
			
			// set this as new best?
			if( opt_trans == null || jvalue.getTotal( ) > opt_trans.getValue( ).getTotal( ) ) {
				dbg_msg( "> New optimal action!" );
				opt_trans = new CROptTransition( jvalue, ja, available_trans );
				
				// update Lmax if possible and prune some more
				if( getSettings( ).useBBPruning( ) && getSettings( ).useBBTightening( ) &&
						ja_iter.hasNext( ) &&
						Lmax.getTotal( ) - jvalue.getTotal( ) < CoRe.PRECISION ) {
					dbg_msg( "Updating Lmax from " + Lmax + " to " + jvalue );
					Lmax = jvalue.copy( );
					
					// do an additional pruning round
					getStats( ).prunedactions_inner += prune( transitions, bounds, Lmax );
				}
			}			
		}
		
		// complete the progress bar
		if( showprog ) progress.setDone( );
		
		// save the value and optimal transition and return the best value
		transmap.put( state, opt_trans );
		return opt_trans.getValue( );
	}
	
	/**
	 * Checks if the state is terminal, i.e. all sub-states are terminal
	 * 
	 * @param state The state to test
	 * @return True iff all agent states are terminal
	 */
	protected boolean isTerminal( CRGJState state ) {
		for( CRGState s : state.getStates( ) )
			if( !crg( s.getAgent( ) ).getStateInfo( s ).isTerminal( ) )
				return false;
		
		return true;
	}

	/**
	 * Enumerates all possible joint actions for the remaining set of agents from
	 * the current state
	 * 
	 * @param jactions The current collection of joint actions
	 * @param state The current state
	 * @param agents The remaining agents to generate actions for
	 * @param curr The joint action that is currently under construction
	 */
	protected void enumJointActions( Collection<JointAction> jactions, CRGJState state, Stack<Agent> agents, JointAction curr ) {
		// no more actions to generate
		if( agents.size( ) == 0 ) {
			jactions.add( new JointAction( curr ) );
			return;
		}
		
		// get the next agent
		final Agent agent = agents.pop( );
		for( Action a : crg( agent ).getAvailableActions( state.get( agent ) ) ) {
			curr.setAction( agent, a );
			enumJointActions( jactions, state, agents, curr );
		}
		agents.push( agent );
	}
	
	/**
	 * Determines all joint state transitions from the specified state 
	 * 
	 * @param state The current state
	 * @param jactions The set of available joint actions
	 * @return The map of transitions per state
	 */
	protected Map<JointAction, Collection<CRGJTransition>> getTransitions( CRGJState state, Collection<JointAction> jactions ) {
		// initialise result mapping
		final Map<JointAction, Collection<CRGJTransition>> transitions = new HashMap<JointAction, Collection<CRGJTransition>>( jactions.size( ) );
		
		// determine all available transitions
		for( JointAction ja : jactions ) {
			
			// initialise available transitions list
			final Collection<CRGJTransition> trans = new LinkedList<CRGJTransition>( );
			
			// generate all new states as a result of this joint action
			double total_prob = 0.0;
			for( CRGJState newstate : state.getNewStates( state, ja ) ) {
				// create joint transition
				final CRGJTransition jtrans = new CRGJTransition( state, ja, newstate );
				
				// find a matching transition in each of the CRGs to compute value
				final StateValue trvalue = getInstance( ).getEmptyValue( );
				ValueBound bound = ValueBound.emptyBound( );
				double prob = 1.0;
				for( Agent a : state.getAgents( ) ) {
					// get the local transition for each agent
					final CRGTransition localtrans = crg( a ).getLocalTransition( jtrans );
					
					// combine the reward and probability
					trvalue.add( crg( a ).getReward( localtrans ) );
					prob *= localtrans.getProbability( );
					
					// update bounds
					final ValueBound newbound = crg( a ).getReturnBound( localtrans.getNewState( ) );
					bound = bound.add( newbound );
						
				}
				total_prob += prob;
				
				// set reward, probability and expected value bounds of joint transition
				jtrans.setReward( trvalue.copy( ) );
				jtrans.setProbability( prob );
				jtrans.setBound( bound );

				// add the transition
				trans.add( jtrans );
			}
			
			// check probability
			assert Math.abs( 1.0 - total_prob ) < CoRe.PRECISION : "Joint action probabilities should sum to 1 (is " + Util.dec( ).format( total_prob ) + ")";
			
			// set the transitions for the joint action
			transitions.put( ja, trans );
		}
		
		return transitions;
	}

	/**
	 * Compute the reward bounds for all available transitions
	 * 
	 * @param transitions The map of transitions per joint action
	 * @param bounds The target bounds map
	 * @return The max lower bound (or null if disabled)
	 */
	protected StateValue computeBounds( Map<JointAction, Collection<CRGJTransition>> transitions, Map<JointAction, ValueBound> bounds ) {
		if( !getSettings( ).useBBPruning( ) ) return null;
		
		// keep track of the highest lower bound
		StateValue Lmax = null;
		
		// compute bounds per joint action
		for( JointAction ja : transitions.keySet( ) ) {
			// sum bound over all transitions
			ValueBound bound = ValueBound.emptyBound( );
			for( CRGJTransition trans : transitions.get( ja ) ) {
				ValueBound b = (new ValueBound( trans.getReward( ) )).add( trans.getBounds( ) );
				b = b.scale( trans.getProbability( ) );
				bound = bound.add( b );
			}
			
			// update max lower if required
			if( Lmax == null || (Lmax.getTotal( ) - bound.getLower( ).getTotal( )) < CoRe.PRECISION )
				Lmax = bound.getLower( ).copy( );
			
			// store the total bound for the joint action
			bounds.put( ja, bound );
		}
		
		// return the bounds
		dbg_msg( "Computed future reward bounds, Lmax = " + Lmax );
		return Lmax;
	}

	/**
	 * Prunes all initially sub-optimal joint actions from the transitions list,
	 * BEFORE doing DFS. This is more memory efficient than during search
	 * 
	 * @param transitions The available transitions
	 * @param bounds The joint action bounds
	 * @param Lmax The maximum lower bound
	 * @return The number of pruned joint actions
	 */
	protected int prune( Map<JointAction, Collection<CRGJTransition>> transitions, Map<JointAction, ValueBound> bounds, StateValue Lmax ) {
		if( !getSettings( ).useBBPruning( ) ) return 0;
		
		// do not prune single-action maps, saves a little time
		final int jasize = transitions.size( );
		if( jasize == 1 ) return 0;
		
		getStats( ).prunes++;
		
		// keep track of joint actions to prune
		final List<JointAction> prunelist = new ArrayList<JointAction>( transitions.size( ) );
		final double L = Lmax.getTotal( ) - CoRe.PRECISION;
		
		// check all joint action bounds
		for( JointAction ja : transitions.keySet( ) ) {
			// remove all lower and equal value bounds
			final StateValue U = bounds.get( ja ).getUpper( );
			if( U.getTotal( ) - L < 0 ) {
				dbg_msg( "[B&B] Marked for pruning " + ja + ": " + U + " <= " + Lmax );
				prunelist.add( ja );			
			}
		}

// This is disabled because it does more harm than good
//		
//		// in the rare case that all actions are 'prune-able', i.e. the best upper
//		// bound is equal to Lmax, keep at least one action with U = Lmax
//		if( prunelist.size( ) == transitions.size( ) ) {
//			getStats( ).fullprune++;
//			for( int i = prunelist.size( ) - 1; i >= 0; i-- )
//				if( Math.abs( bounds.get( prunelist.get( i ) ).getUpper( ).getTotal( ) - L ) < PRECISION ) {
//					prunelist.remove( i );
//					break;
//				}
//			assert prunelist.size( ) == transitions.size( ) - 1 : "At least one transition should be considered after pruning";
//		}
		
		// remove all joint actions on the prune list
		for( JointAction ja : prunelist ) {
			transitions.remove( ja );
			bounds.remove( ja );
		}
		dbg_msg( "[B&B] pruned " + prunelist.size( ) + " of the " + jasize + " joint actions " + prunelist );
		
		return prunelist.size( );
	}
	
	/**
	 * Builds a CRPolicy from the result of the solve run
	 * 
	 * @return The policy
	 */
	public CRPolicy buildPolicy( ) {
		assert domain != null : "No domain set in solver";
		assert transmap.size( ) > 0 : "No solve run has been perfomed";
		
		return CRPolicy.fromCoRe( core, transmap, decouplemap, getInitialState( ) );
	}
	
	/**
	 * @return The (collection of) initial state(s)
	 */
	public Collection<CRGJState> getInitialState( ) {
		return initstate;
	}
	
	/**
	 * @return The instance that is being solver
	 */
	protected Instance getInstance( ) {
		return core.getI( );
	}
	
	/** @return The CoRe settings */
	protected CRSettings getSettings( ) {
		return core.getSettings( );
	}
	
	/** @return The CoRe stats */
	protected CRStats getStats( ) {
		return core.getStats( );
	}
	
	/**
	 * Retrieves the CRG of the agent
	 * 
	 * @param agent The agent
	 * @return Its CRG 
	 */
	protected CRG crg( Agent agent ) {
		return core.getCRG( agent );
	}
	
	/**
	 * @return The current coordination graph
	 */
	public CoordGraph getCoordGraph( ) {
		return CG;
	}
	
	/**
	 * Dumps the policy using the print writer
	 *
	 * @param time Recursively dumps the policy
	 */
	public void dumpPolicy( int time ) {
		if( time > core.getI( ).getHorizon( ) ) return;

		dbg_msg( "\n" );
		dbg_msg( "States at t = " + time );
		for( CRGJState s : transmap.keySet( ) ) {
			if( s.getTime( ) != time ) continue;
			
			// get the state info
			dbg_msg( s );
			final CROptTransition info = transmap.get( s );
			dbg_msg( "> " + info );
		}
		
		dumpPolicy( time + 1 );
	}
	
	/**
	 * @return The current instance
	 */
	public static CoReSolver instance( ) {
		return instance;
	}
}
