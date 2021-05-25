/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2003
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:	RequestBean.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Jim Kelly
 *
 * DATE:        01-31-2002
 *
 * HISTORY:
 *	03/07/2002  psedlak Use type in FormBean constructor.
 *	10/15/2002  psedlak Change history to only put modfying userid on newly inserted record
 *			not on the updated record too.
 *
 *      11/21/2002 shussaini Change Request Navigation.(hd 200039)
 *                  Added m_strSttsCdFrom,  m_strTypInd, m_strRqstTypCd
 *                  and m_strSttsCdTo with its get Methods.
 *                  Change the sql to retrieve following columns
 *                  STTS_CD_FROM,TYP_IND, RQST_TYP_CD, ACTN from Action_T
 *	01/29/2003  psedlak Fix for checking duplicate PONs.
 *	08/05/2003  psedlak Fix to make sure we dont do multiple rollbacks
 *	08/28/2003  psedlak changes to only update what chgd, and record a history of it
 *	09/19/2003 psedlak      use generic base
 *
 *	05/2005 	EK  SLA warning enhancement, on status change, if order going to
                                        one the statuses that reset sla due date, include the sla_due_dt on query.
 *	05/2005 	EK Add dbGetSLADays( int iSqncNmbr ), fetches number SLA days this order type is required.
 */

/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/Archives/express/JAVA/Object/RequestBean.java  $
/*
/*   Rev 1.13   Jun 21 2005 08:13:12   e0069884
/*
/*
/*   Rev 1.10   Feb 17 2003 11:41:52   e0069884
/*Fix duplicate PONs
/*
/*   Rev 1.9   Dec 11 2002 15:01:26   e0069884
/*HDR 200039 Navigation Changes
/*
/*   Rev 1.8   Oct 24 2002 10:24:14   e0069884
/*
/*
/*   Rev 1.7   24 May 2002 11:13:38   dmartz
/*
/*
/*   Rev 1.6   22 May 2002 08:45:06   dmartz
/*
/*
/*   Rev 1.5   09 Apr 2002 16:06:16   dmartz
/*
/*
/*   Rev 1.4   22 Mar 2002 07:56:54   sedlak
/*
/*
/*   Rev 1.3   15 Feb 2002 10:40:44   dmartz
/*
/*
/*   Rev 1.2   13 Feb 2002 14:19:06   dmartz
/*Release 1.1
/*
/*   Rev 1.1   31 Jan 2002 07:43:12   sedlak
/*
/*
/*   Rev 1.0   23 Jan 2002 11:06:20   wwoods
/*Initial Checkin
 */

