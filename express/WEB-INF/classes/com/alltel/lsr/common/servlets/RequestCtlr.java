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
 * MODULE:	RequestCtlr.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        01-31-2002
 * 
 * HISTORY:
 *	10/10/2002  psedlak -put cursor in 1st field of new section
 *
 *      11/21/2002 shussaini Change Request Navigation.(hdr 200039)
 *      Next page after a change request is determined based on
 *      Action_T table through an ActionManager, Actions singleton and
 *      Action object.
 *      12/04/2002 shussaini set the RQST_VRSN attribute of the request 
 *      object to null after a status change. so that the JSP can pickup  
 *      the latest version of the order.
 *
 *	09/22/2003 psedlak      Use generic bean and errors and reduce bean creation
 *	6/3/2004 psedlak	Notification indicator changes
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SERVLET/RequestCtlr.java  $
/*
/*   Rev 1.5   Dec 11 2002 14:58:28   e0069884
/* 
/*
/*   Rev 1.4   Oct 21 2002 08:34:44   e0069884
/*HDR 67872
/*
/*   Rev 1.3   May 30 2002 11:32:26   sedlak
/* 
/*
/*   Rev 1.2   13 Feb 2002 14:21:50   dmartz
/*Release 1.1
/*
/*   Rev 1.0   23 Jan 2002 11:06:22   wwoods
/*Initial Checkin
*/

/* $Revision:   1.5  $
*/

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;
import com.automation.dao.LSRdao;

public class RequestCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{	

		String strURL;

        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr()");
                //System.out.println("come here!!!!!!!!!!!!!!!!!");

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

		String m_strRqstCreate = request.getParameter("rqstcreate");
		if ((m_strRqstCreate == null) || (m_strRqstCreate.length() == 0))
		{}
		else
		//Coming from RequestListView;
		//validate security - user needs to have security to create a request
		// SELECT DISTINCT SCRTY_OBJCT_CD from REQUEST_ACTION_T WHERE RQST_STTS_CD_FROM = "CREATE"
		// If the user has one of the SCRTY_OBJCT codes in their profile they can go to the create view
		//Send to Request Create View to enter required fields for entering a new request
		{
			Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- rqstcreate");
			request.getHttpRequest().setAttribute("NEWRECORD", "1");
			alltelRequestDispatcher.forward("/RequestCreateView.jsp");  
			return;
		}

