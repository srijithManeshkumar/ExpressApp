/*
 * SPSRValidator.java
 *
 * Created on June 22, 2009, 2:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.automation.validator;

import biz.neustar.www.clearinghouse.SOAPRequestHandler._1_0.SOAPRequestHandlerServiceLocator;
import biz.neustar.www.clearinghouse.SOAPRequestHandler._1_0.SOAPRequestHandlerSoapBindingStub;

import com.alltel.lsr.common.util.Log;
import com.alltel.lsr.common.util.PropertiesManager;
import com.automation.bean.LSRDataBean;
import com.automation.bean.ValidationDataBean;
import com.automation.bean.VendorTableDataBean;
import com.automation.bean.AddressBean;
import com.automation.dao.LSRdao;

import com.windstream.winexpcustprof.Addr;
import com.windstream.winexpcustprof.Asoc;
import com.windstream.winexpcustprof.Gtnl;
import com.windstream.winexpcustprof.ImpctdApp;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.net.URL;

import com.windstream.expressorder.webservice.OpSoaActivationCompleteRequest;
import com.windstream.expressorder.webservice.OpCancelOrderRequest;
import com.windstream.expressorder.webservice.OpCancelOrderReply;
import com.windstream.expressorder.webservice.OpSoaActivationCompleteReply;
import com.windstream.expressorder.webservice.OrderData;
import com.windstream.expressorder.webservice.ApplicationInfo;
import com.windstream.expressorder.webservice.ErrorInfo;

import webservice.expressorder.windstream.com.ExpressOrderWebLayer;
import webservice.expressorder.windstream.com.ExpressWebService;
import webservice.expressorder.windstream.com.ExpressWebService_Impl;
import webservice.expressorder.windstream.com.ExpressOrderWebLayer_Stub;

import javax.xml.rpc.JAXRPCException;

/**
 *
 * @author kumar.k
 */
public class SPSRValidator extends LSRBaseValidator {

    private VendorTableDataBean vendorBean;
    private ValidationDataBean validationData;
    private LSRDataBean lsrDataBean;

    /** Creates a new instance of SPSRValidator */
    public SPSRValidator(LSRDataBean lsrDataBean, VendorTableDataBean vendorBean,
            ValidationDataBean validationData) {
        super(lsrDataBean, vendorBean, validationData);
        this.vendorBean = vendorBean;
        this.validationData = validationData;
        this.lsrDataBean = lsrDataBean;
    }

    /*checkCAMS_TGUID_Goto method used for checking
     *Later we can do this validation
     *12 a0 V Check if CAMS account is single line only with no TGUID or goto
     *
     */
    public boolean checkCAMS_TGUID_Goto() {
        boolean flag = false;

        ImpctdApp impctApplist[] = validationData.getImpctdAppList();
        Log.write("SPSRValidator checkCAMS_TGUID_Goto impctApplist " + impctApplist);
        Gtnl gtnllist[] = validationData.getGtnlTnList();
        String actTel = lsrDataBean.getAccountTelephoneNo();
        Log.write("LSRBaseValidator checkCAMS_TGUID_Goto actTel"+ actTel+" gtnllist:" + gtnllist);
        boolean singlelineFlag = false;
        if (gtnllist != null && gtnllist.length == 1) {
            Gtnl gtnl = gtnllist[0];
            String tncams = gtnl.getGtnlTn();
              Log.write("LSRBaseValidator checkCAMS_TGUID_Goto  tncams:" + tncams );
            if (tncams != null && actTel != null) {
                tncams = tncams.trim().replaceAll("-", "");
                actTel = actTel.trim().replaceAll("-", "");
                if (tncams.equalsIgnoreCase(actTel)) {
                    singlelineFlag = true;
                }
            }
        } else if ((gtnllist != null && gtnllist.length == 0) || gtnllist == null) {
            singlelineFlag = true;
        }
        if (singlelineFlag && impctApplist != null && impctApplist.length > 0) {
            for (int i = 0; i < impctApplist.length; i++) {
                ImpctdApp impctdAppvalue = impctApplist[i];
                String appCd = impctdAppvalue.getImpctdAppCd();
                Log.write("SPSRValidator checkCAMS_TGUID_Goto appCd " + appCd);
                if (appCd != null && (singlelineFlag && !appCd.trim().equalsIgnoreCase("TGUID"))) {
                    flag = true;
                    break;
                }
            }

        }else if(singlelineFlag){
            Log.write("SPSRValidator checkCAMS_TGUID_Goto alse ");
            return flag;
        }
        //Code Change for avoiding validation with Cams for Resale Account - Saravanan 
        boolean reselFlag = checkTraitNameisResold();
        if(reselFlag)
        {
        	super.fillterSerTypeRejCode("20007-checkCAMS_TGUID_Goto :BW", true, "SP", false);
        }else
        {
        	super.fillterSerTypeRejCode("20007-checkCAMS_TGUID_Goto :BW", flag, "SP", false);      
        }
        return flag;
    }

