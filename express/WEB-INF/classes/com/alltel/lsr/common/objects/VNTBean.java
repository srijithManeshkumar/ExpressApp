

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class VNTBean extends AlltelCtlrBean {
    final private String m_strTableName = "VNT_T";
    
    private String m_strID = " ";
    private String m_strIDSPANStart = " ";
    private String m_strIDSPANEnd = " ";
    
    private String m_strName = " ";
    private String m_strNameSPANStart = " ";
    private String m_strNameSPANEnd = " ";
    
    private String m_strAge = " ";
    private String m_strAgeSPANStart = " ";
    private String m_strAgeSPANEnd = " ";
    
    private String m_strCmpnySqncNmbr = " ";
    private String m_strCmpnyTyp = " ";
    
    private int    m_iLgnAttmpts = 0;
    private String m_strLstLgnDt = " ";
    
    public VNTBean() {
        setSecurityTags(m_strTableName);
    }
    
    //------------------------------------------------
    // 		public Getters
    //------------------------------------------------
    public String getID() { return m_strID; }
    public String getIDSPANStart() { return m_strIDSPANStart; }
    public String getIDSPANEnd() { return m_strIDSPANEnd; }
    
    public String getName() { return m_strName; }
    public String getNameSPANStart() { return m_strNameSPANStart; }
    public String getNameSPANEnd() { return m_strNameSPANEnd; }
    
    public String getAge() { return m_strAge; }
    public String getAgeSPANStart() { return m_strAgeSPANStart; }
    public String getAgeSPANEnd() { return m_strAgeSPANEnd; }
    
    public String getCmpnySqncNmbr() { return m_strCmpnySqncNmbr; }
    public String getCmpnyTyp() { return m_strCmpnyTyp; }
    
    public int    getLgnAttmpts() { return m_iLgnAttmpts; }
    public String getLstLgnDt() { return m_strLstLgnDt; }
    
    //------------------------------------------------
    // 		public Setters
    //------------------------------------------------
    public void setID(String aUserID) {
        if (aUserID != null)
            this.m_strID = aUserID.trim();
        else
            this.m_strID = aUserID;
    }
    public void setName(String aFrstNm) {
        if (aFrstNm != null)
            this.m_strName = aFrstNm.trim();
        else
            this.m_strName = aFrstNm;
    }
    public void setAge(String aAge) {
        if (aAge != null)
            this.m_strAge = aAge.trim();
        else
            this.m_strAge = aAge;
    }
    
    public void setCmpnySqncNmbr(String aCmpnySqncNmbr) {
        if (aCmpnySqncNmbr != null)
            this.m_strCmpnySqncNmbr = aCmpnySqncNmbr.trim();
        else
            this.m_strCmpnySqncNmbr = aCmpnySqncNmbr;
    }
    public void setCmpnyTyp(String aCmpnyType) {
        if (aCmpnyType != null)
            this.m_strCmpnyTyp = aCmpnyType.trim();
        else
            this.m_strCmpnyTyp = "";
    }
    
    public void setLgnAttmpts(int iAttempt) { this.m_iLgnAttmpts = iAttempt; }
    public void setLstLgnDt() { this.m_strLstLgnDt = Toolkit.getDateTime(); }
    
    public int deleteVNTBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build DELETE SQL statement
            String strQuery = "DELETE VNT_T WHERE ID = '" + Toolkit.replaceSingleQwithDoubleQ(m_strID)
            + "'";
            
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
    
    public int retrieveVNTBeanFromDB(String aUserID) {
        System.out.println("==retrieveVNTBeanFromDB=");
        Log.write(Log.DEBUG_VERBOSE, "retrieveVNTBeanFromDB kk");
        // Clear out beans prior contents...
        Log.write(Log.DEBUG, "VNTBean() retrieve() user = " + aUserID);
        setID(aUserID);
        setName("");
        setAge("");
        setCmpnySqncNmbr("");
        setCmpnyTyp("");
        return retrieveVNTBeanFromDB();
    }
    
    public int retrieveVNTBeanFromDB() {
        Log.write("retrieveVNTBeanFromDB inside kk");
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build SELECT SQL statement
            String strQuery = "SELECT ID, NAME, AGE," +
                    "U.CMPNY_SQNC_NMBR, C.CMPNY_TYP " +
                    "FROM VNT_T U, COMPANY_T C WHERE ID = '" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strID) + "' AND U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            Log.write(Log.DEBUG_VERBOSE, "LPB() rs kk"+rs);
            
            if (rs.next()) {
                System.out.println("==retrieveVNTBeanFromDB= if");
                Log.write(Log.DEBUG, "VNTBean()if got user ");
                this.m_strName = rs.getString("NAME");
                this.m_strAge = rs.getString("AGE");
                this.m_strCmpnySqncNmbr = rs.getString("CMPNY_SQNC_NMBR");
                this.m_strCmpnyTyp = rs.getString("CMPNY_TYP");
            } else {
                System.out.println("==retrieveVNTBeanFromDB= else");
                Log.write(Log.DEBUG, "VNTBean() else user ");
                return 1;
            }
        } catch(SQLException sqle) {
            Log.write(1,sqle.toString());
            sqle.printStackTrace();
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            Log.write(1,e.toString());
            e.printStackTrace();
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public int updateVNTBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        String strQuery = null;
        
        // Get DB Connection
        try {
            // Build UPDATE SQL statement
            int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);
            strQuery = "UPDATE VNT_T SET NAME='"+m_strName+"',AGE='"+m_strAge+"',CMPNY_SQNC_NMBR='"+
                    iCmpnySqncNmbr+"' where ID='"+m_strID+"'";
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
    
    public int saveVNTBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        String strQuery = "";
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);
            
            strQuery = "INSERT INTO VNT_T(ID,NAME,AGE,CMPNY_SQNC_NMBR) " +
                    "values('"+m_strID+"','"+m_strName+"','"+m_strAge+"','"+iCmpnySqncNmbr+"')";
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
    
    public boolean validateVNTBean() {
        boolean rc = true;
        
        m_strIDSPANStart = getSPANStart();
        m_strIDSPANEnd = getSPANEnd();
        m_strNameSPANStart = getSPANStart();
        m_strNameSPANEnd = getSPANEnd();
        m_strAgeSPANStart = getSPANStart();
        m_strAgeSPANEnd = getSPANEnd();
        
        // Validate User ID
        if ((m_strID == null) || (m_strID.length() == 0)) {
            m_strIDSPANStart = getErrSPANStart();
            m_strIDSPANEnd = getErrSPANEnd();
            rc = false;
        } else if (! Validate.isValidUserID(m_strID)) {
            m_strIDSPANStart = getErrSPANStart();
            m_strIDSPANEnd = getErrSPANEnd();
            rc = false;
        }
        
        // Validate First Name
        if ((m_strName == null) || (m_strName.length() == 0)) {
            m_strNameSPANStart = getErrSPANStart();
            m_strNameSPANEnd = getErrSPANEnd();
            rc = false;
        } else if (Validate.containsSpecialChars(m_strName)) {
            m_strNameSPANStart = getErrSPANStart();
            m_strNameSPANEnd = getErrSPANEnd();
            rc = false;
        }
        
        // Validate Last Name
        if ((m_strAge == null) || (m_strAge.length() == 0)) {
            m_strAgeSPANStart = getErrSPANStart();
            m_strAgeSPANEnd = getErrSPANEnd();
            rc = false;
        } else if (Validate.containsSpecialChars(m_strAge)) {
            m_strAgeSPANStart = getErrSPANStart();
            m_strAgeSPANEnd = getErrSPANEnd();
            rc = false;
        }
        
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
            // Build SQL statement
            //kumar hot code temp
            if(true)	 return true;
            String strQuery = "SELECT MDFD_DT FROM VNT_T where ID = '" + m_strID + "'";
            
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
    
   
    
    
    
    
    
}// end of VNTBean()

