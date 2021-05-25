package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class UserGroupAssignmentCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/UserGroupAssignmentView.jsp";

                Log.write(Log.DEBUG_VERBOSE, "UserGroupAssignmentCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the UserGroupAssignmentBean Bean
		UserGroupAssignmentBean beanUserGroupAssignment = new UserGroupAssignmentBean();
		request.getHttpRequest().setAttribute("usergroupassignmentbean", beanUserGroupAssignment);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "UserGroupAssignmentCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strUsrGrpAssgnmntSqncNmbr = request.getParameter("USR_GRP_ASSGNMNT_SQNC_NMBR");
		if ((strUsrGrpAssgnmntSqncNmbr == null) || (strUsrGrpAssgnmntSqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strUserID = request.getParameter("USERID");
		if ((strUserID == null) || (strUserID.length() == 0))
		{
			// Handle the error
		}

		String strUsrGrpCd = request.getParameter("USR_GRP_CD");
		if ((strUsrGrpCd == null) || (strUsrGrpCd.length() == 0))
		{
			// Handle the error
		}

		String strScrtyGrpCd = request.getParameter("SCRTY_GRP_CD");
		if ((strScrtyGrpCd == null) || (strScrtyGrpCd.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanUserGroupAssignment.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanUserGroupAssignment.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanUserGroupAssignment.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanUserGroupAssignment.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanUserGroupAssignment.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "UserGroupAssignmentCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanUserGroupAssignment.setUsrGrpAssgnmntSqncNmbr(strUsrGrpAssgnmntSqncNmbr);
		beanUserGroupAssignment.setUserID(strUserID);
		beanUserGroupAssignment.setUsrGrpCd(strUsrGrpCd);
		beanUserGroupAssignment.setScrtyGrpCd(strScrtyGrpCd);
		beanUserGroupAssignment.setMdfdDt(strMdfdDt);
		beanUserGroupAssignment.setMdfdUserid(sdm.getUser());
		beanUserGroupAssignment.setDbAction(strDbAction);

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanUserGroupAssignment.validateUserGroupAssignmentBean())
			{
				// Send error msg back to view
				strURL= "/UserGroupAssignmentView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanUserGroupAssignment.saveUserGroupAssignmentBeanToDB() != 0)
			{
				strURL= "/UserGroupAssignmentView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=4&rstrctsrch=yes&srchctgry=0&srchvl=" + strUserID;
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanUserGroupAssignment.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/UserGroupAssignmentView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanUserGroupAssignment.updateUserGroupAssignmentBeanToDB() != 0)
			{
				strURL= "/UserGroupAssignmentView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=4&rstrctsrch=yes&srchctgry=0&srchvl=" + strUserID;
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanUserGroupAssignment.retrieveUserGroupAssignmentBeanFromDB() != 0)
			{
				strURL= "/UserGroupAssignmentView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanUserGroupAssignment.deleteUserGroupAssignmentBeanFromDB() != 0)
			{
				strURL= "/UserGroupAssignmentView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=4&rstrctsrch=yes&srchctgry=0&srchvl=" + strUserID;
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanUserGroupAssignment.setUsrGrpAssgnmntSqncNmbr("");
			beanUserGroupAssignment.setUserID("");
			beanUserGroupAssignment.setUsrGrpCd("");
			beanUserGroupAssignment.setScrtyGrpCd("");
			beanUserGroupAssignment.setMdfdDt("");
			beanUserGroupAssignment.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=4&rstrctsrch=yes&srchctgry=0&srchvl=" + strUserID;
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "UserGroupAssignmentCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
