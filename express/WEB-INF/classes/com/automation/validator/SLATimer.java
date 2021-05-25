/*
 * SLATimer.java
 *
 * Created on June 22, 2009, 2:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.validator;
import com.alltel.lsr.common.util.PropertiesManager;
import com.alltel.lsr.common.util.Log;
import com.alltel.lsr.common.util.EmailManager;

import com.automation.bean.LSRDataBean;
import com.automation.dao.LSRdao;
import com.automation.validator.SOAHelper;
import com.automation.bean.VendorTableDataBean;
import com.automation.bw.BWDCRISOrderWebservice;

import com.service.CAMSTrmkUpdate;
import com.service.CAMSTrmkUpdate_Impl;
import com.service.CamsWebLayer;
import com.service.CamsWebLayer_Stub;
import com.windstream.CamsTrmkUpdwebservice.OpCamsTRMKUpdReply;
import com.windstream.expressorder.webservice.ErrorInfo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Calendar;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;



import biz.neustar.www.clearinghouse.SOAPRequestHandler._1_0.*;
import biz.neustar.www.clearinghouse.SOAPResponseHandler._1_0.*;

import org.apache.commons.net.ftp.FTPClient;


import com.windstream.expressorder.webservice.OpSoaActivationCompleteRequest;
import com.windstream.expressorder.webservice.OpCancelOrderRequest;
import com.windstream.expressorder.webservice.OpCancelOrderReply;
import com.windstream.expressorder.webservice.OpSoaActivationCompleteReply;
import com.windstream.expressorder.webservice.OrderData;
import com.windstream.expressorder.webservice.ApplicationInfo;

import webservice.expressorder.windstream.com.ExpressOrderWebLayer;
import webservice.expressorder.windstream.com.ExpressWebService;
import webservice.expressorder.windstream.com.ExpressWebService_Impl;
import webservice.expressorder.windstream.com.ExpressOrderWebLayer_Stub;

import java.util.Hashtable;


import javax.xml.rpc.JAXRPCException;
/**
 *
 * @author Antony Rajan
 */
/**
 * Schedule a task that calls the database function to fetch SLA completed requests once in 20 seconds.
 */

public class SLATimer {
    Timer timer;
    LSRdao lsrDao;
    String returnStr;
    String reqNo;
    String reqVrsn;
    String pon;
    String atn;
    String an;
    String st;
    String serviceType;
    String activityType;
    String custSag;
    String custWCN;
    String dateToday;
    String reqNNSP;
    SOAHelper soaHelper;
    Vector tnList;
    Vector vendorTableVector;
    BWDCRISOrderWebservice bwCreateOrder;
    LSRDataBean lsrDataBean;
    boolean allTasksComplete;
    String suspendFailErrorMsg;
    
