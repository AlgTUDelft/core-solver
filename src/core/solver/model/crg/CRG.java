/**
 * @file CRG.java
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
package core.solver.model.crg;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import core.domains.StateValue;
import core.exceptions.SolverTimeOut;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.solver.CRDomain;
import core.solver.CRStatsCRG;
import core.solver.CoRe;
import core.solver.model.policy.CRGJTransition;
import core.solver.model.policy.ValueBound;
import core.util.Debugable;


/**
 * Conditional return graph
 * 
 * @author Joris Scharpff
 */
public class CRG extends Debugable {
	/** The rewards captured by this CRG */
	protected CRGRewards rewards;
	
	/** The initial state */
	protected CRGState initstate;

	/** State info containing characteristics, available transitions and expected
	 * return bounds */
	protected Map<CRGState, CRGStateInfo> statemap;
	
	/** The CoRe solver */
	protected CoRe core;
	
	/** The domain we are solving */
	protected CRDomain domain;
	
	/** All reward functions involving this agent */
	protected CRGRewards allrewards;
	
	/**
	 * Creates a new CRG for the set of rewards (contains the agent)
	 * 
	 * @param rewards The set of rewards to include in this CRG 
	 */
	public CRG( CRGRewards rewards ) {
		this.initstate = null;
		this.rewards = rewards;
	}
	
	/**
	 * @return The agent modelled by this CRG
	 */
	public Agent getAgent( ) {
		return rewards.getAgent( );
	}

	/**
	 * Builds the CRG for the agent based on the assigned reward functions
	 * 
	 * @param core The CoRe solver reference
	 * @param initstate The initial state of the CRG
	 * @param rewards All reward functions of the problem (for CRI)
	 */
	public void construct( CoRe core, CRGState initstate, Collection<CRGReward> rewards ) throws SolverTimeOut {
		this.core = core;
		domain = core.getDomain( );
		
		// initialise the CRG
		assert this.initstate == null : "CRG already constructed!";
		this.initstate = initstate;
		this.statemap = new HashMap<CRGState, CRGStateInfo>( );
		
		// initialise the Coordination Graph (if required)
		if( core.getSettings( ).useLocalCRI( ) ) {
			// build set of rewards involving this agent
			allrewards = new CRGRewards( getAgent( ) );
			for( CRGReward r : rewards )
				if( r.inScope( getAgent( ) ) )
					allrewards.addReward( r );
		}
		
		// do the actual build!
		build( initstate );
		
		// output states and transitions if debug is enabled
		if( isDebug( ) ) dumpCRG( 0 );
	}
	
	/**
	 * Recursive building of the CRG
	 * 
	 * @param state The current state
	 * @return The bounds on the expected return
	 */
	protected ValueBound build( CRGState state ) throws SolverTimeOut {
		core.CHECK_TIMEOUT( );		
		
		// previously encountered state?
		if( statemap.containsKey( state ) ) {
			dbg_msg( "Previously encountered state " + state );
			getStats( ).duplicates++;
			return getReturnBound( state );			
		}

		// new state
		dbg_msg( "## New state " + state );
		getStats( ).states++;
		
		// terminal state?
		if( domain.isTerminal( state ) ) {
			dbg_msg( "Terminal state " + state );
			getStats( ).terminal++;
			
			// add it to transition map with empty list to 'know it exists'
			addState( state, new CRGStateInfo( true, true ) );
			return setReturnBound( state, new ValueBound( core.getI( ).getEmptyValue( ) ) );
		}
		
		// detect conditional independence
		if( core.getSettings( ).useLocalCRI( ) && isLocallyIndependent( state ) ) {
			dbg_msg( "Locally CRI state " + state );
			getStats( ).independent++;
			
			return new ValueBound( completeOptimally( state ) );
		}
		
		// add the new state to the map
		addState( state, new CRGStateInfo( false, false ) );
		
		// keep track of expected return bounds
		ValueBound bound = ValueBound.emptyBound( );
		
		// get the actions available from this state
		final Collection<Action> actions = domain.getAvailableActions( state );
		dbg_msg( "Generating transitions for actions: " + actions );
		dbg_incSpacing( );
		for( Action action : actions ) {
			dbg_msg( "Action " + action );
			// progress reporting of major branches
			if( core.getSettings( ).showProgress( ) && state.equals( initstate ) )
				System.out.println( "> Branch " + action );
			
			// generate all local transitions
			final Collection<CRGState> newstates = domain.getNewStates( state, action );
			dbg_msg( "> New local states: " + newstates );
			
			// now for every new local state, generate the action dependencies 
			dbg_msg( "Generating dependencies for all transitions" );
			dbg_incSpacing( );
			for( CRGState newstate : newstates ) {
				// create local transition
				final CRGTransition trans = new CRGTransition( state, action, newstate );
				dbg_msg( "Transition: " + trans );
				
				// get the set of dependent actions per agent
				final Map<Agent, Collection<Action>> depact = new HashMap<Agent, Collection<Action>>( );
				for( Agent agent : getScope( ) ) {
					if( agent.equals( getAgent( ) ) ) continue;
					depact.put( agent, domain.getDependentActions( rewards, trans, agent ) );
				}
				
				dbg_msg( "Dependencies: " + depact );
				
				dbg_msg( "Building action tree for " + action );
				dbg_incSpacing( );
				bound = bound.update( buildActionTree( trans, depact ) );
				dbg_decSpacing( );
			}
			dbg_decSpacing( );
		}
		dbg_decSpacing( );
		
		// store the bound in the state and return it		
		return setReturnBound( state, bound );
	}
	
