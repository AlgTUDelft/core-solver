/**
 * @file MPPOutcome.java
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

import core.model.policy.Action;
import core.model.policy.Outcome;
import core.model.policy.OutcomeRealisation;
import core.util.RandGenerator;


/**
 * Outcome of an MPP task
 * 
 * @author Joris Scharpff
 */
public class MPPOutcome extends Outcome {
	/** Delayed or not */
	protected final boolean delayed;
	
	/**
	 * Creates a new MPPOutcome
	 * 
	 * @param task The task for which this outcome is made
	 * @param delayed True if the outcome means the task is delayed
	 */
	public MPPOutcome( Task task, boolean delayed ) {
		this( delayed, delayed ? task.getDelayProb( ) : 1 - task.getDelayProb( ) );
	}
	
	/**
	 * Creates a new MPPOutcome
	 * 
	 * @param delayed True if the outcome means the task is delayed
	 * @param prob The probability of this outcome
	 */
	public MPPOutcome( boolean delayed, double prob ) {
		super( prob );
		
		this.delayed = delayed;
	}	
	
	/**
	 * @return The delay outcome
	 */
	public boolean isDelayed( ) {
		return delayed;
	}
	
	/**
	 * @see core.model.policy.Outcome#getDescription()
	 */
	@Override
	public String getDescription( ) {
		return delayed ? "DEL" : "REG";
	}
	
	/**
	 * @see core.model.policy.Outcome#equalsOutcome(core.model.policy.Outcome)
	 */
	@Override
	protected boolean equalsOutcome( Outcome out ) {
		if( !(out instanceof MPPOutcome) ) return false;
		
		return delayed == ((MPPOutcome)out).delayed;
	}
	
	/**
	 * Generates an outcome realisation for the MPP domain by setting all to
	 * the specified delay status
	 * 
	 * @param inst The MPP instance
	 * @param delay True to delay all, false to delay none
	 * @return The outcome realisation object
	 */
	public static OutcomeRealisation getRealisation( MPPInstance inst, boolean delay ) {
		final OutcomeRealisation or = new OutcomeRealisation( inst );
		
		// set the outcome for all actions and all time steps
		for( Action action : inst.getActions( ) ) {
			for( int time = inst.getInitialState( ).getTime( ); time < inst.getHorizon( ); time++ ) {
				final Task task = (Task)action;
				if( task.canDelay( ) )				
					or.setOutcome( task, time, new MPPOutcome( delay, delay ? task.getDelayProb( ) : 1.0 - task.getDelayProb( ) ) );
				else
					or.setOutcome( task, time, new MPPOutcome( false, 1.0 ) );
			}
		}
		
		return or;
	}
	
	/**
	 * Generates an outcome realisation for the MPP domain by setting all to
	 * the specified delay status to their expected outcome
	 * 
	 * @param inst The MPP instance
	 * @return The outcome realisation object
	 */
	public static OutcomeRealisation getRealisation( MPPInstance inst ) {
		final OutcomeRealisation or = new OutcomeRealisation( inst );
		
		// set the outcome for all actions and all time steps
		for( Action action : inst.getActions( ) ) {
			for( int time = inst.getInitialState( ).getTime( ); time < inst.getHorizon( ); time++ )
				or.setOutcome( action, time, new MPPOutcome( ((Task) action).getDelayProb( ) > 0.5, 1.0 ) );
		}
		
		return or;
	}
	
	/**
	 * Generates an outcome realisation for the MPP domain using the specified
	 * random generator.
	 * 
	 * @param inst The MPP instance
	 * @param rand The random generator
	 * @return The outcome realisation object
	 */
	public static OutcomeRealisation getRealisation( MPPInstance inst, RandGenerator rand ) {
		final OutcomeRealisation or = new OutcomeRealisation( inst );
		
		// set the outcome for all actions and all time steps
		for( Action action : inst.getActions( ) ) {
			for( int time = inst.getInitialState( ).getTime( ); time < inst.getHorizon( ); time++ )
				or.setOutcome( action, time, new MPPOutcome( rand.randDbl( ) <= ((Task) action).getDelayProb( ), 1.0 ) );
		}
		
		return or;
	}
	
	/**
	 * @see core.model.policy.Outcome#toString()
	 */
	@Override
	public String toString( ) {
		return (delayed ? "D" : "N");
	}
}
