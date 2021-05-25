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
 * MODULE:	SLABatchReport.java
 *
 * DESCRIPTION: QUICK FIX ....Batch job which will run SLA report
 *
 * AUTHOR:
 *
 * DATE:       2-25-2005
 *
 * HISTORY:
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */
//package com.alltel.lsr.common.batch;

import java.io.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class SLABatchReport
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
			System.out.println("SLABatchReport e="+ e);
			System.out.println("SLABatchReport Exception return 2");
			System.exit(2);
			return;
		}
		System.out.println("SLABatchReport Props=" + strPropFileName);

		String driver = appProps.getProperty("lsr.jdbc.driver");
		String sURL = appProps.getProperty("lsr.connection.url");
		String sUser = appProps.getProperty("lsr.db.user");
		String sPswd = appProps.getProperty("lsr.db.pswd");

                System.out.println("driver=" + driver + "URL=" + sURL + "User=" + sUser + "Pswd=" + sPswd);

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

		try
		{	// Get Connection to DB
			Class.forName(driver);
			con = DriverManager.getConnection(sURL , sUser, sPswd);
			// Create Statements
			Statement stmt1 = con.createStatement();

		}
		catch (Exception e)
		{
			System.out.println("SLABatchReport Exception return 3");
			System.exit(3);
			return;
		}

		SLABatchReportBean slaBatchReport = new SLABatchReportBean();
		slaBatchReport.setStartDate(strStartDate);
		slaBatchReport.setEndDate(strEnd);

		String strReport = slaBatchReport.runReport(con);


		out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><title>ALLTEL Express SLA Report</title>\n");
//                out.write("driver=" + driver + "\n");
//                out.write("URL=" + sURL + "\n");
//                out.write("User=" + sUser + "\n");
//                out.write("Pswd=" + sPswd + "\n");
//                out.write("Report run: " +  strStartDate + " to " + strEnd + "\n");
                out.write("\n\n");
			out.write(strReport);
                out.write("\n\n");
                out.write("<BODY></HTML>");
 		out.close();

		System.exit(0);
		return;
	}
}
