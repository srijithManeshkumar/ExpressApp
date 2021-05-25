/*
 * LSRBaseController.java
 *
 * Created on June 12, 2009, 5:11 PM
 */
package com.automation.controller;

import com.alltel.lsr.common.error.objects.ExceptionHandler;
import com.automation.bean.LSRDataBean;
import com.automation.bean.ReasonCodeBean;
import com.automation.bw.BWDCRISOrderWebservice;
import com.automation.dao.LSRdao;
import com.alltel.lsr.common.objects.AlltelRequest;
import com.alltel.lsr.common.objects.AlltelRequestDispatcher;
import com.alltel.lsr.common.objects.AlltelResponse;
import com.alltel.lsr.common.objects.AlltelServlet;
import com.alltel.lsr.common.objects.SessionDataManager;
import com.alltel.lsr.common.util.Log;
import com.automation.bean.ValidationDataBean;
import com.automation.bean.VendorTableDataBean;
import com.automation.bw.BWCusInfoWebservice;
import com.automation.validator.DirAssDirDISCValidator;
import com.automation.validator.LSRBaseValidator;
import com.automation.validator.NPValidator;
import com.automation.validator.ResaleDISCValidator;
import com.automation.validator.ResaleSusValidator;
import com.automation.validator.SPSRValidator;
import com.automation.validator.UNEPConverValidator;
import com.automation.validator.UNEPDisValidator;
import com.windstream.expressorder.webservice.ErrorInfo;
import com.windstream.winexpcustprof.Asoc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.servlet.*;

/**
 *
 * @author kumar.k
 * @version
 */
public class LSRBaseController extends AlltelServlet {

    //removing global declaration for bean object - probable fix for SUB/SUB issue 
    // Antony -- 09/13/2011
    //public LSRDataBean objLSRDataBean;
    
    /*
     * init method used for loading vendor,Reason code and holiday table
     * Values.
     */
    public void init(ServletConfig config)
            throws ServletException {
        LSRdao objLSRdao = new LSRdao();
        super.init(config);
        try {
            Vector vendorBeanVector = objLSRdao.loadVendorTable();
            Vector holidayVector = objLSRdao.loadHolidayTable();
            Map rcodeMap = objLSRdao.loadReasonCodeTable();
            Map streetAddrsMap = objLSRdao.loadCommonAddrsStrtTypes();
            config.getServletContext().setAttribute("vendorBeanVector", vendorBeanVector);
            config.getServletContext().setAttribute("holidayVector", holidayVector);
            config.getServletContext().setAttribute("rcodeMap", rcodeMap);
            config.getServletContext().setAttribute("streetAddrsMap", streetAddrsMap);
        } catch (Exception e) {
            Log.write(e.getMessage());
        }
    }

