/**
 * @file CoordGraph.java
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
 * @date         28 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.solver.model.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import core.model.policy.Agent;
import core.solver.model.crg.CRGReward;


/**
 * Coordination graph of agents (nodes) and reward functions (edges) that
 * are still not CRI, corresponding to the state of the joint policy search.
 * 
 * @author Joris Scharpff
 */
public class CoordGraph {
	/** The agent nodes */
	protected Map<Agent, AgentNode> nodes;
	
	/** The reward edges */
	protected List<RewardEdge> edges;
	
	/** The strongly connected components in this CG */
	protected Collection<Collection<Agent>> components;
	
	/**
	 * Creates a new coordination graph with the specified reward functions
	 * 
	 * @param rewards The reward functions
	 */
	public CoordGraph( Collection<CRGReward> rewards ) {
		// extract the agent set from the rewards
		final Collection<Agent> agents = new HashSet<Agent>( );
		for( CRGReward r : rewards )
			agents.addAll( r.getScope( ) );
		
		// create initial nodes set
		nodes = new HashMap<Agent, AgentNode>( agents.size( ) );
		for( Agent a : agents )
			nodes.put( a, new AgentNode( a ) );
					
		// connect agents based on rewards
		edges = new ArrayList<RewardEdge>( );
		for( CRGReward r : rewards ) {
			// skip local rewards in the coordination graph
			if( r.size( ) == 1 ) continue;
			connect( r, new ArrayList<Agent>( r.getScope( ) ), 0 );
		}
		
		// create initial components set
		components = new ArrayList<Collection<Agent>>( );
		components.add( new HashSet<Agent>( agents ) );
	}

	/**
	 * Connects all combinations of agents in this reward function through
	 * binary dependencies, used later in CRI tests
	 * 
	 * @param rew The reward function
	 * @param agents The list of agents
	 * @param idx The index of the agent to link with all others
	 */
	protected void connect( CRGReward rew, List<Agent> agents, int idx ) {
		if( idx == agents.size( ) ) return;
		
		for( int j = 0; j < agents.size( ); j++ ) {
			if( idx == j ) continue;
			
			edges.add( new RewardEdge( rew, agents.get( idx ), agents.get( j ) ) );
		}
	}
	
	/**
	 * Updates the CG, checks remaining non-CRI edges for CRI given the new local
	 * state and returns the collection of edges that are now flagged CRI
	 * 
	 * @param state The new joint state
	 * @param forceupdate True if an update should be performed, even when no new
	 * 				CRI is detected 
	 * @return Collection of edges that have become CRI
	 */
	public Collection<RewardEdge> update( CRGJState state, boolean forceupdate ) {
		final Collection<RewardEdge> newcri = new ArrayList<RewardEdge>( );
		
		// test edges for CRI
		for( RewardEdge e : edges ) {
			// only if not already CRI and the agents are contained within the state
			if( e.isCRI( ) || !e.inScope( state.getAgents( ) ) ) continue;
			
			// check!
			if( e.checkCRI( state ) ) {
				assert e.isCRI( ) : "Edge is added to CRI list but it is not";
				newcri.add( e );
			}
		}		
		
		// should we update the components?
		if( forceupdate || newcri.size( ) > 0 ) updateComponents( );
			
		return newcri;
	}
	
	/**
	 * Updates the set of connected components
	 */
	protected void updateComponents( ) {
		components.clear( );
		
		// list of remaining agents
		final Collection<AgentNode> agents = new HashSet<AgentNode>( nodes.values( ) );
		
		// keep building components until we run out of agents or available edges
		while( agents.size( ) > 0 ) {
			// create component and use a stack to keep track of next-to-add nodes
			final Set<Agent> C = new HashSet<Agent>( );
			final Stack<AgentNode> next = new Stack<AgentNode>( );

			// add an agent from the remaining set to the stack
			next.push( agents.iterator( ).next( ) );
			
			while( next.size( ) > 0 ) {
				final AgentNode a = next.pop( );
				agents.remove( a );
				C.add( a.agent );

				// get its connections
				for( RewardEdge e : a.edges ) {
					if( e.isCRI( ) ) continue;
					final AgentNode na = e.other( a );
					if( !agents.contains( na ) ) continue;
					
					next.push( na );
				}
			}			
			// add the component
			components.add( C );
		}
		
		assert agents.size( ) == 0 : "Not all agents have been checked: " + agents;
	}
	
