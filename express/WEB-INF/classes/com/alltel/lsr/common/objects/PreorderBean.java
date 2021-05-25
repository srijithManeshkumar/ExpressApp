/*
 * NOTICE:
 *      THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *      SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *      USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *            COPYRIGHT (C) 2003
 *               BY
 *            ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:   PreorderBean.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Dan Martz
 *
 * DATE:        04-01-2002
 *
 * HISTORY:
 *
 *	02/05/2008 Steve Korchnak   HD0000002472840 Changed features retrieval to get all features including non-regulated
 *
 *   04/19/2002  psedlak   Added methods to automatically process Preorders
 *   06/03/2002  psedlak    Instead of excluding features/services based on FEATURE_EXCLUSION, chgd
 *         to include based on FEATURE_INCLUSION_T -there is fewer from this list,so it
 *         will hopefully reduce tbl maintenance.
 *   07/16/2002  psedlak     hd 65809 - Address validation -only set WSOPI if TN still active.
 *                              hd 75092 - Replace single quotes w/ dbl before db update/insert.ADD
 *   08/16/2002  psedlak     hd 65809 - Address validation -only set WSOPI if TN still active -this
 *      was never fully tested, so it didnt go in on 7/16 as stated above.
 *   08/20/2002  psedlak     hd 103385 - Number format exception in getMSAG().
 *            Cleaned up getMSAG() and added more queries to get results.
 *   09/12/2002  psedlk   RIS21998 EDBC driver change -added length checks on numeric fields, and
 *            also set WSOPI='N' is no service at address.
 *   10/15/2002 psedlak   HDR 165254 - history update not changing userid on existing record.
 *   10/31/2002 psedlak   HDR 107341 - serv addr trait query changed
 *   11/21/2002 psedlak   Added DB2 connection check and response '014'.
 *      11/21/2002 shussaini Change Request Navigation.(hd 200039)
 *                  Added m_strSttsCdFrom,  m_strTypInd, m_strRqstTypCd
 *                  and m_strSttsCdTo with its get Methods.
 *                  Change the sql to retrieve following columns
 *                  STTS_CD_FROM,TYP_IND, RQST_TYP_CD, ACTN from Action_T
 *   01/09/2003 psedlak   HDR 253552 -Allow CLEC to view own customers via CSI Requests.
 *            HD 256946 -Move CLEC check before service address lookup in doCSI() method.
 *   03/24/2003 psedlak   HD 299878 not all features are being displayed
 *   05/02/2003 psedlak   HD 351542 If fictitious info returned, requery for a similar non-fict acct.
 *            HD 377912 Add STOP_DATE check for CLEC lookup.
 *   08/13/2003 psedlak   HD 518995/517827 to trap Datawarehouse deadlocks and respond with '014'
 *   09/19/2003 psedlak   use generic base
 *   11/19/2003 psedlak   Fix for HD 649271 - No LPIC trait in data warehouse
 *   03/11/2005 psedlak   Fixed single quote response from MSAG
 *   04/06/2005 psedlak   Added criteria for CSI's on UNE-P Accounts, and fixed some ResultSet close's
 *  12/06/2006 Steve Korchnak    HD0000002067789 Added 'FRGN' to 'FICT' as list of exchanges belonging to
 *                              data to be ignored.
 *  12/08/2006 Steve Korchnak   HD0000002250687 Changed lookup for address information so that the ORG_STATE
 *                              will be used for state criteria rather than STATE_CODE.
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import com.automation.dao.LSRdao;

public class PreorderBean extends ExpressBean
{
   //These dictate what to pull from tables
        private PreorderOrder thisOrder = PreorderOrder.getInstance();

   private Random m_random;   //needed for PIC lists

   private int m_iNextForm;   //If auto process Preorder, this is the next form to show

   private static final String AUTOMATIC_RESPONSE = "Automatic";
   private static final String CAMS_DW_UNAVAILABLE = "UNSUCCESSFUL EXECUTION CAUSED BY AN UNAVAILABLE RESOURCE";
   private static final String CAMS_DW_INUSE = "ROLLED BACK DUE TO DEADLOCK OR TIMEOUT";
   private static final int DB2_UNAVAILABLE = -900;
   private static final int MISC_DB_ERROR = -901;
   
   //MASAG TABLE RANGE
   private static final String LOW_RANGE = "LOW_RANGE";
   private static final String HIGH_RANGE = "HIGH_RANGE";
   private static final String STREET = "STREET";
   private static final String COUNT = "COUNT";
   private static final String RANGE_EXISTS = "RANGE_EXISTS";

   public PreorderBean()
   {
      super.init(thisOrder);
                Log.write(Log.DEBUG_VERBOSE, "PreorderBean: constructor");
      this.m_random = new Random (System.currentTimeMillis());
      this.m_iNextForm = 0;
   }

   public int getNextForm() {
      return this.m_iNextForm;
   }

   public int create(int i)
   {
      return -1;   //dummy
   }

   public int create(int iOCNSttSqncNmbr, String strSrvcTypCd, String strActvtyTypCd, int iCmpnySqncNmbr)
   {
      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Create New Preorder");

      int iReturnCode = 0;

      int iPreSqncNmbr = 0;
      String strOCNCd = "";
      String strSttCd = "";
      //int iCmpnySqncNmbr = 0;
      String strQuery1 = "SELECT OCN_STATE_T.OCN_CD, OCN_STATE_T.STT_CD, OCN_T.CMPNY_SQNC_NMBR FROM OCN_STATE_T, OCN_T WHERE OCN_STT_SQNC_NMBR = " + iOCNSttSqncNmbr + " AND OCN_STATE_T.OCN_CD = OCN_T.OCN_CD AND OCN_T.CMPNY_SQNC_NMBR = "+ iCmpnySqncNmbr;

      setSrvcTypCd( strSrvcTypCd );

      //If the preorder is for loop qual, just return gracefully - this Preorder just links the user to the
      //Targus website.
      if ( strSrvcTypCd.equals("H") )
      {
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Loop Qual preorder return OK");
         return 1;
      }

      try
      {
         ResultSet rs1 = m_stmt.executeQuery(strQuery1);

         if (rs1.next())
         {
            strOCNCd = rs1.getString("OCN_CD");
            strSttCd = rs1.getString("STT_CD");
            //iCmpnySqncNmbr = rs1.getInt("CMPNY_SQNC_NMBR");
            rs1.close();
         }
         else
         {
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "PreorderBean : Error finding valid OCN Code and State ");
            iReturnCode = -110;
         }
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean : DB Exception on Query : " + strQuery1);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      // Get next Preorder Sqnc Nmbr
      String strQuery2 = "SELECT PREORDER_SEQ.nextval PRE_ORDR_SQNC_NMBR_NEW FROM dual";

      try
      {
         ResultSet rs2 = m_stmt.executeQuery(strQuery2);
         if (rs2.next())
         {
            iPreSqncNmbr = rs2.getInt("PRE_ORDR_SQNC_NMBR_NEW");
            rs2.close();
         }
         else
         {
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "PreorderBean : Error getting next Sequence Number ");
            iReturnCode = -120;
         }
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean : DB Exception on Query : " + strQuery2);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      String strInsert1 = "";

      try
      {
         strInsert1 = "INSERT INTO PREORDER_T VALUES(" + iPreSqncNmbr + ", 'INITIAL', 0, 0, " + PropertiesManager.getIntegerProperty("lsr.lsog.vrsn") + ", '" + strOCNCd + "', '" + strSttCd + "', " + iOCNSttSqncNmbr + ", " + iCmpnySqncNmbr + ", '" + strSrvcTypCd + "', '" + strActvtyTypCd + "', ' ', '" + getUserid() + "', ' ', ' ', ' ', " + getTimeStamp() + ", '" + getUserid() + "','N')" ;
         m_stmt.executeUpdate(strInsert1);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean : DB Exception on Insert : " + strInsert1);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Successful Insert of New Preorder");

      // generate a new History record
      int iPreHstrySqncNmbr = updateHistory(iPreSqncNmbr, 0, "INITIAL");
      if (iPreHstrySqncNmbr == 0)
      {
            Log.write(Log.ERROR, "PreorderBean : Error Generating History for Preorder Sqnc Nmbr:" + iPreSqncNmbr);
            iReturnCode = -125;
      }

      String strUpdate1 = "UPDATE PREORDER_T SET PRE_ORDR_HSTRY_SQNC_NMBR = " + iPreHstrySqncNmbr + " WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr;

      try
      {
         if (m_stmt.executeUpdate(strUpdate1) <= 0)
         {
            throw new SQLException();
         }
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean : DB Exception on Update : " + strUpdate1);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : PREORDER_T updated with current History Sequence Number : " + strUpdate1);

      // if we got here, we have a new Preorder Sequence Number
      // now get the information we need to create all the required forms.
      // We need to loop through SERVICE_TYPE_FORM and create all the INITIAL Version 0 FORMs

      String strQuery3 = "SELECT FRM_SQNC_NMBR FROM SERVICE_TYPE_FORM_T WHERE SRVC_TYP_CD = '" + strSrvcTypCd + "' AND TYP_IND = 'P'";
      int i_frms = 0;
      int i_frms_created = 0;
      int iFrmSqncNmbr = 0;
      boolean bFormCreated = false;

      try
      {
         ResultSet rs3 = m_stmt.executeQuery(strQuery3);

         while (rs3.next())
         {
            i_frms++;

            iFrmSqncNmbr = rs3.getInt("FRM_SQNC_NMBR");

            bFormCreated = getFormBean().generateNewForm(iFrmSqncNmbr, iPreSqncNmbr, 0);

            if (bFormCreated)
            {
               i_frms_created++;
            }
            else
            {
               Log.write(Log.ERROR, "PreorderBean : Error Generating Form for Preorder Sqnc Nmbr:" + iPreSqncNmbr + " ; Form Sqnc Nmbr = " + iFrmSqncNmbr);
               iReturnCode = -130;
            }

         }
         if ((i_frms_created == 0) || (i_frms_created != i_frms))
         {
            Log.write(Log.ERROR, "PreorderBean : Error Generating Forms for Preorder Sqnc Nmbr:" + iPreSqncNmbr);
            iReturnCode = -135;
         }

         rs3.close();
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean :  ERROR PERFORMING DATABASE ACTIVITY FOR NEW PREORDER FORM CREATION ");
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : All INITIAL Forms Generated for Preorder Sqnc Nmbr:" + iPreSqncNmbr);

      // return the new Preorder Sequence Number
      return(iPreSqncNmbr);
   }

        //Return value refers to the History Seq Numbr that was created
   public int changeStatus(AlltelRequest request, int iPreSqncNmbr, String strPreActn, int iFrmSqncNmbr)
   {
      int iReturnCode = 0;

      // VALIDATE SECURITY HERE
      if ( ! hasAccessTo(iPreSqncNmbr) )
      {
         return (SECURITY_ERROR);
      }

      String strMdfdDt = "";

      // Get the Status Code we need to change the Preorder to based on the Action Code we recieved.
      // Also, get the Current Version and the Indicator that will tell us if we need a new version.
      String strQuery1 = "";
      if (iFrmSqncNmbr > 0)
      {
                    strQuery1 = "SELECT ACTION_T.STTS_CD_TO, ACTION_T.STTS_CD_FROM, ACTION_T.TYP_IND, ACTION_T.RQST_TYP_CD, ACTION_T.ACTN, ACTN_VRSN_IND, ACTN_SND_CUST_RPLY, PRE_ORDR_VRSN, ACTVTY_TYP_DSCRPTN, LST_MDFD_CSTMR, AUTO_RSPNS_FRM FROM PREORDER_T, ACTION_T, ACTIVITY_TYPE_T, SERVICE_TYPE_T, SERVICE_TYPE_FORM_T WHERE PREORDER_T.SRVC_TYP_CD = SERVICE_TYPE_T.SRVC_TYP_CD AND PREORDER_T.ACTVTY_TYP_CD = ACTIVITY_TYPE_T.ACTVTY_TYP_CD AND SERVICE_TYPE_T.SRVC_TYP_CD = SERVICE_TYPE_FORM_T.SRVC_TYP_CD AND PREORDER_T.PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND ACTION_T.STTS_CD_FROM = PREORDER_T.PRE_ORDR_STTS_CD AND SERVICE_TYPE_T.TYP_IND = 'P' AND ACTION_T.TYP_IND = 'P' AND ACTIVITY_TYPE_T.TYP_IND = 'P' AND SERVICE_TYPE_FORM_T.TYP_IND = 'P' AND ACTION_T.ACTN = '" + strPreActn + "' AND FRM_SQNC_NMBR = " + iFrmSqncNmbr;
      }
      else
      {
                    strQuery1 = "SELECT ACTION_T.STTS_CD_TO, ACTION_T.STTS_CD_FROM, ACTION_T.TYP_IND, ACTION_T.RQST_TYP_CD, ACTION_T.ACTN, ACTN_VRSN_IND, ACTN_SND_CUST_RPLY, PRE_ORDR_VRSN, ACTVTY_TYP_DSCRPTN, LST_MDFD_CSTMR, AUTO_RSPNS_FRM FROM PREORDER_T, ACTION_T, ACTIVITY_TYPE_T, SERVICE_TYPE_T, SERVICE_TYPE_FORM_T WHERE PREORDER_T.SRVC_TYP_CD = SERVICE_TYPE_T.SRVC_TYP_CD AND PREORDER_T.ACTVTY_TYP_CD = ACTIVITY_TYPE_T.ACTVTY_TYP_CD AND SERVICE_TYPE_T.SRVC_TYP_CD = SERVICE_TYPE_FORM_T.SRVC_TYP_CD AND PREORDER_T.PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND ACTION_T.STTS_CD_FROM = PREORDER_T.PRE_ORDR_STTS_CD AND SERVICE_TYPE_T.TYP_IND = 'P' AND ACTION_T.TYP_IND = 'P' AND ACTIVITY_TYPE_T.TYP_IND = 'P' AND SERVICE_TYPE_FORM_T.TYP_IND = 'P' AND ACTION_T.ACTN = '" + strPreActn + "'";
      }


      String strSttsCd = "";
      String strPreActnVrsnInd = "";
      int iPreVrsn = 0;
      String strActvtyTypCd = "";
      String strAutoRspnsFrm = "";
      boolean bSendEmail = false;
      String strEmailRcpt = "";

      try
      {
         ResultSet rs1 = m_stmt.executeQuery(strQuery1);

         if (rs1.next())
         {
            strSttsCd = rs1.getString("STTS_CD_TO");

                                setSttsCdTo(strSttsCd);
                                setSttsCdFrom( rs1.getString("STTS_CD_FROM") );
                                //setTypInd ( rs1.getString("TYP_IND") );
                                setRqstTypCd( rs1.getString("RQST_TYP_CD") );

            strPreActnVrsnInd = rs1.getString("ACTN_VRSN_IND");
            iPreVrsn = rs1.getInt("PRE_ORDR_VRSN");
            strActvtyTypCd = rs1.getString("ACTVTY_TYP_DSCRPTN");
            strAutoRspnsFrm = rs1.getString("AUTO_RSPNS_FRM");
            strEmailRcpt = rs1.getString("LST_MDFD_CSTMR");
            String strSendReply = rs1.getString("ACTN_SND_CUST_RPLY");
            if (strSendReply.toUpperCase().equals("Y")) {
                                        bSendEmail = true;
                                }

            rs1.close();
            rs1=null;
         }
         else
         {
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Action Selected is not Allowed ");
            iReturnCode = -155;
         }
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean : DB Exception on Query : " + strQuery1);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      int iFldVldtnErrs = validateFields(request, iPreSqncNmbr, iPreVrsn, "V", strSttsCd);
      if (iFldVldtnErrs != 0)
      {
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Can not perform status change because of validation errors.");
         iReturnCode = -260;
         return (iReturnCode);
      }

      // determine if we need to generate a new Version for this Preorder
      if (strPreActnVrsnInd.equals("Y"))
      {
         iPreVrsn = createVersion(iPreSqncNmbr, iPreVrsn);

         if (iPreVrsn <= 0)
         {
            Log.write(Log.ERROR, "PreorderBean : Error Generating new Version for Preorder Sqnc Nmbr:" + iPreSqncNmbr);
            iReturnCode = -160;
         }

         if (iReturnCode != 0)
         {
            return (iReturnCode);
         }
      }

      // generate a new History record
      int iPreHstrySqncNmbr = updateHistory(iPreSqncNmbr, iPreVrsn, strSttsCd);
      if (iPreHstrySqncNmbr <= 0)
      {
            Log.write(Log.ERROR, "PreorderBean : Error Generating History for Preorder Sqnc Nmbr:" + iPreSqncNmbr);
            iReturnCode = -165;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      String strUpdate1 = "";
      if (getCmpnyTyp().equals("P"))
      {
         strUpdate1 = "UPDATE PREORDER_T SET PRE_ORDR_STTS_CD = '" + strSttsCd + "', PRE_ORDR_HSTRY_SQNC_NMBR = " + iPreHstrySqncNmbr + ", PRE_ORDR_VRSN = " + iPreVrsn + ", LST_MDFD_PRVDR = '" + getUserid() + "', MDFD_DT = " + getTimeStamp() + ", MDFD_USERID = '" + getUserid() + "' WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr;
      }
      else
      {
         strUpdate1 = "UPDATE PREORDER_T SET PRE_ORDR_STTS_CD = '" + strSttsCd + "', PRE_ORDR_HSTRY_SQNC_NMBR = " + iPreHstrySqncNmbr + ", PRE_ORDR_VRSN = " + iPreVrsn + ", LST_MDFD_CSTMR = '" + getUserid() + "', MDFD_DT = " + getTimeStamp() + ", MDFD_USERID = '" + getUserid() + "' WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr;
      }

      try
      {
         if (m_stmt.executeUpdate(strUpdate1) <= 0)
         {
            throw new SQLException();
         }
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean : DB Exception on Update : " + strUpdate1);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      // Send the autoReply if the Cust reply flag is set in ACTION_T and
      //   Auto response is not set in SERVICE_TYPE_FORM_T
      if ( (bSendEmail) && (strAutoRspnsFrm == null || !strAutoRspnsFrm.equals("Y")) )
                {
                        this.sendReply(iPreSqncNmbr, iPreVrsn, strEmailRcpt);
                }

      //if we got here, we had a successful Status Change and Generated a History Record.
      // Return the Preorder History Sequence Number

      return (iPreHstrySqncNmbr);
   }


   // Send the autoReply if necessary
   protected void sendReply(int iSqncNmbr, int iVrsn, String strUserID)
   {
                String strQuery1 = "SELECT PRE_ORDR_STTS_CD, OCN_CD, S.SRVC_TYP_DSCRPTN, A.ACTVTY_TYP_DSCRPTN, " +
         " TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI') AS THE_TIME " +
         " FROM PREORDER_T P, ACTIVITY_TYPE_T A, SERVICE_TYPE_T S WHERE PRE_ORDR_SQNC_NMBR= " + iSqncNmbr +
         " AND PRE_ORDR_VRSN =" + iVrsn + " AND P.ACTVTY_TYP_CD = A.ACTVTY_TYP_CD " +
         " AND A.TYP_IND='" + thisOrder.getTYP_IND() + "' AND P.SRVC_TYP_CD = S.SRVC_TYP_CD AND " +
         " S.TYP_IND = '" + thisOrder.getTYP_IND() + "' ";
                ResultSet rs = null;
      String strSubject = "";
      String strMsg = "";
                try
                {
                        rs = m_stmt.executeQuery(strQuery1);
                        if (rs.next())
                        {
            // Build the Subject
                           strSubject = "Express Preorder Status Change for TXNUM:  " + iSqncNmbr;

            // Build the Message
            strMsg =   "TXNUM         :  " + iSqncNmbr + "\n";
            strMsg = strMsg + "New Status    :  " + rs.getString("PRE_ORDR_STTS_CD") + "\n";
            strMsg = strMsg + "OCN Code      :  " + rs.getString("OCN_CD") + "\n";
            strMsg = strMsg + "Service Type  :  " + rs.getString("SRVC_TYP_DSCRPTN") + "\n";
            strMsg = strMsg + "Activity Type :  " + rs.getString("ACTVTY_TYP_DSCRPTN") + "\n";
            strMsg = strMsg + "Date/Time     :  " + rs.getString("THE_TIME") + "\n\n";
                                rs.close();
                        }
                        else
                        {
                                //DatabaseManager.releaseConnection(m_conn);
                                Log.write(Log.ERROR, "PreorderBean.sendReply(): Error getting email info");
                           return;
                        }
                }
                catch(SQLException e)
                {
                        e.printStackTrace();
                        //rollbackTransaction();
                        try { rs.close(); } catch(Exception e2) {}
                        //DatabaseManager.releaseConnection(m_conn);
                        Log.write(Log.ERROR, "PreorderBean.sendReply(): DB Exception on Query : " + strQuery1);
                        return;
                }

      Log.write(Log.DEBUG_VERBOSE,"PreorderBean.sendReply(): email to " + strUserID + " about to be sent for " + iSqncNmbr);
      if (Toolkit.autoReply(strUserID, strSubject, strMsg) != true)
         Log.write(Log.ERROR, "PreorderBean.sendReply(): AutoReply failed.");

      return;
   }

          // Send the provider autoReply if necessary
        protected void sendProvReply(int iSqncNmbr, int iVrsn)
   {
   }


        /** This method determines if a Preorder is to be automatically processed.  If so, it will populate
         *  the appropriate form fields and bump to the next status.
         *  Returns:    int iReturnCode     The form sequence number that should be displayed next. This is typically the
         *                                  response form for the Preorder (to show end user the results).
         *              >0      OK  (form seq nbr)
         *              <0      Error
         *              =0      OK - but manual preorder...so no need to return a form sequence number
         **/
   public int processPreorder(AlltelRequest request, int iFrmSqncNmbr, int iPreSqncNmbr, int iPreVrsn, String strActn)
        {
      int iReturnCode = 0;
      int iVersion = 0;
                String strSrvTyp = "";
      String strAutoAction = "";
      String strNewStatus = "";
      String strOCNCd = "";

                Log.write(Log.DEBUG_VERBOSE, "PreorderBean --- Process this preorder ");

      m_iNextForm = 0;

      // VALIDATE SECURITY HERE
      if ( ! hasAccessTo(iPreSqncNmbr) )
      {
         return (SECURITY_ERROR);
      }
      String strMdfdDt = "";

      // Get the current Status Code and outer join to ACTION_T to see if this status code
                // has an automated response.
                String strQuery1 = "SELECT PO.PRE_ORDR_VRSN, PO.SRVC_TYP_CD, NVL(A.STTS_CD_TO, '_NOAUTO_'), NVL(S.FRM_SQNC_NMBR," +iFrmSqncNmbr+"), NVL(S.AUTO_RSPNS_FRM, 'N'), PO.OCN_CD " +
                        "FROM PREORDER_T PO, ACTION_T A, SERVICE_TYPE_FORM_T S " +
                        "WHERE PO.PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND A.STTS_CD_FROM (+)=  PO.PRE_ORDR_STTS_CD " +
                        "AND A.TYP_IND (+)= 'P' AND A.ACTN (+)= '" + this.AUTOMATIC_RESPONSE + "'" +
                        "AND S.SRVC_TYP_CD (+)= PO.SRVC_TYP_CD AND S.TYP_IND (+)= 'P' AND S.AUTO_RSPNS_FRM (+)= 'Y'";
      try
      {
         ResultSet rs1 = m_stmt.executeQuery(strQuery1);
         if (rs1.next())
         {
            iVersion = rs1.getInt(1);
            strSrvTyp = rs1.getString(2);
            setSrvcTypCd( strSrvTyp );   //set bean service code

            strNewStatus = rs1.getString(3);
            m_iNextForm = rs1.getInt(4);
            strAutoAction = rs1.getString(5);
            strOCNCd = rs1.getString(6);
         }
         else
         {
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Error chking automatic action ");
            iReturnCode = -155;
         }
                        rs1.close();
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean : DB Exception on Query : " + strQuery1);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

Log.write(Log.DEBUG_VERBOSE, "PreorderBean : iFrmSqncNmbr="+iFrmSqncNmbr+" PO="+iPreSqncNmbr  + " NextForm=" + m_iNextForm);
Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Version="+iVersion+" SrvTyp="+strSrvTyp+" AutoAction="+strAutoAction+" NewStatus="+strNewStatus);
                if (strAutoAction.equals("Y"))
                {
                    Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Auto response - New status s/b = " + strNewStatus);
                }
                else
                {
                    Log.write(Log.DEBUG_VERBOSE, "PreorderBean : manually processed preorder");
                    return iReturnCode; //no auto response, so get out
                }

                //Now depending on type of Preorder, do auto response work
                switch(strSrvTyp.charAt(0)) {
                    case 'A':   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : auto response for Address Validation");
                                iReturnCode = doAddressValidation(iPreSqncNmbr, iVersion);
                                break;
                    case 'C':   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : auto response for Feature Avail");
                                iReturnCode = doFeatureServiceLookup(iPreSqncNmbr, iVersion);
                                break;
                    case 'D':   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : auto response for Appt Sechedule");
                                iReturnCode = doAppointmentSchedule(iPreSqncNmbr, iVersion);
                                break;
                    case 'L':   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : auto response for PIC Avail");
                                iReturnCode = doPICLookup(iPreSqncNmbr, iVersion);
                                break;
          case 'E':
                    case 'M':
                    case 'T':   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : auto response for CSI - type=[" + strSrvTyp + "]");
                          iReturnCode = doCSI(iPreSqncNmbr, iVersion, strSrvTyp, strOCNCd);
                                break;
                    default:    Log.write(Log.DEBUG_VERBOSE, "PreorderBean : auto response for UNKNOWN ?");
                                break;
                }//end-switch
                if (iReturnCode < 0)
                {
//NOTE - Do I need a ROLLBACK somewhere ??

                    return iReturnCode;
                }
                //We did auto processing, but pre-populate logic never happened- so do it now
                iReturnCode = autoPrepopulate(m_iNextForm, iPreSqncNmbr, iVersion);

      // Update m_strTimeStamp so that the history table can correctly show the length
      //   of time required to process the preorder.
      String strQueryTS = "SELECT TO_CHAR(sysdate,'MM/DD/YYYY HH24:MI:SS') PRE_TIMESTAMP FROM dual";
      try
      {
         ResultSet rsTS = m_stmt.executeQuery(strQueryTS);
         if (rsTS.next())
         {
            //this.m_strTimeStamp = "TO_DATE('" + rsTS.getString("PRE_TIMESTAMP") + "' ,'MM/DD/YYYY HH24:MI:SS')";
            setTimeStamp("TO_DATE('" + rsTS.getString("PRE_TIMESTAMP") + "' ,'MM/DD/YYYY HH24:MI:SS')");
            rsTS.close();
         }
         else
         {
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "PreorderBean::processPreorder() : Error getting Transaction TimeStamp ");
            iReturnCode = -105;
         }
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean::processPreorder() : DB Exception on Query : " + strQueryTS);
         iReturnCode = -100;
      }

                // Put to new status now, based on what ACTION_T 'automatic' record says...
                //NOTE: This method returns a History Seq Nbr, not a return code.
                iReturnCode = changeStatus(request,iPreSqncNmbr, this.AUTOMATIC_RESPONSE, iFrmSqncNmbr);
                if (iReturnCode > 0) //Successfully got a History seq nbr
                {
                    Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Successfully processed");
                    iReturnCode = 0;
                }
                else
                {   //we got problems
                    return iReturnCode;
                }

      return iReturnCode;
   }


        /** This method populates those fields that typically get filled in when the authorized user brings up
         *  the form for update. Since Preorders can be automatically responded to, this method spins thru
         *  the form fields for the response form only (as identified in SERVICE_TYPE_FORM_T column AUTO_RSPNS_FRM).
         *  Returns:    int iReturnCode     Return Code
         *              =0      OK  (form seq nbr)
         *              <0      Error
         */
   public int autoPrepopulate(int iFrmSqncNmbr, int iPreSqncNmbr, int iPreVrsn)
        {
      int iReturnCode = 0;
      String strNewValue = "";
                String strFldData = "";
                String strSrcFldQry = "";
      Vector m_vFrmFld = new Vector();

                Log.write(Log.DEBUG_VERBOSE, "PreorderBean : autoPrepopulate() FrmSqnc=" + iFrmSqncNmbr);
      try
      {
         m_vFrmFld = getFormBean().getFormFields(iFrmSqncNmbr, iPreSqncNmbr, iPreVrsn);
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "PreorderBean : DB Exception in getFormBean().getFormFields().");
         return(-100);
      }

      FormField ff;
                ResultSet rsSrcFld;
                for(int ff_idx = 0; ff_idx < m_vFrmFld.size(); ff_idx++)
                {
                        ff = (FormField)m_vFrmFld.elementAt(ff_idx);
                        strFldData = ff.getFieldData();
                        if (strFldData != null && strFldData.length() > 0) {
                           //Log.write(Log.DEBUG_VERBOSE, "PreorderBean : skip this one");
                           continue;   //data already there - no prepopulate required for this one
                        }
                        if (ff.getSrcInd().equals("R") )    //Read from somewhere to fill in our value and UPDATE too
         {
                            //get the data from source and fill in our form
                            strSrcFldQry = "UPDATE " + ff.getCurrentTable() + " SET " + ff.getColumnName() +
                                "=(SELECT NVL(" + ff.getSrcDbClmnNm() + ",0) FROM " + ff.getSrcDbTblNm() +
                                    " WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iPreVrsn + ") " +
                                " WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iPreVrsn;
                            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : AutoFill qry=[" + strSrcFldQry + "]");
                            try
                            {   m_stmt.executeUpdate(strSrcFldQry);
                            }
                            catch(SQLException e)
                            {
                                e.printStackTrace();
                                rollbackTransaction();
                                DatabaseManager.releaseConnection(m_conn);
                                Log.write(Log.ERROR, "PreorderBean : autoPrepopulate() failed on UPDATE query="+strSrcFldQry);
                                iReturnCode = -100;
                            }
                            if (iReturnCode != 0)
                            {   return (iReturnCode);
                            }
                        }//end of 'R'

                        if (ff.getSrcInd().equals("U") )    //Read our form and fill in somewhere else
         {
                           //get the data from source and fill in our form
                            strSrcFldQry = "UPDATE " + ff.getSrcDbTblNm() + " SET " + ff.getSrcDbClmnNm() +
                                "=(SELECT NVL(" + ff.getColumnName() + ", 0) FROM " + ff.getCurrentTable() +
                                    " WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iPreVrsn + ") " +
                                " WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iPreVrsn;
                            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : AutoFill qry=[" + strSrcFldQry + "]");
                            try
                            {   m_stmt.executeUpdate(strSrcFldQry);
                            }
                            catch(SQLException e)
                            {
                                e.printStackTrace();
                                rollbackTransaction();
                                DatabaseManager.releaseConnection(m_conn);
                                Log.write(Log.ERROR, "PreorderBean : autoPrepopulate() failed on UPDATE query="+strSrcFldQry);
                                iReturnCode = -100;
                            }
                            if (iReturnCode != 0)
                            {   return (iReturnCode);
                            }

                        } //'U'
                        if (ff.getSrcInd().equals("D") )    //'D' means data time fill in by system
         {
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Filling in autodate");
                           //get the data from source and fill in our form
                            try
                            {   strSrcFldQry = "UPDATE " + ff.getCurrentTable() + " SET " + ff.getColumnName() +
                                  "=( select to_char(sysdate,'" + PropertiesManager.getProperty("lsr.autofill.datefmt", "MM-DD-YYYY-HHMIAM") + "') from dual) " +
                                  " WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iPreVrsn;
                                Log.write(Log.DEBUG_VERBOSE, "PreorderBean : AutoFill qry=[" + strSrcFldQry + "]");
                                m_stmt.executeUpdate(strSrcFldQry);

            //If source table and column are populated for this "D", then do update (like a "U")
            if ( ff.getSrcDbTblNm().length() > 0 && ff.getSrcDbClmnNm().length() > 0)
            {
               strSrcFldQry = "UPDATE " + ff.getSrcDbTblNm() + " SET " + ff.getSrcDbClmnNm() +
                  "=(SELECT NVL(" + ff.getColumnName() + ", 0) FROM " + ff.getCurrentTable() +
                      " WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iPreVrsn + ") " +
                  " WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iPreVrsn;
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : AutoFill qry2=[" + strSrcFldQry + "]");
               try
               {   m_stmt.executeUpdate(strSrcFldQry);
               }
               catch(SQLException e)
               {
                  e.printStackTrace();
                  rollbackTransaction();
                  DatabaseManager.releaseConnection(m_conn);
                  Log.write(Log.ERROR, "PreorderBean : autoPrepopulate() failed on UPDATE query2="+strSrcFldQry);
                  iReturnCode = -100;
               }
               if (iReturnCode != 0)
               {   return (iReturnCode);
               }
            }
                            }
                            catch(SQLException se)
                            {
                                se.printStackTrace();
                                rollbackTransaction();
                                DatabaseManager.releaseConnection(m_conn);
                                Log.write(Log.ERROR, "PreorderBean : autoPrepopulate() failed on UPDATE query="+strSrcFldQry);
                                iReturnCode = -100;
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                                rollbackTransaction();
                                DatabaseManager.releaseConnection(m_conn);
                                Log.write(Log.ERROR, "PreorderBean : autoPrepopulate() failed on UPDATE query="+strSrcFldQry);
                                iReturnCode = -100;
                            }
                            if (iReturnCode != 0)
                            {   return (iReturnCode);
                            }

                        }//'D'

                }//end for()

                return (iReturnCode);

   }//end of autoPrepopulate()

        /**
    * Check SQLException for unavailability of resources
         * @param   SQLException    Exception from DB2
    * @return  int       MISC_DB_ERROR or DB2_UNAVAILABLE
    */
   private int DB2Unavailable(SQLException se)
          {
      int iRC=MISC_DB_ERROR;
      String strTemp = se.toString();
      if (strTemp.indexOf(CAMS_DW_UNAVAILABLE) >= 0)
      {
         Log.write(Log.WARNING, "PreorderBean : DB2 unavailable!");
         iRC = DB2_UNAVAILABLE;
      }
      if (strTemp.indexOf(CAMS_DW_INUSE)  >= 0)
      {
         Log.write(Log.WARNING, "PreorderBean : DB2 DW in use!");
         iRC = DB2_UNAVAILABLE;
      }
      return iRC;
   }

        /**
    * Complete an Address Validation lookup
    *
         * @param   int    Preorder Sequence Number
         * @param   int    Preorder Version Number
         * @param   String    RESPC code
         * @param   String    RESPD description
         * @param   String    Remarks field
    * @return  void
    */
   private void completeAddressValidation(int iPreSqncNmbr, int iVersion, String strRespc, String strRespd, String strRemark)
   {
      try {
         m_stmt.executeUpdate("UPDATE AVR_T SET AVR_RESPC='" + strRespc + "', " +
            " AVR_RESPD='" + strRespd + "', AVR_REMARKS='" + strRemark + "' " +
            " WHERE PRE_ORDR_SQNC_NMBR="+ iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
      }
      catch (SQLException se) {
         Log.write(Log.ERROR, "PreorderBean : completeAddressValidation() se=["+se+"]");
      }
   }

        /**
    * Using  either the user-entered WTN or address info, query the CAMS datawarehouse
    * for a service address.
         * @param   int Preorder Sequence Number.
         * @param   int Preorder Version Number.
    * @return  int Return Code (0 = OK, <0 = Error)
    */
        private int doAddressValidation(int iPreSqncNmbr, int iVersion)
        {
      int iReturnCode = 0;
      String strWTN = "";
      String strRemark = " (search done by Service Address)";
      boolean bFound = false;
      boolean bWTN = false;
      boolean bInServiceTN = true;
      ResultSet rs = null;
                String strSAPR = "";
                String strSANO = "";
      String strSASD = "";   //directional prefix  ie N, E, SW, ...
      String strSASN = "";
      String strSATH = "";
      String strCITY = "";
      String strSTATE = "";
      String strZIP = "";
      String strZIPWhereClause = "";
      String strLD1 = "";
      String strCounty = "";       //County needed for MSAG lookups

      //First let's get the WTN and address fields
      try
      {
         rs = m_stmt.executeQuery("SELECT SUBSTR(AV_WTN,1,3)||SUBSTR(AV_WTN,5,3)||SUBSTR(AV_WTN,9,4), AV_SAPR, AV_SANO, AV_SASF, AV_SASD, AV_SASN, AV_SATH, AV_SASS, AV_CITY, " +
            "AV_STATE, AV_ZIP, AV_LD1, AV_LV1, AV_COUNTY FROM AV_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " +
            iVersion);
         if (rs.next())
         {
            strWTN = rs.getString(1);
            if (strWTN == null || strWTN.length() == 0)    //No WTN entered, so gather Service Address fields
            {
               bWTN = false;
                                   strSAPR = rs.getString(2);
                                   strSANO = rs.getString(3);
                                   String strSASF = rs.getString(4);
                                   if (strSASF != null)         //put on number suffix (like 1/2)
                                   {   strSANO = strSANO + " " + strSASF.toUpperCase().trim();
                                   }
                                   strSASD = rs.getString(5);
               if (strSASD != null)
               {   strSASD = strSASD.toUpperCase().trim();
               }
                                   strSASN = (rs.getString(6)).toUpperCase().trim();
                                   strSATH = normalizeSATH( rs.getString(7) );
                                   strCITY = (rs.getString(9)).toUpperCase().trim();
                                   strSTATE = (rs.getString(10)).toUpperCase().trim();
                                   strZIP = rs.getString(11);
                                   strLD1 = rs.getString(12);
                                   if (strLD1 != null)
                                   {   strLD1 = strLD1.toUpperCase().trim() + " " + (rs.getString(13)).toUpperCase().trim();
                                   }
               strCounty = (rs.getString(14)).toUpperCase().trim();
                                   if (strZIP != null)      //Tweek entered ZIP
                                   {
                                      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : ZIP entered = [" + strZIP + "]");
                                      if (strZIP.length() == 5)    //user entered 5 digit zip
                                      {
                                         strZIPWhereClause = " AND A.ZIP_CODE LIKE '" + strZIP + "%' ";
                                      }
                                      else if (strZIP.length() > 5)   //user entered 12345-1234 or just 5+ digits
                                      {
                                         //get rid of "-"
                                         int i = strZIP.lastIndexOf("-");
                                         if (i !=  -1)
                                         {   strZIP = strZIP.substring(0,i) + strZIP.substring(i+1);
                                         }
                                         strZIPWhereClause = " AND A.ZIP_CODE = '" + strZIP + "' ";
                                      }
                                      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : ZIP now = [" + strZIP + "]");
                                      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : ZIP Where = [" + strZIPWhereClause + "]");
                                   }
            }
            else
            {   bWTN = true;
               strRemark = " (Search done by WTN) ";
            }
         }
         else
         {   Log.write(Log.ERROR, "PreorderBean : Err getting input args from AV_T for PO=[" + iPreSqncNmbr + "]");
            iReturnCode = -1;
         }
      }
      catch(SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQLException in doAddressValidation() se=" + se);
         iReturnCode = -1;
      }
      catch(Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception in doAddressValidation() e=" + e);
         iReturnCode = -2;
      }
      if (iReturnCode < 0)
      {   return iReturnCode;
      }

      Connection connCAMS=null;
           Statement stmtCAMS = null;
      try {
              connCAMS = DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
              stmtCAMS = connCAMS.createStatement();

         //DB2 connectivity has been a BIG issue, so let's do a query that is guaranteed to work
         //on a good connection.
         try {
            rs = stmtCAMS.executeQuery(DatabaseManager.getTestSQLString( DatabaseManager.CAMSP_CONNECTION ));
            if (rs!=null) rs.next();
         //   rs.close();
         }
         catch (SQLException e) {
            Log.write(Log.ERROR, "PreorderBean : Exception caught, will reset pool! e=["+e+"]");
            boolean bClosed = connCAMS.isClosed();
            Log.write(Log.WARNING, "PreorderBean : conn closed before reset() = " + bClosed);
            DatabaseManager.resetPool(connCAMS);
Thread.sleep(5000);
                      DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
            bClosed = connCAMS.isClosed();
            Log.write(Log.WARNING, "PreorderBean : conn closed after release = " + bClosed);
              connCAMS = DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
            bClosed = connCAMS.isClosed();
            Log.write(Log.WARNING, "PreorderBean : conn closed after getConnection() = " + bClosed);
            //resetting causing us to lose statments and resultsets assoc w/ it
            Log.write(Log.WARNING, "PreorderBean : will try to re-establish with connection");
            stmtCAMS = connCAMS.createStatement();
            bClosed = connCAMS.isClosed();
            Log.write(Log.WARNING, "PreorderBean : conn closed after createStmt = " + bClosed);
         }
      }
      catch (Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : doAddressValidation() e=" + e);
         iReturnCode = -2;
      }
      if (iReturnCode < 0)
                {   try { rs.close();
         rs = null;
         stmtCAMS.close();
         stmtCAMS = null;
         } catch (Exception ee) {}
                   DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         return iReturnCode;
      }

      //Connection tested -so do our work now.
      try {
         //If WTN populated
         if (bWTN)
         {
            String strCustPhone = "";
            String strCustInSrvDate = "";

            //Get the latest Cust Phone and in service date for this guy
//            rs = stmtCAMS.executeQuery("SELECT CUST_PHONE, CUST_INSRV_DATE FROM DB2.CAMS_SENTT WHERE SENT_PHONE='" +
//               strWTN + "' ORDER BY 2 DESC");
           String strQuery1 = "SELECT CUST_PHONE, CUST_INSRV_DATE FROM KASH.CAMS_SENTT WHERE SENT_PHONE='" +
               strWTN + "' ORDER BY 2 DESC";

              Log.write(Log.DEBUG_VERBOSE, "PreorderBean.java: Getting Customer Phone and In Service Date"+strWTN);
              Log.write("KASH.QUERY0:"+strQuery1);

            rs = stmtCAMS.executeQuery("SELECT CUST_PHONE, CUST_INSRV_DATE FROM KASH.CAMS_SENTT WHERE SENT_PHONE='" +
               strWTN + "' ORDER BY 2 DESC");
           // Log.write(Log.DEBUG_VERBOSE, "PreorderBean.java : Line # 1047: Getting Customer Phone and In Service Date"+rs);
            if (rs.next()==true)
            {
               bFound = true;
               strCustPhone = rs.getString(1);
               strCustInSrvDate =  rs.getString(2);
            }
            else
            {
               bFound = false;
               m_stmt.executeUpdate("DELETE FROM AVR_SA_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr +
                         " AND PRE_ORDR_VRSN = " + iVersion);
               Log.write(Log.DEBUG_VERBOSE, "WTN NOT Found on query1");
               try {
                  m_stmt.executeUpdate("INSERT INTO AVR_SA_T (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, " +
                     "MDFD_DT, MDFD_USERID) VALUES (" + iPreSqncNmbr + ", " + iVersion + ",1, sysdate, 'auto')");

               }
               catch(SQLException se) {
                  Log.write(Log.WARNING, "PreorderBean : INSERT Exception in doAddressValidation()");
                  iReturnCode = -101;
               }
            }

            try {   rs.close();
            }
            catch (Exception e) {}

            String strWtnQuery = "";
            if(bFound)
            {
               //See if latest TN is disconnected  (HD 65809)
//               rs = stmtCAMS.executeQuery("SELECT DISC_DATE FROM DB2.CAMS_CUSTOMERT WHERE " +
//                  "CUST_PHONE='" + strCustPhone + "' AND CUST_INSRV_DATE='" + strCustInSrvDate + "'");//datechange
                
                String strQry = "SELECT DISC_DATE FROM KASH.CAMS_CUSTOMERT WHERE " +
                                "CUST_PHONE='" + strCustPhone + "' AND CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd')";
                
                Log.write(Log.DEBUG_VERBOSE, "PreorderBean.java : Getting Disconnect Date of a TN from CAMS CUSTOMER Table "+strCustPhone+" / "+strCustInSrvDate);
                Log.write("KASH.QUERY1: "+strQry);
                
                //Antony - modified date query to convert to correct format - date only format as in CAMS -- datechange
                rs = stmtCAMS.executeQuery("SELECT DISC_DATE FROM KASH.CAMS_CUSTOMERT WHERE " +
                  "CUST_PHONE='" + strCustPhone + "' AND CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd')");
                
                                               
               if (rs.next()==true)
               {   String strDiscDate = rs.getString(1);      //HD 65809
                  Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Disc date=" + strDiscDate);
                  if (strDiscDate == null || strDiscDate.length() < 2)
                  {}
                  else if(strDiscDate.equals("0000-00-00"))
                  {
                      //dont change anything - Antony - 04/28/2014
                  }
                  else
                  {   bInServiceTN = false;
                  }
                  
               }
               try {   rs.close();
               }
               catch (Exception e) {}

               //See if a Service Address Trait exists -if it does, use that ADDR-ID,
               //otherwise take what we can get....
//               rs = stmtCAMS.executeQuery("SELECT T.TRAIT_TXT FROM DB2.CAMS_TRAITT T WHERE T.CUST_PHONE='" +
//                   strCustPhone + "' AND CUST_INSRV_DATE='" + strCustInSrvDate + "' AND T.SENT_PHONE='" +
//                   strWTN + "' AND T.TRAIT_NAME='SERV-ADDR-ID' ORDER BY T.TRAIT_EFF_DATE DESC"); //datechange

               String strQuery2 = "SELECT T.TRAIT_TXT FROM KASH.CAMS_TRAITT T WHERE T.CUST_PHONE='" +
                   strCustPhone + "' AND CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND T.SENT_PHONE='" +
                   strWTN + "' AND T.TRAIT_NAME='SERV-ADDR-ID' ORDER BY T.TRAIT_EFF_DATE DESC" ;

               Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Getting Service Address Trait for a Customer TN from CAMS_TRAITT table."+strCustPhone+"/"+strCustInSrvDate+"/"+strWTN);
               Log.write("KASH.QUERY2:"+strQuery2);

               rs = stmtCAMS.executeQuery("SELECT T.TRAIT_TXT FROM KASH.CAMS_TRAITT T WHERE T.CUST_PHONE='" +
                   strCustPhone + "' AND CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND T.SENT_PHONE='" +
                   strWTN + "' AND T.TRAIT_NAME='SERV-ADDR-ID' ORDER BY T.TRAIT_EFF_DATE DESC");
              // Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Query Line # 1104: Getting Service Address Trait for a Customer TN from CAMS_TRAITT table."+rs);
               if (rs.next()==true)
               {   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Service Address trait found");
//                  strWtnQuery =
//                  "SELECT A.ADDR_ID, A.ADDR_EFF_DATE, A.ADDR_LINE1, A.ADDR_LINE2, A.ADDR_LINE3, " +
//                  "A.ADDR_LINE4, A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE, S.ORG_NPA, S.ORG_NXX " +
//                  " FROM DB2.CAMS_SENTT S, DB2.CAMS_ADDRESST A WHERE S.CUST_PHONE='" + strCustPhone +
//                   "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" +
//                   strWTN + "' AND A.CUST_PHONE=S.CUST_PHONE AND A.CUST_INSRV_DATE=S.CUST_INSRV_DATE " +
//                  " AND S.ADDR_ID=A.ADDR_ID AND S.ADDR_ID ='" + rs.getString(1) + "' ORDER BY 2 DESC";//datechange
                  strWtnQuery =
                  "SELECT A.ADDR_ID, A.ADDR_EFF_DATE, A.ADDR_LINE1, A.ADDR_LINE2, A.ADDR_LINE3, " +//address line change here
                  "NVL(A.ADDR_LINE4,''), A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE, S.ORG_NPA, S.ORG_NXX " +
                  " FROM KASH.CAMS_SENTT S, KASH.CAMS_ADDRESST A WHERE S.CUST_PHONE='" + strCustPhone +
                   "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.SENT_PHONE='" +
                   strWTN + "' AND A.CUST_PHONE=S.CUST_PHONE AND A.CUST_INSRV_DATE=S.CUST_INSRV_DATE " +
                  " AND S.ADDR_ID=A.ADDR_ID AND S.ADDR_ID ='" + rs.getString(1) + "' ORDER BY 2 DESC";
                Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Get Customer Address details like Customer Address, City, State, Zip Code, NPA and NXX."+strCustPhone+"/"+strCustInSrvDate+"/"+strWTN);
                Log.write("KASH.QUERY3:"+strWtnQuery);
               }
               else
               {
//                  strWtnQuery =
//                  "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.ADDR_LINE1, A.ADDR_LINE2, A.ADDR_LINE3,"+
//                  "A.ADDR_LINE4, A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE, S.ORG_NPA, S.ORG_NXX " +
//                  " FROM DB2.CAMS_SENTT S, DB2.CAMS_ADDRESST A WHERE S.CUST_PHONE='" + strCustPhone +
//                   "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" +
//                   strWTN + "' AND A.CUST_PHONE=S.CUST_PHONE AND A.CUST_INSRV_DATE=S.CUST_INSRV_DATE " +//datechange
//                  " AND S.ADDR_ID IN ('SAD1', 'MAIN') ORDER BY 2 DESC, 1 DESC";
                  strWtnQuery =
                  "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.ADDR_LINE1, A.ADDR_LINE2, A.ADDR_LINE3,"+
                  "NVL(A.ADDR_LINE4,''), A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE, S.ORG_NPA, S.ORG_NXX " +//address line change here
                  " FROM KASH.CAMS_SENTT S, KASH.CAMS_ADDRESST A WHERE S.CUST_PHONE='" + strCustPhone +
                   "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.SENT_PHONE='" +
                   strWTN + "' AND A.CUST_PHONE=S.CUST_PHONE AND A.CUST_INSRV_DATE=S.CUST_INSRV_DATE " +
                  " AND S.ADDR_ID IN ('SAD1', 'MAIN') ORDER BY 2 DESC, 1 DESC";
                Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Get Distinct Address details of customer like Customer Address, City, State, Zip Code, NPA and NXX. Else Loop"+strWtnQuery);
                Log.write("KASH.QUERY4:"+strWtnQuery);
               }
               try {   rs.close();
               }
               catch (Exception e) {}

               Log.write("Querying CAMS DB2 data warehouse now by WTN ...");
               rs = stmtCAMS.executeQuery(strWtnQuery);
               if (rs.next() == true)   //only get the first -I dont' know if there may be multiple?
               {
                  //Now clean up any prior records
                  m_stmt.executeUpdate("DELETE FROM AVR_SA_T WHERE PRE_ORDR_SQNC_NMBR = " +
                           iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
                  Log.write(Log.DEBUG_VERBOSE, "WTN SAD1 and/or MAIN record found");
                  bFound = true;
                  PreparedStatement pStmt = m_conn.prepareStatement("INSERT INTO AVR_SA_T " +
                     " (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, AVR_LSO, AVR_AFT,"+
                     " AVR_CITY, AVR_STATE, AVR_ZIP, AVR_CAI, MDFD_DT, MDFD_USERID) " +
                     " VALUES ("+iPreSqncNmbr+"," + iVersion + ",1,?,'D', ?, ?, ?, ?, sysdate, 'auto')");
                  pStmt.setString(1, rs.getString(10)+rs.getString(11));   //LSO (NPANXX)
                  pStmt.setString(2, rs.getString(7));   //city
                  pStmt.setString(3, rs.getString(8));   //state
                  pStmt.setString(4, rs.getString(9));   //zip

                  //Per Tana Henson do not a customers name - most names are in ADDR_LINE1 in warehouse
                  //So if ADDR_LINE1 has all characters, assume its the name and dont include
                  String strNameCheck=rs.getString(3);
                  boolean bNotName=false;
                  for (int i=0; i < strNameCheck.length(); i++)
                  {   //Log.write("char "+i+"=["+strNameCheck.charAt(i)+"]");
                     if ( Character.isDigit(strNameCheck.charAt(i)) )
                     {   bNotName=true;
                        break;
                     }
                  }
                  if (bNotName) {
                     pStmt.setString(5, (rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+
//                           rs.getString(6)).substring(0,119) );//lines 1-4
                             //removed the substring to fix null value at end of CAI field value - Antony - 03/28/2014
                             rs.getString(6)));//lines 1-4
                  }
                  else {
                     Log.write(Log.DEBUG_VERBOSE, "PreorderBean : dont show name ["+ strNameCheck+"]");
                     //pStmt.setString(5, rs.getString(4)+rs.getString(5)+rs.getString(6) );//lines 2-4
                     //put in null check and empty string substitution to fix null value at end of CAI field value - Antony - 03/28/2014
                     
                     Log.write("addr line 2: "+rs.getString(4));
                     Log.write("addr line 3: "+rs.getString(5));
                     Log.write("addr line 4: "+rs.getString(6));
                     
                     String strAddrLine = "";
                     
                     if(rs.getString(5) != null || rs.getString(5).length() != 0) 
                         strAddrLine = rs.getString(4) + " " + rs.getString(5);
                     else {
                         Log.write("Addr Line 3 is null or empty !!!");
                         
                         strAddrLine = rs.getString(4);
                     }
                     
                     Log.write("strAddrLine 4+5 : "+strAddrLine);
                     
                     /*
                     if(rs.getString(6).equals("null") || rs.getString(6) == null || rs.getString(6).length() == 0) {
                         
                         Log.write("Addr Line 4 is null or empty !!!");
                         
                     } else {
                         Log.write("Addr Line 4 is not null or not empty !!!");
                         
                         strAddrLine = strAddrLine + rs.getString(6);
                         
                     }*/
                     
                     String strAddrLine4 = rs.getString(6);
                     
                     //if strAddrLine4 == null replace with ""
                     
                     if(strAddrLine4 == null)
                         strAddrLine4 = "";
                     else
                         strAddrLine4 = " " + strAddrLine4;
                     strAddrLine = strAddrLine + strAddrLine4;
                     
                     Log.write("strAddrLine final : "+strAddrLine);
                     pStmt.setString(5,strAddrLine);
                  }

                  pStmt.executeUpdate();

               }//end-if
               else   //insert empty record
               {
                  //Now clean up any prior records
                  m_stmt.executeUpdate("DELETE FROM AVR_SA_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr +
                           " AND PRE_ORDR_VRSN = " + iVersion);
                  Log.write(Log.DEBUG_VERBOSE, "WTN NOT Found - insert dummy record");
                  try {
                     m_stmt.executeUpdate("INSERT INTO AVR_SA_T " +
                         "(PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC," +
                        "MDFD_DT, MDFD_USERID) VALUES (" + iPreSqncNmbr + ", " +
                         iVersion + ",1, sysdate, 'auto')");
                  }
                  catch(SQLException se) {
                     Log.write(Log.WARNING, "PreorderBean : INSERT Exception doAddressValidation()");
                     iReturnCode = -101;
                  } 
               } Log.write("Done querying CAMS DB2 data warehouse now ...");
            } //if (bFound)
         }
         else //no WTN, so use entered service address to do search
         {
            Log.write("querying CAMS DB2 data warehouse now by Service Address ...Timeout (seconds) = " +
                         stmtCAMS.getQueryTimeout());
             strRemark = " (Search done by Service Address) ";
             String strWtnQuery = "";
            String strTempSASN = strSASN;

//            strWtnQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.ADDR_LINE1, A.ADDR_LINE2, A.ADDR_LINE3,"+
//               " A.ADDR_LINE4, A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE, A.CUST_PHONE, B.ORG_NPA, B.ORG_NXX, B.DISC_DATE, B.ORG_EXCH " +
//               " FROM DB2.CAMS_ADDRESST A, DB2.CAMS_CUSTOMERT B WHERE A.STATE_CODE = '" + strSTATE + "'  " +
//                                        " AND A.CITY_NAME = '" + strCITY + "' " + strZIPWhereClause;
            strWtnQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.ADDR_LINE1, A.ADDR_LINE2, A.ADDR_LINE3,"+
               " NVL(A.ADDR_LINE4,''), A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE, A.CUST_PHONE, B.ORG_NPA, B.ORG_NXX, B.DISC_DATE, B.ORG_EXCH " +//address line change here
               " FROM KASH.CAMS_ADDRESST A, KASH.CAMS_CUSTOMERT B WHERE A.STATE_CODE = '" + strSTATE + "'  " +
                                        " AND A.CITY_NAME = '" + strCITY + "' " + strZIPWhereClause;
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Get Distinct Customer Address details from table CAMS_ADDRESST AND CAMS_CUSTOMERT"+strWtnQuery);
            Log.write("KASH.QUERY5:"+strWtnQuery);
            if (strSASD != null)
            {   strTempSASN = strSASD + " " + strSASN;
            }
            if (strLD1 == null || strLD1.length() == 0)
             {
               if (strSAPR == null || strSAPR.length()==0)
               {
                  strWtnQuery +=
                                   " AND (A.ADDR_LINE2 LIKE '" + strSANO + " " + strTempSASN + " " + strSATH + "%' OR " +//address line change here
                  "      A.ADDR_LINE3 LIKE '" + strSANO + " " + strTempSASN + " " + strSATH + "%') ";
               }
               else
               {
               strWtnQuery +=
                                        " AND (A.ADDR_LINE2 LIKE '" +strSAPR +" " +strSANO +" " +strTempSASN +" " +strSATH +"%'" +//address line change here
                " OR A.ADDR_LINE3 LIKE '" +strSAPR +" " +strSANO +" " +strTempSASN +" " +strSATH +"%') ";
               }
            }
            else
            {
               if (strSAPR == null || strSAPR.length()==0)
               {
               strWtnQuery +=
                                        " AND ( (A.ADDR_LINE2 LIKE '" + strSANO + " " + strTempSASN + " " + strSATH + "%' AND " +//address line change here
               "(A.ADDR_LINE2 LIKE '%" + strLD1 + "%' OR A.ADDR_LINE3 LIKE '%" + strLD1 + "%'))  " +
                                        " OR    (A.ADDR_LINE3 LIKE '" + strSANO + " " + strTempSASN + " " + strSATH + "%' AND " +
               " (A.ADDR_LINE3 LIKE '%" + strLD1 + "%' OR NVL(A.ADDR_LINE4,'') LIKE '%" + strLD1 + "%')) ) ";
               }
               else
               {
               strWtnQuery +=
                                        " AND ( (A.ADDR_LINE2 LIKE '" +strSAPR +" " +strSANO +" " +strTempSASN +" " +strSATH +"%' AND " +//address line change here
               "(A.ADDR_LINE2 LIKE '%" +strLD1 +"%' OR A.ADDR_LINE3 LIKE '%" +strLD1 +"%'))  " +
                                        " OR    (A.ADDR_LINE3 LIKE '" +strSAPR +" " +strSANO +" " +strTempSASN +" " +strSATH +"%' AND " +
               " (A.ADDR_LINE3 LIKE '%" +strLD1 + "%' OR NVL(A.ADDR_LINE4,'') LIKE '%" +strLD1 +"%')) ) ";
               }
            }
            strWtnQuery +=
               " AND (A.ADDR_ID ='SAD1' OR A.ADDR_ID = 'MAIN') AND B.CUST_PHONE=A.CUST_PHONE " +
               " AND B.CUST_INSRV_DATE=A.CUST_INSRV_DATE " +//check datechange
               " ORDER BY 2 DESC, 1 DESC ";
            Log.write("querying =[" + strWtnQuery + "]");
            rs = stmtCAMS.executeQuery(strWtnQuery);

            //Get number of rows in result set...
            //count distinct TNs

            if (rs.next() == true)   //only get the first -I dont' know if there may be multiple?
            {
               Log.write(Log.DEBUG_VERBOSE, " Record found");
               //HD 351542 check for crappy data...
               String strExch = rs.getString(14);   //"FICT/FRGN" ?
               String strCompleteZip = rs.getString(9);

               //Now clean up any prior records
               m_stmt.executeUpdate("DELETE FROM AVR_SA_T WHERE PRE_ORDR_SQNC_NMBR = " +
                         iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
               bFound = true;
               PreparedStatement pStmt = m_conn.prepareStatement("INSERT INTO AVR_SA_T " +
               " (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, AVR_LSO, AVR_AFT, AVR_CITY,"+
               " AVR_STATE, AVR_ZIP, AVR_CAI, MDFD_DT, MDFD_USERID) " +
               " VALUES ("+iPreSqncNmbr+"," + iVersion + ", 1, ?, 'D', ?, ?, ?, ?, sysdate, 'auto')");
               pStmt.setString(1, rs.getString(11)+rs.getString(12));   //LSO or NPANXX
               pStmt.setString(2, rs.getString(7));   //city
               pStmt.setString(3, rs.getString(8));   //state
               pStmt.setString(4, rs.getString(9));   //zip
                                        strWTN = rs.getString(10);       //get WTN
                                        Log.write(Log.DEBUG_VERBOSE, " CAMS CustPhone = " + strWTN);

               String strDiscDate = rs.getString(13);         //HD 65809
               if (strDiscDate == null || strDiscDate.length() < 2)
               {}
               else
               {   bInServiceTN = false;
               }

               //Per Tana Henson do not show a customers name - most names are in ADDR_LINE1 in warehouse
               //So if ADDR_LINE1 has all characters, assume its the name and dont include
               String strNameCheck=rs.getString(3);
               boolean bNotName=false;
               for (int i=0; i < strNameCheck.length(); i++)
               {   //Log.write("char "+i+"=["+strNameCheck.charAt(i)+"]");
                  if ( Character.isDigit(strNameCheck.charAt(i)) )
                  {   bNotName=true;
                     break;
                  }
               }
               //should we change remove the substring here and do a null check here too - Antony - 05/02/2014
               if (bNotName) {
                  pStmt.setString(5, (rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+
                            rs.getString(6)).substring(0,119) );//lines 1-4
               }
               else {
                  Log.write(Log.DEBUG_VERBOSE, "PreorderBean : dont show name ["+ strNameCheck+"]");
                  pStmt.setString(5, rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6) );//lines 2-4
               }

               //If bogus/fictitious/foreign account, re-query to get a valid NPANXX
               if (strExch.equals("FICT") || strExch.equals("FRGN"))
               {
                  Log.write(Log.DEBUG_VERBOSE, "PreorderBean : FICT/FRGN NpaNxx found");
                  String strTempSANO=strSANO;
//                  strWtnQuery = "SELECT DISTINCT B.ORG_NPA, B.ORG_NXX, B.ORG_EXCH " +
//                                             " FROM DB2.CAMS_ADDRESST A, DB2.CAMS_CUSTOMERT B WHERE A.STATE_CODE= '" +
//                    strSTATE + "'  " + " AND A.CITY_NAME = '" + strCITY + "' AND A.ZIP_CODE='"+
//                    strCompleteZip + "' ";
                  strWtnQuery = "SELECT DISTINCT B.ORG_NPA, B.ORG_NXX, B.ORG_EXCH " +
                                             " FROM KASH.CAMS_ADDRESST A, KASH.CAMS_CUSTOMERT B WHERE A.STATE_CODE= '" +
                    strSTATE + "'  " + " AND A.CITY_NAME = '" + strCITY + "' AND A.ZIP_CODE='"+
                    strCompleteZip + "' ";
                  Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get NPA, NXX & EXCHANGE from tables CAMS_ADDRESST, CAMS_CUSTOMERT."+strWtnQuery);
                  Log.write("KASH.QUERY6:"+strWtnQuery);

                  if (strTempSANO.length() > 1)
                  {   strTempSANO = strSANO.substring(0, strSANO.length() - 1) + "%";
                  }
                  else
                     strTempSANO="";
                  Log.write(Log.DEBUG_VERBOSE, "FICT/FRGN SANO =[" + strTempSANO +"]");
                  if (strSASD != null)
                  {       strTempSASN = strSASD + " " + strSASN;
                                      }
                                      if (strLD1 == null || strLD1.length() == 0)
                                      {
                                              if (strSAPR == null || strSAPR.length()==0)
                                                {
                                                      strWtnQuery += " AND (A.ADDR_LINE2 LIKE '" + strTempSANO + " " +//address line change here
                           strTempSASN + " " + strSATH + "%' OR " + " A.ADDR_LINE3 LIKE '" +
                           strTempSANO + " " + strTempSASN + " " + strSATH + "%') ";
                                              }
                                              else
                                              {
                                                 strWtnQuery += " AND (A.ADDR_LINE2 LIKE '" +strSAPR +//address line change here
                          " " +strTempSANO +" " +strTempSASN +" " +strSATH +"%'" +
                          " OR A.ADDR_LINE3 LIKE '" +strSAPR +" " +strTempSANO +" " +strTempSASN +
                          " " +strSATH +"%') ";
                                              }
                                      }
                                      else
                  {
                     if (strSAPR == null || strSAPR.length()==0)
                     {
                        strWtnQuery += " AND ( (A.ADDR_LINE2 LIKE '" + strTempSANO +//address line change here
                           " " + strTempSASN + " " + strSATH + "%' AND " +
                           "(A.ADDR_LINE2 LIKE '%" + strLD1 + "%' OR A.ADDR_LINE3 LIKE '%" +
                           strLD1 + "%'))  " + " OR    (A.ADDR_LINE3 LIKE '" + strTempSANO +
                           " " + strTempSASN + " " + strSATH + "%' AND " +
                           " (A.ADDR_LINE3 LIKE '%" + strLD1 + "%' OR NVL(A.ADDR_LINE4,'') LIKE '%" +
                           strLD1 + "%')) ) ";
                     }
                     else
                     {
                        strWtnQuery += " AND ( (A.ADDR_LINE2 LIKE '" +strSAPR +" " +strTempSANO +//address line change here
                          " " +strTempSASN +" " +strSATH +"%' AND " + "(A.ADDR_LINE2 LIKE '%" +
                          strLD1 +"%' OR A.ADDR_LINE3 LIKE '%" +strLD1 +"%'))  " +
                          " OR    (A.ADDR_LINE3 LIKE '" +strSAPR +" " +strTempSANO +" " +
                          strTempSASN +" " +strSATH +"%' AND " + " (A.ADDR_LINE3 LIKE '%" +
                          strLD1 + "%' OR NVL(A.ADDR_LINE4,'') LIKE '%" +strLD1 +"%')) ) ";
                     }
                  }
                  strWtnQuery +=
                  " AND (A.ADDR_ID ='SAD1' OR A.ADDR_ID = 'MAIN') AND B.CUST_PHONE=A.CUST_PHONE " +
                  " AND B.CUST_INSRV_DATE=A.CUST_INSRV_DATE AND B.ORG_EXCH NOT IN ('FICT','FRGN') " +//check datechange
                  " ORDER BY 2 DESC, 1 DESC ";
                  Log.write("FICT/FRGN querying =[" + strWtnQuery + "]");
                  rs = stmtCAMS.executeQuery(strWtnQuery);
                  if (rs.next() == true)
                  {
                     Log.write("FICT/FRGN new NPANXX=[" + rs.getString(1)+"-"+rs.getString(2)+"]");
                     pStmt.setString(1, rs.getString(1)+rs.getString(2));   //LSO or NPANXX
                  }

               }//FICT/FRGN

               pStmt.executeUpdate();

            }//end-if
            else   //insert empty record
            {
               //Now clean up any prior records
               m_stmt.executeUpdate("DELETE FROM AVR_SA_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr +
                        " AND PRE_ORDR_VRSN = " + iVersion);
               Log.write(Log.DEBUG_VERBOSE, "Serv Address NOT Found - insert dummy record");
               try {
                  m_stmt.executeUpdate("INSERT INTO AVR_SA_T (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN,"+
                             " FRM_SCTN_OCC, MDFD_DT, MDFD_USERID) VALUES (" + iPreSqncNmbr +
                             ", " + iVersion + ",1, sysdate, 'auto')");

               }
               catch(SQLException se) {
                  Log.write(Log.WARNING, "PreorderBean : INSERT Exception2 in doAddressValidation()");
                  iReturnCode = -101;
               }
            }
            Log.write("Done querying CAMS DB2 data warehouse now ...");

         }
         try {   rs.close();
         }
         catch (Exception e) {}
      }
      catch (SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQLException2 in doAddressValidation() se=" + se);
         iReturnCode = DB2Unavailable(se);
      }
      catch (Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception2 in doAddressValidation() e=" + e);
         e.printStackTrace();
         iReturnCode = -2;
      }
      finally
      {
                   DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
      }
       
      rs = null;

      if (iReturnCode < 0)
      {
         if (iReturnCode == DB2_UNAVAILABLE)
         {
            strRemark = " Host system unavailable, please try transaction later ";
            completeAddressValidation(iPreSqncNmbr, iVersion, "014",
                  "Host system unavailable to process Transaction", strRemark);
            iReturnCode = 0;
         }
         return iReturnCode;
      }

      String strUpdate="UPDATE AVR_T SET ";
      String strRespc="003";
      String strRespd="Address match found";
      if (bFound)
      {
          //insert log message here
         if (bInServiceTN)
         {   
             //insert log message here
             Log.write("Log message 1");
             strUpdate += " AVR_WSOPI='Y', ";
            //Per Tana - dont show WTN on a match - so xxxx out the line number
            if (!bWTN)
            {   //Put the WTN we found in CAMS onto the result table
               //strUpdate += " AVR_WTN='" + strWTN.substring(0,3) + "-" + strWTN.substring(3,6) + "-" + strWTN.substring(6,10) + "', ";
               //strUpdate += " AVR_WTN='" + strWTN.substring(0,3) + "-" + strWTN.substring(3,6) + "-xxxx', ";
               strUpdate += " AVR_WTN='  ', ";
            }
         }
         else
         {
            //insert log message here
             Log.write("Log message 2");
            strUpdate += " AVR_WSOPI='N', ";
         }
      }
      else
      {
         //insert log message here
          Log.write("Log message 3");
         strUpdate += " AVR_WSOPI='N', ";
         if (bWTN)
         {
            LSRdao lsrDao = new LSRdao();

            String sent_tn = lsrDao.checkNuvoxTNStatus(strWTN);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean() from db sent_tn="+sent_tn);

            if(sent_tn != null) {

               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Non-Windstream TN ("+strWTN+")");
               strRemark = "Submit all CSR requests for spids 8660, 4890 and 8934 to csrrequests@windstream.com";
               strRespc = "033";
               strRespd = "This is a Non-Windstream Customer";
               updatePreorderICARE(iPreSqncNmbr, iVersion);
	    } else {
                strRespc = "033";
                strRespd = "Telephone number found in windstream";
            }
         }
         else
         {
            LSRdao lsrDao = new LSRdao();

            String sent_tn = lsrDao.checkNuvoxTNStatus(strWTN);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean() from db sent_tn="+sent_tn);

            if(sent_tn != null) {

               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Non-Windstream TN ("+strWTN+")");
               strRemark = "Submit all CSR requests for spids 8660, 4890 and 8934 to csrrequests@windstream.com";
               strRespc = "033";
               strRespd = "This is a Non-Windstream Customer";
               updatePreorderICARE(iPreSqncNmbr, iVersion);
            } else {
                strRespc = "006";
                strRespd = "Address found in windstream";
                //NOTE This may get overlaid below based on MSAG/FrontWare results
            }
      }
      }
      strUpdate += " AVR_RESPC = '"+strRespc+"', AVR_RESPD='"+strRespd+"', AVR_REMARKS='" + strRemark + "', " +
         "MDFD_DT=sysdate, MDFD_USERID = 'auto' WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion;
      try {
         m_stmt.executeUpdate(strUpdate);
      }
      catch(SQLException se) {
         Log.write(Log.WARNING, "PreorderBean : Update Exception in doAddressValidation() se=["+se+"]");
         iReturnCode = -101;
      }
      if (iReturnCode < 0)
      {
         return iReturnCode;
      }

      // If Service Address NOT FOUND and the user DIDN'T enter a WTN, then check for a valid MSAG address range
      // in the FrontWare system.
      if (!bFound && !bWTN)
      {
         iReturnCode = getMSAGRange(iPreSqncNmbr, iVersion, strSASD, strSANO, strSASN, strSATH, strCITY, strSTATE, strCounty);
         if (iReturnCode > 0)
         {
            completeAddressValidation(iPreSqncNmbr, iVersion, "005",
                     "Address Near Match found / Alternatives Provided",
                     strRemark+" alternative(s) found in MSAG");
         }
      }

      return iReturnCode;

   }

   /**
    * After an unsuccessful search for Service Address in the CAMS data warehouse, we now need to check the MSAG
    * data contained in the FrontWare database. This MSAG data has street ranges (ie 1-10 Main St) that define
    * valid addresses.  This logic is similar to the logoci followed by the Frontware application.
    * 1. Read County and Community table - if not found or MSAG-VALID = 'N', exit.
    * 2. Get Street # and Street directional
    * 3. Check MSAG for valid range
    * 4. If one hit, build results and exit.
    *    If multiple hits, exit
    *    If no hits, then concatenate street directional and street name and try query again.
    * 5. If one hit, build results and exit.
    *    If multiple hits, exit
    *    If no hits, then concatenate street name and street and try query again
    * 6. If one hit, build results and exit.
    *    If multiple hits, exit
    *    If no hits, then just try to find the stinking street (with no street #)
    * 7. If multiple hits, build results and exit.
    *    If no hits, exit.
    *
         * @param   int    Preorder Sequence Number.
         * @param   int    Preorder Version Number.
    * @param   Strings   SASD, SANO, SASN, SATH, City, State, County
    * @return  int ReturnCode 0 = OK, but no MSAG found,  1 = MSAG range(s) found, < 0 Error encountered
    */
   private int getMSAGRange(int iPreSqncNmbr, int iVer, String strSASD, String strSANO, String strSASN, String strSATH,
             String strCITY, String strSTATE, String strCounty)
   {
      int iReturnCode = 0;
      boolean bContinue = true;
      String strCommunity="";
      String strDir = "  ";
      String strEvenOdd ="B";

      Connection connFW = null;
      Statement stmtFW = null;
      ResultSet rs = null;

      Log.write("getMSAG() SANO=["+strSANO+"] StreetName=["+strSASN+"] Street=["+strSATH+"]");
      if (strSANO == null || strSANO.length() == 0)
      {
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : MSAG SANO not valid");
         iReturnCode = 0;
         return iReturnCode;
      }

      // Check for valid State,County and Community (City)
      try
      {
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Checking FRONTWARE ");
         connFW = DatabaseManager.getConnection(DatabaseManager.FWP_CONNECTION);
         stmtFW = connFW.createStatement();

         rs = stmtFW.executeQuery("SELECT MSAG_VALID, NVL(COMMUNITY,'_NOT_FOUND_') FROM FW.COUNTY A, FW.COMMUNITY B " +
            " WHERE A.STATE='" + strSTATE + "' AND A.COUNTY='" + strCounty + "' AND B.STATE(+)=A.STATE AND " +
            " B.COUNTY(+)=A.COUNTY AND B.COMMUNITY(+)='" + strCITY + "' ");
         if (rs.next()==true)
         {   
        	String strMsagValid = rs.getString(1);
            strCommunity = rs.getString(2);
            if ( strMsagValid.equals("N") || strCommunity.equals("_NOT_FOUND_") )
            {   //just leave
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : MSAG not avail OR State/County/Community not found");
               iReturnCode = 0;
               bContinue = false;
            }
            else
            {   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : MSAG County/Community was found");
            }
         }
         else
         {   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : MSAG rec not found for ST["+strSTATE+
                  "] COUNTY[" + strCounty+"] CITY["+strCITY+"]");
            bContinue = false;
            iReturnCode = 0;
         }
         rs.close();
         } //try
         catch (Exception e)
         {
         Log.write(Log.ERROR, "PreorderBean : Exception1 in getMSAG() e=[" + e + "]");
         bContinue = false;
         iReturnCode = -1;
         }

      if (bContinue)
      {
          //County & Community was found, now check for street range hit
         strDir = "  ";
         strEvenOdd = "B";
         if (isInteger(strSANO)) {
             if ((Integer.parseInt(strSANO)) % 2 == 0) {
                 strEvenOdd = "E";
             }else {
                 strEvenOdd = "O";
             }
         }
         if (strSASD != null)
         {   strDir = strSASD;
         }
         //OK, we have our number and directional, Check for a valid range now!
         try
         {
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean: MSAG look for exact hit");
            
            rs = stmtFW.executeQuery("SELECT LOW_RANGE, HIGH_RANGE FROM FW.MSAG WHERE STATE='"+strSTATE+"' "+
                    "AND COUNTY='"+strCounty+"' AND COMMUNITY='"+strCommunity+"' AND " +
                    "STREET_NAME='"+strSASN+"' AND DIRECTIONAL='"+strDir+"' "+
                    "AND EVEN_ODD_IND IN ('" + strEvenOdd + "','B') ");
            
            Map<String, String> rangeMap = getMASGSANORange(strSANO, rs, false);
            boolean rangeExists = Boolean.parseBoolean(rangeMap.get(RANGE_EXISTS));
            	
            if (rangeExists)
            { 
            	int iCount = Integer.parseInt(rangeMap.get(COUNT));
            	
               if (iCount == 1)   // Cool, match found, now get data, set results and get out
               {   Log.write(Log.DEBUG_VERBOSE, "MSAG range found on 1st try!");
               	  String strSANOR = rangeMap.get(LOW_RANGE) + "-" + rangeMap.get(HIGH_RANGE);
                  if (strSANOR.length() > 17) strSANOR = strSANOR.substring(0,16);
                  m_stmt.executeUpdate("DELETE FROM AVR_SA_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr +
                             " AND PRE_ORDR_VRSN = " + iVer);
                  m_stmt.executeUpdate("INSERT INTO AVR_SA_T " +
                     " (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, AVR_SANOR, AVR_SASD, AVR_SASN,"+
                     "  AVR_CITY, AVR_STATE, AVR_COUNTY, MDFD_DT, MDFD_USERID) " +
                     " VALUES ("+iPreSqncNmbr+","+iVer+", 1, '"+strSANOR+"', '"+strDir+"', '"+strSASN+"', '" +
                       strCommunity+ "', '"+strSTATE+"', '"+strCounty+"', sysdate, 'auto')");
                  //just drop out now, we're done
                  bContinue = false;
                  iReturnCode = 1;
               }
               else if (iCount == 0) // Still no hit
               {   //will continue with other searches
               }
               else
                  bContinue = false;   //multiple hits means NO dice
            }
            else
            {   Log.write(Log.ERROR, "PreorderBean : getMSAG() db read error");
               iReturnCode = -2;    //Errror reading DB
               bContinue = false;
            }
         }//try
         catch (Exception e)
         {
            Log.write(Log.ERROR, "PreorderBean : Exception3 in getMSAG() e=[" + e + "]");
            bContinue = false;
            iReturnCode = -3;
         }
      }

      if (bContinue)
      {
         //Next search, we'll combine street direction and street name.
         //So we'll look for "S MAIN" instead of "MAIN"
         if (strSASD != null)
         {   strDir="  ";
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean: MSAG lookup 2");
            try
            {   
            	rs = stmtFW.executeQuery("SELECT LOW_RANGE, HIGH_RANGE FROM FW.MSAG WHERE STATE='" +strSTATE+
                        "' AND COUNTY='"+strCounty+"' AND COMMUNITY='"+strCommunity+
                        "' AND STREET_NAME='"+strSASD+" "+strSASN+"' AND DIRECTIONAL='"+strDir+
                        "' AND EVEN_ODD_IND IN ('"+strEvenOdd+"','B') ");
            	
            	Map<String, String> rangeMap = getMASGSANORange(strSANO, rs, false);
            	boolean rangeExists = Boolean.parseBoolean(rangeMap.get(RANGE_EXISTS));
            	if (rangeExists)
            	{
            	  int iCount = Integer.parseInt(rangeMap.get(COUNT));
            		
                  if (iCount == 1)   //Cool, set results and get out
                  {   
                	 Log.write(Log.DEBUG_VERBOSE, "MSAG hit on 2nd try ");
                     String strSANOR = rangeMap.get(LOW_RANGE) + "-" + rangeMap.get(HIGH_RANGE);
                     if (strSANOR.length() > 17) strSANOR = strSANOR.substring(0,16);
                     m_stmt.executeUpdate("DELETE FROM AVR_SA_T WHERE PRE_ORDR_SQNC_NMBR = " +
                           iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVer);
                     m_stmt.executeUpdate("INSERT INTO AVR_SA_T " +
                       " (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, AVR_SANOR, AVR_SASD,"+
                       " AVR_SASN, AVR_CITY, AVR_STATE, AVR_COUNTY, MDFD_DT, MDFD_USERID) " +
                       " VALUES ("+iPreSqncNmbr+","+iVer+", 1, '"+strSANOR+"', '"+strDir+"', '"+
                         strSASN+"', '" +strCommunity+ "', '"+strSTATE+"', '"+strCounty+"'," +
                       " sysdate, 'auto')");
                     //just drop out now, we're done
                     bContinue = false;
                     iReturnCode = 1;
                  }
                  else if (iCount == 0) // Still no hit
                  {   //will continue with other searches
                  }
                  else
                  {   bContinue = false;   //multiple hits means NO dice
                  }
               }
               else
               {   Log.write(Log.ERROR, "PreorderBean : getMSAG() db read error 4");
                  iReturnCode = -4;    //Errror reading DB
                  bContinue = false;
               }
            }//try
            catch (Exception e)
            {
               Log.write(Log.ERROR, "PreorderBean : Exception5 in getMSAG() e=[" + e + "]");
               bContinue = false;
               iReturnCode = -5;
            }
         }
      }

      if (bContinue)
      {
         //Next search, we'll combine Street name and street type
         //So we'll look for "MAIN ST%" instead of "MAIN"
         if (strSATH != null && strSATH.length()>0)
         {   strDir="  ";
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean: MSAG lookup 3");
            try
            {   
            	
            	rs = stmtFW.executeQuery("SELECT LOW_RANGE, HIGH_RANGE, STREET_NAME FROM FW.MSAG WHERE STATE='" +strSTATE+
                    "' AND COUNTY='"+strCounty+"' AND COMMUNITY='"+strCommunity+
                    "' AND STREET_NAME LIKE '"+strSASN+" "+strSATH+"%' AND DIRECTIONAL='"+strDir+
                    "' AND EVEN_ODD_IND IN ('"+strEvenOdd+"','B') ");
            	
            	  
            	Map<String, String> rangeMap = getMASGSANORange(strSANO, rs, true);
            	boolean rangeExists = Boolean.parseBoolean(rangeMap.get(RANGE_EXISTS));
            		
            	if (rangeExists)
                {
                	int iCount = Integer.parseInt(rangeMap.get(COUNT));
            		
                  if (iCount == 1)   //Cool, set results and get out
                  {   
                	 Log.write(Log.DEBUG_VERBOSE, "MSAG hit on 3rd try ");
                     String strSANOR = rangeMap.get(LOW_RANGE) + "-" + rangeMap.get(HIGH_RANGE);
                     String strStreet = rangeMap.get(STREET);
                     if (strSANOR.length() > 17) strSANOR = strSANOR.substring(0,16);
                     m_stmt.executeUpdate("DELETE FROM AVR_SA_T WHERE PRE_ORDR_SQNC_NMBR = " +
                           iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVer);
                     m_stmt.executeUpdate("INSERT INTO AVR_SA_T " +
                       " (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, AVR_SANOR, AVR_SASD,"+
                       " AVR_SASN, AVR_CITY, AVR_STATE, AVR_COUNTY, MDFD_DT, MDFD_USERID) " +
                       " VALUES ("+iPreSqncNmbr+","+iVer+", 1, '"+strSANOR+"', '"+strDir+"', '"+
                         strStreet+"', '" +strCommunity+ "', '"+strSTATE+"', '"+strCounty+"'," +
                       " sysdate, 'auto')");
                     //just drop out now, we're done
                     bContinue = false;
                     iReturnCode = 1;
                  }
               }
               else
               {   Log.write(Log.ERROR, "PreorderBean : getMSAG() db read error 6");
                  iReturnCode = -6;    //Errror reading DB
                  bContinue = false;
               }
            }//try
            catch (Exception e)
            {
               Log.write(Log.ERROR, "PreorderBean : Exception7 in getMSAG() e=[" + e + "]");
               bContinue = false;
               iReturnCode = -7;
            }
         }
      }

      if (bContinue)
      {
         //Last ditch attempt..... find the stinking street
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : MSAG just trying to find street now");
         if (strSASD != null)
         {   strDir = strSASD;
         }
         else
         {   strDir = "  ";
         }
         String strLastTry = "SELECT LOW_RANGE, HIGH_RANGE, STREET_NAME "+
                                        " FROM FW.MSAG WHERE STATE='"+strSTATE+"' AND "+
                                        "COUNTY='"+strCounty+"' AND COMMUNITY='"+strCommunity+
                                        "' AND STREET_NAME LIKE '"+strSASN+"%' AND DIRECTIONAL='"+strDir+
                                        "' AND EVEN_ODD_IND IN ('"+strEvenOdd+"','B') ";
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Msag ["+strLastTry+"]");
         try
         {   rs = stmtFW.executeQuery(strLastTry);
            int iOcc=1;
            while(rs.next())   //suggest all the ranges ...
            {   
               String low_range = rs.getString(1).trim();
               String high_range = rs.getString(2);
                                  
               if(high_range != null){
                   high_range = high_range.trim();
               }
               String strSANOR = low_range + "-" + high_range;
               String strStreet = Toolkit.replaceSingleQwithDoubleQ(rs.getString(3));
               if (iOcc==1) {
                  m_stmt.executeUpdate("DELETE FROM AVR_SA_T WHERE PRE_ORDR_SQNC_NMBR = " +
                      iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVer);
               }
               strLastTry = "INSERT INTO AVR_SA_T (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, AVR_SANOR, AVR_SASD, AVR_SASN,"+
                                          "  AVR_CITY, AVR_STATE, AVR_COUNTY, MDFD_DT, MDFD_USERID) " +
                                          " VALUES ("+iPreSqncNmbr+","+iVer+","+iOcc+" , '"+strSANOR+"', '"+strDir+"', '"+
                                            strStreet+"', '" +strCommunity+ "', '"+strSTATE+"', '"+strCounty+"', sysdate, 'auto')";
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Msag got somethin ["+strLastTry+"]");
               m_stmt.executeUpdate(strLastTry);
               iOcc++;
               iReturnCode = 1;
               bContinue = false;
               if (iOcc>5) break;  //5 is plenty
            }
         }
         catch (Exception e)
         {
            Log.write(Log.ERROR, "PreorderBean : Exception8 in getMSAG() e=[" + e + "]");
            bContinue = false;
            iReturnCode = -8;
         }
      }

      rs = null;
                  DatabaseManager.releaseConnection(connFW, DatabaseManager.FWP_CONNECTION);

      return iReturnCode;
   }

   /**
    * private int getFeatures()
    * Get features/services - this stuff is typically displayed on the PREC/BREC screens in CAMS.
    * Put results in CSIR_FS_T table.
    *
         * @param   int    Preorder Sequence Number.
         * @param   int    Preorder Version Number.
    * @param   String    ATN
         * @param   Statement   SQL Statement object
    * @param   Strings   Keys for ORG
    * @return  int ReturnCode    0 = OK
    *            <0  Error
    */
   private int getFeatures(int iPreSqncNmbr, int iVersion, String strATN, Statement myStmt,
            String strOrgRegion, String strOrgState, String strOrgComp, String strOrgDist, String strOrgBusOff,
            String strCustPhone, String strCustInSvcDate,  boolean bATN, boolean bAN)
   {
      int iReturnCode = 0;
      ResultSet rs = null;
      try {
         int iFeatureCount = 0;   //# features
         int iOcc = 1;      //form occurrence
         int iPrepOcc = 2;   //for Prepare stmt
         String strDate = Toolkit.getDateTime();
         strDate = strDate.substring(0,10);

         //Get rid of empty record
         m_stmt.executeUpdate("DELETE FROM CSIR_FS_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
         PreparedStatement pStmt = m_conn.prepareStatement("INSERT INTO CSIR_FS_T VALUES ("+iPreSqncNmbr+","+iVersion+",?, " +
             "?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?, " +
             "?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?, " +
             " sysdate, 'auto')");

         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : CSI ASOCs date=[" + strDate + "]");
         String strASOCQry = "";

         //per Sheryl Holt - return all TNs regardless of how search was performed
      if (bATN || bAN)
      {
//         strASOCQry = "SELECT S.SENT_PHONE, S.ASOC_ID, S.START_DATE, S.STOP_DATE, S.SUNIT_QTY, A.ASOC_NAME " +
//            " FROM DB2.CAMS_SUNITT S, DB2.CAMS_ASOCT A " +
//            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP = '" + strOrgComp +
//            "' AND S.ORG_DIST = '" + strOrgDist + "' " + " AND S.ORG_BUSOFF = '" + strOrgBusOff + "' AND S.CUST_PHONE = '" +
//            strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSvcDate + "' " +
////HD299878            " AND S.BENT_PHONE='" + strATN + "' " +
////HD0000002472840S
////					   " AND S.STOP_DATE > '" + strDate + "' AND A.ASOC_ID=S.ASOC_ID AND A.ASOC_REG = 'R' ORDER BY 1, 2 ";
//					   " AND S.STOP_DATE > '" + strDate + "' AND A.ASOC_ID=S.ASOC_ID ORDER BY 1, 2 ";//datechange
////HD0000002472840F
                  strASOCQry = "SELECT S.SENT_PHONE, S.ASOC_ID, to_char(S.START_DATE,'yyyy-mm-dd'), to_char(S.STOP_DATE,'yyyy-mm-dd'), S.SUNIT_QTY, A.ASOC_NAME " +
                          //had to date formatting on start stop dates above as express csir table date fields can take only 10 chars and in this format - Antony - 04/25/2014
            " FROM KASH.CAMS_SUNITT S, KASH.CAMS_ASOCT A " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP = '" + strOrgComp +
            "' AND S.ORG_DIST = '" + strOrgDist + "' " + " AND S.ORG_BUSOFF = '" + strOrgBusOff + "' AND S.CUST_PHONE = '" +
            strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSvcDate + "',0,10),'yyyy-mm-dd')" +
//HD299878            " AND S.BENT_PHONE='" + strATN + "' " +
//HD0000002472840S
//					   " AND S.STOP_DATE > '" + strDate + "' AND A.ASOC_ID=S.ASOC_ID AND A.ASOC_REG = 'R' ORDER BY 1, 2 ";
                                            //adding to_date function as date is in yyyy-mm-dd format as in CAMS/DB2 - Antony - 04/24/2014
					   " AND S.STOP_DATE >  to_date('" + strDate + "','yyyy-mm-dd') AND A.ASOC_ID=S.ASOC_ID ORDER BY 1, 2 ";
  Log.write(Log.DEBUG_VERBOSE, "PreorderBean: To Get Sent Phone, ASOC ID, Start Date, Stop Date, ASOC Name from table CAMS_SUNITT, CAMS_ASOCT based on Sent Phone Number. Else Loop."+strASOCQry);
  Log.write("KASH.QUERY7.1:"+strASOCQry);
//HD0000002472840F

      }
      else
      {
//         strASOCQry = "SELECT S.SENT_PHONE, S.ASOC_ID, S.START_DATE, S.STOP_DATE, S.SUNIT_QTY, A.ASOC_NAME " +
//            " FROM DB2.CAMS_SUNITT S, DB2.CAMS_ASOCT A " +
//            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP = '" + strOrgComp +
//            "' AND S.ORG_DIST = '" + strOrgDist + "' " + " AND S.ORG_BUSOFF = '" + strOrgBusOff + "' AND S.SENT_PHONE = '" +
//            strATN + "' AND S.CUST_INSRV_DATE='" + strCustInSvcDate + "' " +
//            " AND S.STOP_DATE > '" + strDate + "' AND A.ASOC_ID=S.ASOC_ID ORDER BY 1, 2 ";
         strASOCQry = "SELECT S.SENT_PHONE, S.ASOC_ID, to_char(S.START_DATE,'yyyy-mm-dd'), to_char(S.STOP_DATE,'yyyy-mm-dd'), S.SUNIT_QTY, A.ASOC_NAME " +
                 //had to date formatting on start stop dates above as express csir table date fields can take only 10 chars and in this format - Antony - 04/25/2014
            " FROM KASH.CAMS_SUNITT S, KASH.CAMS_ASOCT A " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP = '" + strOrgComp +
            "' AND S.ORG_DIST = '" + strOrgDist + "' " + " AND S.ORG_BUSOFF = '" + strOrgBusOff + "' AND S.SENT_PHONE = '" +
            strATN + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSvcDate + "',0,10),'yyyy-mm-dd')" +
            " AND S.STOP_DATE > to_date('" + strDate + "','yyyy-mm-dd') AND A.ASOC_ID=S.ASOC_ID ORDER BY 1, 2 ";
     Log.write(Log.DEBUG_VERBOSE, "PreorderBean: To Get Sent Phone, ASOC ID, Start Date, Stop Date, ASOC Name from table CAMS_SUNITT, CAMS_ASOCT based on Sent Phone Number. Else Loop."+strASOCQry);
     Log.write("KASH.QUERY7.2:"+strASOCQry);

      }

            //Changed above line per Tana Henson/Louis Byers - only show 'R' regulated
            //Used to be below line.
            //"' AND A.ASOC_ID=S.ASOC_ID ORDER BY 1, 2 ";

         rs = myStmt.executeQuery(strASOCQry);
         while (rs.next())
         {
            iFeatureCount++;   //Each occurence holds 10 features -and there are 6 fields per feature
            Log.write(Log.DEBUG_VERBOSE, "Feature [" + iFeatureCount + "] found [" +
                rs.getString(1) + "] [" + rs.getString(2) + "]");

            pStmt.setInt(1, iOcc);
            pStmt.setString(iPrepOcc++, rs.getString(1) );      //tn
            if ((rs.getString(2)).length() > 6) {         //asoc
               pStmt.setString(iPrepOcc++, (rs.getString(2)).substring(0,6) );
            } else {
               pStmt.setString(iPrepOcc++, rs.getString(2) );
            }
            pStmt.setString(iPrepOcc++, rs.getString(3) );      //start date
            pStmt.setString(iPrepOcc++, rs.getString(4) );      //stop date
            if ((rs.getString(5)).length() > 2) {         //qty
               pStmt.setString(iPrepOcc++, (rs.getString(5)).substring(0,2) );
            } else {
               pStmt.setString(iPrepOcc++, rs.getString(5) );
            }
            pStmt.setString(iPrepOcc++, rs.getString(6) );      //asoc name
            if (iFeatureCount == 10)
            {   pStmt.executeUpdate();
               iOcc++;
               iPrepOcc=2;
               iFeatureCount=0;
            }

         }//end while()
         if (iFeatureCount > 0)
         {    while (iFeatureCount < 10)   //fill in empty vars in Prep stmt
            {
               pStmt.setString(iPrepOcc++, "");
               pStmt.setString(iPrepOcc++, "");
               pStmt.setString(iPrepOcc++, "");
               pStmt.setString(iPrepOcc++, "");
               pStmt.setString(iPrepOcc++, "");
               pStmt.setString(iPrepOcc++, "");
               iFeatureCount++;
            }
            pStmt.executeUpdate();
         }
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : CSI features/services done");

      }
      catch (SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQL Exception in getFeatures() se=" + se + "]");
         iReturnCode = DB2Unavailable(se);
      }
      catch (Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception in getFeatures() e=[" + e + "]");
         iReturnCode = -2;
      }
         finally {
         try {
            rs.close();
         }
         catch (Exception e1) {}
         rs = null;
         }

      return iReturnCode;
   }

   /**
    * private int getDirectoryInfo()
    * Get directory listing information. This info is extracted daily from DCRIS (based on service order
    * activity) and FTP-ed to Express.  Express then uploads into Express database. The records recd in
    * upload are a complete replacement of any previous reocrds for that TN.
    * Put results in CSIR_LA_T table.
    *
         * @param   int    Preorder Sequence Number.
         * @param   int    Preorder Version Number.
    * @param   String    ATN
         * @param   Statement   SQL Statement object
    * @param   Strings   Keys for ORG
    * @return  int ReturnCode    0 = OK
    *            <0  Error
    */
   private int getDirectoryInfo(int iPreSqncNmbr, int iVersion, String strATN, Statement myStmt,
                 String strOrgRegion, String strOrgState, String strCustPhone, String strCustInSrvDate, boolean bAN)
   {
      int iReturnCode = 0;
      ResultSet rs = null;

      try
      {
         int iSeqCount = 0;   //# features
         int iOcc = 1;      //form occurrence
         int iPrepOcc = 3;   //for Prepare stmt

         //Get rid of empty record
         m_stmt.executeUpdate("DELETE FROM CSIR_LA_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
         PreparedStatement pStmt = m_conn.prepareStatement("INSERT INTO CSIR_LA_T (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, CSIR_LTY, CSIR_TOA, CSIR_LTN, " +
            " CSIR_LTXNUM_1, CSIR_LTEXT_1, CSIR_LTXNUM_2, CSIR_LTEXT_2, CSIR_LTXNUM_3, CSIR_LTEXT_3, CSIR_LTXNUM_4, CSIR_LTEXT_4, CSIR_LTXNUM_5, CSIR_LTEXT_5, " +
            " CSIR_LTXNUM_6, CSIR_LTEXT_6, CSIR_LTXNUM_7, CSIR_LTEXT_7, CSIR_LTXNUM_8, CSIR_LTEXT_8, CSIR_LTXNUM_9, CSIR_LTEXT_9, CSIR_LTXNUM_10, CSIR_LTEXT_10, MDFD_DT, MDFD_USERID) " +
            " VALUES ("+iPreSqncNmbr+","+iVersion+",?, '', ?, '', ?,?, ?,?, ?,?, ?,?, ?,?, ?,?, ?,?, ?,?, ?,? ,?,?, sysdate, 'auto')");

         //Read the S13 record to get the type of account
         String strTOA = "";
       if (bAN)
       {
         rs = m_stmt.executeQuery("SELECT SUBSTR(DL_DTL_LST,5,1) FROM DIRECTORY_LIST_T " +
                   " WHERE DL_BILL_TN='" + strCustPhone + "' AND DL_RCRD_CD='S13'");
       }
       else
       {
         rs = m_stmt.executeQuery("SELECT SUBSTR(DL_DTL_LST,5,1) FROM DIRECTORY_LIST_T " +
                   " WHERE DL_BILL_TN='" + strATN + "' AND DL_RCRD_CD='S13'");
       }

         if (rs.next()==true)
         {
            strTOA = rs.getString(1);
            //Now read all the S14s and fill results
          if (bAN)
          {
            rs = m_stmt.executeQuery("SELECT SUBSTR(DL_DTL_LST,1,2), SUBSTR(DL_DTL_LST,3) " +
               " FROM DIRECTORY_LIST_T WHERE DL_BILL_TN='" + strCustPhone +
               "' AND DL_RCRD_CD='S14' ORDER BY DL_SRT_SQNC ASC");
	      }
	      else
	      {
            rs = m_stmt.executeQuery("SELECT SUBSTR(DL_DTL_LST,1,2), SUBSTR(DL_DTL_LST,3) " +
               " FROM DIRECTORY_LIST_T WHERE DL_BILL_TN='" + strATN +
               "' AND DL_RCRD_CD='S14' ORDER BY DL_SRT_SQNC ASC");
	      }
            while(rs.next())
            {
               iSeqCount++;      //Each occurence holds 10 seqs
               pStmt.setInt(1, iOcc);
               pStmt.setString(2, strTOA);    //same for all
               pStmt.setString(iPrepOcc++, rs.getString(1) );   //txnum
               pStmt.setString(iPrepOcc++, rs.getString(2) );   //text
               if (iSeqCount == 10)
               {   pStmt.executeUpdate();
                  iOcc++;
                  iPrepOcc=3;
                  iSeqCount=0;
               }
            }//end while()
            if (iSeqCount > 0)
            {   while (iSeqCount < 10)   //fill in empty ones
               {
                  pStmt.setString(iPrepOcc++, "");
                  pStmt.setString(iPrepOcc++, "");
                  iSeqCount++;
               }
               pStmt.executeUpdate();
            }
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : getDirectoryInfo() done ");
         }//if
         else   //directory listing crap not found, put something in results to let vendor know that...
         {
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : getDirInfo() -listings not found");
            m_stmt.executeUpdate("INSERT INTO CSIR_LA_T (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, " +
               " CSIR_LTEXT_1, MDFD_DT, MDFD_USERID) VALUES (" + iPreSqncNmbr + "," + iVersion +
               ",1,'Directory Listing records not found for TN', sysdate, 'auto')");
         }

         try {
            rs.close();
         }
         catch (Exception e1) {}

         //See if a SIC-CODE trait exists. If so, set values in results
//         rs = myStmt.executeQuery("SELECT TRAIT_TXT FROM DB2.CAMS_TRAITT WHERE ORG_REGION='" + strOrgRegion +
//            "' AND ORG_STATE='" + strOrgState + "' AND CUST_PHONE='" + strCustPhone + "' AND CUST_INSRV_DATE='" +//datechange
//             strCustInSrvDate + "' AND TRAIT_NAME='SIC-CODE'");
         String strQuery3 = "SELECT TRAIT_TXT FROM KASH.CAMS_TRAITT WHERE ORG_REGION='" + strOrgRegion +
            "' AND ORG_STATE='" + strOrgState + "' AND CUST_PHONE='" + strCustPhone +
            "' AND CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND TRAIT_NAME='SIC-CODE'";

        Log.write(Log.DEBUG_VERBOSE, "PreorderBean : To check whether trait exists based on the condition TRAIT_NAME='SIC-CODE'."+strQuery3);
        Log.write("KASH.QUERY8:"+strQuery3);

         rs = myStmt.executeQuery("SELECT TRAIT_TXT FROM KASH.CAMS_TRAITT WHERE ORG_REGION='" + strOrgRegion +
            "' AND ORG_STATE='" + strOrgState + "' AND CUST_PHONE='" + strCustPhone + 
            "' AND CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND TRAIT_NAME='SIC-CODE'");
        // Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Query Line # 2125 : To check whether trait exists based on the condition TRAIT_NAME='SIC-CODE'."+rs);
         if (rs.next()==true)
         {
            String strTemp = (rs.getString(1)).trim();
            if (strTemp.length() > 6)
               strTemp = strTemp.substring(0,6);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : SIC CODE trait found =[" + strTemp + "]");
            if (strTemp.length() > 0 && strTemp != null)
            {   //Set ALL occurences with this SIC CODE
               m_stmt.executeUpdate("UPDATE CSIR_LA_T SET CSIR_SIC='" + strTemp +
                  "' WHERE PRE_ORDR_SQNC_NMBR='" + iPreSqncNmbr + "' AND PRE_ORDR_VRSN=" + iVersion);
            }
         }

      }//try
      catch (SQLException se)
      {   Log.write(Log.ERROR,"PreorderBean : SQLException in getDirectoryInfo() ["+se+"]");
         iReturnCode = DB2Unavailable(se);
      }
      catch (Exception e)
      {   Log.write(Log.ERROR,"PreorderBean : Exception in getDirectoryInfo() [" + e + "]");
         iReturnCode = -2;
      }
         finally {
         try {
            rs.close();
         }
         catch (Exception e1) {}
         rs = null;
         }

      return iReturnCode;
   }

   /**
    * private int getTNsAndPICs()
    * Get all the served TNs for this CSI and get the last PIC and IPIC for each.
    * Put results in CSIR_PIC_T table.
    *
         * @param   int    Preorder Sequence Number.
         * @param   int    Preorder Version Number.
    * @param   String    ATN
         * @param   Statement   SQL Statement object
    * @param   Strings   Keys for ORG
    * @return  int ReturnCode    0 = OK
    *            <0  Error
    */
   private int getTNsAndPICs(int iPreSqncNmbr, int iVersion, String strATN, Statement myStmt,
            String strOrgRegion, String strOrgState, String strOrgComp, String strOrgDist, String strOrgBusOff,
            String strCustPhone, String strCustInSrvDate, boolean bATN, boolean bAN)
   {
      int iReturnCode = 0;
      ResultSet rs = null;

      try {
         int iTNCount = 1;   //# TNs on this occurrence
         int iOcc = 1;      //form occurrence
         int iPrepOcc = 2;   //field # for Prepare stmt
         String strTN = "";
         String strSaveTN = "";
         String strPicType = "";

         //Get list of served phones, their PIC/LPIC choices and their effective dates AND spin thru them
      if (bAN)
      {
//         rs = myStmt.executeQuery("SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
//            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM DB2.CAMS_SUNITT S, DB2.CAMS_TRAITT T " +
//            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
//            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
//            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC') AS X GROUP BY X.SENT_PHONE, X.PIC, X.PICVAL " +
//            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
//            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM DB2.CAMS_SUNITT S, DB2.CAMS_TRAITT T " +
//            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
//            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
//            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC') AS X GROUP BY X.SENT_PHONE, X.PIC, X.PICVAL " +//datechange
//            " ORDER BY 1,4,2 desc, 3");
             
             Log.write("Query if bAN : SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            //" AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC') AS X GROUP BY X.SENT_PHONE, X.PIC, X.PICVAL " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            //" AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC') AS X GROUP BY X.SENT_PHONE, X.PIC, X.PICVAL " +
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " ORDER BY 1,4,2 desc, 3");
             

           String strQuery4 = "SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " ORDER BY 1,4,2 desc, 3";


           Log.write(Log.DEBUG_VERBOSE, "PreorderBean : To Get list of Sent Phones, their PIC/LPIC values based on the Trait effective date from tables CAMS_SUNITT, CAMS_TRAITT."+strQuery4);
           Log.write("KASH.QUERY9:"+strQuery4);

             rs = myStmt.executeQuery("SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " ORDER BY 1,4,2 desc, 3");

     // Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Query Line # 2204  : To Get list of Sent Phones, their PIC/LPIC values based on the Trait effective date from tables CAMS_SUNITT, CAMS_TRAITT."+rs);

      }
      else
      {
		if (bATN)
		{
//         rs = myStmt.executeQuery("SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
//            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM DB2.CAMS_SUNITT S, DB2.CAMS_TRAITT T " +
//            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
//            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
//            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC') AS X GROUP BY X.SENT_PHONE, X.PIC, X.PICVAL " +
//            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
//            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM DB2.CAMS_SUNITT S, DB2.CAMS_TRAITT T " +
//            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
//            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
//            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC') AS X GROUP BY X.SENT_PHONE, X.PIC, X.PICVAL " +
//            " ORDER BY 1,4,2 desc, 3");
                    
                    Log.write("Query if bATN : SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " ORDER BY 1,4,2 desc, 3");

         String strQuery5 = "SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            //// Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " ORDER BY 1,4,2 desc, 3";

        Log.write(Log.DEBUG_VERBOSE, "PreorderBean : To get list of Sent Phones, PIC, PICVAL based on the latest Trait Effective Date from tables CAMS_SUNITT, CAMS_TRAITT."+strQuery5);
        Log.write("KASH.QUERY10:"+strQuery5);

         rs = myStmt.executeQuery("SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            //// Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.BENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " ORDER BY 1,4,2 desc, 3");

        // Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Query Line # 2234 : To get list of Sent Phones, PIC, PICVAL based on the latest Trait Effective Date from tables CAMS_SUNITT, CAMS_TRAITT."+rs);
	     }
	     else
	     {
//         rs = myStmt.executeQuery("SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
//            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM DB2.CAMS_SUNITT S, DB2.CAMS_TRAITT T " +
//            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
//            //"' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
//            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
//            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC') AS X GROUP BY X.SENT_PHONE, X.PIC, X.PICVAL " +
//            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
//            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM DB2.CAMS_SUNITT S, DB2.CAMS_TRAITT T " +
//            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
//            //"' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
//            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
//            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC') AS X GROUP BY X.SENT_PHONE, X.PIC, X.PICVAL " +
////HD 1300684            " ORDER BY 1,2 desc, 3");
//            " ORDER BY 1,4,2 desc, 3");
                    Log.write("Query else bATN : SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            //"' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            //"' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
//HD 1300684            " ORDER BY 1,2 desc, 3");
            " ORDER BY 1,4,2 desc, 3");


        String strQuery6 = "SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            //"' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            //// Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            //"' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
    //HD 1300684            " ORDER BY 1,2 desc, 3");
            " ORDER BY 1,4,2 desc, 3";

        Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Get Sent Phones, PIC, PICVAL, Trait Effective Date from tables CAMS_SUNITT, CAMS_TRAITT by union two queries."+strQuery6);
        Log.write("KASH.QUERY11:"+strQuery6);

         rs = myStmt.executeQuery("SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'PIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            //"' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            //// Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTER-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
            " UNION  SELECT SENT_PHONE, PIC, PICVAL, MAX(TRAIT_EFF_DATE) FROM " +
            " (SELECT DISTINCT S.SENT_PHONE, 'LPIC' AS PIC, SUBSTR(TRAIT_TXT,1,3) AS PICVAL, TRAIT_EFF_DATE FROM KASH.CAMS_SUNITT S, KASH.CAMS_TRAITT T " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState + "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist +
            //"' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone + "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            "' AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.SENT_PHONE='" + strATN + "' AND T.SENT_PHONE=S.SENT_PHONE " +
            // Removed AS X aliasing as query does not run in Oracle wincache db - Antony - 04/25/2014
            " AND T.SENT_INSRV_DATE=S.SENT_INSRV_DATE AND T.TRAIT_NAME='INTRA-SEL-PIC')  GROUP BY SENT_PHONE, PIC, PICVAL " +
//HD 1300684            " ORDER BY 1,2 desc, 3");
            " ORDER BY 1,4,2 desc, 3");
        // Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Query Line # 2264 : Get Sent Phones, PIC, PICVAL, Trait Effective Date from tables CAMS_SUNITT, CAMS_TRAITT by union two queries."+rs);
	     }
	   }
         if (rs.next()==true)
         {   //clean up initial record
            m_stmt.executeUpdate("DELETE FROM CSIR_PIC_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : removed initial CSIR_PIC_T records");
            strSaveTN=rs.getString(1);   //prime
            PreparedStatement pStmt = m_conn.prepareStatement("INSERT INTO CSIR_PIC_T VALUES ("+iPreSqncNmbr+","+iVersion+",?, " +
                "?, ?, ?, ?, ?,   ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?,   ?, ?, ?, ?, ?,   ?, ?, ?, ?, ?, sysdate, 'auto')");
            pStmt.setString(iPrepOcc++, strSaveTN);

            boolean bPic=false;
            boolean bLPic=false;
            do {
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : SENT=" + rs.getString(1) + " Pic=[" + rs.getString(3) + "] TE Date : "+rs.getString(4));
               
               if(rs.getString(4) != null && rs.getString(4).length() > 0) {
                   Log.write("Replacing Trait Eff Date with 10 digit date in yyyy-mm-dd format");
                   
                   //rs.set
               }
               
               //PIC and LPIC go on same occurence of CSIR_PIC_T
               strTN = rs.getString(1);
               if (!strTN.equals(strSaveTN))
               {
                  //HD 649271 fix to make sure all prepared stmt vars are filled
                  if (!bPic)
                  {   pStmt.setString( iPrepOcc+1, "" );
                     pStmt.setString( iPrepOcc+2, "" );
                  }
                  if (!bLPic)
                  {   pStmt.setString( iPrepOcc+3, "" );
                     pStmt.setString( iPrepOcc+4, "" );
                  }
                  bPic = false;
                  bLPic = false;
                  iTNCount++;
                  if (iTNCount == 11)   //roll over, so save an occurrence
                  {   pStmt.executeUpdate();
                     iOcc++;
                     iTNCount=1;
                  }
                  strSaveTN = strTN;
               }
               pStmt.setInt(1, iOcc);
               iPrepOcc = (iTNCount*5)-3;
               pStmt.setString(iPrepOcc, strSaveTN);
               strPicType = rs.getString(2);   //PIC or LPIC literal
               if (strPicType.equals("PIC"))
               {   pStmt.setString( iPrepOcc+1, rs.getString(3) );
                  pStmt.setString( iPrepOcc+2, rs.getString(4).substring(0,10) );
                  bPic = true;
               }
               else
               {   pStmt.setString( iPrepOcc+3, rs.getString(3) );
                  pStmt.setString( iPrepOcc+4, rs.getString(4).substring(0,10) );
                  bLPic = true;
               }

            } while(rs.next());
            if (iTNCount > 0)   //Is there an INSERT outstanding?
            {   //do I need to do this?

               //HD 649271 fix to make sure all prepared stmt vars are filled
               if (!bPic)
               {   pStmt.setString( iPrepOcc+1, "" );
                  pStmt.setString( iPrepOcc+2, "" );
               }
               if (!bLPic)
               {   pStmt.setString( iPrepOcc+3, "" );
                  pStmt.setString( iPrepOcc+4, "" );
               }

               iTNCount++;
               while (iTNCount < 11)   //fill in empty ones
               {   iPrepOcc = (iTNCount*5) - 3;
                  pStmt.setString(iPrepOcc++, "");
                  pStmt.setString(iPrepOcc++, "");
                  pStmt.setString(iPrepOcc++, "");
                  pStmt.setString(iPrepOcc++, "");
                  pStmt.setString(iPrepOcc++, "");
                  iTNCount++;
               }
               pStmt.executeUpdate();
            }

         }// if
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : getTNsAndPICs() done");

      }//try
      catch (SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQL Exception in getTNsAndPICs() se=" + se);
         iReturnCode = DB2Unavailable(se);
      }
      catch (Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception5 in getTNsAndPICs() e=" + e);
         iReturnCode = -2;
      }
         finally {
         try {
            rs.close();
         }
         catch (Exception e1) {}
         rs = null;
         }

      return iReturnCode;
   }

   /**
    * private int getDirectoryDeliveryAddress()
    * Get directory delivery address from CAMS.
    * Put results in CSIR_DDA_T table.
    *
         * @param   int    Preorder Sequence Number.
         * @param   int    Preorder Version Number.
    * @param   String    ATN
         * @param   Statement   SQL Statement object
    * @param   Strings   Keys for ORG
    * @return  int ReturnCode    0 = OK
    *            <0  Error
    */
   private int getDirectoryDeliveryAddress(int iPreSqncNmbr, int iVersion, String strATN, Statement myStmt,
            String strOrgRegion, String strOrgState, String strOrgComp, String strOrgDist, String strOrgBusOff,
            String strCustPhone, String strCustInSrvDate, boolean bATN, boolean bAN)
   {
      int iReturnCode = 0;
      ResultSet rs = null;

      // See if a trait defines which address type (addr-id) holds Directory Delivery address.
      String strAddrId = "";
      try {
         //See if a Directory Delivery Trait exists -if it does, use that ADDR-ID, otherwise take what we can get....
     //    rs = myStmt.executeQuery("SELECT T.TRAIT_TXT FROM DB2.CAMS_TRAITT T WHERE T.CUST_PHONE='" + strCustPhone + "' AND T.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND T.SENT_PHONE='" + strATN + "' AND T.TRAIT_NAME='DIRM-ADDR-ID'");
         String strQuery7 = "SELECT T.TRAIT_TXT FROM KASH.CAMS_TRAITT T WHERE T.CUST_PHONE='" + strCustPhone + "' AND T.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND T.SENT_PHONE='" + strATN + "' AND T.TRAIT_NAME='DIRM-ADDR-ID'" ;
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : To check if Trait Exists in table CAMS_TRAITT. Select for Trait only if Trait Name = 'DIRM-ADDR-ID'."+strQuery7);
         Log.write("KASH.QUERY12:"+strQuery7);

         rs = myStmt.executeQuery("SELECT T.TRAIT_TXT FROM KASH.CAMS_TRAITT T WHERE T.CUST_PHONE='" + strCustPhone + "' AND T.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND T.SENT_PHONE='" + strATN + "' AND T.TRAIT_NAME='DIRM-ADDR-ID'");
        // Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Query Line # 2408 : To check if Trait Exists in table CAMS_TRAITT. Select for Trait only if Trait Name = 'DIRM-ADDR-ID'."+rs);
         if (rs.next()==true)
         {
            strAddrId = rs.getString(1);
            strAddrId = strAddrId.trim();
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : DirDelivery Addr trait of [" + strAddrId + "] found");
         }
         try {
            rs.close();
         }
         catch (Exception e1) {}
      }
      catch(SQLException se) {
         Log.write(Log.ERROR, "PreorderBean : SQLException in getDirectoryDeliveryAddr() se=" + se);
         iReturnCode = DB2Unavailable(se);
      }
      catch(Exception e) {
         Log.write(Log.ERROR, "PreorderBean : Exception in getDirectoryDeliveryAddr() e=" + e);
         iReturnCode = -2;
      }
      if (iReturnCode < 0)
      {   try {   rs.close();
            rs=null;
         }
         catch (Exception e) {}
         return iReturnCode;
      }

     String strQuery = "";

     if (bAN)
     {

//      strQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.CUST_INSRV_DATE, A.ADDR_LINE1, A.ADDR_LINE2, " +
//         " A.ADDR_LINE3, A.ADDR_LINE4, A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE FROM DB2.CAMS_ADDRESST A " +
//         " WHERE A.ORG_REGION='" + strOrgRegion + "' AND A.CUST_PHONE='" + strCustPhone + "' AND A.CUST_INSRV_DATE='" +
//         strCustInSrvDate + "'  ";
      strQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.CUST_INSRV_DATE, A.ADDR_LINE1, A.ADDR_LINE2, " +//address line change here
         " A.ADDR_LINE3, NVL(A.ADDR_LINE4,''), A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE FROM KASH.CAMS_ADDRESST A " +
         " WHERE A.ORG_REGION='" + strOrgRegion + "' AND A.CUST_PHONE='" + strCustPhone +
         "' AND A.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd')";
    Log.write(Log.DEBUG_VERBOSE, "PreorderBean : To Get Distinct Customer Address Details from table CAMS_ADDRESST based on Customer Region, Phone Number and Customer In Serve Date."+strQuery);
    Log.write("KASH.QUERY13:"+strQuery);
    
      if (strAddrId.length() > 0)
         strQuery += " AND A.ADDR_ID='" + strAddrId + "' ";      //specific addr_id was defined for service address
      else
         strQuery += " AND (A.ADDR_ID='SAD1' OR A.ADDR_ID='MAIN') ";
      strQuery += " ORDER BY 1 DESC, 2 DESC, 3 DESC ";
     }
     else
     {
		 if (bATN)
		 {
//			 strQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.CUST_INSRV_DATE, A.ADDR_LINE1, A.ADDR_LINE2, " +
//			    " A.ADDR_LINE3, A.ADDR_LINE4, A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE FROM DB2.CAMS_ADDRESST A " +
//			    " WHERE A.ORG_REGION='" + strOrgRegion + "' AND A.CUST_PHONE='" + strCustPhone + "' AND A.CUST_INSRV_DATE='" +
//			    strCustInSrvDate + "'  ";
			 strQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.CUST_INSRV_DATE, A.ADDR_LINE1, A.ADDR_LINE2, " +//address line change here
			    " A.ADDR_LINE3, NVL(A.ADDR_LINE4,''), A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE FROM KASH.CAMS_ADDRESST A " +
			    " WHERE A.ORG_REGION='" + strOrgRegion + "' AND A.CUST_PHONE='" + strCustPhone + 
                            "' AND A.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd')";
                     Log.write(Log.DEBUG_VERBOSE, "PreorderBean : To Get Distinct Customer Address Details from table CAMS_ADDRESST."+strQuery);
                     Log.write("KASH.QUERY14.1:"+strQuery);

			 if (strAddrId.length() > 0)
			    strQuery += " AND A.ADDR_ID='" + strAddrId + "' ";      //specific addr_id was defined for service address
			 else
			    strQuery += " AND (A.ADDR_ID='SAD1' OR A.ADDR_ID='MAIN') ";
             strQuery += " ORDER BY 1 DESC, 2 DESC, 3 DESC ";
	     }
	     else
	     {
//			 strQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.CUST_INSRV_DATE, A.ADDR_LINE1, A.ADDR_LINE2, " +
//			    " A.ADDR_LINE3, A.ADDR_LINE4, A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE FROM DB2.CAMS_ADDRESST A " +
//			    " WHERE A.ORG_REGION='" + strOrgRegion + "' AND A.ADDR_PHONE='" + strATN + "' AND A.CUST_INSRV_DATE='" +
//			    strCustInSrvDate + "'  ";
			 strQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.CUST_INSRV_DATE, A.ADDR_LINE1, A.ADDR_LINE2, " +//address line change here
			    " A.ADDR_LINE3, NVL(A.ADDR_LINE4,''), A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE FROM KASH.CAMS_ADDRESST A " +
			    " WHERE A.ORG_REGION='" + strOrgRegion + "' AND A.ADDR_PHONE='" + strATN + 
                            "' AND A.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd')";
                    Log.write(Log.DEBUG_VERBOSE, "PreorderBean : To Get Distinct Customer Address Details from table CAMS_ADDRESST based on Address Phone."+strQuery);
                    Log.write("KASH.QUERY14.2:"+strQuery);
                    
			 if (strAddrId.length() > 0)
			    strQuery += " AND A.ADDR_ID='" + strAddrId + "' ";      //specific addr_id was defined for service address
			 else
			    strQuery += " AND (A.ADDR_ID='SAD1' OR A.ADDR_ID='MAIN') ";
             strQuery += " ORDER BY 1 DESC, 2 DESC, 3 DESC ";
	     }
      }


      try {
         PreparedStatement pStmt = m_conn.prepareStatement("INSERT INTO CSIR_DDA_T (PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SCTN_OCC, CSIR_NAME, CSIR_DATY, CSIR_DDCAI, CSIR_DDCITY, CSIR_DDSTATE, CSIR_DDZIP, MDFD_DT, MDFD_USERID) " +
            " VALUES ("+iPreSqncNmbr+","+iVersion+",?, ?, '1', ?, ?, ?, ?, sysdate, 'auto')");

         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Querying DB2 warehouse for Dir Del Addr Query=[" + strQuery + "]");
         rs = myStmt.executeQuery(strQuery);
         if (rs.next() == true)   //only get the first (should be most recent record)
         {
            //Get rid of empty record
            m_stmt.executeUpdate("DELETE FROM CSIR_DDA_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);

            Log.write(Log.DEBUG_VERBOSE, " Dir Del Address found");
            //insert into CSIR_DDA_T now
            pStmt.setInt(1, 1);
            //            pStmt.setString(2, (rs.getString(4)).substring(0,24) );   //name
            // removing the substring for now and passing in the entire addr_line1 value - Antony - 04/25/2014
            //null check for Addr line 4 and 5 - Antony - 04/25/2014
            
            String strAddrLine4 = "";
            String strAddrLine5 = "";
                    
            if (rs.getString(6) == null)
                strAddrLine4 = "";
            else
                strAddrLine4 = " "+rs.getString(6);
            
            if (rs.getString(7) == null)
                strAddrLine5 = "";
            else
                strAddrLine5 = " "+rs.getString(7);
            
            pStmt.setString(2, rs.getString(4) );   //name
            //pStmt.setString(3, rs.getString(5)+rs.getString(6)+rs.getString(7) ); //CAI -- to pass after null check
            pStmt.setString(3, rs.getString(5)+strAddrLine4+strAddrLine5 ); //CAI
            pStmt.setString(4, rs.getString(8) );   //city
            pStmt.setString(5, rs.getString(9) );   //state
            pStmt.setString(6, rs.getString(10));   //zip
            pStmt.executeUpdate();

         }//end-if
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : getDirectoryDeliveryAdd() done");

      }
      catch (SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQLException in getDirectoryDeliveryAddr() se=[" + se + "]");
         iReturnCode = DB2Unavailable(se);
      }
      catch (Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception in getDirectoryDeliveryAddr() e=[" + e + "]");
         iReturnCode = -3;
      }
         finally {
         try {
            rs.close();
         }
         catch (Exception e1) {}
         rs = null;
         }

      return iReturnCode;
   }

   private String normalizeSATH( String strEnteredSATH )
   {
      //Added this since SATH not required anymore (HD 98389)
      if (strEnteredSATH == null || strEnteredSATH.length() == 0)
         return "";

      String strResult = strEnteredSATH.toUpperCase().trim();

      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : SATH = [" + strEnteredSATH + "]");

      if ( strResult.startsWith("ST") )      //get ST, str, street, st.
      {   strResult = "ST";
      }
      else if ( strResult.startsWith("RD") || strResult.equals("ROAD") )
      {   strResult = "RD";
      }
      else if ( strResult.startsWith("AV") )      //Avenue
      {   strResult = "AV";
      }
      else if ( strResult.startsWith("B") )      //Blvd
      {   strResult = "B";
      }
      else if ( strResult.startsWith("CIR") )      //Circle
      {   strResult = "CIR";
      }
      else if ( strResult.startsWith("PL") )       //Plaza and Place
      {   strResult = "PL";
      }
      else if ( strResult.startsWith("CT") || strResult.startsWith("COU") )      //Court
      {   strResult = "C";   //might not work all the time, we'll take the risk
      }
      else if ( strResult.startsWith("DR") )      //Drive
      {   strResult = "DR";
      }
      else if ( strResult.startsWith("L") )      //Lane, LN
      {   strResult = "L";
      }
      else if ( strResult.startsWith("TR") )      //Trail
      {   strResult = "TR";
      }
      else
      {   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : SATH unchanged = [" + strResult + "]");
      }

      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : SATH now = [" + strResult + "]");

      return strResult;
   }

        /**
    * Complete a CSI by writing response to CSIR_T results table.
    *
         * @param   int    Preorder Sequence Number
         * @param   int    Preorder Version Number
         * @param   String    RESPC code
         * @param   String    RESPD description
         * @param   String    Remarks field
    * @return  void
    */
   private void completeCSIResponse(int iPreSqncNmbr, int iVersion, String strRespc, String strRespd, String strRemark)
   {
      try {
         m_stmt.executeUpdate("UPDATE CSIR_T SET CSIR_REMARKS='" + strRemark + "', " +
                    " CSIR_RESPC='" +strRespc + "', " +
                    " CSIR_RESPD='" +strRespd + "', MDFD_DT=sysdate, MDFD_USERID='auto' " +
                    " WHERE PRE_ORDR_SQNC_NMBR=" + iPreSqncNmbr + " AND PRE_ORDR_VRSN=" + iVersion);
      }
      catch (Exception se) {
         Log.write(Log.ERROR, "PreorderBean: completeCSIResponse() se=[" + se + "]");
      }
   }

   private void updatePreorderICARE(int iPreSqncNmbr, int iVersion)
   {
      try {
         m_stmt.executeUpdate("UPDATE PREORDER_T SET ICARE='Y'" +
                  " WHERE PRE_ORDR_SQNC_NMBR=" + iPreSqncNmbr + " AND PRE_ORDR_VRSN=" + iVersion);
         }
         catch (Exception se) {
            Log.write(Log.ERROR, "PreorderBean: updatePreorderICARE() se=[" + se + "]");
         }
   }

   /**
    * private boolean bDisplayCSI()
    * Check to see if the account belongs to a CLEC or Reseller.
    * If it does, then we can't show CSI data ---unless the account
    * belongs to the CLEC/Reseller that's running the CSI.
    *
    * We identify CLECs and Resellers by ASOC of ATYP with an attribute of IRS or IFB or
     *   IUP (UNE-P acccounts).
    * We identify the owning CLEC/Reseller by trait of ICLEC-LSP-ID or RESOLD-LSP-ID. Note:
    *   also need to pick up UNE-P accounts, so trait UNEP-ID was added to criteria.
    *
         * @param   int    Preorder Sequence Number.
         * @param   int    Preorder Version Number.
    * @param   String    Alphanumeric OCN Cd
    * @param   String    ATN
         * @param   Statement   SQL Statement object
    * @param   Strings   Keys for ORG
    * @return  boolean    bReturnCode    true ==not CLEC or account belongs to requesting CLEC
    *               false ==CLEC or reseller account that shouldn't be shown
    */
   private boolean bDisplayCSI(int iPreSqncNmbr, int iVersion, String strOCNCd, String strATN, Statement myStmt,
            String strOrgRegion, String strOrgState, String strOrgComp, String strOrgDist,
            String strOrgBusOff, String strCustPhone, String strCustInSrvDate)
   {

      boolean bReturnCode = false;   //default to not showing it...its safer
      int iReturnCode = 0;
      ResultSet rs = null;
      int iCount = 0;
      String strOCN = "";

      //HD 377912 -added STOP_DATE
      String strDate = Toolkit.getDateTime();
      strDate = strDate.substring(0,10);

      try {
         Log.write(Log.DEBUG_VERBOSE,"PreorderBean: bDisplayCSI() CLEC or Reseller check. Requesting OCN="+strOCNCd);
//         String strCLECquery = "SELECT COUNT(*) FROM DB2.CAMS_SUNITT S, DB2.CAMS_CUST_ATTRT A " +
//            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState +
//            "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist + "' " +
//            " AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone +
//            "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND S.ASOC_ID='ATYP' AND " +
//            " S.STOP_DATE > '" + strDate + "' " +
//            " AND A.ORG_REGION=S.ORG_REGION AND A.ORG_STATE=S.ORG_STATE AND A.ORG_COMP=S.ORG_COMP " +
//            " AND A.ORG_DIST=S.ORG_DIST AND A.CUST_PHONE=S.CUST_PHONE AND " +
//            " A.CUST_INSRV_DATE=S.CUST_INSRV_DATE AND S.SUNIT_ID=A.SUNIT_ID " +
//            " AND A.ATTR_TXT IN ('IRS', 'IFB', 'IUP', 'WLS') ";
         String strCLECquery = "SELECT COUNT(*) FROM KASH.CAMS_SUNITT S, KASH.CAMS_CUST_ATTRT A " +
            " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState +
            "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist + "' " +
            " AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone +
            "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND S.ASOC_ID='ATYP' AND " +
            " S.STOP_DATE > to_date('" + strDate + "','yyyy-mm-dd') " +
            " AND A.ORG_REGION=S.ORG_REGION AND A.ORG_STATE=S.ORG_STATE AND A.ORG_COMP=S.ORG_COMP " +
            " AND A.ORG_DIST=S.ORG_DIST AND A.CUST_PHONE=S.CUST_PHONE AND " +
            " A.CUST_INSRV_DATE=S.CUST_INSRV_DATE AND S.SUNIT_ID=A.SUNIT_ID " +
            " AND A.ATTR_TXT IN ('IRS', 'IFB', 'IUP', 'WLS') ";
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : To get Record Counts for tables CAMS_SUNITT, CAMS_CUST_ATTRT."+strCLECquery);
         Log.write("KASH.QUERY15:"+strCLECquery);
         
         Log.write("PreorderBean: bDisplayCSI() strCLECquery=["+strCLECquery+"]");
         rs = myStmt.executeQuery(strCLECquery);
         rs.next();
         iCount = rs.getInt(1);
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : isCustomerCLECorResale() count=" + iCount);
         if (iCount > 0)
         {
            try { rs.close(); } catch (Exception e1) {}
//            rs = myStmt.executeQuery("SELECT S.TRAIT_TXT FROM DB2.CAMS_TRAITT S " +
//               " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState +
//               "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist + "' " +
//               " AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone +
//               "' AND S.CUST_INSRV_DATE='" + strCustInSrvDate + "' " +
//               " AND S.TRAIT_NAME  IN ('RESOLD-LSP-ID', 'ICLEC-LSP-ID', 'UNEP-ID') " +
//               " ORDER BY S.TRAIT_EFF_DATE DESC ");

            String strQuery12 = "SELECT S.TRAIT_TXT FROM KASH.CAMS_TRAITT S " +
               " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState +
               "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist + "' " +
               " AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone +
               "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd')" +
               " AND S.TRAIT_NAME  IN ('RESOLD-LSP-ID', 'ICLEC-LSP-ID', 'UNEP-ID') " +
               " ORDER BY S.TRAIT_EFF_DATE DESC";
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Get Trait Exists from table CAMS_TRAITT."+strQuery12);
            Log.write("KASH.QUERY16:"+strQuery12);

            rs = myStmt.executeQuery("SELECT S.TRAIT_TXT FROM KASH.CAMS_TRAITT S " +
               " WHERE S.ORG_REGION='" + strOrgRegion + "' AND S.ORG_STATE='" + strOrgState +
               "' AND S.ORG_COMP='" + strOrgComp + "' AND S.ORG_DIST='" + strOrgDist + "' " +
               " AND S.ORG_BUSOFF='" + strOrgBusOff + "' AND S.CUST_PHONE='" + strCustPhone +
               "' AND S.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd')" +
               " AND S.TRAIT_NAME  IN ('RESOLD-LSP-ID', 'ICLEC-LSP-ID', 'UNEP-ID') " +
               " ORDER BY S.TRAIT_EFF_DATE DESC ");
         //Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Query Line # 2696 : Get Trait Exists from table CAMS_TRAITT."+rs);
            if (rs.next())
            {   strOCN = rs.getString(1);
               strOCN = strOCN.trim();
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : bDisplayCSI() OCN ["+strOCN+"]");
            }
            else {
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : bDisplayCSI() TRAIT-TXT defining OCN not found");
            }
            try { rs.close(); } catch (Exception e1) {}
            if (strOCN.equals(strOCNCd))
            {
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : bDisplayCSI() Ok to show");
                bReturnCode = true;
            }
         }
         else
         {   bReturnCode = true;//Its ALLTEL's Feds tells us to give it away man
         }
      }//try
      catch (SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQL Exception in bDisplayCSI() se=" + se);
         iReturnCode = DB2Unavailable(se);
      }
      catch (Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception5 in bDisplayCSI() e=" + e);
         iReturnCode = -2;
      }
         finally {
         //try { rs.close(); } catch (Exception e1) {}
         rs = null;
         }
      return bReturnCode;
   }

        /**
    * Using  either the user-entered WTN or ATN, query the CAMS datawarehouse
    * for a customer information - then query Express DB for directory listings.
    * There are 3 types of CSI preorders.  E, M, or T
    * E = CSI, M = CSI w/ directory listing, T = Directory Listings
    *
         * @param   int    Preorder Sequence Number.
         * @param   int    Preorder Version Number.
    * @param   String   Transaction Type (E, M, T)
    * @param   String   Alphanumeric OCN Code
    * @return  int Return Code (0 = OK, <0 = Error)
    */
        private int doCSI(int iPreSqncNmbr, int iVersion, String strXtnType, String strOCNCd)
        {
       Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Our code works!!!!!!!!!!!!!");     
      int iReturnCode = 0;

      Connection    connCAMS = null;
      Statement    stmtCAMS = null;

      //These fields can be used for keys into most CAMS datawarehouse tables
      String strOrgRegion = "";
      String strOrgState = "";
      String strOrgComp = "";
      String strOrgDist = "";
      String strOrgBusOff = "";
      String strCustPhone = "";
      String strCustInSrvDate = "";
      String strTOS1 = "";
      String strCS = "";
      String strBentCamsId = "";
      String strCustCamsId = "";
      String strCustPswdPin = "";
      String strTraitName = "";

      String strATN = "";
      String strATN_d = "";
      String strWTN = "";
      String strAN = "";
      String strPSWD = "";
      String strRemark = " (search done by AN, ATN and PSWD_PIN)";
      String strRESPC = "";
      String strRESPD = "";
      String strRESPC_1 = "";
      String strRESPD_1 = "";
      boolean bFound = false;
      boolean bATN = true;
      boolean bAN = true;
      boolean bInvalidAN = false;
      boolean bInvalidPW = false;
      ResultSet rs = null;
      ResultSet rs4 = null;
      ResultSet rs5 = null;
      ResultSet rs6 = null;

      //First let's get the user-entered ATN and WTN
      try
      {
         rs = m_stmt.executeQuery("SELECT CSI_ATN, CSI_WTN, CSI_AN, CSI_PSWD FROM CSI_T WHERE PRE_ORDR_SQNC_NMBR = " +
            iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
         if (rs.next())
         {
            strATN = rs.getString(1);
            //strATN_d = strATN;
            strWTN = rs.getString(2);
            strAN = rs.getString(3);
            strPSWD = rs.getString(4);
            if (strATN == null || strATN.length() == 0)    //No ATN entered, so use WTN
            {
               bATN = false;
               strATN = strWTN;   //defaulting ATN to WTN
               strRemark = " (search done by AN, WTN and PSWD_PIN) ";
            }
            strCustPhone = strATN; //default
            if (strAN == null || strAN.length() == 0)
            {
				bAN = false;
            }
         }
         else
         {   Log.write(Log.ERROR, "PreorderBean : Err getting input args from CSI_T for PO=[" + iPreSqncNmbr + "]");
            iReturnCode = -1;
         }
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : doCSI() ATN=" + strATN + " WTN="+ strWTN + " PSWD_PIN=" + strPSWD);
      }
      catch(SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQLException in doCSI() se=" + se);
         iReturnCode = -2;
      }
      catch(Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception in doCSI() e=" + e);
         iReturnCode = -3;
      }
      if (iReturnCode < 0)
      {   Log.write(Log.ERROR, "PreorderBean : rc = " + iReturnCode);
         return iReturnCode;
      }

      //-------------------------------------------------------------------------------------
      //          Get a CAMS Connection first
      //-------------------------------------------------------------------------------------
      try {
         connCAMS =  DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
         stmtCAMS = connCAMS.createStatement();
      }
      catch(Exception e) {
         Log.write(Log.ERROR, "PreorderBean : getConnection() Exception in doCSI() e=" + e);
         iReturnCode = -4;
      }
      if (iReturnCode < 0)
      {
                   DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         return iReturnCode;
      }

      //------------------------------------------------------------------------------------
      // Get the CUST_PHONE with the latest inservice date -also get disconnected date if
      // available.
      //------------------------------------------------------------------------------------
      String strQuery = "";
      String strQuery4 = "";
      String strQuery5 = "";
      String strQuery6 = "";

      if (!bAN)
      {

//          strQuery5 = "SELECT BENT_CAMS_ID, CUST_PHONE FROM DB2.CAMS_CUSTOMERT WHERE CUST_PHONE='" + strATN + "' " +
//             " AND DISC_DATE IS NULL";
          strQuery5 = "SELECT BENT_CAMS_ID, CUST_PHONE FROM KASH.CAMS_CUSTOMERT WHERE CUST_PHONE='" + strATN + "' " +
             " AND DISC_DATE = '0000-00-00'";
      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Get Bent CAMS ID, Customer Phone from table CAMS_CUSTOMERT."+strQuery5);
      Log.write("KASH.QUERY17.1:"+strQuery5);

          try {
             rs5 = stmtCAMS.executeQuery(strQuery5);
             if (rs5.next()==true)
             {
				 strAN = rs5.getString(1);
				 strATN_d = rs5.getString(2);
				 Log.write(Log.DEBUG_VERBOSE, "PreorderBean : strAN[" + strAN + "]");
				 bATN = true;
		     }
		     else
		     {
				 bATN = false;
	         }
	      }
	      catch(SQLException se) {
		  	   Log.write(Log.ERROR, "PreorderBean : customer query SQLException in doCSI() se=" + se);
		  	   iReturnCode = DB2Unavailable(se);
		  }
		  catch(Exception e) {
		  	   Log.write(Log.ERROR, "PreorderBean : customer query Exception in doCSI() e=" + e);
		  	   iReturnCode = -11;
	      }
      }
      else
      {
//          strQuery5 = "SELECT BENT_CAMS_ID, CUST_PHONE FROM DB2.CAMS_CUSTOMERT WHERE CUST_PHONE='" + strATN + "' " +
//             " AND DISC_DATE IS NULL";
          strQuery5 = "SELECT BENT_CAMS_ID, CUST_PHONE FROM KASH.CAMS_CUSTOMERT WHERE CUST_PHONE='" + strATN + "' " +
             " AND DISC_DATE = '0000-00-00'";
       Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Get Bent CAMS ID, Customer Phone from table CAMS_CUSTOMERT - Else Loop."+strQuery5);
       Log.write("KASH.QUERY17.2:"+strQuery5);

          try {
                rs5 = stmtCAMS.executeQuery(strQuery5);
                if (rs5.next()==true)
                {
				 strATN_d = rs5.getString(2);
				 Log.write(Log.DEBUG_VERBOSE, "PreorderBean : strAN[" + strAN + "]");
				 bATN = true;
                } else {
				 bATN = false;
                }
	      } catch(SQLException se) {
		  	   Log.write(Log.ERROR, "PreorderBean : customer query SQLException in doCSI() se=" + se);
		  	   iReturnCode = DB2Unavailable(se);
              } catch(Exception e) {
		  	   Log.write(Log.ERROR, "PreorderBean : customer query Exception in doCSI() e=" + e);
		  	   iReturnCode = -11;
	      }
      }


      if (!bATN && !bAN)
      {
//		  strQuery5 = "SELECT C.BENT_CAMS_ID, C.CUST_PHONE FROM DB2.CAMS_CUSTOMERT C, DB2.CAMS_SENTT S " +
//		              " WHERE S.SENT_PHONE='" + strATN + "' " +
//		              " AND C.CUST_PHONE=S.CUST_PHONE AND C.DISC_DATE IS NULL AND S.SENT_DCNCT_DATE IS NULL";
		  strQuery5 = "SELECT C.BENT_CAMS_ID, C.CUST_PHONE FROM KASH.CAMS_CUSTOMERT C, KASH.CAMS_SENTT S " +
		              " WHERE S.SENT_PHONE='" + strATN + "' " +
		              " AND C.CUST_PHONE=S.CUST_PHONE AND C.DISC_DATE = '0000-00-00' AND S.SENT_DCNCT_DATE IS NULL";
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Get Bent CAMS ID, Customer Phone from tables CAMS_CUSTOMERT, CAMS_SENTT if disconnect date and sent date is null."+strQuery5);
            Log.write("KASH.QUERY18.1:"+strQuery5);

		  try {
			 rs5 = stmtCAMS.executeQuery(strQuery5);
			 if (rs5.next()==true)
			 {
			     strAN = rs5.getString(1);
			     strATN_d = rs5.getString(2);
				 Log.write(Log.DEBUG_VERBOSE, "PreorderBean : strAN[" + strAN + "]");
		         strWTN = strATN;
		         }
	              }
	          catch(SQLException se) {
		  	   Log.write(Log.ERROR, "PreorderBean : sent/customer query SQLException in doCSI() se=" + se);
		  	   iReturnCode = DB2Unavailable(se);
		  }
		  catch(Exception e) {
		  	   Log.write(Log.ERROR, "PreorderBean : sent/customer query Exception in doCSI() e=" + e);
		  	   iReturnCode = -11;
		  }
      }
      else
      {
//          strQuery5 = "SELECT C.BENT_CAMS_ID, C.CUST_PHONE FROM DB2.CAMS_CUSTOMERT C, DB2.CAMS_SENTT S " +
//		              " WHERE S.SENT_PHONE='" + strATN + "' " +
//		              " AND C.CUST_PHONE=S.CUST_PHONE AND C.DISC_DATE = '0000-00-00' AND S.SENT_DCNCT_DATE IS NULL";
          strQuery5 = "SELECT C.BENT_CAMS_ID, C.CUST_PHONE FROM KASH.CAMS_CUSTOMERT C, KASH.CAMS_SENTT S " +
		              " WHERE S.SENT_PHONE='" + strATN + "' " +
		              " AND C.CUST_PHONE=S.CUST_PHONE AND C.DISC_DATE = '0000-00-00' AND S.SENT_DCNCT_DATE IS NULL";
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get Bent CAMS ID, Customer Phone from tables CAMS_CUSTOMERT, CAMS_SENTT if disconnect date and sent date is null. Else Loop"+strQuery5);
         Log.write("KASH.QUERY18.2:"+strQuery5);

		  try {
			 rs5 = stmtCAMS.executeQuery(strQuery5);
			 if (rs5.next()==true)
			 {
			  //   strAN = rs5.getString(1);
			     strATN_d = rs5.getString(2);
	    		 Log.write(Log.DEBUG_VERBOSE, "PreorderBean : strAN[" + strAN + "]");
		         strWTN = strATN;
		         }
	              }
	          catch(SQLException se) {
		  	   Log.write(Log.ERROR, "PreorderBean : sent/customer query SQLException in doCSI() se=" + se);
		  	   iReturnCode = DB2Unavailable(se);
		  }
		  catch(Exception e) {
		  	   Log.write(Log.ERROR, "PreorderBean : sent/customer query Exception in doCSI() e=" + e);
		  	   iReturnCode = -11;
	      }
      }

      //if (!bATN && bAN)
      //{
	  //	  strWTN = strATN;
      //}


      if (bATN)
      {

//          strQuery4 =  "SELECT T.TRAIT_NAME, T.TRAIT_TXT " +
//             " FROM DB2.CAMS_TRAITT T WHERE T.CUST_PHONE='" + strATN + "' AND (T.BENT_CAMS_ID='" + strAN + "' " +
//             " OR T.CUST_CAMS_ID='" + strAN + "') AND T.TRAIT_EFF_DATE=(SELECT MAX(T1.TRAIT_EFF_DATE) FROM DB2.CAMS_TRAITT T1 " +
//             " WHERE T.CUST_PHONE=T1.CUST_PHONE AND T1.TRAIT_NAME='PASSCODE-CAPP') AND T.TRAIT_NAME='PASSCODE-CAPP'";
          strQuery4 =  "SELECT T.TRAIT_NAME, T.TRAIT_TXT " +
             " FROM KASH.CAMS_TRAITT T WHERE T.CUST_PHONE='" + strATN + "' AND (T.BENT_CAMS_ID='" + strAN + "' " +
             " OR T.CUST_CAMS_ID='" + strAN + "') AND T.TRAIT_EFF_DATE=(SELECT MAX(T1.TRAIT_EFF_DATE) FROM KASH.CAMS_TRAITT T1 " +
             " WHERE T.CUST_PHONE=T1.CUST_PHONE AND T1.TRAIT_NAME='PASSCODE-CAPP') AND T.TRAIT_NAME='PASSCODE-CAPP'";
       Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get Trait Name, Trait Exists from table CAMS_TRAITT based on Customer Phone, Bent CAMS ID or Customer CAMS ID for Trait Name = 'PASSCODE-CAPP'"+strQuery4);
       Log.write("KASH.QUERY19:"+strQuery4);
     
       
          try {
                     Log.write("Inside 2997 try block...");
                     rs4 = stmtCAMS.executeQuery(strQuery4);
                     Log.write("Inside 2997 exec block...");
                     
	  	     if (rs4.next()==true)
		     {
		  	    strTraitName = rs4.getString(1);
		  	    strCustPswdPin = rs4.getString(2);

		  	    strQuery6 = "SELECT * FROM KASH.CAMS_BILLING_ENTT WHERE BENT_PHONE='" + strATN + "' and BENT_CAMS_ID='" + strAN + "'";
                            
                            Log.write("strQuery6 :"+strQuery6);//remove log messages while merging - Antony
                            
		  	    try {
				   rs6 = stmtCAMS.executeQuery(strQuery6);
                                   Log.write("Inside strQuery6 exec block...");
				   if (rs6.next()==true)
                                   {
                                                //strQuery = "SELECT B.CUST_PHONE, B.CUST_INSRV_DATE, B.DISC_DATE, B.ORG_REGION, B.ORG_STATE, B.ORG_COMP, "+
                                                //" B.ORG_DIST, B.ORG_BUSOFF, B.BENT_TYPE_CODE, B.BENT_CAMS_ID, B.CUST_CAMS_ID,'' " +
                                                //" FROM DB2.CAMS_BILLING_ENTV B, DB2.CAMS_TRAITT T WHERE B.BENT_PHONE='" + strATN + "' " +
                                                //" AND B.BENT_PHONE=T.CUST_PHONE AND T.TRAIT_NAME='" + strTraitName + "' ORDER BY 2 DESC";
                                                strQuery = "SELECT B.CUST_PHONE, B.CUST_INSRV_DATE, B.DISC_DATE, B.ORG_REGION, B.ORG_STATE, B.ORG_COMP, "+
                                                 " B.ORG_DIST, B.ORG_BUSOFF, B.BENT_TYPE_CODE, B.BENT_CAMS_ID, B.CUST_CAMS_ID,'' " +
                                                 " FROM KASH.CAMS_BILLING_ENTT B, KASH.CAMS_TRAITT T WHERE B.BENT_PHONE='" + strATN + "' " +
                                                " AND B.BENT_PHONE=T.CUST_PHONE AND T.TRAIT_NAME='" + strTraitName + "' ORDER BY 2 DESC";
                                                Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get Customer Details like Phone number, region, state, company, CAMS ID, CUST CAMS ID from tables CAMS_BILLING_ENTT, CAMS_TRAITT."+strQuery);
                                                Log.write("KASH.QUERY20.1:"+strQuery);
                                    } else {
                            //                      strQuery = "SELECT C.CUST_PHONE, C.CUST_INSRV_DATE, C.DISC_DATE, C.ORG_REGION, C.ORG_STATE, C.ORG_COMP, "+
                            //			             " C.ORG_DIST, C.ORG_BUSOFF, C.CUST_TYPE_CODE, C.BENT_CAMS_ID, C.CUST_CAMS_ID,'' " +
                            //			             " FROM DB2.CAMS_CUSTOMERT C, DB2.CAMS_TRAITT T WHERE C.CUST_PHONE='" + strATN + "' " +
                            //                         " AND C.CUST_PHONE=T.CUST_PHONE AND T.TRAIT_NAME='" + strTraitName + "' ORDER BY 2 DESC";
                                                  strQuery = "SELECT C.CUST_PHONE, C.CUST_INSRV_DATE, C.DISC_DATE, C.ORG_REGION, C.ORG_STATE, C.ORG_COMP, "+
                                                                 " C.ORG_DIST, C.ORG_BUSOFF, C.CUST_TYPE_CODE, C.BENT_CAMS_ID, C.CUST_CAMS_ID,'' " +
                                                                 " FROM KASH.CAMS_CUSTOMERT C, KASH.CAMS_TRAITT T WHERE C.CUST_PHONE='" + strATN + "' " +
                                                     " AND C.CUST_PHONE=T.CUST_PHONE AND T.TRAIT_NAME='" + strTraitName + "' ORDER BY 2 DESC";
                                                  Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get Customer Details like Customer Phone number, region, state, company, customer type code, CAMS ID, CUST CAMS ID from tables CAMS_CUSTOMERT, CAMS_TRAITT."+strQuery);
                                                  Log.write("KASH.QUERY20.2:"+strQuery);
                                    }
			       } catch(SQLException se) {
                                    Log.write(Log.ERROR, "PreorderBean : trait query SQLException in doCSI() se=" + se);
                                    iReturnCode = DB2Unavailable(se);
                               }
                               catch(Exception e) {
                                    Log.write(Log.ERROR, "PreorderBean : trait query Exception in doCSI() e=" + e);
                                    iReturnCode = -11;
                               }
	         }
	         else
	         {
                strQuery6 = "SELECT * FROM KASH.CAMS_BILLING_ENTT WHERE BENT_PHONE='" + strATN + "' and BENT_CAMS_ID='" + strAN + "'";
		  	    try {
				   rs6 = stmtCAMS.executeQuery(strQuery6);
				   if (rs6.next()==true)
		           {
//			          strQuery = "SELECT CUST_PHONE, CUST_INSRV_DATE, DISC_DATE, ORG_REGION, ORG_STATE, ORG_COMP, "+
//			             " ORG_DIST, ORG_BUSOFF, BENT_TYPE_CODE, BENT_CAMS_ID, CUST_CAMS_ID,'' " +
//			  	         " FROM DB2.CAMS_BILLING_ENTV WHERE BENT_PHONE='" + strATN + "' ORDER BY 2 DESC";
			          strQuery = "SELECT CUST_PHONE, CUST_INSRV_DATE, DISC_DATE, ORG_REGION, ORG_STATE, ORG_COMP, "+
			             " ORG_DIST, ORG_BUSOFF, BENT_TYPE_CODE, BENT_CAMS_ID, CUST_CAMS_ID,'' " +
			  	         " FROM KASH.CAMS_BILLING_ENTT WHERE BENT_PHONE='" + strATN + "' ORDER BY 2 DESC";
                      Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get Customer Phone, Customer Disconnect date, region, state, company, Bent type code, Bent CAMS ID, Cust CAMS ID from table CAMS_BILLING_ENTT."+strQuery);
                      Log.write("KASH.QUERY21:"+strQuery);
			       }
			       else
			       {
//                      strQuery = "SELECT CUST_PHONE, CUST_INSRV_DATE, DISC_DATE, ORG_REGION, ORG_STATE, ORG_COMP, "+
//			             " ORG_DIST, ORG_BUSOFF, CUST_TYPE_CODE, BENT_CAMS_ID, CUST_CAMS_ID,'' " +
//			  	         " FROM DB2.CAMS_CUSTOMERT WHERE CUST_PHONE='" + strATN + "' ORDER BY 2 DESC";
                      strQuery = "SELECT CUST_PHONE, CUST_INSRV_DATE, DISC_DATE, ORG_REGION, ORG_STATE, ORG_COMP, "+
			             " ORG_DIST, ORG_BUSOFF, CUST_TYPE_CODE, BENT_CAMS_ID, CUST_CAMS_ID,'' " +
			  	         " FROM KASH.CAMS_CUSTOMERT WHERE CUST_PHONE='" + strATN + "' ORDER BY 2 DESC";
                     Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Get Customer Phone, Customer Disconnect date, region, state, company, customer type code, Bent CAMS ID, Cust CAMS ID from table CAMS_CUSTOMERT. Else Loop "+strQuery);
                     Log.write("KASH.QUERY22:"+strQuery);
			       }
			       }
                   catch(SQLException se) {
	                    Log.write(Log.ERROR, "PreorderBean : trait query SQLException in doCSI() se=" + se);
	                    iReturnCode = DB2Unavailable(se);
	               }
	               catch(Exception e) {
	                    Log.write(Log.ERROR, "PreorderBean : trait query Exception in doCSI() e=" + e);
	                    iReturnCode = -11;
	               }
	         }
	      }
	      catch(SQLException se) {
	           Log.write(Log.ERROR, "PreorderBean : trait query SQLException in doCSI() se=" + se);
	           iReturnCode = DB2Unavailable(se);
	      }
	      catch(Exception e) {
	           Log.write(Log.ERROR, "PreorderBean : trait query Exception in doCSI() e=" + e);
	           iReturnCode = -11;
	      }
	  }
	  else
      {

//		  strQuery4 = "SELECT T.TRAIT_NAME, T.TRAIT_TXT " +
//             " FROM DB2.CAMS_TRAITT T, DB2.CAMS_TRAITT T2 WHERE T2.SENT_PHONE='" + strWTN + "' AND (T2.BENT_CAMS_ID='" + strAN + "' " +
//             " OR T2.CUST_CAMS_ID='" + strAN + "') AND T2.CUST_PHONE=T.CUST_PHONE AND T.TRAIT_EFF_DATE=(SELECT MAX(T1.TRAIT_EFF_DATE) FROM DB2.CAMS_TRAITT T1 " +
//             " WHERE T.CUST_PHONE=T1.CUST_PHONE AND T1.TRAIT_NAME='PASSCODE-CAPP') AND T.TRAIT_NAME='PASSCODE-CAPP'";
		  strQuery4 = "SELECT T.TRAIT_NAME, T.TRAIT_TXT " +
             " FROM KASH.CAMS_TRAITT T, KASH.CAMS_TRAITT T2 WHERE T2.SENT_PHONE='" + strWTN + "' AND (T2.BENT_CAMS_ID='" + strAN + "' " +
             " OR T2.CUST_CAMS_ID='" + strAN + "') AND T2.CUST_PHONE=T.CUST_PHONE AND T.TRAIT_EFF_DATE=(SELECT MAX(T1.TRAIT_EFF_DATE) FROM KASH.CAMS_TRAITT T1 " +
             " WHERE T.CUST_PHONE=T1.CUST_PHONE AND T1.TRAIT_NAME='PASSCODE-CAPP') AND T.TRAIT_NAME='PASSCODE-CAPP'";
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean: To Get Trait Name, Trait Exists from table CAMS_TRAITT based on the maximum Trait Effective Date"+strQuery4);
         Log.write("KASH.QUERY23:"+strQuery4);

          try {
             rs4 = stmtCAMS.executeQuery(strQuery4);
             if (rs4.next()==true)
		     {
		  	    strTraitName = rs4.getString(1);
		  	    strCustPswdPin = rs4.getString(2);
                            
                             Log.write("KASH.QUERY23: In rs4.next() true"+strTraitName);

//		  	    strQuery = "SELECT S.CUST_PHONE, S.CUST_INSRV_DATE, C.DISC_DATE, S.ORG_REGION, S.ORG_STATE, S.ORG_COMP, "+
//			       " S.ORG_DIST, S.ORG_BUSOFF, S.SENT_ENT_TYPE_CODE, S.BENT_CAMS_ID, S.CUST_CAMS_ID, S.SENT_DCNCT_DATE " +
//			       " FROM DB2.CAMS_SENTT S, DB2.CAMS_CUSTOMERT C, DB2.CAMS_TRAITT T WHERE S.SENT_PHONE='" + strWTN + "' " +
//			       " AND C.CUST_PHONE=S.CUST_PHONE AND C.CUST_INSRV_DATE=S.CUST_INSRV_DATE " +
//                   " AND S.CUST_PHONE=T.CUST_PHONE AND T.TRAIT_NAME='" + strTraitName + "' ORDER BY 2 DESC";
		  	    strQuery = "SELECT S.CUST_PHONE, S.CUST_INSRV_DATE, C.DISC_DATE, S.ORG_REGION, S.ORG_STATE, S.ORG_COMP, "+
			       " S.ORG_DIST, S.ORG_BUSOFF, S.SENT_ENT_TYPE_CODE, S.BENT_CAMS_ID, S.CUST_CAMS_ID, S.SENT_DCNCT_DATE " +
			       " FROM KASH.CAMS_SENTT S, KASH.CAMS_CUSTOMERT C, KASH.CAMS_TRAITT T WHERE S.SENT_PHONE='" + strWTN + "' " +
			       " AND C.CUST_PHONE=S.CUST_PHONE AND C.CUST_INSRV_DATE=S.CUST_INSRV_DATE " +
                   " AND S.CUST_PHONE=T.CUST_PHONE AND T.TRAIT_NAME='" + strTraitName + "' ORDER BY 2 DESC";
          Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get Customer Phone, Customer Disconnect date, state, company, district, SENT_ENT_TYPE_CODE, BENT_CAMS_ID, CUST_CAMS_ID from tables CAMS_SENTT, CAMS_CUSTOMERT, CAMS_TRAITT. "+strQuery);
          Log.write("KASH.QUERY24:"+strQuery);
	         }
	         else
	         {
//			    strQuery = "SELECT S.CUST_PHONE, S.CUST_INSRV_DATE, C.DISC_DATE, S.ORG_REGION, S.ORG_STATE, S.ORG_COMP, "+
//                   " S.ORG_DIST, S.ORG_BUSOFF, S.SENT_ENT_TYPE_CODE, S.BENT_CAMS_ID, S.CUST_CAMS_ID, S.SENT_DCNCT_DATE " +
//                   " FROM DB2.CAMS_SENTT S, DB2.CAMS_CUSTOMERT C WHERE S.SENT_PHONE='" + strWTN + "' " +
//                   " AND C.CUST_PHONE=S.CUST_PHONE AND C.CUST_INSRV_DATE=S.CUST_INSRV_DATE ORDER BY 2 DESC";
			    strQuery = "SELECT S.CUST_PHONE, S.CUST_INSRV_DATE, C.DISC_DATE, S.ORG_REGION, S.ORG_STATE, S.ORG_COMP, "+
                   " S.ORG_DIST, S.ORG_BUSOFF, S.SENT_ENT_TYPE_CODE, S.BENT_CAMS_ID, S.CUST_CAMS_ID, S.SENT_DCNCT_DATE " +
                   " FROM KASH.CAMS_SENTT S, KASH.CAMS_CUSTOMERT C WHERE S.SENT_PHONE='" + strWTN + "' " +
                   " AND C.CUST_PHONE=S.CUST_PHONE AND C.CUST_INSRV_DATE=S.CUST_INSRV_DATE ORDER BY S.SENT_INSRV_DATE DESC";
          Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get Customer Phone, Customer Disconnect date, state, company, district, SENT_ENT_TYPE_CODE, BENT_CAMS_ID, CUST_CAMS_ID from tables CAMS_SENTT, CAMS_CUSTOMERT. Else Loop."+strQuery);
          Log.write("KASH.QUERY25:"+strQuery);
	         }
	      }
	      catch(SQLException se) {
	           Log.write(Log.ERROR, "PreorderBean : trait query SQLException in doCSI() se=" + se);
	           iReturnCode = DB2Unavailable(se);
	      }
	      catch(Exception e) {
	           Log.write(Log.ERROR, "PreorderBean : trait query Exception in doCSI() e=" + e);
	           iReturnCode = -11;
	      }
      }

      try {
         rs = stmtCAMS.executeQuery(strQuery);
         if (rs.next()==true)
         {
            Log.write("Query that got executed now is: " + strQuery );
            bFound = true;
            strCustPhone = rs.getString(1);
            if (bAN) {
               strATN = strCustPhone;
            }
            strCustInSrvDate =  rs.getString(2);
            String strDiscDate =  rs.getString(3);

            strOrgRegion = rs.getString(4);        //Now these key fields can be used to query any table....
                                strOrgState = rs.getString(5);
                                strOrgComp = rs.getString(6);
                                strOrgDist = rs.getString(7);
                                strOrgBusOff = rs.getString(8);
            strCS = rs.getString(9);
            if (strCS.equals("RES")) {
               strTOS1="2";
            }
            else if (strCS.equals("BUS")) {
               strTOS1="1";
            }
            strBentCamsId = rs.getString(10);
            strCustCamsId = rs.getString(11);
            String strSentDcnctDate = rs.getString(12);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Got cust phone[" + strCustPhone + " - " + strCustInSrvDate + "]");
            if (strDiscDate==null || strDiscDate.length() == 0 || strDiscDate.equals("0000-00-00"))
            {
				if (strSentDcnctDate == null || strSentDcnctDate.length() == 0)
				{
                                        //dont change bFound value leave it as true - Antony - 05/01/2014
                                        Log.write(Log.DEBUG_VERBOSE, "PreorderBean : inside DISC DATE 0000 value and if sentdcnctdate is null....");
                                }
				else
				{
					Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Disconnected-dont show CSI");
                    bFound = false;

				}
		    }
            else
            {   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Disconnected-dont show CSI");
               bFound = false;
            }
            // Get TRAIT ICLEC-LSP-ID and check new code added by Venkatesh
             String strQueryForTraitName =  "SELECT T.TRAIT_NAME " +
             " FROM KASH.CAMS_TRAITT T WHERE T.SENT_PHONE='" + strATN + "' AND (T.BENT_CAMS_ID='" + strAN + "' " +
             " OR T.CUST_CAMS_ID='" + strAN + "') AND T.TRAIT_NAME='ICLEC-LSP-ID'";
             
            Log.write("KASH.QUERY FOR ICLEC-LSP-ID:"+strQueryForTraitName);
     
       
            try {
                     rs4 = stmtCAMS.executeQuery(strQueryForTraitName);
                     Log.write("Inside try block, after exec query to get TRAIT ICLEC-LSP-ID");
                     if (rs4.next()==true)
		     {
                         strTraitName = rs4.getString(1);

                            Log.write("Inside exec block...getting rs4.getString for strTraitName");
                            if ((strTraitName != null ) && (strTraitName.trim().length() != 0 ) && (strTraitName.trim().equals("ICLEC-LSP-ID") )){
                                Log.write("Inside  exec block...strTraitName equals ICLEC-LSP-ID");
                                bFound = false;
                            }
                     }
                     
            }
            catch(SQLException se) {
	           Log.write(Log.ERROR, "PreorderBean : trait query SQLException in doCSI() se=" + se);
	           iReturnCode = DB2Unavailable(se);
	      }
	      catch(Exception e) {
	           Log.write(Log.ERROR, "PreorderBean : trait query Exception in doCSI() e=" + e);
	           iReturnCode = -11;
	      }
            // End New code for TRAIT ICLEC-LSP-ID by Venkatesh
      }
         else
         {   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Disconnected-dont show CSI");
			 bFound = false;
         }

         if (!bFound)
         {
            LSRdao lsrDao = new LSRdao();

            String sent_tn = lsrDao.checkNuvoxTNStatus(strATN);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean() from db sent_tn="+sent_tn);

            if(sent_tn != null) {

               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Non-Windstream TN ("+strATN+")");
               //strRemark = "Submit all CSR requests for spids 8660, 4890 and 8934 to csrrequests@windstream.com";
               //strRemark = "Submit CSR requests to wci.csrrequests@windstream.com";
               strRemark = "For questions or concerns, please email wci.scsc.lspac@windstream.com";
               completeCSIResponse(iPreSqncNmbr, iVersion, "001", "This is a Non-Windstream Customer", strRemark);
               updatePreorderICARE(iPreSqncNmbr, iVersion);
	        } else {
	        String sent_discdate = lsrDao.checkTNStatus(strATN);
	        if(sent_discdate != null){
                Log.write(Log.DEBUG_VERBOSE, "PreorderBean : CSI ATN/WTN not found or Disconn set return code and exit");
                //completeCSIResponse(iPreSqncNmbr, iVersion, "033", "Telephone Number InActive in windstream", strRemark);
                strRemark = "For questions or concerns, please email wci.scsc.lspac@windstream.com";
                completeCSIResponse(iPreSqncNmbr, iVersion, "033", "Telephone Number Not Active", strRemark);
	        }else{
	        	Log.write(Log.DEBUG_VERBOSE, "PreorderBean : CSI ATN/WTN found or not Disconn set return code and exit");
	            //completeCSIResponse(iPreSqncNmbr, iVersion, "033", "Telephone Number Active in windstream", strRemark);
                    strRemark = "For questions or concerns, please email wci.scsc.lspac@windstream.com";
	            completeCSIResponse(iPreSqncNmbr, iVersion, "033", "This is a Ported Out Telephone Number", strRemark);
	        }
         }
      }
      }
      catch(SQLException se) {
         Log.write(Log.ERROR, "PreorderBean : SQLException CustQuery in doCSI() se=" + se);
         iReturnCode = DB2Unavailable(se);
      }
      catch(Exception e) {
         Log.write(Log.ERROR, "PreorderBean : Exception Custquery in doCSI() e=" + e);
         iReturnCode = -9;
      }
      finally {
         try {   rs.close();
                 rs4.close();
         }
         catch (Exception e) {}
      }
      if (iReturnCode < 0)
      {   rs=null;
          rs4=null;
          DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         if (iReturnCode == DB2_UNAVAILABLE)
         {
            strRemark = " Host system unavailable, please try transaction later ";
            completeCSIResponse(iPreSqncNmbr, iVersion, "014", "Host system unavailable to process Transaction", strRemark);
            iReturnCode = 0;
         }
         return iReturnCode;
      }

      if (bFound == false)   //No reason to continue, get out!
      {   rs=null;
          rs4=null;
         DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         return iReturnCode;
      }

      //------------------------------------------------------------------------------------
      //       Get the Service Address (and other key fields)
      //
      // We do this for all CSI types (E,M,T)
      //------------------------------------------------------------------------------------
      // See if a trait defines which address type (addr-id) holds service address.
      String strAddrId = "";
      try {
         //See if a Service Address Trait exists -if it does, use that ADDR-ID, otherwise take what we can get....
//         rs = stmtCAMS.executeQuery("SELECT T.TRAIT_TXT FROM DB2.CAMS_TRAITT T WHERE T.CUST_PHONE='" + strCustPhone +
//            "' AND T.CUST_INSRV_DATE='" + strCustInSrvDate + "' " +
//            " AND T.TRAIT_NAME='SERV-ADDR-ID' ORDER BY T.TRAIT_EFF_DATE DESC ");
      String strQuery23 = "SELECT T.TRAIT_TXT FROM KASH.CAMS_TRAITT T WHERE T.CUST_PHONE='" + strCustPhone +
            "' AND T.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd')" +
            " AND T.TRAIT_NAME='SERV-ADDR-ID' ORDER BY T.TRAIT_EFF_DATE DESC ";

    Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Check for Address Trait Exists from table CAMS_TRAITT if the Trait Name is 'SERV-ADDR-ID'. "+strQuery23);
    Log.write("KASH.QUERY26:"+strQuery23);

         rs = stmtCAMS.executeQuery("SELECT T.TRAIT_TXT FROM KASH.CAMS_TRAITT T WHERE T.CUST_PHONE='" + strCustPhone +
            "' AND T.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd')" +
            " AND T.TRAIT_NAME='SERV-ADDR-ID' ORDER BY T.TRAIT_EFF_DATE DESC ");
       //Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Query Line # 3262 : Check for Address Trait Exists from table CAMS_TRAITT if the Trait Name is 'SERV-ADDR-ID'. "+rs);
            //" AND T.SENT_PHONE='" + strATN + "' AND T.TRAIT_NAME='SERV-ADDR-ID'"); //HD 107341 chgd query

         if (rs.next()==true)
         {
            strAddrId = rs.getString(1);
            strAddrId = strAddrId.trim();
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Service Addr trait of [" + strAddrId + "] found");
         }
      }
      catch(SQLException se) {
         Log.write(Log.ERROR, "PreorderBean : trait query SQLException in doCSI() se=" + se);
         iReturnCode = DB2Unavailable(se);
      }
      catch(Exception e) {
         Log.write(Log.ERROR, "PreorderBean : trait query Exception in doCSI() e=" + e);
         iReturnCode = -7;
      }
      finally {
         try {   rs.close();
         }
         catch (Exception e) {}
      }
      if (iReturnCode < 0)
      {   rs=null;
                   DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         if (iReturnCode == DB2_UNAVAILABLE)
         {
            strRemark = " Host system unavailable, please try transaction later ";
            completeCSIResponse(iPreSqncNmbr, iVersion, "014", "Host system unavailable to process Transaction", strRemark);
            iReturnCode = 0;
         }
         return iReturnCode;
      }

      //-------------------------------------------------------------------------------------
      // Before we go on, we must see if this customer is a CLEC or resale account. If so, we
      // can't give out their account information (unless the acct belongs to the requesting
      // CLEC.
      //-------------------------------------------------------------------------------------
      boolean bShowCSI = bDisplayCSI(iPreSqncNmbr, iVersion, strOCNCd, strATN, stmtCAMS, strOrgRegion,
         strOrgState, strOrgComp, strOrgDist, strOrgBusOff, strCustPhone, strCustInSrvDate);
      if (!bShowCSI)
      {
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : CSI NOT allowed ("+strATN+")");
         strRemark = "Please contact Customer or Customer''s current provider for CSR information.";
         //String strTemp = "UPDATE CSIR_T SET CSIR_RESPC='030', " +
         //   " MDFD_DT=sysdate, MDFD_USERID='auto', " +
         //   " CSIR_RESPD='Not Applicable - Not a Windstream Customer', " +
         //   " CSIR_REMARKS='" +strRemark+ "' " +
         //   " WHERE PRE_ORDR_SQNC_NMBR=" + iPreSqncNmbr + " AND PRE_ORDR_VRSN=" + iVersion;
         String strTemp = "UPDATE CSIR_T SET CSIR_RESPC='030', " +
                 " MDFD_DT=sysdate, MDFD_USERID='auto', " +
                 " CSIR_RESPD='This is a Resold Telephone Number', " +
                 " CSIR_REMARKS='" +strRemark+ "' " +
                 " WHERE PRE_ORDR_SQNC_NMBR=" + iPreSqncNmbr + " AND PRE_ORDR_VRSN=" + iVersion;
         try {
            m_stmt.executeUpdate(strTemp);
            rs.close();
            rs=null;
         }
         catch (Exception e1) {}
         DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         return iReturnCode;
      }

	  if (!strBentCamsId.equals(strAN) && !strCustCamsId.equals(strAN))
	  {
		   strRESPC = "001";
		   strRESPD = "Account Information Not Found";
		   bInvalidAN = true;
		   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Invalid AN ("+strAN+")");
      }

      if (strCustPswdPin != null && strCustPswdPin.length() > 0 && strPSWD != null)
      {
		     strCustPswdPin = strCustPswdPin.trim();
             strPSWD = strPSWD.trim();
             //Code Change to pass the validation for lowercase PasswordPin - Saravanan
             if (!strCustPswdPin.equalsIgnoreCase(strPSWD))
             {
		         strRESPC_1 = "061";
		         strRESPD_1 = "Invalid PSWD_PIN";
		         bInvalidPW = true;
		         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Invalid PSWD ("+strPSWD+")");
	         }
	  }
	  else if (strPSWD == null && strCustPswdPin != null && strCustPswdPin.length() > 0)
	  {
			  strRESPC_1 = "061";
			  strRESPD_1 = "Invalid PSWD_PIN";
			  bInvalidPW = true;
		      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Invalid PSWD ("+strPSWD+")");
      }
	  else
	  {}

	  if (bInvalidAN || bInvalidPW)
	  {
           String strTemp = "UPDATE CSIR_T SET CSIR_RESPC='" +strRESPC+ "', " +
		   	  " MDFD_DT=sysdate, MDFD_USERID='auto', CSIR_RESPD='" +strRESPD+ "', " +
		   	  " CSIR_RESPC_1='" +strRESPC_1+ "', CSIR_RESPD_1='" +strRESPD_1+ "', " +
		   	  " CSIR_REMARKS='" +strRemark+ "' " +
			  " WHERE PRE_ORDR_SQNC_NMBR=" + iPreSqncNmbr + " AND PRE_ORDR_VRSN=" + iVersion;
		   try {
			  m_stmt.executeUpdate(strTemp);
			  rs.close();
			  rs=null;
	       }
		   catch (Exception e1) {}
           DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
	       return iReturnCode;
	  }

        //HD0000002250687 changed [AND A.STATE_CODE ='" + strOrgState + "'] to [AND A.ORG_STATE ='" + strOrgState + "']
//      strQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.ADDR_LINE1, A.ADDR_LINE2, A.ADDR_LINE3, " +
//         " A.ADDR_LINE4, A.ADDR_LINE5, A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE  " +
//         " FROM DB2.CAMS_ADDRESST A WHERE A.ORG_REGION='"+strOrgRegion+"' AND A.CUST_PHONE='" + strCustPhone +
//         "' AND A.CUST_INSRV_DATE='" + strCustInSrvDate + "' AND A.ORG_STATE ='" + strOrgState + "' " +//datechange
//         " AND A.ORG_COMP='" + strOrgComp + "' ";
      strQuery = "SELECT DISTINCT A.ADDR_ID, A.ADDR_EFF_DATE, A.ADDR_LINE1, A.ADDR_LINE2, A.ADDR_LINE3, " +//address line change here
         " NVL(A.ADDR_LINE4,''), NVL(A.ADDR_LINE5,''), A.CITY_NAME, A.STATE_CODE, A.ZIP_CODE  " +
         " FROM KASH.CAMS_ADDRESST A WHERE A.ORG_REGION='"+strOrgRegion+"' AND A.CUST_PHONE='" + strCustPhone +
         "' AND A.CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND A.ORG_STATE ='" + strOrgState + "' " +
         " AND A.ORG_COMP='" + strOrgComp + "' ";
    Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get distinct Address Id, Address Effective Date, Address Line1, Address Line 2, Address Line3, Address Line 4, Address Line 5, City, State, Zip from table CAMS_ADDRESST. "+strQuery);
    Log.write("KASH.QUERY27:"+strQuery);
    
      if (strAddrId.length() > 0)
         strQuery += " AND A.ADDR_ID='" + strAddrId + "' ";      //specific addr_id was defined for service address
      else
         strQuery += " AND (A.ADDR_ID='SAD1' OR A.ADDR_ID='MAIN') ";
      strQuery += " ORDER BY 2 DESC, 1 DESC ";

      try {
         Log.write("querying CAMS DB2 data warehouse now by TN ...Query=[" + strQuery + "]");
         rs = stmtCAMS.executeQuery(strQuery);
         if (rs.next() == true)   //only get the first (should be most recent record)
         {
            Log.write(Log.DEBUG_VERBOSE, "SAD1 and/or MAIN address found");
            bFound = true;
            //String strName = (rs.getString(3)).substring(0,25);   //default name to ADDR_LINE1
            
            String strName = (rs.getString(3));   //default name to ADDR_LINE1// removed substring to avoid arr out of bnds exc - Antony
            String strCity = "";
            if(rs.getString(8) != null && rs.getString(8).length() > 0) {
                            Log.write("City is not null !");
                            strCity = Toolkit.replaceSingleQwithDoubleQ(rs.getString(8));
                       } else {
                            Log.write("City is null !");
                            strCity = " ";
                            //do not append null value to existing strCAIValue -- Antony - 04/30/2014
                       }
            String strState = "";
            if(rs.getString(9) != null && rs.getString(9).length() > 0) {
                Log.write("State is not null !");
                strState = Toolkit.replaceSingleQwithDoubleQ(rs.getString(9));
           } else {
                Log.write("State is null !");
                strState = " ";
                //do not update null value to existing CSIR_STATE value.
           }
            String strUpdate = "UPDATE CSIR_T SET CSIR_AFT='D', CSIR_CITY='" +
                       strCity +
                       "', CSIR_STATE='" + strState + "', CSIR_ZIP='" + rs.getString(10) +
                       "', CSIR_ATN='" + strATN_d + "', CSIR_AN='" + strAN + "', CSIR_CS='" + strCS + "', CSIR_TOS_1='" + strTOS1 +
                       //"', CSIR_CAI='" +
                       "', CSIR_CAI='";
                       //Toolkit.replaceSingleQwithDoubleQ((rs.getString(3)+rs.getString(4)+rs.getString(5)+rs.getString(6)).substring(0,119)) +
                       //remove Addr line 4 from here and add it separately after null check
                       String strCAIValue = "";
                       strCAIValue = rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5);
                       if(rs.getString(6) != null && rs.getString(6).length() > 0) {
                            Log.write("Addr Line 4 is not null !");
                            strCAIValue += " "+rs.getString(6);
                       } else {
                            Log.write("Addr Line 4 is null !");
                            //do not append null value to existing strCAIValue -- Antony - 04/30/2014
                       }
                       
                       strUpdate += Toolkit.replaceSingleQwithDoubleQ(strCAIValue) +// removed substring to avoid arr out of bnds/Nlptr exc - Antony
                       "', CSIR_DDQTY='1', CSIR_AAI='";
                       //added nvl function in sql query above - strQuery - to convert to '' if null found as the next line was 
                       //encountering a null pointer exception - Antony - 04/24/2014
                       //Toolkit.replaceSingleQwithDoubleQ(rs.getString(7)) + "', " +//do null check and replace by "" below - Antony
                    
                       String strAddrLine5 = "";
            
                       if(rs.getString(7) == null) {
                            Log.write("Addr line 5 is null ! Replacing with blank.");
                            strAddrLine5 = " ";//Added one blank space to make the field visible in the form - Antony - 04/30/2014
                       } else {
                            Log.write("Addr line 5 is not null !");
                            strAddrLine5 = Toolkit.replaceSingleQwithDoubleQ(rs.getString(7));
                       }
                
                       strUpdate += strAddrLine5  + "', " +
                       " CSIR_REMARKS='" + strRemark + "', CSIR_RESPC='027', CSIR_RESPD='Transaction Successful' ";
            
            Log.write("strUpdate = "+strUpdate);
            
            //Before reusing ResultSet, close() it
            try {
               rs.close();
            }
            catch (Exception e1) {}

            //See if the name exists on the CUSTOMERT table - if so, use this one instead of ADDR_LINE1
//  11-15-2004 Per Tana Henson, we will not look at CAMS_CUSTOMERT for name info.  Sometimes the 2 names differ -and they normally dont.
//This is due to conversions. Having the 2 names differ is causing a lot of customer questions.
//            rs = stmtCAMS.executeQuery("SELECT LAST_NAME, FIRST_NAME FROM DB2.CAMS_CUSTOMERT WHERE ORG_REGION='" + strOrgRegion + "' AND CUST_PHONE='" + strCustPhone + "' AND CUST_INSRV_DATE='" + strCustInSrvDate + "'");
//            if (rs.next()==true)
//            {   String strTemp = (rs.getString(1)).trim();
//               if ( strTemp.length() > 2 )
//               {   strName = strTemp;
//                  strTemp = (rs.getString(2)).trim();
//                  if (strTemp.length() > 2)
//                  {   strName += ", " + strTemp;
//                     strName = strName.trim();
//                  }
//                  if (strName.length() > 25)   //has to fit in db field of len 25
//                  {   strName = strName.substring(0,25);
//                  }
//                  Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Name=[" + strName + "]");
//               }
//            }

            strUpdate += " ,CSIR_NAME='" + Toolkit.replaceSingleQwithDoubleQ(strName) + "' ";

            
            Log.write("strUpdate after adding name = "+strUpdate);
            
//            try {
//               rs.close();
//            }
//            catch (Exception e1) {}

            //See if a Freeze PIC trait exists -setting at order level -may need to add to a repeating section at
            //some point.
//            rs = stmtCAMS.executeQuery("SELECT SUBSTR(TRAIT_TXT,1,1) FROM DB2.CAMS_TRAITT WHERE ORG_REGION='" + strOrgRegion + "' AND ORG_STATE='" + strOrgState + "' AND CUST_PHONE='" + strCustPhone + "' AND CUST_INSRV_DATE='" + strCustInSrvDate + "' AND TRAIT_NAME='PIC-LOCK'");
            
            String strQry = "SELECT SUBSTR(TRAIT_TXT,1,1) FROM KASH.CAMS_TRAITT WHERE ORG_REGION='" + 
                                        strOrgRegion + "' AND ORG_STATE='" + strOrgState + "' AND CUST_PHONE='" + 
                                        strCustPhone + "' AND CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND TRAIT_NAME='PIC-LOCK' ORDER BY TRAIT_EFF_DATE DESC";

            Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Get trimmed characters of Trait Exists for table CAMS_TRAITT if traint name ='PIC-LOCK'."+strOrgRegion+"/"+strOrgState+"/"+strCustPhone+"/"+strCustInSrvDate);
            Log.write("KASH.CAMS28:"+strQry);
            
            rs = stmtCAMS.executeQuery("SELECT SUBSTR(TRAIT_TXT,1,1) FROM KASH.CAMS_TRAITT WHERE ORG_REGION='" + 
                                        strOrgRegion + "' AND ORG_STATE='" + strOrgState + "' AND CUST_PHONE='" + 
                                        strCustPhone + "' AND CUST_INSRV_DATE=to_date(substr('" + strCustInSrvDate + "',0,10),'yyyy-mm-dd') AND TRAIT_NAME='PIC-LOCK' ORDER BY TRAIT_EFF_DATE DESC");
        //  Log.write(Log.DEBUG_VERBOSE, "PreorderBean: Query Line # 3443 : Get trimmed characters of Trait Exists for table CAMS_TRAITT if traint name ='PIC-LOCK'.  "+rs);
            if (rs.next()==true)
            {
               String strTemp = rs.getString(1);
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Freeze PIC trait found =[" + strTemp + "]");
               if (strTemp.length() > 0 && strTemp != null)
               {   strUpdate += ", CSIR_FPIC='" + strTemp + "' ";
               }
            }
            try {
               rs.close();
            }
            catch (Exception e1) {}

            strUpdate += ", MDFD_DT=sysdate, MDFD_USERID='auto' WHERE PRE_ORDR_SQNC_NMBR=" + iPreSqncNmbr + " AND PRE_ORDR_VRSN=" + iVersion;

            Log.write("strUpdate Query : "+strUpdate);
            
            m_stmt.executeUpdate(strUpdate);

         }//end-if
         else      //NOTHING found in CAMS !
         {
            LSRdao lsrDao = new LSRdao();

            String sent_tn = lsrDao.checkNuvoxTNStatus(strATN);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean() from db sent_tn="+sent_tn);

            if(sent_tn != null) {

               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Non-Windstream TN ("+strATN+")");
               //strRemark = "Submit all CSR requests for spids 8660, 4890 and 8934 to csrrequests@windstream.com";
               //strRemark = "Submit CSR requests to wci.csrrequests@windstream.com";
               strRemark = "For questions or concerns, please email wci.scsc.lspac@windstream.com";
               completeCSIResponse(iPreSqncNmbr, iVersion, "033", "This is a Non-Windstream Customer", strRemark);
               updatePreorderICARE(iPreSqncNmbr, iVersion);
	        } else {
	        String sent_discdate = lsrDao.checkTNStatus(strATN);
		    if(sent_discdate != null){
                Log.write(Log.DEBUG_VERBOSE, "PreorderBean : CSI ATN/WTN not found set return code and exit");
                //completeCSIResponse(iPreSqncNmbr, iVersion, "033", "Telephone Number InActive in windstream", strRemark);
                strRemark = "For questions or concerns, please email wci.scsc.lspac@windstream.com";
                completeCSIResponse(iPreSqncNmbr, iVersion, "033", "Telephone Number Not Active", strRemark);
		    }else{
		    	Log.write(Log.DEBUG_VERBOSE, "PreorderBean : CSI ATN/WTN found set return code and exit");
	            //completeCSIResponse(iPreSqncNmbr, iVersion, "033", "Telephone Number Active in windstream", strRemark);
                    strRemark = "For questions or concerns, please email wci.scsc.lspac@windstream.com";
	            completeCSIResponse(iPreSqncNmbr, iVersion, "033", "This is a Ported Out Telephone Number", strRemark);
		    }
	        }
         }
         Log.write(Log.DEBUG_VERBOSE, "PreorderBean : doCSI() serv addr done");

      }
      catch (SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : Exception2 in doCSI() se=" + se);
         iReturnCode = DB2Unavailable(se);
      }
      catch (Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception2 in doCSI() e=" + e);
         iReturnCode =-8;
      }
      if (iReturnCode < 0)
      {
         try {
            rs.close();
         }
         catch (Exception e) {}
         rs=null;
                   DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         if (iReturnCode == DB2_UNAVAILABLE)
         {
            strRemark = " Host system unavailable, please try transaction later ";
            completeCSIResponse(iPreSqncNmbr, iVersion, "014", "Host system unavailable to process Transaction", strRemark);
            iReturnCode = 0;
         }
                   return iReturnCode;
                }

      if (bFound == false)   //No reason to continue, get out!
      {
         DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         return iReturnCode;
      }

      //------------------------------------------------------------------------------------------
      // Now get customer's features and services.
      //------------------------------------------------------------------------------------------
      if (strXtnType.equals("E") || strXtnType.equals("M"))
      {
         iReturnCode = getFeatures(iPreSqncNmbr, iVersion, strATN, stmtCAMS,
                    strOrgRegion, strOrgState, strOrgComp, strOrgDist, strOrgBusOff,
                    strCustPhone, strCustInSrvDate, bATN, bAN);
      }
      if (iReturnCode < 0)
      {
         try {
            rs.close();
            rs=null;
         }
         catch (Exception e) {}
         DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         if (iReturnCode == DB2_UNAVAILABLE)
         {
            strRemark = " Host system unavailable, please try transaction later ";
            completeCSIResponse(iPreSqncNmbr, iVersion, "014", "Host system unavailable to process Transaction", strRemark);
            iReturnCode = 0;
         }
         return iReturnCode;
      }

      //------------------------------------------------------------------------------------------
      // Now get directory information from express table (that was loaded from DCRIS extract data)
      //------------------------------------------------------------------------------------------
      if (strXtnType.equals("M") || strXtnType.equals("T"))
      {
         iReturnCode = getDirectoryInfo(iPreSqncNmbr, iVersion, strATN, stmtCAMS, strOrgRegion, strOrgState,
                      strCustPhone, strCustInSrvDate, bAN);
      }
      if (iReturnCode < 0)
      {
         try {
            rs.close();
            rs=null;
         }
         catch (Exception e) {}
         DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         if (iReturnCode == DB2_UNAVAILABLE)
         {
            strRemark = " Host system unavailable, please try transaction later ";
            completeCSIResponse(iPreSqncNmbr, iVersion, "014", "Host system unavailable to process Transaction", strRemark);
            iReturnCode = 0;
         }
         return iReturnCode;
      }

      //------------------------------------------------------------------------------------------
      // Get all the served phone numbers are get their PIC and LPIC and put in CSIR_PIC_T table
      //------------------------------------------------------------------------------------------
      if (strXtnType.equals("E") || strXtnType.equals("M"))
      {
         iReturnCode = getTNsAndPICs(iPreSqncNmbr, iVersion, strATN, stmtCAMS,
                      strOrgRegion, strOrgState, strOrgComp, strOrgDist, strOrgBusOff,
                      strCustPhone, strCustInSrvDate, bATN, bAN);
      }
      if (iReturnCode < 0)
      {
         try {   rs.close();
            rs=null;
         }
         catch (Exception e) {}
         DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         if (iReturnCode == DB2_UNAVAILABLE)
         {
            strRemark = " Host system unavailable, please try transaction later ";
            completeCSIResponse(iPreSqncNmbr, iVersion, "014", "Host system unavailable to process Transaction", strRemark);
            iReturnCode = 0;
         }
         return iReturnCode;
      }

      //------------------------------------------------------------------------------------------
      //   Now get directory delivery address and put results in CSIR_DDA_T
      //------------------------------------------------------------------------------------------
      if (strXtnType.equals("E") || strXtnType.equals("M"))
      {
         iReturnCode = getDirectoryDeliveryAddress(iPreSqncNmbr, iVersion, strATN, stmtCAMS,
                     strOrgRegion, strOrgState, strOrgComp, strOrgDist, strOrgBusOff,
                     strCustPhone, strCustInSrvDate, bATN, bAN);
      }
      if (iReturnCode < 0)
      {
         try {   rs.close();
            rs=null;
         }
         catch (Exception e) {}
         DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
         if (iReturnCode == DB2_UNAVAILABLE)
         {
            strRemark = " Host system unavailable, please try transaction later ";
            completeCSIResponse(iPreSqncNmbr, iVersion, "014", "Host system unavailable to process Transaction", strRemark);
            iReturnCode = 0;
         }
         return iReturnCode;
      }

      //---------------------------------------------------------------------------
      // Now check for hunt group stuff and put in CSIR_HG_T
      //---------------------------------------------------------------------------
      //Hunt group stuff not available - so CSIR_HG_T will always be EMPTY !

      //clean up
      try {
         rs.close();
         rs=null;
         stmtCAMS.close();
         stmtCAMS=null;
         DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
      }
      catch(Exception e) {}

      return iReturnCode;
   }


        /**
    * Using the user-entered NPANXX, query Frontware database to get a corresponding
    * BOID/BEX. Then use BOID/BEX to look up available features and services.
         * @param   int Preorder Sequence Number.
         * @param   int Preorder Version Number.
    * @return  int Return Code (0 = OK, <0 = Error)
    */
        private int doFeatureServiceLookup(int iPreSqncNmbr, int iVersion)
        {
               int iReturnCode = 0;
      Connection connFW = null;
      Statement stmtFW = null;
      String strNPA = "";
      String strNXX = "";
      String strBoid = "";
      String strBex = "";
      int iFeatureCount = 0;
      ResultSet rs = null;

      //First let's get the NPANXX to do the lookup
      try
      {
         rs = m_stmt.executeQuery("SELECT SUBSTR(FS_NPANXX,1,3), SUBSTR(FS_NPANXX,5,3) FROM FS_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr);
         if (rs.next())
         {
            strNPA = rs.getString(1);
            strNXX = rs.getString(2);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : NPANXX = " + strNPA + "-" + strNXX);
            //Now clean up any prior records
            m_stmt.executeUpdate("DELETE FROM FSR_FS_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
         }
         else
         {   Log.write(Log.ERROR, "PreorderBean : Err getting NPANXX from FS_T for PO=[" + iPreSqncNmbr + "]");
            iReturnCode = -1;
         }
      }
      catch(SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQLException in doFeatureServiceLookup() se=" + se);
         iReturnCode = -1;
      }
      catch(Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception7 in doFeatureServiceLookup() e=" + e);
         iReturnCode = -2;
      }
      if (iReturnCode < 0)
      {   return iReturnCode;
      }

      String strQuery1 = "SELECT DISTINCT C.BOID, C.BEX FROM FW.CARRIER C WHERE C.CARRIER_NPA = '" + strNPA +
         "' AND C.CARRIER_NXX = '" + strNXX + "'";

      //Get connection to Frontware
      try {
         connFW = DatabaseManager.getConnection(DatabaseManager.FWP_CONNECTION);
         stmtFW = connFW.createStatement();
         //Get BOID BEX
         rs = stmtFW.executeQuery(strQuery1);
         if (rs.next() == true)
         {
            //Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Got something back from FWP");
            strBoid = rs.getString(1);
            strBex = rs.getString(2);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Boid="+strBoid+" Bex="+strBex);

            //OK, we got a valid boid/bex.
//HD0000002472840S
//				//Now get list of Features and Services we should be including.
//				strQuery1 = "SELECT DISTINCT INCLD_CTGRY, ASOC_CODE FROM FEATURE_INCLUSION_T WHERE NPA='*' AND NXX='*' UNION " +
//					    "SELECT DISTINCT INCLD_CTGRY, ASOC_CODE FROM FEATURE_INCLUSION_T WHERE NPA='" + strNPA + "' AND NXX='*' UNION " +
//					    "SELECT DISTINCT INCLD_CTGRY, ASOC_CODE FROM FEATURE_INCLUSION_T WHERE NPA='" + strNPA + "' AND NXX='" + strNXX + "'";
//				Vector vKeepFeatures = new Vector();
//				rs = m_stmt.executeQuery(strQuery1);
//				while (rs.next())
//				{
//					Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Got something back from Express DB");
//					vKeepFeatures.addElement(rs.getString(1).trim()+"_"+rs.getString(2).trim());
//				//	Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Keep this category [" + rs.getString(1) + "] Feat[" + rs.getString(2) + "]");
//				}
//				Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Vector size=" + vKeepFeatures.size());
//HD0000002472840F
            // Now using BOID BEX, find list of ASOCs (Features and Services)
            String strQuery2 = "SELECT A.ASOC_CODE, AD.ASOC_DESC, AC.CATEGORY FROM FW.ASOC A, FW.ASOC_DESC AD, FW.ASOC_CATE AC " +
               "WHERE A.BOID = '" + strBoid + "' AND A.BEX = '" + strBex + "' AND A.RECUR_FLAG = 'Y' AND A.EXCLUDE_NXX <> '" + strNXX +
                "' AND AD.ASOC_DESC_NBR = A.ASOC_DESC_NBR AND AC.ASOC_CATE_NBR = A.ASOC_CATE_NBR " +
                "AND AD.ASOC_DESC NOT LIKE '%VALID FOR ASOC%' ORDER BY A.ASOC_CODE";


            String strFetava, strFetDesc, strCategory, strFResp;
            rs = stmtFW.executeQuery(strQuery2);
            PreparedStatement pStmt = m_conn.prepareStatement("INSERT INTO FSR_FS_T VALUES ("+iPreSqncNmbr+","+iVersion+
                                ",?, ?, ?, ?, sysdate, 'auto')");
            while (rs.next() == true)
            {
//               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Got more FWP data");
               strFetava = rs.getString(1).trim();
               strFetDesc = rs.getString(2);
               strCategory = rs.getString(3).trim();
               strFResp = "A";
//HD0000002472840S
//			//See if we should include based on category and ASOC or category and all ASOCs (*)
//				if ( vKeepFeatures.contains(strCategory+"_"+strFetava) || vKeepFeatures.contains(strCategory+"_*") )
//					{
//HD0000002472840F
                  iFeatureCount++;      //Use for occurence
                  Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Feat["+ iFeatureCount+"] = " + strFetava + " Desc=" + strFetDesc);
                  pStmt.setInt(1, iFeatureCount);
                  pStmt.setString(2, strFetava);
                  pStmt.setString(3, strFetDesc);
                  pStmt.setString(4, strFResp);
                  pStmt.executeUpdate();
//HD0000002472840S
//					}
//					else
//					{	//Log.write(Log.DEBUG_VERBOSE, " Skip feat=" + strFetava + " cat=" + strCategory);
//						continue;
//					}
//HD0000002472840F
            }
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : done with loop");
//HD0000002472840S
//            //empty vector
//            vKeepFeatures.clear();
//HD0000002472840F
            rs.close();
            if (iFeatureCount > 0)
            {
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : ready to UPDATE the FSR_T tbl now");
               //Now load FSR_T and FSR_FS_T with results
               String strUpdate = "UPDATE FSR_T SET FSR_RESPC = '027', FSR_RESPD='Transaction Successful', MDFD_DT=sysdate, MDFD_USERID = 'auto' WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion;
               try {
                  m_stmt.executeUpdate(strUpdate);
               }
               catch(SQLException se) {
                  Log.write(Log.WARNING, "PreorderBean : BatchUpdateException in doFeatureServiceLookup()");
                  iReturnCode = -101;
               }
            }
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : almost done");
         }
         else
         {   Log.write(Log.DEBUG_VERBOSE, "PreorderBean : NPANXX not found in Frontware! "); //Set appropriate RESPC and RESPD and return

            String strUpdate = "UPDATE FSR_T SET FSR_RESPC = '019', FSR_RESPD='NPANXX not Found', MDFD_DT=sysdate, MDFD_USERID = 'auto' WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion;
            try {
               m_stmt.executeUpdate(strUpdate);
               iReturnCode = 0;
            }
            catch(SQLException se) {
               Log.write(Log.ERROR, "PreorderBean : doFeatureServiceLookup() NPANXX not fnd update ");
               iReturnCode = -102;
            }
         }
      }//end-try
      catch(SQLException se) {
         Log.write(Log.ERROR, "PreorderBean : FW SQLException in doFeatureServiceLookup() se=" + se);
         iReturnCode = -1;
      }
      catch(Exception e) {
         Log.write(Log.ERROR, "PreorderBean : FW Exception in doFeatureServiceLookup() e=" + e);
         iReturnCode = -2;
      }
      finally
      {
                   DatabaseManager.releaseConnection(connFW, DatabaseManager.FWP_CONNECTION);
                }

               return iReturnCode;

        }//end of doFeatureServiceLookup()


        /**
    * Using the date/time the user submitted their inquiry, give back a canned calculated
    * response. The apointment dates are standard intervals that we retreive from lsr
    * properties file.   Must skip weekends and holidays in calculation.
    * NOTE: This assumes submitdate (DTSENT) begins with MM-DD-YYYY format.
         *
         * @param   int Preorder Sequence Number.
         * @param   int Preorder Version Number.
    * @return  int Return Code (0 = OK, <0 = Error)
    */
        private int doAppointmentSchedule(int iPreSqncNmbr, int iVersion)
        {
               int iReturnCode = 0;
      int iInterval = 0;
      int iUNEInterval = 0;
      boolean bWeekendOrHoliday = true;
      String strSubmitDateTime = "";
      ResultSet rs = null;

      //Use the current date and time to determine appointment inquiry results
      try
      {   rs = m_stmt.executeQuery("SELECT TO_CHAR(SYSDATE,'MM-DD-YYYY-HH24MI') FROM DUAL");
         if (rs.next())
         {
            strSubmitDateTime = rs.getString(1);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : APPT submit date = " + strSubmitDateTime);
         }
      }
      catch(SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQLException in doAppointSchedule() se=" + se);
         iReturnCode = -1;
      }
      catch(Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception in doAppointSchedule() e=" + e);
         iReturnCode = -1;
      }
      if (iReturnCode < 0)  {
         return iReturnCode;
      }
      try {
         iInterval = PropertiesManager.getIntegerProperty("lsr.resale.apptinterval",5);
         iUNEInterval = PropertiesManager.getIntegerProperty("lsr.une.apptinterval",14);
         if (iInterval <  1) iInterval=5;   //safeguard
         if (iUNEInterval <  1) iUNEInterval=14;   //safeguard
      }
      catch(Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception get interval properties=" + e);
      }
      Log.write(Log.DEBUG_VERBOSE, "PreorderBean : Intervals resale=" + iInterval + " UNE=" + iUNEInterval);

      String strTemp =  strSubmitDateTime.substring(6,10) + strSubmitDateTime.substring(0,2) + strSubmitDateTime.substring(3,5);
      String strTemp2 =  strSubmitDateTime.substring(11,13) + strSubmitDateTime.substring(13,15) + "00";

      //Using current submit date/time, figure out actual start time. For example, if appt schedule done at
      //8pm, our starting point is the next business day at start of business (7am).
      String strStart = SLATools.getSLAStartDateTime(strTemp, strTemp2);

      //Convert to calendar object
      Calendar cal = Calendar.getInstance();
      Calendar cal2 = Calendar.getInstance();
      cal.set(Integer.parseInt(strStart.substring(0,4)),
         Integer.parseInt(strStart.substring(4,6)) - 1,
         Integer.parseInt(strStart.substring(6,8)),
         Integer.parseInt(strStart.substring(9,11)),
         Integer.parseInt(strStart.substring(11,13)),
         Integer.parseInt(strStart.substring(13,15)) );
      cal2.set(Integer.parseInt(strStart.substring(0,4)),
         Integer.parseInt(strStart.substring(4,6)) - 1,
         Integer.parseInt(strStart.substring(6,8)),
         Integer.parseInt(strStart.substring(9,11)),
         Integer.parseInt(strStart.substring(11,13)),
         Integer.parseInt(strStart.substring(13,15)) );

      int iDaysAdded = 0;
      int iDOW = 0;
      Holidays h = Holidays.getInstance();
      DateFormat holFmt = new SimpleDateFormat("yyyyMMdd");
      //We now have adjusted starting date/time, add intervals to get results -bypassing weekends & holidays
      while (iDaysAdded < iInterval || bWeekendOrHoliday)
      {
         iDOW =  cal.get(Calendar.DAY_OF_WEEK);
         if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY)
         {   cal.add(Calendar.DATE, 1);
            continue;
         }
         if (h.isHoliday( holFmt.format( cal.getTime() ) ) )
         {   cal.add(Calendar.DATE, 1);
            continue;
         }
         if (iDaysAdded == iInterval)
            break;
         cal.add(Calendar.DATE, 1);
         iDaysAdded++;

         //this makes sure we don't end on a weekend or holiday
         iDOW =  cal.get(Calendar.DAY_OF_WEEK);
         if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY)
         {   bWeekendOrHoliday = true;
            continue;
         }
         if (h.isHoliday( holFmt.format( cal.getTime() ) ) )
         {   bWeekendOrHoliday = true;
            continue;
         }
         bWeekendOrHoliday = false;
      }


                //per Tana, remove AM/PM indicator DateFormat formatter = new SimpleDateFormat("MM-dd-yyyya");
                DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
      String strResaleDate= formatter.format( cal.getTime() );

      bWeekendOrHoliday = true;
      iDaysAdded = 0;
      while (iDaysAdded < iUNEInterval || bWeekendOrHoliday)
      {
         iDOW =  cal2.get(Calendar.DAY_OF_WEEK);
         if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY)
         {   cal2.add(Calendar.DATE, 1);
            continue;
         }
         if (h.isHoliday( holFmt.format( cal2.getTime() ) ) )
         {   cal2.add(Calendar.DATE, 1);
            continue;
         }
         if (iDaysAdded == iUNEInterval)
            break;
         cal2.add(Calendar.DATE, 1);
         iDaysAdded++;

         //this makes sure we don't end on a weekend or holiday
         iDOW =  cal2.get(Calendar.DAY_OF_WEEK);
         if (iDOW == Calendar.SATURDAY || iDOW == Calendar.SUNDAY)
         {   bWeekendOrHoliday = true;
            continue;
         }
         if (h.isHoliday( holFmt.format( cal2.getTime() ) ) )
         {   bWeekendOrHoliday = true;
            continue;
         }
         bWeekendOrHoliday = false;
      }
      String strUNEDate= formatter.format( cal2.getTime() );

      //Now set return values
      String strUpdate = "UPDATE APPTR_T SET APPTR_RESPC = '027', APPTR_RESPD='Transaction Successful', " +
            "MDFD_DT=SYSDATE, MDFD_USERID = 'auto', APPTR_APPRES='" + strResaleDate + "', APPTR_APPRES_UNE ='" + strUNEDate + "' " +
            " WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion;
      try {
         m_stmt.executeUpdate(strUpdate);
      }
      catch(SQLException se) {
         Log.write(Log.ERROR, "PreorderBean : Updating APPTR_T");
         iReturnCode = -101;
      }

      return iReturnCode;

   } //end doAppointmentSchedule()

        /**
    * Using the user-entered NPANXX, query Frontware database to get a list
    * of PIC, LPIC, and IPICs.
         * 'ASOC_CODE' of PIC in Frontware is INTERLATA PIC and
         * 'ASOC_CODE' of IPIC is INTRALATA PIC -there is no International PICs in
         * Frontware, so we just default these to the same list as the Interlata ones.
    * NOTE: A requirement is that PIC lists must be presented to end user in a random
    * order - so we use a vector here to hold values, then randomize them.
         *
         * @param   int Preorder Sequence Number.
         * @param   int Preorder Version Number.
    * @return  int Return Code (0 = OK, <0 = Error)
    */
        private int doPICLookup(int iPreSqncNmbr, int iVersion)
        {
               int iReturnCode = 0;
      Connection connFW = null;
      Statement stmtFW = null;
      String strNPA = "";
      String strNXX = "";
      String strPIC = "";
      String strPICName = "";
                String strFResp = "";
      int iPICCount = 0;
      int iLPICCount = 0;
      ResultSet rs = null;


      //Get NPA NXX from the request and clear out any old data
      try
      {
         rs = m_stmt.executeQuery("SELECT SUBSTR(FS_NPANXX,1,3), SUBSTR(FS_NPANXX,5,3) FROM FS_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr);
         if (rs.next())
         {
            strNPA = rs.getString(1);
            strNXX = rs.getString(2);
            Log.write(Log.DEBUG_VERBOSE, "PreorderBean : NPANXX = " + strNPA + "-" + strNXX);
            //Now clean up any prior records
            m_stmt.executeUpdate("DELETE FROM FSR_PIC_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
            m_stmt.executeUpdate("DELETE FROM FSR_IPIC_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
            m_stmt.executeUpdate("DELETE FROM FSR_LPIC_T WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion);
         }
         else
         {   Log.write(Log.ERROR, "PreorderBean : doPICLookup() Err getting NPANXX from FS_T for PO=[" + iPreSqncNmbr + "]");
            iReturnCode = -1;
         }
      }
      catch(SQLException se)
      {   Log.write(Log.ERROR, "PreorderBean : SQLException in doPICLookup() se=" + se);
         iReturnCode = -1;
      }
      catch(Exception e)
      {   Log.write(Log.ERROR, "PreorderBean : Exception in doPICLookup() e=" + e);
         iReturnCode = -2;
      }
      if (iReturnCode < 0)
      {   return iReturnCode;
      }

      //FYI on Frontware
      //In Frontware 'IPIC' is definitely for Intralata PIC w/ carrier value chose for customer level trait added.
      //In Frontware 'PIC' is definitely for Interlata PIC w/ carrier value chosen for customer level trait added.

      //Since the FCC rquires PIC selection lists to be in random order, we must first extract list into a vector,
      //then we will randomize and put into our results table
      Vector m_vPICs = new Vector();

      String strQuery1 = "SELECT DISTINCT C.CARRIER_ID, C.CARRIER_SNAME FROM FW.CARRIER C WHERE C.CARRIER_NPA = '" + strNPA +
         "' AND C.CARRIER_NXX = '" + strNXX + "' AND C.ASOC_CODE='PIC'";

      try {
         //Get connection to Frontware
         connFW = DatabaseManager.getConnection(DatabaseManager.FWP_CONNECTION);
         stmtFW = connFW.createStatement();
         PreparedStatement pStmt = m_conn.prepareStatement("INSERT INTO FSR_PIC_T VALUES ("+iPreSqncNmbr+","+iVersion+
                                ",?, ?, ?, ?, sysdate, 'auto')");
         PreparedStatement pStmt2 = m_conn.prepareStatement("INSERT INTO FSR_IPIC_T VALUES ("+iPreSqncNmbr+","+iVersion+
                                ",?, ?, ?, ?, sysdate, 'auto')");
         //Get interlata carriers
         rs = stmtFW.executeQuery(strQuery1);
         while (rs.next() == true)    //Fill Vector with PICs
         {
                                iPICCount++;      //Use for occurence
            PIC aPIC = new PIC( rs.getString(1), rs.getString(2), "A");
            m_vPICs.addElement(aPIC);
                                Log.write(Log.DEBUG_VERBOSE, "PreorderBean : PIC["+ iPICCount+"] = " + aPIC.getPIC() +
                         " Desc=" + aPIC.getPICName());
         } //while()

         //OK now we have vector of PICs. Let's randomly pull from list and put in database
         if (iPICCount > 0)
         {
            int iElement = 0;
            for (int i=0; i < iPICCount; i++)
            {
               //get random element from 0 to size of vector
               iElement =(int)(Math.random() * m_vPICs.size() );
               PIC aPIC = (PIC)m_vPICs.elementAt(iElement);
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : random order[" + i + "] = " + aPIC.getPIC() + " Desc = " + aPIC.getPICName() );
               pStmt.setInt(1, i+1);
               pStmt.setString(2, aPIC.getPIC());
               pStmt.setString(3, aPIC.getPICName());
               pStmt.setString(4, aPIC.getFResp());
               pStmt.executeUpdate();
               pStmt2.setInt(1, i+1);
               pStmt2.setString(2, aPIC.getPIC());
               pStmt2.setString(3, aPIC.getPICName());
               pStmt2.setString(4, aPIC.getFResp());
               pStmt2.executeUpdate();

               //get rid of element we used already - this will reduce size of vector for next iteration
               m_vPICs.removeElementAt(iElement);
            } // for()
         }
         else if (iPICCount == 0) //no matches - just put back an empty occurence 1
         {   pStmt.setInt(1, 1);
            pStmt.setString(2, "");
            pStmt.setString(3, "");
            pStmt.setString(4, "");
            pStmt.executeUpdate();
            pStmt2.setInt(1, 1);
            pStmt2.setString(2, "");
            pStmt2.setString(3, "");
            pStmt2.setString(4, "");
            pStmt2.executeUpdate();
         }
                        pStmt2.close();

         m_vPICs.removeAllElements();

                        //    Now do intralata  PICs
          pStmt = m_conn.prepareStatement("INSERT INTO FSR_LPIC_T VALUES ("+iPreSqncNmbr+","+iVersion+
                                ",?, ?, ?, ?, sysdate, 'auto')");
                        iLPICCount=0;
                        strQuery1 = "SELECT DISTINCT C.CARRIER_ID, C.CARRIER_SNAME FROM FW.CARRIER C WHERE C.CARRIER_NPA = '" + strNPA +
         "' AND C.CARRIER_NXX = '" + strNXX + "' AND C.ASOC_CODE='IPIC'";
         rs = stmtFW.executeQuery(strQuery1);
         while (rs.next() == true)
         {
                                iLPICCount++;      //Use for occurence
            PIC aPIC = new PIC( rs.getString(1), rs.getString(2), "A");
            m_vPICs.addElement(aPIC);
                                Log.write(Log.DEBUG_VERBOSE, "PreorderBean : LPIC["+ iLPICCount+"] = " + aPIC.getPIC() +
                         " Desc=" + aPIC.getPICName());
         } //while()

         //OK now we have vector of LPICs. Let's randomly pull from list and put in database
         if (iLPICCount > 0)
         {
            int iElement = 0;
            for (int i=0; i < iLPICCount; i++)
            {
               //get random element from 0 to size of vector
               iElement =(int)(Math.random() * m_vPICs.size() );
               PIC aPIC = (PIC)m_vPICs.elementAt(iElement);
               Log.write(Log.DEBUG_VERBOSE, "PreorderBean : LPIC random order[" + i + "] = " + aPIC.getPIC() + " Desc = " + aPIC.getPICName() );
               pStmt.setInt(1, i+1);
               pStmt.setString(2, aPIC.getPIC());
               pStmt.setString(3, aPIC.getPICName());
               pStmt.setString(4, aPIC.getFResp());
               pStmt.executeUpdate();

               //get rid of element we used already - this will reduce size of vector for next iteration
               m_vPICs.removeElementAt(iElement);
            } // for()
         }
         else
         if (iLPICCount == 0) //no matches - just put back an empty occurence 1
         {   pStmt.setInt(1, 1);
            pStmt.setString(2, "");
            pStmt.setString(3, "");
            pStmt.setString(4, "");
            pStmt.executeUpdate();
         }
                        pStmt.close();
          rs.close();
         m_vPICs.removeAllElements();

         if (iPICCount > 0 || iLPICCount > 0)
         {
            //Now load FSR_T with results
            String strUpdate = "UPDATE FSR_T SET FSR_RESPC = '027', FSR_RESPD='Transaction Successful', MDFD_DT=sysdate, MDFD_USERID = 'auto' WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion;
            try {
               m_stmt.executeUpdate(strUpdate);
            }
            catch(SQLException se) {
               Log.write(Log.ERROR, "PreorderBean : Updating FSR_T in doPICLookup()");
               iReturnCode = -101;
            }
         }
         else if (iPICCount == 0 && iLPICCount == 0) //nothing found !
         {
            String strUpdate = "UPDATE FSR_T SET FSR_RESPC = '019', FSR_RESPD='NPANXX not Found', MDFD_DT=sysdate, MDFD_USERID = 'auto' WHERE PRE_ORDR_SQNC_NMBR = " + iPreSqncNmbr + " AND PRE_ORDR_VRSN = " + iVersion;
            try {
               m_stmt.executeUpdate(strUpdate);
            }
            catch(SQLException se) {
               Log.write(Log.ERROR, "PreorderBean : Updating FSR_T(2) in doPICLookup()");
               iReturnCode = -102;
            }
         }
      }//end-try
      catch(SQLException se) {
         Log.write(Log.ERROR, "PreorderBean : FW SQLException in doPICLookup() se=" + se);
         iReturnCode = -1;
      }
      catch(Exception e) {
         Log.write(Log.ERROR, "PreorderBean : FW Exception in doPICLookup() e=" + e);
         iReturnCode = -2;
      }
      finally
      {
                   DatabaseManager.releaseConnection(connFW, DatabaseManager.FWP_CONNECTION);
                }

               return iReturnCode;

        }//end of doPICLookup()
        
        
        /**
         * This method used for MSAG to return the low range, high range, record count & street name.
         * @param strSANO
         * @param rs
         * @param streetExists
         * @return sanoRangeMap
         * @throws SQLException
         * 
         */
        private Map<String, String> getMASGSANORange(String strSANO, ResultSet rs, boolean streetExists) throws SQLException {
        	
        	Map<String, String> sanoRangeMap = new HashMap<String, String>();
        	
        	int lowRange = 0;
        	int highRange = 0;
        	int iCount = 0;
        	String strStreetName = "";
        	boolean rangeExists = true;
        	
        	while(rs.next()){
        		
        		String strLowRange = rs.getString(1).trim();
        		String strHighRange = rs.getString(2);
        		
        		if(streetExists){
        			strStreetName = rs.getString(3);
        		}

        		if (strHighRange != null) {
        			strHighRange = strHighRange.trim();

        			if (isInteger(strLowRange) && isInteger(strHighRange) && isInteger(strSANO)) {

        				lowRange = Integer.parseInt(strLowRange);
        				highRange = Integer.parseInt(strHighRange);
        				int intSANO = Integer.parseInt(strSANO);

        				if (lowRange <= intSANO && highRange >= intSANO) {
        					sanoRangeMap.put(LOW_RANGE, strLowRange);
        					sanoRangeMap.put(HIGH_RANGE, strHighRange);
        					sanoRangeMap.put(STREET, strStreetName);
        					iCount++;
        				}
        			} else {
        				lowRange = strLowRange.compareTo(strSANO);
        				highRange = strHighRange.compareTo(strSANO);

        				if (lowRange <= 0 && highRange >= 0) {
        					sanoRangeMap.put(LOW_RANGE, strLowRange);
        					sanoRangeMap.put(HIGH_RANGE, strHighRange);
        					sanoRangeMap.put(STREET, strStreetName);
        					iCount++;
        				}
        			}
        		}
        	}
        	sanoRangeMap.put(COUNT, Integer.toString(iCount));
        	sanoRangeMap.put(RANGE_EXISTS, Boolean.toString(rangeExists));
        	
        	return sanoRangeMap;
        }
        
        /**
         * This method used for check the value is numeric or not.
    	 * @param range
    	 * @return boolean
    	 */
    	public static boolean isInteger(String range) {
    		if (range == null || range.isEmpty()) {
    			return false;
    		}
    		for (int i = 0; i < range.length(); i++) {
    			if (!Character.isDigit(range.charAt(i))) {
    				return false;
    			}
    		}
    		return true;
    	}

}//end of class

/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/Archives/express/JAVA/Object/PreorderBean.java  $

      Rev 1.17   Mar 11 2005 16:46:46   e0069884


      Rev 1.14   Oct 06 2003 13:03:20   e0069884


      Rev 1.12   May 14 2003 16:26:24   e0069884
   HD 351542 and 377912

      Rev 1.11   Mar 31 2003 15:14:54   e0069884


      Rev 1.10   Jan 14 2003 12:28:38   e0069884


      Rev 1.9   Dec 11 2002 15:11:52   e0069884
   HDR 200039 and change response if data warehouse unavailable


      Rev 1.8   Nov 05 2002 17:28:10   e0069884





      Rev 1.7   Oct 24 2002 10:25:14   e0069884




      Rev 1.5   Aug 27 2002 10:31:24   sedlak
/*
/*   Rev 1.0
/*Initial Checkin
*/
/* $Revision:   1.17  $
*/