    /*
     * myservice method used for to get AlltelRequest,AlltelResponse input and send to
     * processRequest method.
     */
    public void myservice(AlltelRequest request, AlltelResponse response)
            throws Exception {

        Log.write("LSRBaseValidator myservice method calling ");
        processRequest(request, response);
    }

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param AlltelRequest servlet request
     * @param AlltelResponse servlet response
     */
    protected void processRequest(AlltelRequest request, AlltelResponse response)
            throws Exception {
        try {
            Log.write("LSRBaseController class =processRequest method calling..");
            SessionDataManager sdm = request.getSessionDataManager();
            AlltelRequestDispatcher alltelRequestDispatcher = new AlltelRequestDispatcher(this, request, response);
            LSRdao objLSRdao = new LSRdao();
            Vector vendorBeanVector = (Vector) getServletContext().getAttribute("vendorBeanVector");
            Vector holidayVector = (Vector) getServletContext().getAttribute("holidayVector");
            Map rcodeMap = (Map) getServletContext().getAttribute("rcodeMap");
            Map streetAddrsMap = (Map) getServletContext().getAttribute("streetAddrsMap");
            String reqNo = request.getParameter("reqNo");
            String reqVer = request.getParameter("reqVer");
            String reqUrl = request.getParameter("reqUrl");
            String m_strSupTypCd = request.getParameter("_FF_1_0_25");
            if (m_strSupTypCd == null || m_strSupTypCd.length() == 0) {
                m_strSupTypCd = objLSRdao.getSUPPType(reqNo,reqVer);
                Log.write("SUPP type from form null so getting from db. Value obtained for Supp Type :"+m_strSupTypCd);
                
                if(m_strSupTypCd == null || m_strSupTypCd.length() == 0)
                    m_strSupTypCd = "0";
                
                Log.write("SUPP type from form null so getting from db.Modified Value obtained for Supp Type :"+m_strSupTypCd);
                
            }

            
            LSRDataBean objLSRDataBean = objLSRdao.loadLSRData(reqNo,reqVer);

            Log.write("LSRBaseController class LSRDataBean  " + objLSRDataBean + "  reqNo : " + reqNo + " reqVer : " + reqVer);
            if (reqUrl == null) {
                reqUrl = "RequestListCtlr";
            }
            if (objLSRDataBean == null) {
                alltelRequestDispatcher.forward("/" + reqUrl);
                Log.write("LSRBaseController processRequest LSRDataBean " + objLSRDataBean);
                return;

            }


            objLSRDataBean.setMdfdUserid(sdm.getUser());
            Log.write(" processRequest method reqNo " + reqNo + " reqVer " + reqVer + " reqUrl: " + reqUrl + " objLSRDataBean " + objLSRDataBean);
            objLSRdao.updateInternalStatus(reqNo, reqVer, sdm.getUser());

            LSRBaseValidator lsrbaseValdtr =
                    new LSRBaseValidator(objLSRDataBean);
            String requestType = lsrbaseValdtr.getReqSerActType();
            Log.write(" LBC requestType" + requestType);

            /*
             * RequestType empty that means other than 8 flows it will
             * redirect to RequestListCtlr page.
             */

            if (requestType != null && requestType.trim().length() == 0) {
                alltelRequestDispatcher.forward("/" + reqUrl);
                return;
            }

            /*
             * connecting Buisness Ware so creating BWCusInfoWebservice instance
             */

            BWCusInfoWebservice bwcusInfows = new BWCusInfoWebservice();
            Log.write(" LSRBaseController:BWCusInfoWebservice calling ");
            bwcusInfows.webServiceInvoke(objLSRDataBean);

            Log.write(" processRequest method reqNo: " + reqNo + " reqUrl: " + reqUrl + " objLSRDataBean " + objLSRDataBean);

            /*
             * creating ValidationDataBean instance
             */

            ValidationDataBean validationData = bwcusInfows.getValidationBean();

            if (validationData == null || (!validationData.getErrorInfo().getErrorID().equals("0000") && !validationData.getCustStatus().equals("Invalid"))) {
                Log.write("LSRBaseController processRequest validationData error ID-- " + validationData.getErrorInfo().getErrorID());

                
                if(validationData.getErrorInfo().getErrorID().equals("1000")) {
                    Log.write("Customer not found in CAMS error response received from BW. Sending for reject with no further validations for Invalid TN..");
                    
                    validationData.setCustStatus("CUST_NOT_IN_CAMS");
                    
                } else {
                                
                    /*
                     * redirecting RequestListCtlr and updating AE_t table with BW down
                     */
                    alltelRequestDispatcher.forward("/" + reqUrl);
                    updateExINStatus(objLSRdao, sdm.getUser(), objLSRDataBean, true, "MR0");
                    Log.write("This Request could not obtain Businessware Data(No ValidationDataBean) " +
                            "No automation process");
                    return;
                
                }
            }
            /*
             * updateCustDBReqTable used update custsag from BW(getCustSag)
             *
             */
                      
            objLSRdao.updateCustDBReqTable(reqNo, validationData);
            
            
            /******* moved these two update methods to this location where CI form video field update is being done - Antony - 01/28/2014*****/
            /******* bug found by Diane Walters during UAT of videotype field -- BB field is not being updated always ********/
            Log.write("Updating green,broad field updation");
            objLSRdao.updateGreenfieldReqTable(reqNo, validationData, objLSRDataBean);
            objLSRdao.updateBroadbandReqTable(reqNo, validationData, objLSRDataBean);
            Log.write("Updating green,broad field updation done====");
            
            /*
             * updateCIFormVideoTypeField method added by Antony -- 12/19/2013
             *
             */

            objLSRdao.updateCIFormVideoTypeField(reqNo,reqVer,validationData.getVideoType());
            

            /*
             * creating VendorTableDataBean instance through
             * getVendorTableDataForLSR method
             *
             */
            VendorTableDataBean objVendorBean =
                    getVendorTableDataForLSR(vendorBeanVector, objLSRDataBean, validationData);

            if (objVendorBean == null) {

                /*
                 * redirecting RequestListCtlr and updating AE_t and automation_statues_t
                 * table with Vendor Data not match
                 */
                String Camswcn = objLSRDataBean.getWcnCheckFlag();
                String autoVen = objLSRDataBean.getVendorautoVal();
                alltelRequestDispatcher.forward("/" + reqUrl);
                Log.write("LSRBaseController processRequest VendorBean--Camswcn  " + Camswcn);
                if (Camswcn != null) {
                    updateExINStatus(objLSRdao, sdm.getUser(), objLSRDataBean, true, "VEN");
                } else if (autoVen != null) {
                    updateExINStatus(objLSRdao, sdm.getUser(), objLSRDataBean, true, "autoff");
                } else {
                    updateExINStatus(objLSRdao, sdm.getUser(), objLSRDataBean, false, "WCN");
                }

                Log.write("this Request is not having Vendor Data(No VendorBean) " +
                        "No automation process");
                return;
            }

            //LSR's with CKT/REIT ASOC on the account to fall out to Manual-Review from automation.
            if(validationData != null && objLSRDataBean != null){
                //Getting the ASOC name from CustAsocList (BWCusInfoWebservice).
                Asoc asoc[] = validationData.getCustAsocList();
                for(int i = 0; i < asoc.length; i++){
  
                    if(asoc[i] != null && ("CKT".equalsIgnoreCase(asoc[i].getAsocName()) || "REIT".equalsIgnoreCase(asoc[i].getAsocName()))){
                      
                        alltelRequestDispatcher.forward("/" + reqUrl);
                        updateExINStatus(objLSRdao, sdm.getUser(), objLSRDataBean, true, asoc[i].getAsocName().toUpperCase());
                        Log.write("Request having CKT/REIT ASOC account - No automation process(Manual-Review)");
                        return;
                    }
                }
            }
            
            System.out.println("Go for automation process...!!!");
            //Log.write("LSRBaseController processRequest vendorBeanVector-- " + vendorBeanVector);
            Log.write(" LSRBaseController processRequest VendorTableDataBean " + objVendorBean);
            Log.write("LSRBaseController processRequest ValidationDataBean " + validationData);
            Map mapRejCode = null;
            if (objLSRDataBean != null &&
                    validationData != null && objVendorBean != null) {

                /*
                 * In below If condition will work for NP,UPC,SP flow
                 * here loading LSRBean of portnumber information from lerg DB
                 *
                 */
                if (requestType.equals("NPV") || requestType.equals("UNEPV") || requestType.equals("SP")) {
                    String rcAbr = "";

                    try {//catch LERG DB Exception
                        rcAbr =
                                objLSRdao.getPortNumberLerg(objLSRDataBean.getAccountTelephoneNo());
                    } catch(Exception e) {
                        //send to MR; so set PortNumberFlag as 2
                        Log.write("Error in LERG DB Connection ...");
                        objLSRDataBean.setPorNumberFlag(2);
                    }

                    if(rcAbr != null && rcAbr.length() > 0)
                        objLSRDataBean.setPorNumberFlag(objLSRdao.checkVendorPortable_PortNumber(rcAbr, objVendorBean));
                }
                /*
                 * In below If condition will work for DAD flow
                 * here loading LSRBean of Native Number information from lerg DB
                 *
                 */
                if (requestType.equals("DAD")) {
                    objLSRDataBean.setNativeNumberFlag(objLSRdao.getWindstreamNativeNumberLerg(objLSRDataBean.getAccountTelephoneNo()));
                    Log.write("Value of native flag in lsrbasecontroller: "+objLSRDataBean.isNativeNumberFlag());
                }

                objLSRDataBean.setAtnNPA(objLSRdao.checkNPAOCNState(reqNo, objLSRDataBean.getAccountTelephoneNo()));

                /*
                 * In below If condition will work for 8 flows,
                 * get the LSRBaseValidator instance
                 *
                 */

                lsrbaseValdtr = routeToAppValidationFlow(objLSRDataBean, validationData, objVendorBean, requestType);
                Log.write("== requestType= " + requestType);

                if (lsrbaseValdtr != null) {
                    lsrbaseValdtr.setHoldayVector(holidayVector);
                    lsrbaseValdtr.setStreetAddrsMap(streetAddrsMap);

                    //call method to update slatime if LSR is a Simple order
                    if(requestType.equals("SP")) {
                        lsrbaseValdtr.updateSLATimeforSP();
                    }
                    
                     //to check previous version is not submitted by automation id
                    boolean  prevVerNotAuto = objLSRdao.checkPrevVerNotAuto(objLSRDataBean.getReqstNmbr(),objLSRDataBean.getReqstVer());
                    
                    if (reqVer.equals("0")) {
                        Log.write(Log.DEBUG_VERBOSE, "LSRBaseController() m_strSupTypCd="+m_strSupTypCd);
                    //Code Change to divert Camprod3 PON to Manual-Review irrespective of other Validations - Saravanan
                        boolean valFlag = true;
                        boolean intialFlag  = false ;
                        if (lsrbaseValdtr instanceof NPValidator) {
                            NPValidator objNPValidator = (NPValidator) lsrbaseValdtr;
                            valFlag = objNPValidator.checkCAMS_TN_CAMPRD3();
                             Log.write("Initial Validation - checkCAMS_TN_CAMPRD3 : " + valFlag);
                        }
                       else if (lsrbaseValdtr instanceof SPSRValidator) {
                           SPSRValidator objSPSR = (SPSRValidator) lsrbaseValdtr;
          	           valFlag = objSPSR.checkCAMS_TN_CAMPRD3();
          	            Log.write("Initial Validation - checkCAMS_TN_CAMPRD3 : " + valFlag);
                        }
                        if (valFlag)
                        {
                            intialFlag = processInitialValidation(lsrbaseValdtr, requestType, m_strSupTypCd,objLSRDataBean.getReqstNmbr(),objLSRDataBean.getReqstVer());
                            Log.write(" status  intialFlag : " + intialFlag);
                        }

                        /*
                         * intialFlag will true only other validation will invoke
                         *
                         */

                        if (intialFlag) {
                           lsrGenericflowValidation(lsrbaseValdtr, requestType, m_strSupTypCd);
                           processOtherValidation(lsrbaseValdtr);
                        }

                    } else if ( (m_strSupTypCd.equals("1") || m_strSupTypCd.equals("2") || m_strSupTypCd.equals("3")) && !prevVerNotAuto) {
                    //added SUPP2 and SUPP3 here as the same steps have to be done for them too - Antony - 03/30/2012
                    Log.write(Log.DEBUG_VERBOSE, "LSRBaseController() m_strSupTypCd="+m_strSupTypCd);
                    //Code Change to divert Camprod3 PON to Manual-Review irrespective of other Validations - Saravanan
                        boolean valFlag = true;
                        boolean initialFlag  = false ;
                        if (lsrbaseValdtr instanceof NPValidator) {
                            NPValidator objNPValidator = (NPValidator) lsrbaseValdtr;
                            valFlag = objNPValidator.checkCAMS_TN_CAMPRD3();
                            Log.write("Initial Validation - checkCAMS_TN_CAMPRD3 : " + valFlag);
                        }
                        else if (lsrbaseValdtr instanceof SPSRValidator) {
                            SPSRValidator objSPSR = (SPSRValidator) lsrbaseValdtr;
                	    valFlag = objSPSR.checkCAMS_TN_CAMPRD3();
                            Log.write("Initial Validation - checkCAMS_TN_CAMPRD3 : " + valFlag);
                        }
                        if (valFlag) {
                           initialFlag = processInitialValidation(lsrbaseValdtr, requestType, m_strSupTypCd,objLSRDataBean.getReqstNmbr(),objLSRDataBean.getReqstVer());
                        }
                              
                              //check AE table if this is a partial port - if partial redirect to list page
                              boolean partialPONResult = objLSRdao.checkAEForPartial(reqNo,reqVer);
                              
                              Log.write("After calling checkAEForPartial. Result: "+partialPONResult);
                              
                              if(partialPONResult) {

                                //code change for V5 - to check if pilot tn is active - if not send to MR - with message on AE form:
                                //"ATN not activated in SOA. C-order Required." and -- redirect to Request list page / stop automation
                                // Antony - 3/14/2012

                                //check if first TN in goto list is active in SOA
                                List tnList = objLSRDataBean.getPortedNBR();  
                                  
                                String strTN = (String) tnList.get(0);                

                                Hashtable pilotTNResult = new Hashtable();

                                pilotTNResult = lsrbaseValdtr.checkSVStatusInSOASUPP1("pending",strTN);

                                String strPilotTNNotActive = "";

                                strPilotTNNotActive = (String) pilotTNResult.get("svStatus");

                                //If non-active in SOA then stop post-version flow and send to Submitted/MR (V5) - Antony - 03/14/2012
                                if(strPilotTNNotActive != null && strPilotTNNotActive.equals("non-active") /*&& !allSVsInPendingStatus*/) {
                                    //call method to update AE form and send to MR
                                    Log.write("Sending PON with Pilot TN / ATN to MR: "+strTN);

                                    objLSRdao.callMRStatusUpdateProc(objLSRDataBean.getReqstPon(),objLSRDataBean.getReqstVer(),objLSRDataBean.getOCNcd(),"6002","ATN not activated in SOA. C-order Required.");

                                    Log.write("Result of svStatus in SOA for SUPP1 PON: "+objLSRDataBean.getReqstPon()+" is "+strPilotTNNotActive);

                                    //redirect to requests list page
                                    Log.write("redirect to requests list page....");

                                    Log.write("LSRBaseController:Before forwarding to request list page for reqNo/reqVer as Pilot TN not active:"+reqNo+"/"+reqVer);  
                                    alltelRequestDispatcher.forward("/" + reqUrl);
                                    Log.write("LSRBaseController:After forwarding to request list page for reqNo/reqVer as Pilot TN not active:"+reqNo+"/"+reqVer);

                                    return;
                                }  
                                  
                                  
                                  
                                  
                                //code to do X-order - V7 - post-version flow - Antony - 02/15/2012
                                //this is a SUPP so we need to send a X-order if there is a supp fee; so call getSuppFeeAsocs method
                                String strSuppType = objLSRDataBean.getSupplementalType();
                                Log.write("Calling LSRdao.getSuppFeeAsocs method...");
                                Hashtable reqData = objLSRdao.getRequestData(reqNo,reqVer);
                                Vector vendorAsocVector = objLSRdao.getSuppFeeAsocs(objVendorBean.getVendorConfigSqncNumber(),(String) reqData.get("CUST_TYPE"), m_strSupTypCd,objLSRdao.getFOCStatus(reqNo,reqVer));//need to add version later
                                Log.write("Calling LSRdao.getSuppFeeAsocs method...vector size :"+vendorAsocVector.size());
                                  
                                Log.write("Calling LSRdao.getServiceOrderFeeAsocs method...");
                                Vector vendorServiceOrderFeeAsocVector = objLSRdao.getVendorTableASOCs(objVendorBean.getVendorConfigSqncNumber(),reqData);
                                Log.write("Calling LSRdao.getServiceOrderFeeAsocs method...vector size :"+vendorServiceOrderFeeAsocVector.size());
                                                          
                                
                                //X - order has to be done for all SUPP types so block moved to beginning
                                //check if cancel fee asoc vector has objects and if so send X-order for cancel fee
                                if(vendorAsocVector.size() != 0) {//no x order if no asoc
                                    Log.write("Calling XOrder create method for NP SUPP PON for Cancel Fee...");

                                    BWDCRISOrderWebservice bwCreateOrder = new BWDCRISOrderWebservice();
                                    
                                    ErrorInfo cancelFeeResultStr = bwCreateOrder.sendXOrder(objLSRDataBean,"NP",objVendorBean,vendorAsocVector);

                                    Log.write("Value returned by Cancel Fee sendXOrder in SLATimer:"+cancelFeeResultStr.toString());
                                    Log.write("Finished calling Cancel Fee sendXOrder method...");

                                    if(cancelFeeResultStr == null) {
                                        //call MR status SP with error message "Null Response from BW Order Creation Webservice for X Order. Please contact BW team."
                                        //commented as per Christie//lsrDao.callMRStatusUpdateProc(lsrDataBean.getPurchaseON(),lsrDataBean.getPurVerNum(),lsrDataBean.getOCNcd(),"6001","Null Response from BW Order Creation Webservice. Please contact BW team.");

                                        Log.write("Null Response from BW Order Creation Webservice. Please contact BW team.");
                                    }

                                    if(!cancelFeeResultStr.getErrorID().equals("0000")) {
                                        //call MR status change SP with error message resultStr.getErrorMessage()
                                        //commented as per Christie//lsrDao.callMRStatusUpdateProc(lsrDataBean.getPurchaseON(),lsrDataBean.getPurVerNum(),lsrDataBean.getOCNcd(),resultStr.getErrorID(),resultStr.getErrorMsg());
                                        Log.write("Error Response from BW Order Creation Webservice. Please contact BW team."+cancelFeeResultStr.getErrorID()+": "+cancelFeeResultStr.getErrorMsg());
                                    }
                                }
                                  
                                //code to create new DCRIS order with TNs that are active to non-WIN SPID (TNs in NP table - updated list)
                                //to refresh NP TN list load LSRDataBean again - Antony - 02/27/2012
                                objLSRDataBean = objLSRdao.loadLSRData(reqNo,reqVer);
                                
                                ErrorInfo resultStr;
                                Log.write("LSRBaseController:Calling sendOOrder method...");

                                BWDCRISOrderWebservice bwCreateServiceOrder = new BWDCRISOrderWebservice();
                                
                                Vector gotoList = objLSRdao.getPortedTNs(reqNo,reqVer,"NP_SD_T","NP_SD_PORTEDNBR");
                                
                                String strATN = (String) gotoList.elementAt(0);
                                strATN = strATN.replaceAll("-","");
                                
                                resultStr = bwCreateServiceOrder.sendOOrder(strATN,objLSRDataBean,"NP",gotoList,reqData,objVendorBean,vendorServiceOrderFeeAsocVector); 

                                Log.write("LSRBaseController:Value returned by sendOOrder :"+resultStr);
                                Log.write("LSRBaseController:Finished calling sendOOrder method...");
                            
                                if(resultStr == null) {
                                    //call MR status SP with error message "Null Response from BW Order Creation Webservice. Please contact BW team."
                                    objLSRdao.callMRStatusUpdateProc(objLSRDataBean.getReqstPon(),objLSRDataBean.getReqstVer(),objLSRDataBean.getOCNcd(),"6001","Null Response from BW Order Creation Webservice for a partial port. Please contact BW team.");
                                    return;
                                } else if(!resultStr.getErrorID().equals("0000")) {
                                    //call MR status change SP with error message resultStr.getErrorMessage()
                                    objLSRdao.callMRStatusUpdateProc(objLSRDataBean.getReqstPon(),objLSRDataBean.getReqstVer(),objLSRDataBean.getOCNcd(),resultStr.getErrorID(),resultStr.getErrorMsg());
                                    return;
                                } else {
                                    //call SP to send PON to CANCELLED/CANCELLED
                                    
                                    Log.write("LSRBaseController:Before forwarding to request list page for reqNo/reqVer :"+reqNo+"/"+reqVer);  
                                    alltelRequestDispatcher.forward("/" + reqUrl);
                                    Log.write("LSRBaseController:After forwarding to request list page for reqNo/reqVer :"+reqNo+"/"+reqVer);
                                    
                                    return;
                                }
                              } else {//if non-partial then proceed to 5072 Q&V - Antony - 03/30/2012
                                  
                                //boolean intialFlag = processInitialValidation(lsrbaseValdtr, requestType, m_strSupTypCd,objLSRDataBean.getReqstNmbr(),objLSRDataBean.getReqstVer());
                                Log.write("SUPP2/3 non-partial status  intialFlag : " + initialFlag);
                                
                                if (initialFlag) {
                                    //Code fix to bypass all the validations for Supp1 PON of NP & SP type	- Saravanan
                                    if (!((requestType.equals("NPV") || requestType.equals("SP")) && m_strSupTypCd.equals("1"))) {
                                        lsrGenericflowValidation(lsrbaseValdtr, requestType, m_strSupTypCd);
                                        processOtherValidation(lsrbaseValdtr);
                                    }
                                }
                           }
                        }
                     //previous version not submitted by auto id
                    else if((m_strSupTypCd.equals("1") || m_strSupTypCd.equals("2") || m_strSupTypCd.equals("3")) && prevVerNotAuto ) {
                        objLSRdao.updateStatusMR(objLSRDataBean.getReqstPon(),objLSRDataBean.getReqstNmbr(),objLSRDataBean.getReqstVer(),sdm.getUser());
                        Log.write("Status updated to Manual-Review since the previous version is not submitted by automation id");
                    	alltelRequestDispatcher.forward("/" + reqUrl);
                    //	objLSRdao.callMRStatusUpdateProc(objLSRDataBean.getReqstPon(),objLSRDataBean.getReqstVer(),objLSRDataBean.getOCNcd(),"6002","supp1-previous version not submitted by auto id");
                    //	objLSRdao.updateStatus("MANUAL-REVIEW", reqNo, reqVer, false);
                    	return;
                    }
                    //commented out as old Q&V flow modified to accomodate post-version flow
                    /*} else {//this block is for SUPP2/3 Q&V
                           Log.write(Log.DEBUG_VERBOSE, "LSRBaseController() m_strSupTypCd="+m_strSupTypCd);
                                                           
                           //do V4 here and V5 inside processInitialValidation as per SUPP2 and SUPP3 flows
                           //this is because we need to check status in SOA first, store result and cancel if true after Q&V is complete
                           //call method to check SV status in SOA for SUPP2/3
                           
                           Hashtable htResult = new Hashtable();
                           LSRdao lsrDao = new LSRdao();
                           boolean proceedToV5 = false;
                           boolean soaSVStatusValidForCancel = false;
                           
                           if(lsrDao.checkSVExistsInSOA(reqNo,reqVer)) {
                           
                               htResult = lsrbaseValdtr.checkSVStatusInSOA("pending");

                               String strResult = "";
               
                               strResult = (String) htResult.get("proceedToV5");

                               if(strResult != null && strResult.equals("true"))
                                   proceedToV5 = true;
                               else if(strResult != null && strResult.equals("false"))
                                   proceedToV5 = false;
                               else
                                   proceedToV5 = false;

                               strResult = (String) htResult.get("skipV6V9");

                               if(strResult != null && strResult.equals("true"))
                                   soaSVStatusValidForCancel = true;
                               else if(strResult != null && strResult.equals("false"))
                                   soaSVStatusValidForCancel = false;
                               else
                                   soaSVStatusValidForCancel = false;


                               Log.write("Result of skipV6V9 in SOA for SUPP: "+m_strSupTypCd+" is "+soaSVStatusValidForCancel);

                               Log.write("Result of proceedToV5 in SOA for SUPP: "+m_strSupTypCd+" is "+proceedToV5);
                               
                           } else {//no SV in SOA
                               
                               proceedToV5 = true;

                               soaSVStatusValidForCancel = false;

                               Log.write("No SV in SOA.Result of skipV6V9 in SOA for SUPP: "+m_strSupTypCd+" is "+soaSVStatusValidForCancel);

                               Log.write("No SV in SOA.Result of proceedToV5 in SOA for SUPP: "+m_strSupTypCd+" is "+proceedToV5);
                           }
          
                           /* We don't cancel SOA SVs anymore due to FCC change - NANC updates - Antony - 10/07/2011
                           if(soaSVStatusValidForCancel) {
                               boolean soaCancelFlag = false;
                                                           
                               soaCancelFlag = lsrbaseValdtr.cancelSVInSOA();
                               
                               Log.write("LSRBase after cancelSVInSOA : " + soaCancelFlag);
                           }
                           */
                          /* 
                           boolean slaCancelResult = lsrbaseValdtr.cancelPrevVrsnInSLAQueue();

                           Log.write("Result of SLA queue cancel : "+slaCancelResult);

                           //if(checkFlag) {//cancel dcris orders for both PRE-REJECT and PRE-FOC regardless of checkFlag true/false
                           boolean orderCancelFlag = lsrbaseValdtr.sendCancelOrderToDCRISWS();
                           //}

                           Log.write("LSRBase after sendCancelOrderToDCRISWS : " + orderCancelFlag);

                           
                           //if SOA status check returned true proceed to Q&V -- remaining initial,generic and other validations
                           if(proceedToV5) {
                               
                               boolean intialFlag = processInitialValidation(lsrbaseValdtr, requestType, m_strSupTypCd,objLSRDataBean.getReqstNmbr(),objLSRDataBean.getReqstVer());
                               Log.write(" status  intialFlag : " + intialFlag);
                               if (intialFlag) {
                                  lsrGenericflowValidation(lsrbaseValdtr, requestType, m_strSupTypCd);
                                  processOtherValidation(lsrbaseValdtr);
                               }
                           }
                                                      
                    }   */

                    mapRejCode = lsrbaseValdtr.getRejectionCode();

                    /*
                     * (insertRejCode) getting each validation of Rejection code,Manual code and Pass code insert into AUTOMATION_RESULTS_T table
                     * without removing reasoncode sequnce number with method name. for purpose of validation reports
                     *
                     */

                    objLSRdao.insertRejCode(mapRejCode, objLSRDataBean, sdm.getUser());

                    /*
                     * getting Rejection code from mapRejCode(Map object)
                     *
                     */

                    Vector failVect = (Vector) mapRejCode.get("Fail");
                    Vector passVect = (Vector) mapRejCode.get("Pass");
                    Vector manualVect = (Vector) mapRejCode.get("Manal");

                    Log.write("Sizes of the Fail Vector: " + failVect.size() + " Pass: " + passVect.size() + " Manual Vector: " + manualVect.size());

                    String totalRsStr[] = new String[3];
                    totalRsStr[0] = totalReasonCodeStr(rcodeMap, failVect);
                    totalRsStr[1] = totalReasonCodeStr(rcodeMap, manualVect);
                    totalRsStr[2] = totalReasonCodeStr(rcodeMap, passVect);

                    /*
                     * (insertRejDescrpttoLR) getting Rejection code and Manual code and Pass code insert into AUTOMATION_STATUSES_T table
                     * without removing reasoncode sequnce number with method name. for purpose of validation reports
                     *
                     */

                    objLSRdao.insertRejDescrpttoLR(totalRsStr, objLSRDataBean, sdm.getUser());

                    /*
                     * totalReasonCodeValue used for getting reason code information through sequence number.
                     */

                    String reaValues[] = totalReasonCodeValue(rcodeMap, failVect, lsrbaseValdtr);
                    String totalValue[] = new String[8];
                    totalValue[0] = reaValues[0];
                    totalValue[1] = reaValues[1];
                    totalValue[2] = reaValues[2];
                    totalValue[3] = reaValues[3];
                    totalValue[4] = sdm.getUser();
                    totalValue[5] = objLSRDataBean.getReqstVer();
                    totalValue[6] = objLSRDataBean.getReqstNmbr();
                    totalValue[7] = "";

                    String totalMalValue[] = new String[8];
                    totalMalValue[0] = "";
                    totalMalValue[1] = "";
                    totalMalValue[2] = "";
                    totalMalValue[3] = "";

                    if (failVect.size() == 0 &&
                            manualVect.size() == 0 && requestType.equals("SP")) {//don't send to MR if it is a Simple order -- Antony - 04/20/2011
                        //totalMalValue[3] = "PON passed all validations and sent to " +
                          //      "Manual Review as it is a Simple Port";
                    } else {
                        totalMalValue[3] = totalReasonCodeManual(rcodeMap, manualVect);
                    }
                    totalMalValue[4] = sdm.getUser();
                    totalMalValue[5] = objLSRDataBean.getReqstVer();
                    totalMalValue[6] = objLSRDataBean.getReqstNmbr();
                    totalMalValue[7] = "";


                    Log.write("== totalValue= " + totalValue);

                    /*
                     * dispalying AE form we need update AE_T table with rejection codes,
                     */

                    objLSRdao.inserRejectCodeAE_RRC_T(totalValue);

                    /*
                     * dispalying AE form we need update AE_T table with Manual codes,
                     */

                    objLSRdao.manualRejectCodeAE_RRC_T(totalMalValue);

                    /*future only with of SLA time -- dispalying LR form we need update LR_T table,
                    objLSRdao.inserRejectCodeLR_T(totalValue);
                     */

                    /*
                     * in below if condtion will update internal staus,
                     * SP will not go PRE-FOC status.Gtnl size greater than 50
                     * it goes MANUAL-REVIEW
                     */

                    //in case if this PON is a NP Simple Port then hard-code SLA time as 4 hours
                    //Antony - 12/10/2010
                    String spFlag = objLSRdao.retrieveSPFlag(objLSRDataBean.getReqstNmbr(),objLSRDataBean.getReqstVer());
                    String spSLATime = objLSRdao.getSPSLATime(objLSRDataBean.getReqstNmbr(),objLSRDataBean.getReqstVer());

                    Log.write("Checking if NP is a Simple Port and if so proceed to hard-coding SLA time as 4 hours SPFlag :"+spFlag);

                    if (failVect.size() > 0) {

                        if(spSLATime == null)
                            spSLATime = "48";

                        Log.write("value of slatime for SP :"+spSLATime);

                        if(spFlag.equals("Y")) {
                            objLSRdao.insertSLATimerQueueTable(objLSRDataBean, spSLATime);
                        } else {
                            objLSRdao.insertSLATimerQueueTable(objLSRDataBean, objVendorBean.getSLAWaitTime());
                        }
                        objLSRdao.updateStatus("PRE-REJECT", reqNo, reqVer, true);
                    } else if (manualVect.size() > 0 /*|| requestType.equals("SP")*/) {//don't send to MR if it is a SP; should go to PRE-FOC - antony - 04/20/2011
                        objLSRdao.updateStatus("MANUAL-REVIEW", reqNo, reqVer, false);
                    } else {
                        if ((requestType.equals("NPV") || requestType.equals("UNEPV")) && lsrbaseValdtr.getGtnlLenth() > 50) {
                             objLSRdao.updateStatus("MANUAL-REVIEW", reqNo, reqVer, false);
                        } else {
                            if(spFlag.equals("Y")) {
                               objLSRdao.insertSLATimerQueueTable(objLSRDataBean, spSLATime);
                            } else {
                               objLSRdao.insertSLATimerQueueTable(objLSRDataBean, objVendorBean.getSLAWaitTime());
                            }                            
                               objLSRdao.updateStatus("PRE-FOC", reqNo, reqVer, true);                            
                        }
                    }


                }
            } else {
                mapRejCode = new HashMap();
                Vector failVect = new Vector();
                Vector PassVect = new Vector();
                Vector manVec = new Vector();
                mapRejCode.put("Pass", PassVect);
                mapRejCode.put("Fail", failVect);
                mapRejCode.put("Manal", manVec);
            }
            Log.write("=finishing all validation=reqUrl  " + reqUrl);
            alltelRequestDispatcher.forward("/" + reqUrl);
            
            /******* moving these two update methods to the location where CI form video field update is being done - Antony - 01/28/2014*****/
            /******* bug found by Diane Walters during UAT of videotype field -- BB field is not being updated always ********/
            //Log.write("Updating green,broad field updation");
            //objLSRdao.updateGreenfieldReqTable(reqNo, validationData, objLSRDataBean);
            //objLSRdao.updateBroadbandReqTable(reqNo, validationData, objLSRDataBean);
            //Log.write("Updating green,broad field updation done====");
            return;
        } catch (Exception e) {
            ExceptionHandler.handleException("LSRBASE  ", e);
            throw e;
        }
    }


