/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/**
 * MODULE:		AutoTicketSttsChng.java
 *
 * DESCRIPTION: Batch job which will automatically update Ticket Statuses based on
 *			a WFM status code.
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        06-01-2002
 *
 * HISTORY:
 *	8-16-2004 pjs added log message and continue statement
 *	12-13-2004 pjs skip Bogus TNs
 *      2007-02-13 HD0000002263749 Steve Korchnak
 *                 Altered the WFM Informix DB signon - was not working following LD1
 *  03/23/2007  Steve Korchnak   - added package identification to facilitate .war file distribution
 *
 */

package com.alltel.lsr.common.batch;


import java.io.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import urn.HPD_DNOC_Incident_Inquiry.AuthenticationInfo;
import urn.HPD_DNOC_Incident_Inquiry.GetInputMap1;
import urn.HPD_DNOC_Incident_Inquiry.GetOutputMap1;
import HPD_DNOC_Incident_Inquiry.HPD_DNOC_Incident_InquiryPortTypePortType;
import HPD_DNOC_Incident_Inquiry.HPD_DNOC_Incident_InquiryService_Impl;

public class AutoTicketSttsChng
{
	public static void main(String[] args)
	{
            
                System.out.println("first line !!");
		Connection expressCon = null;
	//      Connection wfmCon = null;
		Statement expressStmt1 = null;
		Statement expressStmt2 = null;
              //  Statement expressStmt3 = null;
	//	CallableStatement wfmSPStmt = null;
		PreparedStatement wfmXrefStmt = null;
		ResultSet expressRs1 = null;
		//ResultSet expressRs2 = null; - Antony - clean up later
		ResultSet expressRs3 = null;
	//	ResultSet wfmRs1 = null;
              //  ResultSet expressRs4 = null;
	//	if (args.length != 7)
                if (args.length != 3)
		{
			System.out.println("Usage:  AutoTicketSttsChng <Express URL> <Express DB userid> <Express DB passwd> <WFM URL> <userid>");
			System.exit(1);
		}
		// Retrieve Parameters from command line
		String strExpressURL = args[0];
		String strExpressUserid = args[1];
		String strExpressPassword = args[2];
                String strUserid = "Express";
                String strRemedUser = "", strLocale = "",strRemedyPassword = "",strTimeZone = "",
		strAuthenticationInfo = "";
// Commented    Obtaining WFM STATUS CODE using Stored procedure, and webservice -- Vijay - 19-04-12
//		String strWfmURL = args[3];
//		String strUserid = args[4];                
//              String strWfmUsr = args[5];
//              String strWfmPwd = args[6];

                Calendar c1 = new GregorianCalendar();
                Calendar c2 = new GregorianCalendar();

                System.out.println("before getting property file system property !!");
                
                String strPropFileName = System.getProperty("PATH");
                
                if(strPropFileName != null)
                    System.out.println("strPropFileName : "+strPropFileName);
                else
                    System.out.println("strPropFileName is null !");
                
		Properties appProps = new Properties();
		try
		{
                        System.out.println("before loading properties file !!");
                        /*
                        appProps.load(new FileInputStream(strPropFileName));
			strRemedUser = appProps.getProperty( "lsr.REMEDY.UserId", "atl_express" );
			strRemedyPassword = appProps.getProperty( "lsr.REMEDY.pswd" , "xgrove^19!" );
			strTimeZone = appProps.getProperty( "lsr.REMEDY.TZ" , "CST" );
			strLocale = appProps.getProperty( "lsr.REMEDY.LC" , "en_US" );
			strAuthenticationInfo = appProps.getProperty("lsr.REMEDY.Authentication", "" );
                        */
                        
                        
                        System.out.println("before loading properties file !!");
                        
                        System.out.println("before creating connection object !!");
			// Connect to Express
			Class.forName("oracle.jdbc.driver.OracleDriver");
			expressCon = DriverManager.getConnection(strExpressURL, strExpressUserid, strExpressPassword);

                        //log message remove later - Antony
                        
                        if(expressCon == null)
                            System.out.println("connection object is null");
                        else
                            System.out.println("connection object is not null : "+expressCon);
                        
			// Turn off Auto Commit
			expressCon.setAutoCommit(false);

			// Create Express Statements
			expressStmt1 = expressCon.createStatement();
			expressStmt2 = expressCon.createStatement();
                  //      expressStmt3 = expressCon.createStatement();
                        
			wfmXrefStmt = expressCon.prepareStatement("SELECT TCKT_STTS_CD FROM WFM_STTS_XREF_T WHERE WFM_STTS_CD = ?");

                        // Commented Obtaining WFM STATUS CODE using Stored procedure, and webservice -- Vijay - 19-04-12
			// Connect to WFM
                      /*  try
			{
                           Class.forName("com.informix.jdbc.IfxDriver");
                        }
                        catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
                        try
			{
                          wfmCon = DriverManager.getConnection(strWfmURL,strWfmUsr,strWfmPwd);
                        }
                        catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
			// Create WFM Statements
			wfmSPStmt = wfmCon.prepareCall("{call retrieve_status(?,?) }");*/

			// Retrieve all Tickets to be automatically processed
			//String strGetTickets = "SELECT DISTINCT T.TCKT_SQNC_NMBR, T.TELNO, T.TCKT_STTS_CD, TH.TCKT_HSTRY_DT_IN, TO_CHAR(TH.TCKT_HSTRY_DT_IN,'MMDDYYYYHH24MI') CREATE_DATE, T.TCKT_HSTRY_SQNC_NMBR FROM TICKET_T T, TICKET_HISTORY_T TH WHERE T.TCKT_SQNC_NMBR = TH.TCKT_SQNC_NMBR AND TCKT_STTS_CD IN ('SUBMITTED','TEST','PENDING_DISPATCH') AND TH.TCKT_STTS_CD_IN = 'INITIAL' ORDER BY T.TCKT_SQNC_NMBR, TCKT_HSTRY_DT_IN";

                        String strGetTickets = " SELECT DISTINCT T.TCKT_SQNC_NMBR, T.TELNO, T.TCKT_STTS_CD, TH.TCKT_HSTRY_DT_IN, to_date(to_char(sysdate,'MMDDYYYY'), 'MMDDYYYY') - to_date(TO_CHAR(TH.TCKT_HSTRY_DT_IN,'MMDDYYYY'),'MMDDYYYY')+1 DAYS, T.TCKT_HSTRY_SQNC_NMBR FROM TICKET_T T, TICKET_HISTORY_T TH WHERE T.TCKT_SQNC_NMBR = TH.TCKT_SQNC_NMBR AND TCKT_STTS_CD IN ('SUBMITTED','TEST','PENDING_DISPATCH','ASSIGNED','IN PROGRESS','PENDING','RESOLVED','CANCELLED') AND TH.TCKT_STTS_CD_IN = 'INITIAL' ORDER BY T.TCKT_SQNC_NMBR, TCKT_HSTRY_DT_IN ";

			expressRs1 = expressStmt1.executeQuery(strGetTickets);

			String strCurrSqncNmbr = "";
			String strPrevSqncNmbr = "";
			String strTelno = "";
			String strCurrSttsCd = "";
			String strCreateDt = "";
			String strWfmSttsCd = "";
			String strWfmClassItem = "";
			String strWfmFault = "";
			String strWfmCause = "";

                        // Webservice call Start -- added by Vijay - 05-04-12
                        
                        strRemedUser = "rmdBIPIface";
			strRemedyPassword = "Skyline";
			strTimeZone = "CST";
			strLocale = "en_US";
			strAuthenticationInfo = "";
                        
                        HPD_DNOC_Incident_InquiryService_Impl webser = new HPD_DNOC_Incident_InquiryService_Impl();
                        HPD_DNOC_Incident_InquiryPortTypePortType webserport =  webser.getHPD_DNOC_Incident_InquiryPortTypePortType(strRemedUser, strRemedyPassword);
                        AuthenticationInfo auth  = new AuthenticationInfo( strRemedUser, strRemedyPassword, strAuthenticationInfo, strLocale, strTimeZone);
                        GetOutputMap1  OutputMap = null;

			// Loop thru each row, we only want the first row for each sqnc nmbr
			while (expressRs1.next())
			{
				strCurrSqncNmbr = expressRs1.getString("TCKT_SQNC_NMBR");

				// Only one row per sequence number
				if (strCurrSqncNmbr.equals(strPrevSqncNmbr))
					continue;

				System.out.println("\nProcessing Request: " + strCurrSqncNmbr);
				strPrevSqncNmbr = strCurrSqncNmbr;

				strTelno = expressRs1.getString("TELNO");
				// Fix to skip 'bad' TNs
				if (strTelno.length() < 12)
				{
					System.out.println("\n Telno is bad ["+ strTelno +"] skipping this one....");
					continue;
				}
				strTelno = strTelno.substring(0,3) + strTelno.substring(4,7) + strTelno.substring(8);
				strCurrSttsCd = expressRs1.getString("TCKT_STTS_CD");
				//strCreateDt = expressRs1.getString("CREATE_DATE");
                   
                                String noofDays = expressRs1.getString("DAYS");

                                /*
                                c1.set(Integer.parseInt(strCreateDt.substring(4, 8)), Integer.parseInt(strCreateDt.substring(2, 4)), Integer.parseInt(strCreateDt.substring(0, 2)), Integer.parseInt(strCreateDt.substring(8, 10)),Integer.parseInt(strCreateDt.substring(10)));
                                expressRs4 = expressStmt3.executeQuery("SELECT TO_CHAR(sysdate, 'MMDDYYYYHH24MI') CURR_DT FROM dual");
				expressRs4.next();
				String sysDt = expressRs4.getString("CURR_DT");
				expressRs4.close();
                                c2.set(Integer.parseInt(sysDt.substring(4, 8)), Integer.parseInt(sysDt.substring(2, 4)), Integer.parseInt(sysDt.substring(0, 2)), Integer.parseInt(sysDt.substring(8, 10)),Integer.parseInt(sysDt.substring(10)));
                                long noOfDays = daysBetween(c1.getTime(), c2.getTime());
                                String noofDays = Long.toString(noOfDays); */
                                
                                System.out.println("strTelNo : "+strTelno);
                                System.out.println("noofDays : "+noofDays);
                                
                                GetInputMap1 InputMap = new GetInputMap1(strTelno,noofDays); 
                                
                                try {
                                    
                                    System.out.println("About to invoke Remedy webservice...");
                                    OutputMap = webserport.express_Search_By_PhoneNumber(InputMap, auth);
                                } catch (Exception e) {
                                    System.out.println("Exception caught :"+e.getMessage());
                                    e.printStackTrace();
                                    
                                    if(e.getMessage().indexOf("Entry does not exist in database;") > 0) {
                                        System.out.println("SoapFaultException caught. Proceeding to get status for next ticket....");
                                        continue;
                                    }
                                }

                                 if (OutputMap!=null)
                                {
                                     System.out.println( "*********OutputMap******" + OutputMap.toString() );
                                
                                     //the name of this variable could be misleading. We are no longer talking to 
                                     //WFM to get the status of ticket; we are getting from remedy hereafter 
                                     //Antony - 06/06/2012
                                     
                                     strWfmSttsCd =  ExpressUtil.fixNullStr(OutputMap.getStatus().toString());
                                     strWfmClassItem = ExpressUtil.fixNullStr(OutputMap.getClass_Item().toString());
                                     strWfmFault = ExpressUtil.fixNullStr(OutputMap.getFault_Code().toString());
                                     strWfmCause = ExpressUtil.fixNullStr(OutputMap.getCause_Code().toString());
                                }
                                 else
                                {	//Error Occurred

                                 System.out.println("***** THIS IS AN ERROR SITUATION !!.  ");

                                 }
                                // Webservice call End -- added by Vijay - 05-04-12
                              
                                // Commented Obtaining WFM STATUS CODE using Stored procedure, and webservice -- Vijay - 19-04-12

				// Obtain WFM Status Code
			/*	wfmSPStmt.setString(1, strTelno);
				wfmSPStmt.setString(2, strCreateDt);

				wfmRs1 = wfmSPStmt.executeQuery();

				if (! wfmRs1.next())
				{
					System.out.println("Error executing stored procedure");
				}

				// Check the return code for this call
				int iReturnCode = wfmRs1.getInt(1);
				if (iReturnCode != 0)
				{
					System.out.println("Error from WFM on TelNo: " + strTelno + " CreateDate ["+ strCreateDt + "] ret code="+iReturnCode);
					continue;
				}

				strWfmSttsCd = wfmRs1.getString(2);
				strWfmClassItem = wfmRs1.getString(3);
				strWfmFault = wfmRs1.getString(4);
				strWfmCause = wfmRs1.getString(5); */

				// Map it to Express Stts Cd
                                String strNewSttsCd = strWfmSttsCd.toUpperCase();
                                
                                
                                /*
                                 *No need to do this mapping anymore as we get the remedy status directly which is equal to the Express
                                 *trouble ticket status - Antony - 06/06/2012
                                 *
                                 *
                                 *
				wfmXrefStmt.setString(1, strWfmSttsCd); 
				expressRs2 = wfmXrefStmt.executeQuery();
				if (! expressRs2.next())
				{
					System.out.println("No matching Express status for WFM code: " + strWfmSttsCd);
					continue;
				}

				String strNewSttsCd = expressRs2.getString("TCKT_STTS_CD");

                                 */
                                
                                
                                System.out.println("strWfmSttsCd :"+strWfmSttsCd);
                                
                                System.out.println("strCurrSttsCd :"+strCurrSttsCd);
                                
				if (! strCurrSttsCd.equals(strNewSttsCd))
				{
					// Update Ticket_T and Ticket_History_T
					System.out.println("Update Ticket Number: " + strCurrSqncNmbr + "  to status: " + strNewSttsCd);

					try
					{
						// Obtain the current system date
						expressRs3 = expressStmt2.executeQuery("SELECT TO_CHAR(sysdate, 'MM/DD/YYYY HH24:MI:SS') CURR_DT FROM dual");
						expressRs3.next();
						String strCurrDt = expressRs3.getString("CURR_DT");
						expressRs3.close();

						// Update Current History Row
						String strUpdHst = "UPDATE TICKET_HISTORY_T SET TCKT_STTS_CD_OUT = '" + strNewSttsCd + "', TCKT_HSTRY_DT_OUT = TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), MDFD_DT = TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), MDFD_USERID = '" + strUserid + "' WHERE TCKT_HSTRY_SQNC_NMBR = " + expressRs1.getInt("TCKT_HSTRY_SQNC_NMBR");

						if (expressStmt2.executeUpdate(strUpdHst) != 1)
						{
							System.out.println("The following UPDATE failed: " + strUpdHst);
							throw new Exception();
						}

						// Get next History Sequence Number
						String strHstQry = "SELECT TICKET_HISTORY_SEQ.nextval TCKT_HSTRY_SQNC_NMBR_NEW FROM dual";
						expressRs3 = expressStmt2.executeQuery(strHstQry);
						expressRs3.next();
						int iTcktHstrySqncNmbrNew = expressRs3.getInt("TCKT_HSTRY_SQNC_NMBR_NEW");
						expressRs3.close();

						// Insert New History Row
						String strInsHst = "INSERT INTO TICKET_HISTORY_T VALUES(" + iTcktHstrySqncNmbrNew + ",0," + strCurrSqncNmbr + ",'" + strNewSttsCd + "', 'N/A', TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), '" + strUserid + "')" ;

						if (expressStmt2.executeUpdate(strInsHst) != 1)
						{
							System.out.println("The following INSERT failed: " + strInsHst);
							throw new Exception();
						}

						// Update TICKET_T
						String strUpdTckt = "UPDATE TICKET_T SET TCKT_STTS_CD = '" + strNewSttsCd + "', TCKT_HSTRY_SQNC_NMBR = " + iTcktHstrySqncNmbrNew + ", MDFD_DT = TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), MDFD_USERID = '" + strUserid + "' WHERE TCKT_SQNC_NMBR = " + strCurrSqncNmbr;

						if (expressStmt2.executeUpdate(strUpdTckt) != 1)
						{
							System.out.println("The following UPDATE failed: " + strUpdTckt);
							throw new Exception();
						}

						// Update Resolution Text for ticket
						String strResTxt = "";
						if (strWfmClassItem != null)
						{
							strResTxt = strWfmClassItem.trim() + " / " + strWfmFault.trim() + " / " + strWfmCause.trim();
							String strUpdResTxt = "UPDATE TCKT_RSPNS_T SET TCKT_RSPNS_NOTES = '" + strResTxt + "', MDFD_DT = TO_DATE('" + strCurrDt + "','MM/DD/YYYY HH24:MI:SS'), MDFD_USERID = '" + strUserid + "' WHERE TCKT_SQNC_NMBR = " + strCurrSqncNmbr;

							if (expressStmt2.executeUpdate(strUpdResTxt) != 1)
							{
								System.out.println("The following UPDATE failed: " + strUpdTckt);
								throw new Exception();
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
						expressCon.rollback();
						continue;
					}
					expressCon.commit();
				}
			} //while()

			// Clean up and Close
			expressStmt1.close();
			expressStmt1 = null;
			expressStmt2.close();
			expressStmt2 = null;
	//		wfmSPStmt.close();
	//		wfmSPStmt = null;
			expressRs1.close();
			expressRs1 = null;
			//expressRs2.close();
			//expressRs2 = null;
	//		wfmRs1.close();
	//		wfmRs1 = null;
			expressCon.close();
	//		wfmCon.close();
		}
		catch (Exception e)
		{
			try
			{
                                System.out.println("Exception caught: "+e.getMessage());
				expressCon.rollback();
				expressCon.close();
	//			wfmCon.rollback();
	//			wfmCon.close();
			}
			catch(Exception se)
			{
				se.printStackTrace();
				return;
			}
			e.printStackTrace();
			return;
		} finally {
                        
                        System.out.println("AutoTicketSttsChng: Inside finally block. Releasing conn object if not null..");
                        
                        try {
                            if (expressCon != null && !expressCon.isClosed()) {
                                System.out.println("AutoTicketSttsChng: Inside finally if block. Connection not closed.Releasing conn object.");
                                expressCon.close();
                            }
                        } catch (Exception sqle) {
                            System.out.println("Exception caught: "+sqle.getMessage());
                            return;
			}
                }
		return;
	}
         static final long ONE_HOUR = 60 * 60 * 1000;

  public static long daysBetween(Date d1, Date d2) {
    return  (((d2.getTime() - d1.getTime() + ONE_HOUR) / (ONE_HOUR * 24))+1);
  }
}
