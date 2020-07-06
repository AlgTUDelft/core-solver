/**
 * @file Plan.java
 * @brief Short description of file
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2013 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         1 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains.mpp;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.domains.Instance;
import core.domains.StateValue;
import core.exceptions.InfeasibleStateException;
import core.exceptions.InvalidPlanException;
import core.exceptions.SolverException;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.policy.JointAction;
import core.model.policy.Outcome;
import core.model.policy.OutcomeRealisation;
import core.model.policy.Plan;
import core.model.policy.Policy;
import core.model.policy.RealisedJAction;
import core.model.sharedreward.SharedActionReward;
import core.util.Util;

/**
 * Plans are simple, non-contingent solver results 
 *
 * @author Joris Scharpff
 */
public class MPPPlan extends Plan {	
	/** The single-agent plan containers */
	protected final Map<Agent, AgentPlan> plans;
	
	/**
	 * Creates a new plan for the specified I and horizon
	 * 
	 * @param instance The instance this plan is associated with
	 */
	public MPPPlan( MPPInstance instance ) {
		super( instance );
		
		// create new plans for all agents
		plans = new HashMap<Agent, AgentPlan>( I.getN( ) );
		for( Agent a : I.getAgents( ) )
			plans.put( a, new AgentPlan( a ) );
	}
	
	/**
	 * Constructs an MPP plan from any other plan
	 * 
	 * @param plan The plan to construct it from
	 */
	public MPPPlan( Plan plan ) {
		this( plan, plan.getI( ) );
	}
	
	
	/**
	 * Constructs an MPP plan from any other plan
	 * 
	 * @param plan The plan to construct it from
	 * @param instance The instance to construct it for
	 */
	public MPPPlan( Plan plan, Instance instance ) {
		this( (MPPInstance)instance );
		
		// plan all tasks
		for( int t = 0; t < getHorizon( ); t++ ) {
			final JointAction ja = plan.getPlanned( t );
			
			for( Agent a : ja.getAgents( ) ) {
				final Task task = (Task)ja.getAction( a );
				
				if( !task.isNoOp( ) && !task.isContinue( ) )
					getPlan( task ).add( task, t, DelayStatus.Pending );
			}
		}
	}
	
	/**
	 * @see core.model.policy.Plan#copy()
	 */
	@Override
	public MPPPlan copy( ) {
		final MPPPlan p = new MPPPlan( getI( ) );
		
		// copy single agent plans
		for( Agent a : plans.keySet( ) )
			p.plans.put( a, new AgentPlan( plans.get( a ) ) );
		
		return p;
	}
	
	/**
	 * @see core.model.policy.Plan#forInstance(core.domains.Instance)
	 */
	@Override
	public MPPPlan forInstance( Instance instance ) throws InvalidPlanException {
		final MPPPlan p;
		try {
			p = new MPPPlan( (MPPInstance) instance );
		} catch( ClassCastException cce ) {
			throw new InvalidPlanException( "Invalid domain instance!" );
		}
		
		// copy single agent plans
		for( Agent a : plans.keySet( ) )
			p.plans.put( a, new AgentPlan( plans.get( a ) ) );
		
		return p;
	}
	
	/**
	 * @return The overall profit
	 */
	public double getProfit( ) {
		return getRevenue( ) - getCost( );
	}
	
	/**
	 * @return The sum of agent revenues
	 */
	public double getRevenue( ) {
		double rev = 0;
		for( AgentPlan ap : plans.values( ) )
			rev += ap.revenue;
		return rev;
	}
	
	/**
	 * @param agent The agent
	 * @return The sum of agent revenues
	 */
	public double getRevenue( Agent agent ) {
		return getPlan( agent ).revenue;
	}
	
	/**
	 * @return The sum of agent costs
	 */
	public double getCost( ) {
		double cost = 0;
		for( AgentPlan ap : plans.values( ) )
			cost += ap.cost;
		return cost;
	}
	
