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
 * MODULE:	BanBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-01-2002
 * 
 * HISTORY:
 *
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class BanBean extends AlltelCtlrBean
{
	final private String m_strTableName = "BAN_T";

	private String m_strBan = " ";
	private String m_strBanSPANStart = " ";
	private String m_strBanSPANEnd = " ";

	private String m_strBanDscrptn = " ";
	private String m_strBanDscrptnSPANStart = " ";
	private String m_strBanDscrptnSPANEnd = " ";

	private String m_strCmpnySqncNmbr = " ";

	public BanBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getBan() { return m_strBan; }
	public String getBanSPANStart() { return m_strBanSPANStart; }
	public String getBanSPANEnd() { return m_strBanSPANEnd; }

	public String getBanDscrptn() { return m_strBanDscrptn; }
	public String getBanDscrptnSPANStart() { return m_strBanDscrptnSPANStart; }
	public String getBanDscrptnSPANEnd() { return m_strBanDscrptnSPANEnd; }

	public String getCmpnySqncNmbr() { return m_strCmpnySqncNmbr; }
	
	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setBan(String aBan) 
	{ 
		if (aBan != null)
			this.m_strBan = aBan.trim(); 
		else
			this.m_strBan = aBan; 
	}
	public void setBanDscrptn(String aBanDscrptn) 
	{ 
		if (aBanDscrptn != null)
			this.m_strBanDscrptn = aBanDscrptn.trim(); 
		else
			this.m_strBanDscrptn = aBanDscrptn; 
	}
	public void setCmpnySqncNmbr(String aCmpnySqncNmbr)
	{ 
		if (aCmpnySqncNmbr != null)
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr.trim(); 
		else
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr; 
	}

	public int deleteBanBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			String strQuery = "DELETE BAN_T WHERE BAN = '" + Toolkit.replaceSingleQwithDoubleQ(m_strBan) + "'";

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

	public int retrieveBanBeanFromDB(String aBan)
	{
		// Clear out beans prior contents...
		//Log.write(Log.DEBUG, "BanBean() retrieve() user = " + aBan);
		setBan(aBan);
		setBanDscrptn("");
		setCmpnySqncNmbr("");

		return retrieveBanBeanFromDB();
	}

	public int retrieveBanBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			String strQuery = "SELECT BAN_DSCRPTN, CMPNY_SQNC_NMBR, " +
				"MDFD_DT, MDFD_USERID " +
				"FROM BAN_T WHERE BAN = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strBan) + "'";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				//Log.write(Log.DEBUG, "BanBean() got user ");
				this.m_strBanDscrptn = rs.getString("BAN_DSCRPTN");
				this.m_strCmpnySqncNmbr = rs.getString("CMPNY_SQNC_NMBR");
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

	public int updateBanBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;
		String strQuery = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			strQuery = "UPDATE BAN_T SET BAN_DSCRPTN = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strBanDscrptn) + 
				"', CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr + 
				", MDFD_DT = sysdate, MDFD_USERID = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "' " +
				"WHERE BAN = '" + Toolkit.replaceSingleQwithDoubleQ(m_strBan) + "'";

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

	public int saveBanBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			String strQuery = "INSERT INTO BAN_T (BAN,BAN_DSCRPTN,CMPNY_SQNC_NMBR,MDFD_DT,MDFD_USERID) VALUES ('" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strBan) + 
				"','" + Toolkit.replaceSingleQwithDoubleQ(m_strBanDscrptn) + "'," + 
				+ iCmpnySqncNmbr + ", sysdate,'" +
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

	public boolean validateBanBean()
	{
		boolean rc = true;

		m_strBanSPANStart = getSPANStart();
		m_strBanSPANEnd = getSPANEnd();
		m_strBanDscrptnSPANStart = getSPANStart();
		m_strBanDscrptnSPANEnd = getSPANEnd();

		// Validate BAN
		if ((m_strBan == null) || (m_strBan.length() == 0))
		{
			m_strBanSPANStart = getErrSPANStart();
			m_strBanSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (! Validate.isValidBan(m_strBan)) 
		{
			m_strBanSPANStart = getErrSPANStart();
			m_strBanSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Description
		if ((m_strBanDscrptn == null) || (m_strBanDscrptn.length() == 0))
		{
			m_strBanDscrptnSPANStart = getErrSPANStart();
			m_strBanDscrptnSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strBanDscrptn)) 
		{
			m_strBanDscrptnSPANStart = getErrSPANStart();
			m_strBanDscrptnSPANEnd = getErrSPANEnd();
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
			// Build SQL statement
			String strQuery = "SELECT MDFD_DT FROM BAN_T where BAN = '" + m_strBan + "'";

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
	
}// end of BanBean()

