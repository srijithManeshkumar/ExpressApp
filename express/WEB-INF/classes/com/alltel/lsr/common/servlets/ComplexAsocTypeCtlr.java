/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alltel.lsr.common.servlets;

import com.alltel.lsr.common.objects.AlltelRequest;
import com.alltel.lsr.common.objects.AlltelRequestDispatcher;
import com.alltel.lsr.common.objects.AlltelResponse;
import com.alltel.lsr.common.objects.AlltelServlet;
import com.alltel.lsr.common.objects.ComplexAsocTypeBean;
import com.alltel.lsr.common.objects.SessionDataManager;
import com.alltel.lsr.common.util.Log;

/**
 *
 * @author satish.t
 */
public class ComplexAsocTypeCtlr extends AlltelServlet {
    /**
     * This method will server the input actions coming from the request
     * and gives response based on the response.For all crud operations this method
     * is called from the controller which will intern calls bean to process all
     * database operations
     * @param request
     * @param response
     * @throws Exception
     */
    public void myservice(AlltelRequest request, AlltelResponse response)
            throws Exception {
        String strURL = "/ComplexAsocTypeView.jsp";

        Log.write(Log.DEBUG_VERBOSE, "ComplexAsocTypeCtlr()");
        SessionDataManager sdm = request.getSessionDataManager();
        AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);

        // Instantiate the UserGroup Bean
        ComplexAsocTypeBean beanAsocType = new ComplexAsocTypeBean();
        request.getHttpRequest().setAttribute("complexasocTypeBean", beanAsocType);

        // Get values entered by the user
        String strDbAction = request.getParameter("action");
        Log.write(Log.DEBUG, "ComplexAsocTypeCtlr() strDbAction = " + strDbAction);
        if ((strDbAction == null) || (strDbAction.length() == 0)) {
            strURL = "/NavigationErrorView.jsp";
            alltelRequestDispatcher.forward(strURL);
            return;
        }

        String StrAsocSqncNo = request.getParameter("COMPLEX_ASOC_SQNC_NMBR");
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
        if ((strDbAction.equals("get") && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgView()))
                || (strDbAction.equals("InsertRow") && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgAdd()))
                || (strDbAction.equals("new") && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgAdd()))
                || (strDbAction.equals("UpdateRow") && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgMod()))
                || (strDbAction.equals("DeleteRow") && !sdm.isAuthorized(beanAsocType.getTblAdmnScrtyTgDel()))) {
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
                strURL = "/ComplexAsocTypeView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }
        }

        // Perform requested action
        if (strDbAction.equals("InsertRow")) {
            // Store to DB
            if (beanAsocType.saveAsocTypeBeanToDB() != 0) {
                strURL = "/ComplexAsocTypeView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=31";
            }
        } else if (strDbAction.equals("UpdateRow")) {
            // Verify that no one else has modifed this row since it was retrieved
            if (!beanAsocType.validateMdfdDt()) {
                // Send error msg back to view
                strURL = "/ComplexAsocTypeView.jsp?action=" + strDbAction;
                alltelRequestDispatcher.forward(strURL);
                return;
            }

            // Update DB
            if (beanAsocType.updateAsocTypeBeanToDB() != 0) {
                strURL = "/ComplexAsocTypeView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=31";
            }
        } else if (strDbAction.equals("get")) {
            // Retrieve from DB
            if (beanAsocType.retrieveAsocTypeBeanFromDB() != 0) {
                strURL = "/ComplexAsocTypeView.jsp?action=" + strDbAction;
            }
        } else if (strDbAction.equals("DeleteRow")) {
            // Delete from DB
            if (beanAsocType.deleteAsocTypeBeanFromDB() != 0) {
                strURL = "/ComplexAsocTypeView.jsp?action=" + strDbAction;
            } else {
                strURL = "/TableAdminCtlr?tblnmbr=31";
            }
        } else if (strDbAction.equals("new")) {
            // Initialize the Bean
            beanAsocType.setStrAsocTypeConfigSeqNo("");
            beanAsocType.setStrAsocType("");
            beanAsocType.setStrAsocDescrption("");
            beanAsocType.setMdfdDt("");
            beanAsocType.setMdfdUserid("");
        } else if (strDbAction.equals("Cancel")) {
            strURL = "/TableAdminCtlr?tblnmbr=31";
        } else {
            strURL = "/NavigationErrorView.jsp";
        }

        // Pass the request and response to the JSP
        Log.write(Log.DEBUG, "CompanyCtlr() strURL = " + strURL);
        alltelRequestDispatcher.forward(strURL);
        return;
    }

    protected void populateVariables()
            throws Exception {
    }
}