	/**
	 * @param agent The agent
	 * @return The sum of agent costs
	 */
	public double getCost( Agent agent ) {
		return getPlan( agent ).cost;
	}
	
	/**
	 * @return The shared reward of the instance
	 */
	public double getNetworkReward( ) {
		// check if there is a network cost model
		if( !getI( ).hasSharedReward( ) ) return 0;

		// get the network model
		final SharedActionReward snc = (SharedActionReward)getI( ).getSharedReward( );
		
		double cost = 0;
		for( int t = 0; t < getHorizon( ); t++ ) {
			final Set<Action> executing = new HashSet<Action>( );
			for( Task task : getExecuting( t ) )
				executing.add( task );
			
			cost += snc.getReward( executing, t );
		}
		
		return cost;
	}
	
	/**
	 * Computes the total shared reward in which the agent is involved
	 * 
	 * @param agent The agent
	 * @return The shared reward of the instance
	 */
	public double getNetworkReward( Agent agent ) {
		// get the network model
		final SharedActionReward snc = (SharedActionReward)getI( ).getSharedReward( );
		
		double cost = 0;
		for( int t = 0; t < getHorizon( ); t++ ) {
			final Set<Action> executing = new HashSet<Action>( );
			for( Task task : getExecuting( t ) )
				executing.add( task );
			
			final Set<Agent> agents = new HashSet<Agent>( 1 );
			agents.add( agent );			
			cost += snc.getReward( executing, t, agents );
		}
		
		return cost;
	}

	
	/**
	 * @return The instance associated with the plan
	 */
	@Override
	public MPPInstance getI( ) {
		return (MPPInstance)I;
	}
	
	/**
	 * @return The plan horizon
	 */
	public int getHorizon( ) {
		return I.getHorizon( );
	}	
	
	/**
	 * Plans the specified activity at the given time
	 * 
	 * @param task The task to plan, agent is inferred
	 * @param time The time to plan it at
	 * @param delay The delay status of the task
	 * @throws InvalidPlanException if the task is added to the plan leads to an
	 * invalid plan because adding either violates the horizon or single-task
	 * constraint. Also an exception if thrown if the task was already planned
	 */
	public void add( Task task, int time, DelayStatus delay ) throws InvalidPlanException {
		try {
			getPlan( task ).add( task, time, delay );
		} catch( InvalidPlanException e ) {
			// add plan to error for more detailed error info
			throw new InvalidPlanException( e.getMessage( ), this ); 
		}
	}
	
	/**
	 * Checks if a task can be added to the plan
	 * 
	 * @param task The task
	 * @param time The time to plan it at
	 * @return True if the plan is valid when adding the task 
	 */
	public boolean canAdd( Task task, int time ) {
		// copy the plan and try an add operation
		try {
			final MPPPlan plan = copy( );
			plan.add( task, time, DelayStatus.Pending );
			return true;
			
		} catch( InvalidPlanException ipe ) {
			return false;
		}
	}
	
	/**
	 * Removes the specified task from the plan
	 * 
	 * @param task The task to remove
	 * @return The time at which the task was planned
	 * @throws InvalidPlanException if the delay status was set for the task, this
	 * indicated that a task was under execution
	 */
	public int remove( Task task ) throws InvalidPlanException {
		return remove( task, true );
	}
	
	/**
	 * Removes the specified task from the plan
	 * 
	 * @param task The task to remove
	 * @param validate True to validate the remove, otherwise no exception is 
	 * thrown and the task is always removed
	 * @return The time at which the task was planned to start
	 * @throws InvalidPlanException if the delay status for the task was already
	 * set, this indicated a task that is already under execution
	 */
	public int remove( Task task, boolean validate ) throws InvalidPlanException {
		return getPlan( task ).remove( task, validate );
	}
	
