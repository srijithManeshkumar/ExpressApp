package com.alltel.lsr.common.objects;

import com.alltel.lsr.common.objects.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class AsocTypeBean extends AlltelCtlrBean {
    
    final private String m_strTableName = "ASOC_CATEGORY_T";
    
    private String strAsocTypeConfigSeqNo = " ";
    private String strAsocType = " ";
    //ASOC_DESCRIPTION
    private String strAsocDescrption = " ";
//    private String m_strUsrGrpCd = " ";
//    private String m_strScrtyGrpCd = " ";
//
    public AsocTypeBean() {
        setSecurityTags(m_strTableName);
    }
    
    
    //------------------------------------------------
    // 		public Getters
    //------------------------------------------------
    public String getStrAsocTypeConfigSeqNo() { return strAsocTypeConfigSeqNo; }
    public String getStrAsocType() { return strAsocType; }
    
    
    //------------------------------------------------
    // 		public Setters
    //------------------------------------------------
    public void setStrAsocTypeConfigSeqNo(String strAsocTypeConfigSeqNo) {
        if (strAsocTypeConfigSeqNo != null)
            this.strAsocTypeConfigSeqNo = strAsocTypeConfigSeqNo.trim();
        else
            this.strAsocTypeConfigSeqNo = strAsocTypeConfigSeqNo;
    }
    public void setStrAsocType(String strAsocType) {
        if (strAsocType != null)
            this.strAsocType = strAsocType.trim();
        else
            this.strAsocType = strAsocType;
    }
    
    
    public int deleteAsocTypeBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build DELETE SQL statement
            
            String strQuery = "DELETE ASOC_CATEGORY_T WHERE ASOC_TYPE_CONFIG_SQNC_NMBR = '" + strAsocTypeConfigSeqNo+"'";
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
    
    public int retrieveAsocTypeBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build SELECT SQL statement
            
            
            String strQuery = "SELECT * " +
                    "FROM ASOC_CATEGORY_T WHERE ASOC_TYPE_CONFIG_SQNC_NMBR = '" + strAsocTypeConfigSeqNo+"'";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            
            if (rs.next()) {
                this.strAsocType = rs.getString("ASOC_TYPE");
                this.strAsocDescrption = rs.getString("ASOC_DESCRIPTION");
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
    
    public int updateAsocTypeBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build UPDATE SQL statement
            String strQuery = "UPDATE ASOC_CATEGORY_T SET ASOC_TYPE = '" +
                    Toolkit.replaceSingleQwithDoubleQ(strAsocType) +"', ASOC_DESCRIPTION='"+ 
                    Toolkit.replaceSingleQwithDoubleQ(strAsocDescrption) +
                    "',MDFD_DT = sysdate, MDFD_USERID = '" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) +
                    "' WHERE ASOC_TYPE_CONFIG_SQNC_NMBR = '" + strAsocTypeConfigSeqNo+"'";
            
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
    
    public int saveAsocTypeBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            String strQuery = "INSERT INTO ASOC_CATEGORY_T " +
                    "VALUES (ASOC_TYPE_CONFIG_SEQ.nextval, '" +
                    Toolkit.replaceSingleQwithDoubleQ(strAsocType) + "','" +
                    Toolkit.replaceSingleQwithDoubleQ(strAsocDescrption) + "',sysdate,'" +
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
    
    public boolean validateAsocTypeBean() {
        return true;
    }
    
    public boolean validateMdfdDt() {
        Connection con = null;
        Statement stmt = null;
        String strMdfdDt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(strAsocTypeConfigSeqNo);
            String strQuery = "SELECT MDFD_DT FROM ASOC_CATEGORY_T " +
                    "WHERE ASOC_TYPE_CONFIG_SQNC_NMBR = " + iUsrGrpAssgnmntSqncNmbr;
            
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
    
    public String getStrAsocDescrption() {
        return strAsocDescrption;
    }
    
    public void setStrAsocDescrption(String strAsocDescrption) {
        this.strAsocDescrption = strAsocDescrption;
    }
    
}// end of AsocTypeBean()