    /*matchSPZIP_CAMS method used for checking
     *Check if SPZIP matches ZIP in CAMS (5 digits)
     *later we can add business logic below method
     */
    public boolean matchSPZIP_CAMS() {
        boolean flag = false;
        String spzip = lsrDataBean.getSimpleportZIP();
        Addr adressCams = validationData.getCustAddress();
        Log.write("SPSRValidator matchSPZIP_CAMS");
        Log.write("spzip " + spzip );
        Log.write(" AddressCams " + adressCams);
        if (adressCams != null) {
            String camsZip = adressCams.getZipCd();
            Log.write("SPSRValidator matchSPZIP_CAMS camsZip " + camsZip);

            /* -- need to ask why ZIP code does not come from CAMS and if this is a rule
            if (camsZip == null || camsZip.trim().length() == 0) {
                return flag;
            }
             */

            if (camsZip != null && camsZip.length() > 5) {
                String zips[] = camsZip.split("[-]");
                Log.write("SPSRValidator matchEU_Bill_Address zips" + zips);
                camsZip = zips[0];
                
            }
            Log.write("SPSRValidator matchEU_Bill_Address spzip " + spzip + " camsZip  " + camsZip);
            if (spzip != null && camsZip != null && spzip.trim().equalsIgnoreCase(camsZip.trim())) {
                flag = true;
            }

        } else {
            return flag;
        }
        //Code Change for avoiding validation with Cams for Resale Account - Saravanan 
        boolean reselFlag = checkTraitNameisResold();
        if(reselFlag)
        {
        	super.fillterSerTypeRejCode("20019-matchSPZIP_CAMS :BW", true, "SP", false);
        }else
        {
        	super.fillterSerTypeRejCode("20019-matchSPZIP_CAMS :BW", flag, "SP", false);      
        }
        Log.write("SPSRValidator flag returned is " + flag);
        return flag;
    }

    /*checkELT method used for checking
     *Check if ELT=A OR (ELT=B AND eligibility to
     *delete permission=true in Vendor table)
     * see in BF v13 and v14
     *If ELT = B check
     * vendor table to verify provider is eligible to delete directory
     *  v23 ELT is not the value of  "C"
     */
    public boolean checkELT() {
        String elt = lsrDataBean.getEUListTreatment();
        String direct = vendorBean.getIsDirectory();
        String delete = vendorBean.getIsEligibleToDeleteDir();
        Log.write("SPSRValidator checkELT AlL the methods elt " + elt + " direct " + direct);
        boolean flag = false;
        if (elt != null) {
            if (direct != null && elt.equals("A") && direct.equals("Y")) {
                flag = true;
            } else if (delete != null && elt.equals("B") && delete.equals("Y")) {
                flag = true;
            } else if (elt.equals("C")) {
                flag = false;
            }
        }
        super.fillterSerTypeRejCode("20018-checkELT_A", flag, "SP", false);
        return flag;
    }

    //V9 V21 V22 V26
    /*
     *  Complex ASOC No Broadband asoc exists No Voice Mail ASOCs
     *  No Ring Plus No DID, Web Hosting, ISDN, Centrex,
     *  Hunt Groups, or Key System ASOCs
     *
     */
    public boolean checkAsocList() {
        boolean flag = true;
        List ascoList = lsrDataBean.getAsocSPList();
        Log.write("SPSRValidator checkAsocList ascoList " + ascoList);
        Asoc asocArray[] = validationData.getCustAsocList();
        Log.write("SPSRValidator checkAsocList asocArray " + asocArray);
        for (int i = 0; i < asocArray.length; i++) {
            Asoc asoc = asocArray[i];
            String asocName = asoc.getAsocName();
            Log.write("SPSRValidator checkAsocList asocName " + asocName);
            boolean ascoFlag = ascoList.contains(asocName);
            Log.write("SPSRValidator checkAsocList ascoFlag " + ascoFlag);
            if (ascoFlag) {
                flag = false;
            }
        }
        //Code Change for avoiding validation with Cams for Resale Account - Saravanan 
        boolean reselFlag = checkTraitNameisResold();
        if(reselFlag)
        {
        	super.fillterSerTypeRejCode("20007-checkAsocList :BW", true, "SP", false);
        }else
        {
        	super.fillterSerTypeRejCode("20007-checkAsocList :BW", flag, "SP", false);      
        }
        return flag;
    }

    /*Override checkDDD method used for checking
     *Reference  Vendor Table for  DD interval:
     * Lower -Limit-DDD-Interval, Upper-Limit-DDD-Interval,
     * & Time-Limit-DDD-Interval. LSR Due Date must meet Interval Requirements
     * to move to Next Step, If not Reject
     */
     /*
      *DDD validation:

      *1.       The DDD Lower Limit should be hard-coded to be 1 (already implemented). So the user can never submit today�s date as the DDD and if the DDD is supplied as today�s date it would end up in a reject (Invalid DDD). And if the DDD supplied is tomorrow it will be a pass or fail based on point# 3 below.    Correct

      *2.       The Upper Limit for DDD will be as set up in the vendor table as for all other types (already implemented in 5072).   Correct/  For calendar vs. business days - This would follow the same logic we have in Express today, which I believe is set on calendar days.

      *3.       Time of the day limit has to obtained from the time zone matrix table and not from the vendor table:

      *3.a If the submitted time is before the TOD limit (as per Time Zone matrix for the state in the LSR) and the DDD supplied is tomorrow or lesser
      *than the Upper DDD limit from vendor table then DDD validation is a pass.   Correct / Business days

      *3.b If the submitted time is after the TOD limit (as per Time Zone matrix for the state in the LSR) and the DDD supplied is tomorrow
      *then DDD validation ends up in a reject (Invalid DDD).   Correct / Business days

      *3.c If the submitted time is after the TOD limit (as per Time Zone matrix for the state in the LSR) and the DDD supplied is the day after tomorrow
      *or lesser than the Upper DDD limit from vendor table then DDD validation is a pass.   Correct / Business days

      *SLA Time determination:

      *1. For simple ports the SLA time for that LSR will be decided based on below logic and will not be retrieved from the vendor table as for other types:

      *1.a If the submitted time is before the TOD limit (as per Time Zone matrix for the state in the LSR) and DDD supplied is tomorrow the SLA time for
      *that LSR will be 4 hours regardless of the outcome of the DDD validation.   Correct with one addition.  The DDD supplied can be either tomorrow or the day after tomorrow.  In both instances, the response is due within 4 hours.    / Business days

      *1.b If the submitted time is after the TOD limit (as per Time Zone matrix for the state in the LSR) and DDD supplied is the day after tomorrow or a
      *date greater than day after tomorrow the SLA time for that LSR will be calculated such that SLA timer will trigger at 12 PM the next day regardless
      *of the outcome of the DDD validation.

      *1.b.c An exception to 1.b above would be that if the DDD supplied is more than 3 days from the time of submission of the LSR then the SLA time
      *for that LSR will be 24 hours regardless of the outcome of the DDD validation.   1.b and 1.b.c. are correct /Business days for both

      */