    /*
     *getVendorTableDataForLSR method used for filter VendorTableDataBean
     * from vector through lsr inputs.
     *
     */
    public VendorTableDataBean getVendorTableDataForLSR(Vector vendorBeanVector,
            LSRDataBean lsrDataBean, ValidationDataBean validationData) {
        VendorTableDataBean objVendorBean = null;

        Log.write("LSRBaseValidator getVendorTableDataForLSR =lsrDataBean.getCmpnySeqNmbr()=" + lsrDataBean.getCmpnySeqNmbr() + " lsrDataBean.getOCNcd() " + lsrDataBean.getOCNcd() + " lsrDataBean.getSerRequestType() " + lsrDataBean.getSerRequestType() + " lsrDataBean.getActivity() " + lsrDataBean.getActivity());
        String wcn = validationData.getCustOrgId();
        Log.write("WCN === " + wcn);
        // A/086/AL/068/001/LEED/03/SPVL/205/467

        if (wcn != null) {
            String[] arr = wcn.split("[/]");
            if (arr.length > 2) {
                wcn = arr[3];
            }
        } else {
        }
        Log.write("WCN ==:= " + wcn);
        boolean vendorFlag = false;
        boolean vendorAutoFlag = false;
        String vendorWCN = "";

        for (int i = 0; i < vendorBeanVector.size(); i++) {
            objVendorBean = (VendorTableDataBean) vendorBeanVector.get(i);
            String service = "";
            String act = "";
            if (objVendorBean.getServiceType() != null) {
                int index = objVendorBean.getServiceType().indexOf("-");
                service = objVendorBean.getServiceType().substring(0, index);
            }
            if (objVendorBean.getAtivityType() != null) {
                int index = objVendorBean.getAtivityType().indexOf("-");
                act = objVendorBean.getAtivityType().substring(0, index);
            }
            //Log.write("==service " + service + " act " + act + " objVendorBean.getOCN()" + objVendorBean.getOCN() + "objVendorBean.getStateCode() " + objVendorBean.getStateCode() + " objVendorBean.getCompSqncNumber() " + objVendorBean.getCompSqncNumber() + "WCN:" + objVendorBean.getWCN());
            String autoFlagValue = objVendorBean.getActtypeAutomateFlag() + objVendorBean.getOcnAutomateFlag() + objVendorBean.getSrvtypeAutomateFlag() + objVendorBean.getStateAutomateFlag() + objVendorBean.getVedorAutomateFlag();
            //Log.write("==autoFlagValue " + autoFlagValue);

            vendorWCN = objVendorBean.getWCN();
            //Log.write("VenodrWCN!!!!!!!!!"+vendorWCN);
            vendorAutoFlag = autoFlagValue.trim().equalsIgnoreCase("YYYYY");
            //Log.write("Vendor Auto flag!!!!!!"+vendorAutoFlag);
            vendorFlag = objVendorBean.getCompSqncNumber().equals(lsrDataBean.getCmpnySeqNmbr()) &&
                    objVendorBean.getOCN().equals(lsrDataBean.getOCNcd()) &&
                    objVendorBean.getStateCode().equals(lsrDataBean.getStateCD()) &&
                    service.equals(lsrDataBean.getSerRequestType()) &&
                    act.equals(lsrDataBean.getActivity());
//Log.write("Vendor flag!!!!!!"+vendorFlag);
            String status = validationData.getCustStatus();
            if (!status.equalsIgnoreCase("Invalid")) {
                if (vendorFlag && vendorAutoFlag && vendorWCN.equals(wcn)) {
                    Log.write("=getVendorTableDataForLSR objVendorBean =object " + objVendorBean);
                    return objVendorBean;
                } else if (vendorFlag && vendorAutoFlag) {
                    lsrDataBean.setWcnCheckFlag(wcn);
                } else if (vendorFlag && vendorWCN.equals(wcn) && !vendorAutoFlag) {
                    lsrDataBean.setVendorautoVal("OFF");
                }
            } else if (vendorFlag && vendorAutoFlag) {
                Log.write("=getVendorTableDataForLSR objVendorBean Invalid  " + objVendorBean);
                return objVendorBean;
            } else if (vendorFlag && !vendorAutoFlag) {
                lsrDataBean.setVendorautoVal("OFF");
            }
        }

        return null;

    }

