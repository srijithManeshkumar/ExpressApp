/* 
 * MODULE:		AlltelSecurityManager
 * 
 * DESCRIPTION: 	This class will be called automatically when a servlet gets invoked.
 *			It will determine if a user has timed out and if they have the
 *			authority to execute the servlet they requested.
 * 
 * AUTHOR:		psedlak
 * 
 * DATE:                11/30/2001		
 * 
 * HISTORY:
 *      02/06/2002  psedlak Express 1.1 changes for request locking and unlocking.
 *                          Also removed some hardcoded values and put them in properties file.
 */


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/OBJECT/AlltelSecurityManager.java  $/*
/*   Rev 1.1   11 Feb 2002 09:32:50   sedlak
/*release 1.1

/*
/*   Rev 1.0   23 Jan 2002 11:05:30   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

package com.alltel.lsr.common.objects;

// JDK import
import java.util.*;
import java.io.*;
import java.text.DecimalFormat;

// JSDK import
import javax.servlet.*;
import javax.servlet.http.*;
// Weblogic import
//import weblogic.common.*;

import com.alltel.lsr.common.util.*;
import com.alltel.lsr.common.error.objects.ExceptionHandler;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.objects.*;


public class AlltelSecurityManager
{
        // These are only DEFAULTs - see application properties file for current value.
        private static final String INACTIVITY_URL = "TimeOut.jsp";
	private static final String INACTIVITY_THRESHOLD_MINS = "90";    //default in MINUTES
	//private static final String INACTIVITY_THRESHOLD_MINS = "45";    //default in MINUTES
 		 
	/**
	 * This method is called prior to entering service() method for
	 * a servlet.
	 * @param	request		The request object
	 * @param	response	The response object
	 * @param	servlet		The servlet
	 */
	public static boolean preservice (AlltelRequest alltelRequest,
					  AlltelResponse alltelResponse, 
					  String strServlet) 
	{
		boolean bSuccess = false;
		boolean bUserLoggedIn = false;
		
		Long LActiveTimeStamp = null;
		String strActivePage = null;
		float fElapsedSessionTimeInMins = 0;
		float fElapsedSessionTimeInSecs = 0;
		long lElapsedSessionTimeStamp = 0;
		long lCurrentTimeStamp = 0;
		
		//Log.write(Log.DEBUG_VERBOSE, "AlltelSecurityMgr() ");
		
		// create SDM if it doesnt exist and get session object
		SessionDataManager sdm = alltelRequest.getSessionDataManager();
	    	HttpSession objSession = alltelRequest.getSession();
	
		//LSR - has the user logged in before?
		bUserLoggedIn = sdm.isUserLoggedIn();
		//Log.write(Log.INFO, "AlltelSecurityMgr() Logged In =" + bUserLoggedIn );

		//Establish active session time stamp either through the current time or from 
		//a predefined memory variable
	        Long LActiveSessionTimeStamp = (Long)objSession.getAttribute("ActiveSessionTimeStamp");
       		if (LActiveSessionTimeStamp != null)
		{
			//Log.write(Log.DEBUG_VERBOSE, "AlltelSecurityMgr() session timestamp already set");
	        	LActiveTimeStamp = LActiveSessionTimeStamp;
	    	}
		else
		{
			//Log.write(Log.DEBUG_VERBOSE, "AlltelSecurityMgr() setting session timestamp");
	       		long lActiveTimeStamp = new java.util.Date().getTime();
	        	objSession.setAttribute("ActiveSessionTimeStamp", new Long(lActiveTimeStamp));
	        	LActiveTimeStamp = new Long(lActiveTimeStamp);
	    	}
       
	        //Calculate the elapsed time from a predefined active time stamp(see above)        	    
		lCurrentTimeStamp = new java.util.Date().getTime();
	        lElapsedSessionTimeStamp = lCurrentTimeStamp - LActiveTimeStamp.longValue();
        	fElapsedSessionTimeInSecs = ((float)lElapsedSessionTimeStamp)/1000;
	        fElapsedSessionTimeInMins = (fElapsedSessionTimeInSecs)/60;	
		
       		try 
		{                   
		    //Attain configurable session time out value
		    Long lSessionTimeOutValue = new Long(PropertiesManager.getProperty("lsr.inactivity.timeout", INACTIVITY_THRESHOLD_MINS));
		    
            	   /* Check if elapsed session time has exceed time limit and if so set boolean to true if
              	    * current controller is not LOGINCTLR, RESIGNONCTLR or LOGOUTCTLR.
            	    **/
	            if ((fElapsedSessionTimeInMins > (float)lSessionTimeOutValue.longValue() && 
                	((!strServlet.toUpperCase().equals("RESIGNONCTLR")) &&
                	(!strServlet.toUpperCase().equals("LOGINCTLR")))) )
		    {	
                        //NOTE: IF WEBLOGIC (webserver) invalidates the user's session -you will never
                        // get here - because you have not exceeded the INACTIVITY_THRESHOLD_MINS. (The elapsed
                        // time will be 0 since you just put "ActiveSessionTimeStamp" into the session.)
                        // You will only get here if you exceeded our internal INACTIVITY_THRESHOLD_MINS 
                        /// BUT STILL have NOT exceeded the weblogic session threshold defined in weblogic.properties
                        // file.
			Log.write(Log.INFO, "AlltelSecurityMgr() this user <" + sdm.getUser() + "> timed out.");
                	//PJS -not used so I commented out below 2 lines
                	//String strURL = getURL(alltelRequest.getHttpRequest(), alltelResponse.getHttpResponse());
                        //objSession.setAttribute("ActiveURL", strURL);  

			//NOTE not doing anything fancy, such as saving requests, url or whatever.
			//Logoff user, let them know they timed out, and force to login page.
			logoff(alltelRequest, sdm);
			alltelResponse.sendRedirect( PropertiesManager.getProperty("lsr.inactivity.url", INACTIVITY_URL) );

            	    }
		    else
		    {
                	//Put the current time into session
                	objSession.setAttribute("ActiveSessionTimeStamp", new Long(lCurrentTimeStamp));

			//If user is not logged in and Not already going to login page, then force to login page
			if ( (!bUserLoggedIn) && (!strServlet.toUpperCase().equals("LOGINCTLR") ) )
			{
				Log.write(Log.INFO, "AlltelSecurityMgr() Forcing user to login page");
	            		alltelRequest.putSessionDataManager(sdm);
	        		
				alltelRequest.getHttpRequest().setAttribute("levent", "login");
	        		alltelRequest.getHttpRequest().setAttribute("loginstat", "Must login first");
				alltelResponse.sendRedirect("LoginCtlr");
			}
			else
			{
				//User authorized to run servlet or its the login page....
				if (strServlet.toUpperCase().equals("LOGINCTLR") || strServlet.toUpperCase().equals("HELPDOCCTLR"))
				{
					//Log.write(Log.INFO, "AlltelSecurityMgr() user attempting to login");
                                        Log.write(Log.DEBUG_VERBOSE, "AlltelSecurityMgr() " + strServlet);
					alltelRequest.putSessionDataManager(sdm);
					bSuccess = true;

				}
				else 
				if (sdm.isAuthorized(strServlet))
				{
					//Log.write(Log.INFO, "AlltelSecurityMgr() user HAS authority to servlet = " + strServlet);
					alltelRequest.putSessionDataManager(sdm);
					bSuccess = true;
				}
				else
				{ 
					Log.write(Log.INFO, "The user is not authorized to access " + strServlet);
					//send them to security error page
					alltelResponse.sendRedirect( PropertiesManager.getProperty("lsr.security.url", "LsrSecurity.jsp") );
					//throw new SecurityException("The user is not authorized to access " + strServlet);
				}
		 	}
		    }			
		}//try
		catch (Exception e) 
		{
			Log.write(Log.ERROR, e.getMessage());
            		try {
                	// invoke exception handler. This logs the stack trace, sends the error details to
	                // the audit engine and displays the user error page.
       			ExceptionHandler.handleException(e, alltelRequest, alltelResponse);
            		} 
			catch (Exception newException)
           		{
		                // nothing we can do
				Log.write(Log.ERROR, newException.getMessage());
            		}
		} 
		finally 
		{
			alltelRequest.putSessionDataManager(sdm);
		}
		
		//Log.write(Log.DEBUG_VERBOSE, "~AlltelSecurityMgr() ");
		
		return bSuccess;

	} // end of service function
	
	
	
	public static void postservice(AlltelRequest alltelRequest,
				       AlltelResponse alltelResponse,
				       String strServlet)
	{
		//SessionDataManager sdm = alltelRequest.getSessionDataManager();

		//Log.write(Log.DEBUG_VERBOSE, "AlltelSecurityMgr() postservice()");
		//Log.write(Log.DEBUG_VERBOSE, "~AlltelSecurityMgr() postservice");
	}
	
	public static void logoff(AlltelRequest request, SessionDataManager sdm)
	{
		String strNextVarName="";
		Log.write(Log.DEBUG, "AlltelSecurityMgr() clearing session attributes for timed out user=" + sdm.getUser());
		sdm.logoff();
		request.putSessionDataManager(sdm);
		try {
			Enumeration enumuration = request.getSession().getAttributeNames();    //all session vars
			while (enumuration.hasMoreElements())
			{
				strNextVarName = (String)enumuration.nextElement();
				if (strNextVarName.compareTo("ActiveSessionTimeStamp") != 0) 
				{ 
					Log.write(Log.DEBUG, "AlltelSecurityMgr() logOff removing = " + strNextVarName);
					request.getSession().removeAttribute(strNextVarName);
				}
			}
		}
		catch(Exception e) 
		{	//can happen if session was already invalidated...
		}
		
	}
	
	/**
	 * This method returns the URL of the request.
	 * @param req	HttpServletRequest	the http request
	 * @param res	HttpServletResponse	the http response
	 * @return String	the url returned.
	 */
	private static String getURL(HttpServletRequest req, HttpServletResponse res)
	{
		  StringBuffer strBufURL = null;
		  String strURL = null;
		  String strQuery = null;
		  
		  String strScheme = null;
		  String strHost = null;
		  String strPort = null;
		  
		  //strQuery = req.getQueryString();
		  // kxz255 uncomented above line and commented out the following line to remove page sharing
		  strURL = req.getServletPath();
		  //strURL = "ForwardToPage="+strURL;
		  
		  strQuery = ""; 
		  Enumeration e= req.getParameterNames();
		  String name = null;
		  while (e.hasMoreElements())
		  {
			name = (String) e.nextElement();
			//System.out.println("Name=" + name);
					  
			String [] strs = req.getParameterValues(name);
			for (int i=0; strs!=null & i < strs.length; i++)
			{
				//System.out.println("value=" + strs[i]);
				strQuery = strQuery + "&" + name + "=";
				try {
					strQuery = strQuery + java.net.URLEncoder.encode(strs[i], "UTF-8");
				} catch (Exception encodeExc) {}
			}
		  }//while()
			 
		  if (req.getMethod().equalsIgnoreCase("POST"))
		  {
			  if (req.getParameter("RESET.y") == null)
			  {
				strQuery = strQuery + "&RESET.y=5";
			  }
		  }
		  if (strQuery != null)
		  {
			  strURL = strURL + "?" + strQuery;
			  // kxz255 uncomented above line and commented out the following line to remove page sharing
			  //strURL = strURL  + strQuery;
		  }
		 
		  strScheme = req.getScheme();
		  strHost = req.getServerName();
		  strPort = ""+req.getServerPort();
		  
		  String strTmp = strScheme + "://" + strHost;
		  if (strPort != null)
		  {
			  strTmp = strTmp + ":" + strPort;
		  }
		  
		  //strURL = strTmp + "/PageSharingServlet?" + strURL;
		  
		  Log.write("*****************strURL=" + strURL);
		  return strURL;
	}
  
}
