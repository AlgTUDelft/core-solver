/**
 * @file CommandLineArgs.java
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
package core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for command line arguments
 *
 * @author Joris Scharpff
 */
public class CommandLineArgs {
	/** The executable name */
	protected final String exename; 
	
	/** The list of unnamed arguments (e.g. in file) */
	protected final List<Arg> unnamed;
	
	/** The list of named arguments */
	protected final List<Arg> named;
	
	/** The parsed command line */
	protected Map<Arg, ArgVal> parsed;
	
	/** Arguments that print the help */
	protected String[] helpargs;
	
	/** Default strings that print help */
	private static final String[] DEFAULT_HELP = new String[] { "--help", "-h", "/help", "/?" };
	
	/**
	 * Creates a new command line parser
	 * 
	 * @param exename The name of the executable (for help text)
	 */
	public CommandLineArgs( String exename ) {
		this( exename, DEFAULT_HELP );
	}
	
	/**
	 * Creates a new command line parser
	 * 
	 * @param exename The name of the executable (for help text)
	 * @param helpargs The commands that trigger the show help
	 */
	public CommandLineArgs( String exename, String[] helpargs ) {
		this.exename = exename;
		unnamed = new ArrayList<Arg>( );
		named = new ArrayList<Arg>( );
		
		setHelpCommands( helpargs );
	}
	
	/**
	 * Set help command strings
	 * 
	 * @param helpargs The strings that trigger the help display
	 */
	public void setHelpCommands( String[] helpargs ) {
		this.helpargs = helpargs;
	}
	
	/**
	 * Parses the command line, stores a map of all parsed parameters including
	 * omitted parameters with default values. 
	 *
	 * @param args The string array of command line arguments
	 * @return True if parse is successful, false to mark unfinished parse (on
	 * help request)
	 * @throws NoSuchFieldException if any of the command line parameters is not in
	 * the list of known parameters or
	 * @throws  IllegalArgumentException if an argument without default value is
	 * missing
	 */
	public boolean parse( String[] args ) throws NoSuchFieldException, IllegalArgumentException {
		// first check if the help command is in the string
		for( String s : args )
			for( String h : helpargs )
				if( s.equalsIgnoreCase( h ) ) {
					return false;
				}
		
		// parse all parts of the command line argument string 
		final CLE[] parsed = new CLE[ args.length ];
		
		// go through the arguments
		for( int i = 0; i < args.length; i++ ) {
			// check if this is an accepted command line argument
			for( Arg a : named )
				if( a.matches( args[i] ) )
					parsed[ i ] = a;
			
			// if matched, go to next argument
			if( parsed[ i ] != null ) continue;
			
			// otherwise parse it as a variable
			try {
				parsed[ i ] = new ArgVal( args[ i ] );
			} catch( IllegalArgumentException iae ) {
				throw new NoSuchFieldException( "Unknown command line parameter: " + args[ i ] );
			}
		}
		
		// now build the argument/value mapping
		final Map<Arg, ArgVal> result = new HashMap<Arg, ArgVal>( );
		
		// check if there are at least enough arguments supplied for the nameless
		// command line arguments
		if( parsed.length < unnamed.size( ) )
			throw new IllegalArgumentException( "Invalid number of command line arguments specified" );
		
		// first parse nameless arguments
		for( int i = 0; i < unnamed.size( ); i++ ) {
			final CLE cle = parsed[ i ];
			
			if( cle instanceof Arg )
				throw new IllegalArgumentException( "Unexpected parameter '" + ((Arg)cle).longname + "', expecting value for parameter '" + unnamed.get( i ).longname + "'" );
			
			result.put( unnamed.get( i ), (ArgVal)cle );
		}
		
		// skip the unnamed arguments, parse the rest
		for( int i = unnamed.size( ); i < parsed.length; i++ ) {
			final CLE cle = parsed[ i ];
			
			// check if this is an argument, not a value
			if( cle instanceof ArgVal )
				throw new IllegalArgumentException( "Unexpected parameter value '" + ((ArgVal)cle).value + "'" );
			
			// command line argument, parse it
			final Arg arg = ((Arg)cle);
			if( arg.novalue ) {
				result.put( arg, null );				
			} else {
				// get the value
				if( i == parsed.length - 1 || !(parsed[i + 1] instanceof ArgVal) )
					throw new IllegalArgumentException( "Missing value for parameter '" + arg.longname + "'" );
				
				// put in map and skip the next command line element
				result.put( arg, (ArgVal)parsed[i + 1] );
				i++;
			}
		}
		
		// add missing parameters with default values to the map
		for( Arg arg : named ) {
			if( !result.containsKey( arg ) && !arg.novalue ) {
				if( arg.defvalue == null )
					throw new IllegalArgumentException( "Missing parameter '" + arg.longname + "'" );
				
				result.put( arg, arg.defvalue );
			}
		}
		
		// store the result map
		this.parsed = new HashMap<Arg, ArgVal>( result );
		return true;
	}
	
