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
 * MODULE:		PreorderUnlockCtlr.java
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

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class PreorderUnlockCtlr extends AlltelServlet
{
    private final static String UNLOCK_SECURITY_OBJECT = "UNLOCK_PREORDERS";
    
	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception
	{	
		String strURL = "/PreorderLockView.jsp";

		Log.write(Log.DEBUG_VERBOSE, "PreorderUnlockCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
			
		String strPreSqncNmbr = request.getParameter("pre");
		if ((strPreSqncNmbr == null) || (strPreSqncNmbr.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		//User has authority to Controller to get here, now see if they have authority to unlock
		if ( !sdm.isAuthorized(UNLOCK_SECURITY_OBJECT)  )
		{
			Log.write(Log.WARNING, "PreorderUnlockCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "PreorderUnlockCtlr(): Preorder=" + strPreSqncNmbr);
		//Instantiate bean
		PreorderLockBean objPre = new PreorderLockBean(strPreSqncNmbr);

		// Unlock it
                if ( objPre.unlockPreorder() > 0 )
		{
			// log error and forward to error page.
			Log.write(Log.ERROR, "PreorderUnlockCtlr(): Unlocking Preorder=" + strPreSqncNmbr); 
			strURL = "/LsrErr.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "PreorderUnlockCtlr(): Preorder unlocked");
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
