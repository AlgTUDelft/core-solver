/**
 * @file CRSettings.java
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

import core.solver.model.crg.CRGRewardHeuristic;
import core.solver.model.crg.rewheuristic.CRGRewardHeurHighestDegree;


/**
 * Settings for the CoRe solver
 * 
 * @author Joris Scharpff
 */
public class CRSettings {
	/** Use branch and bound pruning */
	protected boolean branchbound;
	
	/** Use B&B tightening during joint action evaluation */
	protected boolean branchboundtighten;
	
	/** Use individual CRG optimalisation when locally CRI */
	protected boolean localCRI;
	
	/** Use CRI agent decoupling */
	protected boolean decoupleCRI;
	
	/** Show the progress during the solving run */
	protected boolean showprogress;
	
	/** The reward assignment heuristic that is used */
	protected CRGRewardHeuristic heuristic;
	
	/** Directory for debug output */
	protected File debugdir;
	
	/**
	 * Creates a new settings object with defaults
	 */
	public CRSettings( ) {
		setUseBBPruning( true );
		setUseBBTightening( true );
		setUseDecoupleCRI( true );
		setUseLocalCRI( true );
		setShowProgress( false );
		setAssignHeuristic( new CRGRewardHeurHighestDegree( ) );
		setDebugDirectory( "debug" );
	}
	
	/**
	 * Enables/disables branch-and-bound pruning
	 * 
	 * @param en True to enable
	 */
	public void setUseBBPruning( boolean en ) {
		branchbound = en;
	}
	
	/** @return True iff BB pruning is enabled */
	public boolean useBBPruning( ) { return branchbound; }
	
	/**
	 * Enable/disabled tightening of the Lmax within the joint action evaluation
	 * loop. Also enables B&B in general if set to true.
	 * 
	 * @param en True to enable the B&B tightening
	 */
	public void setUseBBTightening( boolean en ) {
		if( en ) setUseBBPruning( true );
		branchboundtighten = en;
	}
	
	/** @return True if inner loop B&B tightening is enables */
	public boolean useBBTightening( ) { return branchboundtighten; }
	
	/**
	 * Enables/disabled local optimisation of CRGs when local conditional reward
	 * independence occurs
	 * 
	 * @param en True to enable
	 */
	public void setUseLocalCRI( boolean en ) {
		localCRI = en;
	}
	
	/** @return True iff local CRI is enabled */
	public boolean useLocalCRI( ) { return localCRI ; }
	
	/**
	 * Enables/disabled agent decoupling when CRI occurs
	 * 
	 * @param en True to enable
	 */
	public void setUseDecoupleCRI( boolean en ) {
		decoupleCRI = en;
	}
	
	/** @return True iff decoupling of CRI agents is used */
	public boolean useDecoupleCRI( ) { return decoupleCRI; }
	
	/**
	 * Enables/disables solving progress reporting
	 * 
	 * @param en True to enable
	 */
	public void setShowProgress( boolean en ) {
		showprogress = en;
	}
	
	/** @return True iff progress reporting is enabled */
	public boolean showProgress( ) { return showprogress; }
	
	/**
	 * Sets the reward assignment heuristic
	 * 
	 * @param heur The heuristic to use
	 */
	public void setAssignHeuristic( CRGRewardHeuristic heur ) {
		heuristic = heur;
	}
	
	/** @return The reward assign heuristic */
	public CRGRewardHeuristic getAssignHeuristic( ) { return heuristic; }
	
	/**
	 * Sets the directory for debug output
	 * 
	 * @param debugdir The (relative) path of the directory to write debug output
	 * into
	 */
	public void setDebugDirectory( String debugdir ) {
		this.debugdir = new File( debugdir );
	}
	
	/** @return The debug directory */
	protected File getDebugDirectory( ) { return debugdir; }
}
