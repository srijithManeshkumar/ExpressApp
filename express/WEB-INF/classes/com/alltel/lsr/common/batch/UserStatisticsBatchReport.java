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
 * MODULE:	UserStatisticsBatchReport.java
 *
 * DESCRIPTION: Batch job which will run User Statistics report for LSRs
 *
 * AUTHOR:
 *
 * DATE:       3-15-2005
 *
 * HISTORY:
 *	pjs 5/-9/2005 Use report groups
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */
package com.alltel.lsr.common.batch;

import java.io.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class UserStatisticsBatchReport
{
	public static void main(String[] args) throws Exception
	{
		// Set up DB stuff
		Connection con = null;
		Statement stmt1 = null;
		ResultSet rs1 = null;

		// Command line properties
                String strPropFileName = System.getProperty("propfile");
                String strStartDate = System.getProperty("startYYYYMMDD");
                String strEnd= System.getProperty("endYYYYMMDD");
                String strRptName = System.getProperty("rpt");
                String strSuperVisor= System.getProperty("Supervisor");
                String strEmployeeGroup = System.getProperty("Group");
		System.out.println("UserStatisticsBatchReport strStartDate="+ strStartDate + " strEnd="+strEnd);

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
			System.out.println("UserStatisticsBatchReport e="+ e);
			System.out.println("UserStatisticsBatchReport Exception return 2");
			System.exit(2);
			return;
		}
		System.out.println("UserStatisticsBatchReport Props=" + strPropFileName);

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
		{	strOutFileName = "UserStatsReport-" + strDate + ".htm";
		}
		else
		{	strOutFileName = strRptName;
		}
		File outputFile = new File(strOutFileName);
		FileWriter out = new FileWriter(outputFile);

		try
		{	// Get Connection to DB
			Class.forName(driver);
			con = DriverManager.getConnection(sURL , sUser, sPswd);
			// Create Statements
			stmt1 = con.createStatement();

		}
		catch (Exception e)
		{
			System.out.println("UserStatisticsBatchReport Exception return 3");
			System.exit(3);
			return;
		}
		//If Group passed in, translate group name to seq number
		String[] userIds = new String[1];
		if (strEmployeeGroup != null)
		{
			try
			{
				String strTmp=" SELECT USRGRP_EMP_SQNC_NMBR FROM USERGROUP_EMP_T WHERE USERGROUP_NAME='" + Toolkit.replaceSingleQwithDoubleQ(strEmployeeGroup) + "' ";
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
		if ( strSuperVisor.equals("Holt") )
		{	//userIds = new String[] {"e0078256","e0058281","e0034987","e0077962","e0084937","e0084432","e0055968","e0056511","e0049315","e0056766"};
		}
		else if ( strSuperVisor.equals("Woodall") )
		{	//userIds = new String[] {"e0077961","e0075157","e0059734","e0096056","e0059828","e0059836","e0072480","e0059859","e0059830","e0056768","e0082481"};
		}
		else if ( strSuperVisor.equals("Weiland") )
		{	//userIds = new String[] {"e0072671","e0070336","e0073611","e0078255","e0072482","e0056842","e0093408","e0085866","e0059824","e0070243","e0100649"};
		}

		UserReportBean userStatsRpt = new UserReportBean();
		userStatsRpt.setStartDate(strStartDate);
		userStatsRpt.setEndDate(strEnd);
		userStatsRpt.setKeepWeekends(false);
	        userStatsRpt.setCountWeekends(false);
	        //userStatsRpt.setUserids(userIds);
userStatsRpt.extractEmployeeGroups( con, userIds, false);

		String strReport = userStatsRpt.runReport(con);

		out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><title>ALLTEL Express User Statistics Report</title>\n");
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
