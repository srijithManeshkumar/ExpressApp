/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *					BY
 *				ALLTEL Communications Inc
 */

/** 
 * MODULE:	TicketUnlockCtlr.java
 * 
 * DESCRIPTION: Accepts a ticket sequence number as input parameter. It then
 *          unlocks the ticket.
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	03/20/2004  initial check-in.
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

public class DsTicketUnlockCtlr extends AlltelServlet
{
	private final static String UNLOCK_SECURITY_OBJECT = "UNLOCK_DSTICKETS";
	private final static String m_strTypInd = "S";
    
	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception
	{	
		String strURL = "/DsTicketLockView.jsp";

		Log.write(Log.DEBUG_VERBOSE, "DsTicketUnlockCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
			
		String strTcktSqncNmbr = request.getParameter("tckt");
		if ((strTcktSqncNmbr == null) || (strTcktSqncNmbr.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		//User has authority to Controller to get here, now see if they have authority to unlock
		if ( !sdm.isAuthorized(UNLOCK_SECURITY_OBJECT)  )
		{
			Log.write(Log.WARNING, "DsTicketUnlockCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "DsTicketUnlockCtlr(): Ticket=" + strTcktSqncNmbr);
		//Instantiate bean
		LockBean objTckt = new LockBean(m_strTypInd, strTcktSqncNmbr);

		// Unlock it
                if ( objTckt.unlock() > 0 )
		{
			// log error and forward to error page.
			Log.write(Log.ERROR, "DsTicketUnlockCtlr(): Unlocking Ticket=" + strTcktSqncNmbr); 
			strURL = "/LsrErr.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "DsTicketUnlockCtlr(): Ticket unlocked");
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
