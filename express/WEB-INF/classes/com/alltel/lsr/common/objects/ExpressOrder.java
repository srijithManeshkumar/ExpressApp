/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2003
 *                                       BY
 *                              ALLTEL COMMUNICATIONS
 */
/*
 * MODULE:	ExpressOrder.java
 *
 * DESCRIPTION:	Populate information about this order type.  Use fields in db - must be setup
 *		in TABLE_COLUMN_ADMIN_T and TYPE_IND_T first.
 *
 * AUTHOR:      psedlak
 *
 * DATE:        9-9-2003
 *
 * HISTORY:	pjs 6-21-2005 make serializable
 *
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import java.io.Serializable;
import com.alltel.lsr.common.util.*;

public class ExpressOrder implements Serializable
{
	protected String m_strTYP_IND;
	protected String m_strSRVC_TYP_CD;	//NOTE This is the DEFAULT for this order type. Bean can override this value

	private Hashtable m_hash;

	public String toString()
	{
		String tmp="";
		tmp += "Express Order: Type=["+m_strTYP_IND+"] ServiceTypeCode=["+m_strSRVC_TYP_CD+"]";
		return tmp;
	}

	public ExpressOrder(String TYP_IND)
	{
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			init(TYP_IND, conn);
		}
		catch (Exception e)
		{
			Log.write(Log.ERROR, "ExpressOrder() trapped exception");
		}
		finally
		{
			DatabaseManager.releaseConnection(conn);
		}
	}

	public ExpressOrder(String TYP_IND, Connection conn) 
	{	
		init(TYP_IND, conn);
	}

	public void init(String TYP_IND, Connection conn)
	{
		m_strTYP_IND = TYP_IND;
		m_hash = new Hashtable();
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
			String strQuery = "SELECT TBL_CLMN_ADMN_DSCRPTN, TBL_CLMN_ADMN_DB_NM FROM TABLE_COLUMN_ADMIN_T T "+
				" WHERE T.TBL_ADMN_SQNC_NMBR = (SELECT TBL_ADMN_SQNC_NMBR FROM TYPE_IND_T WHERE TYP_IND='"+m_strTYP_IND+
				"') ORDER BY  T.TBL_CLMN_ADMN_SQNC ";
			ResultSet rs = stmt.executeQuery(strQuery);
			while (rs.next())
			{
		        m_hash.put(rs.getString("TBL_CLMN_ADMN_DSCRPTN"), rs.getString("TBL_CLMN_ADMN_DB_NM"));
		        //Log.write(Log.DEBUG, "ExpressOrder() Added hash=" + rs.getString("TBL_CLMN_ADMN_DSCRPTN")+"="+rs.getString("TBL_CLMN_ADMN_DB_NM") );
			}
			rs.close();
			rs=null;
		}
		catch (Exception e)
		{
			System.out.println("ExpressOrder() trapped exception ["+ e + "]");
			//Log.write(Log.ERROR, "ExpressOrder() trapped exception");
		}
		finally
		{
		}
		//this is used alot, so make it easy
		m_strSRVC_TYP_CD = (String)m_hash.get("SRVC_TYP_CD");
	}


	//Getters.
	public String getTYP_IND() 
	{	return m_strTYP_IND;	}

	public String getSRVC_TYP_CD() 
	{	//return (String)m_hash.get("SRVC_TYP_CD");
		return m_strSRVC_TYP_CD;
	}

	public String getTBL_NAME() 
	{	return (String)m_hash.get("TBL_NAME");
	}
	public String getSQNC_COLUMN() 
	{	return (String)m_hash.get("SQNC_COLUMN");
	}
	public String getVRSN_COLUMN() 
	{	return (String)m_hash.get("VRSN_COLUMN");
	}

	public String getAttribute(String attribute)
	{	return (String)m_hash.get(attribute);
	}

	protected void setSRVC_TYP_CD(String newSRVC_TYP_CD)
	{	this.m_strSRVC_TYP_CD = newSRVC_TYP_CD;
	}

}
