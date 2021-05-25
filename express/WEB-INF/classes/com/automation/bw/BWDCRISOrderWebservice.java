/*
 * BWDCRISOrderWebservice.java
 *
 * Created on Nov 23, 2009, 11:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bw;

import java.net.URL;

import com.alltel.lsr.common.util.PropertiesManager;
import com.alltel.lsr.common.error.objects.ExceptionHandler;
import com.alltel.lsr.common.util.Log;

import com.automation.bean.AddressBean;
import com.automation.bean.LSRDataBean;
import com.automation.bean.ValidationDataBean;
import com.automation.bean.VendorTableDataBean;
import com.automation.dao.LSRdao;

import com.windstream.winexpcustprof.WinExpDataProxy;
import com.windstream.winexpcustprof.WinExpDataProxy_Impl;
import com.windstream.winexpcustprof.WinExpWebSrvcIntrfc;
import java.util.List;

import webservice.expressorder.windstream.com.ExpressOrderWebLayer_Stub;

import com.windstream.winexpcustprof.Gtnl;
import com.windstream.winexpcustprof.PndngOrder;
import com.windstream.winexpcustprof.ServiceDataRequest;
import com.windstream.winexpcustprof.ServiceDataResponse;
import com.windstream.winexpcustprof.StatusDataRequest;
import com.windstream.winexpcustprof.StatusDataResponse;
import com.windstream.winexpcustprof.AddLn;
import com.windstream.winexpcustprof.Addr;

import com.windstream.expressorder.webservice.ErrorInfo;
import com.windstream.expressorder.webservice.OpCreateExpressOrderRequest;
import com.windstream.expressorder.webservice.OpCreateExpressOrderReply;
import com.windstream.expressorder.webservice.OpCreateXOrderRequest;
import com.windstream.expressorder.webservice.OpCreateXOrderReply;

import java.util.Date;
import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;
import java.util.List;
import java.text.SimpleDateFormat;

import javax.xml.rpc.JAXRPCException;

import webservice.expressorder.windstream.com.ExpressWebService_Impl;
import webservice.expressorder.windstream.com.ExpressWebService;
import webservice.expressorder.windstream.com.ExpressOrderWebLayer;

import com.windstream.expressorder.webservice.AddressData;
import com.windstream.expressorder.webservice.AsocData;
import com.windstream.expressorder.webservice.ServiceData;
import com.windstream.expressorder.webservice.ApplicationInfo;


/**
 *
 * @author Antony Rajan
 */
public class BWDCRISOrderWebservice {
    
    private  ValidationDataBean validationBean;
    
    /** Creates a new instance of BWCusInfoWebservice */
    public BWDCRISOrderWebservice() {
    }
    
    /*
     * webServiceInvoke method used for invoking
     * order creation webservice (O/I orders) and get the ouput from b/w
     */
    
