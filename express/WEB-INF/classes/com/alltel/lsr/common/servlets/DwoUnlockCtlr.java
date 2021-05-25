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
 * MODULE:		DwoUnlockCtlr.java
 * 
 * DESCRIPTION: Accepts a dwo sequence number as input parameter. It then
 *          unlocks the dwo.
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/20/2002  initial check-in.
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

public class DwoUnlockCtlr extends AlltelServlet
{
    private final static String UNLOCK_SECURITY_OBJECT = "UNLOCK_DWOS";
    
	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception
	{	
		String strURL = "";

		Log.write(Log.DEBUG_VERBOSE, "DwoUnlockCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
			
		String strDwoSqncNmbr = request.getParameter("dwo");
		if ((strDwoSqncNmbr == null) || (strDwoSqncNmbr.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		//User has authority to Controller to get here, now see if they have authority to unlock
		if ( !sdm.isAuthorized(UNLOCK_SECURITY_OBJECT)  )
		{
			Log.write(Log.WARNING, "DwoUnlockCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "DwoUnlockCtlr(): Dwo=" + strDwoSqncNmbr);

		String pjvn = (String)request.getSession().getAttribute("DwOcHoIcE");

		strURL = "/DwoLockView.jsp?pjvn=" + pjvn;

		String m_strTypInd = "";

		if (pjvn.equals("19"))
			m_strTypInd = "W";
		else
			m_strTypInd = "X";

		//Instantiate bean
		DwoLockBean objDwo = new DwoLockBean(m_strTypInd, strDwoSqncNmbr);

		// Unlock it
                if ( objDwo.unlockDwo() > 0 )
		{
			// log error and forward to error page.
			Log.write(Log.ERROR, "DwoUnlockCtlr(): Unlocking Dwo=" + strDwoSqncNmbr); 
			strURL = "/LsrErr.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		Log.write(Log.DEBUG_VERBOSE, "DwoUnlockCtlr(): Dwo unlocked");
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
