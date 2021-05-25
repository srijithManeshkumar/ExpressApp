package com.alltel.lsr.common.objects;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.text.DecimalFormat;

import javax.servlet.*;
import javax.servlet.http.*;
import weblogic.common.*;

// sun jsdt import
//import com.sun.media.jsdt.*;

//
import com.alltel.lsr.common.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.error.objects.*;

public abstract class AlltelServlet extends HttpServlet
{
	protected final String SecurityBreachURL = "/LsrSecurity.jsp";
	private final String PostOn = "Y";
	protected boolean ORACLE;

	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		try {
			//Read AlltelServlet specific config values into class mbrs
			populateAlltelServletVariables();
			populateVariables();
		}
		catch (Exception e) {
			Log.write(e.getMessage());
		}
	}

	public void service (HttpServletRequest request, HttpServletResponse response)
	{
		//Create AlltelRequest object
		AlltelRequest alltelRequest = new AlltelRequest(request);
		AlltelResponse alltelResponse = new AlltelResponse(response);
		SessionDataManager sdm = alltelRequest.getSessionDataManager();

		String strModuleName = this.getClass().getName();
		int iLastDot = strModuleName.lastIndexOf(".");		//get position of last chunk
		String strServlet = strModuleName.substring(iLastDot+1);	//get last chunk

		try {
			if (AlltelSecurityManager.preservice(alltelRequest, alltelResponse, strServlet))
			{	//Log.write(Log.DEBUG, "got past security - calling servlet now ");
				this.myservice(alltelRequest, alltelResponse);
				AlltelSecurityManager.postservice(alltelRequest, alltelResponse, strServlet);
			}
		} //try
		catch (Exception e) {
			e.printStackTrace();
			Log.write(Log.ERROR, "AlltelServlet() exception caught\n" + e.toString()  );
			try {
				// invoke exception handler. This logs the stack trace, sends the error details to
				// the audit engine and displays the user error page.
				ExceptionHandler.handleException(e, alltelRequest, alltelResponse);
			}
			catch (Exception newException)
			{ // nothing we can do
				Log.write(newException.getMessage()); 
			}
		} //catch
		finally
		{ 	alltelRequest.putSessionDataManager(sdm);
		}

	} // end of service function
																	

	/**
	* myservice() method is the place where the business logic resides. This is an abstract
	*  method and will be overidden by subclasses
	* @author      lsr
	* @exception Exception
	*/
	protected abstract void myservice (AlltelRequest alltelRequest, AlltelResponse alltelResponse)
		throws  Exception; 

	/**
	* abstract method to populate individual servlet's configuration variables. Needs to be implemented
	* by individual servlets 
	* @author      lsr
	* @exception   Exception   
	* @return      void 
	*/
	protected abstract void populateVariables()
			throws Exception;

	/**
	* populateAlltelServletVariables() method is used by AlltelServlet only to load its own configurable parameters
	* from the properties file. No subclass shall be allowed to override this method.
	*
	* @exception   Exception   thrown by the PropertiesManager.getProperty method
	* @author      lsr
	* @return      void
	*/     
	private final void populateAlltelServletVariables()
		throws Exception                
	{
		// nothing
	}

}	//end
