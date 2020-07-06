/**
 * @file InstanceParameters.java
 * @brief Short description of file
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2014 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         10 mrt. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.domains.mpp;



/**
 *
 * @author Joris Scharpff
 */
public class MPPInstanceParameters {
	/** The problem horizon */
	protected int horizon;
	
	/** The number of agents */
	protected int N;
	
	/** The number of actions per agent */
	protected int A;
	
	/** The number of delayed tasks */
	protected int numdelayed;
	
	/** The number of NoOps (idle slots) */
	protected int noops;
	
	/** True if all tasks MUST be planned */
	protected boolean mustcomplete;
	
	/** The delay realisation method */
	protected DelayMethod delaymethod;
	
	/** The seed used to generate the instance (-1 for manual) */
	protected long seed;
	
	
	/** Possible delay realisation methods */
	public enum DelayMethod {
		/** delay is realised immediately on planning the task */
		Immediate, 
		/** delay becomes known after execution of the regular period */
		AfterRegular;
		
		/**
		 * @return The short descriptive code for the status
		 */
		public String getCode( ) {
			switch( this ) {
				case Immediate: return "I";
				case AfterRegular: return "AR";
				default: return null;
			}
		}
	}
	
	/**
	 * Creates a new instance parameters object for a custom instance
	 * 
	 * @param N The number of agents
	 * @param horizon The horizon
	 * @param mustcomplete True if all tasks must be completed
	 * @param delaymethod The delay method
	 */
	public MPPInstanceParameters( int N, int horizon, boolean mustcomplete, DelayMethod delaymethod ) {
		this( N, -1, horizon, -1, -1, mustcomplete, delaymethod );
	}
	
	/**
	 * Creates a new instance parameters object with default values for must complete 
	 * (true) and delay method (Immediate)
	 * 
	 * @param N The number of agents
	 * @param A The number of tasks per agent
	 * @param horizon The horizon length
	 * @param numdel The number of tasks that can be delayed
	 * @param noops The number of NoOps (idle slots)
	 * @throws IllegalArgumentException if the parameter combination is not valid
	 */
	public MPPInstanceParameters( int N, int A, int horizon, int numdel, int noops ) throws IllegalArgumentException {
		this( N, A, horizon, numdel, noops, true, DelayMethod.Immediate );
	}

	
	/**
	 * Creates a new instance parameters object
	 * 
	 * @param N The number of agents
	 * @param A The number of tasks per agent
	 * @param horizon The horizon length
	 * @param numdel The number of tasks that can be delayed
	 * @param noops The number of NoOps (idle slots)
	 * @param mustComplete True if all tasks must be planned
	 * @param delayMethod The delay realisation method
	 * @throws IllegalArgumentException if the parameter combination is not valid
	 */
	public MPPInstanceParameters( int N, int A, int horizon, int numdel, int noops, boolean mustComplete, DelayMethod delayMethod ) throws IllegalArgumentException {
		if( N <= 0 ) throw new IllegalArgumentException( "At least one agent must be in the instance" );
		if( A <= 0 && A != -1 ) throw new IllegalArgumentException( "There must be at least one task per agent (set to -1 for varying)" );
		if( numdel < 0 && numdel != -1 ) throw new IllegalArgumentException( "The number of delayed tasks cannot be negative" );
		if( numdel > A && A != -1 && numdel != -1 ) throw new IllegalArgumentException( "There cannot be more delaying tasks than tasks per agent" );
		if( noops < 0 && noops != -1 ) throw new IllegalArgumentException( "The number of NoOps cannot be negative" );
		if( !(A == -1 || numdel == -1 || noops == -1) && A + numdel + noops > horizon )
			throw new IllegalArgumentException( "Horizon length not sufficient for this combination of parameters a horizon of at least " + (A + numdel + noops) + " is required" );
		
		this.N = N;
		this.A = A;
		this.horizon = horizon;
		this.numdelayed = numdel;
		this.noops = noops;
		this.mustcomplete = mustComplete;
		this.delaymethod = delayMethod;
	}
	
	/**
	 * Copies an instance parameters object
	 * 
	 * @param ip The parameters to copy
	 */
	public MPPInstanceParameters( MPPInstanceParameters ip ) {
		this( ip.N, ip.A, ip.horizon, ip.numdelayed, ip.noops, ip.mustcomplete, ip.delaymethod );
	}
	
	/** @return The number of agents */
	public int getN( ) { return N; }
	
	/** @return The number of tasks per agent (-1 for varying) */
	public int getA( ) { return A; }
	
	/** @return The instance horizon */
	public int getH( ) { return horizon; }
	
	/** @return The number of delayed tasks */
	public int getNumDelayed( ) { return numdelayed; }
	
	/** @return The number of NoOps */
	public int getNoops( ) { return noops; }
	
	/** @return True if the instance tasks must all be planned, whatever the cost*/
	public boolean mustComplete( ) { return mustcomplete; }
	
	/** @return The delay method used in the instance */
	public DelayMethod getDelayMethod( ) { return delaymethod; }
	
	/**
	 * Sets the instance seed
	 * 
	 * @param seed The seed used to generate the instance this describes
	 */
	public void setSeed( long seed ) {
		this.seed = seed;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString( ) {
		String str = "";
		str += "N" + N;
		str += "_A" + A;
		str += "_h" + horizon;
		str += "_d" + numdelayed;
		str += "_n" + noops;
		str += "_p" + (mustcomplete ? "C" : "") + delaymethod.getCode( );
		
		return str;
	}
}