/* $Revision:   1.13  $
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import com.automation.bean.UserBean;
import com.automation.dao.LSRdao;

public class RequestBean extends ExpressBean {
    //These dictate what to pull from tables
    private RequestOrder thisOrder = RequestOrder.getInstance();
    private int m_iNmbrPonEntrd = 0;
    private int m_iNmbrPonFoc = 0;
    public RequestBean() {
        super.init(thisOrder);
        Log.write(Log.DEBUG_VERBOSE,  "RequestBean: constructor");
    }

    public int create(int i) {
        return -1;	//bogus
    }

    public int create(String strRqstPON, int iOCNSttSqncNmbr, String strSrvcTypCd, String strRqstTypCd, String strActvtyTypCd, int iCmpnySqncNmbr) {
        Log.write(Log.DEBUG_VERBOSE, "RequestBean : Create New Request");

        int iReturnCode = 0;
        int iRqstSqncNmbr = 0;
        String strPON = strRqstPON.trim();	//trims leading and trailing whitespace
        String strOCNCd = "";
        String strSttCd = "";
        //int iCmpnySqncNmbr = 0;
        String strQuery1 = "SELECT OCN_STATE_T.OCN_CD, OCN_STATE_T.STT_CD FROM OCN_STATE_T, OCN_T WHERE OCN_STT_SQNC_NMBR = " + iOCNSttSqncNmbr + " AND OCN_STATE_T.OCN_CD = OCN_T.OCN_CD AND OCN_T.CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr;
        ResultSet rs = null;

        try {
            rs = m_stmt.executeQuery(strQuery1);

            if (rs.next()) {
                strOCNCd = rs.getString("OCN_CD");
                strSttCd = rs.getString("STT_CD");
                //iCmpnySqncNmbr = rs.getInt("CMPNY_SQNC_NMBR");
                rs.close();
            } else {
                rollbackTransaction();
                DatabaseManager.releaseConnection(m_conn);
                Log.write(Log.ERROR, "RequestBean : Error finding valid OCN Code and State ");
                iReturnCode = -110;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            rollbackTransaction();
            try { rs.close(); } catch(Exception e2) {}
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "RequestBean : DB Exception on Query : " + strQuery1);
            iReturnCode = -100;
        }

        if (iReturnCode != 0) {
            return (iReturnCode);
        }

        //Duplicate PON check
        //We'll insert as Upper case - but for legacy purposes, we must consider same PONs with different Case as duplicates.
        String strQueryPON = "SELECT RQST_PON FROM REQUEST_T WHERE RQST_PON IN ('" + strPON + "', '" +
                strPON.toUpperCase() + "','" + strPON.toLowerCase() + "') AND OCN_CD = '" + strOCNCd + "'";
        Log.write(Log.DEBUG_VERBOSE, "PON DUP query= " + strQueryPON);
        try {
            rs = m_stmt.executeQuery(strQueryPON);

            if (rs.next()) {
                // we found a duplicate PON
                Log.write(Log.DEBUG_VERBOSE, "Duplicate PON caught["+strPON+"]");
                iReturnCode = DUP_PON;
            }

        } catch(SQLException e) {
            e.printStackTrace();
            rollbackTransaction();
            Log.write(Log.ERROR, "RequestBean : DB Exception on Query : " + strQuery1);
            iReturnCode = -100;
        }

        if (iReturnCode != 0) {
            try { rs.close(); } catch(Exception e) {}
            rs=null;
            DatabaseManager.releaseConnection(m_conn);
            return (iReturnCode);
        }

        String strQuery2 = "SELECT REQUEST_SEQ.nextval RQST_SQNC_NMBR_NEW FROM dual";

        try {
            rs = m_stmt.executeQuery(strQuery2);
            if (rs.next()) {
                iRqstSqncNmbr = rs.getInt("RQST_SQNC_NMBR_NEW");
                rs.close();
            } else {
                rollbackTransaction();
                DatabaseManager.releaseConnection(m_conn);
                Log.write(Log.ERROR, "RequestBean : Error getting next Sequence Number ");
                iReturnCode = -120;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            rollbackTransaction();
            try { rs.close(); } catch(Exception e2) {}
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "RequestBean : DB Exception on Query : " + strQuery2);
            iReturnCode = -100;
        }

        if (iReturnCode != 0) {
            return (iReturnCode);
        }

        String strInsert1 = "";

        strPON = strPON.toUpperCase();	//We'll INSERT with UPPERS for now on

        try {
            //start new Added code for Q & V by kumar
              strInsert1 = "INSERT INTO REQUEST_T VALUES(" + iRqstSqncNmbr + ", '" + Toolkit.replaceSingleQwithDoubleQ(strPON) + "', 'INITIAL', 0, 0, " + PropertiesManager.getIntegerProperty("lsr.lsog.vrsn") + ", '" + strOCNCd + "', '" + strSttCd + "', " + iOCNSttSqncNmbr + ", " + iCmpnySqncNmbr + ", '" + strSrvcTypCd + "', '" + strRqstTypCd + "', '" + strActvtyTypCd + "', ' ', ' ', '" +  getUserid() + "', ' ', ' ', ' ', ' ', ' '," + getTimeStamp() + ", '" +  getUserid() +
                    "','','', '','INITIAL','','','','','','','','','','N','')" ;

              //end new Added code for Q & V by kumar
            m_stmt.executeUpdate(strInsert1);
        } catch(SQLException se) {
            rollbackTransaction();
            try { rs.close(); } catch(Exception e2) {}
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "RequestBean : DB Exception on Query : " + strInsert1);
            Log.write(Log.ERROR, "se=["+se+"]");
            iReturnCode = -100;
        } catch(Exception e) {
            e.printStackTrace();
            rollbackTransaction();
            try { rs.close(); } catch(Exception e2) {}
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "RequestBean : DB Exception on Insert : " + strInsert1);
            iReturnCode = -100;
        }

        if (iReturnCode != 0) {
            return (iReturnCode);
        }

        Log.write(Log.DEBUG_VERBOSE, "RequestBean : Successful Insert of New Request");

        // generate a new History record
        int iRqstHstrySqncNmbr = updateHistory(iRqstSqncNmbr, 0, "INITIAL");
        if (iRqstHstrySqncNmbr == 0) {
            Log.write(Log.ERROR, "RequestBean : Error Generating History for Request Sqnc Nmbr:" + iRqstSqncNmbr);
            iReturnCode = -125;
        }

        String strUpdate1 = "UPDATE REQUEST_T SET RQST_HSTRY_SQNC_NMBR = " + iRqstHstrySqncNmbr + " WHERE RQST_SQNC_NMBR = " + iRqstSqncNmbr;

        try {
            if (m_stmt.executeUpdate(strUpdate1) <= 0) {
                throw new SQLException();
            }
        } catch(SQLException e) {
            e.printStackTrace();
            rollbackTransaction();
            try { rs.close(); } catch(Exception e2) {}
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "RequestBean : DB Exception on Update : " + strUpdate1);
            iReturnCode = -100;
        }

        if (iReturnCode != 0) {
            return (iReturnCode);
        }

        Log.write(Log.DEBUG_VERBOSE, "RequestBean : REQUEST_T updated with current History Sequence Number : " + strUpdate1);


        // if we got here, we have a new Request Sequence Number
        // now get the information we need to create all the required forms.
        // We need to loop through SERVICE_TYPE_FORM and create all the INITIAL Version 0 FORMs

        String strQuery3 = "SELECT * FROM SERVICE_TYPE_FORM_T WHERE SRVC_TYP_CD = '" + strSrvcTypCd + "' AND TYP_IND = 'R'";
        int i_frms = 0;
        int i_frms_created = 0;
        int iFrmSqncNmbr = 0;
        boolean bFormCreated = false;

        try {
            rs = m_stmt.executeQuery(strQuery3);

            while (rs.next()) {
                i_frms++;

                iFrmSqncNmbr = rs.getInt("FRM_SQNC_NMBR");

                bFormCreated = getFormBean().generateNewForm(iFrmSqncNmbr, iRqstSqncNmbr, 0);

                if (bFormCreated) {
                    i_frms_created++;
                } else {
                    Log.write(Log.ERROR, "RequestBean : Error Generating Form for Request Sqnc Nmbr:" + iRqstSqncNmbr + " ; Form Sqnc Nmbr = " + iFrmSqncNmbr);
                    iReturnCode = -130;
                }

            }
            if ((i_frms_created == 0) || (i_frms_created != i_frms)) {
                Log.write(Log.ERROR, "RequestBean : Error Generating Forms for Request Sqnc Nmbr:" + iRqstSqncNmbr);
                iReturnCode = -135;
            }

            rs.close();
        } catch(SQLException e) {
            e.printStackTrace();
            rollbackTransaction();
            //try { rs.close(); } catch(Exception e2) {}
            //DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "RequestBean :  ERROR PERFORMING DATABASE ACTIVITY FOR NEW REQUEST FORM CREATION ");
            iReturnCode = -100;
        }

        if (iReturnCode != 0) {
            try { rs.close(); } catch(Exception e2) {}
            DatabaseManager.releaseConnection(m_conn);
            return (iReturnCode);
        }

        Log.write(Log.DEBUG_VERBOSE, "RequestBean : All INITIAL Forms Generated for Request Sqnc Nmbr:" + iRqstSqncNmbr);

        // return the new Request Sequence Number
        return(iRqstSqncNmbr);

    }

    public int changeStatus(AlltelRequest request, int iSqncNmbr, String strActn, int iCurrentVrsn) {

        int iReturnCode = 0;
        int iHistorySequenceNumber = 0;
        int iVrsn = iCurrentVrsn;

        Log.write(Log.DEBUG_VERBOSE,"RequestBean.changeStatus() --- ");
        // VALIDATE SECURITY HERE
        if ( ! hasAccessTo(iSqncNmbr) ) {
            return (SECURITY_ERROR);
        }

        String strMdfdDt = "";

        // Get the Status Code we need to change the <order> to based on the Action Code we recieved.
        // Also, get the Current Version and the Indicator that will tell us if we need a new version.

        String strQuery1 = "SELECT A.STTS_CD_TO, A.STTS_CD_FROM, A.TYP_IND, A.RQST_TYP_CD, "+
                " A.ACTN, A.ACTN_VRSN_IND, A.ACTN_SND_CUST_RPLY, A.ACTN_SND_PROV_RPLY, B." +
                thisOrder.getAttribute("VRSN_COLUMN") +
           " AS THE_VRSN, B.LST_MDFD_CSTMR, A.INN_STTS_CD FROM " + thisOrder.getTBL_NAME() + " B, ACTION_T  A " +
                " WHERE B." + thisOrder.getSQNC_COLUMN() + " = " + iSqncNmbr +
                " AND B.RQST_TYP_CD=A.RQST_TYP_CD " +			//special for RequestOrders
                " AND A.STTS_CD_FROM = B." + thisOrder.getAttribute("STTS_COLUMN") +
                " AND A.TYP_IND = '" + thisOrder.getTYP_IND() + "' AND A.ACTN = '" + strActn + "'";

        String strSttsCd = "";
        String strActnVrsnInd = "";
        boolean bSendEmail = false;
        boolean bSendProvEmail = false;
        String strEmailRcpt = "";
        String strInnSttsCd = "";

        try {
            Log.write(Log.DEBUG_VERBOSE, "RequestBean.changeStatus(): strQuery1=["+strQuery1+"]");
            ResultSet rs1 = m_stmt.executeQuery(strQuery1);
            if (rs1.next()) {
                strSttsCd = rs1.getString("STTS_CD_TO");
                iVrsn = rs1.getInt("THE_VRSN");
                strEmailRcpt = rs1.getString("LST_MDFD_CSTMR");
                setSttsCdTo( strSttsCd );
                setSttsCdFrom( rs1.getString("STTS_CD_FROM") );
                //m_strTypInd = rs1.getString("TYP_IND");
                setRqstTypCd( rs1.getString("RQST_TYP_CD") );
                strActnVrsnInd = rs1.getString("ACTN_VRSN_IND");
                String strSendReply = rs1.getString("ACTN_SND_CUST_RPLY");
                strInnSttsCd = rs1.getString("INN_STTS_CD");
                if (strSendReply.toUpperCase().equals("Y")) {
                    bSendEmail = true;
                }
                String strSendProvReply = rs1.getString("ACTN_SND_PROV_RPLY");
                if (strSendProvReply.toUpperCase().equals("Y")) {
                    bSendProvEmail = true;
                }
                rs1.close();
                rs1=null;
            } else {
                rollbackTransaction();
                DatabaseManager.releaseConnection(m_conn);
                Log.write(Log.DEBUG_VERBOSE, "RequestBean.changeStatus(): Action Selected is not Allowed ");
                iReturnCode = -155;
            }
        } catch(SQLException e) {
            Log.write(Log.ERROR, "RequestBean.changeStatus(): SQLException e=[" + e + "]");
            e.printStackTrace();
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "RequestBean : DB Exception on Query : " + strQuery1);
            iReturnCode = DB_ERROR;
        }

        if (iReturnCode != 0) {
            return (iReturnCode);
        }
        setNewVersion(iVrsn);

        int iFldVldtnErrs = validateFields(request, iSqncNmbr, iVrsn, "V", strSttsCd);
        if (iFldVldtnErrs != 0) {
            Log.write(Log.DEBUG_VERBOSE,"RequestBean.changeStatus(): Cannot perform status chg becuz validation errors.");
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            iReturnCode = VALIDATION_ERROR;
            return (iReturnCode);
        }

        // See if this status change requires us to create a new version of the order
        if (strActnVrsnInd.equals("Y")) {
            Log.write(Log.DEBUG_VERBOSE,"RequestBean.changeStatus(): Curr Vers="+ iVrsn+" New Version REQD");
            iVrsn = createVersion(iSqncNmbr, iVrsn);
            if (iVrsn <= 0) {
                Log.write(Log.ERROR, "RequestBean.changeStatus(): Error Generating new Version for :" + iSqncNmbr);
                iReturnCode = iVrsn;
            }
            if (iReturnCode != 0) {
                return (iReturnCode);
            }
            Log.write(Log.DEBUG_VERBOSE,"RequestBean.changeStatus(): New Version ="+iVrsn);
            setNewVersion(iVrsn);
        }

        // generate a new History record
        iHistorySequenceNumber = updateHistory(iSqncNmbr, iVrsn, strSttsCd);
        if (iHistorySequenceNumber <= 0) {
            Log.write(Log.ERROR, "RequestBean.changeStatus(): Error Generating History for Sqnc Nmbr:" + iSqncNmbr);
            iReturnCode = -165;
        }
        if (iReturnCode != 0) {
            return (iReturnCode);
        }
		// Added for Supplemental changing internal status to SUPP-- Start
        if ( iCurrentVrsn != getNewVersion() && (getRqstTypCd().equals("B") || getRqstTypCd().equals("S")) ) {
            // make sure the Request Type Code gets updated to reflect this is a SUPP
            // this should actually be replaced with the SUP code from the LSR.
            // just default it to '3' for now.
            Log.write(Log.DEBUG_VERBOSE,"RequestBean.changeStatus() set RqstTypCd to B3");
            if(getRqstTypCd().equals("B")){
              setRqstTypCd("B3");
            }
            //Added to update the Internal status as SUPP for Supplemental requests.
            if(strActn!=null){
            if(strActn.equalsIgnoreCase("Supp")||strActn.equalsIgnoreCase("Reset to SUPP")){
                 Log.write(Log.INFO,"RequestBean.changeStatus() set Internal status to SUPP-SATISH");
                strInnSttsCd="SUPP";
            }
            }
        }

        // Added for Supplemental changing status to INITAIL-- end

        String strUpdate1 = "UPDATE " + thisOrder.getTBL_NAME() + " SET " + thisOrder.getAttribute("STTS_COLUMN") + " = '" +
                strSttsCd + "', " + thisOrder.getAttribute("HSTRY_COLUMN") + " = " +
                iHistorySequenceNumber + ", RQST_VRSN=" + iVrsn + ", RQST_TYP_CD = '" + getRqstTypCd() + "', MDFD_DT = " +
                getTimeStamp() + ", MDFD_USERID = '" + getUserid() + "' ";

        Vector vSTTSes = null;

        try{

            vSTTSes = SLATools.getSLAStatuses( m_conn, thisOrder.getTYP_IND()  );
            if( vSTTSes != null ){
                if( vSTTSes.indexOf( strSttsCd ) > -1  ){
                    String strSLAStart = SLATools.getSLAStartDateTime( ( ExpressUtil.getCurrentDateYYYYMMDDD_HH24MMSS()).substring(0,8),
                            ( ExpressUtil.getCurrentDateYYYYMMDDD_HH24MMSS()).substring(9,15)) ;

                    strUpdate1 += ", SLA_DUE_DT = to_date( ' " + SLATools.getSLAExpectedEndDateTime( strSLAStart,  dbGetSLADays( iSqncNmbr ) )
                    + "', 'YYYYMMDD HH24MISS' ) ";
                }
            }

            if (getCmpnyTyp().equals("P")) {
                strUpdate1 += ", LST_MDFD_PRVDR = '" + getUserid() + "' ";
            } else {
                strUpdate1 += ", LST_MDFD_CSTMR = '" + getUserid() + "' ";
            }
      strUpdate1 += ", INN_STTS = '" + strInnSttsCd + "' WHERE " + thisOrder.getSQNC_COLUMN() + " = " + iSqncNmbr;
            Log.write(Log.DEBUG_VERBOSE, "RequestBean.changeStatus():strUpdate=["+strUpdate1+"]");
            if (m_stmt.executeUpdate(strUpdate1) <= 0) {
                throw new SQLException();
            }
        } catch(SQLException e) {
            e.printStackTrace();
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "RequestBean.changeStatus(): DB Exception on Update : " + strUpdate1);
            iReturnCode = DB_ERROR;
        } catch(Exception e) {
            e.printStackTrace();
            rollbackTransaction();
            Log.write(Log.ERROR, "RequestBean.changeStatus(): DB Exception on Update : " + strUpdate1);
        }


        if (iReturnCode != 0) {
            return (iReturnCode);
        }

        if (((getRqstTypCd().equals("B")) || (getRqstTypCd().equals("B1")) || (getRqstTypCd().equals("B2")) || (getRqstTypCd().equals("B3")) || (getRqstTypCd().equals("S"))) && (strActn.equals("Open for Review"))) {

			String strUpdateQry = "UPDATE SLA_TIMER_QUEUE_T SET INTERNAL_STATUS = 'MANUAL-REVIEW', MDFD_DT = " +
			                       getTimeStamp() + ", MDFD_USERID = '" + getUserid() + "'" +
			                       " WHERE RQST_SQNC_NMBR = " + iSqncNmbr +
			                       " AND RQST_VRSN = " + iCurrentVrsn;
	        try {

		      /*commented by kumar k     if(m_stmt.executeUpdate(strUpdateQry) <= 0) {
		              throw new SQLException();
		           }
                       */
                    // added by kumar k
                       m_stmt.executeUpdate(strUpdateQry);

		    } catch(SQLException e) {
		        e.printStackTrace();
		        rollbackTransaction();
		        DatabaseManager.releaseConnection(m_conn);
		        Log.write(Log.ERROR, "RequestBean.changeStatus(): DB Exception on Update : " + strUpdateQry);
		        iReturnCode = DB_ERROR;
		    } catch(Exception e) {
			    e.printStackTrace();
			    rollbackTransaction();
			    Log.write(Log.ERROR, "RequestBean.changeStatus(): DB Exception on Update : " + strUpdateQry);
			}

			if (iReturnCode != 0) {
			    return (iReturnCode);
            }
	     }

        String strNotes="";
        Log.write(Log.DEBUG_VERBOSE,"RequestBean.changeStatus() Rqst="+ getRqstTypCd()+" stts = " + getSttsCdTo());
        // Populate 'NOTES' field if RqstTypCd = "M" and changing status to "FOC"
        if (getRqstTypCd().equals("M") && getSttsCdTo().equals("FOC")) {
            Log.write(Log.DEBUG_VERBOSE,"RequestBean.changeStatus() Multiorder calc");
            retrieveMultiStats(201, iSqncNmbr, iVrsn);
            strNotes = " NOTES= '" + m_iNmbrPonFoc + "/" + m_iNmbrPonEntrd + " FOC '";
            strUpdate1 = "UPDATE REQUEST_T SET " +
                    strNotes + " WHERE RQST_SQNC_NMBR = " + iSqncNmbr;
            Log.write(Log.DEBUG_VERBOSE,"RequestBean.changeStatus() Update="+strUpdate1);
            try {
                if (m_stmt.executeUpdate(strUpdate1) <= 0) {
                    throw new SQLException();
                }
            } catch(SQLException e) {
                e.printStackTrace();
                rollbackTransaction();
                //DatabaseManager.releaseConnection(m_conn);
                Log.write(Log.ERROR, "RequestBean.changeStatus(): DB Exception on Update : " + strUpdate1);
                iReturnCode = DB_ERROR;
            }
        }

        if (iReturnCode != 0) {
            return (iReturnCode);
        }

        if (bSendEmail) {
            this.sendReply(iSqncNmbr, iVrsn, strEmailRcpt);
        }

        if (bSendProvEmail) {
            this.sendProvReply(iSqncNmbr, iVrsn);
        }

        //if we got here, we had a successful Status Change and Generated a History Record.
        // Return the History Sequence Number

        return (iHistorySequenceNumber);
    }

    // Send the autoReply if necessary
    protected void sendReply(int iSqncNmbr, int iVrsn, String strUserID) {
        String strQuery1 = "SELECT RQST_PON, DUE_DT, RQST_STTS_CD, NOTES, OCN_CD, S.SRVC_TYP_DSCRPTN, A.ACTVTY_TYP_DSCRPTN, " +
                " CUST, AN, ATN, SRVC_RDR_NBR, TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI') AS THE_TIME " +
                " FROM REQUEST_T R, ACTIVITY_TYPE_T A, SERVICE_TYPE_T S WHERE RQST_SQNC_NMBR= " + iSqncNmbr +
                " AND RQST_VRSN =" + iVrsn + " AND R.ACTVTY_TYP_CD = A.ACTVTY_TYP_CD AND A.TYP_IND='" + thisOrder.getTYP_IND() +
                "' AND R.SRVC_TYP_CD = S.SRVC_TYP_CD AND S.TYP_IND = '" + thisOrder.getTYP_IND() + "' ";
        ResultSet rs = null;
        String strSubject = "";
        String strMsg = "";
        try {
            rs = m_stmt.executeQuery(strQuery1);
            if (rs.next()) {
                // Build the Subject
                strSubject = "Express Order Status Change for PON:  " + rs.getString("RQST_PON");

                // Build the Message
                strMsg =   "PON           :  " + rs.getString("RQST_PON") + "\n";
                strMsg = strMsg + "Due Date      :  " + rs.getString("DUE_DT") + "\n";
                strMsg = strMsg + "New Status    :  " + rs.getString("RQST_STTS_CD") + "\n";
                strMsg = strMsg + "Notes         :  " + rs.getString("NOTES") + "\n";
                strMsg = strMsg + "OCN Code      :  " + rs.getString("OCN_CD") + "\n";
                strMsg = strMsg + "Service Type  :  " + rs.getString("SRVC_TYP_DSCRPTN") + "\n";
                strMsg = strMsg + "Activity Type :  " + rs.getString("ACTVTY_TYP_DSCRPTN") + "\n";
                strMsg = strMsg + "Date/Time     :  " + rs.getString("THE_TIME") + "\n";
                strMsg = strMsg + "Customer Name :  " + rs.getString("CUST") + "\n";
                strMsg = strMsg + "AN            :  " + rs.getString("AN") + "\n";
                strMsg = strMsg + "ATN           :  " + rs.getString("ATN") + "\n";
                strMsg = strMsg + "Service Order :  " + rs.getString("SRVC_RDR_NBR") + "\n\n";
                rs.close();
            } else {
                //DatabaseManager.releaseConnection(m_conn);
                Log.write(Log.ERROR, "RequestBean.sendReply(): Error getting email info");
                return;
            }
        } catch(SQLException e) {
            e.printStackTrace();
            //rollbackTransaction();
            try { rs.close(); } catch(Exception e2) {}
            //DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "RequestBean.sendReply(): DB Exception on Query : " + strQuery1);
            return;
        }

        Log.write(Log.DEBUG_VERBOSE,"RequestBean.sendReply(): email to " + strUserID + " about to be sent for " + iSqncNmbr);
        if (Toolkit.autoReply(strUserID, strSubject, strMsg) != true)
            Log.write(Log.ERROR, "RequestBean.sendReply(): AutoReply failed.");

        return;
    }

    // Send the provider autoReply if necessary
    protected void sendProvReply(int iSqncNmbr, int iVrsn) {
    }

    private void retrieveMultiStats(int iFrmSqncNmbr, int iRqstSqncNmbr, int iRqstVrsn) {
        Log.write(Log.DEBUG_VERBOSE, "RequestBean --- Retrieve stats for NOTES field for Multi Form seq="+iRqstSqncNmbr
                + " ver"+ iRqstVrsn + " form="+ iFrmSqncNmbr);

        Vector m_vFrmFld = new Vector();

        try {
            m_vFrmFld = getFormBean().getFormFields(iFrmSqncNmbr, iRqstSqncNmbr, iRqstVrsn);
        }
        //	catch(SQLException e)
        catch(Exception e) {
            e.printStackTrace();
            Log.write(Log.ERROR, "RequestBean : DB Exception in getFormBean().getFormFields().");
            return;
        }

        FormField ff;
        int ff_idx;
        int iNmbrPonRej = 0;

        m_iNmbrPonFoc = m_iNmbrPonEntrd = 0;

        //loop through the vector
        for(ff_idx = 0; ff_idx < m_vFrmFld.size(); ff_idx++) {
            ff = (FormField)m_vFrmFld.elementAt(ff_idx);

            // Query section 2 to determine how many Pons were entered and how many were rejected
            if (ff.getFrmSctnSqncNmbr() == 2) {
                String strFldData = ff.getFieldData();
                if (ff.getFrmFldNmbr().equals("EU_1") && ((strFldData != null) && (strFldData.length() > 0))) {
                    m_iNmbrPonEntrd++;
                }

                if (ff.getFrmFldNmbr().equals("REJ") && ((strFldData != null) && (strFldData.equals("Y")))) {
                    iNmbrPonRej++;
                }
            }
        }

        // Get number FOC'd
        m_iNmbrPonFoc = m_iNmbrPonEntrd - iNmbrPonRej;

        return;
    }


        /*
         * EK 5/31/2005
         * This function gets SLA days as stored in db table per order order.
         * @note:  exceptions handled by caller.
         * @PARAM iSqncNmbr RQST_SQNC_NMBR
         */

    public int dbGetSLADays( int iSqncNmbr )  throws Exception {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int iSLADays = 2;
        String strQuery = " select os.OCN_STT_SLA_DYS FROM OCN_STATE_T os, "
                +	" REquest_t rq where "
                +	" os.OCN_CD = rq.OCN_CD "
                +	" AND os.STT_CD = rq.OCN_STT "
                +	" AND rq.RQST_SQNC_NMBR  = ? ";
        String strRsts = "";
        pstmt = m_conn.prepareStatement( strQuery );
        pstmt.clearParameters();
        pstmt.setInt( 1, iSqncNmbr  );
        rs = pstmt.executeQuery();
        if( rs.next() ) {
            iSLADays = rs.getInt( 1 );
        }
        Log.write(Log.DEBUG_VERBOSE, "dbGetSLADays:\n" + strQuery  + "\n"  );
        rs.close(); rs= null;
        pstmt.close(); pstmt= null;
        return iSLADays;
    }

}