    public ErrorInfo sendOOrder(String atn,LSRDataBean lsrDataBean,String orderType,Vector tnList,Hashtable reqData,VendorTableDataBean vendorBean,Vector vendorAsocVector){
        
        ErrorInfo resultStr = new ErrorInfo();
        LSRdao lsrDao = new LSRdao();
        
        try {
            Log.write("BWDCRISOrderWebservice webServiceInvoke calling: ");
            ExpressWebService expressWebserviceImpl = new ExpressWebService_Impl();
            
            ExpressOrderWebLayer expressWebLayer = expressWebserviceImpl.getExpressOrderWebLayer();

            //code to change Endpoint URL dynamically based on lsr.properties entry for lsr.bwexpressord.URL
            ExpressOrderWebLayer_Stub ewlStub = (ExpressOrderWebLayer_Stub)expressWebLayer;
            URL urlString = new URL(PropertiesManager.getProperty("lsr.bwexpressord.URL",""));

            Log.write("BW ExpressOrd URL prior to dynamic setting : "+ewlStub._getTargetEndpoint());
            ewlStub._setTargetEndpoint(urlString);
            Log.write("BW ExpressOrd URL after to dynamic setting : "+ewlStub._getTargetEndpoint());

           
            OpCreateExpressOrderRequest createRequest = new OpCreateExpressOrderRequest();
            
                      
            String atnWithoutHyphens;
            ServiceData [] serviceOrderArray;
            AsocData [] asocDataArray;
            
            //get SPflag
            
            String spFlag = lsrDao.retrieveSPFlag(lsrDataBean.getReqstNmbr(),lsrDataBean.getReqstVer());
            
            //do all variable substitutions to get values from Simple Port valid fields instead of 
            //Complex port valid fields
            
            if(spFlag.equals("Y")) {
                //lsrDataBean.setAct
                
                lsrDataBean.setSimpleportAccountNo(lsrDataBean.getAccountNo());
                lsrDataBean.setSimpleportDDD(lsrDataBean.getDesiedDueDate());
                
                //AddressBean addr = lsrDataBean.getAddress();
                
                Map mapAddress = lsrDataBean.getAddressMap();
                AddressBean addr = (AddressBean) mapAddress.get("EU_LA");
                Log.write("LSRBaseValidator setting SPZIP with EU ZIP value :" + addr.getZip());
        
                
                lsrDataBean.setSimpleportZIP(addr.getZip());
                
                if(addr.getZip() != null)
                    Log.write("Zip code from SPSR :"+addr.getZip());
                
                lsrDataBean.setNewNetworkSP(lsrDataBean.getNewNetwork());
                
                List gotoList = lsrDataBean.getPortedNBR();
                
                String portedNumber = (String) gotoList.get(0);
                
                portedNumber = portedNumber.replaceAll("-","");
                
                //lsrDao.getPortedTNs(lsrDataBean.getReqstNmbr(),)
                
                //lsrDataBean.getPortedNBR();
                
                lsrDataBean.setAccountTelephoneNo(portedNumber);
                
                Log.write("ported Number :"+portedNumber);
            }
            
            //create empty ASOC 
            
            AsocData [] emptyAsocDataArray = new AsocData[1];

            emptyAsocDataArray[0] = new AsocData();

            //send an empty asoc list for all types except UNEP CONV
            emptyAsocDataArray[0].setAsocActionInd("");//always I
            emptyAsocDataArray[0].setAsocID("");//asoc name/ID
            emptyAsocDataArray[0].setAsocQty("");//always 1
            emptyAsocDataArray[0].setAsocRate("");//asoc t
            emptyAsocDataArray[0].setAsocRemark("");//always empty
            emptyAsocDataArray[0].setAsocSeqNum("");//always 1
            emptyAsocDataArray[0].setAsocSign("");//always +
            emptyAsocDataArray[0].setAsocType("");// always an I 
                     
            if(lsrDataBean.getSerRequestType().trim().equals("G") || 
                      lsrDataBean.getSerRequestType().trim().equals("H") ||
                      lsrDataBean.getSerRequestType().trim().equals("J")) {
                asocDataArray = emptyAsocDataArray;
            } else {
                
                if(vendorAsocVector.size() > 0) {
                    //get asoc data from vendor table only for service order asoc
                    //only for UNEP CONV PONs add one more array element to add
                    //PLOCK ASOC for FPI = A, B or E
                    if(lsrDataBean.getSerRequestType().trim().equals("M") 
                    && lsrDataBean.getActivity().trim().equals("V")) {
                        asocDataArray = new AsocData[vendorAsocVector.size()+1];
                    } else {
                        asocDataArray = new AsocData[vendorAsocVector.size()];
                    }

                    for(int i = 0; i < vendorAsocVector.size(); i++) {

                        Hashtable ht = (Hashtable) vendorAsocVector.get(i);

                        asocDataArray[i] = new AsocData();

                        Log.write("ABout to display value of Asoc ht...");
                        Log.write("ASOC CD : "+(String)ht.get("ASOC_CD"));
                        Log.write("ASOC CD : "+(String)ht.get("ASOC_FEE_RATE"));
                        Log.write("Vendor BTN : "+vendorBean.getBTN());
                        String howAsocFeeApplies = (String) ht.get("HOW_ASOC_FEE_APPLIES");                        
                        
                        //send a filled asoc list for all types except Directory PONs
                        //and for UNEPs we will send asoc list from PS form of PON
                        // for all other types we send asoc list from vendor table
                        asocDataArray[i].setAsocActionInd("I");//always I
                        asocDataArray[i].setAsocID((String) ht.get("ASOC_CD"));//asoc name/ID
                        asocDataArray[i].setAsocQty("1");//always 1
                        asocDataArray[i].setAsocRate((String) ht.get("ASOC_FEE_RATE"));//asoc t
                        
                        //for PS ASOCS dont put BTN in remarks and asoc type is N
                        if(howAsocFeeApplies.equals("PS_ASOC")) {
                            asocDataArray[i].setAsocRemark("");//always empty
                            asocDataArray[i].setAsocType("N");// always an I 
                        } else {
                            asocDataArray[i].setAsocRemark(vendorBean.getBTN());//always empty
                            asocDataArray[i].setAsocType("B");// always an I 
                        }
                        
                        asocDataArray[i].setAsocSeqNum("1");//always 1
                        asocDataArray[i].setAsocSign("+");//always +
                    }    
                } else {//if vendorasocvector size = 0
                    asocDataArray = new AsocData[1];

                    asocDataArray[0] = new AsocData();

                    //send an empty asoc list for all types except UNEP CONV
                    asocDataArray[0].setAsocActionInd("");//always I
                    asocDataArray[0].setAsocID("");//asoc name/ID
                    asocDataArray[0].setAsocQty("");//always 1
                    asocDataArray[0].setAsocRate("");//asoc t
                    asocDataArray[0].setAsocRemark("");//always empty
                    asocDataArray[0].setAsocSeqNum("");//always 1
                    asocDataArray[0].setAsocSign("");//always +
                    asocDataArray[0].setAsocType("");// always an I 
                }//end of asocvector size = 0
            }//end of if DA types
                     
            
            
            serviceOrderArray = new ServiceData[tnList.size()];

            for (int i = 0; i < tnList.size(); i++) {

                serviceOrderArray[i] = new ServiceData();

                atnWithoutHyphens = tnList.get(i).toString().replaceAll("-","").trim();

		//if UNEP CONV PON then disassemble string to get PIC,IPIC and FPI
                String pic = "";
                String iPic = "";
                String fpi = "";
                
                if(lsrDataBean.getSerRequestType().trim().equals("M") 
                && lsrDataBean.getActivity().trim().equals("V")) {
                
                   String [] unepList = atnWithoutHyphens.split("/");
                
                   atnWithoutHyphens = unepList[0].trim();
                   pic = unepList[1];
                   iPic = unepList[2];
                   fpi = unepList[3];
                   
                   if(fpi != null && (fpi.trim().equals("A") || fpi.trim().equals("B") ||
                                      fpi.trim().equals("E"))) {
                    asocDataArray[vendorAsocVector.size()] = new AsocData();

                    //add ASOC for PLOCK as FPI is a A,B or E
                    asocDataArray[vendorAsocVector.size()].setAsocActionInd("I");//always I
                    asocDataArray[vendorAsocVector.size()].setAsocID("PLOCK");//asoc name/ID
                    asocDataArray[vendorAsocVector.size()].setAsocQty("1");//always 1
                    asocDataArray[vendorAsocVector.size()].setAsocRate("0.0");//asoc t
                    asocDataArray[vendorAsocVector.size()].setAsocRemark(fpi.trim()+"W");//always empty
                    asocDataArray[vendorAsocVector.size()].setAsocSeqNum("1");//always 1
                    asocDataArray[vendorAsocVector.size()].setAsocSign("+");//always +
                    asocDataArray[vendorAsocVector.size()].setAsocType("I");// always an I 
                   } else {
                    asocDataArray[vendorAsocVector.size()] = new AsocData();

                    //add empty ASOC if FPI is not a A,B or E
                    asocDataArray[vendorAsocVector.size()].setAsocActionInd("");//always I
                    asocDataArray[vendorAsocVector.size()].setAsocID("");//asoc name/ID
                    asocDataArray[vendorAsocVector.size()].setAsocQty("");//always 1
                    asocDataArray[vendorAsocVector.size()].setAsocRate("");//asoc t
                    asocDataArray[vendorAsocVector.size()].setAsocRemark("");//always empty
                    asocDataArray[vendorAsocVector.size()].setAsocSeqNum("");//always 1
                    asocDataArray[vendorAsocVector.size()].setAsocSign("");//always +
                    asocDataArray[vendorAsocVector.size()].setAsocType("");// always an I 
                   }
                }
                serviceOrderArray[i].setTelephoneNumber(atnWithoutHyphens);//atn without hyphens
                
                //associate asoc list only with the first TN or Pilot TN in TNlist
                if(i == 0)  {
                    serviceOrderArray[i].setArrayOfRecAsoc(asocDataArray);//?
                } else {
                    serviceOrderArray[i].setArrayOfRecAsoc(emptyAsocDataArray);//?
                }
                  
                //if UNEP Conv PON send PIC and IPIC value from lsrDataBean
                // for all other types send empty values of IPIC and PIC
                if(lsrDataBean.getSerRequestType().trim().equals("M")
                && lsrDataBean.getActivity().trim().equals("V")) {
                    serviceOrderArray[i].setIpic(pic);
                    serviceOrderArray[i].setPic(iPic);
                   
                } else {
                                        
                    serviceOrderArray[i].setIpic("NONE");
                    serviceOrderArray[i].setPic("NONE");
                }//if for pic and ipic
            }//for loop with TNlist
                           
            createRequest.setArrayOfServiceData(serviceOrderArray);
            
            AddressData addressData = new AddressData();
            
            Map addressMap = lsrDataBean.getAddressMap();
            
            AddressBean addrBean;
            
            if(orderType.equals("DD")) {
               addrBean = (AddressBean) addressMap.get("DSR");
            } else {
               addrBean = (AddressBean) addressMap.get("EU_LA"); 
            }
            
            //Fix for Jira Issue# 46 - Antony - 06/17/2010
            
            String addrLine1 = "";
            String addrLine1Upper="";
            String addrLine2 = "";
            String addrLine2Upper="";
            String addrLine3 = "";
            String addrLine3Upper="";
            String addrCity = "";
            String addrState = "";
            String addrZip = "";
            String addrCityUpper = "";
            String addrStateUpper = "";
            String addrZipUpper = "";
            Addr camsAddress = null;
            AddLn camsAddrLines [] = null;
            ValidationDataBean validationData = null;
            
            //fix for SPIRA
            
            if(spFlag.equals("Y")) {
                //call BW custprofile project only if it is a SP as we need address lines from CAMS
                BWCusInfoWebservice bwcusInfows = new BWCusInfoWebservice();
                Log.write("BWCusInfoWebservice calling from BWDCRISOrder for SP");
                bwcusInfows.webServiceInvoke(lsrDataBean);
                
                //Log.write(" processRequest method reqNo: " + reqNo + " reqUrl: " + reqUrl + " objLSRDataBean " + objLSRDataBean);

                /*
                 * creating ValidationDataBean instance
                 */

                validationData = bwcusInfows.getValidationBean();
                
                camsAddress = validationData.getCustAddress();
                camsAddrLines = camsAddress.getAddrLine();
            }
            
            if(orderType.equals("DD")) {
               addrLine1 = lsrDataBean.getDsrName();
               
               if(addrLine1 != null)
                addrLine1Upper = addrLine1.toUpperCase();
            
               Log.write("Value of addrLine1 in Upper case: "+addrLine1Upper); 
                
               addressData.setAddressLine1(addrLine1Upper);//dsr form name
            } else {
               addrLine1 = lsrDataBean.getEuName();
               
               if(addrLine1 != null)
                addrLine1Upper = addrLine1.toUpperCase();
            
               Log.write("Value of addrLine1 in Upper case: "+addrLine1Upper); 
               
               addressData.setAddressLine1(addrLine1Upper);//eu form name
               
               if(spFlag.equals("Y")) {
                   addressData.setAddressLine1(validationData.getCustName());
               }
                   
            }
            
            addrLine2 = addrBean.getStreet();
               
            if(addrLine2 != null)
                addrLine2Upper = addrLine2.toUpperCase();

            Log.write("Value of addrLine2 in Upper case: "+addrLine2Upper); 

            addressData.setAddressLine2(addrLine2Upper);//concatenated string of all eu addr fields as per Tana
            
            if(spFlag.equals("Y")) {
                   //addressData.setAddressLine2("50 EXECUTIVE PARKWAY HUDSON OH 44236");
                   addressData.setAddressLine2(camsAddrLines[0].getAddrLn());
            }
            
            addrLine3 = addrBean.getCity()+" "+addrBean.getState()+" "+addrBean.getZip();
               
            if(addrLine3 != null)
                addrLine3Upper = addrLine3.toUpperCase();

            Log.write("Value of addrLine3 in Upper case: "+addrLine3Upper); 

            addressData.setAddressLine3(addrLine3Upper);//conc of city,state and zip always
            
            //addressData.setAddressLine2(addrBean.getStreet());//concatenated string of all eu addr fields as per Tana
            //addressData.setAddressLine3(addrBean.getCity()+" "+addrBean.getState()+" "+addrBean.getZip());//conc of city,state and zip always
            
            addrCity = addrBean.getCity();
            
            if(addrCity != null)
                addrCityUpper = addrCity.toUpperCase();

            Log.write("Value of addrCity in Upper case: "+addrCityUpper); 

            addressData.setCity(addrCityUpper);//eu city
            
            //addressData.setCity(addrBean.getCity());//eu city
            
            if(spFlag.equals("Y")) 
                addressData.setCity(camsAddress.getCity());
            
            addrState = addrBean.getState();
            
            if(addrState != null)
                addrStateUpper = addrState.toUpperCase();

            Log.write("Value of addrState in Upper case: "+addrStateUpper); 

            addressData.setState(addrStateUpper);//eu/rt state
            //addressData.setState(addrBean.getState());//eu/rt state
            
            addrZip = addrBean.getZip();
            
            if(addrZip != null)
                addrZipUpper = addrZip.toUpperCase();

            Log.write("Value of addrZip in Upper case: "+addrZipUpper); 

            addressData.setZip(addrZipUpper);//eu zip
            //addressData.setZip(addrBean.getZip());//eu zip
                    
            createRequest.setAddressLsrEUForm(addressData);
            
            ApplicationInfo appInfo = new ApplicationInfo();
            appInfo.setApplicationID("Fqyc@tUFosBi4xY0FnM34FLRW8!");
            appInfo.setApplicationName("EXPRESS");
            
            
            createRequest.setApplicationInfo(appInfo);
            
            //get asoc data from vendor table only for npub and nlist asocs
            // for unep conv and NP PONs; others add empty asoc list
            
            if((lsrDataBean.getSerRequestType().trim().equals("C") || lsrDataBean.getSerRequestType().trim().equals("M"))
            && (lsrDataBean.getActivity().trim().equals("V"))) {
                        
                Vector occAsocDataVector = lsrDao.getVTNPubNListASOCs(vendorBean.getVendorConfigSqncNumber(),reqData);

                if(occAsocDataVector != null && occAsocDataVector.size() > 0) {
                
                    AsocData [] occAsocDataArray = new AsocData[occAsocDataVector.size()];
                    
                    for(int i = 0; i < occAsocDataVector.size(); i++) {

                        Hashtable ht = (Hashtable) occAsocDataVector.get(i);

                        occAsocDataArray[i] = new AsocData();

                        //send an empty asoc list for all types except UNEP CONV
                        occAsocDataArray[i].setAsocActionInd("I");//always I
                        occAsocDataArray[i].setAsocID((String) ht.get("ASOC_CD"));//asoc name/ID
                        occAsocDataArray[i].setAsocQty("1");//always 1
                        occAsocDataArray[i].setAsocRate((String) ht.get("ASOC_FEE_RATE"));//asoc t
                        
                        String asocType = (String) ht.get("ASOC_TYPE");
                        
                        if(asocType.trim().equals("17"))
                            occAsocDataArray[i].setAsocRemark("EXPRESSNONPUB");
                        else if(asocType.trim().equals("18"))
                            occAsocDataArray[i].setAsocRemark("EXPRESSNONLIST");
                            
                        occAsocDataArray[i].setAsocSeqNum("1");//always 1
                        occAsocDataArray[i].setAsocSign("+");//always +
                        occAsocDataArray[i].setAsocType("I");// always an I 
                    }
                    createRequest.setArrayOfOCCAsocData(occAsocDataArray);
                } else {//if occvector size = 0
                    createRequest.setArrayOfOCCAsocData(emptyAsocDataArray);
                }
            } else {
                createRequest.setArrayOfOCCAsocData(emptyAsocDataArray);//if serv act type is NP or UNEP CONV
            }
                           
            
            createRequest.setCustTypeCode(lsrDataBean.getCompanyCode());//rt
            createRequest.setDisconnectReason("DC");//always DC
                        
             //create SimpleDateFormat object with source string date format
            SimpleDateFormat sdfSource = new SimpleDateFormat("MM-dd-yyyy");
            
             //parse the string into Date object      
            Date date = sdfSource.parse(lsrDataBean.getDesiedDueDate());
            
            //create SimpleDateFormat object with desired date format
            SimpleDateFormat sdfDestination = new SimpleDateFormat("yyMMdd");
            
            //parse the date into another format
            String strDueDate = sdfDestination.format(date);  
            
            //if(orderType.equals("DD")) { not needed as getDesiredDueDate returns correct date for all types
            createRequest.setDueDate(strDueDate);//lsr dd
            
            if(lsrDataBean.getEUListTreatment() != null) {
              createRequest.setEltInd(lsrDataBean.getEUListTreatment());//eu_t
            } else {
            createRequest.setEltInd("");//eu_t    
            }
            
            if(lsrDataBean.getEURetainingList() != null) {
                createRequest.setErlInd(lsrDataBean.getEURetainingList());//eu_t  
            } else {
                createRequest.setErlInd("");//eu_t    
            
                if(spFlag.equals("Y")) 
                   createRequest.setErlInd("N");
            
            }
                        
            if(lsrDataBean.getListActivity() != null) {
                createRequest.setLactInd(lsrDataBean.getListActivity());//eu_t
            } else {
                createRequest.setLactInd("");//eu_t    
            }
            
            
            createRequest.setExpressOrderType(orderType);//NP,UD,RD..
            createRequest.setGoToCount(String.valueOf(lsrDataBean.getPortedNBR().size()));
            createRequest.setLspac800Number("8008651498");
            createRequest.setOcnNumber(lsrDataBean.getOCNcd());//rt
            
            if(orderType.equals("DD")) {
               createRequest.setPilotTN(lsrDataBean.getExitingActTeleNo());//dsr ATN
            /*} else if(orderType.equals("NP") && spFlag.equals("Y")) {
                createRequest.setPilotTN(tnList.get(0).toString().replaceAll("-","").trim());
                Log.write("Inside Simple Order if block...");
             */
            } else {
               createRequest.setPilotTN(lsrDataBean.getAccountTelephoneNo());//rt
            }
            
            createRequest.setPonNumber(lsrDataBean.getReqstPon());//rt
            createRequest.setPonVersion(lsrDataBean.getReqstVer());//rt
          
            String vtTaxJur = vendorBean.getTXJUR();
            String taxJur = "";
            
            if (vtTaxJur == null)
                taxJur = (String)reqData.get("CUST_TAXJUR");
            else
                taxJur = (String)reqData.get("CUST_TAXJUR")+"/"+vendorBean.getTXJUR();
            
            taxJur = taxJur.replaceAll("/","");
            createRequest.setTaxJur(taxJur);
            
            createRequest.setTransactionID("123456789012345678901234567");
            createRequest.setUserID("EXP5072A");
            
            //String [] vendorBTN = vendorBean.getBTN().split(",");
            createRequest.setVendorBTN(vendorBean.getBTN());//vt
            
            createRequest.setVendorContactNumber(vendorBean.getContactNo());//vt
            //fix for Jira issue 50 - Antony - 06162010
            String vendorName = (String)reqData.get("COMP_NAME");
            String vendorNameUpper="";
            
            if(vendorName != null)
                vendorNameUpper = vendorName.toUpperCase();
            
            Log.write("Value of Vendor Name in Upper case: "+vendorNameUpper);
            
            createRequest.setVendorName(vendorNameUpper);
            
            //fix for SPIRA incident #to do -- send HD and DD from vendor table with request -- Antony -- 06/01/2011
            
            createRequest.setHasDirectoryInd(vendorBean.getIsDirectory());
            createRequest.setDeleteDirectoryInd(vendorBean.getIsEligibleToDeleteDir());
            
            createRequest.setWirelessInd((String)reqData.get("WIRELESS_IND"));
           
            String isBroadBandCustomer = (String) reqData.get("BROADBAND_CUST");
            
           
            //commenting out check for broadband,resale disc or unep conversion YYN and units block
            // Antony - 05/23/2012
            /*
            if(isBroadBandCustomer != null && isBroadBandCustomer.equalsIgnoreCase("Y")
            && ((lsrDataBean.getSerRequestType().trim().equals("E")//Resale Disconnect PONs
              && lsrDataBean.getActivity().trim().equals("D")) ||
                (lsrDataBean.getSerRequestType().trim().equals("M")//UNEP Conversion PONs
              && lsrDataBean.getActivity().trim().equals("V"))
               )) {
                createRequest.setWorkForceIorder("YYN");//
                createRequest.setWorkForceOorder("YYN");//
                createRequest.setWorkUnitsIorder("2");//
                createRequest.setWorkUnitsOorder("2");//
            } else { */
                createRequest.setWorkForceIorder("");//
                createRequest.setWorkForceOorder("");//
                createRequest.setWorkUnitsIorder("0");//
                createRequest.setWorkUnitsOorder("0");//
            //}
        
            //set native indicator for request
            int isNativeNumber = lsrDao.getWindstreamNativeNumberLerg(atn);
            String nonNativeFlag = "";
            
            if(isNativeNumber == 1)
                nonNativeFlag = "N";
            else if(isNativeNumber == 0)
                nonNativeFlag = "Y";
            else {
                    //send to MR
                    Exception ex = new Exception("LERG");
                    throw ex;
            }
            
            if(nonNativeFlag.equals("Y") && spFlag.equals("Y")) {
                createRequest.setLactInd("D");
            }
            
            createRequest.setNonNativeInd(nonNativeFlag);
            
            //code to set CLEC and SP indicator for Simple Orders project - Antony - 4/15/2011
            
            createRequest.setSimpleInd(spFlag);
            
            String CLECInd = (String) reqData.get("COMP_TYPE");
            
            if(CLECInd != null && CLECInd.equals("C"))
                createRequest.setClecInd("Y");
            else
                createRequest.setClecInd("N");
            
            Log.write("createRequest "+createRequest.toString());
            OpCreateExpressOrderReply createReply = 
                    expressWebLayer.opCreateExpressOrder(createRequest);
          
            resultStr = createReply.getErrorInfo();
            
                       
            Log.write("Create DCRIS order Response :"+createReply.toString());
            
        } catch(JAXRPCException jx) {
            // BW Dcris Order webservice down call MR stored procedure to update with error
            try {
                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"7001","Businessware DCRIS Order Webservice down.");
            } catch(Exception dbEx) {
                
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            Log.write("Exception :"+ex.getMessage());
            
            // BW Dcris Order webservice down call MR stored procedure to update with error
            try {
                if(ex.getMessage().trim().equals("LERG"))
                    lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"7005","Internal Error ! Unable to connect to LERG for Non Native Indicator.");
                else
                    lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"7002","Internal Error ! Exception in calling DCRIS order webservice.");
            } catch(Exception dbEx) {
                
            }
            
