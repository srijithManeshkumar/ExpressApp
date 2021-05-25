/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			ALLTEL COMMUNICATIONS INC.
 */
/** 
 * MODULE:	ExpressUtil.java	
 * 
 * DESCRIPTION: Utility functions that don't depend on the database. 
 * 				Use this file to add function specifically Express and are used useable in more than one files/ script or class.
 *				 
 * 
 * AUTHOR:      EK: Express Development Team
 * 
 * DATE:        02-02-20005
 * 
 * HISTORY:
 * EK: File started on 02-02-20005.
 */
package com.alltel.lsr.common.util;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.Date;
import com.alltel.lsr.common.util.*;

public class ExpressUtil 
{
	
	static final int INVALID_INT_VALUE = -99;

	
	/*Create a formated string of current date/
	 *EK: created 5/25/2005
	 */
	 public static String getCurrentDateYYYYMMDDD_HH24MMSS() {	
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
		java.util.Date dTime =  cal.getTime();
		String dateString = formatter.format(dTime);
		return dateString;
	}
	
	public static String getDisplayDateFormat( String strDateYYYYMMDD_HHMMSS )
	{
		if ( strDateYYYYMMDD_HHMMSS == null || strDateYYYYMMDD_HHMMSS.length() > 15 ){
			return strDateYYYYMMDD_HHMMSS;
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(strDateYYYYMMDD_HHMMSS.substring(0,4)),
			Integer.parseInt(strDateYYYYMMDD_HHMMSS.substring(4,6) ) - 1,
			Integer.parseInt(strDateYYYYMMDD_HHMMSS.substring(6,8) ),
			Integer.parseInt(strDateYYYYMMDD_HHMMSS.substring(9,11) ),
			Integer.parseInt(strDateYYYYMMDD_HHMMSS.substring(11,13) ),
			Integer.parseInt(strDateYYYYMMDD_HHMMSS.substring(13,15) ) );
		SimpleDateFormat formatter = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
		java.util.Date dTime = cal.getTime();
		String dateString = formatter.format(dTime);
		return dateString;
	}
	/* EK. This function attempts to format a bad date string into an accepted SLA calculatable date.
	   Dates that are not fail will print "". 
	 */
	 
	public static String FormatDateYYYYMMDDD_HH24MMSS( String badDate ) 
	{
		String goodDate = "";
		if( badDate == null ) 
		{	
			return goodDate;
		}	
		badDate = badDate.trim();	
		// rid white space.
		goodDate = findReplace( badDate, " ", "" ); 
		goodDate = findReplace( goodDate, "-", "" ); 
		goodDate  = findReplace( goodDate, "/", "" ); 
		
		// make sure that all are digits
		for ( int i = 0; i < goodDate.length(); i++ ) {			
			if (  !Character.isDigit( goodDate.charAt(i)  ) ){ 
				return "";
			}
		}
		
		// if delimiter was used split ( only accept - or /) conventional date delimiters.
		
		String[] dateArray = null;
		try{
			if( badDate.indexOf( "-") > 0 ){
				dateArray =	badDate.split("-",3);
			}else if (badDate.indexOf( "/") > 0)  {
				dateArray =	badDate.split("/",3);
			}
		}catch(Exception e) {
			//do nothing on exceptions just continue to next row.
			//e.printStackTrace();
			//System.err.println( e.toString() );
		}
		
		// start formatting the date to YYYYMMDD HH24MMSS format.
		String strTime = " 070000";
		String yr = "";
		String dy =  "";
		String month = "";
		//try to format if there were delimiters.
		if( dateArray != null  && dateArray.length == 3) {			
			
			if( dateArray[1].length() == 1 || dateArray[1].length()  == 2 ){
				dy =   dateArray[1].length() == 1 ? "0" + dateArray[1]: dateArray[1] ;
			}else{
				return "";
			}	
			if( dateArray[0].length() == 1 || dateArray[0].length()  == 2 ){
				month =   dateArray[0].length() == 1 ? "0" + dateArray[0]: dateArray[0] ;
			}else{
				return "";
			}	
			if( dateArray[0].length() == 2 || dateArray[0].length()  == 4 ){
				yr =   dateArray[2].length() == 2 ? "20" + dateArray[2]: dateArray[2] ;
			}else{
				return "";
			}	
			
		}else if( goodDate.length() == 6 ){	
			// note, when system was just created, 11-12, 2005. users were entering mmddyy. so only try 
			// this reformat this if and only it follows in that time period.
			String strTemp = "";
			yr =  (strTemp = goodDate.substring(4)).equals("05") ? "20" +strTemp : "";
			strTemp = goodDate.substring(0,2);
			month =  (strTemp.equals("12") || strTemp.equals("11")  )? strTemp : "";
			dy =  goodDate.substring(2,4);

		}else{
			return "";
		}		
		// lastly make sure the date is 15 chars long
		goodDate = yr +  month + dy + strTime;		
		if (goodDate.length() != 15) { return "";}				
		return  goodDate;	
		
	}
	