		String m_strRqstNew = request.getParameter("rqstnew");
		if ((m_strRqstNew == null) || (m_strRqstNew.length() == 0))
		{}
		else
		{	
			if (m_strRqstNew.equals("Cancel"))
			{

				Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- rqstnew - Cancel");

				// User selected cancel ; send back to Request List 

				alltelRequestDispatcher.forward("/RequestListCtlr"); 
				return;

			}

			Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- rqstnew - Submit");

			//Coming from the CreateRequestView;
			//validate security - user needs to have security to create a request;
			// SELECT DISTINCT SCRTY_OBJCT_CD from REQUEST_ACTION_T WHERE RQST_STTS_CD_FROM = "CREATE"
			//  AND RQST_TYP_STTS = "status cd from CreateView"
			// If the user has one of the SCRTY_OBJCT codes in their profile they can create the request

			//validate all fields from CreateRequestView needed to create a new Request;	

			String strRqstNewErrMsg = "";
			request.getHttpRequest().setAttribute("NEWRECORD", "1");
			String strRqstPON = request.getParameter("rqstnew_pon");
			if ((strRqstPON == null) || strRqstPON.length() == 0)
			{

		        	Log.write(Log.ERROR, "RequestCtlr() --- Error: A Valid PON is required");

				strRqstNewErrMsg = "Error: A Valid PON is required";
				request.getHttpRequest().setAttribute("rqstnew_errormsg", strRqstNewErrMsg);
				alltelRequestDispatcher.forward("/RequestCreateView.jsp");  
				return;

			}

			String strOCNSttSqncNmbr = request.getParameter("rqstnew_ocnsttsqnc");
			int iOCNSttSqncNmbr = 0;
			if ((strOCNSttSqncNmbr == null) || (strOCNSttSqncNmbr.length() == 0))
			{
		        	Log.write(Log.ERROR, "RequestCtlr() --- Error: A Valid OCN/State combination must be Selected");

				strRqstNewErrMsg = "Error: A Valid OCN/State combination must be Selected";
				request.getHttpRequest().setAttribute("rqstnew_errormsg", strRqstNewErrMsg);

				alltelRequestDispatcher.forward("/RequestCreateView.jsp");  
				return;

			}
			else
			{
				iOCNSttSqncNmbr = Integer.parseInt(strOCNSttSqncNmbr);
			}

			String strRqstSrvcTyp = request.getParameter("rqstnew_srvctyp");
			if ((strRqstSrvcTyp == null) || (strRqstSrvcTyp.length() == 0))
			{
		        	Log.write(Log.ERROR, "RequestCtlr() --- Error: A Valid Service Type must be Selected");

				strRqstNewErrMsg = "Error: A Valid Service Type must be Selected";
				request.getHttpRequest().setAttribute("rqstnew_errormsg", strRqstNewErrMsg);

				alltelRequestDispatcher.forward("/RequestCreateView.jsp");  
				return;

			}

			String strRqstRqstTyp = request.getParameter("rqstnew_rqsttyp");
			if ((strRqstRqstTyp == null) || (strRqstRqstTyp.length() == 0))
			{
		        	Log.write(Log.ERROR, "RequestCtlr() --- Error: A Valid Request Type must be Selected");

				strRqstNewErrMsg = "Error: A Valid Request Type must be Selected";
				request.getHttpRequest().setAttribute("rqstnew_errormsg", strRqstNewErrMsg);

				alltelRequestDispatcher.forward("/RequestCreateView.jsp");  
				return;

			}

			String strRqstActvtyTyp = request.getParameter("rqstnew_actvtytyp");
			if ((strRqstActvtyTyp == null) || (strRqstActvtyTyp.length() == 0))
			{
		        	Log.write(Log.ERROR, "RequestCtlr() --- Error: A Valid Activity Type must be Selected");

				strRqstNewErrMsg = "Error: A Valid Activity Type must be Selected";
				request.getHttpRequest().setAttribute("rqstnew_errormsg", strRqstNewErrMsg);

				alltelRequestDispatcher.forward("/RequestCreateView.jsp");  
				return;

			}
		
			//NOTE this bean newBean goes out of scope
			RequestBean newBean = new RequestBean();
			newBean.setUserid(strUSERID);

			//perform new Request Setup in database ;
			int iRqstSqncNmbrNew = 0;
			int iReturnCode = newBean.getConnection();
			if (iReturnCode == 0) { 
				Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- got connection");
				iReturnCode = newBean.beginTransaction();
				if (iReturnCode == 0) {
					Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- started transaction");
					iRqstSqncNmbrNew = newBean.create(strRqstPON, iOCNSttSqncNmbr, strRqstSrvcTyp, strRqstRqstTyp, strRqstActvtyTyp, iCmpnySqncNmbr);
					if (iRqstSqncNmbrNew > 0) {
						Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- created request");
						iReturnCode = newBean.commitTransaction();
						if (iReturnCode == 0) {
							Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- committed trans");
							iReturnCode = newBean.closeConnection();
						}
					}
					else if (iRqstSqncNmbrNew == newBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
					else {
						iReturnCode = iRqstSqncNmbrNew;
					}
				}
			}

			// if MULTI form, then add extra sections
			if ((strRqstRqstTyp.equals("M")) && (iReturnCode >= 0))
			{
				iReturnCode = newBean.getConnection();
				if (iReturnCode == 0) 
				{
					iReturnCode = newBean.beginTransaction();
				}

				if (iReturnCode == 0) 
				{
					// Determine how many sections to create
					int iNumSctns = PropertiesManager.getIntegerProperty("lsr.multidetail.sections", 100);
Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Sections to create = " + iNumSctns);

					for (int iSctn = 2; iSctn <= iNumSctns; iSctn++)
					{
						// Automatically generate RS_MULTI_DETAIL_T sections
						int iRC = newBean.generateSection(200, iRqstSqncNmbrNew, 0, 4);
						if (iRC == newBean.SECURITY_ERROR)
						{
							alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
							return;
						}
						else if (iRC < 0)
						{
							iReturnCode = iRC;
							break;
						}

						// Automatically generate LR_MULTI_DETAIL_T sections
						iRC = newBean.generateSection(201, iRqstSqncNmbrNew, 0, 2);
						if (iRC == newBean.SECURITY_ERROR)
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

					if (iReturnCode == 0)
					{
						Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- created all sections");
						iReturnCode = newBean.commitTransaction();
						if (iReturnCode == 0) {
							Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- committed trans");
							iReturnCode = newBean.closeConnection();
						}
					}
				}
			}

			if (iReturnCode < 0)
			{
				// there was a problem getting a new Request
				// figure out what the error msg should be and send user back to Request Create View

		        	Log.write(Log.ERROR, "RequestCtlr() --- Error in createRequest() encountered - unable to create request. ReturnCode = " + iReturnCode);

				// for now the duplicate PON message is the only worthwhile message to send back.
				// we should provide a better description of the Error using return code 
				// descriptions from RequestBean.

				if (iReturnCode == newBean.DUP_PON)
				{
					strRqstNewErrMsg = "Error : The PON already exists for the OCN!";
				}
				else
				{
					strRqstNewErrMsg = "Error : Unable to Create the Request. An Application Error was encountered trying to Create the Request!";
				}			
				
				request.getHttpRequest().setAttribute("rqstnew_errormsg", strRqstNewErrMsg);

				alltelRequestDispatcher.forward("/RequestCreateView.jsp");  
				return;
			}

			//show Forms using the 1st form in SERVICE_TYPE_FORM as the default FORM to display

			request.getHttpRequest().setAttribute("rqstformtype","_FIRST_");
			request.getHttpRequest().setAttribute("RQST_SQNC_NMBR", Integer.toString(iRqstSqncNmbrNew));

			alltelRequestDispatcher.forward("/RequestFormView.jsp");  
			return;
		}

		String m_strRqstSqncNmbr = request.getParameter("RQST_SQNC_NMBR");
		String m_strRqstVrsn = request.getParameter("RQST_VRSN");
		String m_strFrmSqncNmbr = request.getParameter("FRM_SQNC_NMBR");
//		String m_strRecordHistoryInd = request.getParameter("REC_HST");

        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- RQST_SQNC_NMBR = " + m_strRqstSqncNmbr);
        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- RQST_VRSN = " + m_strRqstVrsn);
        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- FRM_SQNC_NMBR = " + m_strFrmSqncNmbr);
//       	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- REC_HIST = " + m_strRecordHistoryInd);

		String m_strRqstGet = request.getParameter("seqget");
		if ((m_strRqstGet == null) || (m_strRqstGet.length() == 0))
		{}
		else
		//Coming from the RequestListView or RequestHistoryView where get functions are allowed ;
		//validate user has security to view a request;
		{

	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- seqget : RqstSqncNmbr = " + m_strRqstGet + " ; RqstVrsn = " + m_strRqstVrsn);

			request.getHttpRequest().setAttribute("rqstformtype","_FIRST_");
			request.getHttpRequest().setAttribute("RQST_SQNC_NMBR", m_strRqstGet);
			request.getHttpRequest().setAttribute("RQST_VRSN", m_strRqstVrsn);
			
	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- rqstformtype = _FIRST_");
			String strNotifyOn = request.getParameter("_ntfy_");
			if ((strNotifyOn == null) || (strNotifyOn.length() == 0))	
			{}
			else 
			{	//Do we need to turn off Notify ?
				RequestBean ntfyBean = new RequestBean();
				ntfyBean.setUserid(strUSERID);
				ntfyBean.turnNotifyOff( m_strRqstGet, sdm.getLoginProfileBean().getUserBean().getCmpnyTyp() ); 
			}

			alltelRequestDispatcher.forward("/RequestFormView.jsp");
			return;
		}

		request.getHttpRequest().setAttribute("RQST_SQNC_NMBR", m_strRqstSqncNmbr);
		request.getHttpRequest().setAttribute("RQST_VRSN", m_strRqstVrsn);

		int m_iRqstSqncNmbr = 0;
		if ((m_strRqstSqncNmbr == null) || (m_strRqstSqncNmbr.length() == 0))
		{}
		else
		{
			m_iRqstSqncNmbr = Integer.parseInt(m_strRqstSqncNmbr); 
		}

		int m_iRqstVrsn = 0;
		if ((m_strRqstVrsn == null) || (m_strRqstVrsn.length() == 0))
		{}
		else
		{
			m_iRqstVrsn = Integer.parseInt(m_strRqstVrsn);
		}
	
		int	m_iFrmSqncNmbr = 0;
		if ((m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0))
		{}
		else
		{
			m_iFrmSqncNmbr = Integer.parseInt(m_strFrmSqncNmbr); 
		}

		String m_strRqstNotesUpdate = request.getParameter("notes_update");
		if ((m_strRqstNotesUpdate == null) || (m_strRqstNotesUpdate.length() == 0))
		{}
		else
		{
			String m_strRqstNotesText = request.getParameter("notestext");
			if ((m_strRqstNotesText == null) || (m_strRqstNotesText.length() == 0))
			{
				m_strRqstNotesText = "";
			}

			RequestBean noteBean = new RequestBean();
			noteBean.setUserid(strUSERID);

			int iReturnCode = noteBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = noteBean.beginTransaction();
				if (iReturnCode == 0) {
					iReturnCode = noteBean.updateNotes(m_iRqstSqncNmbr, m_strRqstNotesText);
					if (iReturnCode == 0) {
						iReturnCode = noteBean.commitTransaction();
						if (iReturnCode == 0) {
							iReturnCode = noteBean.closeConnection();
						}
					}
					else if (iReturnCode == noteBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Save Notes Successful");
				// handle telling the user we saved the notes successfully
				request.getHttpRequest().setAttribute("notes", "Notes");
				alltelRequestDispatcher.forward("/RequestNotesView.jsp"); 
				return;
			}
			else
			{
			    	Log.write(Log.ERROR, "RequestCtlr() --- Error Saving Notes");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/RequestErrorView.jsp"); 
				return;
			}
		}

		String m_strRqstPrint = request.getParameter("print");
		if ((m_strRqstPrint == null) || (m_strRqstPrint.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//RQST_SQNC_NMBR needs to be valid parameter;
		//RQST_VRSN needs to be valid parameter;
		//build and display the Print View for the Request
		{
	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- print = " + m_strRqstPrint);

			request.getHttpRequest().setAttribute("print", m_strRqstPrint);
			alltelRequestDispatcher.forward("/RequestFieldPrintView.jsp"); 
			return;
		}

		String m_strRqstPrint2 = request.getParameter("print2");
		if ((m_strRqstPrint2 == null) || (m_strRqstPrint2.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//RQST_SQNC_NMBR needs to be valid parameter;
		//RQST_VRSN needs to be valid parameter;
		//build and display the Print View for the Request
		{
	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- print2 = " + m_strRqstPrint2);

			request.getHttpRequest().setAttribute("print2", m_strRqstPrint2);
			// If you want to show all fields
				request.getHttpRequest().setAttribute("rqstformtype","_FIRST_");

			alltelRequestDispatcher.forward("/RequestFormPrintView.jsp"); 
			return;
		}

		String m_strRqstAtnPrint = request.getParameter("atnprint");
		if ((m_strRqstAtnPrint == null) || (m_strRqstAtnPrint.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//RQST_SQNC_NMBR needs to be valid parameter;
		//RQST_VRSN needs to be valid parameter;
		//build and display the Print View for the Request
		{
	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- atnprint = " + m_strRqstAtnPrint);

			request.getHttpRequest().setAttribute("rqstformtype","_FIRST_");

			alltelRequestDispatcher.forward("/RequestAtnPrintView.jsp"); 
			return;
		}

		//  All actions past this point may first require saving the current data
		//  if we have valid key fields from a FORM with posted data and user had
		//  authority to update data on the form.
		boolean bTransactionExists = false;
		String m_strFrmAuth = request.getParameter("FRM_AUTHORIZATION");
		String m_strMdfdDt = request.getParameter("mdfddt");
                //String m_strSrvcTypCd = request.getParameter("_FF_1_0_23");
                //String m_strActvtyTypCd = request.getParameter("_FF_1_0_24");
                
                String m_strSrvcTypCd = request.getParameter("reqtyp");
                String m_strActvtyTypCd = request.getParameter("act");
                
                
                //code to fix null values issues for serv type and act type
                //m_strSrvcTypCd = strRqstSrvcTyp;
                //m_strActvtyTypCd = strRqstActvtyTyp;
                //String m_strSrvcTypCd = request.getParameter("rqstnew_srvctyp");
                //String m_strActvtyTypCd = request.getParameter("rqstnew_actvtytyp");
                
                
                //String [] paramList = request.getParameterNames();
                
                Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() ---SERV TYPE = " + m_strSrvcTypCd);
                Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() ---ACT TYPE = " + m_strActvtyTypCd);
		Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() ---m_strMdfdDt= " + m_strMdfdDt);

		RequestBean rqstBean = new RequestBean();
		rqstBean.setUserid(strUSERID);


Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- something's null...");
Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() m_strRqstSqncNmbr="+m_strRqstSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() m_strRqstVrsn="+m_strRqstVrsn);
Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() m_strFrmSqncNmbr="+m_strFrmSqncNmbr);
Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() m_strFrmAuth="+m_strFrmAuth);
Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() m_strMdfdDt="+m_strMdfdDt);

		if ((m_strRqstSqncNmbr == null) || (m_strRqstSqncNmbr.length() == 0)
			|| (m_strRqstVrsn == null) || (m_strRqstVrsn.length() == 0)
			|| (m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0)
			|| (m_strFrmAuth == null) || (m_strFrmAuth.length() == 0)
			|| (m_strMdfdDt == null) || (m_strMdfdDt.length() == 0))

		{ }
		else
		{
			// build form field vector and call method to save data here

			Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Save FORM data");

			int iReturnCode = rqstBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = rqstBean.beginTransaction(m_iRqstSqncNmbr, m_strMdfdDt);
				if (iReturnCode == 0) {
					iReturnCode = rqstBean.storeForm(request, m_iFrmSqncNmbr, m_iRqstSqncNmbr, m_iRqstVrsn);
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Save Form Successful");
				// continue on since there may be more to process in this request
				bTransactionExists = true;
			}
			else if (iReturnCode == rqstBean.SECURITY_ERROR)
			{
				alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "RequestCtlr() --- Error Saving Form");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/RequestErrorView.jsp"); 
				return;
			}
		}

		String m_strRqstForm = request.getParameter("rqstform");
		if ((m_strRqstForm == null) || (m_strRqstForm.length() == 0))
		{}
		else
		//Coming from the RequestView - this action is navigation between Forms
		//"rqstform" needs to be a valid form sequence number
		//RQST_SQNC_NMBR and RQST_VRSN need to be valid parameters;
		//validate user has security to view a request;
		//the value of rqstform is the FORM SQNC they want to look at; go get it and display the form
		{

	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- rqstform = " + m_strRqstForm);
	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- rqstformtype = _FRM_CD_");

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = rqstBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = rqstBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("rqstformtype","_FRM_CD_");
				request.getHttpRequest().setAttribute("rqstform", m_strRqstForm);
				alltelRequestDispatcher.forward("/RequestFormView.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "RequestCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/RequestErrorView.jsp"); 
				return;
			}
		}
		
		String strDetailHistSqncNmbr = request.getParameter("dtlhist");
		if ((strDetailHistSqncNmbr== null) || (strDetailHistSqncNmbr.length() == 0))
		{}
		else
		// User wants to view a detailed view of history changes
		{
	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- view history detail "+ strDetailHistSqncNmbr);
			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = rqstBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = rqstBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("histseq", strDetailHistSqncNmbr);
				alltelRequestDispatcher.forward("/RequestDtlHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "RequestCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/RequestErrorView.jsp"); 
				return;
			}
		}

		String m_strRqstHist = request.getParameter("hist");
		if ((m_strRqstHist == null) || (m_strRqstHist.length() == 0))
		{}
		else
		//This action intented to be navigation to view History
		//RQST_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view a request;
		//build and display the History View for the Request
		{

	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- hist = " + m_strRqstHist);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = rqstBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = rqstBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("hist", m_strRqstHist);
				alltelRequestDispatcher.forward("/RequestHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "RequestCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/RequestErrorView.jsp"); 
				return;
			}
		}

		String m_strRqstNotes = request.getParameter("notes");
		if ((m_strRqstNotes == null) || (m_strRqstNotes.length() == 0))
		{}
		else
		//RQST_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view request NOTES;
		//build and display the Notes View for the Request
		{

	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- notes = " + m_strRqstNotes);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = rqstBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = rqstBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("notes", m_strRqstNotes);
				alltelRequestDispatcher.forward("/RequestNotesView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "RequestCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/RequestErrorView.jsp"); 
				return;
			}
		}

		String m_strRqstAction = request.getParameter("action");
		if ((m_strRqstAction == null) || (m_strRqstAction.length() == 0))
		{}
		else
		//This action intented to be a status change
		//or a request to SAVE the current form data or a request to view Validation ERRORS.
		//RQST_SQNC_NMBR needs to be a valid parameter;
		//RQST_VRSN will always be the current version;
		//validate the action can be performed based on security and then take appropriate action
		//when done go back to RequestListCtlr if it's a valid Status change.
		{	

	        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- action = " + m_strRqstAction);
			request.getHttpRequest().setAttribute("action", m_strRqstAction);

			if ( m_strRqstAction.equals("Save"))
			{
				// we already saved the data prior to getting here ; just set some fields & get out
				request.getHttpRequest().setAttribute("RQST_VRSN", m_strRqstVrsn);
				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("rqstformtype","_FRM_SQNC_");
				int iReturnCode = 0;
                                 
				// Set Notify Indicator on list ???       
				Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- before rqstBean.setNotify() ");
				iReturnCode = rqstBean.setNotify(m_iRqstSqncNmbr, m_strRqstAction);
				Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- before rqstBean.setNotify() ");

				if (bTransactionExists)
				{
					iReturnCode = rqstBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = rqstBean.closeConnection();
					}
				}
				if (iReturnCode == 0)
				{
					Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- rqstformtype = _FRM_SQNC_");
					alltelRequestDispatcher.forward("/RequestFormView.jsp");  
					return;
				}
				else
				{
					 Log.write(Log.ERROR, "RequestCtlr() --- Transaction Error");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/RequestErrorView.jsp"); 
					return;
				}
			}			
		
			// Handle "View Validation Errors" Action for viewing Field Validations

			if ( m_strRqstAction.equals("Validate"))
			{
				int iReturnCode = 0; 
				int iRqstVldtnErrs = 0;
				
				// Set Notify Indicator on list ???     (do for Validate too,cuz you can chg data
				// and then hit Validate button
				Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- before rqstBean.setNotify() save");
				iReturnCode = rqstBean.setNotify(m_iRqstSqncNmbr, "Save");
				Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- before rqstBean.setNotify() save");

				if (bTransactionExists == true)
				{
					iReturnCode = rqstBean.commitTransaction();
				}
				else
				{
					iReturnCode = rqstBean.getConnection();
				}

				if (iReturnCode == 0) 
				{
					iRqstVldtnErrs = rqstBean.validateFields(request, m_iRqstSqncNmbr, m_iRqstVrsn, "A", "");
				}
						
				iReturnCode = rqstBean.closeConnection();
				
				alltelRequestDispatcher.forward("/RequestValidationView.jsp");  
				return;

			}			
						
			//Handle all other actions based on REQUEST_ACTION_T workflow
			Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Request Status Change");

			int iReturnCode = 0; 
			if (bTransactionExists == false)
			{
				iReturnCode = rqstBean.getConnection();
				if (iReturnCode == 0) {
					iReturnCode = rqstBean.beginTransaction();
				}
			}
			else
			{
				iReturnCode = rqstBean.commitTransaction();
			}

			if (iReturnCode == 0) {
				iReturnCode = rqstBean.changeStatus(request, m_iRqstSqncNmbr, m_strRqstAction, m_iRqstVrsn);
				if (iReturnCode > 0) {
					iReturnCode = rqstBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = rqstBean.closeConnection();
					}
				}
				else if (iReturnCode == rqstBean.SECURITY_ERROR)
				{
					alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
					return;
				}
			}

			if (iReturnCode >= 0)
			{
					request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
					request.getHttpRequest().setAttribute("rqstformtype","_FRM_SQNC_");

					Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Status Change Successful");
                                        // handle telling the user we saved the form successfully
                                        //Action_T navigation change
                                        String strActnDstntn = null;
                                        ActionManager am = new ActionManager();
                                        strActnDstntn = am.getActionDestination(rqstBean.getSttsCdFrom(), rqstBean.getTypInd(), rqstBean.getRqstTypCd(), rqstBean.getSttsCdTo(), m_strRqstAction);
                                        
                /*
                 * Start This portion code will do the Automation logics
                 *
                 */
                //   Log.write(" Automation process starting Request Number "+m_strRqstSqncNmbr);


                // End This portion code will do the Automation logics Start

              /* below code commented for Automation process
               * here we need to redirect to LSRBaseController
               */
               if (!m_strRqstAction.equals("Submit")) {

					if (strActnDstntn.length() > 0) {
					  Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() -- Next action based on Action_T is: "+strActnDstntn);
                                          //Set the Version in request object to null so that the JSP can pick up the latest version.
                                          request.getHttpRequest().setAttribute("RQST_VRSN", null);
                                          alltelRequestDispatcher.forward("/"+strActnDstntn);  
                                        } else {
    					  alltelRequestDispatcher.forward("/RequestListCtlr");  
                                        }

              /*
               * Automation process will start
               */

                Log.write("before m_strRqstAction  "+m_strRqstAction +" m_strRqstSqncNmbr "+m_strRqstSqncNmbr);

               /* if(m_strRqstAction.equals("Open for Review")){
                *    alltelRequestDispatcher.forward("/RequestFormView.jsp");
                *    return;
                *}
                */

		       } else {
                           Log.write("before Automation process ");
                                           //satish code for no automation for version > 0
                                           //start
                           Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() m_strSrvcTypCd="+m_strSrvcTypCd);
                           Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() m_strActvtyTypCd="+m_strActvtyTypCd);
                           
                           LSRdao lsrDao = new LSRdao();
                           
                           if(m_strSrvcTypCd == null) {
                               m_strSrvcTypCd = lsrDao.getServiceType(m_strRqstSqncNmbr);
                               Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() from db m_strSrvcTypCd="+m_strSrvcTypCd);
                           
                           }
                           
                           if(m_strActvtyTypCd == null) {
                               m_strActvtyTypCd = lsrDao.getActivityType(m_strRqstSqncNmbr);
                               Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() from db m_strActvtyTypCd="+m_strActvtyTypCd);
                               
                           }
                            //Update submitted date in request_t table - Vijay - 04-06-12
                          lsrDao.updateSubmittedDate(m_strRqstSqncNmbr,sdm.getUser());

                   String m_strAtn = lsrDao.getATN(m_strRqstSqncNmbr, m_strRqstVrsn, m_strSrvcTypCd, m_strActvtyTypCd);
                   Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() from db m_strAtn="+m_strAtn);
                                                    
                   String sent_tn = lsrDao.checkNuvoxTNStatus(m_strAtn);
                   Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() from db sent_tn="+sent_tn);

                   if(sent_tn != null) {

					   lsrDao.updateNuvox(m_strRqstSqncNmbr, m_strRqstVrsn, sent_tn);

					   alltelRequestDispatcher.forward("/RequestListCtlr");

			       } else {
                           
                           if((m_iRqstVrsn>0) && !(m_strSrvcTypCd.equals("C") && m_strActvtyTypCd.equals("V"))){
                             LSRdao objLSRdao=new LSRdao();
                             objLSRdao.updateInternalStatus(m_strRqstSqncNmbr,m_strRqstVrsn,sdm.getUser());
                             alltelRequestDispatcher.forward("/RequestListCtlr");
                           }             //end
                           else if(m_strRqstSqncNmbr!=null && ((m_iRqstVrsn == 0) || ((m_iRqstVrsn > 0) && 
                                   (m_strSrvcTypCd.equals("C") && m_strActvtyTypCd.equals("V"))))){
                              Log.write(" Automation process starting Request Number "+m_strRqstSqncNmbr+" Vrsn "+m_strRqstVrsn);
                              request.getHttpRequest().setAttribute("RQST_VRSN", null);
                              alltelRequestDispatcher.forward("/LSRBaseController?reqNo="+m_strRqstSqncNmbr+"&reqVer="+m_strRqstVrsn+"&reqUrl=RequestListCtlr");
                              Log.write("Completed And Commited the New Request Kumar ");
                           }else{
                              alltelRequestDispatcher.forward("/RequestListCtlr");
                           }
		       }
		       }
                                        return;
			}
			else
			{
				if (iReturnCode == rqstBean.VALIDATION_ERROR)
				{
					Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Validation Errors with Form Fields - Sending to Error Validation View");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/RequestValidationView.jsp");  
					return;
				}
				else
				{
					Log.write(Log.ERROR, "RequestCtlr() --- Error Changing Request Status");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/RequestErrorView.jsp");  
					return;
				}
			}
		}

		//Now check to see if we got here with a request to add a section for a FORM.
		//Required parameters are RQST_SQNC_NMBR, RQST_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair describing the section to add.
		//The parameter name will be "add_sctn_?" and the value will be "Add Section".
		String m_strRqstAddSctn = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
			m_strRqstAddSctn = request.getParameter("add_sctn_" + m_iFrmSctnSqncNmbr);

			if ((m_strRqstAddSctn == null) || (m_strRqstAddSctn.length() == 0))
			{}
			else
			{
				if (m_strRqstAddSctn.equals("Add Section"))
				{
					//we know now the user submitted a valid request to add a new section
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- add_sctn");

					int m_iFrmSctnOccNew = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = rqstBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = rqstBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) 
					{	m_iFrmSctnOccNew = rqstBean.generateSection(m_iFrmSqncNmbr, m_iRqstSqncNmbr, m_iRqstVrsn, m_iFrmSctnSqncNmbr);
						if (m_iFrmSctnOccNew > 0) {
							//iReturnCode = rqstBean.commitTransaction();
							//if (iReturnCode == 0) {
							//	iReturnCode = rqstBean.closeConnection();
							//}
						}
						else if (m_iFrmSctnOccNew == rqstBean.SECURITY_ERROR)
						{
							alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
							return;
						}
						else {
							iReturnCode = m_iFrmSctnOccNew;
						}
						if (iReturnCode >= 0) //OK to now...
						{	//If multi-form, keep LR (Multi) in synch
							//& automatically generate LR_MULTI_DETAIL_T sections
							if ( m_iFrmSqncNmbr == 200 && m_iFrmSctnSqncNmbr == 4)
							{
Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- auto adding LR-MULTI sections ");
								int iRC = rqstBean.generateSection(201, m_iRqstSqncNmbr, m_iRqstVrsn, 2);
								if (iRC == rqstBean.SECURITY_ERROR)
								{
									alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
									return;
								}
								else if (iRC < 0)
								{
									iReturnCode = iRC;
								}
								
							}
							iReturnCode = rqstBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = rqstBean.closeConnection();
							}
						}
						
					}
					if (iReturnCode >= 0)
					{
						request.getHttpRequest().setAttribute("RQST_VRSN", m_strRqstVrsn);           
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("rqstformtype","_FRM_SQNC_");

				        Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- m_iFrmSctnOccNew = " + m_iFrmSctnOccNew);
						//Here we need this section we added-cause we want the cursor
						// in the first field of it when the form is presented.
						request.getHttpRequest().setAttribute("NEW_OCC", Integer.toString(m_iFrmSctnOccNew));
						request.getHttpRequest().setAttribute("NEW_SECTION", Integer.toString(m_iFrmSctnSqncNmbr));

				        Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- rqstformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/RequestFormView.jsp"); 
						return;

					}
					else
					{
				        	Log.write(Log.ERROR, "RequestCtlr() --- Error getting a new section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/RequestErrorView.jsp");  
						return;
					}
				}
			}
		}

		//Now check to see if we got here with a request to delete a section for a FORM.
		//Required parameters are RQST_SQNC_NMBR, RQST_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair identifying the
		// the SECTION SEQUENCE number and the SECTION OCCURRENCE that needs to be deleted.
		//The parameter name will be "del_sctn_?_?" and the value will be "Delete Section".
		String m_strRqstDltSctnOcc = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
		  for (int m_iSctnOcc = 1; m_iSctnOcc < 100 ; m_iSctnOcc++)
		  {	
			m_strRqstDltSctnOcc = request.getParameter("del_sctn_" + m_iFrmSctnSqncNmbr + "_" + m_iSctnOcc);

			if ((m_strRqstDltSctnOcc == null) || (m_strRqstDltSctnOcc.length() == 0))
			{}
			else
			{
				if (m_strRqstDltSctnOcc.equals("Delete Section"))
				{
					//we know now the user submitted a valid request to delete a section occurrence
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- del_sctn");
			        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- m_iFrmSctnSqncNmbr = " + m_iFrmSctnSqncNmbr);
			        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- m_iSctnOcc = " + m_iSctnOcc);

					int iFrmSctnDltOcc = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = rqstBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = rqstBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						iFrmSctnDltOcc = rqstBean.deleteSection(m_iFrmSqncNmbr, m_iRqstSqncNmbr, m_iRqstVrsn, m_iFrmSctnSqncNmbr, m_iSctnOcc);						
						if (iFrmSctnDltOcc == 0) {
							iReturnCode = rqstBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = rqstBean.closeConnection();
							}
						}
						else if (iFrmSctnDltOcc == rqstBean.SECURITY_ERROR)
						{
							alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
							return;
						}
					}
					if (iReturnCode == 0)
					{
						request.getHttpRequest().setAttribute("RQST_VRSN", m_strRqstVrsn);                      
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("rqstformtype","_FRM_SQNC_");

				        	Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Delete Section Occurrence Successful");
						Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- rqstformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/RequestFormView.jsp"); 
						return;
					}
					else
					{
				        	Log.write(Log.ERROR, "RequestCtlr() --- Error deleting a section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/RequestErrorView.jsp");  
						return;
					}
				}
			}
		  }
		}
                
		//Roll back and release the connection before the Navigation error.
		Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Release the Connection Before Navigation Error");
		int iReturnCode = 0;
		if (bTransactionExists)
		{
                    iReturnCode = rqstBean.rollbackTransaction();
                    if (iReturnCode == 0) {
                        iReturnCode = rqstBean.closeConnection();
                    }
		}
                
		// if we dropped down to here, we don't know where we're supposed to go and
		// we apparently have a navigation error.

		Log.write(Log.DEBUG_VERBOSE, "RequestCtlr() --- Navigation Error!");

		alltelRequestDispatcher.forward("/NavigationErrorView.jsp");
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}

