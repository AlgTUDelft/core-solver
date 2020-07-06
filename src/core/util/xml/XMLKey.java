package core.util.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Class that provides basic XML input functions for objects in the XML model
 * file.
 *
 * @author Joris Scharpff
 */
public class XMLKey {
	/** The element represented by this XMLObject */
	protected final Element element; 
	
	/** The type field constant */
	private final static String TYPE_FIELD = "Type";

	/**
	 * Creates a new XML key
	 * 
	 * @param element The element to create it for
	 */
	private XMLKey( Element element ) {
		this.element = element;
	}
	
	/**
	 * Creates a new XML key as a child of the specified key
	 * 
	 * @param key The parent XML key  
	 * @param name The element name
	 */
	public XMLKey( XMLKey key, String name ) {
		element = key.element.getOwnerDocument( ).createElement( name );
		key.element.appendChild( element );
	}

	/**
	 * Creates XML Key based on document and key name
	 * 
	 * @param doc The XML document
	 * @param keyname The key name
	 * @param create If true the key is created, otherwise it is searched in the file
	 * @throws XMLException if the key is not found
	 */
	public XMLKey( XMLDoc doc, String keyname, boolean create ) throws XMLException {
		if( create ) {
			element = doc.doc.createElement( keyname );
			doc.doc.appendChild( element );
		} else {
		  NodeList nl = doc.doc.getElementsByTagName( keyname );
		  if( nl.getLength( ) == 0 ) throw new XMLException( "Key '" + keyname + "' not found!" );
		  if( nl.getLength( ) > 1 ) throw new XMLException( "Found multiple occurences of the key '" + keyname + "'!" );
		  element = (Element) nl.item( 0 );
		}
	}
		
	/**
	 * @return The key name
	 */
	public String getName( ) {
		return element.getTagName( );
	}

	/**
	 * Gets the (single) child-key with specified name
	 * 
	 * @param key The key name
	 * @param mustexist If set an exception is thrown when the key does not exist
	 * @return The element corresponding to the key or null if mustexist is false and not found
	 * @throws XMLException if the key is not found or occurs multiple times
	 */
	public XMLKey getKey( String key, boolean mustexist ) throws XMLException {
		// go through child nodes only
		List<XMLKey> keys = getKeyList( key );
		
	  if( keys.size( ) == 0 ) 
	  	if( mustexist )
	  		throw new XMLException( "Key '" + key + "' not found!" );
	  	else
	  		return null;
	  
	  if( keys.size( ) > 1 ) throw new XMLException( "Found multiple occurences of the key '" + key + "'!" );
	  return keys.get( 0 );		
	}
	
	/**
	 * Returns the key with the specified name, requires that the key exists
	 * otherwise a XMLException is thrown.
	 * 
	 * @param key The key name
	 * @return The XMLKey that matches the name
	 * @throws XMLException
	 */
	public XMLKey getKey( String key ) throws XMLException {
		return getKey( key, true );
	} 

	/**
	 * Gets the list of elements with the specified key name
	 * 
	 * @param key The key name
	 * @return The list of elements with the given key name
	 */
	public List<XMLKey> getKeyList( String key ) {
		// return list
		List<XMLKey> result = new ArrayList<XMLKey>( );
		
		// go through child nodes only
		NodeList nl = element.getChildNodes( );
		
		for( int i = 0; i < nl.getLength( ); i++ ) {
			if( (nl.item( i ) instanceof Element) && ((Element)nl.item( i )).getTagName( ).equals( key ) )
				result.add( new XMLKey( (Element ) nl.item( i ) ) );
		}
	  
	  return result;		
	}		
	
	/**
	 * Sets the value of the field with given name
	 * 
	 * @param field The field name
	 * @param value The field value
	 */
	public void addField( String field, String value ) {
		element.setAttribute( field, value );
	}

	/**
	 * Returns a string with the value of the field in the specified key or the
	 * default value if it does not exist
	 * 
	 * @param field The field name
	 * @param defvalue The default value if a key does not exist
	 * @return A string containing the value or null if the field is not specified
	 */
	public String getField( String field, String defvalue ) {
		try {
			return getField( field );
		} catch( XMLException e ) {
			return defvalue;
		}
	}
	/**
	 * Returns a string with the value of the field in the specified key
	 * 
	 * @param field The field name
	 * @return A string containing the value or null if the field is not specified
	 * @throws XMLException if the key is missing
	 */
	public String getField( String field ) throws XMLException {
  	// return the attribute value
  	if( element.hasAttribute( field ) )
  		return element.getAttribute( field );
  	else
			throw new XMLException( "Missing field '" + field + "' in key '" + element.getTagName( ) + "'!" );
	}
	
