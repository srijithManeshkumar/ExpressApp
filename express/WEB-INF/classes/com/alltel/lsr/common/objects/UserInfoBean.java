package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class UserInfoBean extends AlltelCtlrBean
{
	final private String m_strTableName = "USERID_T";

	private String m_strUserID = " ";

	private String m_strEmlAddrss = " ";
	private String m_strEmlAddrssSPANStart = " ";
	private String m_strEmlAddrssSPANEnd = " ";

	private String m_strRcvEmlNtfctns = " ";

	private String m_strPsswdRcvrQstn = " ";
	private String m_strPsswdRcvrQstnSPANStart = " ";
	private String m_strPsswdRcvrQstnSPANEnd = " ";

	private String m_strPsswdRcvrNswr = " ";
	private String m_strPsswdRcvrNswrSPANStart = " ";
	private String m_strPsswdRcvrNswrSPANEnd = " ";

	private String m_strPrintAllFields = " ";

	public UserInfoBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getUserID() { return m_strUserID; }

	public String getEmlAddrss() { return m_strEmlAddrss; }
	public String getEmlAddrssSPANStart() { return m_strEmlAddrssSPANStart; }
	public String getEmlAddrssSPANEnd() { return m_strEmlAddrssSPANEnd; }

	public String getRcvEmlNtfctns() { return m_strRcvEmlNtfctns; }

	public String getPsswdRcvrQstn() { return m_strPsswdRcvrQstn; }
	public String getPsswdRcvrQstnSPANStart() { return m_strPsswdRcvrQstnSPANStart; }
	public String getPsswdRcvrQstnSPANEnd() { return m_strPsswdRcvrQstnSPANEnd; }

	public String getPsswdRcvrNswr() { return m_strPsswdRcvrNswr; }
	public String getPsswdRcvrNswrSPANStart() { return m_strPsswdRcvrNswrSPANStart; }
	public String getPsswdRcvrNswrSPANEnd() { return m_strPsswdRcvrNswrSPANEnd; }

	public String getPrintInd() { return m_strPrintAllFields; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setUserID(String aUserID) 
	{ 
		if (aUserID != null)
			this.m_strUserID = aUserID.trim(); 
		else
			this.m_strUserID = aUserID; 
	}
	public void setEmlAddrss(String aEmlAddrss) 
	{ 
		if (aEmlAddrss != null)
			this.m_strEmlAddrss = aEmlAddrss.trim(); 
		else
			this.m_strEmlAddrss = aEmlAddrss; 
	}
	public void setRcvEmlNtfctns(String aRcvEmlNtfctns) 
	{ 
		if ((aRcvEmlNtfctns != null) && (aRcvEmlNtfctns.equals("yes")))
			this.m_strRcvEmlNtfctns = "Y";
		else
			this.m_strRcvEmlNtfctns = "N";
	}
	public void setPsswdRcvrQstn(String aPsswdRcvrQstn) 
	{ 
		if (aPsswdRcvrQstn != null)
			this.m_strPsswdRcvrQstn = aPsswdRcvrQstn.trim(); 
		else
			this.m_strPsswdRcvrQstn = aPsswdRcvrQstn; 
	}
	public void setPsswdRcvrNswr(String aPsswdRcvrNswr) 
	{ 
		if (aPsswdRcvrNswr != null)
			this.m_strPsswdRcvrNswr = aPsswdRcvrNswr.trim(); 
		else
			this.m_strPsswdRcvrNswr = aPsswdRcvrNswr; 
	}
	public void setPrintInd(String aPrintAllFields) 
	{ 
		if ((aPrintAllFields != null) && (aPrintAllFields.equals("yes")))
			this.m_strPrintAllFields = "Y";
		else
			this.m_strPrintAllFields = "N";
	}

	public int retrieveUserInfoBeanFromDB(String aUserID)
	{
		// Clear out beans prior contents...
		setUserID(aUserID);
		setEmlAddrss("");
		setPsswdRcvrQstn("");
		setPsswdRcvrNswr("");

		return retrieveUserInfoBeanFromDB();
	}

	public int retrieveUserInfoBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			String strQuery = "SELECT EMAIL, PSSWD_RCVR_QSTN, PSSWD_RCVR_NSWR, RCV_EMAIL_IND, " +
				"MDFD_DT, PRINT_IND FROM USERID_T WHERE USERID = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strUserID) + "'";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				this.m_strEmlAddrss = rs.getString("EMAIL");
				this.m_strRcvEmlNtfctns = rs.getString("RCV_EMAIL_IND");
				this.m_strPsswdRcvrQstn = rs.getString("PSSWD_RCVR_QSTN");
				this.m_strPsswdRcvrNswr = rs.getString("PSSWD_RCVR_NSWR");
				this.m_strMdfdDt = rs.getString("MDFD_DT");
				this.m_strPrintAllFields = rs.getString("PRINT_IND");
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

	public int updateUserInfoBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;
		String strQuery = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			strQuery = "UPDATE USERID_T SET EMAIL = '" + Toolkit.replaceSingleQwithDoubleQ(m_strEmlAddrss) + 
				"', RCV_EMAIL_IND = '" + m_strRcvEmlNtfctns + "', PSSWD_RCVR_QSTN = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strPsswdRcvrQstn) + "', PSSWD_RCVR_NSWR = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strPsswdRcvrNswr) + "', PRINT_IND = '" +
				m_strPrintAllFields + "' " + 
				"WHERE USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strUserID) + "'";

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

	public boolean validateUserInfoBean()
	{
		boolean rc = true;

		m_strEmlAddrssSPANStart = getSPANStart();
		m_strEmlAddrssSPANEnd = getSPANEnd();
		m_strPsswdRcvrQstnSPANStart = getSPANStart();
		m_strPsswdRcvrQstnSPANEnd = getSPANEnd();
		m_strPsswdRcvrNswrSPANStart = getSPANStart();
		m_strPsswdRcvrNswrSPANEnd = getSPANEnd();

		// Validate Email Addrss
		if ((m_strEmlAddrss == null) || (m_strEmlAddrss.length() == 0))
		{
			m_strEmlAddrssSPANStart = getErrSPANStart();
			m_strEmlAddrssSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strEmlAddrss)) 
		{
			m_strEmlAddrssSPANStart = getErrSPANStart();
			m_strEmlAddrssSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Question
		if ((m_strPsswdRcvrQstn == null) || (m_strPsswdRcvrQstn.length() == 0))
		{
			m_strPsswdRcvrQstnSPANStart = getErrSPANStart();
			m_strPsswdRcvrQstnSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strPsswdRcvrQstn)) 
		{
			m_strPsswdRcvrQstnSPANStart = getErrSPANStart();
			m_strPsswdRcvrQstnSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Answer
		if ((m_strPsswdRcvrNswr == null) || (m_strPsswdRcvrNswr.length() == 0))
		{
			m_strPsswdRcvrNswrSPANStart = getErrSPANStart();
			m_strPsswdRcvrNswrSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strPsswdRcvrNswr)) 
		{
			m_strPsswdRcvrNswrSPANStart = getErrSPANStart();
			m_strPsswdRcvrNswrSPANEnd = getErrSPANEnd();
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
			String strQuery = "SELECT MDFD_DT FROM USERID_T where USERID = '" + m_strUserID + "'";

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
	
}// end of UserInfoBean()

