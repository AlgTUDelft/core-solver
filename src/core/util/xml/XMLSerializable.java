package core.util.xml;


/**
 * Interface for XML serialisable objects
 * 
 * @author Joris Scharpff
 */
public interface XMLSerializable {
	/**
	 * Each object that implements this interface should add it's own field and
	 * sub keys to the supplied key. This way we can pass the key through the
	 * entire class hierarchy of more complicated objects.
	 * 
	 * @param key The XML key for the object
	 */
	public void toXML( XMLKey key );
	
	/**
	 * Reads the object data from the specified XMLKey, should also call its base
	 * classes in order to read their properties from XML
	 * 
	 * @param key The XML key containing the object data
	 * @throws XMLException if an error occurred during XML reading
	 */
	public void fromXML( XMLKey key ) throws XMLException;
}
