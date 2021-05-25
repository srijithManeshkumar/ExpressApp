/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL INFORMATION SERVICES
 */

/** 
 * MODULE:		RequestUnlockCtlr.java
 * 
 * DESCRIPTION: Accepts a request sequence number as input parameter. It then
 *          unlocks the request.
 * 
 * AUTHOR:      Paul Sedlak
 * 
 * DATE:        01-31-2002
 * 
 * HISTORY:
 *	xx/xx/2002  initial check-in.
 *
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SERVLET/RequestUnlockCtlr.java  $
/*
/*   Rev 1.2   25 Feb 2002 10:27:44   sedlak
/* 
/*
/*   Rev 1.1   21 Feb 2002 10:41:40   dmartz
/* 
/*
/*   Rev 1.0   11 Feb 2002 14:07:02   sedlak
/*release 1.1
/*
*/
/* $Revision:   1.2  $
*/

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class RequestUnlockCtlr extends AlltelServlet
{
    private final static String UNLOCK_SECURITY_OBJECT = "UNLOCK_REQUESTS";
    
	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception
	{	
		String strURL = "/RequestLockView.jsp";

		Log.write(Log.DEBUG_VERBOSE, "RequestUnlockCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
			
		String strRqstSqncNmbr = request.getParameter("rqst");
		if ((strRqstSqncNmbr == null) || (strRqstSqncNmbr.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		//User has authority to Controller to get here, now see if they have authority to unlock
		if ( !sdm.isAuthorized(UNLOCK_SECURITY_OBJECT)  )
		{
			Log.write(Log.WARNING, "RequestUnlockCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "RequestUnlockCtlr(): Request=" + strRqstSqncNmbr);
		//Instantiate bean
		RequestLockBean objRqst = new RequestLockBean(strRqstSqncNmbr);

		// Unlock it
                if ( objRqst.unlockRequest() > 0 )
		{
			// log error and forward to error page.
			Log.write(Log.ERROR, "RequestUnlockCtlr(): Unlocking Request=" + strRqstSqncNmbr); 
			strURL = "/LsrErr.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "RequestUnlockCtlr(): Request unlocked");
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
