package org.fastcatsearch.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * FIXME:this class is not completly
 * @author lupfeliz
 *
 */
public class XMLResultStringer implements ResultStringer {
	enum NODE_TYPE { OBJECT, ARRAY };
	Document document;
	Element root;
	Element currentElement;
	String arrayName;
	NODE_TYPE cType;
	boolean beautify;
	
	public XMLResultStringer(String rootName, boolean beautify) {
		document = DocumentHelper.createDocument();
		root = document.addElement(rootName);
		currentElement = root;
		this.beautify = beautify;
	}
	
	@Override
	public ResultStringer object() throws StringifyException {
		cType = NODE_TYPE.OBJECT;
		return this;
	}

	@Override
	public ResultStringer endObject() throws StringifyException {
		return this;
	}

	@Override
	public ResultStringer array(String arrayName) throws StringifyException {
		cType = NODE_TYPE.ARRAY;
		this.arrayName = arrayName;
		return this;
	}

	@Override
	public ResultStringer endArray() throws StringifyException {
		cType = NODE_TYPE.OBJECT;
		return this;
	}

	@Override
	public ResultStringer key(String key) throws StringifyException {
		currentElement = currentElement.addElement(key);
		return this;
	}

	@Override
	public ResultStringer value(Object obj) throws StringifyException {
		if(cType==NODE_TYPE.ARRAY) {
			currentElement = currentElement.addElement(arrayName);
		}
		currentElement = currentElement.addText(obj.toString());
		currentElement = currentElement.getParent();
		return this;
	}
	
	@Override
	public String toString() {
		if(beautify) {
			StringWriter sw = new StringWriter();
			try {
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform
			    (new StreamSource(new StringReader(document.asXML())), 
			     new StreamResult(sw));
				return sw.toString();
			} catch (TransformerConfigurationException e) {
			} catch (TransformerException e) {
			} finally {
			}
		} else {
			return document.asXML();
		}
		return "";
	}
}