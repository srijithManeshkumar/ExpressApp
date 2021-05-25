/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2002
 *				BY
 *			ALLTEL COMMUNICATIONS INC.
 */
/**
 * MODULE:	x.java
 *
 * DESCRIPTION: Vendor reports. This is a scheduled batch program thats checks for
 *		reports that need to be run. Reports of type "E" (meaning Express generated)
 *		are run here. Reports of type "M" (manual) are reports that are manually
 *		created via LSPAC and upload into Express via a perl script.
 *
 * AUTHOR:      psedlak
 *
 * DATE:        9-4-2002
 *
 * HISTORY:
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */
package com.alltel.lsr.common.batch;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.batch.*;
import com.alltel.lsr.common.util.*;

class ReportInfo
{
	int 	m_iReport;
	String	m_strFreq;
	String	m_strName;
	String  m_strRunDate;
	String  m_strBeginDate;
	String	m_strEndDate;
	ReportInfo(int i, String freq, String Name, String Run, String Beg, String End)
	{
		this.m_iReport=i;
		this.m_strFreq=freq;
		this.m_strName=Name;
		this.m_strRunDate=Run;
		this.m_strBeginDate=Beg;
		this.m_strEndDate=End;
	}
	ReportInfo() {}
	public int 	getReport()	{ return m_iReport; }
	public String	getFreq()	{ return m_strFreq; }
	public String	getName()	{ return m_strName; }
	public String	getRunDate()	{ return m_strRunDate; }
	public String	getBeginDate()	{ return m_strBeginDate; }
	public String	getEndDate()	{ return m_strEndDate; }
}

public class BatchReports
{
	final static long DAY_IN_SEC = (long) 86400;
        final static long HOUR_IN_SEC = (long) 3600;
        final static long MIN_IN_SEC = (long) 60;

        final static int ALL_COMPANIES = 0;
        final static int ALL_OCNS = 0;
        static int PROVIDER_ONLY= 1;	//Should be seq # of "P" company -can replace this with a lookup

        static Connection con = null;
        static Statement stmt1 = null;
        static PreparedStatement pstmtDetail = null;
        static ResultSet rs = null;

	static String strDetailInsert="INSERT INTO VENDOR_REPORT_DETAIL_T (RPRT_SQNC_NMBR, RPRT_RUN_DT, DTL_LN_NMBR, "+
		"CMPNY_SQNC_NMBR, OCN_CD, DTL_LN, MDFD_DT, MDFD_USERID) " +
		" VALUES (?, TO_DATE(?,'YYYYMMDD'),?,?,?,?, SYSDATE, 'batch')";


	public static void main(String[] args)
	{
		int 	iRC = 0;
		int 	iRpt = 0;
		String	strURL = "";
		String	strDbUserid = "";
		String	strDbPassword = "";
		Vector	vReports = new Vector();	//vector of ReportInfo objects

		if (args.length == 3)
		{
			strURL = args[0];
			strDbUserid = args[1];
			strDbPassword = args[2];
		}
		else
		{	System.out.println("Usage: BatchReports <URL> <Userid> <Password> ");
			iRC=1;
			System.exit(iRC);
			return;
		}

		try {
			// Get Connection to DB
			Class.forName("oracle.jdbc.OracleDriver");
			con = DriverManager.getConnection(strURL, strDbUserid, strDbPassword);
		}
		catch (Exception e) {
			System.out.println("Error getting connection! ");
			e.printStackTrace();
			iRC=2;
			System.exit(iRC);
			return;
		}
		System.out.println("Connection OK! ");

		try {
			stmt1 = con.createStatement();

			//Get Provider Seq Nbr
			rs = stmt1.executeQuery("SELECT CMPNY_SQNC_NMBR FROM COMPANY_T WHERE CMPNY_TYP='P'");
			rs.next();
			PROVIDER_ONLY = rs.getInt(1);
			System.out.println("Provider company seq=" + PROVIDER_ONLY);

			//Get current report information - such as frequency and last run date/times.
			//NOTE: On initial startup, the vendor-report-actvty-t table needs to be primed.
			rs = stmt1.executeQuery("SELECT V.RPRT_SQNC_NMBR, V.RPRT_FRQNCY, V.RPRT_DSC, " +
						" TO_CHAR(VRA.RPRT_RUN_DT,'YYYYMMDD'), " +
						" TO_CHAR(VRA.RPRT_BGN_DT,'YYYYMMDD'), TO_CHAR(VRA.RPRT_END_DT,'YYYYMMDD') " +
						" FROM VENDOR_REPORT_T V, VENDOR_REPORT_ACTVTY_T VRA " +
						" WHERE V.RPRT_SRC_IND='E' AND VRA.RPRT_SQNC_NMBR=V.RPRT_SQNC_NMBR " +
						" AND VRA.RPRT_RUN_DT IN " +
						" (SELECT MAX(RPRT_RUN_DT) FROM VENDOR_REPORT_ACTVTY_T VRA2 " +
						"  WHERE VRA2.RPRT_SQNC_NMBR=V.RPRT_SQNC_NMBR)" );
			while(rs.next())
			{	//See what reports are due to run....
				ReportInfo objRpt = new ReportInfo(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
								   rs.getString(5), rs.getString(6));
				vReports.addElement(objRpt);
			}

			pstmtDetail  = con.prepareStatement(strDetailInsert);

			System.out.println(vReports.size() + " reports are defined as being generated from Express data");
			ReportInfo rpt = new ReportInfo();
			for (int x=0; x < vReports.size(); x++)
			{	rpt = (ReportInfo)vReports.elementAt(x);	//note this is a shallow copy
				System.out.println("Report " + rpt.getReport() + " is " + rpt.getName() + " last run on " + rpt.getRunDate());
				System.out.println("       " + rpt.getReport() + " range was " + rpt.getBeginDate() + " to " + rpt.getEndDate());

				//If report is run monthly and today > 35days since last end date then we can run it....
				if (rpt.getFreq().equals("M"))
				{	System.out.println("Report is run monthly");
					rs = stmt1.executeQuery("SELECT 1 FROM DUAL WHERE SYSDATE-35 > to_date('"+
								rpt.getEndDate()+"','YYYYMMDD')");
					if(rs.next())
					{	System.out.println("Report is ready to run!");
						switch(rpt.getReport()) {
						case 1:	iRC = doPreorderReport(rpt); //aka OSS-1
							break;
						case 2:	iRC = doSLAReport(rpt);	//aka "Reject & FOC Timeliness O-1
							break;
						case 3:	iRC = doReport3(rpt);
							break;
						}
					}
					else	System.out.println("Report is not ready to run !\n=============================");
				}
				System.out.println(" ");
			}
			stmt1.close();
			stmt1 = null;

		}//try()
		catch (Exception e)
		{
			System.out.println("Exception thrown and caught");
			rs = null;
			e.printStackTrace();
			iRC=4;
			return;
		}
		finally {
			try {	con.close();	}
			catch (Exception e) {}	//do nothing
		}
		System.exit(iRC);
		return;
	}

