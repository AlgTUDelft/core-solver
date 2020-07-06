package core.util.xml;

import java.util.Stack;


/**
 * Produces XML with nice indentation. Very simple and fast API to write XML. The methods
 * are designed to allow chaining of calls.
 * @author Markus
 */
public class XMLWriter {

	private int indentation = 2;

	private int currentIndent = 0;
	private StringBuilder sb = new StringBuilder();
	private Stack<String> elementNameStack = new Stack<String>();
	private boolean appendInSameLine = false;

	/**
	 * Creates a new XMLWriter
	 */
	public XMLWriter() {
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	}

	/**
	 * Begins a new element
	 * 
	 * @param elementName The name of the new element
	 */
	public void beginElement(String elementName) {
		beginElement(elementName, null);
	}

	/**
	 * Begins a new XML element on a new line.
	 * 
	 * @param elementName name of the element
	 * @param attributes contains all the attributes for this element
	 */
	public void beginElement(String elementName, String attributes) {
		elementNameStack.push(elementName);

		indent();

		sb.append('<');
		sb.append(elementName);
		if (attributes != null) {
			sb.append(' ');
			sb.append(attributes);
		}
		sb.append('>');

		currentIndent += indentation;
	}

	/**
	 * Insert an dest tag for the current XML element opened with
	 * {@link #beginElement(String, String)}. Insert it in a new line, expect when only
	 * text has been added with {@link #print(String)}.
	 */
	public void endElement() {
		currentIndent -= indentation;
		String elementName = elementNameStack.pop();

		if (!appendInSameLine) {
			indent();
		} else {
			appendInSameLine = false;
		}

		sb.append('<');
		sb.append('/');
		sb.append(elementName);
		sb.append('>');
	}

	/**
	 * Appends text at the current position (in the same line as the last
	 * {@link #beginElement(String)} call).
	 * 
	 * @param text The text to append
	 */
	public void print( String text ) {
		sb.append(text);
		appendInSameLine = true;
	}

	/**
	 * Prints a complete line of text. Inserts it in a new line.
	 * 
	 * @param text The text to print
	 */
	public void printLine( String text ) {
		indent();
		sb.append(text);
	}

	private void indent() {
		sb.append('\n');
		for (int i = 0; i < currentIndent; i++) {
			sb.append(' ');
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (!elementNameStack.isEmpty()) {
			throw new IllegalStateException("XML not finished. Element '" + elementNameStack.peek() + "' is still open.");
		}
		return sb.toString();
	}

	/**
	 * Sets the indentation to be used.
	 * 
	 * @param indentation The number of characters to indent
	 */
	public void setIndentation( int indentation ) {
		this.indentation = indentation;
	}

}