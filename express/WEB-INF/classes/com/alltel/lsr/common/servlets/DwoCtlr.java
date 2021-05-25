/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2005
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:		DwoCtlr.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	pjs 4-19-2005 Logical Change Orders
 *	pjs 7-12 Gunther was a great dog - Business Data Prods. Not the best solution, but using a
 *		 session level flag to determine KPEN orders or Bus Data prod orders.
 *	
 */

package com.alltel.lsr.common.servlets;

import com.alltel.lsr.common.util.PropertiesManager;
import com.alltel.lsr.common.error.objects.ExceptionHandler;
import com.alltel.lsr.common.util.Log;

import com.windstream.camscrossreference.CamsCrossReferenceRequestData;
import com.windstream.camscrossreference.CamsCrossReferenceReplyData;
import com.windstream.camscrossreference.CamsCrossReferenceRequestStructure;
import com.windstream.camscrossreference.CamsCrossReferenceReplyStructure;
import com.windstream.camscrossreference.ApplicationInfo;

import camscrossreference.windstream.com.CamsCrossReferenceWebLayer;
import camscrossreference.windstream.com.CamsCrossReferenceWebLayer_Stub;
import camscrossreference.windstream.com.WebInputProxy;
import camscrossreference.windstream.com.WebInputProxy_Impl;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
//import java.text.SimpleDateFormat;

