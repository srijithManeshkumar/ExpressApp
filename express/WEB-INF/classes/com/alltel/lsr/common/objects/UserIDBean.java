/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *					BY
 *				ALLTEL INFORMATION SERVICES
 */

/* 
 * MODULE:		UserIDBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        01-31-2002
 * 
 * HISTORY:
 *	1/31/2002  initial check-in.
 *	6/3/2004 psedlak Put company type in object
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/OBJECT/UserIDBean.java  $
/*
/*   Rev 1.1   08 Mar 2002 15:00:38   dmartz
/*Remove leading and trailing spaces
/*
/*   Rev 1.0   23 Jan 2002 11:06:58   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class UserIDBean extends AlltelCtlrBean
{
	final private String m_strTableName = "USERID_T";

	private String m_strUserID = " ";
	private String m_strUserIDSPANStart = " ";
	private String m_strUserIDSPANEnd = " ";

	private String m_strFrstNm = " ";
	private String m_strFrstNmSPANStart = " ";
	private String m_strFrstNmSPANEnd = " ";

	private String m_strLstNm = " ";
	private String m_strLstNmSPANStart = " ";
	private String m_strLstNmSPANEnd = " ";
        
        private String m_strEmlId = " ";
	private String m_strEmlIdSPANStart = " ";
	private String m_strEmlIdSPANEnd = " ";

	private String m_strEncrptdPsswd = " ";
	private String m_strEncrptdPsswdSPANStart = " ";
	private String m_strEncrptdPsswdSPANEnd = " ";

	private String m_strChngPsswd = " ";

	private String m_strPsswdRcvrQstn = " ";
	private String m_strPsswdRcvrQstnSPANStart = " ";
	private String m_strPsswdRcvrQstnSPANEnd = " ";

	private String m_strPsswdRcvrNswr = " ";
	private String m_strPsswdRcvrNswrSPANStart = " ";
	private String m_strPsswdRcvrNswrSPANEnd = " ";

	private String m_strCmpnySqncNmbr = " ";
	private String m_strCmpnyTyp = " ";
	
	private String m_strFrcPsswdChg = " ";
	private String m_strDsbldUserID = " ";
	private int    m_iLgnAttmpts = 0;
	private String m_strLstLgnDt = " ";

	public UserIDBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getUserID() { return m_strUserID; }
	public String getUserIDSPANStart() { return m_strUserIDSPANStart; }
	public String getUserIDSPANEnd() { return m_strUserIDSPANEnd; }

	public String getFrstNm() { return m_strFrstNm; }
	public String getFrstNmSPANStart() { return m_strFrstNmSPANStart; }
	public String getFrstNmSPANEnd() { return m_strFrstNmSPANEnd; }

	public String getLstNm() { return m_strLstNm; }
	public String getLstNmSPANStart() { return m_strLstNmSPANStart; }
	public String getLstNmSPANEnd() { return m_strLstNmSPANEnd; }
        
        public String getEmlId() { return m_strEmlId; }
	public String getEmlIdSPANStart() { return m_strEmlIdSPANStart; }
	public String getEmlIdSPANEnd() { return m_strEmlIdSPANEnd; }

	public String getEncrptdPsswd() { return m_strEncrptdPsswd; }
	public String getEncrptdPsswdSPANStart() { return m_strEncrptdPsswdSPANStart; }
	public String getEncrptdPsswdSPANEnd() { return m_strEncrptdPsswdSPANEnd; }

	public String getChngPsswd() { return m_strChngPsswd; }

	public String getPsswdRcvrQstn() { return m_strPsswdRcvrQstn; }
	public String getPsswdRcvrQstnSPANStart() { return m_strPsswdRcvrQstnSPANStart; }
	public String getPsswdRcvrQstnSPANEnd() { return m_strPsswdRcvrQstnSPANEnd; }

	public String getPsswdRcvrNswr() { return m_strPsswdRcvrNswr; }
	public String getPsswdRcvrNswrSPANStart() { return m_strPsswdRcvrNswrSPANStart; }
	public String getPsswdRcvrNswrSPANEnd() { return m_strPsswdRcvrNswrSPANEnd; }

	public String getCmpnySqncNmbr() { return m_strCmpnySqncNmbr; }
	public String getCmpnyTyp() { return m_strCmpnyTyp; }
	
	public String getFrcPsswdChg() { return m_strFrcPsswdChg; }
	public String getDsbldUserID() { return m_strDsbldUserID; }
	public int    getLgnAttmpts() { return m_iLgnAttmpts; }
	public String getLstLgnDt() { return m_strLstLgnDt; }

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
	public void setFrstNm(String aFrstNm) 
	{ 
		if (aFrstNm != null)
			this.m_strFrstNm = aFrstNm.trim(); 
		else
			this.m_strFrstNm = aFrstNm; 
	}
	public void setLstNm(String aLstNm) 
	{ 
		if (aLstNm != null)
			this.m_strLstNm = aLstNm.trim(); 
		else
			this.m_strLstNm = aLstNm; 
	}
        public void setEmlId(String aEmlId) 
	{ 
		if (aEmlId != null)
			this.m_strEmlId = aEmlId.trim(); 
		else
			this.m_strEmlId = aEmlId; 
	}
	public void setEncrptdPsswd(String aEncrptdPsswd) 
	{ 
		if (aEncrptdPsswd != null)
			this.m_strEncrptdPsswd = aEncrptdPsswd.trim(); 
		else
			this.m_strEncrptdPsswd = aEncrptdPsswd; 
	}
	public void setChngPsswd(String aChngPsswd) 
	{ 
		if (aChngPsswd != null)
			this.m_strChngPsswd = aChngPsswd.trim(); 
		else
			this.m_strChngPsswd = aChngPsswd; 
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
	public void setCmpnySqncNmbr(String aCmpnySqncNmbr)
	{ 
		if (aCmpnySqncNmbr != null)
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr.trim(); 
		else
			this.m_strCmpnySqncNmbr = aCmpnySqncNmbr; 
	}
	public void setCmpnyTyp(String aCmpnyType)
	{ 
		if (aCmpnyType != null)
			this.m_strCmpnyTyp = aCmpnyType.trim(); 
		else
			this.m_strCmpnyTyp = "";
	}
	public void setFrcPsswdChg(String YorN) { this.m_strFrcPsswdChg = YorN; }
	public void setDsbldUserID(String YorN) { this.m_strDsbldUserID = YorN; }
	public void setLgnAttmpts(int iAttempt) { this.m_iLgnAttmpts = iAttempt; }
	public void setLstLgnDt() { this.m_strLstLgnDt = Toolkit.getDateTime(); }

	public int deleteUserIDBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			String strQuery = "DELETE USERID_T WHERE USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strUserID) 
				+ "'";

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

	public int retrieveUserIDBeanFromDB(String aUserID)
	{
		// Clear out beans prior contents...
		//Log.write(Log.DEBUG, "UserIDBean() retrieve() user = " + aUserID);
		setUserID(aUserID);
		setFrstNm("");
		setLstNm("");
                setEmlId("");
		setEncrptdPsswd("");
		setPsswdRcvrQstn("");
		setPsswdRcvrNswr("");
		setCmpnySqncNmbr("");
		setCmpnyTyp("");

		setFrcPsswdChg("N");
		setDsbldUserID("");
		setLgnAttmpts(0);
		setLstLgnDt();

		return retrieveUserIDBeanFromDB();
	}

	public int retrieveUserIDBeanFromDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			String strQuery = "SELECT FRST_NM, LST_NM, ENCRPTD_PSSWD, PSSWD_RCVR_QSTN, PSSWD_RCVR_NSWR, " +
				"U.CMPNY_SQNC_NMBR, FRC_PSSWD_CHG, DSBLD_USERID, LGN_ATTMPTS, LST_LGN_DT, EMAIL, U.MDFD_DT, U.MDFD_USERID, C.CMPNY_TYP " +
				"FROM USERID_T U, COMPANY_T C WHERE USERID = '" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strUserID) + "' AND U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);

			if (rs.next())
			{
				//Log.write(Log.DEBUG, "UserIDBean() got user ");
				this.m_strFrstNm = rs.getString("FRST_NM");
				this.m_strLstNm = rs.getString("LST_NM");
                                this.m_strEmlId = rs.getString("EMAIL");
				this.m_strEncrptdPsswd = rs.getString("ENCRPTD_PSSWD");
				this.m_strPsswdRcvrQstn = rs.getString("PSSWD_RCVR_QSTN");
				this.m_strPsswdRcvrNswr = rs.getString("PSSWD_RCVR_NSWR");
				this.m_strCmpnySqncNmbr = rs.getString("CMPNY_SQNC_NMBR");
				this.m_strCmpnyTyp = rs.getString("CMPNY_TYP");
				this.m_strFrcPsswdChg = rs.getString("FRC_PSSWD_CHG");
				this.m_strDsbldUserID = rs.getString("DSBLD_USERID");
				this.m_iLgnAttmpts = rs.getInt("LGN_ATTMPTS");
				this.m_strLstLgnDt = rs.getString("LST_LGN_DT");
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

	public int updateUserIDBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;
		String strQuery = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			if (m_strChngPsswd.equals("yes"))
			{
				strQuery = "UPDATE USERID_T SET FRST_NM = '" + Toolkit.replaceSingleQwithDoubleQ(m_strFrstNm) + 
					"', LST_NM = '" + Toolkit.replaceSingleQwithDoubleQ(m_strLstNm) + "', ENCRPTD_PSSWD = '" + 
					Toolkit.replaceSingleQwithDoubleQ(m_strEncrptdPsswd) + "', PSSWD_RCVR_QSTN = '" + 
					Toolkit.replaceSingleQwithDoubleQ(m_strPsswdRcvrQstn) + "', PSSWD_RCVR_NSWR = '" + 
                                        Toolkit.replaceSingleQwithDoubleQ(m_strPsswdRcvrNswr) + "', EMAIL = '" + 
					Toolkit.replaceSingleQwithDoubleQ(m_strEmlId) + "', CMPNY_SQNC_NMBR = " + 
					iCmpnySqncNmbr + ", MDFD_DT = sysdate, MDFD_USERID = '" + 
					Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "', " +
					"DSBLD_USERID='" + m_strDsbldUserID + "', LGN_ATTMPTS=" + m_iLgnAttmpts + 
					", FRC_PSSWD_CHG='" + m_strFrcPsswdChg + "' " +
					"WHERE USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strUserID) + "'";
			}
			else
			{
				strQuery = "UPDATE USERID_T SET FRST_NM = '" + Toolkit.replaceSingleQwithDoubleQ(m_strFrstNm) + 
					"', LST_NM = '" + Toolkit.replaceSingleQwithDoubleQ(m_strLstNm) + "', PSSWD_RCVR_QSTN = '" + 
					Toolkit.replaceSingleQwithDoubleQ(m_strPsswdRcvrQstn) + "', PSSWD_RCVR_NSWR = '" + 
					Toolkit.replaceSingleQwithDoubleQ(m_strPsswdRcvrNswr) + "', EMAIL = '" + 
					Toolkit.replaceSingleQwithDoubleQ(m_strEmlId) + "', CMPNY_SQNC_NMBR = " + 
					iCmpnySqncNmbr + ", MDFD_DT = sysdate, MDFD_USERID = '" + 
					Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "', " +
					"DSBLD_USERID='" + m_strDsbldUserID + "', LGN_ATTMPTS=" + m_iLgnAttmpts + 
					", FRC_PSSWD_CHG='" + m_strFrcPsswdChg + "' " +
					"WHERE USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strUserID) + "'";
			}

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

	public int saveUserIDBeanToDB()
	{	
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);

			String strQuery = "INSERT INTO USERID_T (USERID,FRST_NM,LST_NM,ENCRPTD_PSSWD,PSSWD_RCVR_QSTN,PSSWD_RCVR_NSWR,CMPNY_SQNC_NMBR,FRC_PSSWD_CHG,DSBLD_USERID,LGN_ATTMPTS,LST_LGN_DT,MDFD_DT,MDFD_USERID,EMAIL,RCV_EMAIL_IND,PRINT_IND) VALUES ('" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strUserID) + 
				"','" + Toolkit.replaceSingleQwithDoubleQ(m_strFrstNm) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strLstNm) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strEncrptdPsswd) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strPsswdRcvrQstn) + "','" + 
				Toolkit.replaceSingleQwithDoubleQ(m_strPsswdRcvrNswr) + "'," + iCmpnySqncNmbr + 
				", 'N', 'N', 0, sysdate, sysdate,'" +
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strEmlId) + "','N','Y')";

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

	public boolean validateUserIDBean()
	{
		boolean rc = true;

		m_strUserIDSPANStart = getSPANStart();
		m_strUserIDSPANEnd = getSPANEnd();
		m_strFrstNmSPANStart = getSPANStart();
		m_strFrstNmSPANEnd = getSPANEnd();
		m_strLstNmSPANStart = getSPANStart();
		m_strLstNmSPANEnd = getSPANEnd();
                m_strEmlIdSPANStart = getSPANStart();
		m_strEmlIdSPANEnd = getSPANEnd();
		m_strEncrptdPsswdSPANStart = getSPANStart();
		m_strEncrptdPsswdSPANEnd = getSPANEnd();
		m_strPsswdRcvrQstnSPANStart = getSPANStart();
		m_strPsswdRcvrQstnSPANEnd = getSPANEnd();
		m_strPsswdRcvrNswrSPANStart = getSPANStart();
		m_strPsswdRcvrNswrSPANEnd = getSPANEnd();

		// Validate User ID
		if ((m_strUserID == null) || (m_strUserID.length() == 0))
		{
			m_strUserIDSPANStart = getErrSPANStart();
			m_strUserIDSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (! Validate.isValidUserID(m_strUserID)) 
		{
			m_strUserIDSPANStart = getErrSPANStart();
			m_strUserIDSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate First Name
		if ((m_strFrstNm == null) || (m_strFrstNm.length() == 0))
		{
			m_strFrstNmSPANStart = getErrSPANStart();
			m_strFrstNmSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strFrstNm)) 
		{
			m_strFrstNmSPANStart = getErrSPANStart();
			m_strFrstNmSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Last Name
		if ((m_strLstNm == null) || (m_strLstNm.length() == 0))
		{
			m_strLstNmSPANStart = getErrSPANStart();
			m_strLstNmSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strLstNm)) 
		{
			m_strLstNmSPANStart = getErrSPANStart();
			m_strLstNmSPANEnd = getErrSPANEnd();
			rc = false;
		}
                
                // Validate Email
		if ((m_strEmlId == null) || (m_strEmlId.length() == 0))
		{
			m_strEmlIdSPANStart = getErrSPANStart();
			m_strEmlIdSPANEnd = getErrSPANEnd();
			rc = false;
		}

		// Validate Password
		if (m_strChngPsswd.equals("yes"))
		{
			if ((m_strEncrptdPsswd == null) || (m_strEncrptdPsswd.length() == 0))
			{
				m_strEncrptdPsswdSPANStart = getErrSPANStart();
				m_strEncrptdPsswdSPANEnd = getErrSPANEnd();
				rc = false;
			}
			else if (! Validate.isValidPassword(m_strEncrptdPsswd)) 
			{
				m_strEncrptdPsswdSPANStart = getErrSPANStart();
				m_strEncrptdPsswdSPANEnd = getErrSPANEnd();
				rc = false;
			}
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

		// Validate Question
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
			// Build SQL statement
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
	
	public int setUserLoggedIn(String strUser)
	{
		Connection con = null;
		Statement stmt = null;
		String strQuery = null;

		//for consistency
		setLgnAttmpts(0);
		setLstLgnDt();

		try {
			strQuery = "UPDATE USERID_T SET LGN_ATTMPTS=0, FRC_PSSWD_CHG='N', LST_LGN_DT=sysdate " +
				   "WHERE USERID = '" + Toolkit.replaceSingleQwithDoubleQ(strUser) + "'";
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


}// end of UserIDBean()