            //ExceptionHandler.handleException("Exception:BW ",ex);
        }
        
        return resultStr;
    }
    
    /*
     * webServiceInvoke method used for invoking
     * order creation webservice (X order) and get the ouput from b/w
     */
    
    public ErrorInfo sendXOrder(LSRDataBean lsrDataBean,String orderType,VendorTableDataBean vendorBean,Vector vendorAsocVector){
        
        ErrorInfo resultStr = new ErrorInfo();
        LSRdao lsrDao = new LSRdao();
        
        try {
            Log.write("BWDCRISWebservice X Order webServiceInvoke calling: ");
            ExpressWebService expressWebserviceImpl = new ExpressWebService_Impl();
            
            ExpressOrderWebLayer expressWebLayer = expressWebserviceImpl.getExpressOrderWebLayer();

            //code to change Endpoint URL dynamically based on lsr.properties entry for lsr.bwexpressord.URL
            ExpressOrderWebLayer_Stub ewlStub = (ExpressOrderWebLayer_Stub)expressWebLayer;
            URL urlString = new URL(PropertiesManager.getProperty("lsr.bwexpressord.URL",""));

            Log.write("BW ExpressOrd URL prior to dynamic setting : "+ewlStub._getTargetEndpoint());
            ewlStub._setTargetEndpoint(urlString);
            Log.write("BW ExpressOrd URL after to dynamic setting : "+ewlStub._getTargetEndpoint());

            OpCreateXOrderRequest createXOrderRequest = new OpCreateXOrderRequest();
             
            ApplicationInfo appInfo = new ApplicationInfo();
            appInfo.setApplicationID("Fqyc@tUFosBi4xY0FnM34FLRW8!");
            appInfo.setApplicationName("EXPRESS");
        
            createXOrderRequest.setApplicationInfo(appInfo);
            
            //create SimpleDateFormat object with source string date format
            SimpleDateFormat sdfSource = new SimpleDateFormat("MM-dd-yyyy");
            
             //parse the string into Date object      
            Date date = sdfSource.parse(lsrDataBean.getDesiedDueDate());
            
            //create SimpleDateFormat object with desired date format
            SimpleDateFormat sdfDestination = new SimpleDateFormat("yyMMdd");
            
            //parse the date into another format
            String strDueDate = sdfDestination.format(date);  
            
            createXOrderRequest.setDueDate(strDueDate);
                    
            createXOrderRequest.setExpressOrderType(orderType);
            
            createXOrderRequest.setOcnNumber(lsrDataBean.getOCNcd());
            createXOrderRequest.setPonNumber(lsrDataBean.getReqstPon());
            createXOrderRequest.setPonVersion(lsrDataBean.getReqstVer());
            
            createXOrderRequest.setTransactionID("123456789012345678901234567");
            createXOrderRequest.setUserID("EXP5072A");
            
            String vendorBTN = vendorBean.getBTN();
            createXOrderRequest.setVendorBTN(vendorBTN);
            
            //get asoc data from vendor table only for service order asoc
            AsocData [] asocDataArray = new AsocData[vendorAsocVector.size()];
            
            for(int i = 0; i < vendorAsocVector.size(); i++) {
                            
                Hashtable ht = (Hashtable) vendorAsocVector.get(i);
                
                asocDataArray[i] = new AsocData();

                asocDataArray[i].setAsocActionInd("I");//always I
                asocDataArray[i].setAsocID((String) ht.get("ASOC_CD"));//asoc name/ID
                asocDataArray[i].setAsocQty("1");//always 1
                asocDataArray[i].setAsocRate((String) ht.get("ASOC_FEE_RATE"));//asoc t
                asocDataArray[i].setAsocRemark("");//always empty
                asocDataArray[i].setAsocSeqNum("1");//always 1
                asocDataArray[i].setAsocSign("+");//always +
                asocDataArray[i].setAsocType("N");// always N for X orders
            }
                           
            createXOrderRequest.setArrayOfNonRecAsoc(asocDataArray);
            
            Log.write("createXOrderRequest "+createXOrderRequest.toString());
            
            OpCreateXOrderReply createReply = 
                    expressWebLayer.opCreateXOrder(createXOrderRequest);
          
            resultStr = createReply.getErrorInfo();
            
            Log.write("Create DCRIS order Response :"+createReply.toString());
        } catch(JAXRPCException jx) {
            // BW Dcris Order webservice down call MR stored procedure to update with error
            try {
                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"7001","Businessware DCRIS Order Webservice down.");
            } catch(Exception dbEx) {
                
            }
       
        } catch(Exception ex) {
            ex.printStackTrace();
            Log.write("Exception :"+ex.getMessage());
            
            // BW Dcris Order webservice down call MR stored procedure to update with error
            try {
                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"7002","Internal Error ! Exception in calling DCRIS order webservice.");
            } catch(Exception dbEx) {
                
            }
            
            ExceptionHandler.handleException("Exception:BW ",ex);
        }
        
        return resultStr;
    }
        
    public String ConvertXMLtoJavaObject(){
        return "";
    }
    
    public ValidationDataBean getValidationBean() {
        return validationBean;
    }
    
    public void setValidationBean(ValidationDataBean validationBean) {
        this.validationBean = validationBean;
    }
    
}


