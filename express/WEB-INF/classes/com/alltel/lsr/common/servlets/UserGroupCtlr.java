package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class UserGroupCtlr extends AlltelServlet
{
	public void myservice(AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/UserGroupView.jsp";

                Log.write(Log.DEBUG_VERBOSE, "CompanyCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the UserGroup Bean
		UserGroupBean beanUserGroup = new UserGroupBean();
		request.getHttpRequest().setAttribute("usergroupbean", beanUserGroup);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "UserGroupCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strUsrGrpCd = request.getParameter("USR_GRP_CD");
		if ((strUsrGrpCd == null) || (strUsrGrpCd.length() == 0))
		{
			// Handle the error
		}

		String strUsrGrpDscrptn = request.getParameter("USR_GRP_DSCRPTN");
		if ((strUsrGrpDscrptn == null) || (strUsrGrpDscrptn.length() == 0))
		{
			// Handle the error
		}

		String strCmpnySqncNmbr = request.getParameter("CMPNY_SQNC_NMBR");
		if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strOcnCd = request.getParameter("OCN_CD");
		if ((strOcnCd == null) || (strOcnCd.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanUserGroup.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanUserGroup.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanUserGroup.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanUserGroup.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanUserGroup.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanUserGroup.setUsrGrpCd(strUsrGrpCd);
		beanUserGroup.setUsrGrpDscrptn(strUsrGrpDscrptn);
		beanUserGroup.setCmpnySqncNmbr(strCmpnySqncNmbr);
		beanUserGroup.setOcnCd(strOcnCd);
		beanUserGroup.setMdfdDt(strMdfdDt);
		beanUserGroup.setMdfdUserid(sdm.getUser());
		beanUserGroup.setDbAction(strDbAction);

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanUserGroup.validateUserGroupBean())
			{
				// Send error msg back to view
				strURL= "/UserGroupView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanUserGroup.saveUserGroupBeanToDB() != 0)
			{
				strURL= "/OCNView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=7";
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanUserGroup.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/UserGroupView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanUserGroup.updateUserGroupBeanToDB() != 0)
			{
				strURL= "/OCNView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=7";
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanUserGroup.retrieveUserGroupBeanFromDB() != 0)
			{
				strURL= "/OCNView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanUserGroup.deleteUserGroupBeanFromDB() != 0)
			{
				strURL= "/OCNView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=7";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanUserGroup.setUsrGrpCd("");
			beanUserGroup.setUsrGrpDscrptn("");
			beanUserGroup.setCmpnySqncNmbr("");
			beanUserGroup.setOcnCd("");
			beanUserGroup.setMdfdDt("");
			beanUserGroup.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=7";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "CompanyCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
