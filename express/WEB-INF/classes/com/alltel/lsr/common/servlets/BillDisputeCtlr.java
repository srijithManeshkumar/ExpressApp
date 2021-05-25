/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2003
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:		BillDisputeCtlr.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Vince Pavill
 * 
 * DATE:        03-06-2003
 * 
 * HISTORY:
 *	03/15/2003  Initial Check-in
 *	09/15/2003  psedlak reduce bean creation, use standard errors
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class BillDisputeCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{	

		String strURL;

        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr()");

		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
		String strUSERID = sdm.getUser();
		String  strCmpnySqncNmbr =  sdm.getLoginProfileBean().getUserBean().getCmpnySqncNmbr();
                int     iCmpnySqncNmbr = 0;
                if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0))
                {}
                else {
                        iCmpnySqncNmbr =  Integer.parseInt(strCmpnySqncNmbr);
                }

	
		String m_strDsptCreate = request.getParameter("dsptcreate");
		if ((m_strDsptCreate == null) || (m_strDsptCreate.length() == 0))
		{}
		else
		//Coming from BillDisputeListView;
		//validate security - user needs to have security to create a dispute
		// SELECT DISTINCT SCRTY_OBJCT_CD from DISPUTE_ACTION_T WHERE DSPT_STTS_CD_FROM = "CREATE"
		// If the user has one of the SCRTY_OBJCT codes in their profile they can go to the create view
		//Send to BillDispute Create View to enter required fields for entering a new dispute
		{
			Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dsptcreate");

			alltelRequestDispatcher.forward("/BillDisputeCreateView.jsp");  
			return;
		}

		String m_strDsptNew = request.getParameter("disputenew");
		if ((m_strDsptNew == null) || (m_strDsptNew.length() == 0))
		{}
		else
		{	
			if (m_strDsptNew.equals("Cancel"))
			{

				Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dsptnew - Cancel");

				// User selected cancel ; send back to BillDispute List 
				alltelRequestDispatcher.forward("/BillDisputeListCtlr"); 
				return;

			}
			//Only create bean if we need it
			BillDisputeBean dsptNewBean = new BillDisputeBean();
			dsptNewBean.setUserid(strUSERID);

			Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dsptnew - Submit");

			//Coming from the CreateBillDisputeView;
			//validate security - user needs to have security to create a dispute;
			// SELECT DISTINCT SCRTY_OBJCT_CD from DISPUTE_ACTION_T WHERE DSPT_STTS_CD_FROM = "CREATE"
			//  AND DSPT_TYP_STTS = "status cd from CreateView"
			// If the user has one of the SCRTY_OBJCT codes in their profile they can create the dispute

			//validate all fields from CreateBillDisputeView needed to create a new BillDispute;	

			String strDsptNewErrMsg = "";

			String strOCNSttSqncNmbr = request.getParameter("new_ocnsttsqnc");
			int iOCNSttSqncNmbr = 0;
			if ((strOCNSttSqncNmbr == null) || (strOCNSttSqncNmbr.length() == 0))
			{
		        	Log.write(Log.ERROR, "BillDisputeCtlr() --- Error: A Valid OCN/State combination must be Selected");

				strDsptNewErrMsg = "Error: A Valid OCN/State combination must be Selected";
				request.getHttpRequest().setAttribute("dsptnew_errormsg", strDsptNewErrMsg);

				alltelRequestDispatcher.forward("/BillDisputeCreateView.jsp");  
				return;
			}
			else
			{
				iOCNSttSqncNmbr = Integer.parseInt(strOCNSttSqncNmbr);
			}

			//perform new BillDispute Setup in database ;
			int iDsptSqncNmbr = 0;

			int iReturnCode = dsptNewBean.getConnection();
			if (iReturnCode == 0) { 
				Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- got connection");
				iReturnCode = dsptNewBean.beginTransaction();
				if (iReturnCode == 0) {
					Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- started transaction");
					iDsptSqncNmbr = dsptNewBean.create(iOCNSttSqncNmbr, iCmpnySqncNmbr);
					if (iDsptSqncNmbr > 0) {
						Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- created dispute");
						iReturnCode = dsptNewBean.commitTransaction();
						if (iReturnCode == 0) {
							Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- committed trans");
							iReturnCode = dsptNewBean.closeConnection();
						}
					}
					else if (iDsptSqncNmbr == dsptNewBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
					else
					{
						iReturnCode = iDsptSqncNmbr;
					}
				}
			}

			// Now create multiple detail sections on Dispute form
			if (iReturnCode >= 0)
			{
				iReturnCode = dsptNewBean.getConnection();
				if (iReturnCode == 0)
                                {
                                        iReturnCode = dsptNewBean.beginTransaction();
                                }
				if (iReturnCode == 0)
				{
					//get the number of detail items from properties file...later we can
					//add this to create form if necessary.
					int iNum = PropertiesManager.getIntegerProperty("lsr.dispute.sections", 10);
					Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- create MULTI now ");
					for (int i=2; i <= iNum; i++)
					{
				Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- create occ "+i+ " now");
						// Automatically generate sections
                                                int iRC = dsptNewBean.generateSection(600, iDsptSqncNmbr, 0, 2);
				Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- generate iRC =" + iRC);
                                                if (iRC == dsptNewBean.SECURITY_ERROR)
                                                {
                                                        alltelRequestDispatcher.forward("/LsrSecurity.jsp");
                                                        return;
                                                }
                                                else if (iRC < 0)
                                                {
                                                        iReturnCode = iRC;
                                                        break;
                                                }

                                                // Automatically generate response sections
                                                iRC = dsptNewBean.generateSection(601, iDsptSqncNmbr, 0, 2);
				Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- generate iRC =" + iRC);
                                                if (iRC == dsptNewBean.SECURITY_ERROR)
                                                {
                                                        alltelRequestDispatcher.forward("/LsrSecurity.jsp");
                                                        return;
                                                }
                                                else if (iRC < 0)
                                                {
                                                        iReturnCode = iRC;
                                                        break;
                                                }

					}
					if (iReturnCode == 0) {
						iReturnCode = dsptNewBean.commitTransaction();
						if (iReturnCode == 0) {
							Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- committed trans");
							iReturnCode = dsptNewBean.closeConnection();
						}
					}
				}

			}

			if (iReturnCode < 0)
			{
				// there was a problem getting a new BillDispute
				// figure out what the error msg should be and send user back to BillDispute Create View

		        	Log.write(Log.ERROR, "BillDisputeCtlr() --- Error in createBillDispute() encountered - unable to create dispute. ReturnCode = " + iReturnCode);

				// for now the duplicate PON message is the only worthwhile message to send back.
				// we should provide a better description of the Error using return code 
				// descriptions from BillDisputeBean.

				if (iReturnCode == dsptNewBean.DUP_PON)
				{
					strDsptNewErrMsg = "Error : The Trouble BillDispute Number already exists!";
				}
				else
				{
					strDsptNewErrMsg = "Error : Unable to Create the BillDispute. An Application Error was encountered trying to Create the BillDispute!";
				}			
				
				request.getHttpRequest().setAttribute("dsptnew_errormsg", strDsptNewErrMsg);

				alltelRequestDispatcher.forward("/BillDisputeCreateView.jsp");  
				return;
			}

			//show Forms using the 1st form in SERVICE_TYPE_FORM as the default FORM to display
			request.getHttpRequest().setAttribute("dsptformtype","_FIRST_");
			request.getHttpRequest().setAttribute("DSPT_SQNC_NMBR", Integer.toString(iDsptSqncNmbr));

			alltelRequestDispatcher.forward("/BillDisputeFormView.jsp");  
			return;
		}

		String m_strDsptSqncNmbr = request.getParameter("DSPT_SQNC_NMBR");
		String m_strDsptVrsn = request.getParameter("DSPT_VRSN");
		String m_strFrmSqncNmbr = request.getParameter("FRM_SQNC_NMBR");

        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- DSPT_SQNC_NMBR = " + m_strDsptSqncNmbr);
        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- DSPT_VRSN = " + m_strDsptVrsn);
        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- FRM_SQNC_NMBR = " + m_strFrmSqncNmbr);

		String m_strDsptGet = request.getParameter("seqget");
		if ((m_strDsptGet == null) || (m_strDsptGet.length() == 0))
		{}
		else
		//Coming from the BillDisputeListView or BillDisputeHistoryView where get functions are allowed ;
		//validate user has security to view a dispute;
		{

	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- seqget : DsptSqncNmbr = " + m_strDsptGet + " ; DsptVrsn = " + m_strDsptVrsn);

			request.getHttpRequest().setAttribute("dsptformtype","_FIRST_");
			request.getHttpRequest().setAttribute("DSPT_SQNC_NMBR", m_strDsptGet);
			request.getHttpRequest().setAttribute("DSPT_VRSN", m_strDsptVrsn);
			
	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dsptformtype = _FIRST_");

			alltelRequestDispatcher.forward("/BillDisputeFormView.jsp");
			return;
		}

		request.getHttpRequest().setAttribute("DSPT_SQNC_NMBR", m_strDsptSqncNmbr);
		request.getHttpRequest().setAttribute("DSPT_VRSN", m_strDsptVrsn);

		int m_iDsptSqncNmbr = 0;
		if ((m_strDsptSqncNmbr == null) || (m_strDsptSqncNmbr.length() == 0))
		{}
		else
		{
			m_iDsptSqncNmbr = Integer.parseInt(m_strDsptSqncNmbr); 
		}

		int m_iDsptVrsn = 0;
		if ((m_strDsptVrsn == null) || (m_strDsptVrsn.length() == 0))
		{}
		else
		{
			m_iDsptVrsn = Integer.parseInt(m_strDsptVrsn);
		}
	
		int	m_iFrmSqncNmbr = 0;
		if ((m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0))
		{}
		else
		{
			m_iFrmSqncNmbr = Integer.parseInt(m_strFrmSqncNmbr); 
		}

		String m_strDsptNotesUpdate = request.getParameter("notes_update");
		if ((m_strDsptNotesUpdate == null) || (m_strDsptNotesUpdate.length() == 0))
		{}
		else
		{
			String m_strDsptNotesText = request.getParameter("notestext");
			if ((m_strDsptNotesText == null) || (m_strDsptNotesText.length() == 0))
			{
				m_strDsptNotesText = "";
			}
			//Only create bean if we need it
			BillDisputeBean dsptNotesBean = new BillDisputeBean();
			dsptNotesBean.setUserid(strUSERID);

			int iReturnCode = dsptNotesBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = dsptNotesBean.beginTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dsptNotesBean.updateNotes(m_iDsptSqncNmbr, m_strDsptNotesText);
					if (iReturnCode == 0) {
						iReturnCode = dsptNotesBean.commitTransaction();
						if (iReturnCode == 0) {
							iReturnCode = dsptNotesBean.closeConnection();
						}
					}
					else if (iReturnCode == dsptNotesBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- Save Notes Successful");
				// handle telling the user we saved the notes successfully
				request.getHttpRequest().setAttribute("notes", "Notes");
				alltelRequestDispatcher.forward("/BillDisputeNotesView.jsp"); 
				return;
			}
			else
			{
			    	Log.write(Log.ERROR, "BillDisputeCtlr() --- Error Saving Notes");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp"); 
				return;
			}
		}

		String m_strDsptPrint = request.getParameter("print");
		if ((m_strDsptPrint == null) || (m_strDsptPrint.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//DSPT_SQNC_NMBR needs to be valid parameter;
		//DSPT_VRSN needs to be valid parameter;
		//build and display the Print View for the BillDispute
		{
	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- print = " + m_strDsptPrint);

			request.getHttpRequest().setAttribute("print", m_strDsptPrint);
			alltelRequestDispatcher.forward("/BillDisputeFieldPrintView.jsp"); 
			return;
		}

		String m_strDsptPrint2 = request.getParameter("print2");
		if ((m_strDsptPrint2 == null) || (m_strDsptPrint2.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//DSPT_SQNC_NMBR needs to be valid parameter;
		//DSPT_VRSN needs to be valid parameter;
		//build and display the Print View for the BillDispute
		{
	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- print2 = " + m_strDsptPrint2);

			request.getHttpRequest().setAttribute("print2", m_strDsptPrint2);
			// If you want to show all fields
				request.getHttpRequest().setAttribute("dsptformtype","_FIRST_");

			alltelRequestDispatcher.forward("/BillDisputeFormPrintView.jsp"); 
			return;
		}

		//  All actions past this point may first require saving the current data
		//  if we have valid key fields from a FORM with posted data and user had
		//  authority to update data on the form.
		boolean bTransactionExists = false;
		String m_strFrmAuth = request.getParameter("FRM_AUTHORIZATION");
		String m_strMdfdDt = request.getParameter("mdfddt");
		Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() ---m_strMdfdDt= " + m_strMdfdDt);

		//Only create bean if we need it
		BillDisputeBean dsptBean = new BillDisputeBean();
		dsptBean.setUserid(strUSERID);

		if ((m_strDsptSqncNmbr == null) || (m_strDsptSqncNmbr.length() == 0)
			|| (m_strDsptVrsn == null) || (m_strDsptVrsn.length() == 0)
			|| (m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0)
			|| (m_strFrmAuth == null) || (m_strFrmAuth.length() == 0)
			|| (m_strMdfdDt == null) || (m_strMdfdDt.length() == 0))

		{ }
		else
		{
			// build form field vector and call method to save data here

			Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- Save FORM data");

			int iReturnCode = dsptBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = dsptBean.beginTransaction(m_iDsptSqncNmbr, m_strMdfdDt);
				if (iReturnCode == 0) {
					iReturnCode = dsptBean.storeForm(request, m_iFrmSqncNmbr, m_iDsptSqncNmbr, m_iDsptVrsn);
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- Save Form Successful");
				// continue on since there may be more to process in this dispute
				bTransactionExists = true;
			}
			else if (iReturnCode == dsptBean.SECURITY_ERROR)
			{
				alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "BillDisputeCtlr() --- Error Saving Form");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp"); 
				return;
			}
		}

		String m_strDsptForm = request.getParameter("dsptform");
		if ((m_strDsptForm == null) || (m_strDsptForm.length() == 0))
		{}
		else
		//Coming from the BillDisputeView - this action is navigation between Forms
		//"dsptform" needs to be a valid form sequence number
		//DSPT_SQNC_NMBR and DSPT_VRSN need to be valid parameters;
		//validate user has security to view a dispute;
		//the value of dsptform is the FORM SQNC they want to look at; go get it and display the form
		{

	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dsptform = " + m_strDsptForm);
	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dsptformtype = _FRM_CD_");

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dsptBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dsptBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("dsptformtype","_FRM_CD_");
				request.getHttpRequest().setAttribute("dsptform", m_strDsptForm);
				alltelRequestDispatcher.forward("/BillDisputeFormView.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "BillDisputeCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp"); 
				return;
			}
		}
		
		String strDetailHistSqncNmbr = request.getParameter("dtlhist");
		if ((strDetailHistSqncNmbr== null) || (strDetailHistSqncNmbr.length() == 0))
		{}
		else
		// User wants to view a detailed view of history changes
		{
	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- view history detail "+ strDetailHistSqncNmbr);
			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dsptBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dsptBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("histseq", strDetailHistSqncNmbr);
				alltelRequestDispatcher.forward("/BillDisputeDtlHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "BillDisputeCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp"); 
				return;
			}
		}

		String m_strDsptHist = request.getParameter("hist");
		if ((m_strDsptHist == null) || (m_strDsptHist.length() == 0))
		{}
		else
		//This action intented to be navigation to view History
		//DSPT_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view a dispute;
		//build and display the History View for the BillDispute
		{

	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- hist = " + m_strDsptHist);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dsptBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dsptBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("hist", m_strDsptHist);
				alltelRequestDispatcher.forward("/BillDisputeHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "BillDisputeCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp"); 
				return;
			}
		}

		String m_strDsptNotes = request.getParameter("notes");
		if ((m_strDsptNotes == null) || (m_strDsptNotes.length() == 0))
		{}
		else
		//DSPT_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view BillDisputeNOTES;
		//build and display the Notes View for the BillDispute
		{

	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- notes = " + m_strDsptNotes);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dsptBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dsptBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("notes", m_strDsptNotes);
				alltelRequestDispatcher.forward("/BillDisputeNotesView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "BillDisputeCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp"); 
				return;
			}
		}

		String m_strDsptAction = request.getParameter("action");
		if ((m_strDsptAction == null) || (m_strDsptAction.length() == 0))
		{}
		else
		//This action intented to be a status change
		//or a BillDisputeto SAVE the current form data or a BillDisputeto view Validation ERRORS.
		//DSPT_SQNC_NMBR needs to be a valid parameter;
		//DSPT_VRSN will always be the current version;
		//validate the action can be performed based on security and then take appropriate action
		//when done go back to BillDisputeListCtlr if it's a valid Status change.
		{	

	        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- action = " + m_strDsptAction);
			request.getHttpRequest().setAttribute("action", m_strDsptAction);

			if ( m_strDsptAction.equals("Save"))
			{
				// we already saved the data prior to getting here ; just set some fields & get out
				request.getHttpRequest().setAttribute("DSPT_VRSN", m_strDsptVrsn);
				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("dsptformtype","_FRM_SQNC_");

				int iReturnCode = 0;
				if (bTransactionExists)
				{
					iReturnCode = dsptBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = dsptBean.closeConnection();
					}
				}
				if (iReturnCode == 0)
				{
					Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dsptformtype = _FRM_SQNC_");
					alltelRequestDispatcher.forward("/BillDisputeFormView.jsp");  
					return;
				}
				else
				{
					 Log.write(Log.ERROR, "BillDisputeCtlr() --- Transaction Error");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp"); 
					return;
				}
			}
			
			if ( m_strDsptAction.equals("Validate"))
                        {
                                int iReturnCode = 0;
                                int iVldtnErrs = 0;
                                if (bTransactionExists == true)
                                {	iReturnCode = dsptBean.commitTransaction();
                                }
                                else
                                {	iReturnCode = dsptBean.getConnection();
                                }

                                if (iReturnCode == 0)
                                {
                                        iVldtnErrs = dsptBean.validateFields(request, m_iDsptSqncNmbr, m_iDsptVrsn, "A",
"");
                        Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- after validateFields ["+ iVldtnErrs+"]");
                                }

                                iReturnCode = dsptBean.closeConnection();

                        Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- after validateFields and before forward");
                                alltelRequestDispatcher.forward("/BillDisputeValidationView.jsp");
                                return;
                        }
			
		
			//Handle all other actions based on DISPUTE_ACTION_T workflow
			Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- BillDispute Status Change");

			int iReturnCode = 0; 
			if (bTransactionExists == false)
			{
				iReturnCode = dsptBean.getConnection();
				if (iReturnCode == 0) {
					iReturnCode = dsptBean.beginTransaction();
				}
			}
			else
			{
				iReturnCode = dsptBean.commitTransaction();
			}

			if (iReturnCode == 0) {
				iReturnCode = dsptBean.changeStatus(request, m_iDsptSqncNmbr, m_strDsptAction);
				if (iReturnCode > 0) {
					iReturnCode = dsptBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = dsptBean.closeConnection();
					}
				}
				else if (iReturnCode == dsptBean.SECURITY_ERROR)
				{
					alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
					return;
				}
			}

			if (iReturnCode >= 0)
			{
					request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
					request.getHttpRequest().setAttribute("dsptformtype","_FRM_SQNC_");

					Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- Status Change Successful");
                                        // handle telling the user we saved the form successfully
//Add the code for ActionManager here.
                                        String strActnDstntn = null;
                                        ActionManager am = new ActionManager();
                                        strActnDstntn = am.getActionDestination(dsptBean.getSttsCdFrom(), dsptBean.getTypInd(), dsptBean.getRqstTypCd(), dsptBean.getSttsCdTo(), m_strDsptAction);

					if (strActnDstntn.length() > 0) {
					  Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() -- Next action based on Action_T is: "+strActnDstntn);
                                          alltelRequestDispatcher.forward("/"+strActnDstntn);  
                                        } else {
    					  alltelRequestDispatcher.forward("/BillDisputeListCtlr");  
                                        }

					return;
			}
			else
			{
				//if (iReturnCode == -260)
				if (iReturnCode == dsptBean.VALIDATION_ERROR)
				{
					Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- Validation Errors with Form Fields - Sending to Error Validation View");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/BillDisputeValidationView.jsp");  
					return;
				}
				else
				{
					Log.write(Log.ERROR, "BillDisputeCtlr() --- Error Changing BillDispute Status");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp");  
					return;
				}
			}
		}

		//Now check to see if we got here with a BillDisputeto add a section for a FORM.
		//Required parameters are DSPT_SQNC_NMBR, DSPT_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair describing the section to add.
		//The parameter name will be "add_sctn_?" and the value will be "Add Section".
		String m_strDsptAddSctn = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
			m_strDsptAddSctn = request.getParameter("add_sctn_" + m_iFrmSctnSqncNmbr);

			if ((m_strDsptAddSctn == null) || (m_strDsptAddSctn.length() == 0))
			{}
			else
			{
				if (m_strDsptAddSctn.equals("Add Section"))
				{
					//we know now the user submitted a valid BillDispute to add a new section
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dspt_add_sctn");

					int m_iFrmSctnOccNew = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = dsptBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = dsptBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						m_iFrmSctnOccNew = dsptBean.generateSection(m_iFrmSqncNmbr, m_iDsptSqncNmbr, m_iDsptVrsn, m_iFrmSctnSqncNmbr);
						if (m_iFrmSctnOccNew > 0) {
							iReturnCode = dsptBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = dsptBean.closeConnection();
							}
						}
						else if (m_iFrmSctnOccNew == dsptBean.SECURITY_ERROR)
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
						request.getHttpRequest().setAttribute("DSPT_VRSN", m_strDsptVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("dsptformtype","_FRM_SQNC_");

				        Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- m_iFrmSctnOccNew = " + m_iFrmSctnOccNew);
						//Here we need this section we added-cause we want the cursor
						// in the first field of it when the form is presented.
						request.getHttpRequest().setAttribute("NEW_OCC", Integer.toString(m_iFrmSctnOccNew));
						request.getHttpRequest().setAttribute("NEW_SECTION", Integer.toString(m_iFrmSctnSqncNmbr));
				        Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dsptformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/BillDisputeFormView.jsp"); 
						return;

					}
					else
					{
				        	Log.write(Log.ERROR, "BillDisputeCtlr() --- Error getting a new section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp");  
						return;
					}
				}
			}
		}

		//Now check to see if we got here with a BillDispute to delete a section for a FORM.
		//Required parameters are DSPT_SQNC_NMBR, DSPT_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair identifying the
		// the SECTION SEQUENCE number and the SECTION OCCURRENCE that needs to be deleted.
		//The parameter name will be "del_sctn_?_?" and the value will be "Delete Section".
		String m_strDsptDltSctnOcc = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
		  for (int m_iSctnOcc = 1; m_iSctnOcc < 100 ; m_iSctnOcc++)
		  {	
			m_strDsptDltSctnOcc = request.getParameter("del_sctn_" + m_iFrmSctnSqncNmbr + "_" + m_iSctnOcc);

			if ((m_strDsptDltSctnOcc == null) || (m_strDsptDltSctnOcc.length() == 0))
			{}
			else
			{
				if (m_strDsptDltSctnOcc.equals("Delete Section"))
				{
					//we know now the user submitted a valid BillDispute to delete a section occurrence
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- del_sctn");
			        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- m_iFrmSctnSqncNmbr = " + m_iFrmSctnSqncNmbr);
			        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- m_iSctnOcc = " + m_iSctnOcc);

					int iFrmSctnDltOcc = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = dsptBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = dsptBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						iFrmSctnDltOcc = dsptBean.deleteSection(m_iFrmSqncNmbr, m_iDsptSqncNmbr, m_iDsptVrsn, m_iFrmSctnSqncNmbr, m_iSctnOcc);						
						if (iFrmSctnDltOcc == 0) {
							iReturnCode = dsptBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = dsptBean.closeConnection();
							}
						}
						else if (iFrmSctnDltOcc == dsptBean.SECURITY_ERROR)
						{
							alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
							return;
						}
					}
					if (iReturnCode == 0)
					{
						request.getHttpRequest().setAttribute("DSPT_VRSN", m_strDsptVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("dsptformtype","_FRM_SQNC_");

				        	Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- Delete Section Occurrence Successful");
						Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- dsptformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/BillDisputeFormView.jsp"); 
						return;
					}
					else
					{
				        	Log.write(Log.ERROR, "BillDisputeCtlr() --- Error deleting a section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/BillDisputeErrorView.jsp");  
						return;
					}
				}
			}
		  }
		}
				
		// if we dropped down to here, we don't know where we're supposed to go and
		// we apparently have a navigation error.

		Log.write(Log.DEBUG_VERBOSE, "BillDisputeCtlr() --- Navigation Error!");

		alltelRequestDispatcher.forward("/NavigationErrorView.jsp");
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}

