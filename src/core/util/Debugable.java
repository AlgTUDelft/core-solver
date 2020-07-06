/**
 * @file Debuggable.java
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
 * @date         28 feb. 2014
 * @project      NGI
 * @company      Almende B.V.
 */
package core.util;

import java.io.PrintStream;


/**
 * Provides boiler plate code for debuggable objects
 * 
 * @author Joris Scharpff
 */
public abstract class Debugable {
	/** Is debug enabled? */
	protected boolean debug;
	
	/** Spacing used before debug message, useful for recursive debug */
	protected int dbg_spacing;
	
	/** The output stream */
	protected PrintStream outstream;
	
	/**
	 * Creates a new debuggable object
	 */
	public Debugable( ) {
		debug = false;
		dbg_spacing = 0;
		outstream = System.out;
	}
	
	/**
	 * @return True if debug is enabled
	 */
	public boolean isDebug( ) {
		return debug;
	}
	
	/**
	 * Enables/disable debugging
	 * 
	 * @param enable True to enable debugging
	 */
	public void setDebug( boolean enable ) {
		this.debug = enable;
	}

	/**
	 * Sets the out stream for the debug
	 * 
	 * @param outstream The output stream to output debug info to
	 */
	public void setDebugStream( PrintStream outstream ) {
		this.outstream = outstream;
	}
	
	/**
	 * Sets the debug parameters of the debuggable object to match this settings
	 * 
	 * @param deb The debuggable object
	 */
	public void dbg_set( Debugable deb ) {
		deb.debug = debug;
		deb.dbg_spacing = dbg_spacing;
		deb.outstream = outstream;
	}
	
	/**
	 * Adds a new level of spacing
	 */
	public void dbg_incSpacing( ) {
		dbg_spacing += 2;
	}
	
	/**
	 * Removes a level of spacing
	 */
	public void dbg_decSpacing( ) {
		dbg_spacing -= 2;
		if( dbg_spacing < 0 ) dbg_spacing = 0;
	}
	
	/**
	 * Clears the current spacing
	 */
	public void dbg_clearSpacing( ) {
		dbg_spacing = 0;
	}
	
	/**
	 * Ouputs an empty line
	 */
	public void dbg_newline( ) {
		dbg_msg( "" );
	}
	
	/**
	 * Outputs the object using the toString method if debug is enabled
	 * 
	 * @param object The object
	 */
	public void dbg_msg( Object object ) {
		if( !isDebug( ) ) return;

		dbg_msg( (object == null ? "(null)" : object.toString( )) );
	}
	
	/**
	 * Outputs a debug message if debug is enabled
	 * 
	 * @param message The debug message
	 */
	public void dbg_msg( String message ) {
		dbg_msg( message, true );
	}
	
	/**
	 * Outputs a debug message if debug is enabled
	 * 
	 * @param message The debug message
	 * @param newline True to print newline
	 */
	public void dbg_msg( String message, boolean newline ) {
		if( !isDebug( ) ) return;
		
		String s = "[debug] ";
		for( int i = 0; i < dbg_spacing; i++ )
			s += " ";
		s += message;
		
		outstream.print( s + (newline ? "\n" : "") );
		outstream.flush( );
	}
}
