/**
 * @file CRGCollection.java
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
 * @date         25 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import core.model.policy.Agent;


/**
 * Factored collection that also allows a collection of objects to match against
 * 
 * @author Joris Scharpff
 * @param <T> The object type in the collection 
 */
public abstract class FactoredOtherCollection<T> extends FactoredCollection<T> {
	/** The set of 'other' objects per agent */
	protected Map<Agent, Collection<T>> other;
	
	/**
	 * Creates a new empty collection with no other set 
	 */
	public FactoredOtherCollection( ) {
		super( );
		
		other = new HashMap<Agent, Collection<T>>( );
	}
	
	/**
	 * Creates a copy of the specified other collection
	 * 
	 * @param coll The other collection
	 */
	public FactoredOtherCollection( FactoredOtherCollection<T> coll ) {
		super( coll );
		
		this.other = new HashMap<Agent, Collection<T>>( coll.other );
	}
	
	/**
	 * Sets the collection of 'other' objects for the agent
	 * 
	 * @param agent The agent
	 * @param other The other object collection
	 */
	public void setOther( Agent agent, Collection<T> other ) {
		assert get( agent ) == null : "The regular collection already contains objects";
		assert !this.other.containsKey( agent ) : "An other set is already set for agent " + agent;
		assert other.size( ) == 0 || getAgent( other.iterator( ).next( ) ).equals( agent ) : "Agents do not correspond"; 
		
		this.other.put( agent, other );
	}
	
	/**
	 * Clears the 'other' collection
	 * 
	 * @param agent The agent to clear the collection of
	 */
	public void clearOther( Agent agent ) {
		other.remove( agent );
	}
	
	/**
	 * Retrieves the other set for the agent
	 * 
	 * @param agent The agent
	 * @return The set of other objects
	 */
	public Collection<T> getOther( Agent agent ) {
		assert other.containsKey( agent ) : "No other collection for agent " + agent;
		return other.get( agent );
	}
	
	/**
	 * @param agent The agent
	 * @return True iff an other collection is set
	 */
	public boolean hasOther( Agent agent ) {
		return other.containsKey( agent );
	}
	
	/**
	 * Checks if the collection contains this object and the other set does not
	 * 
	 * @param obj The object to find
	 * @return True iff either the collection has the object stored for the agent
	 *         or there is an other set and it does not contain it.
	 */
	public boolean matches( T obj ) {
		if( collection.contains( obj ) ) return true;
		final Agent a = getAgent( obj );
		if( hasOther( a ) && !getOther( a ).contains( obj ) ) return true;
		
		return false;
	}

	/**
	 * @see core.util.FactoredCollection#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if( !super.equals( obj ) ) return false;
		if( !(obj instanceof FactoredOtherCollection<?>) ) return false;
		
		return other.equals( ((FactoredOtherCollection<?>)obj).other );
	}
	
	/**
	 * @see core.util.FactoredCollection#toString()
	 */
	@Override
	public String toString( ) {
		return super.toString( ) + " OA=" + (other != null ? other.toString( ) : "[]");
	}
}
