/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *					BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:	DsTicketCtlr.java
 * DESCRIPTION: 
 * AUTHOR:      
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	03/20/2004  Initial 
 *	7/19/2004 pjs Get Siebel ticket # real-time (had to chg order...now push to Siebel, then do changeStatus()
 *	7/28/2004 pjs Fix to above...had to run Validate() before pushin' xml
 *
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class DsTicketCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{	

		String strURL;

        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr()");

		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		String strUSERID = sdm.getUser();
		String	strCmpnyTyp =  sdm.getLoginProfileBean().getUserBean().getCmpnyTyp();
		String	strCmpnySqncNmbr =  sdm.getLoginProfileBean().getUserBean().getCmpnySqncNmbr();
		int  	iCmpnySqncNmbr = 0;
		if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0))
		{}
		else {
			iCmpnySqncNmbr =  Integer.parseInt(strCmpnySqncNmbr);
		}
			
Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- User: "+ strUSERID + " Company: " + iCmpnySqncNmbr);

		String m_strTcktCreate = request.getParameter("tcktcreate");
		if ((m_strTcktCreate == null) || (m_strTcktCreate.length() == 0))
		{}
		else
		//Coming from DsTicketListView;
		//validate security - user needs to have security to create a ticket
		// SELECT DISTINCT SCRTY_OBJCT_CD from ACTION_T WHERE STTS_CD_FROM = "CREATE"
		// If the user has one of the SCRTY_OBJCT codes in their profile they can go to the create view
		//Send to DsTicket Create View to enter required fields for entering a new ticket
		{
			Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktcreate");
			request.getHttpRequest().setAttribute("NEWRECORD", "1");
			alltelRequestDispatcher.forward("/DsTicketCreateView.jsp");  
			return;
		}

		String m_strTcktNew = request.getParameter("tcktnew");
		if ((m_strTcktNew == null) || (m_strTcktNew.length() == 0))
		{}
		else
		{
			if (m_strTcktNew.equals("Cancel"))
			{

				Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktnew - User Cancelled");

				// User selected cancel ; send back to DsTicket List 
				alltelRequestDispatcher.forward("/DsTicketListCtlr"); 
				return;

			}

			Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktnew - Submit");

			//Coming from the CreateTicketView;
			//validate all fields from CreateTicketView needed to create a new Ticket;	

			String strTcktNewErrMsg = "";

			String strOCNCd = request.getParameter("tcktnew_ocncd");
			if ((strOCNCd == null) || (strOCNCd.length() == 0))
			{
		        	Log.write(Log.ERROR, "DsTicketCtlr() --- Error: A Valid Community/Cmpny must be Selected");

				strTcktNewErrMsg = "Error: A Valid Community must be Selected";
				request.getHttpRequest().setAttribute("tcktnew_errormsg", strTcktNewErrMsg);

				alltelRequestDispatcher.forward("/DsTicketCreateView.jsp");  
				return;
			}

			//note newTcktBean will go out of scope....
			DsTicketBean newTcktBean = new DsTicketBean();
			newTcktBean.setUserid(strUSERID);
	
			//perform new Ticket Setup in database ;
			int iTcktSqncNmbr = 0;

			int iReturnCode = newTcktBean.getConnection();
			if (iReturnCode == 0) 
			{ 	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- got connection");
				iReturnCode = newTcktBean.beginTransaction();
				if (iReturnCode == 0) {
					Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- started transaction");
					iTcktSqncNmbr = newTcktBean.create(strOCNCd, iCmpnySqncNmbr);
					if (iTcktSqncNmbr > 0) {
						Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- created ticket");
						iReturnCode = newTcktBean.commitTransaction();
						if (iReturnCode == 0) {
							Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- committed trans");
							iReturnCode = newTcktBean.closeConnection();
						}
					}
					else if (iTcktSqncNmbr == newTcktBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
					else
					{
						iReturnCode = iTcktSqncNmbr;
					}
				}
			}

			if (iReturnCode < 0)
			{
				// there was a problem getting a new Ticket
				// figure out what the error msg should be and send user back to Ticket Create View

		        	Log.write(Log.ERROR, "DsTicketCtlr() --- Error in createTicket() encountered - unable to create ticket. ReturnCode = " + iReturnCode);

				// for now the duplicate PON message is the only worthwhile message to send back.
				// we should provide a better description of the Error using return code 
				// descriptions from DsTicketBean.

				if (iReturnCode == newTcktBean.DUP_PON)
				{
					strTcktNewErrMsg = "Error : The Trouble Ticket Number already exists!";
				}
				else
				{
					strTcktNewErrMsg = "Error : Unable to Create the Ticket. An Application Error was encountered trying to Create the Ticket!";
				}			
				
				request.getHttpRequest().setAttribute("tcktnew_errormsg", strTcktNewErrMsg);

				alltelRequestDispatcher.forward("/DsTicketCreateView.jsp");  
				return;
			}

			//show Forms using the 1st form in SERVICE_TYPE_FORM as the default FORM to display
			request.getHttpRequest().setAttribute("tcktformtype","_FIRST_");
			request.getHttpRequest().setAttribute("TCKT_SQNC_NMBR", Integer.toString(iTcktSqncNmbr));

			alltelRequestDispatcher.forward("/DsTicketFormView.jsp");  
			return;
		}

		String m_strTcktSqncNmbr = request.getParameter("TCKT_SQNC_NMBR");
		String m_strTcktVrsn = request.getParameter("TCKT_VRSN");
		String m_strFrmSqncNmbr = request.getParameter("FRM_SQNC_NMBR");

        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- TCKT_SQNC_NMBR = " + m_strTcktSqncNmbr);
        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- TCKT_VRSN = " + m_strTcktVrsn);
        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- FRM_SQNC_NMBR = " + m_strFrmSqncNmbr);

		String m_strTcktGet = request.getParameter("seqget");
		if ((m_strTcktGet == null) || (m_strTcktGet.length() == 0))
		{}
		else
		//Coming from the DsTicketListView or DsTicketHistoryView where get functions are allowed ;
		//validate user has security to view a ticket;
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- seqget : TcktSqncNmbr = " + m_strTcktGet + " ; TcktVrsn = " + m_strTcktVrsn);

			request.getHttpRequest().setAttribute("tcktformtype","_FIRST_");
			request.getHttpRequest().setAttribute("TCKT_SQNC_NMBR", m_strTcktGet);
			request.getHttpRequest().setAttribute("TCKT_VRSN", m_strTcktVrsn);
			
	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktformtype = _FIRST_");

			alltelRequestDispatcher.forward("/DsTicketFormView.jsp");
			return;
		}

		request.getHttpRequest().setAttribute("TCKT_SQNC_NMBR", m_strTcktSqncNmbr);
		request.getHttpRequest().setAttribute("TCKT_VRSN", m_strTcktVrsn);

		int m_iTcktSqncNmbr = 0;
		if ((m_strTcktSqncNmbr == null) || (m_strTcktSqncNmbr.length() == 0))
		{}
		else
		{
			m_iTcktSqncNmbr = Integer.parseInt(m_strTcktSqncNmbr); 
		}

		int m_iTcktVrsn = 0;
		if ((m_strTcktVrsn == null) || (m_strTcktVrsn.length() == 0))
		{}
		else
		{
			m_iTcktVrsn = Integer.parseInt(m_strTcktVrsn);
		}
	
		int	m_iFrmSqncNmbr = 0;
		if ((m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0))
		{}
		else
		{
			m_iFrmSqncNmbr = Integer.parseInt(m_strFrmSqncNmbr); 
		}

		String m_strTcktNotesUpdate = request.getParameter("notes_update");
		if ((m_strTcktNotesUpdate == null) || (m_strTcktNotesUpdate.length() == 0))
		{}
		else
		{
			//note noteTcktBean will go out of scope....
			DsTicketBean noteTcktBean = new DsTicketBean();
			noteTcktBean.setUserid(strUSERID);

			String m_strTcktNotesText = request.getParameter("notestext");
			if ((m_strTcktNotesText == null) || (m_strTcktNotesText.length() == 0))
			{
				m_strTcktNotesText = "";
			}

			int iReturnCode = noteTcktBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = noteTcktBean.beginTransaction();
				if (iReturnCode == 0) {
					iReturnCode = noteTcktBean.updateNotes(m_iTcktSqncNmbr, m_strTcktNotesText);
					if (iReturnCode == 0) {
						iReturnCode = noteTcktBean.commitTransaction();
						if (iReturnCode == 0) {
							iReturnCode = noteTcktBean.closeConnection();
						}
					}
					else if (iReturnCode == noteTcktBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Save Notes Successful");
				// handle telling the user we saved the notes successfully
				request.getHttpRequest().setAttribute("notes", "Notes");
				alltelRequestDispatcher.forward("/DsTicketNotesView.jsp"); 
				return;
			}
			else
			{
			    	Log.write(Log.ERROR, "DsTicketCtlr() --- Error Saving Notes");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DsTicketErrorView.jsp"); 
				return;
			}
		}

		String m_strTcktPrint = request.getParameter("print");
		if ((m_strTcktPrint == null) || (m_strTcktPrint.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//TCKT_SQNC_NMBR needs to be valid parameter;
		//TCKT_VRSN needs to be valid parameter;
		//build and display the Print View for the Ticket
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- print = " + m_strTcktPrint);

			request.getHttpRequest().setAttribute("print", m_strTcktPrint);
			alltelRequestDispatcher.forward("/DsTicketFieldPrintView.jsp"); 
			return;
		}

		String m_strTcktPrint2 = request.getParameter("print2");
		if ((m_strTcktPrint2 == null) || (m_strTcktPrint2.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//TCKT_SQNC_NMBR needs to be valid parameter;
		//TCKT_VRSN needs to be valid parameter;
		//build and display the Print View for the Ticket
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- print2 = " + m_strTcktPrint2);

			request.getHttpRequest().setAttribute("print2", m_strTcktPrint2);
			// If you want to show all fields
				request.getHttpRequest().setAttribute("tcktformtype","_FIRST_");

			alltelRequestDispatcher.forward("/DsTicketFormPrintView.jsp"); 
			return;
		}

		//  All actions past this point may first require saving the current data
		//  if we have valid key fields from a FORM with posted data and user had
		//  authority to update data on the form.
		boolean bTransactionExists = false;
		String m_strFrmAuth = request.getParameter("FRM_AUTHORIZATION");
		String m_strMdfdDt = request.getParameter("mdfddt");
		Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() ---m_strMdfdDt= " + m_strMdfdDt);
			
		PostEvent postEvt = new PostEvent();
		String strTMSUrl = PropertiesManager.getProperty("lsr.TMS.URL", "");
		postEvt.setURL(strTMSUrl);

		DsTicketBean tcktBean = new DsTicketBean();
		tcktBean.setUserid(strUSERID);

		if ((m_strTcktSqncNmbr == null) || (m_strTcktSqncNmbr.length() == 0)
			|| (m_strTcktVrsn == null) || (m_strTcktVrsn.length() == 0)
			|| (m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0)
			|| (m_strFrmAuth == null) || (m_strFrmAuth.length() == 0)
			|| (m_strMdfdDt == null) || (m_strMdfdDt.length() == 0))

		{
			Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() ---stuff is null");
 		}
		else
		{
			// build form field vector and call method to save data here

			Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Save FORM data");

			int iReturnCode = tcktBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = tcktBean.beginTransaction(m_iTcktSqncNmbr, m_strMdfdDt);
				if (iReturnCode == 0) {
					iReturnCode = tcktBean.storeForm(request, m_iFrmSqncNmbr, m_iTcktSqncNmbr, m_iTcktVrsn);
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Save Form Successful");
				// continue on since there may be more to process in this ticket
				bTransactionExists = true;
			}
			else if (iReturnCode == tcktBean.SECURITY_ERROR)
			{
				alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DsTicketCtlr() --- Error Saving Form");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DsTicketErrorView.jsp"); 
				return;
			}
		}

		String m_strTcktForm = request.getParameter("tcktform");
		if ((m_strTcktForm == null) || (m_strTcktForm.length() == 0))
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktform = null");
		}
		else
		//Coming from the TicketView - this action is navigation between Forms
		//"tcktform" needs to be a valid form sequence number
		//TCKT_SQNC_NMBR and TCKT_VRSN need to be valid parameters;
		//validate user has security to view a ticket;
		//the value of tcktform is the FORM SQNC they want to look at; go get it and display the form
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktform = " + m_strTcktForm);
	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktformtype = _FRM_CD_");

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = tcktBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = tcktBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("tcktformtype","_FRM_CD_");
				request.getHttpRequest().setAttribute("tcktform", m_strTcktForm);
				alltelRequestDispatcher.forward("/DsTicketFormView.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DsTicketCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DsTicketErrorView.jsp"); 
				return;
			}
		}
		
		String m_strTcktHist = request.getParameter("hist");
		if ((m_strTcktHist == null) || (m_strTcktHist.length() == 0))
		{}
		else
		//This action intented to be navigation to view History
		//TCKT_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view a ticket;
		//build and display the History View for the Ticket
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- hist = " + m_strTcktHist);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = tcktBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = tcktBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("hist", m_strTcktHist);
				alltelRequestDispatcher.forward("/DsTicketHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DsTicketCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DsTicketErrorView.jsp"); 
				return;
			}
		}
	
		String strDetailHistSqncNmbr = request.getParameter("dtlhist");
		if ((strDetailHistSqncNmbr== null) || (strDetailHistSqncNmbr.length() == 0))
		{}
		else
		// User wants to view a detailed view of history changes
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- view histroy detail "+ strDetailHistSqncNmbr);
			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = tcktBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = tcktBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("histseq", strDetailHistSqncNmbr);
				alltelRequestDispatcher.forward("/DsTicketDtlHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DsTicketCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DsTicketErrorView.jsp"); 
				return;
			}
		}

		String m_strTcktNotes = request.getParameter("notes");
		if ((m_strTcktNotes == null) || (m_strTcktNotes.length() == 0))
		{}
		else
		//TCKT_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view ticket NOTES;
		//build and display the Notes View for the Ticket
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- notes = " + m_strTcktNotes);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = tcktBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = tcktBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("notes", m_strTcktNotes);
				alltelRequestDispatcher.forward("/DsTicketNotesView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DsTicketCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DsTicketErrorView.jsp"); 
				return;
			}
		}

		String m_strTcktAction = request.getParameter("action");
		if ((m_strTcktAction == null) || (m_strTcktAction.length() == 0))
		{}
		else
		//This action intented to be a status change
		//or a ticket to SAVE the current form data or a ticket to view Validation ERRORS.
		//TCKT_SQNC_NMBR needs to be a valid parameter;
		//TCKT_VRSN will always be the current version;
		//validate the action can be performed based on security and then take appropriate action
		//when done go back to DsTicketListCtlr if it's a valid Status change.
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- action = " + m_strTcktAction);
			request.getHttpRequest().setAttribute("action", m_strTcktAction);
			String strRemedyHD = "";

			if ( m_strTcktAction.equals("Save"))
			{
				// we already saved the data prior to getting here ; just set some fields & get out
				request.getHttpRequest().setAttribute("TCKT_VRSN", m_strTcktVrsn);
				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("tcktformtype","_FRM_SQNC_");

				int iReturnCode = 0;
				if (bTransactionExists)
				{
					iReturnCode = tcktBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = tcktBean.closeConnection();
					}
				}
				if (iReturnCode == 0)
				{
					Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktformtype = _FRM_SQNC_");
					alltelRequestDispatcher.forward("/DsTicketFormView.jsp");  
					return;
				}
				else
				{
					 Log.write(Log.ERROR, "DsTicketCtlr() --- Transaction Error");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DsTicketErrorView.jsp"); 
					return;
				}
			}			
		
			//NOTE: "Validate" is not available in DsTickets right now....
			if ( m_strTcktAction.equals("Validate"))
			{
				int iReturnCode = 0; 
				int iVldtnErrs = 0;
				if (bTransactionExists == true)
				{
					iReturnCode = tcktBean.commitTransaction();
				}
				else
				{
					iReturnCode = tcktBean.getConnection();
				}

				if (iReturnCode == 0) 
				{
					iVldtnErrs = tcktBean.validateFields(request, m_iTcktSqncNmbr, m_iTcktVrsn, "A", "");
				}
						
				iReturnCode = tcktBean.closeConnection();
				
				alltelRequestDispatcher.forward("/DsTicketValidationView.jsp");  
				return;

			} //end 'Validate'

			if ( m_strTcktAction.equals("Submit"))
			{
				// we already saved the data prior to getting here ; just set some fields & get out
				request.getHttpRequest().setAttribute("TCKT_VRSN", m_strTcktVrsn);
				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("tcktformtype","_FRM_SQNC_");
				int iReturnCode = 0;
				if (!bTransactionExists)
				{   
					Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- 'Submit' processing Trans Does not exist");    
					iReturnCode = tcktBean.getConnection();
					if (iReturnCode == 0) {
						iReturnCode = tcktBean.beginTransaction();
					}
				}
				else
				{
					iReturnCode = tcktBean.commitTransaction();
				}
				
				if (iReturnCode == tcktBean.SECURITY_ERROR)
				{
					alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
					return;
				}
				else if (iReturnCode == tcktBean.VALIDATION_ERROR)
				{
					Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Validation Errors with Form Fields - Sending to Error Validation View");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DsTicketValidationView.jsp");  
					return;
				}
				
				if (iReturnCode == 0)
				{
					// continue	
				}
				else
				{
					 Log.write(Log.ERROR, "DsTicketCtlr() --- Transaction Error while processing DsTicket");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DsTicketErrorView.jsp"); 
					return;
				}
            } //end 'Submit'

			//Handle all other actions based on ACTION_T workflow
			Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Ticket Status Change");

			int iReturnCode = 0; 
			if (bTransactionExists == false)
			{
				iReturnCode = tcktBean.getConnection();
				if (iReturnCode == 0) {
					iReturnCode = tcktBean.beginTransaction();
				}
			}
			else
			{
				iReturnCode = tcktBean.commitTransaction();
			}
			String strExternalTicketNumber = "";

			if (iReturnCode == 0) 
			{
				//7-19 Moved XML push here, so we can capture real-time ticket # and include in Email and popup
				//Successfully completed - Ship off xml if OK
				if ( m_strTcktAction.equals("Submit") ) 
				{
					//First Validate...we actually will validate again in changeStatus(), but that's OK. We need to
					//validate here as to not push XML unless valid data
					iReturnCode = tcktBean.validateFields(request, m_iTcktSqncNmbr, m_iTcktVrsn, "A", "");
					if (iReturnCode > 0 )
					{	alltelRequestDispatcher.forward("/DsTicketValidationView.jsp");  
						return;
					}
	
					String strSkipSend = PropertiesManager.getProperty("lsr.TMS.BypassSend", "NO");
					
					if ( strSkipSend.equals("YES") )
					{ 
							iReturnCode = 0;
						Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- ** BYPASS ** sending to TMS");
						strExternalTicketNumber ="BYPASS 1";
						tcktBean.setExternalTicketId( strExternalTicketNumber );
					}
					else
					{	
						//Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- should POST strXMLtoTMS here ");
						//Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- TMS Url="+strTMSUrl);
						//iReturnCode = postEvt.sendXMLRequestGET(strXMLtoTMS);
						  strRemedyHD = ( m_strTcktAction.equals("Submit")? tcktBean.CreateRemedyTicket( m_iTcktSqncNmbr, m_iTcktVrsn): null );

						// Handle error checking here:
						if( strRemedyHD != null) {
							iReturnCode = 0;
						}else{
						iReturnCode = -999;
						}
						
						if (iReturnCode == 0)
						{
							Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Remedy HD is created =[" + strRemedyHD +"]");
							//iReturnCode = tcktBean.interpretResponse( postEvt.getResponseString() );
							if (iReturnCode < 0)
							{
								Log.write(Log.ERROR, "DsTicketCtlr() --- Error found in TMS interface");
								alltelRequestDispatcher.forward("/DsTicketErrorView2.jsp");  
								return;
							}
							else {
								strExternalTicketNumber = strRemedyHD;		

								Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Remedy ticket = " + strExternalTicketNumber);
								if (strExternalTicketNumber == null) {
								}
								else {
									tcktBean.setExternalTicketId( strExternalTicketNumber );
								}
							}	
						}
						else if (iReturnCode < 0)
						{
							Log.write(Log.ERROR, "DsTicketCtlr() --- Error invoking Remedy Web service post");
							alltelRequestDispatcher.forward("/DsTicketErrorView2.jsp");  
							//return;
						}
					}
				}//end of Remedy stuff

				if (iReturnCode >= 0)
				{	iReturnCode = tcktBean.changeStatus(request, m_iTcktSqncNmbr, m_strTcktAction);
					if (iReturnCode > 0) {
						iReturnCode = tcktBean.commitTransaction();
						if (iReturnCode == 0) {
							iReturnCode = tcktBean.closeConnection();
						}
					}
					else if (iReturnCode == tcktBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
				}
			}

			if (iReturnCode >= 0)
			{

				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("tcktformtype","_FRM_SQNC_");

				Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Status Change Successful");
                                // handle telling the user we saved the form successfully
				//Add the code for ActionManager here.
                                String strActnDstntn = null;
                                ActionManager am = new ActionManager();
                                strActnDstntn = am.getActionDestination(tcktBean.getSttsCdFrom(), tcktBean.getTypInd(), tcktBean.getRqstTypCd(), 
									tcktBean.getSttsCdTo(), m_strTcktAction);

				// If Provider submitted ticket, and XREF returned...set some fields....
				if ( (strCmpnyTyp.equals("P")) && (strExternalTicketNumber != null) )
				{	if ( strExternalTicketNumber.length() > 1 )
					{
						Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Setting http request vars for Ticket #");
						request.getHttpRequest().setAttribute("_entry_msg_", 
						      "Trouble Ticket:  " + strExternalTicketNumber + "      created for Express: " + m_iTcktSqncNmbr);
					}
				}

				if (strActnDstntn.length() > 0) {
					Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() -- Next action based on Action_T is: "+strActnDstntn);
					alltelRequestDispatcher.forward("/"+strActnDstntn);  
				} 
				else {	alltelRequestDispatcher.forward("/DsTicketListCtlr");  
				}

				return;
			}
			else
			{
				if (iReturnCode == tcktBean.VALIDATION_ERROR)
				{
					Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Validation Errors with Form Fields - Sending to Error Validation View");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DsTicketValidationView.jsp");  
					return;
				}
				else
				{
					Log.write(Log.ERROR, "DsTicketCtlr() --- Error Changing Ticket Status");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DsTicketErrorView.jsp");  
					return;
				}
			}
		}

		//Now check to see if we got here with a ticket to add a section for a FORM.
		//Required parameters are TCKT_SQNC_NMBR, TCKT_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair describing the section to add.
		//The parameter name will be "add_sctn_?" and the value will be "Add Section".
		String m_strTcktAddSctn = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
			m_strTcktAddSctn = request.getParameter("add_sctn_" + m_iFrmSctnSqncNmbr);

			if ((m_strTcktAddSctn == null) || (m_strTcktAddSctn.length() == 0))
			{}
			else
			{
				if (m_strTcktAddSctn.equals("Add Section"))
				{
					//we know now the user submitted a valid ticket to add a new section
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- add_sctn");

					int m_iFrmSctnOccNew = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = tcktBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = tcktBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						m_iFrmSctnOccNew = tcktBean.generateSection(m_iFrmSqncNmbr, m_iTcktSqncNmbr, m_iTcktVrsn, m_iFrmSctnSqncNmbr);
						if (m_iFrmSctnOccNew > 0) {
							iReturnCode = tcktBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = tcktBean.closeConnection();
							}
						}
						else if (m_iFrmSctnOccNew == tcktBean.SECURITY_ERROR)
						{
							alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
							return;
						}
						else {
							iReturnCode = m_iFrmSctnOccNew;
						}
					}
					if (iReturnCode >= 0)
					{
						request.getHttpRequest().setAttribute("TCKT_VRSN", m_strTcktVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("tcktformtype","_FRM_SQNC_");

				        Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- m_iFrmSctnOccNew = " + m_iFrmSctnOccNew);
						//Here we need this section we added-cause we want the cursor
						// in the first field of it when the form is presented.
						request.getHttpRequest().setAttribute("NEW_OCC", Integer.toString(m_iFrmSctnOccNew));
						request.getHttpRequest().setAttribute("NEW_SECTION", Integer.toString(m_iFrmSctnSqncNmbr));
				        Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/DsTicketFormView.jsp"); 
						return;

					}
					else
					{
				        	Log.write(Log.ERROR, "DsTicketCtlr() --- Error getting a new section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/DsTicketErrorView.jsp");  
						return;
					}
				}
			}
		}

		//Now check to see if we got here with a ticket to delete a section for a FORM.
		//Required parameters are TCKT_SQNC_NMBR, TCKT_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair identifying the
		// the SECTION SEQUENCE number and the SECTION OCCURRENCE that needs to be deleted.
		//The parameter name will be "del_sctn_?_?" and the value will be "Delete Section".
		String m_strTcktDltSctnOcc = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
		  for (int m_iSctnOcc = 1; m_iSctnOcc < 100 ; m_iSctnOcc++)
		  {	
			m_strTcktDltSctnOcc = request.getParameter("del_sctn_" + m_iFrmSctnSqncNmbr + "_" + m_iSctnOcc);

			if ((m_strTcktDltSctnOcc == null) || (m_strTcktDltSctnOcc.length() == 0))
			{}
			else
			{
				if (m_strTcktDltSctnOcc.equals("Delete Section"))
				{
					//we know now the user submitted a valid ticket to delete a section occurrence
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- del_sctn");
			        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- m_iFrmSctnSqncNmbr = " + m_iFrmSctnSqncNmbr);
			        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- m_iSctnOcc = " + m_iSctnOcc);

					int iFrmSctnDltOcc = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = tcktBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = tcktBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						iFrmSctnDltOcc = tcktBean.deleteSection(m_iFrmSqncNmbr, m_iTcktSqncNmbr, m_iTcktVrsn, m_iFrmSctnSqncNmbr, m_iSctnOcc);						
						if (iFrmSctnDltOcc == 0) {
							iReturnCode = tcktBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = tcktBean.closeConnection();
							}
						}
						else if (iFrmSctnDltOcc ==  tcktBean.SECURITY_ERROR)
						{
							alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
							return;
						}
					}
					if (iReturnCode == 0)
					{
						request.getHttpRequest().setAttribute("TCKT_VRSN", m_strTcktVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("tcktformtype","_FRM_SQNC_");

				        	Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Delete Section Occurrence Successful");
						Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- tcktformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/DsTicketFormView.jsp"); 
						return;
					}
					else
					{
				        	Log.write(Log.ERROR, "DsTicketCtlr() --- Error deleting a section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/DsTicketErrorView.jsp");  
						return;
					}
				}
			}
		  }
		}
				
		// if we dropped down to here, we don't know where we're supposed to go and
		// we apparently have a navigation error.

		Log.write(Log.DEBUG_VERBOSE, "DsTicketCtlr() --- Navigation Error!");

		alltelRequestDispatcher.forward("/NavigationErrorView.jsp");
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}

