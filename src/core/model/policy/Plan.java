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
import core.domains.State;
import core.domains.StateValue;
import core.exceptions.InfeasibleStateException;
import core.exceptions.InvalidPlanException;
import core.exceptions.SolverException;


/**
 * Deterministic plan containing actions for each time step
 * 
 * @author Joris Scharpff
 */
public abstract class Plan {
	/** The instance that this plan is associated with */
	protected final Instance I;
	
	/**
	 * Creates a new plan for the instance
	 * 
	 * @param instance The instance
	 */
	public Plan( Instance instance ) {
		this.I = instance;
	}
	
	/**
	 * Copies the plan
	 * 
	 * @return The copy
	 */
	public abstract Plan copy( );
		
	/**
	 * Retrieves the action that is planned at the given time for the agent
	 * 
	 * @param agent The agent
	 * @param time The time
	 * @return The action that is planned or null if none
	 */
	public abstract Action getPlanned( Agent agent, int time );
	
	/**
	 * Retrieves the outcome that is known for the action of the agent at the
	 * specified time
	 * 
	 * @param agent The agent
	 * @param time The time
	 * @return The action outcome or null if unknown
	 */
	public abstract Outcome getOutcome( Agent agent, int time );
	
	/**
	 * Checks if an action is planned at some time in the plan
	 * 
	 * @param action The action
	 * @return True if the action is planned
	 */
	public abstract boolean isPlanned( Action action );
	
	/**
	 * Determines the joint action that is planned at the given time, the default
	 * implementation uses the getPlanned function for each agent
	 * 
	 * @param time The time
	 * @return The joint action for all agents
	 */
	public JointAction getPlanned( int time ) {
		final JointAction ja = new JointAction( I.getAgents( ), time );
		for( Agent a : I.getAgents( ) )
			ja.setAction( a, getPlanned( a, time ) );
		
		return ja;		
	}
	
	/**
	 * Determines the realised action that is planned at a given time, the 
	 * default implementation uses the getOutcome function for each agent
	 * 
	 * @param time The time
	 * @return The realised joint action
	 */
	public RealisedJAction getRealised( int time ) {
		final RealisedJAction ra = new RealisedJAction( I.getAgents( ), time );
		for( Agent a : I.getAgents( ) )
			ra.setAction( a, getPlanned( a, time ), getOutcome( a, time ) );
		
		return ra;
	}
	
	/**
	 * Sets the joint action that is planned at the specified time. The default
	 * implementation uses the setPlanned function for each agent
	 * 
	 * @param ja The joint action 
	 */
	public void setPlanned( JointAction ja ) {
		for( Agent a : ja.getAgents( ) )
			setPlanned( a, ja.getAction( a ), ja.getTime( ) );
	}
	
	/**
	 * Sets the action that is planned for the agent at the specified time
	 * 
	 * @param agent The agent
	 * @param action The action that is planned
	 * @param time The time
	 */
	public abstract void setPlanned( Agent agent, Action action, int time );
	
	/**
	 * Sets the outcome for the action planned at the specified time
	 * 
	 * @param agent The agent
	 * @param outcome The outcome
	 * @param time The time
	 */
	public abstract void setOutcome( Agent agent, Outcome outcome, int time );
	
	/**
	 * Sets the action and the outcome for the specified time
	 * 
	 * @param action The action to plan
	 * @param outcome The outcome
	 * @param time The time to plan the action
	 */
	public void setRealised( Action action, Outcome outcome, int time ) {
		setPlanned( action.getAgent( ), action, time );
		setOutcome( action.getAgent( ), outcome, time );
	}

	/**
	 * Sets the realised joint action
	 * 
	 * @param realised The realised joint action
	 */
	public void setRealised( RealisedJAction realised ) {
		for( Agent a : realised.getAgents( ) )
			setRealised( realised.getAction( a ), realised.getOutcome( a ), realised.getTime( ) );
	}
	
	/**
	 * Creates a copy of this plan for the specified instance
	 * 
	 * @param instance The instance
	 * @return The plan for the instance with the same tasks planned
	 * @throws InvalidPlanException if the plan cannot be created for the
	 * specified instance
	 */
	public abstract Plan forInstance( Instance instance ) throws InvalidPlanException;
	
	/**
	 * @return The value of the plan
	 */
	public abstract StateValue getValue( );
	
	/**
	 * Computes the value of the plan for the agent
	 * 
	 * @param agent The agent
	 * @return The value for the agent
	 */
	public abstract StateValue getValue( Agent agent );
	
	/**
	 * Determines the expected plan given the current state and realisation of delay
	 * variables. Queries the policy to build the most-likely optimal plan.
	 * 
	 * @param policy The policy to query
	 * @param initstate The state to start from
	 * @param outcomes The realisation of outcomes
	 * @return The optimal static plan given this realisation, according to the
	 * policy
	 * @throws SolverException if the policy query failed
	 */
	public static Plan fromPolicy( Policy policy, State initstate, OutcomeRealisation outcomes ) throws SolverException {
		// create a new plan that contains the results
		final Instance I = policy.getInstance( );
		final Plan plan = I.getEmptyPlan( );
		
		// copy the initial state
		State state = initstate.copy( );
		
		// query the policy until the horizon has been reached
		for( int t = state.getTime( ); t < I.getHorizon( ); t++ ) {
			final JointAction ja = policy.query( state );
			plan.setPlanned( ja );
			final RealisedJAction ra = outcomes.getOutcomes( ja, t );
			
			try {
				state = state.execute( ra );
			} catch( InfeasibleStateException ise ) {
				throw new SolverException( "Failed to create plan from policy", ise );
			}
		}
		
		// returns the plan
		return plan;
	}			
	
	/**
	 * @return The associated instance
	 */
	public Instance getI( ) {
		return I;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof Plan) ) return false;
		final Plan p = (Plan) obj;
		
		// compare both plans
		for( int t = getI( ).getInitialState( ).getTime( ); t < getI( ).getHorizon( ); t++ )
			for( Agent a : getI( ).getAgents( ) )
				if( getPlanned( a, t ) != p.getPlanned( a, t ) )
					return false;
		
		return true;
	}
}
