package com.alltel.lsr.common.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.alltel.lsr.common.objects.*;
import com.alltel.lsr.common.util.*;

public class VendorAutomationCtlr extends AlltelServlet {
    
    public void myservice(AlltelRequest request, AlltelResponse response)
    throws Exception {
        String strURL = "/VendorAutomationView.jsp";
        String strEnc = null;
        
        Log.write(Log.DEBUG_VERBOSE, "VendorAutomationCtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
        
        // Instantiate the VendorAutomationBean Bean
        //   VendorAutomationBean beanVendorAutomation = new VendorAutomationBean();
        VendorBean beanVendorAutomation = new VendorBean();
        request.getHttpRequest().setAttribute("beanVendorAutomation1", beanVendorAutomation);
        
        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        
        String strDbAction1 = request.getParameter("action1");
        
        String strDbAction2 = request.getParameter("action2");
        
        if(strDbAction2!=null && strDbAction2.trim().length()>0){
            strDbAction=strDbAction2;
        }
        
        Log.write(Log.DEBUG, "VendorAutomationCtlr() strDbAction = " + strDbAction);
        
        String strVendorAutomationConfigSqncNumber = request.getParameter("VENDOR_AUTOMATION_CONFIG_SQNC_NMBR");
        String strVendorAutomationConfigDescription =  request.getParameter("VENDOR_AUTOMATION_CONFIG_DESCRIPTION");
        
        String keys =  request.getParameter("keys");
        Log.write("=keys===="+keys);
        if ((keys!= null) && (keys.equals("automate"))) {
            
            strURL = "/VendorAutomationView.jsp?keys=automate";
            alltelRequestDispatcher.forward(strURL);
            Log.write("=str URL===="+strURL);
            return;
        }
        if(strDbAction1!=null &&  strDbAction1.equals("Cancel")){
            strURL = "/MenuView.jsp?menunmbr=1";
            alltelRequestDispatcher.forward(strURL);
            Log.write("=strURL===="+strURL);
            return;
        }
        
        
        String[]  strCompSqncNumber=  request.getAttributeValue("CMPNY_SQNC_NMBR");
        String[]  strStateCode =  request.getAttributeValue("STT_CD");
        String[]  strOCN =  request.getAttributeValue("OCN_CD");
        String[]  strServiceType=  request.getAttributeValue("SRVC_TYP_CD");
        String[]  strActivityType=  request.getAttributeValue("ACTVTY_TYP_CD");
        
        String[] strVendorConfigSqncNumber = request.getAttributeValue("VENDOR_CONFIG_SQNC_NMBR");
        String[] vedorAutomateFlag=  request.getAttributeValue("VEDOR_AUTOMATE_FLAG");
        String[] ocnAutomateFlag=  request.getAttributeValue("OCN_AUTOMATE_FLAG");
        String[] stateAutomateFlag= request.getAttributeValue("STATE_AUTOMATE_FLAG");
        String[] srvtypeAutomateFlag= request.getAttributeValue("SRVTYPE_AUTOMATE_FLAG");
        String[] acttypeAutomateFlag= request.getAttributeValue("ACTTYPE_AUTOMATE_FLAG");
        String[] allFlag= request.getAttributeValue("ALLFLAG");
        String flag=request.getParameter("flagValue");
        
        String strMdfdDt = request.getParameter("MDFD_DT");
        if ((strMdfdDt == null) || (strMdfdDt.length() == 0)) {
            // Handle the error
        }
        
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }
        
        
        if ((strVendorAutomationConfigSqncNumber == null) || (strVendorAutomationConfigSqncNumber.length() == 0)) {
            // Handle the error
        }
        
        if ((strVendorAutomationConfigDescription == null) || (strVendorAutomationConfigDescription.length() == 0)) {
            // Handle the error
        }
        //User has authority to Controller to get here, now see if they have authority to function
//        if (	(strDbAction.equals("get")	 && !sdm.isAuthorized(beanVendorAutomation.getTblAdmnScrtyTgView())) ||
//                (strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanVendorAutomation.getTblAdmnScrtyTgAdd())) ||
//                (strDbAction.equals("new") 	 && !sdm.isAuthorized(beanVendorAutomation.getTblAdmnScrtyTgAdd())) ||
//                (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanVendorAutomation.getTblAdmnScrtyTgMod())) ||
//                (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanVendorAutomation.getTblAdmnScrtyTgDel()))   ) {
//            Log.write(Log.WARNING, "CompanyCtlr() user " + sdm.getUser() + " is trying to bypass security!");
//            alltelRequestDispatcher.forward(SecurityBreachURL);
//            return;
//        }
        
        // Populate the Bean
        
        beanVendorAutomation.setMdfdUserid(sdm.getUser());
        beanVendorAutomation.setDbAction(strDbAction);
        
        // Validate the Bean
        if (strDbAction.equals("InsertRow") || strDbAction.equals("UpdateRow")) {
            
        }
        
        // Perform requested action
        if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
        /* kumar commented     if (!beanVendorAutomation.validateMdfdDt()) {
                // Send error msg back to view
                strURL= "/VendorAutomationView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }*/
            
            // Update DB
            
            
            List flagList = new ArrayList();
            flagList.add(allFlag);
            flagList.add(strVendorConfigSqncNumber);
            if(flag!=null && flag.trim().length()>0){
                flagList.add(flag);
            }else{
                flagList.add(null);
            }
            flagList.add(vedorAutomateFlag);
            flagList.add(ocnAutomateFlag);
            flagList.add(stateAutomateFlag);
            flagList.add(srvtypeAutomateFlag);
            flagList.add(acttypeAutomateFlag);
            
            
            
            
            if (beanVendorAutomation.updateVendorAutomationBeanToDB(flagList) == 0) {
                strURL= "/VendorSearch.jsp?key2=noupdate&action=" + strDbAction;
            } else {
                HttpSession session = request.getSession();
                session.removeAttribute("listAuto1");
                strURL = "/VendorAutomationView.jsp?action1=updatesucess";
            }
        }else if (strDbAction.equals("Search")) {
            // Save the User ID to the session for later user
            HttpSession session = request.getSession();
            session.setAttribute("VendorConfigSqncNumber", strVendorAutomationConfigSqncNumber);
            List listAuto =null;
//            if(strCompSqncNumber!=null || strStateCode!=null || strOCN!=null ||
//                    strServiceType!=null || strActivityType!=null ){
                String strQuery=beanVendorAutomation.queryBuilder(strCompSqncNumber,strStateCode,strOCN,strServiceType,strActivityType);
                listAuto = beanVendorAutomation.retrieveVendorAutomationBeanFromDB(strQuery);
          //  }
            if(listAuto!=null){
                if (listAuto.size()>0) {
                    session.setAttribute("listAuto1",listAuto);
                    strURL= "/VendorSearch.jsp?action=" + strDbAction;
                }else{
                    strURL= "/VendorAutomationView.jsp?action1=norecords";
                }
                Log.write("=Search==strURL=="+strURL);
            }else{
                strURL= "/VendorAutomationView.jsp?action1=nosearch";
            }
        }
//        else if (strDbAction.equals("DeleteRow")) {
//            // Delete from DB
//            if (beanVendorAutomation.deleteVendorAutomationBeanFromDB() != 0) {
//                strURL= "/VendorAutomationView.jsp?action=" + strDbAction;
//            } else {
//                strURL = "/TableAdminCtlr?tblnmbr=2123";
//            }
//        }
        else if (strDbAction.equals("new")) {
            // Initialize the Bean
//        beanVendorAutomation.setStrCompSqncNumber(strCompSqncNumber);
//        beanVendorAutomation.setStrStateCode(strStateCode);
//        beanVendorAutomation.setStrOCN(strOCN);
//        beanVendorAutomation.setStrServiceType(strServiceType);
//        beanVendorAutomation.setStrActivityType(strActivityType);
            
            //  } else if ((strDbAction.equals("Cancel")) || (strDbAction1!null && strDbAction1.equals("Cancel"))) {
        } else if (strDbAction.equals("Cancel")) {
            HttpSession session = request.getSession();
            session.removeAttribute("listAuto1");
            strURL = "/VendorAutomationView.jsp";
        } else {
            
            strURL = "/NavigationErrorView.jsp";
        }
        
        // Pass the request and response to the JSP
        Log.write(Log.DEBUG, "VendorAutomationCtlr() strURL = " + strURL );
        alltelRequestDispatcher.forward(strURL);
        return;
    }
    
    protected void populateVariables()
    throws Exception {
    }
}


