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
 * MODULE:		CompanyBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        01-31-2002
 * 
 * HISTORY:
 *	1/31/2002  dmartz	initial check-in.
 *	5/29/2002  psedlak	Added targus cols to Company_t
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/Archives/express/JAVA/Object/CompanyBean.java  $
/*
/*   Rev 1.3   May 30 2002 11:45:12   sedlak
/* 
/* 
/*   Rev 1.0   23 Jan 2002 11:06:20   wwoods
/*Initial Checkin
*/

/* $Revision:   1.3  $
*/

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class CompanyBean extends AlltelCtlrBean
{
	final private String m_strTableName = "COMPANY_T";

	private String m_strCmpnySqncNmbr = " ";

	private String m_strCmpnyTyp = " ";
	private String m_strCmpnyTypSPANStart = " ";
	private String m_strCmpnyTypSPANEnd = " ";

	private String m_strCmpnyNm = " ";
	private String m_strCmpnyNmSPANStart = " ";
	private String m_strCmpnyNmSPANEnd = " ";

	private String m_strTargusUserid = " ";
	private String m_strTargusUseridSPANStart = " ";
	private String m_strTargusUseridSPANEnd = " ";

	private String m_strTargusPsswrd = " ";
	private String m_strTargusPsswrdSPANStart = " ";
	private String m_strTargusPsswrdSPANEnd = " ";

	public CompanyBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getCmpnySqncNmbr() { return m_strCmpnySqncNmbr; }

	public String getCmpnyTyp() { return m_strCmpnyTyp; }
	public String getCmpnyTypSPANStart() { return m_strCmpnyTypSPANStart; }
	public String getCmpnyTypSPANEnd() { return m_strCmpnyTypSPANEnd; }

	public String getCmpnyNm() { return m_strCmpnyNm; }
	public String getCmpnyNmSPANStart() { return m_strCmpnyNmSPANStart; }
	public String getCmpnyNmSPANEnd() { return m_strCmpnyNmSPANEnd; }

	public String getTargusUserid() { return m_strTargusUserid; }
	public String getTargusUseridSPANStart() { return m_strTargusUseridSPANStart; }
	public String getTargusUseridSPANEnd() { return m_strTargusUseridSPANEnd; }

	public String getTargusPsswrd() { return m_strTargusPsswrd; }
	public String getTargusPsswrdSPANStart() { return m_strTargusPsswrdSPANStart; }
	public String getTargusPsswrdSPANEnd() { return m_strTargusPsswrdSPANEnd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setCmpnySqncNmbr(String aCmpnySqncNmbr) 
	{ 
		if (aCmpnySqncNmbr != null)
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr.trim(); 
		else
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr; 
	}
	public void setCmpnyTyp(String aCmpnyTyp)
	{ 
		if (aCmpnyTyp != null)
			this.m_strCmpnyTyp = aCmpnyTyp.trim(); 
		else
			this.m_strCmpnyTyp = aCmpnyTyp; 
	}
	public void setCmpnyNm(String aCmpnyNm) 
	{ 
		if (aCmpnyNm != null)
			this.m_strCmpnyNm = aCmpnyNm.trim(); 
		else
			this.m_strCmpnyNm = aCmpnyNm; 
	}

	public void setTargusUserid(String aValue) 
	{ 
		if (aValue != null)
			this.m_strTargusUserid = aValue.trim(); 
		else
			this.m_strTargusUserid = aValue; 
	}

	public void setTargusPsswrd(String aValue) 
	{ 
		if (aValue != null)
			this.m_strTargusPsswrd = aValue.trim(); 
		else
			this.m_strTargusPsswrd = aValue; 
	}

	public int deleteCompanyBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			String strQuery = "DELETE COMPANY_T WHERE CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr;

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

	public int retrieveCompanyBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			String strQuery = "SELECT CMPNY_TYP, CMPNY_NM, TARGUS_USERID, TARGUS_PSSWRD, MDFD_DT, MDFD_USERID " +
				"FROM COMPANY_T where CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr;

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);
			if (rs.next())
			{
				this.m_strCmpnyTyp = rs.getString("CMPNY_TYP"); 
				this.m_strCmpnyNm = rs.getString("CMPNY_NM");
				this.m_strTargusUserid = rs.getString("TARGUS_USERID");
				this.m_strTargusPsswrd = rs.getString("TARGUS_PSSWRD");
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

	public int updateCompanyBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			String strQuery = "UPDATE COMPANY_T SET CMPNY_TYP = '" + Toolkit.replaceSingleQwithDoubleQ(m_strCmpnyTyp) + 
				"', CMPNY_NM = '" + Toolkit.replaceSingleQwithDoubleQ(m_strCmpnyNm) + 
				"', TARGUS_USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strTargusUserid) + 
				"', TARGUS_PSSWRD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strTargusPsswrd) + 
				"', MDFD_DT = sysdate, MDFD_USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + 
				"' WHERE CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr;
			
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			if (stmt.executeUpdate(strQuery) <= 0 )
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

	public int saveCompanyBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			String strQuery = "INSERT INTO COMPANY_T (CMPNY_SQNC_NMBR, CMPNY_TYP, CMPNY_NM, TARGUS_USERID, TARGUS_PSSWRD, " +
				" MDFD_DT, MDFD_USERID) VALUES " +
				" (COMPANY_SEQ.nextval, '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strCmpnyTyp) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strCmpnyNm) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strTargusUserid) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strTargusPsswrd) + 
				 "',sysdate,'" + Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "')";

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

	public boolean validateCompanyBean()
	{
		boolean rc = true;

		m_strCmpnyTypSPANStart = getSPANStart();
		m_strCmpnyTypSPANEnd = getSPANEnd();
		m_strCmpnyNmSPANStart = getSPANStart();
		m_strCmpnyNmSPANEnd = getSPANEnd();

		// Validate Company Type
		if ((m_strCmpnyTyp == null) || (m_strCmpnyTyp.length() == 0))
		{
			m_strCmpnyTypSPANStart = getErrSPANStart();
			m_strCmpnyTypSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Company Nm
		if ((m_strCmpnyNm == null) || (m_strCmpnyNm.length() == 0))
		{
			m_strCmpnyNmSPANStart = getErrSPANStart();
			m_strCmpnyNmSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strCmpnyNm)) 
		{
			m_strCmpnyNmSPANStart = getErrSPANStart();
			m_strCmpnyNmSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Targus Userid
		if ((m_strTargusUserid == null) || (m_strTargusUserid.length() == 0))
		{
			m_strTargusUseridSPANStart = getErrSPANStart();
			m_strTargusUseridSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strTargusUserid)) 
		{
			m_strTargusUseridSPANStart = getErrSPANStart();
			m_strTargusUseridSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Targus Password
		if ((m_strTargusPsswrd == null) || (m_strTargusPsswrd.length() == 0))
		{
			m_strTargusPsswrdSPANStart = getErrSPANStart();
			m_strTargusPsswrdSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strTargusPsswrd)) 
		{
			m_strTargusPsswrdSPANStart = getErrSPANStart();
			m_strTargusPsswrdSPANEnd = getErrSPANEnd();
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
			String strQuery = "SELECT MDFD_DT FROM COMPANY_T WHERE CMPNY_SQNC_NMBR = " + m_strCmpnySqncNmbr;

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

}// end of CompanyBean()
