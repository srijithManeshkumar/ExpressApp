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
 * MODULE:	UserReportBean.java
 * 
 * DESCRIPTION: User statistics/performance report. User picks a date range from UserRptDateSelect.jsp.
 *		The user can also optionally pick to run rpt for a specific userid.
 *		This code was moved from UserReport.jsp to here - so it can be run from batch too.
 * 
 * AUTHOR:      pjs
 * 
 * DATE:        03-12-2005
 * 
 * HISTORY:
 *	03-18-2005 pjs Removed "AND R.RQST_VRSN=RH.RQST_VRSN" from query selection -as it was missing
 *			SUPP-ed requests
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import com.alltel.lsr.common.batch.UserReportInfo;

public class UserReportBean
{

	private final static long DAY_IN_SEC = (long) 86400;
        private final static long HOUR_IN_SEC = (long) 3600;
        private final static long MIN_IN_SEC = (long) 60;
        private final static String SECURITY_OBJECT = "PROV_REPORTS";

	public UserReportBean()
	{
		m_bKeepWeekends = false;
		m_bCountWeekends = false;
	}

	private boolean m_bKeepWeekends;
	private boolean m_bCountWeekends;
        private String m_strStartYr;
        private String m_strStartMth;
        private String m_strStartDay;
        private String m_strStartDate;
        private String m_strEndYr;
        private String m_strEndMth;
        private String m_strEndDay;
        private String m_strEndDate;

	private String[] m_strUserids;
	
	public void setKeepWeekends(boolean tf)
	{	m_bKeepWeekends = tf;
	}
	public boolean getKeepWeekends()
	{	return m_bKeepWeekends;
	}
	public void setCountWeekends(boolean tf)
	{	m_bCountWeekends = tf;
	}
	public boolean getCountWeekends()
	{	return m_bCountWeekends;
	}
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

	public void setUserids( String[] strList )
        {
                String strTemp = "";
                if ( strList != null )
                {       m_strUserids = new String[strList.length];
                        for(int x=0;  x < strList.length; x++ )
                        {
                                strTemp = strList[x].trim();
                                if(strTemp.length() > 0)
                                {
                                        m_strUserids[x] = strTemp;
                                        strTemp = "";
                                }
                        }
                }

        }
        public String[] getUserids() {
                return m_strUserids;
        }