	//Write result record to Detail table
	static int storeDetail(int iRpt, String strRunDate, int iComp, int iOCN, String strDetailLine, int iLine)
	{
		int iRC = 0;

		try {
			pstmtDetail.setInt(1, iRpt);
			pstmtDetail.setString(2, strRunDate);
			pstmtDetail.setInt(3, iLine);
			pstmtDetail.setInt(4, iComp);
			pstmtDetail.setInt(5, iOCN);
			pstmtDetail.setString(6, strDetailLine);

			pstmtDetail.executeUpdate();
		}
		catch (Exception e) {
			System.out.println("storeDetail() exception ["+e+"] ");
			System.out.println("storeDetail() in args["+iRpt+","+strRunDate+","+iComp+","+iOCN+",\n"+strDetailLine+
					","+iLine+"]");
		}
		finally {
		}
		return iRC;
	}

	//Returns description for the given Preorder Transaction Type Code OR Null if not found
	static String getTransactionDesc(String TypCd)
	{
		final boolean bLoadHash = true;
		final Hashtable hashTransTypes = new Hashtable();
		String strDesc="";

		if (bLoadHash)
		{
			Statement stmt=null;
			ResultSet rs = null;
			try {
				stmt = con.createStatement();
				//Put the list of Transaction Types into a hash to include on report
//				hashTransTypes = new Hashtable();
				rs = stmt.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T "+
							" WHERE TYP_IND='P' ORDER BY SRVC_TYP_CD");
				while (rs.next())
				{	hashTransTypes.put(rs.getString(1), rs.getString(2));
				}
			}
			catch (Exception e) {
				System.out.println("getTransactionDesc() Exception !");
			}
			finally {
				try {
					rs.close();
					stmt.close();
				}
				catch (Exception e2) {}
				rs=null;
				stmt=null;
			}
		}
		//Get Desc
		if (hashTransTypes.containsKey(TypCd))
		{	strDesc = (String)hashTransTypes.get(TypCd);
		}
		return strDesc;
	}

	/**
	* ---------------------------------------------
	* Preorder Average Response Time Report (OSS-1),
	* also known as "Preorder Report".
	* ---------------------------------------------
	*/
	static int doPreorderReport(ReportInfo rptInfo)
	{
		String strNewBeginDate="";
		String strNewEndDate="";
		String strNewRunDate="";
		String strDetailLine="";

		int     iLineCount = 0;
		int     iOCNCount = 0;
		long    lIntervalTotals = 0;

		long lPrevSeqNmbr = 0;
		long lSeqNmbr = 0;
		long	lIntervalAverage = 0;
		long    lDay = 0;
		long    lHour = 0;
		long    lMin = 0;
		long    lSec = 0;
		int     iTotal = 0;
		DecimalFormat OCNfmt = new DecimalFormat("0000");

		try {
			System.out.println("=============================================");
			System.out.println("Doing Preorder report OSS-1 ");
			System.out.println("rpt="+rptInfo.getReport());
			System.out.println("beg="+rptInfo.getBeginDate());
			System.out.println("end="+rptInfo.getEndDate());

			String strQry1 = "SELECT TO_CHAR(ADD_MONTHS(TO_DATE('" + rptInfo.getBeginDate() + "','YYYYMMDD'),1),'YYYYMMDD')," +
			  	   " TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE('" + rptInfo.getBeginDate() + "','YYYYMMDD'),1)), 'YYYYMMDD')," +
				   " TO_CHAR(SYSDATE,'YYYYMMDD') FROM DUAL";
			rs = stmt1.executeQuery(strQry1);
			if (rs.next())
			{
				strNewBeginDate = rs.getString(1);
				strNewEndDate = rs.getString(2);
				strNewRunDate = rs.getString(3);
			}
			else
			{
				System.out.println("PROBLEM");
			}

			//Write common header information
			strDetailLine = "<tr><th align=center>&nbsp;OCN&nbsp;</th><th align=center>&nbsp;State&nbsp;</th>"+
					"<th align=center>VENDOR</th>" +
					"<th align=center>Transaction<br>Type</th><th align=center>&nbsp;Total<br>Completed&nbsp;</th>"+
					"<th align=center>&nbsp;Completed<br>Interval&nbsp;</th></tr>";
			storeDetail(rptInfo.getReport(), strNewRunDate, ALL_COMPANIES, ALL_OCNS, strDetailLine, iLineCount++);

			System.out.println("Doing report....");
			String strOCNQuery =
			  "SELECT PH.PRE_ORDR_SQNC_NMBR, PH.PRE_ORDR_STTS_CD_IN, TO_CHAR(PH.PRE_ORDR_HSTRY_DT_IN,'YYYYMMDD HH24MISS'),"+
				" P.SRVC_TYP_CD FROM PREORDER_T P, PREORDER_HISTORY_T PH WHERE P.OCN_CD = ? " +
				" AND P.OCN_STT = ? AND P.PRE_ORDR_SQNC_NMBR = PH.PRE_ORDR_SQNC_NMBR AND "+
				" PH.PRE_ORDR_STTS_CD_IN <> PH.PRE_ORDR_STTS_CD_OUT AND " +
				" EXISTS (SELECT PH2.PRE_ORDR_SQNC_NMBR FROM PREORDER_HISTORY_T PH2 " +
					" WHERE PH2.PRE_ORDR_SQNC_NMBR = P.PRE_ORDR_SQNC_NMBR " +
					" AND PH2.PRE_ORDR_STTS_CD_IN = 'COMPLETED' " +
					" AND PH2.PRE_ORDR_HSTRY_DT_IN BETWEEN " +
					" TO_DATE('" + strNewBeginDate + " 00:00:00', 'YYYYMMDD HH24:MI:SS') AND " +
					" TO_DATE('" + strNewEndDate + " 23:59:59', 'YYYYMMDD HH24:MI:SS') ) " +
			  	" AND PH.PRE_ORDR_STTS_CD_IN in ('SUBMITTED','COMPLETED') " +
			  	" ORDER BY P.SRVC_TYP_CD, PH.PRE_ORDR_SQNC_NMBR, PRE_ORDR_STTS_CD_IN";
			PreparedStatement pstmt  = con.prepareStatement(strOCNQuery);

			strQry1 = "SELECT O.OCN_CD, OS.STT_CD, O.OCN_NM, O.CMPNY_SQNC_NMBR FROM OCN_T O, OCN_STATE_T OS " +
                                " WHERE O.OCN_CD = OS.OCN_CD ORDER BY OS.STT_CD, O.OCN_CD ";
			rs = stmt1.executeQuery(strQry1);
			//Spin thru all OCNs and States
			while(rs.next()==true)
			{
				iOCNCount++;
				int iCmp = rs.getInt(4);
				int iOCN = rs.getInt(1);
				String strSt = rs.getString(2);
				String strNm = rs.getString(3);
				int iCompleted = 0;
				long lIntervalAccumulation = 0;      //this is total seconds
				long lInterval = 0;
				String strIntervalEndDTS = "";
				String strIntervalBeginDTS = "";

				// Start building stats for this OCN-State
				pstmt.setInt(1, iOCN);
				pstmt.setString(2, strSt);
				ResultSet rs2 = pstmt.executeQuery();

				String strPreOrdrSqncNmbr = "";
				String strPrevPreOrdrSqncNmbr = "";

				String strTransType = "&nbsp;";
				String strPrevTransType = "&nbsp;";

				boolean bFirstTime = true;
				boolean bFirstHeader = true;
				strDetailLine="";	//We build the HTML line

				while(rs2.next()==true)
				{
					String strStatus = rs2.getString(2);
					strPreOrdrSqncNmbr = rs2.getString(1);
					strTransType = rs2.getString(4);

					// Check for changing Transaction Type
					if (! strTransType.equals(strPrevTransType) && bFirstTime == false)
					{
						iTotal += iCompleted;
						strDetailLine += "<tr>";
						if (bFirstHeader == true)
						{
							strDetailLine = "<td>&nbsp;"+OCNfmt.format(iOCN)+"&nbsp;</td>"+
									"<td align=center>&nbsp;"+strSt+"&nbsp;</td>"+
									"<td>&nbsp;"+strNm+"&nbsp;</td>";
							bFirstHeader = false;
						}
						else
						{
							strDetailLine = "<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>";
						}
						strDetailLine += "<td>&nbsp;"+strPrevTransType+" "+getTransactionDesc(strPrevTransType)+
								"&nbsp;</td><td align=right>"+ iCompleted+"</td>";
						if (iCompleted > 0)
						{
							lIntervalAverage = lIntervalAccumulation/iCompleted;

							//put in xd xh xm xs format
							lDay = lIntervalAverage / DAY_IN_SEC;
							lIntervalAverage %= DAY_IN_SEC;
							lHour = lIntervalAverage / HOUR_IN_SEC;
							lIntervalAverage %= HOUR_IN_SEC;
							lMin = lIntervalAverage / MIN_IN_SEC;
							lIntervalAverage %= MIN_IN_SEC;
							lSec = lIntervalAverage;
							strDetailLine += "<td align=right>&nbsp;"+lDay+"d&nbsp;"+lHour+"h&nbsp;"+
									lMin+"m&nbsp;"+lSec+"s&nbsp;</td>";
						}
						else
						{
							strDetailLine += "<td align=right>&nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;0s&nbsp;</td>";
						}
						strDetailLine += "</tr>";
						storeDetail(rptInfo.getReport(), strNewRunDate, iCmp, iOCN, strDetailLine, iLineCount++);
						lIntervalTotals += lIntervalAccumulation;
						iCompleted = 0;
						lIntervalAccumulation = 0;
					}

					if ( strStatus.equals("COMPLETED") )
					{
						//System.out.println("Counting 1 for request " + strPreOrdrSqncNmbr);
						iCompleted++;
						strIntervalEndDTS = rs2.getString(3);
						lPrevSeqNmbr = rs2.getInt(1);
					}
					if ( strStatus.equals("SUBMITTED") )
					{
						lSeqNmbr = rs2.getInt(1);
						if (lSeqNmbr == lPrevSeqNmbr)
						{
							strIntervalBeginDTS = rs2.getString(3);
							//Calculate Interval
							strIntervalBeginDTS =SLATools.getSLAStartDateTime(strIntervalBeginDTS.substring(0,8),
													  strIntervalBeginDTS.substring(9,15));
							lInterval = SLATools.calculateSLA(strIntervalBeginDTS, strIntervalEndDTS);
							lIntervalAccumulation = lIntervalAccumulation + lInterval;
							//System.out.println(">>Interval for request " + lSeqNmbr +
							//		   " = " + lInterval + " seconds");
						}
					}

					strPrevTransType = strTransType;
					bFirstTime = false;
				}   //while()
				rs2.close();
				rs2 = null;

				iTotal += iCompleted;
				strDetailLine = "<tr>";
				if (bFirstHeader == true)
				{
					strDetailLine += "<td>&nbsp;"+OCNfmt.format(iOCN)+"&nbsp;</td>"+
							 "<td align=center>&nbsp;"+strSt+"&nbsp;</td>"+
							 "<td>&nbsp;"+strNm+"</td>";
				}
				else
				{
					strDetailLine += "<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>";
				}
				strDetailLine += "<td>&nbsp;"+strTransType+" "+getTransactionDesc(strTransType)+"&nbsp;</td>"+
						 "<td align=right>"+iCompleted+"</td>";
				if (iCompleted > 0)
				{
					lIntervalAverage = lIntervalAccumulation/iCompleted;

					//put in xd xh xm xs format
					lDay = lIntervalAverage / DAY_IN_SEC;
					lIntervalAverage %= DAY_IN_SEC;
					lHour = lIntervalAverage / HOUR_IN_SEC;
					lIntervalAverage %= HOUR_IN_SEC;
					lMin = lIntervalAverage / MIN_IN_SEC;
					lIntervalAverage %= MIN_IN_SEC;
					lSec = lIntervalAverage;
					strDetailLine += "<td align=right>&nbsp;"+lDay+"d&nbsp;"+lHour+"h&nbsp;"+
							 lMin+"m&nbsp;"+lSec+"s&nbsp;</td>";
				}
				else
				{
					strDetailLine += "<td align=right>&nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;0s&nbsp;</td>";
				}
				strDetailLine += "</tr>";
				storeDetail(rptInfo.getReport(), strNewRunDate, iCmp, iOCN, strDetailLine, iLineCount++);
				lIntervalTotals += lIntervalAccumulation;
			} //while()

			if (iTotal > 0)
			{	lIntervalAverage =  lIntervalTotals/iTotal;
			}
			else
			{	lIntervalAverage=0;
			}

			lDay = lIntervalAverage / DAY_IN_SEC;
			lIntervalAverage %= DAY_IN_SEC;
			lHour = lIntervalAverage / HOUR_IN_SEC;
			lIntervalAverage %= HOUR_IN_SEC;
			lMin = lIntervalAverage / MIN_IN_SEC;
			lIntervalAverage %= MIN_IN_SEC;
			lSec = lIntervalAverage;


			strDetailLine = "<tr><td colspan=2><b>TOTALS</b></td><td align=center><b>"+iOCNCount+"&nbsp;VENDORS</b></td>";
			strDetailLine += "<td align=right><b>&nbsp;</b></td><td align=right><b>"+iTotal+"</b></td>";
			strDetailLine += "<td align=right><b>"+lDay+"d&nbsp;"+lHour+"h&nbsp;"+lMin+"m&nbsp;"+lSec+"s</b></td></tr>";

			storeDetail(rptInfo.getReport(), strNewRunDate, PROVIDER_ONLY, ALL_OCNS, strDetailLine, iLineCount++);
			System.out.println("Done with report, about to write ACTVTY record");

			//OK, we're now done creating report, so put Activity record out there so users can get to it...
			//Insert activity record into table
			String strInsert="INSERT INTO VENDOR_REPORT_ACTVTY_T (RPRT_SQNC_NMBR, RPRT_RUN_DT, RPRT_BGN_DT, "+
					 "RPRT_END_DT, MDFD_DT, MDFD_USERID) VALUES ("+rptInfo.getReport()+", " +
					 " TO_DATE('"+strNewRunDate+"','YYYYMMDD'), TO_DATE('"+strNewBeginDate+"','YYYYMMDD'),"+
					 " TO_DATE('"+strNewEndDate+" 23:59:59','YYYYMMDD HH24:MI:SS'), SYSDATE, 'batch')";
			stmt1.executeUpdate(strInsert);
		}//try()
		catch(Exception e) {
			System.out.println("Exception ["+e+"] caught in doPreorderReport()");
			try { con.rollback();	}
			catch(Exception e2) {
				System.out.println("Exception in rollback() too");
			}
		}
		finally {
		}
		System.out.println("=============================================");
		return 0;
	}

	/**
	* -------------------------------------------------------------------------
	* O-1 Reject and FOC completeness, also known as "SLA Report"
	* -------------------------------------------------------------------------
	*/
	static int doSLAReport(ReportInfo rptInfo)
	{
		String strNewBeginDate="";
		String strNewEndDate="";
		String strNewRunDate="";
		String strDetailLine="";

                int     iLineCount = 0;
                int     iOCNCount = 0;
                int     iCompletedTotals = 0;
                int     iSubmittedTotals= 0;
		int     iRejectedTotals = 0;
		int     iWithinFOCTotals = 0;
		int     iPastFOCTotals = 0;
		long    lSLATotals = 0;
	        long    lSLAAverage = 0;

                long 	lPrevSeqNmbr = 0;
                long 	lSeqNmbr = 0;
                long    lIntervalAverage = 0;
                long    lDay = 0;
                long    lHour = 0;
                long    lMin = 0;
                long    lSec = 0;
                int     iTotal = 0;
                DecimalFormat OCNfmt = new DecimalFormat("0000");

		System.out.println("=============================================");
		System.out.println("Doing report 2 ");
		try {
			System.out.println("rpt="+rptInfo.getReport());
			String strQry1 = "SELECT TO_CHAR(ADD_MONTHS(TO_DATE('" + rptInfo.getBeginDate() + "','YYYYMMDD'),1),'YYYYMMDD')," +
			  	   " TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE('" + rptInfo.getBeginDate() + "','YYYYMMDD'),1)), 'YYYYMMDD')," +
				   " TO_CHAR(SYSDATE,'YYYYMMDD') FROM DUAL";
			rs = stmt1.executeQuery(strQry1);
			if (rs.next())
			{
				strNewBeginDate = rs.getString(1);
				strNewEndDate = rs.getString(2);
				strNewRunDate = rs.getString(3);
			}
			else
			{
				System.out.println("PROBLEM");
			}
			Calendar calCutOff = Calendar.getInstance();
			Calendar calTemp = Calendar.getInstance();

			calCutOff.set(Integer.parseInt(strNewBeginDate.substring(0,4)), Integer.parseInt(strNewBeginDate.substring(5,6)) - 1, 1, 0,0,0);
			int iDOW = calCutOff.get(Calendar.DAY_OF_YEAR);

                        //Write common header information
			strDetailLine ="<tr><th align=center>&nbsp;OCN&nbsp;</th>"+
				"<th align=center>State</th>"+
				"<th align=center>Vendor</th>"+
				"<th align=center>&nbsp;Total<br>&nbsp;Submitted&nbsp;</th>"+
				"<th align=center>&nbsp;Total<br>&nbsp;FOCed&nbsp;</th>"+
				"<th align=center>&nbsp;Total<br>&nbsp;Rejected&nbsp;</th>"+
				"<th align=center>&nbsp;TOTAL<br>&nbsp;Responses&nbsp;</th>"+
				"<th align=center>&nbsp;%<br>&nbsp;Responses<br>REJECTED&nbsp;</th>"+
				"<th align=center>&nbsp;%<br>&nbsp;Submitted<br>REJECTED&nbsp;</th>"+
				"<th align=center>&nbsp;Average<br>&nbsp;FOC&nbsp;Interval&nbsp;</th>"+
				"<th align=center>&nbsp;FOC<br>&nbsp;SLA&nbsp;</th>"+
				"<th align=center>&nbsp;Within<br>&nbsp;SLA&nbsp;</th>"+
				"<th align=center>&nbsp;Past<br>&nbsp;SLA&nbsp;</th>"+
				"<th align=center>&nbsp;%&nbsp;within<br>&nbsp;SLA&nbsp;</th></tr>";
                        storeDetail(rptInfo.getReport(), strNewRunDate, ALL_COMPANIES, ALL_OCNS, strDetailLine, iLineCount++);
                        System.out.println("Doing report....");

			String strOCNQuery = "SELECT RH.RQST_SQNC_NMBR, RH.RQST_STTS_CD_IN, " +
			  " TO_CHAR(RH.RQST_HSTRY_DT_IN,'YYYYMMDD HH24MISS'), R.RQST_TYP_CD " +
			  " FROM REQUEST_T R, REQUEST_HISTORY_T RH " +
			  " WHERE R.OCN_CD = ? AND R.OCN_STT = ? AND " +
			  " R.RQST_SQNC_NMBR = RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN <> RH.RQST_STTS_CD_OUT " +
			  " AND RH.RQST_HSTRY_DT_IN  < TO_DATE('" + strNewEndDate + " 23:59:59', 'YYYYMMDD HH24:MI:SS') " +
			  " AND RH.RQST_STTS_CD_IN IN ('SUBMITTED','FOC','REJECTED') " +
			  " AND EXISTS (SELECT RH2.RQST_SQNC_NMBR FROM REQUEST_HISTORY_T RH2 " +
					" WHERE RH2.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR " +
					" AND RH2.RQST_STTS_CD_IN IN ('FOC', 'REJECTED') " +
					" AND RH2.RQST_HSTRY_DT_IN BETWEEN " +
					" TO_DATE('" + strNewBeginDate + " 00:00:00', 'YYYYMMDD HH24:MI:SS') AND " +
					" TO_DATE('" + strNewEndDate + " 23:59:59', 'YYYYMMDD HH24:MI:SS') ) " +
			  " ORDER BY RH.RQST_SQNC_NMBR, RH.RQST_HSTRY_DT_IN DESC";
			PreparedStatement pstmt  = con.prepareStatement(strOCNQuery);

			String strSubmittedCount = "SELECT COUNT(*) FROM REQUEST_T R, REQUEST_HISTORY_T RH " +
			  " WHERE R.OCN_CD = ? AND R.OCN_STT = ? AND " +
			  " R.RQST_SQNC_NMBR = RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' " +
			  " AND RH.RQST_STTS_CD_OUT IN ('IN-REVIEW','N/A') " +
			  " AND RH.RQST_HSTRY_DT_IN BETWEEN " +
			  " TO_DATE('" + strNewBeginDate + " 00:00:00', 'YYYYMMDD HH24:MI:SS') AND " +
			  " TO_DATE('" + strNewEndDate + " 23:59:59', 'YYYYMMDD HH24:MI:SS')";
			PreparedStatement pstmt2  = con.prepareStatement(strSubmittedCount);

			String strQuery1 = "SELECT O.OCN_CD, OS.STT_CD, O.OCN_NM, OS.OCN_STT_SLA_DYS,"+
					" O.CMPNY_SQNC_NMBR FROM OCN_T O, OCN_STATE_T OS "+
					" WHERE O.OCN_CD = OS.OCN_CD " +
			                " ORDER BY OS.STT_CD, O.OCN_CD ";
			rs = stmt1.executeQuery(strQuery1);
			while (rs.next()==true)
			{
				iOCNCount++;
				int iOCN = rs.getInt(1);
				//System.out.println("Getting stats for OCN="+iOCN);
				String strSt = rs.getString(2);
				String strNm = rs.getString(3);
				int iSLA = rs.getInt(4);
				int iCmp = rs.getInt(5);
				long lSLAInSeconds = iSLA * DAY_IN_SEC;
				int iCompleted = 0;
				int iSubmitted = 0;
				int iRejected = 0;
				int iWithinFOC = 0;
				int iPastFOC = 0;
				long lSLAAccumulation = 0;      //this is total seconds
				long lSLA = 0;
				int iState = 0;
				String strSLAEndDTS = "";
				String strSLABeginDTS = "";

				// Get # submitted by these guys
   				pstmt2.setInt(1, iOCN);
				pstmt2.setString(2, strSt);
				ResultSet rs2 = pstmt2.executeQuery();
				if (rs2.next()==true)
				{
					iSubmitted = rs2.getInt(1);
				}
				else
				{
					iSubmitted = 0;
				}

				// Start building stats for this OCN-State
   				pstmt.setInt(1, iOCN);
				pstmt.setString(2, strSt);
				rs2 = pstmt.executeQuery();

				String strRqstTypCd = "";
				String strRqstSqncNmbr = "";
				String strPrevRqstSqncNmbr = "";

				Integer iMultiFoc = new Integer(0);
				Integer iMultiRej = new Integer(0);
				int iMultiTotal = 0;

                                strDetailLine="";       //We build the HTML line

				while(rs2.next()==true)
				{
					String strStatus = rs2.getString(2);
					strRqstSqncNmbr = rs2.getString(1);
					strRqstTypCd = rs2.getString(4);

					//System.out.println("SLA REQ: " + strRqstSqncNmbr + "  STATUS=" + strStatus+
					//			 " at "+rs2.getString(3));
					// If this is a multi-order, get the num foc and rej
					if (strRqstTypCd.equals("M") && !strRqstSqncNmbr.equals(strPrevRqstSqncNmbr))
					{
						Vector vFocRej = SLATools.getMultiFocRej(strRqstSqncNmbr, strStatus);
						iMultiFoc = (Integer)vFocRej.elementAt(0);
						iMultiRej = (Integer)vFocRej.elementAt(1);
						iMultiTotal = iMultiFoc.intValue() + iMultiRej.intValue();
						strPrevRqstSqncNmbr = strRqstSqncNmbr;
					}

					if ( strStatus.equals("FOC") || strStatus.equals("REJECTED") )
					{
						//First make sure this history record is for the month we are reporting on!
						strSLAEndDTS = rs2.getString(3);
			//calCutOff.set(Integer.parseInt(strNewBeginDate.substring(0,4)), Integer.parseInt(strNewBeginDate.substring(5,6)) - 1, 1, 0,0,0);
						calTemp.set(Integer.parseInt(strSLAEndDTS.substring(0,4)), Integer.parseInt(strSLAEndDTS.substring(5,6)) - 1, 1, 0,0,0);
						if (calTemp.before(calCutOff))	//skip this one, its before our given report date range
						{
							System.out.println("Skipping this one " + strRqstSqncNmbr + " " + strSLAEndDTS);
							continue;
						}

						if (strRqstTypCd.equals("M"))
						{
							iCompleted += iMultiFoc.intValue();
							System.out.println("Multi FOC Count + " + iMultiFoc.intValue() + " : " + lPrevSeqNmbr);
							iRejected += iMultiRej.intValue();
							System.out.println("Multi REJ Count + " + iMultiRej.intValue() + " : " + lPrevSeqNmbr);
						}
						else if ( strStatus.equals("FOC") && iState == 0 )
						{
							iCompleted++;
							//System.out.println("++FOC Count : " + iCompleted);
						}
						else if ( strStatus.equals("REJECTED") && iState == 0 )
						{	iRejected++;
							//System.out.println("++Reject Count : " + iRejected);
						}

						strSLAEndDTS = rs2.getString(3);
						iState = 1;
						lPrevSeqNmbr = rs2.getInt(1);
					}
					if ( strStatus.equals("SUBMITTED") && iState == 1 )
					{
						lSeqNmbr = rs2.getInt(1);
						if (lSeqNmbr == lPrevSeqNmbr)
						{
							strSLABeginDTS = rs2.getString(3);
							iState = 0;
							//Calculate SLA
							String strSLA = SLATools.getSLAStartDateTime(strSLABeginDTS.substring(0,8), strSLABeginDTS.substring(9,15));
							strSLABeginDTS = strSLA;
							lSLA = SLATools.calculateSLA(strSLABeginDTS, strSLAEndDTS);
							lSLAAccumulation = lSLAAccumulation + lSLA;
							//System.out.println(">>SLA for request " + lSeqNmbr + " = " + lSLA + " seconds");

							if (strRqstTypCd.equals("M"))
							{
								if (lSLA <= lSLAInSeconds) {
									iWithinFOC += iMultiTotal; }
								else {
									iPastFOC += iMultiTotal; }
							}
							else
							{
								if (lSLA <= lSLAInSeconds) {
									iWithinFOC++; }
								else {
									iPastFOC++; }
							}
						}
						else
						{	iState = 0;	//this should never happen
						}
					}
				}   //while()
				rs2.close();
				rs2 = null;

				iTotal = iCompleted + iRejected;
                                strDetailLine = "<tr><td>&nbsp;"+OCNfmt.format(iOCN)+"&nbsp;</td>"+
					"<td align=center>&nbsp;"+strSt+"&nbsp;</td><td>&nbsp;"+strNm+"&nbsp;</td>"+
					"<td align=right>"+iSubmitted+"</td>" +
					"<td align=right>"+iCompleted+"</td><td align=right>"+iRejected+
					"</td><td align=right>"+iTotal+"</td>";

				if (iTotal > 0)
				{
					strDetailLine += "<td align=right>&nbsp;"+(iRejected*100)/iTotal+"&nbsp;</td>"+
					 "<td align=right>&nbsp;"+(iRejected*100)/iSubmitted+"&nbsp;</td>";
				}
				else
				{
					strDetailLine += "<td align=right>&nbsp;</td><td align=right>&nbsp;</td>";
				}
				strDetailLine += "<td align=right>";

				if (iTotal > 0)
				{
					lSLAAverage = lSLAAccumulation/iTotal;
					//System.out.println("SLAReport: SLA Average= " + lSLAAccumulation + "/" + iTotal +
					//			  " = " + lSLAAverage);
					//put in xd xh xm format
					lDay = lSLAAverage / DAY_IN_SEC;
					lSLAAverage %= DAY_IN_SEC;
					lHour = lSLAAverage / HOUR_IN_SEC;
					lSLAAverage %= HOUR_IN_SEC;
					lMin = lSLAAverage / MIN_IN_SEC;
					strDetailLine += "&nbsp;"+lDay+"d&nbsp;"+lHour+"h&nbsp;"+lMin+"m&nbsp;";
				}
				else
				{
					strDetailLine += "&nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;";
				}
				strDetailLine += "</td><td align=center>"+iSLA+"</td>"+
					"<td align=right>"+iWithinFOC+"</td><td align=right>"+iPastFOC+"</td>";

				if (iTotal > 0)
				{
					strDetailLine += "<td align=right>&nbsp;"+(iWithinFOC*100)/iTotal+
						"&nbsp;</td><tr>";
				}
				else
				{
					strDetailLine += "<td align=right>&nbsp;</td><tr>";
				}
				storeDetail(rptInfo.getReport(), strNewRunDate, iCmp, iOCN, strDetailLine, iLineCount++);

				iSubmittedTotals += iSubmitted;
				iCompletedTotals += iCompleted;
				iRejectedTotals += iRejected;
				lSLATotals += lSLAAccumulation;
				iWithinFOCTotals += iWithinFOC;
				iPastFOCTotals += iPastFOC;
			}//while
			//now for the grand totals
			iTotal = iCompletedTotals+iRejectedTotals;
			if (iTotal > 0)
			{       lSLAAverage =  lSLATotals/iTotal;
			}
			else
			{       lSLAAverage=0;
			}
        		lDay = lSLAAverage / DAY_IN_SEC;
			lSLAAverage %= DAY_IN_SEC;
			lHour = lSLAAverage / HOUR_IN_SEC;
			lSLAAverage %= HOUR_IN_SEC;
			lMin = lSLAAverage / MIN_IN_SEC;

			strDetailLine = "<tr><td colspan=2><b>TOTALS</b></td>"+
				"<td align=center><b>"+iOCNCount+"&nbsp;VENDORS</b></td>"+
				"<td align=right><b>"+iSubmittedTotals+"</b></td>"+
				"<td align=right><b>"+iCompletedTotals+"</b></td>"+
				"<td align=right><b>"+iRejectedTotals+"</b></td>"+
				"<td align=right><b>"+iTotal+"</b></td>";
		        if (iTotal > 0)
        		{
        			strDetailLine+="<td align=right><b>&nbsp;"+ (iRejectedTotals*100)/iTotal +"&nbsp;</b></td>"+
					"<td align=right><b>&nbsp;"+ (iRejectedTotals*100)/iSubmittedTotals +"&nbsp;</b></td>";
        		}
			else
			{
        			strDetailLine+="<td align=right>&nbsp;</td><td align=right>&nbsp;</td>";
			}
			strDetailLine +="<td align=right><b>"+lDay+"d&nbsp;"+lHour+"h&nbsp;"+lMin+"m</b></td>"+
				"<td align=right>&nbsp;</td><td align=right><b>"+iWithinFOCTotals+"</b></td>"+
				"<td align=right><b>"+iPastFOCTotals+"</b></td>";
        		if (iTotal > 0)
			{
				strDetailLine+="<td align=right><b>&nbsp;"+
					(iWithinFOCTotals*100)/iTotal + "&nbsp;</b></td>";
			}
			else
			{
				strDetailLine+="<td align=right>&nbsp;</td>";
			}
			strDetailLine+="<tr>";

			storeDetail(rptInfo.getReport(), strNewRunDate, PROVIDER_ONLY, ALL_OCNS, strDetailLine, iLineCount++);

			System.out.println("Done with report, about to write ACTVTY record");
                        //OK, we're now done creating report, so put Activity record out there so users can get to it...
                        //Insert activity record into table
                        String strInsert="INSERT INTO VENDOR_REPORT_ACTVTY_T (RPRT_SQNC_NMBR, RPRT_RUN_DT, RPRT_BGN_DT, "+
				"RPRT_END_DT, MDFD_DT, MDFD_USERID) VALUES ("+rptInfo.getReport()+", " +
				" TO_DATE('"+strNewRunDate+"','YYYYMMDD'), TO_DATE('"+strNewBeginDate+"','YYYYMMDD'),"+
				" TO_DATE('"+strNewEndDate+" 23:59:59','YYYYMMDD HH24:MI:SS'), SYSDATE, 'batch')";
                        stmt1.executeUpdate(strInsert);
		}
		catch(Exception e) {
			System.out.println("Exception ["+e+"] caught in doSLAReport()");
			try { con.rollback();	}
			catch(Exception e2) {
				System.out.println("Exception in rollback() too");
			}
		}
		finally {
		}
		System.out.println("=============================================");
		return 0;
	}

	/**
	* ---------------------------------------------
	* P-3 Percent FOC Due Dates Misses, also
	* know as "Completed Orders" report.
	* ---------------------------------------------
	*/
	static int doReport3(ReportInfo rptInfo)
	{
		String strNewBeginDate="";
		String strNewEndDate="";
		String strNewRunDate="";
		String strDetailLine="";

		int     iLineCount = 0;
		int     iOCNCount = 0;
		System.out.println("=============================================");
		System.out.println("Doing report 3 ");
		try {
			System.out.println("rpt="+rptInfo.getReport());
			System.out.println("beg="+rptInfo.getBeginDate());
			System.out.println("end="+rptInfo.getEndDate());

			String strQry1 = "SELECT TO_CHAR(ADD_MONTHS(TO_DATE('" + rptInfo.getBeginDate() + "','YYYYMMDD'),1),'YYYYMMDD')," +
			  	   " TO_CHAR(LAST_DAY(ADD_MONTHS(TO_DATE('" + rptInfo.getBeginDate() + "','YYYYMMDD'),1)), 'YYYYMMDD')," +
				   " TO_CHAR(SYSDATE,'YYYYMMDD') FROM DUAL";
			rs = stmt1.executeQuery(strQry1);
			if (rs.next())
			{
				strNewBeginDate = rs.getString(1);
				strNewEndDate = rs.getString(2);
				strNewRunDate = rs.getString(3);
			}
			else
			{
				System.out.println("PROBLEM");
			}
		}
		catch(Exception e) {
			System.out.println("Exception ["+e+"] caught in doReport3()");
			try { con.rollback();	}
			catch(Exception e2) {
				System.out.println("Exception in rollback() too");
			}
		}
		finally {
		}
		System.out.println("=============================================");
		return 0;

	}

}
