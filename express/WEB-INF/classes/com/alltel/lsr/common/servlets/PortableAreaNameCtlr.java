package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class PortableAreaNameCtlr extends AlltelServlet {
    public void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        String strURL = "/PortableAreaNameView.jsp";
        
        Log.write(Log.DEBUG_VERBOSE, "PortableAreaNameCtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
        
        // Instantiate the UserGroup Bean
        PortableAreaNameBean beanPortableAreaName = new PortableAreaNameBean();
        request.getHttpRequest().setAttribute("portableAreaNameBean1", beanPortableAreaName);
        
//        if ((true)) {
//            strURL = "/LSRBaseController";
//            alltelRequestDispatcher.forward(strURL);
//            return;
//        }
        
        
        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        Log.write(Log.DEBUG, "PortableAreaNameCtlr() strDbAction = " + strDbAction);
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        String StrPortableAreaNameSeqNo = request.getParameter("PORTABLE_AREA_NAME_SQNC_NMBR");
        if ((StrPortableAreaNameSeqNo == null) || (StrPortableAreaNameSeqNo.length() == 0)) {
            // Handle the error
        }
        
        String StrPortableName = request.getParameter("PORTABLE_AREA_NAME");
        if ((StrPortableName == null) || (StrPortableName.length() == 0)) {
            // Handle the error
        }
        
//        String StrAsocDesc = request.getParameter("ASOC_DESCRIPTION");
//        if ((StrAsocDesc == null) || (StrAsocDesc.length() == 0)) {
//            // Handle the error
//        }
        
        String strMdfdUserId = request.getParameter("MDFD_USERID");
        if ((strMdfdUserId == null) || (strMdfdUserId.length() == 0)) {
            // Handle the error
        }
        
        String strMdfdDt = request.getParameter("MDFD_DT");
        if ((strMdfdDt == null) || (strMdfdDt.length() == 0)) {
            // Handle the error
        }
        
        //User has authority to Controller to get here, now see if they have authority to function
        if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanPortableAreaName.getTblAdmnScrtyTgView())) ||
                (strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanPortableAreaName.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("new") 	 && !sdm.isAuthorized(beanPortableAreaName.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanPortableAreaName.getTblAdmnScrtyTgMod())) ||
                (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanPortableAreaName.getTblAdmnScrtyTgDel()))   ) {
            Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
            alltelRequestDispatcher.forward(SecurityBreachURL);
            return;
        }
        
        // Populate the Bean
        beanPortableAreaName.setStrPortableAreaNameConfigSeqNo(StrPortableAreaNameSeqNo);
        beanPortableAreaName.setStrPortableAreaName(StrPortableName);
        beanPortableAreaName.setMdfdDt(strMdfdDt);
        beanPortableAreaName.setMdfdUserid(sdm.getUser());
        beanPortableAreaName.setDbAction(strDbAction);
        
        // Validate the Bean
        if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow")) {
            if (!beanPortableAreaName.validatePortableAreaNameBean()) {
                // Send error msg back to view
                strURL= "/PortableAreaNameView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
        }
        
        // Perform requested action
        if (strDbAction.equals("InsertRow")) {
            // Store to DB
            if (beanPortableAreaName.savePortableAreaNameBeanToDB() != 0) {
                strURL= "/PortableAreaNameView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=27";
            }
        } else if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
            if (!beanPortableAreaName.validateMdfdDt()) {
                // Send error msg back to view
                strURL= "/PortableAreaNameView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
            
            // Update DB
            if (beanPortableAreaName.updatePortableAreaNameBeanToDB() != 0) {
                strURL= "/PortableAreaNameView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=27";
            }
        } else if (strDbAction.equals("get")) {
            // Retrieve from DB
            if (beanPortableAreaName.retrievePortableAreaNameBeanFromDB() != 0) {
                strURL= "/PortableAreaNameView.jsp?action=" + strDbAction;
            }
        } else if (strDbAction.equals("DeleteRow")) {
            // Delete from DB
            if (beanPortableAreaName.deletePortableAreaNameBeanFromDB() != 0) {
                strURL= "/PortableAreaNameView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=27";
            }
        } else if (strDbAction.equals("new")) {
            // Initialize the Bean
            beanPortableAreaName.setStrPortableAreaNameConfigSeqNo("");
            beanPortableAreaName.setStrPortableAreaName("");
            beanPortableAreaName.setMdfdDt("");
            beanPortableAreaName.setMdfdUserid("");
        } else if (strDbAction.equals("Cancel")) {
            strURL = "/TableAdminCtlr?tblnmbr=27";
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
