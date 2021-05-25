/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/**
 * MODULE:		AutoSttsChng.java
 *
 * DESCRIPTION: Batch job which will use ACTION_T to automatically change the
 *		status of aged requests.
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        02-15-2002
 *
 * HISTORY:
 *	10/22/2002  pjs fixed DSL history update and added trbl tickets.
 *	11/05/2002  pjs Changed hstry record update to not change the userid. This is
 *			fallout from HDR 165254.
 *	10/04/2003  pjs added BillDisputes
 *	10/13/2003  pjs Purge Detail history records on closure --status chg records
 *			are not purged.
 *	7/19/2004	pjs added DsTicket's
 *	10/19/2004	pjs added Data Work Orders
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */

/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/BATCH/AutoSttsChng.java  $
/*
/*   Rev 1.7   Jul 19 2004 15:11:56   e0069884
/*
/*
/*   Rev 1.5   Oct 06 2003 12:27:22   e0069884
/*
/*   Rev 1.3   Oct 22 2002 16:37:44   e0069884
/*
/*
/*   Rev 1.2   Jun 14 2002 09:19:38   dmartz
/*
/*
/*   Rev 1.0   13 Feb 2002 15:34:56   dmartz
/*Release 1.1
/*
*/
/* $Revision:   1.7  $
*/