//---------------------all import statements above added by Antony for Cust Acct Nmbr 
//---------------------update project

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class DwoCtlr extends AlltelServlet
{
	private final static String KPEN_TBL = "19";
	private final static String KPEN_QC = "Kpen";
	private final static String KPEN_VIEW = "DwoCreateView.jsp";
	private final static String BDP_TBL = "21";
	private final static String BDP_QC = "Bdp";
	private final static String BDP_VIEW = "DwoCreateView2.jsp";

	public void myservice (AlltelRequest request, AlltelResponse response)
			throws Exception
	{	

		String strCreateURL = KPEN_VIEW;

        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr()");

		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		String strUSERID = sdm.getUser();
		String	strCmpnySqncNmbr =  sdm.getLoginProfileBean().getUserBean().getCmpnySqncNmbr();
		int  	iCmpnySqncNmbr = 0;
		if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0))
		{}
		else {
			iCmpnySqncNmbr =  Integer.parseInt(strCmpnySqncNmbr);
		}
		String strPJVN = (String) request.getSession().getAttribute("DwOcHoIcE");
		String strTypInd = "";
		if (strPJVN==null) {
			strPJVN=KPEN_QC;	//Defalut to KPEN
			strTypInd = "W";
		}
		boolean bBDP=false;
		if (strPJVN.equals(KPEN_QC))
		{
			strPJVN="?pjvn="+ KPEN_TBL;
			strTypInd = "W";
		}
		else {
			bBDP = true;		//Business Data Products
			strPJVN="?pjvn="+ BDP_TBL;
			strCreateURL = BDP_VIEW;
			strTypInd = "X";
		}
			
Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- User: "+ strUSERID + " Company: " + iCmpnySqncNmbr + " ordertype="+ strPJVN);

		String m_strDwoCreate = request.getParameter("dwocreate");
		if ((m_strDwoCreate == null) || (m_strDwoCreate.length() == 0))
		{}
		else
		//Coming from DwoListView;
		//validate security - user needs to have security to create a dwo
		// SELECT DISTINCT SCRTY_OBJCT_CD from DWO_ACTION_T WHERE DWO_STTS_CD_FROM = "CREATE"
		// If the user has one of the SCRTY_OBJCT codes in their profile they can go to the create view
		//Send to Dwo Create View to enter required fields for entering a new dwo
		{
			Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- dwocreate fwd to "+strCreateURL);
			alltelRequestDispatcher.forward("/"+strCreateURL);  
			return;
		}

		String m_strDwoNew = request.getParameter("dwonew");
		if ((m_strDwoNew == null) || (m_strDwoNew.length() == 0))
		{}
		else
		{
			if (m_strDwoNew.equals("Cancel"))
			{

				Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- dwonew - Cancel");

				// User selected cancel ; send back to Dwo List 
				alltelRequestDispatcher.forward("/DwoListCtlr"+strPJVN); 
				return;

			}

			Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- dwonew - Submit");
                        
                        
                       
                       
                        //Coming from the CreateDwoView;
			//validate security - user needs to have security to create a dwo;
			// SELECT DISTINCT SCRTY_OBJCT_CD from DWO_ACTION_T WHERE DWO_STTS_CD_FROM = "CREATE"
			//  AND DWO_TYP_STTS = "status cd from CreateView"
			// If the user has one of the SCRTY_OBJCT codes in their profile they can create the dwo

			//validate all fields from CreateDwoView needed to create a new Dwo;	

			String strDwoNewErrMsg = "";

			// Get OCN State Sequence Number
			String strOCNCd = request.getParameter("dwonew_ocncd");
			if ((strOCNCd == null) || (strOCNCd.length() == 0))
			{
		        	Log.write(Log.ERROR, "DwoCtlr() --- Error: A Valid Site must be Selected");

				strDwoNewErrMsg = "Error: A Site must be Selected";
				request.getHttpRequest().setAttribute("dwonew_errormsg", strDwoNewErrMsg);

				//alltelRequestDispatcher.forward("/DwoCreateView.jsp");  
				alltelRequestDispatcher.forward("/"+strCreateURL);  
				return;
			}

			// Get Product Code
			String strPrdctTypCd = request.getParameter("dwonew_prdcttyp");
			if ((strPrdctTypCd == null) || (strPrdctTypCd.length() == 0))
			{
		        	Log.write(Log.ERROR, "DwoCtlr() --- Error: A Valid Product Code must be Selected");

				strDwoNewErrMsg = "Error: A Valid Product Code must be Selected";
				request.getHttpRequest().setAttribute("dwonew_errormsg", strDwoNewErrMsg);

				//alltelRequestDispatcher.forward("/DwoCreateView.jsp");  
				alltelRequestDispatcher.forward("/"+strCreateURL);  
				return;
			}

			// Get Service Type Code
			String strSrvcTypCd = request.getParameter("dwonew_srvctyp");
			if ((strSrvcTypCd == null) || (strSrvcTypCd.length() == 0))
			{
		        	Log.write(Log.ERROR, "DwoCtlr() --- Error: A Valid Order Type Code must be Selected");
				strDwoNewErrMsg = "Error: A Valid Order Type Code must be Selected";
				request.getHttpRequest().setAttribute("dwonew_errormsg", strDwoNewErrMsg);
				//alltelRequestDispatcher.forward("/DwoCreateView.jsp");  
				alltelRequestDispatcher.forward("/"+strCreateURL);  
				return;
			}
			//Only permit "NEW" site with a NEW order type
			if ((strOCNCd != null) && (strOCNCd.equals("New")))
			{
				if ((strSrvcTypCd != null) && (!strSrvcTypCd.equals("D")))
				{
					Log.write(Log.ERROR, "DwoCtlr() --- Error: Order Type must be New for a New Site");
					strDwoNewErrMsg = "Error: Order Type must be New for a New Site";
					request.getHttpRequest().setAttribute("dwonew_errormsg", strDwoNewErrMsg);
					//alltelRequestDispatcher.forward("/DwoCreateView.jsp");  
					alltelRequestDispatcher.forward("/"+strCreateURL);  
					return;
				}
			}

			// Get Change Type Code - Null is OK
			String strChgTypCd = request.getParameter("dwonew_acttyp");
			if ((strChgTypCd == null) || (strChgTypCd.length() == 0))
			{
				if (strSrvcTypCd.equals("A"))
				{
					Log.write(Log.ERROR, "DwoCtlr() --- Error: A Valid Change Type Code must be Selected");
					strDwoNewErrMsg = "Error: A Valid Change Type must be Selected";
					request.getHttpRequest().setAttribute("dwonew_errormsg", strDwoNewErrMsg);
					//alltelRequestDispatcher.forward("/DwoCreateView.jsp");  
					alltelRequestDispatcher.forward("/"+strCreateURL);  
					return;
				}
				else
				{
					strChgTypCd="";
				}
			}

			// Get Change SUB-Type Code
			String strChgSubTypCd = request.getParameter("dwonew_actsubtyp");
			if ((strChgSubTypCd == null) || (strChgSubTypCd.length() == 0))
			{
				strChgSubTypCd = "";
			}
Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- Product: "+ strPrdctTypCd);
Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- SrvcTyp: "+ strSrvcTypCd);
Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- ChngTyp: "+ strChgTypCd);
Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- ChgSubT: "+ strChgSubTypCd);

			//note newDwoBean will go out of scope....
			DwoBean newDwoBean = new DwoBean(strTypInd);
			newDwoBean.setUserid(strUSERID);
	
			//perform new Dwo Setup in database ;
			int iDwoSqncNmbr = 0;
			
			// seqnum is a hidden parameter from SIS customer search to retreive 
			// selected SIS match from temp table, SIS_CUSTOMER_SEARCH_T if customer search form
			// was used before create order!.
			
			String strSearchSeq = request.getParameter("seqnum");							
			int iReturnCode = newDwoBean.getConnection();
			if (iReturnCode == 0) { 
				Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- got connection");
				iReturnCode = newDwoBean.beginTransaction();
				if (iReturnCode == 0) {
					Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- started transaction");
					iDwoSqncNmbr = newDwoBean.create(strOCNCd, iCmpnySqncNmbr, strPrdctTypCd, strSrvcTypCd, strChgTypCd, strChgSubTypCd, strSearchSeq );
					if (iDwoSqncNmbr > 0) {
						Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- created dwo");
						iReturnCode = newDwoBean.commitTransaction();
						if (iReturnCode == 0) {
							Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- committed trans");
							iReturnCode = newDwoBean.closeConnection();
						}
					}
					else if (iDwoSqncNmbr == newDwoBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
					else
					{
						iReturnCode = iDwoSqncNmbr;
					}
				}
			}

			if (iReturnCode < 0)
			{
				// there was a problem getting a new Dwo
				// figure out what the error msg should be and send user back to Dwo Create View

		        	Log.write(Log.ERROR, "DwoCtlr() --- Error in createDwo() encountered - unable to create dwo. ReturnCode = " + iReturnCode);

				// for now the duplicate PON message is the only worthwhile message to send back.
				// we should provide a better description of the Error using return code 
				// descriptions from DwoBean.

				if (iReturnCode == newDwoBean.DUP_PON)
				{
					strDwoNewErrMsg = "Error : The Dwo Number already exists!";
				}
				else
				{
					strDwoNewErrMsg = "Error : Unable to Create the Dwo. An Application Error was encountered trying to Create the Dwo!";
				}			
				
				request.getHttpRequest().setAttribute("dwonew_errormsg", strDwoNewErrMsg);

				//alltelRequestDispatcher.forward("/DwoCreateView.jsp");  
				alltelRequestDispatcher.forward("/"+strCreateURL);  
				return;
			}

			//show Forms using the 1st form in SERVICE_TYPE_FORM as the default FORM to display
			request.getHttpRequest().setAttribute("dwoformtype","_FIRST_");
			request.getHttpRequest().setAttribute("DWO_SQNC_NMBR", Integer.toString(iDwoSqncNmbr));
			request.getHttpRequest().setAttribute("NEWRECORD", "1");
			alltelRequestDispatcher.forward("/DwoFormView.jsp");  
			return;
		}

		String m_strDwoSqncNmbr = request.getParameter("DWO_SQNC_NMBR");
		String m_strDwoVrsn = request.getParameter("DWO_VRSN");
		String m_strFrmSqncNmbr = request.getParameter("FRM_SQNC_NMBR");

                
        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- DWO_SQNC_NMBR = " + m_strDwoSqncNmbr);
        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- DWO_VRSN = " + m_strDwoVrsn);
        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- FRM_SQNC_NMBR = " + m_strFrmSqncNmbr);

		String m_strDwoGet = request.getParameter("seqget");
		if ((m_strDwoGet == null) || (m_strDwoGet.length() == 0))
		{}
		else
		//Coming from the DwoListView or DwoHistoryView where get functions are allowed ;
		//validate user has security to view a dwo;
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- seqget : DwoSqncNmbr = " + m_strDwoGet + " ; DwoVrsn = " + m_strDwoVrsn);

			request.getHttpRequest().setAttribute("dwoformtype","_FIRST_");
			request.getHttpRequest().setAttribute("DWO_SQNC_NMBR", m_strDwoGet);
			request.getHttpRequest().setAttribute("DWO_VRSN", m_strDwoVrsn);
			
	        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- dwoformtype = _FIRST_");

			alltelRequestDispatcher.forward("/DwoFormView.jsp");
			return;
		}

		request.getHttpRequest().setAttribute("DWO_SQNC_NMBR", m_strDwoSqncNmbr);
		request.getHttpRequest().setAttribute("DWO_VRSN", m_strDwoVrsn);

		int m_iDwoSqncNmbr = 0;
		if ((m_strDwoSqncNmbr == null) || (m_strDwoSqncNmbr.length() == 0))
		{}
		else
		{
			m_iDwoSqncNmbr = Integer.parseInt(m_strDwoSqncNmbr); 
		}

		int m_iDwoVrsn = 0;
		if ((m_strDwoVrsn == null) || (m_strDwoVrsn.length() == 0))
		{}
		else
		{
			m_iDwoVrsn = Integer.parseInt(m_strDwoVrsn);
		}
	
		int	m_iFrmSqncNmbr = 0;
		if ((m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0))
		{}
		else
		{
			m_iFrmSqncNmbr = Integer.parseInt(m_strFrmSqncNmbr); 
		}

		String m_strDwoNotesUpdate = request.getParameter("notes_update");
		if ((m_strDwoNotesUpdate == null) || (m_strDwoNotesUpdate.length() == 0))
		{}
		else
		{
			//note noteDwoBean will go out of scope....
			DwoBean noteDwoBean = new DwoBean(strTypInd);
			noteDwoBean.setUserid(strUSERID);

			String m_strDwoNotesText = request.getParameter("notestext");
			if ((m_strDwoNotesText == null) || (m_strDwoNotesText.length() == 0))
			{
				m_strDwoNotesText = "";
			}

			int iReturnCode = noteDwoBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = noteDwoBean.beginTransaction();
				if (iReturnCode == 0) {
					iReturnCode = noteDwoBean.updateNotes(m_iDwoSqncNmbr, m_strDwoNotesText);
					if (iReturnCode == 0) {
						iReturnCode = noteDwoBean.commitTransaction();
						if (iReturnCode == 0) {
							iReturnCode = noteDwoBean.closeConnection();
						}
					}
					else if (iReturnCode == noteDwoBean.SECURITY_ERROR)
					{
						alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
						return;
					}
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- Save Notes Successful");
				// handle telling the user we saved the notes successfully
				request.getHttpRequest().setAttribute("notes", "Notes");
				alltelRequestDispatcher.forward("/DwoNotesView.jsp"); 
				return;
			}
			else
			{
			    	Log.write(Log.ERROR, "DwoCtlr() --- Error Saving Notes");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DwoErrorView.jsp"); 
				return;
			}
		}

		/************ SIS TEST SECTION PLEASE DON't DEPLOY HIS SECTION TO PROD*****/
		
		String m_strDwoPrintXml = request.getParameter("printxml");
		if ((m_strDwoPrintXml == null) || (m_strDwoPrintXml.length() == 0))
		{}
		else		
		{
	        Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- print = " + m_strDwoPrintXml);
			request.getHttpRequest().setAttribute(m_strDwoPrintXml, m_strDwoPrintXml);
			request.getHttpRequest().setAttribute("dwo_typeInd",  strTypInd );
			
			alltelRequestDispatcher.forward("/xmlSample.jsp"); 
			return;
		}
		
		String m_strDwoPrint = request.getParameter("print");
		if ((m_strDwoPrint == null) || (m_strDwoPrint.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//DWO_SQNC_NMBR needs to be valid parameter;
		//DWO_VRSN needs to be valid parameter;
		//build and display the Print View for the Dwo
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- print = " + m_strDwoPrint);

			request.getHttpRequest().setAttribute("print", m_strDwoPrint);
			alltelRequestDispatcher.forward("/DwoFieldPrintView.jsp"); 
			return;
		}

		String m_strDwoPrint2 = request.getParameter("print2");
		if ((m_strDwoPrint2 == null) || (m_strDwoPrint2.length() == 0))
		{}
		else
		//This action intented to be navigation to Print
		//DWO_SQNC_NMBR needs to be valid parameter;
		//DWO_VRSN needs to be valid parameter;
		//build and display the Print View for the Dwo
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- print2 = " + m_strDwoPrint2);

			request.getHttpRequest().setAttribute("print2", m_strDwoPrint2);
			// If you want to show all fields
				request.getHttpRequest().setAttribute("dwoformtype","_FIRST_");

			alltelRequestDispatcher.forward("/DwoFormPrintView.jsp"); 
			return;
		}

		//  All actions past this point may first require saving the current data
		//  if we have valid key fields from a FORM with posted data and user had
		//  authority to update data on the form.
		boolean bTransactionExists = false;
		String m_strFrmAuth = request.getParameter("FRM_AUTHORIZATION");
		String m_strMdfdDt = request.getParameter("mdfddt");
		Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() ---m_strMdfdDt= " + m_strMdfdDt);
			
		DwoBean dwoBean = new DwoBean(strTypInd);
		dwoBean.setUserid(strUSERID);

		if ((m_strDwoSqncNmbr == null) || (m_strDwoSqncNmbr.length() == 0)
			|| (m_strDwoVrsn == null) || (m_strDwoVrsn.length() == 0)
			|| (m_strFrmSqncNmbr == null) || (m_strFrmSqncNmbr.length() == 0)
			|| (m_strFrmAuth == null) || (m_strFrmAuth.length() == 0)
			|| (m_strMdfdDt == null) || (m_strMdfdDt.length() == 0))

		{ }
		else
		{
			// build form field vector and call method to save data here

			Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- Save FORM data");

			int iReturnCode = dwoBean.getConnection();
			if (iReturnCode == 0) {
				iReturnCode = dwoBean.beginTransaction(m_iDwoSqncNmbr, m_strMdfdDt);
				if (iReturnCode == 0) {
					iReturnCode = dwoBean.storeForm(request, m_iFrmSqncNmbr, m_iDwoSqncNmbr, m_iDwoVrsn);
				}
			}
			if (iReturnCode == 0)
			{
				Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- Save Form Successful");
				// continue on since there may be more to process in this dwo
				bTransactionExists = true;
                                                             
			}
			else if (iReturnCode == dwoBean.SECURITY_ERROR)
			{
				alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DwoCtlr() --- Error Saving Form");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DwoErrorView.jsp"); 
				return;
			}
		}

		String m_strDwoForm = request.getParameter("dwoform");
		if ((m_strDwoForm == null) || (m_strDwoForm.length() == 0))
		{}
		else
		//Coming from the DwoView - this action is navigation between Forms
		//"dwoform" needs to be a valid form sequence number
		//DWO_SQNC_NMBR and DWO_VRSN need to be valid parameters;
		//validate user has security to view a dwo;
		//the value of dwoform is the FORM SQNC they want to look at; go get it and display the form
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- dwoform = " + m_strDwoForm);
	        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- dwoformtype = _FRM_CD_");

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dwoBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dwoBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("dwoformtype","_FRM_CD_");
				request.getHttpRequest().setAttribute("dwoform", m_strDwoForm);
				alltelRequestDispatcher.forward("/DwoFormView.jsp");  
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DwoCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DwoErrorView.jsp"); 
				return;
			}
		}
		
		String m_strDwoHist = request.getParameter("hist");
		if ((m_strDwoHist == null) || (m_strDwoHist.length() == 0))
		{}
		else
		//This action intented to be navigation to view History
		//DWO_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view a dwo;
		//build and display the History View for the Dwo
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- hist = " + m_strDwoHist);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dwoBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dwoBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("hist", m_strDwoHist);
				alltelRequestDispatcher.forward("/DwoHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DwoCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DwoErrorView.jsp"); 
				return;
			}
		}
	
		String strDetailHistSqncNmbr = request.getParameter("dtlhist");
		if ((strDetailHistSqncNmbr== null) || (strDetailHistSqncNmbr.length() == 0))
		{}
		else
		// User wants to view a detailed view of history changes
		{
	        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- view histroy detail "+ strDetailHistSqncNmbr);
			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dwoBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dwoBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("histseq", strDetailHistSqncNmbr);
				alltelRequestDispatcher.forward("/DwoDtlHistoryView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DwoCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DwoErrorView.jsp"); 
				return;
			}
		}

		String m_strDwoNotes = request.getParameter("notes");
		if ((m_strDwoNotes == null) || (m_strDwoNotes.length() == 0))
		{}
		else
		//DWO_SQNC_NMBR needs to be valid parameter;
		//validate user has security to view dwo NOTES;
		//build and display the Notes View for the Dwo
		{

	        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- notes = " + m_strDwoNotes);

			int iReturnCode = 0;
			if (bTransactionExists)
			{
				iReturnCode = dwoBean.commitTransaction();
				if (iReturnCode == 0) {
					iReturnCode = dwoBean.closeConnection();
				}
			}
			if (iReturnCode == 0)
			{
				request.getHttpRequest().setAttribute("notes", m_strDwoNotes);
				alltelRequestDispatcher.forward("/DwoNotesView.jsp"); 
				return;
			}
			else
			{
			     	Log.write(Log.ERROR, "DwoCtlr() --- Transaction Error");
				// handle telling the user we had an error !!!
				alltelRequestDispatcher.forward("/DwoErrorView.jsp"); 
				return;
			}
		}

		String m_strDwoAction = request.getParameter("action");
		if ((m_strDwoAction == null) || (m_strDwoAction.length() == 0))
		{}
		else
		//This action intented to be a status change
		//or a dwo to SAVE the current form data or a dwo to view Validation ERRORS.
		//DWO_SQNC_NMBR needs to be a valid parameter;
		//DWO_VRSN will always be the current version;
		//validate the action can be performed based on security and then take appropriate action
		//when done go back to DwoListCtlr if it's a valid Status change.
		{	

			Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- action = " + m_strDwoAction);
			request.getHttpRequest().setAttribute("action", m_strDwoAction);
                       
			if ( m_strDwoAction.equals("Save"))
			{
				// we already saved the data prior to getting here ; just set some fields & get out
				request.getHttpRequest().setAttribute("DWO_VRSN", m_strDwoVrsn);
				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("dwoformtype","_FRM_SQNC_");

				int iReturnCode = 0;
				if (bTransactionExists)
				{
					iReturnCode = dwoBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = dwoBean.closeConnection();
					}
				}
				if (iReturnCode == 0)
				{
					Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- dwoformtype = _FRM_SQNC_");
					alltelRequestDispatcher.forward("/DwoFormView.jsp");  
					return;
				}
				else
				{
					 Log.write(Log.ERROR, "DwoCtlr() --- Transaction Error");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DwoErrorView.jsp"); 
					return;
				}
			}			

	        // Handle "View Validation Errors" Action for viewing Field Validations
	
	        if ( m_strDwoAction.equals("Validate"))
	        {
				int iReturnCode = 0;
				int iDwoVldtnErrs = 0;
				if (bTransactionExists == true)
				{
				    iReturnCode = dwoBean.commitTransaction();
				}
				else
				{
				    iReturnCode = dwoBean.getConnection();
				}
				
				if (iReturnCode == 0)
				{
				    iDwoVldtnErrs = dwoBean.validateFields(request, m_iDwoSqncNmbr, m_iDwoVrsn, "A", "");
				    Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- validation errors = " + iDwoVldtnErrs);
				}
					
				iReturnCode = dwoBean.closeConnection();
				
				alltelRequestDispatcher.forward("/DwoValidationView.jsp");
				return;
			}

			//Handle all other actions based on DWO_ACTION_T workflow
			Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- Dwo Status Change");

			int iReturnCode = 0; 
			if (bTransactionExists == false)
			{
				iReturnCode = dwoBean.getConnection();
				if (iReturnCode == 0) {
					iReturnCode = dwoBean.beginTransaction();
				}
			}
			else
			{
				iReturnCode = dwoBean.commitTransaction();
			}
			dwoBean.setDwoPrdProd( m_iDwoSqncNmbr );
			if (iReturnCode == 0) {
				iReturnCode = dwoBean.changeStatus(request, m_iDwoSqncNmbr, m_strDwoAction);
				if (iReturnCode > 0) {
					iReturnCode = dwoBean.commitTransaction();
					if (iReturnCode == 0) {
						iReturnCode = dwoBean.closeConnection();
					}
				}
				else if (iReturnCode == dwoBean.SECURITY_ERROR)
				{
					alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
					return;
				}
			}

			if (iReturnCode >= 0)
			{
				request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
				request.getHttpRequest().setAttribute("dwoformtype","_FRM_SQNC_");

				Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- Status Change Successful");
                                
                                /* This probably is the point in the flow where the status changes from INITIAL to SUBMITTED
                                * So calling the camscrossreference webservice to update the CUS_ACCT_NMBR should be done here.
                                * Need to write separate method to invoke webservice and update field in DWO_CHANGE_T in this class and 
                                * a separate method to do these operations has to be written - Antony - 01/07/2014
                                */
                
                                //call CAN update method here as Save is complete here -- Antony
                                
                                if(bBDP /*&& m_strDwoAction.equals("Submit")*/) {
                            
                                    Log.write("DwoCtlr() User trying to Submit the order. Calling CustAcctNmbr update method here....");
                                    Log.write("DwoCtlr() Value of MBTN field : "+request.getParameter("BILL_TO_MBTN"));

                                    Log.write("DwoCtlr() Value of DWO_SQNC_NMBR: "+request.getParameter("DWO_SQNC_NMBR"));

                                    //no version number for dwo orders so using just dwo_sqnc_nmbr
                                    String strDwoSqncNmbr = request.getParameter("DWO_SQNC_NMBR");

                                    //call method to retrieve bill_to_mbtn
                                    String strMBTN = getMBTN(strDwoSqncNmbr);

                                    Log.write("DwoCtlr() Value of BILL_TO_MBTN: "+strMBTN);

                                    updateCustAcctNmbrField(strDwoSqncNmbr,strMBTN);
                            
                                }
                                
				// handle telling the user we saved the form successfully
				//Add the code for ActionManager here.					               
               String strActnDstntn = null;
                ActionManager am = new ActionManager();
                strActnDstntn = am.getActionDestinationDwo(dwoBean.getSttsCdFrom(), dwoBean.getTypInd(), dwoBean.getRqstTypCd(), dwoBean.getSttsCdTo(), m_strDwoAction, dwoBean.getPrdctTypCd() );

				if (strActnDstntn.length() > 0) 
				{
					Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() -- Next action based on Action_T is: "+strActnDstntn);
					strActnDstntn+=strPJVN;
				  	alltelRequestDispatcher.forward("/"+strActnDstntn);  
				} else {
				alltelRequestDispatcher.forward("/DwoListCtlr"+strPJVN);  
				}
				return;
			}
			else
			{
				if (iReturnCode == dwoBean.VALIDATION_ERROR)
				{
					Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- Validation Errors with Form Fields - Sending to Error Validation View");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DwoValidationView.jsp");  
					return;
				}
				else
				{
					Log.write(Log.ERROR, "DwoCtlr() --- Error Changing Dwo Status");
					// handle telling the user we had an error !!!
					alltelRequestDispatcher.forward("/DwoErrorView.jsp");  
					return;
				}
			}
		}

		//Now check to see if we got here with a dwo to add a section for a FORM.
		//Required parameters are DWO_SQNC_NMBR, DWO_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair describing the section to add.
		//The parameter name will be "add_sctn_?" and the value will be "Add Section".
		String m_strDwoAddSctn = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
			m_strDwoAddSctn = request.getParameter("add_sctn_" + m_iFrmSctnSqncNmbr);

			if ((m_strDwoAddSctn == null) || (m_strDwoAddSctn.length() == 0))
			{}
			else
			{
				if (m_strDwoAddSctn.equals("Add Section"))
				{
					//we know now the user submitted a valid dwo to add a new section
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- add_sctn");

					int m_iFrmSctnOccNew = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = dwoBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = dwoBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						m_iFrmSctnOccNew = dwoBean.generateSection(m_iFrmSqncNmbr, m_iDwoSqncNmbr, m_iDwoVrsn, m_iFrmSctnSqncNmbr);
						if (m_iFrmSctnOccNew > 0) {
							iReturnCode = dwoBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = dwoBean.closeConnection();
							}
						}
						else if (m_iFrmSctnOccNew == dwoBean.SECURITY_ERROR)
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
						request.getHttpRequest().setAttribute("DWO_VRSN", m_strDwoVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("dwoformtype","_FRM_SQNC_");

				        Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- m_iFrmSctnOccNew = " + m_iFrmSctnOccNew);
						//Here we need this section we added-cause we want the cursor
						// in the first field of it when the form is presented.
						request.getHttpRequest().setAttribute("NEW_OCC", Integer.toString(m_iFrmSctnOccNew));
						request.getHttpRequest().setAttribute("NEW_SECTION", Integer.toString(m_iFrmSctnSqncNmbr));
				        Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- dwoformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/DwoFormView.jsp"); 
						return;

					}
					else
					{
				        	Log.write(Log.ERROR, "DwoCtlr() --- Error getting a new section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/DwoErrorView.jsp");  
						return;
					}
				}
			}
		}

		//Now check to see if we got here with a dwo to delete a section for a FORM.
		//Required parameters are DWO_SQNC_NMBR, DWO_VRSN, FRM_SQNC_NMBR.
		//ALSO PARSE the parameter names for a name/value pair identifying the
		// the SECTION SEQUENCE number and the SECTION OCCURRENCE that needs to be deleted.
		//The parameter name will be "del_sctn_?_?" and the value will be "Delete Section".
		String m_strDwoDltSctnOcc = "";
		for (int m_iFrmSctnSqncNmbr = 1; m_iFrmSctnSqncNmbr < 100 ; m_iFrmSctnSqncNmbr++)
		{
		  for (int m_iSctnOcc = 1; m_iSctnOcc < 100 ; m_iSctnOcc++)
		  {	
			m_strDwoDltSctnOcc = request.getParameter("del_sctn_" + m_iFrmSctnSqncNmbr + "_" + m_iSctnOcc);

			if ((m_strDwoDltSctnOcc == null) || (m_strDwoDltSctnOcc.length() == 0))
			{}
			else
			{
				if (m_strDwoDltSctnOcc.equals("Delete Section"))
				{
					//we know now the user submitted a valid dwo to delete a section occurrence
					//from within the current form they were working with

			        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- del_sctn");
			        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- m_iFrmSctnSqncNmbr = " + m_iFrmSctnSqncNmbr);
			        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- m_iSctnOcc = " + m_iSctnOcc);

					int iFrmSctnDltOcc = 0;
					int iReturnCode = 0; 
					if (bTransactionExists == false)
					{
						iReturnCode = dwoBean.getConnection();
						if (iReturnCode == 0) {
							iReturnCode = dwoBean.beginTransaction();
						}
					}
					if (iReturnCode == 0) {
						iFrmSctnDltOcc = dwoBean.deleteSection(m_iFrmSqncNmbr, m_iDwoSqncNmbr, m_iDwoVrsn, m_iFrmSctnSqncNmbr, m_iSctnOcc);						
						if (iFrmSctnDltOcc == 0) {
							iReturnCode = dwoBean.commitTransaction();
							if (iReturnCode == 0) {
								iReturnCode = dwoBean.closeConnection();
							}
						}
						else if (iFrmSctnDltOcc ==  dwoBean.SECURITY_ERROR)
						{
							alltelRequestDispatcher.forward("/LsrSecurity.jsp");  
							return;
						}
					}
					if (iReturnCode == 0)
					{
						request.getHttpRequest().setAttribute("DWO_VRSN", m_strDwoVrsn);
						request.getHttpRequest().setAttribute("FRM_SQNC_NMBR", m_strFrmSqncNmbr);
						request.getHttpRequest().setAttribute("dwoformtype","_FRM_SQNC_");

				        	Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- Delete Section Occurrence Successful");
						Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- dwoformtype = _FRM_SQNC_");

						alltelRequestDispatcher.forward("/DwoFormView.jsp"); 
						return;
					}
					else
					{
				        	Log.write(Log.ERROR, "DwoCtlr() --- Error deleting a section Occurrence");
						// handle telling the user we had an error !!!
						alltelRequestDispatcher.forward("/DwoErrorView.jsp");  
						return;
					}
				}
			}
		  }
		}
				
		// if we dropped down to here, we don't know where we're supposed to go and
		// we apparently have a navigation error.

		Log.write(Log.DEBUG_VERBOSE, "DwoCtlr() --- Navigation Error!");

		alltelRequestDispatcher.forward("/NavigationErrorView.jsp");
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
        
        /***** Antony 02/10/2014 ******************
         * Adding new method to call camscrossreference BW webservice and populate CustAcctNum field with 
         * cust cams ID from the response from BW WS call
         */
        
       private void updateCustAcctNmbrField(String strDwoSqncNmbr,String strMBTNField) {
           
            String strCustCAMSId = "";
           
            try {
                Log.write("bwcamscrossreference webServiceInvoke calling: ");
                WebInputProxy objWinExpDataProxy = new WebInputProxy_Impl();
                CamsCrossReferenceWebLayer objWinExpWebSrvcIntrfc = objWinExpDataProxy.getCamsCrossReferenceWebLayer();
                    
                //property file entry to look for CAMSCROSSREFERENCE webservice URL - Antony - 02/13/2014
                //lsr.bwcamscrossreference.URL

                //code to change Endpoint URL dynamically based on lsr.properties entry for bwcamscrossreference
                CamsCrossReferenceWebLayer_Stub  wewStub = (CamsCrossReferenceWebLayer_Stub)objWinExpWebSrvcIntrfc;
                URL urlString = new URL(PropertiesManager.getProperty("lsr.bwcamscrossreference.URL",""));

                Log.write("DwoCtlr() ccr bw ws URL from props file : "+PropertiesManager.getProperty("lsr.bwcamscrossreference.URL",""));
                
                Log.write("BW bwcamscrossreference URL prior to dynamic setting : "+wewStub._getTargetEndpoint());
                wewStub._setTargetEndpoint(urlString);
                Log.write("BW bwcamscrossreference URL after to dynamic setting : "+wewStub._getTargetEndpoint());
                
                CamsCrossReferenceRequestData requestData = new CamsCrossReferenceRequestData(); 
                
                requestData.setControlDate("");
                requestData.setTelephoneNumber(strMBTNField);
                                
                CamsCrossReferenceRequestStructure requestStructure = new CamsCrossReferenceRequestStructure();
                requestStructure.setCamsCrossReferenceData(requestData);
                
                ApplicationInfo appInfo = new ApplicationInfo();
                appInfo.setApplicationID("8Sg0T@NnrpdcXZRHco4WgB");
                appInfo.setApplicationName("EXPRESS");
                requestStructure.setApplicationInfo(appInfo);
                        
                requestStructure.setTransactionId("12345");
                                                
                Log.write("DwoCtlr() WS requestStructure sent : "+requestStructure.toString());
                Log.write("DwoCtlr() WS request sent : "+requestData.toString());
                Log.write("DwoCtlr() MBTN field sent : "+requestData.getTelephoneNumber());
                                
                CamsCrossReferenceReplyStructure replyStructure = objWinExpWebSrvcIntrfc.opCamsCrossReference(requestStructure);
                
                Log.write("DwoCtlr() WS replyStructure sent : "+replyStructure.toString());
                                
                CamsCrossReferenceReplyData replyData = new CamsCrossReferenceReplyData();
                
                replyData = replyStructure.getCamsCrossReferenceData();
                
                Log.write("DwoCtlr() WS response received : "+replyData.toString());
                Log.write("DwoCtlr() Cust Cams ID received : "+replyData.getCustCamsId());
                
                strCustCAMSId = replyData.getCustCamsId();
                
                // If custCamsId returned by BW is null or empty then update field with a 999999999
                if (strCustCAMSId == null || strCustCAMSId.length() == 0)
                    strCustCAMSId = "999999999";
                                     
                Log.write("DwoCtlr() Cust Cams ID received : "+replyData.getCustCamsId());
                Log.write("DwoCtlr() Dwo Sqnc Nmbr received : "+strDwoSqncNmbr);
                Log.write("DwoCtlr() Cust Cams ID received : "+strCustCAMSId);
                
                updateCusAcctNum(strDwoSqncNmbr,strCustCAMSId);
                
                //return;
            } catch(Exception ex) {
                ex.printStackTrace();
                Log.write("Exception while calling bwcamscrossreference webservice. Unable to update CUS ACCT NUM field !! Exception: "+ex.getMessage());
                //update Cust Acct Num field with 9999999999 -- call method here
                strCustCAMSId = "999999999";
                
                //return;
            } finally {
                //return;
            }
        }
       
     /*
     * Retrieve BILL_TO_MBTN from DWO_CHG_T table - Antony - 02/14/2014
     *
     */

    private String getMBTN(String strDwoSqncNmbr) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String strMBTN = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select BILL_TO_MBTN from DWO_CHG_T where dwo_sqnc_nmbr='"+strDwoSqncNmbr+"'";

            Log.write("DwoCtlr() getMBTN strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                strMBTN = rs.getString("BILL_TO_MBTN");
            }

            if(strMBTN == null || strMBTN.length() == 0) 
                strMBTN = "";
            else
                strMBTN = strMBTN.replaceAll("-","");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return strMBTN;
    }
    
    /*
     * Update CUS_ACCT_NUM from DWO_CHG_T table - Antony - 02/14/2014
     *
     */

    private void updateCusAcctNum(String strDwoSqncNmbr,String strCustAcctNmbr) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "update dwo_chg_t set CUS_ACCT_NUM='"+strCustAcctNmbr+"' where DWO_SQNC_NMBR =" +strDwoSqncNmbr;
            Log.write("DwoCtlr() updateCusAcctNum strQuery-: " + strQuery);
            
            stmt.executeQuery(strQuery);

            Log.write("DwoCtlr() Successfully updated Customer Account Number field in DWO Form !!!");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
            Log.write("DwoCtlr() In finally block after updating CAN field in DWO EIA form.");
            //return;      
        }
        
      
    }
    
    
}

