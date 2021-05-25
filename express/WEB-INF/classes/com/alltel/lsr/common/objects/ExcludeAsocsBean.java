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
 * MODULE:	ExcludeAsocsBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-01-2002
 * 
 * HISTORY:
 *	06/01/2002 dmartz
 *	06/03/2002 psedlak	Chgd table name to FEATURE_INCLUSION_T.
 *
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class ExcludeAsocsBean extends AlltelCtlrBean
{
	final private String m_strTableName = "FEATURE_INCLUSION_T";

	private String m_strSqncNmbr = " ";
	private String m_strSqncNmbrSPANStart = " ";
	private String m_strSqncNmbrSPANEnd = " ";

	private String m_strExcldCtgry = " ";
	private String m_strExcldCtgrySPANStart = " ";
	private String m_strExcldCtgrySPANEnd = " ";

	private String m_strNpa = " ";
	private String m_strNpaSPANStart = " ";
	private String m_strNpaSPANEnd = " ";

	private String m_strNxx = " ";
	private String m_strNxxSPANStart = " ";
	private String m_strNxxSPANEnd = " ";

	private String m_strAsocCode = " ";
	private String m_strAsocCodeSPANStart = " ";
	private String m_strAsocCodeSPANEnd = " ";

	public ExcludeAsocsBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getSqncNmbr() { return m_strSqncNmbr; }
	public String getSqncNmbrSPANStart() { return m_strSqncNmbrSPANStart; }
	public String getSqncNmbrSPANEnd() { return m_strSqncNmbrSPANEnd; }

	public String getExcldCtgry() { return m_strExcldCtgry; }
	public String getExcldCtgrySPANStart() { return m_strExcldCtgrySPANStart; }
	public String getExcldCtgrySPANEnd() { return m_strExcldCtgrySPANEnd; }

	public String getNpa() { return m_strNpa; }
	public String getNpaSPANStart() { return m_strNpaSPANStart; }
	public String getNpaSPANEnd() { return m_strNpaSPANEnd; }

	public String getNxx() { return m_strNxx; }
	public String getNxxSPANStart() { return m_strNxxSPANStart; }
	public String getNxxSPANEnd() { return m_strNxxSPANEnd; }

	public String getAsocCode() { return m_strAsocCode; }
	public String getAsocCodeSPANStart() { return m_strAsocCodeSPANStart; }
	public String getAsocCodeSPANEnd() { return m_strAsocCodeSPANEnd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setSqncNmbr(String aSqncNmbr) 
	{ 
		if (aSqncNmbr != null)
			this.m_strSqncNmbr = aSqncNmbr.trim(); 
		else
			this.m_strSqncNmbr = aSqncNmbr; 
	}
	public void setExcldCtgry(String aExcldCtgry) 
	{ 
		if (aExcldCtgry != null)
			this.m_strExcldCtgry = aExcldCtgry.trim(); 
		else
			this.m_strExcldCtgry = aExcldCtgry; 
	}
	public void setNpa(String aNpa) 
	{ 
		if (aNpa != null)
			this.m_strNpa = aNpa.trim(); 
		else
			this.m_strNpa = aNpa; 
	}
	public void setNxx(String aNxx) 
	{ 
		if (aNxx != null)
			this.m_strNxx = aNxx.trim(); 
		else
			this.m_strNxx = aNxx; 
	}
	public void setAsocCode(String aAsocCode) 
	{ 
		if (aAsocCode != null)
			this.m_strAsocCode = aAsocCode.trim(); 
		else
			this.m_strAsocCode = aAsocCode; 
	}

	public int deleteExcludeAsocsBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			String strQuery = "DELETE FEATURE_INCLUSION_T WHERE SQNC_NMBR = " + m_strSqncNmbr;

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

	public int retrieveExcludeAsocsBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			String strQuery = "SELECT INCLD_CTGRY, NPA, NXX, ASOC_CODE, " +
				"MDFD_DT, MDFD_USERID FROM FEATURE_INCLUSION_T WHERE " + 
				"SQNC_NMBR = " + m_strSqncNmbr;

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				this.m_strExcldCtgry = rs.getString("INCLD_CTGRY");
				this.m_strNpa = rs.getString("NPA");
				this.m_strNxx = rs.getString("NXX");
				this.m_strAsocCode = rs.getString("ASOC_CODE");
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

        public int updateExcludeAsocsBeanToDB()
        {
                Connection con = null;
                Statement stmt = null;

                // Get DB Connection
                try {
                        // Build UPDATE SQL statement
                        String strQuery = "UPDATE FEATURE_INCLUSION_T SET " +
				"INCLD_CTGRY = '" + Toolkit.replaceSingleQwithDoubleQ(m_strExcldCtgry) + 
				"', NPA = '" + Toolkit.replaceSingleQwithDoubleQ(m_strNpa) + 
				"', NXX = '" + Toolkit.replaceSingleQwithDoubleQ(m_strNxx) + 
				"', ASOC_CODE = '" + Toolkit.replaceSingleQwithDoubleQ(m_strAsocCode) + 
				"', MDFD_DT = sysdate, MDFD_USERID = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + 
				"' WHERE SQNC_NMBR = " + m_strSqncNmbr;

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
                {       DatabaseManager.releaseConnection(con);
                }

                return 0;
        }


	public int saveExcludeAsocsBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			String strQuery = "INSERT INTO FEATURE_INCLUSION_T " +
				"(SQNC_NMBR, INCLD_CTGRY,NPA,NXX,ASOC_CODE,MDFD_DT,MDFD_USERID)" +
				" VALUES (FEATURE_INCLUSION_SEQ.nextval, '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strExcldCtgry) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strNpa) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strNxx) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strAsocCode) + "',sysdate,'" + 
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

	public boolean validateExcludeAsocsBean()
	{
		boolean rc = true;

		m_strExcldCtgrySPANStart = getSPANStart();
		m_strExcldCtgrySPANEnd = getSPANEnd();
		m_strNpaSPANStart = getSPANStart();
		m_strNpaSPANEnd = getSPANEnd();
		m_strNxxSPANStart = getSPANStart();
		m_strNxxSPANEnd = getSPANEnd();
		m_strAsocCodeSPANStart = getSPANStart();
		m_strAsocCodeSPANEnd = getSPANEnd();

		// Validate Excluded Category
		if ((m_strExcldCtgry == null) || (m_strExcldCtgry.length() == 0))
		{
			m_strExcldCtgrySPANStart = getErrSPANStart();
			m_strExcldCtgrySPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strExcldCtgry)) 
		{
			m_strExcldCtgrySPANStart = getErrSPANStart();
			m_strExcldCtgrySPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate NPA
		if ((m_strNpa == null) || (m_strNpa.length() == 0))
		{
			m_strNpaSPANStart = getErrSPANStart();
			m_strNpaSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (!Validate.isDigits(m_strNpa) &&
			(m_strNpa != null && !m_strNpa.equals("*"))) 
		{
			m_strNpaSPANStart = getErrSPANStart();
			m_strNpaSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate NXX
		if ((m_strNxx == null) || (m_strNxx.length() == 0))
		{
			m_strNxxSPANStart = getErrSPANStart();
			m_strNxxSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (!Validate.isDigits(m_strNxx) &&
			(m_strNxx != null && !m_strNxx.equals("*"))) 
		{
			m_strNxxSPANStart = getErrSPANStart();
			m_strNxxSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate ASOC Code
		if ((m_strAsocCode == null) || (m_strAsocCode.length() == 0))
		{
			m_strAsocCodeSPANStart = getErrSPANStart();
			m_strAsocCodeSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strAsocCode)) 
		{
			m_strAsocCodeSPANStart = getErrSPANStart();
			m_strAsocCodeSPANEnd = getErrSPANEnd();
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
			String strQuery = "SELECT MDFD_DT FROM FEATURE_INCLUSION_T WHERE " + 
				"SQNC_NMBR = " + m_strSqncNmbr;

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
	
}// end of ExcludeAsocsBean()

