/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2003
 *					BY
 *			ALLTEL INFORMATION SERVICES
 */

/** 
 * MODULE:	BillDisputeUnlockCtlr.java
 * 
 * DESCRIPTION: Accepts a dispute sequence number as input parameter. It then
 *          unlocks the dispute.
 * 
 * AUTHOR:      V Pavill
 * 
 * DATE:        03-06-2003
 * 
 * HISTORY:
 *	03/15/2003  initial check-in.
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

public class BillDisputeUnlockCtlr extends AlltelServlet
{
    private final static String UNLOCK_SECURITY_OBJECT = "UNLOCK_DISPUTES";
    private final static String m_strTypInd = "B";
    
	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception
	{	
		String strURL = "/BillDisputeLockView.jsp";

		Log.write(Log.DEBUG_VERBOSE, "BillDisputeUnlockCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
			
		String strDsptSqncNmbr = request.getParameter("dspt");
		if ((strDsptSqncNmbr == null) || (strDsptSqncNmbr.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		//User has authority to Controller to get here, now see if they have authority to unlock
		if ( !sdm.isAuthorized(UNLOCK_SECURITY_OBJECT)  )
		{
			Log.write(Log.WARNING, "BillDisputeUnlockCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "BillDisputeUnlockCtlr(): BillDispute=" + strDsptSqncNmbr);
		//Instantiate bean
		LockBean objDspt = new LockBean(m_strTypInd, strDsptSqncNmbr);

		// Unlock it
                if ( objDspt.unlock() > 0 )
		{
			// log error and forward to error page.
			Log.write(Log.ERROR, "BillDisputeUnlockCtlr(): Unlocking BillDispute=" + strDsptSqncNmbr); 
			strURL = "/LsrErr.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "BillDisputeUnlockCtlr(): BillDispute unlocked");
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
