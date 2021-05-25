/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *						BY
 *				ALLTEL Communications Inc
 */

/* 
 * MODULE:		Toolkit
 * 
 * DESCRIPTION: A toolbox of useful utility methods.
 * 
 * AUTHOR:      CMM003
 * 
 * DATE:        08/09/1999
 * 
 * HISTORY:
 *	pjs	11/5/2001 cloned from eWave
 *	pjs	9-27-2004 added getDateTimeStamp(String)
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/Toolkit.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:06:52   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
 
package com.alltel.lsr.common.util;
import java.util.*;
import java.io.*;
import java.security.*;
import java.sql.*;
import java.text.*;


/**
 * This class contains useful helper methods not provided by the standard Java classes.
 */ 
public class Toolkit {
	
	/**
	 * private static variable that indicate the characters that are allowed in user id.
	 * Now only alphanumeric, space and dash characters are allowed. 
	 */
    	private final static String USERID_ALLOWED_CHAR = 
                                "abcdeefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -";
    	
	public final static String REPORT_DATE_FORMAT = "MM-dd-yyyy @ hh:mm:ss a";

	/**
	 * Replaces all occurrences of a substring within a string.
	 * This method will search through a string and replace each occurrence of a
	 * substring with a supplied string replacement.
	 *
	 * @param   strOriginalString	string in which to make replacements
	 * @param   strToBeReplaced	    substring to be replaced
	 * @param   strReplacement	    string that replaces above substring
	 *
	 * @return  String with all replacements made
	 *
	 */
    	public static String substringReplace( String strOriginalString, String strToBeReplaced,
                                           String strReplacement) 
	{
         	String  strNewString = strOriginalString;
         
         	int iIndex = strNewString.indexOf(strToBeReplaced);
         	int iLengthToReplace = strToBeReplaced.length();
         
         	while (iIndex != -1) {
            	// replace this occurrence of the replacee
            	strNewString = strNewString.substring(0, iIndex)
                         + strReplacement
                         + strNewString.substring(iIndex + iLengthToReplace);           
            	// look for another occurrence of the replacee             
            	iIndex = strNewString.indexOf(strToBeReplaced);
         	}
         
         	// no more occurrences of the replacee       
         	return strNewString;
     	}
     
     	/**
	 * Replaces all occurrences of a spaces within a string with plusses.
	 * This alleviates the truncation of strings being passed through the URL.
	 * This method will search through a string and replace each occurrence of a
	 * space with a plus.  It will return the string with plusses.
	 *
	 * @param   strOriginalString	string in which to make replacements
	 * @param   strConvertedString  converted string
	 *
	 * @return  strConvertedString  string with all replacements made
	 *
	 */
    	public static String convertStringForRequest(String strOriginalString) 
	{
        	String strConvertedString = substringReplace(strOriginalString," ","+");
       		return strConvertedString;
    	}
        
     
     	/**
      	* Replace Single quote ("'") with double single quotes("''") for database pruposes
      	* @param	strOriginalString string which may or may not contain single quote(s)
      	* 
     	* @return String with all replacements (from one single quote to two single quotes)
      	*/
	public static String replaceSingleQwithDoubleQ(String str)
	{
		StringTokenizer parser = new StringTokenizer(str , "'", true) ;
		String newStr = new String();
		String tempStr = new String();
		while (parser.hasMoreTokens()){
			tempStr = parser.nextToken() ;
			if (tempStr.equals("'"))
				newStr += "''" ;
			else
				newStr += tempStr ;
		}
		return newStr ;
	}
	