	/**
	 * Restores the CRG by 'unflagging' the edges that are specified in the 
	 * collection
	 * 
	 * @param edges The edges to restore
	 */
	public void restore( Collection<?> edges ) {
		for( Object e : edges )
			((RewardEdge)e).unsetCRI( );
		
		// update the components to reconnect when required
		updateComponents( );
	}
	
	
	/**
	 * Returns the connected components in the form of (scoped) joint states
	 * 
	 * @param state The current joint state
	 * @return The collection of connected component states
	 */
	public Collection<CRGJState> getConnectedComponents( CRGJState state ) {
		final Collection<CRGJState> states = new HashSet<CRGJState>( components.size( ) );
		
		// build a joint state of every component within this joint state
		for( Collection<Agent> c : components ) {			
			CRGJState s = new CRGJState( c );
			for( Agent a : c ) { 
				if( !state.has( a ) ) {
					// not the right component, abandon!
					s = null;
					break;
				}
				
				assert state.get( a ) != null : "Local state not set in " + state;
				s.set( state.get( a ) );
			}
			
			assert s == null || s.isValid( ) : "The state is not valid " + s; 
			if( s != null ) states.add( s );
		}
		return states;
	}
	
	/**
	 * @return The connected components of agents
	 */
	public Collection<Collection<Agent>> getComponents( ) {
		return components;
	}
	
	
	/**
	 * @return The number of components
	 */
	public int size( ) {
		return components.size( );
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		String str = "CG {";
		for( AgentNode a : nodes.values( ) ) {
			str += "\n> " + a + ": " + a.edges;
		}
		return str + " \n}";
	}
	
	
	
	/** Agent node */
	protected class AgentNode {
		/** The agent */
		protected final Agent agent;
		
		/** Edges this agent is connected by */
		protected final List<RewardEdge> edges;
		
		/**
		 * Creates a new agent node
		 * 
		 * @param agent The agent
		 */
		protected AgentNode( Agent agent ) {
			this.agent = agent;
			this.edges = new ArrayList<RewardEdge>( );
		}
		
		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode( ) {
			return agent.hashCode( );
		}
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals( Object obj ) {
			if( obj == null || !(obj instanceof AgentNode) ) return false;
			return agent.equals( ((AgentNode)obj).agent );
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString( ) {
			return agent.toString( );
		}
	}
	
	
	/** Edge for functions */
	protected class RewardEdge {
		/** The function */
		protected final CRGReward reward;
		
		/** CRI? */
		protected boolean CRI;
		
		/** The agents that are connected through this edge */
		protected final AgentNode[] agents;
		
		/**
		 * Creates a new reward edge between the agents
		 * 
		 * @param reward The reward function
		 * @param a1
		 * @param a2
		 */
		protected RewardEdge( CRGReward reward, Agent a1, Agent a2 ) {
			this.reward = reward;
			this.agents = new AgentNode[] { nodes.get( a1 ), nodes.get( a2 ) };
			
			this.CRI = false;
			
			// add edges to list in nodes
			agents[0].edges.add( this );
			agents[1].edges.add( this );
		}
		
		/**
		 * Checks if the reward is CRI now given the state, sets the flag if true
		 * 
		 * @param state The joint state
		 * @return True iff the reward is now CRI
		 */
		protected boolean checkCRI( CRGJState state ) {
			assert !isCRI( ) : "Function is already CRI for agents " + agents[0] + " and " + agents[1];
			// check if now CRI
			if( !reward.CRI( agents[0].agent, agents[1].agent, state ) ) return false;
				
			// flag CRI and return success
			this.CRI = true;
			return true;
		}
	
		/**
		 * Clears the CRI value from the edge, used to restore
		 */
		protected void unsetCRI( ) {
			this.CRI = false; 
		}
		
		/**
		 * @return True iff this edge is marked CRI, i.e. its two agents are CRI
		 */
		protected boolean isCRI( ) {
			return CRI;
		}
		
		/**
		 * Checks if this edge connects to the agent node
		 * 
		 * @param agent The agent node
		 */
		protected boolean contains( AgentNode agent ) {
			return agents[0].equals( agent ) || agents[1].equals( agent );
		}
		
		/**
		 * Checks if all of the edge's agents are contained in the collection
		 * 
		 * @param agents The agents
		 * @return True iff both agents of this reward edge are present
		 */
		protected boolean inScope( Collection<Agent> agents ) {
			return agents.contains( this.agents[0].agent ) &&
					agents.contains( this.agents[1].agent );
		}
		
		/**
		 * Returns the agent node on the other end
		 * 
		 * @param agent The agent node to start from
		 * @return The agent node on the other end
		 */
		protected AgentNode other( AgentNode agent ) {
			assert contains( agent ) : "Agent " + agent + " is not part of this edge";
			return agents[0].equals( agent ) ? agents[1] : agents[0];
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString( ) {
			return agents[0] + "<->" + agents[1] + (CRI ? "(CRI)" : ""); 
		}
	}
}
