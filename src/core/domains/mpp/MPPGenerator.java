/**
 * @file Generator.java
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
package core.domains.mpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.exceptions.GeneratorException;
import core.model.function.ConstFunction;
import core.model.function.Function;
import core.model.function.TabularFunction;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.sharedreward.SharedActionReward;
import core.util.RandGenerator;

/**
 * The MPP I generator, can be run from command line through MPPGen
 *
 * @author Joris Scharpff
 */
// TODO make constants settable
public class MPPGenerator {
	/** The random generator */
	protected final RandGenerator rand;
	
	/** The default minimum and maximum revenue factor (relative to cost) */
	private double[] DEF_REV_FACT = new double[ ] { 3.0, 4.0 };
	
	/** The default minimum and maximum task costs (constant) */
	private double[] DEF_COST = new double[ ] { 10, 50 };
		
	/** The default minimum and maximum social cost */
	private double[] DEF_SOCIAL_COST = new double[] { 50, 100 };

	/** The default minimum and maximum social cost */
	private double[] DEF_DEL_PROB = new double[] { 0, 1.0 };	
	
	/** The default random cost function factor of base cost */
	private double MAX_COST_FACTOR = 1.0;
	
	/** The cost function type to generate */
	protected CostType costfunctype;
	
	/** Available cost function model types */
	public enum CostType {
		/** Random numbers */
		Random,
		/** Random offset from base */
		RandomOffset,
		/** Sinusoid function with peaks at start and eend (seasons) */
		Sinusoid;
	}
	
	/**
	 * Creates a new I generator with the specified random seed
	 * 
	 * @param seed The random seed
	 */
	public MPPGenerator( long seed ) {
		rand = new RandGenerator( seed );
	}
	
	/**
	 * Sets the min and max revenue factors
	 * 
	 * @param min
	 * @param max
	 */
	public void setRevFactor( double min, double max ) {
		DEF_REV_FACT = new double[] { min, max };
	}
	
	/**
	 * Sets the minimum and maximum task costs
	 * 
	 * @param min
	 * @param max
	 */
	public void setCosts( double min, double max ) {
		DEF_COST = new double[] { min, max };
	}
	
	/**
	 * Sets the minimum and maximum network costs
	 * 
	 * @param min
	 * @param max
	 */
	public void setNetworkCosts( double min, double max ) {
		DEF_SOCIAL_COST = new double[] { min, max };
	}
	
	/**
	 * Sets the minimum and maximum delay probabilities for tasks
	 * 
	 * @param min
	 * @param max
	 */
	public void setDelayProbabilities( double min, double max ) {
		DEF_DEL_PROB = new double[] { min, max };
	}
	
	/**
	 * Sets the maximal cost variance factor in random/sinus generation
	 * 
	 * @param factor The cost factor
	 */
	public void setCostFactor( double factor ) {
		MAX_COST_FACTOR = factor;
	}
	
	
	
	/**
	 * Generates an instance for the specified instance parameters
	 * 
	 * @param IP The instance parameters
	 * @return The instance
	 * @throws GeneratorException
	 */
	public MPPInstance generate( MPPInstanceParameters IP ) throws GeneratorException {
		return generate( IP, CostType.Sinusoid );
	}
	
		
	/**
	 * Generates an instance for the specified instance parameters
	 * 
	 * @param IP The instance parameters
	 * @param costtype The cost type functions 
	 * @return The instance
	 * @throws GeneratorException
	 */
	public MPPInstance generate( MPPInstanceParameters IP, CostType costtype ) throws GeneratorException {
		this.costfunctype = costtype;
		final int hrem = IP.getH( ) - IP.getNoops( );
		
		// generate agents
		final Set<Agent> agents = new HashSet<Agent>( IP.getN( ) );
		for( int i = 0; i < IP.getN( ); i++ ) {
			final Agent agent = new Agent( i );
			agents.add( agent );
			
			// randomly split the time into a regular and delayed time such that
			// there always is enough time to complete all tasks
			int maxtries = 10000;
			int regdur = -1;
			do {
				if( maxtries == 0 ) throw new GeneratorException( "Failed to generate the instance" );
				maxtries--;
				
				regdur = rand.randInt( IP.getA( ), hrem );
			} while( IP.getNumDelayed( ) + regdur > hrem );
		
			// draw task durations randomly
			try {
				final int[] dur = rand.randIntArray( regdur, IP.getA( ), false );
				final int[] deldur = (IP.getNumDelayed( ) > 0 ? rand.randIntArray( hrem - regdur, IP.getNumDelayed( ), false ) : new int[0]);

				// fill in zeroes for non-delayed tasks and shuffle
				final List<Integer> del = new ArrayList<Integer>( );
				for( int d : deldur ) del.add( d );
				while( del.size( ) < IP.getA( ) ) {
					final int insertidx = rand.randInt( 0, del.size( ) );
					if( insertidx == del.size( ) )
						del.add( 0 );
					else
						del.add( insertidx, 0 );
				}
				
				// generate actions for the agent with random 'default' properties
				for( int a = 0; a < IP.getA( ); a++ )
					agent.addAction( genTask( agent, IP.getH( ), a, dur[ a ], del.get( a ), IP.mustComplete( ) ) );
			} catch( IllegalArgumentException ia ) {
				ia.printStackTrace( );
				throw new GeneratorException( ia.getMessage( ) );
			}
		}
		
		// create the I
		final MPPInstance I = new MPPInstance( agents, IP );
		I.setGenerationSeed( rand.getSeed( ) );
		return I;
	}
	
