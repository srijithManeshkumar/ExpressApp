/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2003
 *						BY
 *				ALLTEL INFORMATION SERVICES
 */

/* 
 * MODULE:		Validate.java
 * 
 * DESCRIPTION: 
 * The Validate class is a common module used to contain shared validation methods.
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        11-23-2001
 *
 * CHANGE HISTORY:
 *	02/x222002  psedlak Allow dates before 1-1-2000
 *	03/12/2003  psedlak Trap data exceptions in date routines, chg isDigits()
 *	07/26/2004  psedlak Allow " in special chars fields...
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/UTIL/Validate.java  $
/*
/*   Rev 1.4   May 30 2002 14:00:30   dmartz
/*
/*   Rev 1.2   22 Feb 2002 12:59:54   sedlak
/*UAT issue Release 1.1
/*
/*   Rev 1.1   31 Jan 2002 08:36:42   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:07:02   wwoods
/*Initial Checkin
*/

package com.alltel.lsr.common.util;

import java.sql.*;
import com.alltel.lsr.common.util.*;


public class Validate 
{
	public static final int EARLIEST_YEAR = 1901;
	public static final int LATEST_YEAR = 2050;

	// returns true if any special chars exist in String
	public static final boolean containsSpecialChars(String mystring)
	{
		for (int i=0; i<mystring.length(); i++)
		{
			switch (mystring.charAt(i))
			{
				case '<':
				case '>':
//7-26-2004			case '"':
					return true;
			}
		}

		return false;
	}

	public static final boolean hasSpaces(String mystring)
	{
		for (int i=0; i<mystring.length(); i++)
		{
			if (mystring.charAt(i) == ' ')
			{
				return true;
			}
		}

		return false;
	}

	public static final boolean hasSingleQuotes(String mystring)
	{
		for (int i=0; i<mystring.length(); i++)
		{
			if (mystring.charAt(i) == '\'')
			{
				return true;
			}
		}

		return false;
	}

	// Returns true if userid is valid
	public static final boolean isValidUserID(String myid)
	{
		if (myid == null)
		{
			return false;
		}
		else if (containsSpecialChars(myid) || hasSingleQuotes(myid) || hasSpaces(myid))
		{
			return false;
		}

		return true;
	}

	// Returns true if password is valid
	public static final boolean isValidPassword(String mypassword)
	{
		if (mypassword == null)
		{
			return false;
		}
		else if (containsSpecialChars(mypassword))
		{
			return false;
		}

		return true;
	}

	// Returns true if all chars in String are digits
	public static final boolean isDigits(String mystring)
	{
		for (int i=0; i < mystring.length(); i++)
		{	if ( !Character.isDigit(mystring.charAt(i)) )
			{	return false;	//something other than digits
			}			
		}
		return true;
	}

	// Returns true if 'mydate' is a valid date
	public static final boolean isValidDate(String mydate)
	{
		try {

			boolean bCheck1 = isValidDate(mydate, '/');
			boolean bCheck2 = isValidDate(mydate, '-');
			if (!bCheck1 && !bCheck2)
				return false;

		} //try
		catch (Exception e) {
			Log.write("Validate.isValidDate() exception caught"+e);
			return false;
		}
		return true;
	}
	
	// Returns true mydate is a valid date
	public static final boolean isValidDate(String mydate, char dl)
	{
		try {

			if(mydate.length() < 10){
				Log.write("Invalid length for date: " + mydate);
				return(false);
			}

			//  Check the mode
			if ((mydate.charAt(2) != dl) || (mydate.charAt(5) != dl))
			{
				return false;
			}

			// Validate the Month
			String strTemp = mydate.substring(0,2);
			if ( !isDigits(strTemp) ) {
				return false;
			}
			int month = Integer.parseInt(strTemp);
			if ((month < 0) || (month > 12))
			{
				return false;
			}

			// Validate the Year
			strTemp = mydate.substring(6);
			if ( !isDigits(strTemp) ) {
				return false;
			}
			int year = Integer.parseInt(strTemp);
			if ((year < EARLIEST_YEAR) || (year > LATEST_YEAR))
			{
				return false;
			}

			// Validate the Day
			strTemp = mydate.substring(3,5);
			if ( !isDigits(strTemp) ) {
				return false;
			}
			int day = Integer.parseInt(strTemp);
			if (day < 1)
			{
				return false;
			}

			switch (month)
			{
				// Check all months with 31 days
				case 1:
				case 3:
				case 5:
				case 7:
				case 8:
				case 10:
				case 12:
					if (day > 31)
					{
						return false;
					}
					break;
				case 4:
				case 6:
				case 9:
				case 11:
					if (day > 30)
					{
						return false;
					}
					break;
				case 2:
					if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0))
					{ // Leap Year !!!!!!
						if (day > 29)
						{ 
							return false;
						}
					}
					else
					{
						if (day > 28)
						{ 
							return false;
						}
					}
			}

		} //try
		catch (Exception e) {
			Log.write("Validate.isValidDate() exception caught"+e);
			return false;
		}

		return true;
	}
	

	// Verifies that the company and ocn selected are valid
	public static final boolean isValidCmpnyOCN(int iCmpny, String strOcn)
	{
		boolean rc = true;

		// Verify valid OCN
		if ((strOcn == null) || (strOcn.length() == 0))
		{
			return false;
		}

		// If OCN is WildCard, then valid
		if (strOcn.equals("*"))
		{
			return true;
		}

		// Validate that the OCN belongs to the Company
		Connection con = null;
		Statement stmt = null;

                try {
			String strQuery = "SELECT OCN_CD FROM OCN_T WHERE OCN_CD = '" + strOcn +
				"' AND CMPNY_SQNC_NMBR = " + iCmpny;

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);
			if (rs.next())
			{
				rc = true;
			}
			else
			{
				rc = false;
			}
		}
		catch(SQLException sqle)
		{	rc = false;
		}
		catch(Exception e)
		{	rc = false;
		}
		finally
		{       DatabaseManager.releaseConnection(con);
		}

		return rc;
	}

	// Returns true if mystring contains mychar
	public static final boolean containsChar(String mystring, char mychar)
	{
		for (int i=0; i<mystring.length(); i++)
		{
			if (mystring.charAt(i) == mychar)
			{
				return true;
			}
		}

		return false;
	}

	// Returns true if ban is valid
	public static final boolean isValidBan(String myban)
	{
		if (myban == null)
		{
			return false;
		}
		else if (containsSpecialChars(myban) || hasSingleQuotes(myban) || hasSpaces(myban))
		{
			return false;
		}

		return true;
	}

}
