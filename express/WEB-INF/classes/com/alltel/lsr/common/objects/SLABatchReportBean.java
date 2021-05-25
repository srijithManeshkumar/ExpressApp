/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			ALLTEL COMMUNICATIONS INC.
 */
/**
 * MODULE:	SLABatchReportBean.java
 *
 * DESCRIPTION: SLA batch report bean - cloned SLA report into this object to be able to run &
 *		and schedule in batch
 *
 * AUTHOR:      psedlak
 *
 * DATE:        2-20-2005
 *
 * HISTORY:
 *	02/20/2005	psedlak Yanked from JSP to this class.
 *	3-9-2005 	psedlak Fix array issue
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class SLABatchReportBean
{
	public SLABatchReportBean()
	{
		m_strStartYr="2005";
		m_strStartMth="";
		m_strStartDay="01";
		m_strStartDate = m_strStartYr + m_strStartMth + m_strStartDay;
		m_strEndYr="2005";
		m_strEndMth="";
		m_strEndDay="01";
		m_strEndDate = m_strEndYr + m_strEndMth + m_strEndDay;
		m_strOCN_CDs= null;
		m_strSTATE_CDs = null;
		m_strVENDORs = null;
		m_strSRVC_TYP_CDs = null;
	}

	private final static long DAY_IN_SEC = (long) 86400;
	private final static long HOUR_IN_SEC = (long) 3600;
	private final static long MIN_IN_SEC = (long) 60;
	private final static String SECURITY_OBJECT = "PROV_REPORTS";

	private String m_strStartYr;
	private String m_strStartMth;
	private String m_strStartDay;
	private String m_strStartDate;
	private String m_strEndYr;
	private String m_strEndMth;
	private String m_strEndDay;
	private String m_strEndDate;

	private String[] m_strOCN_CDs;
	private String[] m_strSTATE_CDs = null;
	private String[] m_strVENDORs = null;
	private	String[] m_strSRVC_TYP_CDs = null;

	public void setStartDate(String strStartYYYYMMDD)
	{
		//set yr, mth, day pieces here...
		m_strStartYr = strStartYYYYMMDD.substring(0,4);
		m_strStartMth = strStartYYYYMMDD.substring(4,6);
		m_strStartDay = strStartYYYYMMDD.substring(6,8);
		m_strStartDate =strStartYYYYMMDD;
	}
	public void setStartYr(String strStartYr)
	{
		m_strStartYr =strStartYr;
		m_strStartDate = m_strStartYr + m_strStartMth + m_strStartDay;
	}
	public void setStartMth(String strStartMth)
	{
		m_strStartMth =strStartMth;
		m_strStartDate = m_strStartYr + m_strStartMth + m_strStartDay;
	}
	public void setStartDay(String strStartDay)
	{
		m_strStartDay =strStartDay;
		m_strStartDate = m_strStartYr + m_strStartMth + m_strStartDay;
	}
	public void setEndDate(String strEndYYYYMMDD)
	{
		//set yr, mth, day pieces here...
		m_strEndDate =strEndYYYYMMDD;
		m_strEndYr = strEndYYYYMMDD.substring(0,4);
		m_strEndMth = strEndYYYYMMDD.substring(4,6);
		m_strEndDay = strEndYYYYMMDD.substring(6,8);
	}
	public void setEndYr(String strEndYr)
	{
		m_strEndYr =strEndYr;
		m_strEndDate = m_strEndYr + m_strEndMth + m_strEndDay;
	}
	public void setEndMth(String strEndMth)
	{
		m_strEndMth =strEndMth;
		m_strEndDate = m_strEndYr + m_strEndMth + m_strEndDay;
	}
	public void setEndDay(String strEndDay)
	{
		m_strEndDay =strEndDay;
		m_strEndDate = m_strEndYr + m_strEndMth + m_strEndDay;
	}
	public String getStartDate(){
		return m_strStartDate;
	}
	public String getStartYr(){
		return m_strStartYr;
	}
	public String getStartMth(){
		return m_strStartMth;
	}
	public String getStartDay(){
		return m_strStartDay;
	}
	public String getEndDate(){
		return m_strEndDate;
	}
	public String getEndYr(){
		return m_strEndYr;
	}
	public String getEndMth(){
		return m_strEndMth;
	}
	public String getEndDay(){
		return m_strEndDay;
	}


	public void setOCNs( String[] strList )
	{
                String strTemp = "";
                if ( strList != null )
                {	m_strOCN_CDs = new String[strList.length];
                        for(int x=0;  x < strList.length; x++ )
                        {
                                strTemp = strList[x].trim();
                                if(strTemp.length() > 0)
                                {
                                        m_strOCN_CDs[x] = strTemp;
                                        strTemp = "";
                                }
                        }
                }

	}
	public String[] getOCNs() {
		return m_strOCN_CDs;
	}
	public void setSTATE_CDs( String[] strList )
	{
                String strTemp = "";
                if ( strList != null )
                {	m_strSTATE_CDs = new String[strList.length];
                        for(int x=0;  x < strList.length; x++ )
                        {
                                strTemp = strList[x].trim();
                                if(strTemp.length() > 0)
                                {
                                        m_strSTATE_CDs[x] = strTemp;
                                        strTemp = "";
                                }
                        }
                }
	}
	public String[] getSTATE_CDs() {
		return m_strSTATE_CDs;
	}
	public void setVENDORs( String[] strList )
	{
                String strTemp = "";
                if ( strList != null )
                {	m_strVENDORs = new String[strList.length];
                        for(int x=0;  x < strList.length; x++ )
                        {
                                strTemp = strList[x].trim();
                                if(strTemp.length() > 0)
                                {
                                        m_strVENDORs[x] = strTemp;
                                        strTemp = "";
                                }
                        }
                }
	}
	public String[] getVENDORs() {
		return m_strVENDORs;
	}
	public void setSRVC_TYP_CDs( String[] strList )
	{
                String strTemp = "";
                if ( strList != null )
                {	m_strSRVC_TYP_CDs = new String[strList.length];
                        for(int x=0;  x < strList.length; x++ )
                        {
                                strTemp = strList[x].trim();
                                if(strTemp.length() > 0)
                                {
                                        m_strSRVC_TYP_CDs[x] = strTemp;
                                        strTemp = "";
                                }
                        }
                }
	}
	public String[] getSRVC_TYP_CDs() {
		return m_strSRVC_TYP_CDs;
	}


	public String runReport() throws Exception
	{
		Connection con = null;
		try {
			con = DatabaseManager.getConnection();
		}
		catch(Exception e) {
			//Log.write(Log.ERROR, "SLAReportBean error getting DB connection or creating stmt");
			throw new Exception("Error getting db connection 1 ");
		}
		if (con == null) return null;
		String strReport = runReport(con);
		DatabaseManager.releaseConnection(con);
		return strReport;
	}

	public String runReport(Connection con) throws Exception
	{
		StringBuffer nstrBuff = new StringBuffer();
		Statement  stmt = null;
		int     iOCNCount = 0;
		int     iCompletedTotals = 0;
		int 	iRejectedTotals = 0;
		int     iWithinFOCTotals = 0;
		int     iPastFOCTotals = 0;
		long 	lSLATotals = 0;
		long    lSLAAverage = 0;

		if ((m_strStartYr.length() == 0) || (m_strStartMth.length()==0) || (m_strStartDay.length()==0))
		{
			throw new Exception("Invalid SLA start date");
		}
		if ((m_strEndYr.length() == 0) || (m_strEndMth.length()==0) || (m_strEndDay.length()==0))
		{
			throw new Exception("Invalid SLA end date");
		}
		if ( m_strStartDate.compareTo(m_strEndDate) > 0 )
		{
			throw new Exception("'From Date' must be less than or equal to 'To Date'!");
		}
		//Check days of month and adjust if necessary ...
		Calendar calStart = Calendar.getInstance();
		calStart.set(Integer.parseInt(m_strStartYr),  Integer.parseInt(m_strStartMth) - 1, Integer.parseInt(m_strStartDay), 0, 0, 0);
		int iMaxDays = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
		//Log.write("iMaxDays ToDate=" + iMaxDays + "  Startdate=" + m_strStartYr + m_strStartMth + m_strStartDay);
		if (Integer.parseInt(m_strStartDay)  > iMaxDays)
		{
			throw new Exception("'From Date' - invalid day of month selected");
		}
		Calendar calEnd = Calendar.getInstance();
		calEnd.set(Integer.parseInt(m_strEndYr),  Integer.parseInt(m_strEndMth) - 1, Integer.parseInt(m_strEndDay), 23, 59, 59);
		iMaxDays = calEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
		//Log.write("iMaxDays FromDate=" + iMaxDays + "  Enddate=" + m_strEndYr + m_strEndMth + m_strEndDay);
		if (Integer.parseInt(m_strEndDay)  > iMaxDays)
		{
			throw new Exception("'To Date' - invalid day of month selected");
		}
		Calendar calTemp = Calendar.getInstance();
		DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
		//Log.write(Log.DEBUG_VERBOSE, "SLA Date:" + m_strStartDate + " Date:" + m_strEndDate);

		Statement stmt2 = null;
		ResultSet rs2 = null;
		try {
			//con = DatabaseManager.getConnection();
			stmt2 = con.createStatement();
		}
		catch(Exception e) {
			//Log.write(Log.ERROR, "SLAReportBean error creating stmt");
			throw new Exception("Error getting db connection 1 ");
		}

		// HDR 1071942 -additional criteria
		String strOCN_CD="";
		String strSTATE_CD="";
		String strVENDOR="";
		String strSRVC_TYP_CD="";

		String strHeaderCriteria = "<center><b>Other report selection criteria:&nbsp;&nbsp;&nbsp;</b>";
		String strTemp = "";
		boolean bPickedAll = true;

		String strOCN_CD_Where = "";
		if ( m_strOCN_CDs != null )
		{	strTemp = strHeaderCriteria;//save it
			strHeaderCriteria += " <b>OCN:</b>&nbsp;";
			strOCN_CD_Where = " AND ( ";
			for (int j=0;  j<m_strOCN_CDs.length; j++)
			{	strOCN_CD = m_strOCN_CDs[j];
				if ( !strOCN_CD.equals("ALL") && (strOCN_CD.length()> 3) )
				{	bPickedAll = false;
					//NOTE this parm will have <ocn>-<company seq #> format, so parse it first
					int i = strOCN_CD.lastIndexOf("-");
					if (i !=  -1)
					{       strOCN_CD_Where += " (O.OCN_CD ='" + strOCN_CD.substring(0,i) +
								   "' AND O.CMPNY_SQNC_NMBR=" + strOCN_CD.substring(i+1)+") OR ";
						strHeaderCriteria += strOCN_CD.substring(0,i) + "&nbsp;&nbsp;&nbsp";
					}
				}
				else
				{	strHeaderCriteria = strTemp;
					bPickedAll = true;
					break;
				}
			}
			if (bPickedAll) {	strOCN_CD_Where = "";
			}
			else {	//get rid of dangling OR
				strOCN_CD_Where = strOCN_CD_Where.substring(0,strOCN_CD_Where.length()-3);
				strOCN_CD_Where += " ) ";
			}
		}
		else
		{  strOCN_CD="ALL";
		}
		//Log.write(Log.DEBUG_VERBOSE, "OCN where =[" + strOCN_CD_Where + "]");

		bPickedAll = true;
		String strSTATE_CD_Where = "";
		if ( m_strSTATE_CDs != null )
		{	strTemp = strHeaderCriteria;//save it
			strHeaderCriteria += " <b>State:</b>&nbsp;";
			strSTATE_CD_Where = " AND ( ";
			for (int j=0;j<m_strSTATE_CDs.length;j++)
			{	strSTATE_CD = m_strSTATE_CDs[j];
				if ( !strSTATE_CD.equals("ALL") )
				{	bPickedAll = false;
					strSTATE_CD_Where += " (OS.STT_CD = '" + strSTATE_CD + "') OR ";
					strHeaderCriteria += "&nbsp;" + strSTATE_CD + "&nbsp;&nbsp;&nbsp";
				}
				else {	strHeaderCriteria = strTemp;
					bPickedAll = true;
					break;
				}
			}
			if (bPickedAll) {	strSTATE_CD_Where = "";
			}
			else {	//get rid of dangling OR
				strSTATE_CD_Where = strSTATE_CD_Where.substring(0,strSTATE_CD_Where.length()-3);
				strSTATE_CD_Where += " ) ";
			}
		}
		else
		{	strSTATE_CD="ALL";
		}
		//Log.write(Log.DEBUG_VERBOSE, "State where =[" + strSTATE_CD_Where+ "]");

		bPickedAll = true;
		String strVENDOR_Where = "";
		if ( m_strVENDORs != null )
		{	strTemp = strHeaderCriteria;//save it
			strHeaderCriteria += " <b>Vendor:</b>&nbsp;";
			strVENDOR_Where = " AND ( ";
			for (int j=0;j<m_strVENDORs.length;j++)
			{	strVENDOR = m_strVENDORs[j];
				if ( !strVENDOR.equals("ALL") )
				{	bPickedAll = false;
					strVENDOR_Where += " (O.CMPNY_SQNC_NMBR=" + strVENDOR + ") OR ";
					rs2 = stmt2.executeQuery("SELECT CMPNY_NM FROM COMPANY_T WHERE CMPNY_SQNC_NMBR=" + strVENDOR);
					if (rs2.next()==true)
					{	strHeaderCriteria += "&nbsp;" + rs2.getString("CMPNY_NM") + "&nbsp;&nbsp;&nbsp";
					}
				}
				else {	strHeaderCriteria = strTemp;
					bPickedAll = true;
					break;
				}
			}
			if (bPickedAll) {	strVENDOR_Where = "";
			}
			else {	//get rid of dangling OR
				strVENDOR_Where = strVENDOR_Where.substring(0,strVENDOR_Where.length()-3);
				strVENDOR_Where += " ) ";
			}
		}
		//Log.write(Log.DEBUG_VERBOSE, "Vendor where =[" + strVENDOR_Where + "]");

		bPickedAll = true;
		String strSRVC_TYP_CD_Where = "";
		if ( m_strSRVC_TYP_CDs != null )
		{	strTemp = strHeaderCriteria;//save it
			strHeaderCriteria += " <b>Service Type:</b>&nbsp;";
			strSRVC_TYP_CD_Where = " AND ( ";
			for (int j=0;j<m_strSRVC_TYP_CDs.length;j++)
			{	strSRVC_TYP_CD = m_strSRVC_TYP_CDs[j];
				if ( !strSRVC_TYP_CD.equals("ALL") )
				{	bPickedAll = false;
					strSRVC_TYP_CD_Where += " (R.SRVC_TYP_CD='" + strSRVC_TYP_CD +"') OR ";
					rs2 = stmt2.executeQuery("SELECT SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T WHERE TYP_IND='R' AND SRVC_TYP_CD='" +
								 strSRVC_TYP_CD + "' ");
					if (rs2.next()==true)
					{	strHeaderCriteria += "&nbsp;" + rs2.getString("SRVC_TYP_DSCRPTN") + "&nbsp;&nbsp;&nbsp";
					}
				}
				else {	strHeaderCriteria = strTemp;
					bPickedAll = true;
					break;
				}
			}
			if (bPickedAll) {	strSRVC_TYP_CD_Where = "";
			}
			else {	//get rid of dangling OR
				strSRVC_TYP_CD_Where = strSRVC_TYP_CD_Where.substring(0,strSRVC_TYP_CD_Where.length()-3);
				strSRVC_TYP_CD_Where += " ) ";
			}
		}
		//Log.write(Log.DEBUG_VERBOSE, "SrvTyp where =[" + strSRVC_TYP_CD_Where + "]");

		strHeaderCriteria += "</center>";

		String strQuery1 = "SELECT O.OCN_CD, OS.STT_CD, O.OCN_NM, OS.OCN_STT_SLA_DYS, C.CMPNY_SQNC_NMBR " +
			" FROM OCN_T O, OCN_STATE_T OS, COMPANY_T C  WHERE O.OCN_CD = OS.OCN_CD AND O.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR " +
			" AND C.CMPNY_TYP IN ('R','C','L') " + strOCN_CD_Where + strSTATE_CD_Where + strVENDOR_Where +
			" ORDER BY OS.STT_CD, O.OCN_CD ";
		//Log.write(Log.DEBUG_VERBOSE, "OCN query =[" + strQuery1 + "]");

		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(strQuery1);

	nstrBuff.append("<br><center><SPAN CLASS=\"header1\"> S&nbsp;L&nbsp;A&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R&nbsp;S&nbsp;</SPAN>" +
		"<br><b>Date&nbsp;Range:&nbsp;" + m_strStartMth + "/" + m_strStartDay + "/" + m_strStartYr +
		"&nbsp;-&nbsp;" + m_strEndMth + "/" + m_strEndDay + "/" + m_strEndYr +
		"</b><br>Effective:&nbsp;" + dFmt.format(new java.util.Date()) + "<br></center><br> <br>");

	//This is just to throw FYI on report that additinoal criterium was used to produce results....
	if ( (strOCN_CD_Where.length() > 0) || (strSTATE_CD_Where.length() > 0) || (strVENDOR_Where.length() > 0) || (strSRVC_TYP_CD_Where.length() > 0) )
	{
		nstrBuff.append(strHeaderCriteria);
	}
	nstrBuff.append("<br><table border=1 align=center cellspacing=0 cellpadding=1>" +
		"<tr><th align=center>&nbsp;OCN&nbsp;</th> <th align=center>VENDOR</th> <th align=center>&nbsp;FOCed&nbsp;</th> <th align=center>&nbsp;REJ&nbsp;</th> <th align=center>&nbsp;TOTAL&nbsp;</th> <th align=center>&nbsp;%&nbsp;REJECTED&nbsp;</th> <th align=center>&nbsp;FOC&nbsp;INT&nbsp;</th> <th align=center>&nbsp;FOC<br>&nbsp;SLA&nbsp;</th> <th align=center>&nbsp;Within<br>&nbsp;SLA&nbsp;</th> <th align=center>&nbsp;Past<br>&nbsp;SLA&nbsp;</th> <th align=center>&nbsp;%&nbsp;within<br>&nbsp;SLA&nbsp;</th> </tr>");

		long lPrevSeqNmbr = 0;
		long lSeqNmbr = 0;
		long    lDay = 0;
		long    lHour = 0;
		long    lMin = 0;
		int     iTotal = 0;
		DecimalFormat OCNfmt = new DecimalFormat("0000");
		while(rs.next()==true)
		{
			iOCNCount++;
			//int iOCN = rs.getInt("OCN_CD");
			String strOCN = rs.getString("OCN_CD");
			String strSt = rs.getString("STT_CD");
			String strNm = rs.getString("OCN_NM");
			String strCmpnySqncNmbr = rs.getString("CMPNY_SQNC_NMBR");
			int iSLA = rs.getInt("OCN_STT_SLA_DYS");
			long lSLAInSeconds = iSLA * DAY_IN_SEC;
			int iCompleted = 0;
			int iRejected = 0;
			int iWithinFOC = 0;
			int iPastFOC = 0;
			long lSLAAccumulation = 0;      //this is total seconds
			long lSLA = 0;
			int iState = 0;
			String strSLAEndDTS = "";
			String strSLABeginDTS = "";

			//Log.write(Log.DEBUG_VERBOSE, "SLA OCN=" + strOCN + " Cmp:"+ strCmpnySqncNmbr + " St:"+strSt+ " Dt1:" +m_strStartDate+" Dt2:"+m_strEndDate+"-------");
			// Start building stats for this OCN-State  && COMPANY SEQ #
			String strOCNQuery =
			  "SELECT RH.RQST_SQNC_NMBR, RH.RQST_STTS_CD_IN, TO_CHAR(RH.RQST_HSTRY_DT_IN,'YYYYMMDD HH24MISS'), R.RQST_TYP_CD " +
			  " FROM REQUEST_T R, REQUEST_HISTORY_T RH WHERE R.OCN_CD = '" + strOCN + "' AND R.OCN_STT = '" + strSt + "' AND " +
			  " R.CMPNY_SQNC_NMBR=" + strCmpnySqncNmbr + strSRVC_TYP_CD_Where +
			  " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN <> RH.RQST_STTS_CD_OUT " +
			  " AND RH.RQST_STTS_CD_IN IN ('SUBMITTED','FOC','REJECTED') " +
			  " AND EXISTS (SELECT RH2.RQST_SQNC_NMBR FROM REQUEST_HISTORY_T RH2 " +
					" WHERE RH2.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR " +
					" AND RH2.RQST_STTS_CD_IN IN ('FOC', 'REJECTED') " +
					" AND RH2.RQST_HSTRY_DT_IN BETWEEN " + " TO_DATE('" + m_strStartDate + " 00:00:00', 'YYYYMMDD HH24:MI:SS') AND " +
					" TO_DATE('" + m_strEndDate + " 23:59:59', 'YYYYMMDD HH24:MI:SS') ) " +
			  " ORDER BY RH.RQST_SQNC_NMBR, RH.RQST_HSTRY_DT_IN DESC";

	//Log.write(Log.DEBUG_VERBOSE, "OCN specific query=["+ strOCNQuery + "]");

			rs2 = stmt2.executeQuery(strOCNQuery);

			String strRqstTypCd = "";
			String strRqstSqncNmbr = "";
			String strPrevRqstSqncNmbr = "";

			Integer iMultiFoc = new Integer(0);
			Integer iMultiRej = new Integer(0);
			int iMultiTotal = 0;
			String strTEST;

			while(rs2.next()==true)
			{
				String strStatus = rs2.getString("RQST_STTS_CD_IN");
				strRqstSqncNmbr = rs2.getString("RQST_SQNC_NMBR");
				strRqstTypCd = rs2.getString("RQST_TYP_CD");

				System.out.println("SLA REQ: " + strRqstSqncNmbr + "  STATUS=" + strStatus);
				strTEST= rs2.getString(3);
				calTemp.set(Integer.parseInt(strTEST.substring(0,4)),
                                     Integer.parseInt(strTEST.substring(4,6)) - 1,
                                     Integer.parseInt(strTEST.substring(6,8)),
                                     Integer.parseInt(strTEST.substring(9,11)),
                                     Integer.parseInt(strTEST.substring(11,13)),
                                     Integer.parseInt(strTEST.substring(13,15)) );
	           if ( (iState==0) && (calTemp.before(calStart)) )
       				{       //we're done with this OCN....
					System.out.println("SLA REQ: skip 1");
					continue;
				}
	                        if ( (iState==0) && (calTemp.after(calEnd)) )
       				{       //records too early, bypass em
					System.out.println("SLA REQ: skip 1b");
					continue;
				}


				// If this is a multi-order, get the num foc and rej
				if (strRqstTypCd.equals("M") && !strRqstSqncNmbr.equals(strPrevRqstSqncNmbr))
				{
					Vector vFocRej = SLATools.getMultiFocRej(strRqstSqncNmbr, strStatus,con);

					iMultiFoc = (Integer)vFocRej.elementAt(0);
					iMultiRej = (Integer)vFocRej.elementAt(1);
					iMultiTotal = iMultiFoc.intValue() + iMultiRej.intValue();

					strPrevRqstSqncNmbr = strRqstSqncNmbr;
				}

				if ( strStatus.equals("FOC") || strStatus.equals("REJECTED") )
				{
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
						System.out.println("++FOC Count : " + lPrevSeqNmbr);
					}
					else if ( strStatus.equals("REJECTED") && iState == 0 )
					{	iRejected++;
						System.out.println("++Reject Count : " + lPrevSeqNmbr);
					}

					strSLAEndDTS = rs2.getString(3);
					iState = 1;
					lPrevSeqNmbr = rs2.getInt("RQST_SQNC_NMBR");
				}
				if ( strStatus.equals("SUBMITTED") && iState == 1 )
				{
					lSeqNmbr = rs2.getInt("RQST_SQNC_NMBR");
					if (lSeqNmbr == lPrevSeqNmbr)
					{
						strSLABeginDTS = rs2.getString(3);
						iState = 0;
						//Calculate SLA
						String strSLA = SLATools.getSLAStartDateTime(strSLABeginDTS.substring(0,8), strSLABeginDTS.substring(9,15), con);
						strSLABeginDTS = strSLA;
						lSLA = SLATools.calculateSLA(strSLABeginDTS, strSLAEndDTS, con);
						lSLAAccumulation = lSLAAccumulation + lSLA;
						System.out.println(">>SLA for request " + lSeqNmbr + " = " + lSLA + " seconds");
						//Log.write(Log.DEBUG_VERBOSE, "SLA running total:" + lSLAAccumulation);

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

			nstrBuff.append("<tr><td>" + strOCN + "-" + strSt + "</td><td>" + strNm +
				"</td><td align=right>" + iCompleted + "</td><td align=right>" +
				 iRejected + "</td><td align=right>" + iTotal + "</td>");

			if (iTotal > 0)
			{
				nstrBuff.append("<td align=right>&nbsp;" + (iRejected*100)/iTotal + "&nbsp;</td>");
			}
			else
			{
				nstrBuff.append("<td align=right>&nbsp;</td>");
			}
			nstrBuff.append("<td align=right>");

			if (iTotal > 0)
			{
				lSLAAverage = lSLAAccumulation/iTotal;
				//Log.write(Log.DEBUG, "SLAReport: SLA Average= " + lSLAAccumulation + "/" + iTotal +  " = " + lSLAAverage);
				//put in xd xh xm format
				lDay = lSLAAverage / DAY_IN_SEC;
				lSLAAverage %= DAY_IN_SEC;
				lHour = lSLAAverage / HOUR_IN_SEC;
				lSLAAverage %= HOUR_IN_SEC;
				lMin = lSLAAverage / MIN_IN_SEC;
				nstrBuff.append("&nbsp;" + lDay + "d&nbsp;" + lHour + "h&nbsp;" + lMin + "m&nbsp;");
			}
			else
			{
				nstrBuff.append("&nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;");
			}
			nstrBuff.append("</td><td align=center>" + iSLA + "</td><td align=right>" + iWithinFOC + "</td><td align=right>" + iPastFOC + "</td>");
			if (iTotal > 0)
			{
			    nstrBuff.append("<td align=right>&nbsp;" + (iWithinFOC*100)/iTotal + "&nbsp;</td> <tr>");
			}
			else
			{
			    nstrBuff.append("<td align=right>&nbsp;</td> <tr>");
			}

			iCompletedTotals += iCompleted;
			iRejectedTotals += iRejected;
			lSLATotals += lSLAAccumulation;
			iWithinFOCTotals += iWithinFOC;
			iPastFOCTotals += iPastFOC;

		} //while()
		iTotal = iCompletedTotals+iRejectedTotals;

		if (iTotal > 0)
		{	lSLAAverage =  lSLATotals/iTotal;
		}
		else
		{	lSLAAverage=0;
		}

		lDay = lSLAAverage / DAY_IN_SEC;
		lSLAAverage %= DAY_IN_SEC;
		lHour = lSLAAverage / HOUR_IN_SEC;
		lSLAAverage %= HOUR_IN_SEC;
		lMin = lSLAAverage / MIN_IN_SEC;

		nstrBuff.append(" <tr> <td><b>TOTALS</b></td> <td align=center><b>" + iOCNCount + "&nbsp;VENDORS</b></td>"+
		" <td align=right><b>" + iCompletedTotals + "</b></td><td align=right><b>" + iRejectedTotals + "</b></td>" +
		" <td align=right><b>" + iTotal + "</b></td>");
		if (iTotal > 0)
		{
			nstrBuff.append("<td align=right><b>&nbsp;" + (iRejectedTotals*100)/iTotal + "&nbsp;</b></td>");
		}
		else
		{
			nstrBuff.append("<td align=right>&nbsp;</td>");
		}
		nstrBuff.append("<td align=right><b>" + lDay + "d&nbsp;" + lHour + "h&nbsp;" + lMin + "m</b></td><td align=right>&nbsp;</td>" +
		"<td align=right><b>" + iWithinFOCTotals + "</b></td><td align=right><b>" + iPastFOCTotals + "</b></td>");
		if (iTotal > 0)
		{
			nstrBuff.append("<td align=right><b>&nbsp;" + (iWithinFOCTotals*100)/iTotal  + "&nbsp;</b></td>");
		}
		else
		{
			nstrBuff.append("<td align=right>&nbsp;</td>");
		}
		nstrBuff.append("<tr> </table>");
		//DatabaseManager.releaseConnection(con);

		nstrBuff.append(" </UL> <BR> <BR> <BR>");
		return nstrBuff.toString();

	}
}


