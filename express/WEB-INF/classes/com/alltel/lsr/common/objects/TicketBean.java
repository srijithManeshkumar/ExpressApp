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
 * MODULE:	TicketBean.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/20/2002  initial check-in.
 *	10/15/2002  psedlak History update chgd (hdr 165254)
 *
 *      11/21/2002 shussaini Change Request Navigation.(hd 200039)
 *                  Added m_strSttsCdFrom,  m_strTypInd, m_strRqstTypCd
 *                  and m_strSttsCdTo with its get Methods.
 *                  Change the sql to retrieve following columns
 *                  STTS_CD_FROM,TYP_IND, RQST_TYP_CD, ACTN from Action_T
 *	09/19/2003 psedlak use generic
 */

package com.alltel.lsr.common.objects;

import java.io.*;
import java.text.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class TicketBean extends ExpressBean
{

	//These dictate what to pull from tables
        private TicketOrder thisOrder = TicketOrder.getInstance();

	public TicketBean() {
	
		super.init(thisOrder);
                Log.write(Log.DEBUG_VERBOSE, "TicketBean: constructor");
	}

	public int create(int iUNUSED) {
		return -1;
	}

	public int create(int iOCNSttSqncNmbr, int iCmpnySqncNmbr) {

		Log.write(Log.DEBUG_VERBOSE, "TicketBean : Create New Ticket");

		int iReturnCode = 0;

		String strOCNCd = "";
		String strSttCd = "";
//		int iCmpnySqncNmbr = 0;

		String strQuery1 = "SELECT OCN_STATE_T.OCN_CD, OCN_STATE_T.STT_CD FROM OCN_STATE_T, OCN_T WHERE OCN_STT_SQNC_NMBR = " + iOCNSttSqncNmbr + " AND OCN_STATE_T.OCN_CD = OCN_T.OCN_CD AND OCN_T.CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr;

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
				Log.write(Log.ERROR, "TicketBean : Error finding valid OCN Code and State ");
				iReturnCode = -110;		
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "TicketBean : DB Exception on Query : " + strQuery1);
			iReturnCode = -100;		
		}
Log.write("TicketBean - create CMPNY_SQNC_NMBR = " + iCmpnySqncNmbr);
		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}

		// Get the new Ticket Number
		int iTcktSqncNmbr = 0;
		String strQueryTSN = "SELECT TICKET_SEQ.nextval TCKT_SQNC_NMBR_NEW FROM dual";

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
			Log.write(Log.ERROR, "TicketBean : DB Exception on Query : " + strQueryTSN);
			iReturnCode = -100;		
		}

		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}

		// Insert new row into TICKET_T
		String strInsert1 = "";
		try 
		{
			strInsert1 = "INSERT INTO TICKET_T VALUES(" + iTcktSqncNmbr + ",0, 'INITIAL', 0, '" + strOCNCd + "', '" +
				strSttCd + "', " + iOCNSttSqncNmbr + ", " + iCmpnySqncNmbr + ", ' ', '" + getUserid() + "', ' '," +
				getTimeStamp() + ", '" + getUserid() + "', '" + thisOrder.getSRVC_TYP_CD() + "')" ;
			m_stmt.executeUpdate(strInsert1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "TicketBean : DB Exception on Insert : " + strInsert1);
			iReturnCode = -100;		
		}

		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}

		Log.write(Log.DEBUG_VERBOSE, "TicketBean : Successful Insert of New Ticket");

		// generate a new History record 
		int iTcktHstrySqncNmbr = updateHistory(iTcktSqncNmbr, 0, "INITIAL");
		if (iTcktHstrySqncNmbr <= 0)
		{
			Log.write(Log.ERROR, "TicketBean : Error Generating History for Ticket Sqnc Nmbr:" + iTcktSqncNmbr);
			return(-125);		
		}

		String strUpdate1 = "UPDATE TICKET_T SET TCKT_HSTRY_SQNC_NMBR = " + iTcktHstrySqncNmbr + " WHERE TCKT_SQNC_NMBR = " + iTcktSqncNmbr;

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
			Log.write(Log.ERROR, "TicketBean : DB Exception on Update : " + strUpdate1);
			iReturnCode = -100;		
		}

		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}

		Log.write(Log.DEBUG_VERBOSE, "TicketBean : TICKET_T updated with current History Sequence Number : " + strUpdate1);


		// if we got here, we have a new Ticket Sequence Number
		// now get the information we need to create all the required forms.
		// We need to loop through SERVICE_TYPE_FORM and create all the INITIAL FORMs 

		String strQuery3 = "SELECT * FROM SERVICE_TYPE_FORM_T WHERE SRVC_TYP_CD = '2' AND TYP_IND = 'T'";
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
					Log.write(Log.ERROR, "TicketBean : Error Generating Form for Ticket Sqnc Nmbr:" + iTcktSqncNmbr + " ; Form Sqnc Nmbr = " + iFrmSqncNmbr);
					iReturnCode = -130;		
				}

			}
			if ((i_frms_created == 0) || (i_frms_created != i_frms))
			{
				Log.write(Log.ERROR, "TicketBean : Error Generating Forms for Ticket Sqnc Nmbr:" + iTcktSqncNmbr);
				iReturnCode = -135;		
			}

			rs3.close();
		} 
		catch(SQLException e) 
		{
			e.printStackTrace();
			rollbackTransaction();
			DatabaseManager.releaseConnection(m_conn);
			Log.write(Log.ERROR, "TicketBean :  ERROR PERFORMING DATABASE ACTIVITY FOR NEW TICKET FORM CREATION ");
			iReturnCode = -100;		
		}

		if (iReturnCode != 0)
		{ 
			return (iReturnCode);
		}
		
		Log.write(Log.DEBUG_VERBOSE, "TicketBean : All INITIAL Forms Generated for Ticket Sqnc Nmbr:" + iTcktSqncNmbr); 

		// return the new Ticket Sequence Number
		return(iTcktSqncNmbr); 

	}
	
        // Send the autoReply if necessary
        protected void sendReply(int iSqncNmbr, int iVrsn, String strUserID)
        {
        }
 
        // Send the provider autoReply if necessary
        protected void sendProvReply(int iSqncNmbr, int iVrsn)
        {
        }

}
