/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL COMMUNICATIONS, INC.
 */

/** 
 * MODULE:		BillingCtlr.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-01-2002
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

public class BillingCtlr extends AlltelServlet
{
    private final static String BILLING_SECURITY_OBJECT = "VIEW_BILLING";
    
	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception
	{	
		String strURL = "/BillingListView.jsp";

		Log.write(Log.DEBUG_VERBOSE, "BillingCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
		
		String strSelect = "SELECT DISTINCT B.BAN, B.BAN_DSCRPTN, R.LD_DT" +
			" FROM USERID_T U, BAN_T B, BAN_REPORT_T R";

		String strWhere = " WHERE U.USERID = '" + sdm.getUser() + "'" +
			" AND U.CMPNY_SQNC_NMBR = B.CMPNY_SQNC_NMBR" +
			" AND B.BAN = R.BAN(+)";

		String strOrderBy = " ORDER BY B.BAN ASC";

		//User has authority to Controller to get here, now see if they have authority to unlock
		if ( !sdm.isAuthorized(BILLING_SECURITY_OBJECT)  )
		{
			Log.write(Log.WARNING, "BillingCtlr() user " + sdm.getUser() + " is trying to bypass security!");
			alltelRequestDispatcher.forward(SecurityBreachURL);
			return;
		}

		// Check for BAN Search
		String strBanSearch = request.getParameter("search");
		if ((strBanSearch != null) && (strBanSearch.length() > 0))
		{
			// Retrieve BAN
			String strBanValue = request.getParameter("ban_srch_value");
			if ((strBanValue != null) && (strBanValue.length() > 0))
			{
				strWhere = strWhere + " AND UPPER(B.BAN) LIKE UPPER('%" + strBanValue + "%')";
			}
		}

		String strBan = request.getParameter("ban");
		String strLdDt = request.getParameter("ld_dt");
		if ((strBan == null) || (strBan.length() == 0) ||
		    (strLdDt == null) || (strLdDt.length() == 0))
		{
			// Build the query and forward to the view
			String strQuery = strSelect + strWhere + strOrderBy;
			request.getHttpRequest().setAttribute("banlistquery", strQuery);

			// Handle the error
			strURL = "/BillingListView.jsp";
			alltelRequestDispatcher.forward(strURL);
		}
		else
		{
			request.getHttpRequest().setAttribute("ban", strBan);
			request.getHttpRequest().setAttribute("ld_dt", strLdDt);
			strURL = "/BillingView.jsp";
			alltelRequestDispatcher.forward(strURL);
		}

		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
