/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2002
 *                                       BY
 *                              ALLTEL INFORMATION SERVICES
 */
/*
 * MODULE:	AlltelCtlrBean.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Dan Martz
 *
 * DATE:        01-01-2002
 *
 * HISTORY:
 *      xx/xx/2002  initial check-in.
 *
 */

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public abstract class AlltelCtlrBean implements Serializable {
    
    //vendor table added by kumar
    protected String strVendorConfigSqncNumber ="";
    protected String strVendorAutomationConfigSqncNumber="";
    
	protected String m_strErrMsg = " ";
	protected String m_strMdfdDt = " ";
	protected String m_strMdfdUserid = " ";
	protected String m_strDbAction = " ";

	private String m_strSPANStart = " ";
	private String m_strSPANEnd = " ";

	private String m_strErrSPANStart = "<SPAN class=errorfield>";
	private String m_strErrSPANEnd = "</SPAN>";

	//Security tags
	private String m_strTblAdmnScrtyTgView = "noaccess";
	private String m_strTblAdmnScrtyTgAdd  = "noaccess";
	private String m_strTblAdmnScrtyTgMod  = "noaccess";
	private String m_strTblAdmnScrtyTgDel  = "noaccess";

	//------------------------------------------------
	// 		public Getters
	//------------------------------------------------
	public String getErrMsg() { return m_strErrMsg; }
	public String getMdfdDt() { return m_strMdfdDt; }
	public String getMdfdUserid() { return m_strMdfdUserid; }
	public String getDbAction() { return m_strDbAction; }

	protected String getSPANStart() { return m_strSPANStart; }
	protected String getSPANEnd() { return m_strSPANEnd; }

	protected String getErrSPANStart() { return m_strErrSPANStart; }
	protected String getErrSPANEnd() { return m_strErrSPANEnd; }

	public String getTblAdmnScrtyTgView() { return m_strTblAdmnScrtyTgView; }
	public String getTblAdmnScrtyTgAdd() { return m_strTblAdmnScrtyTgAdd; }
	public String getTblAdmnScrtyTgMod() { return m_strTblAdmnScrtyTgMod; }
	public String getTblAdmnScrtyTgDel() { return m_strTblAdmnScrtyTgDel; }

    //vendor table added by kumar
    public String getStrVendorAutomationConfigSqncNumber() {
        return strVendorAutomationConfigSqncNumber;
    }
    
    //vendor table added by kumar
    public String getStrVendorConfigSqncNumber() {
        return strVendorConfigSqncNumber;
    }
	//------------------------------------------------
	// 		public Setters
	//------------------------------------------------
    
    //vendor table added by kumar
    public void setStrVendorConfigSqncNumber(String strVendorConfigSqncNumber) {
        this.strVendorConfigSqncNumber = strVendorConfigSqncNumber;
    }
    
    public void setStrVendorAutomationConfigSqncNumber(String strVendorAutomationConfigSqncNumber) {
        this.strVendorAutomationConfigSqncNumber = strVendorAutomationConfigSqncNumber;
    }
    
	public void setErrMsg(String aErrMsg) { this.m_strErrMsg = aErrMsg; }
	public void setMdfdDt(String aMdfdDt) { this.m_strMdfdDt = aMdfdDt; }
	public void setMdfdUserid(String aMdfdUserid) { this.m_strMdfdUserid = aMdfdUserid; }
	public void setDbAction(String aDbAction) { this.m_strDbAction = aDbAction; }

	public void setTblAdmnScrtyTgView(String strSecObject) {
		this.m_strTblAdmnScrtyTgView = strSecObject;
	}
	public void setTblAdmnScrtyTgAdd(String strSecObject) {
		this.m_strTblAdmnScrtyTgAdd = strSecObject;
	}
	public void setTblAdmnScrtyTgMod(String strSecObject) {
		this.m_strTblAdmnScrtyTgMod = strSecObject;
	}
	public void setTblAdmnScrtyTgDel(String strSecObject) {
		this.m_strTblAdmnScrtyTgDel = strSecObject;
	}

	public void setNoAccess() {
		this.m_strTblAdmnScrtyTgView = "noaccess";
		this.m_strTblAdmnScrtyTgAdd  = "noaccess";
		this.m_strTblAdmnScrtyTgMod  = "noaccess";
		this.m_strTblAdmnScrtyTgDel  = "noaccess";
	}

	protected int handleSQLError(int code)
	{
		switch (code)
		{
			// Database Integrity Error
			case 1:
				m_strErrMsg = "ERROR:  Duplicate Record, this record already exists.";
				break;
			case 100:
				m_strErrMsg = "ERROR:  Row not found.";
				break;
			case 2292:
				m_strErrMsg = "ERROR:  This record can not be deleted because it is requred by another row.";
				break;
			default:
				m_strErrMsg = "ERROR:  Database SQL code = " + code;
				break;
		}

		return code;
	}

	public void setSecurityTags(String strTableName)
	{
		//read dbase using strTableName
		Connection con = null;
		Statement stmt = null;
		try {
			String strQuery = "SELECT DISTINCT TBL_ADMN_SCRTY_TG_VIEW, TBL_ADMN_SCRTY_TG_ADD, TBL_ADMN_SCRTY_TG_MOD,"+
					   "TBL_ADMN_SCRTY_TG_DEL FROM TABLE_ADMIN_T WHERE TBL_ADMN_DB_NM = '" + strTableName + "'";
			con = DatabaseManager.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(strQuery);
            Log.write(" setSecurityTags before if " );
            if (rs.next()) {
                Log.write(" setSecurityTags in if " );
				setTblAdmnScrtyTgView( rs.getString("TBL_ADMN_SCRTY_TG_VIEW") );
				setTblAdmnScrtyTgAdd( rs.getString("TBL_ADMN_SCRTY_TG_ADD") );
				setTblAdmnScrtyTgMod( rs.getString("TBL_ADMN_SCRTY_TG_MOD") );
				setTblAdmnScrtyTgDel( rs.getString("TBL_ADMN_SCRTY_TG_DEL") );
			}
			else
			{	setNoAccess();
			}

		}
		catch(SQLException sqle)
		{
			setNoAccess();
		}
		catch(Exception e)
		{
			setNoAccess();
		}
		finally
		{
			DatabaseManager.releaseConnection(con);
		}
	}

	public int getColumnSize(String strTableName, String strColumnName)
	{
		int iSize = 0;
		Connection con = null;
                Statement stmt = null;
                ResultSet rs = null;
		try {
			con = DatabaseManager.getConnection();
                        stmt = con.createStatement();
                        rs = stmt.executeQuery("SELECT DATA_LENGTH FROM USER_TAB_COLUMNS WHERE TABLE_NAME ='" + strTableName + "' " +
                                               " AND COLUMN_NAME='" + strColumnName + "'");
			if (rs.next())
                        {
                                iSize = rs.getInt(1);
                        }
                }
                catch(Exception e) {
                }
		finally {
                        try {   rs.close();
                                rs=null;
                        }
                        catch (Exception e) {}
                        DatabaseManager.releaseConnection(con);
                }

		return iSize;
	}

	
}// end of AlltelCtlrBean()


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/AlltelCtlrBean.java  $
/*
/*   Rev 1.0   23 Jan 2002 11:03:32   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0  $
*/