	public static String printDateSelect(  boolean bFieldFlag, boolean bFlag, String strStartMth,
							String strStartDay, String strStartYr,
							String strEndMth, String strEndDay, String strEndYr   )
	{
 	 	
 	 	String strEndStart = bFieldFlag ? "_end" : "";
 	 	String strDatesel = "<SELECT name=\"from_due_date_mnth" + strEndStart + "\">\n";
		String y;
		Calendar cal = Calendar.getInstance();
		
		// set the default range for to six month back for starting dates
		// and set it to 3 months back for ending dates.
		
		if(!bFlag && !bFieldFlag )
		{
			cal.add(Calendar.MONTH, -6);
		}else if( !bFlag && bFieldFlag ){
			cal.add(Calendar.MONTH, -3);
		}
		int iMth = cal.get(Calendar.MONTH)+1;
		int iDay = cal.get(Calendar.DAY_OF_MONTH);
		int iYear = cal.get(Calendar.YEAR);
		
		String strCurrentMonth = !bFlag ? ( strStartMth == null ? "":strStartMth) :			
			 (strEndMth == null ? "": strEndMth) ;
		
		String strCurrentYear = !bFlag  ? (strStartYr==null ? "": strStartYr) :
			( strEndYr == null? "" : strEndYr )  ;
		
		String strCurrentDay =  !bFlag  ? (strStartDay == null ? "": strStartDay ) : 
			( strEndDay == null ? "" : strEndDay ) ;
		
		for (int x = 1; x < 13 ; x++)
		{	y = "" + x;
			if (y.length()==1) y="0"+x;
			
			if(y.equals(strCurrentMonth) )
			{
				strDatesel += "<OPTION SELECTED value=\"" + y + "\">" + y;
			}else if (x == iMth) 
			{
				strDatesel += "<OPTION SELECTED value=\"" + y + "\">" + y;
			} else {
				strDatesel += "<OPTION value=\"" + y + "\">" + y ;
			}
		}
		strDatesel += "</SELECT><SELECT name=\"from_due_date_dy" + strEndStart + "\">";
		for (int x = 1; x < 32 ; x++)
		{	y = "" + x;
			if (y.length()==1) y="0"+x;
			
			if( y.equals(strCurrentDay) )
			{
				strDatesel += "<OPTION SELECTED value=\"" + y + "\">" + y;
			} else if (x==iDay)
			{
				strDatesel += "<OPTION SELECTED value=\"" + y + "\">" + y;
			} else 
			{
				strDatesel += "<OPTION value=\"" + y + "\">" + y ;
			}
		}

		strDatesel += "</SELECT><SELECT name=\"from_due_date_yr" + strEndStart + "\"> ";
		for (int x = 2001; x < 2011 ; x++)
		{
			
			if( strCurrentYear.equals(""+x ))
			{
				strDatesel += "<OPTION SELECTED value=\"" + x + "\">" + x ;
			}else if (x==iYear) 
			{
				strDatesel += "<OPTION SELECTED value=\"" + x + "\">" + x ;
			} else {
				strDatesel += "<OPTION value=\"" + x + "\">" + x ;

			}
		}
		strDatesel += "</SELECT> ";
		return strDatesel;
}	

