/****************************************************************/
/* A L L T E L    C O P Y R I G H T    S T A T E M E N T        */
/****************************************************************/
/*                                                              */
/*  NOTICE: THIS SOFTWARE CONTAINS TRADE SECRETS THAT BELONG TO */
/*          ALLTEL INFORMATION SERVICES, INC. AND IS LICENSED   */
/*          BY AN AGREEMENT.  ANY UNAUTHORIZED ACCESS, USE,     */
/*          DUPLICATION OR DISCLOSURE IS UNLAWFUL.              */
/*                                                              */
/*  COPYRIGHT (C) 2003-2004 ALLTEL INFORMATION SERVICES, INC.   */
/*  ALL RIGHTS RESERVED.                                        */
/****************************************************************/
package com.alltel.lsr.common.util;


import org.xml.sax.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
//import javax.xml.rpc.Stub;


/**
* XMLUtility.java - Utility class for manipulating XML requests and XML responses.
* @author  Jason E. Dimm
* @modified Milind A. Joshi 
*/
public class XMLUtility {


   /**
    * createXMLHeader - Creates XML header based on root element and DTD URL.
    * @param rootElement String that represents root element of XML document.
    * @param dtdURL String that represents URL to Document Type Definition.
    * @return String - XML Header.
    */
	public String createXMLHeader(String rootElement, String dtdURL) {
		StringBuffer returnStringBuffer = new StringBuffer();

		returnStringBuffer.append("<?xml version=\"1.0\" standalone=\"no\"?>\n");
	//	returnStringBuffer.append("<!DOCTYPE " + rootElement + " SYSTEM " + "\"" + dtdURL + "\"" + ">\n");

		 returnStringBuffer.append("<!DOCTYPE " + rootElement + " SYSTEM " + "\"" + dtdURL + "\">\n");


 		return returnStringBuffer.toString();
	} //end createXMLHeader


   /**
    * inputStreamToXML - Converts input stream into Document object.
    * @param inputStream InputStream to convert.
    * @return Document - created with inputStream.
    * @exception ParserConfigurationException
    * @exception IOException
    * @exception SAXException
    */
	public Document inputStreamToXML(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
		Log.write("XMLUtility::inputStreamToXML:: invoked");

		// convert the document to string so we can read the document
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int i=inputStream.read();i > -1;i=inputStream.read()) baos.write(i);
		byte xmlbytes[] = baos.toByteArray();
		baos.close();
		Log.write("inputStreamToXML =\n"+new String(xmlbytes));
		ByteArrayInputStream bais = new ByteArrayInputStream(xmlbytes);
		//end conversion

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder        documentBuilder        = documentBuilderFactory.newDocumentBuilder();
	   Document               document               = documentBuilder.parse( bais );

