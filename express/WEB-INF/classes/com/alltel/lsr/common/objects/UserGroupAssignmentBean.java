package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class UserGroupAssignmentBean extends AlltelCtlrBean
{
	final private String m_strTableName = "USER_GROUP_ASSIGNMENT_T";

	private String m_strUsrGrpAssgnmntSqncNmbr = " ";
	private String m_strUserID = " ";
	private String m_strUsrGrpCd = " ";
	private String m_strScrtyGrpCd = " ";

	public UserGroupAssignmentBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getUsrGrpAssgnmntSqncNmbr() { return m_strUsrGrpAssgnmntSqncNmbr; }
	public String getUserID() { return m_strUserID; }
	public String getUsrGrpCd() { return m_strUsrGrpCd; }
	public String getScrtyGrpCd() { return m_strScrtyGrpCd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setUsrGrpAssgnmntSqncNmbr(String aUsrGrpAssgnmntSqncNmbr) 
	{ 
		if (aUsrGrpAssgnmntSqncNmbr != null)
			this.m_strUsrGrpAssgnmntSqncNmbr = aUsrGrpAssgnmntSqncNmbr.trim(); 
		else
			this.m_strUsrGrpAssgnmntSqncNmbr = aUsrGrpAssgnmntSqncNmbr; 
	}
	public void setUserID(String aUserID) 
	{ 
		if (aUserID != null)
			this.m_strUserID = aUserID.trim(); 
		else
			this.m_strUserID = aUserID; 
	}
	public void setUsrGrpCd(String aUsrGrpCd) 
	{ 
		if (aUsrGrpCd != null)
			this.m_strUsrGrpCd = aUsrGrpCd.trim(); 
		else
			this.m_strUsrGrpCd = aUsrGrpCd; 
	}
	public void setScrtyGrpCd(String aScrtyGrpCd) 
	{ 
		if (aScrtyGrpCd != null)
			this.m_strScrtyGrpCd = aScrtyGrpCd.trim(); 
		else
			this.m_strScrtyGrpCd = aScrtyGrpCd; 
	}

	public int deleteUserGroupAssignmentBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(m_strUsrGrpAssgnmntSqncNmbr);
			
			String strQuery = "DELETE USER_GROUP_ASSIGNMENT_T WHERE USR_GRP_ASSGNMNT_SQNC_NMBR = " + iUsrGrpAssgnmntSqncNmbr;

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

	public int retrieveUserGroupAssignmentBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(m_strUsrGrpAssgnmntSqncNmbr);
			
			String strQuery = "SELECT USERID, USR_GRP_CD, SCRTY_GRP_CD, MDFD_DT, MDFD_USERID " +
				"FROM USER_GROUP_ASSIGNMENT_T WHERE USR_GRP_ASSGNMNT_SQNC_NMBR = " + iUsrGrpAssgnmntSqncNmbr;

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				this.m_strUserID = rs.getString("USERID");
				this.m_strUsrGrpCd = rs.getString("USR_GRP_CD");
				this.m_strScrtyGrpCd = rs.getString("SCRTY_GRP_CD");
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

	public int updateUserGroupAssignmentBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(m_strUsrGrpAssgnmntSqncNmbr);

			String strQuery = "UPDATE USER_GROUP_ASSIGNMENT_T SET USR_GRP_CD = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strUsrGrpCd) + "', SCRTY_GRP_CD = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpCd) + "', MDFD_DT = sysdate, MDFD_USERID = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + 
				"' WHERE USR_GRP_ASSGNMNT_SQNC_NMBR = " + iUsrGrpAssgnmntSqncNmbr;
			
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

	public int saveUserGroupAssignmentBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			String strQuery = "INSERT INTO USER_GROUP_ASSIGNMENT_T VALUES (USR_GRP_ASSIGN_SEQ.nextval, '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strUserID) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strUsrGrpCd) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strScrtyGrpCd) + "',sysdate,'" + 
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

	public boolean validateUserGroupAssignmentBean()
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
			int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(m_strUsrGrpAssgnmntSqncNmbr);
			String strQuery = "SELECT MDFD_DT FROM USER_GROUP_ASSIGNMENT_T " +
				"WHERE USR_GRP_ASSGNMNT_SQNC_NMBR = " + iUsrGrpAssgnmntSqncNmbr;

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

}// end of UserGroupAssignmentBean()


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/UserGroupAssignmentBean.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:06:52   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