    public boolean checkDDD() {

        boolean flag = false;
        boolean holidayflag = false;
        Date dueDate = null;
        if (lsrDataBean.getDesiedDueDate() != null) {
            dueDate = dateToString(lsrDataBean.getDesiedDueDate());
            Calendar calendar1 = Calendar.getInstance();
            Calendar currentDate = Calendar.getInstance();
  
            calendar1.setTime(dueDate);
            Log.write("---checkDDD -dat--- :: " + calendar1.getTime());
            currentDate.add(currentDate.DATE, 0);
            Log.write("----checkDDD-currentDate-- : " + currentDate.getTime());

            int diffDDDCurrDate = calendar1.DAY_OF_YEAR - currentDate.DAY_OF_YEAR;

            Calendar calCurrent = Calendar.getInstance();
            calCurrent.add(calCurrent.DATE, -1);
            Log.write("----checkDDD-calCurrent-- : " + calCurrent.getTime());
            Log.write("----checkDDD-holdayVector-- : " + holdayVector);

            if ((calendar1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
                    holdayVector.contains(dateToStringYYMMDD(calendar1.getTime())))) {
                holidayflag = true;
            }//keep this logic as we still need to reject if the DDD value is a SAT, SUN or WIN holiday


            Log.write("----checkDDD-holidayflag--: " + holidayflag);
            if (calCurrent.after(calendar1) || holidayflag) {

                //this method will be called only for a Simple Port so comment other types
                Log.write("--checkDDD--Rejecting Date-- if: ");
                //fillterSerTypeRejCode("70013-checkDDD", flag, "UNEPV", false);
                //fillterSerTypeRejCode("50010-checkDDD", flag, "UNEPD", false);
                //fillterSerTypeRejCode("40010-checkDDD", flag, "RED", false);
                //fillterSerTypeRejCode("80010-checkDDD", flag, "UNEPS", false);
                //fillterSerTypeRejCode("60011-checkDDD", flag, "RES", false);
                //fillterSerTypeRejCode("10015-checkDDD", flag, "NPV", false);
                fillterSerTypeRejCode("20017-checkDDD", flag, "SP", false);
                //fillterSerTypeRejCode("30014-checkDDD", flag, "DAD", false);
                return false;
            }


            int time = currentDate.get(currentDate.HOUR_OF_DAY);
            String minstr = "0." + currentDate.get(currentDate.MINUTE);
            Log.write("=checkDDD=minstr=" + minstr);
            float min = Float.parseFloat(minstr);
            double totaltime = time + min;
            Log.write("=checkDDD totaltime==" + totaltime);

            LSRdao lsrDao = new LSRdao();

            //String todstr = vendorBean.getValidTimeOfDayDDD();
            //change for Simple Ports project - Antony - 01/11/2011

            Map mapAddress = lsrDataBean.getAddressMap();
            AddressBean addbean = (AddressBean) mapAddress.get("EU_LA");

            String todstr = lsrDao.getTODLimit(addbean.getState());

            int tod = Integer.parseInt(todstr);
            Log.write("=checkDDD tod==" + tod);

            int diffendays;
            //code change for bug 1277
            int llDiffInDays;//separate variable for checking lower limit diff in days as we need to exclude SAT,SUN or WIN holidays here

            if (totaltime < tod) {

                //code change for Simple Ports project - Antony - 01/11/2011
                //if lsr came in before tod today then today should be considered in the count
                diffendays = 1;
                llDiffInDays = 1;//code change for bug 1277

                Log.write("==if=totaltime < tod diffendays" + diffendays);

            } else {
                //if lsr came after tod today then today should not be considered in the count
                diffendays = 0;
                llDiffInDays = 0;//code change for bug 1277

                Log.write("==else=diffendays" + diffendays);
            }


            Log.write("before while LBV diffendays" + diffendays);
            Log.write("before while LBV llDiffInDays" + llDiffInDays);
            boolean sameday = true;
            while (currentDate.before(calendar1)) {
                sameday = false;
                boolean hoFlag = holdayVector.contains(dateToStringYYMMDD(calendar1.getTime()));

                if ((calendar1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                        calendar1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
                        holdayVector.contains(dateToStringYYMMDD(calendar1.getTime())))) {
                    Log.write("===holdayVector.contains==hoFlag " + hoFlag);
                    //code change for bug 1277
                    diffendays++;//for upper limit diff in days count we need to include calendar days so need to increment here too
                } else {
                    diffendays++;
                    //code change for bug 1277
                    llDiffInDays++;//for lower limit diff in days count we need to include only business days so need to increment here only
                }
                calendar1.add(Calendar.DAY_OF_WEEK, -1);
            }


            //code to set slatime for simple port LSRs

            int slatime = 4;

            Log.write("SLA Time arrived after setting default"+slatime) ;

            if(totaltime < tod)  {
                slatime = 4;
                Log.write("SLA Time arrived at inside if totaltime < tod"+slatime) ;
            } else {
                llDiffInDays = llDiffInDays - 1;

                Log.write("SLA Time arrived at outside if totaltime < tod else"+slatime) ;

                if(llDiffInDays < 3)
                    slatime = 12;
                else
                    slatime = 24;

                Log.write("SLA Time arrived after outside if totaltime < tod else"+slatime) ;

                llDiffInDays = llDiffInDays + 1;
            }

            Log.write("SLA Time arrived at outside if totaltime < tod"+slatime) ;

            Log.write("SLA Time string value of "+String.valueOf(slatime)) ;


            //lsrDataBean.setSLATimeForSP(String.valueOf(slatime));

            lsrDao.updateSLATimeForSP(lsrDataBean.getReqstNmbr(),lsrDataBean.getReqstVer(),String.valueOf(slatime));

            //hardcode always to be one for Simple Ports
            int lowerDate = 1;

            int upperDate = Integer.parseInt(vendorBean.getDueDateUpperLimit());

            Log.write(" checkDDD diffendays " + diffendays + " lowerDate " + lowerDate + " upperDate " + upperDate + " sameday "
                    + sameday + "llDiffInDays "+llDiffInDays);

            if (llDiffInDays > lowerDate && diffendays <= upperDate) {
                Log.write("===diffDays if statisfied ");
                if (sameday) {
                    Log.write("===diffDays same day ");
                    if (totaltime < tod) {
                        flag = true;
                        Log.write("====totaltime<tod");
                    } else {
                        Log.write("==else==totaltime<tod");
                        flag = false;
                    }
                }  else {
                    Log.write("===else block ==UL_LL==");
                    flag = true;
                }
            }
        }

        //this method will be called only for a Simple Port so comment other types
        //fillterSerTypeRejCode("70013-checkDDD", flag, "UNEPV", false);
        //fillterSerTypeRejCode("50010-checkDDD", flag, "UNEPD", false);
        //fillterSerTypeRejCode("40010-checkDDD", flag, "RED", false);
        //fillterSerTypeRejCode("80010-checkDDD", flag, "UNEPS", false);
        //fillterSerTypeRejCode("60011-checkDDD", flag, "RES", false);
        //fillterSerTypeRejCode("10015-checkDDD", flag, "NPV", false);
        fillterSerTypeRejCode("20017-checkDDD", flag, "SP", false);
        //fillterSerTypeRejCode("30014-checkDDD", flag, "DAD", false);

        return flag;
        
    }

