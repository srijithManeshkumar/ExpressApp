package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class SecurityGroupAssignmentCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/SecurityGroupAssignmentView.jsp";

                Log.write(Log.DEBUG_VERBOSE, "SecurityGroupAssignmentCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the SecurityGroupAssignmentBean Bean
		SecurityGroupAssignmentBean beanSecurityGroupAssignment = new SecurityGroupAssignmentBean();
		request.getHttpRequest().setAttribute("securitygroupassignmentbean", beanSecurityGroupAssignment);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "SecurityGroupAssignmentCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strScrtyGrpAssgnmntSqncNmbr = request.getParameter("SCRTY_GRP_ASSGNMNT_SQNC_NMBR");
		if ((strScrtyGrpAssgnmntSqncNmbr == null) || (strScrtyGrpAssgnmntSqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strScrtyGrpCd = request.getParameter("SCRTY_GRP_CD");
		if ((strScrtyGrpCd == null) || (strScrtyGrpCd.length() == 0))
		{
			// Handle the error
		}

		String strScrtyObjctCd = request.getParameter("SCRTY_OBJCT_CD");
		if ((strScrtyObjctCd == null) || (strScrtyObjctCd.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanSecurityGroupAssignment.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanSecurityGroupAssignment.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanSecurityGroupAssignment.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanSecurityGroupAssignment.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanSecurityGroupAssignment.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "SecurityGroupAssignmentCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanSecurityGroupAssignment.setScrtyGrpAssgnmntSqncNmbr(strScrtyGrpAssgnmntSqncNmbr);
		beanSecurityGroupAssignment.setScrtyGrpCd(strScrtyGrpCd);
		beanSecurityGroupAssignment.setScrtyObjctCd(strScrtyObjctCd);
		beanSecurityGroupAssignment.setMdfdDt(strMdfdDt);
		beanSecurityGroupAssignment.setMdfdUserid(sdm.getUser());
		beanSecurityGroupAssignment.setDbAction(strDbAction);

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanSecurityGroupAssignment.validateSecurityGroupAssignmentBean())
			{
				// Send error msg back to view
				strURL= "/SecurityGroupAssignmentView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanSecurityGroupAssignment.saveSecurityGroupAssignmentBeanToDB() != 0)
			{
				strURL= "/SecurityGroupAssignmentView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=9&rstrctsrch=yes&srchctgry=0&srchvl=" + strScrtyGrpCd;
			}

		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanSecurityGroupAssignment.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/SecurityGroupAssignmentView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanSecurityGroupAssignment.updateSecurityGroupAssignmentBeanToDB() != 0)
			{
				strURL= "/SecurityGroupAssignmentView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=9&rstrctsrch=yes&srchctgry=0&srchvl=" + strScrtyGrpCd;
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanSecurityGroupAssignment.retrieveSecurityGroupAssignmentBeanFromDB() != 0)
			{
				strURL= "/SecurityGroupAssignmentView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanSecurityGroupAssignment.deleteSecurityGroupAssignmentBeanFromDB() != 0)
			{
				strURL= "/SecurityGroupAssignmentView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=9&rstrctsrch=yes&srchctgry=0&srchvl=" + strScrtyGrpCd;
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanSecurityGroupAssignment.setScrtyGrpAssgnmntSqncNmbr("");
			beanSecurityGroupAssignment.setScrtyGrpCd(strScrtyGrpCd);
			beanSecurityGroupAssignment.setScrtyObjctCd("");
			beanSecurityGroupAssignment.setMdfdDt("");
			beanSecurityGroupAssignment.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=9&rstrctsrch=yes&srchctgry=0&srchvl=" + strScrtyGrpCd;
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "SecurityGroupAssignmentCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
