package com.alltel.lsr.common.objects;

import com.alltel.lsr.common.objects.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import java.io.Serializable;

public class VendorAsocBean extends AlltelCtlrBean {
    
    final private String m_strTableName = "VENDOR_ASOC_CONFIG_T";
    
    private String strVendorAsocConfigSeqNo= " ";
    private String strAsocTypeConfigSeqNo = " ";
    private String strAsocTypeCode = " ";
    private String strHowAsocFeeApplies = " ";
    private String strAsocFeeRate = " ";
    
    private String strAsocFeeRateSPANStart = " ";
    private String strAsocFeeRateSPANEnd = " ";
    
    private String filedInd = " ";
    
    
    public String getASFRSPANStart() { return strAsocFeeRateSPANStart; }
    public String getASFRSPANEnd() { return strAsocFeeRateSPANEnd; }
    public VendorAsocBean() {
        setSecurityTags(getM_strTableName());
    }
    
    
    public int deleteVendorAsocBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build DELETE SQL statement
            
            String strQuery = "DELETE VENDOR_ASOC_CONFIG_T WHERE VENDOR_ASOC_CONFIG_SQNC_NMBR = '" + getStrVendorAsocConfigSeqNo()+"'";
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
    
    public int retrieveVendorAsocBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build SELECT SQL statement
            
            
            String strQuery = "SELECT * " +
                    "FROM VENDOR_ASOC_CONFIG_T WHERE VENDOR_ASOC_CONFIG_SQNC_NMBR = '" + getStrVendorAsocConfigSeqNo()+"'";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            Log.write("====strQuery=="+strQuery);
            ResultSet rs = stmt.executeQuery(strQuery);
            
            if (rs.next()) {
                
                this.setStrAsocTypeConfigSeqNo(rs.getString("ASOC_TYPE_CONFIG_SQNC_NMBR"));
                this.setStrAsocTypeCode(rs.getString("ASOC_CD"));
                //    this.setDoesAsocFeeApply(rs.getString("ASOC_FEE_APPLIES"));
                this.setStrHowAsocFeeApplies(rs.getString("HOW_ASOC_FEE_APPLIES"));
                this.setStrAsocFeeRate(rs.getString("ASOC_FEE_RATE"));
                this.setFiledInd(rs.getString("BUS_RES_IND"));
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
    
    public int updateVendorAsocBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build UPDATE SQL statement
            String strQuery = "UPDATE VENDOR_ASOC_CONFIG_T SET ASOC_TYPE_CONFIG_SQNC_NMBR = '" +
                    Toolkit.replaceSingleQwithDoubleQ(getStrAsocTypeConfigSeqNo()) +"',ASOC_CD='"+Toolkit.replaceSingleQwithDoubleQ(getStrAsocTypeCode()) +
                    "',HOW_ASOC_FEE_APPLIES='"+Toolkit.replaceSingleQwithDoubleQ(getStrHowAsocFeeApplies()) +
                    "',ASOC_FEE_RATE='"+getStrAsocFeeRate() +
                    "', MDFD_DT = sysdate, MDFD_USERID = '" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) +"',BUS_RES_IND='"+ filedInd+
                    "' WHERE VENDOR_ASOC_CONFIG_SQNC_NMBR = '" + getStrVendorAsocConfigSeqNo()+"'";
            
            Log.write("====updateVendorAsocBeanToDB==strQuery="+strQuery);
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
    
    public boolean validateVendorBean() {
        try{
            boolean rc = true;
            if(rc) return true;
            strAsocFeeRateSPANStart = getSPANStart();
            strAsocFeeRateSPANEnd = getSPANEnd();
            
            if (rc == false)
                m_strErrMsg = "ERROR:  Please review the data";
            
            return rc;
            
        }catch(Exception e) {
            Log.write("====saveVendorBeanToDB==="+e.getMessage());
            Log.write(e.toString());
        }
        return true;
    }
    
    
    public int saveVendorAsocBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            String strQuery = "INSERT INTO VENDOR_ASOC_CONFIG_T " +
                    "VALUES (VENDOR_ASOC_CONFIG_SEQ.nextval,'" +
                    Toolkit.replaceSingleQwithDoubleQ(getStrAsocTypeConfigSeqNo()) + "','" +
                    Toolkit.replaceSingleQwithDoubleQ(getStrAsocTypeCode()) + "','" +
                    Toolkit.replaceSingleQwithDoubleQ(getStrHowAsocFeeApplies()) + "','" +
                    getStrAsocFeeRate()+ "','" +
                    Toolkit.replaceSingleQwithDoubleQ(strVendorConfigSqncNumber)+
                    "',sysdate,'" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "','"+filedInd+
                    "')";
            
            Log.write("====saveVendorAsocBeanToDB==strQuery="+strQuery);
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(strQuery);
        } catch(SQLException sqle) {
            Log.write("====saveVendorAsocBeanToDB==="+sqle.getMessage());
            sqle.printStackTrace();
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            Log.write("====saveVendorAsocBeanToDB==="+e.getMessage());
            e.printStackTrace();
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public boolean validateVendorAsocBean() {
        return true;
    }
    
    public boolean validateMdfdDt() {
        Connection con = null;
        Statement stmt = null;
        String strMdfdDt = null;
        
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(getStrVendorAsocConfigSeqNo());
            String strQuery = "SELECT MDFD_DT FROM VENDOR_ASOC_CONFIG_T " +
                    "WHERE VENDOR_ASOC_CONFIG_SQNC_NMBR = " + iUsrGrpAssgnmntSqncNmbr;
            
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
    
    public String getStrVendorAsocConfigSeqNo() {
        return strVendorAsocConfigSeqNo;
    }
    
    public void setStrVendorAsocConfigSeqNo(String strVendorAsocConfigSeqNo) {
        this.strVendorAsocConfigSeqNo = strVendorAsocConfigSeqNo;
    }
    
    public String getStrAsocTypeConfigSeqNo() {
        return strAsocTypeConfigSeqNo;
    }
    
    public void setStrAsocTypeConfigSeqNo(String strAsocTypeConfigSeqNo) {
        this.strAsocTypeConfigSeqNo = strAsocTypeConfigSeqNo;
    }
    
    public String getStrAsocTypeCode() {
        return strAsocTypeCode;
    }
    
    public void setStrAsocTypeCode(String strAsocTypeCode) {
        this.strAsocTypeCode = strAsocTypeCode;
    }
    
    public String getStrHowAsocFeeApplies() {
        return strHowAsocFeeApplies;
    }
    
    public void setStrHowAsocFeeApplies(String strHowAsocFeeApplies) {
        this.strHowAsocFeeApplies = strHowAsocFeeApplies;
    }
    
    public String getStrAsocFeeRate() {
        return strAsocFeeRate;
    }
    
    public void setStrAsocFeeRate(String strAsocFeeRate) {
        this.strAsocFeeRate = strAsocFeeRate;
    }
    
    public String getFiledInd() {
        return filedInd;
    }
    
    public void setFiledInd(String filedInd) {
        this.filedInd = filedInd;
    }
    
    public static void main(String[] args) {
        for(int i=1;i<=24;i++){
            System.out.println("--"+i);
        }
    }
    
}// end of VendorAsocBean()



