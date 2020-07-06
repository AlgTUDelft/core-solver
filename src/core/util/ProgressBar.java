/**
 * @file ProgressBas.java
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
 * @date         9 jan. 2016
 * @project      NGI
 * @company      Almende B.V.
 */
package core.util;


/**
 * Progress bar functionality that prints a text-style PB
 * 
 * @author Joris Scharpff
 */
public class ProgressBar {
	/** The minimum value */
	protected final long min;
	
	/** The maximum value */
	protected final long max;
	
	/** The number of bars to display */
	protected static final int BARS = 50;
	
	/** The value increase per bar */
	protected final double val_per_bar;
	
	/** Currently drawn number of bars */
	protected int bars;
	
	/** Current progress bar value */
	protected long value;

	/**
	 * Creates a new progress bar
	 * 
	 * @param title The title to display, null for no title 
	 * @param min The minimum value
	 * @param max The maximum value
	 */
	public ProgressBar( String title, long min, long max ) {
		this.min = min;
		this.max = max;
		value = min;
		
		val_per_bar = ((double)max - (double)min) / BARS;
		bars = 0;
		draw( title != null ? title : "" );
	}
	
	/**	
	 * Performs the initial draw of the progress bar
	 * 
	 * @param title The title to draw on the bar
	 */
	private void draw( String title ) {
		// setup title so that it can be centred
		String t = title;
		if( t.length( ) > BARS ) t = t.substring( 0, BARS );
		if( t.length( ) % 2 == 1 ) t += "-";
		
		// draw the progress bar
		System.out.print( "|" );
		for( int i = 0; i < (BARS - t.length( )) / 2; i++ ) System.out.print( "-" );
		System.out.print( t );
		for( int i = 0; i < (BARS - t.length( )) / 2; i++ ) System.out.print( "-" );
		System.out.print( "|\n|");
	}
	
	/**
	 * Advances the specified step
	 * 
	 * @param step The step value
	 * @return The new value
	 */
	public long step( long step ) {
		value += step;
		
		// draw additional bars
		while( value / val_per_bar > bars ) {
			System.out.print( "=" );
			bars++;
		}
		
		// draw end marker?
		if( bars == BARS ) {
			System.out.println( "|" );
			bars ++;
		}

		return value;
	}
	
	/**
	 * Advances to the maximum value, forcing the progress bar to draw its completed state
	 */
	public void setDone( ) {
		if( bars > BARS ) return;
		for( int i = bars; i < BARS; i++ )
			System.out.print( "=" );
		System.out.println( "|" );
	}
}