	/**
	 * Recursively builds the action tree for the given transition and dependent
	 * actions
	 * 
	 * @param trans The local transition
	 * @param depact The mapping of dependent actions per agent
	 */
	protected ValueBound buildActionTree( CRGTransition trans, Map<Agent, Collection<Action>> depact ) throws SolverTimeOut {
		// check if there are agents remaining in the map
		if( depact.size( ) == 0 ) {
			// determine the influences per agent
			final Map<Agent, Collection<CRGInfluence>> influence = new HashMap<Agent, Collection<CRGInfluence>>( );
			for( Agent agent : getScope( ) ) {
				if( agent.equals( getAgent( ) ) ) continue;
				influence.put( agent, domain.getTransitionInfluence( rewards, trans, agent ) );
			}
			
			dbg_msg( "Building transition influence tree" );
			dbg_incSpacing( );
			final ValueBound b = buildInfluenceTree( trans, influence );
			dbg_decSpacing( );
			return b;
		}
		
		// remove the first agent from the map
		final Agent agent = depact.keySet( ).iterator( ).next( );
		final Collection<Action> actions = depact.remove( agent );
		
		// update bounds
		ValueBound bound = ValueBound.emptyBound( );
		
		// generate all action tree branches
		for( Action act : actions ) {
			dbg_msg( "> Depency: " + act );
			trans.getDependencies( ).add( act );
			getStats( ).depbranches++;
			bound = bound.update( buildActionTree( trans, depact ) );
			trans.getDependencies( ).remove( act );
		}
		
		// also generate branch for all non-dependent actions
		dbg_msg( "> Non-dependent actions for agent " + agent );
		trans.getDependencies( ).setOther( agent, actions );
		bound = bound.update( buildActionTree( trans, depact ) );
		trans.getDependencies( ).clearOther( agent );
		
		// restore the influence for further iterations
		depact.put( agent, actions );
		
		// return the bound on the return
		return bound;
	}

	/**
	 * Builds the influence tree for the current local transaction and influence
	 * map
	 * 
	 * @param trans The local transition
	 * @param influence The influence map
	 */
	protected ValueBound buildInfluenceTree( CRGTransition trans, Map<Agent, Collection<CRGInfluence>> influence ) throws SolverTimeOut {
		// no more influence to consider?
		if( influence.size( ) == 0 )
			return buildTransition( trans );

		
		// remove the first agent from the map
		final Agent agent = influence.keySet( ).iterator( ).next( );
		final Collection<CRGInfluence> infl = influence.remove( agent );
		
		// collect bound information
		ValueBound bound = ValueBound.emptyBound( );
		
		// generate all action tree branches
		for( CRGInfluence I : infl ) {
			dbg_msg( "> Influence: " + I );
			trans.getInfluences( ).add( I );
			getStats( ).inflbranches++;
			bound = bound.update( buildInfluenceTree( trans, influence ) );
			trans.getInfluences( ).remove( I );
		}
		
		// also generate branch for no influences
		dbg_msg( "> No influence" );
		trans.getInfluences( ).setOther( agent, infl );
		bound = bound.update( buildInfluenceTree( trans, influence ) );
		trans.getInfluences( ).clearOther( agent );
		
		// restore the influence for further iterations
		influence.put( agent, infl );
		return bound;
	}
	
