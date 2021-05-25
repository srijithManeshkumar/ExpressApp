

package com.alltel.lsr.common.objects;

import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class VendorAutomationBean extends AlltelCtlrBean {
    
    final private String m_strTableName = "VENDOR_AUTOMATION_CONFIG_T";
    
    private String strVendorAutomationConfigDescription ="";
    
    
//    private String cLEC_RSLoginDTime=  "";
//    private String cLEC_RSLoginETime=  "";
    
    
    
    
    private String strConfigDescriptionSPANStart = " ";
    private String strConfigDescriptionSPANEnd = " ";
    
    public String getConfigDescriptionSPANStart() { return strConfigDescriptionSPANStart; }
    public String getConfigDescriptionSPANEnd() { return strConfigDescriptionSPANEnd; }
    
    private int    m_iLgnAttmpts = 0;
    private String m_strLstLgnDt = " ";
    
    public VendorAutomationBean() {
        setSecurityTags(getM_strTableName());
    }
    
    public int    getLgnAttmpts() { return m_iLgnAttmpts; }
    public String getLstLgnDt() { return m_strLstLgnDt; }
    
    public void setLgnAttmpts(int iAttempt) { this.m_iLgnAttmpts = iAttempt; }
    public void setLstLgnDt() { this.m_strLstLgnDt = Toolkit.getDateTime(); }
    
    public int deleteVendorAutomationBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        int a=0;
        // Get DB Connection
        try {
            // Build DELETE SQL statement
            String strQuery = "DELETE VENDOR_AUTOMATION_CONFIG_T WHERE Vendor_Automation_Config_sqnc_nmbr = '" + Toolkit.replaceSingleQwithDoubleQ(getStrVendorAutomationConfigSqncNumber())
            + "'";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            a = stmt.executeUpdate(strQuery);
        } catch(SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public int retrieveVendorAutomationBeanFromDB(String configNumber) {
        System.out.println("==retrieveVendorAutomationBeanFromDB=");
        Log.write(Log.DEBUG_VERBOSE, "retrieveVendorAutomationBeanFromDB kk");
        // Clear out beans prior contents...
        Log.write(Log.DEBUG, "VendorAutomationBean() retrieve() user = " + configNumber);
        setStrVendorAutomationConfigSqncNumber(configNumber);
        setStrVendorAutomationConfigDescription("");
        return retrieveVendorAutomationBeanFromDB();
    }
    
    public int retrieveVendorAutomationBeanFromDB() {
        Log.write("retrieveVendorAutomationBeanFromDB inside kk");
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            
            // Build SELECT SQL statement
            String strQuery = "SELECT * from  VENDOR_AUTOMATION_CONFIG_T WHERE Vendor_Automation_Config_sqnc_nmbr = '" +
                    Toolkit.replaceSingleQwithDoubleQ(getStrVendorAutomationConfigSqncNumber()) + "'";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
			// removed the word kk which was not needed from log message -- Antony -- 05/26/2010
            Log.write(Log.DEBUG_VERBOSE, "LPB() rs "+rs);
            
            if (rs.next()) {
                System.out.println("==retrieveVendorAutomationBeanFromDB= if");
                Log.write(Log.DEBUG, "VendorAutomationBean()if got user ");
                this.strVendorAutomationConfigSqncNumber = rs.getString("Vendor_Automation_Config_sqnc_nmbr");
                this.strVendorAutomationConfigDescription= rs.getString("Vendor_Automation_Config_description");
                
            } else {
                System.out.println("==retrieveVendorAutomationBeanFromDB= else");
                Log.write(Log.DEBUG, "VendorAutomationBean() else user ");
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
    
    public int updateVendorAutomationBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        String strQuery = null;
        
        // Get DB Connection
        try {
            // Build UPDATE SQL statement
            // int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);
            
            strQuery = "UPDATE VENDOR_AUTOMATION_CONFIG_T SET Vendor_Automation_Config_description = '" + Toolkit.replaceSingleQwithDoubleQ(strVendorAutomationConfigDescription) +
                    "' WHERE Vendor_Automation_Config_sqnc_nmbr = '" + Toolkit.replaceSingleQwithDoubleQ(getStrVendorAutomationConfigSqncNumber()) + "'";
            
            
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
    
    public int saveVendorAutomationBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        String strQuery = "";
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            //   int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);
            
            
            strQuery = "INSERT INTO VENDOR_AUTOMATION_CONFIG_T " +
                    "values('VENDOR_AUTOMATION_CONFIG_SEQ.nextval','"+
                    Toolkit.replaceSingleQwithDoubleQ(strVendorAutomationConfigDescription)
                    + "',sysdate,'"+ Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid)+"')";
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
    
    public boolean validateVendorAutomationBean() {
        boolean rc = true;
        
        strConfigDescriptionSPANStart = getSPANStart();
        strConfigDescriptionSPANEnd = getSPANEnd();
        
        
        // Validate
        if ((strVendorAutomationConfigDescription == null) || (strVendorAutomationConfigDescription.length() == 0)) {
            strConfigDescriptionSPANStart = getErrSPANStart();
            strConfigDescriptionSPANEnd = getErrSPANEnd();
            rc = false;
        } else if (Validate.containsSpecialChars(strVendorAutomationConfigDescription)) {
            strConfigDescriptionSPANStart = getErrSPANStart();
            strConfigDescriptionSPANEnd = getErrSPANEnd();
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
            String strQuery = "SELECT MDFD_DT FROM VENDOR_AUTOMATION_CONFIG_T ";
            
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
    
    public String getM_strTableName() {
        return m_strTableName;
    }
    
    
    public String getStrVendorAutomationConfigDescription() {
        return strVendorAutomationConfigDescription;
    }
    
    public void setStrVendorAutomationConfigDescription(String strVendorAutomationConfigDescription) {
        this.strVendorAutomationConfigDescription = strVendorAutomationConfigDescription;
    }
    
    
    
    
}// end of VendorAutomationBean()

