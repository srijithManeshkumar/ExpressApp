package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class PortableAreaCtlr extends AlltelServlet {
    public void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        String strURL = "/PortableAreaView.jsp";
        
        Log.write(Log.DEBUG_VERBOSE, "PortableAreaCtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
        
        // Instantiate the UserGroup Bean
        PortableAreaBean beanPortableArea = new PortableAreaBean();
        request.getHttpRequest().setAttribute("portableAreaBean", beanPortableArea);
        
        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        Log.write(Log.DEBUG, "PortableAreaCtlr() strDbAction = " + strDbAction);
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        String strPortableAreaSqncNo = request.getParameter("PORTABLE_AREA_NAME_SQNC_NMBR");
        if ((strPortableAreaSqncNo == null) || (strPortableAreaSqncNo.length() == 0)) {
            // Handle the error
        }
        
        String strVendorConSqnc = request.getParameter("VENDOR_CONFIG_SQNC_NMBR");
        if ((strVendorConSqnc == null) || (strVendorConSqnc.length() == 0)) {
            // Handle the error
        }
        
        String strPortableAreaSqnc = request.getParameter("PORTABLE_AREA_SQNC_NO");
        if ((strPortableAreaSqnc == null) || (strPortableAreaSqnc.length() == 0)) {
            // Handle the error
        }
        
        String strMdfdUserId = request.getParameter("MDFD_USERID");
        if ((strMdfdUserId == null) || (strMdfdUserId.length() == 0)) {
            // Handle the error
        }
        
        String strMdfdDt = request.getParameter("MDFD_DT");
        if ((strMdfdDt == null) || (strMdfdDt.length() == 0)) {
            // Handle the error
        }
        
        //User has authority to Controller to get here, now see if they have authority to function
        if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanPortableArea.getTblAdmnScrtyTgView())) ||
                (strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanPortableArea.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("new") 	 && !sdm.isAuthorized(beanPortableArea.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanPortableArea.getTblAdmnScrtyTgMod())) ||
                (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanPortableArea.getTblAdmnScrtyTgDel()))   ) {
            Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
            alltelRequestDispatcher.forward(SecurityBreachURL);
            return;
        }
        
        // Populate the Bean
        beanPortableArea.setStrPortableAreaNameSqncNo(strPortableAreaSqncNo);
        beanPortableArea.setStrPortableAreaSqncNumber(strPortableAreaSqnc);
        beanPortableArea.setStrVendorConfigSqncNumber(strVendorConSqnc);
        beanPortableArea.setMdfdDt(strMdfdDt);
        beanPortableArea.setMdfdUserid(sdm.getUser());
        beanPortableArea.setDbAction(strDbAction);
        
        // Validate the Bean
        if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow")) {
            if (!beanPortableArea.validatePortableAreaBean()) {
                // Send error msg back to view
                strURL= "/PortableAreaView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
        }
        
        // Perform requested action
        if (strDbAction.equals("InsertRow")) {
            // Store to DB
            if (beanPortableArea.savePortableAreaBeanToDB() != 0) {
                strURL= "/PortableAreaView.jsp?action=" + strDbAction;
            } else {
              strURL = "/TableAdminCtlr?tblnmbr=29&rstrctsrch=yes&srchctgry=0&srchvl=" + strVendorConSqnc;
            }
        } else if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
            if (!beanPortableArea.validateMdfdDt()) {
                // Send error msg back to view
                strURL= "/PortableAreaView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
            
            // Update DB
            if (beanPortableArea.updatePortableAreaBeanToDB() != 0) {
                strURL= "/PortableAreaView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=29&rstrctsrch=yes&srchctgry=0&srchvl=" + strVendorConSqnc;
            }
        } else if (strDbAction.equals("get")) {
            // Retrieve from DB
            if (beanPortableArea.retrievePortableAreaBeanFromDB() != 0) {
                strURL= "/PortableAreaView.jsp?action=" + strDbAction;
            }
        } else if (strDbAction.equals("DeleteRow")) {
            // Delete from DB
            if (beanPortableArea.deletePortableAreaBeanFromDB() != 0) {
                strURL= "/PortableAreaView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=29&rstrctsrch=yes&srchctgry=0&srchvl=" + strVendorConSqnc;
            }
        } else if (strDbAction.equals("new")) {
            // Initialize the Bean
            beanPortableArea.setStrPortableAreaNameSqncNo("");
            beanPortableArea.setStrPortableAreaSqncNumber("");
            beanPortableArea.setStrVendorConfigSqncNumber("");
            beanPortableArea.setMdfdDt("");
            beanPortableArea.setMdfdUserid("");
        } else if (strDbAction.equals("Cancel")) {
            strURL = "/TableAdminCtlr?tblnmbr=29&rstrctsrch=yes&srchctgry=0&srchvl=" + strVendorConSqnc;
        } else {
            strURL = "/NavigationErrorView.jsp";
        }
        
        // Pass the request and response to the JSP
        Log.write(Log.DEBUG, "CompanyCtlr() strURL = " + strURL );
        alltelRequestDispatcher.forward(strURL);
        return;
    }
    
    protected void populateVariables()
    throws Exception {
    }
}
