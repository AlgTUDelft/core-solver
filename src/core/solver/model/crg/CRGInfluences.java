/**
 * @file CRGInfluences.java
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

import core.model.policy.Agent;
import core.util.FactoredOtherCollection;


/**
 * Container for transition influences
 * 
 * @author Joris Scharpff
 */
public class CRGInfluences extends FactoredOtherCollection<CRGInfluence> {
	/**
	 * Creates a new influences container
	 * 
	 * @param size The initial size
	 */
	public CRGInfluences( ) {
		super( );
	}
	
	/**
	 * Copies the transition influences collection
	 * 
	 * @param infl The influences to copy
	 */
	public CRGInfluences( CRGInfluences infl ) {
		super( infl );
	}
	
	/**
	 * @see core.util.FactoredCollection#getAgent(java.lang.Object)
	 */
	@Override
	protected Agent getAgent( CRGInfluence object ) {
		return object.getAgent( );
	}
}
