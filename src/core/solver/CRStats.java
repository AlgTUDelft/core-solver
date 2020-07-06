/**
 * @file CRStats.java
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

import java.util.HashMap;
import java.util.Map;

import core.solver.base.SolverStats;
import core.solver.model.crg.CRG;
import core.util.Util;


/**
 * Stats object for the CoRe solver
 * 
 * @author Joris Scharpff
 */
public class CRStats extends SolverStats {
	/** Stats per CRG */
	protected Map<CRG, CRStatsCRG> crgstats;
	
	/** Evaluated number of states */
	protected long states;
	
	/** Actually evaluated joint actions */
	protected long jactions;
	
	/** Number of terminal states encountered */
	protected long terminal;
	
	/** Previously optimised states visited */
	protected long visited;
	
	
	/** Total B&B prune runs */
	protected long prunes;
	
	/** Number of joint actions pruned by BB before evaluation */
	protected long prunedactions_outer;
	
	/** Number of joint actions pruned by BB during ja evaluation loop*/
	protected long prunedactions_inner;
	
	/** Number of times all actions could have been pruned */
	protected long fullprune;
	
	/** Total joint state size */
	protected long statesize;
	
	/** Number of states that have been decoupled */
	protected long decoupled;
	
	/** Average decoupling size (i.e. resulting states after split)*/
	protected double decouple_size;
	
	/**
	 * Creates a new stats object, initialises values 
	 */
	public CRStats( ) {
		super( );
		clear( );
	}
	
	/**
	 * @see core.solver.base.SolverStats#clear()
	 */
	@Override
	public void clear( ) {
		super.clear( );
	
		states = 0;
		jactions = 0;
		terminal = 0;
		visited = 0;
		
		// B&B
		prunes = 0;
		fullprune = 0;
		prunedactions_outer = 0;
		prunedactions_inner = 0;
		
		// CRI
		statesize = 0;
		decoupled = 0;
		decouple_size = 0.0;
		
		// CRG
		crgstats = new HashMap<CRG, CRStatsCRG>( );
	}
	
	/**
	 * Retrieves the stats for the CRG, creates a new stats object if it does not
	 * exist
	 * 
	 * @param crg The CRG
	 * @return The CRStatsCRG object
	 */
	public CRStatsCRG forCRG( CRG crg ) {
		CRStatsCRG stats = crgstats.get( crg );
		if( stats == null ) {
			stats = new CRStatsCRG( crg );
			crgstats.put( crg, stats );
		}
		return stats;
	}

	
	/**
	 * @see core.solver.base.SolverStats#toString()
	 */
	@Override
	public String toString( ) {
		return super.toString( ) +
				"\nCoRe Statistics:" +
				"\n> States evaluated: " + states +
				"\n> ... previously visited: " + visited +
				"\n> ... terminal: " + terminal +
				"\n> Actions evaluated: " + jactions +
				"\n\nB&B:" +
				"\n> Prune attempts: " + prunes +
				"\n> Actions pruned (outer): " + prunedactions_outer +
				"\n> Actions pruned (inner): " + prunedactions_inner +
//				"\n> Full prunes: " + fullprune + " (" + p( fullprune / (double)prunes ) + ")" +
				"\n\nCRI:" +
				"\n> States decoupled: " + decoupled +
				"\n> Average split size: " + Util.dec( ).format( decouple_size ) +
				"\n> Average state size: " + Util.dec( ).format( statesize / (double)states );
	}
	
	/**
	 * Formats the percentage
	 * 
	 * @param perc The percentage
	 * @return "0.0%"
	 */
	protected String p( double perc ) {
		return Util.perc( 1 ).format( perc );
	}
	
	/**
	 * Prints all CRG statistics
	 */
	public void printCRGStats( ) {
		for( CRStatsCRG crg : crgstats.values( ) )
			System.out.println( crg.toString( ) + "\n" );
	}
}
