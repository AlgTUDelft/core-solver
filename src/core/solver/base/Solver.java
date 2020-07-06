/**
 * @file Solver.java
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
 * @date         17 okt. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.base;

import core.domains.Instance;
import core.exceptions.SolverException;
import core.exceptions.SolverTimeOut;
import core.model.policy.Policy;
import core.util.Debugable;



/**
 * Abstract base class for the CoRe solver
 * @author Joris Scharpff
 */
public abstract class Solver extends Debugable {
	/** The instance we are solving */
	private Instance I;
	
	/*** Current running solver */
	protected static Solver runningSolver;
	
	/** Statistics */
	protected SolverStats stats;
	
	/** The maximum allowed run time (in msec), -1 for infinite */
	protected long maxruntime = -1;
	
	/**
	 * Creates a new solver
	 * 
	 * @param 
	 */
	public Solver( ) {
		setDebug( false );
		
		// create initial stats object
		stats = initStats( );
	}
	
	/**
	 * Runs the solver!
	 * 
	 * @param instance The instance to solve
	 * @throws SolverException upon an unexpected solve failure
	 * @throws SolverTimeOut if the solver times out
	 */
	public abstract Policy solve( Instance instance ) throws SolverException, SolverTimeOut;
	
	/**
	 * Sets the solver time out in milliseconds
	 * 
	 * @param time The maximum run time for the solver in milliseconds
	 */
	public void setMaxRuntime( long time ) {
		if( time <= 0 )
			clearMaxRuntime( );
		else
			maxruntime = time;
	}
	
	
	/**
	 * Sets the solver time out in seconds
	 * 
	 * @param sec The maximum run time for the solver in seconds
	 */
	public void setMaxRuntimeSec( int sec ) {
		setMaxRuntime( sec * 1000 );
	}	
	/**
	 * Sets the solver maximum runtime in minutes
	 * 
	 * @param mins The time out in minutes
	 */
	public void setMaxRuntimeMins( int mins ) {
		setMaxRuntime( mins * 60 * 1000 );
	}
	
	/**
	 * @return The maximum allowed runtime in milliseconds
	 */
	public long getMaxRuntime( ) {
		return maxruntime;
	}
	
	/**
	 * @return The maximum allowed runtime in seconds
	 */
	public long getMaxRuntimeSec( ) {
		return maxruntime / 1000;
	}
	

	
	/**
	 * Clears the maximal solver runtime
	 */
	public void clearMaxRuntime( ) {
		maxruntime = -1;
	}
	
	/**
	 * Should be implemented in solvers to check for time out every once in a
	 * while
	 * 
	 * @throws SolverTimeOut if the solver has timed out
	 */
	public void CHECK_TIMEOUT( ) throws SolverTimeOut {
		if( maxruntime == -1 ) return;
		
		final double start = stats.solveStart == 0 ? stats.preprocStart : stats.solveStart;
		
		if( System.currentTimeMillis( ) - start > maxruntime )
			throw new SolverTimeOut( );
	}
	
	/**
	 * Creates the statistics object, can be overridden for custom statistics
	 */
	protected SolverStats initStats( ) {
		return new SolverStats( );
	}
	
	/**
	 * @return The solve run statistics
	 */
	public SolverStats getStats( ) {
		return stats;
	}
	
	/**
	 * Sets the instance to solve
	 * 
	 * @param inst The instance
	 */
	public void setInstance( Instance inst ) {
		I = inst;
	}
	
	/**
	 * @return The instance we are solving
	 */
	public Instance getI( ) {
		return I;
	}
	
	/**
	 * @return The horizon of the current instance we are solving
	 */
	public int getHorizon( ) {
		return I.getHorizon( );
	}
	
	/**
	 * Should be called by solver when starting pre-processing
	 */
	protected void PREPROCESS_START( ) {
		stats.preprocStart = System.currentTimeMillis( );
	}
	
	/**
	 * Should be called by solver when preprocessing is done
	 */
	protected void PREPROCESS_END( ) {
		stats.preprocTime = System.currentTimeMillis( ) - stats.preprocStart;
	}
	
	/**
	 * Should be called by solver on start of solving
	 */
	protected void SOLVE_START( ) {
		stats.solveStart = System.currentTimeMillis( );
		
		// flag this solver as the running one
		Solver.runningSolver = this;
	}
	
	/**
	 * Should be called by solver on end of solving
	 */
	protected void SOLVE_END( ) {
		stats.solveTime = System.currentTimeMillis( ) - stats.solveStart;
	}
	
	/**
	 * Should be called when starting post-processing
	 */
	protected void POSTPROCESS_START( ) {
		stats.postprocStart = System.currentTimeMillis( );
	}
	
	/**
	 * Should be called when ending post processing
	 */
	protected void POSTPROCESS_END( ) {
		stats.postprocTime = System.currentTimeMillis( ) - stats.postprocStart;
	}
	
	/**
	 * @return Preprocessing time of the last solver run, -1 if not completed
	 */
	public long getPreprocessingTime( ) {
		return stats.getPreProcTime( );
	}
	
	/**
	 * @return Runtime of last solver run in msec, -1 if no run completed
	 */
	public long getRuntime( ) {
		return stats.getSolveTime( );
	}
	
	/**
	 * @return The total solver time, including preprocessing time, -1 if any of
	 * both has not completed
	 */
	public long getTotalTime( ) {
		if( getPreprocessingTime( ) == -1 || getRuntime( ) == -1 ) return -1;
		return getPreprocessingTime( ) + getRuntime( );
	}
	
	/**
	 * @return The runtime of the last run in seconds, -1 if no run completed
	 */
	public int getRuntimeSec( ) {
		return (getRuntime( ) == -1 ? -1 : (int) (getRuntime( ) / 1000) );
	}
	
	/**
	 * @return The running solver
	 */
	public static Solver getRunning( ) {
		return runningSolver;
	}
}
