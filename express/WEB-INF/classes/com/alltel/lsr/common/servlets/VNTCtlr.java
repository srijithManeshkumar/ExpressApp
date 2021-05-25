package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class VNTCtlr extends AlltelServlet {
    public void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        String strURL = "/VNTView.jsp";
        String strEnc = null;
        
        Log.write(Log.DEBUG_VERBOSE, "VNTCtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
        
        // Instantiate the VNTBean Bean
        VNTBean beanVNT = new VNTBean();
        request.getHttpRequest().setAttribute("beanVNT1", beanVNT);
        
        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        Log.write(Log.DEBUG, "VNTCtlr() strDbAction = " + strDbAction);
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        String strID = request.getParameter("ID");
        if ((strID == null) || (strID.length() == 0)) {
            // Handle the error
        }
        
        String strName = request.getParameter("NAME");
        if ((strName == null) || (strName.length() == 0)) {
            // Handle the error
        }
        
        String strAge = request.getParameter("AGE");
        if ((strAge == null) || (strAge.length() == 0)) {
            // Handle the error
        }
        
        String strCmpnySqncNmbr = request.getParameter("CMPNY_SQNC_NMBR");
        if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0)) {
            // Handle the error
        }
        
        
        //User has authority to Controller to get here, now see if they have authority to function
        if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanVNT.getTblAdmnScrtyTgView())) ||
                (strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanVNT.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("new") 	 && !sdm.isAuthorized(beanVNT.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanVNT.getTblAdmnScrtyTgMod())) ||
                (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanVNT.getTblAdmnScrtyTgDel()))   ) {
            Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
            alltelRequestDispatcher.forward(SecurityBreachURL);
            return;
        }
        
        // Populate the Bean
        beanVNT.setID(strID);
        beanVNT.setName(strName);
        beanVNT.setAge(strAge);
        beanVNT.setCmpnySqncNmbr(strCmpnySqncNmbr);
        beanVNT.setDbAction(strDbAction);
        
        
        // Validate the Bean
        if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow")) {
            if (!beanVNT.validateVNTBean()) {
                // Send error msg back to view
                strURL= "/VNTView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
        }
        
        // Perform requested action
        if (strDbAction.equals("InsertRow")) {
            // Store to DB
            if (beanVNT.saveVNTBeanToDB() != 0) {
                strURL= "/VNTView.jsp?action=" + strDbAction;
            } else {
                // Save the User ID to the session for later user
                HttpSession session = request.getSession();
                session.setAttribute("UserID_userid", strID);
                
                strURL = "/TableAdminCtlr?tblnmbr=2123&rstrctsrch=yes&srchctgry=0&srchvl=" + strID;
            }
        } else if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
            if (!beanVNT.validateMdfdDt()) {
                // Send error msg back to view
                strURL= "/VNTView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
            
            // Update DB
            if (beanVNT.updateVNTBeanToDB() != 0) {
                strURL= "/VNTView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=2123";
            }
        } else if (strDbAction.equals("get")) {
            // Save the User ID to the session for later user
            HttpSession session = request.getSession();
            session.setAttribute("UserID_userid", strID);
            
            // Retrieve from DB
            if (beanVNT.retrieveVNTBeanFromDB() != 0) {
                strURL= "/VNTView.jsp?action=" + strDbAction;
            }
        } else if (strDbAction.equals("DeleteRow")) {
            // Delete from DB
            if (beanVNT.deleteVNTBeanFromDB() != 0) {
                strURL= "/VNTView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=2123";
            }
        } else if (strDbAction.equals("new")) {
            // Initialize the Bean
            beanVNT.setID("");
            beanVNT.setName("");
            beanVNT.setAge("");
            beanVNT.setCmpnySqncNmbr("");
            beanVNT.setMdfdUserid("");
            beanVNT.setLgnAttmpts(0);
        } else if (strDbAction.equals("Cancel")) {
            strURL = "/TableAdminCtlr?tblnmbr=2123";
        } else {
            strURL = "/NavigationErrorView.jsp";
        }
        
        // Pass the request and response to the JSP
        Log.write(Log.DEBUG, "VNTCtlr() strURL = " + strURL );
        alltelRequestDispatcher.forward(strURL);
        return;
    }
    
    protected void populateVariables()
    throws Exception {
    }
}


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SERVLET/VNTCtlr.java  $
/*
/*   Rev 1.0.1.0   25 Feb 2002 10:28:24   sedlak
/*
/*
/*   Rev 1.0   23 Jan 2002 11:07:00   wwoods
/*Initial Checkin
 */

/* $Revision:   1.0.1.0  $
 */