	/**
	 * Adds a nameless argument, used to parse e.g. input or output files in the
	 * same way as named arguments. Nameless arguments are parsed in the order
	 * they are defined as there is no other way to know which value maps to what
	 * argument.
	 * 
	 * @param argname The argument name (for the value mapping)
	 * @param usage The usage string
	 * @throws IllegalArgumentException if the argument name is invalid or the
	 * name already exists in another argument 
	 */
	public void addNameless( String argname, String usage ) throws IllegalArgumentException {
		unnamed.add( check( new Arg( argname, null, false, null, usage ) ) ); 
	}
	
	/**
	 * Adds a no-value argument
	 * 
	 * @param longname The argument long name
	 * @param shortname The argument short name
	 * @param usage The usage text
	 * @throws IllegalArgumentException if the argument names are invalid or any
	 * of the names already exists
	 */
	public void add( String longname, String shortname, String usage ) throws IllegalArgumentException {
		named.add( check( new Arg( longname, shortname, true, null, usage ) ) );
	}	
	
	/**
	 * Adds an accepted argument
	 * 
	 * @param longname The argument long name
	 * @param shortname The argument short name
	 * @param defvalue The default value if not specified
	 * @param usage The usage text
	 * @throws IllegalArgumentException if the argument names are invalid or any
	 * of the names already exists
	 */
	public void add( String longname, String shortname, String defvalue, String usage ) throws IllegalArgumentException {
		named.add( check( new Arg( longname, shortname, false, defvalue, usage ) ) );
	}
	
	/**
	 * Checks if the argument if does not exist in either argument lists, or if
	 * it is a help command
	 * 
	 * @param arg The argument to add
	 * @return The argument
	 * @throws IllegalArgumentException if any of the argument's names is already
	 * in the accepted names list
	 */
	private Arg check( Arg arg ) throws IllegalArgumentException {
		// check if it is a help command
		for( String s : helpargs ) {
			if( s.equalsIgnoreCase( arg.longname ) )
				throw new IllegalArgumentException( "Parameter name '" + arg.longname + "' is already set as help parameter" );
			if( s.equalsIgnoreCase( arg.shortname ) )
				throw new IllegalArgumentException( "Parameter name '" + arg.shortname + "' is already set as help parameter" );
		}
		
		// check if the argument already exists in unnamed list
		for( Arg a : unnamed ) {
			if( a.matches( arg.longname ) )
					throw new IllegalArgumentException( "Duplicate parameter name '" + arg.longname + "'" );
			
			if( a.matches( arg.shortname ) )
				throw new IllegalArgumentException( "Duplicate parameter name '" + arg.shortname + "'" ); 			
		}

		// check if the argument already exists in named list
		for( Arg a : named ) {
			if( a.matches( arg.longname ) )
					throw new IllegalArgumentException( "Duplicate parameter name '" + arg.longname + "'" );
			
			if( a.matches( arg.shortname ) )
				throw new IllegalArgumentException( "Duplicate parameter name '" + arg.shortname + "'" ); 			
		}
		
		return arg;
	}
	