	/* EK. 04/12/2005 
	 * extractEmployeeGroups(), extracts employee groups and rebuilds the 
	 * m_strUserids. 
	 * @PARAM groupIds, Array of Employee groups we want to extract
	 * @PARAM bAllUsers, This flag indicates whether we want to extract all groups that have 
	 * 		users in them. True means ALL groups and false, means selected groups in teh array.	
	 */
	public int  extractEmployeeGroups(  String[] groupIds, boolean bAllUsers )
	{
		int iReturnV = 0;
		Connection conn = null;
		try{
			conn  = DatabaseManager.getConnection();
			iReturnV = extractEmployeeGroups(  conn,  groupIds,  bAllUsers );
		
		} catch ( SQLException SqlE ) {
			SqlE.printStackTrace();
			Log.write(Log.DEBUG_VERBOSE, 
			"UserReportBean.java:extractEmployeeGroups:Caught SQLException=[" + SqlE + "]");
	
		}catch(Exception e) {
			e.printStackTrace();
			Log.write(Log.DEBUG_VERBOSE, 
				"UserReportBean.java:extractEmployeeGroups:Caught  Exception=[" + e + "]");
		}
		finally {
			try {
			} catch (Exception eee) {}
			DatabaseManager.releaseConnection(conn);
		}
		return iReturnV;			
	}
	
	
	/* EK. 04/15/2005
	 * Accommodate batch db connectivity as it doesn't have access to  DatabaseManager connections. 
	 * Connection must be closed by caller... caller also catches connections. See above!
	 */
	public int  extractEmployeeGroups( Connection conn, String[] groupIds, boolean bAllUsers ) 
	throws SQLException, Exception {
        PreparedStatement pstmt = null;
        ResultSet rset = null;      
        String strActiveStatus = "N";
        String strQry = " Select DISTINCT USERID FROM USR_USRGRP_LINK_T WHERE "
        		+ " STATUS = ? ";                		
        Vector vUsers = new Vector( 40 );		 
        String strWhereClause = "";		
        if(  groupIds  == null ){  
         	return 0;                 	       
        }
        int iIdCounter = 0, iTempCnt = 0;
        if( !bAllUsers ){
        	strWhereClause = " AND USRGRP_EMP_SQNC_NMBR IN (";
    		iIdCounter = groupIds.length;
    		if( iIdCounter > iTempCnt )
        	{
        		 iTempCnt++;
        		 strWhereClause += "?";
        	}	
    		while( iIdCounter > iTempCnt )  
    		{        		
    			strWhereClause += ",? ";
    			iTempCnt++;
    		}                
    		strWhereClause += " )"; 
    	}               
    	strQry += strWhereClause;	            	
        pstmt = conn.prepareStatement( strQry  );
        pstmt.clearParameters();
		pstmt.setString( 1, strActiveStatus );
		for( int j = 0; j < iIdCounter;j++ ){
			pstmt.setInt( j+2, Integer.parseInt( groupIds[j] ) );
		}
		rset = pstmt.executeQuery( );	  
		while( rset.next() )
		{
			vUsers.add( rset.getString(1) ); 
		}                         
    	rset.close(); rset=null;
		pstmt.close(); pstmt=null;	
			   
		/******expand user array*********/
		int  iUsrInGroups = vUsers.size();
		int ipos = 0;
		String new_users[] = null;        		
		if(  m_strUserids  == null ){
			new_users = new String[iUsrInGroups];
			ipos = 0;
		}else
		{
			new_users = new String[m_strUserids.length + iUsrInGroups];
			System.arraycopy(m_strUserids,0,new_users,0,m_strUserids.length);
			ipos = m_strUserids.length;
		}
		String strTempId = "";
		for( int i =0; i < iUsrInGroups; i++ )
		{
			strTempId = (String)vUsers.get(i);
			//skip duplicates
			if( !ExpressUtil.isElementOf( m_strUserids, strTempId  ) ) {
				new_users[ipos] = strTempId;
				ipos++;
			}			
		}
		// Trim array
		if( ipos > 0 ) {
			m_strUserids = new String[ipos];
			System.arraycopy( new_users,0,m_strUserids,0,ipos);
		}
		return ipos;
	}

	
	
	public String runReport() throws Exception
        {
                Connection con = null;
                try {
                        con = DatabaseManager.getConnection();
                }
                catch(Exception e) {
                        //System.out.println( "UserReportBean error getting DB connection or creating stmt");
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
		ResultSet rs = null;
		long    lIntervalTotals = 0;
		boolean	bSpecificUserids = false;
		Hashtable m_hashUserids;
		Vector m_vSortedUsers = new Vector();	//use this to retreive hash in same ascending order every time

		System.out.println("UserReport() Weekend options = " + m_bKeepWeekends + " " + m_bCountWeekends);
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
		calStart.set(Integer.parseInt(m_strStartYr),  Integer.parseInt(m_strStartMth) - 1,  1, 0, 0, 0);
		int iMaxDays = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
		if (Integer.parseInt(m_strStartDay)  > iMaxDays)
		{
			throw new Exception("'From Date' - invalid day of month selected");
		}
		calStart.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m_strStartDay) );

