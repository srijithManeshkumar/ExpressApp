/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2005
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/**
 * MODULE:	SLAByUserBatchReport.java
 *
 * DESCRIPTION: Batch job which will run SLA by user report
 *
 * AUTHOR:     pjs
 *
 * DATE:       5-20-2005
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

public class SLAByUserBatchReport
{
	public static void main(String[] args) throws Exception
	{
		// Set up DB stuff
		Connection con = null;
		ResultSet rs1 = null;

		// Command line properties
                String strPropFileName = System.getProperty("propfile");
                String strStartDate = System.getProperty("startYYYYMMDD");
                String strEnd= System.getProperty("endYYYYMMDD");
                String strRptName = System.getProperty("rpt");
		String strSuperVisor= System.getProperty("Supervisor");
                String strEmployeeGroup = System.getProperty("Group");

                // get debug argument
                String strDebug = System.getProperty("debug");
                boolean bDebug;
                if (null != strDebug && strDebug.equals("true"))
                        bDebug = true;
                else
                        bDebug = false;

		Properties appProps = new Properties();
		try {
			appProps.load(new FileInputStream(strPropFileName));
		}
		catch (Exception e) {
			System.out.println("SLAByUserBatchReport e="+ e);
			System.out.println("SLAByUserBatchReport Exception return 2");
			System.exit(2);
			return;
		}
		System.out.println("SLAByUserBatchReport Props=" + strPropFileName);

		String driver = appProps.getProperty("lsr.jdbc.driver");
		String sURL = appProps.getProperty("lsr.connection.url");
		String sUser = appProps.getProperty("lsr.db.user");
		String sPswd = appProps.getProperty("lsr.db.pswd");

		java.util.Calendar cal = Calendar.getInstance();
                String strDate = String.valueOf(cal.get(Calendar.MONTH)+1) + "-" +
                                 String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + "-" +
                                 String.valueOf(cal.get(Calendar.YEAR));

		String strOutFileName="";
		//If rpt supplied, use it...else build
		if (strRptName == null)
		{	strOutFileName = "SLAReport-" + strDate + ".htm";
		}
		else
		{	strOutFileName = strRptName;
		}
		File outputFile = new File(strOutFileName);
		FileWriter out = new FileWriter(outputFile);

		Statement stmt1 = null;
		try
		{	// Get Connection to DB
			Class.forName(driver);
			con = DriverManager.getConnection(sURL , sUser, sPswd);
			// Create Statements
			stmt1 = con.createStatement();

		}
		catch (Exception e)
		{
			System.out.println("SLAByUserBatchReport Exception return 3");
			System.exit(3);
			return;
		}
		//If Group passed in, translate group name to seq number
                String[] userIds = new String[1];
                if (strEmployeeGroup != null)
                {
                        try
                        {
                                String strTmp=" SELECT USRGRP_EMP_SQNC_NMBR FROM USERGROUP_EMP_T WHERE USERGROUP_NAME='" +
					 Toolkit.replaceSingleQwithDoubleQ(strEmployeeGroup) + "' ";
                                rs1=stmt1.executeQuery(strTmp);
                                while (rs1.next())
                                {
                                        userIds[0] = rs1.getString(1);
                                }
                                rs1.close();
                        }
                        catch (Exception e)
                        {
                                System.out.println("UserStatisticsBatchReport Exception return 4");
                                System.exit(4);
                                return;
                        }
                }

                System.out.println("UserStatisticsBatchReport Supervisor="+ strSuperVisor + " ReportGrouping=[" +  strEmployeeGroup + "] " +
                        " seq=["+  userIds[0] + "]");

		SLAReportByUserBean slaReport = new SLAReportByUserBean();
		slaReport.setStartDate(strStartDate);
		slaReport.setEndDate(strEnd);
		slaReport.extractEmployeeGroups( con, userIds, false);
		String strReport = slaReport.runReport(con);

		out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><title>ALLTEL Express SLA Report</title>\n");
                //out.write("driver=" + driver + "\n");
                //out.write("URL=" + sURL + "\n");
                //out.write("User=" + sUser + "\n");
                //out.write("Pswd=" + sPswd + "\n");
                //out.write("Report run: " +  strStartDate + " to " + strEnd + "\n");
                out.write("\n\n");
			out.write(strReport);
                out.write("\n\n");
                out.write("<BODY></HTML>");
 		out.close();

		System.exit(0);
		return;
	}
}