	/**
	 * Completes the transition and adds it to the CRG transition map
	 * 
	 * @param trans The fully-specified transition
	 * @return The return obtained from this transition
	 */
	protected ValueBound buildTransition( CRGTransition trans ) throws SolverTimeOut {
		// add transition
		dbg_msg( "Adding transition " + trans );
		getStats( ).transitions++;
		
		// determine probability
		final double prob = domain.getTransitionProbability( trans );
				
		// copy and set value & probability
		final CRGTransition tr = trans.copy( );
		setReward( tr );
		tr.setProbability( prob );
		
		// add transition to the state info
		final CRGStateInfo info = getStateInfo( trans.getState( ) );
		info.addTransition( tr );
		
		// continue the construction of the CRG, return the state bound plus this
		// transition value
		return new ValueBound( build( trans.getNewState( ) ), tr.getValue( ) );
	}
	
	/**
	 * Completes the remainder of the CRG with only the optimal actions and does
	 * no longer consider dependencies. This can be performed when an independent
	 * state is detected during build.
	 * 
	 * @param state The independent state
	 * @return The expected value of the remaining transitions
	 */
	protected StateValue completeOptimally( CRGState state ) throws SolverTimeOut {
		core.CHECK_TIMEOUT( );
		
		// previously encountered state?
		if( statemap.containsKey( state ) ) {
			dbg_msg( "Previously encountered state " + state );
			getStats( ).duplicates++;
			return getReturnBound( state ).getLower( );			
		}

		// new state
		dbg_msg( "## New state " + state );
		getStats( ).states++;
		
		// terminal state?
		if( domain.isTerminal( state ) ) {
			dbg_msg( "Terminal state " + state );
			getStats( ).terminal++;
			
			// add it to transition map with empty list to 'know it exists'
			addState( state, new CRGStateInfo( true, true ) );			
			return setReturnBound( state, new ValueBound( core.getI( ).getEmptyValue( ) ) ).getLower( );
		}
		
		// add the state to the map
		addState( state, new CRGStateInfo( false, true ) );
		
		// get all available actions and keep best action
		StateValue bestvalue = null;
		Collection<CRGTransition> best_trans = null;
		
		// get the set of available actions and find the best one!
		final Collection<Action> actions = domain.getAvailableActions( state );
		for( Action action : actions ) {
			// get all new states
			final Collection<CRGState> newstates = domain.getNewStates( state, action );
		
			// build and test all transitions
			final Collection<CRGTransition> trans = new HashSet<CRGTransition>( newstates.size( ) );
			StateValue expvalue = core.getI( ).getEmptyValue( );
			for( CRGState newstate : newstates ) {
				// get transition and compute its reward and probability
				final CRGTransition tr = new CRGTransition( state, action, newstate );
				setReward( tr );
				tr.setProbability( domain.getTransitionProbability( tr ) );
				trans.add( tr );
				
				// determine its expected future reward
				final StateValue sv = tr.getValue( ).copy( );
				sv.add( completeOptimally( tr.getNewState( ) ) );
				sv.scale( tr.getProbability( ) );

				// and add that to the total expected reward for this action
				expvalue.add( sv );
			}
			
			// keep only the best transition
			if( bestvalue == null || bestvalue.getTotal( ) - expvalue.getTotal( ) < CoRe.PRECISION ) {
				bestvalue = expvalue;
				best_trans = trans;
			}
		}
		
		// add the state with the optimal transition(s) and return that value
		getStateInfo( state ).setTransitions( best_trans );
		setReturnBound( state, new ValueBound( bestvalue ) );
		return bestvalue;
	}
	
