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
 * MODULE:	PreorderCtlr.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        04-01-2002
 * 
 * HISTORY:
 *	04/01/2002  dmartz Express 2.0
 *      04/11/2002 psedlak Changed navigation - ACTN_DSTNTN from ACTION_T table is used.
 *
 *      11/21/2002 shussaini Change Request Navigation.(hdr 200039)
 *      Next page after a change request is determined based on
 *      Action_T table through an ActionManager, Actions singleton and
 *      Action object.
 * 	09/22/2003 psedlak	Use generic bean and errors and reduce bean creation
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SERVLET/PreorderCtlr.java  $
   
      Rev 1.1   Dec 11 2002 14:58:22   e0069884
    
   

      Rev 1.0   May 30 2002 07:54:56   sedlak

   Express 2.0 Controllers

*/
/* $Revision:   1.1  $
*/

package com.alltel.lsr.common.servlets;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class PreorderCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{	

		String strURL;

        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr()");

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

		String m_strPreCreate = request.getParameter("precreate");
		if ((m_strPreCreate == null) || (m_strPreCreate.length() == 0))
		{}
		else
		//Coming from PreorderListView;
		//validate security - user needs to have security to create a preorder
		// SELECT DISTINCT SCRTY_OBJCT_CD from ACTION_T WHERE PRE_ORDR_STTS_CD_FROM = "CREATE"
		// If the user has one of the SCRTY_OBJCT codes in their profile they can go to the create view
		//Send to Preorder Create View to enter required fields for entering a new preorder
		{
			Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- precreate");
			request.getHttpRequest().setAttribute( "NEWRECORD", "1");
			alltelRequestDispatcher.forward("/PreorderCreateView.jsp");  
			return;
		}

		String m_strPreNew = request.getParameter("prenew");
		if ((m_strPreNew == null) || (m_strPreNew.length() == 0))
		{}
		else
		{	
			if (m_strPreNew.equals("Cancel"))
			{

				Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- prenew - Cancel");

				// User selected cancel ; send back to Preorder List 

				alltelRequestDispatcher.forward("/PreorderListCtlr"); 
				return;

			}

			Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- prenew - Submit");

			//Coming from the CreatePreorderView;
			//validate security - user needs to have security to create a preorder;
			// SELECT DISTINCT SCRTY_OBJCT_CD from ACTION_T WHERE PRE_ORDR_STTS_CD_FROM = "CREATE"
			//  AND PRE_ORDR_TYP_STTS = "status cd from CreateView"
			// If the user has one of the SCRTY_OBJCT codes in their profile they can create the preorder

			//validate all fields from CreatePreorderView needed to create a new Preorder;	

			String strPreNewErrMsg = "";

			String strOCNSttSqncNmbr = request.getParameter("prenew_ocnsttsqnc");
			int iOCNSttSqncNmbr = 0;
			if ((strOCNSttSqncNmbr == null) || (strOCNSttSqncNmbr.length() == 0))
			{
		        	Log.write(Log.ERROR, "PreorderCtlr() --- Error: A Valid OCN/State combination must be Selected");

				strPreNewErrMsg = "Error: A Valid OCN/State combination must be Selected";
				request.getHttpRequest().setAttribute("prenew_errormsg", strPreNewErrMsg);

				alltelRequestDispatcher.forward("/PreorderCreateView.jsp");  
				return;

			}
			else
			{
				iOCNSttSqncNmbr = Integer.parseInt(strOCNSttSqncNmbr);
			}

			String strPreSrvcTyp = request.getParameter("prenew_srvctyp");
			if ((strPreSrvcTyp == null) || (strPreSrvcTyp.length() == 0))
			{
		        	Log.write(Log.ERROR, "PreorderCtlr() --- Error: A Valid Service Type must be Selected");

				strPreNewErrMsg = "Error: A Valid Service Type must be Selected";
				request.getHttpRequest().setAttribute("prenew_errormsg", strPreNewErrMsg);

				alltelRequestDispatcher.forward("/PreorderCreateView.jsp");  
				return;

			}

			String strPreActvtyTyp = request.getParameter("prenew_actvtytyp");
			if ((strPreActvtyTyp == null) || (strPreActvtyTyp.length() == 0))
			{
		        	Log.write(Log.ERROR, "PreorderCtlr() --- Error: A Valid Activity Type must be Selected");

				strPreNewErrMsg = "Error: A Valid Activity Type must be Selected";
				request.getHttpRequest().setAttribute("prenew_errormsg", strPreNewErrMsg);

				alltelRequestDispatcher.forward("/PreorderCreateView.jsp");  
				return;

			}
			
			//NOTE this bean goes out of scope poNewBean
			PreorderBean poNewBean = new PreorderBean();
			poNewBean.setUserid(strUSERID);
	
			//perform new Preorder Setup in database ;
			int iPreSqncNmbrNew = 0;
			int iReturnCode = poNewBean.getConnection();
			if (iReturnCode == 0) { 
				Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- got connection");
				iReturnCode = poNewBean.beginTransaction();
				if (iReturnCode == 0) {
					Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- started transaction");
					iPreSqncNmbrNew = poNewBean.create(iOCNSttSqncNmbr, strPreSrvcTyp, strPreActvtyTyp, iCmpnySqncNmbr);
					if (iPreSqncNmbrNew > 0) {
						Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- created preorder");
						iReturnCode = poNewBean.commitTransaction();
						if (iReturnCode == 0) {
							Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- committed trans");
							iReturnCode = poNewBean.closeConnection();
						}
					}
					else if (iPreSqncNmbrNew == poNewBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
					else {
						iReturnCode = iPreSqncNmbrNew;
					}
				}
			}

			if (iReturnCode < 0)
			{
				// there was a problem getting a new Preorder
				// figure out what the error msg should be and send user back to Preorder Create View

		        	Log.write(Log.ERROR, "PreorderCtlr() --- Error in createPreorder() encountered - unable to create preorder. ReturnCode = " + iReturnCode);

				strPreNewErrMsg = "Error : Unable to Create the Preorder. An Application Error was encountered trying to Create the Preorder!";
				
				request.getHttpRequest().setAttribute("prenew_errormsg", strPreNewErrMsg);

				alltelRequestDispatcher.forward("/PreorderCreateView.jsp");  
				return;
			}
			
			//If the Preorder was for a Loop Qual, we forward user to the Targus website
			if ( strPreSrvcTyp.equals("H") )
			{
				alltelRequestDispatcher.forward("/DSLLookup.jsp");  
				return;
			}

			//show Forms using the 1st form in SERVICE_TYPE_FORM as the default FORM to display

			request.getHttpRequest().setAttribute("preformtype","_FIRST_");
			request.getHttpRequest().setAttribute("PRE_ORDR_SQNC_NMBR", Integer.toString(iPreSqncNmbrNew));

			alltelRequestDispatcher.forward("/PreorderFormView.jsp");  
			return;
		}

		String m_strPreSqncNmbr = request.getParameter("PRE_ORDR_SQNC_NMBR");
		String m_strPreVrsn = request.getParameter("PRE_ORDR_VRSN");
		String m_strFrmSqncNmbr = request.getParameter("FRM_SQNC_NMBR");

        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- PRE_ORDR_SQNC_NMBR = " + m_strPreSqncNmbr);
        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- PRE_ORDR_VRSN = " + m_strPreVrsn);
        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- FRM_SQNC_NMBR = " + m_strFrmSqncNmbr);

		String m_strPreGet = request.getParameter("seqget");
		if ((m_strPreGet == null) || (m_strPreGet.length() == 0))
		{}
		else
		//Coming from the PreorderListView or PreorderHistoryView where get functions are allowed ;
		//validate user has security to view a preorder;
		{

	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- seqget : PreSqncNmbr = " + m_strPreGet + " ; PreVrsn = " + m_strPreVrsn);

			request.getHttpRequest().setAttribute("preformtype","_FIRST_");
			request.getHttpRequest().setAttribute("PRE_ORDR_SQNC_NMBR", m_strPreGet);
			request.getHttpRequest().setAttribute("PRE_ORDR_VRSN", m_strPreVrsn);
			
	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- preformtype = _FIRST_");

			alltelRequestDispatcher.forward("/PreorderFormView.jsp");
			return;
		}

		request.getHttpRequest().setAttribute("PRE_ORDR_SQNC_NMBR", m_strPreSqncNmbr);
		request.getHttpRequest().setAttribute("PRE_ORDR_VRSN", m_strPreVrsn);

		int m_iPreSqncNmbr = 0;
		if ((m_strPreSqncNmbr == null) || (m_strPreSqncNmbr.length() == 0))
		{}
		else
		{
			m_iPreSqncNmbr = Integer.parseInt(m_strPreSqncNmbr); 
		}

		int m_iPreVrsn = 0;
		if ((m_strPreVrsn == null) || (m_strPreVrsn.length() == 0))
		{}
		else
		{
			m_iPreVrsn = Integer.parseInt(m_strPreVrsn);
		}
	
		int	m_iFrmSqncNmbr = 0;
		if ((m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0))
		{}
		else
		{
			m_iFrmSqncNmbr = Integer.parseInt(m_strFrmSqncNmbr); 
		}

		String m_strPreNotesUpdate = request.getParameter("notes_update");
		if ((m_strPreNotesUpdate == null) || (m_strPreNotesUpdate.length() == 0))
		{}
		else
		{
			String m_strPreNotesText = request.getParameter("notestext");
			if ((m_strPreNotesText == null) || (m_strPreNotesText.length() == 0))
			{
				m_strPreNotesText = "";
			}

			PreorderBean poNotesBean = new PreorderBean();
			poNotesBean.setUserid(strUSERID);
			int iReturnCode = poNotesBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = poNotesBean.beginTransaction();
				if (iReturnCode == 0) {
					iReturnCode = poNotesBean.updateNotes(m_iPreSqncNmbr, m_strPreNotesText);
					if (iReturnCode == 0) {
						iReturnCode = poNotesBean.commitTransaction();
						if (iReturnCode == 0) {
							iReturnCode = poNotesBean.closeConnection();
						}
					}
					else if (iReturnCode == poNotesBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Save Notes Successful");
				// handle telling the user we saved the notes successfully
				request.getHttpRequest().setAttribute("notes", "Notes");
				alltelRequestDispatcher.forward("/PreorderNotesView.jsp"); 
				return;
			}
			else
			{
			    	Log.write(Log.ERROR, "PreorderCtlr() --- Error Saving Notes");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/PreorderErrorView.jsp"); 
				return;
			}
		}

		String m_strPrePrint = request.getParameter("print");
		if ((m_strPrePrint == null) || (m_strPrePrint.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//PRE_ORDR_SQNC_NMBR needs to be valid parameter;
		//PRE_ORDR_VRSN needs to be valid parameter;
		//build and display the Print View for the Preorder
		{
	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- print = " + m_strPrePrint);

			request.getHttpRequest().setAttribute("print", m_strPrePrint);
			alltelRequestDispatcher.forward("/PreorderFieldPrintView.jsp"); 
			return;
		}

		String m_strPrePrint2 = request.getParameter("print2");
		if ((m_strPrePrint2 == null) || (m_strPrePrint2.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//PRE_ORDR_SQNC_NMBR needs to be valid parameter;
		//PRE_ORDR_VRSN needs to be valid parameter;
		//build and display the Print View for the Preorder
		{
	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- print2 = " + m_strPrePrint2);

			request.getHttpRequest().setAttribute("print2", m_strPrePrint2);
			// If you want to show all fields
				request.getHttpRequest().setAttribute("preformtype","_FIRST_");

			alltelRequestDispatcher.forward("/PreorderFormPrintView.jsp"); 
			return;
		}

		String m_strPreAtnPrint = request.getParameter("preatnprint");
		if ((m_strPreAtnPrint == null) || (m_strPreAtnPrint.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//PRE_ORDR_SQNC_NMBR needs to be valid parameter;
		//PRE_ORDR_VRSN needs to be valid parameter;
		//build and display the Print View for the Preorder
		{
	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- preatnprint = " + m_strPreAtnPrint);

			request.getHttpRequest().setAttribute("preformtype","_FIRST_");

			alltelRequestDispatcher.forward("/PreorderAtnPrintView.jsp"); 
			return;
		}

		//  All actions past this point may first require saving the current data
		//  if we have valid key fields from a FORM with posted data and user had
		//  authority to update data on the form.
		boolean bTransactionExists = false;
		String m_strFrmAuth = request.getParameter("FRM_AUTHORIZATION");
		String m_strMdfdDt = request.getParameter("mdfddt");
		Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() ---m_strMdfdDt= " + m_strMdfdDt);

		PreorderBean preorderBean = new PreorderBean();
		preorderBean.setUserid(strUSERID);

		if ((m_strPreSqncNmbr == null) || (m_strPreSqncNmbr.length() == 0)
			|| (m_strPreVrsn == null) || (m_strPreVrsn.length() == 0)
			|| (m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0)
			|| (m_strFrmAuth == null) || (m_strFrmAuth.length() == 0)
			|| (m_strMdfdDt == null) || (m_strMdfdDt.length() == 0))

		{ }
		else
		{
			// build form field vector and call method to save data here

			Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Save FORM data");

			int iReturnCode = preorderBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = preorderBean.beginTransaction(m_iPreSqncNmbr, m_strMdfdDt);
				if (iReturnCode == 0) {
					iReturnCode = preorderBean.storeForm(request, m_iFrmSqncNmbr, m_iPreSqncNmbr, m_iPreVrsn);
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Save Form Successful");
				// continue on since there may be more to process in this preorder
				bTransactionExists = true;
			}
			else if (iReturnCode == preorderBean.SECURITY_ERROR)
			{
				alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "PreorderCtlr() --- Error Saving Form");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/PreorderErrorView.jsp"); 
				return;
			}
		}

		String m_strPreForm = request.getParameter("preform");
		if ((m_strPreForm == null) || (m_strPreForm.length() == 0))
		{}
		else
		//Coming from the PreorderView - this action is navigation between Forms
		//"preform" needs to be a valid form sequence number
		//PRE_ORDR_SQNC_NMBR and PRE_ORDR_VRSN need to be valid parameters;
		//validate user has security to view a preorder;
		//the value of preform is the FORM SQNC they want to look at; go get it and display the form
		{

	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- preform = " + m_strPreForm);
	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- preformtype = _FRM_CD_");

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = preorderBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = preorderBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("preformtype","_FRM_CD_");
				request.getHttpRequest().setAttribute("preform", m_strPreForm);
				alltelRequestDispatcher.forward("/PreorderFormView.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "PreorderCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/PreorderErrorView.jsp"); 
				return;
			}
		}
		
		String strDetailHistSqncNmbr = request.getParameter("dtlhist");
		if ((strDetailHistSqncNmbr== null) || (strDetailHistSqncNmbr.length() == 0))
		{}
		else
		// User wants to view a detailed view of history changes
		{
	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- view history detail "+ strDetailHistSqncNmbr);
			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = preorderBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = preorderBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("histseq", strDetailHistSqncNmbr);
				alltelRequestDispatcher.forward("/PreorderDtlHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "PreorderCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/PreorderErrorView.jsp"); 
				return;
			}
		}

		String m_strPreHist = request.getParameter("hist");
		if ((m_strPreHist == null) || (m_strPreHist.length() == 0))
		{}
		else
		//This action intented to be navigation to view History
		//PRE_ORDR_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view a preorder;
		//build and display the History View for the Preorder
		{

	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- hist = " + m_strPreHist);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = preorderBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = preorderBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("hist", m_strPreHist);
				alltelRequestDispatcher.forward("/PreorderHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "PreorderCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/PreorderErrorView.jsp"); 
				return;
			}
		}

		String m_strPreNotes = request.getParameter("notes");
		if ((m_strPreNotes == null) || (m_strPreNotes.length() == 0))
		{}
		else
		//PRE_ORDR_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view preorder NOTES;
		//build and display the Notes View for the Preorder
		{

	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- notes = " + m_strPreNotes);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = preorderBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = preorderBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("notes", m_strPreNotes);
				alltelRequestDispatcher.forward("/PreorderNotesView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "PreorderCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/PreorderErrorView.jsp"); 
				return;
			}
		}

		//Get the value of the Action button the user selected.
		String m_strPreAction = request.getParameter("action");                
		if ((m_strPreAction == null) || (m_strPreAction.length() == 0))
		{}
		else
		//This action intended to be a status change and possibly an automated Response.
		//PRE_ORDR_SQNC_NMBR needs to be a valid parameter;
		//PRE_ORDR_VRSN will always be the current version;
		//Validate the action can be performed based on security and then take appropriate action
		//when done go back to PreorderListCtlr if it's a valid Status change.
		{	
 
	        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- action = " + m_strPreAction);
			request.getHttpRequest().setAttribute("action", m_strPreAction);

			if ( m_strPreAction.equals("Save"))
			{
				// we already saved the data prior to getting here ; just set some fields & get out
				request.getHttpRequest().setAttribute("PRE_ORDR_VRSN", m_strPreVrsn);
				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("preformtype","_FRM_SQNC_");

				int iReturnCode = 0;
				if (bTransactionExists)
				{
					iReturnCode = preorderBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = preorderBean.closeConnection();
					}
				}
				if (iReturnCode == 0)
				{
					Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- preformtype = _FRM_SQNC_");
//pjs			        	alltelRequestDispatcher.forward("/" + m_strNextDestination);  
                                        alltelRequestDispatcher.forward("/PreorderFormView.jsp");  
					return;
				}
				else
				{
					 Log.write(Log.ERROR, "PreorderCtlr() --- Transaction Error");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/PreorderErrorView.jsp"); 
					return;
				}
			} // end 'Save'			
                       
			// Handle "View Validation Errors" Action for viewing Field Validations

			if ( m_strPreAction.equals("Validate"))
			{
				int iReturnCode = 0; 
				int iPreVldtnErrs = 0;
				if (bTransactionExists == true)
				{
					iReturnCode = preorderBean.commitTransaction();
				}
				else
				{
					iReturnCode = preorderBean.getConnection();
				}

				if (iReturnCode == 0) 
				{
					iPreVldtnErrs = preorderBean.validateFields(request, m_iPreSqncNmbr, m_iPreVrsn, "A", "");
				}
						
				iReturnCode = preorderBean.closeConnection();
				
				alltelRequestDispatcher.forward("/PreorderValidationView.jsp");  
				return;

			} //end 'Validate'			

      			if ( m_strPreAction.equals("Submit"))       //Preorder response MAY be automated !
			{
				// we already saved the data prior to getting here ; just set some fields & get out
				request.getHttpRequest().setAttribute("PRE_ORDR_VRSN", m_strPreVrsn);
				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("preformtype","_FRM_SQNC_");

				int iReturnCode = 0;
				if (!bTransactionExists)
                                {       Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- 'Submit' processing Trans Does not exist");    
                                        iReturnCode = preorderBean.getConnection();
                                        if (iReturnCode == 0) {
                                            iReturnCode = preorderBean.beginTransaction();
                                        }
                                }
				else
				{
					iReturnCode = preorderBean.commitTransaction();
				}
				
                                //NOTE: ReturnCode from changePreorderStatus is the History Seq Nbr.
				iReturnCode = preorderBean.changeStatus(request, m_iPreSqncNmbr, m_strPreAction, m_iFrmSqncNmbr);
				if (iReturnCode > 0) {
                                        Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- 1st Status chg done-do COMMIT"); 
					// We commit here to accurately show HISTORY records and provide the ability to see
					// how long an automated order takes.
					iReturnCode = preorderBean.commitTransaction();
                                        iReturnCode = preorderBean.processPreorder(request, m_iFrmSqncNmbr, 
                                                                                    m_iPreSqncNmbr, m_iPreVrsn, m_strPreAction);
					//If iReturnCode >= 0 then everything's OK
                                        if (iReturnCode >= 0)
                                        {
					    Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- processPreorder done-ready to COMMIT");
                                            iReturnCode = preorderBean.commitTransaction();
                                            if (iReturnCode == 0) {
						iReturnCode = preorderBean.closeConnection();
                                            }
					    if (preorderBean.getNextForm() > 0)	//Take user to a another results form?
					    {
                                            	String strNextForm = ""+ preorderBean.getNextForm();
                                            	request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", strNextForm); //form to show next
					    }
					    else
					    {	
						Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Send back to PO List");
						alltelRequestDispatcher.forward("/PreorderListCtlr");  
						return;
					    }
					
                                        }
                                        
                                }
				else if (iReturnCode == preorderBean.SECURITY_ERROR)
				{
					alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
					return;
				}
				else if (iReturnCode == preorderBean.VALIDATION_ERROR)
				{
					Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Validation Errors with Form Fields - Sending to Error Validation View");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/PreorderValidationView.jsp");  
					return;
				}
				
				if (iReturnCode == 0)
				{
					Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- process Preorder OK");
//pjs			        	alltelRequestDispatcher.forward("/" + m_strNextDestination);  
                                        alltelRequestDispatcher.forward("/PreorderFormView.jsp");  
					return;
				}
				else
				{
					 Log.write(Log.ERROR, "PreorderCtlr() --- Transaction Error while processing Preorder");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/PreorderErrorView.jsp"); 
					return;
				}
                        } //end 'Submit'		

			//Handle all other actions based on ACTION_T workflow
			Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Preorder simple Status Change");

			int iReturnCode = 0; 
			if (bTransactionExists == false)
			{
				iReturnCode = preorderBean.getConnection();
				if (iReturnCode == 0) {
					iReturnCode = preorderBean.beginTransaction();
				}
			}
			else
			{
				iReturnCode = preorderBean.commitTransaction();
			}

			if (iReturnCode == 0) {
				iReturnCode = preorderBean.changeStatus(request, m_iPreSqncNmbr, m_strPreAction, m_iFrmSqncNmbr);
				if (iReturnCode > 0) {
					iReturnCode = preorderBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = preorderBean.closeConnection();
					}
				}
				else if (iReturnCode == preorderBean.SECURITY_ERROR)
				{
					alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
					return;
				}
			}

			if (iReturnCode >= 0)
			{
					request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
					request.getHttpRequest().setAttribute("preformtype","_FRM_SQNC_");

					Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Status Change Successful");
					// handle telling the user we saved the form successfully
//pjs                                        alltelRequestDispatcher.forward("/"+m_strNextDestination);  
//Action_T navigation change.--smh
                                        String strActnDstntn = null;
                                        ActionManager am = new ActionManager();
                                        strActnDstntn = am.getActionDestination(preorderBean.getSttsCdFrom(), preorderBean.getTypInd(), preorderBean.getRqstTypCd(), preorderBean.getSttsCdTo(), m_strPreAction);

					if (strActnDstntn.length() > 0) {
					  Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() -- Next action based on Action_T is: "+strActnDstntn);
                                          alltelRequestDispatcher.forward("/"+strActnDstntn);  
                                        } else {
    					  alltelRequestDispatcher.forward("/PreorderListCtlr");  
                                        }

					return;
			}
			else
			{
				if (iReturnCode == preorderBean.VALIDATION_ERROR)
				{
					Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Validation Errors with Form Fields - Sending to Error Validation View");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/PreorderValidationView.jsp");  
					return;
				}
				else
				{
					Log.write(Log.ERROR, "PreorderCtlr() --- Error Changing Preorder Status");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/PreorderErrorView.jsp");  
					return;
				}
			}
		}

		//Now check to see if we got here with a preorder to add a section for a FORM.
		//Required parameters are PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair describing the section to add.
		//The parameter name will be "add_sctn_?" and the value will be "Add Section".
		String m_strPreAddSctn = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
			m_strPreAddSctn = request.getParameter("add_sctn_" + m_iFrmSctnSqncNmbr);

			if ((m_strPreAddSctn == null) || (m_strPreAddSctn.length() == 0))
			{}
			else
			{
				if (m_strPreAddSctn.equals("Add Section"))
				{
					//we know now the user submitted a valid preorder to add a new section
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- add_sctn");

					int m_iFrmSctnOccNew = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = preorderBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = preorderBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						m_iFrmSctnOccNew = preorderBean.generateSection(m_iFrmSqncNmbr, m_iPreSqncNmbr, m_iPreVrsn, m_iFrmSctnSqncNmbr);
						if (m_iFrmSctnOccNew > 0) {
							iReturnCode = preorderBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = preorderBean.closeConnection();
							}
						}
						else if (m_iFrmSctnOccNew == preorderBean.SECURITY_ERROR)
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
						request.getHttpRequest().setAttribute("PRE_ORDR_VRSN", m_strPreVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("preformtype","_FRM_SQNC_");

				        Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- m_iFrmSctnOccNew = " + m_iFrmSctnOccNew);
						//Here we need this section we added-cause we want the cursor
						// in the first field of it when the form is presented.
						request.getHttpRequest().setAttribute("NEW_OCC", Integer.toString(m_iFrmSctnOccNew));
						request.getHttpRequest().setAttribute("NEW_SECTION", Integer.toString(m_iFrmSctnSqncNmbr));

				        Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- preformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/PreorderFormView.jsp"); 
						return;

					}
					else
					{
				        	Log.write(Log.ERROR, "PreorderCtlr() --- Error getting a new section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/PreorderErrorView.jsp");  
						return;
					}
				}
			}
		}

		//Now check to see if we got here with a preorder to delete a section for a FORM.
		//Required parameters are PRE_ORDR_SQNC_NMBR, PRE_ORDR_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair identifying the
		// the SECTION SEQUENCE number and the SECTION OCCURRENCE that needs to be deleted.
		//The parameter name will be "del_sctn_?_?" and the value will be "Delete Section".
		String m_strPreDltSctnOcc = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
		  for (int m_iSctnOcc = 1; m_iSctnOcc < 100 ; m_iSctnOcc++)
		  {	
			m_strPreDltSctnOcc = request.getParameter("del_sctn_" + m_iFrmSctnSqncNmbr + "_" + m_iSctnOcc);

			if ((m_strPreDltSctnOcc == null) || (m_strPreDltSctnOcc.length() == 0))
			{}
			else
			{
				if (m_strPreDltSctnOcc.equals("Delete Section"))
				{
					//we know now the user submitted a valid preorder to delete a section occurrence
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- del_sctn");
			        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- m_iFrmSctnSqncNmbr = " + m_iFrmSctnSqncNmbr);
			        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- m_iSctnOcc = " + m_iSctnOcc);

					int iFrmSctnDltOcc = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = preorderBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = preorderBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						iFrmSctnDltOcc = preorderBean.deleteSection(m_iFrmSqncNmbr, m_iPreSqncNmbr, m_iPreVrsn, m_iFrmSctnSqncNmbr, m_iSctnOcc);						
						if (iFrmSctnDltOcc == 0) {
							iReturnCode = preorderBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = preorderBean.closeConnection();
							}
						}
						else if (iFrmSctnDltOcc == preorderBean.SECURITY_ERROR)
						{
							alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
							return;
						}
					}
					if (iReturnCode == 0)
					{
						request.getHttpRequest().setAttribute("PRE_ORDR_VRSN", m_strPreVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("preformtype","_FRM_SQNC_");

				        	Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Delete Section Occurrence Successful");
						Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- preformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/PreorderFormView.jsp"); 
						return;
					}
					else
					{
				        	Log.write(Log.ERROR, "PreorderCtlr() --- Error deleting a section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/PreorderErrorView.jsp");  
						return;
					}
				}
			}
		  }
		}
				
		// if we dropped down to here, we don't know where we're supposed to go and
		// we apparently have a navigation error.

		Log.write(Log.DEBUG_VERBOSE, "PreorderCtlr() --- Navigation Error!");

		alltelRequestDispatcher.forward("/NavigationErrorView.jsp");
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
        
}

