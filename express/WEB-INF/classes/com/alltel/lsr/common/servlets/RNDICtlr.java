

package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class RNDICtlr extends AlltelServlet {
    
    protected void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        String strURL = "/RNDIView.jsp";
        System.out.println("RNDICtlr == kk");
        Log.write(Log.DEBUG_VERBOSE, "RNDICtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
        
        // Instantiate the Company Bean
        RNDIBean rndibean = new RNDIBean();
        request.getHttpRequest().setAttribute("rndibean", rndibean);
        
        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        Log.write(Log.DEBUG, "RNDICtlr() strDbAction = " + strDbAction);
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            // Handle the error
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        String strCmpnySqncNmbr = request.getParameter("id");
        if ((strCmpnySqncNmbr == null) || (strCmpnySqncNmbr.length() == 0)) {
            // Handle the error
        }
        
//        String strCmpnyTyp = request.getParameter("CMPNY_TYP");
//        if ((strCmpnyTyp == null) || (strCmpnyTyp.length() == 0)) {
//            // Handle the error
//        }
        
        String strCmpnyNm = request.getParameter("name");
        if ((strCmpnyNm == null) || (strCmpnyNm.length() == 0)) {
            // Handle the error
        }
        
//        String strTargusUserid = request.getParameter("TARGUS_USERID");
//        if ((strTargusUserid == null) || (strTargusUserid.length() == 0)) {
//            // Handle the error
//        }
//
//        String strTargusPsswrd = request.getParameter("TARGUS_PSSWRD");
//        if ((strTargusPsswrd == null) || (strTargusPsswrd.length() == 0)) {
//            // Handle the error
//        }
//
//        String strMdfdDt = request.getParameter("MDFD_DT");
//        if ((strMdfdDt == null) || (strMdfdDt.length() == 0)) {
//            // Handle the error
//        }
        
        //User has authority to Controller to get here, now see if they have authority to function
        System.out.println("RNDICtlr == if==");
        Log.write("=RNDICtlr== strDbAction "+strDbAction);
        if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(rndibean.getTblAdmnScrtyTgView())) ||
                (strDbAction.equals("InsertRow") && !sdm.isAuthorized(rndibean.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("new") 	 && !sdm.isAuthorized(rndibean.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(rndibean.getTblAdmnScrtyTgMod())) ||
                (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(rndibean.getTblAdmnScrtyTgDel()))   ) {
            Log.write("=RNDICtlr== strDbAction inside if "+strDbAction);
            Log.write(Log.WARNING, "RNDICtlr() user " + sdm.getUser() + " is trying to bypass security!");
            alltelRequestDispatcher.forward(SecurityBreachURL);
            return;
        }
        
        // Populate the Bean
        rndibean.setCmpnySqncNmbr(strCmpnySqncNmbr);
//        rndibean.setCmpnyTyp(strCmpnyTyp);
        rndibean.setName(strCmpnyNm);
//        rndibean.setTargusUserid(strTargusUserid);
//        rndibean.setTargusPsswrd(strTargusPsswrd);
        //  rndibean.setMdfdDt(strMdfdDt);
        rndibean.setMdfdUserid(sdm.getUser());
        rndibean.setDbAction(strDbAction);
        
        // Validate the Bean
        if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow")) {
            if (!rndibean.validateRNDIBean()) {
                // Send error msg back to view
                strURL= "/RNDIView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
        }
        
        // Perform requested action
        if (strDbAction.equals("InsertRow")) {
            // Store to DB
            if (rndibean.saveRNDIBeanToDB() != 0) {
                strURL= "/RNDIView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=1";
            }
        } else if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
            if (!rndibean.validateMdfdDt()) {
                // Send error msg back to view
                strURL= "/RNDIView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
            
            // Update DB
            if (rndibean.updateRNDIBeanToDB() != 0) {
                strURL= "/RNDIView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=1";
            }
        } else if (strDbAction.equals("get")) {
            // Retrieve from DB
            if (rndibean.retrieveRNDIBeanFromDB() != 0) {
                strURL= "/RNDIView.jsp?action=" + strDbAction;
            }
        } else if (strDbAction.equals("DeleteRow")) {
            // Delete from DB
            rndibean.deleteRNDIBeanFromDB();
            if (rndibean.deleteRNDIBeanFromDB() != 0) {
                strURL= "/RNDIView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=1";
            }
        } else if (strDbAction.equals("new")) {
            // Initialize the Bean
            rndibean.setCmpnySqncNmbr("");
//            rndibean.setCmpnyTyp("");
//            rndibean.setCmpnyNm("");
//            rndibean.setTargusUserid("");
//            rndibean.setTargusPsswrd("");
            rndibean.setMdfdDt("");
            rndibean.setMdfdUserid("");
        } else if (strDbAction.equals("Cancel")) {
            strURL = "/TableAdminCtlr?tblnmbr=1";
        } else {
            strURL = "/NavigationErrorView.jsp";
        }
        
        Log.write(Log.DEBUG, "RNDICtlr() strURL = " + strURL );
        alltelRequestDispatcher.forward(strURL);
        return;
    }
    
    protected void populateVariables()
    throws Exception {
    }
}
