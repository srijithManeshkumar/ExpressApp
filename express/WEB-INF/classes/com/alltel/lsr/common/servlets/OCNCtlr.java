package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class OCNCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/OCNView.jsp";

                Log.write(Log.DEBUG_VERBOSE, "OCNCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the OCN Bean
		OCNBean beanOCN = new OCNBean();
		request.getHttpRequest().setAttribute("ocnbean", beanOCN);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "OCNCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strOcnCd = request.getParameter("OCN_CD");
		if ((strOcnCd == null) || (strOcnCd.length() == 0))
		{
			// Handle the error
		}

		String strCmpnySqncNmbr = request.getParameter("CMPNY_SQNC_NMBR");
		if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strOcnNm = request.getParameter("OCN_NM");
		if ((strOcnNm == null) || (strOcnNm.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanOCN.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanOCN.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanOCN.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanOCN.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanOCN.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "OCNCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanOCN.setOcnCd(strOcnCd);
		beanOCN.setCmpnySqncNmbr(strCmpnySqncNmbr);
		beanOCN.setOcnNm(strOcnNm);
		beanOCN.setMdfdDt(strMdfdDt);
		beanOCN.setMdfdUserid(sdm.getUser());
		beanOCN.setDbAction(strDbAction);

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanOCN.validateOCNBean())
			{
				// Send error msg back to view
				strURL= "/OCNView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanOCN.saveOCNBeanToDB() != 0)
			{
				strURL= "/OCNView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=2";
			}

			strURL = "/TableAdminCtlr?tblnmbr=2";
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanOCN.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/OCNView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanOCN.updateOCNBeanToDB() != 0)
			{
				strURL= "/OCNView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=2";
			}

			strURL = "/TableAdminCtlr?tblnmbr=2";
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanOCN.retrieveOCNBeanFromDB() != 0)
			{
				strURL= "/OCNView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanOCN.deleteOCNBeanFromDB() != 0)
			{
				strURL= "/OCNView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=2";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanOCN.setOcnCd("");
			beanOCN.setCmpnySqncNmbr("");
			beanOCN.setOcnNm("");
			beanOCN.setMdfdDt("");
			beanOCN.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=2";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "OCNCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
