/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:		DslCtlr.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
 *
 *      11/21/2002 shussaini Change Request Navigation.(hdr 200039)
 *      Next page after a change request is determined based on
 *      Action_T table through an ActionManager, Actions singleton and
 *      Action object.
 *	09/19/2003 psedlak Use generic bean and errors and reduce bean creation
 *	05/27/2004 psedlak Remove call to getSrvcTypCd() method...hide this in create()
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class DslCtlr extends AlltelServlet
{
	final String DEFAULT_SRVC_TYP_CD = "5";

	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{	

		String strURL;

        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr()");

		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		String strUSERID = sdm.getUser();
		
		String m_strDslNew = request.getParameter("dslnew");
		if ((m_strDslNew == null) || (m_strDslNew.length() == 0))
		{}
		else
		{	
			if (m_strDslNew.equals("Cancel"))
			{

				Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- dslnew - Cancel");

				// User selected cancel ; send back to Dsl List 
				alltelRequestDispatcher.forward("/DslListCtlr"); 
				return;

			}

			Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- dslnew - Submit");

			String strDslNewErrMsg = "";
			DslBean dslNewBean = new DslBean();
			dslNewBean.setUserid(strUSERID);

			//perform new Dsl Setup in database ;
			int iDslSqncNmbr = 0;

			int iReturnCode = dslNewBean.getConnection();
			if (iReturnCode == 0) { 
				Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- got connection");
				iReturnCode = dslNewBean.beginTransaction();
				if (iReturnCode == 0) {
					Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- started transaction");
					// If user can create indirect DSL orders, then use right form
					if ( sdm.isAuthorized("INDIRECT_DSL_AGT") )
					{
						Log.write(Log.DEBUG_VERBOSE,"DslCtlr() INDIRECT AGT");
						
					}
					// iDslSqncNmbr = dslNewBean.create(strSrvcTypCd);
					iDslSqncNmbr = dslNewBean.create();
					
					if (iDslSqncNmbr > 0) {
						Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- created dsl order");
						iReturnCode = dslNewBean.commitTransaction();
						if (iReturnCode == 0) {
							Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- committed trans");
							iReturnCode = dslNewBean.closeConnection();
						}
					}
					else if (iDslSqncNmbr == dslNewBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
					else
					{
						iReturnCode = iDslSqncNmbr;
					}
				}
			}

			if (iReturnCode < 0)
			{
				// there was a problem getting a new Dsl
				// figure out what the error msg should be and send user back to Dsl Create View

		        	Log.write(Log.ERROR, "DslCtlr() --- Error in createDsl() encountered - unable to create dsl order. ReturnCode = " + iReturnCode);

				// for now the duplicate PON message is the only worthwhile message to send back.
				// we should provide a better description of the Error using return code 
				// descriptions from DslBean.

				if (iReturnCode == dslNewBean.DUP_PON)
				{
					strDslNewErrMsg = "Error : The Dsl Number already exists!";
				}
				else
				{
					strDslNewErrMsg = "Error : Unable to Create the Dsl. An Application Error was encountered trying to Create the Dsl!";
				}			
				
				request.getHttpRequest().setAttribute("dslnew_errormsg", strDslNewErrMsg);

				alltelRequestDispatcher.forward("/DslErrorView.jsp");  
				return;
			}

			//show Forms using the 1st form in SERVICE_TYPE_FORM as the default FORM to display
			request.getHttpRequest().setAttribute("dslformtype","_FIRST_");
			request.getHttpRequest().setAttribute("DSL_SQNC_NMBR", Integer.toString(iDslSqncNmbr));
			request.getHttpRequest().setAttribute("NEWRECORD", "1");
			alltelRequestDispatcher.forward("/DslFormView.jsp");  
			return;
		}

		String m_strDslSqncNmbr = request.getParameter("DSL_SQNC_NMBR");
		String m_strDslVrsn = request.getParameter("DSL_VRSN");
		String m_strFrmSqncNmbr = request.getParameter("FRM_SQNC_NMBR");

        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- DSL_SQNC_NMBR = " + m_strDslSqncNmbr);
        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- DSL_VRSN = " + m_strDslVrsn);
        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- FRM_SQNC_NMBR = " + m_strFrmSqncNmbr);

		String m_strDslGet = request.getParameter("seqget");
		if ((m_strDslGet == null) || (m_strDslGet.length() == 0))
		{}
		else
		//Coming from the DslListView or DslHistoryView where get functions are allowed ;
		//validate user has security to view a dsl order;
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- seqget : DslSqncNmbr = " + m_strDslGet + " ; DslVrsn = " + m_strDslVrsn);

			request.getHttpRequest().setAttribute("dslformtype","_FIRST_");
			request.getHttpRequest().setAttribute("DSL_SQNC_NMBR", m_strDslGet);
			request.getHttpRequest().setAttribute("DSL_VRSN", m_strDslVrsn);
			
	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- dslformtype = _FIRST_");

			alltelRequestDispatcher.forward("/DslFormView.jsp");
			return;
		}

		request.getHttpRequest().setAttribute("DSL_SQNC_NMBR", m_strDslSqncNmbr);
		request.getHttpRequest().setAttribute("DSL_VRSN", m_strDslVrsn);

		int m_iDslSqncNmbr = 0;
		if ((m_strDslSqncNmbr == null) || (m_strDslSqncNmbr.length() == 0))
		{}
		else
		{
			m_iDslSqncNmbr = Integer.parseInt(m_strDslSqncNmbr); 
		}

		int m_iDslVrsn = 0;
		if ((m_strDslVrsn == null) || (m_strDslVrsn.length() == 0))
		{}
		else
		{
			m_iDslVrsn = Integer.parseInt(m_strDslVrsn);
		}
	
		int	m_iFrmSqncNmbr = 0;
		if ((m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0))
		{}
		else
		{
			m_iFrmSqncNmbr = Integer.parseInt(m_strFrmSqncNmbr); 
		}

		String m_strDslNotesUpdate = request.getParameter("notes_update");
		if ((m_strDslNotesUpdate == null) || (m_strDslNotesUpdate.length() == 0))
		{}
		else
		{
			String m_strDslNotesText = request.getParameter("notestext");
			if ((m_strDslNotesText == null) || (m_strDslNotesText.length() == 0))
			{
				m_strDslNotesText = "";
			}

			//NOTE this bean (dslBean)goes out of scope
			DslBean dslBean = new DslBean();
			dslBean.setUserid(strUSERID);

			int iReturnCode = dslBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = dslBean.beginTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dslBean.updateNotes(m_iDslSqncNmbr, m_strDslNotesText);
					if (iReturnCode == 0) {
						iReturnCode = dslBean.commitTransaction();
						if (iReturnCode == 0) {
							iReturnCode = dslBean.closeConnection();
						}
					}
					else if (iReturnCode == dslBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- Save Notes Successful");
				// handle telling the user we saved the notes successfully
				request.getHttpRequest().setAttribute("notes", "Notes");
				alltelRequestDispatcher.forward("/DslNotesView.jsp"); 
				return;
			}
			else
			{
			    	Log.write(Log.ERROR, "DslCtlr() --- Error Saving Notes");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DslErrorView.jsp"); 
				return;
			}
		}

		String m_strDslPrint = request.getParameter("print");
		if ((m_strDslPrint == null) || (m_strDslPrint.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//DSL_SQNC_NMBR needs to be valid parameter;
		//DSL_VRSN needs to be valid parameter;
		//build and display the Print View for the Dsl
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- print = " + m_strDslPrint);

			request.getHttpRequest().setAttribute("print", m_strDslPrint);
			alltelRequestDispatcher.forward("/DslFieldPrintView.jsp"); 
			return;
		}

		String m_strDslPrint2 = request.getParameter("print2");
		if ((m_strDslPrint2 == null) || (m_strDslPrint2.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//DSL_SQNC_NMBR needs to be valid parameter;
		//DSL_VRSN needs to be valid parameter;
		//build and display the Print View for the Dsl
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- print2 = " + m_strDslPrint2);

			request.getHttpRequest().setAttribute("print2", m_strDslPrint2);

			// This shows all fields
			request.getHttpRequest().setAttribute("dslformtype","_FIRST_");

			alltelRequestDispatcher.forward("/DslFormPrintView.jsp"); 
			return;
		}

		//  All actions past this point may first require saving the current data
		//  if we have valid key fields from a FORM with posted data and user had
		//  authority to update data on the form.
		boolean bTransactionExists = false;
		String m_strFrmAuth = request.getParameter("FRM_AUTHORIZATION");
		String m_strMdfdDt = request.getParameter("mdfddt");
		Log.write(Log.DEBUG_VERBOSE, "DslCtlr() ---m_strMdfdDt= " + m_strMdfdDt);

		DslBean dslBean = new DslBean();
		dslBean.setUserid(strUSERID);

		if ((m_strDslSqncNmbr == null) || (m_strDslSqncNmbr.length() == 0)
			|| (m_strDslVrsn == null) || (m_strDslVrsn.length() == 0)
			|| (m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0)
			|| (m_strFrmAuth == null) || (m_strFrmAuth.length() == 0)
			|| (m_strMdfdDt == null) || (m_strMdfdDt.length() == 0))

		{ }
		else
		{
			// build form field vector and call method to save data here

			Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- Save FORM data");

			int iReturnCode = dslBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = dslBean.beginTransaction(m_iDslSqncNmbr, m_strMdfdDt);
				if (iReturnCode == 0) {
					iReturnCode = dslBean.storeForm(request, m_iFrmSqncNmbr, m_iDslSqncNmbr, m_iDslVrsn);
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- Save Form Successful");
				// continue on since there may be more to process in this dsl order
				bTransactionExists = true;
			}
			else if (iReturnCode == dslBean.SECURITY_ERROR)
			{
				alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DslCtlr() --- Error Saving Form");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DslErrorView.jsp"); 
				return;
			}
		}

		String m_strDslForm = request.getParameter("dslform");
		if ((m_strDslForm == null) || (m_strDslForm.length() == 0))
		{}
		else
		//Coming from the DslView - this action is navigation between Forms
		//"dslform" needs to be a valid form sequence number
		//DSL_SQNC_NMBR and DSL_VRSN need to be valid parameters;
		//validate user has security to view a dsl order;
		//the value of dslform is the FORM SQNC they want to look at; go get it and display the form
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- dslform = " + m_strDslForm);
	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- dslformtype = _FRM_CD_");

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dslBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dslBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("dslformtype","_FRM_CD_");
				request.getHttpRequest().setAttribute("dslform", m_strDslForm);
				alltelRequestDispatcher.forward("/DslFormView.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DslCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DslErrorView.jsp"); 
				return;
			}
		}
		
		String strDetailHistSqncNmbr = request.getParameter("dtlhist");
		if ((strDetailHistSqncNmbr== null) || (strDetailHistSqncNmbr.length() == 0))
		{}
		else
		// User wants to view a detailed view of history changes
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- view history detail "+ strDetailHistSqncNmbr);
			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dslBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dslBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("histseq", strDetailHistSqncNmbr);
				alltelRequestDispatcher.forward("/DslDtlHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DslCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DslErrorView.jsp"); 
				return;
			}
		}

		String m_strDslHist = request.getParameter("hist");
		if ((m_strDslHist == null) || (m_strDslHist.length() == 0))
		{}
		else
		//This action intented to be navigation to view History
		//DSL_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view a dsl order;
		//build and display the History View for the Dsl
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- hist = " + m_strDslHist);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dslBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dslBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("hist", m_strDslHist);
				alltelRequestDispatcher.forward("/DslHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DslCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DslErrorView.jsp"); 
				return;
			}
		}

		String m_strDslNotes = request.getParameter("notes");
		if ((m_strDslNotes == null) || (m_strDslNotes.length() == 0))
		{}
		else
		//DSL_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view dsl order NOTES;
		//build and display the Notes View for the Dsl
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- notes = " + m_strDslNotes);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dslBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dslBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("notes", m_strDslNotes);
				alltelRequestDispatcher.forward("/DslNotesView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DslCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DslErrorView.jsp"); 
				return;
			}
		}

		String m_strDslAction = request.getParameter("action");
		if ((m_strDslAction == null) || (m_strDslAction.length() == 0))
		{}
		else
		//This action intented to be a status change
		//or a dsl order to SAVE the current form data or a dsl order to view Validation ERRORS.
		//DSL_SQNC_NMBR needs to be a valid parameter;
		//DSL_VRSN will always be the current version;
		//validate the action can be performed based on security and then take appropriate action
		//when done go back to DslListCtlr if it's a valid Status change.
		{	

	        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- action = " + m_strDslAction);
			request.getHttpRequest().setAttribute("action", m_strDslAction);

			if ( m_strDslAction.equals("Save"))
			{
				// we already saved the data prior to getting here ; just set some fields & get out
				request.getHttpRequest().setAttribute("DSL_VRSN", m_strDslVrsn);
				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("dslformtype","_FRM_SQNC_");

				int iReturnCode = 0;
				if (bTransactionExists)
				{
					iReturnCode = dslBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = dslBean.closeConnection();
					}
				}
				if (iReturnCode == 0)
				{
					Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- dslformtype = _FRM_SQNC_");
					alltelRequestDispatcher.forward("/DslFormView.jsp");  
					return;
				}
				else
				{
					 Log.write(Log.ERROR, "DslCtlr() --- Transaction Error");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DslErrorView.jsp"); 
					return;
				}
			}			
		
                        // Handle "View Validation Errors" Action for viewing Field Validations

			if ( m_strDslAction.equals("Validate"))
			{
				int iReturnCode = 0;
				int iDslVldtnErrs = 0;
				if (bTransactionExists == true)
				{
					iReturnCode = dslBean.commitTransaction();
				}
				else
				{
					iReturnCode = dslBean.getConnection();
				}

				if (iReturnCode == 0)
				{
					iDslVldtnErrs = dslBean.validateFields(request, m_iDslSqncNmbr, m_iDslVrsn, "A", "");
					Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- validation errors = " + iDslVldtnErrs);
				}

				iReturnCode = dslBean.closeConnection();

				alltelRequestDispatcher.forward("/DslValidationView.jsp");
				return;
			}

			//Handle all other actions based on ACTION_T workflow
			Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- Dsl Status Change");

			int iReturnCode = 0; 
			if (bTransactionExists == false)
			{
				iReturnCode = dslBean.getConnection();
				if (iReturnCode == 0) {
					iReturnCode = dslBean.beginTransaction();
				}
			}
			else
			{
				iReturnCode = dslBean.commitTransaction();
			}

			if (iReturnCode == 0) {
				iReturnCode = dslBean.changeStatus(request, m_iDslSqncNmbr, m_strDslAction);
				if (iReturnCode > 0) {
					iReturnCode = dslBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = dslBean.closeConnection();
					}
				}
				else if (iReturnCode == dslBean.SECURITY_ERROR)
				{
					alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
					return;
				}
			}

			if (iReturnCode >= 0)
			{
					request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
					request.getHttpRequest().setAttribute("dslformtype","_FRM_SQNC_");
					Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- Status Change Successful");
                                        // handle telling the user we saved the form successfully
//Add the code for ActionManager here.
                                        String strActnDstntn = null;
                                        ActionManager am = new ActionManager();
                                        strActnDstntn = am.getActionDestination(dslBean.getSttsCdFrom(), dslBean.getTypInd(), dslBean.getRqstTypCd(), dslBean.getSttsCdTo(), m_strDslAction);

					if (strActnDstntn.length() > 0) {
					  Log.write(Log.DEBUG_VERBOSE, "DslCtlr() -- Next action based on Action_T is: "+strActnDstntn);
                                          alltelRequestDispatcher.forward("/"+strActnDstntn);  
                                        } else {
    					  alltelRequestDispatcher.forward("/DslListCtlr");  
                                        }
					return;
			}
			else
			{
				if (iReturnCode == dslBean.VALIDATION_ERROR)
				{
					Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- Validation Errors with Form Fields - Sending to Error Validation View");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DslValidationView.jsp");  
					return;
				}
				else
				{
					Log.write(Log.ERROR, "DslCtlr() --- Error Changing Dsl Status");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DslErrorView.jsp");  
					return;
				}
			}
		}

		//Now check to see if we got here with a dsl order to add a section for a FORM.
		//Required parameters are DSL_SQNC_NMBR, DSL_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair describing the section to add.
		//The parameter name will be "add_sctn_?" and the value will be "Add Section".
		String m_strDslAddSctn = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
			m_strDslAddSctn = request.getParameter("add_sctn_" + m_iFrmSctnSqncNmbr);

			if ((m_strDslAddSctn == null) || (m_strDslAddSctn.length() == 0))
			{}
			else
			{
				if (m_strDslAddSctn.equals("Add Section"))
				{
					//we know now the user submitted a valid dsl order to add a new section
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- add_sctn");

					int m_iFrmSctnOccNew = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = dslBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = dslBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						m_iFrmSctnOccNew = dslBean.generateSection(m_iFrmSqncNmbr, m_iDslSqncNmbr, m_iDslVrsn, m_iFrmSctnSqncNmbr);
						if (m_iFrmSctnOccNew > 0) {
							iReturnCode = dslBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = dslBean.closeConnection();
							}
						}
						else if (m_iFrmSctnOccNew == dslBean.SECURITY_ERROR)
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
						request.getHttpRequest().setAttribute("DSL_VRSN", m_strDslVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("dslformtype","_FRM_SQNC_");

				        Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- m_iFrmSctnOccNew = " + m_iFrmSctnOccNew);
				        
						//Here we need this section we added-cause we want the cursor
						// in the first field of it when the form is presented.
						request.getHttpRequest().setAttribute("NEW_OCC", Integer.toString(m_iFrmSctnOccNew));
						request.getHttpRequest().setAttribute("NEW_SECTION", Integer.toString(m_iFrmSctnSqncNmbr));
					Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- dslformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/DslFormView.jsp"); 
						return;

					}
					else
					{
				        	Log.write(Log.ERROR, "DslCtlr() --- Error getting a new section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/DslErrorView.jsp");  
						return;
					}
				}
			}
		}

		//Now check to see if we got here with a dsl order to delete a section for a FORM.
		//Required parameters are DSL_SQNC_NMBR, DSL_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair identifying the
		// the SECTION SEQUENCE number and the SECTION OCCURRENCE that needs to be deleted.
		//The parameter name will be "del_sctn_?_?" and the value will be "Delete Section".
		String m_strDslDltSctnOcc = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
		  for (int m_iSctnOcc = 1; m_iSctnOcc < 100 ; m_iSctnOcc++)
		  {	
			m_strDslDltSctnOcc = request.getParameter("del_sctn_" + m_iFrmSctnSqncNmbr + "_" + m_iSctnOcc);

			if ((m_strDslDltSctnOcc == null) || (m_strDslDltSctnOcc.length() == 0))
			{}
			else
			{
				if (m_strDslDltSctnOcc.equals("Delete Section"))
				{
					//we know now the user submitted a valid dsl order to delete a section occurrence
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- del_sctn");
			        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- m_iFrmSctnSqncNmbr = " + m_iFrmSctnSqncNmbr);
			        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- m_iSctnOcc = " + m_iSctnOcc);

					int iFrmSctnDltOcc = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = dslBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = dslBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						iFrmSctnDltOcc = dslBean.deleteSection(m_iFrmSqncNmbr, m_iDslSqncNmbr, m_iDslVrsn, m_iFrmSctnSqncNmbr, m_iSctnOcc);						
						if (iFrmSctnDltOcc == 0) {
							iReturnCode = dslBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = dslBean.closeConnection();
							}
						}
						else if (iFrmSctnDltOcc == dslBean.SECURITY_ERROR)
						{
							alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
							return;
						}
					}
					if (iReturnCode == 0)
					{
						request.getHttpRequest().setAttribute("DSL_VRSN", m_strDslVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("dslformtype","_FRM_SQNC_");

				        	Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- Delete Section Occurrence Successful");
						Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- dslformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/DslFormView.jsp"); 
						return;
					}
					else
					{
				        	Log.write(Log.ERROR, "DslCtlr() --- Error deleting a section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/DslErrorView.jsp");  
						return;
					}
				}
			}
		  }
		}
				
		// if we dropped down to here, we don't know where we're supposed to go and
		// we apparently have a navigation error.

		Log.write(Log.DEBUG_VERBOSE, "DslCtlr() --- Navigation Error!");

		alltelRequestDispatcher.forward("/NavigationErrorView.jsp");
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}

