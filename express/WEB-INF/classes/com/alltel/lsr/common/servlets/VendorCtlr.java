package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class VendorCtlr extends AlltelServlet {
    
    public void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        String strURL = "/VendorView.jsp";
        String strEnc = null;
        
        Log.write(Log.DEBUG_VERBOSE, "VendorCtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
        
        // Instantiate the VendorBean Bean
        VendorBean beanVendor = new VendorBean();
        request.getHttpRequest().setAttribute("beanVendor1", beanVendor);
        
        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        if(strDbAction==null){
            String action1 = request.getParameter("action1");
            if(action1!=null && action1.trim().length()>0){
                strDbAction = action1;
            }
        }
        Log.write(Log.DEBUG, "VendorCtlr() strDbAction = " + strDbAction);
        
        String strVendorConfigSqncNumber = request.getParameter("VENDOR_CONFIG_SQNC_NMBR");
        String strCompSqncNumber=  request.getParameter("CMPNY_SQNC_NMBR");
        String strStateCode =  request.getParameter("STT_CD");
        String strOCN =  request.getParameter("OCN_CD");
        String strCompanyType =  request.getParameter("CMPNY_TYP");
        String strBTN =  request.getParameter("BTN");
        String strWCN =  request.getParameter("WCN");
        String isEmbargoed =  request.getParameter("IS_EMBARGOED");
        String strTXJUR =  request.getParameter("TXJUR");
        String strServiceType=  request.getParameter("SRVC_TYP_CD");
        String strActivityType=  request.getParameter("ACTVTY_TYP_CD");
        String isDirectory=  request.getParameter("IS_DIRECTORY");
        String isEligibleToDeleteDir=  request.getParameter("IS_ELIGIBLE_TO_DIR_DELETE");
        
        String validTimeOfDayDDD = request.getParameter("VALID_TIME_OF_DAY_FOR_DDD");
        String dueDateLowerLimit=  request.getParameter("DDD_INTERVAL_LOWER_LIMIT");
        String dueDateUpperLimit=  request.getParameter("DDD_INTERVAL_UPPER_LIMIT");
        String sLAWaitTime=  request.getParameter("SLA_WAIT_TIME");
        
        String contactNo=  request.getParameter("CONTACTNUMBER");
        String strMdfdDt = request.getParameter("MDFD_DT");
        if ((strMdfdDt == null) || (strMdfdDt.length() == 0)) {
            // Handle the error
        }
        
        
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        String key1 = request.getParameter("key1");
        
        Log.write("key1=="+key1);
        if ((strCompSqncNumber == null) || (strCompSqncNumber.length() == 0)) {
            // Handle the error
        }
        
        if ((strStateCode == null) || (strStateCode.length() == 0)) {
            // Handle the error
        }
        
        if ((strOCN == null) || (strOCN.length() == 0)) {
            // Handle the error
        }
        
        if ((strCompanyType == null) || (strCompanyType.length() == 0)) {
            // Handle the error
        }
        
        if ((strBTN == null) || (strBTN.length() == 0)) {
            // Handle the error
        }
        
        
        if ((strWCN == null) || (strWCN.length() == 0)) {
            // Handle the error
        }
        
        if ((isEmbargoed == null) || (isEmbargoed.length() == 0)) {
            // Handle the error
        }
        
        if ((strTXJUR == null) || (strTXJUR.length() == 0)) {
            // Handle the error
        }
        
        
        if ((strServiceType == null) || (strServiceType.length() == 0)) {
            // Handle the error
        }
        
        if ((strActivityType == null) || (strActivityType.length() == 0)) {
            // Handle the error
        }
        
        //User has authority to Controller to get here, now see if they have authority to function
        if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanVendor.getTblAdmnScrtyTgView())) ||
                (strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanVendor.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("new") 	 && !sdm.isAuthorized(beanVendor.getTblAdmnScrtyTgAdd())) ||
                (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanVendor.getTblAdmnScrtyTgMod())) ||
                (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanVendor.getTblAdmnScrtyTgDel()))   ) {
            Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
            alltelRequestDispatcher.forward(SecurityBreachURL);
            return;
        }
        
        // Populate Settermetods for the Vendor Bean
        
        
        beanVendor.setDbAction(strDbAction);
        beanVendor.setStrVendorConfigSqncNumber(strVendorConfigSqncNumber);
        beanVendor.setStrCompSqncNumber(strCompSqncNumber);
        beanVendor.setStrStateCode(strStateCode);
        beanVendor.setStrOCN(strOCN);
        beanVendor.setStrBTN(strBTN);
        beanVendor.setStrWCN(strWCN);
        beanVendor.setIsEmbargoed(isEmbargoed);
        beanVendor.setStrTXJUR(strTXJUR);
        beanVendor.setStrServiceType(strServiceType);
        beanVendor.setStrActivityType(strActivityType);
        beanVendor.setIsDirectory(isDirectory);
        beanVendor.setIsEligibleToDeleteDir(isEligibleToDeleteDir);
        
        beanVendor.setValidTimeOfDayDDD(validTimeOfDayDDD);
        beanVendor.setDueDateLowerLimit(dueDateLowerLimit);
        beanVendor.setDueDateUpperLimit(dueDateUpperLimit);
        beanVendor.setSLAWaitTime(sLAWaitTime);
        beanVendor.setContactNo(contactNo);
        beanVendor.setMdfdDt(strMdfdDt);
        beanVendor.setMdfdUserid(sdm.getUser());
        
        // Validate the Bean
        if ((strDbAction.equals("InsertRow")&&(key1==null || key1.trim().equals(""))) || strDbAction.equals("UpdateRow")) {
            if (!beanVendor.validateVendorBean()) {
                // Send error msg back to view
                strURL= "/VendorView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
        }
        
        //getting OCN ,state, service with input of Company , OCN and Activity
        if(key1!= null &&key1.equals("ocn")) {
            List listOcn=  beanVendor.getOCN(strCompSqncNumber);
            HttpSession session = request.getSession();
            session.removeAttribute("hsState");
            session.removeAttribute("state");
            session.removeAttribute("OcnValue");
            session.setAttribute("listOcn1",listOcn);
            session.setAttribute("cmpny",strCompSqncNumber);
            Log.write("==company=="+session.getAttribute("cmpny") +":=kk=="+strCompSqncNumber);
            strURL= "/VendorView.jsp?action=" +strDbAction;
            Log.write(Log.DEBUG, "VendorCtlr() line 182 strURL = " + strURL );
            alltelRequestDispatcher.forward(strURL);
            return;
            
        } else if(key1!= null &&key1.equals("state")){
            HashMap hsState = beanVendor.getState(strOCN);
            HttpSession session = request.getSession();
            session.setAttribute("OcnValue",strOCN);
            Log.write("==OcnValue=="+session.getAttribute("OcnValue") +":=kk=="+strOCN);
            session.setAttribute("hsState",hsState);
            strURL= "/VendorView.jsp?action=" + strDbAction;
            Log.write(Log.DEBUG, "VendorCtlr() line 182 strURL = " + strURL );
            alltelRequestDispatcher.forward(strURL);
            return;
        }else if(key1!= null &&key1.equals("service")){
            HttpSession session = request.getSession();
            session.setAttribute("state",strStateCode);
            String sericeType1=null;
            int len = strServiceType.lastIndexOf("^");
            Log.write("====len==="+len);
            if(len!=-1){
                sericeType1 =strServiceType.substring(len+1,strServiceType.length());
            }
            List listAct = new ArrayList();
            if(sericeType1!=null){
                listAct = beanVendor.getActvity(sericeType1);
            }
            session.setAttribute("listAct",listAct);
            session.setAttribute("service",strServiceType);
            Log.write("==OcnValue=="+session.getAttribute("listAct") +":=kk=="+strServiceType);
            if(listAct.size()>0){
                strURL= "/VendorView.jsp?action=" + strDbAction;
            }else{
                strURL= "/VendorView.jsp?norecord=no&action=" + strDbAction;
            }
            Log.write(Log.DEBUG, "VendorCtlr() line 182 strURL = " + strURL );
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        
        // Perform requested action
        if (strDbAction.equals("InsertRow") && (key1==null ||  key1.trim().equals(""))) {
            // Store to DB
            Log.write("VendorCtlr() strCompSqncNumber 11 " + beanVendor.getStrCompSqncNumber());
            Log.write("VendorCtlr() strCompSqncNumber 11 " + strCompSqncNumber);
            if (beanVendor.saveVendorBeanToDB() != 0) {
                strURL= "/VendorView.jsp?action=" + strDbAction;
            } else {
                // Save the User ID to the session for later user
                HttpSession session = request.getSession();
                session.removeAttribute("listOcn1");
                session.removeAttribute("OcnValue");
                session.removeAttribute("cmpny");
                session.removeAttribute("listAct");
                session.removeAttribute("service");
                session.removeAttribute("hsState");
                session.removeAttribute("state");
                session.setAttribute("vendorConfigSqncNumber", beanVendor.getStrVendorConfigSqncNumber());
                strURL = "/TableAdminCtlr?tblnmbr=28";
            }
        } else if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
        /* kumar commented     if (!beanVendor.validateMdfdDt()) {
                // Send error msg back to view
                strURL= "/VendorView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }*/
            
            // Update DB
            if (beanVendor.updateVendorBeanToDB() != 0) {
                strURL= "/VendorView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=28";
            }
        } else if (strDbAction.equals("get")) {
            // Save the User ID to the session for later user
            HttpSession session = request.getSession();
            if( beanVendor.getStrVendorConfigSqncNumber()!=null &&
                    beanVendor.getStrVendorConfigSqncNumber().trim().length()>0){
                session.setAttribute("vendorConfigSqncNumber", beanVendor.getStrVendorConfigSqncNumber());
            }else{
                session.setAttribute("vendorConfigSqncNumber", strVendorConfigSqncNumber);
            }
            
            // Retrieve from DB
            if (beanVendor.retrieveVendorBeanFromDB() != 0) {
                strURL= "/VendorView.jsp?action=" + strDbAction;
            }
        } else if (strDbAction.equals("DeleteRow")) {
            // Delete from DB
            if (beanVendor.deleteVendorBeanFromDB() != 0) {
                strURL= "/VendorView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=28";
            }
        } else if (strDbAction.equals("new")) {
            // Initialize the Bean to empty
            beanVendor.setStrVendorConfigSqncNumber("");
            beanVendor.setStrCompSqncNumber("");
            beanVendor.setStrStateCode("");
            beanVendor.setStrOCN("");
            beanVendor.setStrBTN("");
            beanVendor.setStrWCN("");
            beanVendor.setIsEmbargoed("");
            beanVendor.setStrTXJUR("");
            beanVendor.setStrServiceType("");
            beanVendor.setStrActivityType("");
            beanVendor.setIsDirectory("");
            beanVendor.setIsEligibleToDeleteDir("");
            beanVendor.setLgnAttmpts(0);
            beanVendor.setValidTimeOfDayDDD("");
            beanVendor.setDueDateLowerLimit("");
            beanVendor.setDueDateUpperLimit("");
            beanVendor.setSLAWaitTime("");
            beanVendor.setMdfdDt("");
            beanVendor.setMdfdUserid("");
            
        } else if (strDbAction.equals("Cancel")) {
            HttpSession session = request.getSession();
            session.removeAttribute("listOcn1");
            session.removeAttribute("OcnValue");
            session.removeAttribute("cmpny");
            session.removeAttribute("listAct");
            session.removeAttribute("service");
            session.removeAttribute("hsState");
            session.removeAttribute("state");
            session.setAttribute("vendorConfigSqncNumber", beanVendor.getStrVendorConfigSqncNumber());
            strURL = "/TableAdminCtlr?tblnmbr=28";
        } else {
            strURL = "/NavigationErrorView.jsp";
        }
        
        // Pass the request and response to the JSP
        Log.write(Log.DEBUG, "VendorCtlr() line 277 strURL = " + strURL );
        alltelRequestDispatcher.forward(strURL);
        return;
    }
    
    protected void populateVariables()
    throws Exception {
    }
}