	/**
	 * Sets the delay status for the specified task, updates state value
	 * accordingly. Can only be done when the task is pending delay
	 * 
	 * @param task The task to set delay status
	 * @param delay True if the task is delayed
	 * @throws InvalidPlanException if the status was not pending
	 */
	public void setDelayStatus( Task task, boolean delay ) throws InvalidPlanException {
		// update delay status
		getPlan( task ).setDelayStatus( task, delay );
	}
	
	/**
	 * @see core.model.policy.Plan#setOutcome(core.model.policy.Agent, core.model.policy.Outcome, int)
	 */
	@Override
	public void setOutcome( Agent agent, Outcome outcome, int time ) {
		// set the delay status of the task at that time
		final Task task = getTask( agent, time );
		if( task == null ) return;
		
		final MPPOutcome del = (MPPOutcome) outcome;
		if( getDelayStatus( task ) == DelayStatus.Pending )
			setDelayStatus( task, del.isDelayed( ) );
	}
	
	/**
	 * Gets the delay status that is currently set for the task
	 * 
	 * @param task The task
	 * @return The delay status
	 */
	public DelayStatus getDelayStatus( Task task ) {
		return task.isNoOp( ) ? DelayStatus.NotDelayed : getPlan( task ).getDelayStatus( task );
	}
	
	/**
	 * @see core.model.policy.Plan#getOutcome(core.model.policy.Agent, int)
	 */
	@Override
	public Outcome getOutcome( Agent agent, int time ) {
		Task task = getTask( agent, time );
		if( task == null ) task = Task.NoOp( agent );
		
		if( task.isContinue( ) || task.isNoOp( ) )
			return new MPPOutcome( task, false );
		else
			return new MPPOutcome( task, isDelayed( task ) );
	}
	
	/**
	 * @see core.model.policy.Plan#getValue()
	 */
	@Override
	public MPPStateValue getValue( ) {
		return new MPPStateValue( getRevenue( ), getCost( ), getNetworkReward( ) );
	}
	
	/**
	 * @see core.model.policy.Plan#getValue(core.model.policy.Agent)
	 */
	@Override
	public StateValue getValue( Agent agent ) {
		return new MPPStateValue( getRevenue( agent ), getCost( agent ), getNetworkReward( agent ) );
	}
	
	/**
	 * Clears the delay statuses of all tasks, resetting it to pending. This is
	 * used to clear the status after a solver run.
	 */
	public void clearStatus( ) {
		for( AgentPlan ap : plans.values( ) )
			ap.clearStatus( );
	}
	
	/**
	 * Clears the delay status for the task, resets it to pending. This is used
	 * to clear the status after a solver run.
	 * 
	 * @param task The task
	 */
	public void clearStatus( Task task ) {
		getPlan( task ).clearStatus( task );
	}
		
	/**
	 * Returns the plan that is associated with the agent
	 * 
	 * @param agent The agent
	 * @return The plan for the agent
	 */
	private AgentPlan getPlan( Agent agent ) {
		return plans.get( agent );
	}
	
	/**
	 * Infers the correct plan for the agent responsible for the task
	 * 
	 * @param task The task to infer the plan from
	 * @return The agent plan that corresponds to the agent in the task
	 */
	private AgentPlan getPlan( Task task ) {
		return getPlan( task.getAgent( ) );
	}
	
	/**
	 * Retrieves the task that the specified agent is performing at the given
	 * time. Does not consider tasks with pending delay duration, but otherwise
	 * the delay status is accounted for.
	 * 
	 * @param agent The agent
	 * @param time The time
	 * @return The task that an agent is performing at that time
	 */
	public Task getTask( Agent agent, int time ) {
		return getPlan( agent ).getTask( time );
	}	
	
