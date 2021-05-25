/*
 * NOTICE:
 *      THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *      SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *      USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *            COPYRIGHT (C) 2005
 *               BY
 *            ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:   DwoBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 * pjs 4-19-2005 Logical Change Orders
 * EK 8/2005 BDP Logical Change Orders
  * EK 3/2006 BDP, Add to the Email Notification the Circuit ID and Port Assignments Fields
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class DwoBean extends ExpressBean
{

   //These dictate what to pull from tables
    private DwoOrder thisOrder = null;   
   public DwoBean(String strTypInd) {
      thisOrder = DwoOrder.getInstance(strTypInd);
      super.init(thisOrder);
                Log.write(Log.DEBUG_VERBOSE, "DwoBean: constructor");
   }

        public int create(int iUNUSED) {
                return -1;
        }
   
   public int create(String strOCNCd, int iCmpnySqncNmbr, String strPrdctTypCd, String strSrvcTypCd, String strChgTypCd, String strChgSubTypCd, String strSearchSeqNum )
   {
      Log.write(Log.DEBUG_VERBOSE, "DwoBean : Create New Dwo");   
      setDwoPrdProd( strPrdctTypCd );
      int iReturnCode = 0;
      String strNewSrvcTypCd = "";
      // Get the OCN Name for this order
      String strOcnNm = "";
   
      ResultSet rs1=null;

      if (strOCNCd.equals("New"))
      {
         strOcnNm = "New";
      }
      else
      {
         String strQueryOcn = "SELECT OCN_NM FROM OCN_T WHERE OCN_CD = '" + strOCNCd +
            "' AND CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr;

         try 
         {
            rs1 = m_stmt.executeQuery(strQueryOcn);
         
            if (rs1.next())
               strOcnNm = rs1.getString("OCN_NM");

            rs1.close();
         }
         catch(SQLException e)
         {
            e.printStackTrace();
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "DwoBean : DB Exception on Query : " + strQueryOcn);
            iReturnCode = -100;      
         }
      }

      strNewSrvcTypCd = strSrvcTypCd;
      // For logical change orders, can be activty and sub-activties that drive form creation. Use
      // xref table to get a new unique service type code for this combination of:
      // Product, Srvc Typ, Activty Typ, and Sub Activity Type

      //But only do if Activty and SubActivity are selected...
      if ( (strChgTypCd != null) && (strChgTypCd.length() > 0) )
      {
         String strNewSrvcQuery = "SELECT NEW_SRVC_TYP_CD FROM SRVC_ACTVTY_SUB_T WHERE TYP_IND = '" + thisOrder.getTYP_IND() + "' " +
            " AND PRDCT_TYP_CD='" + strPrdctTypCd + "' AND SRVC_TYP_CD='" + strSrvcTypCd + "' AND ACTVTY_TYP_CD = '" + strChgTypCd + "' ";
         if ( (strChgSubTypCd != null) && (strChgSubTypCd.length() > 0) )
         {   strNewSrvcQuery += " AND SUB_ACTVTY_TYP_CD = '" + strChgSubTypCd + "' ";
         }
         try 
         {
            Log.write(Log.DEBUG_VERBOSE, "DwoBean NewSrvTyp query["+ strNewSrvcQuery + "]");
            rs1 = m_stmt.executeQuery(strNewSrvcQuery);
            if (rs1.next())
            {   strNewSrvcTypCd = rs1.getString(1);
            }
            rs1.close();
         }
         catch(SQLException e)
         {
            e.printStackTrace();
            rollbackTransaction();
            DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "DwoBean : DB Exception on Query : " + strNewSrvcQuery);
            iReturnCode = -100;      
         }
      }
      Log.write(Log.DEBUG_VERBOSE, "DwoBean NewSrvTyp =["+ strNewSrvcTypCd + "]");

      // Get the new Dwo Number
      int iDwoSqncNmbr = 0;
      String strQueryDSN = "SELECT DWO_SEQ.nextval DWO_SQNC_NMBR_NEW FROM dual";

      try 
      {
         rs1 = m_stmt.executeQuery(strQueryDSN);
         
         rs1.next();
         iDwoSqncNmbr = rs1.getInt("DWO_SQNC_NMBR_NEW");
         rs1.close();
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "DwoBean : DB Exception on Query : " + strQueryDSN);
         iReturnCode = -100;      
      }

      if (iReturnCode != 0)
      { 
         return (iReturnCode);
      }

      // Insert new row into DWO_T
      String strInsert1 = "";
      try 
      {
         strInsert1 = "INSERT INTO DWO_T (DWO_SQNC_NMBR, DWO_VRSN, DWO_STTS_CD, DWO_HSTRY_SQNC_NMBR, " +
            "OCN_CD, CMPNY_SQNC_NMBR, SRVC_TYP_CD, LST_MDFD_PRVDR, " +
            "LST_MDFD_CSTMR, DUE_DATE, MDFD_DT, MDFD_USERID, OCN_NM, PRDCT_TYP_CD, ORIG_SRVC_TYP_CD, ACTVTY_TYP_CD, "+ 
            "SUB_ACTVTY_TYP_CD, STT_CD, LCTN_NM, BSNSS_NM) " + " VALUES(" + iDwoSqncNmbr + ",0, 'INITIAL', 0, '" +  strOCNCd + "', " +
             iCmpnySqncNmbr + ",'" + strNewSrvcTypCd + "', ' ', '" + getUserid() + "', ''," +
             getTimeStamp() + ",'" + getUserid() + "','" + strOcnNm + "','" + strPrdctTypCd + "','" + strSrvcTypCd + "','" + strChgTypCd + "', '" + strChgSubTypCd + "','','','' )";
Log.write(Log.DEBUG_VERBOSE, "DwoBean : insert[" + strInsert1 + "]");
         m_stmt.executeUpdate(strInsert1);
      } 
      catch(Exception e) 
      { 
         e.printStackTrace(); 
         rollbackTransaction(); 
         DatabaseManager.releaseConnection(m_conn); 
         Log.write(Log.ERROR, "DwoBean : DB Exception on Insert : " + strInsert1);
         iReturnCode = -100;      
      }

      if (iReturnCode != 0)
      { 
         return (iReturnCode);
      }

      Log.write(Log.DEBUG_VERBOSE, "DwoBean : Successful Insert of New Dwo");

      // generate a new History record 
      int iDwoHstrySqncNmbr = updateHistory(iDwoSqncNmbr, 0, "INITIAL");
      if (iDwoHstrySqncNmbr <= 0)
      {
         Log.write(Log.ERROR, "DwoBean : Error Generating History for Dwo Sqnc Nmbr:" + iDwoSqncNmbr);
         return(-125);      
      }

      String strUpdate1 = "UPDATE DWO_T SET DWO_HSTRY_SQNC_NMBR = " + iDwoHstrySqncNmbr + " WHERE DWO_SQNC_NMBR = " + iDwoSqncNmbr;

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
         Log.write(Log.ERROR, "DwoBean : DB Exception on Update : " + strUpdate1);
         iReturnCode = -100;      
      }

      if (iReturnCode != 0)
      { 
         return (iReturnCode);
      }

      Log.write(Log.DEBUG_VERBOSE, "DwoBean : DWO_T updated with current History Sequence Number : " + strUpdate1);

      // if we got here, we have a new Dwo Sequence Number
      // now get the information we need to create all the required forms.
      // We need to loop through SERVICE_TYPE_FORM and create all the INITIAL FORMs 

      String strQuery3 = "SELECT FRM_SQNC_NMBR FROM SERVICE_TYPE_FORM_T " +
         " WHERE TYP_IND = '" + thisOrder.getTYP_IND() + "' AND SRVC_TYP_CD = '" + strNewSrvcTypCd + "' ";

Log.write(Log.DEBUG_VERBOSE, "DwoBean : form selection criteria=["+ strQuery3 +"]");

      int i_frms = 0;
      int i_frms_created = 0;
      int iFrmSqncNmbr = 0;
      boolean bFormCreated = false;

      try 
      {
         rs1 = m_stmt.executeQuery(strQuery3);

         while (rs1.next())
         {
            i_frms++;

            iFrmSqncNmbr = rs1.getInt("FRM_SQNC_NMBR");

            bFormCreated = getFormBean().generateNewForm(iFrmSqncNmbr, iDwoSqncNmbr, 0);

            if (bFormCreated)
            {
               i_frms_created++;
            }
            else
            {
               Log.write(Log.ERROR, "DwoBean : Error Generating Form for Dwo Sqnc Nmbr:" + iDwoSqncNmbr + " ; Form Sqnc Nmbr = " + iFrmSqncNmbr);
               iReturnCode = -130;      
            }

         }
         if ((i_frms_created == 0) || (i_frms_created != i_frms))
         {
            Log.write(Log.ERROR, "DwoBean : Error Generating Forms for Dwo Sqnc Nmbr:" + iDwoSqncNmbr);
            iReturnCode = -135;      
         }

         rs1.close();
      } 
      catch(SQLException e) 
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "DwoBean :  ERROR PERFORMING DATABASE ACTIVITY FOR NEW DWO FORM CREATION ");
         iReturnCode = -100;      
      }

      // if user selected to create order from search results from SIS.
      // Here we overwrite user's default falues.
            
      if( strSearchSeqNum != null && strSearchSeqNum.length() > 0  ){
         try{
            CustomerContactInfoBean csInfoBean = new CustomerContactInfoBean( "" );
            int iSearchSeqNum = Integer.parseInt( strSearchSeqNum );
            csInfoBean.setSeqNumber( iSearchSeqNum );
            csInfoBean.dbLoad( m_conn );
            csInfoBean.dbUpdateExpress( m_conn, iDwoSqncNmbr);   
         }catch(SQLException e) 
         {
            e.printStackTrace();
            rollbackTransaction();
            Log.write(Log.ERROR, "DwoBean SQLException : " + e.toString() );
            iReturnCode = -100;      
         }
         catch( Exception sqle) 
         {
            sqle.printStackTrace();
            rollbackTransaction();
            Log.write(Log.ERROR, "DwoBean Exception  : " + sqle.toString() );
            iReturnCode = -100;      
         }
      }
      
      if (iReturnCode != 0)
      { 
         return (iReturnCode);
      }
      
      Log.write(Log.DEBUG_VERBOSE, "DwoBean : All INITIAL Forms Generated for Dwo Sqnc Nmbr:" + iDwoSqncNmbr); 

      // return the new Dwo Sequence Number
      return(iDwoSqncNmbr); 

   }
   
    // Send the autoReply if necessary
    protected void sendReply(int iSqncNmbr, int iVrsn, String strUserID)
    {
      String strQuery1 = "SELECT DUE_DATE, DWO_STTS_CD, D.OCN_NM, S.SRVC_TYP_DSCRPTN, " +
         " TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI') AS THE_TIME, D.LCTN_NM,  D.BSNSS_NM, P.PRDCT_DSCRPTN,  " +
         " ced.PRT_ASSGNMNTS, ced.CRCT_IDS FROM DWO_T D, DWO_CED_T ced, SERVICE_TYPE_T S, PRODUCT_T P " +
         " WHERE " +
         " D.DWO_SQNC_NMBR = ced.DWO_SQNC_NMBR(+) " +
         " AND P.PRDCT_TYP_CD = D.PRDCT_TYP_CD " +
         " AND D.SRVC_TYP_CD = S.SRVC_TYP_CD " +
         " AND D.DWO_SQNC_NMBR= " + iSqncNmbr +
         " AND D.DWO_VRSN =" + iVrsn + 
         " AND S.TYP_IND = '" + thisOrder.getTYP_IND() + "' " +
         " AND P.TYP_IND = '" + thisOrder.getTYP_IND() + "' " ;
      ResultSet rs = null;
      String strSubject = "";
      String strMsg = "";
      String strInitId = strUserID;   
      try
      {
         rs = m_stmt.executeQuery(strQuery1);
         if (rs.next())
         {
            // Build the Subject
            strSubject = "Express Customer Order Status Change for:  " + iSqncNmbr;
            // Build the Message
            if (getTypInd().equals("X"))  // BDP
            {
               strMsg =          "Order Number       :  " + iSqncNmbr + "\n";
               strMsg = strMsg + "Due Date           :  " + rs.getString("DUE_DATE") + "\n";
               strMsg = strMsg + "New Status         :  " + rs.getString("DWO_STTS_CD") + "\n";
               strMsg = strMsg + "Location Name      :  " + rs.getString("LCTN_NM") + "\n";
               strMsg = strMsg + "Main Business Name :  " + rs.getString("BSNSS_NM") + "\n";
               strMsg = strMsg + "Service Type       :  " + rs.getString("SRVC_TYP_DSCRPTN") + "\n";
               strMsg = strMsg + "Product Type       :  " + rs.getString("PRDCT_DSCRPTN") + "\n";
               strMsg = strMsg + "Circuit IDs        :  " + ExpressUtil.fixNullStr( rs.getString("CRCT_IDS") ) + "\n";
               strMsg = strMsg + "Port Assignments   :  " + ExpressUtil.fixNullStr( rs.getString("PRT_ASSGNMNTS" ) ) + "\n";
               strMsg = strMsg + "Date/Time          :  " + rs.getString("THE_TIME") + "\n\n";
               strInitId = getInitiatorUserId( iSqncNmbr );
            }
            else
            {
               strMsg =          "Order Number  :  " + iSqncNmbr + "\n";
               strMsg = strMsg + "Due Date      :  " + rs.getString("DUE_DATE") + "\n";
               strMsg = strMsg + "New Status    :  " + rs.getString("DWO_STTS_CD") + "\n";
               strMsg = strMsg + "Site          :  " + rs.getString("OCN_NM") + "\n";
               strMsg = strMsg + "Service Type  :  " + rs.getString("SRVC_TYP_DSCRPTN") + "\n";
               strMsg = strMsg + "Date/Time     :  " + rs.getString("THE_TIME") + "\n\n";
            }
               rs.close();
           }
           else
           {
            //DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "DwoBean.sendReply(): Error getting email info");
            return;
           }
      }
        catch(SQLException e)
        {
            e.printStackTrace();
            //rollbackTransaction();
            try { rs.close(); } catch(Exception e2) {}
            //DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "DwoBean.sendReply(): DB Exception on Query : " + strQuery1);
            return;
        }

      Log.write(Log.DEBUG_VERBOSE,"DwoBean.sendReply(): email to " + strUserID + " about to be sent for " + iSqncNmbr);
      if (Toolkit.autoReply(strInitId, strSubject, strMsg) != true)
         Log.write(Log.ERROR, "DwoBean.sendReply(): AutoReply failed.");
      return;
   }
 
    // Send the provider autoReply if necessary
    protected void sendProvReply(int iSqncNmbr, int iVrsn)
    {
      String strQuery1 = "SELECT DWO_STTS_CD, D.OCN_NM, S.SRVC_TYP_DSCRPTN, " +
         " TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI') AS THE_TIME, D.LCTN_NM, D.BSNSS_NM, P.PRDCT_DSCRPTN,  " +
         " ced.PRT_ASSGNMNTS, ced.CRCT_IDS FROM DWO_T D, DWO_CED_T ced, SERVICE_TYPE_T S, PRODUCT_T P " +
         " WHERE " +
         " D.DWO_SQNC_NMBR = ced.DWO_SQNC_NMBR(+) " +
         " AND P.PRDCT_TYP_CD = D.PRDCT_TYP_CD " +
         " AND D.SRVC_TYP_CD = S.SRVC_TYP_CD " +
         " AND D.DWO_SQNC_NMBR= " + iSqncNmbr +
         " AND D.DWO_VRSN =" + iVrsn + 
         " AND S.TYP_IND = '" + thisOrder.getTYP_IND() + "'" +
         " AND P.TYP_IND = '" + thisOrder.getTYP_IND() + "'";
      ResultSet rs = null;
      String strSubject = "";
      String strMsg = "";
      String strEmail = "";
        try
        {
           rs = m_stmt.executeQuery(strQuery1);
           if (rs.next())
           {
         // Build the Subject
            strSubject = "Express Customer Order Status Change for:  " + iSqncNmbr;
   
            // Build the Message
            if (getTypInd().equals("X"))  // BDP
            {
               strMsg =          "Order Number       :  " + iSqncNmbr + "\n";
               strMsg = strMsg + "New Status         :  " + rs.getString("DWO_STTS_CD") + "\n";
               strMsg = strMsg + "Location Name      :  " + rs.getString("LCTN_NM") + "\n";
               strMsg = strMsg + "Main Business Name :  " + rs.getString("BSNSS_NM") + "\n";
               strMsg = strMsg + "Service Type       :  " + rs.getString("SRVC_TYP_DSCRPTN") + "\n";
               strMsg = strMsg + "Product Type       :  " + rs.getString("PRDCT_DSCRPTN") + "\n";
               strMsg = strMsg + "Circuit IDs        :  " + ExpressUtil.fixNullStr( rs.getString("CRCT_IDS" ) ) + "\n";
               strMsg = strMsg + "Port Assignments   :  " + ExpressUtil.fixNullStr( rs.getString("PRT_ASSGNMNTS" )) + "\n";               
               strMsg = strMsg + "Date/Time          :  " + rs.getString("THE_TIME") + "\n\n";
               
            }
            else
            {
               strMsg =          "Order Number  :  " + iSqncNmbr + "\n";
               strMsg = strMsg + "New Status    :  " + rs.getString("DWO_STTS_CD") + "\n";
               strMsg = strMsg + "Site          :  " + rs.getString("OCN_NM") + "\n";
               strMsg = strMsg + "Service Type  :  " + rs.getString("SRVC_TYP_DSCRPTN") + "\n";
               strMsg = strMsg + "Date/Time     :  " + rs.getString("THE_TIME") + "\n\n";
            }
               rs.close();
               } 
               else {
                //DatabaseManager.releaseConnection(m_conn);
                Log.write(Log.ERROR, "DwoBean.sendProvReply(): Error getting email info");
               return;
            }
      }
        catch(SQLException e)
        {
            e.printStackTrace();
            try { rs.close(); } catch(Exception e2) {}
            Log.write(Log.ERROR, "DwoBean.sendProvReply(): DB Exception on Query : " + strQuery1);
            return;
        }
      try
      {
         // Get Email address from properties file
         strEmail = PropertiesManager.getProperty("lsr.dwo.ProviderEmailAddress","");
      }
      catch(Exception e)
      {
            e.printStackTrace();
            Log.write(Log.ERROR, "DwoBean.sendProvReply(): Error retrieving e-mail address from lsr.properties");
      }

      Log.write(Log.DEBUG_VERBOSE,"DwoBean.sendProvReply(): email to " + strEmail + " about to be sent for " + iSqncNmbr);
      if (Toolkit.autoProvReply(strEmail, strSubject, strMsg) != true)
         Log.write(Log.ERROR, "DwoBean.sendProvReply(): AutoReply failed.");

      return;
   }
 
        // Send the provider autoReply if necessary
    protected void sendProvReply(int iSqncNmbr, int iVrsn, String strEmailDistList)
    {
      String strQuery1 = "SELECT D.DWO_STTS_CD, D.OCN_NM, S.SRVC_TYP_DSCRPTN, " +
         " TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI') AS THE_TIME, D.LCTN_NM, D.BSNSS_NM, P.PRDCT_DSCRPTN, " +
         " ced.PRT_ASSGNMNTS, ced.CRCT_IDS FROM DWO_T D, DWO_CED_T ced, SERVICE_TYPE_T S, PRODUCT_T P " +         
         " WHERE " +
         " D.DWO_SQNC_NMBR = ced.DWO_SQNC_NMBR(+) " +
         " AND P.PRDCT_TYP_CD = D.PRDCT_TYP_CD " +
         " AND D.SRVC_TYP_CD = S.SRVC_TYP_CD " +
         " AND D.DWO_SQNC_NMBR = " + iSqncNmbr +
         " AND D.DWO_VRSN = " + iVrsn + 
         " AND S.TYP_IND = '" + thisOrder.getTYP_IND() + "'" +
         " AND P.TYP_IND = '" + thisOrder.getTYP_IND()  + "'" ;
         
      ResultSet rs = null;
      String strSubject = "";
      String strMsg = "";
      String strEmail = "";
      try
      {
          rs = m_stmt.executeQuery(strQuery1);
          if (rs.next())
          {
            // Build the Subject
            strSubject = "Express Customer Order Status Change for:  " + iSqncNmbr;
            
            // Build the Message
            if (getTypInd().equals("X"))  // BDP
               {
               strMsg =          "Order Number       :  " + iSqncNmbr + "\n";
               strMsg = strMsg + "New Status         :  " + rs.getString("DWO_STTS_CD") + "\n";
               strMsg = strMsg + "Location Name      :  " + rs.getString("LCTN_NM") + "\n";
               strMsg = strMsg + "Main Business Name :  " + rs.getString("BSNSS_NM") + "\n";
               strMsg = strMsg + "Service Type       :  " + rs.getString("SRVC_TYP_DSCRPTN") + "\n";
               strMsg = strMsg + "Product Type       :  " + rs.getString("PRDCT_DSCRPTN") + "\n";
               strMsg = strMsg + "Circuit IDs        :  " + ExpressUtil.fixNullStr( rs.getString("CRCT_IDS") ) + "\n";
               strMsg = strMsg + "Port Assignments   :  " + ExpressUtil.fixNullStr( rs.getString("PRT_ASSGNMNTS" )) + "\n";               
               strMsg = strMsg + "Date/Time          :  " + rs.getString("THE_TIME") + "\n\n";
            }
            else
            {
               strMsg =          "Order Number  :  " + iSqncNmbr + "\n";
               strMsg = strMsg + "New Status    :  " + rs.getString("DWO_STTS_CD") + "\n";
               strMsg = strMsg + "Site          :  " + rs.getString("OCN_NM") + "\n";
               strMsg = strMsg + "Service Type  :  " + rs.getString("SRVC_TYP_DSCRPTN") + "\n";
               strMsg = strMsg + "Date/Time     :  " + rs.getString("THE_TIME") + "\n\n";
            }
            rs.close();         
         }
             else
         {
            //DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "DwoBean.sendProvReply(): Error getting email info");
            return;
         }
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         try { rs.close(); } catch(Exception e2) {}
         Log.write(Log.ERROR, "DwoBean.sendProvReply(): DB Exception on Query : " + strQuery1);
         return;
      }

      try
      {
         rs = m_stmt.executeQuery("SELECT EML_DST_LST, EML_SBJCT_TXT FROM EMAIL_DIST_T WHERE EML_DST_NM='" + 
            strEmailDistList + "' ");
         if( rs.next() ){
            strEmail = rs.getString("EML_DST_LST");
            String strTemp = rs.getString("EML_SBJCT_TXT");
            if ( (strTemp != null) && (strTemp.length() > 5) )
            {   
               strSubject = strTemp;
            }
         }
         rs.close();
      }
      catch(Exception e)
      {
           e.printStackTrace();
           Log.write(Log.ERROR, "DwoBean.sendProvReply() 2: Error retrieving e-mail dist list");
      }

      Log.write(Log.DEBUG_VERBOSE,"DwoBean.sendProvReply(): email to " + strEmail + " about to be sent for " + iSqncNmbr);
      if (Toolkit.autoProvReply(strEmail, strSubject, strMsg) != true)
         Log.write(Log.ERROR, "DwoBean.sendProvReply(): AutoReply failed.");

      return;
   }
   
   /*EK:
    * Call super changeStatus to do the nomal status update. Then instantiate
    * ExpressSisDataExchange to generate and push xml to SIS. Lastly update Express table
    * with SIS response or out come of attempting to push. Note, this includes 
    * communication failure should SIS be down.
    * @see: ExpressSisDataExchange.java, ExpressBean.java, and ExpressSISBean.java
    */
    
   public int changeStatus(AlltelRequest request, int m_iDwoSqncNmbr, String m_strDwoAction )
   {                                       
                                           
      int iReturnCode = 0;
      // Call base class method first....      
      iReturnCode = super.changeStatus( request, m_iDwoSqncNmbr, m_strDwoAction);
      PreparedStatement pstmt = null;
      ResultSet rs = null;   
      String strSTTCD = "";
      String strQuery = " select DWO_STTS_CD from DWO_T where DWO_SQNC_NMBR = ? ";
      String strInsert = "insert into DWO_SIS_RESPONSE_T( DWO_SQNC_NMBR, RESP_TXT, MDFD_USERID) "
               + " VALUES( ?,?,? ) ";
      try{
         pstmt = m_conn.prepareStatement( strQuery );
         pstmt.clearParameters();
         pstmt.setInt( 1, m_iDwoSqncNmbr);      
         rs = pstmt.executeQuery();    
         if ( rs.next() )      
         {
            strSTTCD = rs.getString( 1 );
         }         
         if ( rs != null ){ rs.close(); }
         if ( pstmt != null ){ pstmt.close(); }         
         strSTTCD = strSTTCD==null? "": strSTTCD.trim();

         // HD2457829
         if( strSTTCD.equalsIgnoreCase( "DSTAC ACCEPTED" )
             ||  strSTTCD.equalsIgnoreCase( "COMPLETED" ) ) {      
            
            ExpressSisDataExchange xmlDataPoint = new ExpressSisDataExchange( "" );
            xmlDataPoint.setDwoSqncNmbr( m_iDwoSqncNmbr );
            xmlDataPoint.setTypeInd( thisOrder.getTYP_IND()  );
            xmlDataPoint.setDwoBean( this );
            //Log.write(Log.ERROR, xmlDataPoint.createXml( getNewVersion() ) );
            String xml =  xmlDataPoint.createXml( getNewVersion() ); 

            //Log.write(Log.DEBUG_VERBOSE, xml );
            xml = xmlDataPoint.sendXml(xml,ExpressUtil.getSisUrlProd());
            // process return string ....
            //.... update the db          
            pstmt = m_conn.prepareStatement( strInsert );
            pstmt.clearParameters();
            pstmt.setInt( 1, m_iDwoSqncNmbr);      
            pstmt.setString( 2,   xml);
            pstmt.setString( 3,getUserid() );
            pstmt.executeUpdate();

         }
      } catch(SQLException e){
         e.printStackTrace();   
         Log.write(Log.ERROR,  e.toString() );
      } finally {   // Clean up
         try{
            if ( pstmt != null ){ pstmt.close(); }
         }catch (Exception e){
            // connection handled  by DwoBean's caller
            e.printStackTrace();
            Log.write(Log.ERROR, e.toString() );
         }
      }         
      return    iReturnCode;          
                    
   }  
   
   /* EK-11/2/2005
    * FOR HD0000001695030:                                    
   * This function handles REQUEST:  change the email notification on the 
   * Reject status to go back to the Order Initiator
   * NOTE, Connection already open and handled by super class. See ExpressBean.java.
   */
   public String getInitiatorUserId( int m_iDwoSqncNmbr ) throws SQLException {
      
      String strInitUserId = "";
      PreparedStatement pstmt = null;
      ResultSet rset = null;
      String strQry = " SELECT MDFD_USERID, max(MDFD_DT) "
            + " as lastdate from dwo_history_t "
            + " where DWO_SQNC_NMBR = ? "
            + " AND DWO_STTS_CD_IN = ? and " 
            + " DWO_STTS_CD_OUT = ? "
            + " group by MDFD_USERID order by 2 desc "   ;
      pstmt = m_conn.prepareStatement( strQry );
      pstmt.clearParameters();
      pstmt.setInt( 1, m_iDwoSqncNmbr );   
      pstmt.setString( 2, "INITIAL" );   
      pstmt.setString( 3, "SUBMITTED");      
      rset= pstmt.executeQuery();
       if (rset.next()){
          strInitUserId = rset.getString(1);
       }
       if(rset != null){ rset.close(); rset = null;}
      if(pstmt != null){ pstmt.close(); pstmt = null;}      
       return strInitUserId;
   }
}   
    
    
    
    
    
    
    
    
    
   
   
   
   
   
   
   
   