    /*
     * routeToAppValidationFlow method used for redirecting request
     * depands upon on the inputs.
     *
     */
    public LSRBaseValidator routeToAppValidationFlow(LSRDataBean objLSRDataBean,
            ValidationDataBean validationData, VendorTableDataBean objVendorBean, String requestType) {
        Log.write("LSRBaseValidator routeToAppValidationFlow calling requestType" + requestType);
        LSRBaseValidator lsrbaseValdtr =
                null;
        if (requestType.equals("NPV")) {
            lsrbaseValdtr = new NPValidator(objLSRDataBean,
                    objVendorBean, validationData);
        } else if (requestType.equals("DAD")) {
            lsrbaseValdtr = new DirAssDirDISCValidator(objLSRDataBean,
                    objVendorBean, validationData);

        } else if (requestType.equals("UNEPV")) {
            lsrbaseValdtr = new UNEPConverValidator(objLSRDataBean,
                    objVendorBean, validationData);

        } else if (requestType.equals("UNEPD")) {
            lsrbaseValdtr = new UNEPDisValidator(objLSRDataBean,
                    objVendorBean, validationData);

        } else if (requestType.equals("RED")) {
            lsrbaseValdtr = new ResaleDISCValidator(objLSRDataBean,
                    objVendorBean, validationData);

        } else if (requestType.equals("RES") || requestType.equals("UNEPS")) {
            lsrbaseValdtr = new ResaleSusValidator(objLSRDataBean,
                    objVendorBean, validationData);

        } else if (requestType.equals("SP")) {
            lsrbaseValdtr = new SPSRValidator(objLSRDataBean,
                    objVendorBean, validationData);
        }
        return lsrbaseValdtr;
    }