	/**
	 * Retrieves the joint action at the specified time
	 * 
	 * @param time The time
	 * @return The joint action
	 */
	@Override
	public RealisedJAction getRealised( int time ) {
		// create the joint action
		final RealisedJAction ra = new RealisedJAction( I.getAgents( ), time );
		
		// build list
		for( Agent a : I.getAgents( ) ) {
			final Task t = getPlan( a ).getTask( time );
			if( t != null ) {
				if( getTime( t ) == time ) {
					final boolean delay = getPlan( a ).getDelayStatus( t ).isDelayed( );
					ra.setAction( a, t, new MPPOutcome( t, delay ) );
				} else {
					final Task cont = Task.CONT( t );
					ra.setAction( a, cont, new MPPOutcome( cont, false) );
				}
			} else {
				// NoOP
				final Task noop = Task.NoOp( a );
				ra.setAction( a, noop, new MPPOutcome( noop, false ) );
			}
		}
		
		return ra;
	}	

	/**
	 * Returns the realised action of the agent at the specified time
	 * 
	 * @param agent The agent
	 * @param time The time
	 * @return The realised action 
	 */
	public PTask getObservation( Agent agent, int time ) {
		final Task task = getPlan( agent ).getTask( time );
		if( task == null ) return new PTask( Task.NoOp( agent ), time, false );
		
		final int starttime = getTime( task );
		return new PTask( task, starttime, ((MPPOutcome)getOutcome( agent, starttime )).isDelayed( )  );
	}
	
	/**
	 * Returns the planned task for the task
	 * 
	 * @param task The task
	 * @return The planned task or null if not planned
	 */
	public PTask getPlanned( Task task ) {
		final int t = getPlan( task ).getTime( task );
		if( t == -1 ) return null;
		return new PTask( task, t, getPlan( task ).getDelayStatus( task ).isDelayed( ) );
	}
	
	/**
	 * @see core.model.policy.Plan#setPlanned(core.model.policy.Agent, core.model.policy.Action, int)
	 */
	@Override
	public void setPlanned( Agent agent, Action action, int time ) {
		add( (Task)action, time, DelayStatus.Pending );
	}
	
	/**
	 * @see core.model.policy.Plan#getPlanned(core.model.policy.Agent, int)
	 */
	@Override
	public Task getPlanned( Agent agent, int time ) {
		// check what the agent has planned
		final Task t = getPlan( agent ).getTask( time );
		
		// nothing?
		if( t == null )
			return Task.NoOp( agent );
		// starting a task now
		else if( getPlan( agent ).getTime( t ) == time )
			return t;
		// continuing its task
		else
			return Task.CONT( t );
	}
	
	/**
	 * @return The set of all planned tasks
	 */
	public Set<Task> getPlanned( ) {
		final Set<Task> planned = new HashSet<Task>( );
		for( Agent a : getI( ).getAgents( ) )
			planned.addAll( getPlan( a ).getPlanned( ) );
		
		return planned;
	}
	
	/**
	 * @see core.model.policy.Plan#isPlanned(core.model.policy.Action)
	 */
	@Override
	public boolean isPlanned( Action action ) {
		final Task task = (Task) action;
		return getPlan( task ).getTime( task ) != -1;
	}
	
	/**
	 * Returns the set of tasks that are being executed at the specified time
	 * 
	 * @param time The time
	 * @return The set of tasks that are active at the time
	 */
	public Set<Task> getExecuting( int time ) {
		final Set<Task> actions = new HashSet<Task>( );
		for( AgentPlan ap : plans.values( ) ) {
			final Task p = ap.getTask( time );
			if( p != null )
				actions.add( p );
		}
		
		return actions;
	}
	
	/**
	 * Returns the time at which a task is planned or -1 if not planned
	 * 
	 * @param task The task
	 * @return The time at which it is planned or -1 if not part of the plan
	 */
	public int getTime( Task task ) {
		return getPlan( task ).getTime( task );
	}
	
	/**
	 * Checks the plan and returns the list of tasks of the agent that are not
	 * in the plan yet
	 * 
	 * @param agent The agent
	 * @return The tasks that are not planned yet
	 */
	public List<Task> getUnplanned( Agent agent ) {
		final List<Task> unplanned = new ArrayList<Task>( );
		for( Action a : agent.getActions( ) ) {
			final Task t = (Task)a;
			if( !isPlanned( t ) )
				unplanned.add( t );
		}
		
		return unplanned;
	}
	
