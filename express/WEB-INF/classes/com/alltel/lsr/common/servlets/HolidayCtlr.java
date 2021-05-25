package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class HolidayCtlr extends AlltelServlet
{
	protected void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/HolidayView.jsp";

		Log.write(Log.DEBUG_VERBOSE, "HolidayCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate new bean
		HolidayBean beanHoliday = new HolidayBean();
		request.getHttpRequest().setAttribute("holidaybean", beanHoliday);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "HolidayCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strHldyDt;

		if (strDbAction.equals("InsertRow"))
		{
			String strHldyDy = request.getParameter("HLDY_DY");
			if ((strHldyDy == null) || (strHldyDy.length() == 0))
			{
				// Handle the error
			}
			else if (strHldyDy.length() == 1)
			{
				strHldyDy = "0" + strHldyDy;
			}
	
			String strHldyMnth = request.getParameter("HLDY_MNTH");
			if ((strHldyMnth == null) || (strHldyMnth.length() == 0))
			{
				// Handle the error
			}
			else if (strHldyMnth.length() == 1)
			{
				strHldyMnth = "0" + strHldyMnth;
			}
	
			String strHldyYr = request.getParameter("HLDY_YR");
			if ((strHldyYr == null) || (strHldyYr.length() == 0))
			{
				// Handle the error
			}
			else if (strHldyYr.length() == 2)
			{
				strHldyYr = "20" + strHldyYr;
			}
	
			strHldyDt = strHldyMnth + "/" + strHldyDy + "/" + strHldyYr;
	
		}
		else // it must be a "get"
		{
			strHldyDt = request.getParameter("HLDY_DT");
			if ((strHldyDt == null) || (strHldyDt.length() == 0))
			{
				// Handle the error
			}
		}

		String strHldyDscrptn = request.getParameter("HLDY_DSCRPTN");
		if ((strHldyDscrptn == null) || (strHldyDscrptn.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanHoliday.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanHoliday.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanHoliday.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanHoliday.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanHoliday.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "HolidayCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanHoliday.setHldyDt(strHldyDt);
		beanHoliday.setDbAction(strDbAction);
		beanHoliday.setHldyDscrptn(strHldyDscrptn);
		beanHoliday.setMdfdDt(strMdfdDt);
		beanHoliday.setMdfdUserid(sdm.getUser());

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanHoliday.validateHolidayBean())
			{
				// Send error msg back to view
				strURL= "/HolidayView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanHoliday.saveHolidayBeanToDB() != 0)
			{
				strURL= "/HolidayView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=10";
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanHoliday.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/HolidayView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanHoliday.updateHolidayBeanToDB() != 0)
			{
				strURL= "/HolidayView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=10";
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Retrieve from DB
			if (beanHoliday.retrieveHolidayBeanFromDB() != 0)
			{
				strURL= "/HolidayView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanHoliday.deleteHolidayBeanFromDB() != 0)
			{
				strURL= "/HolidayView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=10";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanHoliday.setHldyDt("");
			beanHoliday.setHldyDscrptn("");
			beanHoliday.setMdfdDt("");
			beanHoliday.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=10";
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
