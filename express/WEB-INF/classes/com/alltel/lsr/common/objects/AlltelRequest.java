/* 
 * MODULE:		AlltelRequest
 * 
 * DESCRIPTION: 	The AlltelRequest class is used to encapsulate HttpServletRequest
 * 
 * AUTHOR:		cloned from ewave/rhapsody
 * 
 * DATE:		Nov 2001
 * 
 * HISTORY:
 *      2/6/2002    psedlak Release 1.1 Added getURL methods.
 */

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/OBJECT/AlltelRequest.java  $/*
/*   Rev 1.1   11 Feb 2002 09:48:14   sedlak
/*Release 1.1

/*
/*   Rev 1.0   23 Jan 2002 11:05:26   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

package com.alltel.lsr.common.objects;

// JDK import
import com.alltel.lsr.common.util.Log;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;

// JSDK import
import javax.servlet.*;
import javax.servlet.http.*;

import com.google.gson.Gson;

/**
 * AlltelRequest is used to encapsulate HttpServletRequest object. It provides methods to
 * query for URL parameters and methods to interface with the session.
 */
public class AlltelRequest
{
	/**
	 * private member data used to identify Session Data Manager in session
	 */
	final private String SDM_NAME = "SessionDataManager";

	/**
	 * private member data holding request parameters
	 */
	private Vector requestNVCollection;
	
	/**
	 * private member data indicating GET or POST method
	 */
	private boolean bIsGet;
	
	HttpServletRequest request;
	
	/**
	 * AlltelRequest constructor does the following
	 * 1. determine request method, GET or POST
	 * 2. retrieve all request parameters from the request object and out them into a vector
	 * @param  req  HttpServletRequest object passed by the web server.
	 */
	public AlltelRequest(HttpServletRequest req)
	{
		request = req;
                String rqstform = "";
		
		// determine GET or POST
		if(request.getMethod().equals("GET"))
		{ // YES. It is a GET
			bIsGet = true;
		}
		else
		{ // NO. It is a POST
			String strReset = req.getParameter("RESET.y");
			if (strReset != null)
				bIsGet = true;
			else
				bIsGet = false;
		}
		
		//retrieve all request parameters
		requestNVCollection = new Vector();
		Enumeration enumuration = request.getParameterNames();
		while(enumuration.hasMoreElements())
		{ // YES. has more elements
			// GET parameter name
			String strName = (String)enumuration.nextElement();
			// Get parameter values
			String strValues[] = request.getParameterValues(strName);
			if (strValues != null)
			{
                            // Set the Json Data to the NV collection for matched parameter
                            if("expJsonData".equalsIgnoreCase(strName)){
				
                                // Fix for xss attacks characters
                            	String formValues = strValues[0].replace("&0x27;", "'");
                                formValues = formValues.replace("&0x28;", "INTO");
                            	formValues = formValues.replace("&0x29;", "into");
                                formValues = formValues.replace("&0x23;", "#");
                                
                                Gson gson = new Gson();
                                HashMap<String, String> expJson = gson.fromJson(formValues, HashMap.class);
                                
                                Log.write("ExpJsonData Parameter Size : "+expJson.size());
                                // Get submit form name from Json data
                                rqstform = expJson.get("rqstform");
                                
                                //Split the Json data as key and value pairs
                                for(Entry<String, String> entry : expJson.entrySet()){
                                    
                                    String jsonKey = (String)entry.getKey();
                                    Object jsonValue = entry.getValue();
                                    
                                    if(jsonValue instanceof ArrayList){
                                        
                                        ArrayList<String> valueList = (ArrayList)jsonValue ;
                                        String[] jsonStrValues = valueList.toArray(new String[valueList.size()]);
                                        
                                        // put into NV collection
                                        RequestAttribute reqAttr = new RequestAttribute(jsonKey, jsonStrValues);
                                        requestNVCollection.addElement(reqAttr);
                                    }else{
                                        String[] jsonStrValues = new String[]{(String)jsonValue};
                                        
                                        // put into NV collection
                                        RequestAttribute reqAttr = new RequestAttribute(jsonKey, jsonStrValues);
                                        requestNVCollection.addElement(reqAttr);
                                    }
                                }
				}else{
					// put into NV collection
					RequestAttribute reqAttr = new RequestAttribute(strName, strValues);
					requestNVCollection.addElement(reqAttr);
				}
			}
		}
                //LOG - Request Information. 
                SessionDataManager sdm = this.getSessionDataManager();
                String strUSERID = sdm.getUser();
		Log.write("AlltelRequest() --- REQUEST INFO | " + strUSERID + " | " + rqstform + " | " + requestNVCollection.size());
	}
	
