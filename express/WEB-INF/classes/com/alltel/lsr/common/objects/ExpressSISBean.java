/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2005
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:		CustomerSearBean.java
 * 
 * DESCRIPTION: super class for all SIS communications.  
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-2005
 * 
 * HISTORY:
 *	6/8/2005 Edris Kalibala created
 *
*/
package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import com.alltel.lsr.common.util.*;

public abstract class ExpressSISBean implements ExpressSISConstants
{
	final private String 	CNAME = "ExpressSISBean.";
	
	public ExpressSISBean( String xml ){		
	
	}
	
	public  String sendXml( String xmlDocument, String strUlr )
	{
		String FNAME = "sendXml()";
		DataOutputStream dos = null; 
		StringBuffer  response = new StringBuffer( ONE_K ) ;
		BufferedReader buffReader = null;
		HttpURLConnection  uConnection = null;
		URL uRlPath = null;
		try{ 
			uRlPath = new URL( strUlr );
			uConnection =  ( HttpURLConnection )uRlPath.openConnection();
			uConnection.setDoOutput( true );
			uConnection.setDoInput( true );     	
			uConnection.setUseCaches( false ); 
			dos = new DataOutputStream( uConnection.getOutputStream());
			dos.writeBytes( xmlDocument );
			dos.flush(); 
			dos.close(); 	      	
			buffReader = new BufferedReader(	new InputStreamReader(
					uConnection.getInputStream()));
			
			String inputLine = "";
			while ((inputLine = buffReader.readLine()) != null)
			{
			    response.append( inputLine );
			}
			buffReader.close();	
		}catch(Exception ex) 
		{
      		response.append("<p><span class=errormsg> Couldn't connect to SIS system, please try again later. <br>");
      		response.append("If this problem persists, please contact Express support.\n<br> ");
      		response.append("<br>" + ex.toString() + "</span></p>" );
      		ex.printStackTrace();
      		Log.write(Log.ERROR, CNAME + FNAME + ex.toString() );
      	}
			
		return  response.toString();		
	}
	
		// extracts the xml file from url 
	public  String recieveUrl( String strUlr ){
		String FNAME = "recieveUrl()\n";		
		StringBuffer  response = new StringBuffer( ONE_K ) ;
		BufferedReader buffReader = null;
		HttpURLConnection  uConnection = null;
		URL uRlPath = null;
		String xmlResults = "";
		try{ 
			uRlPath = new URL( strUlr);
			uConnection =  (HttpURLConnection)uRlPath.openConnection();
			uConnection.setDoOutput(false);
			uConnection.setDoInput(true);  				     	
			buffReader = new BufferedReader(	new InputStreamReader(
					uConnection.getInputStream()));			
			String inputLine = "";
			while ((inputLine = buffReader.readLine()) != null)
			{
			    response.append( inputLine );
			}
			buffReader.close();	
		}catch(Exception ex) 
		{
      		response.append("<p><span class=errormsg> Couldn't connect to SIS system, please try again later. <br>");
      		response.append("If this problem persists, please contact Express support.\n<br> ");
      		response.append("<br>" + ex.toString() + "</span></p>" );
      		ex.printStackTrace();
      		Log.write(Log.ERROR, CNAME + FNAME + ex.toString() );
      		
      		
      	}finally{
      		xmlResults = response.toString();
      	}    	
		return  xmlResults;		
	}
	
	// extracts the xml file figures out what type or kind and 
	public  abstract Vector extractXML( String xmlDocument )	;	

	
}