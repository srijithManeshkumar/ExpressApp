/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/**
 * MODULE:		DirectoryListPurge.java
 *
 * DESCRIPTION: Batch job which will purge the DIRECTORY_LIST_T table
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        06-01-2002
 *
 * HISTORY:
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */
package com.alltel.lsr.common.batch;

import java.io.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class DirectoryListPurge
{
	public static void main(String[] args)
	{
		String strURL = "";
		String strDbUserid = "";
		String strDbPassword = "";
		String strPurgeFile = "";
		String strRow = "";
		String strBillTn = "";

		// Set up DB stuff
		Connection con = null;

		// Retrieve Parameters from command line
		if (args.length == 4)
		{
			strURL = args[0];
			strDbUserid = args[1];
			strDbPassword = args[2];
			strPurgeFile = args[3];
			System.out.println("DirectoryListPurge args [" + strURL + "] ["+ strDbUserid + "] ["+ strDbPassword +"] [" + strPurgeFile + "]");
		}
		else
		{
			System.out.println("Usage: DirectoryListPurge <URL> <Userid> <Password> <Purge file>");
			System.exit(1);
			return;
		}

		// Define the Prepared Statements
		String strDeleteDL = "DELETE DIRECTORY_LIST_T WHERE DL_BILL_TN = ?";

		try
		{
			// Get Connection to DB
			Class.forName("oracle.jdbc.OracleDriver");
			con = DriverManager.getConnection(strURL, strDbUserid, strDbPassword);
			if (con == null)
			{	System.out.println("DirectoryListPurge DB conn is null");
				System.exit(1);
				return;
			}
			// Create Statements
			PreparedStatement pDelStmt = con.prepareStatement(strDeleteDL);

			// Purge rows
			strRow = "";

			// Open Input File and read DLs into the Vector
			ExpressInputFile in = new ExpressInputFile(strPurgeFile);
			while ((strRow = in.getLine()) != null)
			{
				strBillTn = strRow.substring(0,10);
				//System.out.println(" ..."+strBillTn+"..");

				// Build the Delete for this DL
				pDelStmt.setString(1,strBillTn);
				pDelStmt.executeQuery();
			}
			in.cleanup();

			// Clean up and Close
			pDelStmt.close();
			pDelStmt = null;
			con.close();
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
				System.exit(2);
				return;
			}
			e.printStackTrace();
			System.exit(3);
			return;
		}
		return;
	}
}
