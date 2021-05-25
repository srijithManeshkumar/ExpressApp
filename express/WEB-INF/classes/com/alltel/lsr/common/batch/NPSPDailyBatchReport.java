/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2016
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/**
 * MODULE:	NPSPDailyBatchReport.java
 *
 * DESCRIPTION: Batch job which will run NP and SP Daily report for LSRs
 *
 * AUTHOR:
 *
 * DATE:       12-22-2016
 *
 * HISTORY:
 *  12/22/2016 - Initial Creation for NP and SP Daily Report.
 *
 */
package com.alltel.lsr.common.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import com.alltel.lsr.common.util.DatabaseManager;

public class NPSPDailyBatchReport {
	
	private static final String TAB_SEPARATOR 	= "\t";
	private static final String LINE_SEPARATOR	= System.lineSeparator();
	
	public static void main(String[] args) throws SQLException, Exception {
		
		// Set up DB stuff
		Connection con = null;

		// Command line properties
		String strPropFileName = System.getProperty("propfile");
		String strStartDate = System.getProperty("startYYYYMMDD");
		String strEndDate= System.getProperty("endYYYYMMDD");
		String strRptName = System.getProperty("rpt");
		
		System.out.println("NPSPDailyBatchReport strStartDate="+ strStartDate + " strEndDate="+strEndDate);

		Properties appProps = new Properties();
		try {
			 appProps.load(new FileInputStream(strPropFileName));
		 }
		 catch (Exception e) {
			 
			System.out.println("NPSPDailyBatchReport e="+ e);
			System.out.println("NPSPDailyBatchReport Exception return 2");
			System.exit(2);
		}
			System.out.println("NPSPDailyBatchReport Props=" + strPropFileName);

			String driver = appProps.getProperty("lsr.jdbc.driver");
			String sURL = appProps.getProperty("lsr.connection.url");
			String sUser = appProps.getProperty("lsr.db.user");
			String sPswd = appProps.getProperty("lsr.db.pswd");

			System.out.println("NPSPDailyBatchReport rpt=" + strRptName);
			
			File outputFile = new File(strRptName);
			FileWriter out = new FileWriter(outputFile);

			try
			{	
				// Get Connection to DB
				Class.forName(driver);
				con = DriverManager.getConnection(sURL , sUser, sPswd);
			}
			catch (Exception e)
			{
				System.out.println("NPSPDailyBatchReport Exception return 3");
				System.exit(3);
			}
			
			// Get the report details as string
			String strReport = runReport(con, strStartDate, strEndDate);
			
			// Write the report details in output file
		    out.write(strReport);
		    
		 	out.close();

			System.exit(0);
		}
	
