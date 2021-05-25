package com.alltel.lsr.common.batch;

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
 * AUTHOR:    EK
 * 
 * DATE:       2-6-2006
 * 
 * HISTORY:
 *
 */

import java.io.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class RunUserDisputeReport
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
			System.out.println("RunUserDisputeReport e="+ e);
			System.out.println("RunUserDisputeReport Exception return 2");
			System.exit(2);
			return;
		}
		System.out.println("RunUserDisputeReport Props=" + strPropFileName);
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
		{	strOutFileName = "runUserDispute" + strDate + ".htm";
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
			System.out.println("RunUserDisputeReport Exception return 3");
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
	            System.out.println("RunUserDisputeReport Exception return 4");
	            System.exit(4);
	            return;
		    }
		}
		
		System.out.println("RunUserDisputeReport Supervisor="+ strSuperVisor + " ReportGrouping=[" +  strEmployeeGroup + "] " +
		    " seq=["+  userIds[0] + "]");
		
		UserReportDisputeBean report = new UserReportDisputeBean();
		report.setStartDate(strStartDate);
		report.setEndDate(strEnd);
		report.extractEmployeeGroups( con, userIds, false);
		String strReport = report.runReport(con);

		out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html>" );
		out.write("<HEAD>" );
		out.write("<STYLE TYPE=\"text/css\">");
		out.write( "BODY {font-family: \"arial\"; background: #ffeed0; font-size: 8pt}" );
		out.write( "P {font-family: \"arial\";Font-size: 8pt}" );
		out.write( " TH {font-family: \"arial\";Font-size: 8pt}" );
		out.write( " TD {font-family: \"arial\";Font-size: 8pt}" );
		out.write( " A {font-family: \"arial\";Font-size: 8pt;Font-weight:bold;text-decoration:none;Color: #336699}" );
		out.write( " a:hover{font-family: \"arial\";Font-size: 8pt;Font-weight:bold;text-decoration:none;Color: #ff0000}" );
		out.write( "SPAN.header1 {color:#336699;Font-family: \"arial\";Font-weight:bold;text-align:center;Font-size:14pt}" );
		out.write("</STYLE>" );
		out.write("<title>ALLTEL Express Billing  Disputes User Statistics  Report </title>\n");
		out.write("</HEAD>" ); 
                //out.write("driver=" + driver + "\n");
                //out.write("URL=" + sURL + "\n");
                //out.write("User=" + sUser + "\n");
                //out.write("Pswd=" + sPswd + "\n");
                //out.write("Report run: " +  strStartDate + " to " + strEnd + "\n");
                out.write("\n\n");
			out.write(strReport);
                out.write("\n\n");
              out.write( ExpressUtil.readFile("batchJavaScript.js" ));
                out.write("<BODY></HTML>");
 		out.close();

		System.exit(0);
		return;
	}
}