	/**
    	 * Returns all values for a parameter name. This method is intended to be used
         * when the caller expects the parameter to hold more than one value.
     	 * @param   strName     parameter name
	 * @return	String array of parameter values; null if parameter not found.
	 */
	public String[] getAttributeValue(String strName)
	{
		// walk through all request parameters
		for(int iIndex=0; iIndex<requestNVCollection.size();iIndex++)
		{
			RequestAttribute reqParm = (RequestAttribute)requestNVCollection.elementAt(iIndex);
			if (reqParm.getAttributeName().equals(strName))
			{
				// found requested parameter
				return reqParm.getAttributeValues();
			}
		}
		
		// The specified name can not be found in request parameters
		return null;
	}
	
	
	/**
     	 * Returns the value for a parameter name. This method is intended to be used
     	 * when the caller expects the parameter to hold a single value rather than
      	 * an array of values.
     	 * @param   strName     parameter name
	 * @return	parameter value; null if parameter not found.
     	 */ 
	public String getParameter(String strName)
	{
		String strValue = null;
		String [] strValues = this.getAttributeValue(strName);
		if (strValues != null)
		{
			strValue = strValues[0];
		}
		
		return strValue;
	}

	/**
     	 * Determines if the module was invoked via a GET or a POST request. In most cases
      	 * a module invoked via a GET request is supposed to display a form allowing the user
     	 * to select an action. A module invoked via a POST request was probably invoked
     	 * because the user made a selecton on the form and clicked a button, in which case
     	 * the module is expected to process the user's input.
     	 * @return  true if request method was GET; false if request method was POST.
     	 */ 
	public boolean doDisplay()
	{
		return bIsGet;
	}
	
	/**
	 * This method is invoked by AlltelServlet for purpose of repopulating private member
	 * requestNVCollection after a session activity has exceeded a predefined time out at which
	 * time we must redirect to a destination page not currently in memory.
	 * UNUSED IN LSR
	 */
	public void setRequestParameters(boolean bPostFlag)
	{
	    if (bPostFlag){
	        bIsGet = false;
	    };
	    
	    HttpSession Session = this.getSession();
	    Vector PostDataCollection = (Vector)Session.getAttribute("PostDataCollection");
        
       	    for (int iIndex=0; iIndex<PostDataCollection.size();iIndex++)
            {
           	 RequestAttribute reqParm = (RequestAttribute)PostDataCollection.elementAt(iIndex);
            	String strReqParmName = reqParm.getAttributeName();
            	String strReqParmValues[] = reqParm.getAttributeValues();
            
            	//Put into private Requested data vector
            	if (strReqParmValues != null)
           	{
              		RequestAttribute ReqParms = new RequestAttribute(strReqParmName, strReqParmValues);
			requestNVCollection.addElement(ReqParms);
		}
       	    };
    	}
    
    	/**
	 * This method is invoked by AlltelServlet for purpose of populating into memory all elements of
	 *  requestNVCollection after a session activity has exceeded a predefined time.
	 *  UNUSED in LSR.
	 */
	public void getRequestParameters()
	{
	    	HttpSession Session = this.getSession();
            	Vector PostDataCollection = new Vector();
	    	Enumeration Enum = request.getParameterNames();
	            
	    	while(Enum.hasMoreElements())
	    	{ 
	       	// GET parameter name
		   String strReqName = (String)Enum.nextElement();
		   // Get parameter values
		   String strReqValues[] = request.getParameterValues(strReqName);
		   if (strReqValues != null)
		   {
			 RequestAttribute ReqAttr = new RequestAttribute(strReqName, strReqValues);
			 PostDataCollection.addElement(ReqAttr);
		   }
	    	};
       		Session.setAttribute("PostDataCollection", PostDataCollection);
    	}
    