	/**
	 * Checks if the task is in any plan
	 * 
	 * @param task The task
	 * @return True if the task is in a plan
	 */
	public boolean isPlanned( Task task ) {
		return getTime( task ) != -1;
	}
	
	/**
	 * Checks whether a task is completed at the given time, considering also its
	 * delay status. If the status is pending, the task is assumed to be
	 * non-delayed
	 * 
	 * @param task The task
	 * @param time The time
	 * @return True iff the task should be completed according to the plan
	 */
	public boolean isCompleted( Task task, int time ) {
		if( !isPlanned( task ) ) return false;
		
		return getTime( task ) + getPlan( task ).getDuration( task ) - 1 < time;
	}
	
	/**
	 * Checks whether a task is delayed
	 * 
	 * @param task The task to check
	 * @return True if its status is Delayed
	 */
	public boolean isDelayed( Task task ) {
		return getPlan( task ).getDelayStatus( task ).isDelayed( );
	}
	
	/**
	 * Checks if all the specified tasks are planned
	 * 
	 * @param tasks The tasks
	 * @return True iff all the tasks have been planned
	 */
	public boolean containsAll( Task[] tasks ) {
		for( Task t : tasks )
			if( !isPlanned( t ) ) return false;
		
		return true;
	}
	
	/**
	 * Counts the number of completed tasks at the given time
	 * 
	 * @param time The time
	 * @return The total number of completed tasks
	 */
	public int getNumCompleted( int time ) {
		int sum = 0;
		for( Task task : getI( ).getTasks( ) )
			sum += (isCompleted( task, time ) ? 1 : 0);
		
		return sum;
	}
	
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
	public static MPPPlan fromPolicy( Policy policy, MPPState initstate, OutcomeRealisation outcomes ) throws SolverException {
		final MPPInstance I;
		try {
			I = (MPPInstance)policy.getInstance( );
		} catch( ClassCastException cce ) {
			throw new SolverException( "The specified policy is not for the MPP domain." );
		}
		
		// copy the initial state
		MPPState state = initstate.copy( );
		
		// query the policy until the horizon has been reached
		for( int t = state.getTime( ); t < I.getHorizon( ); t++ ) {
			final JointAction ja = policy.query( state );
			final RealisedJAction ra = outcomes.getOutcomes( ja, t );
			
			try {
				state = (MPPState)state.execute( ra );
			} catch( InfeasibleStateException ise ) {
				throw new SolverException( "Failed to create plan from policy", ise );
			}
		}
		
		// returns the plan
		final MPPPlan plan = state.getHistory( );
		return plan;
	}			
		
	/**
	 * Checks if the plan is feasible for this instance
	 * 
	 * @return True if the plan is feasible
	 */
	public boolean validate( ) {
		// check if all task must complete
		if( getI( ).mustCompleteAll( ) )
			for( Task t : getI( ).getTasks( ) )
				if( getTime( t ) == -1 )
					return false;
		
		return true;
	}	
	
	/**
	 * Two plans equal if they plan the same activities and all at the same time.
	 * This overrides the default function because it is much faster that the
	 * naive 'check-all' equals in the super class.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof MPPPlan) ) return false;
		final MPPPlan p = (MPPPlan)obj;
		
		// compare horizon
		if( getHorizon( ) != p.getHorizon( ) ) return false;
		
		// compare agent plans
		if( !plans.equals( p.plans ) ) return false;
		
		return true;
	}	
	
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		final NumberFormat df = Util.dec( );
		String str = "[Plan]\nAgents: " + plans.size( ) + ", horizon: " + getHorizon( ) + "\n";
		str += "Total value: " + df.format( getValue( ).getTotal( ) ) + "\n";
		str += "Profits: " + df.format( getProfit( ) ) + " (Rev: " + df.format( getRevenue( ) ) + ", Cost: " + df.format( getCost( ) ) + ")\n";
		str += "Network cost: " + df.format( getValue( ).getNetworkReward( ) ) + "\n";
		
		str += "\nTime ";
		for( int t = 0; t < getHorizon( ); t++ )
			str += "\t " + t;

		for( AgentPlan ap : plans.values( ) )
			str += "\n" + ap.toString( );		
		return str;
	}	
	
	/**
	 * Private class containing a single agent plan
	 */
	private class AgentPlan {
		/** The agent */
		protected final Agent agent;
		