	/*@ EK: Description: This method walks through a string txt, 
					finds all occurrence of fnd and replaces them with rplc
					
	 String and replace it.
	 *@param	String txt String to search in
	 *@param	String fnd String to search for
	 *@param 	String rplc String to replace with.
	 */
	 	
	public static String findReplace(String txt, String fnd, String rplc)
	{
		StringBuffer nstr;
		int nsubs = 0;	
		int subs = 0;	
		int fndl = fnd.length();
		int txtl = txt.length();
		nstr = new StringBuffer(txtl);
		while(-1 != ( nsubs = txt.indexOf( fnd, subs ) ) )	
		{
			nstr.append( txt.substring( subs, nsubs ) );	
			nstr.append( rplc );	
			subs = nsubs + fndl;	
		}
		nstr.append(txt.substring(subs));
		return nstr.toString();
	}
	
	
	/*EK:
	/*text box with int value
	/*
	/**********************************************/
	public static String printInputText(String name, int size, int maxlength, int value)
	{
		return printInputText(name,size,maxlength,String.valueOf (value));
	}
	
	public static String printInputText(String name, int size, int maxlength, String value)
	{
		return "<input type=\"text\" name=\"" + name + "\" size=\"" + size + "\" maxlength=\"" + maxlength + "\" value=\"" + value + "\">";
	}
	
	/* EK: Prints out an html select box in servlet or jsp page.
	/*
	/*
	/**********************************************/
	public static String printSelectBox(String name, int size, String values[], String option_names[], String val_selected)
	{
		StringBuffer s = new StringBuffer(1024);
		s.append("<select name=\"" + name + "\" size=" + size + ">\n");
		for (int i = 0; i < values.length; ++i) {
			
			s.append("<option value=\"" + values[i] + "\"" + (option_names[i].equalsIgnoreCase(val_selected) ? " selected" : "") + ">" + option_names[i] + "</option>\n");
		}
		s.append("</select>\n");		
		return s.toString();
	}	
	
	
	/* EK. Multiple select with Array of selected values
	 * This function creates a multiple box with multiple 
	 * selected values.
	 */	
	public static String printMultipleSelectedValues( String name, int size, Object values[], Object option_names[], String val_selected[] )
	{
		StringBuffer s = new StringBuffer(1024);
		s.append("<select name=\"" + name + "\" size=" + size + " MULTIPLE>\n");			
		int nValLen = values.length;
		int nSelectLen =  val_selected == null ? 0 : val_selected.length;
		boolean bSelected;	
		String strTemVal = "";
		for ( int i = 0; i < nValLen; ++i ) {
			strTemVal = (String)values[i];
			bSelected = false;
			for( int j = 0; j < nSelectLen; ++j ){
				if( strTemVal.equals( val_selected[j] ) ){
				 	bSelected = true;
				}
			}	
			s.append("<option value=\"" + strTemVal + "\">" + (String)option_names[i] + "</option>\n");
		}
		s.append("</select>\n");
		
		return s.toString();
	}
	
	
	
	/** EK
    * Creates a Listbox containing states.
    *
    * @param  name           defines the name for the selected state
    * @param  val_selected   defines the value for the selected state
    * @return                a new state selection Listbox
    */
	public static String printSelectBoxStates( String name, String[] val_selected, int iSize ) 
	{
		
		
		Object values[] = { "AL","AK","AZ","AR","CA","CO","CT",
			"DE","DC","FL","GA","HI","ID","IL","IN","IA","KS","KY",
			"LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV",
			"NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI",
			"SC","TN","TX","UT","VT","VA","WA","WV","WI","WY" };		
		
		//options	
		Object option_names[] = {"Alabama","Alaska","Arizona","Arkansas","California","Colorado",
			"Connecticut","Delaware","District of Columbia","Florida","Georgia","Hawaii","Idaho",
			"Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland",
			"Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska",
			"Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina",
			"North Dakota","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","Tennessee","Texas","Utah","Vermont",
			"Virginia","Washington","West Virginia","Wisconsin","Wyoming"};
	
		return printMultipleSelectedValues(name,iSize, values, option_names, val_selected  );
	}
		
