/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL INFORMATION SERVICES
 */

/* 
 * MODULE:		HolidayBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        01-31-2002
 * 
 * HISTORY:
 *	xx/xx/2002  initial check-in.
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/HolidayBean.java  $
/*
/*   Rev 1.1   31 Jan 2002 08:45:58   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:46   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class HolidayBean extends AlltelCtlrBean 
{
	final private String m_strTableName = "HOLIDAY_T";

	private String m_strHldyDt = "          ";
	private String m_strHldyDtSPANStart = " ";
	private String m_strHldyDtSPANEnd = " ";

	private String m_strHldyDscrptn = " ";
	private String m_strHldyDscrptnSPANStart = " ";
	private String m_strHldyDscrptnSPANEnd = " ";

	public HolidayBean() {
		setSecurityTags(m_strTableName);
	}
	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getHldyDt() { return m_strHldyDt; }
	public String getHldyDtSPANStart() { return m_strHldyDtSPANStart; }
	public String getHldyDtSPANEnd() { return m_strHldyDtSPANEnd; }

	public String getHldyDscrptn() { return m_strHldyDscrptn; }
	public String getHldyDscrptnSPANStart() { return m_strHldyDscrptnSPANStart; }
	public String getHldyDscrptnSPANEnd() { return m_strHldyDscrptnSPANEnd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setHldyDt(String aHldyDt)
	{ 
		if (aHldyDt != null)
			this.m_strHldyDt = aHldyDt.trim(); 
		else
			this.m_strHldyDt = aHldyDt; 
	}
	public void setHldyDscrptn(String aHldyDscrptn)
	{ 
		if (aHldyDscrptn != null)
			this.m_strHldyDscrptn = aHldyDscrptn.trim(); 
		else
			this.m_strHldyDscrptn = aHldyDscrptn; 
	}

	public int deleteHolidayBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			String strQuery = "DELETE HOLIDAY_T WHERE HLDY_DT = TO_DATE('" + m_strHldyDt + "', 'MM-DD-YYYY')";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			stmt.executeUpdate(strQuery);
		}
		catch(SQLException sqle)
		{
			return handleSQLError(sqle.getErrorCode());
		}
		catch(Exception e)
		{
			return 1;
		}
		finally
		{	DatabaseManager.releaseConnection(con);
		}

		return 0;
	}

	public int retrieveHolidayBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			String strQuery = "SELECT HLDY_DSCRPTN, MDFD_DT, MDFD_USERID " +
				"FROM HOLIDAY_T WHERE HLDY_DT = TO_DATE('" + m_strHldyDt + "', 'MM-DD-YYYY')";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);
			String tmpHldyDt;

			if (rs.next())
			{
				this.m_strHldyDscrptn = rs.getString("HLDY_DSCRPTN");
				this.m_strMdfdDt = rs.getString("MDFD_DT");
				this.m_strMdfdUserid = rs.getString("MDFD_USERID");
			}
			else
			{
				return 1;
			}
		}
		catch(SQLException sqle)
		{
			return handleSQLError(sqle.getErrorCode());
		}
		catch(Exception e)
		{
			return 1;
		}
		finally
		{	DatabaseManager.releaseConnection(con);
		}

		return 0;
	}

	public int updateHolidayBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			String strQuery = "UPDATE HOLIDAY_T SET HLDY_DSCRPTN = '" + Toolkit.replaceSingleQwithDoubleQ(m_strHldyDscrptn) + 
				"', MDFD_DT = sysdate, MDFD_USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + 
				"' WHERE HLDY_DT = TO_DATE('" + m_strHldyDt + "', 'MM-DD-YYYY')";
			
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			if (stmt.executeUpdate(strQuery) <= 0)
			{
				throw new SQLException(null,null,100);
			}
		}
		catch(SQLException sqle)
		{
			return handleSQLError(sqle.getErrorCode());
		}
		catch(Exception e)
		{
			return 1;
		}
		finally
		{	DatabaseManager.releaseConnection(con);
		}

		return 0;
	}

	public int saveHolidayBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			String strQuery = "INSERT INTO HOLIDAY_T VALUES (TO_DATE('" + m_strHldyDt + "','MM/DD/YYYY') ,'" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strHldyDscrptn) + "', sysdate,'" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "')";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			stmt.executeUpdate(strQuery);
		}
		catch(SQLException sqle)
		{
			return handleSQLError(sqle.getErrorCode());
		}
		catch(Exception e)
		{
			return 1;
		}
		finally
		{	DatabaseManager.releaseConnection(con);
		}

		return 0;
	}

	public boolean validateHolidayBean()
	{
		boolean rc = true;

		m_strHldyDtSPANStart = getSPANStart();
		m_strHldyDtSPANEnd = getSPANEnd();
		m_strHldyDscrptnSPANStart = getSPANStart();
		m_strHldyDscrptnSPANEnd = getSPANEnd();

		// Validate Holiday Date
		if ((m_strHldyDt == null) || (m_strHldyDt.length() != 10))
		{
			m_strHldyDtSPANStart = getErrSPANStart();
			m_strHldyDtSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (! Validate.isValidDate(m_strHldyDt))
		{
			m_strHldyDtSPANStart = getErrSPANStart();
			m_strHldyDtSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Holiday Description
		if ((m_strHldyDscrptn == null) || (m_strHldyDscrptn.length() == 0))
		{
			m_strHldyDscrptnSPANStart = getErrSPANStart();
			m_strHldyDscrptnSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strHldyDscrptn)) 
		{
			m_strHldyDscrptnSPANStart = getErrSPANStart();
			m_strHldyDscrptnSPANEnd = getErrSPANEnd();
			rc = false;
		}

		if (rc == false)
			m_strErrMsg = "ERROR:  Please review the data";

		return rc;
	}

	public boolean validateMdfdDt()
	{	
		Connection con = null;
		Statement stmt = null;
		String strMdfdDt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			String strQuery = "SELECT MDFD_DT FROM HOLIDAY_T " + 
				"WHERE HLDY_DT = TO_DATE('" + m_strHldyDt + "', 'MM-DD-YYYY')";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);
			if (rs.next())
			{
				strMdfdDt = rs.getString("MDFD_DT");
			}
			else
			{
				throw new SQLException(null,null,100);
			}
		}
		catch(SQLException sqle)
		{
			handleSQLError(sqle.getErrorCode());
			return false;
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{	DatabaseManager.releaseConnection(con);
		}

		// As long as the dates are equal, all is well in the world and no one has changed the record
		if (strMdfdDt.equals(m_strMdfdDt))
		{
			return true;
		}
		else
		{
			m_strErrMsg = "ERROR:  This row has been modified since you retrieved it. " +
				"Please CANCEL and retrieve the row again.";
			return false;
		}
	}

}// end of HolidayBean()
