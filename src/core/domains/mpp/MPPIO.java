/**
 * @file XMLIO.java
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
 * @date         16 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains.mpp;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.domains.Instance;
import core.domains.mpp.MPPInstanceParameters.DelayMethod;
import core.exceptions.IIOException;
import core.model.function.Function;
import core.model.policy.Action;
import core.model.policy.Agent;
import core.model.sharedreward.SharedReward;
import core.util.xml.XMLDoc;
import core.util.xml.XMLException;
import core.util.xml.XMLKey;

/**
 * XML reader/writer for instances
 *
 * @author Joris Scharpff
 */
public class MPPIO {
	/** String constants for key names */
	private enum Key {
		Agent,
		Agents,
		Instance,
		SocialCost,
		Task,
		Tasks;
	}

	/** String constants for field names */
	private enum Field {
		Cost,
		DelayDuration,
		DelayMethod,
		DelayProbability,
		Duration,
		Horizon,
		ID,
		Model,
		MustComplete,
		Revenue,
		Seed,
		Version
	}
	
	/** Versions number */
	private enum Version {
		V1, // last compatible version
		V2,	// adds mustcomplete parameter to instance
		V3,	// adds delay method parameter
		V4, // adds seed parameter
	}
	
	/** Current version number */
	private final static Version VERSION_CURRENT = Version.values( )[ Version.values( ).length - 1 ]; 
	
	/**
	 * Writes an MPP instance to a file
	 * 
	 * @param instance The instance to write
	 * @param file The file to write to
	 * @throws IIOException if the instance failed to write
	 */
	public void write( MPPInstance instance, File file ) throws IIOException {
		// create new XML document
		XMLDoc doc = new XMLDoc( );
		
		// add root key
		final XMLKey inst;
		try {
			inst = new XMLKey( doc, Key.Instance.toString( ), true );
		} catch( XMLException e ) {
			throw new IIOException( "Failed to write instance to file '" + file.getAbsolutePath( ) + "'", e );
		}
		
		// add version number to XML file
		addField( inst, Field.Version, VERSION_CURRENT.toString( ) );

		// write instance properties
		addField( inst, Field.Horizon, instance.getHorizon( ) );
		addField( inst, Field.MustComplete, instance.mustCompleteAll( ) );
		addField( inst, Field.DelayMethod, instance.getDelayMethod( ).ordinal( ) );
		addField( inst, Field.Seed, instance.getGenerationSeed( ) );
		
		// add all agents
		final XMLKey agents = key( inst, Key.Agents );
		for( Agent a : instance.getAgents( ) ) {
			// create agent key
			final XMLKey agent = key( agents, Key.Agent );
			
			// add ID
			addField( agent, Field.ID, a.ID );
			
			// and add all its tasks
			final XMLKey tasks = key( agent, Key.Tasks );
			for( Action act : a.getActions( ) ) {
				final XMLKey task = key( tasks, Key.Task );
				final Task t = (Task) act;
				
				// write fields
				addField( task, Field.ID, t.ID );
				addField( task, Field.Revenue, t.getRevenue( ) );
				addField( task, Field.Cost, t.getRewardFunction( ).serialise( ) );
				addField( task, Field.Duration, t.getDuration( ) );
				addField( task, Field.DelayProbability, t.getDelayProb( ) );
				addField( task, Field.DelayDuration, t.getDelayDur( ) );
			}
		}
		
		// add social cost model
		if( instance.hasSharedReward( ) ) {
			final XMLKey SC = key( inst, Key.SocialCost );
			addField( SC, Field.Model, instance.getSharedReward( ).serialise( ) );
		}
		
		try {
			doc.toFile( file.getAbsolutePath( ) );
		} catch( XMLException e ) {
			throw new IIOException( "Failed to write instance to file '" + file.getAbsolutePath( ) + "'", e );
		}
	}
	