		Calendar calEnd = Calendar.getInstance();
		calEnd.set(Integer.parseInt(m_strEndYr),  Integer.parseInt(m_strEndMth) - 1,  1, 0, 0, 0);
		iMaxDays = calEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
		if (Integer.parseInt(m_strEndDay)  > iMaxDays)
		{
			throw new Exception("'To Date' - invalid day of month selected");
		}
		calEnd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m_strEndDay) );
		calEnd.set(Calendar.HOUR_OF_DAY, 23);
		DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
		DateFormat dowFmt = new SimpleDateFormat("MM/dd - EE");
		
		String strUserid = "";
		if (m_strUserids != null)	
		{	for (int i=0;i<m_strUserids.length;i++)
			{	if (m_strUserids[i].equals("ALL"))
				{	strUserid="ALL";
					break;
				}
				else
				{	if(strUserid.length()>0)  strUserid += ",";
					strUserid += "'"+m_strUserids[i]+"'";
				}
			}
		}
		else if ( (strUserid == null) || (strUserid.length()<1) )
		{
			strUserid="ALL";
		}

		String strQuery1 = "";
		if (strUserid.equals("ALL"))
		{
			strQuery1 = "SELECT U.USERID, U.LST_NM, U.FRST_NM " +
				" FROM USERID_T U, COMPANY_T C, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA " +
				" WHERE U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP='P' " + 
				" AND UGA.USERID=U.USERID AND SGA.SCRTY_GRP_CD=UGA.SCRTY_GRP_CD " +
				" AND SGA.SCRTY_OBJCT_CD='PROV_RQST_ACTIONS' ORDER BY U.LST_NM";
		}
		else
		{
			bSpecificUserids = true;
			strQuery1 = "SELECT U.USERID, U.LST_NM, U.FRST_NM FROM USERID_T U, COMPANY_T C " +
				" WHERE U.USERID IN (" + strUserid + ") AND U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR " +
				" AND C.CMPNY_TYP='P'";
		}
		m_hashUserids = new Hashtable();

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(strQuery1);
			String strTemp;
			while (rs.next())
			{
				strTemp = rs.getString(1);
				UserReportInfo objURI = new UserReportInfo( strTemp, rs.getString(2), rs.getString(3) );
				m_hashUserids.put(strTemp, objURI);
				m_vSortedUsers.addElement(strTemp);
			}

			if (m_hashUserids.size()<1)
			{	throw new Exception("Error getting db connection 1 ");
			}
		}
		catch(Exception e) {
                        throw new Exception("Error getting in UserReportBean sdfasdf");
                }
		nstrBuff.append("<STYLE TYPE=\"text/css\"> .break { page-break-before: always; } </STYLE>");
		nstrBuff.append("<br><center><SPAN CLASS=\"header1\">L&nbsp;S&nbsp;R&nbsp;s&nbsp;</SPAN><br><SPAN CLASS=\"header1\"> U&nbsp;s&nbsp;e&nbsp;r&nbsp;&nbsp;&nbsp;S&nbsp;t&nbsp;a&nbsp;t&nbsp;i&nbsp;s&nbsp;t&nbsp;i&nbsp;c&nbsp;s&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;e&nbsp;p&nbsp;o&nbsp;r&nbsp;t&nbsp;&nbsp;&nbsp;</SPAN>");
		nstrBuff.append("<br><b>Date&nbsp;Range:&nbsp;" + m_strStartMth + "/" + m_strStartDay + "/" + m_strStartYr + "&nbsp;-&nbsp;" + m_strEndMth + "/" + m_strEndDay + "/" + m_strEndYr + "</b><br>Effective:&nbsp;" + dFmt.format(new java.util.Date()) + "<br></center><br><table border=1 align=center cellspacing=0 cellpadding=1><tr bgcolor=\"#DBDBDB\"><th align=center>&nbsp;DATE&nbsp;</th>");

		//Spin thru userids to create header
		for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
		{
			UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
			nstrBuff.append("<th align=center>&nbsp;"+ objURI.getFirstName() + "<br>" + objURI.getLastName() +
				 "<br>&nbsp;(" + objURI.getUserid() + ")&nbsp;</th>");
		}
		nstrBuff.append("<th align=center>&nbsp;TOTAL&nbsp;</th></tr>");
		
		//Counters
		int 	iDays = 0;
		int 	iCount = 0;
		int 	iCount2 = 0;
		int 	iResp = 0;
		int 	iFOC = 0;
		int 	iCompleted = 0;
		int 	iRejected = 0;
		int 	iFOCTotals = 0;
		int 	iCompletedTotals = 0;
		int 	iRejectedTotals = 0;
		Vector  vFocRej;

		//Build query string to get our stats - this is run for each DAY
		String strStatsQuery = "SELECT RH.MDFD_USERID, RH.RQST_STTS_CD_IN, COUNT(*) " +
			" FROM REQUEST_HISTORY_T RH,  USERID_T U, COMPANY_T C, REQUEST_T R "+
			" WHERE RH.RQST_STTS_CD_IN IN ('FOC', 'REJECTED', 'COMPLETED') "+
			" AND RH.RQST_STTS_CD_IN <> RH.RQST_STTS_CD_OUT AND RH.MDFD_USERID = U.USERID"+
			" AND U.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP='P' ";
		if (bSpecificUserids)
		{	strStatsQuery += " AND RH.MDFD_USERID IN (" + strUserid + ") ";
		}
		strStatsQuery += " AND RH.RQST_HSTRY_DT_IN BETWEEN TO_DATE(?, 'MM/DD/YYYY HH24:MI:SS') AND " +
			" TO_DATE(?, 'MM/DD/YYYY HH24:MI:SS') ";
		//Must exclude MULTI orders for now
		//3-18-05 strStatsQuery += " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND R.RQST_VRSN=RH.RQST_VRSN " +
		strStatsQuery += " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR " +
			 " AND R.RQST_TYP_CD<>'M' GROUP BY RH.MDFD_USERID, RH.RQST_STTS_CD_IN ";
		System.out.println("Query=[" + strStatsQuery + "]");
		PreparedStatement pStmt = con.prepareStatement(strStatsQuery);

		//Now do the MULTI order query
		String strMultiStatsQuery = "SELECT RH.MDFD_USERID, R.RQST_SQNC_NMBR, RH.RQST_STTS_CD_IN " +
			" FROM REQUEST_HISTORY_T RH,  USERID_T U, COMPANY_T C, REQUEST_T R " +
			" WHERE RH.RQST_STTS_CD_IN IN ('FOC', 'REJECTED', 'COMPLETED') " +
			" AND RH.RQST_STTS_CD_IN <> RH.RQST_STTS_CD_OUT AND RH.MDFD_USERID = U.USERID "+
			" AND U.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP='P' " ;
		if (bSpecificUserids)
		{	strMultiStatsQuery += " AND RH.MDFD_USERID IN (" + strUserid + ") ";
		}
		strMultiStatsQuery += " AND RH.RQST_HSTRY_DT_IN BETWEEN TO_DATE(?, 'MM/DD/YYYY HH24:MI:SS') AND " +
				      " TO_DATE(?, 'MM/DD/YYYY HH24:MI:SS') ";
		//Must INCLUDE  MULTI orders now
		//3-18-05 strMultiStatsQuery += " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND R.RQST_VRSN=RH.RQST_VRSN " +
		strMultiStatsQuery += " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR " +
				 " AND R.RQST_TYP_CD='M' ";
		System.out.println("MultiQuery=[" + strMultiStatsQuery + "]");
		PreparedStatement pStmt2 = con.prepareStatement(strMultiStatsQuery);
	
		int iDOW = 0;
		int iWeek = calStart.get(Calendar.WEEK_OF_YEAR);
		int iPrevWeek = iWeek;
		int iMth= calStart.get(Calendar.MONTH);
		int iPrevMth = iMth;
		boolean bWE = false;
		//Loop for each day the report should be run....
		while (calStart.before(calEnd))
		{	
			bWE = false;
			iDOW = calStart.get(Calendar.DAY_OF_WEEK);
			if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY)
			{	bWE = true;
			}
			if (!bWE || m_bCountWeekends)
				iDays++;

			// Start building stats for the users
			String strTmpDate = "" + (calStart.get(Calendar.MONTH)+1);
			if (strTmpDate.length() ==1)	strTmpDate = "0" + strTmpDate;
			String strTemp2 = "" + calStart.get(Calendar.DAY_OF_MONTH);
			if (strTemp2.length() ==1)	strTemp2 = "0" + strTemp2;
			strTmpDate += "/" + strTemp2;
			strTmpDate += "/" + calStart.get(Calendar.YEAR);

			String strTemp="";
			pStmt.setString(1, strTmpDate + " 00:00:00");		//start date
			pStmt.setString(2, strTmpDate + " 23:59:59");		//end date (same day since we only do a day at a time
			rs = pStmt.executeQuery();
			while (rs.next())
			{
				strTemp = rs.getString(1);
				iCount = rs.getInt(3);
				//System.out.println("query results: User:"+strTemp+" stat:"+rs.getString(2)+": "+iCount);
				if (m_hashUserids.containsKey(strTemp))
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get(strTemp);
					strTemp = rs.getString(2);	//status
					if (strTemp.equals("FOC")) {
						objURI.addFOCed(iCount);
						iFOC += iCount;
					} else if (strTemp.equals("REJECTED")) {
						objURI.addRejected(iCount);
						iRejected += iCount;
					} else if (strTemp.equals("COMPLETED")) {
						objURI.addCompleted(iCount);
						iCompleted += iCount;
					}
				}
				else {
					System.out.println("UserReport: DB synch problem userid not found = " + strTemp);
				}
			}
			rs.close();	
		
			//Now do the same for the MultiOrders
			pStmt2.setString(1, strTmpDate + " 00:00:00");		//start date
			pStmt2.setString(2, strTmpDate + " 23:59:59");		//end date (same day since we only do a day at a time
			rs = pStmt2.executeQuery();
			while (rs.next())
			{
				strTemp = rs.getString(1);
				vFocRej = SLATools.getMultiFocRej(rs.getString(2), rs.getString(3), con );
				iCount = ((Integer)vFocRej.elementAt(0)).intValue();		//FOC
				iCount2 =((Integer)vFocRej.elementAt(1)).intValue();		//Rejected

				//System.out.println("Mult Results: User:"+strTemp+" Req:"+rs.getString(2)+" Foc: "+iCount+" Rej:"+iCount2);
				if (m_hashUserids.containsKey(strTemp))
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get(strTemp);
					strTemp = rs.getString(3); 	//status
					if (strTemp.equals("COMPLETED")) {
						objURI.addCompleted(iCount+iCount2);
						iCompleted += iCount + iCount2;
					}
					else {
						objURI.addFOCed(iCount);
						iFOC += iCount;
						objURI.addRejected(iCount2);
						iRejected += iCount2;
					}	
				}
				else {
					System.out.println( "UserReport: DB synch problem userid not found = " + strTemp);
				}
			}
			rs.close();	

			//-----------------------------------------------------------
			//	Spit out daily totals (if they want em)
			//-----------------------------------------------------------
			if (!bWE || m_bKeepWeekends) 
			{	strTemp = dowFmt.format(calStart.getTime());
				nstrBuff.append("<tr><td align=center ");
				if(bWE) { 
					nstrBuff.append("bgcolor=\"#DBD000\"");
				}
				nstrBuff.append("><b>&nbsp;" + strTemp + "</b>&nbsp;</td></tr><tr><td align=center>&nbsp;FOC&nbsp;</td>");
	
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{
					UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					nstrBuff.append("<td align=center>&nbsp;" + objURI.getNbrFOCed() + "&nbsp;</td>");
				}	
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iFOC + "&nbsp;</td></tr><tr><td align=center>&nbsp;REJECTS&nbsp;</td>");
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{
					UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					nstrBuff.append("<td align=center>&nbsp;" + objURI.getNbrRejected() + "&nbsp;</td>");
				}
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iRejected + "&nbsp;</td></tr><tr bgcolor=\"#FFFFF0\"><td align=center>&nbsp;FOC/REJECTS&nbsp;</td>");

				iResp=0;
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{
					UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					iResp += objURI.getNbrFOCed()+objURI.getNbrRejected();
					int iTemp = objURI.getNbrFOCed()+objURI.getNbrRejected();
					nstrBuff.append("<td align=center>&nbsp;" + iTemp + "&nbsp;</td>");
				}
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iResp + "&nbsp;</td></tr><tr bgcolor=\"#FFFFF0\"><td align=center>&nbsp;COMPLETED&nbsp;</td>");
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{
					UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					nstrBuff.append("<td align=center>&nbsp;" + objURI.getNbrCompleted() + "&nbsp;</td>");
				}
	
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iCompleted +
					 "&nbsp;</td></tr><tr border=0><td border=0 colspan=" +  m_vSortedUsers.size()+2  + ">&nbsp;</td></tr>");
			}	//end-if (!m_bKeepWeekends)
		
			//Reset daily counts for the userids
			iCompletedTotals += iCompleted;
			iRejectedTotals += iRejected;
			iFOCTotals += iFOC;
			
			iFOC=0;
			iRejected=0;
			iCompleted=0;
			for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
			{
				UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
				 objURI.resetCounts();
			}

			//Increment day
			calStart.add(Calendar.DATE, 1);

			//New week?
			iWeek = calStart.get(Calendar.WEEK_OF_YEAR);
			iMth= calStart.get(Calendar.MONTH);

			//-----------------------------------------------------------
			//	Pump out Weekly figures
			//-----------------------------------------------------------
			if (iWeek != iPrevWeek)
			{	iPrevWeek = iWeek;
				nstrBuff.append("<tr><td align=center colspan=2 bgcolor=\"#DBEAF5\"><b>&nbsp;Weekly&nbsp;Totals</b>&nbsp;</td></tr><tr><td align=center>&nbsp;FOC&nbsp;</td>");
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					iFOC += objURI.getWeeklyFOCed();
					nstrBuff.append("<td align=center>&nbsp;" + objURI.getWeeklyFOCed() + "&nbsp;</td>");
				}
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iFOC +
						 "&nbsp;</td></tr><tr><td align=center>&nbsp;REJECTS&nbsp;</td>");
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					iRejected += objURI.getWeeklyRejected();
					nstrBuff.append("<td align=center>&nbsp;" + objURI.getWeeklyRejected() + "&nbsp;</td>");
				}
	
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iRejected + "&nbsp;</td></tr><tr bgcolor=\"#FFFFF0\"><td align=center>&nbsp;FOC/REJECTS&nbsp;</td>");
				iResp=0;
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					int itemp = objURI.getWeeklyFOCed()+objURI.getWeeklyRejected();
					nstrBuff.append("<td align=center>&nbsp;" + itemp + "&nbsp;</td>");
					iResp += itemp;
				}
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iResp + "&nbsp;</td></tr><tr bgcolor=\"#FFFFF0\"><td align=center>&nbsp;COMPLETED&nbsp;</td>");
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					iCompleted += objURI.getWeeklyCompleted();
					nstrBuff.append("<td align=center>&nbsp;" + objURI.getWeeklyCompleted() + "&nbsp;</td>");
				}
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iCompleted + "&nbsp;</td></tr><tr border=0><td border=0 colspan=" + m_vSortedUsers.size()+2 + " >&nbsp;</td></tr>");
				iFOC=0;
				iRejected=0;
				iCompleted=0;

				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{
					UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					objURI.resetWeeklyCounts();
				}
			}

			if (iMth != iPrevMth)
			{	iPrevMth = iMth;
				
				nstrBuff.append("<tr><td align=center colspan=2 bgcolor=\"#DBEAF5\"><b>&nbsp;Monthly&nbsp;Totals</b>&nbsp;</td>" +
				 	"</tr><tr><td align=center>&nbsp;FOC&nbsp;</td>");
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					iFOC += objURI.getMonthlyFOCed();
					nstrBuff.append("<td align=center>&nbsp;" + objURI.getMonthlyFOCed() + "&nbsp;</td>");
				}
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iFOC + "&nbsp;</td>");
				nstrBuff.append("</tr><tr><td align=center>&nbsp;REJECTS&nbsp;</td>");
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					iRejected += objURI.getMonthlyRejected();
					nstrBuff.append("<td align=center>&nbsp;" + objURI.getMonthlyRejected() + "&nbsp;</td>");
				}
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iRejected + "&nbsp;</td>");
				nstrBuff.append("</tr><tr bgcolor=\"#FFFFF0\"><td align=center>&nbsp;FOC/REJECTS&nbsp;</td>");
				iResp=0;
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					int itemp = objURI.getMonthlyFOCed()+objURI.getMonthlyRejected();
					nstrBuff.append("<td align=center>&nbsp;" + itemp + "&nbsp;</td>");
					iResp += itemp;
				}
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iResp + "&nbsp;</td>");
				nstrBuff.append("</tr><tr bgcolor=\"#FFFFF0\"><td align=center>&nbsp;COMPLETED&nbsp;</td>");
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					iCompleted += objURI.getMonthlyCompleted();
					nstrBuff.append("<td align=center>&nbsp;" + objURI.getMonthlyCompleted() + "&nbsp;</td>");
				}
				nstrBuff.append("<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iCompleted  + "&nbsp;</td></tr>");
				nstrBuff.append("<tr border=0><td border=0 colspan="+ m_vSortedUsers.size()+2 + ">&nbsp;</td></tr>");
				iFOC=0;
				iRejected=0;
				iCompleted=0;
				
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{
					UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					objURI.resetMonthlyCounts();
				}
			}
		}
		System.out.println( "UserReport() Days in period="+iDays);
	
		nstrBuff.append("<tr><td bgcolor=\"#3366cc\" align=center><b><FONT color=\"#ffffff\" STYLE=\"cursor:hand\" ONMOUSEOUT=\"hidepopupmsg();\" ONMOUSEOVER=\"showpopupmsg('Totals for each user for| the date range selected.');\">&nbsp;TOTALS</FONT></b></td></tr>");

		nstrBuff.append("<tr  bgcolor=\"#FFFFF0\"><td align=center>&nbsp;FOC&nbsp;</td>");
		for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
		{
			UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
			nstrBuff.append("<td align=center>&nbsp;" + objURI.getTotalFOCed() + "&nbsp;</td>");
			iFOC+=objURI.getTotalFOCed();
		}
		nstrBuff.append("<td align=right>&nbsp;" + iFOC + "&nbsp;</td></tr>");

		nstrBuff.append("<tr  bgcolor=\"#FFFFF0\"><td align=center>&nbsp;REJECTS&nbsp;</td>");
		for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
		{
			UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
			nstrBuff.append("<td align=center>&nbsp;" + objURI.getTotalRejected() + "&nbsp;</td>");
			iRejected+=objURI.getTotalRejected();
		}
		nstrBuff.append("<td align=right>&nbsp;"  + iRejected + "&nbsp;</td></tr>");

		nstrBuff.append("<tr  bgcolor=\"#FFFFF0\"><td align=center>&nbsp;FOC/REJECTS&nbsp;</td>");
		iRejected=0;
		for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
		{
			UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
			int itemp = objURI.getTotalFOCed()+objURI.getTotalRejected();
			nstrBuff.append("<td align=center>&nbsp;" + itemp + "&nbsp;</td>");
			iRejected+= itemp;
		}
		nstrBuff.append("<td align=right>&nbsp;" + iRejected + "&nbsp;</td> </tr>");

		nstrBuff.append("<tr  bgcolor=\"#FFFFF0\">");
		nstrBuff.append("<td align=center>");

		nstrBuff.append("<FONT STYLE=\"cursor:hand\" ONMOUSEOUT=\"hidepopupmsg();\" ONMOUSEOVER=\"showpopupmsg('Total responses (FOC and Rejects)| divided by nbr of days | in reporting period. The nbr | of days is in parenthesis.');\">&nbsp;AVG*&nbsp;(" + iDays + ")&nbsp;</FONT></td>");
		DecimalFormat Avgfmt = new DecimalFormat("0.00");
		iRejected=0;
		for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
		{
			UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
			int iTemp=objURI.getTotalFOCed()+objURI.getTotalRejected();
			if (iDays==0) {
				nstrBuff.append("<td align=center>&nbsp;N/A&nbsp;</td>");
			}
			else {
				nstrBuff.append("<td align=center>&nbsp;" +  Avgfmt.format( (float)iTemp/iDays) + "&nbsp;</td>");
			}
			iRejected+=iTemp;
		}
		if (iDays==0) {
			nstrBuff.append("<td align=center>&nbsp;N/A&nbsp;</td>");
		} else {
			nstrBuff.append("<td align=right>&nbsp;" + Avgfmt.format((float)iRejected/iDays) + "&nbsp;</td>");
		}
		nstrBuff.append("</tr>");

		nstrBuff.append("<tr  bgcolor=\"#FFFFF0\"><td align=center>&nbsp;COMPLETED&nbsp;</td>");

		for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
		{
			UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
			nstrBuff.append("<td align=center>&nbsp;" + objURI.getTotalCompleted() + "&nbsp;</td>");
			iCompleted+=objURI.getTotalCompleted();
		}
		nstrBuff.append("<td align=right>&nbsp;" + iCompleted + "&nbsp;</td>");
		nstrBuff.append("</tr>  </table>");

		if (m_bCountWeekends) {
			nstrBuff.append("&nbsp;&nbsp;* Weekends are included in AVG<br>");
		} else {
			nstrBuff.append("&nbsp;&nbsp;* Weekends are not include in AVG<br>	 ");
		}
		nstrBuff.append("<BR>");
		return nstrBuff.toString();
	}

	private void dumpTotals(String strHdr, String strType) 
	{
		System.out.println("UserReport() dumpTotals()");
	}

}