	/**
     	 * Returns a reference to to the persistent session.
     	 * @return  reference to persistent weblogic session.
     	 */ 
	public HttpSession getSession()
	{
		return request.getSession(true);
	}
	
	
	/**
     	 * Returns a copy of the persistent data manager stored in the session.
     	 * @return  SessionDataManager	the session data manager object stored in session.
     	 */ 
	public SessionDataManager getSessionDataManager()
	{
		HttpSession session = this.getSession();
		SessionDataManager sdm = (SessionDataManager)session.getAttribute(this.SDM_NAME);
		if (sdm == null)
		{
        		sdm = new SessionDataManager();
			session.setAttribute(this.SDM_NAME, sdm);
		}
		return sdm;
	}
	
	
	/**
     	 * Saves the persistent data manager back to the weblogic session.
     	 * @param   sdm     persistent data manager
     	 * @return  true if successful; false if unsuccessful.
     	 */ 
	public boolean putSessionDataManager(SessionDataManager sdm)
	{
		boolean bStatus = false;
		// put session data manager back to session
		HttpSession session = this.getSession();
        	if (sdm != null)
		{
			session.setAttribute(this.SDM_NAME, sdm);
			bStatus = true;
        	}
		
		return bStatus;
	}
	
		
	/**
     	 * Determines if a custom image button or standard HTML button on a form
     	 * was clicked. The button on the form must have a NAME parameter as it is 
     	 * this parameter that is passed as a parameter to this method.
     	 * @param   strButtonName    button name on HTML form.
     	 * @return  true if button was clicked; false if button was not clicked.
     	 */ 
	public boolean buttonClicked(String strButtonName)
	{
		// for image buttons the actual X or Y coordinate of the mouseclick
		// within the button is what is passed as a parameter value
        	if (request.getParameter(strButtonName + ".y") != null) 
		{
            		// image button was clicked
            		return true;
        	}
	        // for standard HTML buttons the VALUE tag for the button is what is
       		// passed as a parameter value
	        if (request.getParameter(strButtonName) != null) 
		{
       		    	// standard button was clicked
            		return true;
        	}
       		// button was not clicked
	        return false;
	}

	/**
	 * public method to get input stream
	 * @return	ServletInputStream	the input stream
	 * @exception	IOException
	 */
	public ServletInputStream getInputStream() throws IOException 
	{
		return request.getInputStream();
	}
	
	/**
	 * public method to get Cookie from client browser
	 * @return	Cookie[]	a list of Cookies
	 * @exception	Exception
	 */
	public Cookie[] getCookies()
		throws Exception 
	{
	  Cookie[] cookieList  = request.getCookies();
	  return cookieList;
   	}
	
	/**
	 * public method to get remote client IP address
	 * @return	String	the remote client IP address
	 * @exception	Exception
	 */
	public String getRemoteAddr() 
		throws Exception 
	{
	    String strIPAddress = request.getRemoteAddr();	
		return strIPAddress;
	}
	
	public String getQueryString()
	{
		String strQueryString = null;
		strQueryString = request.getQueryString();
		return strQueryString;
	}
	
	public String getSchemeHostPort()
	{
	  StringBuffer strBufURL = null;
	  String strURL = null;
	  String strQuery = null;
	  
	  String strScheme = null;
	  String strHost = null;
	  String strPort = null;
	  
	  strScheme = request.getScheme();
	  strHost = request.getServerName();
	  strPort = ""+request.getServerPort();
	  
	  String strTmp = strScheme + "://" + strHost;
	  if (strPort != null)
	  {
		  strTmp = strTmp + ":" + strPort;
	  }
	  return strTmp;
	}
	
	/**
	 * public method to get the part of the URL that comes after the host
         * and port but before any form data. The leading '/' is removed.
	 * @return	String	The requested jsp/servlet/script from URL
	 */
	public String getURL()
	{
	  return request.getServletPath();
	}

	public String getURLNoBackSlash()
	{
	  String strTmp = getURL();
          return strTmp.substring( strTmp.lastIndexOf("/") + 1 );
 	}

        public HttpServletRequest getHttpRequest()
	{
		return this.request;	
	}
        public Vector getParameterNames()
        {
		return this.requestNVCollection;
        }

} //end of AlltelRequest()

