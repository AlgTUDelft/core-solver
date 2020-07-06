package core.util.xml;

import java.io.IOException;

/**
 * Exception for XML i/o errors
 *
 * @author Joris Scharpff
 */
@SuppressWarnings ("serial" )
public class XMLException extends IOException {
	/**
	 * Creates a new XMLException from another Exception
	 * 
	 * @param e The exception 
	 */
	public XMLException( Exception e ) {
		super( e );
	}
	
	/**
	 * Creates a new XMLException with the specified message
	 * 
	 * @param msg The error message
	 */
	public XMLException( String msg ) {
		super( msg );
	}
}