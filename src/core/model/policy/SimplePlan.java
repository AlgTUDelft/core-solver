/**
 * @file Plan.java
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
 * @date         20 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.policy;

import core.domains.Instance;
import core.domains.StateReward;
import core.exceptions.InvalidPlanException;


/**
 * Deterministic plan containing actions for each time step
 * 
 * @author Joris Scharpff
 */
public class SimplePlan extends Plan {	
	/** Matrix that contains the plan */
	protected Action[][] planned;
	
	/** Matrix that contains the outcomes */
	protected Outcome[][] outcomes;
	
	/**
	 * Creates a new plan for the instance
	 * 
	 * @param instance The instance
	 */
	public SimplePlan( Instance instance ) {
		super( instance );
		
		planned = new Action[ I.getN( ) ][ I.getHorizon( ) ];
		outcomes = new Outcome[ I.getN( ) ][ I.getHorizon( ) ];
	}
	
	/**
	 * Copies the plan
	 * 
	 * @return The copy
	 */
	@Override
	public SimplePlan copy( ) {
		final SimplePlan p = new SimplePlan( getI( ) );
		
		// copy plan and outcomes
		for( int i = 0; i < getI( ).getN( ); i++ )
			for( int j = 0; j < getI( ).getHorizon( ); j++ ) {
				p.planned[ i ][ j ] = planned[ i ][ j ];
				p.outcomes[ i ][ j ] = outcomes[ i ][ j ];
			}
		
		return p;
	}
	
	/**
	 * @see core.model.policy.Plan#isPlanned(core.model.policy.Action)
	 */
	@Override
	public boolean isPlanned( Action action ) {
		for( int i = 0; i < I.getHorizon( ); i++ )
			if( planned[ action.getAgent( ).ID ][ i ].equals( action ) )
				return true;
		return false;
	}
		
	/**
	 * Retrieves the action that is planned at the given time for the agent
	 * 
	 * @param agent The agent
	 * @param time The time
	 * @return The action that is planned or null if none
	 */
	@Override
	public Action getPlanned( Agent agent, int time ) {
		return planned[ agent.ID ][ time ];
	}
	
	/**
	 * @see core.model.policy.Plan#setPlanned(core.model.policy.Agent, core.model.policy.Action, int)
	 */
	@Override
	public void setPlanned( Agent agent, Action action, int time ) {
		planned[ agent.ID ][ time ] = action;		
	}
	
	/**
	 * @see core.model.policy.Plan#getOutcome(core.model.policy.Agent, int)
	 */
	@Override
	public Outcome getOutcome( Agent agent, int time ) {
		return outcomes[ agent.ID ][ time ];
	}
	
	/**
	 * @see core.model.policy.Plan#setOutcome(core.model.policy.Agent, core.model.policy.Outcome, int)
	 */
	@Override
	public void setOutcome( Agent agent, Outcome outcome, int time ) {
		outcomes[ agent.ID ][ time ] = outcome;
	}
	
	/**
	 * Creates a copy of this plan for the specified instance
	 * 
	 * @param instance The instance
	 * @return The plan for the instance with the same tasks planned
	 * @throws InvalidPlanException if the plan cannot be created for the
	 * specified instance
	 */
	@Override
	public SimplePlan forInstance( Instance instance ) throws InvalidPlanException {
		final SimplePlan plan = new SimplePlan( instance );
		
		// copy planned actions and their outcomes
		for( int i = 0; i < instance.getN( ); i++ )
			for( int j = 0; j <instance.getHorizon( ); j++ ) {
				plan.planned[ i ][ j ] = planned[ i ][ j ];
				plan.outcomes[ i ][ j ] = outcomes[ i ][ j ];
			}

		return plan;
	}
	
	/**
	 * @return The value of the plan
	 */
	@Override
	public StateReward getValue( ) {
		// sum all action costs
		StateReward val = new StateReward( 0 );
		for( Agent a : getI( ).getAgents( ) )
			val.add( getValue( a ) );
		
		return val;
	}
	
	/**
	 * @see core.model.policy.Plan#getValue(core.model.policy.Agent)
	 */
	@Override
	public StateReward getValue( Agent agent ) {
		// sum all action costs
		double val = 0;
		for( int time = 0; time < getI( ).getHorizon( ); time++ ) {
			final Action act = planned[ agent.ID ][ time ];
			val += (act != null ? act.getRewardFunction( ).eval( time, getI( ).getHorizon( ) ) : 0);
		}
		
		return new StateReward( val );
	}
	
	/**
	 * @return The associated instance
	 */
	@Override
	public Instance getI( ) {
		return I;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		String str = "";
		for( int t = 0; t < getI( ).getHorizon( ); t++ )
			str += "\t" + t;
		str += "\n";
		for( Agent a : getI( ).getAgents( ) ) {
			str += a.toString( );
			for( int t = 0; t < getI( ).getHorizon( ); t++ ) {
				final Action action = planned[ a.ID ][ t ];
				str += "\t" + (action != null ? action.toString( ) : "-");
			}
			str += "\n";
		}
		
		return str;
	}
}
