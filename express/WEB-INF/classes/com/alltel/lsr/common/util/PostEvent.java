/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2004
 *					BY
 *                           Alltel Communications Inc
 */

/*
 * MODULE:	PostEvent.java
 *
 * DESCRIPTION:
 * 	Post events (like XML) to URL
 * AUTHOR:
 * DATE: 
 *
 * CHANGE HISTORY:
 *      03/22/2004 pjs init
 *
 */

/* $Log:   $
/*
*/
package com.alltel.lsr.common.util;

import org.w3c.dom.*;
import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.alltel.lsr.common.util.*;

public class PostEvent
{
	private String m_strURL;
	private String m_strParamsString;
	private String m_strResponse;

	public void setURL(String strURL) 
	{
		this.m_strURL = strURL;
	}
	public String getURL()
	{	return this.m_strURL;
	}

	public void setParams(String strParams) 
	{
		this.m_strParamsString = strParams;
	}
	public String getParams()
	{	return this.m_strParamsString;
	}

	public void setResponseString(String strValue)
	{	this.m_strResponse = strValue;
	}

	public String getResponseString()
	{	return this.m_strResponse;
	}

	public Document getResponseDocument() throws Exception
	{	XMLUtility xmlUtility = new XMLUtility();
		Document returnDocument = xmlUtility.inputStreamToXML(new java.io.ByteArrayInputStream(this.getResponseString().getBytes()));
		return returnDocument;
	}

        // Push a 'GET' xml event to a URL
        public int sendXMLRequestGET(String strXMLEvent)
        {
		int iRC=0;
		String tmpURL="";

                Log.write(Log.DEBUG_VERBOSE,"PostEvent.sendXMLRequest() URL=["+ m_strURL + "]");
                Log.write(Log.DEBUG_VERBOSE,"PostEvent.sendXMLRequest() Event=["+strXMLEvent+"]");

		setResponseString("");

		String strDocRoot = "";
		String response = "";
		try
		{	int rootIndexStart = 0;
			int rootIndexEnd = 0;
			String strOneLine = null;
			String responseString = "";

			// Construct URL && Connection
			//NOTE: Xml event must be encoded cause it can contain spaces, special chars, etc
			//String strEncodedURL = m_strURL + URLEncoder.encode(strXMLEvent);
			String strEncodedURL = m_strURL + URLEncoder.encode(strXMLEvent, "UTF-8");

			Log.write(Log.DEBUG_VERBOSE,"PostEvent.sendXMLRequest() strEncodedURL=\n["+ strEncodedURL + "]");
			URL url = new URL(strEncodedURL);
			URLConnection con = url.openConnection();

			BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
     		
			int j=0;
			if (!in.ready() && j < 30)	//get other URL a chance to respond....
			{	Log.write(Log.DEBUG_VERBOSE,"PostEvent.sendXMLRequest() waiting...");
				Thread.sleep(1000);
				j++;
			}

	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                	for (int i=in.read();i > -1;i=in.read())
				baos.write(i);
			byte xmlbytes[] = baos.toByteArray();
                	baos.close();
		
// 7-12			String strTemp22 = new String(xmlbytes) ;
	String strTemp22 = baos.toString();

			setResponseString(strTemp22);

                	//Log.write(Log.DEBUG_VERBOSE,"inputStreamToXML =\n"+new String(xmlbytes));
                	Log.write(Log.DEBUG_VERBOSE,"inputStreamToXML =\n"+getResponseString() );
			in.close();

		}
		catch (Exception ee) {
			System.out.println("Event Check Exception: " + ee.toString());
                	Log.write(Log.ERROR,"Event Check Exception: " + ee.toString());
			iRC=-1;
		}
		catch (Throwable e) {
			System.out.println("Event Check throwable: " + e.toString());
                	Log.write(Log.ERROR,"Event Check throwable: " + e.toString());
			iRC=-2;
		}

		return iRC;
	}//end of sendXMLRequest()

        // Push a 'POST' xml event to a URL
        public int sendXMLRequestPOST(String strXMLEvent) throws Exception
        {
		int iRC=0;
		String tmpURL="";

		InputStream inputStream = null;
                Log.write(Log.DEBUG_VERBOSE,"PostEvent.POST() URL=["+ m_strURL + "]");
                Log.write(Log.DEBUG_VERBOSE,"PostEvent.POST() Event=["+strXMLEvent+"]");

		URL url = new URL(m_strURL);
		HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("HTTP-Version","HTTP/1.1");
		httpURLConnection.setDoOutput(true);

		OutputStream outputStream = httpURLConnection.getOutputStream();
		outputStream.write( strXMLEvent.toString().getBytes() );

		Log.write(Log.DEBUG_VERBOSE,"PostEvent.POST() after POST");

		inputStream = httpURLConnection.getInputStream();
		Log.write(Log.DEBUG_VERBOSE,"PostEvent.POST() after POST");

		XMLUtility xmlUtility     = new XMLUtility();
		Document   returnDocument = xmlUtility.inputStreamToXML(inputStream);
// 7-12			String strTemp22 = new String(xmlbytes) ;
//	String strTemp22 = baos.toString();

//          return returnDocument;
		return iRC;
        }


}
