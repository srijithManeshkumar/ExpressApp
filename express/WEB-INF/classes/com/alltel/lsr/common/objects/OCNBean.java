/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2002
 *                                      BY
 *                              ALLTEL COMMUNICATIONS INC.
 */
/*
 * MODULE:      OCNBean.java
 *
 * DESCRIPTION:	Hold OCN object
 *
 * AUTHOR:      Dan Martz
 *
 * DATE:        01-01-2002
 *
 * HISTORY:
 *	12/18/2002 pjs	Removed numeric edit on OCN_CD field. Industry ran out of OCN codes, so now
 *			need to use alphanumerics.
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class OCNBean extends AlltelCtlrBean
{
	final private String m_strTableName = "OCN_T";

	private String m_strOcnCd = " ";
	private String m_strOcnCdSPANStart = " ";
	private String m_strOcnCdSPANEnd = " ";

	private String m_strOcnNm = " ";
	private String m_strOcnNmSPANStart = " ";
	private String m_strOcnNmSPANEnd = " ";

	private String m_strCmpnySqncNmbr = " ";

	public OCNBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getOcnCd() { return m_strOcnCd; }
	public String getOcnCdSPANStart() { return m_strOcnCdSPANStart; }
	public String getOcnCdSPANEnd() { return m_strOcnCdSPANEnd; }

	public String getOcnNm() { return m_strOcnNm; }
	public String getOcnNmSPANStart() { return m_strOcnNmSPANStart; }
	public String getOcnNmSPANEnd() { return m_strOcnNmSPANEnd; }

	public String getCmpnySqncNmbr() { return m_strCmpnySqncNmbr; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setOcnCd(String aOcnCd)
	{ 
		if (aOcnCd != null)
			this.m_strOcnCd = aOcnCd.trim(); 
		else
			this.m_strOcnCd = aOcnCd; 
	}
	public void setOcnNm(String aOcnNm)
	{ 
		if (aOcnNm != null)
			this.m_strOcnNm = aOcnNm.trim(); 
		else
			this.m_strOcnNm = aOcnNm; 
	}
	public void setCmpnySqncNmbr(String aCmpnySqncNmbr)
	{ 
		if (aCmpnySqncNmbr != null)
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr.trim(); 
		else
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr; 
	}

	public int deleteOCNBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			//String strQuery = "DELETE OCN_T WHERE OCN_CD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strOcnCd) + "'";
                        String strQuery = "DELETE OCN_T WHERE OCN_CD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strOcnCd) + "' AND CMPNY_SQNC_NMBR = '" + Toolkit.replaceSingleQwithDoubleQ(m_strCmpnySqncNmbr) + "'";

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

	public int retrieveOCNBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			String strQuery = "SELECT OCN_NM, CMPNY_SQNC_NMBR,  MDFD_DT, MDFD_USERID " +
				"FROM OCN_T WHERE OCN_CD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strOcnCd) + "'";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				this.m_strOcnNm = rs.getString("OCN_NM");
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

	public int updateOCNBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			String strQuery = "UPDATE OCN_T SET OCN_NM = '" + Toolkit.replaceSingleQwithDoubleQ(m_strOcnNm) + 
				"', CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr + ", MDFD_DT = sysdate, MDFD_USERID = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "' WHERE OCN_CD = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strOcnCd) + "'";
			
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

	public int saveOCNBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			String strQuery = "INSERT INTO OCN_T VALUES ('" + Toolkit.replaceSingleQwithDoubleQ(m_strOcnCd) + 
				"'," + iCmpnySqncNmbr + ",'" + Toolkit.replaceSingleQwithDoubleQ(m_strOcnNm) + 
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

	public boolean validateOCNBean()
	{
		boolean rc = true;

		m_strOcnCdSPANStart = getSPANStart();
		m_strOcnCdSPANEnd = getSPANEnd();
		m_strOcnNmSPANStart = getSPANStart();
		m_strOcnNmSPANEnd = getSPANEnd();

		// Validate OCN Code
		if ((m_strOcnCd == null) || (m_strOcnCd.length() == 0))
		{
			m_strOcnCdSPANStart = getErrSPANStart();
			m_strOcnCdSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (! Validate.isDigits(m_strOcnCd))
		{
		//	m_strOcnCdSPANStart = getErrSPANStart();
		//	m_strOcnCdSPANEnd = getErrSPANEnd();
		//	rc = false;
			Log.write(Log.DEBUG_VERBOSE, "OCNBean --- accepting alpha OCN code = [" + m_strOcnCd + "]");
		}

		// Validate OCN Name
		if ((m_strOcnNm == null) || (m_strOcnNm.length() == 0))
		{
			m_strOcnNmSPANStart = getErrSPANStart();
			m_strOcnNmSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strOcnNm)) 
		{
			m_strOcnNmSPANStart = getErrSPANStart();
			m_strOcnNmSPANEnd = getErrSPANEnd();
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
			String strQuery = "SELECT MDFD_DT FROM OCN_T " + "WHERE OCN_CD = '" + m_strOcnCd + "'";

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

}// end of OcnBean()


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/OBJECT/OCNBean.java  $
/*
/*   Rev 1.1   08 Mar 2002 14:59:54   dmartz
/*Remove leading and trailing spaces
/*
/*   Rev 1.0   23 Jan 2002 11:06:06   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
