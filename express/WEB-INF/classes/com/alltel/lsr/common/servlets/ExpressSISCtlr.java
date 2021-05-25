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
 * MODULE:		ExpressSISCtlr.java
 * 
 * DESCRIPTION: This servlets controls Express-sis communications by instantiating the appropriate beans and forwarding 
 * 				appropriate jsp files ( screens).
 * 
 * AUTHOR:      Edris Kalibala
 * 
 * DATE:        06-20-2005
 * 
 *	
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class ExpressSISCtlr extends AlltelServlet implements ExpressSISConstants
{
	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{	

		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
		String strUSERID = sdm.getUser();		
	
		// action number is a hidden param on customer search form
		String strActionName = request.getParameter("ExpressSISAction");
		int iActionNumber = Integer.parseInt( strActionName );
		
		//@see ExpressSISConstants.java for values of cases in the switch	
		switch ( iActionNumber )
		{
			case CUSTOMER_LOOKUP_FORM:{
				alltelRequestDispatcher.forward("/CustomerLookup.jsp");  
				break;
			}
			case CUSTOMER_LOOKUP_FORM_SUBMIT:
			{
				// extract parameters from search form and set customersearch values.
				CustomerSearchBean customersearchbean  = new CustomerSearchBean( "" );
				customersearchbean.setPhone( request.getParameter( "phone" ) );
				customersearchbean.setBusinessName( request.getParameter( "bname" ) );
				customersearchbean.setLastName( request.getParameter( "lname" ) );
				customersearchbean.setFirstName( request.getParameter( "fname" ) );				
				customersearchbean.setAddress( request.getParameter( "address" ) );
				customersearchbean.setCity( request.getParameter( "city" ) );
				customersearchbean.setState( request.getParameter( "state" ) );
				customersearchbean.setZip( request.getParameter( "zip" ) );	
				
				// search with with query string
				String strUlr =  getSisUrlSearchScript () +"?" + customersearchbean.getQueryString();
				String  strXmlResults = ExpressUtil.findReplace( customersearchbean.recieveUrl( strUlr), "&", "&amp;" );
				//strXmlResults = strXmlResults.substring(3);
				// echo xml return results in weblogic log
				Log.write(Log.DEBUG_VERBOSE, strXmlResults );
				if( 0 > strXmlResults.indexOf("<?xml") ){
					customersearchbean.setHTMLResults( strXmlResults );
					request.getHttpRequest().setAttribute("customersearchbean", customersearchbean);
					alltelRequestDispatcher.forward("/CustomerLookupResults.jsp");  
					break;
				}
				CustomerContactInfoBean csInfoBean = new CustomerContactInfoBean( "" );
				csInfoBean.setUserId( strUSERID );	
				String strNoMatch = csInfoBean.processSISNoMatches( strXmlResults );
				if( strNoMatch.length() > 0 )
				{
					customersearchbean.setHTMLResults( strNoMatch );
					request.getHttpRequest().setAttribute("customersearchbean", customersearchbean);
					alltelRequestDispatcher.forward("/CustomerLookupResults.jsp");  
					return;		
					
				}
				// extract XML with DOM package and return results as 
				// CustomerContactInfoBean instances in a vector
				Vector vContactInfoBeans =  csInfoBean.extractXML( strXmlResults );					
				// print results and save matches to db as being printed to html string.
				StringBuffer sb = new StringBuffer( ONE_K );
				sb.append( "<table width=80% align=center border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=3>" );
				sb.append( "<TR><td width=20%><b>Business Name</b></td>" );
				sb.append( "<td width=10%><b>Phone</b></td>" );
				sb.append( "<td width=45%><b>Address</b></td>" );
				sb.append( "<td width=15%><b>Actions</b></td></tr>" );
				CustomerContactInfoBean bTempContact = null;
				Connection conn = null;
				try{					
					conn = DatabaseManager.getConnection();
					for( int i = 0; i <  vContactInfoBeans.size(); i++ ){
						bTempContact = (CustomerContactInfoBean)vContactInfoBeans.get(i); 
						if( bTempContact != null ){
							// save match in db table 
							bTempContact.dbSave( conn  );
							sb.append( bTempContact.displayHtml());	
						}					
					}
				}catch( Exception e ) 
				{
					e.printStackTrace();
					Log.write(Log.DEBUG_VERBOSE, "ExpressSISCtlr:CUSTOMER_LOOKUP_FORM_SUBMIT Caught Exception e=[" + e.toString() + "]");
				}
				finally {	
					sb.append( "</table>" );
					DatabaseManager.releaseConnection( conn );				
				} 			
				
				customersearchbean.setHTMLResults( sb.toString() );
				request.getHttpRequest().setAttribute("customersearchbean", customersearchbean);
				alltelRequestDispatcher.forward("/CustomerLookupResults.jsp");  
				break;
			}
			case CUSTOMER_LOOKUP_FORM_CANCEL:
			{
			
				break;
			}
			case CREATE_CUSTOMER_FROM_LIST:
			{
			
				break;
			}
		
			default: {	
				alltelRequestDispatcher.forward("/CustomerLookup.jsp"); 
			}
		}
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
	
	public String getSisUrl(){
		String strUrl = "";
		try{
			strUrl = PropertiesManager.getProperty( "lsr.sis.url", EXPRESS_SIS_URL);
		}catch( Exception e ) 
		{
			e.printStackTrace();
			Log.write(Log.DEBUG_VERBOSE, "ExpressSISCtlr:getSisUrl Caught Exception e=[" + e.toString() + "]");
		}
		return strUrl;
	}
	
	public  String getSisUrlSearchScript(){
		
		String strUrlscript = "";
		try{
			strUrlscript = PropertiesManager.getProperty( "lsr.sis.url", EXPRESS_SIS_URL) 
					+ PropertiesManager.getProperty( "lsr.sis.searchscript", SIS_SEARCH_SCRIPT);
		}catch( Exception e ) 
		{
			e.printStackTrace();
			Log.write(Log.DEBUG_VERBOSE, "ExpressSISCtlr:getSisUrlSearchScript() Caught Exception e=[" + e.toString() + "]");
		}
		return strUrlscript;
	}
}