	/**
	 * Generates a 'default' tasks with random properties but fixed durations
	 * 
	 * @param agent The agent responsible for the task
	 * @param h The instance horizon
	 * @param ID The task ID
	 * @param duration The task duration
	 * @param delduration The task delay duration
	 * @param noprofit True to set 0 profit, used in cost minimisation problems
	 * @return The task
	 */
	private Task genTask( Agent agent, int h, int ID, int duration, int delduration, boolean noprofit ) {		
		// random revenue and cost
		final double cost = rand.randDbl( DEF_COST );
		final double revenue = (noprofit ? 0 : cost * rand.randDbl( DEF_REV_FACT ) * duration);
		final Function costfunc;
		switch( costfunctype ) {
			default:
			case Random:			
				costfunc = randomCostFunction( DEF_COST, h );
				break;

			case RandomOffset:			
				costfunc = randomCostFunction( cost, MAX_COST_FACTOR, h );
				break;

			case Sinusoid:
				costfunc = sinusoidCostFunction( cost, MAX_COST_FACTOR, h );
				break;
		}
		
		// random delay probability and duration 
		final double delprob = rand.randDbl( DEF_DEL_PROB );

		return new Task( agent, ID, revenue, costfunc, duration, delprob, delduration );
	}	

	/**
	 * Equal to genSCR( I, SC, 2, maxK )
	 *  
	 * @param I The instance
	 * @param SC The number of social cost rules
	 * @param maxK The maximal rule cardinality
	 */
	public void genNetworkReward( MPPInstance I, int SC, int maxK ) {
		genNetworkReward( I, SC, 2, maxK );
	}

	
	/**
	 * Generates a 'default' network cost model with the specified number of rules
	 * of a given minimal and maximal cardinality. Note that this generation does
	 * not ensure that all agents are coupled or that constraints of given
	 * cardinality actually exist, this depends on the random process. This
	 * functions sets the social cost model in the instance.
	 * 
	 * @param I The I to generate for
	 * @param rules The number of network cost rules
	 * @param minK The minimum constraint cardinality
	 * @param maxK The maximum constraint cardinality
	 */
	public void genNetworkReward( MPPInstance I, int rules, int minK, int maxK ) {
		assert (minK > 1) : "Cardinality must be a positive integer > 1";
		assert (maxK <= I.getN( )) : "Cardinality cannot exceed number of agents";
		assert (minK <= maxK) : "Minimum cardinality must be smaller than maximum value";
		
		// create the social cost rule model
		final SharedActionReward netwrewards = new SharedActionReward( I );		
		
		// Las Vegas: max number of retries else give up
		final int MAX_TRIES = 10000;

		// in the 2D case we use a matrix to generate more efficiently
		if( maxK == 2 ) {						
			// determine maximum number of constraints
			int A = 0;
			@SuppressWarnings("unused") // TODO extend to K > 2
			int maxSC = 1;
			for( Agent a : I.getAgents( ) ) {
				A += a.getNumActions( );
				maxSC *= a.getNumActions( );
			}
			
			// FIMXE
			//if( SC > maxSC )
			//	throw new RuntimeException( "Maximum number of SC constraints for this instance is " + maxSC );

			final boolean[][] connected = new boolean[A][A];
			final List<Task> actions = new ArrayList<Task>( I.getTasks( ) );
			int rem = rules;
			while( rem > 0 ) {				
				boolean next = false;
				int maxtries = MAX_TRIES;
				
				// get the first task that can still connect to another task, starting
				// from a random task
				int tidx = rand.randInt( 0, A - 1 ) - 1;
				do {
					maxtries--;
					if( maxtries <= 0 )
						throw new RuntimeException( "Unable to generate required number of network constraints" );
					
					// get next task, no connection possible
					tidx = (tidx + 1) % A;
					
					// connect to a random other task, if possible
					int t2idx = rand.randInt( 0, A - 1 ) - 1;
					int tries = 10000;
					final int startidx = t2idx;
					do {
						tries--;
						if( tries == 0 )
							break;

						t2idx = (t2idx + 1) % A;
						
						if( !connected[tidx][t2idx]) {
							final Task t1 = actions.get( tidx );
							final Task t2 = actions.get( t2idx );
							
							if( t1.getAgent( ).equals( t2.getAgent( ) ) )
								continue;
							
							// connect the two
							connected[tidx][t2idx] = true;
							connected[t2idx][tidx] = true;
							
							netwrewards.addRule( t1, t2, new ConstFunction( -rand.randDbl( DEF_SOCIAL_COST ) ) );
							next = true;
						}
					} while( next == false && (startidx != t2idx) );
				} while( next == false );
								
				rem--;
			}
			
			return;
		}
		
		// generate new constraints randomly
		int rem = rules;
		while( rem > 0 ) {
			// pick a cardinality at random
			int c = rand.randInt( minK, maxK );
			final Set<Action> rule = new HashSet<Action>( c );
			do {
				// pick a random task and check if it does not belong to a player
				// already in the set
				final List<Action> tlist = new ArrayList<Action>( I.getActions( ) );
				final Action a = tlist.get( rand.randInt( 0, I.getA( ) - 1 ) );
				
				// check if this task is not of a player already in the rule
				boolean ok = true;
				for( Action act : rule )
					ok &= (act.getAgent( ) != a.getAgent( ));
				
				// next task if okay
				if( ok ) {
					rule.add( a );
					c--;
				}
				
			} while( c > 0 );
			
			// add the rule, only decrease remaining on success
			if( netwrewards.addRule( rule, new ConstFunction( -rand.randDbl( DEF_SOCIAL_COST ) ) ) )
				rem--;
		}
	}
	