    public SLATimer() {
        lsrDao = new LSRdao();
        bwCreateOrder = new BWDCRISOrderWebservice();
        
        try {
            vendorTableVector = lsrDao.loadVendorTable();
        } catch(Exception e) {
            Log.write("Exception thrown :"+e.getMessage());
        }

        try {
            //insert try catch add log messages
            timer = new Timer(true);//set name and make it run as a daemon thread
            
            timer.schedule(new SLATimerTask(),
                           15*1000,        //initial delay of seconds for connection pool to be created -- increase to 1 min
                           60*1000);  //current rate - once every minute
        } catch (IllegalStateException ex) {
            Log.write("Inside IllegalStateException block...: "+ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            Log.write("Inside Exception block...: "+ex.getMessage());
            ex.printStackTrace();
        } catch(Throwable t) {
              //Log message here
              Log.write("Inside catch for Throwable...");
              Log.write("Throwable caught : "+t.getMessage());
              t.printStackTrace();
        }

    }
    
    public void setSOAHelper(SOAHelper soaHelperObj) {
        soaHelper = soaHelperObj;
    }
    
    public void cancelTimer() {
            timer.cancel(); 
            timer = null;

    }

    class SLATimerTask extends TimerTask {
	
       public void run() {
          String strPON = "";
          String strVrsn = "";
          String strOCN = "";
          String atnWithoutHyphens  = "";
          String spFlag = "";
          String strSuppType = "";
          
          Thread.currentThread().setName("SLATIMER");
          
          /*add unit test code here and remove when done - Antony*/
          
                   
          try {
                    Log.write("Calling LSRdao.getSLACompletedRequests() method...");
                    returnStr = lsrDao.getSLACompletedRequests();
                    Log.write("Value returned by SLA Procedure in SLATimer:"+returnStr);
                    Log.write("Finished calling LSRdao.getSLACompletedRequests() method...");

                    if (returnStr != null && returnStr.trim().length() > 5) {
                    StringTokenizer paramList = new StringTokenizer(returnStr,"/");
                                                  
                    reqNo = paramList.nextToken();
                    reqVrsn = paramList.nextToken();
                    pon = paramList.nextToken();
                    atn = paramList.nextToken();
                    an  = paramList.nextToken();
                    st  = paramList.nextToken();
                    serviceType = paramList.nextToken();
                    activityType = paramList.nextToken();
                    custSag = paramList.nextToken();
                    custWCN = paramList.nextToken();
                    dateToday = paramList.nextToken();
                    
                    Log.write("Calling lsrdao getRequestData method...");
                    Hashtable reqData = lsrDao.getRequestData(reqNo,reqVrsn);
                    Log.write("Finished calling lsrdao getRequestData method..."+(String)reqData.get("CUST_TAXJUR"));
                    
                    spFlag = lsrDao.retrieveSPFlag(reqNo,reqVrsn);
                    
                    //if(serviceType.trim().equals("C") && activityType.trim().equals("V") &&(!spFlag.equals("Y")))
                    if(serviceType.trim().equals("C") && activityType.trim().equals("V"))
                        
                    	{
                    	String req_NNSP = paramList.nextToken();
                    	reqNNSP = req_NNSP.toUpperCase();
                    	}
                    else
                        {
                    	reqNNSP = "";
                        }
                   
                    
                    if(!spFlag.equals("Y"))
                        atnWithoutHyphens = atn.replaceAll("-","").trim();
                    
                    Log.write("Req No = "+reqNo);
                    Log.write("Req Version = "+reqVrsn);
                    Log.write("PON = "+pon);
                    Log.write("ATN = "+atn);
                    Log.write("AN = "+an);
                    Log.write("State = "+st);
                    Log.write("Service Type = "+serviceType);
                    Log.write("Activity Type = "+activityType);
                    Log.write("Cust SAG = "+custSag);
                    Log.write("Cust WCN = "+custWCN);
                    Log.write("Date Updated = "+dateToday);
               
                    Log.write("Calling LSRdao.loadLSRDataBean method...");
	            lsrDataBean = lsrDao.loadLSRData(reqNo,reqVrsn);//need to add version later

                    //populate global variables
                    strPON = pon;
                    strVrsn = reqVrsn;
                    strOCN = lsrDataBean.getOCNcd();
                    
                    Log.write("EU Name value :"+lsrDataBean.getEuName());
                    Log.write("Finished calling LSRdao.loadLSRDataBean method...");
                                
                    Log.write("EU Name value :"+lsrDataBean.getCmpnySeqNmbr());
                    VendorTableDataBean vendorDataBean = getVendorTableDataBean(vendorTableVector,lsrDataBean,(String)reqData.get("CUST_WCN"));
                    Log.write("Vendor data :"+vendorDataBean.getBTN());
                    Log.write("Vendor data :"+vendorDataBean.getVendorConfigSqncNumber());
                    
                    Vector vendorAsocVector = new Vector();
                    Vector vendorAsocVectorForSUPP23 = new Vector();
                    
                    if(reqVrsn.equals("0")) {
                        Log.write("Calling test LSRdao.getVendorTableASOCs method...");
                        vendorAsocVector = lsrDao.getVendorTableASOCs(vendorDataBean.getVendorConfigSqncNumber(),reqData);//need to add version later
                        Log.write("Calling test LSRdao.getVendorTableASOCs method...vector size :"+vendorAsocVector.size());
                    } else {
                        //this is a SUPP so we need to send a X-order if there is a supp fee; so call getSuppFeeAsocs method
                        strSuppType = lsrDataBean.getSupplementalType();
                        Log.write("Calling LSRdao.getSuppFeeAsocs method...");
                        vendorAsocVector = lsrDao.getSuppFeeAsocs(vendorDataBean.getVendorConfigSqncNumber(),(String) reqData.get("CUST_TYPE"), strSuppType,lsrDao.getFOCStatus(reqNo,reqVrsn));//need to add version later
                        Log.write("Calling LSRdao.getSuppFeeAsocs method...vector size :"+vendorAsocVector.size());
                        //get Initial conversion fee asocs list for SUPP2 and SUPP3
                        Log.write("Calling test LSRdao.getVendorTableASOCs method for SUPP2 and 3...");
                        vendorAsocVectorForSUPP23 = lsrDao.getVendorTableASOCs(vendorDataBean.getVendorConfigSqncNumber(),reqData);//need to add version later
                        Log.write("Calling test LSRdao.getVendorTableASOCs method for SUPP2 and 3...vector size :"+vendorAsocVectorForSUPP23.size());
                    }
                    
                    try {
                        
                        if (serviceType.trim().equals("C") && activityType.trim().equals("V"))
                            tnList = lsrDao.getPortedTNs(reqNo,reqVrsn,"NP_SD_T","NP_SD_PORTEDNBR");
                        else if(serviceType.trim().equals("E") && (activityType.trim().equals("S") || activityType.trim().equals("D")))
                            tnList = lsrDao.getPortedTNs(reqNo,reqVrsn,"RS_SD_T","RS_SD_TNS");
                        else if(serviceType.trim().equals("M") && (activityType.trim().equals("S") || activityType.trim().equals("D") || activityType.trim().equals("V")))
                            tnList = lsrDao.getPortedTNs(reqNo,reqVrsn,"PS_SD_T","PS_SD_TNS");
                        else if(serviceType.trim().equals("G") || serviceType.trim().equals("H") 
                                                         || serviceType.trim().equals("J")) 
                            tnList = lsrDao.getPortedTNs(reqNo,reqVrsn,"DL_LD_T","DL_LD_LTN");
                                                                       
                        } catch(Exception e) {
                            e.printStackTrace();
                            Log.write("Error in obtaining Ported Number list: "+e);
                        }
                                
                    if((serviceType.trim().equals("E") || serviceType.trim().equals("M")) &&
                       (activityType.trim().equals("S"))) {
                        allTasksComplete = true;
                        suspendFailErrorMsg = "";
                        
                        /*if(custSag.trim().equals("SSI")) {
                            if(st.trim().equals("KY") || st.trim().equals("GA")) {
                                //generate and send file by FTP to K or Y drive
                                allTasksComplete = createSSIFile(st,tnList);
                                
                                if(!allTasksComplete) {
                                    suspendFailErrorMsg = "Failed to send SSI TN file by FTP.";
                                }
                            } else {

                                //send email to SSI mailbox....
                                String strSingleEmail = PropertiesManager.getProperty("lsr.SSIEmail.emailid");

                                for(int i = 0; i < tnList.size(); i++) {

                                    StringBuffer strMessage = new StringBuffer();

                                    atn = tnList.get(i).toString().trim();

                                    strMessage.append("PON: "+pon+"\n");
                                    strMessage.append("ATN: "+atn+"\n");
                                    strMessage.append("Account Number: "+an+"\n");

                                    try
                                    {
                                            Log.write("Sending email....");
                                            //added the word SUSPEND in below line - fix for ISSASOI-38 - Antony - 05/26/2010
                                            EmailManager.send(null, strSingleEmail, "Express SSI Email on PON: "+pon+" SUSPEND", strMessage.toString());
                                    }
                                    catch (Exception e)
                                    {
                                            allTasksComplete = false;
                                            suspendFailErrorMsg = "Failed to send SSI Email";
                                            e.printStackTrace();
                                            Log.write("Failed on EmailManager.send()");
                                    }
                                }
                            } 
                       } else*/ 
                       if(custSag.trim().equals("Architel") || custSag.trim().equals("SSI")) {
                            
                            for(int i = 0; i < tnList.size(); i++) {
                            
                                atnWithoutHyphens = tnList.get(i).toString().replaceAll("-","").trim();
                                                                
                                String architelPOSTReturnStr = lsrDao.sendArchitelPOSTRequest(atnWithoutHyphens.trim());

                                Log.write("Finished calling LSRdao.sendArchitelPOSTRequest() method and" +
                                                   "result = "+architelPOSTReturnStr);

                                //send email to group if successful

                                if(architelPOSTReturnStr.trim().equals("0")) {
                                    
                                    Log.write("Inside Architel POST response 0...");
                                    
                                } else {
                                  Log.write("Inside Architel POST != 0....");
                                  allTasksComplete = false;
                                  suspendFailErrorMsg = "Failed to send Architel POST request";
                                }
                            }     
                            
                       } else {
                            allTasksComplete = false;
                            suspendFailErrorMsg = "Invalid value of CustSAG";                            
                            Log.write("Invalid value of CustSAG");
                       }
                        
                       for(int i = 0; i < tnList.size(); i++) { 
                           // call UPDATE TREATMENT remarks webservice ....
                            
                           //if unep susp then remove pic,ipic and fpi from TN
                           if(tnList.get(i).toString().indexOf("/") > 0) {
                               atn = tnList.get(i).toString();
                               Log.write("Inside TN with / : "+atn);
                               String [] strArray = atn.split("/");
                               atn = strArray[0];
                               Log.write("Inside TN without / : "+atn);
                           } else {
                                atn = tnList.get(i).toString().trim();
                           }    
                           

                           if(updateTreatmentRemarks("EXP-"+pon,an,atn,pon,custWCN,dateToday)) {
                                    Log.write("Successfully updated Treatment Remarks in CAMS");
                           } else {
                                    allTasksComplete = false;
                                    suspendFailErrorMsg = "Failed to update Treatment Remarks in CAMS";                            
                                    Log.write("Failed to update Treatment Remarks in CAMS");
                           }
                       }
                        
                       // call Suspend Request Completion stored procedure ....
                       Log.write("Calling lsrdao suspend completion method...");
                       
                       String suspUpdateResult = "";
                       
                       if(allTasksComplete) {
                        suspUpdateResult = lsrDao.callStatusUpdateProc(reqNo,reqVrsn,"Y",suspendFailErrorMsg);
                       } else {
                        suspUpdateResult = lsrDao.callStatusUpdateProc(reqNo,reqVrsn,"N",suspendFailErrorMsg);
                       }
                       
                       Log.write("Finished calling callStatusUpdateProc method...ressult: "+suspUpdateResult);
                 
                        
                      } else if(serviceType.trim().equals("C") && activityType.trim().equals("V") && reqVrsn.equals("0")) {
                        
                        String soaResult = "";
                        
                        for(int i = 0; i < tnList.size(); i++) {
                            
                            atnWithoutHyphens = tnList.get(i).toString().replaceAll("-","").trim();
                            
                            Log.write("Calling sendPushToSOA method...");
                            
                            Log.write("Before calling processSync...");
                
                            //create SimpleDateFormat object with source string date format "yyyyy.MMMMM.dd GGG hh:mm aaa"
                            //"07-06-2010-121400PM"
                            SimpleDateFormat sdfDateFormat = new SimpleDateFormat("MM-dd-yyyy-hhmmssaaa");
                            SimpleDateFormat sdfTimeFormat = new SimpleDateFormat("-hhmmssaaa");
                            //Create Date object      
                            Date date = new Date();

                            
                            //format the date into the required format
                            String strCurrentDate = sdfDateFormat.format(date);  
                            Log.write("Date value sent:"+strCurrentDate);
                            //String strOldSPDueDate = lsrDataBean.getDesiedDueDate()+sdfTimeFormat.format(date);
                            //fix for time to be hardcoded as 120100AM for the OldSPDueDate for PORT-OUT
                            //fix for setting oldsp due date as the previous day at -080000PM - Antony - 09-26-2011
                            SimpleDateFormat oldSPDateFormat = new SimpleDateFormat("MM-dd-yyyy");
                            Date oldSPDueDate = oldSPDateFormat.parse(lsrDataBean.getDesiedDueDate());
  
                            Log.write("DDD value from LSR form : "+oldSPDueDate.toString());
                            
                            int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
                            
                            oldSPDueDate.setTime(oldSPDueDate.getTime() - MILLIS_IN_DAY);
                            String prevDate = oldSPDateFormat.format(oldSPDueDate);
  
                            Log.write("Previous day of DDD value from LSR form : "+prevDate );
                            
                            String strOldSPDueDate = prevDate+"-080000PM";
                            Log.write("OldSPDueDate value sent:"+strOldSPDueDate);
                            
                            //"http://10.106.95.55:8200/axis/services/SOAPRequestHandler";
                            String soapURL = PropertiesManager.getProperty("lsr.SOA.soapURL");

                            String reqONSP = lsrDao.getWINSPID(st,custWCN);
                            Log.write("ONSP value sent:"+reqONSP);
                            spFlag = lsrDao.retrieveSPFlag(reqNo,reqVrsn);
                            
                            //hardcoding to be removed before moving to production
                            //remove on Friday before handing over code to Andy
                            //reqNNSP = "X113";
                            //reqONSP = "1180";
                            
                            String userid = PropertiesManager.getProperty("lsr.SOA.userid");
                            String passwd = PropertiesManager.getProperty("lsr.SOA.passwd");
                            String domain = PropertiesManager.getProperty("lsr.SOA.domain");
                            
                            //header and body for svreleaserequest
                            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                            "<header>"+
                                               "<Request value=\"SOARequest\"/>"+
                                               "<Subrequest value=\"SvReleaseRequest\"/>"+
                                               "<Supplier value=\"NEUSTAR\"/>"+
                                               "<InterfaceVersion value=\"1_0\"/>"+
                                               "<UserIdentifier value=\""+userid+"\"/>"+
                                               "<UserPassword value=\""+passwd+"\"/>"+
                                               "<CustomerIdentifier value=\""+domain+"\"/>"+
                                               "<InputSource value=\"A\"/>"+
                                            "</header>";


                            //Added code for setting SP Indicator for simple orders - Antony - 4/15/2011
                            
                                                       
                            String body = "";
                                    
                                    
                            if(spFlag != null && spFlag.equals("Y")) {
                                
                                body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                          "<SOAMessage>"+
                                            "<UpstreamToSOA>"+
                                                "<UpstreamToSOAHeader>"+
                                                    "<InitSPID value=\""+reqONSP+"\"/>"+
                                                    "<DateSent value=\""+strCurrentDate+"\"/>"+
                                                    "<Action value=\"submit\"/>"+
                                                "</UpstreamToSOAHeader>"+
                                                "<UpstreamToSOABody>"+
                                                    "<SvReleaseRequest>"+
                                                        "<LnpType value=\"lspp\"/>"+
                                                        "<Subscription>"+
                                                            "<Tn value=\""+tnList.get(i).toString()+"\"/>"+
                                                        "</Subscription>"+
                                                        "<OldSP value=\""+reqONSP+"\"/>"+
                                                        "<NewSP value=\""+reqNNSP+"\"/>"+
                                                        "<OldSPDueDate value=\""+strOldSPDueDate+"\"/>"+
                                                        "<ONSPSimplePortIndicator value=\"1\"/>"+
                                                    "</SvReleaseRequest>"+
                                                "</UpstreamToSOABody>"+
                                            "</UpstreamToSOA>"+
                                          "</SOAMessage>";
                            } else {
                                
                                body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                          "<SOAMessage>"+
                                            "<UpstreamToSOA>"+
                                                "<UpstreamToSOAHeader>"+
                                                    "<InitSPID value=\""+reqONSP+"\"/>"+
                                                    "<DateSent value=\""+strCurrentDate+"\"/>"+
                                                    "<Action value=\"submit\"/>"+
                                                "</UpstreamToSOAHeader>"+
                                                "<UpstreamToSOABody>"+
                                                    "<SvReleaseRequest>"+
                                                        "<LnpType value=\"lspp\"/>"+
                                                        "<Subscription>"+
                                                            "<Tn value=\""+tnList.get(i).toString()+"\"/>"+
                                                        "</Subscription>"+
                                                        "<OldSP value=\""+reqONSP+"\"/>"+
                                                        "<NewSP value=\""+reqNNSP+"\"/>"+
                                                        "<OldSPDueDate value=\""+strOldSPDueDate+"\"/>"+
                                                        "<ONSPSimplePortIndicator value=\"0\"/>"+
                                                    "</SvReleaseRequest>"+
                                                "</UpstreamToSOABody>"+
                                            "</UpstreamToSOA>"+
                                          "</SOAMessage>";
                            }   
                   
                            Log.write("xml request sent to SOA in SLATimer -- header:"+header);
                            Log.write("xml request sent to SOA in SLATimer -- body:"+body);
                            
                            soaResult = processSync(header,body,soapURL,reqNo,reqVrsn,reqONSP);
                            
                           // Sending the PON to Manual-Review as we got error msg from SOA - SaravananB
                            if(soaResult != null && !soaResult.equals("1")) {
                            	lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6002","SOA Error Message.");
                            	Log.write("Sending the PON to Manual-Review");
                                break;
                            }
                            
                            Log.write("Value returned by sendPushToSOA in SLATimer:"+soaResult);
                            Log.write("Finished calling sendPushToSOA method...");
                        }
                        
                        if(soaResult != null && soaResult.equals("1")) {
                            ErrorInfo resultStr;
                            Log.write("Calling sendOOrder method...");

                            resultStr = bwCreateOrder.sendOOrder(atnWithoutHyphens,lsrDataBean,"NP",tnList,reqData,vendorDataBean,vendorAsocVector); //DC for disconnect    

                            Log.write("Value returned by sendOOrder in SLATimer:"+resultStr.toString());
                            Log.write("Finished calling sendOOrder method...");
                            
                            if(resultStr == null) {
                                //call MR status SP with error message "Null Response from BW Order Creation Webservice. Please contact BW team."
                                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6001","Null Response from BW Order Creation Webservice. Please contact BW team.");
                            }
                            
                            if(!resultStr.getErrorID().equals("0000")) {
                                //call MR status change SP with error message resultStr.getErrorMessage()
                                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),resultStr.getErrorID(),resultStr.getErrorMsg());
                            }
                        }
                      } else if(serviceType.trim().equals("C") && activityType.trim().equals("V") && (!reqVrsn.equals("0"))) {
                        
                        //change for FCC NANC update - Antony - 10/19/2011
                        //X - order has to be done for all SUPP types so block moved to beginning
                        //check if cancel fee asoc vector has objects and if so send X-order for cancel fee
                        if(vendorAsocVector.size() != 0) {//no x order if no asoc
                            Log.write("Calling XOrder create method for NP SUPP PON for Cancel Fee...");

                            ErrorInfo cancelFeeResultStr = bwCreateOrder.sendXOrder(lsrDataBean,"NP",vendorDataBean,vendorAsocVector);

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
                        
                        //change for FCC NANC update - Antony - 10/19/2011
                        //cancel existing SV only for SUPP1
                        //call check status in SOA method here; if not canceled send to MR
                        boolean soaSVCanceled = false;
                        boolean prevVerAuto = false;
                       
                        soaSVCanceled = checkSVStatusInSOA();
//                        prevVerAuto = lsrDao.checkPrevVerAuto(lsrDataBean.getReqstNmbr(),lsrDataBean.getReqstVer());
                        
                        
                        
                        if( soaSVCanceled && lsrDataBean.getSupplementalType().equals("1")) {
                        
                            //if supp type = 1 call cancelled stored procedure to send PON to cancelled
                        
                                //call cancelled SP
                                lsrDao.callAutomationStatusUpdateProc(lsrDataBean.getReqstNmbr(),lsrDataBean.getReqstVer());
                        } else if(!soaSVCanceled && lsrDataBean.getSupplementalType().equals("1")){
                            //call MR update stored procedure as SV status in SOA is not canceled
                            Log.write("SV status not canceled in SOA yet.Sending to MR..");
                            lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6002","SV status not canceled in SOA.");
                        } else {
                        
                            if(lsrDataBean.getSupplementalType().equals("2") || lsrDataBean.getSupplementalType().equals("3")) {
                            //change for FCC NANC update - Antony - 10/19/2011
                            //no SV cancel for SUPP2 and SUPP3 we just modify due date in existing SV

                            //lsrDataBean.getSupplementalType() if supplementaltype is 2 or 3
                            //send soa port-out and if successful send dcris I/andO order

                                String soaResult = "";

                                for(int i = 0; i < tnList.size(); i++) {

                                    atnWithoutHyphens = tnList.get(i).toString().replaceAll("-","").trim();

                                    Log.write("Calling sendPushToSOA method...");

                                    Log.write("Before calling processSync...");

                                    //create SimpleDateFormat object with source string date format "yyyyy.MMMMM.dd GGG hh:mm aaa"
                                    //"07-06-2010-121400PM"
                                    SimpleDateFormat sdfDateFormat = new SimpleDateFormat("MM-dd-yyyy-hhmmssaaa");
                                    SimpleDateFormat sdfTimeFormat = new SimpleDateFormat("-hhmmssaaa");
                                    //Create Date object      
                                    Date date = new Date();

                                    //format the date into the required format
                                    String strCurrentDate = sdfDateFormat.format(date);  
                                    Log.write("Date value sent:"+strCurrentDate);
                                    //String strOldSPDueDate = lsrDataBean.getDesiedDueDate()+sdfTimeFormat.format(date);
                                    //fix for time to be hardcoded as 120100AM for the OldSPDueDate for PORT-OUT
                                    
                                    //fix for setting oldsp due date as the previous day at -080000PM - Antony - 09-26-2011
                                    SimpleDateFormat oldSPDateFormat = new SimpleDateFormat("MM-dd-yyyy");
                                    Date oldSPDueDate = oldSPDateFormat.parse(lsrDataBean.getDesiedDueDate());

                                    Log.write("DDD value from LSR form : "+oldSPDueDate.toString());

                                    int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

                                    oldSPDueDate.setTime(oldSPDueDate.getTime() - MILLIS_IN_DAY);
                                    String prevDate = oldSPDateFormat.format(oldSPDueDate);

                                    Log.write("Previous day of DDD value from LSR form : "+prevDate );

                                    String strOldSPDueDate = prevDate+"-080000PM";
                                    
                                    Log.write("OldSPDueDate value sent:"+strOldSPDueDate);

                                    //"http://10.106.95.55:8200/axis/services/SOAPRequestHandler";
                                    String soapURL = PropertiesManager.getProperty("lsr.SOA.soapURL");

                                    String reqONSP = lsrDao.getWINSPID(st,custWCN);
                                    Log.write("ONSP value sent:"+reqONSP);
                                    spFlag = lsrDao.retrieveSPFlag(reqNo,reqVrsn);

                                    //hardcoding to be removed before moving to production
                                    //remove on Friday before handing over code to Andy
                                    //reqNNSP = "X113";
                                    //reqONSP = "1180";

                                    String userid = PropertiesManager.getProperty("lsr.SOA.userid");
                                    String passwd = PropertiesManager.getProperty("lsr.SOA.passwd");
                                    String domain = PropertiesManager.getProperty("lsr.SOA.domain");

                                    //header and body for svmodifyrequest
                                    String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                                    "<header>"+
                                                       "<Request value=\"SOARequest\"/>"+
                                                       "<Subrequest value=\"SvModifyRequest\"/>"+
                                                       "<Supplier value=\"NEUSTAR\"/>"+
                                                       "<InterfaceVersion value=\"1_0\"/>"+
                                                       "<UserIdentifier value=\""+userid+"\"/>"+
                                                       "<UserPassword value=\""+passwd+"\"/>"+
                                                       "<CustomerIdentifier value=\""+domain+"\"/>"+
                                                       "<InputSource value=\"A\"/>"+
                                                    "</header>";


                                    //Added code for setting SP Indicator for simple orders - Antony - 4/15/2011


                                    String body = "";

                                    /* sample svModifyRequest
                                     <?xml version="1.0" encoding="UTF-8"?>
                                        <SOAMessage>
                                           <UpstreamToSOA>
                                                <UpstreamToSOAHeader>
                                                    <InitSPID value="1167"/>
                                                    <DateSent value="10-05-2011-080000AM"/>
                                                    <Action value="submit"/>
                                                </UpstreamToSOAHeader>
                                                <UpstreamToSOABody>
                                                    <SvModifyRequest>
                                                        <Subscription>
                                                            <Tn value="321-267-1111"/>
                                                        </Subscription>
                                                        <DataToModify>
                                                            <OldSPDueDate value="10-16-2011-080000PM"/>
                                                        </DataToModify>
                                                        <SvStatus value="pending"/>
                                                    </SvModifyRequest>
                                                </UpstreamToSOABody>
                                           </UpstreamToSOA>
                                       </SOAMessage>
                                     */

                                    //change for FCC NANC update - Antony - 10/19/2011
                                    //SV modify request does not need simple port indicator so 
                                    //commenting out old code and adding new modifyrequest code
                                    
                                    body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                                  "<SOAMessage>"+
                                                    "<UpstreamToSOA>"+
                                                        "<UpstreamToSOAHeader>"+
                                                            "<InitSPID value=\""+reqONSP+"\"/>"+
                                                            "<DateSent value=\""+strCurrentDate+"\"/>"+
                                                            "<Action value=\"submit\"/>"+
                                                        "</UpstreamToSOAHeader>"+
                                                        "<UpstreamToSOABody>"+
                                                            "<SvModifyRequest>"+
                                                                "<Subscription>"+
                                                                    "<Tn value=\""+tnList.get(i).toString()+"\"/>"+
                                                                "</Subscription>"+
                                                                "<DataToModify>"+
                                                                    "<OldSPDueDate value=\""+strOldSPDueDate+"\"/>"+
                                                                "</DataToModify>"+
                                                                "<SvStatus value=\"pending\"/>"+
                                                            "</SvModifyRequest>"+
                                                        "</UpstreamToSOABody>"+
                                                    "</UpstreamToSOA>"+
                                                  "</SOAMessage>";
                                    
                                    
                                    
                                    Log.write("xml request sent to SOA in SLATimer -- header:"+header);
                                    Log.write("xml request sent to SOA in SLATimer -- body:"+body);

                                    soaResult = processSync(header,body,soapURL,reqNo,reqVrsn,reqONSP);

                                    
                                    if(soaResult != null && soaResult.equals("2")) {//update AE form here -- NANC updates - Antony - 10/19/2011
                                        //2 means SV is in a status other than pending,cancel-pending or conflict so ModifyRequest failed
                                        //so we update AE form with error message but we still process dcris orders
                                        lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6003","SOA Error: "+
                                                "A subscription version must have a status of  (pending) or (conflict) or (cancel-pending) in order to send a modify request by old service provider.");
                                        soaResult = "1";
                                        break;
                                    } else if(soaResult != null && soaResult.equals("3")) {//no need to update AE form here -- NANC updates - Antony - 10/19/2011
                                        //3 means no SV exists in SOA for previous version so we proceed to creating a new SV and then the DCRIS orders if successful
                                        //lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6005","SOA Error: Unable to modify Due Date of SV.");
                                        
                                        //create new SOA SV

                                        //header and body for svreleaserequest
                                        header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                                    "<header>"+
                                                       "<Request value=\"SOARequest\"/>"+
                                                       "<Subrequest value=\"SvReleaseRequest\"/>"+
                                                       "<Supplier value=\"NEUSTAR\"/>"+
                                                       "<InterfaceVersion value=\"1_0\"/>"+
                                                       "<UserIdentifier value=\""+userid+"\"/>"+
                                                       "<UserPassword value=\""+passwd+"\"/>"+
                                                       "<CustomerIdentifier value=\""+domain+"\"/>"+
                                                       "<InputSource value=\"A\"/>"+
                                                    "</header>";

                                        
                                        if(spFlag != null && spFlag.equals("Y")) {

                                            body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                                      "<SOAMessage>"+
                                                        "<UpstreamToSOA>"+
                                                            "<UpstreamToSOAHeader>"+
                                                                "<InitSPID value=\""+reqONSP+"\"/>"+
                                                                "<DateSent value=\""+strCurrentDate+"\"/>"+
                                                                "<Action value=\"submit\"/>"+
                                                            "</UpstreamToSOAHeader>"+
                                                            "<UpstreamToSOABody>"+
                                                                "<SvReleaseRequest>"+
                                                                    "<LnpType value=\"lspp\"/>"+
                                                                    "<Subscription>"+
                                                                        "<Tn value=\""+tnList.get(i).toString()+"\"/>"+
                                                                    "</Subscription>"+
                                                                    "<OldSP value=\""+reqONSP+"\"/>"+
                                                                    "<NewSP value=\""+reqNNSP+"\"/>"+
                                                                    "<OldSPDueDate value=\""+strOldSPDueDate+"\"/>"+
                                                                    "<ONSPSimplePortIndicator value=\"1\"/>"+
                                                                "</SvReleaseRequest>"+
                                                            "</UpstreamToSOABody>"+
                                                        "</UpstreamToSOA>"+
                                                      "</SOAMessage>";
                                        } else {

                                            body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                                      "<SOAMessage>"+
                                                        "<UpstreamToSOA>"+
                                                            "<UpstreamToSOAHeader>"+
                                                                "<InitSPID value=\""+reqONSP+"\"/>"+
                                                                "<DateSent value=\""+strCurrentDate+"\"/>"+
                                                                "<Action value=\"submit\"/>"+
                                                            "</UpstreamToSOAHeader>"+
                                                            "<UpstreamToSOABody>"+
                                                                "<SvReleaseRequest>"+
                                                                    "<LnpType value=\"lspp\"/>"+
                                                                    "<Subscription>"+
                                                                        "<Tn value=\""+tnList.get(i).toString()+"\"/>"+
                                                                    "</Subscription>"+
                                                                    "<OldSP value=\""+reqONSP+"\"/>"+
                                                                    "<NewSP value=\""+reqNNSP+"\"/>"+
                                                                    "<OldSPDueDate value=\""+strOldSPDueDate+"\"/>"+
                                                                    "<ONSPSimplePortIndicator value=\"0\"/>"+
                                                                "</SvReleaseRequest>"+
                                                            "</UpstreamToSOABody>"+
                                                        "</UpstreamToSOA>"+
                                                      "</SOAMessage>";
                                        }   
                                         
                                        Log.write("SUPP2/3 PON: no SV exists for previous version. So proceeding to creating new SV...");
                                        
                                        Log.write("xml request sent to SOA in SLATimer -- header:"+header);
                                        Log.write("xml request sent to SOA in SLATimer -- body:"+body);

                                        soaResult = processSync(header,body,soapURL,reqNo,reqVrsn,reqONSP);

                                        Log.write("SUPP2/3 PON: no SV exists for previous version. Result of processSync: "+soaResult);
                                    } else if(soaResult != null && !soaResult.equals("1")) {//update AE form here -- NANC updates - Antony - 10/19/2011
                                        //any other result other than 1,2,3 -- we update AE form but we still process dcris orders
                                        lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6005","SOA Error: Unable to modify Due Date of SV.");
                                        soaResult = "1";
                                        break;
                                    }
                                    
                                    Log.write("Value returned by sendModifySVToSOA in SLATimer for PON: "+lsrDataBean.getReqstPon()+" SUPP"+lsrDataBean.getSupplementalType()+" :"+soaResult);
                                    Log.write("Finished calling sendModifySVToSOA method...");
                                }

                                //change for FCC NANC update - Antony - 10/19/2011
                                //we still create new dcris orders for SUPP2 and SUPP3 even if 
                                //SOA SV due date modify request fails
                                
                                if(soaResult != null && soaResult.equals("1")) {
                                
                                    ErrorInfo resultStr;
                                    Log.write("Calling sendOOrder method for PON: "+lsrDataBean.getReqstPon()+" SUPP"+lsrDataBean.getSupplementalType());

                                    resultStr = bwCreateOrder.sendOOrder(atnWithoutHyphens,lsrDataBean,"NP",tnList,reqData,vendorDataBean,vendorAsocVectorForSUPP23); //DC for disconnect    

                                    Log.write("Value returned by sendOOrder in SLATimer:"+resultStr.toString());
                                    Log.write("Finished calling sendOOrder method for PON: "+lsrDataBean.getReqstPon()+" SUPP"+lsrDataBean.getSupplementalType());

                                    if(resultStr == null) {
                                        //call MR status SP with error message "Null Response from BW Order Creation Webservice. Please contact BW team."
                                        lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6001","Null Response from BW Order Creation Webservice. Please contact BW team.");
                                    }

                                    if(!resultStr.getErrorID().equals("0000")) {
                                        //call MR status change SP with error message resultStr.getErrorMessage()
                                        lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),resultStr.getErrorID(),resultStr.getErrorMsg());
                                    }
                                
                                } 
                            }
                        } 
                        
                      } else if((serviceType.trim().equals("E") || serviceType.trim().equals("M")) &&
                                (activityType.trim().equals("D"))){
                        
                        ErrorInfo resultStr = new ErrorInfo();
                        Log.write("Calling sendOOrder method...");

                        if(serviceType.trim().equals("E")) {
                            resultStr = bwCreateOrder.sendOOrder(atnWithoutHyphens,lsrDataBean,"RD",tnList,reqData,vendorDataBean,vendorAsocVector); //DC for disconnect    
                        } else if(serviceType.trim().equals("M")) {
                            resultStr = bwCreateOrder.sendOOrder(atnWithoutHyphens,lsrDataBean,"UD",tnList,reqData,vendorDataBean,vendorAsocVector); //DC for disconnect    
                        }

                        Log.write("Value returned by sendOOrder in SLATimer:"+resultStr.toString());
                        Log.write("Finished calling sendOOrder method...");
                        
                        if(resultStr == null) {
                            //call MR status SP with error message "Null Response from BW Order Creation Webservice. Please contact BW team."
                            lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6001","Null Response from BW Order Creation Webservice. Please contact BW team.");
                        }

                        if(!resultStr.getErrorID().equals("0000")) {
                            //call MR status change SP with error message resultStr.getErrorMessage()
                            lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),resultStr.getErrorID(),resultStr.getErrorMsg());
                        }
                        
                      } else if(serviceType.trim().equals("G") || serviceType.trim().equals("H") 
                                                         || serviceType.trim().equals("J")) {
                        ErrorInfo resultStr;
                        
                        Log.write("Calling Dir PON sendOOrder method...");

                        atnWithoutHyphens = atn.replaceAll("-","").trim();
                        
                        if(tnList.size() == 0) {
                            tnList = new Vector();
                            tnList.add(atnWithoutHyphens);
                            Log.write("Setting ATN in tnList array ..."+atnWithoutHyphens);
                        }
                        
                        resultStr = bwCreateOrder.sendOOrder(atnWithoutHyphens,lsrDataBean,"DD",tnList,reqData,vendorDataBean,vendorAsocVector); //DC for disconnect    
                        
                        Log.write("Value returned by sendOOrder in SLATimer:"+resultStr.toString());
                        Log.write("Finished calling sendOOrder method...");
                        
                        if(resultStr == null) {
                            //call MR status SP with error message "Null Response from BW Order Creation Webservice. Please contact BW team."
                            lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6001","Null Response from BW Order Creation Webservice. Please contact BW team.");
                        }

                        if(!resultStr.getErrorID().equals("0000")) {
                            //call MR status change SP with error message resultStr.getErrorMessage()
                            lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),resultStr.getErrorID(),resultStr.getErrorMsg());
                        }
                        
                        if(vendorAsocVector.size() != 0) {//no x order if no asoc
                            Log.write("Calling XOrder create method for DADL PON...");

                            resultStr = bwCreateOrder.sendXOrder(lsrDataBean,"DD",vendorDataBean,vendorAsocVector);

                            Log.write("Value returned by sendXOrder in SLATimer:"+resultStr.toString());
                            Log.write("Finished calling sendXOrder method...");
                            
                            if(resultStr == null) {
                                //call MR status SP with error message "Null Response from BW Order Creation Webservice for X Order. Please contact BW team."
                                //commented as per Christie//lsrDao.callMRStatusUpdateProc(lsrDataBean.getPurchaseON(),lsrDataBean.getPurVerNum(),lsrDataBean.getOCNcd(),"6001","Null Response from BW Order Creation Webservice. Please contact BW team.");
                                
                                Log.write("Null Response from BW Order Creation Webservice. Please contact BW team.");
                            }
                            
                            if(!resultStr.getErrorID().equals("0000")) {
                                //call MR status change SP with error message resultStr.getErrorMessage()
                                //commented as per Christie//lsrDao.callMRStatusUpdateProc(lsrDataBean.getPurchaseON(),lsrDataBean.getPurVerNum(),lsrDataBean.getOCNcd(),resultStr.getErrorID(),resultStr.getErrorMsg());
                                Log.write("Error Response from BW Order Creation Webservice. Please contact BW team."+resultStr.getErrorID()+": "+resultStr.getErrorMsg());
                            }
                        }
                      } else if (serviceType.trim().equals("M") && activityType.trim().equals("V")) {
                        
                            List psAsocList = lsrDataBean.getAsocListPS();
                            String asocCD = "";
                            String howAsocFeeApplies = "PS_ASOC";
                            String asocFeeRate = "0.0";
                            String busResInd = "";
                            Vector asocsVector = new Vector();
                            
                            if(psAsocList.size() > 0) {
                                                        
                                for (int i = 0; i < psAsocList.size(); i++) {
                                    Hashtable asocData = new Hashtable();

                                    asocCD = (String) psAsocList.get(i);

                                    if(asocCD == null) 
                                        asocCD = "";
                                    else
                                        asocCD = asocCD.toUpperCase();
                                    
                                    asocData.put("ASOC_CD",asocCD);
                                    asocData.put("HOW_ASOC_FEE_APPLIES",howAsocFeeApplies);
                                    asocData.put("ASOC_FEE_RATE", asocFeeRate);
                                    asocData.put("BUS_RES_IND", busResInd);

                                    Log.write("PS Asoc table data ASOC_CD: "+asocCD);
                                    
                                    asocsVector.add(asocData);
                                }//for loop psAsocs Vector
                                
                                if(asocsVector.size() > 0) {
                                    ErrorInfo resultStr;
                                    Log.write("Calling sendOOrder method for UNEP Conv PON...");

                                    resultStr = bwCreateOrder.sendOOrder(atnWithoutHyphens,lsrDataBean,"UC",tnList,reqData,vendorDataBean,asocsVector); //DC for disconnect    

                                    Log.write("Value returned by sendOOrder method for UNEP Conv PON in SLATimer:"+resultStr.toString());
                                    Log.write("Finished calling sendOOrder method for UNEP Conv PON ...");
                                    
                                    if(resultStr == null) {
                                        //call MR status SP with error message "Null Response from BW Order Creation Webservice. Please contact BW team."
                                        lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"6001","Null Response from BW Order Creation Webservice. Please contact BW team.");
                                    }

                                    if(!resultStr.getErrorID().equals("0000")) {
                                        //call MR status change SP with error message resultStr.getErrorMessage()
                                        lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),resultStr.getErrorID(),resultStr.getErrorMsg());
                                    }
                                    
                                }//if asocsVector.size -- add else with error message and send to MR stored Proc
                            }//if psAsocs.size
                      }//if UNEP CONV
                          
                }//if returnStr > 5 in length    
          } catch(Exception e) {
                e.printStackTrace();
                Log.write("Exception :"+e.getMessage());
                
                try {
                    lsrDao.callMRStatusUpdateProc(strPON,strVrsn,strOCN,"7001","Internal error in completing FOC operations. Please verify PON.");
                } catch(Exception ex) {
                    Log.write("DB Exception while calling MR status update procedure:"+ex.getMessage());
                }
          } catch(Throwable t) {
              //Log message here
              Log.write("Inside catch for Throwable...");
              Log.write("Throwable caught : "+t.getMessage());
              t.printStackTrace();
              //fix for Throwable issue - Jira 25 - Antony 06162010
              try {
                    lsrDao.callMRStatusUpdateProc(strPON,strVrsn,strOCN,"7001","Internal error in completing FOC operations. Please verify PON.");
              } catch(Exception ex) {
                    Log.write("Throwable error while calling MR status update procedure:"+ex.getMessage());
              }
          }
       }
    
    public boolean updateTreatmentRemarks(String trmtRmksUpdateID,String camsID,String TN,String pon,String repName,String dateToday) {
            boolean updateSuccessful = false;
        
        try {
            String remarks1 = dateToday+"/EXPRESS/SUSPEND/PON "+pon.toUpperCase();
            String remarks2 = "";
            String remarks3 = "";
            
            Log.write("Treatment Remarks sent: "+remarks1);
            
            CAMSTrmkUpdate objCAMSTrmkUpdate = new CAMSTrmkUpdate_Impl();
            
            
            Log.write("objCAMSTrmkUpdate==  " + objCAMSTrmkUpdate);
            CamsWebLayer objCamsWebLayer = objCAMSTrmkUpdate.getCamsWebLayer();
            Log.write("objCamsWebLayer==  " + objCamsWebLayer);

            //code to change Endpoint URL dynamically based on lsr.properties entry for lsr.bwcamstrmkupd.URL
            CamsWebLayer_Stub cwlStub = (CamsWebLayer_Stub)objCamsWebLayer;
            URL urlString = new URL(PropertiesManager.getProperty("lsr.bwcamstrmkupd.URL",""));

            Log.write("TRMK URL prior to dynamic setting : "+cwlStub._getTargetEndpoint());
            cwlStub._setTargetEndpoint(urlString);
            Log.write("TRMK URL after dynamic setting : "+cwlStub._getTargetEndpoint());

            OpCamsTRMKUpdReply objOpCamsTRMKUpdReply = 
                    objCamsWebLayer.opCamsTRMKUpd(trmtRmksUpdateID,camsID, TN, remarks1, remarks2, remarks3);
            Log.write("objOpCamsTRMKUpdReply==  " + objOpCamsTRMKUpdReply);
            if (objOpCamsTRMKUpdReply != null) {
                Log.write("objOpCamsTRMKUpdReply.getCamsID()==" + objOpCamsTRMKUpdReply.getCamsID());
                Log.write(" objOpCamsTRMKUpdReply.getCamsReturnCode()==" + objOpCamsTRMKUpdReply.getCamsReturnCode());
                Log.write("objOpCamsTRMKUpdReply.getCamsReturnDescription()==" + objOpCamsTRMKUpdReply.getCamsReturnDescription());
                Log.write(" objOpCamsTRMKUpdReply.getOid()==" + objOpCamsTRMKUpdReply.getOid());
                Log.write("objOpCamsTRMKUpdReply.getTn()==" + objOpCamsTRMKUpdReply.getTn());
            }
            
            if (objOpCamsTRMKUpdReply.getCamsReturnCode().trim().equals("0000")) {
                updateSuccessful = true;
            }
              
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateSuccessful;
     }
    
    /*
     *getVendorTableDataBean method used to retrieve VendorTableDataBean
     * from vector for given request.
     *
     */
        public VendorTableDataBean getVendorTableDataBean(Vector vendorBeanVector,LSRDataBean lsrData,
                String WCN) {
            VendorTableDataBean objVendorBean = null;

            Log.write("Inside getVendorTableDataBean method in SLATimer...");
            
            for (int i = 0; i < vendorBeanVector.size(); i++) {
                objVendorBean = (VendorTableDataBean) vendorBeanVector.get(i);
                
                String service = "";
                String act = "";
                boolean vendorFlag = false;
                
                if (objVendorBean.getServiceType() != null) {
                    int index = objVendorBean.getServiceType().indexOf("-");
                    service = objVendorBean.getServiceType().substring(0, index);
                }
                if (objVendorBean.getAtivityType() != null) {
                    int index = objVendorBean.getAtivityType().indexOf("-");
                    act = objVendorBean.getAtivityType().substring(0, index);
                }
                
                //Log.write("Service Type: " + service + " Activity Type: " + act + " objVendorBean.getOCN()" + objVendorBean.getOCN() + "objVendorBean.getStateCode() " + objVendorBean.getStateCode() + " objVendorBean.getCompSqncNumber() " + objVendorBean.getCompSqncNumber() + "WCN:" + objVendorBean.getWCN());
                
                vendorFlag = objVendorBean.getCompSqncNumber().equals(lsrData.getCmpnySeqNmbr()) 
                          && objVendorBean.getOCN().equals(lsrData.getOCNcd()) 
                          && objVendorBean.getStateCode().equals(lsrData.getStateCD())
                          && service.equals(lsrData.getSerRequestType())
                          && act.equals(lsrData.getActivity())
                          && objVendorBean.getWCN().equals(WCN);

                if(vendorFlag) {
                    return objVendorBean;
                }
            }
          return objVendorBean;  
        }
    } 
    
    public boolean createSSIFile(String state,Vector tnList) {
        boolean fileSent = false;
        
        String s = "";
        String temp = "";
        String [] strArray;
        int location = 0;
            
        SimpleDateFormat sdf = new SimpleDateFormat ("hhmmss") ; 
        Date date = new Date();
        String currentTime = sdf.format (date) ; 
        
        try {//file location tbc in production
            String ssiSourceFolder = PropertiesManager.getProperty("lsr.SSISource.Folder","");
            String userID = PropertiesManager.getProperty("lsr.SSISource.userID","");
            String password = PropertiesManager.getProperty("lsr.SSISource.password","");
            FileOutputStream fo = new FileOutputStream(ssiSourceFolder + "DL"+currentTime+".txt");
            PrintStream ps = new PrintStream(fo);
            for (int i = 0; i < tnList.size(); ++i) {
                
                if(tnList.get(i).toString().indexOf("/") > 0) {
                    temp = tnList.get(i).toString();
                    Log.write("Inside TN with / : "+temp);
                    strArray = temp.split("/");
                    temp = strArray[0];
                    Log.write("Inside TN without / : "+temp);
                } else {
                    temp = tnList.get(i).toString();
                }
                
                location = temp.indexOf("-");
                s = temp.substring(0, location);
                ps.println("(" + s + ") " + temp.substring(location + 1, temp.length()));
            }
            ps.close();
            fo.close();
            
            //send FTP
            FTPClient client = new FTPClient();
            FileInputStream fis = null;
            
            Log.write("Before connect....");
            
            String kdriveIP = PropertiesManager.getProperty("lsr.SSI.Kdrive","");
            String ydriveIP = PropertiesManager.getProperty("lsr.SSI.Ydrive","");
            
            if(state.trim().equals("KY"))
                client.connect(kdriveIP);//K drive box
            else if(state.trim().equals("GA")) 
                client.connect(ydriveIP);//Y drive box
            
            Log.write("Response after connect :"+client.getReplyString());
            Log.write("Response code after connect :"+client.getReplyCode());
            
            client.login(userID, password);//need to get this from property file
            Log.write("Response after login :"+client.getReplyString());
            
            String filename = "DL"+currentTime+".txt";
            fis = new FileInputStream(ssiSourceFolder + "DL"+currentTime+".txt");
            client.storeFile(filename, fis);
            Log.write("Response after storeFile :"+client.getReplyString());
            
            if(client.getReplyCode()==226)// return code for transfer complete
                fileSent = true;
            
            client.logout();
            fis.close();
            

        } catch (Exception e) {
         Log.write("Exception in file creation : "+e.getMessage() );
        }
        return fileSent;
    }
    
    