	/**
	 * Adds the state to the map
	 * 
	 * @param state The state
	 * @param info The state info
	 */
	protected void addState( CRGState state, CRGStateInfo info ) {
		assert !statemap.containsKey( state ) : "State already in CRG: " + state;
		statemap.put( state, info );
	}
	
	/**
	 * Retrieves the information for the specified state
	 * 
	 * @param state The state
	 * @return The state information
	 */
	public CRGStateInfo getStateInfo( CRGState state ) {
		assert statemap.containsKey( state ) : "Unknown state " + state;
		return statemap.get( state );
	}
	
	
	/**
	 * Computes and sets the reward of a fully-specified transition
	 * 
	 * @param trans The transition
	 */
	protected void setReward( CRGTransition trans ) {
		// compute reward  for all functions
		StateValue value = null;
		for( CRGReward r : rewards.getRewards( ) ) {
			final StateValue tvalue = r.getReward( trans ); 
			
			if( value == null ) value = tvalue;
			else value.add( tvalue );
		}
		
		// set the reward
		dbg_msg( "Computed transition reward: " + value );
		trans.setValue( value );
	}
	
	/**
	 * Returns the stored transition reward
	 * 
	 * @param tr The transition
	 * @return The reward stored in the CRG
	 */
	public StateValue getReward( CRGTransition tr ) {
		assert statemap.containsKey( tr.getState( ) ) : "Transition start state not in CRG: " + tr.getState( );
		assert statemap.get( tr.getState( ) ).getTransitions( ).contains( tr ) : "Transition " + tr + " not in state transitions: " + statemap.get( tr.getState( ) ).getTransitions( );

		for( CRGTransition t : statemap.get( tr.getState( ) ).getTransitions( ) ) {
			if( t.equals( tr ) )
				return t.getValue( );
		}
		
		assert false : "No matching transition in state for " + tr;
		throw new RuntimeException( "No matching transition in state for " + tr );
	}
	
	/**
	 * Determines the set of actions available from the given local state
	 * 
	 * @param state The local state
	 * @return The set of available actions
	 */
	public Collection<Action> getAvailableActions( CRGState state ) {
		assert statemap.containsKey( state ) : "Unknown state " + state;
		
		// no more transitions from terminal state
		final CRGStateInfo info = getStateInfo( state );
		if( info.terminal ) return new HashSet<Action>( 0 );
		
		// get all actions remaining
		final Collection<CRGTransition> T = statemap.get( state ).getTransitions( );
		
		// determine all available actions by adding those of the transitions
		final Collection<Action> actions = new HashSet<Action>( T.size( ) );
		for( CRGTransition tr : T )
			actions.add( tr.getAction( ) );
		
		return actions;		
	}
	
	/**
	 * Determines the best matching local transition given the joint transition
	 * 
	 * @param jtrans The joint transition
	 * @return The best matching joint transition
	 */
	@SuppressWarnings("all") // caused by the assert
	public CRGTransition getLocalTransition( CRGJTransition jtrans ) {
		assert jtrans.getState( ).getAgents( ).contains( getAgent( ) ) :
			"Agent " + getAgent( ) + " not in joint transition";
		assert statemap.containsKey( jtrans.getState( ).get( getAgent( )) ) : 
			"Local agent state not known: " + jtrans.getState( ).get( getAgent( ) );
		
		// perform a test to verify that only one transition, but only when 
		// assertions are enabled
		boolean docheck = false;
		assert docheck = true; 
		
		// go over all transitions from the agent's local state and find a match
		final Collection<CRGTransition> T = statemap.get( jtrans.getState( ).get( getAgent( ) ) ).getTransitions( );
		CRGTransition trans = null;
		for( CRGTransition t : T ) {
			assert jtrans.getState( ).getAgents( ).contains( getAgent( ) ) :
				"The joint transition does not contain agent " + getAgent( );
			
			// action first, this check is the simplest
			if( !t.getAction( ).equals( jtrans.getJointAction( ).getAction( getAgent( ) ) ) ) continue;
			
			// get the local states and action
			if( !t.getState( ).equals( jtrans.getState( ).get( getAgent( ) ) ) ) continue;
			if( !t.getNewState( ).equals( jtrans.getNewState( ).get( getAgent( ) ) ) ) continue;
			
			// test dependency and influence per agent
			boolean match = true;
			for( Agent a : getScope( ) ) {
				// skip the local agent or out of scope agents
				if( getAgent( ).equals( a ) ) continue;
				
				// check if dependency still holds
				if( jtrans.getState( ).getAgents( ).contains( a ) ) {
					// test dependencies
					if( !t.getDependencies( ).matches( jtrans.getJointAction( ).getAction( a ) ) ) {
						match = false; break;
					}
					if( !t.getInfluences( ).matches( new CRGInfluence( jtrans.getState( ).get( a ), jtrans.getNewState( ).get( a ) ) ) ) {
						match = false; break;
					}
				} else {
					// independent of the agent, require that no dependent action is set
					assert core.getSettings( ).useDecoupleCRI( ) : "Independency between agents cannot occur when CRI is disabled";
					if( t.getDependencies( ).has( a ) ) { match = false; break; }
					if( t.getInfluences( ).has( a ) ) { match = false; break; }
				}
			}			
			if( !match ) continue;

			// all tests succeeded, this is the one!
			assert trans == null : "Duplicate transition match,\nwas: " + trans + "\nnew: " + t;
			if( docheck )
				trans = t;
			else
				return t;
		}
		
		assert trans != null : "No transition found for agent " + getAgent( ) + " that matches " + jtrans + "\nAvailable: " + statemap.get( jtrans.getState( ).get( getAgent( ) ) ).getTransitions( );
		return trans;
	}
	