	/**
	 * Reads an MPP instance from the file
	 * 
	 * @param file The file
	 * @return The instance
	 * @throws IIOException on read error
	 */
	public Instance read( File file ) throws IIOException {
		// create new XML document
		final XMLDoc doc;
		try {
			doc = new XMLDoc( file.getAbsolutePath( ) );
			
			// get instance root key
			final XMLKey inst = new XMLKey( doc, Key.Instance.toString( ), false );
			
			// read and check version number
			final Version version = Version.valueOf( getField( inst, Field.Version ) );
			if( version == null ) throw new IIOException( "Version incompatible" );
			
			// read the instance properties
			final int horizon = getIntField( inst, Field.Horizon );
			final boolean mustcomplete = !atleast( version, Version.V2 ) ? false : getBoolField( inst, Field.MustComplete );
			final DelayMethod delaymethod = !atleast( version, Version.V3 ) ? DelayMethod.Immediate : DelayMethod.values( )[ getIntField( inst, Field.DelayMethod ) ];
			final long seed = !atleast( version, Version.V4 ) ? -1 : getLongField( inst, Field.Seed );
			
			// read all agents
			int numdel = 0;
			int tlength = 0;
			final List<XMLKey> agentkeys = inst.getKey( Key.Agents.toString( ) ).getKeyList( Key.Agent.toString( ) );
			final Set<Agent> agents = new HashSet<Agent>( agentkeys.size( ) );
			for( int i = 0; i < agentkeys.size( ); i++ ) {
				final XMLKey agent = agentkeys.get( i );
				final int agentID = getIntField( agent, Field.ID );
				
				final Agent ag = new Agent( agentID );
				agents.add( ag );
				
				// read the tasks
				for( XMLKey task : agent.getKey( Key.Tasks.toString( ) ).getKeyList( Key.Task.toString( ) ) ) {
					final int taskID = getIntField( task, Field.ID );
					final double revenue = getDoubleField( task, Field.Revenue );
					final Function costfunc = Function.deserialise( getField( task, Field.Cost ) );
					final int duration = getIntField( task, Field.Duration );
					final double delprob = getDoubleField( task, Field.DelayProbability );
					final int deldur = getIntField( task, Field.DelayDuration );
					
					final Task t = new Task( ag, taskID, revenue, costfunc, duration, delprob, deldur ) ;
					ag.addAction( t );
					
					// count task length and number of delayable tasks (only for first
					// agent, the rest is similar)
					if( i == 0 ) tlength += t.getDuration( true );
					if( i == 0 ) if( t.canDelay( ) ) numdel++;
				}					
			}
			
			// create the instance
			final MPPInstanceParameters IP = new MPPInstanceParameters( agents.size( ), agents.iterator( ).next( ).getNumActions( ), horizon, numdel, horizon - tlength, mustcomplete, delaymethod );
			final MPPInstance I = new MPPInstance( agents, IP );
			I.setGenerationSeed( seed );
			
			// read social cost model, if any
			final XMLKey SC = inst.getKey( Key.SocialCost.toString( ), false );
			if( SC != null )
				SharedReward.deserialise( I, getField( SC, Field.Model ) );
			
			// return the instance
			return I;
		} catch( XMLException e ) {
			throw new IIOException( "An error occurred when reading from file '" + file.getAbsolutePath( ) + "'", e );
		}
	}

	/**
	 * Helper function to create new keys
	 * 
	 * @param parent The parent key
	 * @param keyname The key name enum constant
	 * @return The new XMLKey
	 */
	private XMLKey key( XMLKey parent, Key keyname ) {
		return new XMLKey( parent, keyname.toString( ) );
	}
	
	/**
	 * Helper function to add a field to a key
	 * 
	 * @param key The XMLKey
	 * @param field The field name enum constant
	 * @param value The field value (as string)
	 */
	private void addField( XMLKey key, Field field, String value ) {
		key.addField( field.toString( ), value );
	}