	   return document;
	} //end inputStreamToXML


   /**
    * getNodeValue - Search utility to retrieve specific attribute from Node.
    * Uses recursive calls
    * @param mainNode Node to be searched.
    * @param stringNode String representing attribute to find delimited by "." dot notation.
    * @return String - String representing value found.
    */
	public String getNodeValue(Node mainNode, String stringNode) {
		Log.write("XMLUtility::getNodeValue:: invoked");
		try {
		Log.write("\n\n");
		Log.write("XmlUtility:: [Entry Point] Node name "+stringNode+ " value "+mainNode);
		StringTokenizer stringTokenizer = new StringTokenizer(stringNode, ".");
	        String searchToken     = stringTokenizer.nextToken();

		NodeList nodeList = mainNode.getChildNodes();

		String returnString = null;
		StringBuffer sb = new StringBuffer();

		for (int i=0; i<nodeList.getLength(); i++) {
			Node node = nodeList.item( i );
			String nodeName = node.getNodeName();
			//Log.write("***** " + nodeList.getLength()+" : "+nodeName);

			if( nodeName.equalsIgnoreCase(searchToken) ) {
				//Log.write("Node name "+stringNode+ " found a match");

				if ( stringTokenizer.hasMoreTokens() ) { // u got to go deeper
					//Log.write("Node name "+stringNode+ " got to go deeper");
					if(Node.ELEMENT_NODE == node.getNodeType()) {
						Log.write("Node name "+stringNode+ " going down the rabbit hole");
						returnString=getNodeValue(node, getRemainingTokens(stringTokenizer) );
					} else {
						Log.write("XmlUtility:: The Node type is "+node.getNodeType());
					}
				} //end if
				else {
					if(Node.ELEMENT_NODE == node.getNodeType()) {
						Log.write("Node name "+stringNode+ " reached wonderland");
						if ( null != node.getFirstChild()) {
							returnString=node.getFirstChild().getNodeValue();
						if (searchToken.equals("mdnSubscriberList"))
						{
							Log.write("### I am here ... with " + returnString + "[" + searchToken + "]");
							NodeList nl = node.getChildNodes();
							Node ndChild;
							for (int iNode=0; iNode < nl.getLength(); iNode++) {
								ndChild = nl.item(iNode);
								if (ndChild.getNodeType() == Node.ELEMENT_NODE) {
								Log.write(ndChild.getNodeType() + ":"  + ndChild.getNodeName() + ":" + ndChild.getNodeValue() + "::" + ndChild.getFirstChild().getNodeValue());
							returnString = ndChild.getFirstChild().getNodeValue();
							sb.append(returnString);
							sb.append("|");
							}
							}


							continue;
						}
						else
							break;
						} else {
							returnString=null;
							break;
						}

					}
				} // end else
			} //end if
		} //end for
		Log.write("XmlUtility:: Node name "+stringNode+ "value "+returnString);

		if (searchToken.equals("mdnSubscriberList"))
		{
			returnString = sb.toString();
		}
		return returnString;
		}catch (NullPointerException npe) {
			Log.write("XmlUtility:: Node name "+stringNode+ "blew up NUllPointerException");
		}
		return null;
	} //end getNodeValue


	private String getRemainingTokens(StringTokenizer stringTokenizer) {
		StringBuffer stringBuffer = new StringBuffer();

		while ( stringTokenizer.hasMoreTokens() ) {
//		Log.write("*** getRemainingTokens " + stringBuffer.toString());
			stringBuffer.append( stringTokenizer.nextToken() );
			if (stringTokenizer.hasMoreTokens()) stringBuffer.append(".");
		} //end while

		return stringBuffer.toString();
	} //end getRemainingTokens


   /**
    * validate - Converts String into Document object and validates.
    * @param xmlString String to be converted into Document object.
    * @return Docment
    * @exception SAXException
    * @exception IOException
    * @exception ParserConfigurationException
    */
	public Document validate(String xmlString) throws SAXException, IOException, ParserConfigurationException {
		//Log.write("XMLUtility::validate:: validate invoked");

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		boolean xmlvalidate=false;
		try{
			xmlvalidate=PropertiesManager.getBooleanProperty("XML.Parsing",false);
		}catch (Exception e){}
		documentBuilderFactory.setValidating(xmlvalidate);

		xmlString=massageXMLString(xmlString);
		byte[]               xmlStringBytes       = xmlString.getBytes();

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( xmlStringBytes );

		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

		//TOD add ErrorHandler
		Log.write("XMLUtility::validate:: before documentBuilder.parse");
		Document        document        = documentBuilder.parse(byteArrayInputStream);

        return document;
	} //end validate

	//This patch is to accomodate Vitria not able to handle proper dtd tags
	private String massageXMLString(String xmlString){
		try {
			String dtdname=PropertiesManager.getProperty("WnpPqrGUI.dtd","WnpPqrGUI.dtd");
			String xmllocation=PropertiesManager.getProperty("location.url.dtd", null);
			StringBuffer sbuff = new StringBuffer(xmlString);
			int start=xmlString.indexOf(dtdname);
			int length=dtdname.length();
			sbuff.replace(start,start+length,xmllocation+"/"+dtdname);
//			Log.write("XML Document >>"+sbuff.toString());
			return sbuff.toString();
		} catch(Exception e){
			Log.write("check location.url.dtd in wnp.properties file");
			Log.write(e.toString());
			return xmlString;
		}
	}


	//get the root element name as string.
	public String getRootElementString(Document document)
	{
		return (document.getDocumentElement().getTagName());
	}

	//method to convert Document to String
	public String getStringFromDocument(Document doc)
	{
		String strXML=null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			strXML = doc.toString();
		} catch (Exception e) {
			System.out.println ("IO Exception in XMLText:refresh");
			strXML=null;
		}
		strXML = outputStream.toString();
		return strXML;
	}

} //end class