/* sample Message Header given by Vinayak
    String[1]: Header XML:

<?xml version="1.0" encoding="UTF-8"?>
<header>
   <Request value="SOARequest"/>
   <Subrequest value="SvQueryRequest"/>
   <Supplier value="NEUSTAR"/>
   <InterfaceVersion value="1_0"/>
   <UserIdentifier value="wndsapi"/>
   <UserPassword value=" wndsapi "/>
   <CustomerIdentifier value="WNDS_E2E"/>
<InputSource value="A"/>
</header>

String[2]: Body XML:

<?xml version="1.0" encoding="UTF-8"?><SOAMessage>
<UpstreamToSOA>
<UpstreamToSOAHeader> 
<InitSPID value="1180 "/>
 <DateSent value="07-06-2010-121400PM"/> 
<Action value="submit"/> 
</UpstreamToSOAHeader>
<UpstreamToSOABody>
 <SvQueryRequest> 
<QueryNPAC> 
<Subscription> 
<Sv>
<Tn value="612-419-1111"/>
</Sv>
</Subscription>
 </QueryNPAC> 
</SvQueryRequest>
 </UpstreamToSOABody>
</UpstreamToSOA>
</SOAMessage>
 */
    //changed method to return String as there are multiple results which are required 
    //for NANC updates - Antony - 10/25/2011
     public String processSync (String soapParam1,String soapParam2, String soapURL, String reqNo, String reqVrsn, String onspSPID ) throws Exception
     {
        String result = "0"; //return false
         
        try
        {
            //SSLUtils.initializeOnce();

            SOAPRequestHandlerServiceLocator serviceLocator = new SOAPRequestHandlerServiceLocator();

            SOAPRequestHandlerSoapBindingStub stub = (SOAPRequestHandlerSoapBindingStub) serviceLocator.getSOAPRequestHandler(new java.net.URL(soapURL));

	    Log.write( "processSync called...");
            Log.write( "contacting server [" + soapURL +"]\n" + 
			"with soapParam1 ["+soapParam1+"]\n" + "and soapParam2 ["+ soapParam2 +"]\n");

            String[] respArray = stub.processSync(soapParam1, soapParam2 );
            Log.write("processSync returned: soapParam1=" + respArray[0] + " =" + respArray[1] );
            
            if(!reqNo.equals("")) {
            
            if(respArray[1].indexOf("RequestStatus value=\"success\"") > 0) {
                result = "1";//return true
            
                String soapSyncResponse = respArray[1]; 
                    
                int startIndex = 0;
                int endIndex = 0;

                String soaStatus = "";
                String dateSent = "";
                String tnInRequest = "";
                String soaRequestId = "";

                startIndex = soapSyncResponse.indexOf("DateSent");
                endIndex = startIndex + 36;
                dateSent = soapSyncResponse.substring(startIndex,endIndex);

                dateSent = dateSent.substring(dateSent.indexOf("\"")+1,dateSent.indexOf("\"")+20);

                Log.write("Value parsed DateSent:"+dateSent);

                startIndex = soapSyncResponse.indexOf("RequestStatus value");
                startIndex = startIndex + 21;
                endIndex = startIndex + 7;
                soaStatus = soapSyncResponse.substring(startIndex,endIndex);

                Log.write("Value parsed RequestStatus :"+soaStatus);

                startIndex = soapSyncResponse.indexOf("Tn value");
                startIndex = startIndex + 10;
                endIndex = startIndex + 12;
                tnInRequest = soapSyncResponse.substring(startIndex,endIndex);

                Log.write("Value parsed Tn value :"+tnInRequest);

                soaRequestId = soapSyncResponse.substring(soapSyncResponse.indexOf("<TnRequestId>"),soapSyncResponse.indexOf("</TnRequestId>"));
                
                Log.write("Value parsed Request ID partial string:"+soaRequestId);
                //sample content for soa Request ID
                /*
                <TnRequestId>
                <Tn value="704-782-4837"/>
                <RequestId value="6842955"/>
                </TnRequestId>
                */
                                
                soaRequestId = soaRequestId.replaceAll("<TnRequestId>","");
                soaRequestId = soaRequestId.replaceAll("<","");
                soaRequestId = soaRequestId.replaceAll("/>","");
    
                Log.write("Value parsed Request Id:"+soaRequestId);
                
                //changed success status to pending which will change to active after listener receives activate notification
                // Antony -- 04/03/2012
                lsrDao.updateSOATXNID(reqNo,reqVrsn,soaRequestId,dateSent,"","","pending",onspSPID,tnInRequest);
            }
            }
                
	    
        }
        catch (java.net.MalformedURLException e)
        {
            e.printStackTrace();        
		      	throw new Exception("Invalid SOAP Service URL: " + e );
        }
        catch (java.rmi.RemoteException e)
        {
            e.printStackTrace();
            String errorString = e.getMessage();
            String soaErrorMessage = errorString.substring(errorString.indexOf("<ruleerror>")+12,errorString.indexOf("</ruleerror>"));
            
            soaErrorMessage = soaErrorMessage.replaceAll("<","");
            soaErrorMessage = soaErrorMessage.replaceAll("/>","");
            
            Log.write("SOA Error Message :"+soaErrorMessage);
            
            SimpleDateFormat sdfDateFormat = new SimpleDateFormat("MM-dd-yyyy-hhmmssaaa");
            //Create Date object      
            Date date = new Date();

            //format the date into the required format
            String strCurrentDate = sdfDateFormat.format(date);  
            Log.write("Date value sent to SOA Error Report:"+strCurrentDate);
                            
            if(soaErrorMessage.indexOf("A subscription version must have a status of  (pending) or (conflict) or (cancel-pending) in order to send a modify request by old service provider.") > 0)
                result = "2";//invalid status for modify request so return 2 to process dcris orders and send PON to FOC
            else if(soaErrorMessage.indexOf("This request type SvModifyRequest is not allowed when the subscription version does not exist.") > 0)
                result = "3";//SV does not exist in SOA for previous version of PON so return 3 to create new SV and process dcris orders and send PON to FOC
            
                
            lsrDao.updateSOATXNID(reqNo,reqVrsn,"",strCurrentDate,"ERROR",soaErrorMessage,"failed",onspSPID,"");
            
            //throw new Exception("SOAP service failed: " + e );
            return result;
        }
        
        return result;
    }
     
    /* method to check SV status in SOA for SUPP Q&V  - Antony - 05/02/2011*/
     
    public boolean checkSVStatusInSOA() 
    {
        boolean result = false; 
        
        //String atn = lsrDataBean.getAccountTelephoneNo(); -- will be blank as atn on LSR form is empty for Simple Orders
        //String lsrONSP = "1180"; //get using state cd,atn using lsrdao method -- todo
        
        List tnList = lsrDataBean.getPortedNBR();
        
        String atn = (String) tnList.get(0);
        
        Log.write("ATN from tnList :"+atn);
        
        LSRdao lsrDao = new LSRdao();
        
        String lsrONSP = lsrDao.getONSP(lsrDataBean.getReqstNmbr(),atn);
        
        Log.write("value of lsrONSP : "+lsrONSP);
        
        String[] respArray = null;
        
      /*//Code Change for Cancelling 	PON with no transaction with Soa in express  - Saravanan
        if(!lsrDao.checkPrevSoaTxn(lsrDataBean.getReqstNmbr()))
        	return true;*/
        	
        
        //Code Change for Dcris Cancel Order not getting cancelled in Express alone - Saravanan
        if(!lsrDao.checkSVExistsInSOA(lsrDataBean.getReqstNmbr(),lsrDataBean.getReqstVer()))
            //return true;
        	return false;

        try
        {
        
          //populate header
          //get SOAP URL
          String userid = PropertiesManager.getProperty("lsr.SOA.userid");
          String passwd = PropertiesManager.getProperty("lsr.SOA.passwd");
          String domain = PropertiesManager.getProperty("lsr.SOA.domain");
          String soapURL = PropertiesManager.getProperty("lsr.SOA.soapURL");

          //header and body for svqueryrequest
          String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                            "<header>"+
                               "<Request value=\"SOARequest\"/>"+
                               "<Subrequest value=\"SvQueryRequest\"/>"+
                               "<Supplier value=\"NEUSTAR\"/>"+
                               "<InterfaceVersion value=\"1_0\"/>"+
                               "<UserIdentifier value=\""+userid+"\"/>"+
                               "<UserPassword value=\""+passwd+"\"/>"+
                               "<CustomerIdentifier value=\""+domain+"\"/>"+
                               "<InputSource value=\"A\"/>"+
                            "</header>";

          
          //SimpleDateFormat sdfDateFormat = new SimpleDateFormat("MM-dd-yyyy-hhmmssaaa");
          SimpleDateFormat sdfDateFormat = new SimpleDateFormat("MM-dd-yyyy-hhmmssaaa");
          //Create Date object      
          Date date = new Date();

          //format the date into the required format
          String strCurrentDate = sdfDateFormat.format(date);  
          Log.write("Date value sent in svQueryRequest xml:"+strCurrentDate);
        
          
          //populate body
          String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                "<SOAMessage>"+
                                    "<UpstreamToSOA>"+
                                        "<UpstreamToSOAHeader>"+
                                            "<InitSPID value=\""+lsrONSP+"\" />"+ //need to retrieve this from db - todo
                                            "<DateSent value=\""+strCurrentDate+"\" />"+
                                            "<Action value=\"submit\" />"+
                                        "</UpstreamToSOAHeader>"+
                                        "<UpstreamToSOABody>"+
                                            "<SvQueryRequest>"+
                                                "<QueryLocal>"+
                                                    "<LnpType value=\"lspp\" />"+
                                                    //"<NewSP value=\"X113\" />"+ -- need not supply NNSP value
                                                    "<Subscription>"+
                                                        "<Sv>"+
                                                            "<Tn value=\""+atn+"\" />"+
                                                        "</Sv>"+
                                                    "</Subscription>"+
                                                "</QueryLocal>"+
                                            "</SvQueryRequest>"+
                                        "</UpstreamToSOABody>"+
                                    "</UpstreamToSOA>"+
                                "</SOAMessage>";

            SOAPRequestHandlerServiceLocator serviceLocator = new SOAPRequestHandlerServiceLocator();

            SOAPRequestHandlerSoapBindingStub stub = (SOAPRequestHandlerSoapBindingStub) serviceLocator.getSOAPRequestHandler(new java.net.URL(soapURL));

	    Log.write( "processSync called...");
            Log.write( "contacting server [" + soapURL +"]\n" + 
			"with soapParam1 ["+header+"]\n" + "and soapParam2 ["+body+"]\n");

            respArray = stub.processSync(header,body);
            Log.write("processSync returned: soapParam1=" + respArray[0] + " =" + respArray[1] );
            
            
            if(respArray[1].indexOf("SOARequestStatus value=\"success\"") > 0) {
                
                String soapSyncResponse = respArray[1]; 
                    
                int startIndex = 0;
                int endIndex = 0;

                String svStatus = "";
                String NNSPValueInSV = "";

                /*
                 *<SvStatus value="active"/>
                 *<OldSP value="1180"/>
                 */
                
                startIndex = soapSyncResponse.indexOf("SvStatus value=");
                startIndex = startIndex + 16;
                endIndex = soapSyncResponse.indexOf("/>",startIndex) - 1;
                
                svStatus = soapSyncResponse.substring(startIndex,endIndex);

                Log.write("svStatus value in query response : "+svStatus);
                
                startIndex = soapSyncResponse.indexOf("NewSP value=");
                startIndex = startIndex + 13;
                endIndex = startIndex + 4;
                
                NNSPValueInSV = soapSyncResponse.substring(startIndex,endIndex);

                Log.write("NNSP value in query response : "+NNSPValueInSV);
                
                Log.write("checkStatus value passed : "+NNSPValueInSV);
                
                
                if(svStatus.equals("canceled") || svStatus.equals("cancel-pending")) {
                     Log.write("SUPP1 PON passed SOA check SV status cancelled validation (V9)!");
                     result = true;

                     //no need to add rejects here need only boolean result
                     //fillterSerTypeRejCode("100040-checkSVStatusInSOACanceled", result, "SP", false); //add reason code sqnc nmbr
                     //fillterSerTypeRejCode("100040-checkSVStatusInSOACanceled", result, "NPV", false); //add reason code sqnc nmbr
                } else {
                    //add manual error code to manual error Array -- "SV Status in SOA not changed to canceled yet."
                    //fillterSerTypeRejCode("100040-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
                    //fillterSerTypeRejCode("100040-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
                    //need to send false here as it should be sent to MR in SLATimer
                    result = false;
                    Log.write("SV status not changed to canceled yet.");
                }
                
            } else {
                result = false;
                Log.write("Obtained unexpected response for svQueryRequest. Unable to check status of SV in SOA.");
            }
            
                
	    
        }
        catch (java.net.MalformedURLException e)
        {
            e.printStackTrace();        
            Log.write("MalformedURLException thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            result = false;
            
            //throw new Exception("Invalid SOAP Service URL: " + e );
        }
        catch (java.rmi.RemoteException e)
        {

            e.printStackTrace();        
            Log.write("RemoteException thrown in checksvstatus method : "+e.getMessage());
	    result = false;
            //throw new Exception("Invalid SOAP Service URL: " + e );
            
            
        }
        catch (Exception e)
        {
            e.printStackTrace();        
            Log.write("Exception thrown in checksvstatus method : "+e.getMessage());
	    result = false;
            //throw new Exception("Invalid SOAP Service URL: " + e );
        }
        
        if(respArray[1].indexOf("SvStatus value=\"pending\"") > 0 || 
           respArray[1].indexOf("SvStatus value=\"disconnect-pending\"") > 0 || 
           respArray[1].indexOf("SvStatus value=\"conflict\"") > 0) {
            
            Log.write("SOA cancel valid status found among multiple SVs");
            return false;
        } 
        
        return result;
    }
}
        