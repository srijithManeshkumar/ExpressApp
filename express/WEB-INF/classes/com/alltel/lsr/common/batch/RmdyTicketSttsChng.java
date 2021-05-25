/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2006
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/**
 * MODULE:	RmdyTicketSttsChng.java
 *
 * DESCRIPTION: Batch job which will automatically update Rmdy statuses,etc into Express
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:      6/2006
 *
 * HISTORY:
 *
 * EK, 6/2006: Retrieve ticket infomation from
 * 			Remedy webservice insteady of Fusion/TMS/Siebel system. This is part of Transition
 *			Project: TAC/DNOC Remedy Wireline Migration
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */
package com.alltel.lsr.common.batch;

import java.io.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;
import Remedy.*; // RemedyWebService_client.jar
import urn.HPD_HelpDesk_DNOC_AlltelExpress.*;
public class RmdyTicketSttsChng
{
	public static void main(String[] args)
	{
		Connection expressCon = null;
		Statement expressStmt1 = null;
		Statement expressStmt2 = null;
		PreparedStatement remedyXrefStmt = null;
		PreparedStatement actionPstmt = null;
		ResultSet expressRs1 = null;
		ResultSet expressRs2 = null;
		ResultSet expressRs3 = null;
		boolean bChangesNecessary = false;
		boolean bSendCustEmail = false;
		boolean bSendProvEmail = false;
		int iCount=0;
		final String TYP_IND="S";

		if (args.length != 3)
		{
			System.out.println("Usage:  RmdyTicketSttsChng <Express URL> <Express DB userid> <Express DB passwd>");
			System.exit(1);
		}

		// Retrieve Parameters from command line
		String strExpressURL = args[0];
		String strExpressUserid = args[1];
		String strExpressPassword = args[2];

		String strUserid = "Express";
		Hashtable hRemedyHsh = new Hashtable( 10 );
		String strRemedUser = "", strLocale = "",strCaseType = "",strRemedyPassword = "",strTimeZone = "",
				strCategory = "",strAuthenticationInfo = "",strRequesterID = "", strSelCustomerType = "";

		String strPropFileName = System.getProperty( "propfile" );
		Properties appProps = new Properties();
		try
		{

			appProps.load(new FileInputStream(strPropFileName));
			strRequesterID = appProps.getProperty( "lsr.REMEDY.RequesterID", "atl_express");
			strCategory = appProps.getProperty( "lsr.REMEDY.Category", "WAN" );
			strCaseType =  appProps.getProperty( "lsr.REMEDY.CaseType", "Incident" );
			strRemedUser = appProps.getProperty( "lsr.REMEDY.UserId", "atl_express" );
			strRemedyPassword = appProps.getProperty( "lsr.REMEDY.pswd" , "xgrove^19!" );
			strTimeZone = appProps.getProperty( "lsr.REMEDY.TZ" , "CST" );
			strLocale = appProps.getProperty( "lsr.REMEDY.LC" , "en_US" );
			strAuthenticationInfo = appProps.getProperty("lsr.REMEDY.Authentication", "" );
			// EK: Connect to Express  Oracle DB
			Class.forName("oracle.jdbc.OracleDriver");
			expressCon = DriverManager.getConnection(strExpressURL, strExpressUserid, strExpressPassword);

			// EK:  Remedy-xpress status mapping
			remedyXrefStmt = expressCon.prepareStatement("select REMEDY_STTS_CD, EXP_STTS_CD FROM REMEDY_STTS_XREF_T");
			expressRs2 = remedyXrefStmt.executeQuery();
			while( expressRs2.next() ){
				hRemedyHsh.put( expressRs2.getString(1), expressRs2.getString(2) );
			}


			// EK: clean up
			expressRs2.close();
			expressRs2 = null;

			// EK: Turn off Auto Commit
			expressCon.setAutoCommit(false);
			// EK: Create Express Statements
			expressStmt1 = expressCon.createStatement();
			expressStmt2 = expressCon.createStatement();
			String str_IND = TYP_IND;
			String str_RQST_TYP_CD = "T";
			actionPstmt = expressCon.prepareStatement( "SELECT A.ACTN_SND_CUST_RPLY, A.ACTN_SND_PROV_RPLY FROM ACTION_T A " +
					" WHERE A.STTS_CD_FROM=? AND A.STTS_CD_TO=? AND A.TYP_IND = ? AND A.RQST_TYP_CD =? " );

			// Retrieve all Tickets to be automatically processed
			String strGetTickets = "SELECT D.TCKT_SQNC_NMBR, D.VRSN, D.STTS_CD, D.HSTRY_SQNC_NMBR, D.OCN_NM, "+
				" DR.CLLBCK_TELNO||' '||DR.CLLBCK_TELNO_EXTNSN||'    '||DR.CNTCT_FRST_NM||' '||DR.CNTCT_LST_NM AS CONTACT, "+
				" D.TCKT_ID, DR.SUB_STTS, TO_CHAR(DR.CLSD_DT,'YYYYMMDDHH24MISS') CLS, TO_CHAR(DR.RSLVD_DT,"+
				"'YYYYMMDDHH24MISS') RES, DR.RSPNS_NOTES, D.NT_ACTN, TO_CHAR(D.NT_ACTN_DT,'YYYYMMDDHH24MISS') aNoteDt "+
				" FROM DSTICKET_T D, DSTCKT_RSPNS_T DR "+
				" WHERE D.TCKT_ID like 'HD%' AND STTS_CD NOT IN ('INITIAL','CLOSED','CANCELLED') AND D.TCKT_SQNC_NMBR=DR.TCKT_SQNC_NMBR AND D.VRSN=DR.VRSN order by 1 ";



			String strCurrSqncNmbr = "";
			int iVrsn = 0;
			String strPrevSqncNmbr = "";
			String strCurrHistrySqncNmbr = "";
			String strCurrSttsCd = "";
			String strCurrSubSttsCd = "";
			String strCurrResDt = "";
			String strCurrClsDt = "";
			String strCurrRmdyTicketId = "";
			String strCurrResolution = "";

			String strCurrNoteAction= "";
			String strCurrNoteDt= "";

			String strNewSttsCd = "";
			String strRmdyTicketId = "";
			String strRmdySttsCd = "";
			String strRmdySubSttsCd = "";
			String strRmdyResDt = "";
			String strRmdyClsDt = "";
			String strRmdyResolution = "";

			//String strNote = "";
			String strNoteAction = "";
			String strNoteDt = "";
			StringBuffer buffer = new StringBuffer();

			String strCurrDt = "";

			HPD_HelpDesk_DNOC_AlltelExpressService_Impl wbRemedy =  new HPD_HelpDesk_DNOC_AlltelExpressService_Impl();
			HPD_HelpDesk_DNOC_AlltelExpressPortType webSrvcport =  webSrvcport = wbRemedy.getHPD_HelpDesk_DNOC_AlltelExpressPortType( strRemedUser, strRemedyPassword);
			AuthenticationInfo auth  = auth = new AuthenticationInfo( strRemedUser, strRemedyPassword, strAuthenticationInfo, strLocale, strTimeZone);
			GetOutputMap OutputMap =null;

			// Loop thru each row, we only want the first row for each sqnc nmbr
			expressRs1 = expressStmt1.executeQuery(strGetTickets);
			while (expressRs1.next())
			{
				bChangesNecessary = false;
				strCurrSqncNmbr = expressRs1.getString("TCKT_SQNC_NMBR");
				iVrsn = expressRs1.getInt("VRSN");
				strCurrSttsCd = expressRs1.getString("STTS_CD");
				strCurrHistrySqncNmbr = expressRs1.getString("HSTRY_SQNC_NMBR");

				strCurrRmdyTicketId = expressRs1.getString("TCKT_ID");
				strCurrSubSttsCd = expressRs1.getString("SUB_STTS");
				strCurrClsDt = expressRs1.getString("CLS");
				strCurrResDt = expressRs1.getString("RES");
				strCurrResolution = expressRs1.getString("RSPNS_NOTES");
				strCurrNoteAction = expressRs1.getString("NT_ACTN");
				if (strCurrNoteAction == null) strCurrNoteAction="";
					strCurrNoteDt = expressRs1.getString("aNoteDt");
 				if (strCurrNoteDt == null) strCurrNoteDt="";

				buffer.append("\nExpress:" + strCurrSqncNmbr + " " + iVrsn+ " Rmdy:"+strCurrRmdyTicketId+" Status:"+strCurrSttsCd+
					" Sub:["+strCurrSubSttsCd+"] CloseDate:"+strCurrClsDt+" ResDate:"+strCurrResDt+"\nResol:["+
					strCurrResolution+"]");

				strPrevSqncNmbr = strCurrSqncNmbr;

				String strROWID = "";
				// Obtain Remedy Info
				//RmdyPstmt.setString(1, "EXP-"+strCurrSqncNmbr);
				GetInputMap remedyInput= new GetInputMap( strCurrRmdyTicketId );
				OutputMap  = webSrvcport.opGet( remedyInput,  auth);
				if ( OutputMap != null )
				{
					System.out.println( "*********OutputMap******" + OutputMap.toString() );

					strRmdyTicketId = ExpressUtil.fixNullStr( OutputMap.getCase_ID() );
					strRmdySttsCd = ExpressUtil.fixNullStr( OutputMap.getStatus().getValue() );
					strRmdySubSttsCd = strRmdySttsCd;
					strNoteAction  = ExpressUtil.fixNullStr( OutputMap.getZselDNOC_LastActionCode().getValue() );
					if( OutputMap.getResolved_Time()!= null ) {
						strRmdyResDt = ExpressUtil.getDateTime(  OutputMap.getResolved_Time()  ) ;
					}
					if( OutputMap.getDtm_ClosedTime()!= null ) {
						strRmdyClsDt = 	ExpressUtil.getDateTime( OutputMap.getDtm_ClosedTime() ) ;
					}

					strRmdyResolution =  ExpressUtil.fixNullStr( OutputMap.getMemDNOC_CustomerNotes() );
					//strRmdyResolution +=  ExpressUtil.fixNullStr( OutputMap.getSolution_Description() );

					if( OutputMap.getModified_Time()!= null ) {
						strNoteDt = ExpressUtil.getDateTime( OutputMap.getModified_Time() ) ;
					}

					buffer.append("\n Remedy: Ticket="+strRmdyTicketId+" Express Status="+strRmdySttsCd+" Remedy Status=["+
						strRmdySubSttsCd + "] ResDate="+strRmdyResDt+" CloseDate="+strRmdyClsDt+"\n Remedy Resolution=["+
						strRmdyResolution+"] NoteAction=["+strNoteAction+"] NoteDt=["+ strNoteDt + "] ");
				}
				else
				{	//Error Occurred

					System.out.println("***** THIS IS AN ERROR SITUATION !!. An alert file will be created and ITO will pick it up! ");

					//CHG 7/6
					// Send email to end-user notifying them there may have been an issue when they created their ticket
					String strComm = expressRs1.getString("OCN_NM");
					String strSubject = "Trouble Ticket Status Change for Express #:  " + strCurrSqncNmbr;
					// Build the Message
					StringBuffer buffer2 = new StringBuffer();
					buffer2.append("Express Number:  " + strCurrSqncNmbr + "\n");
					buffer2.append("New Status    :  " + strNewSttsCd + "\n");
					buffer2.append("Community     :  " + strComm + "\n");
					buffer2.append("Trouble Ticket:  " + strRmdyTicketId + "\n");
					buffer2.append("Date/Time     :  " + strCurrDt + "\n\n");
					buffer2.append("There may have been a problem creating this Trouble Ticket.\n\nTo ensure the ticket gets properly reported and worked as soon as possible, please call the DS TAC at: 1-866-990-DATA (3282).\n\n");
					if (Toolkit.autoReply("edris.kalibala@alltel.com", strSubject, buffer2.toString()) != true)
					{
					}
					buffer2.delete(0, buffer2.length()+1);

					continue;
				}

				// Map it to Express Stts Cd
				strNewSttsCd = (String) hRemedyHsh.get(  OutputMap.getStatus().getValue() );
				if ( strNewSttsCd == null || strNewSttsCd.length() < 1 )
				{
					System.out.println("No matching Express status for Rmdy code: " + strRmdySttsCd);
					strNewSttsCd = strCurrSttsCd;//no chg
					//continue;
				}

				String strUpdHst = "";

				System.err.println( strCurrSqncNmbr + "\t Old status: \t" +  strCurrSttsCd + " new status:\t" + strNewSttsCd );
				if (! strCurrSttsCd.equals(strNewSttsCd))
				{
					bSendCustEmail = false;
					bSendProvEmail = false;

					// Update DSTICKET_T and ticket history
					buffer.append("\nUpdate Ticket Number: " + strCurrSqncNmbr + "  to status: " + strNewSttsCd);

					try
					{
						// Obtain the current system date
						expressRs3 = expressStmt2.executeQuery("SELECT TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI:SS') CURR_DT FROM dual");
						expressRs3.next();
						strCurrDt = expressRs3.getString("CURR_DT");
						expressRs3.close();

						// Update Current History Row
						 strUpdHst = "UPDATE DSTICKET_HISTORY_T SET STTS_CD_OUT = '" + strNewSttsCd +
							"', HSTRY_DT_OUT = TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), " +
							"MDFD_DT = TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS')" +
//leave this userid alone..				", MDFD_USERID = '" + strUserid + "' "+
							" WHERE HSTRY_SQNC_NMBR = " + strCurrHistrySqncNmbr;

						if (expressStmt2.executeUpdate(strUpdHst) != 1)
						{
							System.out.println("The following UPDATE failed: " + strUpdHst);
							throw new Exception();
						}
						// Get next History Sequence Number
						String strHstQry = "SELECT DSTICKET_HISTORY_SEQ.nextval TCKT_HSTRY_SQNC_NMBR_NEW FROM dual";
						expressRs3 = expressStmt2.executeQuery(strHstQry);
						expressRs3.next();
						int iTcktHstrySqncNmbrNew = expressRs3.getInt("TCKT_HSTRY_SQNC_NMBR_NEW");
						expressRs3.close();

						// Insert New History Row
						String strInsHst = "INSERT INTO DSTICKET_HISTORY_T VALUES(" + iTcktHstrySqncNmbrNew + ",0," + strCurrSqncNmbr + ",'" + strNewSttsCd + "', 'N/A', TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), '" + strUserid + "')" ;

						if (expressStmt2.executeUpdate(strInsHst) != 1)
						{
							System.out.println("The following INSERT failed: " + strInsHst);
							throw new Exception();
						}

						// Update DSTICKET_T
						String strUpdTckt = "UPDATE DSTICKET_T SET STTS_CD = '" + strNewSttsCd + "', " +
							"TCKT_ID='"+ strRmdyTicketId + "', HSTRY_SQNC_NMBR = " + iTcktHstrySqncNmbrNew + ", " +
							"MDFD_DT = TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), " +
							"MDFD_USERID = '" + strUserid + "' " +
							" WHERE TCKT_SQNC_NMBR = " + strCurrSqncNmbr + " AND VRSN="+iVrsn;
						buffer.append("\nstrUpdTckt="+strUpdTckt);

						if (expressStmt2.executeUpdate(strUpdTckt) != 1)
						{
							System.out.println("The following UPDATE failed: " + strUpdTckt);
							throw new Exception();
						}

					}
					catch (Exception e)
					{
						e.printStackTrace();
						expressCon.rollback();
						continue;
					}
					bChangesNecessary = true;

					//EMail block...
					try {
						actionPstmt.setString(1,strCurrSttsCd);
						actionPstmt.setString(2,strNewSttsCd);
						actionPstmt.setString(3, TYP_IND );
						actionPstmt.setString(4, str_RQST_TYP_CD );

						expressRs2 = actionPstmt.executeQuery();
						if ( expressRs2.next() ) {
							if ( expressRs2.getString("ACTN_SND_CUST_RPLY").equals("Y") )
							{
								bSendCustEmail = true;
								System.out.println("email to Cust");
							}
							if ( expressRs2.getString("ACTN_SND_PROV_RPLY").equals("Y") )
							{
								System.out.println("email to prov");
								bSendProvEmail = true;
							}
						}
						if (expressRs2!= null ){ expressRs2.close();expressRs2=null; }
					}
					catch (Exception e)
					{
						e.printStackTrace();
						//Not a fatal error
						System.out.println("ERROR in email area !! Ticket=" + strCurrSqncNmbr + " "+iVrsn +
							" "+ strCurrSttsCd + " " + strNewSttsCd);
							expressRs2.close();expressRs2=null;
					}



				}
				if (strCurrRmdyTicketId==null) strCurrRmdyTicketId="";
				if (strCurrSubSttsCd==null) strCurrSubSttsCd="";
				if (strCurrResDt==null) strCurrResDt="";
				if (strCurrClsDt==null) strCurrClsDt="";
				if (strCurrResolution==null) strCurrResolution="";
				if (strRmdyTicketId==null) strRmdyTicketId="";
				if (strRmdySubSttsCd==null) strRmdySubSttsCd="";
				if (strRmdyResDt==null) strRmdyResDt="";
				if (strRmdyClsDt==null) strRmdyClsDt="";
				if (strRmdyResolution==null) strRmdyResolution="";
				strRmdyResolution = Toolkit.replaceSingleQwithDoubleQ(strRmdyResolution);

				if (strRmdyClsDt==null) strRmdyClsDt="";
				if (strNoteAction== null) strNoteAction="";
				if (strNoteDt== null) strNoteDt="";

				// Regardless if the status changed, check for these field updates
				// 	Resolve Date, Close Date, Resolution Text, SR-NUM, NOTES
				if ( (!strCurrRmdyTicketId.equals(strRmdyTicketId))
					 || (!strCurrSubSttsCd.equals(strRmdySubSttsCd))
					 ||	(!strCurrResDt.equals(strRmdyResDt))
					 || (!strCurrClsDt.equals(strRmdyClsDt))
					 ||	(!strCurrResolution.equals(strRmdyResolution))
					 || (!strCurrNoteDt.equals(strNoteDt))   )
				{
					boolean bNoteChg = false;
					if ( (!strCurrNoteDt.equals(strNoteDt)) && !bChangesNecessary )
					{	System.out.println(" Note added...must also upd DSTICKET_T ");
						bNoteChg = true;
					}

					String strUpdResTxt = "UPDATE DSTCKT_RSPNS_T SET TMS_TCKT_ID='"+strRmdyTicketId+"', " +
						" SUB_STTS='"+strRmdySubSttsCd+"', RSPNS_NOTES = '" + ( (strRmdyResolution.length() > 3999 ) ? strRmdyResolution.substring(0,3999): strRmdyResolution ) + "', ";

					if (strRmdyResDt.length()>1) strUpdResTxt+= " RSLVD_DT=TO_DATE('"+strRmdyResDt+"','YYYY/MM/DD HH24:MI:SS'), ";
					if (strRmdyClsDt.length()>1) strUpdResTxt+= " CLSD_DT=TO_DATE('"+strRmdyClsDt+"','YYYY/MM/DD HH24:MI:SS'), ";
					String strNoteUpd = "";
					if (!strCurrNoteDt.equals(strNoteDt))
					{	if (strNoteAction.length()>1){
							strNoteUpd+= " NT_ACTN='"+ strNoteAction +"', ";
						}
						if (strNoteDt.length()>1) {
							strNoteUpd+= " NT_ACTN_DT=TO_DATE('"+strNoteDt+"','YYYY/MM/DD HH24:MI:SS'), ";
						}
					}
					strUpdResTxt += strNoteUpd +
						" MDFD_DT = sysdate, MDFD_USERID = '" + strUserid + "' "+
						" WHERE TCKT_SQNC_NMBR = " + strCurrSqncNmbr + " AND VRSN="+iVrsn;
					buffer.append("\nstrUpdResTxt="+strUpdResTxt);
					System.out.println( strUpdResTxt );
					PreparedStatement pstmt =null;
					try {
						System.out.println( "line 406" );
						//expressStmt2.close();
						///expressStmt2 = null;
						pstmt = expressCon.prepareStatement( strUpdResTxt );
						pstmt.executeUpdate();
						///
						pstmt.close();


					}
					catch (Exception e) {

						e.printStackTrace();
						expressCon.rollback();
						continue;
					}

					if ( ((strCurrRmdyTicketId.length()<2) && (strRmdyTicketId.length()>1)) || bNoteChg )
					{
						strUpdResTxt = "UPDATE DSTICKET_T SET TCKT_ID='"+ strRmdyTicketId + "', " + strNoteUpd +
							" MDFD_DT = sysdate, MDFD_USERID = '" + strUserid + "' " +
							" WHERE TCKT_SQNC_NMBR = " + strCurrSqncNmbr + " AND VRSN="+iVrsn;
						System.out.println("UPDATE : " + strUpdResTxt);
						try {
							if (expressStmt2.executeUpdate(strUpdResTxt) != 1)
							{
								System.out.println("The following UPDATE failed: " + strUpdResTxt);
								throw new Exception();
							}
						}
						catch (Exception e) {
								System.out.println( "line 433" );
							e.printStackTrace();
							expressCon.rollback();
							continue;
						}
					}
					bChangesNecessary = true;
				}

				//if changes, commit
				if (bChangesNecessary)
				{	expressCon.commit();
					iCount++;
					System.out.println(buffer);
				}
				buffer.delete(0, buffer.length()+1);
				//Send email?
				if (bSendCustEmail)
				{
					String strComm = expressRs1.getString("OCN_NM");
					String strSubject = "Trouble Ticket Status Change for Express #:  " + strCurrSqncNmbr;
					// Build the Message
					buffer.append("Express Number:  " + strCurrSqncNmbr + "\n");
					buffer.append("New Status    :  " + strNewSttsCd + "\n");
					buffer.append("Community     :  " + strComm + "\n");
					buffer.append("Trouble Ticket:  " + strRmdyTicketId + "\n");
					buffer.append("Date/Time     :  " + strCurrDt + "\n\n");
					if (Toolkit.autoReply(strUserid, strSubject, buffer.toString()) != true)
					{
					}
					buffer.delete(0, buffer.length()+1);
				}
				if (bSendProvEmail)
				{
					String strComm = expressRs1.getString("OCN_NM");
					String strContact = expressRs1.getString("CONTACT");
					String strSubject = "BATCH JOB: Trouble Ticket Status Change for Express #:  " + strCurrSqncNmbr;
					// Build the Message
					buffer.append("Express Number:  " + strCurrSqncNmbr + "\n");
					buffer.append("New Status    :  " + strNewSttsCd + "\n");
					buffer.append("Community     :  " + strComm + "\n");
					buffer.append("Contact       :  " + strContact + "\n");
					buffer.append("Trouble Ticket:  " + strRmdyTicketId + "\n");
					buffer.append("Date/Time     :  " + strCurrDt + "\n\n");
					if (Toolkit.autoReply("paul.sedlak@alltel.com", strSubject, buffer.toString()) != true)
					{
					}
					buffer.delete(0, buffer.length()+1);
				}

			} //while()

			// Clean up and Close
			expressStmt1.close();
			expressStmt1 = null;
			expressStmt2.close();
			expressStmt2 = null;
			if ( expressRs1 != null) expressRs1.close();
			expressRs1 = null;
			if ( expressRs2 != null) expressRs2.close();
			expressRs2 = null;
			expressCon.close();

			System.out.println("\n\n*** Express records updated="+iCount+"\n");
		}
		catch (Exception e)
		{
			System.out.println("\n\n*** Exception=["+e+"]");
			try
			{
				if ( expressCon != null ){
					expressCon.rollback();
					expressCon.close();
				}
			}
			catch(Exception se)
			{
				se.printStackTrace();
				return;
			}
			e.printStackTrace();
			return;
		}
		return;
	}
}