    /*checkGiftBill method used for checking
     *GIFT billing is available for the given telephone number
     */
     public boolean checkGiftBill() {
		 Log.write("SPSRValidator checkGiftBill calling :: ");
		 String giftBill = validationData.getGiftService();
		 Log.write("SPSRValidator checkGiftBill giftBill  " + giftBill);

		 boolean flag = true;
		 if (giftBill.equals("Y")) {
		     flag = false;
	     }

	     fillterSerTypeRejCode("20007-checkGiftBill :BW", flag, "SP", false);
	     return flag;
	 }

	 /*checkComplexASOC_Ind method used for checking
	  *Complex ASOC. If the Indicator is Y, the order is complex order,
	  *else the order is simple order.
	  */
	 public boolean checkComplexASOC_Ind() {
		 Log.write("SPSRValidator checkComplexASOC calling :: ");
		 String complexAsoc = validationData.getComplex();
		 Log.write("SPSRValidator checkComplexASOC complexAsoc  " + complexAsoc);

		 boolean flag = true;
		 if (complexAsoc.equals("Y")) {
			  flag = false;
		 }

		 fillterSerTypeRejCode("20007-checkComplexASOC_Ind :BW", flag, "SP", false);
		 return flag;
	 }
         
    
     
    /* method to check SV status in SOA for SUPP Q&V  - Antony - 05/02/2011*/
    /* 
    public boolean checkSVStatusInSOA(String checkStatus) 
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

            String[] respArray = stub.processSync(header,body);
            Log.write("processSync returned: soapParam1=" + respArray[0] + " =" + respArray[1] );
            
            
            if(respArray[1].indexOf("SOARequestStatus value=\"success\"") > 0) {
                
                String soapSyncResponse = respArray[1]; 
                    
                int startIndex = 0;
                int endIndex = 0;

                String svStatus = "";
                String NNSPValueInSV = "";

                
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
                
                
                if(checkStatus.equals("pending")) {
                    if(svStatus.equals("pending") || svStatus.equals("disconnect-pending") || svStatus.equals("conflict") || svStatus.equals("canceled") || svStatus.equals("cancel-pending")) {
                        //proceed to check if ONSP is WIN SPID
                   
                        
                        if(!(NNSPValueInSV.equals("1180") ||
                             NNSPValueInSV.equals("4263") ||
                             NNSPValueInSV.equals("8334") ||
                             NNSPValueInSV.equals("0999") ||
                             NNSPValueInSV.equals("2147") ||
                             NNSPValueInSV.equals("1226") ||
                             NNSPValueInSV.equals("938D") ||
                             NNSPValueInSV.equals("7815") ||
                             NNSPValueInSV.equals("1482")
                            )) {//add method call here to pass ONSP to db method and check win_spids table for presence of ONSP
                            //validation is a pass -- add to passed array here
                            Log.write("SUPP1 PON passed SOA SV status validation!");
                            result = true;
                            
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:nonWINSPID", result, "NPV", false); //add reason code sqnc nmbr
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:nonWINSPID", result, "SP", false); //add reason code sqnc nmbr
                            
                            if(svStatus.equals("canceled") || svStatus.equals("cancel-pending")) {
                                Log.write("SV status is canceled or cancel-pending so skip V6 & V9");
                                result = false;
                            }
                        } else {
                            //reject with no further validations -- this method should be called in processInitialValidations
                            //add to reject/error array with error message "NNSP in SV is not a Windstream SPID"
                            //do not add reject for this case as per new validation change. Just add a pass. - V4,V6&V9 -- Antony -- 07/11/2011
                            result = true;
                            
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:WINSPID", result, "NPV", false);
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:WINSPID", result, "SP", false); //add reason code sqnc nmbr
                            
                            Log.write("NNSP in SV is a Windstream SPID. So skip V6&V9..");
                            result = false;
                            
                        }
                    } else if(svStatus.equals("active")) {
                        
                        Log.write("SV status is active.");
                        
                        if(  NNSPValueInSV.equals("1180") ||
                             NNSPValueInSV.equals("4263") ||
                             NNSPValueInSV.equals("8334") ||
                             NNSPValueInSV.equals("0999") ||
                             NNSPValueInSV.equals("2147") ||
                             NNSPValueInSV.equals("1226") ||
                             NNSPValueInSV.equals("938D") ||
                             NNSPValueInSV.equals("7815") ||
                             NNSPValueInSV.equals("1482")
                          ) {
                        
                            Log.write("NNSP is a windstream SPID so V4 is a pass.Skip V6&V9");
                            
                            result = true;
                            
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:WINDSPID", result, "NPV", false); //add reason code sqnc nmbr
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:WINDSPID", result, "SP", false); //add reason code sqnc nmbr
                            
                            //make result false as we need to skip V6&V9
                            result = false;
                            
                        } else {
                            Log.write("SV is a non-windstream SPID so V4 is a reject.Fatal reject.Skip V5,V6&V9");
                            
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:nonWINDSPID", result, "NPV", false); //add reason code sqnc nmbr
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:nonWINDSPID", result, "SP", false); //add reason code sqnc nmbr
                            
                            //make result false as we need to skip V6&V9
                            result = false;
                            
                        }
                        
                    } else {
                        //reject with no further validations -- this method should be called in processInitialValidations
                        //add to reject/error array with error message "SV status in SOA is not valid for sending a CancelSV request."
                        fillterSerTypeRejCode("100039-checkSVStatusInSOAPending:invalidstatus", result, "SP", false); //add reason code sqnc nmbr
                        fillterSerTypeRejCode("100039-checkSVStatusInSOAPendinginvalidstatus", result, "NPV", false); //add reason code sqnc nmbr
                        Log.write("SV status in SOA is not valid for sending a CancelSV request.So skip V6&V9");
                        
                        result = false;
                    }
                } else {
                    if(svStatus.equals("canceled") || svStatus.equals("cancel-pending")) {
                         Log.write("SUPP1 PON passed SOA check SV status cancelled validation!");
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
                }
                
                    
                
            } else {
                //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
                fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
                fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
                Log.write("Obtained unexpected response for svQueryRequest. Unable to check status of SV in SOA.");
            }
            
                
	    
        }
        catch (java.net.MalformedURLException e)
        {
            e.printStackTrace();        
            Log.write("MalformedURLException thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
 
            //throw new Exception("Invalid SOAP Service URL: " + e );
        }
        catch (java.rmi.RemoteException e)
        {

            e.printStackTrace();        
            Log.write("RemoteException thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
            //throw new Exception("Invalid SOAP Service URL: " + e );
            
            
        }
        catch (Exception e)
        {
            e.printStackTrace();        
            Log.write("Exception thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
            //throw new Exception("Invalid SOAP Service URL: " + e );
        }
        
        return result;
    }
*/
    /* method to send cancel request for SV in SOA for SUPP Q&V  - Antony - 05/02/2011*/
     
