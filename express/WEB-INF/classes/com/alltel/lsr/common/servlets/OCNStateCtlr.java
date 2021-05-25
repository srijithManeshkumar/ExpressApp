package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class OCNStateCtlr extends AlltelServlet
{
	public void myservice(AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/OCNStateView.jsp";

		Log.write(Log.DEBUG_VERBOSE, "OCNStateCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the OCN State Bean
		OCNStateBean beanOCNState = new OCNStateBean();
		request.getHttpRequest().setAttribute("ocnstatebean", beanOCNState);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "OCNStateCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strOcnSttSqncNmbr = request.getParameter("OCN_STT_SQNC_NMBR");
		if ((strOcnSttSqncNmbr == null) || (strOcnSttSqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strOcnCd = request.getParameter("OCN_CD");
		if ((strOcnCd == null) || (strOcnCd.length() == 0))
		{
			// Handle the error
		}

		String strSttCd = request.getParameter("STT_CD");
		if ((strSttCd == null) || (strSttCd.length() == 0))
		{
			// Handle the error
		}

		String strOcnSttSlaDys = request.getParameter("OCN_STT_SLA_DYS");
		if ((strOcnSttSlaDys == null) || (strOcnSttSlaDys.length() == 0))
		{
			// Handle the error
		}

		String strOcnSttCntrctPrcntg = request.getParameter("OCN_STT_CNTRCT_PRCNTG");
		if ((strOcnSttCntrctPrcntg == null) || (strOcnSttCntrctPrcntg.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanOCNState.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanOCNState.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanOCNState.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanOCNState.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanOCNState.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "OCNStateCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanOCNState.setOcnSttSqncNmbr(strOcnSttSqncNmbr);
		beanOCNState.setOcnCd(strOcnCd);
		beanOCNState.setSttCd(strSttCd);
		beanOCNState.setOcnSttSlaDys(strOcnSttSlaDys);
		beanOCNState.setOcnSttCntrctPrcntg(strOcnSttCntrctPrcntg);
		beanOCNState.setMdfdDt(strMdfdDt);
		beanOCNState.setMdfdUserid(sdm.getUser());
		beanOCNState.setDbAction(strDbAction);

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanOCNState.validateOCNStateBean())
			{
				// Send error msg back to view
				strURL= "/OCNStateView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanOCNState.saveOCNStateBeanToDB() != 0)
			{
				strURL= "/OCNStateView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=5";
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanOCNState.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/OCNStateView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanOCNState.updateOCNStateBeanToDB() != 0)
			{
				strURL= "/OCNStateView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=5";
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanOCNState.retrieveOCNStateBeanFromDB() != 0)
			{
				strURL= "/OCNStateView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanOCNState.deleteOCNStateBeanFromDB() != 0)
			{
				strURL= "/OCNStateView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=5";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanOCNState.setOcnSttSqncNmbr("");
			beanOCNState.setOcnCd("");
			beanOCNState.setSttCd("");
			beanOCNState.setOcnSttSlaDys("");
			beanOCNState.setOcnSttCntrctPrcntg("");
			beanOCNState.setMdfdDt("");
			beanOCNState.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=5";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "OCNStateCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
