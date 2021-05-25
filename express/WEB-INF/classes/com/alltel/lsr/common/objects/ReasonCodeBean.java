package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class ReasonCodeBean extends AlltelCtlrBean
{
	final private String m_strTableName = "REASON_CODE_T";

	private String m_strRsnCdSqncNmbr = " ";

	private String m_strRsnCd = " ";
//	private String m_strRsnCdSPANStart = " ";
//	private String m_strRsnCdSPANEnd = " ";

	private String m_strRsnCdTyp = " ";

	private String m_strRsnCdDscrptn = " ";
	private String m_strRsnCdDscrptnSPANStart = " ";
	private String m_strRsnCdDscrptnSPANEnd = " ";

	private String m_strSrvcTypDscrptn = " ";
	private String m_strSrvcTypDscrptn1 = " ";
//	private String m_strSrvcTypDscrptnSPANStart = " ";
//	private String m_strSrvcTypDscrptnSPANEnd = " ";

	private String m_strActvtyTypDscrptn = " ";
	private String m_strActvtyTypDscrptn1 = " ";
//	private String m_strActvtyTypDscrptnSPANStart = " ";
//	private String m_strActvtyTypDscrptnSPANEnd = " ";

	private String m_strFrmCd = " ";
//	private String m_strFrmCdSPANStart = " ";
//	private String m_strFrmCdSPANEnd = " ";

	public ReasonCodeBean() {
		setSecurityTags(m_strTableName);
	}

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getRsnCdSqncNmbr() { return m_strRsnCdSqncNmbr; }

	public String getRsnCd() { return m_strRsnCd; }
//	public String getRsnCdSPANStart() { return m_strRsnCdSPANStart; }
//	public String getRsnCdSPANEnd() { return m_strRsnCdSPANEnd; }

	public String getRsnCdTyp() { return m_strRsnCdTyp; }

	public String getRsnCdDscrptn() { return m_strRsnCdDscrptn; }
	public String getRsnCdDscrptnSPANStart() { return m_strRsnCdDscrptnSPANStart; }
	public String getRsnCdDscrptnSPANEnd() { return m_strRsnCdDscrptnSPANEnd; }

	public String getSrvcTypDscrptn() { return m_strSrvcTypDscrptn;}
//	public String getSrvcTypDscrptnSPANStart() { return m_strSrvcTypDscrptnSPANStart; }
//	public String getSrvcTypDscrptnSPANEnd() { return m_strSrvcTypDscrptnSPANEnd; }

	public String getActvtyTypDscrptn() { return m_strActvtyTypDscrptn;}
//	public String getActvtyTypDscrptnSPANStart() { return m_strActvtyTypDscrptnSPANStart; }
//	public String getActvtyTypDscrptnSPANEnd() { return m_strActvtyTypDscrptnSPANEnd; }

	public String getFrmCd() { return m_strFrmCd;}
//	public String getFrmCdSPANStart() { return m_strFrmCdSPANStart; }
//	public String getFrmCdSPANEnd() { return m_strFrmCdSPANEnd; }

	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
	public void setRsnCdSqncNmbr(String aRsnCdSqncNmbr)
	{
		if (aRsnCdSqncNmbr != null)
			this.m_strRsnCdSqncNmbr = aRsnCdSqncNmbr.trim();
		else
			this.m_strRsnCdSqncNmbr = aRsnCdSqncNmbr;
	}
	public void setRsnCd(String aRsnCd)
	{
		if (aRsnCd != null)
			this.m_strRsnCd = aRsnCd.trim();
		else
			this.m_strRsnCd = aRsnCd;
	}
	public void setRsnCdTyp(String aRsnCdTyp)
	{
		if (aRsnCdTyp != null)
			this.m_strRsnCdTyp = aRsnCdTyp.trim();
		else
			this.m_strRsnCdTyp = aRsnCdTyp;
	}
	public void setRsnCdDscrptn(String aRsnCdDscrptn)
	{
		if (aRsnCdDscrptn != null)
			this.m_strRsnCdDscrptn = aRsnCdDscrptn.trim();
		else
			this.m_strRsnCdDscrptn = aRsnCdDscrptn;
	}
	public void setSrvcTypDscrptn(String aSrvcTypDscrptn)
	{
		if (aSrvcTypDscrptn != null)
		    this.m_strSrvcTypDscrptn = aSrvcTypDscrptn.trim();
		else
		    this.m_strSrvcTypDscrptn = aSrvcTypDscrptn;
    }
    public void setActvtyTypDscrptn(String aActvtyTypDscrptn)
		{
			if (aActvtyTypDscrptn != null)
			    this.m_strActvtyTypDscrptn = aActvtyTypDscrptn.trim();
			else
			    this.m_strActvtyTypDscrptn = aActvtyTypDscrptn;
    }
    public void setFrmCd(String aFrmCd)
		{
			if (aFrmCd != null)
			    this.m_strFrmCd = aFrmCd.trim();
			else
			    this.m_strFrmCd = aFrmCd;
    }

	public int deleteReasonCodeBeanFromDB()
	{
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build DELETE SQL statement
			int iRsnCdSqncNmbr = Integer.parseInt(m_strRsnCdSqncNmbr);

			String strQuery = "DELETE REASON_CODE_T WHERE RSN_CD_SQNC_NMBR = " + iRsnCdSqncNmbr;

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

	public int retrieveReasonCodeBeanFromDB()
	{
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build SELECT SQL statement
			int iRsnCdSqncNmbr = Integer.parseInt(m_strRsnCdSqncNmbr);

			String strQuery = "SELECT RSN_CD, RSN_CD_TYP, RSN_CD_DSCRPTN, SRVC_TYP_DSCRPTN, ACTVTY_TYP_DSCRPTN, FRM_CD, MDFD_DT, MDFD_USERID " +
				"FROM REASON_CODE_T WHERE RSN_CD_SQNC_NMBR = " + iRsnCdSqncNmbr;

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);
			int dano;
			if (rs.next())
			{
				this.m_strRsnCd = rs.getString("RSN_CD");
				this.m_strRsnCdTyp = rs.getString("RSN_CD_TYP");
				this.m_strRsnCdDscrptn = rs.getString("RSN_CD_DSCRPTN");
				this.m_strSrvcTypDscrptn = rs.getString("SRVC_TYP_DSCRPTN");
				this.m_strActvtyTypDscrptn = rs.getString("ACTVTY_TYP_DSCRPTN");
				this.m_strFrmCd = rs.getString("FRM_CD");
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

	public int updateReasonCodeBeanToDB()
	{
		Connection con = null;
		Statement stmt = null;

		// Get DB Connection
		try {
			// Build UPDATE SQL statement
			int iRsnCdSqncNmbr = Integer.parseInt(m_strRsnCdSqncNmbr);

			String strQuery = "UPDATE REASON_CODE_T SET RSN_CD = '" +
			    Toolkit.replaceSingleQwithDoubleQ(m_strRsnCd) + "', RSN_CD_TYP = '" +
			    Toolkit.replaceSingleQwithDoubleQ(m_strRsnCdTyp) + "', RSN_CD_DSCRPTN = '" +
				Toolkit.replaceSingleQwithDoubleQ(m_strRsnCdDscrptn) + "', SRVC_TYP_DSCRPTN = '" +
				Toolkit.replaceSingleQwithDoubleQ(m_strSrvcTypDscrptn) + "', ACTVTY_TYP_DSCRPTN = '" +
				Toolkit.replaceSingleQwithDoubleQ(m_strActvtyTypDscrptn) + "', FRM_CD = '" +
				Toolkit.replaceSingleQwithDoubleQ(m_strFrmCd) + "', MDFD_DT = sysdate, MDFD_USERID = '" +
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "' WHERE RSN_CD_SQNC_NMBR = " + iRsnCdSqncNmbr;

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

	public int saveReasonCodeBeanToDB()
	{
		Connection con = null;
	    Statement stmt = null;

		try {
			String strQuery1 = "SELECT SRVC_TYP_DSCRPTN, ACTVTY_TYP_DSCRPTN FROM SERVICE_TYPE_T, ACTIVITY_TYPE_T " +
			           "WHERE SERVICE_TYPE_T.TYP_IND = 'R' AND SERVICE_TYPE_T.SRVC_TYP_CD = '" + m_strSrvcTypDscrptn +
			           "' AND ACTIVITY_TYPE_T.TYP_IND = 'R' AND ACTIVITY_TYPE_T.ACTVTY_TYP_CD = '" + m_strActvtyTypDscrptn + "'";

			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery1);
			   if (rs.next())
			   {
				   this.m_strSrvcTypDscrptn1 = rs.getString("SRVC_TYP_DSCRPTN");
				   this.m_strActvtyTypDscrptn1 = rs.getString("ACTVTY_TYP_DSCRPTN");
			   }
			   else
			   {
				   m_strErrMsg = "ERROR:  This row has been modified since you retrieved it. " +
				   "Please CANCEL and retrieve the row again 2009.";
				   return 1;
			   }
			   rs.close();
			}
		 catch(SQLException sqle)
		{
				return handleSQLError(sqle.getErrorCode());
		}
		catch(Exception e)
		{
			return 1;
		}
//		finally
//		{
//			try {
//			            rs.close();
//			         }
//			         catch (Exception e1) {}
//         rs = null;
//			DatabaseManager.releaseConnection(con);
//		}

//		con = null;
//		stmt = null;

		// Get DB Connection
		try {
			// Build INSERT SQL statement
			String strQuery = "INSERT INTO REASON_CODE_T VALUES (REASON_CODE_SEQ.nextval, '" +
				Toolkit.replaceSingleQwithDoubleQ(m_strRsnCd) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strRsnCdTyp) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strRsnCdDscrptn) + "',sysdate,'" +
				Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strSrvcTypDscrptn1) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strActvtyTypDscrptn1) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strFrmCd) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strSrvcTypDscrptn) + "','" +
				Toolkit.replaceSingleQwithDoubleQ(m_strActvtyTypDscrptn) + "')";