	/**
	 * Checks if an argument is present in the command line
	 * 
	 * @param argname The argument name
	 * @return True if the argument is present
	 * @throws RuntimeException if the command line has not been parsed
	 */
	public boolean contains( String argname ) throws RuntimeException {
		try {
			getArgVal( argname );
		} catch( NoSuchFieldException nsfe ) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Retrieves the string value of the parameter with the given long or short
	 * name
	 * 
	 * @param argname The argument name
	 * @return The value of the argument
	 * @throws RuntimeException if the command line has not been parsed
	 * @throws NoSuchFieldException if there is no argument with the name
	 */
	private ArgVal getArgVal( String argname ) throws RuntimeException, NoSuchFieldException {
		if( parsed == null )
			throw new RuntimeException( "Command line has not been parsed!" );

		// check if the argument exists
		for( Arg a : parsed.keySet( ) )
			if( a.matches( argname ) )
				return parsed.get( a );
		
		throw new NoSuchFieldException( "No parameter '" + argname + "' on the command line!" );
	}
	
	/**
	 * Retrieves the string value of an argument
	 * 
	 * @param argname The argument name
	 * @return The string value of the argument
	 * @throws RuntimeException if the command line has not been parsed
	 * @throws NoSuchFieldException if there is no such argument
	 */
	public String get( String argname ) throws RuntimeException, NoSuchFieldException {
		return getArgVal( argname ).value;
	}
	
	/**
	 * Retrieves the boolean value of an argument
	 * 
	 * @param argname The argument name
	 * @return The boolean value of the argument
	 * @throws RuntimeException if the command line has not been parsed
	 * @throws NoSuchFieldException if there is no such argument
	 * @throws NumberFormatException if the argument value failed to parse
	 */
	public boolean getBoolean( String argname ) throws RuntimeException, NoSuchFieldException, NumberFormatException {
		return Boolean.parseBoolean( get( argname ) );
	}

	
	/**
	 * Retrieves the integer value of an argument
	 * 
	 * @param argname The argument name
	 * @return The integer value of the argument
	 * @throws RuntimeException if the command line has not been parsed
	 * @throws NoSuchFieldException if there is no such argument
	 * @throws NumberFormatException if the argument value failed to parse
	 */
	public int getInt( String argname ) throws RuntimeException, NoSuchFieldException, NumberFormatException {
		return Integer.parseInt( get( argname ) );
	}
	
	/**
	 * Retrieves the long value of an argument
	 * 
	 * @param argname The argument name
	 * @return The long value of the argument
	 * @throws RuntimeException if the command line has not been parsed
	 * @throws NoSuchFieldException if there is no such argument
	 * @throws NumberFormatException if the argument value failed to parse
	 */
	public long getLong( String argname ) throws RuntimeException, NoSuchFieldException, NumberFormatException {
		return Long.parseLong( get( argname ) );
	}

	/**
	 * Retrieves the double value of an argument
	 * 
	 * @param argname The argument name
	 * @return The double value of the argument
	 * @throws RuntimeException if the command line has not been parsed
	 * @throws NoSuchFieldException if there is no such argument
	 * @throws NumberFormatException if the argument value failed to parse
	 */
	public double getDouble( String argname ) throws RuntimeException, NoSuchFieldException, NumberFormatException {
		return Double.parseDouble( get( argname ) );
	}
	
	/**
	 * Returns the command line usage help as string
	 * 
	 * @return The string
	 */
	public String getUsage( ) {
		String str = exename + " command line usage:\n\n";
		
		// determine longest argument names
		int maxun = 0; int maxlong = 0; int maxshort = 0;
		for( Arg a : unnamed )
			if( a.longname.length( ) + 2 > maxun ) maxun = a.longname.length( ) + 2;
		for( Arg a : named ) {
			if( a.longname.length( ) > maxlong ) maxlong = a.longname.length( );
			if( a.shortname.length( ) > maxshort ) maxshort = a.shortname.length( );
		}
		
		// print unnamed variables
		str += exename;
		for( Arg a : unnamed )
			str += " <" + a.longname + ">";
		
		if( named.size( ) > 0 ) str += " [OPTIONS]";

		str += "\n\nwhere:\n";
		for( Arg a : unnamed )
			str += "  " + space( "<" + a.longname + ">", maxun ) + " : " + a.usage + "\n";
		
		// add optional part
		if( unnamed.size( ) > 0 )
			str += "\nand OPTIONS can be:\n"; 
				
		// sort arguments alphabetically
		Collections.sort( named );
		for( Arg arg : named ) {
			str += "  " + space( arg.longname, maxlong ) + (maxshort > 0 ? "  " : "" ) + (arg.shortname != null ? space( arg.shortname, maxshort ) : "" ) + " : " + arg.usage;
			if( arg.defvalue != null )
				str += " (default: " + arg.defvalue.value + ")";
			str += "\n";
		}
		
		return str;
	}
	
	/**
	 * Fills the string with spaces on the right hand side such that it matches
	 * the given length
	 * 
	 * @param string The string
	 * @param length The required string length
	 * @return The string with spaced appended to the right such that
	 * string.length( ) >= length
	 */
	private String space( String string, int length ) {
		String str = string;
		while( str.length( ) < length ) {
			str += " ";
		}
		return str;
	}
	
	/**
	 * Interface for command line elements
	 */
	private interface CLE {
		// interface
	}
	
	/**
	 * Container for acceptable command line arguments
	 */
	private class Arg implements CLE, Comparable<Arg> {
		/** The long argument name */
		protected final String longname;
		
		/** The abbreviated argument name */
		protected final String shortname;
		
		/** Usage text */
		protected final String usage;
		
		/** Value-less argument */
		protected final boolean novalue;
		
		/** The default value, null if no default */
		protected final ArgVal defvalue;
		
		/**
		 * Creates a new command line argument
		 * 
		 * @param longname The long argument name
		 * @param shortname The short argument name
		 * @param noval No-value argument
		 * @param defval The default value, null for required parameter
		 * @param usage The usage text
		 * @throws IllegalArgumentException if long name is empty or the default value is invalid
		 */
		protected Arg( String longname, String shortname, boolean noval, String defval, String usage ) throws IllegalArgumentException {
			final String l = (longname != null && !longname.equals( "" ) ? longname : null ); 
			final String s = (shortname != null && !shortname.equals( "" ) && !shortname.equalsIgnoreCase( l ) ? shortname : null ); 

			// check if long name is specified
			if( l == null )
				throw new IllegalArgumentException( "Long parameter name must be specified" );
			
			this.longname = l;
			this.shortname = s;
			
			this.novalue = noval;
			this.defvalue = (defval != null ? new ArgVal( defval ) : null);
			this.usage = usage;
		}

		/**
		 * Returns true if either the long or the short name matches the string
		 * 
		 * @param name The name string to match
		 * @return True iff long or short name equals the string, ignoring case
		 */
		protected boolean matches( String name ) {
			if( name == null ) return false;
			
			return (name.equalsIgnoreCase( longname ) || name.equalsIgnoreCase( shortname ) );
		}
		
		/**
		 * Return the case ignorant syntactic difference between long names
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo( Arg o ) {
			return longname.compareToIgnoreCase( o.longname );
		}
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals( Object obj ) {
			if( obj == null || !(obj instanceof Arg) ) return false;
			final Arg a = (Arg)obj;
			
			return longname.equalsIgnoreCase( a.longname );
		}
		
		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode( ) {
			return longname.hashCode( );
		}
	}
	
	/**
	 * Command line argument value
	 */
	private class ArgVal implements CLE {
		/** String the value */
		protected final String value;
		
		/**
		 * Creates the argument value
		 * 
		 * @param value The value
		 * @throws NullPointerException if the value is null
		 * @throws IllegalArgumentException if the value starts with '-'
		 */
		protected ArgVal( String value ) throws IllegalArgumentException {
			if( value == null )
				throw new NullPointerException( );
			if( value.startsWith( "-" ) )
				throw new IllegalArgumentException( "Invalid parameter value '" + value + "'" );
			
			this.value = value;
		}
	}
}
