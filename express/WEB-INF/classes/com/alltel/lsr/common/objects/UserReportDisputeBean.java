	/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2006
 *				BY
 *			ALLTEL COMMUNICATIONS INC.
 */
/** 
 * MODULE:	UserReportDisputeBean.java
 * 
 * 
 * DATE:        12-12-2005
 * 
 * HISTORY:
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import com.alltel.lsr.common.batch.UserReportInfo;

public class UserReportDisputeBean
{
	public UserReportDisputeBean()
	{	
		m_strStartYr="2006";
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
		m_bSkipZeroReportLines = true;
		m_bCountWeekends = true;
		m_bKeepWeekends = true;
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
	boolean m_bKeepWeekends;
	private boolean m_bSkipZeroReportLines;	//Defaults to true
	
	private String[] m_strUserids;

	private String[] m_strOCN_CDs;
	private String[] m_strSTATE_CDs = null;
	private String[] m_strVENDORs = null;
	private	String[] m_strSRVC_TYP_CDs = null;
	private	String[] m_strACTVTY_TYP_CDs = null;
	boolean m_bCountWeekends = true;

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

	public void setSkipZeroReportLines(boolean bVal){
		m_bSkipZeroReportLines = bVal;
	}
	public boolean getSkipZeroReportLines(){
		return m_bSkipZeroReportLines;
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
	public String[] getSTATE_CDs() {
		return m_strSTATE_CDs;
	}
	public void setVENDORs( String[] strList )
	{
	    String strTemp = "";
	    if ( strList != null )
	    {	
	    	m_strVENDORs = new String[strList.length];
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
	    {	
	    	m_strSRVC_TYP_CDs = new String[strList.length];
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


	public void setACTVTY_TYP_CDs( String[] strList )
	{
        String strTemp = "";
        if ( strList != null )
        {	m_strACTVTY_TYP_CDs = new String[strList.length];
            for(int x=0;  x < strList.length; x++ )
            {
                strTemp = strList[x].trim();
                if(strTemp.length() > 0)
                {
                        m_strACTVTY_TYP_CDs[x] = strTemp;
                        strTemp = "";
                }
            }
        }
	}
	public String[] getACTVTY_TYP_CDs() {
		return m_strACTVTY_TYP_CDs;
	}

	public void setUserids( String[] strList )
	{
        String strTemp = "";
        if ( strList != null )
    	{      
    		m_strUserids = new String[strList.length];
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
			m_strUserids = ExpressUtil.extractEmployeeGroups(  conn,  groupIds,  bAllUsers, m_strUserids );
			iReturnV = m_strUserids.length;
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
	 * Connection must be closed by caller... caller also catches exceptions. See above!
	 */
	public int  extractEmployeeGroups( Connection conn, String[] groupIds, boolean bAllUsers ) 
	throws SQLException, Exception
	{
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
			//Log.write(Log.ERROR, "SLAReportByUserBean error getting DB connection or creating stmt");
			throw new Exception("Error getting db connection 1 ");
		}
		if (con == null) return null;
		String strReport = runReport(con);
		DatabaseManager.releaseConnection(con);
		return strReport;
	}
  	
  	/* EK: 02/27/006 This function performs the report processing. Please note that the connection is
  	 * passed in and is expected to be closed or released by the caller.
  	 *@Para: con  ( open connection)
  	 *@return HTML page of report 
  	 *@ See db object: 
  	 * @Comments, Major steps:
	  	 1. Extract the parameters from query-string and validate them.
	  	 2. Create dynamic where-clause  for prepared Statement from multi-select boxes
	  	 2b. DB call to get names of items included in the where-clause for building html header.
	  	 3. Create page header based on searched (selected items)
	  	 4. Start iterating over users array and running main query for each user.
	  	 4b. Build dynamic binding statement
	  	 5. return page including all users' reports.
  	 */
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
		boolean bSpecificUserids = false;
		ResultSet rs = null;
		String strTemp="";
		Vector m_vSortedUsers = new Vector();   //use this to retreive hash in same ascending order every time
		Hashtable m_hashUserids = new Hashtable( 100);
		
		// 	 1. Extract the parameters from query-string and validate them.
		
		if ((m_strStartYr.length() == 0) || (m_strStartMth.length()==0) || (m_strStartDay.length()==0))
		{
			throw new Exception("Invalid start date");
		}
		if ((m_strEndYr.length() == 0) || (m_strEndMth.length()==0) || (m_strEndDay.length()==0))
		{
			throw new Exception("Invalid end date");
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
		DateFormat dowFmt = new SimpleDateFormat("MM/dd - EE");
		//Log.write(Log.DEBUG_VERBOSE, "Date:" + m_strStartDate + " Date:" + m_strEndDate);
		// HDR 1071942 -additional criteria
		PreparedStatement stmt2 = null;
		ResultSet rs2 = null;	
		String strOCN_CD="";
		String strSTATE_CD="";
		String strVENDOR="";
		String strSRVC_TYP_CD="";
		String strACTVTY_TYP_CD="";
		Vector vOcns = new Vector( 100 );
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

		String strCount = "";
		String strQuery1 = "";
		int iHashSize=0;
		if (strUserid.equals("ALL"))
		{
			
			strQuery1 = "SELECT U.USERID, U.LST_NM, U.FRST_NM " +
				" FROM USERID_T U, COMPANY_T  C, USER_GROUP_ASSIGNMENT_T  UGA, SECURITY_GROUP_ASSIGNMENT_T SGA " +
				" WHERE U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP='P' " + 
				" AND UGA.USERID=U.USERID AND SGA.SCRTY_GRP_CD=UGA.SCRTY_GRP_CD " +
				" AND SGA.SCRTY_OBJCT_CD='PROV_RQST_ACTIONS' ORDER BY U.LST_NM";
		}
		else
		{
			bSpecificUserids = true;
			strQuery1 = "SELECT U.USERID, U.LST_NM, U.FRST_NM FROM USERID_T  U, COMPANY_T  C " +
				" WHERE U.USERID IN (" + strUserid + ") AND U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR " +
				" AND C.CMPNY_TYP='P'";
		}

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(strQuery1);
			strTemp="";
			while (rs.next())
			{
				strTemp = rs.getString(1);
				UserReportInfo objName = new UserReportInfo( strTemp, rs.getString(2), rs.getString(3) );
				m_vSortedUsers.addElement(strTemp);	//use data struct for name
				m_hashUserids.put(strTemp, objName );
								
			}
			
		}
		catch(Exception e)
		{
			throw new Exception("Error extracting users:\n" + e.toString() );
		}finally {
			try {
				if( rs != null ){ rs.close(); rs=null; }
				if( stmt != null ){ stmt.close(); stmt=null; }
			} catch (Exception eee) { eee.printStackTrace();}
		}

		String strHeaderCriteria = "<center><b>Other report selection criteria:&nbsp;&nbsp;&nbsp;</b>";
		strTemp = "";
		boolean bPickedAll = true;
		int iOcnCount = 0;		
		String strOCN_CD_Where = "";
				
		/***** 2. Create dynamic where-clause  for prepared Statement from multi-select boxes *******/
		
		if ( m_strOCN_CDs != null )
		{	strTemp = strHeaderCriteria;//save it
			strHeaderCriteria += " <b><br>OCN:</b>&nbsp;"; 
			strOCN_CD_Where = " AND ( ";
			for (int j=0;  j<m_strOCN_CDs.length; j++)
			{	strOCN_CD = m_strOCN_CDs[j];
				if ( !strOCN_CD.equals("ALL") && (strOCN_CD.length()> 3) )
				{	bPickedAll = false;
					//NOTE this parm will have <ocn>-<company seq #> format, so parse it first
					int i = strOCN_CD.lastIndexOf("-");
					if (i !=  -1)
					{   
						 iOcnCount++;
						   strOCN_CD_Where += " ( r.OCN_CD = ? "
								   + " AND r.CMPNY_SQNC_NMBR = ?  ) OR ";
						  //		 strOCN_CD_Where += " (O.OCN_CD ='" + strOCN_CD.substring(0,i)
						  //  strOCN_CD_Where += " (O.OCN_CD ='" + strOCN_CD.substring(0,i) +
						//		   "' AND O.CMPNY_SQNC_NMBR=" + strOCN_CD.substring(i+1)+") OR ";
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
		int iStateCount = 0;
		bPickedAll = true;
		String strSTATE_CD_Where = "";
		if ( m_strSTATE_CDs != null )
		{	strTemp = strHeaderCriteria;//save it
			strHeaderCriteria += " <BR><b>State:</b>&nbsp;";
			strSTATE_CD_Where = " AND R.OCN_STT IN ( ";			
			if( m_strSTATE_CDs.length > 0 ) {
				strSTATE_CD = m_strSTATE_CDs[0];
			 	if( !strSTATE_CD.equals("ALL") ){
			 		bPickedAll = false;
			 		iStateCount++;
			 		strSTATE_CD_Where += " ? ";	
			 		strHeaderCriteria += "&nbsp;" + strSTATE_CD + "&nbsp;&nbsp;&nbsp";
					for (int j=1;j<m_strSTATE_CDs.length;j++)
					{	strSTATE_CD = m_strSTATE_CDs[j];
						if ( !strSTATE_CD.equals("ALL") )
						{	bPickedAll = false;		
							iStateCount++;			
							strSTATE_CD_Where += ",? ";
							strHeaderCriteria += "&nbsp;" + strSTATE_CD + "&nbsp;&nbsp;&nbsp";
						}
						else {	
							strHeaderCriteria = strTemp;
							bPickedAll = true;
							strSTATE_CD_Where = "";	
							break;
						}
					}
						
				strSTATE_CD_Where += " ) ";
				}else 	{	
					strHeaderCriteria = strTemp;
					strSTATE_CD_Where = "";								
				}
			}		
		}else
		{	
			strSTATE_CD="ALL";
		}
		//Log.write(Log.DEBUG_VERBOSE, "State where =[" + strSTATE_CD_Where+ "]");
		
		bPickedAll = true;
		String strVENDOR_Where = "";
		int iVendorCount = 0;
		if ( m_strVENDORs != null )
		{	strTemp = strHeaderCriteria;//save it
			strHeaderCriteria += " <BR><b>Vendor:</b>&nbsp;";
			strVENDOR_Where = " AND R.CMPNY_SQNC_NMBR IN ( ";
						
			if( m_strVENDORs.length > 0 ) {
				strVENDOR = m_strVENDORs[0];
			 	if( !strVENDOR.equals("ALL") ){
			 		bPickedAll = false;
			 		iVendorCount++;
			 		strVENDOR_Where += " ? ";				 		
					for (int j=1;j<m_strVENDORs.length;j++)
					{	strVENDOR = m_strVENDORs[j];
						if ( !strVENDOR.equals("ALL") )
						{	bPickedAll = false;
							iVendorCount++;
							strVENDOR_Where += ",?";							
						}
						else {	strHeaderCriteria = strTemp;
							bPickedAll = true;
							break;
						}
					}
					strVENDOR_Where += " ) ";	
				// 2b. DB call to get names of items included in the where-clause for building html header.
				try {
					//con = DatabaseManager.getConnection();
						String strqry = "SELECT R.CMPNY_NM FROM COMPANY_T R WHERE 1=1 "
							+ 	strVENDOR_Where;
						stmt2 = con.prepareStatement( strqry );
						stmt2.clearParameters();
						if( !strVENDOR_Where.equals("")  ){			
							for( int i = 0; i < m_strVENDORs.length; i++ )
							{
								stmt2.setString( i+1, m_strVENDORs[i])	;
							}				
						}
						rs2 = stmt2.executeQuery();							
						while (rs2.next()==true)
						{	
							strHeaderCriteria += "&nbsp;" + rs2.getString("CMPNY_NM") + "&nbsp;&nbsp;&nbsp";
						
						}
						
					}catch(Exception e) {
							//Log.write(Log.ERROR, "ReportByUserBean error creating stmt");
							throw new Exception("Error:\n" + e.toString());
					}	
					finally {
						try {
							if( rs2 != null ){ rs2.close(); rs2=null; }
							if( stmt2 != null ){ stmt2.close(); stmt2=null; }
						} 
						catch (Exception eee) 
							{ eee.printStackTrace();}											
						}
					}
				else {	strHeaderCriteria = strTemp;
					strVENDOR_Where = "";			
				}
			}		
		}
		
		//Log.write(Log.DEBUG_VERBOSE, "Vendor where =[" + strVENDOR_Where + "]");
		int iSTypeCount = 0;
		bPickedAll = true;
		String strSRVC_TYP_CD_Where = "";
		if ( m_strSRVC_TYP_CDs != null )
		{	strTemp = strHeaderCriteria;//save it
			strHeaderCriteria += " <BR><b>Service Type:</b>&nbsp;";
			strSRVC_TYP_CD_Where = " AND R.SRVC_TYP_CD IN ( ";	
			if( m_strSRVC_TYP_CDs.length > 0 ) {
				strSRVC_TYP_CD = m_strSRVC_TYP_CDs[0];
			 	if( !strSRVC_TYP_CD.equals("ALL") ){
			 		bPickedAll = false;
			 		iSTypeCount++;
			 		strSRVC_TYP_CD_Where += "?";			 		
					for (int j=1;j<m_strSRVC_TYP_CDs.length;j++)
					{	strSRVC_TYP_CD = m_strSRVC_TYP_CDs[j];
						if ( !strSRVC_TYP_CD.equals("ALL") )
						{	bPickedAll = false;
							iSTypeCount++;
							strSRVC_TYP_CD_Where += ",? ";								
						}
						else {	strHeaderCriteria = strTemp;
							bPickedAll = true;
							break;
						}
					}
					strSRVC_TYP_CD_Where += " ) ";
					try {
					//con = DatabaseManager.getConnection();
						String strqry = "SELECT R.SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T  R WHERE R.TYP_IND=? "
							+ 	strSRVC_TYP_CD_Where;
						stmt2 = con.prepareStatement(strqry );
						stmt2.clearParameters();
						stmt2.setString( 1, "R" );
						if( !strSRVC_TYP_CD_Where.equals("") ){			
							for( int i = 0; i < m_strSRVC_TYP_CDs.length; i++ )
							{
								stmt2.setString( i+2, m_strSRVC_TYP_CDs[i])	;
							}				
						}
						rs2 = stmt2.executeQuery();	
												
						while (rs2.next()==true)
						{	
							strHeaderCriteria += "&nbsp;" + rs2.getString("SRVC_TYP_DSCRPTN") + "&nbsp;&nbsp;&nbsp";
						
						}						
					}catch(Exception e) {
							//Log.write(Log.ERROR, "ReportByUserBean error creating stmt");
							throw new Exception("Error getting db: Getting Service Type\n" + e.toString());
					}finally {
						try {
							if( rs2 != null ){rs2.close(); rs2=null; }
							if( stmt2 != null ) { stmt2.close(); stmt2=null; }
						} catch (Exception eee) { eee.printStackTrace();}					
					}	
				}else {	strHeaderCriteria = strTemp;
					strSRVC_TYP_CD_Where = "";							
				}
			}			
		}
				
		//Log.write(Log.DEBUG_VERBOSE, "SrvTyp where =[" + strSRVC_TYP_CD_Where + "]");
		int iActvityCount = 0;
		bPickedAll = true;
		String strACTVTY_TYP_CD_Where = "";
		if ( m_strACTVTY_TYP_CDs != null )
		{	strTemp = strHeaderCriteria;	//save it
			strHeaderCriteria += "<BR> <b>Activity Type:</b>&nbsp;";
			strACTVTY_TYP_CD_Where = " AND R.ACTVTY_TYP_CD IN  ( ";
			
			if( m_strACTVTY_TYP_CDs.length > 0 ) {
				strACTVTY_TYP_CD = m_strACTVTY_TYP_CDs[0];
			 	if( !strACTVTY_TYP_CD.equals("ALL") ){
			 		bPickedAll = false;
			 		iActvityCount++;
			 		strACTVTY_TYP_CD_Where += "? ";								
					for (int j=1;j<m_strACTVTY_TYP_CDs.length;j++ )
					{	strACTVTY_TYP_CD = m_strACTVTY_TYP_CDs[j];
						if ( !strACTVTY_TYP_CD.equals("ALL") )
						{	bPickedAll = false;
							iActvityCount++;
							strACTVTY_TYP_CD_Where += ",?";							
						}
						else {	strHeaderCriteria = strTemp;
							bPickedAll = true;
							break;
						}
					}
					strACTVTY_TYP_CD_Where += " ) ";
					try {
					//con = DatabaseManager.getConnection();
						String strqry = "SELECT R.ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T R WHERE R.TYP_IND=? " 
									+ strACTVTY_TYP_CD_Where;
						stmt2 = con.prepareStatement(strqry );
						stmt2.clearParameters();
						stmt2.setString( 1, "R" );
						if( !m_strACTVTY_TYP_CDs.equals("") ){			
							for( int i = 0; i < m_strACTVTY_TYP_CDs.length; i++ )
							{
								stmt2.setString( i+2, m_strACTVTY_TYP_CDs[i])	;
							}				
						}
						rs2 = stmt2.executeQuery();							
						while (rs2.next()==true)
						{	
							strHeaderCriteria += "&nbsp;" + rs2.getString("ACTVTY_TYP_DSCRPTN") + "&nbsp;&nbsp;&nbsp";
						
						}							
					}catch(Exception e) {
							//Log.write(Log.ERROR, "ReportByUserBean error creating stmt");
							throw new Exception("Error: Getting ACTVTY_TYP_DSCRPTN\n" + e.toString() );
					}finally {
						try {
							if( rs2 != null ){rs2.close(); rs2=null; }
							if( stmt2 != null ) { stmt2.close(); stmt2=null;}
						} catch (Exception eee) { eee.printStackTrace();}	
					}	
				}else {	strHeaderCriteria = strTemp;
					strACTVTY_TYP_CD_Where = "";					
				}		
			}			
		}
		// 3. Create page header based on searched (selected items)
		
		strHeaderCriteria += "</center>";
		nstrBuff.append("<style>\nP.page {page-break-after: always }\n</style>\n");
		nstrBuff.append("<br><center>" );
		nstrBuff.append("<SPAN CLASS=\"header1\">B&nbsp;i&nbsp;l&nbsp;l&nbsp;i&nbsp;n&nbsp;g&nbsp;&nbsp;&nbsp;D&nbsp;i&nbsp;s&nbsp;p&nbsp;u&nbsp;t&nbsp;e&nbsp;s&nbsp;</SPAN><br>\n");
		nstrBuff.append("<SPAN CLASS=\"header1\"> U&nbsp;s&nbsp;e&nbsp;r&nbsp;&nbsp;&nbsp;S&nbsp;t&nbsp;a&nbsp;t&nbsp;i&nbsp;s&nbsp;t&nbsp;i&nbsp;c&nbsp;s&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;e&nbsp;p&nbsp;o&nbsp;r&nbsp;t</SPAN>\n" );
		nstrBuff.append("<br><b>Date&nbsp;Range:&nbsp;" + m_strStartMth + "/" + m_strStartDay + "/" + m_strStartYr  + "&nbsp-&nbsp;" + m_strEndMth + "/" + m_strEndDay + "/" + m_strEndYr +  "</b><br>\n " );
		nstrBuff.append("Effective:&nbsp;" + dFmt.format(new java.util.Date()) + "<br></center><br>" );

		//This is just to throw FYI on report that additinoal criterium was used to produce results....
		if ( (strOCN_CD_Where.length() > 0) || (strSTATE_CD_Where.length() > 0) || (strVENDOR_Where.length() > 0) 
			|| (strSRVC_TYP_CD_Where.length() > 0) || (strACTVTY_TYP_CD_Where.length() > 0) )
		{
			nstrBuff.append(strHeaderCriteria);
		}
		
		
		nstrBuff.append("<P CLASS=page>");			
		nstrBuff.append("<table border=1 align=center cellspacing=0 cellpadding=1>");	
		nstrBuff.append("<tr bgcolor=\"#DBDBDB\">");	
		nstrBuff.append("<th align=center>&nbsp;DATE&nbsp;</th>");	
		//Spin thru userids to create header
		for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
		{
			//System.out.println( "============\t" + m_vSortedUsers.size() );
			UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
			nstrBuff.append("<th align=center>&nbsp;" );
			nstrBuff.append(objURI.getFirstName() + "<br>" );
			nstrBuff.append(objURI.getLastName() + "<br>&nbsp;");
			nstrBuff.append( objURI.getUserid()+ "&nbsp;</th>" );
		}
		nstrBuff.append("<th align=center>&nbsp;TOTAL&nbsp;</th>" );
		//Counters
		int 	iDays = 0;
		int 	iCount = 0;
		int 	iCount2 = 0;
		int 	iResp = 0;
		int 	iResolved = 0;
		int 	iCompleted = 0;
		int 	iResolvedTotals = 0;
		Vector  vFocRej;	
	
		//Build query string to get our stats - this is run for each DAY
		String strStatsQuery = "SELECT DH.MDFD_USERID, COUNT(*) " +
			" FROM DISPUTE_HISTORY_T DH,  USERID_T U, COMPANY_T C, DSPT_RSPNS_DETAIL_T DR " +
			" WHERE DH.STTS_CD_IN IN ('RESOLVED') AND DH.STTS_CD_IN <> DH.STTS_CD_OUT " +
			" AND DH.MDFD_USERID = U.USERID AND U.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP='P' " ;
		if (bSpecificUserids)
		{	strStatsQuery += " AND DH.MDFD_USERID IN (" + strUserid + ") ";
		}
	  	strStatsQuery += " AND DH.HSTRY_DT_IN BETWEEN TO_DATE(?, 'MM/DD/YYYY HH24:MI:SS')   " +
	  		" AND TO_DATE(?, 'MM/DD/YYYY HH24:MI:SS') AND DR.DSPT_SQNC_NMBR=DH.DSPT_SQNC_NMBR AND " +
			" DR.DSPT_VRSN=DH.DSPT_VRSN AND LENGTH(DR.DSPT_ADJSTD_AMNT) > 0 GROUP BY DH.MDFD_USERID ";
		
		Log.write("Query=[" + strStatsQuery + "]");
		PreparedStatement pStmt = con.prepareStatement(strStatsQuery);

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
	
			pStmt.setString(1, strTmpDate + " 00:00:00");		//start date
			pStmt.setString(2, strTmpDate + " 23:59:59");		//end date (same day since we only do a day at a time
			rs = pStmt.executeQuery();
			while (rs.next())
			{
				strTemp = rs.getString(1);
				iCount = rs.getInt(2);
	//			Log.write(Log.DEBUG_VERBOSE, "query results: User:"+strTemp+" count:"+iCount);
				if (m_hashUserids.containsKey(strTemp))
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get(strTemp);
					objURI.addFOCed(iCount);	//reusing FOC bucket for our totals...
					iResolved += iCount;
				}
				else {
					//Log.write(Log.ERROR, "UserReport: DB synch problem userid not found = " + strTemp);
				}
			}
				rs.close();	
			
			//-----------------------------------------------------------
			//	Spit out daily totals (if they want em)
			//-----------------------------------------------------------
			if (!bWE || m_bKeepWeekends) 
			{	strTemp = dowFmt.format(calStart.getTime());
	
				nstrBuff.append( "<tr><td align=center" );
				if(bWE) { 
					nstrBuff.append( " bgcolor=\"#DBD000\" ");
				} 
				nstrBuff.append( "><b>&nbsp;" + strTemp +"</b>&nbsp;</td></tr>");			
				nstrBuff.append( "<tr><td align=center>&nbsp;Resolved&nbsp;</td>");
	
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{
					UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
	
					nstrBuff.append( "<td align=center>&nbsp;" + objURI.getNbrFOCed()+ "&nbsp;</td>" );
				}	
	
				nstrBuff.append( "<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" + iResolved + "&nbsp;</td></tr>" );
				nstrBuff.append( "<TR border=0><TD border=0 colspan=" + ( m_vSortedUsers.size()+2) + ">&nbsp;</TD></TR>" );
	
			}	//end-if (!bKeepWeekends)
			
			//Reset daily counts for the userids
			iResolvedTotals += iResolved;
		
			iResolved=0;
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
				nstrBuff.append( "<tr><td align=center colspan=2 bgcolor=\"#DBEAF5\">" );
				nstrBuff.append( "<b>&nbsp;Weekly&nbsp;Totals</b>&nbsp;</td></tr><tr>");
				nstrBuff.append( "<td align=center>&nbsp;Resolved&nbsp;</td>" );
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					iResolved += objURI.getWeeklyFOCed();
					nstrBuff.append( "<td align=center>&nbsp;" + objURI.getWeeklyFOCed() + "&nbsp;</td>" );
				}
				nstrBuff.append( "<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" +iResolved + "&nbsp;</td></tr>" );
				nstrBuff.append( "<tr border=0><TD border=0 colspan=" + (m_vSortedUsers.size()+2 ) +">&nbsp;</td></tr>" );
				iResolved=0;
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{
					UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					objURI.resetWeeklyCounts();
				}
			}	

			if (iMth != iPrevMth)
			{	
				iPrevMth = iMth;			
				nstrBuff.append( "<tr><td align=center colspan=2 bgcolor=\"#DBEAF5\">");
				nstrBuff.append( "<b>&nbsp;Monthly&nbsp;Totals</b>&nbsp;</td>" );
				nstrBuff.append( "</tr><tr>" );
				nstrBuff.append( "<td align=center>&nbsp;Resolved&nbsp;</td>" );
	
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{	UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					iResolved += objURI.getMonthlyFOCed();
	
					nstrBuff.append( "<td align=center>&nbsp;" + objURI.getMonthlyFOCed() + "&nbsp;</td>");
				}
	
				nstrBuff.append( "<td bgcolor=\"#FFFFF0\" align=right>&nbsp;" +iResolved + "&nbsp;</td></tr> ");
				nstrBuff.append( "<tr border=0><TD border=0 colspan="+ (m_vSortedUsers.size()+2 )+ ">&nbsp;</td></tr>");
				iResolved=0;
				for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
				{
					UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
					objURI.resetMonthlyCounts();
				}
			}	
		}
		
		nstrBuff.append( "<tr> ");
		nstrBuff.append( "<td bgcolor=\"#3366cc\" align=center><b><FONT color=\"#ffffff\" STYLE=\"cursor:hand\" ONMOUSEOUT=\"hidepopupmsg();\" ONMOUSEOVER=\"showpopupmsg('Totals for each user for| the date range selected.');\">\n" );
		nstrBuff.append( "&nbsp;TOTALS</FONT></b></td></tr>" );
		
		
		nstrBuff.append( "<tr bgcolor=\"#FFFFF0\">" );
		nstrBuff.append( "<td align=center>&nbsp;Resolved&nbsp;</td>" );
		for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
		{
			UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
			nstrBuff.append( "<td align=center>&nbsp;" + objURI.getTotalFOCed()+ "&nbsp;</td>" );
			iResolved+=objURI.getTotalFOCed();
		}
	
		nstrBuff.append( "<td align=right>&nbsp;" + iResolved + "&nbsp;</td></tr>" );
		nstrBuff.append( "<tr  bgcolor=\"#FFFFF0\">" );
		nstrBuff.append( "<td align=center>" );
		nstrBuff.append( "<FONT STYLE=\"cursor:hand\" ONMOUSEOUT=\"hidepopupmsg();\" ONMOUSEOVER=\"showpopupmsg('Total disputes | divided by nbr of days | in reporting period. The nbr | of days is in parenthesis.');\">" );
		nstrBuff.append( "&nbsp;AVG*&nbsp;(" + iDays + ")&nbsp;</FONT></td> " );
		DecimalFormat Avgfmt = new DecimalFormat("0.00");
		int iTemp=0;
		int iTempTot=0;
		for (Iterator it = m_vSortedUsers.iterator(); it.hasNext(); )
		{
			UserReportInfo objURI = (UserReportInfo)m_hashUserids.get((String)it.next());
			iTemp=objURI.getTotalFOCed();
			if (iDays==0) {
			
				nstrBuff.append( "<td align=center>&nbsp;N/A&nbsp;</td>" );
			}
			else {
			
				nstrBuff.append( "<td align=center>&nbsp;" + Avgfmt.format( (float)iTemp/iDays)+ "&nbsp;</td>" );
			}
			iTempTot += iTemp;
		}
		if (iDays==0) {	
		   nstrBuff.append( "<td align=center>&nbsp;N/A&nbsp;</td>");
		} else {	
		  nstrBuff.append( " <td align=right>&nbsp;" + Avgfmt.format((float)iTempTot/iDays)+ "&nbsp;</td>" );
		}
		nstrBuff.append( "</tr></table>" );
		if (m_bCountWeekends) {
			nstrBuff.append( "* Weekends are included in AVG<br> ");
		} else {	
			nstrBuff.append( "* Weekends are not include in AVG<br>	" );
		}	
		//Log.write(Log.DEBUG_VERBOSE, "UserReport() Days in period="+iDays);
		return nstrBuff.toString();
	}
}

