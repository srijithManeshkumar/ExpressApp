package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class VendorAsocCtlr extends AlltelServlet {
    public void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        String strURL = "/VendorAsocView.jsp";
        
        Log.write(Log.DEBUG_VERBOSE, "VendorAsocCtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
        
        // Instantiate the UserGroup Bean
        VendorAsocBean beanVendorAsoc = new VendorAsocBean();
        request.getHttpRequest().setAttribute("vendorAsocBean", beanVendorAsoc);
        
        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        String action1=request.getParameter("action1");
        if(action1!=null && action1.trim().length()>0){
            if(strDbAction==null)
                strDbAction=action1;
        }
        Log.write(Log.DEBUG, "VendorAsocCtlr() strDbAction = " + strDbAction);
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        
        String strVendorConSqnc = request.getParameter("VENDOR_CONFIG_SQNC_NMBR");
        if ((strVendorConSqnc == null) || (strVendorConSqnc.length() == 0)) {
            // Handle the error
        }
        
        String strVendorAsocSqnc = request.getParameter("VENDOR_ASOC_CONFIG_SQNC_NMBR");
        
        if ((strVendorAsocSqnc == null) || (strVendorAsocSqnc.length() == 0)) {
            // Handle the error
        }
        
        String strAsocType = request.getParameter("ASOC_TYPE_CONFIG_SQNC_NMBR");
        if ((strAsocType == null) || (strAsocType.length() == 0)) {
            // Handle the error
        }
        String strAsocCD = request.getParameter("ASOC_CD");
        if ((strAsocCD == null) || (strAsocCD.length() == 0)) {
            // Handle the error
        }
//        String strAsocFee = request.getParameter("ASOC_FEE_APPLIES");
//        if ((strAsocFee == null) || (strAsocFee.length() == 0)) {
//            // Handle the error
//        }
        String strHowAsocFee = request.getParameter("HOW_ASOC_FEE_APPLIES");
        if ((strHowAsocFee == null) || (strHowAsocFee.length() == 0)) {
            // Handle the error
        }
        String strAsocFeeRate= request.getParameter("ASOC_FEE_RATE");
        if ((strAsocFeeRate == null) || (strAsocFeeRate.length() == 0)) {
            // Handle the error
        }
        
        String fldInd= request.getParameter("BUS_RES_IND");
        if ((fldInd == null) || (fldInd.length() == 0)) {
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
        if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanVendorAsoc.getTblAdmnScrtyTgView())) ||
                (strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanVendorAsoc.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("new") 	 && !sdm.isAuthorized(beanVendorAsoc.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanVendorAsoc.getTblAdmnScrtyTgMod())) ||
                (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanVendorAsoc.getTblAdmnScrtyTgDel()))   ) {
            Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
            alltelRequestDispatcher.forward(SecurityBreachURL);
            return;
        }
        
        // Populate the Bean
        beanVendorAsoc.setStrAsocTypeCode(strAsocCD);
        beanVendorAsoc.setStrVendorAsocConfigSeqNo(strVendorAsocSqnc);
        beanVendorAsoc.setStrAsocTypeConfigSeqNo(strAsocType);
//        beanVendorAsoc.setDoesAsocFeeApply(strAsocFee);
        beanVendorAsoc.setStrAsocFeeRate(strAsocFeeRate);
        beanVendorAsoc.setStrHowAsocFeeApplies(strHowAsocFee);
        beanVendorAsoc.setStrVendorConfigSqncNumber(strVendorConSqnc);
        beanVendorAsoc.setFiledInd(fldInd);
        beanVendorAsoc.setMdfdDt(strMdfdDt);
        beanVendorAsoc.setMdfdUserid(sdm.getUser());
        beanVendorAsoc.setDbAction(strDbAction);
        
        // Validate the Bean
        if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow")) {
            if (!beanVendorAsoc.validateVendorAsocBean()) {
                // Send error msg back to view
                strURL= "/VendorAsocView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
        }
        
        // Perform requested action
        if (strDbAction.equals("InsertRow")) {
            // Store to DB
            if (beanVendorAsoc.saveVendorAsocBeanToDB() != 0) {
                strURL= "/VendorAsocView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=30&rstrctsrch=yes&srchctgry=0&srchvl=" + strVendorConSqnc;
                
            }
        } else if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
            if (!beanVendorAsoc.validateMdfdDt()) {
                // Send error msg back to view
                strURL= "/VendorAsocView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
            
            // Update DB
            if (beanVendorAsoc.updateVendorAsocBeanToDB() != 0) {
                strURL= "/VendorAsocView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=30&rstrctsrch=yes&srchctgry=0&srchvl=" + strVendorConSqnc;
            }
        } else if (strDbAction.equals("get")) {
            // Retrieve from DB
            if (beanVendorAsoc.retrieveVendorAsocBeanFromDB() != 0) {
                strURL= "/VendorAsocView.jsp?action=" + strDbAction;
            }
        } else if (strDbAction.equals("DeleteRow")) {
            // Delete from DB
            if (beanVendorAsoc.deleteVendorAsocBeanFromDB() != 0) {
                strURL= "/VendorAsocView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=30&rstrctsrch=yes&srchctgry=0&srchvl=" + strVendorConSqnc;
            }
        } else if (strDbAction.equals("new")) {
            // Initialize the Bean
            beanVendorAsoc.setStrAsocTypeCode("");
//            beanVendorAsoc.setDoesAsocFeeApply("");
            beanVendorAsoc.setStrAsocFeeRate("");
            beanVendorAsoc.setStrAsocTypeConfigSeqNo("");
            beanVendorAsoc.setStrHowAsocFeeApplies("");
            beanVendorAsoc.setStrVendorConfigSqncNumber("");
            beanVendorAsoc.setFiledInd("");
            beanVendorAsoc.setMdfdDt("");
            beanVendorAsoc.setMdfdUserid("");
        } else if (strDbAction.equals("Cancel")) {
            strURL = "/TableAdminCtlr?tblnmbr=30&rstrctsrch=yes&srchctgry=0&srchvl=" + strVendorConSqnc;
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