		/** Its plan */
		protected final HashMap<Task, PlanTask> plan;
		
		/** The total revenue of this plan */
		protected double revenue;
		
		/** The total task cost of this plan */
		protected double cost;
		
		/**
		 * Creates a new single-agent plan
		 * 
		 * @param agent The agent to create it for
		 */
		public AgentPlan( Agent agent ) {
			this.agent = agent;
			plan = new HashMap<Task, PlanTask>( );
			
			revenue = 0;
			cost = 0;
		}
		
		/**
		 * Copies an existing plan
		 * 
		 * @param agentplan The agent plan to copy
		 */
		public AgentPlan( AgentPlan agentplan ) {
			this.agent = agentplan.agent;
			
			// copy maps
			//plan = new HashMap<Task, PlanTask>( agentplan.plan );
			plan = new HashMap<Task, PlanTask>( );
			for( Task t : agentplan.plan.keySet( ) )
				plan.put( t, new PlanTask( agentplan.plan.get( t ) ) );
			
			// copy stored revenue and cost
			revenue = agentplan.revenue;
			cost = agentplan.cost;
		}
		
		/**
		 * @return All planned tasks
		 */
		public Set<Task> getPlanned( ) {
			final Set<Task> tasks = new HashSet<Task>( );
			for( Task t : plan.keySet( ) )
				if( plan.get( t ) != null )
					tasks.add( t );
			
			return tasks;
		}
		
		/**
		 * Adds the task to the plan at the given time
		 * 
		 * @param task The task to add
		 * @param time The time to add it add
		 * @param delay The task delay status
		 * @throws InvalidPlanException if 1) the task cannot be completed
		 * within the horizon, 2) adding the task violates the single-task
		 * constraint, 3) the time is not within the horizon or 4) the task is
		 * already part of the plan 
		 */
		protected void add( Task task, int time, DelayStatus delay ) throws InvalidPlanException {
			if( task.isContinue( ) || task.isNoOp( ) ) return;
			
			// constraint 1
			if( task.getDuration( delay.isPossiblyDelayed( ) ) + time - 1 >= getHorizon( ) )
				throw new InvalidPlanException( "Unable to complete the task within the horizon (task: " + task.getFullID( ) + ", time: " + time + ", horizon: " + getHorizon( ) + ")" );

			// constraint 4
			if( plan.containsKey( task ) )
				throw new InvalidPlanException( "Task already planned (task: " + task.getFullID( ) + ", current planned time: " + getTime( task ) + ", new time: " + time + ")" );
			
			// constraint 2
			for( int t = time; t <= time + task.getDuration( ) - 1; t ++ )
				if( getTask( t ) != null )
					throw new InvalidPlanException( "Task violates the single-task constraint (task: " + task.getFullID( ) + ", violation time: " + t + ", violating task: " + getTask( t ).getFullID( ) + ")" );
			
			// constraint 3
			if( time >= getHorizon( ) )
				throw new InvalidPlanException( "Time is not within planning horizon (time: " + time + ", horizon: " + getHorizon( ) + " )" );
			
			// all good, plan the task and set its delay status
			set( task, time, DelayStatus.Pending );
			
			// immediately set the delay status as this veryfies whether that is allowed
			if( delay != DelayStatus.Pending ) {
				try {
					setDelayStatus( task, delay.isDelayed( ) );					
				} catch( InvalidPlanException ipe ) {
					// remove it from the plan and throw exception again
					del( task );
					throw ipe;
				}
			}
			
			// add the revenue and cost
			revenue += task.getRevenue( );
			cost += task.getCost( time, getHorizon( ), false );
		}
		
