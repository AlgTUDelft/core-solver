/**
 * @file CoRe.java
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
package core.solver;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.domains.Instance;
import core.domains.StateValue;
import core.exceptions.SolverException;
import core.exceptions.SolverTimeOut;
import core.model.policy.Agent;
import core.model.policy.Policy;
import core.solver.base.Solver;
import core.solver.base.SolverStats;
import core.solver.model.crg.CRG;
import core.solver.model.crg.CRGReward;
import core.solver.model.crg.CRGRewards;
import core.solver.model.policy.CRGJState;
import core.solver.model.policy.CRPolicy;


/**
 * Conditional Return Policy Search 
 * 
 * @author Joris Scharpff
 */
public class CoRe extends Solver {
	/** The domain that is being solved */
	protected CRDomain domain;
	
	/** The solver configuration */
	protected CRSettings settings;
	
	/** The CRGs per agent */
	protected Map<Agent, CRG> crgs; 
	
	/** The actual CoRe solver (main loop) */
	protected CoReSolver coresolver;
	
	/** The solver precision for floats */
	public static final double PRECISION = 0.00000001;	
	
	/**
	 * Creates a new instance of the solver
	 * 
	 *  @param domain The (generic) domain that is to be solved
	 */
	public CoRe( CRDomain domain ) {
		setDomain( domain );
		settings = new CRSettings( );
	}
	
	/**
	 * @see core.solver.base.Solver#initStats()
	 */
	@Override
	protected SolverStats initStats( ) {
		return new CRStats( );
	}
	
	/**
	 * @see core.solvers.SolvesStochastic#solveStochastic(core.domains.Instance)
	 */
	@Override
	public Policy solve( Instance instance ) throws SolverException, SolverTimeOut {
		setInstance( instance );
		stats.clear( );
		
		// prepare debugging directory
		if( isDebug( ) ) {
			final File dbgdir = getSettings( ).getDebugDirectory( );
			if( dbgdir != null ) {
				if( dbgdir.exists( ) && !dbgdir.isDirectory( ) )
					throw new SolverException( "Not a valid directory " + dbgdir.getAbsolutePath( ));
				if( !dbgdir.exists( ) && !dbgdir.mkdirs( ) )
					throw new SolverException( "Cannot create debug directory '" + dbgdir.getAbsolutePath( ) + "'");
			}
		}
		
		msg(  "==[CoRe Solver starting]==" );
		
		dbg_msg( "> Pre-process start" );
		PREPROCESS_START( );
		preprocess( );
		PREPROCESS_END( );
		dbg_msg( "> Pre-processing completed in " + getStats( ).getPreProcTime( ) + " msec\n" );
		
		dbg_msg( "> Starting solve run" );
		SOLVE_START( );
		solve( );
		SOLVE_END( );
		dbg_msg( "> Solve completed in " + getStats( ).getSolveTime( ) + " msec" );	
		
		dbg_msg( "> Post-process start" );
		PREPROCESS_START( );
		final CRPolicy pol = postprocess( );
		PREPROCESS_END( );
		dbg_msg( "> Post-processing completed in " + getStats( ).getPostProcTime( ) + " msec\n" );
		
		
		return pol;
	}
	
	/**
	 * Pre-process the instance, create CRGs
	 */
	protected void preprocess( ) throws SolverException, SolverTimeOut {		
		// first construct all reward functions
		final List<CRGReward> rewards = domain.createRewards( );
		dbg_msg( "Created domain reward functions" );

		// the assign the rewards according to the chosen heuristic
		final Map<Agent, CRGRewards> assignment = domain.assignRewards( rewards );
		msg( "Assignment of reward functions:" );
		msg( assignment.toString( ) );
		msg( "" );
		
		// create the CRGs using the reward assignment (do not build yet)
		crgs = new HashMap<Agent, CRG>( assignment.size( ) );
		for( Agent a : assignment.keySet( ) )
			crgs.put( a, new CRG( assignment.get( a ) ) );		

		// generate CRGs
		dbg_msg( "Generating Conditional Return Graphs" );
		
		// get initial state from the domain and factor it
		final CRGJState initstate = getDomain( ).factorState( getI( ).getInitialState( ) );
		
		// build all CRGs
		for( CRG c : crgs.values( ) ) {
			// setup debug stream
			PrintStream ps = null;
			File crg_dbg = null;
			if( isDebug( ) && getSettings( ).getDebugDirectory( ) != null ) {
				 crg_dbg = new File( getSettings( ).getDebugDirectory( ), c.getAgent( ) + ".crg" 	);
				try {
					ps = new PrintStream( crg_dbg );
					c.setDebug( isDebug( ) );
					c.setDebugStream( ps );				
				} catch( IOException e ) {
					throw new SolverException( "Failed to create debug output file " + crg_dbg.getAbsolutePath( ), e );
				}
			}
			
			msg( "Building CRG for agent " + c.getAgent( ) );
			c.construct( this, initstate.get( c.getAgent( ) ), rewards );
			
			// close debug files
			if( ps != null ) {
				dbg_msg( "CRG " + c.getAgent( ) + " generation complete, debug info written to " + crg_dbg.getAbsoluteFile( ) );
				ps.flush( );
				ps.close( );
			}
		}
		
		// show the CRG stats
		if( getSettings( ).showProgress( ) || isDebug( ) ) {
			System.out.println( "\nCompleted CRG generation, resulting CRGs:\n");
			getStats( ).printCRGStats( );
		}
	}
	