    /*
     * lsrGenericflowValidation method used for invoking Generic
     * validation methods
     *
     */
    public void lsrGenericflowValidation(LSRBaseValidator lsrbaseValdtr, String requestType, String m_strSupTypCd) {

        Log.write("LSRBaseValidator lsrGenericflowValidation calling " + lsrbaseValdtr);
        lsrbaseValdtr.matchesNPA_ATN_OCNState();
        lsrbaseValdtr.matchesNPA_BTN_OCN_State();
        lsrbaseValdtr.isExitPendingOrder();
        if (m_strSupTypCd.equals("0")) {
           lsrbaseValdtr.checkDDD();
        }

        if (requestType.equals("NPV") || requestType.equals("UNEPV")) {
            // if condition by Venkatesh testing for supp 1.            
            //lsrbaseValdtr.matchName_CAMS();
            
            if( m_strSupTypCd != null && m_strSupTypCd.trim().equals("1") ){
                // ignore the name validation  
                Log.write("Ignoring the name validation for supp: " + m_strSupTypCd );
            }
            else{
                lsrbaseValdtr.matchName_CAMS();
            }
        }

        if (!requestType.equals("DAD") && !requestType.equals("SP")) {
            //lsrbaseValdtr.matchName_CAMS();
            lsrbaseValdtr.checkisEXPBlank();
            lsrbaseValdtr.tosBus_Res();
            //lsrbaseValdtr.matchEU_Bill_Address();
        }
        
        if (requestType.equals("NPV") || requestType.equals("UNEPV")) {
            lsrbaseValdtr.matchEU_Bill_Address();
        }
        
        if (!requestType.equals("DAD") && !requestType.equals("SP")) {
            lsrbaseValdtr.matchZIP_CAMS();
        }

        if (!requestType.equals("SP")) {
            lsrbaseValdtr.checkisCHCBlank();
            //lsrbaseValdtr.matchATN_CAMSPilotNumber();//comment this line -- fix for duplicate reject message issue
            lsrbaseValdtr.matchEATN_ATN();
        }

        if (!requestType.equals("SP") && !requestType.equals("NPV")) {

            lsrbaseValdtr.matchATN_CAMSPilotNumber();//this block is  needed as for SP is not needed as per bus flow and
            //for NP we should do pilot tn validation in the initialvalidations call

        }

        if (requestType.equals("UNEPD") ||
                requestType.equals("UNEPV") || requestType.equals("RES") ||
                requestType.equals("RED") || requestType.equals("UNEPS")) {
            lsrbaseValdtr.checkReqTypeRes();

        }

        // few common validation for NP,SPSR,UNC


        if (requestType.equals("NPV") || requestType.equals("UNEPV") || requestType.equals("SP")) {

            //Antony change -- added SP for NNSP check too for Simple ports project - 12/10/2010
            if (requestType.equals("NPV") || requestType.equals("SP")) {//NNSP validation to be done for SP too
                lsrbaseValdtr.matchNNSP_SOA();
            }

            lsrbaseValdtr.checkLock_CAMS();
            lsrbaseValdtr.checkDIDP_Asoc();
            lsrbaseValdtr.isExit_AcessLine();

        }
        if (requestType.equals("NPV") || requestType.equals("UNEPV")) {
            lsrbaseValdtr.matchEach_QuantityTN();
            lsrbaseValdtr.checkProjectfield();
        }
        if (requestType.equals("UNEPD") ||
                requestType.equals("RES") ||
                requestType.equals("RED") || requestType.equals("UNEPS")) {
            lsrbaseValdtr.checkLACT();
            lsrbaseValdtr.isTCOPT();
        }

    }

