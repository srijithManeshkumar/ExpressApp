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
 * MODULE:		DslUnlockCtlr.java
 * 
 * DESCRIPTION: Accepts a dsl sequence number as input parameter. It then
 *          unlocks the dsl order.
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
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

public class DslUnlockCtlr extends AlltelServlet
{
    private final static String UNLOCK_SECURITY_OBJECT = "UNLOCK_DSLS";
    
	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception
	{	
		String strURL = "/DslLockView.jsp";

		Log.write(Log.DEBUG_VERBOSE, "DslUnlockCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
			
		String strDslSqncNmbr = request.getParameter("dsl");
		if ((strDslSqncNmbr == null) || (strDslSqncNmbr.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		//User has authority to Controller to get here, now see if they have authority to unlock
		if ( !sdm.isAuthorized(UNLOCK_SECURITY_OBJECT)  )
		{
			Log.write(Log.WARNING, "DslUnlockCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "DslUnlockCtlr(): Dsl=" + strDslSqncNmbr);
		//Instantiate bean
		DslLockBean objDsl = new DslLockBean(strDslSqncNmbr);

		// Unlock it
                if ( objDsl.unlockDsl() > 0 )
		{
			// log error and forward to error page.
			Log.write(Log.ERROR, "DslUnlockCtlr(): Unlocking Dsl=" + strDslSqncNmbr); 
			strURL = "/LsrErr.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "DslUnlockCtlr(): Dsl unlocked");
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