	/**
	 * Check if the state is locally conditional reward independent
	 * 
	 * @param true Returns true if the state is locally CRI
	 */
	protected boolean isLocallyIndependent( CRGState state ) {
		// check all reward functions involving the agent
		for( CRGReward r : allrewards.getRewards( ) )
			if( !r.localCRI( state ) )
				return false;
		
		return true;
	}

	/**
	 * Retrieves the expected return bound for the given state
	 * 
	 * @param state The state
	 * @return The bound on the return
	 */
	public ValueBound getReturnBound( CRGState state ) {
		assert statemap.containsKey( state ) : "Unknown state " + state;
		return statemap.get( state ).getBounds( );
	}
	
	/**
	 * Sets the return bound for the given state
	 * 
	 * @param state The state
	 * @param bound The expected return of the state
	 * @return The CRGBound object 
	 */
	public ValueBound setReturnBound( CRGState state, ValueBound bound ) {
		assert statemap.containsKey( state ) : "Unknown state " + state;
		statemap.get( state ).setBounds( bound );
		return bound;
	}
	
	/**
	 * @return The scope of the rewards represented by this CRG
	 */
	protected Collection<Agent> getScope( ) {
		return rewards.getScope( );
	}
	
	/**
	 * @return The collection of reward function this CRG covers
	 */
	public Collection<CRGReward> getRewards( ) {
		return rewards.getRewards( );
	}
	
	/**
	 * @return The CoRe stats object for this CRG
	 */
	protected CRStatsCRG getStats( ) {
		return core.getStats( ).forCRG( this );
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode( ) {
		return getAgent( ).hashCode( );
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof CRG) ) return false;
		return ((CRG)obj).getAgent( ).equals( getAgent( ) );
	}
	
	/**
	 * Prints all the states in the CRG for the given time step and then
	 * iterates to the next time step until the horizon is reached
	 * 
	 * @param time The time step to debug
	 */
	protected void dumpCRG( int time ) {
		if( time > core.getI( ).getHorizon( ) ) return;
		
		dbg_msg( "\n" );
		dbg_msg( "States at t = " + time );
		for( CRGState s : statemap.keySet( ) ) {
			if( s.getTime( ) != time ) continue;

			final CRGStateInfo info = statemap.get( s );

			// get the state value bound
			dbg_msg( s );
			dbg_msg( "Status: " + (info.isTerminal( ) ? "TERM" : (info.isIndependent( ) ? "IND" : "" ) ) );
			dbg_msg( "Expected return: " + info.getBounds( ) );
			
			if( !info.isTerminal( ) )
				for( CRGTransition t : info.getTransitions( ) )
					dbg_msg( "> " + t );
		}
		
		dumpCRG( time + 1 );
	}
}
