/**
 * @file CRGDepActions.java
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

import core.model.policy.Action;
import core.model.policy.Agent;
import core.util.FactoredOtherCollection;


/**
 * Container for dependent actions
 * 
 * @author Joris Scharpff
 */
public class CRGDepActions extends FactoredOtherCollection<Action> {
	
	/**
	 * Creates a new dependent actions container
	 * 
	 * @param size The initial size
	 */
	public CRGDepActions(  ) {
		super( );
	}
	
	/**
	 * Copies the dependent action collection
	 * 
	 * @param actions The dependent actions to copy
	 */
	public CRGDepActions( CRGDepActions actions ) {
		super( actions );
	}
	
	/**
	 * @see core.util.FactoredCollection#getAgent(java.lang.Object)
	 */
	@Override
	protected Agent getAgent( Action object ) {
		return object.getAgent( );
	}
}
