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
 * MODULE:	AutoUnlockRequest.java
 *
 * DESCRIPTION: This program is run via a batch script. It will free requests that have
 *      been locked for more than xx minutes.  This time frame coincides with the
 *      application session timeout value.
 *
 * AUTHOR:      psedlak
 *
 * DATE:        02-12-2002
 *
 * HISTORY:
 *	xx/xx/2002  initial check-in.
 *	08/15/2002  psedlak Adding DSL orders, TTs and Preorders by removing TYP_IND check in query.
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/BATCH/AutoUnlockRequest.java  $
/*
/*   Rev 1.1   22 Mar 2002 10:12:14   dmartz
/*Merged ACTION and LOCK tables
/*
/*   Rev 1.0   12 Feb 2002 15:16:56   sedlak
/*release 1.1
/*
*/
/* $Revision:   1.1  $
*/
package com.alltel.lsr.common.batch;

import java.io.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class AutoUnlockRequest
{
	private final static int UNLOCK_AFTER_MINUTES = 45;

	public static void main(String[] args)
	{
		final String strQuery = "SELECT SQNC_NMBR, USERID, TO_CHAR(LCK_DT, 'MMDDYYYY HH24MISS'), TYP_IND " +
			" FROM LOCK_T WHERE (SYSDATE - LCK_DT)*24*60 > ?";
		final String strDeleteSQL = "DELETE FROM LOCK_T WHERE SQNC_NMBR = ? AND TYP_IND = ? AND USERID = ? ";

		Connection 	con = null;
		int		iSqncNmbr = 0;
		String		strUserID = "";
		String		strLockDateTime = "";
		String		strType = "";	//ie. R, D, P, T
		int		iCount = 0;

		// Retrieve Parameters from command line
		String strURL = args[0];
		String strDbUserid = args[1];
		String strDbPassword = args[2];
		System.out.println("JDBC = " + strURL);


		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection(strURL, strDbUserid, strDbPassword);

			// Turn off Auto Commit
			con.setAutoCommit(false);

			PreparedStatement pstmt  = con.prepareStatement(strQuery);
			PreparedStatement pstmt2 = con.prepareStatement(strDeleteSQL);
			pstmt.setInt(1, UNLOCK_AFTER_MINUTES);	//can get minutes from properties file later

			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{	iCount++;
				iSqncNmbr = rs.getInt("SQNC_NMBR");
				strUserID = rs.getString("USERID");
				strLockDateTime = rs.getString(3);
				strType = rs.getString("TYP_IND");

				System.out.println("Unlock: " + strType + ":" + iSqncNmbr + " held by " + strUserID + " since " + strLockDateTime);
				pstmt2.setInt(1, iSqncNmbr);
				pstmt2.setString(2, strType);
				pstmt2.setString(3, strUserID);
				try {
					pstmt2.executeUpdate();
				}
				catch (SQLException e) {
					System.out.println("Didnt unlock " + strType + ":" + iSqncNmbr);
				}
				con.commit();
			}

			con.close();
			System.out.println("Requests unlocked = " + iCount);
		}
		catch (Exception e)
		{
			try
			{
				con.rollback();
				con.close();
			}
			catch(Exception se)
			{
				se.printStackTrace();
				return;
			}
			e.printStackTrace();
			return;
		}
		return;
	}
}
