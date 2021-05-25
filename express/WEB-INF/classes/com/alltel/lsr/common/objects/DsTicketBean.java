/*
 * NOTICE:
 *      THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *      SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *      USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *            COPYRIGHT (C) 2004
 *               BY
 *            ALLTEL COMMUNICATIONS INC.
 */

/*
 * MODULE:   TicketBean.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        03-20-2004
 *
 * HISTORY:
 *   03/20/2004  initial
 *   07/08/2004 pjs add OrigOwnerGroup to XML
 *   07/19/2004 pjs Get External ticket id (Siebel), also include in email
 *   10/22/2004 pjs Prefix 'Creation Comments' with the contact info
 *   06/6/2006 EK, Remove xml interactions to TMS and add Remedy webservices.
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;
import Remedy.*;
import urn.HPD_DNOC_Incident_Create.*;

public class DsTicketBean extends ExpressBean
{
   private static final String TMS_OK = "Insert Completed";
   private static final String TMS_OK2 = " Trouble Ticket";
   private static final String TMS_OK3 = " created";
   private static final String TMS_DUP = "failed because a matching record";
   private static final String TMS_ERROR = "Error";

   private String m_strExternalTicketId;

   //These dictate what to pull from tables
        private DsTicketOrder thisOrder = DsTicketOrder.getInstance();

   public DsTicketBean() {

      super.init(thisOrder);
      Log.write(Log.DEBUG_VERBOSE, "DsTicketBean: constructor");
      this.m_iNextForm = 0;
      this.m_strExternalTicketId = "";
   }

   private static final String AUTOMATIC_RESPONSE = "Automatic";
   private int m_iNextForm;   //If auto process , this is the next form to show

   public int getNextForm() {
      return this.m_iNextForm;
   }

   public int create(int iUNUSED) {
      return -1;
   }

   public int create(String strOCNCd, int iCmpnySqncNmbr)
   {
      Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : Create New Ticket");

      int iReturnCode = 0;

      String strSttCd = "";
      String strOcnNm = "";
      int iOCNSttSqncNmbr = 0;

      strSttCd = "KY";   //dont think we need this....

      String strQuery1 = "SELECT DISTINCT O.OCN_NM FROM OCN_T O WHERE O.OCN_CD = '" + strOCNCd + "' AND O.CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr;
      try
      {
         ResultSet rs1 = m_stmt.executeQuery(strQuery1);

         if (rs1.next())
         {
            strOcnNm = rs1.getString("OCN_NM");
            rs1.close();
         }
         else
         {
            rollbackTransaction();
            //DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : Error finding valid OCN Name ");
            iReturnCode = -110;
         }
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         //DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : DB Exception on Query : " + strQuery1);
         iReturnCode = -100;
      }
      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      // Get the new Ticket Number
      int iTcktSqncNmbr = 0;
      String strQueryTSN = "SELECT DSTICKET_SEQ.nextval TCKT_SQNC_NMBR_NEW FROM dual";

      try
      {
         ResultSet rsTSN = m_stmt.executeQuery(strQueryTSN);

         rsTSN.next();
         iTcktSqncNmbr = rsTSN.getInt("TCKT_SQNC_NMBR_NEW");
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.ERROR, "DsTicketBean : DB Exception on Query : " + strQueryTSN);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      // Insert new row into DSTICKET_T
      String strInsert1 = "";
      try
      {
         strInsert1 = "INSERT INTO DSTICKET_T " +
            " (TCKT_SQNC_NMBR, VRSN, STTS_CD, HSTRY_SQNC_NMBR, OCN_CD, OCN_STT, OCN_STT_SQNC_NMBR, CMPNY_SQNC_NMBR, "+
            "  SRVC_TYP_CD, TELNO, TCKT_ID, MDFD_DT, MDFD_USERID, LST_MDFD_PRVDR, LST_MDFD_CSTMR, OCN_NM) " +
            " VALUES(" + iTcktSqncNmbr + ",0, 'INITIAL', 0, '" + strOCNCd + "', '" + strSttCd + "', " + iOCNSttSqncNmbr +
            ", " + iCmpnySqncNmbr + ",'T', '', ''," + getTimeStamp() + ", '" + getUserid() + "', " +
            " ' ','" + getUserid() + "','" + strOcnNm + "')" ;
         Log.write("DsTicketBean.create() INSERT=["+ strInsert1 +"]");
         m_stmt.executeUpdate(strInsert1);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : DB Exception on Insert : " + strInsert1);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : Successful Insert of New Ticket");

      // generate a new History record
      int iTcktHstrySqncNmbr = updateHistory(iTcktSqncNmbr, 0, "INITIAL");
      if (iTcktHstrySqncNmbr <= 0)
      {
         Log.write(Log.ERROR, "DsTicketBean : Error Generating History for Ticket Sqnc Nmbr:" + iTcktSqncNmbr);
         return(-125);
      }

      String strUpdate1 = "UPDATE DSTICKET_T SET HSTRY_SQNC_NMBR = " + iTcktHstrySqncNmbr + " WHERE TCKT_SQNC_NMBR = " + iTcktSqncNmbr;

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
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : DB Exception on Update : " + strUpdate1);
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : DSTICKET_T updated with current History Sequence Number : " + strUpdate1);


      // if we got here, we have a new Ticket Sequence Number
      // now get the information we need to create all the required forms.
      // We need to loop through SERVICE_TYPE_FORM and create all the INITIAL FORMs

      String strQuery3 = "SELECT * FROM SERVICE_TYPE_FORM_T WHERE SRVC_TYP_CD = 'T' AND TYP_IND = 'S'";
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

            bFormCreated = getFormBean().generateNewForm(iFrmSqncNmbr, iTcktSqncNmbr, 0);

            if (bFormCreated)
            {
               i_frms_created++;
            }
            else
            {
               Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : Error Generating Form for Ticket Sqnc Nmbr:" + iTcktSqncNmbr + " ; Form Sqnc Nmbr = " + iFrmSqncNmbr);
               iReturnCode = -130;
            }
         }
         if ((i_frms_created == 0) || (i_frms_created != i_frms))
         {
            Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : Error Generating Forms for Ticket Sqnc Nmbr:" + iTcktSqncNmbr);
            iReturnCode = -135;
         }

         rs3.close();
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         rollbackTransaction();
         DatabaseManager.releaseConnection(m_conn);
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean :  ERROR PERFORMING DATABASE ACTIVITY FOR NEW TICKET FORM CREATION ");
         iReturnCode = -100;
      }

      if (iReturnCode != 0)
      {
         return (iReturnCode);
      }

      Log.write(Log.DEBUG_VERBOSE, "DsTicketBean : All INITIAL Forms Generated for Ticket Sqnc Nmbr:" + iTcktSqncNmbr);

      // return the new Ticket Sequence Number
      return(iTcktSqncNmbr);

   }

   // Send the autoReply if necessary
   protected void sendReply(int iSqncNmbr, int iVrsn, String strUserID)
   {   // gao, RIS #1414, added the OCN_CD for the following query

      String strQuery1 = "SELECT D.STTS_CD, D.OCN_NM, D.OCN_CD, S.SRVC_TYP_DSCRPTN, NVL(TCKT_ID,'Not assigned Yet') AS TID, " +
                        " TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI') AS THE_TIME " +
                        " FROM DSTICKET_T D, SERVICE_TYPE_T S WHERE D.TCKT_SQNC_NMBR=" + iSqncNmbr +
                        " AND D.VRSN =" + iVrsn + " AND D.SRVC_TYP_CD = S.SRVC_TYP_CD AND " +
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
            strSubject = "Trouble Ticket Status Change for Express #:  " + iSqncNmbr;

            // Build the Message
            strMsg =  "Express Number:  " + iSqncNmbr + "\n";
            strMsg = strMsg + "New Status    :  " + rs.getString("STTS_CD") + "\n";
            strMsg = strMsg + "Community     :  " + rs.getString("OCN_NM") + "\n";
            strMsg = strMsg + "Site ID       :  " + rs.getString("OCN_CD") + "\n"; //gao, RIS #1414
            if (rs.getString("TID").equals("Not assigned Yet") && this.getExternalTicketId().length() > 1)
               strMsg = strMsg + "Trouble Ticket:  " + this.getExternalTicketId() + "\n";
            else
               strMsg = strMsg + "Trouble Ticket:  " + rs.getString("TID") + "\n";
            strMsg = strMsg + "Date/Time     :  " + rs.getString("THE_TIME") + "\n\n";
            rs.close();
         }
         else
         {
            //DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.DEBUG_VERBOSE, "DsTicketBean.sendReply(): Error getting email info ["+ iSqncNmbr + ":"+ iVrsn + "]");
            return;
         }
      }
      catch(SQLException e)
      {
         e.printStackTrace();
         try { rs.close(); } catch(Exception e2) {}
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean.sendReply(): DB Exception on Query : " + strQuery1);
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean.sendReply(): ["+ iSqncNmbr + ":"+ iVrsn + "]");
         return;
      }

      Log.write(Log.DEBUG_VERBOSE,"DsTicketBean.sendReply(): email to " + strUserID + " about to be sent for " + iSqncNmbr);
      if (Toolkit.autoReply(strUserID, strSubject, strMsg) != true)
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean.sendReply(): AutoReply failed.");

      return;
   }

   /* EK
     This function creates a Remedy HD tickets through a webservices.
     @Param m_iTcktSqncNmbr, Express sequence Id  (primary ID )
     @Param m_iVrsn, Ticket version

   */
   public String CreateRemedyTicket(int m_iTcktSqncNmbr, int m_iVrsn)
   {
      String strAccount = "";
      String strCircuitId = "";
      String strArea = "";       //Trouble Type
      String strSubArea = "";      //Trouble Sub Type
      String strDesc = "Test Express to Remedy ticket";
      String strAssignedToGroup = "";
      String strPriority = "";
      String strProductType = "";
      String strContact = "";   //who reported the issue
      String strContactPhone = "";
      String strEnvironment = "";
      String strSelCustomerType = "";
      String strCategory = "";
      String strRemedUser = "";
      String strAuthenticationInfo = "";
      String strPassword = "";
      String strTimeZone = "";
      String strLocale = "";
      String strServiceType = "";
      String strStatus = "";
      String strEmployee_ID_ = "";
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      RemedyValues xpressRemdyMap = RemedyValues.getInstance();
      RemedyExpMapNode mapNode = null;

      String strQuery1 =
         "SELECT " +
            "DR.CRCT_ID, " +
            "DR.PRDCT_TYP, " +
            "DR.TRBL_TYP, " +
            "DR.TRBL_SUB_TYP, " +
            "DR.SVRTY, " +
            "DR.PRRTY, " +
            "DR.TRBL_RPRTD, " +
            "DR.ACN, " +
            "DR.CNTCT_FRST_NM||'' || DR.CNTCT_LST_NM as CNAME, " +
            "DR.CLLBCK_TELNO as CPHONE, " +
            "DR.CLLBCK_TELNO_EXTNSN AS ext " +
         "FROM " +
            "DSTICKET_T D, " +
            "DSTCKT_RQST_T DR " +
         "WHERE " +
            "D.TCKT_SQNC_NMBR = DR.TCKT_SQNC_NMBR AND " +
            "D.VRSN = DR.VRSN AND " +
            "D.TCKT_SQNC_NMBR = ?  AND D.VRSN =  ? ";

      String strUpdateQuery = "Update DSTICKET_T  set TCKT_ID = ? WHERE  TCKT_SQNC_NMBR = ? ";


      Log.write(Log.DEBUG_VERBOSE, "Creating Remedy HD for Express-seq:[\t"+m_iTcktSqncNmbr+ "\t]");

      try
      {
         // Get Property Values ( contants),
         strAssignedToGroup = PropertiesManager.getProperty("lsr.REMEDY.OwnedByGroupId", "DSTAC Complex Data Services");

         strEnvironment = PropertiesManager.getProperty( "lsr.REMEDY.Environment", "Network Services" );

         strSelCustomerType = PropertiesManager.getProperty( "lsr.REMEDY.CustomerType", "Internal" );

         strCategory = PropertiesManager.getProperty( "lsr.REMEDY.Category", "VPN/Virtual Private Network" );

         strEmployee_ID_ = PropertiesManager.getProperty( "lsr.REMEDY.Employee_ID", "rmdDNocIface" );

         strRemedUser = PropertiesManager.getProperty( "lsr.REMEDY.UserId", "rmdWebService" );

         strPassword = PropertiesManager.getProperty( "lsr.REMEDY.pswd" , "rmdWebService" );

         strTimeZone = PropertiesManager.getProperty( "lsr.REMEDY.TZ" , "CST" );

         strLocale = PropertiesManager.getProperty( "lsr.REMEDY.LC" , "en_US" );
         strAuthenticationInfo = PropertiesManager.getProperty("lsr.REMEDY.Authentication", "" );

         strServiceType = PropertiesManager.getProperty("lsr.REMEDY.ServiceType" , "User Service Restoration" );

         strStatus = PropertiesManager.getProperty("lsr.REMEDY.Status", "New" );

         pstmt = m_conn.prepareStatement( strQuery1 );
         pstmt.clearParameters();
         pstmt.setInt( 1, m_iTcktSqncNmbr);
         pstmt.setInt( 2, m_iVrsn);

         rs = pstmt.executeQuery();

         strLocale = null;
         strTimeZone = "";

         if ( rs.next() ) {

            mapNode = (RemedyExpMapNode) xpressRemdyMap.getMapNode( rs.getString("SVRTY"), rs.getString("PRRTY") );

            strPriority = mapNode.getstrRemedyPrty();

            Log.write(Log.DEBUG_VERBOSE, "\n Creating a  \nn   " + strPriority  + "\t for exp-" + m_iTcktSqncNmbr + "\n" );

            strCircuitId= rs.getString("CRCT_ID");
            strProductType= rs.getString("PRDCT_TYP");
            strArea= rs.getString("TRBL_TYP");
            strSubArea= rs.getString("TRBL_SUB_TYP");
            strDesc= rs.getString("TRBL_RPRTD");
            strAccount= rs.getString("ACN");
            strContact= rs.getString("CNAME");
            // include user extention if provided.
            if( rs.getString( "ext" ) != null ){
               strContactPhone = rs.getString( "CPHONE" ) + " ext " + rs.getString("ext") ;
            }else{
               strContactPhone = rs.getString( "CPHONE" );
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean.CreateRemedy : "     + e.toString() );

      } finally {   // Clean up
         try{
            if ( rs != null ){ rs.close(); }
            if ( pstmt != null ){ pstmt.close(); }
         }catch (Exception e){
            e.printStackTrace();
            Log.write(Log.DEBUG_VERBOSE, "DsTicketBean.CreateRemedy() DB Exception on Query" + e.toString() );
         }
      }

      String strResponse = "";
      int iReturnCode = 0;

      // Setup the global JAXM message factory
      System.setProperty("javax.xml.soap.MessageFactory", "weblogic.webservice.core.soap.MessageFactoryImpl");

      // Setup the global JAX-RPC service factory
      System.setProperty( "javax.xml.rpc.ServiceFactory", "weblogic.webservice.core.rpc.ServiceFactoryImpl");

      String strSummary = "";

      OutputMapping OutputMap = null;

      try {

         Remedy.HPD_DNOC_Incident_CreateService_Impl wbRemedy = new HPD_DNOC_Incident_CreateService_Impl();

         Remedy.HPD_DNOC_Incident_CreatePortType webSrvcport = wbRemedy.getHPD_DNOC_Incident_CreatePortType();

         urn.HPD_DNOC_Incident_Create.AuthenticationInfo auth = new AuthenticationInfo( strRemedUser, strPassword, strAuthenticationInfo, strLocale, strTimeZone);

         urn.HPD_DNOC_Incident_Create.InputMapping inputmap = new InputMapping();

         inputmap.setCategorization_Tier_1(strEnvironment);

         inputmap.setCategorization_Tier_2(strSubArea);

         inputmap.setCategorization_Tier_3("");

         if( strDesc != null ) {

            strDesc = (strDesc.length() > 100 ? strDesc.substring(0,99) : strDesc);
         }
         inputmap.setDescription(strDesc);

         inputmap.setEmployee_ID_(strEmployee_ID_);

         //Impact (Impact field on the main Incident form) - This field has valid values of 1000, 2000, 3000, and 4000 that equate to:
         //  1000 = Extensive/Widespread
         //  2000 = Significant/Large
         //  3000 = Moderate/Limited
         //  4000 = Minor/Localized

         inputmap.setImpact(ImpactType.fromValue(mapNode.getstrRemedyImpct()) );

         inputmap.setProduct_Categorization_Tier_1(strEnvironment);

         inputmap.setProduct_Categorization_Tier_2(strCategory);

         inputmap.setProduct_Categorization_Tier_3("");

         //Service_Type (Service Type field on the Classification tab) -
         //   This field has valid values of 0, 1, 2, and 3 that
         //   equate to:
         //   0 = User Service Restoration
         //   1 = User Service Request
         //   2 = Infrastructure Restoration
         //   3 = Infrastructure Event

         inputmap.setService_Type(Service_TypeType.fromValue(strServiceType));

         // Status (Status field on main Incident form) -
         // This field has valid values of 0, 1, 2, 3, 4, 5, AND 6;
         // however, new Incidents must have the value of 0 for the
         // Status of "New"

         inputmap.setStatus(StatusType.fromValue(strStatus));

         //Urgency (Urgency field on the main Incident form) - This field has valid values of 1000, 2000, 3000, and 4000 that equate to:
         //   1000 = Critical
         //   2000 = High
         //   3000 = Medium
         //   4000 = Low
         inputmap.setUrgency(UrgencyType.fromValue(mapNode.getstrRemedyUrg()) );

         inputmap.setAssigned_Group(strAssignedToGroup);

         inputmap.setDetailed_Decription(strDesc);

         inputmap.setChrDNOC_CallBackName(strContact);

         inputmap.setChrDNOC_CallBackNumber(strContactPhone);

         inputmap.setChrDNOC_CircuitID(strCircuitId);

         inputmap.setChrDNOC_CustomerName(strAccount);

         inputmap.setMemDNOC_CustomerNotes(strDesc);

         // This field has valid values of 0, 1, and 2 that
         //   equate to:
         //   0 = Dedicated
         //   1 = Static IP
         //   2 = VPN

         inputmap.setSelDNCO_ProductType((SelDNCO_ProductTypeType.fromValue( strProductType )));

         Log.write(Log.DEBUG_VERBOSE,"CreateRemedyTicket(): inputmap = " + inputmap.toString() );

         OutputMap = webSrvcport.dNOC_Incident_Create(inputmap,auth);

         if( (OutputMap.getIncident_Number() != null) &&
             (OutputMap.getIncident_Number().length() > 0) ) {

            pstmt = m_conn.prepareStatement(strUpdateQuery);
            pstmt.clearParameters();
            pstmt.setString(1,OutputMap.getIncident_Number());
            pstmt.setInt(2,m_iTcktSqncNmbr);
            pstmt.executeUpdate();
         }
      } catch (Exception ee) {
         ee.printStackTrace();
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean.CreateRemedy : " + ee.toString() );
      }finally {   // Clean up
         try{
            if ( pstmt != null ){ pstmt.close(); }
         }catch (Exception e){
            e.printStackTrace();
            Log.write(Log.DEBUG_VERBOSE, "DsTicketBean.CreateRemedy() DB Exception on Query" + e.toString() );
         }
      }
      return OutputMap.getIncident_Number();
   }

   // NOTE: This methos is specific to response strings returned from TMS/Fusion/Siebel system
   //   after Express posts (GET) an XML ticket create event.
   // We're looking for success message, duplicate message or a failure....
   //
        public int interpretResponse(String strResponse)
        {
      int iRC = 0;

      //SUCCESS example:   <h2>Insert Completed Successfully</h2> <b>TMS Trouble Ticket 2-6BU09 created for Express Ticket Number EXP-83. </b>

   //DUPLICATE example: <h2>Error</h2> <b>Insert operation on integration component 'Service Request' failed because a matching record in
   //      business component 'Service Request' with search specification '[Customer Ref Number]="EXP-83"' was found.
   //      --
   //      Error invoking service 'EAI Siebel Adapter', method 'Insert' at step 'Write TT Record'.

      //BAD URL/Interface Down:
      //

      String strTemp2 = strResponse.trim();
      Log.write(Log.DEBUG_VERBOSE, "DsTicketBean() scanning......[" + strTemp2 +   "]");
      String strTemp = strTemp2.substring(5);
      Log.write(Log.DEBUG_VERBOSE, "DsTicketBean() scanning......[" + strTemp +   "]");

      int ii = strTemp.indexOf(TMS_OK);
      Log.write(Log.DEBUG_VERBOSE, "DsTicketBean() ii="+ii+" len="+strTemp.length());
      ii = strTemp.indexOf("E");
      Log.write(Log.DEBUG_VERBOSE, "DsTicketBean() ii="+ii+" len="+strTemp.length());

      if ( strTemp.indexOf(TMS_OK) >= 0 )   //OK ?
{
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean() OK!");
}
      if ( strTemp.indexOf("Insert Completed") >= 0 )   //OK ?
      {
         strTemp = (strTemp.substring( strTemp.indexOf(TMS_OK) + TMS_OK.length() )).trim();
         Log.write(Log.DEBUG_VERBOSE, "DsTicketBean() Get ROW-id buff["+strTemp+"]");
         if ( (strTemp.indexOf(" Trouble Ticket") >= 0) )
         {
            strTemp = (strTemp.substring( strTemp.indexOf(TMS_OK2)+ TMS_OK2.length() , strTemp.indexOf(TMS_OK3))).trim();
            Log.write(Log.DEBUG_VERBOSE, "DsTicketBean() Get ROW-id buff["+strTemp+"]");
         }
         iRC = 0;
      }
      else //DUP ?
      if ( strTemp.indexOf("failed because a matching record") >= 0 )
      {   Log.write(Log.WARNING, "DsTicketBean() TMS ticket already created, and will NOT be updated");
         Log.write(Log.WARNING, "DsTicketBean() Can occur if ticket reset to INITIAL and reSUBMITTED");
         iRC = 1;
      }
      else  //OTHER ERROR ?
      if ( strTemp.indexOf("Error") >= 0 )
      {   Log.write(Log.DEBUG_VERBOSE, "DsTicketBean() TMS Error encountered !");
         iRC = -1;
      }

       return iRC;
      }


   /* Send the provider autoReply if necessary
    * EK: 7/06, Clean up function and add remedy information in message.
    */
   protected void sendProvReply(int iSqncNmbr, int iVrsn)
   {
        String strQuery1 = " SELECT D.STTS_CD, D.OCN_NM,  "
         +   " TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI') AS THE_TIME,  "
         +   " rq.SVRTY, rq.PRRTY, rq.TRBL_RPRTD FROM DSTICKET_T D,  SERVICE_TYPE_T S, DSTCKT_RQST_T rq "
         +   " WHERE D.TCKT_SQNC_NMBR = " + iSqncNmbr
         +   " AND D.VRSN = " + iVrsn + " AND rq.TCKT_SQNC_NMBR = D.TCKT_SQNC_NMBR "
         +   " AND D.SRVC_TYP_CD = S.SRVC_TYP_CD AND S.TYP_IND = '" + thisOrder.getTYP_IND() + "' ";

      ResultSet rs = null;
      String strSubject = "";
      String strMsg = "";
      String strEmail = "";
      String strRemdysev = "";
      RemedyValues xpressRemdyMap = RemedyValues.getInstance();
      RemedyExpMapNode mapNode = null;
        try
        {
         rs = m_stmt.executeQuery(strQuery1);
         if (rs.next())
         {
            mapNode = (RemedyExpMapNode)  xpressRemdyMap.getMapNode( rs.getString("SVRTY"), rs.getString("PRRTY") );
            strRemdysev = mapNode.getstrRemedyPrty();

            // Build the Subject
            strSubject = "Express Trouble Ticket Status Change for:  " + iSqncNmbr;

            // Build the Message
            strMsg =          "Express Number:  " + iSqncNmbr + "\n";
            strMsg = strMsg + "New Status    :  " + rs.getString("STTS_CD") + "\n";
            strMsg = strMsg + "Community     :  " + rs.getString("OCN_NM") + "\n";
            strMsg = strMsg + "Trouble Ticket:  " + this.getExternalTicketId() + "\n";
            strMsg = strMsg + "Remedy Severity: " + strRemdysev + "\n";
            strMsg = strMsg + "Date/Time     :  " + rs.getString("THE_TIME") + "\n\n";
            rs.close();

         }
         else
         {
            //DatabaseManager.releaseConnection(m_conn);
            Log.write(Log.ERROR, "DsTicketBean.sendProvReply(): Error getting email info");
            return;
         }
      } catch(SQLException e) {
         e.printStackTrace();
         try { rs.close(); } catch(Exception e2) {}
         Log.write(Log.ERROR, "DsTicketBean.sendProvReply(): DB Exception on Query : " + strQuery1);
         return;
      }

      try
      {
         // Get Email address from properties file
         strEmail = PropertiesManager.getProperty("lsr.dstt.ProviderEmailAddress","");
      }
      catch(Exception e)
      {
           e.printStackTrace();
           Log.write(Log.ERROR, "DsTicketBean.sendProvReply(): Error retrieving e-mail address from lsr.properties");
      }

      Log.write(Log.DEBUG_VERBOSE,"DsTicketBean.sendProvReply(): email to " + strEmail + " about to be sent for " + iSqncNmbr);
      if (Toolkit.autoProvReply(strEmail, strSubject, strMsg) != true){
         Log.write(Log.ERROR, "DsTicketBean.sendProvReply(): AutoReply failed.");
      }
      return;
   }

   /**
   * Set external Ticket id (currently from Seibel system)
   * @param   String    representing the TicketId from TMS/Siebel (external) system
   * @return    void
   */
   public void setExternalTicketId(String strExternalTicketId)
   {
      this.m_strExternalTicketId = strExternalTicketId;
   }

   /**
   * Get external Ticket id member variable
   * @return    String    representing the TicketId from TMS/Siebel (external) system
   */
   public String getExternalTicketId()
   {
      return this.m_strExternalTicketId;
   }
}
