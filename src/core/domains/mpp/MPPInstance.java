/**
 * @file MPPInstance.java
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
 * @date         19 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains.mpp;

import java.util.HashSet;
import java.util.Set;

import core.domains.Instance;
import core.domains.mpp.MPPInstanceParameters.DelayMethod;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.policy.OutcomeRealisation;
import core.model.sharedreward.SharedReward;


/**
 * Instance of the Maintenance Planning Problem
 * 
 * @author Joris Scharpff
 */
public class MPPInstance extends Instance {
	/** Instance parameters */
	protected MPPInstanceParameters IP;
	
	/**
	 * Creates a new MPP instance that was generated
	 * 
	 * @param agents The agents
	 * @param IP The instance parameters
	 */
	public MPPInstance( Set<Agent> agents, MPPInstanceParameters IP ) {
		super( agents, IP.horizon );
		
		setInitialState( new MPPState( this ) );
		this.IP = IP;
	}
	
	/**
	 * @see core.domains.Instance#getInitialState()
	 */
	@Override
	public MPPState getInitialState( ) {
		return (MPPState)super.getInitialState( );
	}
	
	/**
	 * @see core.domains.Instance#getEmptyValue()
	 */
	@Override
	public MPPStateValue getEmptyValue( ) {
		return new MPPStateValue( );
	}
	
	/**
	 * @see core.domains.Instance#getEmptyPlan()
	 */
	@Override
	public MPPPlan getEmptyPlan( ) {
		return new MPPPlan( this );
	}
	
	/**
	 * @return True if all tasks of the instance must complete
	 */
	public boolean mustCompleteAll( ) {
		return IP.mustComplete( );
	}
	
	/**
	 * @return The method used to realise delays
	 */
	public DelayMethod getDelayMethod( ) {
		return IP.getDelayMethod( );
	}
	
	/**
	 * @return The set of all tasks in the instance
	 */
	public Set<Task> getTasks( ) {
		final Set<Task> tasks = new HashSet<Task>( );
		for( Action a : getActions( ) )
			tasks.add( (Task)a );
		return tasks;
	}
	
	/**
	 * Creates a copy of the instance with task durations to match the delay
	 * realisation. Each task will be transformed using the following rules:
	 * 
	 * 1) task is not delayed => task duration same, delay probability and delay
	 * duration set to zero.
	 * 
	 * 2) task is delayed => task duration set to regular + delay duration, delay
	 * probability and duration set to zero
	 * 
	 * 3) delay unknown => task is kept unchanged
	 * 
	 * @param outcomes The outcome realisation
	 * @return The copy of the instance with new durations.
	 */
	@Override
	public MPPInstance toDeterministicInstance( OutcomeRealisation outcomes ) {
		// copy all agents
		final Set<Agent> AG = new HashSet<Agent>( agents.size( ) );
		
		// copy all agents
		for( Agent A : agents ) {
			final Agent newA = new Agent( A.ID );
			
			// copy tasks with new lengths
			for( Action act : A.getActions( ) ) {
				final Task T = (Task) act;
				newA.addAction( new Task( T, DelayStatus.fromOutcome( (MPPOutcome)outcomes.getOutcome( T, 0 ) ) ) );
			}
			
			// set the agent
			AG.add( newA );
		}
		
		// create the instance
		final MPPInstance I = new MPPInstance( AG, IP );
		I.setGenerationSeed( seed );
		
		// copy social cost model
		final SharedReward scr = getSharedReward( );
		if( scr != null )
			scr.copyFor( I );
		
		// return the new instance
		return I;
	}
	
	/**
	 * Creates a copy of the instance with cost and social cost weighted using
	 * the specified weights
	 * 
	 * @see core.domains.Instance#getWeightedInstance(double[])
	 */
	@Override
	public MPPInstance getWeightedInstance( double[] w ) throws IllegalArgumentException {
		if( w.length != 2 ) throw new IllegalArgumentException( "Exactly two weights are required to transform an MPP instance!" );
		
		final double costweight = w[0];
		final double scweight = w[1];
		
		// copy all agents
		final Set<Agent> AG = new HashSet<Agent>( agents.size( ) );
		for( Agent A : agents ) {
			final Agent newA = new Agent( A.ID );
			
			// copy tasks with new lengths
			for( Action act : A.getActions( ) ) {
				final Task T = (Task)act;
				
				newA.addAction( new Task( A, T.ID, T.getRevenue( ) * costweight, T.getRewardFunction( ).copy( costweight ), T.getDuration( ), T.getDelayProb( ), T.getDelayDur( ) ) );
			}
			
			// set the agent
			AG.add( newA );
		}
		
		// create the instance
		final MPPInstance I = new MPPInstance( AG, IP );
		I.setGenerationSeed( seed );
		
		// copy social cost model
		final SharedReward scr = getSharedReward( );
		if( scr != null ) {
			final SharedReward newscr = scr.copyFor( I );

			newscr.setWeight( scweight );
		}
		
		// return the new instance
		return I;		
	}
	
	/**
	 * Creates a copy of the instance without the specified agent, useful to
	 * compute policies \pi_{-i}
	 * 
	 * @param agent The agent to remove
	 * @return A copy of the instance without I
	 * @throws IllegalArgumentException if the agent is not in the instance
	 */
	public MPPInstance without( Agent agent ) throws IllegalArgumentException {
		if( !agents.contains( agent ) )
			throw new IllegalArgumentException( "Agent '" + agent + "' not part of this instance" );

		// copy all agents
		final Set<Agent> AG = new HashSet<Agent>( agents.size( ) );
		for( Agent A : agents ) {
			// skip the agent to remove
			if( A.equals( agent ) ) continue;
			
			final Agent newA = new Agent( A.ID );
			
			// copy tasks with new lengths
			for( Action act : A.getActions( ) ) {
				final Task T = (Task)act;
				
				newA.addAction( new Task( A, T.ID, T.getRevenue( ), T.getRewardFunction( ).copy( ), T.getDuration( ), T.getDelayProb( ), T.getDelayDur( ) ) );
			}
			
			// set the agent
			AG.add( newA );
		}
		
		// create the instance
		final MPPInstanceParameters IP = new MPPInstanceParameters( this.IP );
		IP.N--;
		final MPPInstance I = new MPPInstance( AG, IP );
		I.setGenerationSeed( seed );
		
		// copy social cost model
		final SharedReward scr = getSharedReward( );
		if( scr != null ) scr.copyFor( I );
		
		// return the new instance
		return I;				
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		// output I details
		String str = "=====[ Instance ]=====\n";
		str += "Agents: " + getN( ) + "\n";
		str += "Tasks: ";
		for( Agent a : agents ) {
			str += a.getNumActions( ) + ", ";
		}
		str = str.substring( 0, str.length( ) - 2 );
		str += "\n";
		str += "Instance Parameters: " + IP.toString( ) + "\n";
				
		// output agents
		for( Agent a : agents ) {
			str += "\n[Agent " + a.ID + "]\n";
			for( Action act : a.getActions( ) ) {
				str += " -> " + ((Task) act).toLongString( ) + "\n";
			}
		}
		
		// output social cost model
		if( sharedreward != null ) {
			str += "\n" + sharedreward.getDescription( );
		}
		return str;
	}
}
