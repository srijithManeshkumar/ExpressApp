package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class AsocTypeCtlr extends AlltelServlet {
    public void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        String strURL = "/AsocTypeView.jsp";
        
        Log.write(Log.DEBUG_VERBOSE, "AsocTypeCtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
        
        // Instantiate the UserGroup Bean
        AsocTypeBean beanAsocType = new AsocTypeBean();
        request.getHttpRequest().setAttribute("asocTypeBean1", beanAsocType);
        
        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        Log.write(Log.DEBUG, "AsocTypeCtlr() strDbAction = " + strDbAction);
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        String StrAsocSqncNo = request.getParameter("ASOC_TYPE_CONFIG_SQNC_NMBR");
        if ((StrAsocSqncNo == null) || (StrAsocSqncNo.length() == 0)) {
            // Handle the error
        }
        
        String StrAsocType = request.getParameter("ASOC_TYPE");
        if ((StrAsocType == null) || (StrAsocType.length() == 0)) {
            // Handle the error
        }
        
        String StrAsocDesc = request.getParameter("ASOC_DESCRIPTION");
        if ((StrAsocDesc == null) || (StrAsocDesc.length() == 0)) {
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
        if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgView())) ||
                (strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("new") 	 && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgMod())) ||
                (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgDel()))   ) {
            Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
            alltelRequestDispatcher.forward(SecurityBreachURL);
            return;
        }
        
        // Populate the Bean
        beanAsocType.setStrAsocTypeConfigSeqNo(StrAsocSqncNo);
        beanAsocType.setStrAsocDescrption(StrAsocDesc);
        beanAsocType.setStrAsocType(StrAsocType);
        beanAsocType.setMdfdDt(strMdfdDt);
        beanAsocType.setMdfdUserid(sdm.getUser());
        beanAsocType.setDbAction(strDbAction);
        
        // Validate the Bean
        if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow")) {
            if (!beanAsocType.validateAsocTypeBean()) {
                // Send error msg back to view
                strURL= "/AsocTypeView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
        }
        
        // Perform requested action
        if (strDbAction.equals("InsertRow")) {
            // Store to DB
            if (beanAsocType.saveAsocTypeBeanToDB() != 0) {
                strURL= "/AsocTypeView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=26";
            }
        } else if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
            if (!beanAsocType.validateMdfdDt()) {
                // Send error msg back to view
                strURL= "/AsocTypeView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
            
            // Update DB
            if (beanAsocType.updateAsocTypeBeanToDB() != 0) {
                strURL= "/AsocTypeView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=26";
            }
        } else if (strDbAction.equals("get")) {
            // Retrieve from DB
            if (beanAsocType.retrieveAsocTypeBeanFromDB() != 0) {
                strURL= "/AsocTypeView.jsp?action=" + strDbAction;
            }
        } else if (strDbAction.equals("DeleteRow")) {
            // Delete from DB
            if (beanAsocType.deleteAsocTypeBeanFromDB() != 0) {
                strURL= "/AsocTypeView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=26";
            }
        } else if (strDbAction.equals("new")) {
            // Initialize the Bean
            beanAsocType.setStrAsocTypeConfigSeqNo("");
            beanAsocType.setStrAsocType("");
            beanAsocType.setStrAsocDescrption("");
            beanAsocType.setMdfdDt("");
            beanAsocType.setMdfdUserid("");
        } else if (strDbAction.equals("Cancel")) {
            strURL = "/TableAdminCtlr?tblnmbr=26";
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