	/* EK:
	*/
	public static String getStateFullNames( String[] state_cds ){
		String states = "";
		int sttcounter = 0;
		if ( state_cds == null ){
		 return "All States"; 	
		}
		while( sttcounter < state_cds.length){
			states += getStateFullName(state_cds[sttcounter] );
			states += "&nbsp;&nbsp;";
			sttcounter++;
		}
		return states;	
	}

	/* EK:
	*/
	public static String getStateFullName( String state_cd ){
	
		if( state_cd == null || state_cd.length() != 2 ){
		 	return "Invalid State Code";
		}
		
		
		String values[] = { "__","AL","AK","AZ","AR","CA","CO","CT",
			"DE","DC","FL","GA","HI","ID","IL","IN","IA","KS","KY",
			"LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV",
			"NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI",
			"SC","TN","TX","UT","VT","VA","WA","WV","WI","WY" };		
		int i = 0;
		for ( ; i < values.length; i++ ){
			if( state_cd.equalsIgnoreCase(values[i] ) )
			{
				break;
			}
		 }
		String option_names[] = {"All States", "Alabama","Alaska","Arizona","Arkansas","California","Colorado",
			"Connecticut","Delaware","District of Columbia","Florida","Georgia","Hawaii","Idaho",
			"Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland",
			"Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska",
			"Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina",
			"North Dakota","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","Tennessee","Texas","Utah","Vermont",
			"Virginia","Washington","West Virginia","Wisconsin","Wyoming"};

		return option_names[i];
	}
	/** EK
	* Get Integer parameter from the request object
	*/
	public static int getIntParam(HttpServletRequest req, String param)
	{
		int intParam = INVALID_INT_VALUE;

		try {
			intParam = Integer.parseInt (req.getParameter(param));
		}
		catch (NumberFormatException nfe) {
			return INVALID_INT_VALUE;
		}

		return intParam;
	}

	
	/** EK
	* Get Integer parameter from the request object
	* return the default value if any exceptions occure
	*/
	public static int getIntParam(HttpServletRequest req, String param, int defaultInt)
	{
		int intParam = INVALID_INT_VALUE;

		try {
			intParam = Integer.parseInt (req.getParameter(param));
		}
		catch (NumberFormatException nfe) {
			return defaultInt;
		}

		return intParam;
	}

	
	/** EK
	* Get String parameter from the request object
	*/
	public static String getStringParam(HttpServletRequest req, String param)
	{
		String strParam = "";
		strParam = req.getParameter(param);
		return strParam;
	}

	
	/** EK
	* Get String parameter from the request object
	* Return the default if null is returned
	*/
	public static String getStringParam(HttpServletRequest req, String param, String defaultStr)
	{
		String strParam = "";
		strParam = req.getParameter(param);
		if(strParam != null)
			return strParam;
		return defaultStr;
	}

 // EK check if string exists in array.
 public static boolean isElementOf( String[] arr, String otarget){
			
		if( arr == null ) 
		{
			return false;
		}
		if(arr.length < 1  ){
			return false;
		}
		int i = 0 ;
		while( i < arr.length ){
			if( otarget.equalsIgnoreCase(arr[i]) ){
				return true;
			}
			i++;
		}
		return false;	

	 }
	
	// EK check if string exists in array and return position of the target
 public static int getElementPos( String[] arr, String otarget){
			
		if( arr == null ) 
		{
			return -1;
		}
		if(arr.length < 1  ){
			return -1;
		}
		int i = 0 ;
		while( i < arr.length ){
			if( otarget.equalsIgnoreCase(arr[i]) ){
				return i;
			}
			i++;
		}
		return -1;	

	 }
	 

