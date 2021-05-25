package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class UserIDCtlr extends AlltelServlet
{
	public void myservice (AlltelRequest request, AlltelResponse response)
				throws Exception
	{	
		String strURL = "/UserIDView.jsp";
		String strEnc = null;

                Log.write(Log.DEBUG_VERBOSE, "UserIDCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

		// Instantiate the UserID Bean
		UserIDBean beanUserID = new UserIDBean();
		request.getHttpRequest().setAttribute("useridbean", beanUserID);

		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		Log.write(Log.DEBUG, "UserIDCtlr() strDbAction = " + strDbAction);
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

		String strFrstNm = request.getParameter("FRST_NM");
		if ((strFrstNm == null) || (strFrstNm.length() == 0))
		{
			// Handle the error
		}

		String strLstNm = request.getParameter("LST_NM");
		if ((strLstNm == null) || (strLstNm.length() == 0))
		{
			// Handle the error
		}
                
                String strEmlId = request.getParameter("EMAIL");
		if ((strEmlId == null) || (strEmlId.length() == 0))
		{
			// Handle the error
		}

		String strUnEncrptdPsswd = request.getParameter("ENCRPTD_PSSWD");
		if ((strUnEncrptdPsswd == null) || (strUnEncrptdPsswd.length() == 0))
		{
			// Handle the error
		}

		String strChngPsswd = request.getParameter("CHNG_PSSWD");
		if ((strChngPsswd == null) || (strChngPsswd.length() == 0))
		{
			strChngPsswd = "no";
		}
		Log.write(Log.DEBUG, "strChngPsswd = " + strChngPsswd);

		String strForcePsswrdChg = request.getParameter("FRC_PSSWD_CHNG");
		if ((strForcePsswrdChg == null) || (strForcePsswrdChg.length() == 0))
		{	strForcePsswrdChg = "N";
		}
		if (strForcePsswrdChg.equals("yes"))
		{	strForcePsswrdChg = "Y";
		}
		else
		{	strForcePsswrdChg = "N";
		}
		Log.write(Log.DEBUG, "strForcePsswrdChg = " + strForcePsswrdChg);
		
		//NOTE: The text of this checkbox toggles between "Enable" and "Disable", but
		// both return 'yes' if checked.
		String strEnableOrDisablePsswrd = request.getParameter("ENABLE_PSSWD");
		if ((strEnableOrDisablePsswrd == null) || (strEnableOrDisablePsswrd.length() == 0))
		{	strEnableOrDisablePsswrd = "N";
		}
		if (strEnableOrDisablePsswrd.equals("yes"))
		{	strEnableOrDisablePsswrd = "Y";
		}
		else
		{	strEnableOrDisablePsswrd = "N";
		}
		Log.write(Log.DEBUG, "strEnableOrDisablePsswrd = " + strEnableOrDisablePsswrd);

		String strPsswdRcvrQstn = request.getParameter("PSSWD_RCVR_QSTN");
		if ((strPsswdRcvrQstn == null) || (strPsswdRcvrQstn.length() == 0))
		{
			// Handle the error
		}

		String strPsswdRcvrNswr = request.getParameter("PSSWD_RCVR_NSWR");
		if ((strPsswdRcvrNswr == null) || (strPsswdRcvrNswr.length() == 0))
		{
			// Handle the error
		}

		String strCmpnySqncNmbr = request.getParameter("CMPNY_SQNC_NMBR");
		if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0))
		{
			// Handle the error
		}

		String strMdfdDt = request.getParameter("MDFD_DT");
		if ((strMdfdDt == null) || (strMdfdDt.length() == 0))
		{
			// Handle the error
		}

		String strDisabled = request.getParameter("DSBLD_USERID");
		if ((strDisabled == null) || (strDisabled.length() ==0))
			strDisabled="N";
		Log.write(Log.DEBUG, "strDisabled = " + strDisabled);

		//User has authority to Controller to get here, now see if they have authority to function
		if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanUserID.getTblAdmnScrtyTgView())) ||
			(strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanUserID.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("new") 	 && !sdm.isAuthorized(beanUserID.getTblAdmnScrtyTgAdd())) ||
			(strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanUserID.getTblAdmnScrtyTgMod())) ||
			(strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanUserID.getTblAdmnScrtyTgDel()))   )
		{
			Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Populate the Bean
		beanUserID.setUserID(strUserID);
		beanUserID.setFrstNm(strFrstNm);
		beanUserID.setLstNm(strLstNm);
                beanUserID.setEmlId(strEmlId);
		beanUserID.setEncrptdPsswd(strUnEncrptdPsswd);
		beanUserID.setChngPsswd(strChngPsswd);
		beanUserID.setPsswdRcvrQstn(strPsswdRcvrQstn);
		beanUserID.setPsswdRcvrNswr(strPsswdRcvrNswr);
		beanUserID.setCmpnySqncNmbr(strCmpnySqncNmbr);
		beanUserID.setMdfdDt(strMdfdDt);
		beanUserID.setMdfdUserid(sdm.getUser());
		beanUserID.setDbAction(strDbAction);
		
		beanUserID.setFrcPsswdChg(strForcePsswrdChg);
		if (strDisabled.equals("Y") && strEnableOrDisablePsswrd.equals("Y"))
		{
			beanUserID.setDsbldUserID("N");	//User was disabled, now enable
			beanUserID.setLgnAttmpts(0);
		}
		else
		if (strDisabled.equals("N") && strEnableOrDisablePsswrd.equals("Y"))
		{
			beanUserID.setDsbldUserID("Y");	//User was OK, but now disable
		}
		else
		{
			beanUserID.setDsbldUserID(strDisabled);	//no change
		}

		// Validate the Bean
		if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow"))
		{
			if (!beanUserID.validateUserIDBean())
			{
				// Send error msg back to view
				strURL= "/UserIDView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}
		}

		if (strChngPsswd.equals("yes"))
		{
			// Now that UnEncrypted Password has been validated, we can encrypt it
			if (!strDbAction.equals("new"))	//Dont encrypt if 'new' since its empty
			{
				//Note: password entered via html page is unencrypted, so encrypt here before persisting
				strEnc = Toolkit.encryptPassword(strUnEncrptdPsswd);
			}
			else
				strEnc = strUnEncrptdPsswd;
		
			beanUserID.setEncrptdPsswd(strEnc);
		}

		// Perform requested action
		if (strDbAction.equals("InsertRow"))
		{
			// Store to DB
			if (beanUserID.saveUserIDBeanToDB() != 0)
			{
				strURL= "/UserIDView.jsp?action=" + strDbAction;
			}
			else
			{
				// Save the User ID to the session for later user
				HttpSession session = request.getSession();
				session.setAttribute("UserID_userid", strUserID);

				strURL = "/TableAdminCtlr?tblnmbr=4&rstrctsrch=yes&srchctgry=0&srchvl=" + strUserID;
			}
		}
		else if (strDbAction.equals("UpdateRow"))
		{
			// Verify that no one else has modifed this row since it was retrieved
			if (!beanUserID.validateMdfdDt())
			{
				// Send error msg back to view
				strURL= "/UserIDView.jsp?action=" + strDbAction;
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			// Update DB
			if (beanUserID.updateUserIDBeanToDB() != 0)
			{
				strURL= "/UserIDView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=3";
			}
		}
		else if (strDbAction.equals("get"))
		{
			// Save the User ID to the session for later user
			HttpSession session = request.getSession();
			session.setAttribute("UserID_userid", strUserID);

			// Retrieve from DB
			if (beanUserID.retrieveUserIDBeanFromDB() != 0)
			{
				strURL= "/UserIDView.jsp?action=" + strDbAction;
			}
		}
		else if (strDbAction.equals("DeleteRow"))
		{
			// Delete from DB
			if (beanUserID.deleteUserIDBeanFromDB() != 0)
			{
				strURL= "/UserIDView.jsp?action=" + strDbAction;
			}
			else
			{
				strURL = "/TableAdminCtlr?tblnmbr=3";
			}
		}
		else if (strDbAction.equals("new"))
		{
			// Initialize the Bean
			beanUserID.setUserID("");
			beanUserID.setFrstNm("");
			beanUserID.setLstNm("");
                        beanUserID.setEmlId("");
			beanUserID.setEncrptdPsswd("");
			beanUserID.setPsswdRcvrQstn("");
			beanUserID.setPsswdRcvrNswr("");
			beanUserID.setCmpnySqncNmbr("");
			beanUserID.setMdfdDt("");
			beanUserID.setMdfdUserid("");
			beanUserID.setDsbldUserID("N");
			beanUserID.setFrcPsswdChg("N");
			beanUserID.setLgnAttmpts(0);
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/TableAdminCtlr?tblnmbr=3";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		// Pass the request and response to the JSP 
		Log.write(Log.DEBUG, "UserIDCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
                return;
	}

	protected void populateVariables()
			throws Exception
	{
	}
}


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SERVLET/UserIDCtlr.java  $
/*
/*   Rev 1.0.1.0   25 Feb 2002 10:28:24   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:07:00   wwoods
/*Initial Checkin
*/

/* $Revision:   1.0.1.0  $
*/