	/**
	 * Replaces all occurrences of a substring with an integer.
	 * This method will search through a string and replace each occurrence of a
	 * substring with a supplied integer replacement. It converts the integer
	 * to a String and then calls the substringReplace() method above.
	 *
	 * @param   strOriginalString	string in which to make replacements
	 * @param   strToBeReplaced	    substring to be replaced
	 * @param   iReplacement	    integer that replaces above substring
	 *
	 * @return  String with all replacements made
	 *
	 */
    	public static String substringReplace(String strOriginalString, 
					      String strToBeReplaced,
					      int iReplacement) 
	{
	        String strReplacement = (new Integer(iReplacement)).toString();
       		return substringReplace(strOriginalString, strToBeReplaced, strReplacement);
    	}
    
    
	/**
	 * Returns the current time in "YYYY-MM-DD HH:MM:SS" format.
	 *
	 * @return  String containing formatted date-time.
	 */
    	public static String getDateTime() 
	{
        	Calendar cal = Calendar.getInstance();
			
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
	
	
	/**
	 * Returns the current time in milliseconds. Used to return a start time for
	 * an interval. The caller should save this start time and pass it to the
	 * stopTimer() method below, which will return an elapsed time in seconds.
	 *
	 * @return  current time in milliseconds
	 */
	public static long startTimer()
	{
        	return new java.util.Date().getTime();    // timer start time in milliseconds
	}
    
    
	/**
	 * Returns the time elapsed from the supplied start time. This method first
	 * gets the current time in milliseconds, substracts the start time from the
	 * current time to obtain the interval in milliseconds, and finally it
	 * converts this interval to seconds. Some precision is lost in this final
	 * conversion as the value will be rounded down to the last full second.
	 *
	 * @return  elapsed time in seconds since lStartTime
	 */
    	public static float stopTimer(long lStartTime) 
	{
        	long   lStopTime    = new java.util.Date().getTime();  // timer stop time in milliseconds
	        long   lElapsedTime = lStopTime - lStartTime;          // elapsed time in milliseconds
       		float  fElapsedTime = ((float)lElapsedTime)/1000;      // elapsed time in seconds
       		 return fElapsedTime;
    	}

    	/**
     	* This function returns the string which contains the permitted characters in user id 
     	*/
	public static String getAllowedUserId () 
	{
		return USERID_ALLOWED_CHAR;
	}
	
	/**
	 * This functioin read all text from a given file and return the content of 
	 * the file as a string.
	 */
	public static String readTemplateFile(String strFullFilePath) 
	         throws Exception 
	{
        	String strNextLine = null;
	        String strTotolText ="";
		
		// read in template file
       		BufferedReader inFile = new BufferedReader(new FileReader(strFullFilePath));
	  
		while ((strNextLine = inFile.readLine()) != null)
	    	{
           		strTotolText =  strTotolText + strNextLine;
		}
		
		inFile.close();
		
		return strTotolText;
		
	}	// end of readTemplateFile	

	// One way encryption - using algorithms from java.security   
	public static String encryptPassword(String strUnEncryptedPassword)
	 {
		String strEnc = null;
		
		try {
			//From java.security.*
			MessageDigest algorithm = MessageDigest.getInstance("SHA-1");
			algorithm.reset();
			algorithm.update( strUnEncryptedPassword.getBytes() );
			byte[] barHash = algorithm.digest();
			strEnc = "";
			//Convert byte encrypt to hex chars
			for (int i=0;i<barHash.length; i++)
			{
			   strEnc += "" + "0123456789ABCDEF".charAt(0xf&barHash[i]>>4) + "0123456789ABCDEF".charAt(barHash[i]&0xF);
			}
			//Log.write(Log.DEBUG, "Toolkit() Encrypt   = " + strEnc);
			//Log.write(Log.DEBUG, "Toolkit() Unencrypt = " + strUnEncryptedPassword);
		}
		catch(Exception e) {
			Log.write(Log.ERROR, "Toolkit() Encryption algorithm not available");
		}

		return strEnc;
	 }

	// One way encryption - using algorithms from java.security   
	public static String wildCardIt(String strIn)
	{
		String strOut = "";

		if ((strIn == null) || (strIn.length() == 0))
		{
		}
		else
		{
			strOut = Toolkit.replaceSingleQwithDoubleQ(strIn).replace('*', '%');
			if (! strOut.endsWith("%"))
			{
				strOut = strOut + "%";
			}
		}

		return strOut;
	}

	// Send auto provider reply e-mail
	public static boolean autoProvReply(String strEmail, String strSubject, String strMsg)
	{
		boolean bRC = true;

		// Format Msg
		strMsg = "This message was automatically generated by Windstream Express.  Please do NOT reply to it.\n\n\n" + strMsg;

		// Send e-mail 
		if ( (strEmail != null) && (strEmail.length() > 0) )
		{
			// Loop thru e-mails separated by ;
			Vector vEmails = new Vector();
			vEmails = parseEmail(strEmail);

			for(int i=0 ; i < vEmails.size() ; i++)
			{
				String strSingleEmail = "<" + (String)vEmails.elementAt(i) + ">";

				try
				{
					EmailManager.send(null, strSingleEmail, strSubject, strMsg);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.write("Failed on EmailManager.send()");
					bRC = false;
				}
			}
		}

		return bRC;
	}

	// Send auto reply e-mail
	public static boolean autoReply(String strUserID, String strSubject, String strMsg)
	{
		String strEmail = "";
		String strRcvEmailInd = "";
		boolean bRC = true;

		// Verify valid strUserID
		if ((strUserID == null) || (strUserID.length() == 0))
		{
			return false;
		}

		// Get e-mail Address for given UserID
		Connection con = null;
		Statement stmt = null;

                try {
			String strQuery = "SELECT EMAIL, RCV_EMAIL_IND FROM USERID_T WHERE USERID = '" + strUserID + "'";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);
			rs.next();
			strEmail = rs.getString("EMAIL");
			strRcvEmailInd = rs.getString("RCV_EMAIL_IND");
		}
		catch(SQLException sqle)
		{	bRC = false;
		}
		catch(Exception e)
		{	bRC = false;
		}
		finally
		{       DatabaseManager.releaseConnection(con);
		}

		// Format Msg
		strMsg = "This message was automatically generated by Windstream Express.  Please do NOT reply to it.\n\n\n" + strMsg;

		// Send e-mail 
		if ( (bRC) && (strRcvEmailInd.equals("Y")) && (strEmail != null) && (strEmail.length() > 0) )
		{
			// Loop thru e-mails separated by ;
			Vector vEmails = new Vector();
			vEmails = parseEmail(strEmail);

			for(int i=0 ; i < vEmails.size() ; i++)
			{
				String strSingleEmail = "<" + (String)vEmails.elementAt(i) + ">";

				try
				{
					EmailManager.send(null, strSingleEmail, strSubject, strMsg);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.write("Failed on EmailManager.send()");
					bRC = false;
				}
			}
		}

		return bRC;
	}

	// parse e-mail string by semi-colon
	private static Vector parseEmail(String strEmail)
	{
		Vector vParsedEmail = new Vector();
		String strTmp = "";

		for (int i=0; i<strEmail.length(); i++)
		{
			if (strEmail.charAt(i) == ';')
			{
				vParsedEmail.addElement(strTmp);
				strTmp = "";
			}
			else
				strTmp = strTmp + strEmail.charAt(i);
		}

		if (strTmp.length() > 0)
			vParsedEmail.addElement(strTmp);

		return vParsedEmail;
	}


    	/**
	* Creates date formatted as specified in argument
    	* @param dateFormat String representing middleware date format.
	* @return String
    	*/
    	public static String getDateTimeStamp( String dateFormat)
	{
		String           returnString     = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		java.util.Date dateToday        = new java.util.Date();
        	return returnString = simpleDateFormat.format(dateToday);

	}

}

