/**
 * @file CRStatsCRG.java
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

import core.solver.model.crg.CRG;


/**
 * @author Joris Scharpff
 */
public class CRStatsCRG {
	/** The CRG to which the statistics correspond */
	protected final CRG crg;
	
	/** Number of states */
	public long states;
	
	/** Number of transitions */
	public long transitions;
	
	/** Number of terminal states */
	public long terminal;
	
	/** Number of locally independent states */
	public long independent;
	
	/** Number of duplicate states */
	public long duplicates;
	
	/** Number of action dependency branches */
	public long depbranches;
	
	/** Number of transition influence branches */
	public long inflbranches;	
	
	/**
	 * Initialises a new stats object for a CRG
	 * 
	 * @param crg The CRG to which the statistics correspond 
	 */
	public CRStatsCRG( CRG crg ) {
		this.crg = crg;
		
		states = 0;
		transitions = 0;
		terminal = 0;
		independent = 0;
		duplicates = 0;
		depbranches = 0;
		inflbranches = 0;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		final String[] info = { 
				"states: " + states,  
				"transitions: " + transitions,
				"terminal states: " + terminal,
				"revisited states: " + duplicates,
				"independent states: " + independent,
				"dep. action branches: " + depbranches,
				"trans. infl. branches: " + inflbranches
		};
		String str = "CRG " + crg.getAgent( ) + ":\n";
		for( String s : info )
			str += "> " + s + "\n";
		str = str.substring( 0, str.length( ) - 1 );
		return str;
	}
}
