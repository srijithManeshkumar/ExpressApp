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
 * MODULE:	BanCtlr.java
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

public class BanCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/BanView.jsp";
		String strEnc = null;

                Log.write(Log.DEBUG_VERBOSE, "BanCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the Ban Bean
		BanBean beanBan = new BanBean();
		request.getHttpRequest().setAttribute("banbean", beanBan);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "BanCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strBan = request.getParameter("BAN");
		if ((strBan == null) || (strBan.length() == 0))
		{
			// Handle the error
		}

		String strBanDscrptn = request.getParameter("BAN_DSCRPTN");
		if ((strBanDscrptn == null) || (strBanDscrptn.length() == 0))
		{
			// Handle the error
		}

		String strCmpnySqncNmbr = request.getParameter("CMPNY_SQNC_NMBR");
		if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanBan.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanBan.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanBan.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanBan.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanBan.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "BanCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanBan.setBan(strBan);
		beanBan.setBanDscrptn(strBanDscrptn);
		beanBan.setCmpnySqncNmbr(strCmpnySqncNmbr);
		beanBan.setMdfdDt(strMdfdDt);
		beanBan.setMdfdUserid(sdm.getUser());
		beanBan.setDbAction(strDbAction);
		
		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanBan.validateBanBean())
			{
				// Send error msg back to view
				strURL= "/BanView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanBan.saveBanBeanToDB() != 0)
			{
				strURL= "/BanView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=15";
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanBan.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/BanView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanBan.updateBanBeanToDB() != 0)
			{
				strURL= "/BanView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=15";
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanBan.retrieveBanBeanFromDB() != 0)
			{
				strURL= "/BanView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanBan.deleteBanBeanFromDB() != 0)
			{
				strURL= "/BanView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=15";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanBan.setBan("");
			beanBan.setBanDscrptn("");
			beanBan.setCmpnySqncNmbr("");
			beanBan.setMdfdDt("");
			beanBan.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=15";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "BanCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
			throws Exception
	{
	}
}
