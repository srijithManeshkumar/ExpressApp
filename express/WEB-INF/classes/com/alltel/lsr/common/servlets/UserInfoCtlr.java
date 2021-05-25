package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class UserInfoCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/UserInfoView.jsp";
		String strEnc = null;

                Log.write(Log.DEBUG_VERBOSE, "UserInfoCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the UserInfoBean
		UserInfoBean beanUserInfo = new UserInfoBean();
		request.getHttpRequest().setAttribute("userinfobean", beanUserInfo);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "UserInfoCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strUserID = request.getParameter("USERID");
		if ((strUserID == null) || (strUserID.length() == 0))
		{
			// Handle the error
		}

		String strEmlAddrss = request.getParameter("EML_ADDRSS");
		if ((strEmlAddrss == null) || (strEmlAddrss.length() == 0))
		{
			strEmlAddrss = "";
		}

		String strRcvEmlNtfctns = request.getParameter("RCV_EML_NTFCTNS");
		if ((strRcvEmlNtfctns == null) || (strRcvEmlNtfctns.length() == 0))
		{
			strRcvEmlNtfctns = "no";
		}

		String strPsswdRcvrQstn = request.getParameter("PSSWD_RCVR_QSTN");
		if ((strPsswdRcvrQstn == null) || (strPsswdRcvrQstn.length() == 0))
		{
			strPsswdRcvrQstn = "";
		}

		String strPsswdRcvrNswr = request.getParameter("PSSWD_RCVR_NSWR");
		if ((strPsswdRcvrNswr == null) || (strPsswdRcvrNswr.length() == 0))
		{
			strPsswdRcvrNswr = "";
		}

		String strPrintAllFields = request.getParameter("PRINT_IND");
		if ((strPrintAllFields == null) || (strPrintAllFields.length() == 0))
		{
			strPrintAllFields = "no";
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		// Verify that the user requesting the info is logged in
		if (! strUserID.equals(sdm.getUser()))
		{
			Log.write(Log.WARNING, "UserInfoCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanUserInfo.setUserID(strUserID);
		beanUserInfo.setEmlAddrss(strEmlAddrss);
		beanUserInfo.setRcvEmlNtfctns(strRcvEmlNtfctns);
		beanUserInfo.setPsswdRcvrQstn(strPsswdRcvrQstn);
		beanUserInfo.setPsswdRcvrNswr(strPsswdRcvrNswr);
		beanUserInfo.setMdfdDt(strMdfdDt);
		beanUserInfo.setMdfdUserid(sdm.getUser());
		beanUserInfo.setDbAction(strDbAction);
		beanUserInfo.setPrintInd(strPrintAllFields);
		
		// Validate the Bean
		if ( (strDbAction.equals("UpdateRow")) || (strDbAction.equals("Update")) )
		{
			if (!beanUserInfo.validateUserInfoBean())
			{
				// Send error msg back to view
				strURL= "/UserInfoView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		if ( (strDbAction.equals("UpdateRow")) || (strDbAction.equals("Update")) )
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanUserInfo.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/UserInfoView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanUserInfo.updateUserInfoBeanToDB() != 0)
			{
				strURL= "/UserInfoView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/UserOptions.jsp";
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Save the User ID to the session for later user
			HttpSession session = request.getSession();
			session.setAttribute("UserID_userid", strUserID);

			// Retrieve from DB
			if (beanUserInfo.retrieveUserInfoBeanFromDB() != 0)
			{
				strURL= "/UserInfoView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/UserOptions.jsp";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "UserInfoCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
			throws Exception
	{
	}
}