		/**
		 * Removes the task from the plan
		 * 
		 * @param task The task to remove
		 * @param validate Validate the removal?
		 * @return The time at which it was planned
		 * @throws InvalidPlanException if validate == true and the task status was
		 * not pending
		 */
		protected int remove( Task task, boolean validate ) throws InvalidPlanException {
			// check if the method was not executing
			final PlanTask pt = get( task );
			if( validate && pt.status != DelayStatus.Pending )
				throw new InvalidPlanException( "Cannot remove executed task!" );
			
			// remove it and store previous time
			final int time = del( task ).time;
			
			// update costs
			revenue -= task.getRevenue( );
			cost -= task.getCost( time, getHorizon( ), false );			
			
			// return the planned time
			return time;
		}
		
		/**
		 * Retrieves the time at which the task is currently planned
		 * 
		 * @param task The task
		 * @return The time or -1 if it is not in the plan
		 */
		protected int getTime( Task task ) {
			final PlanTask t = get( task );
			return (t != null ? t.time : -1);
		}
		
		/**
		 * Retrieves the duration for the task, considering its delay status
		 * 
		 * @param task The task
		 * @return The total planned time for the task, including delay time if
		 * the delay status is Delayed
		 */
		protected int getDuration( Task task ) {
			return task.getDuration( getDelayStatus( task ).isDelayed( ) );
		}
		
		/**
		 * Retrieves the task planned at the given time
		 * 
		 * @param time The time
		 * @return The task that is planned then or null if no task is planned
		 */
		protected Task getTask( int time ) {
			// go through all tasks to find a match
			for( Task t : plan.keySet( ) ) {
				final int start = getTime( t );
				if( start <= time && start + getDuration( t ) > time )
					return t;
			}
			
			return null;
		}
		
		/**
		 * Retrieves the delay status of the task
		 * 
		 * @param task The task
		 * @return The delay status of the task
		 */
		protected DelayStatus getDelayStatus( Task task ) {
			return get( task ).getStatus( );
		}
		
		/**
		 * Sets the delay status of a task, only possible when a task is pending.
		 * Updates plan value accordingly.
		 * 
		 * @param task The task
		 * @param delay True if the task is to be delayed
		 * @throws InvalidPlanException if the status was not pending or if this
		 * causes a violation of the single-task constraint
		 */
		protected void setDelayStatus( Task task, boolean delay ) throws InvalidPlanException {
			// check if doing so will not violate the single-task constraint
			if( delay ) {
				final int tstart = get( task ).time + task.getDuration( );
				for( int t = tstart; t <= tstart + task.getDelayDur( ) - 1; t++ )
					if( getTask( t ) != null )
						throw new InvalidPlanException( "Delaying causes an invalid plan! Task: " + task );
			}
			
			// update the status, will throw exception if invalid
			get( task ).setStatus( delay );
			
			// add the additional cost of the delay if required
			if( delay ) {
				// remove previous cost and add delayed cost
				final int t = getTime( task );
				cost -= task.getCost( t, getHorizon( ), false );
				cost += task.getCost( t, getHorizon( ), true );
			}
		}
		
		/**
		 * Clears the delay status (back to pending) for all planned tasks. This is
		 * used by solvers to reset the status after a run.
		 */
		protected void clearStatus( ) {
			for( Task task : plan.keySet( ) )
				clearStatus( task );
		}
		
