/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL COMMUNICATIONS, INC.
 */

/* 
 * MODULE:		OCNStateBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        01-31-2002
 * 
 * HISTORY:
 *	1/31/2002  initial check-in.
 *
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class OCNStateBean extends AlltelCtlrBean
{
	final private String m_strTableName = "OCN_STATE_T";

	private String m_strOcnSttSqncNmbr = " ";
	private String m_strOcnCd = " ";
	private String m_strSttCd = " ";

	private String m_strOcnSttSlaDys = " ";
	private String m_strOcnSttSlaDysSPANStart = " ";
	private String m_strOcnSttSlaDysSPANEnd = " ";

	private String m_strOcnSttCntrctPrcntg = " ";
	private String m_strOcnSttCntrctPrcntgSPANStart = " ";
	private String m_strOcnSttCntrctPrcntgSPANEnd = " ";

	public OCNStateBean() {
		setSecurityTags(m_strTableName);
	}
	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getOcnSttSqncNmbr() { return m_strOcnSttSqncNmbr; }
	public String getOcnCd() { return m_strOcnCd; }
	public String getSttCd() { return m_strSttCd; }

	public String getOcnSttSlaDys() { return m_strOcnSttSlaDys; }
	public String getOcnSttSlaDysSPANStart() { return m_strOcnSttSlaDysSPANStart; }
	public String getOcnSttSlaDysSPANEnd() { return m_strOcnSttSlaDysSPANEnd; }

	public String getOcnSttCntrctPrcntg() { return m_strOcnSttCntrctPrcntg; }
	public String getOcnSttCntrctPrcntgSPANStart() { return m_strOcnSttCntrctPrcntgSPANStart; }
	public String getOcnSttCntrctPrcntgSPANEnd() { return m_strOcnSttCntrctPrcntgSPANEnd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setOcnSttSqncNmbr(String aOcnSttSqncNmbr)
	{ 
		if (aOcnSttSqncNmbr != null)
			this.m_strOcnSttSqncNmbr = aOcnSttSqncNmbr.trim(); 
		else
			this.m_strOcnSttSqncNmbr = aOcnSttSqncNmbr; 
	}
	public void setOcnCd(String aOcnCd)
	{ 
		if (aOcnCd != null)
			this.m_strOcnCd = aOcnCd.trim(); 
		else
			this.m_strOcnCd = aOcnCd; 
	}
	public void setSttCd(String aSttCd)
	{ 
		if (aSttCd != null)
			this.m_strSttCd = aSttCd.trim(); 
		else
			this.m_strSttCd = aSttCd; 
	}
	public void setOcnSttSlaDys(String aOcnSttSlaDys)
	{ 
		if (aOcnSttSlaDys != null)
			this.m_strOcnSttSlaDys = aOcnSttSlaDys.trim(); 
		else
			this.m_strOcnSttSlaDys = aOcnSttSlaDys; 
	}
	public void setOcnSttCntrctPrcntg(String aOcnSttCntrctPrcntg)
	{ 
		if (aOcnSttCntrctPrcntg != null)
			this.m_strOcnSttCntrctPrcntg = aOcnSttCntrctPrcntg.trim(); 
		else
			this.m_strOcnSttCntrctPrcntg = aOcnSttCntrctPrcntg; 
	}

	public int deleteOCNStateBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			String strQuery = "DELETE OCN_STATE_T WHERE OCN_STT_SQNC_NMBR = " + m_strOcnSttSqncNmbr;

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

	public int retrieveOCNStateBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			String strQuery = "SELECT OCN_CD, STT_CD, OCN_STT_SLA_DYS, OCN_STT_CNTRCT_PRCNTG, MDFD_DT, MDFD_USERID FROM OCN_STATE_T WHERE OCN_STT_SQNC_NMBR = " + m_strOcnSttSqncNmbr;

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				this.m_strOcnCd = rs.getString("OCN_CD");
				this.m_strSttCd = rs.getString("STT_CD");
				this.m_strOcnSttSlaDys = rs.getString("OCN_STT_SLA_DYS");
				this.m_strOcnSttCntrctPrcntg = rs.getString("OCN_STT_CNTRCT_PRCNTG");
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

	public int updateOCNStateBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iOcnSttSqncNmbr = Integer.parseInt(m_strOcnSttSqncNmbr);
			int iOcnSttSlaDys = Integer.parseInt(m_strOcnSttSlaDys);
			int iOcnSttCntrctPrcntg = Integer.parseInt(m_strOcnSttCntrctPrcntg);

			String strQuery = "UPDATE OCN_STATE_T SET OCN_CD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strOcnCd) + "', STT_CD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strSttCd) + "', OCN_STT_SLA_DYS = " + iOcnSttSlaDys + ", OCN_STT_CNTRCT_PRCNTG = " + iOcnSttCntrctPrcntg + ", MDFD_DT = sysdate, MDFD_USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "' WHERE OCN_STT_SQNC_NMBR = " + iOcnSttSqncNmbr;
			
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

	public int saveOCNStateBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			int iOcnSttSlaDys = Integer.parseInt(m_strOcnSttSlaDys);
			int iOcnSttCntrctPrcntg = Integer.parseInt(m_strOcnSttCntrctPrcntg);

			String strQuery = "INSERT INTO OCN_STATE_T (OCN_STT_SQNC_NMBR, OCN_CD, STT_CD, OCN_STT_SLA_DYS, OCN_STT_CNTRCT_PRCNTG, MDFD_DT, MDFD_USERID) VALUES (OCN_STATE_SEQ.nextval, '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strOcnCd) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strSttCd) + "'," + iOcnSttSlaDys + 
				"," + iOcnSttCntrctPrcntg + ",sysdate,'" + 
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

	public boolean validateOCNStateBean()
	{
		boolean rc = true;

		m_strOcnSttSlaDysSPANStart = getSPANStart();
		m_strOcnSttSlaDysSPANEnd = getSPANEnd();
		m_strOcnSttCntrctPrcntgSPANStart = getSPANStart();
		m_strOcnSttCntrctPrcntgSPANEnd = getSPANEnd();

		// Validate OCN State SLA Days
		if ((m_strOcnSttSlaDys == null) || (m_strOcnSttSlaDys.length() == 0))
		{
			m_strOcnSttSlaDysSPANStart = getErrSPANStart();
			m_strOcnSttSlaDysSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (! Validate.isDigits(m_strOcnSttSlaDys))
		{
			m_strOcnSttSlaDysSPANStart = getErrSPANStart();
			m_strOcnSttSlaDysSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate OCN State Contract Percentage
		if ((m_strOcnSttCntrctPrcntg == null) || (m_strOcnSttCntrctPrcntg.length() == 0))
		{
			m_strOcnSttCntrctPrcntgSPANStart = getErrSPANStart();
			m_strOcnSttCntrctPrcntgSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (! Validate.isDigits(m_strOcnSttCntrctPrcntg))
		{
			m_strOcnSttCntrctPrcntgSPANStart = getErrSPANStart();
			m_strOcnSttCntrctPrcntgSPANEnd = getErrSPANEnd();
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
			String strQuery = "SELECT MDFD_DT FROM OCN_STATE_T " + "WHERE OCN_STT_SQNC_NMBR = " + m_strOcnSttSqncNmbr;

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

}// end of OcnStateBean()

