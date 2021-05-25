/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL INFORMATION SERVICES
 */

/* 
 * MODULE:		Holidays.java
 * 
 * DESCRIPTION: Singleton class to hold Express holidays
 * 
 * AUTHOR:      Paul Sedlak
 * 
 * DATE:        01-31-2002
 * 
 * HISTORY:
 *	1/31/2002  initial check-in.
 *	6/6/2005 pjs overload to use from batch. Also made the constructors private (this is supposed to be Singleton)
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/Archives/express/JAVA/Object/Holidays.java  $
/*
/*   Rev 1.2   Sep 05 2002 13:13:40   sedlak
/*comment out "log" writes
/*
/*   Rev 1.1   31 Jan 2002 08:42:20   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:50   wwoods
/*Initial Checkin
*/

/* $Revision:   1.2  $
*/

package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.sql.*;

import com.alltel.lsr.common.util.*;

/**
 * This class is a Singleton - and holds the static Holiday information that
 * is retrieved from HOLIDAY_T tables.
 */
public class Holidays
{
	public static Holidays m_instance;
	
	private Hashtable m_hashHoliday;
	
	private Holidays()
	{
		m_hashHoliday = new Hashtable();
		
		Connection con = null;
		Statement stmt = null;
		try {
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			String strQuery = "SELECT DISTINCT TO_CHAR(HLDY_DT, 'YYYYMMDD'), HLDY_DSCRPTN " +
				"FROM HOLIDAY_T ORDER BY 1";
			ResultSet rs = stmt.executeQuery(strQuery);
			while (rs.next())
			{	
				String hKey = rs.getString(1);
				m_hashHoliday.put(hKey, rs.getString("HLDY_DSCRPTN"));
				//Log.write(Log.DEBUG, "Holidays() Added Holiday=" + hKey);
			}
		}
		catch (Exception e)
		{
			//Log.write(Log.ERROR, "Holidays() trapped exception");
		}
		finally
		{
			DatabaseManager.releaseConnection(con);
		}
	}

	/** 
	 * For use from batch 
	 */
	private Holidays(Connection dbConn)
	{
		m_hashHoliday = new Hashtable();
		
		Statement stmt = null;
		try {
			//dbConn = DatabaseManager.getConnection();
			stmt = dbConn.createStatement();
			String strQuery = "SELECT DISTINCT TO_CHAR(HLDY_DT, 'YYYYMMDD'), HLDY_DSCRPTN " +
				"FROM HOLIDAY_T ORDER BY 1";
			ResultSet rs = stmt.executeQuery(strQuery);
			while (rs.next())
			{	
				String hKey = rs.getString(1);
				m_hashHoliday.put(hKey, rs.getString("HLDY_DSCRPTN"));
				//Log.write(Log.DEBUG, "Holidays() Added Holiday=" + hKey);
			}
		}
		catch (Exception e)
		{
			//Log.write(Log.ERROR, "Holidays(dbConn) trapped exception");
			System.out.println("Holidays(conn) exception encoutnered e=["+e+"]");
		}
		finally
		{
			//DatabaseManager.releaseConnection(con);
		}
	}
	
	/** 
	 * Returns true if given date is a holiday
	 */
	public boolean isHoliday(String strYYYYMMDD)
	{
		boolean bHol = false;
		//Log.write(Log.DEBUG_VERBOSE, "HDY: Searching for " + strYYYYMMDD);
		String strDesc = (String)m_hashHoliday.get(strYYYYMMDD);
		if (strDesc != null)
		{
			//Log.write(Log.DEBUG_VERBOSE, "HDY: Found = " + strDesc);
			bHol = true;
		}
		
		return bHol;
	}

	/** 
	 * Get the single instance of this object - or create the first one.
	 */
	public static Holidays getInstance()
	{
		if (m_instance == null)
			m_instance = new Holidays();
		
		return m_instance;
	}

	public static Holidays getInstance(Connection dbConn)
	{
		if (m_instance == null)
			m_instance = new Holidays(dbConn);
		
		return m_instance;
	}
}