	/**
	 * Performs the actual policy search using the CRGs
	 * 
	 * @return The expected value of the optimal policy
	 * @throws SolverException if debug files cannot be created
	 */
	protected StateValue solve( ) throws SolverException, SolverTimeOut {
		// create CoReSolver
		coresolver = new CoReSolver( this );
		
		// setup policy solving output file
		PrintStream ps = null;
		File solver_dbg = null;		
		if( isDebug( ) && getSettings( ).getDebugDirectory( ) != null ) {
			 solver_dbg = new File( getSettings( ).getDebugDirectory( ), "solver.crg" 	);
			try {
				ps = new PrintStream( solver_dbg );
				coresolver.setDebug( isDebug( ) );
				coresolver.setDebugStream( ps );				
			} catch( IOException e ) {
				throw new SolverException( "Failed to create debug output file " + solver_dbg.getAbsolutePath( ), e );
			}
		}

		// optimise the policy!
		final StateValue polval = coresolver.findOptimal( );
		if( getSettings( ).showprogress )
			System.out.println( "CoRe solve completed, expected policy value: " + polval );
				
		// close debug file
		if( ps != null ) {
			dbg_msg( "Policy search complete, debug info written to " + solver_dbg.getAbsoluteFile( ) );
			dbg_msg( "" );
			
			ps.flush( );
			ps.close( );
		}
		
		return polval;
	}
	
	/**
	 * Does post-processing, builds the actual policy from the solve run
	 * 
	 * @return The policy found by CoRe
	 * @throws SolverException if the debug file cannot be created
	 */
	protected CRPolicy postprocess( ) throws SolverException {
		POSTPROCESS_START( );
		
		// build the actual policy
		if( getSettings( ).showProgress( ) )
			System.out.print( "Building policy...");
		final CRPolicy policy = coresolver.buildPolicy( );
		if( getSettings( ).showProgress( ) )
			System.out.println( "done!");		
		
		
		// output the policy if in debug mode
		if( isDebug( ) && getSettings( ).getDebugDirectory( ) != null ) {
			 final File pol_dbg = new File( getSettings( ).getDebugDirectory( ), "policy.crg" 	);
			try {
				final PrintStream ps = new PrintStream( pol_dbg );
				policy.dumpPolicy( ps, getI( ).getHorizon( ), 0 );
				ps.flush( );
				ps.close( );
			} catch( IOException e ) {
				throw new SolverException( "Failed to create debug output file " + pol_dbg.getAbsolutePath( ), e );
			}
		}

		POSTPROCESS_END( );
		
		return policy;
	}
	
	
	/**
	 * Retrieves the CRG of the agent
	 * 
	 * @param agent The agent
	 * @return The constructed CRG of the agent
	 */
	public CRG getCRG( Agent agent ) {
		assert crgs.containsKey( agent ) : "No CRG for the agent";
		return crgs.get( agent );
	}
	
	
	/**
	 * Sets the domain that is being solved
	 * 
	 * @param domain The domain
	 */
	public void setDomain( CRDomain domain ) {
		this.domain = domain;
		domain.setCoRe( this );
	}
	
	/** @return The domain we are solving */
	public CRDomain getDomain( ) { return domain; }
	
	/** @return The settings object */
	public CRSettings getSettings( ) {
		return settings;
	}
	
	/**
	 * @see core.solver.base.Solver#getStats()
	 */
	@Override
	public CRStats getStats( ) {
		return (CRStats)super.getStats( );
	}
	
	/**
	 * Prints a message to standard out if show progress is enabled
	 * 
	 * @param message The message to print
	 */
	protected void msg( String message ) {
		if( !getSettings( ).showProgress( ) ) return;
		
		System.out.println( message );
	}
}