	/*EK.
	* change special signs to HTML representation
	*/
	public static String escapeHTML(String src)
	{
		//check if source is null or blank
		if (src == null || src.equals(""))
			return "";
		Hashtable reserved_chars = new Hashtable(17);
		StringBuffer res = new StringBuffer(256);
		String str = "";
		String s = "";
		reserved_chars.put("<", "&lt;");    // less than: <
		reserved_chars.put(">", "&gt;");    // greater than: >
		reserved_chars.put("'", "&#039;");  // apostrophe: '
		reserved_chars.put(";", "&#059;");    // semicolon: ;
		reserved_chars.put("\"", "&#034;");    // double quote
		reserved_chars.put("'", "&#145;");    // left single quote
		reserved_chars.put("'", "&#146;");    // right single quote
		reserved_chars.put("\"", "&#147;");    // left double quote
		reserved_chars.put("&", "&amp;");   // ampersand: &
		reserved_chars.put("\"", "&#148;");    // right double quote
		reserved_chars.put("`", "&#096;");    // grave accent (backtick)
		// Convert existing escape
		src = reverseEscapeHTML(src);
		for (int i = 0; i < src.length(); ++i) {
			s = String.valueOf(src.charAt(i));
			str = (String) reserved_chars.get(s);
			if (str != null)
				res.append(str);
			else
				res.append(s);
		}
		return res.toString();
	}	
	
	/* EK:
	* change special signs to HTML representation
	*/
	public static String reverseEscapeHTML(String src)
	{
		int srcLen = src.length();
		int hashkeyLen = 0;
		if (src == null || src.equals(""))
			return src;
		Hashtable reserved_chars = new Hashtable(17);
		StringBuffer res = new StringBuffer(256);
		String str, s, srcChar;
		reserved_chars.put("&#039;", "'");  // apostrophe: '
		reserved_chars.put("&#8217;", "'");  // smart apostrophe: '
		reserved_chars.put("&amp;", "&");   // ampersand: &
		reserved_chars.put("&lt;", "<");    // less than: <
		reserved_chars.put("&gt;", ">");    // greater than: >
		reserved_chars.put("&#059;", ";");    // semicolon: ;
		reserved_chars.put("&#034;", "\"");    // double quote
		reserved_chars.put("&#145;", "�");    // left single quote
		reserved_chars.put("&#146;", "�");    // right single quote
		reserved_chars.put("&#147;", "�");    // left double quote
		reserved_chars.put("&#148;", "�");    // right double quote
		reserved_chars.put("&#096;", "`");    // grave accent (backtick)
		reserved_chars.put("&#8220;", "\"");    // left double quote
		reserved_chars.put("&#8221;", "\"");    // right double quote
		reserved_chars.put("&#8212;", "-");    // right double quote
		for (int i = 0; i < srcLen; ++i) {
			if (hashkeyLen > 0) {
				--hashkeyLen;
				continue;		// skip remaining code in loop
			}

			srcChar = String.valueOf(src.charAt(i));

			/** get hash key **/
			// check substring of 6 chars
			if ((i + 6) < srcLen) {
				s = src.substring(i, (i + 7)).toString();
				str = (String) reserved_chars.get(s);
				if (str != null) {
					res.append(str);
					hashkeyLen = 6;
					continue;		// skip remaining code in loop
				}
			}
			if ((i + 5) < srcLen) {
				s = src.substring(i, (i + 6)).toString();
				str = (String) reserved_chars.get(s);
				if (str != null) {
					res.append(str);
					hashkeyLen = 5;
					continue;		// skip remaining code in loop
				}
			}
			// check substring of 5 chars
			if ((i + 4) < srcLen) {
				s = src.substring(i, (i + 5)).toString();
				str = (String) reserved_chars.get(s);
				if (str != null) {
					res.append(str);
					hashkeyLen = 4;
					continue;		// skip remaining code in loop
				}
			}
			// check substring of 4 chars
			if ((i + 3) < srcLen) {
				s = src.substring(i, (i + 4)).toString();
				str = (String) reserved_chars.get(s);
				if (str != null) {
					res.append(str);
					hashkeyLen = 3;
					continue;		// skip remaining code in loop
				}
			}
			// not an escape char
			res.append(srcChar);
		}
		return res.toString();
	}	
	
	
	/* EK
	*/
	// strip out bad Characters possible tag name such as form names ...etc.
	public static String makeValidXmlTag( String src){
		char spChar[] = { '-',':', '.', '~','!','#','$','%','^','*','(', ')','&','+',',','|', ';', '`', '\'', ' ',  '\\', '/', '?'}; 
		//check for unacceptable characters in tag string
		if( src == null ) // should never happened.
		{
			return "";
		}
		// only allow up 20 characaters for a tag. if longer than 20 character
		// include comments in the xml string.
		String localSrc = "";
		if(	src.length() > 20 ) 
		{
			
			localSrc = src.substring( 20, src.length());
			int itemp = localSrc.indexOf( " " ) ;
			if( itemp > 0 ){				
				localSrc = src.substring(0,20) + 
				localSrc.substring( 0,   ( itemp > -1 ? itemp : 1) );
			}else
			{			
				localSrc = src.substring(0,20);
				if( !src.substring(20, 21).equals(" ")) {					
					itemp = localSrc.lastIndexOf( " " ) ;
					localSrc = localSrc.substring( 0,   ( itemp > -1 ? itemp : 20) );
				}
			}
		
		}else
		{
			localSrc = src;
		}
		// strip bad characters.
		
		for(int i = 0; i < spChar.length; i++){
			if(localSrc.indexOf(spChar[i]) > -1)
			{	
	 			localSrc = findReplace( localSrc, ""+spChar[i], "" );
			}
		}
		
		// lastly check if first character is a digit.
		if( Character.isDigit( localSrc.charAt(0) ) ){
		//change to digit English name
			localSrc = getDigitEnglishName( localSrc.charAt(0))+ localSrc.substring(3);
		}
		return localSrc;
	}
	
	
	/* EK
	*/
	static String getDigitEnglishName( char cdigit ){
		Hashtable hDnames  = new Hashtable( 10 );
		hDnames.put( "0", "Zero" );
		hDnames.put( "1", "First" );
		hDnames.put( "2", "Second" );		
		hDnames.put( "3", "Third" );		
		hDnames.put( "4", "Fourth" );
		hDnames.put( "6", "Sixth" );
		hDnames.put( "7", "Seventh" );
		hDnames.put( "8", "Eighth" );
		hDnames.put( "9", "Ninth" );
		return (String)hDnames.get( ""+cdigit );
	}
	/********************** check if String is Null ************************************/
	
