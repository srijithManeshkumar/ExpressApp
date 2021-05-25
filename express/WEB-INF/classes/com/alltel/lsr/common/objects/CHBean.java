package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class CHBean extends AlltelCtlrBean {
    final private String m_strTableName = "CH_T";
    
    private String m_strUsrGrpAssgnmntSqncNmbr = " ";
    private String m_strID = " ";
    private String m_strName1 = " ";
    private String m_strName2 = " ";
    
    public CHBean() {
        setSecurityTags(m_strTableName);
    }
    
    //------------------------------------------------
    // 		public Getters
    //------------------------------------------------
    public String getUsrGrpAssgnmntSqncNmbr() { return m_strUsrGrpAssgnmntSqncNmbr; }
    public String getID() { return m_strID; }
    public String getName1() { return m_strName1; }
    public String getName2() { return m_strName2; }
    
    //------------------------------------------------
    // 		public Setters
    //------------------------------------------------
    public void setUsrGrpAssgnmntSqncNmbr(String aUsrGrpAssgnmntSqncNmbr) {
        if (aUsrGrpAssgnmntSqncNmbr != null)
            this.m_strUsrGrpAssgnmntSqncNmbr = aUsrGrpAssgnmntSqncNmbr.trim();
        else
            this.m_strUsrGrpAssgnmntSqncNmbr = aUsrGrpAssgnmntSqncNmbr;
    }
    public void setID(String aUserID) {
        if (aUserID != null)
            this.m_strID = aUserID.trim();
        else
            this.m_strID = aUserID;
    }
    public void setName1(String aUsrGrpCd) {
        if (aUsrGrpCd != null)
            this.m_strName1 = aUsrGrpCd.trim();
        else
            this.m_strName1 = aUsrGrpCd;
    }
    public void setName2(String aScrtyGrpCd) {
        if (aScrtyGrpCd != null)
            this.m_strName2 = aScrtyGrpCd.trim();
        else
            this.m_strName2 = aScrtyGrpCd;
    }
    
    public int deleteCHBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build DELETE SQL statement
            int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(m_strUsrGrpAssgnmntSqncNmbr);
            
            String strQuery = "DELETE CH_T WHERE SEQUENCE_NBR= " + iUsrGrpAssgnmntSqncNmbr;
            
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
    
    public int retrieveCHBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build SELECT SQL statement
            int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(m_strUsrGrpAssgnmntSqncNmbr);
            
            String strQuery = "SELECT * " +
                    "FROM CH_T WHERE SEQUENCE_NBR = " + iUsrGrpAssgnmntSqncNmbr;
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            
            if (rs.next()) {
                this.m_strID = rs.getString("ID");
                this.m_strName1 = rs.getString("NAME1");
                this.m_strName2 = rs.getString("NAME2");
                
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
    
    public int updateCHBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build UPDATE SQL statement
            int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(m_strUsrGrpAssgnmntSqncNmbr);
            
            String strQuery = "UPDATE CH_T SET NAME1 = '" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strName1) + "', NAME2 = '" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strName2) + "'" +
                    " WHERE SEQUENCE_NBR = " + iUsrGrpAssgnmntSqncNmbr;
            
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
    
    public int saveCHBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            String strQuery = "INSERT INTO CH_T VALUES ('" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strName1) + "','" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strName2) + "',K1.nextval,'" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strID) + "')";
            
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
    
    public boolean validateCHBean() {
        return true;
    }
    
    public boolean validateMdfdDt() {
        Connection con = null;
        Statement stmt = null;
        String strMdfdDt = null;
        
        // Get DB Connection
        try {
            //kumar hot code temp
            if(true) return true;
            // Build INSERT SQL statement
            int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(m_strUsrGrpAssgnmntSqncNmbr);
            String strQuery = "SELECT MDFD_DT FROM CH_T " +
                    "WHERE USR_GRP_ASSGNMNT_SQNC_NMBR = " + iUsrGrpAssgnmntSqncNmbr;
            
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
    
}
