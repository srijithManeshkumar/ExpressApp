package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class SecurityGroupCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/SecurityGroupView.jsp";

                Log.write(Log.DEBUG_VERBOSE, "SecurityGroupCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

                // Instantiate the UserID Bean
		SecurityGroupBean beanSecurityGroup = new SecurityGroupBean();
		request.getHttpRequest().setAttribute("securitygroupbean", beanSecurityGroup);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "SecurityGroupCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strScrtyGrpCd = request.getParameter("SCRTY_GRP_CD");
		if ((strScrtyGrpCd == null) || (strScrtyGrpCd.length() == 0))
		{
			// Handle the error
		}
		else
		{
			// Save the User ID to the session for later user
			HttpSession session = request.getSession();
			session.setAttribute("SecGrp_scrtygrpcd", strScrtyGrpCd);
		}

		String strScrtyGrpDscrptn = request.getParameter("SCRTY_GRP_DSCRPTN");
		if ((strScrtyGrpDscrptn == null) || (strScrtyGrpDscrptn.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanSecurityGroup.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanSecurityGroup.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanSecurityGroup.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanSecurityGroup.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanSecurityGroup.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "SecurityGroupCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanSecurityGroup.setScrtyGrpCd(strScrtyGrpCd);
		beanSecurityGroup.setScrtyGrpDscrptn(strScrtyGrpDscrptn);
		beanSecurityGroup.setMdfdDt(strMdfdDt);
		beanSecurityGroup.setMdfdUserid(sdm.getUser());
		beanSecurityGroup.setDbAction(strDbAction);

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanSecurityGroup.validateSecurityGroupBean())
			{
				// Send error msg back to view
				strURL= "/SecurityGroupView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanSecurityGroup.saveSecurityGroupBeanToDB() != 0)
			{
				strURL= "/SecurityGroupView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=8";
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanSecurityGroup.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/SecurityGroupView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanSecurityGroup.updateSecurityGroupBeanToDB() != 0)
			{
				strURL= "/SecurityGroupView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=8";
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanSecurityGroup.retrieveSecurityGroupBeanFromDB() != 0)
			{
				strURL= "/SecurityGroupView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanSecurityGroup.deleteSecurityGroupBeanFromDB() != 0)
			{
				strURL= "/SecurityGroupView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=8";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanSecurityGroup.setScrtyGrpCd("");
			beanSecurityGroup.setScrtyGrpDscrptn("");
			beanSecurityGroup.setMdfdDt("");
			beanSecurityGroup.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=8";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "SecurityGroupCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
	       throws Exception
	{
	}
}