	/**
	 * Generates a tabular cost function for the task based
	 * 
	 * @param basecost The base cost per week
	 * @param maxfact The maximum factor of the base cost
	 * @param tablesize The tabular function table size
	 * @return The tabular cost function 
	 */
	public TabularFunction randomCostFunction( double basecost, double maxfact, int tablesize ) {
		// generate values table
		final double[] values = new double[ tablesize ];
		for( int i = 0; i < tablesize; i++ )
			values[ i ] = rand.randDbl( DEF_COST );
		
		return new TabularFunction( values );
	}
	
	/**
	 * Generates a fully-connected but acyclic reward structure
	 * 
	 * @param I The instance to generate it for
	 * @param IP The instance parameters
	 * @param K The number of dependencies between two agents
	 */
	public void genNetworkRewardAcyclic( MPPInstance I, int K ) {
		final int A = I.getAgents( ).iterator( ).next( ).getNumActions( );
		final int N = I.getN( );
		assert K <= A * A : 
			"Too many inter-agent connections " + K + " (max " + (A * A) + ")";  
					
		// create the social cost rule model
		final SharedActionReward SC = new SharedActionReward( I );
		
		// create a random spanning tree
		final List<Agent> agents = new ArrayList<Agent>( );
		agents.addAll( I.getAgents( ) );
		Collections.shuffle( agents, rand.getRandomGenerator( ) );
		
		for( int idx = 1; idx < N; idx++ ) {
			// connect the next agent to a random agent from the list so far
			final Agent a1 = agents.get( idx );
			final Agent a2 = agents.get( rand.randInt( idx - 1 ) );
			
			// generate all action pairs
			final List<Action[]> pairs = new ArrayList<Action[]>( A * A );
			for( Action act1 : a1.getActions( ) )
				for( Action act2 : a2.getActions( ) )
					pairs.add( new Action[] { act1, act2 } );
			
			// shuffle them and pick K of them
			Collections.shuffle( pairs, rand.getRandomGenerator( ) );
			for( int i = 0; i < K; i++ )
				SC.addRule( pairs.get( i )[0], pairs.get( i )[1], new ConstFunction( -rand.randDbl( DEF_SOCIAL_COST ) ) );
		}
	}

	
	
	/**
	 * Generates a tabular cost function for the task with random values
	 *
	 * @param range The min and max of the random range
	 * @param tablesize The tabular function table size
	 * @return The tabular cost function 
	 */
	public TabularFunction randomCostFunction( double[] range, int tablesize ) {
		// generate values table
		final double[] values = new double[ tablesize ];
		for( int i = 0; i < tablesize; i++ )
			values[ i ] = rand.randDbl( range );
		
		return new TabularFunction( values );
	}
	
	/**
	 * Generates a sinusoidal cost function 
	 * 
	 * @param basecost The base cost that is multiplied by the sinusoid
	 * @param maxamplitude The maximum amplitude, the amplitude itself will be
	 * drawn randomly
	 * @param horizon The problem horizon
	 * @return A tabular function that contains the sinusoid values for each time
	 * step
	 */
	public TabularFunction sinusoidCostFunction( double basecost, double maxamplitude, int horizon ) {
		// compute period factor (game time should equal two periods)
		final double period = (1 / (2 * Math.PI)) * horizon;
		
		// amplitude is the maximum factor of 'normal' cost
		final double ampl = rand.randDbl( ) * maxamplitude;
		
		final double values[] = new double[ horizon ];
		for( int i = 0; i < horizon; i++ )
			values[i] = (1.0 + ampl * Math.cos( i / period ) ) * basecost;
		
		return new TabularFunction( values );
	}
}
