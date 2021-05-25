package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class UserGroupBean extends AlltelCtlrBean
{
	final private String m_strTableName = "USER_GROUP_T";
	
	private String m_strUsrGrpCd = " ";
	private String m_strUsrGrpCdSPANStart = " ";
	private String m_strUsrGrpCdSPANEnd = " ";

	private String m_strUsrGrpDscrptn = " ";
	private String m_strUsrGrpDscrptnSPANStart = " ";
	private String m_strUsrGrpDscrptnSPANEnd = " ";

	private String m_strCmpnySqncNmbr = " ";

	private String m_strOcnCd = " ";
	private String m_strOcnCdSPANStart = " ";
	private String m_strOcnCdSPANEnd = " ";

	public UserGroupBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getUsrGrpCd() { return m_strUsrGrpCd; }
	public String getUsrGrpCdSPANStart() { return m_strUsrGrpCdSPANStart; }
	public String getUsrGrpCdSPANEnd() { return m_strUsrGrpCdSPANEnd; }

	public String getUsrGrpDscrptn() { return m_strUsrGrpDscrptn; }
	public String getUsrGrpDscrptnSPANStart() { return m_strUsrGrpDscrptnSPANStart; }
	public String getUsrGrpDscrptnSPANEnd() { return m_strUsrGrpDscrptnSPANEnd; }

	public String getCmpnySqncNmbr() { return m_strCmpnySqncNmbr; }

	public String getOcnCd() { return m_strOcnCd; }
	public String getOcnCdSPANStart() { return m_strOcnCdSPANStart; }
	public String getOcnCdSPANEnd() { return m_strOcnCdSPANEnd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setUsrGrpCd(String aUsrGrpCd) 
	{ 
		if (aUsrGrpCd != null)
			this.m_strUsrGrpCd = aUsrGrpCd.trim(); 
		else
			this.m_strUsrGrpCd = aUsrGrpCd;
	}
	public void setUsrGrpDscrptn(String aUsrGrpDscrptn) 
	{ 
		if (aUsrGrpDscrptn != null)
			this.m_strUsrGrpDscrptn = aUsrGrpDscrptn.trim(); 
		else
			this.m_strUsrGrpDscrptn = aUsrGrpDscrptn;
	}
	public void setCmpnySqncNmbr(String aCmpnySqncNmbr) 
	{ 
		if (aCmpnySqncNmbr != null)
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr.trim(); 
		else
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr;
	}
	public void setOcnCd(String aOcnCd) 
	{ 
		if (aOcnCd != null)
			this.m_strOcnCd = aOcnCd.trim(); 
		else
			this.m_strOcnCd = aOcnCd;
	}

	public int deleteUserGroupBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			String strQuery = "DELETE USER_GROUP_T WHERE USR_GRP_CD = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strUsrGrpCd) + "'";

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

	public int retrieveUserGroupBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			String strQuery = "SELECT USR_GRP_DSCRPTN, CMPNY_SQNC_NMBR, OCN_CD, MDFD_DT, MDFD_USERID " +
				"FROM USER_GROUP_T WHERE USR_GRP_CD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strUsrGrpCd) + "'";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				this.m_strUsrGrpDscrptn = rs.getString("USR_GRP_DSCRPTN");
				this.m_strCmpnySqncNmbr = rs.getString("CMPNY_SQNC_NMBR");
				this.m_strOcnCd = rs.getString("OCN_CD");
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

	public int updateUserGroupBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			String strQuery = "UPDATE USER_GROUP_T SET USR_GRP_DSCRPTN = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strUsrGrpDscrptn) + "', CMPNY_SQNC_NMBR = " + 
				iCmpnySqncNmbr + ", OCN_CD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strOcnCd) + 
				"', MDFD_DT = sysdate, MDFD_USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + 
				"' WHERE USR_GRP_CD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strUsrGrpCd) + "'";
			
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

	public int saveUserGroupBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			String strQuery = "INSERT INTO USER_GROUP_T VALUES ('" + Toolkit.replaceSingleQwithDoubleQ(m_strUsrGrpCd) + 
				"','" + Toolkit.replaceSingleQwithDoubleQ(m_strUsrGrpDscrptn) + "'," + iCmpnySqncNmbr + 
				",'" + Toolkit.replaceSingleQwithDoubleQ(m_strOcnCd) + "', sysdate,'" + 
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

	public boolean validateUserGroupBean()
	{
		boolean rc = true;

		m_strUsrGrpCdSPANStart = getSPANStart();
		m_strUsrGrpCdSPANEnd = getSPANEnd();
		m_strUsrGrpDscrptnSPANStart = getSPANStart();
		m_strUsrGrpDscrptnSPANEnd = getSPANEnd();

		// Validate User Group Code
		if ((m_strUsrGrpCd == null) || (m_strUsrGrpCd.length() == 0))
		{
			m_strUsrGrpCdSPANStart = getErrSPANStart();
			m_strUsrGrpCdSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strUsrGrpCd)) 
		{
			m_strUsrGrpCdSPANStart = getErrSPANStart();
			m_strUsrGrpCdSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate User Group Code Description
		if ((m_strUsrGrpDscrptn == null) || (m_strUsrGrpDscrptn.length() == 0))
		{
			m_strUsrGrpDscrptnSPANStart = getErrSPANStart();
			m_strUsrGrpDscrptnSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strUsrGrpDscrptn)) 
		{
			m_strUsrGrpDscrptnSPANStart = getErrSPANStart();
			m_strUsrGrpDscrptnSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Company/OCN Combination
		if (! Validate.isValidCmpnyOCN(Integer.parseInt(m_strCmpnySqncNmbr), m_strOcnCd)) 
		{
			m_strOcnCdSPANStart = getErrSPANStart();
			m_strOcnCdSPANEnd = getErrSPANEnd();
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
			String strQuery = "SELECT MDFD_DT FROM USER_GROUP_T " + "WHERE USR_GRP_CD = '" + m_strUsrGrpCd + "'";

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

}// end of UserGroupBean()