    /*
     * processInitialValidation method used for invoking Main(Initial)
     * validation methods if it is fail then it will stop the validation.
     *
     */
    public boolean processInitialValidation(LSRBaseValidator lsrbaseValdtr, String requestType, String m_strSupTypCd,String reqNo,String reqVer) {
        
      try {   
        
        Log.write("LSRBase processInitialValidation calling " + lsrbaseValdtr);
        boolean checkFlag = false;
        
        Log.write("LSRBase checkFlag : " + checkFlag);
        
        LSRdao lsrDao = new LSRdao();
        
        //V1 in post-version flow - if one or more TNs are active to a 
        //non-WIN company then this boolean variable will be true - Antony 01/17/2012
        boolean activeTNFound = false;
        int origTNListSize = 0;
        
        //this if block modified as the same steps have to be done for SUPP2 and SUPP3 too - Antony -03/30/2012
        if ((requestType.equals("NPV") || requestType.equals("SP")) && (m_strSupTypCd.equals("2") || m_strSupTypCd.equals("3") || m_strSupTypCd.equals("1"))) {
            
          //start go to loop for cancel sv in SOA
        
         
            LSRDataBean objLSRDataBean = lsrDao.loadLSRData(reqNo,reqVer);
            List tnList = objLSRDataBean.getPortedNBR();  
            origTNListSize = tnList.size();
            
            //call method to check if all SVs are in pending - if true
            //then we should not remove records from NP form for any SV
            boolean allSVsInPendingStatus = lsrbaseValdtr.checkIfAllSVsInPendingStatus(tnList,reqNo,reqVer);
            
            
            for(int i = 0;i < tnList.size();i++) {
                
                String strTN = (String) tnList.get(i);                
            
            Hashtable htResult = new Hashtable();
                //LSRdao lsrDao = new LSRdao();
           
            boolean soaSVStatusValidForCancel = false;

            if(lsrDao.checkSVExistsInSOA(reqNo,reqVer)) {

                   htResult = lsrbaseValdtr.checkSVStatusInSOASUPP1("pending",strTN);

               String strResult = "";
                
               strResult = (String) htResult.get("proceedToV5");
               
               if(strResult != null && strResult.equals("true"))
                   checkFlag = true;
               else if(strResult != null && strResult.equals("false"))
                   checkFlag = false;
               else
                   checkFlag = false;
               
               strResult = (String) htResult.get("skipV6V9");
               
               if(strResult != null && strResult.equals("true"))
                   soaSVStatusValidForCancel = true;
               else if(strResult != null && strResult.equals("false"))
                   soaSVStatusValidForCancel = false;
               else
                   soaSVStatusValidForCancel = false;
               
                   strResult = (String) htResult.get("svStatus");
                   //Code Fix to deprecate Service Detail section removal for non-active Ported Number - Saravanan
                   //If non-active in SOA then remove from NP tab - V3 - post-version flow - Antony - 01/17/2012
                   if(strResult != null && strResult.equals("non-active") && !allSVsInPendingStatus) {
                       //call method to remove this TN's section record from NP_T table
                       //Log.write("Deleting Service Detail section for this non-active Ported Number: "+strTN);
                       //lsrDao.deleteNPSectionForPortedNbr(reqNo,reqVer,strTN);
                       //Log.write("Result of svStatus in SOA for SUPP: "+m_strSupTypCd+" is "+strResult);
                   }    

                   if(strResult != null && strResult.equals("active") && !activeTNFound) {
                       Log.write("Active TN to a non-WIN company found!");
                       activeTNFound = true;
                   }
                   
                   Log.write("Result of svStatus in SOA for SUPP: "+m_strSupTypCd+" is "+strResult);
                   
               Log.write("Result of skipV6V9 in SOA for SUPP: "+m_strSupTypCd+" is "+soaSVStatusValidForCancel);

               Log.write("Result of proceedToV5 in SOA for SUPP: "+m_strSupTypCd+" is "+checkFlag);

                   Log.write("Result of activeTNFound in SOA for SUPP: "+m_strSupTypCd+" is "+activeTNFound);

            } else {//no SV in SOA

               checkFlag = true;

               soaSVStatusValidForCancel = false;

               Log.write("No SV in SOA.Result of skipV6V9 in SOA for SUPP: "+m_strSupTypCd+" is "+soaSVStatusValidForCancel);

               Log.write("No SV in SOA.Result of proceedToV5 in SOA for SUPP: "+m_strSupTypCd+" is "+checkFlag);
            }
            
                boolean soaCancel = false;
                
                //don't cancel SV if all TNs are pending in SOA - FCC mandate - Antony - 04/05/2012
                //but cancel SV even if all TNs are pending in SOA if it is a SUPP1
                if((soaSVStatusValidForCancel && !allSVsInPendingStatus) || (allSVsInPendingStatus && m_strSupTypCd.equals("1"))) {
                    soaCancel = lsrbaseValdtr.cancelSVInSOASUPP1(strTN);
                }

                Log.write("LSRBase after cancelSVInSOA : " + soaCancel);
            }
            //end go to loop for cancel sv in SOA
                      
          //if all TNs are pending in SOA then clear up LR form so that the stored procedure doesnt fail at FOC
          //due to unique constraint error - Antony - 04/04/2012
          
          //fix for LR form cannot be updated issue - Antony - 04/09/2012  
          //if(allSVsInPendingStatus)
            lsrDao.clearLRFormForAllPendingInSOA(reqNo,reqVer);
          
          //if activeTN found then re-direct to post-version flow for SUPP1
          if(activeTNFound)
              checkFlag = false;
            
            if(checkFlag) {
                checkFlag = lsrbaseValdtr.checkDDDForSUPP();
            }
            
            Log.write("LSRBase after checkDDD : " + checkFlag);
            
            
            boolean slaCancelResult = lsrbaseValdtr.cancelPrevVrsnInSLAQueue();
          
            Log.write("Result of SLA queue cancel : "+slaCancelResult);
            
            //if(checkFlag) {//cancel dcris orders for both PRE-REJECT and PRE-FOC regardless of checkFlag true/false
                boolean dcrisOrderCancel = lsrbaseValdtr.sendCancelOrderToDCRISWS();
            //}
            
            Log.write("LSRBase after sendCancelOrderToDCRISWS : " + dcrisOrderCancel);
            
          
          try {
                
                //code change to not to send to postversion flow when all TNs have been activated - Antony - 02/29/2012
                //V6 - post version flow - call method to put PON in FOC and update AE form with message
                //that this is a partial - Antony - 01/24/2012
                if(activeTNFound && !(origTNListSize == lsrDao.getPortedTNs(reqNo,reqVer,"NP_SD_T","NP_SD_PORTEDNBR").size())) {
                    Log.write("calling method to put PON in FOC -> CANCELLED and update AE form - V6 - Post version for SUPP1...");
                    String spResult = lsrDao.updateSUPP1PartialToCancelled(reqNo,reqVer);
              
                    Log.write("Result of updateSUPP1PartialToCancelled : "+spResult);
                }
                
                
          } catch(Exception e) {
                  Log.write("Exception while updating partial PON to CANCELLED : "+e.getMessage());
          }          
           
          //only for SUPP2 and SUPP3 proceed with further Q&V if checkflag is true
          if ((requestType.equals("NPV") || requestType.equals("SP")) && (m_strSupTypCd.equals("2") || m_strSupTypCd.equals("3"))) {
            
                  //move checkSuppDDD here 
            
                  checkFlag = lsrbaseValdtr.checkDDDForSUPP();
                              
                  Log.write("Result of check DDD for SUPP in SOA for SUPP: "+m_strSupTypCd+" is "+checkFlag);
                   
                  if (requestType.equals("NPV") || requestType.equals("UNEPV") ||
                      requestType.equals("SP")) {
                      
                      //proceed to 5072 Q&V only if V4 and V5 were a pass
                      if(checkFlag) {
                        checkFlag = lsrbaseValdtr.checkIsembargoed();//1st validation point
                        Log.write("LSRBase checkIsembargoed " + checkFlag);
                      }
                      if (checkFlag) {
                         checkFlag = lsrbaseValdtr.isStatus();//2nd validation point//for NPV also check for pilot TN validation here
                         Log.write("LSRBase isStatus " + checkFlag);
                      }
                      if (checkFlag && (requestType.equals("NPV") || requestType.equals("SP"))) {
                         checkFlag = lsrbaseValdtr.iseligibleforPortNumber();//3rd validation point
                         Log.write("LSRBase iseligibleforPortNumber " + checkFlag);
                      }//code change for bug 1446 -- switched last two if blocks to be in correct order as per flow.
                      if (checkFlag) {
                         checkFlag = lsrbaseValdtr.isExitOCN_Trait_CAMS();//4th validation point
                         Log.write("LSRBase isExitOCN_Trait_CAMS " + checkFlag);
                      }
                      
                      //this validation was already done in V5 so skip it for SUPP2 and SUPP3 Q&V
                      //but as per LSPAC we should still do this validation on LSR DDD
                      
                      if (checkFlag && (requestType.equals("NPV") || requestType.equals("SP"))) {
                         checkFlag = lsrbaseValdtr.checkDDD();//comment this method invocation as we call checksuppddd instead
                         Log.write("LSRBase checkDDD " + checkFlag);
                      }
                      
                  } else if (requestType.equals("UNEPD") ||
                       requestType.equals("DAD") || requestType.equals("RES") ||
                       requestType.equals("RED") || requestType.equals("UNEPS")) {
                       checkFlag = lsrbaseValdtr.isStatus();
                       Log.write("LSRBase isStatus " + checkFlag);
                       if (checkFlag) {
                          checkFlag = lsrbaseValdtr.matchOCN_Trait_CC();
                          Log.write("LSRBase matchOCN_Trait_CC " + checkFlag);
                       }
                  }
          }
        } else {//for all non-NP version 0 PONs
                   
               if (requestType.equals("NPV") || requestType.equals("UNEPV") ||
                  requestType.equals("SP")) {
                  checkFlag = lsrbaseValdtr.checkIsembargoed();//1st validation point
                  Log.write("LSRBase checkIsembargoed " + checkFlag);
                  if (checkFlag) {
                     checkFlag = lsrbaseValdtr.isStatus();//2nd validation point//for NPV also check for pilot TN validation here
                     Log.write("LSRBase isStatus " + checkFlag);
                  }
                  if (checkFlag && (requestType.equals("NPV") || requestType.equals("SP"))) {
                     checkFlag = lsrbaseValdtr.iseligibleforPortNumber();//3rd validation point
                     Log.write("LSRBase iseligibleforPortNumber " + checkFlag);
                  }//code change for bug 1446 -- switched last two if blocks to be in correct order as per flow.
                  if (checkFlag) {
                     checkFlag = lsrbaseValdtr.isExitOCN_Trait_CAMS();//4th validation point
                     Log.write("LSRBase isExitOCN_Trait_CAMS " + checkFlag);
                  }
                  if (checkFlag && (requestType.equals("NPV") || requestType.equals("SP"))) {
                     checkFlag = lsrbaseValdtr.checkDDD();//comment this method invocation as we call checksuppddd instead
                     Log.write("LSRBase checkDDD " + checkFlag);
                  }
               } else if (requestType.equals("UNEPD") ||
                   requestType.equals("DAD") || requestType.equals("RES") ||
                   requestType.equals("RED") || requestType.equals("UNEPS")) {
                   checkFlag = lsrbaseValdtr.isStatus();
                   Log.write("LSRBase isStatus " + checkFlag);
                   if (checkFlag) {
                      checkFlag = lsrbaseValdtr.matchOCN_Trait_CC();
                      Log.write("LSRBase matchOCN_Trait_CC " + checkFlag);
                   }
               }
               
        }

        Log.write("LSRBase checkFlag  " + checkFlag);

        return checkFlag;
        
      } catch(Exception e) {
            Log.write("Exception while loading LSR data : "+e.getMessage());
      }  
      return false;
    }

