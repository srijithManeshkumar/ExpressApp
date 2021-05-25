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
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-08-2005
 * 
 * HISTORY:
 *	6/8/2005 Edris Kalibala created
 *
*/
package com.alltel.lsr.common.objects;

import org.w3c.dom.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.sql.*;
import com.alltel.lsr.common.util.*;
public class CustomerSearchBean  extends ExpressSISBean
{  
	
	 //Member variables
	private String strPhone;
	private String strBusinessName;
	private String strLastName;
	private String strFirstName;
	private String strAddress;
	private String strCity;
	private String strState;
	private String strZip;	
	
	// results string is used to hold return results to be pass jsp page.
	private String strHTMLResults;
	
	// constructor
	public CustomerSearchBean( String xmlstring ){
			super(xmlstring);
			clean();
	}
	
	
	/* initialize private members ( called in constructor only ).
	 * @return void.
	 */
	 
	private void clean() 
	{		
		strPhone = "";
		strBusinessName = "";
		strLastName = "";
		strFirstName = "";
		strAddress = "";
		strCity = "";
		strState = "";
		strZip = "";	
		strHTMLResults = "";
	}		
	
	// public accessory functions
	
	public void setPhone( String In ) 
	{
		strPhone = In;
	}
	public String getPhone( ) 
	{
		return strPhone;
	}
	
	public void setBusinessName( String In )
	{
		strBusinessName = In;
	}	
	
	public String getBusinessName() 
	{
		return strBusinessName;
	}
	
	public void setLastName( String In )
	{
		strLastName = In;
	}	
	
	public String getLastName() 
	{
		return strLastName;
	}
	
	public void setFirstName( String In )
	{
		strFirstName = In;
	}	
	public String getFirstName() 
	{
		return strFirstName;
	}
	
	public void setAddress( String In )
	{
		strAddress = In;
	}
	
	public String getAddress( )
	{
		return strAddress;
	}
	
	public void setCity( String In )
	{
		strCity = In;
	}
	
	public String getCity( )
	{
		return strCity;
	}
	
	public void setState( String In )
	{
		strState = In;
	}
	
	public String getState()
	{
		return strState;
	}
	
	public void setZip( String In )
	{
		strZip = In;
	}
	
	public String getZip()
	{
		return strZip;
	}
		
	public void setHTMLResults( String In )
	{
		strHTMLResults = In;
	}
	
	public String getHTMLResults()
	{
		return strHTMLResults;
	}
	
	/* CreateXml, this function should be called after setting the 
	 * values to create an xml string.
	 * @param
	 * @Return: xml string based on member variable.
	 
	 */
	public String ceateXml()
	{
				
		StringBuffer sb = new StringBuffer( ONE_K );
		// first make sure required fields are set.
		sb.append( XML_HEADER );
		sb.append( "<SISExpressSearch>\n" );
		sb.append( "<phone>" +  getPhone() + "</phone>\n" );
  		sb.append( "<businessname>" +  getBusinessName() + "</businessname>\n" );
  		sb.append( "<lastname>" +  getLastName() + "</lastname>\n" );
		sb.append( "<firstname>" +  getFirstName() + "</firstname>\n" );
		sb.append( "<address>" +  getAddress() + "</address>\n" ); 
		sb.append( "<city>" +  getCity() + "</city>\n" ); 
		sb.append( "<state>" +  getState() + "</state>\n" ); 
		sb.append( "<zip>" +  getZip() + "</zip>\n" );
		sb.append( "</SISExpressSearch>\n" );
		return sb.toString(); 
		
	}
	
	/* validateRequiredValues is a public function that checks if required fields are set.
	 * a valide query (search should have at a business name or phone and client's last name)
	 */
	 
	public String validateRequiredValues(){
		String invalidField = "";
		if( ( getBusinessName().equals( "" ) || getBusinessName() == null )
			|| ( getLastName().equals( "" ) || getLastName() == null )
			|| ( getPhone().equals( "" ) || getPhone() == null ) )
			
		{
			invalidField = "You need at least one of the following:\n Business Phone number\tBusiness Name\tCustomer's Last Name\n";	
		}
		return invalidField;
	}
	
	/* Required by super abstract class not needed here for the first release.
	 */
	public  Vector extractXML( String xmlDocument )
	{
		Vector vThisSearch = new Vector(1);
		vThisSearch.add( this );
		return vThisSearch;
	}
	
	/* Generates a query-string of search parameters in case SIS is not parsing a xml.
	 * 
	 * @ returns a query-string of current search.
	 */
	
	public String getQueryString(){
		String strQryStr = "phone="+ ExpressUtil.findReplace(  getPhone(), " ", "%20" )+ "&" 
					+ "businessname="+ ExpressUtil.findReplace( getBusinessName(), " ", "%20" ) + "&" 
					+ "lastname=" + ExpressUtil.findReplace(  getLastName(), " ", "%20" )+ "&" 
					+ "firstname=" + ExpressUtil.findReplace(  getFirstName(), " ", "%20" ) + "&" 
					+ "address=" + ExpressUtil.findReplace(  getAddress(), " ", "%20" )+ "&" 
					+ "city=" + ExpressUtil.findReplace(  getCity(), " ", "%20" )+ "&" 
					+ "state=" + ExpressUtil.findReplace(  (getState().equals("__") ?"": getState()), " ", "%20" ) + "&" 
					+ "zip="+ ExpressUtil.findReplace(  getZip(), " ", "%20" );
		return strQryStr;
	}
	
	
}