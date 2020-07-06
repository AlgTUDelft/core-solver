/**
 * @file FactoredCollection.java
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
 * @date         17 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.util;

import java.util.Collection;
import java.util.HashSet;

import core.model.policy.Agent;


/**
 * Collection of objects factored by agents
 * 
 * @author Joris Scharpff
 * @param <T> The type of objects contained in this collection
 */
public abstract class FactoredCollection<T> {
	/** The factored collection */
	protected final Collection<T> collection;
	
	/**
	 * Creates a new factored container
	 */
	public FactoredCollection( ) {
		collection = new HashSet<T>( );
	}
	
	/**
	 * Copies the collection
	 * 
	 * @param fcoll The collection to copy
	 */
	public FactoredCollection( FactoredCollection<T> fcoll ) {
		collection = new HashSet<T>( fcoll.collection );
	}
	
	/**
	 * Adds an object to the collection
	 * 
	 * @param object The object to add
	 */
	public void add( T object ) {
		assert !collection.contains( object ) : "Duplicate object in collection: " + object; 
		assert get( getAgent( object ) ) == null :
			"Agent already has a object in collection " + get( getAgent( object ) );
		
		collection.add( object );
	}
	
	/**
	 * Removes an object from the collection
	 * 
	 * @param object The object to remove
	 */
	public void remove( T object ) {
		assert collection.contains( object ) : "Object " + object + " not in collection";
		collection.remove( object );
	}
	
	
	/**
	 * Checks if the collection has an object for the agent
	 *  
	 * @param agent The agent
	 * @return True iff the collection contains an element for the agent
	 */
	public boolean has( Agent agent ) {
		return get( agent ) != null;
	}

	
	
	/**
	 * Retrieves the object in the collection for the specified agent
	 * 
	 * @param agent The agent
	 * @return The object of that agent or null if not in collection
	 */
	public T get( Agent agent ) {
		for( T object : collection )
			if( getAgent( object ).equals( agent ) )
				return object;
		
		return null;
	}
	
	/**
	 * @return The collection of all objects
	 */
	public Collection<T> get( ) {
		return collection;
	}
	
	/**
	 * @return The size of the collection
	 */
	public int size( ) {
		return collection.size( );
	}
	
	/**
	 * @return The collection of agents in the map
	 */
	public Collection<Agent> getAgents( ) {
		final Collection<Agent> agents = new HashSet<Agent>( collection.size( ) );
		for( T obj : collection )
			agents.add( getAgent( obj ) );
		return agents;			
	}
	
	/**
	 * Abstract method to determine the agent from an object of type T
	 * 
	 * @param object The object
	 * @return The agent that owns the object
	 */
	protected abstract Agent getAgent( T object );
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( obj == null || !(obj instanceof FactoredCollection<?>) ) return false;
		final FactoredCollection<?> coll = (FactoredCollection<?>)obj;
	
		return collection.equals( coll.collection );
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		return collection.toString( );
	}
}
