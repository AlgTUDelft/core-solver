/**
 * @file SolverStats.java
 * @brief [brief description]
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2014 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         12 feb. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.base;


/**
 * Statistics for solver runs, can be overridden to add custom statistics
 * 
 * @author Joris Scharpff
 */
public class SolverStats {
	/** Start of pre-processing */
	protected long preprocStart;
	
	/** Pre-processing time of the last solver run */
	protected long preprocTime;
	
	/** Start of last solver run */
	protected long solveStart;

	/** Runtime of the last executed solve */
	protected long solveTime;

	/** Start of post-processing */
	protected long postprocStart;
	
	/** Post-processing time of the last solver run */
	protected long postprocTime;

	/**
	 * Creates a new empty stats object
	 */
	public SolverStats( ) {
		clear( );
	}
	
	/**
	 * Clears the statistics
	 */
	public void clear( ) {
		preprocStart = 0;
		preprocTime = 0;
		solveStart = 0;
		solveTime = 0;
	}
	
	/** @return The pre-processing start time */
	public long getPreProcStart( ) { return preprocStart; }

	/** @return The pre-processing duration in msec */
	public long getPreProcTime( ) { return preprocTime; }

	/** @return The solving start time */
	public long getSolveStart( ) { return solveStart; }

	/** @return The pre-processing start time */
	public long getSolveTime( ) { return solveTime; }
	
	/** @return The post-processing start time */
	public long getPostProcStart( ) { return postprocStart; }

	/** @return The post-processing duration in msec */
	public long getPostProcTime( ) { return postprocTime; }

	/** @return The total solver run time */
	public long getTotalTime( ) { return getPreProcTime( ) + getSolveTime( ) + getPostProcTime( ); }
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		String str = "";
		str += "Total run time: " + getTotalTime( ) + " msec\n"; 
		str += ".. pre-processing time: " + getPreProcTime( ) + " msec\n";
		str += ".. solving time: " + getSolveTime( ) + " msec\n";
		str += ".. post-processing time: " + getPostProcTime( ) + " msec\n";
		
		return str;
	}
}
