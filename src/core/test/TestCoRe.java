/**
 * @file TestCoRe.java
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
 * @date         11 aug. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.test;

import core.domains.mpp.MPPGenerator;
import core.domains.mpp.MPPInstance;
import core.domains.mpp.MPPInstanceParameters;
import core.solver.CoRe;
import core.solver.domains.mpp.CRDomainMPP;
import core.solver.model.policy.CRPolicy;

/**
 * @file TestCoRe.java
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
 * @date         11 aug. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
public class TestCoRe {
	/**
	 * Tests whether the core solver runs properly
	 * 
	 * @param args
	 */
	public static void main( String[] args ) {
		// generate a random MPP instance
		final MPPGenerator g = new MPPGenerator( 0 );
		final MPPInstanceParameters IP = new MPPInstanceParameters( 2, 2, 7, 2, 1 );
		//g.setCosts( -100, -200 );
		final MPPInstance I = g.generate( IP );
		
		// generate bilateral, acylclic network rewards
		g.genNetworkRewardAcyclic( I, 4 );
		
		// print the generated instance
		System.out.println(  I  );
	

		// instantiate the CoRe solver
		final CoRe core = new CoRe( new CRDomainMPP( ) );
		core.getSettings( ).setShowProgress( true );
		//core.setDebug( true );
		
		// run the solver
		try {
			final CRPolicy policy = (CRPolicy) core.solve( I );
			
			System.out.println( "\nSolver statistics:" );
			System.out.println( core.getStats( ).toString( ) );
			
			System.out.println( "\nResulting policy:" );
			System.out.println( "Expected value: " + policy.getExpectedValue( I ) );
		} catch( Exception ex ) {
			System.err.println( "CoRe solver failed: " + ex.getMessage( ) );
			ex.printStackTrace( System.err );
		}
	}
}
