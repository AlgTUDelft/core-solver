/**
 * @file Rand.java
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
 * @date         3 jul. 2013
 * @project      NGI
 * @company      Almende B.V.
 */
package core.util;

import java.util.Random;

/**
 * Random generator wrapper with some useful common functions
 *
 * @author Joris Scharpff
 */
public class RandGenerator {
	/** The seed */
	public final long seed;
	
	/** The random generator used */
	private Random rand;
	
	/**
	 * Creates a new random generator
	 * 
	 * @param seed The random generator seed
	 */
	public RandGenerator( long seed ) {
		this.seed = seed;
		rand = new Random( seed );
	}
	
	/**
	 * Retrieves a random double
	 * 
	 * @return Random double from [0, 1)
	 */
	public double randDbl( ) {
		return randDbl( 0.0, 1.0 );
	}
	
	/**
	 * Retrieves a random double from a specified range given by a length 2 array
	 * 
	 * @param range The length 2 array specifying the range
	 * @return A random double x such that range[0] <= x < range[1]
	 */
	public double randDbl( double[] range ) {
		if( range.length != 2 ) throw new IllegalArgumentException( "Invalid array length, must be 2!" );
		return randDbl( range[0], range[1] );
	}
	
	/**
	 * Generates an array of random double values
	 * 
	 * @param length The array length
	 * @return An array of random doubles [0, 1) of the given length
	 */
	public double[] randDblArray( int length ) {
		return randDblArray( length, 0, 1 );
	}
	
	/**
	 * Generates an array of random double values
	 * 
	 * @param length The array length
	 * @param min The min value
	 * @param max The max vlaue
	 * @return An array of random doubles [min, max) of the given length
	 */
	public double[] randDblArray( int length, double min, double max ) {
		final double[] res = new double[ length ];
		
		for( int i = 0; i < length; i++ )
			res[ i ] = randDbl( min, max );
		
		return res;
	}
	
	/**
	 * Retrieves a random double from the specified range
	 * 
	 * @param min The minimal value
	 * @param max The maximal value
	 * @return A random double x such that min <= x < max 
	 */
	public double randDbl( double min, double max ) {
		return rand.nextDouble( ) * (max - min) + min;
	}
	
	/**
	 * Retrieves a random integer from a range specified by a size 2 array
	 * 
	 * @param range The range given by an array of length 2
	 * @return A random integer x such that range[0] <= x <= range[1]
	 */
	public int randInt( int[] range ) {
		if( range.length != 2 ) throw new IllegalArgumentException( "Invalid array length, must be 2!" );
		return randInt( range[0], range[1] );
	}
	
	/**
	 * Retrieves a random integer from a range 0 to the specified maximum
	 * (inclusive)
	 * 
	 * @param max The maximum value
	 * @return A random integer x such that 0 <= x <= max 
	 */
	public int randInt( int max ) {
		return randInt( 0, max ); 
	}	
	
	
	/**
	 * Retrieves a random integer from a range (inclusive)
	 * 
	 * @param min The minimum value
	 * @param max The maximum value
	 * @return A random integer x such that min <= x <= max 
	 */
	public int randInt( int min, int max ) {
		if( max - min + 1 == 0 ) return min;
		return rand.nextInt( max - min + 1 ) + min; 
	}	
	
	/**
	 * Generates a random integer array of the specified length such that the
	 * values sum up to the specified total.
	 * 
	 * @param total The total value that the array should have
	 * @param length The array length
	 * @param zeroes Allow zero values?
	 * @return The integer array such that its entries sum up to total
	 * @throws IllegalArgumentException if the generator fails to produce the
	 * array
	 */
	public int[] randIntArray( int total, int length, boolean zeroes ) throws IllegalArgumentException {
		// generate new array
		final int[] res = new int[ length ];
		
		// try it for a limited number of times, otherwise report fail
		int tries = 100000;
		
		// randomly generate fractions for each entry
		boolean okay = false;
		do {
			if( tries == 0 ) throw new IllegalArgumentException( "Failed to produce integer array (total: " + total + ", length: " + length + ", zeroes: " + zeroes + ")" );
			tries--;
			
			final double[] frac = randDblArray( length );
			double sum = 0;
			for( int i = 0; i < length; i++ ) sum += frac[ i ];
			
			// convert fraction to integer value
			for( int i = 0; i < length; i++ )
				res[ i ] = (int) ((frac[ i ] / sum) * total);
			
			// make sure the sum matches the specified total (mismatches may occur
			// due to rounding)
			int innertries = 1000;
			int isum = 0;
			for( int i = 0; i < length; i++ ) isum += res[ i ];
			while( total - isum != 0 ) {
				if( innertries == 0 ) break;
				
				// randomly pick an entry to lower or higher it to match the sum
				final int index = randInt( 0, length - 1 );
				if( total - isum > 0 ) {
					res[ index ]++;
					isum++;
				} else {
					if( (res[ index ] > 0 && zeroes) || (res[ index ] > 1) ) {
						res[ index ]--;
						isum--;
					}
				}
			}
			
			// check if the array is okay
			okay = (total == isum);
			if( !zeroes )
				for( int i = 0; i < length; i++ )
					okay &= (res[i] > 0);
		} while( !okay );
			
		return res;
	}
	
	/**
	 * @return The seed of the generator
	 */
	public long getSeed( ) {
		return seed;
	}
	
	/**
	 * @return The internal generator
	 */
	public Random getRandomGenerator( ) {
		return rand;
	}
}
