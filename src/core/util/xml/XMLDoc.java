package core.util.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Container for org.w3c.dom.Document class so to provide a 'native' XML interface
 * 
 * @author Joris Scharpff
 */
public class XMLDoc {
	/** The org.w3c.dom.Document */
	protected Document doc;
	
	/**
	 * Creates empty XML document
	 */
	public XMLDoc( ) {
		// create empty document
		doc = new DocumentImpl( );
	}
	
	/**
	 * Creates the XML document from the specified file name
	 * 
	 * @param filename The XML file name
	 * @return The document
	 * @throws XMLException
	 */
	public XMLDoc( String filename ) throws XMLException {
		// create XMLReader for the file
		DocumentBuilderFactory dbf;
	  DocumentBuilder db;
	  try {
			dbf = DocumentBuilderFactory.newInstance();
		  db = dbf.newDocumentBuilder();
		  doc = db.parse( filename );
		  doc.getDocumentElement().normalize();
	  } catch( FileNotFoundException fnf ) {
	  	throw new XMLException( "XML file '" + filename + "' not found!" );
	  } catch( Exception e ) {
	  	throw new XMLException( "Exception while reading from the XML file '" + filename + "': " + e.getMessage( ) );	  	
	  }		
	}

	/**
	 * Writes the given XML key to a file
	 * 
	 * @param filename The XML filename
	 * @throws XMLException
	 */
	public void toFile( String filename ) throws XMLException {
		// create XML formatter
  	OutputFormat format = new OutputFormat( doc );
  	format.setLineWidth(65);             
  	format.setIndenting(true);             
  	format.setIndent(2);
  	
  	// create serializer & serialize the model
  	FileOutputStream fileout = null;
  	try {
  		fileout = new FileOutputStream( new File( filename ) );
			XMLSerializer serializer = new XMLSerializer( fileout, format );
			serializer.asDOMSerializer( );
			serializer.serialize( doc );
  	} catch( Exception e ) {
  		throw new XMLException( e );
  	} finally {
  		if( fileout != null )
				try {
					fileout.close( );
				} catch( IOException e ) {
					// ignore
				}
  	}
	}
}
