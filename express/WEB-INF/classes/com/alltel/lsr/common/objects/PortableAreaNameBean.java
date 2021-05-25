package com.alltel.lsr.common.objects;

import com.alltel.lsr.common.objects.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class PortableAreaNameBean extends AlltelCtlrBean {
    
    final private String m_strTableName = "PORTABLE_AREA_NAME_T";
    
    private String strPortableAreaNameSeqNo = " ";
    private String strPortableAreaName = " ";
   
//
    public PortableAreaNameBean() {
        setSecurityTags(m_strTableName);
    }
    
    
    //------------------------------------------------
    // 		public Getters
    //------------------------------------------------
    public String getStrPortableAreaNameConfigSeqNo() { return strPortableAreaNameSeqNo; }
    public String getStrPortableAreaName() { return strPortableAreaName; }
    
    
    //------------------------------------------------
    // 		public Setters
    //------------------------------------------------
    public void setStrPortableAreaNameConfigSeqNo(String strPortableAreaNameSeqNo) {
        if (strPortableAreaNameSeqNo != null)
            this.strPortableAreaNameSeqNo = strPortableAreaNameSeqNo.trim();
        else
            this.strPortableAreaNameSeqNo = strPortableAreaNameSeqNo;
    }
    public void setStrPortableAreaName(String strPortableAreaName) {
        if (strPortableAreaName != null)
            this.strPortableAreaName = strPortableAreaName.trim();
        else
            this.strPortableAreaName = strPortableAreaName;
    }
    
    
    public int deletePortableAreaNameBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build DELETE SQL statement
            
            String strQuery = "DELETE PORTABLE_AREA_NAME_T WHERE PORTABLE_AREA_NAME_SQNC_NMBR = '" + strPortableAreaNameSeqNo+"'";
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
    
    public int retrievePortableAreaNameBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build SELECT SQL statement
            
            
            String strQuery = "SELECT * " +
                    "FROM PORTABLE_AREA_NAME_T WHERE PORTABLE_AREA_NAME_SQNC_NMBR = '" + strPortableAreaNameSeqNo+"'";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            
            if (rs.next()) {
                this.strPortableAreaName = rs.getString("PORTABLE_AREA_NAME");
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
    
    public int updatePortableAreaNameBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build UPDATE SQL statement
            String strQuery = "UPDATE PORTABLE_AREA_NAME_T SET PORTABLE_AREA_NAME = '" +
                    Toolkit.replaceSingleQwithDoubleQ(strPortableAreaName) +
                    "',MDFD_DT = sysdate, MDFD_USERID = '" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) +
                    "' WHERE PORTABLE_AREA_NAME_SQNC_NMBR = '" + strPortableAreaNameSeqNo+"'";
            
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
    
    public int savePortableAreaNameBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            String strQuery = "INSERT INTO PORTABLE_AREA_NAME_T " +
                    "VALUES (PORTABLE_AREA_NAME_SEQ.nextval, '" +
                    Toolkit.replaceSingleQwithDoubleQ(strPortableAreaName) +"',sysdate,'" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "')";
            
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
    
    public boolean validatePortableAreaNameBean() {
        return true;
    }
    
    public boolean validateMdfdDt() {
        Connection con = null;
        Statement stmt = null;
        String strMdfdDt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(strPortableAreaNameSeqNo);
            String strQuery = "SELECT MDFD_DT FROM PORTABLE_AREA_NAME_T " +
                    "WHERE PORTABLE_AREA_NAME_SQNC_NMBR = " + iUsrGrpAssgnmntSqncNmbr;
            
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
    
   
}// end of PortableAreaNameBean()