package com.alltel.lsr.common.batch;
import java.io.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class AutoSttsChng
{
	// Get Statuses from ACTION_T
	static Connection con = null;
	static Statement stmt1 = null;
	static Statement stmt2 = null;
	static Statement stmt3 = null;
	static ResultSet rs1 = null;
	static ResultSet rs2 = null;
	static ResultSet rs3 = null;

	static RequestOrder rOrder = null;
	static DslOrder dOrder = null;
	static BillDisputeOrder bdOrder = null;
	static TicketOrder tOrder = null;
	static PreorderOrder poOrder = null;
	static DsTicketOrder dsOrder = null;
	static DwoOrder dwOrder = null;
	static DwoOrder dwOrder2 = null;

	static String strTypInd = "";
	static String strSttsCdTo = "";
	static String strSttsCdFrom = "";
	static String strActnExprtnDys = "";

	static String strUserid = "";

	static int m_iRows = 0;
	static int m_iDtlRows = 0;

	public static void main(String[] args)
	{
		AutoSttsChng thisBatch = new AutoSttsChng();

		// Retrieve Parameters from command line
		String strURL = args[0];
		String strDbUserid = args[1];
		String strDbPassword = args[2];
		strUserid = args[3];

		try
		{
			Class.forName("oracle.jdbc.OracleDriver");
			con = DriverManager.getConnection(strURL, strDbUserid, strDbPassword);

			rOrder = RequestOrder.getInstance(con);
			dOrder = DslOrder.getInstance(con);
			bdOrder = BillDisputeOrder.getInstance(con);
			tOrder = TicketOrder.getInstance(con);
			poOrder = PreorderOrder.getInstance(con);
			dsOrder = DsTicketOrder.getInstance(con);
			dwOrder = DwoOrder.getInstance("W", con);	// KPEN
			dwOrder2 = DwoOrder.getInstance("X", con);	// BDP

			// Turn off Auto Commit
			con.setAutoCommit(false);

			// Create Statements
			stmt1 = con.createStatement();
			stmt2 = con.createStatement();
			stmt3 = con.createStatement();

			// Retrieve all Status Codes where expiration days is greater than 0
			String strQuery = "SELECT STTS_CD_FROM, STTS_CD_TO, ACTN_EXPRTN_DYS, TYP_IND FROM ACTION_T WHERE ACTN_EXPRTN_DYS > 0";

			rs1 = stmt1.executeQuery(strQuery);

			// Loop thru each STTS_CD_FROM
			while (rs1.next())
			{
				// What are we processing ?
				strTypInd = rs1.getString("TYP_IND");
				strSttsCdFrom = rs1.getString("STTS_CD_FROM");
				strSttsCdTo = rs1.getString("STTS_CD_TO");
				strActnExprtnDys = rs1.getString("ACTN_EXPRTN_DYS");
				if (strTypInd.equals("R"))
				{
					processOrder(rOrder, strSttsCdFrom, strSttsCdTo, strActnExprtnDys);
				}
				else if (strTypInd.equals("P"))
				{
					processOrder(poOrder, strSttsCdFrom, strSttsCdTo, strActnExprtnDys);
				}
				else if (strTypInd.equals("D"))
				{
					processOrder(dOrder, strSttsCdFrom, strSttsCdTo, strActnExprtnDys);
				}
				else if (strTypInd.equals("T"))
				{
					processOrder(tOrder, strSttsCdFrom, strSttsCdTo, strActnExprtnDys);
				}
				else if (strTypInd.equals("B"))
				{
					processOrder(bdOrder, strSttsCdFrom, strSttsCdTo, strActnExprtnDys);
				}
				else if (strTypInd.equals("S"))
				{
					processOrder(dsOrder, strSttsCdFrom, strSttsCdTo, strActnExprtnDys);
				}
				else if (strTypInd.equals("W"))
				{
					processOrder(dwOrder, strSttsCdFrom, strSttsCdTo, strActnExprtnDys);
				}
				else if (strTypInd.equals("X"))
				{
					processOrder(dwOrder2, strSttsCdFrom, strSttsCdTo, strActnExprtnDys);
				}
				else
				{
					System.out.println("Unknown Type Indicator: " + strTypInd);
				}

			}

			// Clean up and Close
			stmt3.close();
			stmt3 = null;
			rs2.close();
			rs2 = null;
			stmt2.close();
			stmt2 = null;
			rs1.close();
			rs1 = null;
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
				return;
			}
			e.printStackTrace();
			return;
		}
		System.out.println("\nHistory purge summary: Hist: "+ m_iRows + "  Dtl rows: " + m_iDtlRows + "\n");

		return;
	}


	//
	// Added new generic method for all order types
	//
	public static void processOrder(ExpressOrder thisOrder, String strSttsCdFrom, String strSttsCdTo, String strExDys)
		throws Exception
	{
		System.out.println("ORDERS Type[" + thisOrder.getTYP_IND() + "] - From: " + strSttsCdFrom +
			"  To: " + strSttsCdTo + "  After: " + strExDys + " Days");

		// Retrieve all orders that have a status of STTS_CD_FROM and whose last mdfd_dt > ACTN_EXPRTN_DYS
		String strQuery = "SELECT " + thisOrder.getSQNC_COLUMN() + " as SQNC, " + thisOrder.getVRSN_COLUMN() + " as VRSN, " +
			thisOrder.getAttribute("HSTRY_COLUMN") + " as HSTRY_SQNC_NMBR FROM " + thisOrder.getTBL_NAME() +
			" WHERE " + thisOrder.getAttribute("STTS_COLUMN") +
			" = '" + strSttsCdFrom + "' AND (sysdate - MDFD_DT) > " + strExDys;
		//System.out.println("Query = " + strQuery );

		rs2 = stmt2.executeQuery(strQuery);
		String strSeq = "";
		String strVrsn = "";
		// Loop thru each seq #
		while (rs2.next())
		{
			// Obtain the current system date
			rs3 = stmt3.executeQuery("SELECT TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI:SS') CURR_DT FROM dual");
			rs3.next();
			String strCurrDt = rs3.getString("CURR_DT");
			rs3.close();

			// Build the Update Statement
			strSeq = rs2.getString("SQNC");
			strVrsn = rs2.getString("VRSN");
			System.out.println("SQNC_NMBR: " + strSeq);

			// Update Current History Row
			String strUpdHst = "UPDATE " + thisOrder.getAttribute("HSTRY_TBL_NAME") + "  SET " +
				thisOrder.getAttribute("HSTRY_STTS_CD_OUT") + " = '" +
				strSttsCdTo + "', " + thisOrder.getAttribute("HSTRY_DT_OUT") + " = TO_DATE('" + strCurrDt +
				"','MM/DD/YYYY HH24:MI:SS'), MDFD_DT = TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS') " +
				" WHERE " + thisOrder.getAttribute("HSTRY_SQNC_COLUMN") + " = " + rs2.getInt("HSTRY_SQNC_NMBR");
			//System.out.println("update =[" + strUpdHst +"]");

			try
			{
				if (stmt3.executeUpdate(strUpdHst) != 1)
				{
					System.out.println("The following UPDATE failed: " + strUpdHst);
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				try
				{
					con.rollback();
				}
				catch(Exception se)
				{
					se.printStackTrace();
					return;
				}
				e.printStackTrace();
				continue;
			}

			// Get next History Sequence Number
			String strHstQry = "SELECT " + thisOrder.getAttribute("HSTRY_SEQUENCE") + ".nextval NEXT_SQNC_NMBR FROM dual";
			rs3 = stmt3.executeQuery(strHstQry);
			rs3.next();
			int iHstrySqncNmbrNew = rs3.getInt("NEXT_SQNC_NMBR");
			rs3.close();

			// Insert New History Row
			String strInsHst = "INSERT INTO " + thisOrder.getAttribute("HSTRY_TBL_NAME") +
				" ("+thisOrder.getAttribute("HSTRY_SQNC_COLUMN")+","+
				thisOrder.getSQNC_COLUMN()+","+
				thisOrder.getVRSN_COLUMN()+","+
				thisOrder.getAttribute("HSTRY_STTS_CD_IN")+","+
				thisOrder.getAttribute("HSTRY_STTS_CD_OUT")+","+
				thisOrder.getAttribute("HSTRY_DT_IN")+","+
				thisOrder.getAttribute("HSTRY_DT_OUT")+","+
				" MDFD_DT, MDFD_USERID) " +
				" VALUES(" + iHstrySqncNmbrNew + ", " + strSeq + ", " + strVrsn + ",'" +
				strSttsCdTo + "', 'N/A', TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), " +
				" TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), TO_DATE('" +
				strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), '" + strUserid + "')" ;
			//System.out.println("new insert =[" + strInsHst +"]");
			try
			{
				if (stmt3.executeUpdate(strInsHst) != 1)
				{
					System.out.println("The following INSERT failed: " + strInsHst);
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				try
				{
					con.rollback();
				}
				catch(Exception se)
				{
					se.printStackTrace();
					return;
				}
				e.printStackTrace();
				continue;
			}

			// Update main order table now
			String strUpdRqst = "UPDATE " + thisOrder.getTBL_NAME() + " SET " + thisOrder.getAttribute("STTS_COLUMN") +
				" = '" + strSttsCdTo + "', " +  thisOrder.getAttribute("HSTRY_COLUMN") +  " = " + iHstrySqncNmbrNew +
				", MDFD_DT = TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), MDFD_USERID = '" +
				strUserid + "' WHERE " + thisOrder.getSQNC_COLUMN() + " = " + strSeq;

			//System.out.println("old upd=[" + strUpdRqst +"]");
			try
			{
				if (stmt3.executeUpdate(strUpdRqst) != 1)
				{
					System.out.println("The following UPDATE failed: " + strUpdRqst);
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				try
				{
					con.rollback();
				}
				catch(Exception se)
				{
					se.printStackTrace();
					return;
				}
				e.printStackTrace();
				continue;
			}
			con.commit();

			//We need to purge the order detail history now...
			if ( strSttsCdTo.equals("CLOSED") )
			{
				int iRows = 0;
				int iDtlRows = 0;
				String strWhere = "  WHERE O.TYP_IND='" + thisOrder.getTYP_IND() + "' AND O.SQNC_NMBR="+ strSeq;

				strUpdRqst = "DELETE FROM ORDER_DETAIL_HISTORY_T OD WHERE OD.DTL_HSTRY_SQNC_NMBR IN  " +
					"(SELECT O.DTL_HSTRY_SQNC_NMBR FROM ORDER_HISTORY_T O " + strWhere + ")";
				try {
					iDtlRows = stmt3.executeUpdate(strUpdRqst);
//				System.out.println(" " + thisOrder.getTYP_IND() + " " + strSeq + " purged:" + iDtlRows);
					m_iDtlRows += iDtlRows;
				}
				catch (Exception e) {
					System.out.println("The following stmt failed =["+strUpdRqst+"]");
					e.printStackTrace();
					continue;
				}
				try {
					strUpdRqst = "DELETE FROM ORDER_HISTORY_T O " + strWhere;
					iRows = stmt3.executeUpdate(strUpdRqst);
//				System.out.println(" " + thisOrder.getTYP_IND() + " " + strSeq + " purged:" + iRows);
					m_iRows += iRows;
				}
				catch (Exception e) {
					System.out.println("The following stmt failed =["+strUpdRqst+"]");
					e.printStackTrace();
					continue;
				}
				System.out.println(" " + thisOrder.getTYP_IND() + " " + strSeq + " purged:" + iRows +
					"  Dtl: " + iDtlRows);

			}
			con.commit();
		}
	}


}