		/**
		 * Clears the delay status (back to pending), used by solver before
		 * returning the plan.
		 * 
		 * @param task The task to clear
		 * @return The previous delay status
		 */
		protected DelayStatus clearStatus( Task task ) {
			final DelayStatus prevstatus = get( task ).clearStatus( );
			
			// update cost if required
			if( prevstatus == DelayStatus.Delayed ) {
				final int t = getTime( task );
				cost -= task.getCost( t, getHorizon( ), true );
				cost += task.getCost( t, getHorizon( ), false );
			}
			
			return prevstatus;
		}
	
		/**
		 * Returns the PlanTask object for the task
		 * 
		 * @param task The task
		 * @return The plan task object or null if it is not planned
		 */
		private PlanTask get( Task task ) {
			return plan.get( task );
		}
		
		/**
		 * Creates and sets the new PlanTask object for the task
		 * 
		 * @param task The task
		 * @param time The time at which it is to be planned
		 * @param delay The delay status
		 */
		private void set( Task task, int time, DelayStatus delay ) {
			plan.put( task, new PlanTask( time, delay ) );
		}
		
		/**
		 * Removes the plan task object from the plan
		 * 
		 * @param task The task to remove it for
		 * @return The PlanTask object that was stored
		 */
		private PlanTask del( Task task ) { 
			return plan.remove( task );
		}
		
		/**
		 * Two agent plans equal if the same activities are planned and all at the
		 * same time
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals( Object obj ) {
			if( obj == null || !(obj instanceof AgentPlan) ) return false;
			final AgentPlan ap = (AgentPlan) obj;
						
			return plan.equals( ap.plan );
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString( ) {
			final NumberFormat df = Util.dec( );
			
			String str = "Agent " + agent.ID + ": ";
			for( int t = 0; t < getHorizon( ); t++ ) {
				final Task task = getTask( t );
				str += (task != null ? task.getFullID( ) : "NoOp");
				if( task != null && getTime( task ) + task.getDuration( ) <= t ) str += " d";
				str += "\t";
			}
			str += "P: " + df.format( revenue + cost ) + " (R: " + df.format( revenue ) + ", C: " + df.format( cost ) + ")";
			return str;
		}
		
		/**
		 * Private class that holds all relevant data for a single planned task
		 */
		private class PlanTask {
			/** The time at which it is planned */
			public final int time;
			
			/** The delay status of a task */
			protected DelayStatus status;
						
			/**
			 * Copies an existing plan task
			 * 
			 * @param pt The plantask to copy
			 */
			public PlanTask( PlanTask pt ) {
				this( pt.time, pt.status );
			}
			
			/**
			 * Adds a new plan task
			 * 
			 * @param time The time at which it is planned
			 * @param status The delay status to plan it with
			 */
			public PlanTask( int time, DelayStatus status ) {
				this.time = time;
				this.status = status;
			}
			
			/**
			 * @return The delay status
			 */
			protected DelayStatus getStatus( ) {
				return status;
			}
			
			/**
			 * Sets the delay status, only possible when status is pending
			 * 
			 * @param delay True if the task is to be delayed
			 * @throws InvalidPlanException if the status was not pending 
			 */
			protected void setStatus( boolean delay ) throws InvalidPlanException {
				if( status != DelayStatus.Pending )
					throw new InvalidPlanException( "Delay status can only be set for pending tasks!" );
				
				status = (delay ? DelayStatus.Delayed : DelayStatus.NotDelayed);
			}
			
			/**
			 * Clears the status of the task (i.e. set to pending), used before
			 * returning solver results
			 * 
			 * @return Previous status
			 */
			protected DelayStatus clearStatus( ) {
				final DelayStatus prevstatus = getStatus( );
				status = DelayStatus.Pending;
				
				return prevstatus;
			}
			
			/**
			 * Plan tasks equal if both their time and status equal
			 * 
			 * @see java.lang.Object#equals(java.lang.Object)
			 */
			@Override
			public boolean equals( Object obj ) {
				if( obj == null || !(obj instanceof PlanTask) ) return false;
				final PlanTask pt = (PlanTask) obj;
				
				return (time == pt.time && status.equals( pt.status ) );
			}
		}
	}
}