	/* EK
	*/
	public static String fixNullStr( String s ){
		if (s == null){
			return "";
		}
		return s;
	}
	
	/* EK
	*/
	public  static String getSisUrlProd(){
		
		String strUrlscript = "";
		try{
			strUrlscript = PropertiesManager.getProperty( "lsr.sis.url", "http://sis.dsys.alltel.net/") 
					+ PropertiesManager.getProperty( "lsr.sis.pushdatascrip", "writeExpressOrder.php" );
		}catch( Exception e ) 
		{
			e.printStackTrace();
			Log.write(Log.DEBUG_VERBOSE, "ExpressUtil:getSisUrlProd() Caught Exception e=[" + e.toString() + "]");
		}
		return strUrlscript;
	}
	
	/* EK
	*/
	public static String[]  extractEmployeeGroups( Connection conn, String[] groupIds, 
	boolean bAllUsers, String[] m_strUserids ) 
	throws SQLException, Exception
	{
		PreparedStatement pstmt = null;
		ResultSet rset = null;      
		String strActiveStatus = "N";
		String strQry = " Select DISTINCT USERID FROM USR_USRGRP_LINK_T@express WHERE "
				+ " STATUS = ? ";                		
		Vector vUsers = new Vector( 40 );		 
		String strWhereClause = "";		
		if(  groupIds  == null){  
			return m_strUserids;                 	       
		}
		int iIdCounter = 0, iTempCnt = 0;
		if( !bAllUsers ){
			strWhereClause = " AND USRGRP_EMP_SQNC_NMBR IN (";
			iIdCounter = groupIds.length;
			if( iIdCounter > iTempCnt )
			{
				 iTempCnt++;
				 strWhereClause += "?";
			}	
			while( iIdCounter > iTempCnt )  
			{        		
				strWhereClause += ",? ";
				iTempCnt++;
			}                
			strWhereClause += " )"; 
		}               
		strQry += strWhereClause;	            	
		pstmt = conn.prepareStatement( strQry  );
		pstmt.clearParameters();
		pstmt.setString( 1, strActiveStatus );
		for( int j = 0; j < iIdCounter;j++ ){
			pstmt.setInt( j+2, Integer.parseInt( groupIds[j] ) );
		}
		rset = pstmt.executeQuery( );	  
		while( rset.next() )
		{
			vUsers.add( rset.getString(1) ); 
		}                         
		rset.close(); rset=null;
		pstmt.close(); pstmt=null;	
			   
		/******expand user array*********/
		int  iUsrInGroups = vUsers.size();
		int ipos = 0;
		String new_users[] = null;        		
		if(  m_strUserids  == null ){
			new_users = new String[iUsrInGroups];
			ipos = 0;
		}else
		{
			new_users = new String[m_strUserids.length + iUsrInGroups];
			System.arraycopy(m_strUserids,0,new_users,0,m_strUserids.length);
			ipos = m_strUserids.length;
		}
		String strTempId = "";
		for( int i =0; i < iUsrInGroups; i++ )
		{
			strTempId = (String)vUsers.get(i);
			//skip duplicates
			if( !ExpressUtil.isElementOf( m_strUserids, strTempId  ) ) {
				new_users[ipos] = strTempId;
				ipos++;
			}			
		}
		// Trim array
		if( ipos > 0 ) {
			m_strUserids = new String[ipos];
			System.arraycopy( new_users,0,m_strUserids,0,ipos);
		}
		
		return m_strUserids;
	}

