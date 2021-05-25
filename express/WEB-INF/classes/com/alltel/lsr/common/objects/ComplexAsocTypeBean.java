/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alltel.lsr.common.objects;

import com.alltel.lsr.common.util.DatabaseManager;
import com.alltel.lsr.common.util.Toolkit;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author satish.t
 */
public class ComplexAsocTypeBean extends AlltelCtlrBean {

    final private String m_strTableName = "COMPLEX_ASOCS_T";
    private String strAsocTypeConfigSeqNo = " ";
    private String strAsocType = " ";
    private String strAsocDescrption = " ";

    public ComplexAsocTypeBean() {
        setSecurityTags(m_strTableName);
    }

    public String getStrAsocTypeConfigSeqNo() {
        return strAsocTypeConfigSeqNo;
    }

    public String getStrAsocType() {
        return strAsocType;
    }

    public void setStrAsocTypeConfigSeqNo(String strAsocTypeConfigSeqNo) {
        if (strAsocTypeConfigSeqNo != null) {
            this.strAsocTypeConfigSeqNo = strAsocTypeConfigSeqNo.trim();
        } else {
            this.strAsocTypeConfigSeqNo = strAsocTypeConfigSeqNo;
        }
    }

    public void setStrAsocType(String strAsocType) {
        if (strAsocType != null) {
            this.strAsocType = strAsocType.trim();
        } else {
            this.strAsocType = strAsocType;
        }
    }
    /**
     * This method will delete the selected record from the complex asocs table
     * @return integer value if delete is successful.
     */
    public int deleteAsocTypeBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        try {
            String strQuery = "DELETE COMPLEX_ASOCS_T WHERE COMPLEX_ASOC_SQNC_NMBR = '" + strAsocTypeConfigSeqNo + "'";
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(strQuery);
        } catch (SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch (Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return 0;
    }
    /**
     * This method will retrive the asoc type from complex asocs table
     * for the sequence number.
     * @return integer if the record is successfully retrieved
     */
    public int retrieveAsocTypeBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        try {
            String strQuery = "SELECT * "
                    + "FROM COMPLEX_ASOCS_T WHERE COMPLEX_ASOC_SQNC_NMBR = '" + strAsocTypeConfigSeqNo + "'";

            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);

            if (rs.next()) {
                this.strAsocType = rs.getString("ASOC_CD");
                this.strAsocDescrption = rs.getString("ASOC_DESC");
                this.m_strMdfdDt = rs.getString("MDFD_DT");
                this.m_strMdfdUserid = rs.getString("MDFD_USERID");
            } else {
                return 1;
            }
        } catch (SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch (Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return 0;
    }
    /**
     * This method will update the existing asoc type or description based
     * on the given input values
     * @return integer if the asoc type is successfully updated
     */
    public int updateAsocTypeBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        try {
            String strQuery = "UPDATE COMPLEX_ASOCS_T SET ASOC_CD = '"
                    + Toolkit.replaceSingleQwithDoubleQ(strAsocType) + "', ASOC_DESC='"
                    + Toolkit.replaceSingleQwithDoubleQ(strAsocDescrption)
                    + "',MDFD_DT = sysdate, MDFD_USERID = '"
                    + Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid)
                    + "' WHERE COMPLEX_ASOC_SQNC_NMBR = '" + strAsocTypeConfigSeqNo + "'";

            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            if (stmt.executeUpdate(strQuery) <= 0) {
                throw new SQLException(null, null, 100);
            }
        } catch (SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch (Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return 0;
    }
    /**
     * This method will create new asoc type and description for that type.
     * @return integer if the asoc type is successfully created
     */
    public int saveAsocTypeBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        try {
            String strQuery = "INSERT INTO COMPLEX_ASOCS_T "
                    + "VALUES (COMPLEX_ASOC_SEQ.nextval, '"
                    + Toolkit.replaceSingleQwithDoubleQ(strAsocType) + "','"
                    + Toolkit.replaceSingleQwithDoubleQ(strAsocDescrption) + "',sysdate,'"
                    + Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) + "')";

            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(strQuery);
        } catch (SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch (Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return 0;
    }

    public boolean validateAsocTypeBean() {
        return true;
    }
    /**
     * This method will check that no one else has modifed
     * this row since it was retrieved
     * @return
     */
    public boolean validateMdfdDt() {
        Connection con = null;
        Statement stmt = null;
        String strMdfdDt = null;
        try {
            int iUsrGrpAssgnmntSqncNmbr = Integer.parseInt(strAsocTypeConfigSeqNo);
            String strQuery = "SELECT MDFD_DT FROM COMPLEX_ASOCS_T "
                    + "WHERE COMPLEX_ASOC_SQNC_NMBR = " + iUsrGrpAssgnmntSqncNmbr;

            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                strMdfdDt = rs.getString("MDFD_DT");
            } else {
                throw new SQLException(null, null, 100);
            }
        } catch (SQLException sqle) {
            handleSQLError(sqle.getErrorCode());
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        // As long as the dates are equal, all is well in the world and no one has changed the record
        if (strMdfdDt.equals(m_strMdfdDt)) {
            return true;
        } else {
            m_strErrMsg = "ERROR:  This row has been modified since you retrieved it. "
                    + "Please CANCEL and retrieve the row again.";
            return false;
        }
    }

    public String getStrAsocDescrption() {
        return strAsocDescrption;
    }

    public void setStrAsocDescrption(String strAsocDescrption) {
        this.strAsocDescrption = strAsocDescrption;
    }
}
