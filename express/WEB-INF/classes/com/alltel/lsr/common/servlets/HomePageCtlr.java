package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class HomePageCtlr extends AlltelServlet
{
	protected void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/HomePageView.jsp";
		Log.write(Log.DEBUG_VERBOSE, "HomePageCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate new bean
		HomePageBean beanHomePage = new HomePageBean();
		request.getHttpRequest().setAttribute("homepagebean", beanHomePage);
		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "HomePageCtlr() strDbAction = " + strDbAction);
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		String strNoteSqncNmbr = request.getParameter("NOTE_SQNC_NMBR");
		if ((strNoteSqncNmbr == null) || (strNoteSqncNmbr.length() == 0))
		{
			strNoteSqncNmbr = "";
		}
	
		String strNoteStrtDt = request.getParameter("NOTE_STRT_DT");
		if ((strNoteStrtDt == null) || (strNoteStrtDt.length() == 0))
		{
			strNoteStrtDt = "";
		}
		strNoteStrtDt = strNoteStrtDt.replace('/','-');
	
		String strNoteEndDt = request.getParameter("NOTE_END_DT");
		if ((strNoteEndDt == null) || (strNoteEndDt.length() == 0))
		{
			strNoteEndDt = "";
		}
		strNoteEndDt = strNoteEndDt.replace('/','-');

		String strNoteMsg = request.getParameter("NOTE_MSG");
		if ((strNoteMsg == null) || (strNoteMsg.length() == 0))
		{
			strNoteMsg = "";
		}
	
		String[] strCmpnyTyp = request.getAttributeValue("cmpny_typ");
		String strCmpnyTypList = "";
		if (strCmpnyTyp != null)
		{
			for (int i=0; i<strCmpnyTyp.length; i++)
			{
				strCmpnyTypList = strCmpnyTypList + strCmpnyTyp[i];
			}
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			strMdfdDt = "";
		}

		
		String[] strStates =  request.getAttributeValue( "NT_STTS" );
		
		String strNoteTyp = request.getParameter("NT_TYP");
		if ((strNoteTyp == null) || (strNoteTyp.length() == 0))
		{
			strNoteTyp = "0";
		}
		String strTitle = request.getParameter("NT_TITLE");
		if ((strTitle == null) || (strTitle.length() == 0))
		{
			strTitle = "";
		}
	
		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanHomePage.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanHomePage.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanHomePage.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanHomePage.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanHomePage.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "HomePageCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanHomePage.setNoteSqncNmbr(strNoteSqncNmbr);
		beanHomePage.setNoteStrtDt(strNoteStrtDt);
		beanHomePage.setNoteEndDt(strNoteEndDt);
		beanHomePage.setDbAction(strDbAction);
		beanHomePage.setNoteMsg(strNoteMsg);
		beanHomePage.setCmpnyTypList(strCmpnyTypList);
		beanHomePage.setMdfdDt(strMdfdDt);
		beanHomePage.setMdfdUserid(sdm.getUser());
		beanHomePage.setNoteTyp( strNoteTyp );
		beanHomePage.setNoteStates( strStates );
		beanHomePage.setNoteTitle( strTitle );
		// Validate the Bean
		
		
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanHomePage.validateHomePageBean())
			{
				// Send error msg back to view
				strURL= "/HomePageView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanHomePage.saveHomePageBeanToDB() != 0)
			{
				strURL= "/HomePageView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=12";
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanHomePage.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/HomePageView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanHomePage.updateHomePageBeanToDB() != 0)
			{
				strURL= "/HomePageView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=12";
			}
		}
		else if (strDbAction.equals("get"))
		{
			
		
			// Retrieve from DB
			if (beanHomePage.retrieveHomePageBeanFromDB() != 0)
			{
				strURL= "/HomePageView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanHomePage.deleteHomePageBeanFromDB() != 0)
			{
				strURL= "/HomePageView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=12";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanHomePage.setNoteSqncNmbr("");
			beanHomePage.setNoteStrtDt("");
			beanHomePage.setNoteEndDt("");
			beanHomePage.setNoteMsg("");
			beanHomePage.setCmpnyTypList("");
			beanHomePage.setMdfdDt("");
			beanHomePage.setMdfdUserid("");
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=12";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "HomePageCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

        protected void populateVariables()
		throws Exception
	{
	}
}
