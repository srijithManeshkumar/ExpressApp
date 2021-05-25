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
 * MODULE:		RNDIBean.java
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

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/Archives/express/JAVA/Object/RNDIBean.java  $
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

public class RNDIBean extends AlltelCtlrBean {
    
    final private String m_strTableName = "RNDI_T";
    
    private String iD = " ";
    
 
    
    private String m_strName = " ";
    private String m_strNameSPANStart = " ";
    private String m_strNameSPANEnd = " ";
 
    
    public RNDIBean() {
        setSecurityTags(m_strTableName);
    }
    
    //------------------------------------------------
    // 		public Getters
    //------------------------------------------------
    public String getCmpnySqncNmbr() { return iD; }
    

    public String getCmpnyNm() { return m_strName; }
    public String getCmpnyNmSPANStart() { return m_strNameSPANStart; }
    public String getCmpnyNmSPANEnd() { return m_strNameSPANEnd; }
    
    
    
    
    //------------------------------------------------
    // 		public Setters
    //------------------------------------------------
    public void setCmpnySqncNmbr(String aCmpnySqncNmbr) {
        if (aCmpnySqncNmbr != null)
            this.iD = aCmpnySqncNmbr.trim();
        else
            this.iD = aCmpnySqncNmbr;
    }
    
    
     public void setName(String m_strName1) {
        if (m_strName1 != null)
            this.m_strName = m_strName1.trim();
        else
            this.m_strName = m_strName1;
    }
    
   
    
    public int deleteRNDIBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build DELETE SQL statement
            int iCmpnySqncNmbr = Integer.parseInt(iD);
            
            String strQuery = "DELETE RNDI_T WHERE id = " + iCmpnySqncNmbr;
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(strQuery);
        } catch(SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public int retrieveRNDIBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build SELECT SQL statement
            int iCmpnySqncNmbr = Integer.parseInt(iD);
            
            String strQuery = "SELECT * " +
                    "FROM RNDI_T where id = " + iCmpnySqncNmbr;
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                this.m_strName = rs.getString("NAME");
                  } else {
                return 1;
            }
            
        } catch(SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public int updateRNDIBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build UPDATE SQL statement
            int iCmpnySqncNmbr = Integer.parseInt(iD);
            
            String strQuery = "";//"UPDATE COMPANY_T SET CMPNY_TYP = '" + Toolkit.replaceSingleQwithDoubleQ(m_strCmpnyTyp) +
//                    "', CMPNY_NM = '" + Toolkit.replaceSingleQwithDoubleQ(m_strCmpnyNm) +
//                    "', TARGUS_USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strTargusUserid) +
//                    "', TARGUS_PSSWRD = '" + Toolkit.replaceSingleQwithDoubleQ(m_strTargusPsswrd) +
//                    "', MDFD_DT = sysdate, MDFD_USERID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) +
//                    "' WHERE CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr;
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            if (stmt.executeUpdate(strQuery) <= 0 ) {
                throw new SQLException(null,null,100);
            }
        } catch(SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public int saveRNDIBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            String strQuery = "INSERT INTO COMPANY_T (CMPNY_SQNC_NMBR, CMPNY_TYP, CMPNY_NM, TARGUS_USERID, TARGUS_PSSWRD, " +
                    " MDFD_DT, MDFD_USERID) VALUES " +
                    " (COMPANY_SEQ.nextval, '" +
//                    Toolkit.replaceSingleQwithDoubleQ(m_strCmpnyTyp) + "','" +
//                    Toolkit.replaceSingleQwithDoubleQ(m_strCmpnyNm) + "','" +
//                    Toolkit.replaceSingleQwithDoubleQ(m_strTargusUserid) + "','" +
//                    Toolkit.replaceSingleQwithDoubleQ(m_strTargusPsswrd) +
                    "',sysdate,'" + Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "')";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(strQuery);
        } catch(SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public boolean validateRNDIBean() {
        boolean rc = true;
        
//        m_strCmpnyTypSPANStart = getSPANStart();
//        m_strCmpnyTypSPANEnd = getSPANEnd();
//        m_strCmpnyNmSPANStart = getSPANStart();
//        m_strCmpnyNmSPANEnd = getSPANEnd();
        
        // Validate Company Type
//        if ((m_strCmpnyTyp == null) || (m_strCmpnyTyp.length() == 0)) {
//            m_strCmpnyTypSPANStart = getErrSPANStart();
//            m_strCmpnyTypSPANEnd = getErrSPANEnd();
//            rc = false;
//        }
        
        // Validate Company Nm
        if ((m_strName == null) || (m_strName.length() == 0)) {
            m_strNameSPANStart = getErrSPANStart();
            m_strNameSPANEnd = getErrSPANEnd();
            rc = false;
        } else if (Validate.containsSpecialChars(m_strName)) {
            m_strNameSPANStart = getErrSPANStart();
            m_strNameSPANEnd = getErrSPANEnd();
            rc = false;
        }
        
        // Validate Targus Userid
//        if ((m_strTargusUserid == null) || (m_strTargusUserid.length() == 0)) {
//            m_strTargusUseridSPANStart = getErrSPANStart();
//            m_strTargusUseridSPANEnd = getErrSPANEnd();
//            rc = false;
//        } else if (Validate.containsSpecialChars(m_strTargusUserid)) {
//            m_strTargusUseridSPANStart = getErrSPANStart();
//            m_strTargusUseridSPANEnd = getErrSPANEnd();
//            rc = false;
//        }
        
        // Validate Targus Password
//        if ((m_strTargusPsswrd == null) || (m_strTargusPsswrd.length() == 0)) {
//            m_strTargusPsswrdSPANStart = getErrSPANStart();
//            m_strTargusPsswrdSPANEnd = getErrSPANEnd();
//            rc = false;
//        } else if (Validate.containsSpecialChars(m_strTargusPsswrd)) {
//            m_strTargusPsswrdSPANStart = getErrSPANStart();
//            m_strTargusPsswrdSPANEnd = getErrSPANEnd();
//            rc = false;
//        }
        
        if (rc == false)
            m_strErrMsg = "ERROR:  Please review the data";
        
        return rc;
    }
    
    public boolean validateMdfdDt() {
        Connection con = null;
        Statement stmt = null;
        String strMdfdDt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            String strQuery = "SELECT * FROM RNDI_T WHERE id = " + iD;
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                strMdfdDt = rs.getString("name");
            } else {
                throw new SQLException(null,null,100);
            }
        } catch(SQLException sqle) {
            handleSQLError(sqle.getErrorCode());
            return false;
        } catch(Exception e) {
            return false;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        // As long as the dates are equal, all is well in the world and no one has changed the record
        if (strMdfdDt.equals(m_strMdfdDt)) {
            return true;
        } else {
            m_strErrMsg = "ERROR:  This row has been modified since you retrieved it. " +
                    "Please CANCEL and retrieve the row again.";
            return false;
        }
    }
    
}// end of RNDIBean()