    public boolean cancelSVInSOA() 
    {
        boolean result = false; 
        
        LSRdao lsrDao = new LSRdao();
        
        //String atn = lsrDataBean.getAccountTelephoneNo(); -- will be blank as atn on LSR form is empty for Simple Orders
        //String lsrONSP = "1180"; //get using state cd,atn using lsrdao method -- todo
        
        List tnList = lsrDataBean.getPortedNBR();
        
        String atn = (String) tnList.get(0);
        
        Log.write("ATN from tnList :"+atn);
        
        String lsrONSP = lsrDao.getONSP(lsrDataBean.getReqstNmbr(),atn);
        
        Log.write("value of lsrONSP : "+lsrONSP);

        try
        {
        
          //populate header
          //get SOAP URL
          String userid = PropertiesManager.getProperty("lsr.SOA.userid");
          String passwd = PropertiesManager.getProperty("lsr.SOA.passwd");
          String domain = PropertiesManager.getProperty("lsr.SOA.domain");
          String soapURL = PropertiesManager.getProperty("lsr.SOA.soapURL");
    
                    
          //header and body for svCancelRequest
          String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                            "<header>"+
                               "<Request value=\"SOARequest\"/>"+
                               "<Subrequest value=\"SvCancelRequest\"/>"+
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
          Log.write("Date value sent in svCancelRequest xml:"+strCurrentDate);
        
          //populate body
          String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                "<SOAMessage>"+
                                    "<UpstreamToSOA>"+
                                        "<UpstreamToSOAHeader>"+
                                            "<InitSPID value=\""+lsrONSP+"\" />"+//
                                            "<DateSent value=\""+strCurrentDate+"\" />"+//
                                            "<Action value=\"submit\" />"+
                                        "</UpstreamToSOAHeader>"+
                                        "<UpstreamToSOABody>"+
                                            "<SvCancelRequest>"+
                                                        "<Subscription>"+
                                                            "<Tn value=\""+atn+"\" />"+//
                                                        "</Subscription>"+
                                            "</SvCancelRequest>"+
                                        "</UpstreamToSOABody>"+
                                    "</UpstreamToSOA>"+
                                "</SOAMessage>";

            SOAPRequestHandlerServiceLocator serviceLocator = new SOAPRequestHandlerServiceLocator();

            SOAPRequestHandlerSoapBindingStub stub = (SOAPRequestHandlerSoapBindingStub) serviceLocator.getSOAPRequestHandler(new java.net.URL(soapURL));

	    Log.write( "processSync called...");
            Log.write( "contacting server [" + soapURL +"]\n" + 
			"with soapParam1 ["+header+"]\n" + "and soapParam2 ["+body+"]\n");

            String[] respArray = stub.processSync(header,body);
            Log.write("processSync returned: soapParam1=" + respArray[0] + " =" + respArray[1] );
            
            
            if(respArray[1].indexOf("RequestStatus value=\"success\"") > 0) {
                result = true;
                Log.write("SUPP1 PON passed SOA SV Cancel request validation! Proceeding to cancel DCRIS order...");
            
            } else {
                //send to MR -- "Unable to cancel SV in SOA.Error while calling SOA API."
                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11001","Unable to cancel SV in SOA.Error while calling SOA API.");
                Log.write("Obtained unexpected response for svCancelRequest. Unable to cancel SV in SOA.");
            }
            
        }
        catch (java.net.MalformedURLException e)
        {
            e.printStackTrace();        
            Log.write("MalformedURLException thrown in cancelSVInSOA method : "+e.getMessage());
	    //send to MR -- "Unable to cancel SV in SOA.Error while calling SOA API."
            
            try {
                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11001","Unable to cancel SV in SOA.Error while calling SOA API.");
            } catch(Exception ex) {
                    Log.write("DB Exception while calling MR status update procedure:"+ex.getMessage());
            }
            //throw new Exception("Invalid SOAP Service URL: " + e );
        }
        catch (java.rmi.RemoteException e)
        {

            e.printStackTrace();        
            Log.write("RemoteException thrown in cancelSVInSOA method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to cancel SV in SOA.Error while calling SOA API."
            //throw new Exception("Invalid SOAP Service URL: " + e );
            
            String errorMessage = e.getMessage();
            
                        
            if(errorMessage.indexOf("RULE_ID value=\"Status\"") > 0){
                //send to MR -- "A subscription version must have a status of (pending), (conflict), or (disconnect-pending) in order to send a cancel request.."
                try {
                    lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11002","Error while sending cancel SV to SOA: A subscription version must have a status of (pending), (conflict), or (disconnect-pending) in order to send a cancel request.");
                } catch(Exception ex) {
                    Log.write("DB Exception while calling MR status update procedure:"+ex.getMessage());
                }    
                Log.write("Encountered an AxisFault: A subscription version must have a status of (pending), (conflict), or (disconnect-pending) in order to send a cancel request.");
            } else if(errorMessage.indexOf("RULE_ID value=\"exists\"") > 0) { 
                //send to MR -- "This request type SvCancelRequest is not allowed when the subscription version does not exist.."
                try {
                    lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11003","Error while sending cancel SV to SOA: This request type SvCancelRequest is not allowed when the subscription version does not exist.");
                } catch(Exception ex) {
                    Log.write("DB Exception while calling MR status update procedure:"+ex.getMessage());
                }
                Log.write("Encountered an AxisFault: This request type SvCancelRequest is not allowed when the subscription version does not exist.");
            } else {
                //send to MR -- "Unable to Cancel SV in SOA due to exception at SOA.."
                try {
                    lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11004","Error while sending cancel SV to SOA: Unable to Cancel SV in SOA due to exception at SOA.");
                } catch(Exception ex) {
                    Log.write("DB Exception while calling MR status update procedure:"+ex.getMessage());
                }
                Log.write("Encountered an AxisFault. Unable to Cancel SV in SOA due to exception at SOA.");
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();        
            Log.write("Exception thrown in cancelSVInSOA method : "+e.getMessage());
	    //send to MR -- "Unable to Cancel SV in SOA due to exception at SOA.."
            try {
                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11005","Error while sending cancel SV to SOA: Unable to Cancel SV in SOA due to exception at SOA.");
            } catch(Exception ex) {
                    Log.write("DB Exception while calling MR status update procedure:"+ex.getMessage());
            }
            Log.write("Exception : Unable to Cancel SV in SOA due to exception at SOA.");
            //throw new Exception("Invalid SOAP Service URL: " + e );
        }
        
        return result;
    }
    
    /* -- commenting method as it is defined in base class and that is the version to be used*/
    /*method to send cancel dcris order to BW WS -- Antony - 05/05/2011*/
    
    /*
    public boolean sendCancelOrderToDCRISWS() {
            
            String rqstSqncNmbr = "";
            String rqstVersion = "";
            String rqstPON = "";
            String strOCNcd = "";       
            
            boolean result = false;
        
            LSRdao lsrDao = new LSRdao();
        
            
            OpCancelOrderRequest cancelRequest = new OpCancelOrderRequest();
            
            //code from BWDCRISOrderRequest -- start
            try {
                List tnList = lsrDataBean.getPortedNBR();
        
                String atn = (String) tnList.get(0);

                Log.write("ATN from tnList :"+atn);
                
                String OLDSPValue = "1180";//todo -- get from win spids table for given state and npa
                
                Hashtable htDCRISOrderParams = lsrDao.retrieveDCRISOrderParams(atn,OLDSPValue);
            
                
            

                rqstSqncNmbr = (String) htDCRISOrderParams.get("RQST_SQNC_NMBR");
                rqstVersion = (String) htDCRISOrderParams.get("RQST_VRSN");
                rqstPON = (String) htDCRISOrderParams.get("RQST_PON");
                String strBOID = "";//get BOID BEX value after splitting order number
                String strBEX = "";
                String strOrderNumber = (String) htDCRISOrderParams.get("ORDER_NO");
                strOCNcd = (String) htDCRISOrderParams.get("OCN_CD");
                String strSPFlag = (String) htDCRISOrderParams.get("SP_FLAG");
                String strCLECInd = (String) htDCRISOrderParams.get("CLEC_IND");

             
                
                
                Log.write("BW Dcris order webServiceInvoke calling (cancel order call): ");
                ExpressWebService expressWebserviceImpl = new ExpressWebService_Impl();
            
                ExpressOrderWebLayer expressWebLayer = expressWebserviceImpl.getExpressOrderWebLayer();

                //code to change Endpoint URL dynamically based on lsr.properties entry for lsr.bwexpressord.URL
                ExpressOrderWebLayer_Stub ewlStub = (ExpressOrderWebLayer_Stub)expressWebLayer;
                
                String url = PropertiesManager.getProperty("lsr.bwexpressord.URL","");
                URL urlString = new URL(url);
                
                Log.write("BW ExpressOrd URL prior to dynamic setting : "+ewlStub._getTargetEndpoint());
                ewlStub._setTargetEndpoint(urlString);
                Log.write("BW ExpressOrd URL after to dynamic setting : "+ewlStub._getTargetEndpoint());

                boolean cancelOrderRequestSent = false;
                String strNotification = "";
                               
                
                String atnWithoutHyphens = atn.replaceAll("-","");//soa_t
                
                if(strOrderNumber != null) {
                    strOrderNumber = strOrderNumber.trim();
                    String [] orderArray = strOrderNumber.split(" ");
                    OrderData[] orderData = new OrderData[orderArray.length-2];
                    
                    strBEX = orderArray[0];
                    strBOID = orderArray[orderArray.length - 1];

                    System.out.print("size of orderArray: "+orderArray.length);
                    System.out.print("strBEX: "+strBEX);
                    System.out.print("strBOID: "+strBOID);
                    
                    for(int i = 1; i < orderArray.length - 1; i++) {
                        orderData[i-1] = new OrderData();

                        orderData[i-1].setBex(strBEX);//lr_t
                        orderData[i-1].setBoid(strBOID);//lr_t
                        orderData[i-1].setOrderNumber(orderArray[i]);//lr_t    
                        
                        System.out.print("orderData order number : "+orderData[i-1].getOrderNumber());
                    }
                    
                    cancelRequest.setArrayOfOrderData(orderData);
                } else {
                    //send to MR with message "Internal Error: Unable to find order number data in Express db!"
                    lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11006","Internal Error while sending cancel order request to DCRIS: Unable to find order number data in Express db!");
                    Log.write("Internal Error while sending second order request to DCRIS: Unable to find order number data in Express db!");
                    return false;
                }
                
                ApplicationInfo appInfo = new ApplicationInfo();
                appInfo.setApplicationID("Fqyc@tUFosBi4xY0FnM34FLRW8!");
                appInfo.setApplicationName("EXPRESS");
            
            
                cancelRequest.setApplicationInfo(appInfo);
                           
            
                cancelRequest.setExpressOrderType("NP");//always NP as we do port-outs only for Number Port PONs
                cancelRequest.setPonNumber(rqstPON);//req_t
                cancelRequest.setPonVersion(rqstVersion);//soa_t
                cancelRequest.setATN(atnWithoutHyphens);//soa_t
                cancelRequest.setOcnNumber(strOCNcd);//req_t
                //cancelRequest.setClecInd(strCLECInd);//company_type_t
                //cancelRequest.setSimpleInd(strSPFlag);//req_t
                cancelRequest.setTransactionID("123456789012345678901234567");
                cancelRequest.setUserID("EXP5072A");
                cancelRequest.setBusDaysToDueDate("1");//hard-code for now
                //cancelRequest.setSoaActivationStatus("success");

                Log.write("Cancel DCRIS order request content sent : "+cancelRequest.toString());
                
                OpCancelOrderReply cancelRequestReply = expressWebLayer.opCancelOrder(cancelRequest);
                
                Log.write("Cancel DCRIS order request reply content received : "+cancelRequestReply.toString());

                if(cancelRequestReply != null) {
                    ErrorInfo resultStr = cancelRequestReply.getErrorInfo();
                    strNotification = resultStr.toString();
                    
                    if(resultStr.getErrorID().equals("0000")) {
                        Log.write("DCRIS order cancel call successful for PON: "+rqstPON+" Version: "+rqstVersion+". Proceeding to add PON in SLA queue...");
                        Log.write("DCRIS order cancel call successful for PON: "+rqstPON+" Version: "+rqstVersion);
                        result = true;
                    } else {
                        if(rqstSqncNmbr != null) {
                           try {
                            lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11007","Error Response received from BW dcris order webservice: "+resultStr.getErrorID()+"-"+resultStr.getErrorMsg());
                           } catch(Exception dbEx) {
                            Log.write("DCRIS order cancel call failed for PON: "+lsrDataBean.getReqstPon()+" Version: "+lsrDataBean.getReqstVer()+". Unable to update to MR status. Please check db being called.");
                           }
                        } else {
                            Log.write("DCRIS order cancel call failed for PON: "+lsrDataBean.getReqstPon()+" Version: "+lsrDataBean.getReqstVer()+". Please check db being called.");
                        }
                            
                    }
                    //if(strNotification) -- check here for 0000 returned for the errorID for cancel call to DCRIS WS also check for errorMsg = Success
                    //if 0000 set return boolean parameter as true and false for all other cases
                } else {
                    //need to send to MR as notification is not valid
                    
                    if(rqstSqncNmbr != null) {
                       try {
                        lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11008","Null Response for cancel order received from BW dcris order webservice. Internal Error businessware down.");
                       } catch(Exception dbEx) {
                        Log.write("DCRIS order cancel call failed for PON: "+lsrDataBean.getReqstPon()+" Version: "+lsrDataBean.getReqstVer()+". Cancel Order - Unable to update to MR status. Please check db being called.");
                       }
                    } else {
                        Log.write("DCRIS order cancel call failed for PON: "+lsrDataBean.getReqstPon()+" Version: "+lsrDataBean.getReqstVer()+". Please check db being called.");
                    }
                }

                Log.write("cancel DCRIS order call response :"+strNotification);
                
                
            
        } catch(JAXRPCException jx) {
            // BW Dcris Order webservice down call MR stored procedure to update with error
            if(rqstSqncNmbr != null) {
               try {
                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11009","Null Response received for cancel order from BW dcris order webservice. Internal Error businessware down-JAXRPCException thrown");
               } catch(Exception dbEx) {
                Log.write("DCRIS order cancel call failed for PON: "+lsrDataBean.getReqstPon()+" Version: "+lsrDataBean.getReqstVer()+". Unable to update to MR status. Please check db being called.");
               }
            } else {
                Log.write("DCRIS order cancel call failed for PON: "+lsrDataBean.getReqstPon()+" Version: "+lsrDataBean.getReqstVer()+". Please check db being called.");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            Log.write("Exception :"+ex.getMessage());
            
            // BW Dcris Order webservice down call MR stored procedure to update with error
            if(rqstSqncNmbr != null) {
               try {
                lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"11010","Null Response received for cancel order from BW dcris order webservice. Internal Error businessware down-Exception thrown");
               } catch(Exception dbEx) {
                Log.write("DCRIS order cancel call failed for PON: "+lsrDataBean.getReqstPon()+" Version: "+lsrDataBean.getReqstVer()+". Unable to update to MR status. Please check db being called.");
               }
            } else {
                Log.write("DCRIS order cancel call failed for PON: "+lsrDataBean.getReqstPon()+" Version: "+lsrDataBean.getReqstVer()+". Please check db being called.");
            }
            
        }
            return result;
    }
    */
    
    /*method to check for FOC DDD/LSR DDD for SUPPs -- Antony -- 05/10/2011*/
    
    public boolean checkDDDForSUPP() {
        
     boolean focdateflag = false;
     LSRdao lsrDao = new LSRdao();
     
     String strVersion = lsrDataBean.getReqstVer();
     int reqVersion = Integer.parseInt(strVersion);
     
     reqVersion = reqVersion - 1;
     
     strVersion = String.valueOf(reqVersion);
     
     String focdueDate = lsrDao.getFOCDDD(lsrDataBean.getReqstNmbr(),strVersion);
     
     if (focdueDate == null || focdueDate.length() == 0) { 
         focdueDate = lsrDataBean.getDesiedDueDate() + " 21:00:00";//if null use lsrDataBean LSR DDD and validate; retrieve from time_zone table later
         Log.write("Resetting FOC DDD to LSR DDD....");
      /*else {//we have foc date in lr_t //commented block as getFOCDDD always returns 21:00 even if no DDD in LR_T
       // Antony - 02142013
         focdueDate = focdueDate + " 21:00:00";
     }*/  
     //fix to avoid 21:00 21:00 error - first get DDD from LSR form and then add 21:00 and overwrite focduedate
     // Antony - 02142013
     } else if (focdueDate.equals(" 21:00:00")) {//we don't have foc date in lr_t but 21:00 string has been added to an empty string
         focdueDate = lsrDataBean.getDesiedDueDate() + focdueDate;
     }
     
     String DATE_FORMAT = "MM-dd-yyyy hh:mm:ss";
     //Create object of SimpleDateFormat and pass the desired date format.
     SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);


     Date FOCddd;
     try {
        FOCddd = sdf.parse(focdueDate);
        Log.write("FOC DDD is " + sdf.parse(focdueDate));
     } catch (ParseException ex) {
        ex.printStackTrace();
        Log.write("Unable to parse FOC DDD. Exception thrown : "+ex.getMessage());
        return false;
     }

     Calendar calendarfocDD = Calendar.getInstance();
     Calendar currentDate = Calendar.getInstance();
     Log.write("currentDate :"+currentDate.toString());

     calendarfocDD.setTime(FOCddd);
    
     if (calendarfocDD.after(currentDate)) {
        focdateflag = true;
     }
            
     //fix for Sup after FOC date change - Antony - 10/30/2012
     Log.write("PON SUPP type: "+lsrDataBean.getSupplementalType());
     if(lsrDataBean.getSupplementalType() != null && lsrDataBean.getSupplementalType().equals("1")) {
        focdateflag = true;
     }
          
     if(focdateflag)
         Log.write("FOC due date not past yet.");
     else
         Log.write("FOC due date is past.");
     
     
     //add reject or pass to vector
     //fix for SPIRA incident 3786 -- changed reason code sequence number to "SUP after FOC date"
     //Antony -- 06/29/2011
     //fillterSerTypeRejCode("20017-checkDDD", focdateflag, "SP", false);
     fillterSerTypeRejCode("20001-checkDDDForSupp", focdateflag, "SP", false);
     fillterSerTypeRejCode("10001-checkDDDForSupp", focdateflag, "NPV", false);
     
     return focdateflag;
    }
    
    /*method to cancel previous version of PON - 04/26/2011 - Satish*/
    public boolean cancelPrevVrsnInSLAQueue() {
        LSRdao lsrDao = new LSRdao();
        
        int result = lsrDao.updateSLATimerQueueforSUP(lsrDataBean.getReqstNmbr(),lsrDataBean.getReqstVer());
        
        if(result == 1)
            return true;
        else
            return false;
        
    }
}