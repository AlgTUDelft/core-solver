/**
 * @file SocialCost.java
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
 * @date         27 jun. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.model.sharedreward;

import java.lang.reflect.InvocationTargetException;

import core.domains.Instance;
import core.domains.State;
import core.exceptions.IIOException;

/**
 * Container for all kinds of shared reward functions
 *
 * @author Joris Scharpff
 */
public abstract class SharedReward {
	/** The instance associated with this model */ 
	protected final Instance I;
	
	/** The reward weight factor for scaled rewards */
	protected double weight;
	
	/**
	 * Creates a new shared reward model for the instance
	 * 
	 * @param instance The instance
	 */
	public SharedReward( Instance instance ) {
		this.I = instance;
		instance.setSharedReward( this );
		
		clearWeight( );
	}
	
	/**
	 * Makes a copy of the shared reward model
	 * 
	 * @param instance The instance to copy it for
	 * @return The copy
	 */
	public final SharedReward copyFor( Instance instance ) {
		final SharedReward cp = copyCreate( instance );
		cp.copy( this );
		return cp;
	}
	
	/**
	 * Creates a copy of the reward model, should be implemented by non-abstract
	 * sub classes
	 * 
	 * @param instance The instance to create a copy for
	 * @return The copy
	 */
	protected abstract SharedReward copyCreate( Instance instance );
	
	/**
	 * Copies any abstract super class properties
	 * 
	 * @param parent The parent class to copy from
	 */
	protected void copy( SharedReward sr ) {
		weight = sr.weight;
	}
	
	/**
	 * Computes the shared reward for the specified state
	 * 
	 * @param state The global state
	 * @return The shared reward for that state, possibly scaled
	 */
	public final double getReward( State state ) {
		return computeReward( state );
	}
	
	/**
	 * Computes the shared reward for the specified state
	 * 
	 * @param state The global state
	 * @return The shared reward (non-weighted)
	 */
	protected abstract double computeReward( State state );
		
	/**
	 * Scales the shared reward by the specified weight
	 * 
	 * @param w The scale weight
	 */
	public void scale( double w ) {
		this.weight *= w;
	}
	
	/**
	 * Clears the scale weight
	 */
	public void clearWeight( ) {
		this.weight = 1.0;
	}
	
	/**
	 * @return The scale weight
	 */
	public double getWeight( ) {
		return weight;
	}
	
	/**
	 * Sets the weight
	 * 
	 * @param w The new weight
	 * @return The previous weight value
	 */
	public double setWeight( double w ) {
		final double oldweight = this.weight;
		this.weight = w;
		return oldweight;
	}

	/**
	 * String description of the shared reward model
	 * 
	 * @return The string that describes this model
	 */
	public abstract String getDescription( );
	
	/**
	 * Converts the shared reward model to a serialisable string
	 * 
	 * @return The string that can be serialised
	 */
	protected abstract String doSerialise( );
	
	/**
	 * Creates the shared reward model from a serialised string
	 * 
	 * @param serialised The serialised string
	 * @throws IIOException on error
	 */
	protected abstract void deserialise( String serialised ) throws IIOException;
	
	/**
	 * Serialises the social shared reward model
	 * 
	 * @return The shared reward model
	 */
	public String serialise( ) {
		return this.getClass( ).getName( ) + "|w=" + weight + ";" + doSerialise( );
	}
	
	/**
	 * Reads the shared reward model for the instance from a serialised string
	 * 
	 * @param instance The instance
	 * @param classname The shared reward model full class name
	 * @param serialised The serialised shared reward model string
	 * @return The social cost model
	 * @throws IIOException
	 */
	public static SharedReward deserialise( Instance instance, String serialised ) throws IIOException {
		try {
			final int idx = serialised.indexOf( ";" );
			final String classname;
			final double weight;
			if( serialised.indexOf( "|" ) > 0 ) {
				final String[] sr = serialised.substring( 0, idx ).split( "\\|" );
				classname = sr[0];
				weight = Double.parseDouble( sr[1].substring( 2 ) );
			} else {
				classname = serialised.substring( 0, idx );
				weight = 1.0;
			}
			final String s = serialised.substring( idx + 1 ); 
			
			// create the shared reward model and deserialise it
			final SharedReward rew = (SharedReward) Class.forName( classname ).getConstructor( Instance.class ).newInstance( instance );
			rew.deserialise( s );
			rew.scale( weight );
			return rew;
		} catch( Exception e ) {
			if( e instanceof NumberFormatException )
				throw new IIOException( "Invalid scale weight specified!" );
			if( e instanceof NoSuchMethodException )
				throw new IIOException( "Shared reward model does not implement a constructor (constructor(Instance))" );
			if( e instanceof InvocationTargetException )
				throw ((IIOException) ((InvocationTargetException) e).getTargetException( ) );
				
			e.printStackTrace( );
			throw new IIOException( "Failed to create shared reward model", e );
		}
	}
	
	/**
	 * @return The instance
	 */
	public Instance getI( ) {
		return I;
	}
}
