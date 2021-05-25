/*  
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.automation.validator;

import com.alltel.lsr.common.util.PropertiesManager;
import com.verisign.soa.LocalTransactionAdapter;
import com.verisign.soa.LocalTransactionAdapterService;
import com.verisign.soa.LocalTransactionAdapterService_Impl;
import com.verisign.soa.LocalTransactionAdapter_Stub;
import com.verisign.soa.soap.objects.GenericResponse;
import com.verisign.soa.soap.objects.DatedGenericResponse;
import com.verisign.soa.soap.objects.Subscriber;
import com.verisign.soa.soap.objects.SaveSubscriberResponse;
import com.verisign.soa.soap.objects.SubscriberList;
import com.verisign.soa.soap.objects.TnPair;
import com.verisign.soa.soap.objects.TnList;
import com.verisign.soa.soap.objects.TnRecord;
import com.verisign.soa.soap.objects.ShortSubscriber;

import com.verisign.soa.RemoteTransactionAdapter;
import com.verisign.soa.RemoteTransactionAdapterService;
import com.verisign.soa.RemoteTransactionAdapterService_Impl;
import com.verisign.soa.RemoteTransactionAdapter_Stub;
import com.verisign.soa.soap.objects.PushRequest;
import com.verisign.soa.soap.objects.PushResponse;
import com.verisign.soa.soap.objects.StatusChangeCauseCode;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.alltel.lsr.common.util.Log;
import com.alltel.lsr.common.util.EmailManager;

import com.automation.dao.LSRdao;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author antony rajan
 */
public class SOAHelper{
     
    LSRdao lsrDao;
    Hashtable htSOAAdapters;
    
    public void SOAHelper() {
        lsrDao = new LSRdao();
    }
    
