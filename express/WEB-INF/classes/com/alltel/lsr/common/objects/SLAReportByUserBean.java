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
 * MODULE:	SLAReportByUserBean.java
 * 
 * DESCRIPTION: SLA report by User - for SLA info for a particular userid(s)
 * 	Main routine (runReport() returns HTML, can be called from batch or online.
 * 
 * AUTHOR:      psedlak
 * 
 * DATE:        5-15-2005
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

public class SLAReportByUserBean
{
	public SLAReportByUserBean()
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
		m_bSkipZeroReportLines = true;
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
	private boolean m_bSkipZeroReportLines;	//Defaults to true
    boolean bSpecificOrder = false;
	private String[] m_strUserids;

	private String[] m_strOCN_CDs;
	private String[] m_strSTATE_CDs = null;
	private String[] m_strVENDORs = null;
	private	String[] m_strSRVC_TYP_CDs = null;
	private	String[] m_strACTVTY_TYP_CDs = null;
    private String m_orderFlag = null;

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

    public void setOrderFlag(String strOrderFlag) {

        Log.write(Log.DEBUG_VERBOSE, "strOrderFlag=[" + strOrderFlag + "]");
        m_orderFlag = strOrderFlag;
    }

    public String getOrderFlag() {
        return m_orderFlag;
    }

    public void setUserids(String[] strList) {
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
  	
  	/* EK: 8/24/05 This function performs the report processing. Please note that the connection is
  	 * passed in and is expected to be closed or released by the caller.
  	 *@Para: con  ( open connection)
  	 *@return HTML page of report 
  	 *@ See db object: 
  	 *	Views: 	RS_REPORT_V2 
  	 * 	Functions: 	1. GETOCNSTTSLADAYS
					2. GETOCNNAME
					3. GETCOMPANYTYPE

  	 * @Comments, Major steps:
	  	 1. Extract the parameters from query-string and validate them.
	  	 2. Create dynamic where-clause  for prepared Statement from multi-select boxes
	  	 2b. DB call to get names of items included in the where-clause for building html header.
	  	 3. Create page header based on searched (selected items)
	  	 4. Start iterating over users array and running main query for each users.
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
		Hashtable m_hStoredUsers = new Hashtable( 100);
		
		// 	 1. Extract the parameters from query-string and validate them.
		
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

		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(strQuery1);
			strTemp="";
			while (rs.next())
			{
				strTemp = rs.getString(1);
				UserReportInfo objName = new UserReportInfo( strTemp, rs.getString(2), rs.getString(3) );
				m_vSortedUsers.addElement(objName);	//use data struct for name
								
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
							//Log.write(Log.ERROR, "SLAReportByUserBean error creating stmt");
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
							//Log.write(Log.ERROR, "SLAReportByUserBean error creating stmt");
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
							//Log.write(Log.ERROR, "SLAReportByUserBean error creating stmt");
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

        //start new Added code for Order Type(ICARE or CAMS)- Vijay - 01/19/2012
        String strOrderFlagWhere = "";

        Log.write(Log.DEBUG_VERBOSE, "strOrderFlag=[" + m_orderFlag + "]");

        if (m_orderFlag.equals("ALL")) {
            m_orderFlag = "ALL";
        } else {
            bSpecificOrder = true;
        }
        if (bSpecificOrder) {
            strOrderFlagWhere = " AND R.ICARE='" + m_orderFlag + "'";
        }
//End new Added code for Order Type(ICARE or CAMS)- Vijay - 01/19/2012
		// 3. Create page header based on searched (selected items)
		
		strHeaderCriteria += "</center>";
		nstrBuff.append("<style>\nP.page {page-break-after: always }\n</style>\n");
		nstrBuff.append("<br><center><SPAN CLASS=\"header1\"> S&nbsp;L&nbsp;A&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R&nbsp;S&nbsp;</SPAN>" +
		"<br><b>Date&nbsp;Range:&nbsp;" + m_strStartMth + "/" + m_strStartDay + "/" + m_strStartYr +
		"&nbsp;-&nbsp;" + m_strEndMth + "/" + m_strEndDay + "/" + m_strEndYr +
		"</b><br>Effective:&nbsp;" + dFmt.format(new java.util.Date()) + "<br></center><br> <br>");

		//This is just to throw FYI on report that additinoal criterium was used to produce results....
		if ( (strOCN_CD_Where.length() > 0) || (strSTATE_CD_Where.length() > 0) || (strVENDOR_Where.length() > 0) 
			|| (strSRVC_TYP_CD_Where.length() > 0) || (strACTVTY_TYP_CD_Where.length() > 0) )
		{
			nstrBuff.append(strHeaderCriteria);
		}
		nstrBuff.append("<P CLASS=page>");			
		String strOCNQuery =
					"SELECT RH.RQST_SQNC_NMBR, RH.RQST_STTS_CD_IN, TO_CHAR(RH.RQST_HSTRY_DT_IN,'YYYYMMDD HH24MISS'), R.RQST_TYP_CD, RH.MDFD_USERID, " +
					" R.OCN_CD, R.OCN_STT, R.OCN_NM, R.OCN_STT_SLA_DYS, R.CMPNY_SQNC_NMBR " +
					" FROM RS_REPORT_V2 R, REQUEST_HISTORY_T RH WHERE " +
					" R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN <> RH.RQST_STTS_CD_OUT " +
					" AND RH.RQST_STTS_CD_IN IN ( ?,?,?) "
					+ strOCN_CD_Where + strSTATE_CD_Where + strVENDOR_Where 
                + strSRVC_TYP_CD_Where + strACTVTY_TYP_CD_Where + strOrderFlagWhere +
					" AND EXISTS (SELECT RH2.RQST_SQNC_NMBR FROM REQUEST_HISTORY_T RH2 " +
					" WHERE RH2.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR " +
					" AND RH2.RQST_STTS_CD_IN IN ( ?, ?) " +
					" AND RH2.RQST_HSTRY_DT_IN BETWEEN " + " TO_DATE( ?, ? ) AND " + 
					" TO_DATE(?, ?) " +
					" AND RH2.MDFD_USERID =  ? ) " +
					" ORDER BY R.OCN_STT, R.OCN_CD, RH.RQST_SQNC_NMBR, RH.RQST_HSTRY_DT_IN DESC";
               Log.write(Log.DEBUG_VERBOSE, "strOCNQuery=[" + strOCNQuery + "]");
		System.out.println( strOCNQuery );
		Iterator it = 	m_vSortedUsers.iterator();
		m_strStartDate = m_strStartDate + " 00:00:00";
		m_strEndDate = m_strEndDate + " 23:59:59";	
		PreparedStatement pstmt = null;
		pstmt = con.prepareStatement(  strOCNQuery );
		String strPreviousOCN_state = "";
		String strCurrentOCNSTT = "";
		String strTempONCST = "";
		int iCurrentCompSqn = 0;
		int iPrevCompSqn = 0;
		// 4. Start iterating over users' array and running main query for each users.
	  	
		while( it.hasNext()  ){
			//NOTE the <P CLASS=page> is to put page breaks in if this thing is printed....
			UserReportInfo objURI =  (UserReportInfo)it.next();
			nstrBuff.append("</P><P CLASS=page><br><table border=1 align=center cellspacing=0 cellpadding=1><tr><th align=left colspan=11  bgcolor=\"#efefef\">" + objURI.getFirstName() + "&nbsp;" + objURI.getLastName() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;("+ objURI.getUserid() + ")<tr>");
			//System.out.println("\nUser being processed="+ objURI.getUserid());
			nstrBuff.append("<tr><th align=center>&nbsp;OCN&nbsp;</th> <th align=center>VENDOR</th> <th align=center>&nbsp;FOCed&nbsp;</th> <th align=center>&nbsp;REJ&nbsp;</th> <th align=center>&nbsp;TOTAL&nbsp;</th> <th align=center>&nbsp;%&nbsp;REJECTED&nbsp;</th> <th align=center>&nbsp;FOC&nbsp;INT&nbsp;</th> <th align=center>&nbsp;FOC<br>&nbsp;SLA&nbsp;</th> <th align=center>&nbsp;Within<br>&nbsp;SLA&nbsp;</th> <th align=center>&nbsp;Past<br>&nbsp;SLA&nbsp;</th> <th align=center>&nbsp;%&nbsp;within<br>&nbsp;SLA&nbsp;</th> </tr>");
		
			long lPrevSeqNmbr = 0;
			long lSeqNmbr = 0;
			long    lDay = 0;
			long    lHour = 0;
			long    lMin = 0;
			int     iTotal = 0;
			//int iOCN = rs.getInt("OCN_CD");
			String strOCN ="";
			String strSt = "";
			String strNm = "";
			String strCmpnySqncNmbr = "";
			int iSLA = 0;
			long lSLAInSeconds = -99;
			int iCompleted = 0;
			int iRejected = 0;
			int iWithinFOC = 0;
			int iPastFOC = 0;
			long lSLAAccumulation = 0;      //this is total seconds
			long lSLA = 0;
			int iState = 0;
			String strSLAEndDTS = "";
			String strSLABeginDTS = "";
			strPreviousOCN_state = "";
			strCurrentOCNSTT = "";
			strTempONCST = "";
			iCurrentCompSqn = 0;
			iPrevCompSqn = 0;
			DecimalFormat OCNfmt = new DecimalFormat("0000");					
			String strDateFormat = "YYYYMMDD HH24:MI:SS";			
			int iBindCounter = 1;				
			iCompleted = 0;
			iRejected = 0;
			iWithinFOC = 0;
			iPastFOC = 0;
			lSLAAccumulation = 0;      //this is total seconds
			lSLA = 0;
			iState = 0;			
			pstmt.clearParameters();				
			pstmt.setString( 1 , "SUBMITTED" );
			pstmt.setString( 2 , "FOC");	
			pstmt.setString( 3, "REJECTED" );
			iBindCounter = 4;
							
			 //4b. Build dynamic binding statement			 
	  	 
			if( !strOCN_CD_Where.equals("") && iOcnCount > 0 ){					
				for( int ocCount = 0; ocCount < m_strOCN_CDs.length; ocCount++ )
				{
					strOCN_CD = m_strOCN_CDs[ocCount];
					 int i = strOCN_CD.lastIndexOf("-");
					if (i !=  -1)
					{   
						pstmt.setString( iBindCounter++, strOCN_CD.substring(0,i) )	;
						pstmt.setString( iBindCounter++, strOCN_CD.substring(i+1) );
					}
				}				
			}	
			// bind States	
			if( !strSTATE_CD_Where.equals("") && iStateCount > 0 ){			
				
				for( int i = 0; i < m_strSTATE_CDs.length; i++ )
				{
					pstmt.setString( iBindCounter++, m_strSTATE_CDs[i])	;
				}				
			}
			// bind  activities			
			
			if( !strVENDOR_Where.equals("") && iVendorCount > 0 ){			
				for( int i = 0; i < m_strVENDORs.length; i++ )
				{
					pstmt.setString( iBindCounter++, m_strVENDORs[i])	;
				}				
			}
			if( !strSRVC_TYP_CD_Where.equals("") && iSTypeCount > 0 ){				
				for( int i = 0; i < m_strSRVC_TYP_CDs.length; i++ )
				{
					pstmt.setString( iBindCounter++, m_strSRVC_TYP_CDs[i])	;
				}				
			}		
			if( !strACTVTY_TYP_CD_Where.equals("") && iActvityCount > 0 ){			
				for( int i = 0; i < m_strACTVTY_TYP_CDs.length; i++ )
				{
					pstmt.setString( iBindCounter++, m_strACTVTY_TYP_CDs[i])	;
				}				
			}				
		
			pstmt.setString( iBindCounter++, "FOC");	
			pstmt.setString( iBindCounter++, "REJECTED" );
			pstmt.setString( iBindCounter++, m_strStartDate );
			pstmt.setString( iBindCounter++, strDateFormat );	
			pstmt.setString( iBindCounter++, m_strEndDate );
			pstmt.setString( iBindCounter++, strDateFormat );
			pstmt.setString( iBindCounter++, objURI.getUserid() );					
			rs2 = pstmt.executeQuery();
			String strRqstTypCd = "";
			String strRqstSqncNmbr = "";
			String strPrevRqstSqncNmbr = "";
			Integer iMultiFoc = new Integer(0);
			Integer iMultiRej = new Integer(0);
			int iMultiTotal = 0;
			String strTEST;
			String strStatus = "";
			UserReportInfo objName = null;			
			boolean bPrinted = false;
			iOCNCount = 0;
			while( rs2.next()==true )
			{						
				bPrinted = false;					
				strOCN =   rs2.getString( "OCN_CD" ).trim() ;
				strSt = rs2.getString("OCN_STT" ).trim() ;
				iCurrentCompSqn = rs2.getInt( "CMPNY_SQNC_NMBR" );	
				// Result Set primary Key.				
				strCurrentOCNSTT = strOCN + "-" + strSt +"-" + iCurrentCompSqn;			
					
				// change of OCN and state combination
				if( !(strPreviousOCN_state.equals( strCurrentOCNSTT ))
					&& !strPreviousOCN_state.equals("") )
				{
					iTotal = iCompleted + iRejected;
					iOCNCount++;
					bPrinted = true;
					//If 0 totals, and SkipZeroReportLines = true, then dont write 0 report lines to report...
					if ( (iTotal > 0) || (!m_bSkipZeroReportLines) )
					{
			
						nstrBuff.append("<tr><td>" + strTempONCST + "</td><td>" + strNm +
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
							//System.out.println("SLAReport: SLA Average= " + lSLAAccumulation + "/" + iTotal +  " = " + lSLAAverage);
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
					}		
					iCompletedTotals += iCompleted;
					iRejectedTotals += iRejected;
					lSLATotals += lSLAAccumulation;
					iWithinFOCTotals += iWithinFOC;
					iPastFOCTotals += iPastFOC;						
					iCompleted = 0;
					iRejected = 0;
					iWithinFOC = 0;
					iPastFOC = 0;
					lSLAAccumulation = 0;      //this is total seconds
					lSLA = 0;
					iState = 0;	
					strPrevRqstSqncNmbr = "";						
				}      
	            strStatus = rs2.getString( "RQST_STTS_CD_IN" ).trim();
	            strStatus = rs2.getString( "RQST_STTS_CD_IN" ).trim();				
				iSLA = rs2.getInt("OCN_STT_SLA_DYS");			
				strRqstSqncNmbr = rs2.getString( "RQST_SQNC_NMBR" );
				strRqstTypCd = rs2.getString( "RQST_TYP_CD" );
				
				lSLAInSeconds = iSLA * DAY_IN_SEC;
				strNm = rs2.getString( "OCN_NM" ).trim();
				System.out.println( strCurrentOCNSTT + "\t" + strNm  );	
				strTEST= rs2.getString(3);
				calTemp.set(Integer.parseInt(strTEST.substring(0,4)),
                     Integer.parseInt( strTEST.substring(4,6)) - 1,
                     Integer.parseInt( strTEST.substring(6,8)),
                     Integer.parseInt( strTEST.substring(9,11)),
                     Integer.parseInt( strTEST.substring(11,13)),
                     Integer.parseInt( strTEST.substring(13,15)) );
                     
	            if ( (iState==0) && (calTemp.before(calStart)) )
   				{       //we're done with this OCN....
					//System.out.println("SLA REQ: skip 1");
					continue;
				}
                if ( (iState==0) && (calTemp.after(calEnd)) )
   				{       //records too early, bypass em
					//System.out.println("SLA REQ: skip 1b");
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
						//System.out.println("Multi FOC Count + " + iMultiFoc.intValue() + " : " + lPrevSeqNmbr);
						iRejected += iMultiRej.intValue();
						//System.out.println("Multi REJ Count + " + iMultiRej.intValue() + " : " + lPrevSeqNmbr);
					}
					else if ( strStatus.equals("FOC") && iState == 0 )
					{	
						iCompleted++;
						//System.out.println("++FOC Count : " + lPrevSeqNmbr);
					}
					else if ( strStatus.equals("REJECTED") && iState == 0 )
					{	iRejected++;
							//System.out.println("++Reject Count : " + lPrevSeqNmbr);
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
						//System.out.println(">>SLA for request " + lSeqNmbr + " = " + lSLA + " seconds");
						//System.out.println(">>SLA running total:" + lSLAAccumulation);

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
					{	
							iState = 0;	//this should never happen
					}
				}								
				strPreviousOCN_state = strCurrentOCNSTT;	
				strTempONCST = strOCN + "-" + strSt;			
			} // while loop.
						
			if(  !bPrinted  && !strPreviousOCN_state.equals("") )
			{
				iTotal = iCompleted + iRejected;
				iOCNCount++;
				//If 0 totals, and SkipZeroReportLines = true, then dont write 0 report lines to report...
				if ( (iTotal > 0) || (!m_bSkipZeroReportLines) )
				{
		
					nstrBuff.append("<tr><td>" + strPreviousOCN_state + "</td><td>" + strNm +
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
						//System.out.println("SLAReport: SLA Average= " + lSLAAccumulation + "/" + iTotal +  " = " + lSLAAverage);
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
				}
				
				iCompletedTotals += iCompleted;
				iRejectedTotals += iRejected;
				lSLATotals += lSLAAccumulation;
				iWithinFOCTotals += iWithinFOC;
				iPastFOCTotals += iPastFOC;		
			}			 
			iTotal = iCompletedTotals+iRejectedTotals;
			if (iTotal > 0) 
			{	
				lSLAAverage =  lSLATotals/iTotal;
			}
			else	
			{	
				lSLAAverage=0;
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
	
			nstrBuff.append(" </UL> <BR> <BR>");
	
			iCompletedTotals=0;
			iRejectedTotals=0;
			iTotal=0;
			iWithinFOCTotals=0;
			iPastFOCTotals=0;
			iOCNCount=0;		
			lSLATotals =0;	
		} 			
		if( rs2 != null ){rs2.close(); rs2=null; }
		if( pstmt != null ){pstmt.close(); pstmt=null; }
		
		//5. return page including all users' reports.
		return nstrBuff.toString();
	}
}

