package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class SecurityGroupAssignmentBean extends AlltelCtlrBean
{
	final private String m_strTableName = "SECURITY_GROUP_ASSIGNMENT_T";

	private String m_strScrtyGrpAssgnmntSqncNmbr = " ";
	private String m_strScrtyGrpCd = " ";
	private String m_strScrtyObjctCd = " ";

	public SecurityGroupAssignmentBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getScrtyGrpAssgnmntSqncNmbr() { return m_strScrtyGrpAssgnmntSqncNmbr; }
	public String getScrtyGrpCd() { return m_strScrtyGrpCd; }
	public String getScrtyObjctCd() { return m_strScrtyObjctCd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setScrtyGrpAssgnmntSqncNmbr(String aScrtyGrpAssgnmntSqncNmbr) 
	{ 
		if (aScrtyGrpAssgnmntSqncNmbr != null)
			this.m_strScrtyGrpAssgnmntSqncNmbr = aScrtyGrpAssgnmntSqncNmbr.trim(); 
		else
			this.m_strScrtyGrpAssgnmntSqncNmbr = aScrtyGrpAssgnmntSqncNmbr; 
	}
	public void setScrtyGrpCd(String aScrtyGrpCd) 
	{ 
		if (aScrtyGrpCd != null)
			this.m_strScrtyGrpCd = aScrtyGrpCd.trim(); 
		else
			this.m_strScrtyGrpCd = aScrtyGrpCd; 
	}
	public void setScrtyObjctCd(String aScrtyObjctCd) 
	{ 
		if (aScrtyObjctCd != null)
			this.m_strScrtyObjctCd = aScrtyObjctCd.trim(); 
		else
			this.m_strScrtyObjctCd = aScrtyObjctCd; 
	}

	public int deleteSecurityGroupAssignmentBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			int iScrtyGrpAssgnmntSqncNmbr = Integer.parseInt(m_strScrtyGrpAssgnmntSqncNmbr);

			String strQuery = "DELETE SECURITY_GROUP_ASSIGNMENT_T WHERE SCRTY_GRP_ASSGNMNT_SQNC_NMBR = " + 
				iScrtyGrpAssgnmntSqncNmbr;

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

	public int retrieveSecurityGroupAssignmentBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			int iScrtyGrpAssgnmntSqncNmbr = Integer.parseInt(m_strScrtyGrpAssgnmntSqncNmbr);

			String strQuery = "SELECT SCRTY_GRP_CD, SCRTY_OBJCT_CD, MDFD_DT, MDFD_USERID " +
				"FROM SECURITY_GROUP_ASSIGNMENT_T WHERE SCRTY_GRP_ASSGNMNT_SQNC_NMBR = " + iScrtyGrpAssgnmntSqncNmbr;

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				this.m_strScrtyGrpCd = rs.getString("SCRTY_GRP_CD");
				this.m_strScrtyObjctCd = rs.getString("SCRTY_OBJCT_CD");
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

	public int updateSecurityGroupAssignmentBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iScrtyGrpAssgnmntSqncNmbr = Integer.parseInt(m_strScrtyGrpAssgnmntSqncNmbr);

			String strQuery = "UPDATE SECURITY_GROUP_ASSIGNMENT_T SET SCRTY_GRP_CD = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpCd) + "', SCRTY_OBJCT_CD = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strScrtyObjctCd) + "', MDFD_DT = sysdate, MDFD_USERID = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "' WHERE SCRTY_GRP_ASSGNMNT_SQNC_NMBR = " + 
				iScrtyGrpAssgnmntSqncNmbr;
			
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

	public int saveSecurityGroupAssignmentBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			String strQuery = "INSERT INTO SECURITY_GROUP_ASSIGNMENT_T VALUES (SEC_GRP_ASSIGN_SEQ.nextval, '" +
				Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpCd) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strScrtyObjctCd) + "', sysdate,'" + 
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

	public boolean validateSecurityGroupAssignmentBean()
	{
		return true;
	}

	public boolean validateMdfdDt()
	{	
		Connection con = null;
		Statement stmt = null;
		String strMdfdDt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			int iScrtyGrpAssgnmntSqncNmbr = Integer.parseInt(m_strScrtyGrpAssgnmntSqncNmbr);
			String strQuery = "SELECT MDFD_DT FROM SECURITY_GROUP_ASSIGNMENT_T " +
				"WHERE SCRTY_GRP_ASSGNMNT_SQNC_NMBR = " + iScrtyGrpAssgnmntSqncNmbr;

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

}// end of SecurityGroupAssignmentBean()


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SecurityGroupAssignmentBean.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:06:38   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