	// EK, Read file from currently dir.
	public static String readFile(String fileName)
	{
		
		final String FNAME = "readFile";		
		if(fileName.equals("")) {
			System.err.println("File-name cannot be Null! " );
			return "";
		}
		BufferedReader inFile = null;
		String newLine = "";
		StringBuffer fileBuffer = new StringBuffer( 10480 ); 
		try {
			inFile = new BufferedReader(new FileReader(fileName));
			while ((newLine = inFile.readLine()) != null) {
		  		fileBuffer.append(newLine + "\n");
			}
			inFile.close();
		}		
		catch (IOException e) {
			System.err.println("IOException while reading file: " + fileName +" \n" + e.getMessage());
		}
		return  fileBuffer.toString();
	} // 
	
	
	
	/** EK
	 * Creates date formatted as specified in argument
     * @param dateFormat String representing middleware date format.
	 * @return string date of second argument
    	*/
    public static String getDateTimeStamp( Date dDt, String dateFormat)
	{
		String  returnString     = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
       return returnString = simpleDateFormat.format(dDt);

	}
	
	/** EK
	 * Returns the current time in "YYYY-MM-DD HH:MM:SS" format.
	 *YYYY/MM/DD HH24:MI:SS
	 * @return  String containing formatted date-time.
	 */
    public static String getDateTime( Calendar cal  )        	
	{	
	    	// switch month count from 0-11 to 1-12
        	int iMonth =  cal.get(Calendar.MONTH) + 1;
			
	    //  return current time in YYYY-MM-DD HH:MM:SS format
		String strDateTime = "" + cal.get(Calendar.YEAR) + "-";
		if (iMonth<10)
			strDateTime += "0";
		strDateTime += iMonth + "-";
		if (cal.get(Calendar.DAY_OF_MONTH) < 10)
			strDateTime += "0";
		strDateTime += cal.get(Calendar.DAY_OF_MONTH) + " ";
		if (cal.get(Calendar.HOUR_OF_DAY) < 10)
			strDateTime += "0";
		strDateTime += cal.get(Calendar.HOUR_OF_DAY) + ":";
		if (cal.get(Calendar.MINUTE) < 10)
			strDateTime += "0";
		strDateTime += cal.get(Calendar.MINUTE) + ":";
		if (cal.get(Calendar.SECOND) < 10)
			strDateTime += "0";
		strDateTime += cal.get(Calendar.SECOND);
		return strDateTime;
	}
}
	
	