    public void initialize() {
        Vector spidList;
        
        Log.write("Inside SOAHelper.initialize method.....");
    
        lsrDao = new LSRdao();
        htSOAAdapters = new Hashtable();
        
        //getSPIDList
        try {
            spidList = lsrDao.getWINSPIDList();
        
            String onspSPID = "";
            String userID = "";
            String password = "";

            Log.write("Inside SOA initialize try block: spidList.size = "+spidList.size());
            
            for(int i = 0; i < spidList.size(); i++) {
                
                Hashtable htonsp = (Hashtable) spidList.get(i);

                if(htonsp != null) {
                    Log.write("ONSP value returned : "+htonsp.get("ONSP"));
                    Log.write("userid value returned : "+htonsp.get("USERID"));
                    Log.write("password value returned : "+htonsp.get("PASSWD"));

                    onspSPID = (String) htonsp.get("ONSP");
                    userID = (String) htonsp.get("USERID");
                    password = (String) htonsp.get("PASSWD");

                    Log.write("Creating LTA object for ONSP: "+onspSPID);

                    LocalTransactionAdapterService lta_impl = new LocalTransactionAdapterService_Impl();

                    LocalTransactionAdapter lta = lta_impl.getLocalTransactionAdapter();

                    //code to change Endpoint URL dynamically based on ONSP for given TN

                    LocalTransactionAdapter_Stub ltaStub = (LocalTransactionAdapter_Stub)lta;
                    String soaIP = PropertiesManager.getProperty("lsr.SOA.IPAddress","");
                    String url = "http://" + soaIP + "/"+onspSPID.trim()+"/services/LocalTransactionAdapter";
   
                    URL urlString = new URL(url);

                    Log.write("Existing address url 1 LTA: "+ltaStub._getTargetEndpoint());
                    ltaStub._setTargetEndpoint(urlString);
                    Log.write("Existing address url 2 LTA: "+ltaStub._getTargetEndpoint());

                    GenericResponse loginResponse = lta.logon(userID.trim(),password.trim());

                    Log.write("Output from SOA for LTA login:"+loginResponse.getStatus());

                    if(loginResponse.getStatus() == 0) {
                        Log.write("LTA object created successfully for ONSP : "+onspSPID);
                        htSOAAdapters.put(onspSPID+"LTA",lta);
                        
                    } else if(loginResponse.getReasonCode().trim().equals("1003")) {//password about to expire - send email
                        
                        Log.write("LTA object created successfully for ONSP : "+onspSPID);
                        htSOAAdapters.put(onspSPID+"LTA",lta);
                        
                        Log.write("SOA API password about to expire for ONSP : "+onspSPID+". Message from SOA : "+loginResponse.getReasonString());

                        //send email
                        String strSingleEmail = PropertiesManager.getProperty("lsr.SOA.passwordexpiry.emailid");

                        StringBuffer strMessage = new StringBuffer();
                        strMessage.append("SOA password about to expire for SPID : "+onspSPID+"\n");
                        strMessage.append(loginResponse.getReasonString()+"\n");

                        try
                        {
                                Log.write("Sending email for SOA password about expire....");
                                EmailManager.send(null, strSingleEmail, "Express Email on SOA password about to expire for SPID: "
                                                                        +onspSPID, strMessage.toString());
                        }
                        catch (Exception e)
                        {
                                e.printStackTrace();
                                Log.write("Error while creating SOA LTA Adapter: Failed on SOA password EmailManager.send()");
                        }
                    } else {
                        Log.write("Error while creating SOA LTA Adapter: Invalid response for SOA LTA Login. Error Message returned : "+loginResponse.getReasonString());
                    }
                    
                    //creating RTA objects and adding to hashtable..
                    
                    Log.write("Creating RTA object for ONSP: "+onspSPID);

                    RemoteTransactionAdapterService rta_impl = new RemoteTransactionAdapterService_Impl();

                    RemoteTransactionAdapter rta = rta_impl.getRemoteTransactionAdapter();

                    //code to change Endpoint URL dynamically based on ONSP for given TN

                    RemoteTransactionAdapter_Stub rtaStub = (RemoteTransactionAdapter_Stub)rta;
                    soaIP = PropertiesManager.getProperty("lsr.SOA.IPAddress","");
                    url = "http://" + soaIP + "/"+onspSPID.trim()+"/services/RemoteTransactionAdapter";
   
                    urlString = new URL(url);

                    Log.write("Existing address url 1 RTA: "+rtaStub._getTargetEndpoint());
                    rtaStub._setTargetEndpoint(urlString);
                    Log.write("Existing address url 2 RTA: "+rtaStub._getTargetEndpoint());

                    loginResponse = rta.logon(userID.trim(),password.trim(),false);

                    Log.write("Output from SOA for RTA login:"+loginResponse.getStatus());

                    if(loginResponse.getStatus() == 0) {
                        Log.write("RTA object created successfully for ONSP : "+onspSPID);
                        htSOAAdapters.put(onspSPID+"RTA",rta);
                        
                    } else {
                        Log.write("Error while creating SOA RTA Adapter: Invalid response for SOA LTA Login. Error Message returned : "+loginResponse.getReasonString());
                    }
                    
                } else {
                    Log.write("Error while creating SOA LTA Adapter: Unable to retrieve SPID list from DB.");
                }//if htonsp not null
            }//for loop
        } catch(Exception e) {
            Log.write("Error while creating SOA LTA Adapter: Exception while retrieving SPID list: "+ e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean closeAllSOAConnections() {
        //logout of all LTA and RTA objects for each SPID
        boolean returnFlag = false;
        
        //getSPIDList and logout of all LTA Adapters in hashtable
        try {
            Vector spidList = lsrDao.getWINSPIDList();
        
            String onspSPID = "";
            
            for(int i = 0; i < spidList.size(); i++) {

                Hashtable htonsp = (Hashtable) spidList.get(i);

                if(htonsp != null) {
                    Log.write("Logging out of SOA LTA/RTA: ONSP value returned : "+htonsp.get("ONSP"));

                    onspSPID = (String) htonsp.get("ONSP");
                    
                    Log.write("Logging out of LTA object for ONSP: "+onspSPID);
                    
                    LocalTransactionAdapter lta = (LocalTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"LTA");
                    
                    if(lta != null) {
                        if(lta.logoff()) {
                            Log.write("Logged out of LTA object successfully for ONSP : "+onspSPID);
                            htSOAAdapters.remove(onspSPID+"LTA");
                            returnFlag = true;
                        } else {//error in LTA logout
                            Log.write("SOA Error: Failed to logout of LTA object for WIN SPID: "+onspSPID);
                        }
                    } else {
                        Log.write("SOA Error: Failed to logout of LTA object for WIN SPID: "+onspSPID+". LTA object is null for this SPID.");
                    }
                    
                    Log.write("Logging out of RTA object for ONSP: "+onspSPID);
                    
                    RemoteTransactionAdapter rta = (RemoteTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"RTA");
                    
                    if(rta != null) {
                        if(rta.logoff()) {
                            Log.write("Logged out of RTA object successfully for ONSP : "+onspSPID);
                            htSOAAdapters.remove(onspSPID+"RTA");
                            returnFlag = true;
                        } else {//error in RTA logout
                            Log.write("SOA Error: Failed to logout of RTA object for WIN SPID: "+onspSPID);
                        }
                    } else {
                        Log.write("SOA Error: Failed to logout of RTA object for WIN SPID: "+onspSPID+". RTA object is null for this SPID.");
                    }
                    
                } else {
                    Log.write("SOA Error: Failed to logout of RTA/LTA object for WIN SPID: "+onspSPID+". WIN SPID hashtable retrieved from db is null.");
                }
            }//for    
        } catch(Exception e)   {
            Log.write("SOA Error: Failed to logout of LTA/RTA object. Exception thrown : "+e.getMessage());
            e.printStackTrace();
        }
        
        return returnFlag;
    }
    
    public boolean checkSOAConnection() {//method to check if SOA LTA/RTA object sessions are active in SOA
        boolean returnFlag = false;
        boolean ltaFlag = false;
        boolean rtaFlag = false;
        String onspSPID = "";
        String onspSPIDKey = "";
        LSRdao lsrDao = new LSRdao();
        Vector spidList;
        Hashtable htSPID;
        String [] tnArray;
        TnList tnList;
        GenericResponse rtaResponse;
        TnPair tnPair;
        try {
                    LocalTransactionAdapter lta;
                    lsrDao = new LSRdao();
                    spidList = lsrDao.getWINSPIDList();
                    
                    for(int i = 0; i < spidList.size(); i++) {
                    
                        htSPID = (Hashtable) spidList.get(i);
                        onspSPID = (String) htSPID.get("ONSP");

                        if (onspSPID == null) {
                            Log.write("Error in determining old WIN SPID value in SOA check connection method.");
                            return false;
                        } else {
                            //just pass SPID to hashtable and get LTA object
                            onspSPIDKey = onspSPID+"LTA";
                            lta = (LocalTransactionAdapter) htSOAAdapters.get(onspSPIDKey);

                            if (lta == null) {
                                Log.write("Error in getting LTA object for WIN SPID in SOA check connection method.");
                                return false;
                            }
                        }

                        tnArray = new String[1];
                        tnArray[0] = "0000000000"; 

                        Log.write("About to call tnspecific list for test..");

                        tnList = lta.getSpecificTnList(tnArray);

                        ltaFlag = true;
                        //added additional log message to show LTA object is active -- antony - 05/26/2010
                        Log.write("LTA object active for SPID :"+onspSPID);
                                            
                        onspSPIDKey = onspSPID+"RTA";
                        RemoteTransactionAdapter rta = (RemoteTransactionAdapter) htSOAAdapters.get(onspSPIDKey);
                        //rtaResponse = rta.getTransactionResponse(00000001);
                        PushResponse pushResponse = new PushResponse();
                        tnPair = new TnPair();
                        tnPair.setStartTn("0000000000");
                        tnPair.setEndTn("0000000000");
                        pushResponse.setPair(tnPair);
                        pushResponse.setOldProvider("test");
                        pushResponse.setNewProvider("test");
                        SimpleDateFormat sdf = new SimpleDateFormat ("MM/dd/yyyy hh:mm:ss") ; 
                        Date date = new Date();
                                        
                        String strDate = sdf.format (date) ; 
                        pushResponse.setAcceptDate(strDate);
                        pushResponse.setLrn("1234567890");
                        rtaResponse = rta.acceptPush(pushResponse); 
                        
                                               
                        if(rtaResponse != null) {
                            rtaFlag = true; 
                            Log.write("RTA Health Check: PushResponse.reasoncode : "+rtaResponse.getReasonCode());
                            Log.write("RTA Health Check: PushResponse.reasonstring : "+rtaResponse.getReasonString());
							//added additional logs for showing RTA created for which SPID -- Antony - 05/26/2010
                            Log.write("RTA object active for SPID :"+onspSPID);
                        }
                            
                    }
                                       
                    
            } catch (Exception e) {
                
                Log.write("Exception while checking SOA LTA/RTA objects are active. Exception thrown : "+e.getMessage());
                e.printStackTrace();
                /*
                Log.write("Trying to re-login for SPID object : "+onspSPIDKey);
                
                //try to login again and add object to hashtable object
                //if Key contains LTA then login into LTA object else RTA object
                if(onspSPIDKey.indexOf("LTA") > 0) {
                    //call method to create LTA object for SPID -- if successful htSOAAdapters.put(lta);ltaFlag = true;
                } else if(onspSPIDKey.indexOf("RTA") > 0) {
                    //call method to create RTA object for SPID -- if successful htSOAAdapters.put(rta);rtaFlag = true;
                } else {//invalid SPID Key
                    Log.write("Invalid value of SPID Key. Neither LTA nor RTA !");
                    return false;
                }
                */
                
                return false;
            } finally {//add finally here to dispose newly created objects here
                htSPID = null;
                tnList = null;
                tnArray = null;
                rtaResponse = null;
                lsrDao = null;
                spidList = null;
            }
        
        if(ltaFlag && rtaFlag)
            returnFlag = true;
        
        
            
        return returnFlag;//send to MR with app error message
    }
    
    public boolean checkNNSPInSOA(String lsrNNSP,String state,String wcn) throws Exception {//this method should pass WCN and state for TN as ip to get ONSP 
        boolean returnFlag = false;
        
        try {
                    LocalTransactionAdapter lta;
                    LSRdao lsrDao = new LSRdao();
                    
                    String onspSPID = lsrDao.getWINSPID(state,wcn);
                                        
                    if (onspSPID == null) {
                        Log.write("Error in determining old WIN SPID value: No WIN SPID found for State : "+state+" and WCN : "+wcn);
                        //return false;
                        throw new Exception("Error in determining old WIN SPID value.");
                    } else {
                        //just pass SPID to hashtable and get LTA object
                        lta = (LocalTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"LTA");
                        
                        if (lta == null) {
                            Log.write("Error in getting LTA object for WIN SPID : "+onspSPID+" and State : "+state+" and wcn : "+wcn);
                            //return false;
                            throw new Exception("Error in getting LTA object for WIN SPID");
                        }
                    }
                                
                    String [] providerNames = lta.getProviderNames();
                    
                    if (providerNames != null && providerNames.length > 0) {
                        for(int i=0; i < providerNames.length; i++) {
                            
                            if (providerNames[i].indexOf(lsrNNSP.trim().toUpperCase()) > 0) {
                                returnFlag = true;
                                Log.write("NNSP valid in SOA: "+providerNames[i]);    
                                break;
                            }
							//removed unwanted log message below -- Antony - 05/26/2010
                            //Log.write("List of Providers from SOA: "+providerNames[i]);   
                        }
                        
                    }
            } catch (Exception e) {
                Log.write("Error in NNSP SOA validation: Exception thrown :"+e);
                throw new Exception("Exception in SOA CheckNNSP method.");
            }
        return returnFlag;//send to MR with app error message
    } 
    
       
    //method to update SOA transaction response data in the soa_txn_response_t table
    
    public void updateTransactionResponse() {
        String soaTxnID = "";
        String onspSPID = "";
        
        try {
                    List soaTxnIDList = lsrDao.retrieveSOATxnIDs();
                    
                    if (soaTxnIDList != null && soaTxnIDList.size() > 0) {
                        for(int i=0; i < soaTxnIDList.size(); i++) {
                            
                            Hashtable htSOAID = (Hashtable) soaTxnIDList.get(i);
                            
                            soaTxnID =(String) htSOAID.get("SOA_TXN_ID");
                            onspSPID =(String) htSOAID.get("ONSP");
                            
                            Log.write("SOA Txn ID from db: "+soaTxnID);    
                            Log.write("ONSP SPID from db: "+onspSPID);    
                            
                            //get rta from hashtable for that ONSP

                            RemoteTransactionAdapter rta;

                            //just pass SPID to hashtable and get RTA object
                            rta = (RemoteTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"RTA");
                            
                            DatedGenericResponse txnResponse = rta.getTransactionResponse(Integer.parseInt(soaTxnID));
                            
                            if(txnResponse != null) {
                            
                                GenericResponse asyncResponse = txnResponse.getResponse();

                                lsrDao.updateSOATXNResponse(soaTxnID,txnResponse.getDate(),
                                                                     asyncResponse.getReasonCode(),
                                                                     asyncResponse.getReasonString(),
                                                                     String.valueOf(asyncResponse.getStatus()),
                                                                     onspSPID);
                            }
                        }
                    }
                    
            } catch (Exception e) {
                Log.write("Error in getting SPID whole string from SOA: Exception thrown :"+e);
   
            }
    }
    
    
    //method to get whole SPID String from SOA DB
    public String getSPIDWholeString(String lsrNNSP,LocalTransactionAdapter lta) {
        String spidString = "";
        
        try {
                    String [] providerNames = lta.getProviderNames();
                    
                    if (providerNames != null && providerNames.length > 0) {
                        for(int i=0; i < providerNames.length; i++) {
                            
                            if (providerNames[i].indexOf(lsrNNSP.trim().toUpperCase()) > 0) {
                                spidString = providerNames[i];
                                Log.write("SPID whole String retrieved from SOA: "+providerNames[i]);    
                                break;
                            }
							//removed unwanted log message below -- Antony - 05/26/2010
                            //Log.write("List of Providers from SOA: "+providerNames[i]);    
                        }
                    }
                    
            } catch (Exception e) {
                Log.write("Error in getting SPID whole string from SOA: Exception thrown :"+e);
   
            }
        return spidString;
    }
       
    public boolean sendPushToSOA(String lsrNNSP, String atn,String desdDueDate,String reqPON,String reqVrsn,String reqOCN,String stateCD,String wcn,String reqNo) {
    
        boolean returnFlag = false;
        boolean loggedOut = false;
        boolean noTNInSOA = false;
        TnPair [] tnPairArray = new TnPair[1];
        String dueDate;
        
        dueDate = desdDueDate.replaceAll("-","/");
        
        try {
                    
                    LocalTransactionAdapter lta;
                    LSRdao lsrDao = new LSRdao();
                    
                    String onspSPID = lsrDao.getWINSPID(stateCD,wcn);
                                        
                    if (onspSPID == null) {
                        Log.write("Error in determining old WIN SPID value: No WIN SPID found for State : "+stateCD+" and WCN : "+wcn);
                        return false;
                    } else {
                        //just pass SPID to hashtable and get LTA object
                        lta = (LocalTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"LTA");
                        
                        if (lta == null) {
                            Log.write("Error in getting LTA object for WIN SPID : "+onspSPID+" and State : "+stateCD+" and wcn : "+wcn);
                            return false;
                        }
                    }
            
                    //code added for getspecifictnlist to determine npac status of TN
                    String [] tnArray = new String[1];
                    tnArray[0] = atn; 
                   
                    Log.write("About to send PUSH for atn: "+atn);
                    
                    TnList tnList = lta.getSpecificTnList(tnArray);
                                    
                    Log.write("Tn list status : "+tnList.getResultType());
                    
                    if(tnList != null) {
                        TnRecord [] tnRecord = tnList.getTns();
                    
                        if(tnRecord.length > 0) {
                            Log.write("Npac state in SOA : "+tnRecord[0].getNpacState());
                            Log.write("Subscriber Name : "+tnRecord[0].getSubscriberName());
                            Log.write("Subscriber TN : "+tnRecord[0].getTn());
                            
                            tnPairArray[0] = new TnPair();
                            
                            tnPairArray[0].setStartTn(tnRecord[0].getTn());
                            tnPairArray[0].setEndTn(tnRecord[0].getTn());
                            String npacState = String.valueOf(tnRecord[0].getNpacState());
                            String subscriberName = tnRecord[0].getSubscriberName();
                            String newServiceProvider = tnRecord[0].getNewProvider();
                            
                            Log.write("tnPairArray start TN : "+tnPairArray[0].getStartTn());
                            Log.write("tnPairArray end TN : "+tnPairArray[0].getEndTn());
                            
                            SubscriberList subscriberList = lta.getSubscriberList(subscriberName,tnPairArray[0],false,false,1);
                            
                            Log.write("ResultType of Subscriber List : "+subscriberList.getResultType());
                            
                            ShortSubscriber [] subscribersArray = subscriberList.getSubscribers();
                            int subscriberId = subscribersArray[0].getSubscriberId();
                            
                            Log.write("Subscriber ID for delete Subscriber: "+subscriberId);
                            
                            if(npacState.equals("0") || npacState.equals("8") 
                             ||npacState.equals("9") || npacState.equals("11")) {
                                //deleteSV -> create new SV -> push
                                //deleteTn -> deleteSV -> create new SV -> push -- as per Heather - bug fix for 287 (UAT5)
                                Log.write("About to Delete TN....for ATN: "+atn);
                                GenericResponse deleteTNResponse = lta.deleteTn(tnArray);
                                
                                Log.write("Delete TN Response ID: "+deleteTNResponse.getId());
                                Log.write("Delete TN Response Reason Code: "+deleteTNResponse.getReasonCode());
                                Log.write("Delete TN Response Reason String: "+deleteTNResponse.getReasonString());
                                Log.write("Delete TN Response Status: "+deleteTNResponse.getStatus());
                                
                                
                                if(deleteTNResponse.getStatus() == 0) {
                                    GenericResponse deleteResponse = lta.deleteSubscriber(subscriberId);
                                    Log.write("Delete Subscriber Response ID: "+deleteResponse.getId());
                                    Log.write("Delete Subscriber Response Reason String: "+deleteResponse.getReasonString());
                                    Log.write("Delete Subscriber Response Reason Code: "+deleteResponse.getReasonCode());
                                    Log.write("Delete Subscriber Response Status: "+deleteResponse.getStatus());

                                    if(deleteResponse.getStatus() == 0) {
                                        Subscriber newSubscriber = new Subscriber();

                                        tnPairArray[0] = new TnPair();
                                        tnPairArray[0].setStartTn(atn);
                                        tnPairArray[0].setEndTn(atn);

                                        String onspProviderString = getSPIDWholeString(onspSPID,lta);
                                        Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                                        newSubscriber.setOriginalProvider(onspProviderString);

                                        String nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                                        Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                                        newSubscriber.setCurrentProvider(nnspProviderString);

                                        newSubscriber.setResultState(0);
                                        newSubscriber.setPairs(tnPairArray);
                                        newSubscriber.setSubscriberName(atn);
                                        newSubscriber.setSubscriberId(0);
                                        newSubscriber.setAddress("");
                                        newSubscriber.setCity("");
                                        newSubscriber.setState("");
                                        newSubscriber.setZip("");
                                        newSubscriber.setPortNeeded(false);

                                        SaveSubscriberResponse subResponse = lta.saveSubscriber(newSubscriber,1,false);

                                        Log.write("SaveSubscriberResponse Status obtained : "+subResponse.getResponse().getStatus());

                                        Log.write("SaveSubscriberResponse obtained subscriber id: "+subResponse.getSubscriberId());

                                        Log.write("SaveSubscriberResponse obtained error message Code: "+subResponse.getResponse().getReasonCode());

                                        Log.write("SaveSubscriberResponse obtained error message String: "+subResponse.getResponse().getReasonString());

                                        if (subResponse.getResponse().getReasonCode().trim().equals("2112")) {
                                            //subscriber already exists so try with a different subscriber name
                                            SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                                            Date date = new Date();

                                            String currentTime = sdf.format (date) ; 

                                            newSubscriber.setSubscriberName(atn+currentTime);

                                            subResponse = lta.saveSubscriber(newSubscriber,1,false);

                                        }

                                        if(subResponse.getResponse().getReasonCode().trim().equals("0")) {
                                            //rta invocation

                                            //get rta from hashtable for that ONSP

                                            RemoteTransactionAdapter rta;

                                            //just pass SPID to hashtable and get RTA object
                                            rta = (RemoteTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"RTA");

                                            if (rta == null) {
                                                Log.write("Error in getting RTA object for WIN SPID : "+onspSPID+" and State : "+stateCD+" and wcn : "+wcn);
                                                return false;
                                            }

                                            PushRequest pushRequest = new PushRequest();

                                            pushRequest.setAuthorization(1);

                                            StatusChangeCauseCode stCode = new StatusChangeCauseCode();
                                            stCode.setCauseCode(0);
                                            stCode.setNeeded(false);

                                            pushRequest.setCauseCode(stCode);

                                            SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                                            Date date = new Date();

                                            String currentTime = sdf.format (date) ; 
                                            currentTime = "00:00:00";

                                            dueDate = dueDate + " "+currentTime;//add hardcoded time

                                            pushRequest.setDate(dueDate);
                                            pushRequest.setLnpType(1);

                                            onspProviderString = getSPIDWholeString(onspSPID,lta);
                                            Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                                            pushRequest.setOldProvider(onspProviderString);


                                            nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                                            Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                                            pushRequest.setNewProvider(nnspProviderString);//call method to get entire ONSP or NNSP string

                                            pushRequest.setPair(tnPairArray[0]);

                                            GenericResponse pushResponse = rta.push(pushRequest);


                                            if(pushResponse.getStatus() == 0) {
                                                Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                                lsrDao.updateSOATXNID(reqNo,reqVrsn,String.valueOf(pushResponse.getId()),"","","","",onspSPID,atn);
                                            } else {
                                                Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                                lsrDao.updateSOATXNID(reqNo,reqVrsn,"ERR","",pushResponse.getReasonCode(),
                                                                      pushResponse.getReasonString(),String.valueOf(pushResponse.getStatus()),onspSPID,atn);
                                            }

                                            Log.write("PushSubscriberResponse Status obtained : "+pushResponse.getStatus());

                                            Log.write("PushSubscriberResponse obtained error message Code: "+pushResponse.getReasonCode());

                                            Log.write("pushSubscriberResponse obtained error message String: "+pushResponse.getReasonString());

                                            //no need to log off
                                            returnFlag = true;

                                        } else {
                                            //send to MR: delete SV failed
                                            Log.write("Save SV failed for TN: "+atn);
                                        }
                                    } else {
                                        //send to MR: Unable to save Subscriber
                                        Log.write("Delete SV failed for TN: "+atn);
                                    }
                                    
                                } else {
                                    Log.write("TN could not be deleted in SOA for TN: "+atn);
                                }
                                returnFlag = true;
                            } else if(npacState.equals("3")) {//pending status
                                //cancel SV->create new SV-> push
                                
                                //get rta from hashtable for that ONSP
                                        
                                RemoteTransactionAdapter rta;

                                //just pass SPID to hashtable and get RTA object
                                rta = (RemoteTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"RTA");

                                if (rta == null) {
                                    Log.write("Error in getting RTA object for WIN SPID : "+onspSPID+" and State : "+stateCD+" and wcn : "+wcn);
                                    return false;
                                }
                                
                                GenericResponse cancelTNResponse = rta.cancel(tnPairArray[0]);
                                
                                Log.write("cancel TN Response Status obtained : "+cancelTNResponse.getStatus());

                                Log.write("cancel TN Response obtained error message Code: "+cancelTNResponse.getReasonCode());

                                Log.write("cancel TN Response obtained error message String: "+cancelTNResponse.getReasonString());

                                try{
                                  Log.write("About to pause for 5 seconds for the status to change to CANCELLED");
                                  //pause processing for 5 seconds
                                  Thread.currentThread().sleep(5000);//sleep for 1000 ms
                                } catch(InterruptedException ie){
                                //If this thread was intrrupted by another thread 
                                    Log.write("Caught Interrupted exception.Cancel status change check Sleep timer interrupted by another thread :"+ie.getMessage());
                                }
                                
                                TnList tnListCheckCancel = lta.getSpecificTnList(tnArray);
                                    
                                Log.write("Tn list to check cancel status : "+tnListCheckCancel.getResultType());
                                
                                if(tnListCheckCancel != null) {
                                    TnRecord [] tnRecordCancelled = tnListCheckCancel.getTns();

                                    if(tnRecordCancelled.length > 0) {
                                        Log.write("Npac state in SOA for Cancelled TN:"+atn+" : "+tnRecordCancelled[0].getNpacState());
                                        
                                        if(tnRecordCancelled[0].getNpacState() == 9) {//if TN status is Cancelled
                                            
                                            //first delete and then create new SV 
                                            
                                            //deleteSV -> create new SV -> push
                                            GenericResponse deleteResponse = lta.deleteSubscriber(subscriberId);
                                            Log.write("Delete Response ID: "+deleteResponse.getId());
                                            Log.write("Delete Response Reason String: "+deleteResponse.getReasonString());
                                            Log.write("Delete Response Reason Code: "+deleteResponse.getReasonCode());
                                            Log.write("Delete Response Status: "+deleteResponse.getStatus());

                                            if(deleteResponse.getStatus() == 0) {
                                                
                                                Subscriber newSubscriber = new Subscriber();


                                                String onspProviderString = getSPIDWholeString(onspSPID,lta);
                                                Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                                                newSubscriber.setOriginalProvider(onspProviderString);

                                                String nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                                                Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                                                newSubscriber.setCurrentProvider(nnspProviderString);
                                    

                                                newSubscriber.setResultState(0);
                                                newSubscriber.setPairs(tnPairArray);
                                                newSubscriber.setSubscriberName(atn);
                                                newSubscriber.setSubscriberId(0);
                                                newSubscriber.setAddress("");
                                                newSubscriber.setCity("");
                                                newSubscriber.setState("");
                                                newSubscriber.setZip("");
                                                newSubscriber.setPortNeeded(false);

                                                SaveSubscriberResponse subResponse = lta.saveSubscriber(newSubscriber,1,false);

                                                Log.write("SaveSubscriberResponse Status obtained : "+subResponse.getResponse().getStatus());

                                                Log.write("SaveSubscriberResponse obtained subscriber id: "+subResponse.getSubscriberId());

                                                Log.write("SaveSubscriberResponse obtained error message Code: "+subResponse.getResponse().getReasonCode());

                                                Log.write("SaveSubscriberResponse obtained error message String: "+subResponse.getResponse().getReasonString());

                                                if (subResponse.getResponse().getReasonCode().trim().equals("2112")) {
                                                    //subscriber already exists so try with a different subscriber name
                                                    SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                                                    Date date = new Date();

                                                    String currentTime = sdf.format (date) ; 

                                                    newSubscriber.setSubscriberName(atn+currentTime);

                                                    subResponse = lta.saveSubscriber(newSubscriber,1,false);

                                                }
                                                
                                                if(subResponse.getResponse().getReasonCode().trim().equals("0")) {

                                                    PushRequest pushRequest = new PushRequest();

                                                    pushRequest.setAuthorization(1);

                                                    StatusChangeCauseCode stCode = new StatusChangeCauseCode();
                                                    stCode.setCauseCode(0);
                                                    stCode.setNeeded(false);

                                                    pushRequest.setCauseCode(stCode);

                                                    SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                                                    Date date = new Date();
                                                    String currentTime = sdf.format (date) ; 
                                                    currentTime = "00:00:00";

                                                    dueDate = dueDate + " "+currentTime;//add hardcoded time

                                                    pushRequest.setDate(dueDate);
                                                    pushRequest.setLnpType(1);
                                                    
                                                    onspProviderString = getSPIDWholeString(onspSPID,lta);
                                                    Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                                                    pushRequest.setOldProvider(onspProviderString);


                                                    nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                                                    Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                                                    pushRequest.setNewProvider(nnspProviderString);//call method to get entire ONSP or NNSP string
                                                    
                                                    pushRequest.setPair(tnPairArray[0]);

                                                    GenericResponse pushResponse = rta.push(pushRequest);

                                                    if(pushResponse.getStatus() == 0) {
                                                        Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                                        lsrDao.updateSOATXNID(reqNo,reqVrsn,String.valueOf(pushResponse.getId()),"","","","",onspSPID,atn);
                                                    } else {
                                                        Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                                        lsrDao.updateSOATXNID(reqNo,reqVrsn,"ERR","",pushResponse.getReasonCode(),
                                                                              pushResponse.getReasonString(),String.valueOf(pushResponse.getStatus()),onspSPID,atn);
                                                    }
                                                    
                                                    Log.write("PushSubscriberResponse Status obtained : "+pushResponse.getStatus());

                                                    Log.write("PushSubscriberResponse obtained error message Code: "+pushResponse.getReasonCode());

                                                    Log.write("pushSubscriberResponse obtained error message String: "+pushResponse.getReasonString());

                                                    
                                                } else {//if subscriber response
                                                    Log.write("Save Subscriber failed for TN: "+atn);
                                                }
                                            } else {//if SV delete not successful
                                                Log.write("SV could not be deleted for TN: "+atn);
                                            }
                                        } else {//if status is not changed to cancelled
                                            Log.write("TN Status not changed to 'Cancelled' for TN: "+atn);
                                        }
                                    } else {
                                        Log.write("Unable to check Cancelled Status for TN: "+atn);
                                    }
                                }
                                returnFlag = true;
                            } else if(npacState.equals("4")||npacState.equals("1")) {
                                if(npacState.equals("4"))
                                    Log.write("NPAC Status: Sending. No action taken");
                                else
                                    Log.write("NPAC Status: Conflict. No action taken");
                            
                                returnFlag = true;
                            } else if(npacState.equals("2")) {//Active Status
                                Log.write("NPAC Status: Active. About to check if NSP is Windstream SPID..");
                                Log.write("New Service Provider for TN: "+newServiceProvider);
                                
                                if((newServiceProvider.indexOf("1902") >= 0) ||
                                   (newServiceProvider.indexOf("1900") >= 0) ||
                                   (newServiceProvider.indexOf("0474") >= 0) ||
                                   (newServiceProvider.indexOf("7002") >= 0) ||
                                   (newServiceProvider.indexOf("1180") >= 0) ||
                                   (newServiceProvider.indexOf("1997") >= 0) ||
                                   (newServiceProvider.indexOf("2097") >= 0) ||
                                   (newServiceProvider.indexOf("7708") >= 0) ||
                                   (newServiceProvider.indexOf("4114") >= 0) ||
                                   (newServiceProvider.indexOf("2147") >= 0) ||
                                   (newServiceProvider.indexOf("7815") >= 0)) {//add here -- change to later to get from db
                                    //deleteTN (changed for bug 549) -> deleteSV -> create new SV -> push
                                    
                                    Log.write("About to Delete TN....for ATN: "+atn);
                                    GenericResponse deleteTNResponse = lta.deleteTn(tnArray);

                                    Log.write("Delete TN Response ID: "+deleteTNResponse.getId());
                                    Log.write("Delete TN Response Reason Code: "+deleteTNResponse.getReasonCode());
                                    Log.write("Delete TN Response Reason String: "+deleteTNResponse.getReasonString());
                                    Log.write("Delete TN Response Status: "+deleteTNResponse.getStatus());
                                
                                
                                    if(deleteTNResponse.getStatus() == 0) {                                    
                                    
                                    GenericResponse deleteResponse = lta.deleteSubscriber(subscriberId);
                                    Log.write("Delete Response ID: "+deleteResponse.getId());
                                    Log.write("Delete Response Reason String: "+deleteResponse.getReasonString());
                                    Log.write("Delete Response Reason Code: "+deleteResponse.getReasonCode());
                                    Log.write("Delete Response Status: "+deleteResponse.getStatus());

                                    if(deleteResponse.getStatus() == 0) {
                                        Subscriber newSubscriber = new Subscriber();

                                        tnPairArray[0] = new TnPair();
                                        tnPairArray[0].setStartTn(atn);
                                        tnPairArray[0].setEndTn(atn);

                                        String onspProviderString = getSPIDWholeString(onspSPID,lta);
                                        Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                                        newSubscriber.setOriginalProvider(onspProviderString);

                                        String nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                                        Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                                        newSubscriber.setCurrentProvider(nnspProviderString);
                                    
                                        newSubscriber.setResultState(0);
                                        newSubscriber.setPairs(tnPairArray);
                                        newSubscriber.setSubscriberName(atn);
                                        newSubscriber.setSubscriberId(0);
                                        newSubscriber.setAddress("");
                                        newSubscriber.setCity("");
                                        newSubscriber.setState("");
                                        newSubscriber.setZip("");
                                        newSubscriber.setPortNeeded(false);

                                        SaveSubscriberResponse subResponse = lta.saveSubscriber(newSubscriber,1,false);

                                        Log.write("SaveSubscriberResponse Status obtained : "+subResponse.getResponse().getStatus());

                                        Log.write("SaveSubscriberResponse obtained subscriber id: "+subResponse.getSubscriberId());

                                        Log.write("SaveSubscriberResponse obtained error message Code: "+subResponse.getResponse().getReasonCode());

                                        Log.write("SaveSubscriberResponse obtained error message String: "+subResponse.getResponse().getReasonString());

                                        if (subResponse.getResponse().getReasonCode().trim().equals("2112")) {
                                            //subscriber already exists so try with a different subscriber name
                                            SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                                            Date date = new Date();

                                            String currentTime = sdf.format (date) ; 

                                            newSubscriber.setSubscriberName(atn+currentTime);

                                            subResponse = lta.saveSubscriber(newSubscriber,1,false);

                                        }
                                        
                                        
                                        if(subResponse.getResponse().getReasonCode().trim().equals("0")) {
                                                                                        
                                            //get rta from hashtable for that ONSP
                                        
                                            RemoteTransactionAdapter rta;

                                            //just pass SPID to hashtable and get RTA object
                                            rta = (RemoteTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"RTA");

                                            if (rta == null) {
                                                Log.write("Error in getting RTA object for WIN SPID : "+onspSPID+" and State : "+stateCD+" and wcn : "+wcn);
                                                return false;
                                            }

                                            PushRequest pushRequest = new PushRequest();

                                            pushRequest.setAuthorization(1);

                                            StatusChangeCauseCode stCode = new StatusChangeCauseCode();
                                            stCode.setCauseCode(0);
                                            stCode.setNeeded(false);

                                            pushRequest.setCauseCode(stCode);

                                            SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                                            Date date = new Date();
                                            String currentTime = sdf.format (date) ; 
                                            currentTime = "00:00:00";
                                   
                                            dueDate = dueDate + " "+currentTime;//add hardcoded time

                                            pushRequest.setDate(dueDate);
                                            pushRequest.setLnpType(1);

                                            onspProviderString = getSPIDWholeString(onspSPID,lta);
                                            Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                                            pushRequest.setOldProvider(onspProviderString);


                                            nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                                            Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                                            pushRequest.setNewProvider(nnspProviderString);//call method to get entire ONSP or NNSP string
                                                    
                                            pushRequest.setPair(tnPairArray[0]);

                                            GenericResponse pushResponse = rta.push(pushRequest);

                                            if(pushResponse.getStatus() == 0) {
                                                Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                                lsrDao.updateSOATXNID(reqNo,reqVrsn,String.valueOf(pushResponse.getId()),"","","","",onspSPID,atn);
                                            } else {
                                                Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                                lsrDao.updateSOATXNID(reqNo,reqVrsn,"ERR","",pushResponse.getReasonCode(),
                                                                      pushResponse.getReasonString(),String.valueOf(pushResponse.getStatus()),onspSPID,atn);
                                            }
                                            
                                            Log.write("PushSubscriberResponse Status obtained : "+pushResponse.getStatus());

                                            Log.write("PushSubscriberResponse obtained error message Code: "+pushResponse.getReasonCode());

                                            Log.write("pushSubscriberResponse obtained error message String: "+pushResponse.getReasonString());

                                            if(pushResponse.getStatus() == 0)
                                                returnFlag = true;
                                            else {
                                                //call Manual Review function with error message
                                                // "TN Active in SOA.  Unable to create SV"
                                                lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5001","TN Active in SOA.  Unable to create SV");
                                            }

                                        } else {//saveSubscriber
                                            //saveSubscriber failed
                                            Log.write("Save Subscriber failed for TN: "+atn);
                                            lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5001","TN Active in SOA.  Unable to create SV");
                                        }
                                    } else {//delete SV
                                        //delete SV failed
                                        Log.write("Delete SV failed for TN: "+atn);
                                        lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5001","TN Active in SOA.  Unable to delete SV");
                                    }
                                    } else {
                                        //delete TN failed
                                        Log.write("Delete TN failed for TN: "+atn);
                                        lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5001","TN Active in SOA.  Unable to delete TN");
                                    }
                                } else {//if indexOf WIN SPID
                                    //call Manual Review function with error message "TN not eligible to port."
                                    lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5002","TN not eligible to port");
                                }
                            } else if(npacState.equals("10")) {//cancel pending status
                                // Accept cancel -> check status is cancelled -> if cancelled -> delete SV -> 
                                //create new SV-> push
                                
                                //get rta from hashtable for that ONSP
                                        
                                RemoteTransactionAdapter rta;

                                //just pass SPID to hashtable and get RTA object
                                rta = (RemoteTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"RTA");

                                if (rta == null) {
                                    Log.write("Error in getting RTA object for WIN SPID : "+onspSPID+" and State : "+stateCD+" and wcn : "+wcn);
                                    return false;
                                }
                                
                                GenericResponse acceptCancelTNResponse = rta.acceptCancel(tnPairArray[0],0);
                                
                                Log.write("cancel TN Response Status obtained : "+acceptCancelTNResponse.getStatus());

                                Log.write("cancel TN Response obtained error message Code: "+acceptCancelTNResponse.getReasonCode());

                                Log.write("cancel TN Response obtained error message String: "+acceptCancelTNResponse.getReasonString());
                                
                                try{
                                  Log.write("About to pause for 5 seconds for the status to change to CANCELLED");
                                  //pause processing for 5 seconds
                                  Thread.currentThread().sleep(5000);//sleep for 1000 ms
                                } catch(InterruptedException ie){
                                  //If this thread was intrrupted by another thread 
                                  Log.write("Caught Interrupted exception.Cancel status change check Sleep timer interrupted by another thread :"+ie.getMessage());
                                }

                                TnList tnListCheckCancel = lta.getSpecificTnList(tnArray);
                                    
                                Log.write("Tn list to check cancel status : "+tnListCheckCancel.getResultType());
                    
                                if(tnListCheckCancel != null) {
                                    TnRecord [] tnRecordCancelled = tnListCheckCancel.getTns();

                                    if(tnRecordCancelled.length > 0) {
                                        Log.write("Npac state in SOA for Cancel Accepted TN:"+atn+" : "+tnRecordCancelled[0].getNpacState());
                                        
                                        if(tnRecordCancelled[0].getNpacState() == 9) {//if TN status is Cancelled
                                            
                                            //first delete and then create new SV 
                                            
                                            //deleteSV -> create new SV -> push
                                            GenericResponse deleteResponse = lta.deleteSubscriber(subscriberId);
                                            Log.write("Delete Response ID: "+deleteResponse.getId());
                                            Log.write("Delete Response Reason String: "+deleteResponse.getReasonString());
                                            Log.write("Delete Response Reason Code: "+deleteResponse.getReasonCode());
                                            Log.write("Delete Response Status: "+deleteResponse.getStatus());

                                            if(deleteResponse.getStatus() == 0) {
                                                
                                                Subscriber newSubscriber = new Subscriber();

                                                String onspProviderString = getSPIDWholeString(onspSPID,lta);
                                                Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                                                newSubscriber.setOriginalProvider(onspProviderString);

                                                String nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                                                Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                                                newSubscriber.setCurrentProvider(nnspProviderString);
                                    
                                                newSubscriber.setResultState(0);
                                                newSubscriber.setPairs(tnPairArray);
                                                newSubscriber.setSubscriberName(atn);
                                                newSubscriber.setSubscriberId(0);
                                                newSubscriber.setAddress("");
                                                newSubscriber.setCity("");
                                                newSubscriber.setState("");
                                                newSubscriber.setZip("");
                                                newSubscriber.setPortNeeded(false);

                                                SaveSubscriberResponse subResponse = lta.saveSubscriber(newSubscriber,1,false);

                                                Log.write("SaveSubscriberResponse Status obtained : "+subResponse.getResponse().getStatus());

                                                Log.write("SaveSubscriberResponse obtained subscriber id: "+subResponse.getSubscriberId());

                                                Log.write("SaveSubscriberResponse obtained error message Code: "+subResponse.getResponse().getReasonCode());

                                                Log.write("SaveSubscriberResponse obtained error message String: "+subResponse.getResponse().getReasonString());

                                                if (subResponse.getResponse().getReasonCode().trim().equals("2112")) {
                                                    //subscriber already exists so try with a different subscriber name
                                                    SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                                                    Date date = new Date();

                                                    String currentTime = sdf.format (date) ; 

                                                    newSubscriber.setSubscriberName(atn+currentTime);

                                                    subResponse = lta.saveSubscriber(newSubscriber,1,false);

                                                }
                                                
                                                if(subResponse.getResponse().getReasonCode().trim().equals("0")) {

                                                    PushRequest pushRequest = new PushRequest();

                                                    pushRequest.setAuthorization(1);

                                                    StatusChangeCauseCode stCode = new StatusChangeCauseCode();
                                                    stCode.setCauseCode(0);
                                                    stCode.setNeeded(false);

                                                    pushRequest.setCauseCode(stCode);

                                                    SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                                                    Date date = new Date();
                                                    String currentTime = sdf.format (date) ; 
													currentTime = "00:00:00";
                                                    dueDate = dueDate + " "+currentTime;//add hardcoded time

                                                    pushRequest.setDate(dueDate);
                                                    pushRequest.setLnpType(1);
                                                    
                                                    onspProviderString = getSPIDWholeString(onspSPID,lta);
                                                    Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                                                    pushRequest.setOldProvider(onspProviderString);


                                                    nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                                                    Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                                                    pushRequest.setNewProvider(nnspProviderString);//call method to get entire ONSP or NNSP string
                                                    
                                                    pushRequest.setPair(tnPairArray[0]);

                                                    GenericResponse pushResponse = rta.push(pushRequest);

                                                    if(pushResponse.getStatus() == 0) {
                                                        Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                                        lsrDao.updateSOATXNID(reqNo,reqVrsn,String.valueOf(pushResponse.getId()),"","","","",onspSPID,atn);
                                                    } else {
                                                        Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                                        lsrDao.updateSOATXNID(reqNo,reqVrsn,"ERR","",pushResponse.getReasonCode(),
                                                                              pushResponse.getReasonString(),String.valueOf(pushResponse.getStatus()),onspSPID,atn);
                                                    }
                                                    
                                                    Log.write("PushSubscriberResponse Status obtained : "+pushResponse.getStatus());

                                                    Log.write("PushSubscriberResponse obtained error message Code: "+pushResponse.getReasonCode());

                                                    Log.write("pushSubscriberResponse obtained error message String: "+pushResponse.getReasonString());
                                                    
                                                } else {//if subscriber response
                                                    Log.write("Save Subscriber failed for TN: "+atn);
                                                }
                                            } else {//if SV delete not successful
                                                Log.write("SV could not be deleted for TN: "+atn);
                                            }
                                        } else {//if status is not changed to cancelled
                                            Log.write("TN Status not changed to 'Cancelled' for TN: "+atn);
                                        }
                                    } else {
                                        Log.write("Unable to check Cancelled Status for TN: "+atn);
                                    }
                                }
                                returnFlag = true;
                            } else if(npacState.equals("5") || npacState.equals("6") ||
                                      npacState.equals("7") || npacState.equals("12") ||
                                      npacState.equals("13") || npacState.equals("14")) {
                                // no SOA PUSH and no DCRIS orders
                                // send to MR with appropriate verbiage
                                
                                if(npacState.equals("5")) {
                                    //Unable to create SOA SV, TN in DL_Failed status.
                                    lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5003","Unable to create SOA SV, TN in DL_Failed status.");
                                } else if(npacState.equals("6")) {
                                    //Unable to create SOA SV, TN in DL_Failed_Partial status.
                                    lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5004","Unable to create SOA SV, TN in DL_Failed_Partial status.");
                                } else if(npacState.equals("7")) {
                                    //TN in Disconnect Pending Status in SOA.  Unable to create SV
                                    lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5005","TN in Disconnect Pending Status in SOA.  Unable to create SV.");
                                } else if(npacState.equals("12")) {
                                    //Unable to create SOA SV, TN in Inconsistent status.
                                    lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5006","Unable to create SOA SV, TN in Inconsistent status.");
                                } else if(npacState.equals("13")) {
                                    //TN in Partial Disconnect Status in SOA.  Unable to create SV
                                    lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5007","TN in Partial Disconnect Status in SOA.  Unable to create SV.");
                                } else if(npacState.equals("14")) {
                                    //Unable to create SOA SV, TN in Last Entry status.
                                    lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5008","Unable to create SOA SV, TN in Last Entry status.");
                                }
                            }//else npacStatus
                        } else {
                            Log.write("TnRecord Array has 0 elements ! Unable to find TN in SOA.");
                            //do not send to Manual Review with appropriate error message
                            noTNInSOA = true;
                        }
                    } else {
                        Log.write("TnRecord object null !!!Unable to find TN in SOA.");
                        //do not send to MR
                        noTNInSOA = true;
                    }
                    
                    if(noTNInSOA) {//create new SV and push as per Theresa
                        tnPairArray[0] = new TnPair();
                        tnPairArray[0].setStartTn(atn);
                        tnPairArray[0].setEndTn(atn);
                        
                        Subscriber newSubscriber = new Subscriber();

                        String onspProviderString = getSPIDWholeString(onspSPID,lta);
                        Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                        newSubscriber.setOriginalProvider(onspProviderString);

                        String nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                        Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                        newSubscriber.setCurrentProvider(nnspProviderString);
                                    
                        newSubscriber.setResultState(0);
                        newSubscriber.setPairs(tnPairArray);
                        newSubscriber.setSubscriberName(atn);
                        newSubscriber.setSubscriberId(0);
                        newSubscriber.setAddress("");
                        newSubscriber.setCity("");
                        newSubscriber.setState("");
                        newSubscriber.setZip("");
                        newSubscriber.setPortNeeded(false);

                        SaveSubscriberResponse subResponse = lta.saveSubscriber(newSubscriber,1,false);

                        Log.write("SaveSubscriberResponse Status obtained : "+subResponse.getResponse().getStatus());

                        Log.write("SaveSubscriberResponse obtained subscriber id: "+subResponse.getSubscriberId());

                        Log.write("SaveSubscriberResponse obtained error message Code: "+subResponse.getResponse().getReasonCode());

                        Log.write("SaveSubscriberResponse obtained error message String: "+subResponse.getResponse().getReasonString());

                        if (subResponse.getResponse().getReasonCode().trim().equals("2112")) {
                            //subscriber already exists so try with a different subscriber name
                            SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                            Date date = new Date();

                            String currentTime = sdf.format (date) ; 

                            newSubscriber.setSubscriberName(atn+currentTime);

                            subResponse = lta.saveSubscriber(newSubscriber,1,false);

                        }
                        
                        
                        if(subResponse.getResponse().getReasonCode().trim().equals("0")) {
                            
                            //get rta from hashtable for that ONSP
                                        
                            RemoteTransactionAdapter rta;

                            //just pass SPID to hashtable and get RTA object
                            rta = (RemoteTransactionAdapter) htSOAAdapters.get(onspSPID.trim()+"RTA");

                            if (rta == null) {
                                Log.write("Error in getting RTA object for WIN SPID : "+onspSPID+" and State : "+stateCD+" and wcn : "+wcn);
                                return false;
                            }
                                                        
                            PushRequest pushRequest = new PushRequest();

                            pushRequest.setAuthorization(1);

                            StatusChangeCauseCode stCode = new StatusChangeCauseCode();
                            stCode.setCauseCode(0);
                            stCode.setNeeded(false);

                            pushRequest.setCauseCode(stCode);

                            SimpleDateFormat sdf = new SimpleDateFormat ("hh:mm:ss") ; 
                            Date date = new Date();
                            String currentTime = sdf.format (date) ; 
							currentTime = "00:00:00";
                            dueDate = dueDate + " "+currentTime;//add hardcoded time

                            pushRequest.setDate(dueDate);
                            pushRequest.setLnpType(1);
                            
                            onspProviderString = getSPIDWholeString(onspSPID,lta);
                            Log.write("Whole SPID String from SOA for ONSP : "+onspProviderString);
                            pushRequest.setOldProvider(onspProviderString);


                            nnspProviderString = getSPIDWholeString(lsrNNSP,lta);
                            Log.write("Whole SPID String from SOA for NNSP : "+nnspProviderString);
                            pushRequest.setNewProvider(nnspProviderString);//call method to get entire ONSP or NNSP string
                                                    
                            pushRequest.setPair(tnPairArray[0]);

                            GenericResponse pushResponse = rta.push(pushRequest);

                            if(pushResponse.getStatus() == 0) {
                                Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                lsrDao.updateSOATXNID(reqNo,reqVrsn,String.valueOf(pushResponse.getId()),"","","","",onspSPID,atn);
                            } else {
                                Log.write("PushSubscriberResponse SOA Txn ID obtained : "+pushResponse.getId());

                                lsrDao.updateSOATXNID(reqNo,reqVrsn,"ERR","",pushResponse.getReasonCode(),
                                                      pushResponse.getReasonString(),String.valueOf(pushResponse.getStatus()),onspSPID,atn);
                            }
                            
                            Log.write("PushSubscriberResponse Status obtained : "+pushResponse.getStatus());

                            Log.write("PushSubscriberResponse obtained error message Code: "+pushResponse.getReasonCode());

                            Log.write("pushSubscriberResponse obtained error message String: "+pushResponse.getReasonString());

			
                            returnFlag = true;
                        }
		  }//if noTNInSOA
                                        
            } catch (Exception e) {
                e.printStackTrace();
                Log.write("Exception thrown :"+e);
                
                Log.write("Sendpushtosoa PON: "+reqPON);
                Log.write("Sendpushtosoa Version: "+reqVrsn);
                Log.write("Sendpushtosoa OCN: "+reqOCN);
                
                //send to MR because of Exception
                try {
                    lsrDao.callMRStatusUpdateProc(reqPON,reqVrsn,reqOCN,"5010","Internal Error ! Exception in calling SOA API.");
                } catch(Exception e1) {
                    //report db exception
                    Log.write("Exception : "+e1.getMessage());
                }
            } catch(Throwable t) {
              //Log message here
              Log.write("Inside catch for Throwable...");
              Log.write("Throwable caught : "+t.getMessage());
              t.printStackTrace();
            }
        return returnFlag;
    }
    
}



