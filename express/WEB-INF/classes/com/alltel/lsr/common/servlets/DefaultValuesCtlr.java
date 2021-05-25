/*
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				ALLTEL INFORMATION SERVICES
 */

/* 
 * MODULE:		DefaultValuesCtlr.java
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        01-31-2002
 * 
 * HISTORY:
 *	1/31/2002  initial check-in.
 *
 */

/* $Log:     $
/*
*/
/* $Revision:   1.0  $
*/

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class DefaultValuesCtlr extends AlltelServlet
{

	protected void myservice (AlltelRequest request, AlltelResponse response)
		throws Exception
	{	
		String strURL = "/DefaultValuesCtlr.jsp";

		Log.write(Log.DEBUG_VERBOSE, "DefaultValuesCtlr()");
		SessionDataManager sdm = request.getSessionDataManager();
		AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
			
		// Get values entered by the user
		String strDbAction = request.getParameter("action");
		if ((strDbAction == null) || (strDbAction.length() == 0))
		{
			// Handle the error
			strURL = "/NavigationErrorView.jsp";
			alltelRequestDispatcher.forward(strURL);
			return;
		}

		if (strDbAction.equals("Submit"))
		{
			Connection con = null;
			Statement stmt = null;

			try
			{
				con = DatabaseManager.getConnection();
				stmt = con.createStatement();
	
				con.setAutoCommit(false);
	
				// Delete all rows from DEFAULT_USERID_T for this user
				String strQuery = "DELETE DEFAULT_USERID_T WHERE USERID = '" + sdm.getUser() + "'";
				stmt.executeUpdate(strQuery);

				// Retrieve all Parameters
				java.util.Enumeration params = request.getParameterNames().elements();
				while (params.hasMoreElements())
				{
			                RequestAttribute reqParm = (RequestAttribute) params.nextElement();
					String paramName = reqParm.getAttributeName();
					String paramValues[] = reqParm.getAttributeValues();
	
					// did we find a populated parameter
					if ( (paramName.startsWith("_FF")) && (paramValues[0] != null) && (paramValues[0].length() > 0) )
					{  
						// found one, so parse it out and store in DB
						int first_ = paramName.indexOf("_", 4);
						int second_ = paramName.indexOf("_", first_+1);
	
						String iFrm = paramName.substring(4, first_);
						String iFrmSctn = paramName.substring(first_+1, second_); 
						String iFrmFld =  paramName.substring(second_+1);
	
						// Insert Row into DB
						strQuery = "INSERT INTO DEFAULT_USERID_T VALUES ('" + sdm.getUser() + 
							"', '" + iFrm + "','" + iFrmSctn + "','" + iFrmFld + "','" + 
							Toolkit.replaceSingleQwithDoubleQ(paramValues[0]) + "')";
	
						stmt.executeUpdate(strQuery);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				con.rollback();
				DatabaseManager.releaseConnection(con);
				Log.write(Log.DEBUG_VERBOSE, "DefaultValuesCtlr : Exception");
				strURL = "/LsrErr.jsp";
				alltelRequestDispatcher.forward(strURL);
				return;
			}

			con.commit();
			con.setAutoCommit(true);
			DatabaseManager.releaseConnection(con);

			strURL = "/UserOptions.jsp";
		}
		else if (strDbAction.equals("Cancel"))
		{
			strURL = "/UserOptions.jsp";
		}
		else
		{
			strURL = "/NavigationErrorView.jsp";
		}

		Log.write(Log.DEBUG, "DefaultValuesCtlr() strURL = " + strURL );
		alltelRequestDispatcher.forward(strURL);
		return;
	}

	protected void populateVariables()
		throws Exception
	{
	}
}