//			con = DatabaseManager.getConnection();
//			stmt = con.createStatement();
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

	public boolean validateReasonCodeBean()
	{
		boolean rc = true;

//		m_strRsnCdSPANStart = getSPANStart();
//		m_strRsnCdSPANEnd = getSPANEnd();
		m_strRsnCdDscrptnSPANStart = getSPANStart();
		m_strRsnCdDscrptnSPANEnd = getSPANEnd();
//		m_strSrvcTypDscrptnSPANStart = getSPANStart();
//		m_strSrvcTypDscrptnSPANEnd = getSPANEnd();
//		m_strActvtyTypDscrptnSPANStart = getSPANStart();
//		m_strActvtyTypDscrptnSPANEnd = getSPANEnd();
//		m_strFrmCdSPANStart = getSPANStart();
//		m_strFrmCdSPANEnd = getSPANEnd();

		// Validate Reason Code
//		if ((m_strRsnCd == null) || (m_strRsnCd.length() == 0))
//		{
//			m_strRsnCdSPANStart = getErrSPANStart();
//			m_strRsnCdSPANEnd = getErrSPANEnd();
//			rc = false;
//		}
//		else if (Validate.containsSpecialChars(m_strRsnCd))
//		{
//			m_strRsnCdSPANStart = getErrSPANStart();
//			m_strRsnCdSPANEnd = getErrSPANEnd();
//			rc = false;
//		}

		// Validate Reason Code Description
		if ((m_strRsnCdDscrptn == null) || (m_strRsnCdDscrptn.length() == 0))
		{
			m_strRsnCdDscrptnSPANStart = getErrSPANStart();
			m_strRsnCdDscrptnSPANEnd = getErrSPANEnd();
			rc = false;
		}
		else if (Validate.containsSpecialChars(m_strRsnCdDscrptn))
		{
			m_strRsnCdDscrptnSPANStart = getErrSPANStart();
			m_strRsnCdDscrptnSPANEnd = getErrSPANEnd();
			rc = false;
		}

//		if ((m_strSrvcTypDscrptn == null) || (m_strSrvcTypDscrptn.length() == 0))
//		{
//			m_strSrvcTypDscrptnSPANStart = getErrSPANStart();
//			m_strSrvcTypDscrptnSPANEnd = getErrSPANEnd();
//			rc = false;
//		}
//		else if (Validate.containsSpecialChars(m_strSrvcTypDscrptn))
//		{
//			m_strSrvcTypDscrptnSPANStart = getErrSPANStart();
//			m_strSrvcTypDscrptnSPANEnd = getErrSPANEnd();
//			rc = false;
//		}

//		if ((m_strActvtyTypDscrptn == null) || (m_strActvtyTypDscrptn.length() == 0))
//		{
//			m_strActvtyTypDscrptnSPANStart = getErrSPANStart();
//			m_strActvtyTypDscrptnSPANEnd = getErrSPANEnd();
//			rc = false;
//		}
//		else if (Validate.containsSpecialChars(m_strActvtyTypDscrptn))
//		{
//			m_strActvtyTypDscrptnSPANStart = getErrSPANStart();
//			m_strActvtyTypDscrptnSPANEnd = getErrSPANEnd();
//			rc = false;
//		}

//		if ((m_strFrmCd == null) || (m_strFrmCd.length() == 0))
//		{
//			m_strFrmCdSPANStart = getErrSPANStart();
//			m_strFrmCdSPANEnd = getErrSPANEnd();
//			rc = false;
//		}
//		else if (Validate.containsSpecialChars(m_strFrmCd))
//		{
//			m_strFrmCdSPANStart = getErrSPANStart();
//			m_strFrmCdSPANEnd = getErrSPANEnd();
//			rc = false;
//		}

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
			int iRsnCdSqncNmbr = Integer.parseInt(m_strRsnCdSqncNmbr);
			String strQuery = "SELECT MDFD_DT FROM REASON_CODE_T WHERE RSN_CD_SQNC_NMBR = " + iRsnCdSqncNmbr;

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

}// end of ReasonCodeBean()


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/ReasonCodeBean.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:06:14   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
