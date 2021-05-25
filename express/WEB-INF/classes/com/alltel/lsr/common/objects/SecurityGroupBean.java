package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class SecurityGroupBean extends AlltelCtlrBean
{
	final private String m_strTableName = "SECURITY_GROUP_T";
	
	private String m_strScrtyGrpCd = " ";
	private String m_strScrtyGrpCdSPANStart = " ";
	private String m_strScrtyGrpCdSPANEnd = " ";

	private String m_strScrtyGrpDscrptn = " ";
	private String m_strScrtyGrpDscrptnSPANStart = " ";
	private String m_strScrtyGrpDscrptnSPANEnd = " ";

	public SecurityGroupBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getScrtyGrpCd() { return m_strScrtyGrpCd; }
	public String getScrtyGrpCdSPANStart() { return m_strScrtyGrpCdSPANStart; }
	public String getScrtyGrpCdSPANEnd() { return m_strScrtyGrpCdSPANEnd; }

	public String getScrtyGrpDscrptn() { return m_strScrtyGrpDscrptn; }
	public String getScrtyGrpDscrptnSPANStart() { return m_strScrtyGrpDscrptnSPANStart; }
	public String getScrtyGrpDscrptnSPANEnd() { return m_strScrtyGrpDscrptnSPANEnd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setScrtyGrpCd(String aScrtyGrpCd) 
	{ 
		if (aScrtyGrpCd != null)
			this.m_strScrtyGrpCd = aScrtyGrpCd.trim(); 
		else
			this.m_strScrtyGrpCd = aScrtyGrpCd; 
	}
	public void setScrtyGrpDscrptn(String aScrtyGrpDscrptn) 
	{ 
		if (aScrtyGrpDscrptn != null)
			this.m_strScrtyGrpDscrptn = aScrtyGrpDscrptn.trim(); 
		else
			this.m_strScrtyGrpDscrptn = aScrtyGrpDscrptn; 
	}

	public int deleteSecurityGroupBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			String strQuery = "DELETE SECURITY_GROUP_T WHERE SCRTY_GRP_CD = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpCd) + "'";

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

	public int retrieveSecurityGroupBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			String strQuery = "SELECT SCRTY_GRP_DSCRPTN, MDFD_DT, MDFD_USERID FROM SECURITY_GROUP_T " +
				"WHERE SCRTY_GRP_CD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpCd) + "'";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				this.m_strScrtyGrpDscrptn = rs.getString("SCRTY_GRP_DSCRPTN");
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

	public int updateSecurityGroupBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			String strQuery = "UPDATE SECURITY_GROUP_T SET SCRTY_GRP_DSCRPTN = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpDscrptn) + "', MDFD_DT = sysdate, MDFD_USERID = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "' WHERE SCRTY_GRP_CD = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpCd) + "'";
			
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

	public int saveSecurityGroupBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			String strQuery = "INSERT INTO SECURITY_GROUP_T VALUES ('" + Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpCd) + 
				"','" + Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpDscrptn) + "', sysdate,'" + 
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

	public boolean validateSecurityGroupBean()
	{
		boolean rc = true;

		m_strScrtyGrpCdSPANStart = getSPANStart();
		m_strScrtyGrpCdSPANEnd = getSPANEnd();
		m_strScrtyGrpDscrptnSPANStart = getSPANStart();
		m_strScrtyGrpDscrptnSPANEnd = getSPANEnd();

		// Validate Security Group Code
		if ((m_strScrtyGrpCd == null) || (m_strScrtyGrpCd.length() == 0))
		{
			m_strScrtyGrpCdSPANStart = getErrSPANStart();
			m_strScrtyGrpCdSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strScrtyGrpCd)) 
		{
			m_strScrtyGrpCdSPANStart = getErrSPANStart();
			m_strScrtyGrpCdSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Security Group Code Description
		if ((m_strScrtyGrpDscrptn == null) || (m_strScrtyGrpDscrptn.length() == 0))
		{
			m_strScrtyGrpDscrptnSPANStart = getErrSPANStart();
			m_strScrtyGrpDscrptnSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strScrtyGrpDscrptn)) 
		{
			m_strScrtyGrpDscrptnSPANStart = getErrSPANStart();
			m_strScrtyGrpDscrptnSPANEnd = getErrSPANEnd();
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
			String strQuery = "SELECT MDFD_DT FROM SECURITY_GROUP_T " + "WHERE SCRTY_GRP_CD = '" + m_strScrtyGrpCd + "'";

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

}// end of SecurityGroupBean()


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SecurityGroupBean.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:06:40   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