    /*
     * processOtherValidation method used for invoking individual
     * validation methods on depands on the input
     *
     */
    public void processOtherValidation(LSRBaseValidator lsrbaseValdtr) {

        Log.write("LSRBaseValidator processOtherValidation calling " + lsrbaseValdtr);
        if (lsrbaseValdtr instanceof NPValidator) {
            NPValidator objNPValidator = (NPValidator) lsrbaseValdtr;
            objNPValidator.matchERLLACT();
            objNPValidator.checkReqTypeRes();
            objNPValidator.checkREQTYP_LNA();

            if(objNPValidator.getDoPasscodeValidation()) {
                objNPValidator.matchPasscode();
                objNPValidator.matchEAN_AN();
            }

            objNPValidator.checkSUPP();
         //Code Change to divert Camprod3 PON to Manual-Review irrespective of other Validations - Saravanan
            //objNPValidator.checkCAMS_TN_CAMPRD3();
        } else if (lsrbaseValdtr instanceof DirAssDirDISCValidator) {
            DirAssDirDISCValidator objDirDISCVal = (DirAssDirDISCValidator) lsrbaseValdtr;
            objDirDISCVal.isWindstreamNativeNumber();
            objDirDISCVal.isRecordType();
            /*objDirDISCVal.matchNameAddressCams();*/
            objDirDISCVal.checkisEXPNotY();
            objDirDISCVal.notEmptyDADT();
            objDirDISCVal.checkREQTYP_LACT_DL();

        } else if (lsrbaseValdtr instanceof UNEPConverValidator) {
            UNEPConverValidator objUNEPCON = (UNEPConverValidator) lsrbaseValdtr;
            objUNEPCON.iseligibleforPortNumber();
            objUNEPCON.matchERLLACT();
            objUNEPCON.checkEUMI();
            //Fix for PI Issue# 53 -- Invalid LACT validation is not needed
            //for UNEP CONV flow
            //objUNEPCON.checkREQTYP_LACT_DL();
            objUNEPCON.checkREQTYP_LNA_PS();
            objUNEPCON.checkREQTYP_LNA_LS();
            objUNEPCON.checkUNEPAsocList();

            if(objUNEPCON.getDoPasscodeValidation()) {
                objUNEPCON.matchPasscode();
                objUNEPCON.matchEAN_AN();
            }

        } else if (lsrbaseValdtr instanceof UNEPDisValidator) {
            UNEPDisValidator objUNEPDis = (UNEPDisValidator) lsrbaseValdtr;
            objUNEPDis.checkREQTYP_LACT_DL();
            objUNEPDis.checkREQTYP_LNA_PS();
            objUNEPDis.checkREQTYP_LNA_LS();
        } else if (lsrbaseValdtr instanceof ResaleDISCValidator) {
            ResaleDISCValidator objResaleDis = (ResaleDISCValidator) lsrbaseValdtr;
            objResaleDis.checkREQTYP_LNA_RS();
            objResaleDis.checkREQTYP_RSform();
            objResaleDis.checkREQTYP_LACT_DL();
        } else if (lsrbaseValdtr instanceof ResaleSusValidator) {
            ResaleSusValidator objResaleSus = (ResaleSusValidator) lsrbaseValdtr;
            objResaleSus.checkREQTYP_LNA_RS();
        } else if (lsrbaseValdtr instanceof SPSRValidator) {
            SPSRValidator objSPSR = (SPSRValidator) lsrbaseValdtr;
            objSPSR.checkCAMS_TGUID_Goto();
            objSPSR.checkGiftBill();
            //below two validations are not required for NP Simple Port - Antony - 12/10/2010
            //Required the CAMPRD3 validation for NP Simple Port - Pramod - 28/08/2017
         //Code Change to divert Camprod3 PON to Manual-Review irrespective of other Validations - Saravanan
            //objSPSR.checkCAMS_TN_CAMPRD3();
            //objSPSR.checkELT();
            objSPSR.matchSPZIP_CAMS();
            objSPSR.checkComplexASOC_Ind();
            objSPSR.checkAsocList();

            if(objSPSR.getDoPasscodeValidation()) {
                objSPSR.matchPasscode();
                //objSPSR.matchEAN_AN(); //not needed for SPSR flow
            }
            
            //added fix for supp value reject for version 0 for Simple orders
            //issue INC8423095 - Antony - 09-08-2011
            objSPSR.checkSUPP();
        }

    }

