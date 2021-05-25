package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class CHCtlr extends AlltelServlet {
    public void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        String strURL = "/CHView.jsp";
        
        Log.write(Log.DEBUG_VERBOSE, "CHCtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
        
        // Instantiate the CHBean Bean
        CHBean beanCH = new CHBean();
        request.getHttpRequest().setAttribute("beanCH1", beanCH);
        
        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        Log.write(Log.DEBUG, "CHCtlr() strDbAction = " + strDbAction);
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        String strUsrGrpAssgnmntSqncNmbr = request.getParameter("sequence_nbr");
        if ((strUsrGrpAssgnmntSqncNmbr == null) || (strUsrGrpAssgnmntSqncNmbr.length() == 0)) {
            // Handle the error
        }
        
        String strID = request.getParameter("ID");
        if ((strID == null) || (strID.length() == 0)) {
            // Handle the error
        }
        
        String strName1 = request.getParameter("NAME1");
        if ((strName1 == null) || (strName1.length() == 0)) {
            // Handle the error
        }
        
        String strName2 = request.getParameter("NAME2");
        if ((strName2 == null) || (strName2.length() == 0)) {
            // Handle the error
        }
        
        //User has authority to Controller to get here, now see if they have authority to function
        
        if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanCH.getTblAdmnScrtyTgView())) ||
                (strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanCH.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("new") 	 && !sdm.isAuthorized(beanCH.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanCH.getTblAdmnScrtyTgMod())) ||
                (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanCH.getTblAdmnScrtyTgDel()))   ) {
            Log.write(Log.WARNING, "CHCtlr() user " + sdm.getUser() + " is trying to bypass security!");
            alltelRequestDispatcher.forward(SecurityBreachURL);
            return;
        }
        
        // Populate the Bean
        beanCH.setUsrGrpAssgnmntSqncNmbr(strUsrGrpAssgnmntSqncNmbr);
        beanCH.setID(strID);
        beanCH.setName1(strName1);
        beanCH.setName2(strName2);
        beanCH.setMdfdUserid(sdm.getUser());
        beanCH.setDbAction(strDbAction);
        
        // Validate the Bean
        if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow")) {
            if (!beanCH.validateCHBean()) {
                // Send error msg back to view
                strURL= "/CHView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
        }
        
        // Perform requested action
        if (strDbAction.equals("InsertRow")) {
            // Store to DB
            if (beanCH.saveCHBeanToDB() != 0) {
                strURL= "/CHView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=2124&rstrctsrch=yes&srchctgry=0&srchvl=" + strID;
            }
        } else if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
            if (!beanCH.validateMdfdDt()) {
                // Send error msg back to view
                strURL= "/CHView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
            
            // Update DB
            if (beanCH.updateCHBeanToDB() != 0) {
                strURL= "/CHView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=2124&rstrctsrch=yes&srchctgry=0&srchvl=" + strID;
            }
        } else if (strDbAction.equals("get")) {
            // Retrieve from DB
            if (beanCH.retrieveCHBeanFromDB() != 0) {
                strURL= "/CHView.jsp?action=" + strDbAction;
            }
        } else if (strDbAction.equals("DeleteRow")) {
            // Delete from DB
            if (beanCH.deleteCHBeanFromDB() != 0) {
                strURL= "/CHView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=2124&rstrctsrch=yes&srchctgry=0&srchvl=" + strID;
            }
        } else if (strDbAction.equals("new")) {
            // Initialize the Bean
            beanCH.setUsrGrpAssgnmntSqncNmbr("");
            beanCH.setID("");
            beanCH.setName1("");
            beanCH.setName2("");
            beanCH.setMdfdDt("");
            beanCH.setMdfdUserid("");
        } else if (strDbAction.equals("Cancel")) {
            strURL = "/TableAdminCtlr?tblnmbr=2124&rstrctsrch=yes&srchctgry=0&srchvl=" + strID;
        } else {
            strURL = "/NavigationErrorView.jsp";
        }
        
        // Pass the request and response to the JSP
        Log.write(Log.DEBUG, "CHCtlr() strURL = " + strURL );
        alltelRequestDispatcher.forward(strURL);
        return;
    }
    
    protected void populateVariables()
    throws Exception {
    }
}