	/**
	 * Helper function to add an integer field to a key
	 * 
	 * @param key The XMLKey
	 * @param field The field name enum constant
	 * @param value The field value (as string)
	 */
	private void addField( XMLKey key, Field field, int value ) {
		key.addField( field.toString( ), "" + value );
	}

	
	/**
	 * Helper function to add a field to a key, formats a double
	 * 
	 * @param key The XMLKey
	 * @param field The field name enum constant
	 * @param value The field value
	 */
	private void addField( XMLKey key, Field field, double value ) {
		key.addField( field.toString( ), "" + value );
	}
	
	/**
	 * Helper function to add a field to a key
	 * 
	 * @param key The XMLKey
	 * @param field The field name enum constant
	 * @param value The field value
	 */
	private void addField( XMLKey key, Field field, boolean value ) {
		key.addField( field.toString( ), "" + value );
	}
	
	/**
	 * Helper function to add a field to a key
	 * 
	 * @param key The XMLKey
	 * @param field The field name enum constant
	 * @param value The field value
	 */
	private void addField( XMLKey key, Field field, long value ) {
		key.addField( field.toString( ), "" + value );
	}

	/**
	 * Helper function to read a String field
	 * 
	 * @param key The key to read from
	 * @param field The field to read
	 * @return The field value
	 * @throws XMLException if the field does not exist
	 */
	private String getField( XMLKey key, Field field ) throws XMLException {
		return key.getField( field.toString( ) );
	}
	
	/**
	 * Helper function to read a boolean field
	 * 
	 * @param key The key name
	 * @param field The field name
	 * @return The boolean value
	 * @throws XMLException if the field does not exist
	 * @throws IIOException if the field is not a valid boolean
	 */
	private boolean getBoolField( XMLKey key, Field field ) throws XMLException, IIOException {
		try {
			return Boolean.parseBoolean( getField( key, field ) );
		} catch( NumberFormatException nfe ) {
			throw new IIOException( "Invalid field value: '" + getField( key, field ) + "'", nfe );
		}
	}
	
	/**
	 * Helper function to read an integer field
	 * 
	 * @param key The key name
	 * @param field The field name
	 * @return The integer value
	 * @throws XMLException if the field does not exist
	 * @throws IIOException if the field is not a valid integer
	 */
	private int getIntField( XMLKey key, Field field ) throws XMLException, IIOException {
		try {
			return Integer.parseInt( getField( key, field ) );
		} catch( NumberFormatException nfe ) {
			throw new IIOException( "Invalid field value: '" + getField( key, field ) + "'", nfe );
		}
	}
	
	/**
	 * Helper function to read an double field
	 * 
	 * @param key The key name
	 * @param field The field name
	 * @return The double value
	 * @throws XMLException if the field does not exist
	 * @throws IIOException if the field is not a valid double
	 */
	private double getDoubleField( XMLKey key, Field field ) throws XMLException, IIOException {
		try {
			return Double.parseDouble( getField( key, field ) );
		} catch( NumberFormatException nfe ) {
			throw new IIOException( "Invalid field value: '" + getField( key, field ) + "'", nfe );
		}
	}
	
	/**
	 * Helper function to read a long field
	 * 
	 * @param key The key name
	 * @param field The field name
	 * @return The long value
	 * @throws XMLException if the field does not exist
	 * @throws IIOException if the field is not a valid long
	 */
	private long getLongField( XMLKey key, Field field ) throws XMLException, IIOException {
		try {
			return Long.parseLong( getField( key, field ) );
		} catch( NumberFormatException nfe ) {
			throw new IIOException( "Invalid field value: '" + getField( key, field ) + "'", nfe );
		}
	}	
	
	/**
	 * Checks if the version number is at least the specified version 
	 * 
	 * @param version The read version number
	 * @param reqversion The required version
	 */
	private boolean atleast( Version version, Version required ) {
		return version.ordinal( ) >= required.ordinal( );
	}

}
