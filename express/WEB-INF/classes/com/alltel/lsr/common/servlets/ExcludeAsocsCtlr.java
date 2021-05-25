/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL COMMUNICATIONS INC.
 */

/* 
 * MODULE:	ExcludeAsocsCtlr.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-01-2002
 * 
 * HISTORY:
 *
 */

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class ExcludeAsocsCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/ExcludeAsocsView.jsp";
		String strEnc = null;

                Log.write(Log.DEBUG_VERBOSE, "ExcludeAsocsCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the Ban Bean
		ExcludeAsocsBean beanExcludeAsocs = new ExcludeAsocsBean();
		request.getHttpRequest().setAttribute("exasocbean", beanExcludeAsocs);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "ExcludeAsocsCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strSqncNmbr = request.getParameter("SQNC_NMBR");
		if ((strSqncNmbr == null) || (strSqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strExcldCtgry = request.getParameter("EXCLD_CTGRY");
		if ((strExcldCtgry == null) || (strExcldCtgry.length() == 0))
		{
			// Handle the error
		}

		String strNpa = request.getParameter("NPA");
		if ((strNpa == null) || (strNpa.length() == 0))
		{
			// Handle the error
		}

		String strNxx = request.getParameter("NXX");
		if ((strNxx == null) || (strNxx.length() == 0))
		{
			// Handle the error
		}

		String strAsocCode = request.getParameter("ASOC_CODE");
		if ((strAsocCode == null) || (strAsocCode.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanExcludeAsocs.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanExcludeAsocs.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanExcludeAsocs.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") 	 && !sdm.isAuthorized(beanExcludeAsocs.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanExcludeAsocs.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "ExcludeAsocsCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanExcludeAsocs.setSqncNmbr(strSqncNmbr);
		beanExcludeAsocs.setExcldCtgry(strExcldCtgry);
		beanExcludeAsocs.setNpa(strNpa);
		beanExcludeAsocs.setNxx(strNxx);
		beanExcludeAsocs.setAsocCode(strAsocCode);
		beanExcludeAsocs.setMdfdDt(strMdfdDt);
		beanExcludeAsocs.setMdfdUserid(sdm.getUser());
		beanExcludeAsocs.setDbAction(strDbAction);
		
		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanExcludeAsocs.validateExcludeAsocsBean())
			{
				// Send error msg back to view
				strURL= "/ExcludeAsocsView.jsp";
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanExcludeAsocs.saveExcludeAsocsBeanToDB() != 0)
			{
				strURL= "/ExcludeAsocsView.jsp";
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=16";
			}
		}
                else if (strDbAction.equals("UpdateRow"))
                {
                        // Verify that no one else has modifed this row since it was retrieved
                        if (!beanExcludeAsocs.validateMdfdDt())
                        {
                                // Send error msg back to view
                                strURL= "/ExcludeAsocsView.jsp";
                                alltelRequestDispatcher.forward(strURL);
                                return;
                        }

                        // Update DB
                        if (beanExcludeAsocs.updateExcludeAsocsBeanToDB() != 0)
                        {
                                strURL= "/ExcludeAsocsView.jsp";
                        }
                        else
                        {
                                strURL = "/TableAdminCtlr?tblnmbr=16";
                        }
                }
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanExcludeAsocs.retrieveExcludeAsocsBeanFromDB() != 0)
			{
				strURL= "/ExcludeAsocsView.jsp";
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanExcludeAsocs.deleteExcludeAsocsBeanFromDB() != 0)
			{
				strURL= "/ExcludeAsocsView.jsp";
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=16";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanExcludeAsocs.setSqncNmbr("");
			beanExcludeAsocs.setExcldCtgry("");
			beanExcludeAsocs.setNpa("");
			beanExcludeAsocs.setNxx("");
			beanExcludeAsocs.setAsocCode("");
			beanExcludeAsocs.setMdfdDt("");
			beanExcludeAsocs.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=16";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "ExcludeAsocsCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
			throws Exception
	{
	}
}
