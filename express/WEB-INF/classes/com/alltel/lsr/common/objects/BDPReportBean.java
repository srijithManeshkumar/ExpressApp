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
 * MODULE:	BDPReportBean.java
 * AUTHOR:      Edris Kalibala
 *
 * DATE:       11/01/2005
 *
 * HISTORY:
 *	11/01/2005	Started
 *  12/01/2006  Steve Korchnak	-Modified WG Interval report to include userid with elapsed times,
 *              and average statistics line at end of report.
 * 01/02/2006	Steve Korchnak	RREQ00000001769 Added processing to select either report
 *				output or worksheet output when generating data.
 */

package com.alltel.lsr.common.objects;

import java.lang.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class BDPReportBean
{
	public final static long DAY_IN_SEC = (long) 86400;
	public final static long HOUR_IN_SEC = (long) 3600;
	public final static long MIN_IN_SEC = (long) 60;
	public final static String SECURITY_OBJECT = "PROV_REPORTS";
	public final static String SUBMITTED = "SUBMITTED";
	public final static String SERVICE_COMPLETE ="SERVICE COMPLETE";
	public final static String BILL_COMPLETE = "BILL COMPLETE"	;
	public final static String TEST_COMPLETE = "DSTAC IPTST COMP";
	public final static String INITIAL = "INITIAL";
	public final static String MKT_PENDING = "MKT-PENDING";
	public final static String MKT_COMPLETE = "MKT-COMPLETE";
	public final static String DE_IN_PROGRESS = "DE-IN PROGRESS";
	public final static String DE_COMPLETE = "DE-COMPLETE";
	public final static String DSTAC_ACCEPTED = "DSTAC ACCEPTED";
	public final static String DSTAC_IPTST_COMP = "DSTAC IPTST COMP";
	public final static String BILLING = "BILLING";
	public static Hashtable hProducts;
	public static Hashtable hOrderTypes;
	public static Hashtable hChangeTypes;
	public static Hashtable hSubChangeTypes;

	public final static String ALL_ITEMS = "ALL";
	public final static String  DATE_FORMAT = "YYYYMMDD HH24:MI:SS";
	public final static int WGWRKSHT          = 3;
	public final static int WGREPORT	      = 2;
	public final static int FULFILLMENTREPORT = 1;
	public static final int ONE_K = 1024;
	public static final int TWO_K = 2048;
	public static final int THREE_K = 3072;
	public static final int FOUR_K = 4096;

	protected String m_strStartYr;
	protected String m_strStartMth;
	protected String m_strStartDay;
	protected String m_strStartDate;
	protected String m_strEndYr;
	protected String m_strEndMth;
	protected String m_strEndDay;
	protected String m_strEndDate;
	protected String[] m_strSTATE_CDs;
	protected String[] m_strVENDORs;
	protected	String[] m_strSRVC_TYP_CDs;
	protected String[] m_strProducts;
	protected String[] m_strOrderType;
	protected String[] m_strChangeType;
	protected String[] m_strChangeSubType ;
	protected String[] m_strUserids;
	protected int m_ReportType;

    Hashtable hOrderHistory = new Hashtable(12);
	Hashtable hOrdHist = new Hashtable(12);

	public BDPReportBean()
	{
		Clean();
	}

	void Clean(){
		m_strStartYr="2005";
		m_strStartMth="";
		m_strStartDay="01";
		m_strStartDate = m_strStartYr + m_strStartMth + m_strStartDay;
		m_strEndYr="2005";
		m_strEndMth="";
		m_strEndDay="01";
		m_strEndDate = m_strEndYr + m_strEndMth + m_strEndDay;
		m_strSTATE_CDs = null;
		m_strVENDORs = null;
		m_strSRVC_TYP_CDs = null;
		m_strProducts = null;
		m_strOrderType = null;
		m_strChangeType = null;
		m_strChangeSubType = null;
		m_ReportType = 0;
		m_strUserids =  null;
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


	public int getReportType(){
		return m_ReportType;
	}

	public void setReportType( int In ){
		m_ReportType = In;
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

	public void setProductTypes( String[] strList )
	{
		String strTemp = "";
		if ( strList != null )
		{	m_strProducts = new String[strList.length];
		    for(int x=0;  x < strList.length; x++ )
		    {
		        strTemp = strList[x].trim();
		        if(strTemp.length() > 0)
		        {
		            m_strProducts[x] = strTemp;
		            strTemp = "";
		        }
		    }
		}
	}

	public void setOrderTypes( String[] strList )
	{
		String strTemp = "";
		if ( strList != null )
		{	m_strOrderType = new String[strList.length];
		    for(int x=0;  x < strList.length; x++ )
		    {
		        strTemp = strList[x].trim();
		        if(strTemp.length() > 0)
		        {
		            m_strOrderType[x] = strTemp;
		            strTemp = "";
		        }
		    }
		}
	}

	public String[] getOrderTypes() {
		return m_strProducts;
	}

	public void setChangeTypes( String[] strList )
	{
		String strTemp = "";
		if ( strList != null )
		{	m_strChangeType = new String[strList.length];
		    for(int x=0;  x < strList.length; x++ )
		    {
		        strTemp = strList[x].trim();
		        if(strTemp.length() > 0)
		        {
		            m_strChangeType[x] = strTemp;
		            strTemp = "";
		        }
		    }
		}
	}

	public String[] getChangeTypes() {
		return m_strChangeType;
	}

	public void setChangeSubTypes( String[] strList )
	{
		String strTemp = "";
		if ( strList != null )
		{	m_strChangeSubType = new String[strList.length];
		    for(int x=0;  x < strList.length; x++ )
		    {
		        strTemp = strList[x].trim();
		        if(strTemp.length() > 0)
		        {
		            m_strChangeSubType[x] = strTemp;
		            strTemp = "";
		        }
		    }
		}
	}

	public String[] getChangeSubTypes() {
		return m_strChangeSubType;
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

	// extract from users groups and example the user array.
	public int  extractEmployeeGroups(  String[] groupIds, boolean bAllUsers )
	{
		int iReturnV = 0;
		Connection conn = null;
		try{
			conn  = DatabaseManager.getConnection();
			m_strUserids = ExpressUtil.extractEmployeeGroups(  conn,  groupIds,  bAllUsers, m_strUserids );

		} catch ( SQLException SqlE ) {
			SqlE.printStackTrace();
			Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  SqlE.toString()  );

		}catch(Exception e) {
			e.printStackTrace();
			Log.write( Log.DEBUG_VERBOSE,  e.toString()  );
		}
		finally {
			DatabaseManager.releaseConnection(conn);
		}
		return iReturnV;
	}

	/*Validate date
	*/
	public void  validateDates() throws Exception{

		// 	 1. Extract the parameters from query-string and validate them.

		if ((m_strStartYr.length() == 0) || (m_strStartMth.length()==0) || (m_strStartDay.length()==0))
		{
			throw new Exception("Invalid  start date");
		}
		if ((m_strEndYr.length() == 0) || (m_strEndMth.length()==0) || (m_strEndDay.length()==0))
		{
			throw new Exception("Invalid  end date");
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
	}

	/* EK
	 * runReport Report:
	 * 	This function generates the reports data for Business Data Products.
	 *  m_ReportType is a class variable that determines what report to run. SetReportType( int)
	 * 	must be set in the jsp or calling app.
	 * 	FULFILLMENTREPORT = 1
	 *	WGREPORT = 2 ( Work Group Interval Report).
	 * Steve Korchnak 12/15/2006
	 *	WGWRKSHT = 3 ( Work Group Interval Worksheet).
	 *
	 * Exceptions must be caught by the caller!
	 */
	public String runReport( Connection conn ) throws
	SQLException, Exception
	{
		StringBuffer hbHtml = new StringBuffer( FOUR_K );
		String strStates = "States:&nbsp;&nbsp;";
		String whereClause = "";
		String strQry1 = "";
		String orderBy = "";
		DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
		System.out.println( "Business Data Product Report Started:\t" + dFmt.format(new java.util.Date() ) );
		if( m_ReportType == FULFILLMENTREPORT){
		 	strQry1 = "SELECT  dt.STT_CD, dt.DWO_SQNC_NMBR, BSNSS_NM, "
				+ " dt.PRDCT_TYP_CD, dt.ORIG_SRVC_TYP_CD, dt.ACTVTY_TYP_CD, dt.SUB_ACTVTY_TYP_CD ,"
				+ " dh.DWO_STTS_CD_IN, TO_CHAR( max(dh.DWO_HSTRY_DT_IN), 'YYYYMMDD HH24MISS') datein, "
				+ " dch.BLL_EFFCTV_DT, dt.LCTN_NM, dh.MDFD_USERID  From Dwo_t  dt, dwo_history_t  dh, DWO_CHG_T  dch  "
				+ " WHERE dt.DWO_SQNC_NMBR = dh.DWO_SQNC_NMBR "
				+ " AND dt.DWO_SQNC_NMBR =  dch.DWO_SQNC_NMBR "
				+ " AND dh.DWO_STTS_CD_IN IN ( ?, ?, ? ) ";
			orderBy =" order by dt.STT_CD, dt.DWO_SQNC_NMBR, dt.BSNSS_NM";
		}else if((m_ReportType == WGREPORT) || (m_ReportType == WGWRKSHT)) { // Vijay - 20-Feb-12
                        strQry1 = " SELECT  STT_CD, DWO_SQNC_NMBR, BSNSS_NM,  PRDCT_TYP_CD, ORIG_SRVC_TYP_CD, ACTVTY_TYP_CD, SUB_ACTVTY_TYP_CD , "
                                + " DWO_STTS_CD_IN, max(datein) as datein, BLL_EFFCTV_DT, LCTN_NM , MDFD_USERID from ( " ;
			strQry1 += "SELECT  dt.STT_CD, dt.DWO_SQNC_NMBR, BSNSS_NM, "
				+ " dt.PRDCT_TYP_CD, dt.ORIG_SRVC_TYP_CD, dt.ACTVTY_TYP_CD, dt.SUB_ACTVTY_TYP_CD ,"
				+ " dh.DWO_STTS_CD_IN, TO_CHAR( max(dh.DWO_HSTRY_DT_IN), 'YYYYMMDD HH24MISS') datein, "
				+ " dch.BLL_EFFCTV_DT, dt.LCTN_NM , dh.MDFD_USERID From Dwo_t  dt, dwo_history_t  dh, DWO_CHG_T  dch  "
				+ " WHERE dt.DWO_SQNC_NMBR = dh.DWO_SQNC_NMBR "
				+ " AND dt.DWO_SQNC_NMBR =  dch.DWO_SQNC_NMBR ";

			//orderBy =" order by dt.STT_CD, dt.PRDCT_TYP_CD, dt.ORIG_SRVC_TYP_CD, dt.DWO_SQNC_NMBR, dh.DWO_STTS_CD_IN";

		  orderBy = " order by STT_CD, PRDCT_TYP_CD, ORIG_SRVC_TYP_CD, DWO_SQNC_NMBR, DWO_STTS_CD_IN ";
		}

		boolean isVpnMetroE = isVPNorMetroE();
		int nBindCounter = 0;
		if( m_strProducts != null  && !ExpressUtil.isElementOf( m_strProducts,  ALL_ITEMS ) ) {
			// bind products
			if ( m_strProducts[0].length() > 0 )
			{
				whereClause += " AND  dt.PRDCT_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < m_strProducts.length; i++ ){
					if ( m_strProducts[i].length() > 0 )
					{
						whereClause += ", ?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}

		// bind types
		if( m_strOrderType != null && !ExpressUtil.isElementOf( m_strOrderType, ALL_ITEMS ) ){
			if ( m_strOrderType[0].length() > 0 )
			{
				whereClause += " AND  dt.ORIG_SRVC_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < m_strOrderType.length; i++ )
				{
					if ( m_strOrderType[i].length() > 0 )
					{
						whereClause += ", ?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}
		// bind states
		if( m_strSTATE_CDs != null && !ExpressUtil.isElementOf(m_strSTATE_CDs, "__" ) )
		{
			if ( m_strSTATE_CDs[0].length() > 0 )
			{
				whereClause += " AND  dt.STT_CD IN ( ?";
				nBindCounter++;
				strStates += m_strSTATE_CDs[0]+"&nbsp;&nbsp;";
				for ( int i = 1; i < m_strSTATE_CDs.length; i++ )
				{
					if ( m_strSTATE_CDs[i].length() > 0 )
					{
						whereClause += ",?";
						nBindCounter++;
						strStates += m_strSTATE_CDs[i]+"&nbsp;&nbsp;";
					}
				}
				whereClause += " )";
			}
		}

	// bind Change types
		if( isVpnMetroE && m_strChangeType != null && !ExpressUtil.isElementOf( m_strChangeType,  ALL_ITEMS ) )
		{
			if ( m_strChangeType[0].length() > 0 )
			{
				whereClause += " AND  dt.ACTVTY_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < m_strChangeType.length; i++ )
				{
					if ( m_strChangeType[i].length() > 0 )
					{
						whereClause += ",?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}
		// bind Change sub types
		// nReportType determines the type of fulfillment report.
		// report 1a is 0 and report > 0 is 1b
		if( isVpnMetroE  && m_strChangeSubType != null && !ExpressUtil.isElementOf( m_strChangeSubType, ALL_ITEMS ) )
		{
			if ( m_strChangeSubType[0].length() > 0 )

			{
				whereClause += " AND  dt.SUB_ACTVTY_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < m_strChangeSubType.length; i++ )
				{
					if ( m_strChangeSubType[i].length() > 0 )
					{
						whereClause += ",?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}
		whereClause += " AND exists (SELECT  dh2.DWO_SQNC_NMBR FROM DWO_HISTORY_T dh2 "
			+ " WHERE dt.DWO_SQNC_NMBR = dh2.DWO_SQNC_NMBR "
			+ " AND  dh2.DWO_STTS_CD_IN  = ?  "
			+ " AND dh2.DWO_HSTRY_DT_IN BETWEEN "
			+ " TO_DATE( ?, ? ) AND  TO_DATE(?, ?) ) "
			+ " GROUP by dt.STT_CD, dt.DWO_SQNC_NMBR, BSNSS_NM, "
			+ " dt.PRDCT_TYP_CD, dt.ORIG_SRVC_TYP_CD, dt.ACTVTY_TYP_CD, dt.SUB_ACTVTY_TYP_CD, "
			+ " dh.DWO_STTS_CD_IN,dch.BLL_EFFCTV_DT, dt.LCTN_NM, dh.MDFD_USERID " ;

            if((m_ReportType == WGREPORT) || (m_ReportType == WGWRKSHT))    { // Vijay - 20-Feb-12
                //vijay - start
        whereClause += " UNION SELECT  dt.STT_CD, o.SQNC_NMBR , BSNSS_NM,  dt.PRDCT_TYP_CD, dt.ORIG_SRVC_TYP_CD, dt.ACTVTY_TYP_CD, "
        + " dt.SUB_ACTVTY_TYP_CD , dh.DWO_STTS_CD_IN, TO_CHAR( max(o.MDFD_DT), 'YYYYMMDD HH24MISS') datein, "
        + " dch.BLL_EFFCTV_DT, dt.LCTN_NM , dh.MDFD_USERID From Dwo_t  dt, dwo_history_t  dh, DWO_CHG_T  dch  , DWO_ORDER_T O "
        + " WHERE O.TYP_IND='X' and O.SQNC_NMBR = dh.DWO_SQNC_NMBR and dt.DWO_SQNC_NMBR = dh.DWO_SQNC_NMBR  "
        + " AND dt.DWO_SQNC_NMBR =  dch.DWO_SQNC_NMBR  " ;
                
               // nBindCounter = 0;
		if( m_strProducts != null  && !ExpressUtil.isElementOf( m_strProducts,  ALL_ITEMS ) ) {
			// bind products
			if ( m_strProducts[0].length() > 0 )
			{
				whereClause += " AND  dt.PRDCT_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < m_strProducts.length; i++ ){
					if ( m_strProducts[i].length() > 0 )
					{
						whereClause += ", ?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}

		// bind types
		if( m_strOrderType != null && !ExpressUtil.isElementOf( m_strOrderType, ALL_ITEMS ) ){
			if ( m_strOrderType[0].length() > 0 )
			{
				whereClause += " AND  dt.ORIG_SRVC_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < m_strOrderType.length; i++ )
				{
					if ( m_strOrderType[i].length() > 0 )
					{
						whereClause += ", ?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}
		// bind states
		if( m_strSTATE_CDs != null && !ExpressUtil.isElementOf(m_strSTATE_CDs, "__" ) )
		{
			if ( m_strSTATE_CDs[0].length() > 0 )
			{
				whereClause += " AND  dt.STT_CD IN ( ?";
				nBindCounter++;
				strStates += m_strSTATE_CDs[0]+"&nbsp;&nbsp;";
				for ( int i = 1; i < m_strSTATE_CDs.length; i++ )
				{
					if ( m_strSTATE_CDs[i].length() > 0 )
					{
						whereClause += ",?";
						nBindCounter++;
						strStates += m_strSTATE_CDs[i]+"&nbsp;&nbsp;";
					}
				}
				whereClause += " )";
			}
		}

	// bind Change types
		if( isVpnMetroE && m_strChangeType != null && !ExpressUtil.isElementOf( m_strChangeType,  ALL_ITEMS ) )
		{
			if ( m_strChangeType[0].length() > 0 )
			{
				whereClause += " AND  dt.ACTVTY_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < m_strChangeType.length; i++ )
				{
					if ( m_strChangeType[i].length() > 0 )
					{
						whereClause += ",?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}
		// bind Change sub types
		// nReportType determines the type of fulfillment report.
		// report 1a is 0 and report > 0 is 1b
		if( isVpnMetroE  && m_strChangeSubType != null && !ExpressUtil.isElementOf( m_strChangeSubType, ALL_ITEMS ) )
		{
			if ( m_strChangeSubType[0].length() > 0 )

			{
				whereClause += " AND  dt.SUB_ACTVTY_TYP_CD IN ( ?";
				nBindCounter++;
				for ( int i = 1; i < m_strChangeSubType.length; i++ )
				{
					if ( m_strChangeSubType[i].length() > 0 )
					{
						whereClause += ",?";
						nBindCounter++;
					}
				}
				whereClause += " )";
			}
		}

whereClause += " AND exists (SELECT  dh2.DWO_SQNC_NMBR FROM DWO_HISTORY_T dh2  "
+ " WHERE dt.DWO_SQNC_NMBR = dh2.DWO_SQNC_NMBR  AND  dh2.DWO_STTS_CD_IN  = ?  and dh.DWO_STTS_CD_IN = o.status  "
+ " AND dh2.DWO_HSTRY_DT_IN BETWEEN  TO_DATE( ?, ? ) AND  TO_DATE(?, ?) ) "
+ " GROUP by dt.STT_CD, o.sqnc_nmbr, BSNSS_NM,  dt.PRDCT_TYP_CD, dt.ORIG_SRVC_TYP_CD, dt.ACTVTY_TYP_CD, "
+ " dt.SUB_ACTVTY_TYP_CD,  dh.DWO_STTS_CD_IN,dch.BLL_EFFCTV_DT, dt.LCTN_NM, dh.MDFD_USERID) "
+ " GROUP by STT_CD, DWO_SQNC_NMBR, BSNSS_NM,  PRDCT_TYP_CD, ORIG_SRVC_TYP_CD, ACTVTY_TYP_CD , "
+ " SUB_ACTVTY_TYP_CD,  DWO_STTS_CD_IN,BLL_EFFCTV_DT, LCTN_NM, MDFD_USERID "   + orderBy;
            }

		// Start building jdbc call.
		PreparedStatement pstmt = null;
		ResultSet rset = null;

        Hashtable hHistory = new Hashtable(12);
		Hashtable hUser    = new Hashtable(12);
		int nCurrentSeq = -9999;
		int nPrevSeq = -9999;
		StaticBDPReportData data = StaticBDPReportData.getInstance();
		hProducts = data.getProducts() ;
		hOrderTypes  = StaticBDPReportData.getOrderTypes();
		hChangeTypes = StaticBDPReportData.getChangeTypes();
		hSubChangeTypes = StaticBDPReportData.getSubChangeTypes();

		hbHtml.append("<style>\nP.page {page-break-after: always }\n</style>\n");
		String strST  = "";
		int nSnqNun  = 0;
		String strBname = "";
		String strLcName = "";
		String strProdType  = "";
		String strOrdType = "";
		String strBllD = "";
		String strChgType = "";
		String strChgSubType  = "";
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" + "DB Query for report is: " + strQry1 + whereClause);
		System.err.println( strQry1 + whereClause);
		pstmt = conn.prepareStatement( strQry1 + whereClause );
		pstmt.clearParameters();
		nBindCounter = 1;
		if( m_ReportType == FULFILLMENTREPORT) {
			pstmt.setString( nBindCounter++, SUBMITTED );
			pstmt.setString( nBindCounter++, StaticBDPReportData.isVPNorMetroE( m_strProducts) ? TEST_COMPLETE : SERVICE_COMPLETE );
			pstmt.setString( nBindCounter++, StaticBDPReportData.isVPNorMetroE( m_strProducts) ? SERVICE_COMPLETE : BILL_COMPLETE  );
		}
		if (m_strProducts != null &&
				!ExpressUtil.isElementOf(m_strProducts, ALL_ITEMS ) )
			{
				for ( int i = 0; i < m_strProducts.length; i++ ){
					if ( m_strProducts[i].length() > 0 )
					{
						pstmt.setString( nBindCounter++, m_strProducts[i] );
						Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Products value is " + m_strProducts[i] );
					}
				}
			}
		if (m_strOrderType != null &&
			!ExpressUtil.isElementOf(m_strOrderType, ALL_ITEMS ) )
		{
			for ( int i = 0; i < m_strOrderType.length; i++ ){
				if ( m_strOrderType[i].length() > 0 )
				{
					pstmt.setString( nBindCounter++, m_strOrderType[i] );
					Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Order Type value is " + m_strOrderType[i] );
				}
			}
		}
		if ( m_strSTATE_CDs != null &&
			!ExpressUtil.isElementOf(m_strSTATE_CDs, "__" ))
		{
			for ( int i = 0; i < m_strSTATE_CDs.length; i++ ){
				if ( m_strSTATE_CDs[i].length() > 0 )
				{
					pstmt.setString( nBindCounter++, m_strSTATE_CDs[i] );
					Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":State cd value is " + m_strSTATE_CDs[i] );
				}
			}
		}
		//
		if( isVpnMetroE  && m_strChangeType != null  &&
			!ExpressUtil.isElementOf(m_strChangeType, ALL_ITEMS ))
		{
			for ( int i = 0; i < m_strChangeType.length; i++ ){
				if ( m_strChangeType[i].length() > 0 )
				{
					pstmt.setString( nBindCounter++, m_strChangeType[i] );
					Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Change Type value is " + m_strChangeType[i] );
				}
			}
		}
		if( isVpnMetroE && m_strChangeSubType != null &&
			!ExpressUtil.isElementOf(m_strChangeSubType, ALL_ITEMS ) )
		{
			for ( int i = 0; i < m_strChangeSubType.length; i++ ){
				if ( m_strChangeSubType[i].length() > 0 )
				{
					pstmt.setString( nBindCounter++, m_strChangeSubType[i] );
					Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Change Sub Type value is " + m_strChangeSubType[i] );
				}
			}
		}
		pstmt.setString( nBindCounter++,  StaticBDPReportData.isVPNorMetroE( m_strProducts ) ? SERVICE_COMPLETE  : BILL_COMPLETE );
                Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":StaticBDPReportData " + StaticBDPReportData.isVPNorMetroE( m_strProducts ) );
		pstmt.setString( nBindCounter++, m_strStartDate + "070000" );
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Start Date value is " + m_strStartDate + "070000" );
		pstmt.setString( nBindCounter++, DATE_FORMAT );
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Start Date format value is " + DATE_FORMAT );
		pstmt.setString( nBindCounter++, m_strEndDate + "235959" );
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":End Date value is " + m_strEndDate + "235959" );
		pstmt.setString( nBindCounter++, DATE_FORMAT );
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":End Date format value is " + DATE_FORMAT );

              if((m_ReportType == WGREPORT) || (m_ReportType == WGWRKSHT))    { // Vijay - 20-Feb-12
                if (m_strProducts != null &&
				!ExpressUtil.isElementOf(m_strProducts, ALL_ITEMS ) )
			{
				for ( int i = 0; i < m_strProducts.length; i++ ){
					if ( m_strProducts[i].length() > 0 )
					{
						pstmt.setString( nBindCounter++, m_strProducts[i] );
						Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Products value is " + m_strProducts[i] );
					}
				}
			}
		if (m_strOrderType != null &&
			!ExpressUtil.isElementOf(m_strOrderType, ALL_ITEMS ) )
		{
			for ( int i = 0; i < m_strOrderType.length; i++ ){
				if ( m_strOrderType[i].length() > 0 )
				{
					pstmt.setString( nBindCounter++, m_strOrderType[i] );
					Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Order Type value is " + m_strOrderType[i] );
				}
			}
		}
		if ( m_strSTATE_CDs != null &&
			!ExpressUtil.isElementOf(m_strSTATE_CDs, "__" ))
		{
			for ( int i = 0; i < m_strSTATE_CDs.length; i++ ){
				if ( m_strSTATE_CDs[i].length() > 0 )
				{
					pstmt.setString( nBindCounter++, m_strSTATE_CDs[i] );
					Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":State cd value is " + m_strSTATE_CDs[i] );
				}
			}
		}
		//
		if( isVpnMetroE  && m_strChangeType != null  &&
			!ExpressUtil.isElementOf(m_strChangeType, ALL_ITEMS ))
		{
			for ( int i = 0; i < m_strChangeType.length; i++ ){
				if ( m_strChangeType[i].length() > 0 )
				{
					pstmt.setString( nBindCounter++, m_strChangeType[i] );
					Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Change Type value is " + m_strChangeType[i] );
				}
			}
		}
		if( isVpnMetroE && m_strChangeSubType != null &&
			!ExpressUtil.isElementOf(m_strChangeSubType, ALL_ITEMS ) )
		{
			for ( int i = 0; i < m_strChangeSubType.length; i++ ){
				if ( m_strChangeSubType[i].length() > 0 )
				{
					pstmt.setString( nBindCounter++, m_strChangeSubType[i] );
					Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Change Sub Type value is " + m_strChangeSubType[i] );
				}
			}
		}
		pstmt.setString( nBindCounter++,  StaticBDPReportData.isVPNorMetroE( m_strProducts ) ? SERVICE_COMPLETE  : BILL_COMPLETE );
                Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":StaticBDPReportData " + StaticBDPReportData.isVPNorMetroE( m_strProducts ) );
		pstmt.setString( nBindCounter++, m_strStartDate + "070000" );
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Start Date value is " + m_strStartDate + "070000" );
		pstmt.setString( nBindCounter++, DATE_FORMAT );
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":Start Date format value is " + DATE_FORMAT );
		pstmt.setString( nBindCounter++, m_strEndDate + "235959" );
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":End Date value is " + m_strEndDate + "235959" );
		pstmt.setString( nBindCounter++, DATE_FORMAT );
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "pstmt " + nBindCounter + ":End Date format value is " + DATE_FORMAT );
            }
		switch(m_ReportType){
			case FULFILLMENTREPORT: {
				Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "Case FULLFILLMENTREPORT");
				hbHtml.append("<br><center><SPAN CLASS=\"header1\">O&nbsp;R&nbsp;D&nbsp;E&nbsp;R &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;F&nbsp;U&nbsp;L&nbsp;F&nbsp;I&nbsp;L&nbsp;L&nbsp;M&nbsp;E&nbsp;N&nbsp;T&nbsp;&nbsp;&nbsp;&nbsp; R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T</SPAN>" +
					"<br><b>Date&nbsp;Range:&nbsp;" + m_strStartMth + "/" + m_strStartDay + "/" + m_strStartYr +
					"&nbsp;-&nbsp;" + m_strEndMth + "/" + m_strEndDay + "/" + m_strEndYr +
					"</b><br>Effective:&nbsp;" + dFmt.format(new java.util.Date()) +
					"<br>" + ExpressUtil.getStateFullNames( m_strSTATE_CDs )+ "<br>" + StaticBDPReportData.getProductNames(m_strProducts, hProducts ) + "<br>" +
					 StaticBDPReportData.getOrderTypeNames(m_strOrderType, hOrderTypes) ) ;
				if( !isVpnMetroE ){
					hbHtml.append("</center><BR CLEAR=ALL><br>");
					hbHtml.append( StaticBDPReportData.fulFillmentAHeader1A());
				}else {
					hbHtml.append(  StaticBDPReportData.getChangeTypeNames( m_strChangeType, hChangeTypes  ) );
					hbHtml.append( "<br>" +  StaticBDPReportData.getChangeSubTypes( m_strChangeSubType, hSubChangeTypes   )  );
					hbHtml.append("</center><BR CLEAR=ALL><br>");
					hbHtml.append( StaticBDPReportData.fulFillmentHeader1B() );
				}
				rset = pstmt.executeQuery();
				while(rset.next() )
				{
					nCurrentSeq = rset.getInt(2);
					//System.err.println( "\n" + nCurrentSeq );
					if( (nCurrentSeq != nPrevSeq)  && (nPrevSeq > 0)  )
					{

						if( strProdType.equals( "P" ) || strProdType.equals( "E" ) ) {
							hbHtml.append( printFulfillmentLine1C(	strST,nSnqNun,strBname,  strProdType, strOrdType, strChgType, strChgSubType, strBllD, hHistory, strLcName) );
						}else if( !isVpnMetroE ) {
							hbHtml.append( printFulfillmentLine1A(strST,nSnqNun,strBname,  strProdType, strOrdType, strBllD, hHistory, strLcName )  );
						} else {
							hbHtml.append( printFulfillmentLine1B(	strST,nSnqNun,strBname,  strProdType, strOrdType,strChgType, strChgSubType, strBllD, hHistory, strLcName) );
						}
							hHistory =  null;
							hHistory =  new Hashtable( 3);
					}
					strST  = ExpressUtil.fixNullStr( rset.getString( 1 ) );
					nSnqNun  = rset.getInt( 2 );
					strBname = ExpressUtil.fixNullStr( rset.getString( 3 )  );
					strProdType  = ExpressUtil.fixNullStr( rset.getString( 4 ) );
					strOrdType = ExpressUtil.fixNullStr( rset.getString( 5 ) );
					strBllD =  ExpressUtil.fixNullStr( rset.getString( 10 ) );
					strChgType =  ExpressUtil.fixNullStr( rset.getString( 6 ) );
					strChgSubType  =  ExpressUtil.fixNullStr( rset.getString( 7 ) );
					strLcName = ExpressUtil.fixNullStr( rset.getString( 11 ) );
					hHistory.put( rset.getString( 8 ), rset.getString( 9 ) );
					nPrevSeq = nCurrentSeq;
				}
				// clean up.
				if(rset != null){ rset.close(); rset = null;}
				if(pstmt != null){ pstmt.close(); pstmt = null;}

				// print last one on the record set.
				if( nPrevSeq > 0  )
				{
					if( strProdType.equals( "P" ) || strProdType.equals( "E" ) ) {
						hbHtml.append( printFulfillmentLine1C(	strST,nSnqNun,strBname,  strProdType, strOrdType, strChgType, strChgSubType, strBllD, hHistory, strLcName) );
					}else if( !isVpnMetroE ) {
						hbHtml.append( printFulfillmentLine1A(strST,nSnqNun,strBname,  strProdType, strOrdType, strBllD, hHistory, strLcName )  );
					} else {
						hbHtml.append( printFulfillmentLine1B(	strST,nSnqNun,strBname,  strProdType, strOrdType,strChgType, strChgSubType, strBllD, hHistory, strLcName) );

					}
				}
				hbHtml.append("</table>\n" );
				break;

			}
			// Work group Report
			case WGREPORT:{
				Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "Case WGREPORT");
				hbHtml.append("<br><center><SPAN CLASS=\"header1\">W&nbsp;O&nbsp;R&nbsp;K&nbsp;&nbsp;&nbsp; G&nbsp;R&nbsp;O&nbsp;U&nbsp;P&nbsp;&nbsp;&nbsp; I&nbsp;N&nbsp;T&nbsp;E&nbsp;R&nbsp;V&nbsp;A&nbsp;L&nbsp;&nbsp;&nbsp; R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T</SPAN>" +
					"<br><b>Date&nbsp;Range:&nbsp;" + m_strStartMth + "/" + m_strStartDay + "/" + m_strStartYr +
					"&nbsp;-&nbsp;" + m_strEndMth + "/" + m_strEndDay + "/" + m_strEndYr +
					"</b><br>Effective:&nbsp;" + dFmt.format(new java.util.Date()) +
					"<br>" + ExpressUtil.getStateFullNames( m_strSTATE_CDs ) + "<br>" + StaticBDPReportData.getProductNames(m_strProducts, hProducts ) + "<br>" +
					 StaticBDPReportData.getOrderTypeNames(m_strOrderType, hOrderTypes) ) ;
				if( isVpnMetroE ){
					Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "Case WGREPORT:isVpnMetroE");
					hbHtml.append(  StaticBDPReportData.getChangeTypeNames( m_strChangeType, hChangeTypes  ) );
					hbHtml.append( "<br>" +  StaticBDPReportData.getChangeSubTypes( m_strChangeSubType, hSubChangeTypes )  );
						//hbHtml.append("<table border=1 bordercolor=\"#336699\" cellspacing=0 cellpadding=2 align=center class=fulfillmentRPT><tr><td>");
				}
				hbHtml.append("</center><br><br>");
				String strWGKey = "";
				String strWGKeyPrev = "";
				String strTempPrd = "";
				rset = pstmt.executeQuery();
				while(rset.next() )
				{
					//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "Case WGREPORT:while loop iteration");
					nCurrentSeq = rset.getInt(2);
					strTempPrd = rset.getString( 4 );
					strWGKey  = ExpressUtil.fixNullStr( rset.getString( 1 ) )
								+ "_"+ ExpressUtil.fixNullStr( rset.getString( 4 ) )
								+ "_"+ ExpressUtil.fixNullStr( rset.getString( 5 ) );
					if( strWGKeyPrev.equals( "" ) )
						{
							String strHeader = ExpressUtil.getStateFullName( rset.getString( 1 ) ) + " / " + StaticBDPReportData.getProductsName( strTempPrd, hProducts )  +  " / " + StaticBDPReportData.getOrderTypeName( rset.getString( 5 ), hOrderTypes ) + "\n";
							if( strTempPrd.equals( "D" ) ){
								hbHtml.append(	StaticBDPReportData.wgReportHeaderDI( strHeader) );
							}else if ( strTempPrd.equals( "P" ) || strTempPrd.equals( "E" ) ) {
								hbHtml.append(	StaticBDPReportData.wgReportHeader1B( strHeader ) );
							}else if( StaticBDPReportData.isVPNorMetroE( m_strProducts )  ) {
								hbHtml.append ( StaticBDPReportData.wgReportHeaderIPVPN( strHeader  ));
							}else{
								hbHtml.append(	StaticBDPReportData.wgReportHeader1A( strHeader ) );
							}
						}

					if( (nCurrentSeq != nPrevSeq)  && (nPrevSeq > 0)  )
					{

						if(strProdType.equals( "D" ) ){
							hbHtml.append( printWGDelicatedRow( nSnqNun,  strBname, strLcName ));
						}else if ( strProdType.equals( "P" ) || strProdType.equals( "E" ) )
						{
							hbHtml.append( printWG1ARow(	  nSnqNun,  strBname, strChgType, strChgSubType,  true, strLcName ) );
						}
						else if(strProdType.equals( "M" ) || strProdType.equals( "I" )
							 ) //gao, 08/25/2006, RIS 1449
						{
							hbHtml.append( printWGVpnMetroELine(   nSnqNun,  strBname, strChgType, strChgSubType, strLcName ) );
						}else{
							hbHtml.append( printWG1ARow(	   nSnqNun,  strBname, "", "",  false, strLcName ) );
						}
						if( !strWGKeyPrev.equals( strWGKey  )  && !strWGKeyPrev.equals( "" ) )
						{
							// EK. clean this up some later, put in function
							hbHtml.append(	"</table><br clear=all><br>");
							String strHeader = ExpressUtil.getStateFullName( rset.getString( 1 ) ) + " / " + StaticBDPReportData.getProductsName( strTempPrd, hProducts )  +  " / " + StaticBDPReportData.getOrderTypeName( rset.getString( 5 ), hOrderTypes ) + "\n";
							if( strTempPrd.equals( "D" ) ){
								hbHtml.append(	StaticBDPReportData.wgReportHeaderDI( strHeader) );
							}else if ( strTempPrd.equals( "P" ) || strTempPrd.equals( "E" ) ) {
								hbHtml.append(	StaticBDPReportData.wgReportHeader1B( strHeader ) );
							}else if( StaticBDPReportData.isVPNorMetroE( m_strProducts )  ) {
								hbHtml.append ( StaticBDPReportData.wgReportHeaderIPVPN( strHeader  ));
							}else{
								hbHtml.append(	StaticBDPReportData.wgReportHeader1A( strHeader ) );
							}
						}

                        hOrdHist.clear();
						hHistory =  null;
						hHistory =  new Hashtable(12);
						hUser =     null;
						hUser =     new Hashtable(12);
					}
		            //Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "Loading data returned from the query");
		            OrderHistory oHist = new OrderHistory();
					strWGKeyPrev = strWGKey;
					nSnqNun  = rset.getInt( 2 );
					strBname = ExpressUtil.fixNullStr( rset.getString( 3 ) );
					strProdType  = ExpressUtil.fixNullStr( rset.getString( 4 ) );
					strOrdType = ExpressUtil.fixNullStr( rset.getString( 5 ));
					strST  = ExpressUtil.fixNullStr( rset.getString( 1 ) );
					strBllD = ExpressUtil.fixNullStr(  rset.getString( 10 ) );
					strChgType =  ExpressUtil.fixNullStr( rset.getString( 6) );
					strChgSubType  =  ExpressUtil.fixNullStr( rset.getString( 7 ));
					strLcName = ExpressUtil.fixNullStr( rset.getString( 11 ) );
					oHist.orderCompTime = ExpressUtil.fixNullStr( rset.getString( 9 ) );
					oHist.orderCompUser = ExpressUtil.fixNullStr( rset.getString( 12 ) );
					hOrdHist.put( rset.getString( 8 ), oHist);
					nPrevSeq = nCurrentSeq;
				}
				// clean up.
				if(rset != null){ rset.close(); rset = null;}
				if(pstmt != null){ pstmt.close(); pstmt = null;}

				// print last one on the record set.
				if( nPrevSeq > 0  )
				{
					if(strProdType.equals( "D" ) ){
						//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "hbHtml.printWGDelicatedRow(" + nSnqNun + "," + strBname + ", " + strLcName );
						hbHtml.append( printWGDelicatedRow(  nSnqNun,  strBname, strLcName ));
					}else if ( strProdType.equals( "P" ) || strProdType.equals( "E" ) )
					{
						//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "hbHtml.printWG1ARow(" + nSnqNun + "," + strBname + "," + strChgType + "," + strChgSubType + ", true, " + strLcName);
						hbHtml.append( printWG1ARow(	   nSnqNun,  strBname, strChgType, strChgSubType,  true, strLcName ) );
					}
					else if(strProdType.equals( "E" ) || strProdType.equals( "I" )
						 ) //gao, 08/25/2006, RIS 1449
					{
						//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "hbHtml.printWGVpnMetroLine(" + nSnqNun + "," + strBname + "," + strChgType + "," + strChgSubType + ", " + strLcName);
						hbHtml.append( printWGVpnMetroELine(   nSnqNun,  strBname, strChgType, strChgSubType, strLcName ) );
					}else{
						//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "hbHtml.printWG1ARow(" + nSnqNun + "," + strBname + ",,, false , " + strLcName);
						hbHtml.append( printWG1ARow(	   nSnqNun,  strBname, "", "",  false, strLcName ) );

					}
				    hbHtml.append("</TD></tr>\n");
					hbHtml.append("</center><br>");

					String strHeader = " / Statistics Section /" + "\n";
					if( strTempPrd.equals( "D" ) ){
						hbHtml.append(	StaticBDPReportData.wgReportTrailerDI( strHeader) );
						hbHtml.append( printWGDelicatedRowAvgSummary() );
					}else if ( strTempPrd.equals( "P" ) || strTempPrd.equals( "E" ) ) {
						hbHtml.append(	StaticBDPReportData.wgReportTrailer1B( strHeader ) );
						hbHtml.append( printWG1ARowAvgSummary() );
					}else if( StaticBDPReportData.isVPNorMetroE( m_strProducts )  ) {
						hbHtml.append ( StaticBDPReportData.wgReportTrailerIPVPN( strHeader  ));
						hbHtml.append( printWGVpnMetroELineAvgSummary() );
					}else{
						hbHtml.append(	StaticBDPReportData.wgReportTrailer1A( strHeader ) );
						hbHtml.append( printWG1ARowAvgSummary() );
					}
				    hbHtml.append("</table><br clear=all><br>");

				}
				break;
			}
			// Work group Worksheet:
			case WGWRKSHT:{
				Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "Case WGWRKSHT");
				String strWGKey = "";
				String strWGKeyPrev = "";
				String strTempPrd = "";
				rset = pstmt.executeQuery();
				hbHtml.append("<table  border=1>");
				hbHtml.append("<tr bgcolor=#E5F1CC>");
				while(rset.next() )
				{
					//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "Case WGWRKSHT:while loop iteration");
					strOrdType = ExpressUtil.fixNullStr( rset.getString( 5 ));
					strST  = ExpressUtil.fixNullStr( rset.getString( 1 ) );
					nCurrentSeq = rset.getInt(2);
					strTempPrd = rset.getString( 4 );
					strWGKey  = ExpressUtil.fixNullStr( rset.getString( 1 ) )
								+ "_"+ ExpressUtil.fixNullStr( rset.getString( 4 ) )
								+ "_"+ ExpressUtil.fixNullStr( rset.getString( 5 ) );
					if( strWGKeyPrev.equals( "" ) )
						{
							String strState = ExpressUtil.getStateFullName( rset.getString( 1 ) );
							String strPrdctNm = StaticBDPReportData.getProductsName( strTempPrd, hProducts );
							String strOrdrTyp = StaticBDPReportData.getOrderTypeName( rset.getString( 5 ), hOrderTypes );

							String strHeader = ExpressUtil.getStateFullName( rset.getString( 1 ) ) + " / " + StaticBDPReportData.getProductsName( strTempPrd, hProducts )  +  " / " + StaticBDPReportData.getOrderTypeName( rset.getString( 5 ), hOrderTypes ) + "\n";

							if( strTempPrd.equals( "D" ) ){
									hbHtml.append(BDPWorksheetBean.wgWrkshtHeaderDI());
							}else if ( strTempPrd.equals( "P" ) || strTempPrd.equals( "E" ) ) {
									hbHtml.append(BDPWorksheetBean.wgWrkshtHeader1B());
							}else if( StaticBDPReportData.isVPNorMetroE( m_strProducts )  ) {
									hbHtml.append(BDPWorksheetBean.wgWrkshtHeaderIPVPN());
							}else{
									hbHtml.append(BDPWorksheetBean.wgWrkshtHeader1A());
							}
						}

					if( (nCurrentSeq != nPrevSeq)  && (nPrevSeq > 0)  )
					{

						if(strProdType.equals( "D" ) ){
							hbHtml.append(insertWGDelicatedRow(strST, StaticBDPReportData.getProductsName( strTempPrd, hProducts ),strOrdType,nSnqNun,strBname,strChgType,strChgSubType,false,strLcName ));
						}else if ( strProdType.equals( "P" ) || strProdType.equals( "E" ) )
						{
							hbHtml.append(insertWG1ARow(strST, StaticBDPReportData.getProductsName( strTempPrd, hProducts ),strOrdType,nSnqNun,strBname,strChgType,strChgSubType,true,strLcName ));
						}
						else if(strProdType.equals( "M" ) || strProdType.equals( "I" )
							 ) //gao, 08/25/2006, RIS 1449
						{
							hbHtml.append(insertWGVpnMetroELine(strST, StaticBDPReportData.getProductsName( strTempPrd, hProducts ),strOrdType,nSnqNun,strBname,strChgType,strChgSubType,false,strLcName ));
						}else{
							hbHtml.append(insertWG1ARow(strST, StaticBDPReportData.getProductsName( strTempPrd, hProducts ),strOrdType,nSnqNun,strBname,strChgType,strChgSubType,false,strLcName ));
						}
                        hOrdHist.clear();
						hHistory =  null;
						hHistory =  new Hashtable(12);
						hUser =     null;
						hUser =     new Hashtable(12);
					}
		            //Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "Loading data returned from the query");
		            OrderHistory oHist = new OrderHistory();
					strWGKeyPrev = strWGKey;
					nSnqNun  = rset.getInt( 2 );
					strBname = ExpressUtil.fixNullStr( rset.getString( 3 ) );
					strProdType  = ExpressUtil.fixNullStr( rset.getString( 4 ) );
					strOrdType = ExpressUtil.fixNullStr( rset.getString( 5 ));
					strST  = ExpressUtil.fixNullStr( rset.getString( 1 ) );
					strBllD = ExpressUtil.fixNullStr(  rset.getString( 10 ) );
					strChgType =  ExpressUtil.fixNullStr( rset.getString( 6) );
					strChgSubType  =  ExpressUtil.fixNullStr( rset.getString( 7 ));
					strLcName = ExpressUtil.fixNullStr( rset.getString( 11 ) );
					oHist.orderCompTime = ExpressUtil.fixNullStr( rset.getString( 9 ) );
					oHist.orderCompUser = ExpressUtil.fixNullStr( rset.getString( 12 ) );
					hOrdHist.put( rset.getString( 8 ), oHist);
					nPrevSeq = nCurrentSeq;
				}
				// clean up.
				if(rset != null){ rset.close(); rset = null;}
				if(pstmt != null){ pstmt.close(); pstmt = null;}

				// print last one on the record set.
				if( nPrevSeq > 0  )
				{
					if(strProdType.equals( "D" ) ){
						hbHtml.append(insertWGDelicatedRow(strST, StaticBDPReportData.getProductsName( strTempPrd, hProducts ),strOrdType,nSnqNun,strBname,strChgType,strChgSubType,false,strLcName ));
					}else if ( strProdType.equals( "P" ) || strProdType.equals( "E" ) )
					{
						hbHtml.append(insertWG1ARow(strST, StaticBDPReportData.getProductsName( strTempPrd, hProducts ),strOrdType,nSnqNun,strBname,strChgType,strChgSubType,true,strLcName ));
					}
					else if(strProdType.equals( "M" ) || strProdType.equals( "I" )
						 ) //gao, 08/25/2006, RIS 1449
					{
						hbHtml.append(insertWGVpnMetroELine(strST, StaticBDPReportData.getProductsName( strTempPrd, hProducts ),strOrdType,nSnqNun,strBname,strChgType,strChgSubType,false,strLcName ));
					}else{
						hbHtml.append(insertWG1ARow(strST, StaticBDPReportData.getProductsName( strTempPrd, hProducts ),strOrdType,nSnqNun,strBname,strChgType,strChgSubType,false,strLcName ));
					}
				}
				break;
			}			default:{}

		}
		System.out.println( "Business Data Product Report Completed:\t" + dFmt.format(new java.util.Date() ) );
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:hbhtml=" +  hbHtml.toString());
		return hbHtml.toString();
	}

	/* print online on report
	*/

	public long printReportColumnAvgSummary(StringBuffer sb, String strStepName)
	{
		//Log.write(Log.DEBUG_VERBOSE,"BDPReportBean:" + "printReportLineColumnAverageSummary(" + strStepName + ")");
		OrderHistory ohst = new OrderHistory();
		ohst = (OrderHistory)hOrderHistory.get(strStepName);
        long ohAvgTime = (ohst.orderTimeAccum.longValue()) / (ohst.orderStepCount.longValue());
		//Log.write(Log.DEBUG_VERBOSE,"BDPReportBean:" + "ohAvgTime = " + ohAvgTime);
		sb.append("<TD>&nbsp;" + StaticBDPReportData.printElapsedTime(ohAvgTime) + "</TD>\n");
		//Log.write(Log.DEBUG_VERBOSE,"BDPReportBean:" + "Elapsed Time = " + StaticBDPReportData.printElapsedTime(ohAvgTime) );
		return ohAvgTime;
	}


    public String insertWorksheetCellTimeDetail(String strStepIn, String strStepOut)
	{
		StringBuffer sb = new StringBuffer(128);
		Log.write(Log.DEBUG_VERBOSE,"BDPReportBean:" + "printWorksheetCellTimeDetail(" + strStepIn + ", " + strStepOut);
		OrderHistory ihst = new OrderHistory();
		OrderHistory ohst = new OrderHistory();
		ihst = (OrderHistory)hOrdHist.get(strStepIn);
		ohst = (OrderHistory)hOrdHist.get(strStepOut);
                Log.write(Log.DEBUG_VERBOSE,"BDPReportBean:" + "ohTimeAccum" + ihst.orderCompTime + ", "+ohst.orderCompTime );
		long ohTimeAccum = SLATools.calculateSLA(ihst.orderCompTime, ohst.orderCompTime);
            Log.write(Log.DEBUG_VERBOSE,"BDPReportBean:" + "ohTimeAccum" + ohTimeAccum);
		sb.append(BDPWorksheetBean.wgWrkshtCell(new Long(ohTimeAccum).toString()));
		sb.append(BDPWorksheetBean.wgWrkshtCell(ohst.orderCompUser));
		return sb.toString();
	}
    public void printReportLineColumnDetail(StringBuffer sb, String strStepIn, String strStepOut)
	{
		//Log.write(Log.DEBUG_VERBOSE,"BDPReportBean:" + "printReportLineColumnDetail(StringBuffer sb, " + strStepIn + ", " + strStepOut);
		OrderHistory ihst = new OrderHistory();
		OrderHistory ohst = new OrderHistory();
		ihst = (OrderHistory)hOrdHist.get(strStepIn);
		ohst = (OrderHistory)hOrdHist.get(strStepOut);
		long ohTimeAccum = SLATools.calculateSLA(ihst.orderCompTime, ohst.orderCompTime);
		long ohStepCount = 1;
		sb.append("<TD>&nbsp;" + StaticBDPReportData.printElapsedTime(ohTimeAccum) + " by " + ohst.orderCompUser + "</TD>\n");
		OrderHistory hist = new OrderHistory();
		if ((boolean)hOrderHistory.containsKey(strStepOut)) {
			hist = (OrderHistory)hOrderHistory.get(strStepOut);
		    ohTimeAccum = ohTimeAccum + hist.orderTimeAccum.longValue();
		    ohStepCount = ohStepCount + hist.orderStepCount.longValue();
		}
		hist.orderTimeAccum = new Long(ohTimeAccum);
		hist.orderStepCount = new Long(ohStepCount);
		hOrderHistory.put(strStepOut, hist);
	}
	public String printFulfillmentLine1A(	String strST, int nSnqNun, String strBname,
		String strProdType, String strOrdType, String strBllD, Hashtable hTTime, String strLcName )
	{
		StringBuffer sb = new StringBuffer( ONE_K);
		String strBllEDate = ExpressUtil.FormatDateYYYYMMDDD_HH24MMSS( strBllD );
		sb.append("<TR>");
		sb.append("<TD>&nbsp;" + ExpressUtil.getStateFullName( strST ) + "</td><td>" + nSnqNun + "</td>\n" );
		sb.append("<TD>&nbsp;" + strBname + "</td>\n") ;
		sb.append("<TD>&nbsp;" + strLcName + "</td>\n") ;
		sb.append("<TD>&nbsp;" +  StaticBDPReportData.getOrderTypeName( strOrdType, hOrderTypes) + "</td>\n" );
		if( hTTime != null ) // should never be null but good practice
		{
			String sbFmtDate = ExpressUtil.getDisplayDateFormat( (String) hTTime.get( SUBMITTED ));
			String sbSvcDate =  ExpressUtil.getDisplayDateFormat( (String) hTTime.get( SERVICE_COMPLETE) );
			sb.append("<TD>&nbsp;" + sbFmtDate  + "</TD> \n" )	;
			sb.append("<TD>&nbsp;" + sbSvcDate + "</TD>\n" );
			String strTemp = "<TD>&nbsp;" + (int)SLATools.calculateSLADays( (String) hTTime.get( SUBMITTED ) ,  (String) hTTime.get( SERVICE_COMPLETE) ) + "</TD>\n";
			if( strBllEDate.length() > 0 )
			{
				int nSubDays = (int)SLATools.calculateSLADays( (String) hTTime.get( SUBMITTED ) ,  strBllEDate);
				int ncompDays = (int)SLATools.calculateSLADays( (String) hTTime.get( SERVICE_COMPLETE) ,  strBllEDate);
				sb.append("<TD>&nbsp;" + strBllD + "</TD>\n" );
				sb.append("<TD>"  + (nSubDays < 0 ? "<span class=negativetime>&nbsp;" +  nSubDays : "<span>&nbsp;" + nSubDays ) + "</span></TD>\n" );
				sb.append( strTemp );
				sb.append("<TD>&nbsp;" +   (ncompDays < 0 ? "<span class=negativetime>&nbsp;"  + ncompDays : "<span>&nbsp;" + ncompDays)  +  "</span></TD>\n" );
			}else{
				sb.append("<TD> &nbsp;</TD>\n" );
				sb.append("<TD> &nbsp;</TD>\n" );
				sb.append( strTemp );
				sb.append("<TD> &nbsp;</TD>\n" );
			}
			sb.append("</tr>\n" );
		}
		return sb.toString();
	}

	/* print online on report
	*/

	public String printFulfillmentLine1C(String strST, int nSnqNun, String strBname,
		String strProdType, String strOrdType, String strChgType,String strChgSubType, String strBllD, Hashtable hTTime, String strLcName )
	{
		StringBuffer sb = new StringBuffer( 128);
		String strBllEDate = ExpressUtil.FormatDateYYYYMMDDD_HH24MMSS( strBllD );
		sb.append("<TR>");
		sb.append("<TD>&nbsp;" + strST  + "</td><td>" + nSnqNun + "</td>\n" );
		sb.append("<TD>&nbsp;" + strBname + "</td>\n" );
		sb.append("<TD>&nbsp;" + strLcName + "</td>\n") ;
		sb.append("<TD>&nbsp;" + StaticBDPReportData.getOrderTypeName( strOrdType, hOrderTypes ) + "</td>\n" );
		sb.append("<TD>&nbsp;" + StaticBDPReportData.getChangeTypeName( strChgType, hChangeTypes ) + "</td>\n" );
		sb.append("<TD>&nbsp;" + StaticBDPReportData.getChangeSubTypeName( strChgSubType, hSubChangeTypes )+ "</td>\n" );
		if( hTTime != null ) // should never be null but good practice
		{
			String sbFmtDate = ExpressUtil.getDisplayDateFormat( (String) hTTime.get( SUBMITTED ));
			String sbSvcDate =  ExpressUtil.getDisplayDateFormat( (String) hTTime.get( SERVICE_COMPLETE) );
			sb.append("<TD>&nbsp;" + sbFmtDate  + "</TD> \n" )	;
			sb.append("<TD>&nbsp;" + sbSvcDate + "</TD>\n" );
			String strTemp = "<TD>&nbsp;" + (int)SLATools.calculateSLADays( (String) hTTime.get( SUBMITTED ) ,  (String) hTTime.get( SERVICE_COMPLETE) ) + "</TD>\n";

			if( strBllEDate.length() > 0 )
			{

				int nSubDays = (int)SLATools.calculateSLADays( (String) hTTime.get( SUBMITTED ) ,  strBllEDate);
				int ncompDays = (int)SLATools.calculateSLADays( (String) hTTime.get( SERVICE_COMPLETE) ,  strBllEDate);
				sb.append("<TD>&nbsp;" + strBllD + "</TD>\n" );
				sb.append("<TD>"  + (nSubDays < 0 ? "<span class=negativetime>&nbsp;" + nSubDays : "<span>&nbsp;" + nSubDays  ) + "</span></TD>\n" );
				sb.append( strTemp );
				sb.append("<TD>&nbsp;" + (ncompDays < 0 ? "<span class=negativetime>&nbsp;" + ncompDays : "<span>&nbsp;" + ncompDays) +  "</span></TD>\n" );
			}else{
				sb.append("<TD> &nbsp;</TD>\n" );
				sb.append("<TD> &nbsp;</TD>\n" );
				sb.append( strTemp );
				sb.append("<TD> &nbsp;</TD>\n" );
			}
			sb.append("</tr>\n" );
		}
		return sb.toString();
	}


	/* print online on report
	*/

	public String printFulfillmentLine1B(	String strST, int nSnqNun, String strBname,
		String strProdType, String strOrdType, String strChgType,String strChgSubType, String strBllD, Hashtable hTTime, String strLcName )
	{
		StringBuffer sb = new StringBuffer( 128 );

		String strBllEDate = ExpressUtil.FormatDateYYYYMMDDD_HH24MMSS( strBllD );
		sb.append("<TR>");
		sb.append("<TD>&nbsp;" + strST  + "</td><td>" + nSnqNun + "</td>\n" );
		sb.append("<TD>&nbsp;" + strBname + "</td>\n" );
		sb.append("<TD>&nbsp;" + strLcName + "</td>\n") ;
		sb.append("<TD>&nbsp;" + StaticBDPReportData.getOrderTypeName( strOrdType, hOrderTypes ) + "</td>\n" );
		sb.append("<TD>&nbsp;" + StaticBDPReportData.getChangeTypeName( strChgType, hChangeTypes ) + "</td>\n" );
		sb.append("<TD>&nbsp;" + StaticBDPReportData.getChangeSubTypeName( strChgSubType, hSubChangeTypes )+ "</td>\n" );

		if( hTTime != null ) // should never be null but good practice
		{

			String sbFmtDate = ExpressUtil.getDisplayDateFormat( (String) hTTime.get( SUBMITTED ));
			String sbSvcDate =  ExpressUtil.getDisplayDateFormat( (String) hTTime.get( TEST_COMPLETE) );
			//System.out.println( sbFmtDate   + "Submit" + sbSvcDate );
			sb.append("<TD>&nbsp;" + sbFmtDate  + "</TD> \n" )	;
			sb.append("<TD>&nbsp;" + sbSvcDate + "</TD>\n" );
			String strTemp = "<TD>&nbsp;" + (int)SLATools.calculateSLADays( (String) hTTime.get( SUBMITTED ) ,  (String) hTTime.get( TEST_COMPLETE) ) + "</TD>\n";
			if( strBllEDate.length() > 0 )
			{
				int nSubDays = (int)SLATools.calculateSLADays( (String) hTTime.get( SUBMITTED ) ,  strBllEDate);
				int ncompDays = (int)SLATools.calculateSLADays( (String) hTTime.get( TEST_COMPLETE) ,  strBllEDate);
				sb.append("<TD>&nbsp;" + strBllD + "</TD>\n" );
				sb.append("<TD>"  + (nSubDays < 0 ? "<span class=negativetime>&nbsp;" + nSubDays : "<span>&nbsp;" + nSubDays ) + "</span></TD>\n" );
				sb.append( strTemp );
				sb.append("<TD>&nbsp;" +   (ncompDays < 0 ? "<span class=negativetime>&nbsp;" + ncompDays : "<span>&nbsp;" + ncompDays ) +  "</span></TD>\n" );
			}else{
				sb.append("<TD> &nbsp;</TD>\n" );
				sb.append("<TD> &nbsp;</TD>\n" );
				sb.append( strTemp );
				sb.append("<TD> &nbsp;</TD>\n" );
			}
			sb.append("</tr>\n" );
		}
		return sb.toString();
	}
	//*******************************************************************************************************************************************
	// printWGDelicatedRow, prints online on screen
	public String printWGDelicatedRow(	int nSnqNun, String strBname, String  strLcName )
	{
		//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "printWGDelicatedRow(" + nSnqNun + "," + strBname + ", hTTime ," + strLcName + ", hUser" );
		StringBuffer sb = new StringBuffer( 128);
		sb.append("<TR>");
		sb.append("<td>" + nSnqNun + "</td>\n" );
		sb.append("<TD>&nbsp;" + strBname + "</td>\n" );
		sb.append("<TD>&nbsp;" + strLcName + "</td>\n") ;
		if( hOrdHist != null ) // should never be null but good practice
		{
			OrderHistory iOrdHst = new OrderHistory();
			iOrdHst = (OrderHistory)hOrdHist.get(INITIAL);
			sb.append("<TD>&nbsp;" + ExpressUtil.getDisplayDateFormat( iOrdHst.orderCompTime )  + "</TD> \n" )	;
			printReportLineColumnDetail(sb,INITIAL         ,SUBMITTED       );
			printReportLineColumnDetail(sb,SUBMITTED       ,DE_IN_PROGRESS  );
			printReportLineColumnDetail(sb,DE_IN_PROGRESS  ,DE_COMPLETE     );
			printReportLineColumnDetail(sb,DE_COMPLETE     ,MKT_PENDING     );
			printReportLineColumnDetail(sb,MKT_PENDING     ,MKT_COMPLETE    );
			printReportLineColumnDetail(sb,MKT_COMPLETE    ,DSTAC_ACCEPTED  );
			printReportLineColumnDetail(sb,DSTAC_ACCEPTED  ,SERVICE_COMPLETE);
			printReportLineColumnDetail(sb,SERVICE_COMPLETE,BILLING         );
			printReportLineColumnDetail(sb,BILLING         ,BILL_COMPLETE   );
			OrderHistory oOrdHst = new OrderHistory();
			oOrdHst = (OrderHistory)hOrdHist.get(BILL_COMPLETE);
			sb.append("<TD>&nbsp;" + ExpressUtil.getDisplayDateFormat( oOrdHst.orderCompTime )  + "</TD> \n" )	;
			sb.append("<TD>&nbsp;"
				+ StaticBDPReportData.printElapsedTime(SLATools.calculateSLA(iOrdHst.orderCompTime, oOrdHst.orderCompTime ), 9 )
				+ "</TD></tr>\n");
		}else{
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD></tr> \n" )	;
		}
		return sb.toString();
	}
	//  insert one row of work group worksheet for WGDelicatedRow.
	public String insertWGDelicatedRow(String strStNm, String strPrdNm, String strOrdTyp, int nSnqNun, String strBname,  String strChgType, String  strChgSubType,  boolean bMteVpn, String strLcName )
	{
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "insertWGDelicatedRow(" + strStNm + ", " + strPrdNm + ", " + strOrdTyp + ", " + nSnqNun + ", " + strBname );
		StringBuffer sb = new StringBuffer( 128);
		sb.append("<tr>");
		sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getStateFullName(strStNm)));
		sb.append(BDPWorksheetBean.wgWrkshtCell(strPrdNm));
		sb.append(BDPWorksheetBean.wgWrkshtCell(StaticBDPReportData.getOrderTypeName( strOrdTyp, hOrderTypes )));
		sb.append(BDPWorksheetBean.wgWrkshtCell(String.valueOf(nSnqNun)));
		sb.append(BDPWorksheetBean.wgWrkshtCell(strBname));
		sb.append(BDPWorksheetBean.wgWrkshtCell(strLcName));
		if( hOrdHist != null ) // should never be null but good practice
		{
			OrderHistory iOrdHst = new OrderHistory();
			iOrdHst = (OrderHistory)hOrdHist.get(INITIAL);
			OrderHistory oOrdHst = new OrderHistory();
			oOrdHst = (OrderHistory)hOrdHist.get(BILL_COMPLETE);
			sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getDisplayDateFormat( iOrdHst.orderCompTime )));
			sb.append(insertWorksheetCellTimeDetail(INITIAL         ,SUBMITTED       ));
			sb.append(insertWorksheetCellTimeDetail(SUBMITTED       ,DE_IN_PROGRESS  ));
			sb.append(insertWorksheetCellTimeDetail(DE_IN_PROGRESS  ,DE_COMPLETE     ));
			sb.append(insertWorksheetCellTimeDetail(DE_COMPLETE     ,MKT_PENDING     ));
			sb.append(insertWorksheetCellTimeDetail(MKT_PENDING     ,MKT_COMPLETE    ));
			sb.append(insertWorksheetCellTimeDetail(MKT_COMPLETE    ,DSTAC_ACCEPTED  ));
			sb.append(insertWorksheetCellTimeDetail(DSTAC_ACCEPTED  ,SERVICE_COMPLETE));
			sb.append(insertWorksheetCellTimeDetail(SERVICE_COMPLETE,BILLING         ));
			sb.append(insertWorksheetCellTimeDetail(BILLING         ,BILL_COMPLETE   ));
			sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getDisplayDateFormat( oOrdHst.orderCompTime )));
		}
		sb.append("<\tr>");
		return sb.toString();
	}
	public String printWGDelicatedRowAvgSummary()
	{
		//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "printWGDelicatedRowAvgSummary()" );
		StringBuffer sb = new StringBuffer( 128);
		long totTime = 0;
		sb.append("<TR>");
		if( hOrderHistory != null ) // should never be null but good practice
		{
			totTime += printReportColumnAvgSummary(sb,SUBMITTED       );
		    totTime += printReportColumnAvgSummary(sb,DE_IN_PROGRESS  );
			totTime += printReportColumnAvgSummary(sb,DE_COMPLETE     );
			totTime += printReportColumnAvgSummary(sb,MKT_PENDING     );
			totTime += printReportColumnAvgSummary(sb,MKT_COMPLETE    );
			totTime += printReportColumnAvgSummary(sb,DSTAC_ACCEPTED  );
			totTime += printReportColumnAvgSummary(sb,SERVICE_COMPLETE);
			totTime += printReportColumnAvgSummary(sb,BILLING         );
			totTime += printReportColumnAvgSummary(sb,BILL_COMPLETE   );
			sb.append("<TD>&nbsp;</TD>\n" );
		    sb.append("<TD>&nbsp;" + StaticBDPReportData.printElapsedTime(totTime, 9 ) + "</TD></tr> \n");
		}else{
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD></tr> \n" )	;
		}
		return sb.toString();
	}
	//*******************************************************************************************************************************************
	//  print one line of work group report for vpn and metroe E.
	public String printWG1ARow(	int nSnqNun, String strBname,  String strChgType, String  strChgSubType,  boolean bMteVpn, String strLcName )
	{
		//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "printWG1ARow(" + nSnqNun + "," + strBname + "," + strChgType + "," + strChgSubType + "," + bMteVpn + ", " + strLcName );
		StringBuffer sb = new StringBuffer( 128);
		sb.append("<TR>");
		sb.append("<td>" + nSnqNun + "</td>\n" );
		sb.append("<TD>&nbsp;" + strBname + "</td>\n" );
		sb.append("<TD>&nbsp;" + strLcName + "</td>\n") ;
		if( bMteVpn) {
			sb.append("<TD>&nbsp;" + StaticBDPReportData.getChangeTypeName( strChgType, hChangeTypes )  + "</td>\n" );
			sb.append("<TD>&nbsp;" +  StaticBDPReportData.getChangeSubTypeName( strChgSubType, hSubChangeTypes  ) + "</td>\n" );
		}
		if( hOrdHist != null ) // should never be null but good practice
		{
			//System.err.println( hTTime.toString() );
			OrderHistory iOrdHst = new OrderHistory();
			iOrdHst = (OrderHistory)hOrdHist.get(INITIAL);
			sb.append("<TD>&nbsp;" + ExpressUtil.getDisplayDateFormat( iOrdHst.orderCompTime )  + "</TD> \n" )	;
			printReportLineColumnDetail(sb,INITIAL         ,SUBMITTED       );
			printReportLineColumnDetail(sb,SUBMITTED       ,DE_IN_PROGRESS  );
			printReportLineColumnDetail(sb,DE_IN_PROGRESS  ,DE_COMPLETE     );
			printReportLineColumnDetail(sb,DE_COMPLETE     ,DSTAC_ACCEPTED  );
			printReportLineColumnDetail(sb,DSTAC_ACCEPTED  ,SERVICE_COMPLETE);
			printReportLineColumnDetail(sb,SERVICE_COMPLETE,BILLING         );
			printReportLineColumnDetail(sb,BILLING         ,BILL_COMPLETE   );
			OrderHistory oOrdHst = new OrderHistory();
			oOrdHst = (OrderHistory)hOrdHist.get(BILL_COMPLETE);
			sb.append("<TD>&nbsp;" + ExpressUtil.getDisplayDateFormat( oOrdHst.orderCompTime )  + "</TD> \n" )	;
			sb.append("<TD>&nbsp;"
				+ StaticBDPReportData.printElapsedTime(SLATools.calculateSLA(iOrdHst.orderCompTime, oOrdHst.orderCompTime), 7 )
				+ "</TD></tr>\n");
		}else{
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD></tr> \n" )	;
		}
		//System.err.println( sb.toString());
		return sb.toString();
	}

	//  insert one row of work group worksheet for vls
	public String insertWG1ARow(String strStNm, String strPrdNm, String strOrdTyp, int nSnqNun, String strBname,  String strChgType, String  strChgSubType,  boolean bMteVpn, String strLcName )
	{
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "insertWG1ARow(" + strStNm + ", " + strPrdNm + ", " + strOrdTyp + ", " + nSnqNun + ", " + strBname );
		StringBuffer sb = new StringBuffer(128);
		sb.append("<tr>");
		sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getStateFullName(strStNm)));
		sb.append(BDPWorksheetBean.wgWrkshtCell(strPrdNm)); //StaticBDPReportData.getProductsName( strPrdNm, hProducts ));
		sb.append(BDPWorksheetBean.wgWrkshtCell(StaticBDPReportData.getOrderTypeName( strOrdTyp, hOrderTypes )));
		sb.append(BDPWorksheetBean.wgWrkshtCell(String.valueOf(nSnqNun)));
		sb.append(BDPWorksheetBean.wgWrkshtCell(strBname));
		sb.append(BDPWorksheetBean.wgWrkshtCell(strLcName));
		if( bMteVpn) {
			sb.append(BDPWorksheetBean.wgWrkshtCell(StaticBDPReportData.getChangeTypeName( strChgType, hChangeTypes )));
			sb.append(BDPWorksheetBean.wgWrkshtCell(StaticBDPReportData.getChangeSubTypeName( strChgSubType, hSubChangeTypes  )));
		}
//		else {
//			sb.append("<TD></td>\n" );
//			sb.append("<TD></td>\n" );
//		}
		if( hOrdHist != null ) // should never be null but good practice
		{
			OrderHistory iOrdHst = new OrderHistory();
			iOrdHst = (OrderHistory)hOrdHist.get(INITIAL);
			OrderHistory oOrdHst = new OrderHistory();
			oOrdHst = (OrderHistory)hOrdHist.get(BILL_COMPLETE);
			sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getDisplayDateFormat( iOrdHst.orderCompTime )));
			sb.append(insertWorksheetCellTimeDetail(INITIAL         ,SUBMITTED       ));
			sb.append(insertWorksheetCellTimeDetail(SUBMITTED       ,DE_IN_PROGRESS  ));
			sb.append(insertWorksheetCellTimeDetail(DE_IN_PROGRESS  ,DE_COMPLETE     ));
			sb.append(insertWorksheetCellTimeDetail(DE_COMPLETE     ,DSTAC_ACCEPTED  ));
			sb.append(insertWorksheetCellTimeDetail(DSTAC_ACCEPTED  ,DSTAC_IPTST_COMP));
			sb.append(insertWorksheetCellTimeDetail(DSTAC_IPTST_COMP,BILLING         ));
			sb.append(insertWorksheetCellTimeDetail(BILLING         ,BILL_COMPLETE   ));

			sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getDisplayDateFormat( oOrdHst.orderCompTime )));
			sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getDisplayDateFormat( oOrdHst.orderCompTime )));
		}
		sb.append("</tr>");
		return sb.toString();
	}

	public String printWG1ARowAvgSummary()
	{
		//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "printWG1ARowAvgSummary()" );
		StringBuffer sb = new StringBuffer( 128);
		long totTime = 0;
		sb.append("<TR>");
		sb.append("<td></td>\n" );
		if( hOrderHistory != null ) // should never be null but good practice
		{
			totTime += printReportColumnAvgSummary(sb,SUBMITTED       );
		    totTime += printReportColumnAvgSummary(sb,DE_IN_PROGRESS  );
			totTime += printReportColumnAvgSummary(sb,DE_COMPLETE     );
			totTime += printReportColumnAvgSummary(sb,DSTAC_ACCEPTED  );
			totTime += printReportColumnAvgSummary(sb,SERVICE_COMPLETE);
			totTime += printReportColumnAvgSummary(sb,BILLING         );
			totTime += printReportColumnAvgSummary(sb,BILL_COMPLETE   );
			sb.append("<TD>&nbsp;</TD>\n" );
		    sb.append("<TD>&nbsp;" + StaticBDPReportData.printElapsedTime(totTime, 7 ) + "</TD></tr> \n");
		}else{
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD></tr> \n" )	;
		}
		return sb.toString();
	}
	//*******************************************************************************************************************************************
	// print metro-e and vpn one line ( change or move )
	public String printWGVpnMetroELine(	 int nSnqNun, String strBname,String strChgType, String  strChgSubType, String strLcName )
	{
		//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "printWGVpnMetroELine(" + nSnqNun + "," + strBname + "," + strChgType + "," + strChgSubType + ", " + strLcName );
		StringBuffer sb = new StringBuffer( 128);
		sb.append("<TR>");
		sb.append("<td>" + nSnqNun + "</td>\n" );
		sb.append("<TD>&nbsp;" +  strBname  + "</td>\n" );
		sb.append("<TD>&nbsp;" + strLcName + "</td>\n") ;
		sb.append("<TD>&nbsp;" + StaticBDPReportData.getChangeTypeName( strChgType, hChangeTypes )  + "</td>\n" );
		sb.append("<TD>&nbsp;" +  StaticBDPReportData.getChangeSubTypeName( strChgSubType, hSubChangeTypes  ) + "</td>\n" );
		if( hOrdHist != null ) // should never be null but good practice
		{
			//System.err.println( hTTime.toString() );
			OrderHistory iOrdHst = new OrderHistory();
			iOrdHst = (OrderHistory)hOrdHist.get(INITIAL);
			sb.append("<TD>&nbsp;" + ExpressUtil.getDisplayDateFormat( iOrdHst.orderCompTime )  + "</TD> \n" )	;
			printReportLineColumnDetail(sb,INITIAL         ,SUBMITTED       );
			printReportLineColumnDetail(sb,SUBMITTED       ,DE_IN_PROGRESS  );
			printReportLineColumnDetail(sb,DE_IN_PROGRESS  ,DE_COMPLETE     );
			printReportLineColumnDetail(sb,DE_COMPLETE     ,MKT_PENDING     );
			printReportLineColumnDetail(sb,MKT_PENDING     ,MKT_COMPLETE    );
			printReportLineColumnDetail(sb,MKT_COMPLETE    ,DSTAC_ACCEPTED  );
			printReportLineColumnDetail(sb,DSTAC_ACCEPTED  ,DSTAC_IPTST_COMP);
			printReportLineColumnDetail(sb,DSTAC_IPTST_COMP,BILLING         );
			printReportLineColumnDetail(sb,BILLING         ,BILL_COMPLETE   );
			printReportLineColumnDetail(sb,BILL_COMPLETE   ,SERVICE_COMPLETE);
			OrderHistory oOrdHst = new OrderHistory();
			oOrdHst = (OrderHistory)hOrdHist.get(SERVICE_COMPLETE);
			sb.append("<TD>&nbsp;" + ExpressUtil.getDisplayDateFormat( oOrdHst.orderCompTime )  + "</TD> \n" )	;
			sb.append("<TD>&nbsp;"
				+ StaticBDPReportData.printElapsedTime(SLATools.calculateSLA(iOrdHst.orderCompTime, oOrdHst.orderCompTime ), 10 )
				+ "</TD></tr>\n");
		}else{
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD> \n" )	;
			sb.append("<TD>&nbsp;</TD></tr> \n" )	;
		}
		return sb.toString();
	}
	//  insert one row of work group worksheet for vpn and metroe E.
	public String insertWGVpnMetroELine(String strStNm, String strPrdNm, String strOrdTyp, int nSnqNun, String strBname,  String strChgType, String  strChgSubType,  boolean bMteVpn, String strLcName )
	{
		Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "insertWGVpnMetroELine(" + strStNm + ", " + strPrdNm + ", " + strOrdTyp + ", " + nSnqNun + ", " + strBname );
		StringBuffer sb = new StringBuffer(128);
		sb.append("<tr>");
		sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getStateFullName(strStNm)));
		sb.append(BDPWorksheetBean.wgWrkshtCell(strPrdNm));
		sb.append(BDPWorksheetBean.wgWrkshtCell(StaticBDPReportData.getOrderTypeName( strOrdTyp, hOrderTypes )));
		sb.append(BDPWorksheetBean.wgWrkshtCell(String.valueOf(nSnqNun)));
		sb.append(BDPWorksheetBean.wgWrkshtCell(strBname));
		sb.append(BDPWorksheetBean.wgWrkshtCell(strLcName));

		if( hOrdHist != null ) // should never be null but good practice
		{
			OrderHistory iOrdHst = new OrderHistory();
			iOrdHst = (OrderHistory)hOrdHist.get(INITIAL);
			sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getDisplayDateFormat( iOrdHst.orderCompTime )));
			sb.append(insertWorksheetCellTimeDetail(INITIAL         ,SUBMITTED       ));
			sb.append(insertWorksheetCellTimeDetail(SUBMITTED       ,DE_IN_PROGRESS  ));
			sb.append(insertWorksheetCellTimeDetail(DE_IN_PROGRESS  ,DE_COMPLETE     ));
			sb.append(insertWorksheetCellTimeDetail(DE_COMPLETE     ,MKT_PENDING     ));
			sb.append(insertWorksheetCellTimeDetail(MKT_PENDING     ,MKT_COMPLETE    ));
			sb.append(insertWorksheetCellTimeDetail(MKT_COMPLETE    ,DSTAC_ACCEPTED  ));
			sb.append(insertWorksheetCellTimeDetail(DSTAC_ACCEPTED  ,DSTAC_IPTST_COMP));
			sb.append(insertWorksheetCellTimeDetail(DSTAC_IPTST_COMP,BILLING         ));
			sb.append(insertWorksheetCellTimeDetail(BILLING         ,BILL_COMPLETE   ));
			sb.append(insertWorksheetCellTimeDetail(BILL_COMPLETE   ,SERVICE_COMPLETE));
			OrderHistory oOrdHst = new OrderHistory();
			oOrdHst = (OrderHistory)hOrdHist.get(SERVICE_COMPLETE);
			sb.append(BDPWorksheetBean.wgWrkshtCell(ExpressUtil.getDisplayDateFormat( oOrdHst.orderCompTime )));
		}
		sb.append("<\tr>");
		return sb.toString();
	}
	public String printWGVpnMetroELineAvgSummary()
	{
		//Log.write(Log.DEBUG_VERBOSE,  "BDPReportBean:" +  "printWGVpnMetroELineRowAvgSummary()" );
		StringBuffer sb = new StringBuffer( 128);
		long totTime = 0;
		sb.append("<TR>");
		if( hOrderHistory != null ) // should never be null but good practice
		{
			totTime += printReportColumnAvgSummary(sb,SUBMITTED       );
		    totTime += printReportColumnAvgSummary(sb,DE_IN_PROGRESS  );
			totTime += printReportColumnAvgSummary(sb,DE_COMPLETE     );
			totTime += printReportColumnAvgSummary(sb,MKT_PENDING     );
			totTime += printReportColumnAvgSummary(sb,MKT_COMPLETE    );
			totTime += printReportColumnAvgSummary(sb,DSTAC_ACCEPTED  );
			totTime += printReportColumnAvgSummary(sb,DSTAC_IPTST_COMP);
			totTime += printReportColumnAvgSummary(sb,BILLING         );
			totTime += printReportColumnAvgSummary(sb,BILL_COMPLETE   );
			totTime += printReportColumnAvgSummary(sb,SERVICE_COMPLETE);
			sb.append("<TD>&nbsp;</TD>\n" );
		    sb.append("<TD>&nbsp;" + StaticBDPReportData.printElapsedTime(totTime, 10 ) + "</TD></tr> \n");
		}else{
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD> \n" )	;
			sb.append("<TD>&nbsp; </TD></tr> \n" )	;
		}
		return sb.toString();
	}
	//*******************************************************************************************************************************************
	/* Decide if IP_VPN or Metro E product were selected.
	 */
	public boolean isVPNorMetroE()
	{
		// gao, 08/25/2006, RIS 1449, add type 'L'
		// String[] strVpnMtre = {"I", "M" , "E", "P" };
		String[] strVpnMtre = {"I", "M" , "E", "P" ,"L"};
		int i = 0;
		boolean bFoundIt = false;
		while( i < strVpnMtre.length )
		{
			if( ExpressUtil.isElementOf(m_strProducts, strVpnMtre[i] ) )
			{
				bFoundIt = true;
				i = strVpnMtre.length;
			}
			i++;
		}
		return bFoundIt;
	}

}