	/**
	 * Checks if the XMLKey has a field with the specified name
	 * 
	 * @param field The field name
	 * @return True if the XMLKey has such a field
	 */
	public boolean hasField( String field ) {
		return element.hasAttribute( field );
	}
	
	/**
	 * @return The text between the tags
	 */
	public String getText( ) {
		return element.getTextContent( );
	}
	
	/**
	 * Instantiates the class as specified in the XML field, uses the type field
	 * to determine the class name and the name of the specified base class as the
	 * class suffix  
	 * 
	 * @param baseclass The base class
	 * @return The instantiated class that is of the baseclass type
	 * @throws XMLException if the class doesn't exist or an error occurred in the
	 * creation of the class
	 */
	public <T> T toClass( Class<T> baseclass ) throws XMLException {		
		// get the base class name as suffix
		final String suffix = baseclass.getName( ).substring( baseclass.getName( ).lastIndexOf( '.' ) + 1 );
		
		// instantiate and return the class
		return toClass( baseclass, suffix );
	}
		
	/**
	 * Instantiates the class as specified in the XML field, uses the type field
	 * to determine the class name and the specified suffix  
	 * 
	 * @param baseclass The base class
	 * @param suffix The suffix that is added to the class name
	 * @return The instantiated class that is of the base class type
	 * @throws XMLException if the class doesn't exist or an error occurred in the
	 * creation of the class
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T toClass( Class<T> baseclass, String suffix ) throws XMLException {
		// get the type field from the xml key
		final String type = getType( );

		// instantiate and return the class
		return (T)toClass( baseclass.getPackage( ).getName( ) + "." + type + suffix );
	}	
	
	/**
	 * Creates a class from a key however uses a specified full class name
	 * 
	 * @param fullclassname The full class name including package
	 * @throws XMLException if the class doesn't exist or an error occurred in the
	 * creation of the class
	 */
	private Object toClass( String fullclassname ) throws XMLException {
		try {
			// get the correct class name
			Class<?> o = Class.forName( fullclassname );
			
			// is it serialisable?
			if( !XMLSerializable.class.isAssignableFrom( o ) ) throw new XMLException( "Class '" + fullclassname + "' does not implement the XML serialisable interface!" );

			// create the object and call fromXML method
			XMLSerializable x = (XMLSerializable) o.newInstance( );
			x.fromXML( this );
			return x;
		} catch( NoSuchMethodError nsme ) {
			throw new XMLException( "Class '" + fullclassname + "' is not XML serialisable, implement fromXML method!" );
		} catch( ClassNotFoundException cnfe ) {
			throw new XMLException( "Missing implementation of class '" + fullclassname + "'!" );
		} catch( IllegalArgumentException e ) {
			throw new XMLException( "Illegal arguments specified to load class '" + fullclassname + "'!" );
		} catch( IllegalAccessException e ) {
			throw new XMLException( "Invalid access to class '" + fullclassname + "'!" );
		} catch( SecurityException e ) {
			throw new XMLException( "Invalid access sto class '" + fullclassname + "'!" );
		} catch( InstantiationException e ) {
			throw new XMLException( "Error while loading class '" + fullclassname + "':\n" + e.getCause( ).getMessage( ) );			
		}
	}

	/**
	 * @return The type of the class from the type field in the XML key
	 * @throws XMLException if key does not exist
	 */
	private String getType( ) throws XMLException {
		return getField( TYPE_FIELD );
	}
	
	/**
	 * Sets the value of the type field using the class name and removing the base
	 * class suffix
	 * 
	 * @param o The class object
	 */
	public void addTypeField( Object o ) {
		// get the base class to remove the suffix (without the "class" in front)
		String suffix = o.getClass( ).getSuperclass( ).toString( ).substring( 6 );
		suffix = suffix.substring( suffix.lastIndexOf( '.' ) + 1 );
		
		// add the type field
		addTypeField( o, suffix );
	}
	
	/**
	 * Sets the value of the type field using the class name and removing the
	 * specified suffix instead of the base class suffix
	 * 
	 * @param o The object to resolve class name for
	 * @param suffix The class suffix to add
	 */
	public void addTypeField( Object o, String suffix ) {
		// strip suffix and package path
		String name = o.getClass( ).getName( ).substring( 0, o.getClass( ).getName( ).indexOf( suffix ) );
		name = name.substring( name.lastIndexOf( '.' ) + 1 );

		// set the value of the type field
		addField( TYPE_FIELD, name );	
	}
}