		public static String runReport(Connection con, String strStartDate, String strEndDate) throws SQLException{
			
			StringBuilder strBuild = new StringBuilder();
			
			String DATE_FORMAT1 = "MM-dd-yyyy";

			String DATE_FORMAT2 = "yyyyMMdd";

			DateFormat sdf1 = new SimpleDateFormat(DATE_FORMAT1);

			DateFormat sdf2 = new SimpleDateFormat(DATE_FORMAT2);
			
			Statement stmt = null;
			Statement stmt1 = null;
			ResultSet rs= null;
			ResultSet rs1= null;
			Statement stmt2 = null;
			Statement stmt3 = null;
			Statement stmt4 = null;
			Statement stmt5 = null;
			ResultSet rs2= null;
			ResultSet rs3= null;
			ResultSet rs4= null;
			ResultSet rs5= null;
			
			try {
				stmt = con.createStatement();
				stmt1 = con.createStatement();
				stmt2 = con.createStatement();
				stmt3 = con.createStatement();
				stmt4 = con.createStatement();
				stmt5 = con.createStatement();
	
				strBuild.append("Bus/Res");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("CLEC");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Customer Name");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Customer Address");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("City");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("State");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Zip");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Porting");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Disconnecting");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Staying");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Broadband");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Video");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Greenfield");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Order numbers");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("PON");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("FOCD");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("SRD");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("DFDT");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("CHC");
				strBuild.append(TAB_SEPARATOR);
	
				strBuild.append("Remarks");
				strBuild.append(TAB_SEPARATOR);
			
				strBuild.append("NP Remarks");
				strBuild.append(TAB_SEPARATOR);
			                
				strBuild.append("Updates");
	
				String strQry = "SELECT t2.cus_typ AS CusTyp, t8.ci_clec AS CLEC, t3.eu_la_name AS CusName, t3.eu_la_sano AS SANO, " +
				                "t3.eu_la_sasn AS SASN, t3.eu_la_sath AS SATH, t3.eu_la_sass AS SASS, t3.eu_la_ld1 AS LD1, t3.eu_la_lv1 AS LV1, " +
				                "t3.eu_la_city AS CITY, t2.ocn_stt AS ST, t3.eu_la_zip AS ZIP, t8.ci_stay AS STAY, t8.ci_bb AS BB, " +
				                "t8.ci_video AS VI, t8.ci_gf AS GF, t6.lr_ord AS ORD, t2.rqst_pon AS PON, t6.lr_dd AS FOCDt, " +
				                "to_char(to_date(t6.lr_dd,'mm/dd/yyyy') + 1,'yyyymmdd') AS SRD, " +
				                "t8.ci_dfdt AS DFDT, decode(t8.ci_chc,'Y','Yes') AS CHC, t8.ci_remarks AS REMARKS, t9.np_remarks as NP_REMARKS, " +
				                "t1.rqst_sqnc_nmbr AS RQST, t1.rqst_vrsn AS VRSN, t2.srvc_typ_cd as SVCTYPE ,t1.rqst_stts_cd_out as STTSCD,t7.LSR_SUP as SUPVALUE " +
				                "FROM request_history_t t1, request_t t2, eu_la_t t3, lr_t t6, lsr_t t7, ci_t t8, np_t t9 " +
				                "WHERE t1.rqst_stts_cd_out = 'FOC' " +
				                "AND t1.rqst_hstry_dt_out BETWEEN TO_DATE('" + strStartDate + "00:00:01','YYYYMMDD HH24:MI:SS') " +
				                "AND TO_DATE('" + strEndDate + "23:59:59','YYYYMMDD HH24:MI:SS') " +
				                "AND t1.rqst_sqnc_nmbr = t2.rqst_sqnc_nmbr " +
				                "AND t1.rqst_vrsn = t2.rqst_vrsn " +
				                "AND t2.srvc_typ_cd in ('C') AND t2.ICARE = 'N' " +
				                "AND t1.rqst_sqnc_nmbr = t3.rqst_sqnc_nmbr " +
				                "AND t1.rqst_vrsn = t3.rqst_vrsn " +
				                "AND t1.rqst_sqnc_nmbr = t6.rqst_sqnc_nmbr " +
				                "AND t1.rqst_vrsn = t6.rqst_vrsn " +
				                "AND t1.rqst_sqnc_nmbr = t7.rqst_sqnc_nmbr " +
				                "AND t1.rqst_vrsn = t7.rqst_vrsn " +
				                "AND t1.rqst_sqnc_nmbr = t8.rqst_sqnc_nmbr " +
				                "AND t1.rqst_vrsn = t8.rqst_vrsn " +
				                "AND t1.rqst_sqnc_nmbr = t9.rqst_sqnc_nmbr " +
				                "AND t1.rqst_vrsn = t9.rqst_vrsn " + 
				                "AND t8.CI_VIDEO = 'IPTV' " +
		          
				                " UNION " +
				                "SELECT t2.cus_typ AS CusTyp, t8.ci_clec AS CLEC, null AS CusName, null AS SANO, " +
								"' ' AS SASN, ' ' AS SATH, ' ' AS SASS, ' ' AS LD1, ' ' AS LV1, " +
								"null AS CITY, t2.ocn_stt AS ST, to_char(t3.sp_zip) AS ZIP, t8.ci_stay AS STAY, t8.ci_bb AS BB, " +
								"t8.ci_video AS VI, t8.ci_gf AS GF, t4.lr_ord AS ORD, t2.rqst_pon AS PON, t4.lr_sp_ddt AS FOCDt, " +
								"to_char(to_date(t4.lr_sp_ddt,'mm/dd/yyyy') + 1,'yyyymmdd') AS SRD, " +
								"t8.ci_dfdt AS DFDT, decode(t8.ci_chc,'Y','Yes') AS CHC, t8.ci_remarks AS REMARKS, t9.np_remarks as NP_REMARKS, " +
								"t1.rqst_sqnc_nmbr AS RQST, t1.rqst_vrsn AS VRSN, t2.srvc_typ_cd as SVCTYPE,t1.rqst_stts_cd_out as STTSCD,null as SUPVALUE  " +
							    "FROM request_history_t t1, request_t t2, sp_t t3, lr_t t4, ci_t t8, np_t t9 " +
								"WHERE t1.rqst_stts_cd_out = 'FOC' " +
								"AND t1.rqst_hstry_dt_out BETWEEN TO_DATE('" + strStartDate + "00:00:01','YYYYMMDD HH24:MI:SS') " +
								"AND TO_DATE('" + strEndDate + "23:59:59','YYYYMMDD HH24:MI:SS') " +
								"AND t1.rqst_sqnc_nmbr = t2.rqst_sqnc_nmbr " +
								"AND t1.rqst_vrsn = t2.rqst_vrsn " +
								"AND t2.srvc_typ_cd in ('S') AND t2.ICARE = 'N' " +
								"AND t1.rqst_sqnc_nmbr = t3.rqst_sqnc_nmbr " +
								"AND t1.rqst_vrsn = t3.rqst_vrsn " +
								"AND t1.rqst_sqnc_nmbr = t4.rqst_sqnc_nmbr " +
								"AND t1.rqst_vrsn = t4.rqst_vrsn " +
								"AND t1.rqst_sqnc_nmbr = t8.rqst_sqnc_nmbr " +
						        "AND t1.rqst_vrsn = t8.rqst_vrsn " +
						        "AND t1.rqst_sqnc_nmbr = t9.rqst_sqnc_nmbr " +
						        "AND t1.rqst_vrsn = t9.rqst_vrsn " +
						        "AND t8.CI_VIDEO = 'IPTV' " +
						        "order by CusName ";

		       rs = stmt.executeQuery(strQry);
		       System.out.println("SQL exectued");

		       String strCusTyp = "";
		       String strCLEC = "";
		       String strCusName = "";
		       String strSANO = "";
		       String strSASN = "";
		       String strSATH = "";
		       String strSASS = "";
		       String strLD1 = "";
		       String strLV1 = "";
		       String strCusAddr = "";
		       String strCITY = "";
		       String strST = "";
		       String strZIP = "";
		       String strStaying = "";
		       String strBB = "";
		       String strVI = "";
		       String strGF = "";
		       String strOrderNum = "";
		       String strPON = "";
		       String strFOCDate = "";
		       String strSRD = "";
		       String strDFDT = "";
		       String strCHC = "";
		       String strRemarks = "";
		       String strNPRemarks = "";
		       String strRQST = "";
		       String strVRSN = "";
		       int iRqst = 0;
		       int iVrsn = 0;
		       String strYr = "";
		       String strMth = "";
		       String strDay = "";
		       String strSvcType = "";
		       String supValue = "";
		       String sttsCd = "";
		       String strUpdates = "";

		       Calendar cal = Calendar.getInstance();

		       while(rs.next())
		       {
		    	   	strSvcType = rs.getString("SVCTYPE");
		       	    strCusTyp = rs.getString("CusTyp");
				    sttsCd=rs.getString("STTSCD");
					supValue=rs.getString("SUPVALUE"); 	
		            if (sttsCd != null && supValue != null) {
		            	if (sttsCd.equals("FOC") && (supValue.equals("1"))) {
		            		strUpdates = "Cancel";
		                } else if (sttsCd.equals("FOC") && (supValue.equals("2"))) {
		                	strUpdates = "Due Date Change";
		                } else if (sttsCd.equals("FOC") && (supValue.equals("3"))) {
		                	strUpdates = "Misc Change";
		                } else if (!sttsCd.equals("FOC")) {
		                	strUpdates = "";
		                }
		            }
		            if (sttsCd != null && supValue == null) {
		            	strUpdates = "";
		            }

		            if (strCusTyp == null) {strCusTyp = "";}
		                strCLEC = rs.getString("CLEC");
		            if (strCLEC == null) {strCLEC = "";}
		       		strCusName = rs.getString("CusName");
		            if (strCusName == null) {strCusName = "";}
		       		strSANO = rs.getString("SANO");
		       		strSASN = rs.getString("SASN");
		       		strSATH = rs.getString("SATH");
		       		strSASS = rs.getString("SASS");
		       		strLD1 = rs.getString("LD1");
		       		strLV1 = rs.getString("LV1");
		       	    if (strSANO != null) { strCusAddr += strSANO+" ";}
		       	    if (strSASN != null) { strCusAddr += strSASN+" ";}
		       	    if (strSATH != null) { strCusAddr += strSATH+" ";}
		       	    if (strSASS != null) { strCusAddr += strSASS+" ";}
		       	    if (strLD1 != null) { strCusAddr += strLD1+" ";}
		       	    if (strLV1 != null) { strCusAddr += strLV1+" ";}
		       	    strCITY = rs.getString("CITY");
		            if (strCITY == null) {strCITY = "";}
		            strST = rs.getString("ST");
		       		strZIP = rs.getString("ZIP");
		       		if (strZIP == null) {strZIP = "";}
		       	    strStaying = rs.getString("STAY");
		       	    if (strStaying == null) {strStaying = "";}
		       		strBB = rs.getString("BB");
		       	    if (strBB == null) {strBB = "";}
					strVI = rs.getString("VI");
		       	    if (strVI == null) {strVI = "";}
		       		strGF = rs.getString("GF");
		       	    if (strGF == null) {strGF = "";}
		       		strOrderNum = rs.getString("ORD");
		       	    if (strOrderNum == null) {strOrderNum = "";}
		       		strPON = rs.getString("PON");
		       		strFOCDate = rs.getString("FOCDt");
		       	    if (strFOCDate == null) {strFOCDate = "";}
		       		strSRD = rs.getString("SRD");
		       		strDFDT = rs.getString("DFDT");
		       	    if (strDFDT == null) {strDFDT = "";}
		       	        strCHC = rs.getString("CHC");
		       	    if (strCHC == null) {strCHC = "";}
		       	        strRemarks = rs.getString("REMARKS");
		       	    if (strRemarks == null) {strRemarks = "";}
		       	        strNPRemarks = rs.getString("NP_REMARKS");
		       	    if (strNPRemarks == null) {strNPRemarks = "";}                       
		       		strRQST = rs.getString("RQST");
		       		strVRSN = rs.getString("VRSN");
		       		iRqst = Integer.parseInt(strRQST);
		       		iVrsn = Integer.parseInt(strVRSN);
		       		if (strSRD == null)
		       		{
		       		   strSRD = "";
		       		} else {
		       		strYr = strSRD.substring(0,4);
		       		strMth = strSRD.substring(4,6);
		       		strDay = strSRD.substring(6);

		            cal.set(Integer.parseInt(strYr),  Integer.parseInt(strMth) - 1,  Integer.parseInt(strDay), 0, 0, 0);

		            String strCt4 = "";
		            int iCt4 = 0;

		       		String strQry4 = "select count(hldy_dt) from holiday_t where to_char(hldy_dt,'yyyymmdd') = " + strSRD;

		       		rs4 = stmt4.executeQuery(strQry4);

		       		while(rs4.next())
		       		{
		       		     strCt4 = rs4.getString(1);
		       		     iCt4 = Integer.parseInt(strCt4);
		       		}

		       		if (iCt4 == 1)
		       		{
		       		   cal.add(Calendar.DATE, 1);

		                   int iDays = cal.get(Calendar.DAY_OF_WEEK);

		                   if (iDays == 7)
		                   {
		                      cal.add(Calendar.DATE, 2);
		                      strSRD = sdf1.format(cal.getTime());
		                   }
		                   else
		                   {
		                      strSRD = sdf1.format(cal.getTime());
		                   }
		                }
		                else
		                {

		                   int iDays = cal.get(Calendar.DAY_OF_WEEK);

						   if (iDays == 7)
						   {
						      cal.add(Calendar.DATE, 2);
						   }
		
				           String strCt5 = "";
				           int iCt5 = 0;
		
				           strSRD = sdf2.format(cal.getTime());
		
				           String strQry5 = "select count(hldy_dt) from holiday_t where to_char(hldy_dt,'yyyymmdd') = " + strSRD;
		
						   rs5 = stmt5.executeQuery(strQry5);
		
						   while(rs5.next())
						   {
						        strCt5 = rs5.getString(1);
						        iCt5 = Integer.parseInt(strCt5);
						   }
		
						   if (iCt5 == 1)
						   {
						      cal.add(Calendar.DATE, 1);
				       		  strSRD = sdf1.format(cal.getTime());
				       	   }
				       	   else
				       	   {
				       		   strSRD = sdf1.format(cal.getTime());
				       	   }
		                }
		       		}
		       		String strCt2 = "";
		       		String strCt3 = "";
		       		int iCt2 = 0;
		       		int iCt3 = 0;

		       		String strQry2 = "select count(np_sd_portednbr) from np_sd_t where rqst_sqnc_nmbr = " + iRqst + " and rqst_vrsn = " + iVrsn;

		       		rs2 = stmt2.executeQuery(strQry2);

		       		while(rs2.next())
		       		{
		       			strCt2 = rs2.getString(1);
		                iCt2 = Integer.parseInt(strCt2);
		            }
		            String strQry3 = "select count(eu_dd_discnbr) from eu_dd_t where rqst_sqnc_nmbr = " + iRqst + " and rqst_vrsn = " + iVrsn;

		            rs3 = stmt3.executeQuery(strQry3);

		        	while(rs3.next())
		        	{
		        		strCt3 = rs3.getString(1);
		                iCt3 = Integer.parseInt(strCt3);
		            }

		        	System.out.println("NPSPDailyBatchReport: strCt2:" + strCt2 + " strCt3:" + strCt3 + " strSvcType:" + strSvcType);
		            String strQry1  = "";
			        if ((iCt2 > iCt3) || (strSvcType.equals("S"))) {
				       if (strSvcType.equals("S")) {
					       strQry1 = "select sp_ptn, null from sp_t t1 where  t1.rqst_sqnc_nmbr = " + iRqst + " and t1.rqst_vrsn = " + iVrsn;
				       } else {
					       strQry1 = "select t1.np_sd_portednbr, t2.eu_dd_discnbr from np_sd_t t1 left outer join eu_dd_t t2 on (t1.rqst_sqnc_nmbr = t2.rqst_sqnc_nmbr and t1.rqst_vrsn = t2.rqst_vrsn and t1.frm_sctn_occ = t2.frm_sctn_occ) " +
						  "where t1.rqst_sqnc_nmbr = " + iRqst + " and t1.rqst_vrsn = " + iVrsn;
				       }

		       		rs1 = stmt1.executeQuery(strQry1);

		       		String strPorting = "";
		       		String strDisconn = "";

		       		while(rs1.next())
		       		{
		       			strPorting = rs1.getString(1);
		                if (strPorting == null) {strPorting = "";}
		                strDisconn = rs1.getString(2);
		                if (strDisconn == null) {strDisconn = "";}
	
		                strBuild.append(LINE_SEPARATOR);
		                strBuild.append(strCusTyp);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strCLEC);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strCusName);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strCusAddr);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strCITY);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strST);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strZIP);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strPorting);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strDisconn);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strStaying);
						strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strBB);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strVI);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strGF);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strOrderNum);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strPON);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strFOCDate);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strSRD);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strDFDT);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strCHC);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strRemarks);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strNPRemarks);
		                strBuild.append(TAB_SEPARATOR);
		                strBuild.append(strUpdates);

		                strCusTyp = "";
		                strCLEC = "";
		                strCusName = "";
		                strCusAddr = "";
		                strCITY = "";
		                strST = "";
		                strZIP = "";
		                strStaying = "";
		                strBB = "";
		                strVI = "";
		                strGF = "";
		                strOrderNum = "";
		                strPON = "";
		                strFOCDate = "";
		                strSRD = "";
		                strDFDT = "";
		                strCHC = "";
		                strRemarks = "";
		                strNPRemarks = "";
		                }
		                }else{
		                  System.out.println("!!!!!!!!!!!! strSvcType 2nd IF: " + strSvcType);
		                  if (strSvcType.equals("S")) {
		                      strQry1 = "select '', sp_ptn from sp_t t1 where  t1.rqst_sqnc_nmbr = " + iRqst + " and t1.rqst_vrsn = " + iVrsn;
		                  } else {
		                   strQry1 = "select t1.eu_dd_discnbr, t2.np_sd_portednbr from eu_dd_t t1 left outer join np_sd_t t2 on (t1.rqst_sqnc_nmbr = t2.rqst_sqnc_nmbr and t1.rqst_vrsn = t2.rqst_vrsn and t1.frm_sctn_occ = t2.frm_sctn_occ) " +
		                          "where t1.rqst_sqnc_nmbr = " + iRqst + " and t1.rqst_vrsn = " + iVrsn;
		                }
		                rs1 = stmt1.executeQuery(strQry1);

		       			String strPorting = "";
		       			String strDisconn = "";

		       			while(rs1.next())
		                {
		                         strPorting = rs1.getString(2);
		                     if (strPorting == null) {strPorting = "";}
		                         strDisconn = rs1.getString(1);
		                     if (strDisconn == null) {strDisconn = "";}
		
		                     strBuild.append(LINE_SEPARATOR);
		                     strBuild.append(strCusTyp);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strCLEC);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strCusName);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strCusAddr);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strCITY);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strST);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strZIP);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strPorting);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strDisconn);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strStaying);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strBB);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strVI);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strGF);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strOrderNum);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strPON);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strFOCDate);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strSRD);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strDFDT);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strCHC);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strRemarks);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strNPRemarks);
		                     strBuild.append(TAB_SEPARATOR);
		                     strBuild.append(strUpdates);
	
		                     strCusTyp = "";
		                     strCLEC = "";
		                     strCusName = "";
		                     strCusAddr = "";
		                     strCITY = "";
		                     strST = "";
		                     strZIP = "";
		                     strStaying = "";
		                     strBB = "";
		                     strVI = "";
		                     strGF = "";
		                     strOrderNum = "";
		                     strPON = "";
		                     strFOCDate = "";
		                     strSRD = "";
		                     strDFDT = "";
		                     strCHC = "";
		                     strRemarks = "";
		                     strNPRemarks = "";
		                }
		            }
		       	}
		       	
			} // try
			catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.toString());
			}
			finally {
				
				//Release Connection and ResultSet.
				releaseRS(rs);
			    releaseRS(rs1);
			    releaseRS(rs2);
			    releaseRS(rs3);
			    releaseRS(rs4);
			    releaseRS(rs5);
				DatabaseManager.releaseConnection(con);
			}
			
			return strBuild.toString();
		}

		private static void releaseRS(ResultSet rs) throws SQLException {
			if(rs != null){
		    	   rs.close();
		       }
		}
}