    public void triggerSLATimer(LSRDataBean data) {
        Log.write("LSRBaseValidator triggerSLATimer calling ");
    }

    public void monitorSLATimer(LSRDataBean data) {
        Log.write("LSRBaseValidator monitorSLATimer calling ");
    }

    /*
     * totalReasonCodeStr method used for concataning RScode and RsDescrption
     *
     */
    public String totalReasonCodeStr(Map rsCodeMap, Vector failRs) {
        Log.write("LSRBaseValidator totalReasonCodeStr calling ");
        String totalRsStr = "";
        String keySeq = "";
        for (int i = 0; i < failRs.size(); i++) {
            keySeq = keySeq + "\n" + (String) failRs.get(i);

        //temp totalRsStr=totalRsStr+rsCodeMap.get(keySeq);
        }
        return keySeq;
    }

    /*
     * totalReasonCodeManual method used for concataning manual RScode and RsDescrption
     *
     */
    public String totalReasonCodeManual(Map rsCodeMap, Vector failRs) {
        Log.write("LSRBaseValidator totalReasonCodeStr calling ");
        String totalRsStr = "";
        String keySeq = "";
        ReasonCodeBean rsBean = null;
        for (int i = 0; i < failRs.size(); i++) {
            keySeq = (String) failRs.get(i);
            if (keySeq != null) {
                String vaSeq[] = keySeq.split("[-]");
                keySeq = vaSeq[0];
                if (keySeq == null) {
                    keySeq = "";
                }
            }
            rsBean = (ReasonCodeBean) rsCodeMap.get(keySeq);
            if (rsBean != null && rsBean.getReaCodeType() != null) {
                totalRsStr = totalRsStr + "\n" + rsBean.getReaCodeDscr();
            }
        }
        return totalRsStr;
    }

    /*
     * totalReasonCodeValue method used for concataning RScode and RsDescrption
     *
     */
    public String[] totalReasonCodeValue(Map rsCodeMap, Vector failRs,
            LSRBaseValidator lsrbaseValdtr) {
        Log.write("totalReasonCodeValue calling " + rsCodeMap + "failRs " + failRs);
        String totalRsStr[] = new String[4];
        totalRsStr[0] = totalRsStr[1] = totalRsStr[2] = totalRsStr[3] = "";
        String keySeq = "";
        ReasonCodeBean rsBean = null;
        for (int i = 0; i < failRs.size(); i++) {
            keySeq = (String) failRs.get(i);
            if (keySeq != null) {
                String vaSeq[] = keySeq.split("[-]");
                keySeq = vaSeq[0];
                if (keySeq == null) {
                    keySeq = "";
                }
            }

            rsBean = (ReasonCodeBean) rsCodeMap.get(keySeq);
            if (rsBean != null && rsBean.getReaCodeType() != null) {
                if (rsBean.getReaCodeType().equals("2A")) {
                    Log.write("totalReasonCodeValue totalRsStr :  " + rsBean.getReaCodeDscr());
                    if (lsrbaseValdtr.getUnepAsocValue() != null && rsBean.getReaCodeDscr().trim().endsWith("xxx")) {
                        totalRsStr[0] = totalRsStr[0] + "\n" + rsBean.getReaCodeDscr().
                                replaceAll("xxx", lsrbaseValdtr.getUnepAsocValue());
                        Log.write("totalReasonCodeValue totalRsStr  if  " + totalRsStr[0]);
                    } else if (lsrbaseValdtr.getGtnlNumbrs() != null && rsBean.getReaCodeDscr().trim().startsWith("Service")) {
                        totalRsStr[0] = totalRsStr[0] + "\n" + rsBean.getReaCodeDscr().
                                replaceAll("x", lsrbaseValdtr.getGtnlNumbrs());
                        Log.write("totalReasonCodeValue totalRsStr else if  " + totalRsStr[0]);
                    } else {
                        Log.write("totalReasonCodeValue totalRsStr else " + totalRsStr[0]);
                        totalRsStr[0] = totalRsStr[0] + "\n" + rsBean.getReaCodeDscr();
                    }
                } else if (rsBean.getReaCodeType().equals("1T")) {
                    totalRsStr[1] = totalRsStr[1] + "\n" + rsBean.getReaCodeDscr();
                } else if (rsBean.getReaCodeType().equals("1X")) {
                    totalRsStr[2] = totalRsStr[2] + "\n" + rsBean.getReaCodeDscr();
                } else if (rsBean.getReaCodeType().equals("1P")) {
                    totalRsStr[3] = totalRsStr[3] + "\n" + rsBean.getReaCodeDscr();
                }
            }
        }
        Log.write("totalReasonCodeValue totalRsStr " + totalRsStr);
        return totalRsStr;
    }

    public int updateExINStatus(LSRdao objLSRdao,
            String user, LSRDataBean lsrBean, boolean rejflag, String aeStr) {

        int result = objLSRdao.updateEx_IN_Status(user, lsrBean, rejflag, aeStr);
        return result;
    }

    protected void populateVariables()
            throws Exception {
        Log.write("LSRBaseValidator populateVariables calling ");
    }
}
