package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class ReasonCodeCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/ReasonCodeView.jsp";

                Log.write(Log.DEBUG_VERBOSE, "ReasonCodeCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the ReasonCode Bean
		ReasonCodeBean beanReasonCode = new ReasonCodeBean();
		request.getHttpRequest().setAttribute("reasoncodebean", beanReasonCode);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "ReasonCodeCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strRsnCdSqncNmbr = request.getParameter("RSN_CD_SQNC_NMBR");
		if ((strRsnCdSqncNmbr == null) || (strRsnCdSqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strRsnCd = request.getParameter("RSN_CD");
		if ((strRsnCd == null) || (strRsnCd.length() == 0))
		{
			// Handle the error
		}

		String strRsnCdTyp = request.getParameter("RSN_CD_TYP");
		if ((strRsnCdTyp == null) || (strRsnCdTyp.length() == 0))
		{
			// Handle the error
		}

		String strRsnCdDscrptn = request.getParameter("RSN_CD_DSCRPTN");
		if ((strRsnCdDscrptn == null) || (strRsnCdDscrptn.length() == 0))
		{
			// Handle the error
		}

		String strSrvcTypDscrptn = request.getParameter("SRVC_TYP_DSCRPTN");
		if ((strSrvcTypDscrptn == null) || (strSrvcTypDscrptn.length() == 0))
		{
			// Handle the error
		}

		String strActvtyTypDscrptn = request.getParameter("ACTVTY_TYP_DSCRPTN");
		if ((strActvtyTypDscrptn == null) || (strActvtyTypDscrptn.length() == 0))
		{
			// Handle the error
		}

		String strFrmCd = request.getParameter("FRM_CD");
		if ((strFrmCd == null) || (strFrmCd.length() == 0))
		{
			// Handle the error
		}


		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanReasonCode.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanReasonCode.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanReasonCode.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanReasonCode.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanReasonCode.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "ReasonCodeCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanReasonCode.setRsnCdSqncNmbr(strRsnCdSqncNmbr);
		beanReasonCode.setRsnCd(strRsnCd);
		beanReasonCode.setRsnCdTyp(strRsnCdTyp);
		beanReasonCode.setRsnCdDscrptn(strRsnCdDscrptn);
		beanReasonCode.setSrvcTypDscrptn(strSrvcTypDscrptn);
		beanReasonCode.setActvtyTypDscrptn(strActvtyTypDscrptn);
		beanReasonCode.setFrmCd(strFrmCd);
		beanReasonCode.setMdfdDt(strMdfdDt);
		beanReasonCode.setMdfdUserid(sdm.getUser());
		beanReasonCode.setDbAction(strDbAction);

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanReasonCode.validateReasonCodeBean())
			{
				// Send error msg back to view
				strURL= "/ReasonCodeView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanReasonCode.saveReasonCodeBeanToDB() != 0)
			{
				strURL= "/ReasonCodeView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=6";
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanReasonCode.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/ReasonCodeView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanReasonCode.updateReasonCodeBeanToDB() != 0)
			{
				strURL= "/ReasonCodeView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=6";
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanReasonCode.retrieveReasonCodeBeanFromDB() != 0)
			{
				strURL= "/ReasonCodeView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanReasonCode.deleteReasonCodeBeanFromDB() != 0)
			{
				strURL= "/ReasonCodeView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=6";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanReasonCode.setRsnCdSqncNmbr("");
			beanReasonCode.setRsnCd("");
			beanReasonCode.setRsnCdTyp("");
			beanReasonCode.setRsnCdDscrptn("");
			beanReasonCode.setSrvcTypDscrptn("");
		    beanReasonCode.setActvtyTypDscrptn("");
		    beanReasonCode.setFrmCd("");
			beanReasonCode.setMdfdDt("");
			beanReasonCode.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=6";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "ReasonCodeCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
