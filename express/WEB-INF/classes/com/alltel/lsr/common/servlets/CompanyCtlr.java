/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2002
 *                                     BY
 *                           ALLTEL COMMUNICATIONS, INC.
 */
/*
 * MODULE:              CompanyCtlr.java
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Dan Martz
 *
 * DATE:        01-31-2002
 *
 * HISTORY:
 *      1/31/2002  dmartz 	initial check-in.
 *      5/29/2002  psedlak 	New cols added to COMPANY_T
 *
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class CompanyCtlr extends AlltelServlet
{

	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception
	{	
		String strURL = "/CompanyView.jsp";

		Log.write(Log.DEBUG_VERBOSE, "CompanyCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
			
		// Instantiate the Company Bean
		CompanyBean beanCmpny = new CompanyBean();
		request.getHttpRequest().setAttribute("cmpnybean", beanCmpny);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "CompanyCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strCmpnySqncNmbr = request.getParameter("CMPNY_SQNC_NMBR");
		if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strCmpnyTyp = request.getParameter("CMPNY_TYP");
		if ((strCmpnyTyp == null) || (strCmpnyTyp.length() == 0))
		{
			// Handle the error
		}

		String strCmpnyNm = request.getParameter("CMPNY_NM");
		if ((strCmpnyNm == null) || (strCmpnyNm.length() == 0))
		{
			// Handle the error
		}

		String strTargusUserid = request.getParameter("TARGUS_USERID");
		if ((strTargusUserid == null) || (strTargusUserid.length() == 0))
		{
			// Handle the error
		}

		String strTargusPsswrd = request.getParameter("TARGUS_PSSWRD");
		if ((strTargusPsswrd == null) || (strTargusPsswrd.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanCmpny.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanCmpny.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanCmpny.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanCmpny.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanCmpny.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanCmpny.setCmpnySqncNmbr(strCmpnySqncNmbr);
		beanCmpny.setCmpnyTyp(strCmpnyTyp);
		beanCmpny.setCmpnyNm(strCmpnyNm);
		beanCmpny.setTargusUserid(strTargusUserid);
		beanCmpny.setTargusPsswrd(strTargusPsswrd);
		beanCmpny.setMdfdDt(strMdfdDt);
		beanCmpny.setMdfdUserid(sdm.getUser());
		beanCmpny.setDbAction(strDbAction);

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanCmpny.validateCompanyBean())
			{
				// Send error msg back to view
				strURL= "/CompanyView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanCmpny.saveCompanyBeanToDB() != 0)
			{
				strURL= "/CompanyView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=1";
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanCmpny.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/CompanyView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanCmpny.updateCompanyBeanToDB() != 0)
			{
				strURL= "/CompanyView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=1";
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanCmpny.retrieveCompanyBeanFromDB() != 0)
			{
				strURL= "/CompanyView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			beanCmpny.deleteCompanyBeanFromDB();
			if (beanCmpny.deleteCompanyBeanFromDB() != 0)
			{
				strURL= "/CompanyView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=1";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanCmpny.setCmpnySqncNmbr("");
			beanCmpny.setCmpnyTyp("");
			beanCmpny.setCmpnyNm("");
			beanCmpny.setTargusUserid("");
			beanCmpny.setTargusPsswrd("");
			beanCmpny.setMdfdDt("");
			beanCmpny.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=1";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		Log.write(Log.DEBUG, "CompanyCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
