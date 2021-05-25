package com.alltel.lsr.common.objects;

import com.alltel.lsr.common.objects.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class PortableAreaBean extends AlltelCtlrBean {
    
    final private String m_strTableName = "PORTABLE_AREA_T";
    
    private String strPortableAreaSqncNumber = " ";
    private String strPortableAreaNameSqncNo = " ";
//    private String m_strUsrGrpCd = " ";
//    private String m_strScrtyGrpCd = " ";
//
    public PortableAreaBean() {
        setSecurityTags(m_strTableName);
    }
    
    
    //------------------------------------------------
    // 		public Getters
    //------------------------------------------------
    public String getStrPortableAreaSqncNumber() { return strPortableAreaSqncNumber; }
    public String getStrPortableAreaNameSqncNo() { return strPortableAreaNameSqncNo; }
    
    
    //------------------------------------------------
    // 		public Setters
    //------------------------------------------------
    public void setStrPortableAreaSqncNumber(String aUsrGrpAssgnmntSqncNmbr) {
        if (aUsrGrpAssgnmntSqncNmbr != null)
            this.strPortableAreaSqncNumber = aUsrGrpAssgnmntSqncNmbr.trim();
        else
            this.strPortableAreaSqncNumber = aUsrGrpAssgnmntSqncNmbr;
    }
    public void setStrPortableAreaNameSqncNo(String aUserID) {
        if (aUserID != null)
            this.strPortableAreaNameSqncNo = aUserID.trim();
        else
            this.strPortableAreaNameSqncNo = aUserID;
    }
    
    
    public int deletePortableAreaBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build DELETE SQL statement
            
            String strQuery = "DELETE PORTABLE_AREA_T WHERE portable_area_sqnc_no = '" + strPortableAreaSqncNumber+"'";
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
    
    public int retrievePortableAreaBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build SELECT SQL statement
            
            
            String strQuery = "SELECT * " +
                    "FROM PORTABLE_AREA_T WHERE portable_area_sqnc_no = '" + strPortableAreaSqncNumber+"'";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            
            if (rs.next()) {
                this.strPortableAreaNameSqncNo = rs.getString("PORTABLE_AREA_NAME_SQNC_NMBR");
                this.m_strMdfdDt = rs.getString("MDFD_DT");
                this.m_strMdfdUserid = rs.getString("MDFD_USERID");
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
    
    public int updatePortableAreaBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build UPDATE SQL statement
            String strQuery = "UPDATE PORTABLE_AREA_T SET PORTABLE_AREA_NAME_SQNC_NMBR = '" +
                    Toolkit.replaceSingleQwithDoubleQ(strPortableAreaNameSqncNo) +"', MDFD_DT = sysdate, MDFD_USERID = '" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) +
                    "' WHERE portable_area_sqnc_no = '" + strPortableAreaSqncNumber+"'";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            if (stmt.executeUpdate(strQuery) <= 0) {
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
    
    public int savePortableAreaBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            String strQuery = "INSERT INTO PORTABLE_AREA_T" +
                    " VALUES (PORTABLE_AREA_SEQ.nextval, '" +
                    Toolkit.replaceSingleQwithDoubleQ(strPortableAreaNameSqncNo) + "','" +
                    Toolkit.replaceSingleQwithDoubleQ(strVendorConfigSqncNumber) +
                     "',sysdate,'" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "')";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            Log.write("strQuery=="+strQuery);
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
    
    public boolean validatePortableAreaBean() {
        return true;
    }
    
    public boolean validateMdfdDt() {
        Connection con = null;
        Statement stmt = null;
        String strMdfdDt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(strPortableAreaSqncNumber);
            String strQuery = "SELECT MDFD_DT FROM PORTABLE_AREA_T " +
                    "WHERE portable_area_sqnc_no = " + iUsrGrpAssgnmntSqncNmbr;
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                strMdfdDt = rs.getString("MDFD_DT");
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
    
}// end of PortableAreaBean()



