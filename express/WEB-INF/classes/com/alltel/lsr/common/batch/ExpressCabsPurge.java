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
 * MODULE:	ExpressCabsPurge.java
 *
 * DESCRIPTION: Batch job which will purge the BAN_REPORT_T table
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        06-01-2002
 *
 * HISTORY:
 *	02-13-2003 psedlak	Purge criteria (LD_DT) in wrong order (HD 292509) resulting in
 *				data just loaded being purged. Change SELECT stmt to use date
 *				stuff to put in chrono order.
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */
package com.alltel.lsr.common.batch;

import java.io.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class ExpressCabsPurge
{
	public static void main(String[] args)
	{
		String strURL = "";
		String strDbUserid = "";
		String strDbPassword = "";
		String strBanFile = "";
		String strBan = "";
		int iNumToKeep = 0;

		// Set up DB stuff
		Connection con = null;
		ResultSet rs1 = null;

		Vector vBans = new Vector();

		// If 4 parms passed, purge whole table.
		// If 5 parms passed, purge only those bans passed in file
		if (args.length == 4)
		{
			strURL = args[0];
			strDbUserid = args[1];
			strDbPassword = args[2];
			iNumToKeep = Integer.parseInt(args[3]);
		}
		else if (args.length == 5)
		{
			strURL = args[0];
			strDbUserid = args[1];
			strDbPassword = args[2];
			iNumToKeep = Integer.parseInt(args[3]);
			strBanFile = args[4];
		}
		else
		{
			System.out.println("Usage: ExpressCabsPurge <URL> <Userid> <Password> <number of dates to keep> [ Ban File ]");
			System.exit(1);
			return;
		}

		// Define the Prepared Statements
		//hd 292509 String strGetBanDates = "SELECT DISTINCT LD_DT FROM BAN_REPORT_T WHERE BAN_REPORT_T.BAN = ? ORDER BY LD_DT DESC";
		String strGetBanDates = "SELECT DISTINCT TO_CHAR(TO_DATE(LD_DT,'MM-DD-YYYY'),'YYYYMMDD'), LD_DT " +
					" FROM BAN_REPORT_T WHERE BAN_REPORT_T.BAN = ? ORDER BY 1 DESC";

		String strDeleteBan = "DELETE BAN_REPORT_T WHERE BAN = ? AND LD_DT = ?";

		try
		{
			// Get Connection to DB
			Class.forName("oracle.jdbc.OracleDriver");
			con = DriverManager.getConnection(strURL, strDbUserid, strDbPassword);

			// Create Statements
			Statement stmt1 = con.createStatement();
			PreparedStatement pGetStmt = con.prepareStatement(strGetBanDates);
			PreparedStatement pDelStmt = con.prepareStatement(strDeleteBan);

			// Build Vector of all BANs to process
			// If strBanFile, then read in file to build Vector
			// If no strBanFile, then read all BANS from BAN_T
			if (strBanFile.length() > 0)
			{
				String strRow = "";

				// Open Input File and read BANs into the Vector
				ExpressInputFile in = new ExpressInputFile(strBanFile);
				while ((strBan = in.getLine()) != null)
				{
					String strStoreBan = new String(strBan);
					strStoreBan = strStoreBan.replace(' ','_');
					vBans.addElement(strStoreBan);
				}
				in.cleanup();
			}
			else
			{
				// Retrieve all BANs from BAN_T
				String strGetBans = "SELECT BAN FROM BAN_T";

				rs1 = stmt1.executeQuery(strGetBans);

				// Put BANs in Vector
				while (rs1.next())
				{
					String strStoreBan = new String(rs1.getString("BAN"));
					strStoreBan = strStoreBan.replace(' ','_');
					vBans.addElement(strStoreBan);
				}
				rs1.close();
			}

			// Now that we have our Vector, we can build our deletes
			Iterator it = vBans.iterator();
			while (it.hasNext())
			{
				strBan = (String)it.next();

				// Get the dates to delete for this BAN
				pGetStmt.setString(1,strBan);
				rs1 = pGetStmt.executeQuery();

				int iCounter = 1;
				while(rs1.next())
				{
					//System.out.println("BAN purge check: " + strBan + " " + rs1.getString(1) + " " + rs1.getString(2) );
					if (iCounter++ <= iNumToKeep)
						continue;
					//System.out.println("Its getting purged");

					// Build the Delete for this BAN and Date
					pDelStmt.setString(1,strBan);
					pDelStmt.setString(2,rs1.getString(2));
					pDelStmt.executeQuery();
				}
				rs1.close();
			}

			// Clean up and Close
			pGetStmt.close();
			pGetStmt = null;
			pDelStmt.close();
			pDelStmt = null;
			stmt1.close();
			stmt1 = null;
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
