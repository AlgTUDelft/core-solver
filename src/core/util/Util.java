/**
 * @file Utile.java
 * @brief [brief description]
 *
 * This file is created at Almende B.V. It is open-source software and part of the Common
 * Hybrid Agent Platform (CHAP). A toolbox with a lot of open-source tools, ranging from
 * thread pools and TCP/IP components to control architectures and learning algorithms.
 * This software is published under the GNU Lesser General Public license (LGPL).
 *
 * Copyright � 2013 Joris Scharpff <joris@almende.com>
 *
 * @author       Joris Scharpff
 * @date         23 okt. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import core.domains.mpp.Task;
import core.model.policy.Agent;


/**
 * General utility functions
 * 
 * @author Joris Scharpff
 */
public class Util {
	/**
	 * Retrieves the set of agents that is contained by the specified set of
	 * tasks
	 * 
	 * @param tasks The tasks
	 * @return The set of agents that have a task in the task set
	 */
	public static Agent[] getAgents( Task[] tasks ) {
		final List<Agent> agents = new ArrayList<Agent>( );
		for( Task t : tasks )
			if( !agents.contains( t.getAgent( ) ) )
				agents.add( t.getAgent( ) );
		
		return agents.toArray( new Agent[0] );
	}
	
	/**
	 * Retrieves the maximum agent ID in the set of agents
	 * 
	 * @param agents The agents
	 * @return The maximum agent ID, -1 for empty set
	 */
	public static int maxID( Collection<Agent> agents ) {
		int maxID = -1;
		for( Agent a : agents )
			if( a.ID > maxID ) maxID = a.ID;
		
		return maxID;
	}
	
	/**
	 * Performs round-error safe equal checks of doubles
	 * 
	 * @param d1
	 * @param d2
	 * @return True iff |d1 - d2| < 0.00001
	 */
	public static boolean doubleEq( double d1, double d2 ) {
		return doubleEq( d1, d2, 0.000001 );
	}
	
	/**
	 * Performs round-error safe equal checks of doubles, with custom precision
	 * 
	 * @param d1
	 * @param d2
	 * @param prec
	 * @return True iff |d1 - d2| < prec
	 */
	public static boolean doubleEq( double d1, double d2, double prec ) {
		return d1 - d2 < prec && d2 - d1 < prec;
	}
	
	/**
	 * Computes the factorial of n
	 * 
	 * @param n
	 * @return n!
	 */
	public static int fact( int n ) {
		if( n == 1 )
			return 1;
		else
			return n * fact( n - 1 );
	}
	
	/**
	 * Copies a file without exception throwing
	 * 
	 * @param file The file to copy
	 * @param newfile The new file
	 * @return True on success
	 */
	public static boolean copyFileUnsafe( File file, File newfile ) {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(file);
	        os = new FileOutputStream(newfile);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } catch( IOException e) {
	    	// ignore the error
	    	return false;
			} finally {
				try {
					if( is != null ) is.close();
	        if( os != null ) os.close();
				} catch( IOException e ) {
					// ignore
					return false;
				} 
	    }
	    
	    return true;
	}
	
	/**
	 * Creates the standard decimal number formatter
	 * 
	 * @return The standard decimal format (0.000)
	 */
	public static DecimalFormat dec( ) {
		return dec( 3 );
	}
	
	/**
	 * Creates a decimal number formatter
	 * 
	 * @param d The number of decimals
	 * @return The decimal format 
	 */
	public static DecimalFormat dec( int d ) {
		String f = "0" + (d > 0 ? "." : "");
		for( int i = 0; i < d; i++ ) f += "0";
		
		return new DecimalFormat( f, DecimalFormatSymbols.getInstance( Locale.UK ) );
	}
	
	/**
	 * Creates standard percentage formatter
	 * 
	 * @return The standard percentage formatter (0 %)
	 */
	public static DecimalFormat perc( ) {
		return perc( 0 );
	}
	
	/**
	 * Creates a percentage formatter
	 * 
	 * @param d The number of decimals
	 * @return The percentage formatter
	 */
	public static DecimalFormat perc( int d ) {
		String f = "0" + (d > 0 ? "." : "");
		for( int i = 0; i < d; i++ ) f += "0";
		f += " %";
		
		return new DecimalFormat( f, DecimalFormatSymbols.getInstance( Locale.UK ) );
	}
}
