/*
 * LSRBaseValidator.java
 * Created on June 19, 2009, 2:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.automation.validator;

import biz.neustar.www.clearinghouse.SOAPRequestHandler._1_0.SOAPRequestHandlerServiceLocator;
import biz.neustar.www.clearinghouse.SOAPRequestHandler._1_0.SOAPRequestHandlerSoapBindingStub;
import com.alltel.lsr.common.util.Log;
import com.alltel.lsr.common.util.PropertiesManager;
import com.automation.bean.AddressBean;
import com.automation.bean.LSRDataBean;
import com.automation.bean.ValidationDataBean;
import com.automation.bean.VendorTableDataBean;
import com.automation.validator.SOAHelper;
import com.automation.dao.LSRdao;
import com.windstream.expressorder.webservice.ApplicationInfo;
import com.windstream.expressorder.webservice.ErrorInfo;
import com.windstream.expressorder.webservice.OpCancelOrderReply;
import com.windstream.expressorder.webservice.OpCancelOrderRequest;
import com.windstream.expressorder.webservice.OrderData;

import com.windstream.winexpcustprof.AddLn;
import com.windstream.winexpcustprof.Addr;
import com.windstream.winexpcustprof.Asoc;
import com.windstream.winexpcustprof.Ctrt;
import com.windstream.winexpcustprof.Gtnl;
import com.windstream.winexpcustprof.OrderSegment;
import com.windstream.winexpcustprof.PndngOrder;
import com.windstream.winexpcustprof.SneData;
import com.windstream.winexpcustprof.TnTrait;
import java.net.URL;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.rpc.JAXRPCException;
import webservice.expressorder.windstream.com.ExpressOrderWebLayer;
import webservice.expressorder.windstream.com.ExpressOrderWebLayer_Stub;
import webservice.expressorder.windstream.com.ExpressWebService;
import webservice.expressorder.windstream.com.ExpressWebService_Impl;

/**
 *
 * @author kumar.k
 */
public class LSRBaseValidator {

    private VendorTableDataBean vendorBean;
    private ValidationDataBean validationData;
    private LSRDataBean lsrDataBean;
    private Vector rejectionvectorfail;
    private Vector rejectionvectorPass;
    private Vector rejvctrManual;
    private String statusManual = "";
    private String serType = null;
    public Vector holdayVector = null;
    public Map streetAddrsMap = null;
    private int gtnlLenth = 0;
    private String gtnlNumbrs;
    private String unepAsocValue;
    private boolean doPasscodeValidation;
    private LSRdao lsrDao;
    
    /** Creates a new instance of LSRBaseValidator */
    public LSRBaseValidator(LSRDataBean lsrDataBean, VendorTableDataBean vendorBean,
            ValidationDataBean validationData) {
        Log.write("LSRBaseValidator constructor , LSRDataBean: " + lsrDataBean );
        Log.write("LSRBaseValidator constructor , VendorTableDataBean: " + vendorBean );
        Log.write("LSRBaseValidator constructor , ValidationDataBean: " + validationData );
        this.vendorBean = vendorBean;
        this.validationData = validationData;
        this.lsrDataBean = lsrDataBean;
        rejectionvectorfail = new Vector();
        rejectionvectorPass = new Vector();
        rejvctrManual = new Vector();
        lsrDao = new LSRdao();
        
        getReqSerActType();
    }

    public LSRBaseValidator(LSRDataBean lsrDataBean) {
        this.lsrDataBean = lsrDataBean;
    }

    /*
     * fillterSerTypeRejCode method used for filltering validation
     * for service ,activity types and adding Rejection Code for
     * Vectors depends upon the request.
     */
    public void fillterSerTypeRejCode(String rejCode, boolean flag,
            String serTypeValue, boolean flagMaual) {

        if (serType.equals(serTypeValue)) {
            if (!flagMaual) {
                if (rejCode != null && flag) {
                    rejectionvectorPass.add(rejCode);
                } else if (rejCode != null) {
                    rejectionvectorfail.add(rejCode);
                }
            } else {
                rejvctrManual.add(rejCode);

            }
        }
    }

    /*
     * getReqSerActType method used for getting Validation Flow
     *
     */
    public String getReqSerActType() {
        Log.write("LSRBaseValidator getReqSerActType calling ");
        String serviceTy = lsrDataBean.getSerRequestType();
        String acty = lsrDataBean.getActivity();
        String retype = lsrDataBean.getReqType();
        Log.write("LSRBaseValidator getReqSerActType serviceTy: " + serviceTy + " acty :" + acty + " retype: " + retype);
        

        LSRdao lsrDao = new LSRdao();
        String simplePortFlag = lsrDao.retrieveSPFlag(lsrDataBean.getReqstNmbr(),lsrDataBean.getReqstVer());

               
        if (serType != null) {
            return serType;
        }
        if (serviceTy != null && acty != null && retype != null) {
            //added if check for Simple Ports - Antony - 12/10/2010
            if (serviceTy.equals("C") &&
                    acty.equals("V") && (retype.equals("B") || retype.equals("B1") || retype.equals("B2") || retype.equals("B3")) && 
                    simplePortFlag.equals("Y")) {
                serType = "SP";
                Log.write("==NP Simple Port===");
            } else if (serviceTy.equals("C") &&//if not Simple Port it is a NP Complex port
                    acty.equals("V") && (retype.equals("B") || retype.equals("B1") || retype.equals("B2") || retype.equals("B3"))) {
                serType = "NPV";
                Log.write("==NP Complex Port===");
            } else if ((serviceTy.equals("G") || serviceTy.equals("H") || serviceTy.equals("J")) && acty.equals("D") && retype.equals("B")) {
                Log.write("==Dir_Ass_Dir_DISC_tech_flow===");
                serType = "DAD";

            } else if (serviceTy.equals("M") &&
                    acty.equals("V") && retype.equals("B")) {
                Log.write("==UNEP_CONV_tech_flow===");
                serType = "UNEPV";

            } else if (serviceTy.equals("M") &&
                    acty.equals("D") && retype.equals("B")) {
                Log.write("==UNEP_DISC_tech_flow===");
                serType = "UNEPD";

            } else if (serviceTy.equals("E") &&
                    acty.equals("D") && retype.equals("B")) {
                Log.write("==Resale_DISC_tech_flow===");
                serType = "RED";

            } else if (serviceTy.equals("E") &&
                    acty.equals("S") && retype.equals("B")) {
                Log.write("==Resale_SUSP_tech_flow===");
                serType = "RES";

            } else if (serviceTy.equals("M") &&
                    acty.equals("S") && retype.equals("B")) {
                Log.write("==UNEPS supended _flow===");
                serType = "UNEPS";

            } 
            
            //this is not needed anymore as Simple Port Service Request form has been 
            //decommissioned - Antony - 12/10/2010
            /*
            else if (serviceTy.equals("S") &&
                    acty.equals("F") && retype.equals("S")) {

                Log.write("=SP===");
                serType = "SP";
            }
             */
            //Fixed - Null Pointer Exception
            if("SP".equals(serType)) {
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
                
        }
        Log.write("LSRBaseValidator getReqSerActType serType  " + serType);
        if (serType != null) {
            return serType;
        } else {
            return "";
        }
    }

    /*
     * getRejectionCode method used for getting Rejection code
     *
     */
    public Map getRejectionCode() {
        Map mapRejection = new HashMap();
        mapRejection.put("Pass", rejectionvectorPass);
        mapRejection.put("Fail", rejectionvectorfail);
        mapRejection.put("Manal", rejvctrManual);


        return mapRejection;
    }

    /*
     * LSR_Generic_tech_flow start
     */
   
    /*matchEATN_ATN method used for checking
     *EATN on EU form matches ATN value on LSR form
     */
    public boolean matchEATN_ATN() {
        String actTel = lsrDataBean.getAccountTelephoneNo();
        String eactTel = lsrDataBean.getExitingActTeleNo();
        Log.write("LSRBaseValidator matchEATN_ATN actTel  " + actTel + " eactTel " + eactTel);
        boolean flag = false;
        if (eactTel != null && actTel != null && eactTel.trim().equals(actTel.trim())) {
            flag = true;
        }
        fillterSerTypeRejCode("10024-matchEATN_ATN", flag, "NPV", false);
        fillterSerTypeRejCode("70022-matchEATN_ATN", flag, "UNEPV", false);
        fillterSerTypeRejCode("50019-matchEATN_ATN", flag, "UNEPD", false);
        fillterSerTypeRejCode("40017-matchEATN_ATN", flag, "RED", false);
        fillterSerTypeRejCode("80017-matchEATN_ATN", flag, "UNEPS", false);
        fillterSerTypeRejCode("30008-matchEATN_ATN", flag, "DAD", false);
        fillterSerTypeRejCode("60018-matchEATN_ATN", flag, "RES", false);
        return flag;
    }

    /*isStatus method used for checking
     *Active ATN and matches CAMS pilot TN (ATN means main number)
     *Active Cust number go to Next Step
     *Inactive TN REJECT without further validation
     *Invalid Cust.Manual Review (only for NP,UPC other flows --> rejection)
     */
    public boolean isStatus() {

        String actsts = validationData.getCustStatus();
        Log.write("LSRBaseValidator isStatus actsts  " + actsts);
        boolean flag = false;
        boolean manflag = false;
        boolean manInvalidFlag = false;//added for 1408 fix
        boolean camsError = false;//added for 296 fix
        boolean custNotFoundInCAMS = false;
        
        
        if (actsts != null) {
            actsts = actsts.trim();
            if (actsts.equalsIgnoreCase("active")) {
                //active valid
                flag = true;
            } else if (actsts.equalsIgnoreCase("disconnected")) {
                //Inactive
                flag = false;
            } else if (actsts.equalsIgnoreCase("Invalid") &&
                    ("NPV".equals(serType) || "UNEPV".equals(serType))) {
                // doubt below flag
                flag = false;
                //manflag = true; fix for 1408
                manInvalidFlag = true;
            } else if (actsts.equalsIgnoreCase("Invalid")) {
                flag = false;
                manflag = false;

            } else if (actsts.equalsIgnoreCase("CUST_NOT_IN_CAMS")) {
                flag = false;
                custNotFoundInCAMS = true;
            
            } else if (actsts.trim().toUpperCase().equals("ERROR")) {
                camsError = true;
                flag = false;
            } else {

                return flag;
            }
        }
   
        if(manInvalidFlag) {
            
            fillterSerTypeRejCode("100017-isStatus", flag, "NPV", manInvalidFlag);//100017
            fillterSerTypeRejCode("100018-isStatus", flag, "UNEPV", manInvalidFlag);//100018
        } else if(camsError) {
            fillterSerTypeRejCode("100033-isStatus", flag, "SP", camsError);
            fillterSerTypeRejCode("100033-isStatus", flag, "NPV", camsError);
            fillterSerTypeRejCode("100033-isStatus", flag, "UNEPV", camsError);
            fillterSerTypeRejCode("100033-isStatus", flag, "UNEPD", camsError);
            fillterSerTypeRejCode("100033-isStatus", flag, "RED", camsError);
            fillterSerTypeRejCode("100033-isStatus", flag, "DAD", camsError);
            fillterSerTypeRejCode("100033-isStatus", flag, "UNEPS", camsError);
            fillterSerTypeRejCode("100033-isStatus", flag, "RES", camsError);
         } else if(custNotFoundInCAMS) {
            fillterSerTypeRejCode("10010-isStatus", flag, "NPV", manflag);//
            fillterSerTypeRejCode("20012-isStatus", flag, "SP", manflag);//
            fillterSerTypeRejCode("70009-isStatus", flag, "UNEPV", manflag);//70009
            fillterSerTypeRejCode("50006-isStatus", flag, "UNEPD", manflag);//50006
            fillterSerTypeRejCode("40006-isStatus", flag, "RED", manflag);
            fillterSerTypeRejCode("30006-isStatus", flag, "DAD", manflag);//30006
            fillterSerTypeRejCode("80006-isStatus", flag, "UNEPS", manflag);//80006
            fillterSerTypeRejCode("60006-isStatus", flag, "RES", manflag);//60006
        } else {
            fillterSerTypeRejCode("20012-isStatus", flag, "SP", manflag);//
            fillterSerTypeRejCode("70009-isStatus", flag, "UNEPV", manflag);//70009
            fillterSerTypeRejCode("50006-isStatus", flag, "UNEPD", manflag);//50006
            fillterSerTypeRejCode("40006-isStatus", flag, "RED", manflag);
            fillterSerTypeRejCode("30006-isStatus", flag, "DAD", manflag);//30006
            fillterSerTypeRejCode("80006-isStatus", flag, "UNEPS", manflag);//80006
            fillterSerTypeRejCode("60006-isStatus", flag, "RES", manflag);//60006
        }
        return flag;
    }

    /*checkDDD method used for checking
     *Reference  Vendor Table for  DD interval:
     * Lower -Limit-DDD-Interval, Upper-Limit-DDD-Interval,
     * & Time-Limit-DDD-Interval. LSR Due Date must meet Interval Requirements
     * to move to Next Step, If not Reject
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
                Log.write("--checkDDD--Rejecting Date-- if: ");
                fillterSerTypeRejCode("70013-checkDDD", flag, "UNEPV", false);
                fillterSerTypeRejCode("50010-checkDDD", flag, "UNEPD", false);
                fillterSerTypeRejCode("40010-checkDDD", flag, "RED", false);
                fillterSerTypeRejCode("80010-checkDDD", flag, "UNEPS", false);
                fillterSerTypeRejCode("60011-checkDDD", flag, "RES", false);
                fillterSerTypeRejCode("10015-checkDDD", flag, "NPV", false);
                fillterSerTypeRejCode("20017-checkDDD", flag, "SP", false);
                fillterSerTypeRejCode("30014-checkDDD", flag, "DAD", false);
                return false;
            }


            int time = currentDate.get(currentDate.HOUR_OF_DAY);
            String minstr = "0." + currentDate.get(currentDate.MINUTE);
            Log.write("=checkDDD=minstr=" + minstr);
            float min = Float.parseFloat(minstr);
            double totaltime = time + min;
            Log.write("=checkDDD totaltime==" + totaltime);
            String todstr = vendorBean.getValidTimeOfDayDDD();
            int tod = Integer.parseInt(todstr);
            Log.write("=checkDDD tod==" + tod);

            int diffendays;
            //code change for bug 1277
            int llDiffInDays;//separate variable for checking lower limit diff in days as we need to exclude SAT,SUN or WIN holidays here
            
            if (totaltime < tod) {
                diffendays = 1;
                llDiffInDays = 1;//code change for bug 1277
                
                Log.write("==if=totaltime < tod diffendays" + diffendays);
            } else {
                diffendays = 0;
                llDiffInDays = 0;//code change for bug 1277
                
                Log.write("==else=diffendays" + diffendays);
            }


            Log.write("before while LBV diffendays" + diffendays);
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

            /*
            //int lowerDate = Integer.parseInt(vendorBean.getDueDateLowerLimit());
            
            //if simple port then hard-code lower DDD limit as 1 - Antony - 01/03/2011
            
            LSRdao lsrDao = new LSRdao();
            String simplePortFlag = lsrDao.retrieveSPFlag(lsrDataBean.getReqstNmbr(),lsrDataBean.getReqstVer());

            int lowerDate = 0;
            
            if(simplePortFlag != null && simplePortFlag.length() > 0 && simplePortFlag.equals("Y"))
                lowerDate = 1;
            else
             */ 
            int lowerDate = Integer.parseInt(vendorBean.getDueDateLowerLimit());
                
            int upperDate = Integer.parseInt(vendorBean.getDueDateUpperLimit());

            Log.write(" checkDDD diffendays " + diffendays + " lowerDate " + lowerDate + " upperDate " + upperDate + " sameday " + sameday);

            if (llDiffInDays >= lowerDate && diffendays <= upperDate) {
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

        fillterSerTypeRejCode("70013-checkDDD", flag, "UNEPV", false);
        fillterSerTypeRejCode("50010-checkDDD", flag, "UNEPD", false);
        fillterSerTypeRejCode("40010-checkDDD", flag, "RED", false);
        fillterSerTypeRejCode("80010-checkDDD", flag, "UNEPS", false);
        fillterSerTypeRejCode("60011-checkDDD", flag, "RES", false);
        fillterSerTypeRejCode("10015-checkDDD", flag, "NPV", false);
        fillterSerTypeRejCode("20017-checkDDD", flag, "SP", false);
        fillterSerTypeRejCode("30014-checkDDD", flag, "DAD", false);

        return flag;
    }

    /*
     *matchesNPA_OCNState method used for checking
     *NPA matches OCN State Combo
     */
    public boolean matchesNPA_ATN_OCNState() {



        boolean flag = lsrDataBean.isAtnNPA();
        Log.write("LSRBaseValidator matchesNPA_ATN_OCNState flag " + flag);

        fillterSerTypeRejCode("10011-matchesNPA_ATN_OCNState", flag, "NPV", false);
        fillterSerTypeRejCode("20014-matchesNPA_ATN_OCNState", flag, "SP", false);
        fillterSerTypeRejCode("70010-matchesNPA_ATN_OCNState", flag, "UNEPV", false);
        fillterSerTypeRejCode("50007-matchesNPA_ATN_OCNState", flag, "UNEPD", false);
        fillterSerTypeRejCode("40007-matchesNPA_ATN_OCNState", flag, "RED", false);
        fillterSerTypeRejCode("30009-matchesNPA_ATN_OCNState", flag, "DAD", false);
        fillterSerTypeRejCode("80007-matchesNPA_ATN_OCNState", flag, "UNEPS", false);
        fillterSerTypeRejCode("60007-matchesNPA_ATN_OCNState", flag, "RES", false);

        return flag;
    }

    /*matchesOCN_State method used for checking
     *OCN State matches vendor table w/ BTN est.
     */
    public boolean matchesNPA_BTN_OCN_State() {

        boolean flag = false;

        String btn = vendorBean.getBTN();
        Log.write("LSRBaseValidator matchesNPA_BTN_OCN_State btn " + btn);

        if (btn != null) {
            btn = btn.trim();
            if (btn.length() == 17) {
                int index = btn.indexOf(",");
                if (index == 10) {
                    flag = true;
                }
            }
        }


        fillterSerTypeRejCode("10011-matchesNPA_BTN_OCN_State", flag, "NPV", false);
        fillterSerTypeRejCode("20014-matchesNPA_BTN_OCN_State", flag, "SP", false);
        fillterSerTypeRejCode("70010-matchesNPA_BTN_OCN_State", flag, "UNEPV", false);
        fillterSerTypeRejCode("50007-matchesNPA_BTN_OCN_State", flag, "UNEPD", false);
        fillterSerTypeRejCode("40007-matchesNPA_BTN_OCN_State", flag, "RED", false);
        fillterSerTypeRejCode("30009-matchesNPA_BTN_OCN_State", flag, "DAD", false);
        fillterSerTypeRejCode("80007-matchesNPA_BTN_OCN_State", flag, "UNEPS", false);
        fillterSerTypeRejCode("60007-matchesNPA_BTN_OCN_State", flag, "RES", false);
        return flag;
    }

    /*tosBus_Res method used for checking
     *If populated, TOS1 field matches BUS or RES
     *in CAMS cross reference to service type table in Express.
     * If populated, TOS2 will be validated  against reference table
     *(two new reference tables in Express)
     */
    public boolean tosBus_Res() {

        String tos = lsrDataBean.getTypeOfService();
        String tos2 = lsrDataBean.getTypeOfService2();
        Log.write("LSRBaseValidator before tosBus_Res tos " + tos);
        boolean flag = false;
        boolean checkflag = false, tos1Flag = false, tos2Flag = false;
        String camstos = validationData.getCustType();
        Log.write("LSRBaseValidator tosBus_Res camstos " + camstos);
        if (camstos != null) {
            camstos = camstos.trim();
            if (camstos.equalsIgnoreCase("BUS") || camstos.equalsIgnoreCase("BOF")) {
                camstos = "1";
            } else if (camstos.equalsIgnoreCase("RES")) {
                camstos = "2";
            } else if (camstos.equalsIgnoreCase("BGL") ||
                    camstos.equalsIgnoreCase("BGF") || camstos.equalsIgnoreCase("BGS")) {
                camstos = "3";
            }
        }

        if (tos != null) {
            if (tos.equals("4")) {
                tos = "1";
            } else if (tos.equals("5")) {
                tos = "2";
            }
        }

        Log.write("LSRBaseValidator tosBus_Res after tos " + tos);

        Log.write("LSRBaseValidator tosBus_Res after camstos " + camstos);
        Log.write("LSRBaseValidator tosBus_Res tos2 " + tos2);

        if (tos != null || tos2 != null) {
            checkflag = true;
        }
        if (tos != null && camstos != null && camstos.equals(tos)) {
            flag = true;
            tos1Flag = true;
        }
        //if (tos2 != null && !tos2.equals("-")) { -- fix for PI issue 25 - Antony 05/18/10
        if (tos2 != null && (tos2.trim().equals("-") || tos2.trim().equals("")  || 
                             tos2.trim().equals(" ") || tos2.trim().equals("A") ||
                             tos2.trim().equals("B") || tos2.trim().equals("C") ||
                             tos2.trim().equals("D") || tos2.trim().equals("E") ||
                             tos2.trim().equals("H") || tos2.trim().equals("J") ||
                             tos2.trim().equals("K") || tos2.trim().equals("L") ||
                             tos2.trim().equals("M") || tos2.trim().equals("N"))) {
            flag = true;
            tos2Flag = true;
        }
        if ((tos != null && camstos != null) && tos2 != null) {
            if (tos1Flag && tos2Flag) {
                flag = true;
            } else {
                flag = false;
            }
        }

        Log.write("LSRBaseValidator tosBus_Res tos1 " + tos1Flag + "  tos2Flag " + tos2Flag);
        if (checkflag) {
            //Code Change for avoiding validation with Cams for Resale Account - Saravanan
        	boolean reselFlag = checkTraitNameisResold();
            if(reselFlag)
            {
            	fillterSerTypeRejCode("10012-tosBus_Res", true, "NPV", false);
            }
            else
            {
            	fillterSerTypeRejCode("10012-tosBus_Res", flag, "NPV", false);
            }
            fillterSerTypeRejCode("70011-tosBus_Res", flag, "UNEPV", false);
            fillterSerTypeRejCode("50008-tosBus_Res", flag, "UNEPD", false);
            fillterSerTypeRejCode("40008-tosBus_Res", flag, "RED", false);
            fillterSerTypeRejCode("30010-tosBus_Res", flag, "DAD", false);
            fillterSerTypeRejCode("80008-tosBus_Res", flag, "UNEPS", false);
            fillterSerTypeRejCode("60008-tosBus_Res", flag, "RES", false);
        }
        return flag;
    }

    /*matchEU_Bill_Address method used for checking
     *
     *EU Form Address components exact match to the BIL ADDR is default
     *
     */
    public boolean matchEU_Bill_Address() {
        boolean flag = false;
        boolean finalflag = false;
        String euAddress = lsrDataBean.getEuAddressTrim();
        Addr camsAddress = validationData.getCustAddress();
        Map mapAddress = lsrDataBean.getAddressMap();
        AddressBean addbean = (AddressBean) mapAddress.get("EU_LA");
        Log.write("LSRBaseValidator matchEU_Bill_Address euAddress " + euAddress + " camsAddress " + camsAddress + "  addbean " + addbean);
        Log.write("LSRBaseValidator euAddress: " + euAddress );
        String catStr = "";
        String catStrWithoutFirstLine = "";
        String catSrvStr = "";
        String catSrvStrWithoutFirstLine = "";
        if (camsAddress != null && euAddress != null) {
            AddLn addrLine[] = camsAddress.getAddrLine();
            if (addrLine != null) {
                for (int i = 0; i < addrLine.length; i++) {
                    AddLn addrLineValue = addrLine[i];
                    Log.write(" matchEU_Bill_Address BW addrLineValue " + addrLineValue);
                    if (addrLineValue != null) {
                        String strAdds = addrLineValue.getAddrLn();
                        Log.write("LSRBaseValidator BW strAdds " + strAdds);
                        if (strAdds != null) {
                            String strSrvAdds = strAdds;
                            
                            if (i < 2) {
                                catStr = catStr + strAdds.replaceAll(" ", "");
                                catSrvStr = catSrvStr + " " + strSrvAdds;
                            }
                            
                            if (i != 0){
                                catStrWithoutFirstLine = catStrWithoutFirstLine+strAdds.replaceAll(" ","");
                                catSrvStrWithoutFirstLine = catSrvStrWithoutFirstLine + " " + strSrvAdds;
                            }
              
                            Log.write("LBV BW strAdds " + strAdds);
                            Log.write("LBV  BW catStr " + catStr);
                            Log.write("cat String without first line :"+catStrWithoutFirstLine);
                            Log.write("LBV BW strSrvAdds " + strSrvAdds);
                            Log.write("LBV  BW catSrvStr " + catSrvStr);
                            Log.write("cat Srv String without first line :"+catSrvStrWithoutFirstLine);
                            
                            //code inserted to fix bug 1450
                            //start
                            if(!(lsrDataBean.getSerRequestType().trim().equals("C") && lsrDataBean.getActivity().trim().equals("V"))//NP
                             ||!(lsrDataBean.getSerRequestType().trim().equals("M") && lsrDataBean.getActivity().trim().equals("V"))) {//UNEPCONV 
                                euAddress = euAddress.replaceAll(";","");
                                strAdds = strAdds.replaceAll(";","");
                                catStr = catStr.replaceAll(";","");
                                catStrWithoutFirstLine = catStrWithoutFirstLine.replaceAll(";","");
                                
                                strSrvAdds = strSrvAdds.replaceAll(";","");
                                catSrvStr = catSrvStr.replaceAll(";","");
                                catSrvStrWithoutFirstLine = catSrvStrWithoutFirstLine.replaceAll(";","");
                            }
                            //end
                            
                            //calling special character removing method - Antony - 01/17/2012
                            euAddress = RemoveSpecial(euAddress);
                            strAdds = RemoveSpecial(strAdds);
                            catStr = RemoveSpecial(catStr);;
                            catStrWithoutFirstLine = RemoveSpecial(catStrWithoutFirstLine);
                            
                            Log.write("LBV BW euaddress after removing spl chars " + euAddress);
                            Log.write("LBV BW strAdds after removing spl chars " + strAdds);
                            Log.write("LBV  BW catStr after removing spl chars " + catStr);
                            Log.write("cat String without first line after removing spl chars: "+catStrWithoutFirstLine);
                        
                            if (euAddress.equalsIgnoreCase(strAdds.replaceAll(" ", ""))) {
                                flag = true;
                                break;
                            } else if (euAddress.equalsIgnoreCase(catStr)) {
                                flag = true;
                                break;
                            } else if (euAddress.equalsIgnoreCase(catStrWithoutFirstLine)) {//compare without Addr Line 1 content
                                flag = true;                                                //as customer name spills over to Line 1
                                break;                                                      //for some CAMS customer names
                            }else{
                                //compare the street address with common street suffix address
                            	if(srvAddrsStrtTypValidation(strSrvAdds, euAddress, streetAddrsMap) 
                                    || srvAddrsStrtTypValidation(catSrvStr, euAddress, streetAddrsMap)
                                    || srvAddrsStrtTypValidation(catSrvStrWithoutFirstLine, euAddress, streetAddrsMap)){
                                    flag = true;
                                    break;
                            	}
                            }
                        }
                    }
                }
            }
        }

        if (camsAddress != null && addbean != null) {
            String city = camsAddress.getCity();
            String state = camsAddress.getState();
            String citylsr = addbean.getCity();
            String statelsr = addbean.getState();
            Log.write("LSRBaseValidator matchEU_Bill_Address flag " + flag + " city " + city + " citylsr " + citylsr + " state " + state + " statelsr " + statelsr);

            //removing spl characters - Antony - 01/20/2012
            city = RemoveSpecial(city);
            state = RemoveSpecial(state);
            citylsr = RemoveSpecial(citylsr);
            statelsr = RemoveSpecial(statelsr);
            
            
            
            if (flag && city != null && citylsr != null && city.trim().equalsIgnoreCase(citylsr.trim()) &&
                    state != null && statelsr != null && state.trim().equalsIgnoreCase(statelsr.trim())) {
                finalflag = true;
            }
            
            Log.write("LSRBaseValidator matchEU_Bill_Address flag after removing spl chars: " + flag + " ,finalflag: " + finalflag + " city " + city + " citylsr " + citylsr + " state " + state + " statelsr " + statelsr);

        }

        //Code Change for avoiding validation with Cams for Resale Account - Saravanan
        boolean reselFlag = checkTraitNameisResold();
        if(reselFlag)
        {
            fillterSerTypeRejCode("10020-matchEU_Bill_Address :BW ", true, "NPV", false);
        }
        else {
            fillterSerTypeRejCode("10020-matchEU_Bill_Address :BW ", finalflag, "NPV", false);
        }
        fillterSerTypeRejCode("50017-matchEU_Bill_Address :BW ", finalflag, "UNEPD", false);

        fillterSerTypeRejCode("40015-matchEU_Bill_Address :BW ", finalflag, "RED", false);
        fillterSerTypeRejCode("80015-matchEU_Bill_Address :BW ", finalflag, "UNEPS", false);
        fillterSerTypeRejCode("60016-matchEU_Bill_Address :BW ", finalflag, "RES", false);
        return flag;
    }


    /*matchZIP_CAMS method used for checking
     *
     *EU Form Zip  exact match to the CAMS ZIP
     *
     */
    public boolean matchZIP_CAMS() {
        boolean flag = false;
        Map mapAddress = lsrDataBean.getAddressMap();
        AddressBean addbean = (AddressBean) mapAddress.get("EU_LA");
        Addr camsAddress = validationData.getCustAddress();
        String ziplsr = addbean.getZip();
        String zip = camsAddress.getZipCd();
        if (zip != null && zip.length() > 5) {
            String zips[] = zip.split("[-]");
            Log.write("LSRBaseValidator matchEU_Bill_Address zips" + zips);
            zip = zips[0];
            Log.write("LSRBaseValidator matchEU_Bill_Address zip " + zip);
        }
        //Venkatesh changes for zip validation
        if (ziplsr != null && zip != null && ziplsr.trim().equalsIgnoreCase(zip.trim())) {
            flag = true;
        }
        else if( zip == null || zip.trim().length() == 0 || zip.trim().equals("99999") || zip.trim().equals("99999-0000")) {
            
            flag = true;
            Log.write("Cams zipcode is null or blank or 99999 or 99999-0000, so bypassing zipcode validation");
            
            
        }  
        
        //Code Change for avoiding validation with Cams for Resale Account - Saravanan
        boolean reselFlag = checkTraitNameisResold();
        if(reselFlag)
        {
       	fillterSerTypeRejCode("10021-matchZIP_CAMS :BW ", true, "NPV", false);
        }else
        {
        fillterSerTypeRejCode("10021-matchZIP_CAMS :BW ", flag, "NPV", false);
        }
        fillterSerTypeRejCode("70025-matchZIP_CAMS :BW ", flag, "UNEPV", false);
        fillterSerTypeRejCode("50021-matchZIP_CAMS :BW ", flag, "UNEPD", false);

        fillterSerTypeRejCode("40019-matchZIP_CAMS :BW ", flag, "RED", false);
        fillterSerTypeRejCode("80019-matchZIP_CAMS :BW ", flag, "UNEPS", false);
        fillterSerTypeRejCode("60020-matchZIP_CAMS :BW ", flag, "RES", false);
        return flag;
    }


    /*matchName_CAMS method used for checking
     *14V EU Form Name must match CAMS name
     * On EU form Name must match CAMS SERV-ADR-ID or default to main Name
     */
    public boolean matchName_CAMS() {

        
        String euName = lsrDataBean.getEuName();
        String billName = lsrDataBean.getBillname();
        String camsName = validationData.getCustName();
        String strSpace25 = "                         ";        
                
        //fix for bug 298
        if(euName != null) {
            euName = (euName+strSpace25).substring(0, 25);
            euName = euName.replaceAll(" ","");
            euName = RemoveSpecial(euName);
        }
        if(billName != null) {
            billName = (billName+strSpace25).substring(0, 25);
            billName = billName.replaceAll(" ","");
            billName = RemoveSpecial(billName);
        }
        if(camsName != null) {
            camsName = (camsName+strSpace25).substring(0, 25);
            camsName = camsName.replaceAll(" ","");
            camsName = RemoveSpecial(camsName);
        }
        
        Log.write("LSRBaseValidator matchName_CAMS euName " + euName + " camsName " + camsName + "  billName " + billName);
        boolean flag = false;
        boolean billflag = false;

        Addr camsAddress = validationData.getCustAddress();

        if (billName != null && camsName != null) {
            if (billName.trim().equalsIgnoreCase(camsName.trim())) {
                billflag = true;
            } else {
                //fillterSerTypeRejCode("10025-matchName_CAMS :BW ", flag, "NPV", false); -- as no billnm valdn for NP - bug 300
                fillterSerTypeRejCode("70023-matchName_CAMS :BW ", flag, "UNEPV", false);
                fillterSerTypeRejCode("50020-matchName_CAMS :BW ", flag, "UNEPD", false);
                fillterSerTypeRejCode("40018-matchName_CAMS :BW ", flag, "RED", false);
                fillterSerTypeRejCode("80018-matchName_CAMS :BW ", flag, "UNEPS", false);
                fillterSerTypeRejCode("60019-matchName_CAMS :BW ", flag, "RES", false);
                
                //as no billnm valdn for NP - bug 300
                if(!(lsrDataBean.getSerRequestType().trim().equals("C") && lsrDataBean.getActivity().trim().equals("V")))
                    billflag = true;
                else 
                    return flag;
            }
        } else if (billName == null) {
            billflag = true;
        }
        Log.write(" matchName_CAMS BW billflag: " + billflag + " camsAddress: " + camsAddress);
        String catStr = "";
        String nameAddr = "";
        if (billflag && euName != null && camsName != null) {
            if (camsName.trim().equalsIgnoreCase(euName.trim())) {
                flag = true;
            } else {
                
                catStr = camsName;
                if (camsAddress != null) {
                    AddLn addrLine[] = camsAddress.getAddrLine();
                    if (addrLine != null) {
                        for (int i = 0; i < addrLine.length; i++) {
                          if(i < 2) {
                            AddLn addrLineValue = addrLine[i];
                            Log.write(" matchName_CAMS BW addrLineValue: " + addrLineValue);
                            if (addrLineValue != null) {
                                String strAdds = addrLineValue.getAddrLn();
                                
                                catStr = catStr + strAdds;
                                
                                if (i == 0) {
                                    nameAddr = camsName + strAdds;
                                }
                                
                                Log.write(" matchName_CAMS BW strAdds: " + strAdds);
                                Log.write(" matchName_CAMS BW catStr: " + catStr);
                                
                                //match only the whole string - fix for bug 299
                                //Pattern pattern = Pattern.compile(euName.replaceAll(" ", ""));
                                //Matcher matcher = pattern.matcher(strAdds.replaceAll(" ", ""));
                                
                                euName = euName.replaceAll(" ","");
                                euName = RemoveSpecial(euName);
                                catStr = catStr.replaceAll(" ","");
                                catStr = RemoveSpecial(catStr);
                                
                                /*
                                if (matcher.find()) {
                                    flag = true;
                                    break;
                                }
                                */
                                
                                if (euName.trim().equalsIgnoreCase(catStr.trim())) {
                                    flag = true;
                                    break;
                                }

                            }
                          }
                        }
                    }
                    Log.write(" matchName_CAMS BW catStr: " + catStr + "  euName " + euName);
                    if (!flag && catStr.length() > 0) {
                        //match only the whole string - fix for bug 299
                        //Pattern pattern = Pattern.compile(euName.replaceAll(" ", ""));
                        //Matcher matcher = pattern.matcher(catStr.replaceAll(" ", ""));
                        euName = euName.replaceAll(" ","");
                        euName = RemoveSpecial(euName);
                        catStr = catStr.replaceAll(" ","");
                        catStr = RemoveSpecial(catStr);
                        /*
                        if (matcher.find()) {
                            flag = true;
                            break;
                        }
                        */
                        if (euName.trim().equalsIgnoreCase(catStr.trim())) {
                            flag = true;
                            //break;
                        }
                    }
                    if (!flag && nameAddr.length() > 0) {
                        //match only the whole string - fix for bug 299
                        //Pattern pattern = Pattern.compile(euName.replaceAll(" ", ""));
                        //Matcher matcher = pattern.matcher(nameAddr.replaceAll(" ", ""));
                        euName = euName.replaceAll(" ","");
                        euName = RemoveSpecial(euName);
                        nameAddr = nameAddr.replaceAll(" ","");
                        nameAddr = RemoveSpecial(nameAddr);
                        /*
                        if (matcher.find()) {
                            flag = true;
                            break;
                        }
                        */
                        if (euName.trim().equalsIgnoreCase(nameAddr.trim())) {
                            flag = true;
                            //break;
                        }
                    }
                }
            }

        }
        //Code Change for avoiding validation with Cams for Resale Account - Saravanan
        boolean reselFlag = checkTraitNameisResold();
        if(reselFlag)
        {
         fillterSerTypeRejCode("10019-matchName_CAMS :BW ", true, "NPV", false);
        }
        else {
        fillterSerTypeRejCode("10019-matchName_CAMS :BW ", flag, "NPV", false);
        }
        fillterSerTypeRejCode("70017-matchName_CAMS :BW ", flag, "UNEPV", false);
        fillterSerTypeRejCode("50016-matchName_CAMS :BW ", flag, "UNEPD", false);
        fillterSerTypeRejCode("40014-matchName_CAMS :BW ", flag, "RED", false);
        fillterSerTypeRejCode("80014-matchName_CAMS :BW ", flag, "UNEPS", false);
        fillterSerTypeRejCode("60015-matchName_CAMS :BW ", flag, "RES", false);
        return flag;
    }

    /*matchATN_CAMSPilotNumber method used for checking
     *15V ATN matches CAMS pilot # Must be BENT number
     * later we need to check with getCamsId for ATN BenT number
     */
    public boolean matchATN_CAMSPilotNumber() {
        boolean flag = false;
        String atn = lsrDataBean.getAccountTelephoneNo();
        Log.write("LSRBaseValidator matchATN_CAMSPilotNumber atn " + atn);
        String pilotNo = validationData.getPilotNo();
        Log.write("LBV  BW matchATN_CAMSPilotNumber pilotNo " + pilotNo);
        if (atn != null && pilotNo != null && pilotNo.trim().equals(atn.trim())) {
            flag = true;
        }
        fillterSerTypeRejCode("70009-matchATN_CAMSPilotNumber :BW ", flag, "UNEPV", false);
        fillterSerTypeRejCode("50006-matchATN_CAMSPilotNumber :BW ", flag, "UNEPD", false);
        fillterSerTypeRejCode("40006-matchATN_CAMSPilotNumber :BW ", flag, "RED", false);
        fillterSerTypeRejCode("30006-matchATN_CAMSPilotNumber :BW ", flag, "DAD", false);
        fillterSerTypeRejCode("80006-matchATN_CAMSPilotNumber :BW ", flag, "UNEPS", false);
        fillterSerTypeRejCode("60006-matchATN_CAMSPilotNumber :BW ", flag, "RES", false);
        return flag;
    }

    /*isExitPendingOrder method used for checking
     *16v  Later we can do this validation
     *Pending order does not exist dep--b/w
     *  If just PLOCK or LLOCK removal send for paper order,
     *  If any other action on service order then send to reject
     * If P order send for manual Review(SP,NP,UPC)
     *
     */
    public boolean isExitPendingOrder() {
        boolean flag = false;
        boolean manflag = false;
        boolean manPflag = false;
	boolean otherflag = false;
	boolean lockflag = false;
        
        PndngOrder pndgOrderlist[] = validationData.getCustPndgOrderList();
        Log.write("LSRBaseValidator isExitPendingOrder pndgOrderlist " + pndgOrderlist);
        if (pndgOrderlist == null || pndgOrderlist.length == 0) {
            //Rejection
            flag = true;
            Log.write("Debug point 1");
        } else if (pndgOrderlist != null) {
            Log.write("Debug point 2");
            for (int i = 0; i < pndgOrderlist.length; i++) {
                Log.write("Debug point 3");
		lockflag = false;
                PndngOrder pndgOrderValue = pndgOrderlist[i];
                Log.write("LBV  BW isExitPendingOrder pndgOrderValue " + pndgOrderValue);
                if (pndgOrderValue != null) {
                    String type = pndgOrderValue.getOrderAction();
                    String sts = pndgOrderValue.getOrderStatus();
                    OrderSegment orderSegment[] = pndgOrderValue.getOrderSegmentList();

                    Log.write("LBV  BW isExitPendingOrder type: " + type + "  status: " + sts);
                    Log.write("LBV  BW isExitPendingOrder orderSegment: " + orderSegment);

                    if (sts != null && (sts.equalsIgnoreCase("50") || sts.equalsIgnoreCase("A0") || sts.equalsIgnoreCase("B0"))) {
                        flag = true;
                        break;
                    }
                    //I,O,C,P
                    if (type != null && type.trim().equalsIgnoreCase("P")) {
                    manPflag = true;
                    break;
                    //Manual Review
                    }
                    
                    //if block to skip if I or O order for a version > 0 as orders are cancelled already
                    if (type != null && (type.trim().equalsIgnoreCase("I") || type.trim().equalsIgnoreCase("O")) && !lsrDataBean.getReqstVer().equals("0")) {
                        //skip this order and go to next order in list
                        flag = true;
                        Log.write("Debug point 4");
                        continue;
                    }
	
                    if (orderSegment != null) {
                        for (int j = 0; j < orderSegment.length; j++) {
                            SneData sneDataList[] = orderSegment[j].getSegSneList();
                            if (sneDataList != null) {
                                for (int k = 0; k < sneDataList.length; k++) {
                                    SneData sneData = sneDataList[k];
                                    String sneAct = sneData.getSneAction();
                                    String sneCode = sneData.getSneCode();
                                    String sneRemarks = sneData.getSneRemarks();
                                    if (sneAct != null && sneCode != null && sneRemarks != null) {
                                        Log.write("Debug point 5");
                                        if (sneAct.trim().equalsIgnoreCase("I") &&
                                             (sneCode.trim().equalsIgnoreCase("PLOCK") || sneCode.trim().equalsIgnoreCase("LLOCK")) &&
                                              sneRemarks.trim().equalsIgnoreCase("N")) {
Log.write("Debug point 6");
                                                flag = true;
                                                lockflag = true;

                                        } else if (sneCode.trim().equalsIgnoreCase("INFO")||
                                                   sneCode.trim().equalsIgnoreCase("MISC")||
                                                   sneCode.trim().equalsIgnoreCase("DRIVE")||
                                                   sneCode.trim().equalsIgnoreCase("EMADR")||
                                                   sneCode.trim().equalsIgnoreCase("EMAIL")||
                                                   sneCode.trim().equalsIgnoreCase("EMAL")||
                                                   sneCode.trim().equalsIgnoreCase("SELLA")||
                                                   sneCode.trim().equalsIgnoreCase("SELLP")) {
                                                   //  If PLOCK or LLOCK removal is on the order along with any of the above asoc's,
                                                   //  it should pass validation.  The lockflag variable in the prior "if" is used later to override the
                                                   //  flag = true being set in this statement.  This will allow the reject to happen when the above asocs
                                                   //  exist on an order without a PLOCK/LLOCK being present. CG
                                                   flag = true;
                                                   Log.write("Debug point 7");
                                        } else {
                                                   otherflag = true;
                                                   Log.write("Debug point 8");
                                        }
                                        Log.write("Debug point 9");
                                    }
                                }

                            }
                        }
                        if (!lockflag){ // If there was no PLOCK/LLOCK removal on the order we need to reject no matter what other asocs on the order
                            otherflag = true;
                            Log.write("Debug point 10");
			}
                    }
                }
            }
            if(otherflag){
                flag = false;
		manflag = false;
		manPflag = false;
                Log.write("Debug point 11");
            }
        }
		Log.write("LSRBaseValidator isExitPendingOrder manPflag "+manPflag +" manflag "+manflag
               +" flag "+flag );
        if (manPflag) {
            fillterSerTypeRejCode("90016-isExitPendingOrder :BW", false, "NPV", manPflag);
            fillterSerTypeRejCode("100022-isExitPendingOrder :BW", false, "SP", manPflag);
            fillterSerTypeRejCode("100021-isExitPendingOrder :BW", false, "UNEPV", manPflag);
        } else if (manflag) {
            fillterSerTypeRejCode("90007-isExitPendingOrder :BW", flag, "NPV", manflag);
            fillterSerTypeRejCode("100020-isExitPendingOrder :BW", flag, "SP", manflag);
            fillterSerTypeRejCode("100019-isExitPendingOrder :BW", flag, "UNEPV", manflag);
        } else {
            fillterSerTypeRejCode("10002-isExitPendingOrder :BW", flag, "NPV", manflag);
            fillterSerTypeRejCode("20002-isExitPendingOrder :BW", flag, "SP", manflag);
            fillterSerTypeRejCode("70002-isExitPendingOrder :BW", flag, "UNEPV", manflag);
        }

        fillterSerTypeRejCode("50002-isExitPendingOrder :BW", flag, "UNEPD", false);
        fillterSerTypeRejCode("40002-isExitPendingOrder :BW", flag, "RED", false);
        fillterSerTypeRejCode("30002-isExitPendingOrder :BW", flag, "DAD", false);
        fillterSerTypeRejCode("80002-isExitPendingOrder :BW", flag, "UNEPS", false);
        fillterSerTypeRejCode("60002-isExitPendingOrder :BW", flag, "RES", false);
        return flag;
    }

    /* checkisEXPBlank method used for checking
     *17 V EXP field is blank
     * Send for manual review(only NP,UPC)
     */
    public boolean checkisEXPBlank() {

        String exp = lsrDataBean.getEXPedite();
        Log.write("LSRBaseValidator checkisEXPBlank exp " + exp);
        boolean flag = false;
        if (exp == null) {
            flag = true;
        }
        fillterSerTypeRejCode("100005-checkisEXPBlank", flag, "NPV", !flag);
        fillterSerTypeRejCode("100011-checkisEXPBlank", flag, "UNEPV", !flag);
        fillterSerTypeRejCode("50012-checkisEXPBlank", flag, "UNEPD", false);

        fillterSerTypeRejCode("40012-checkisEXPBlank", flag, "RED", false);
        //fillterSerTypeRejCode("DL0050 checkisEXPBlank",flag,"DAD",manflag);
        fillterSerTypeRejCode("80012-checkisEXPBlank", flag, "UNEPS", false);
        fillterSerTypeRejCode("60013-checkisEXPBlank", flag, "RES", false);
        return flag;
    }

    /* checkisCHCBlank method used for checking
     *18 V CHC field is blank
     *Send for manual review(only NP,UPC)
     */
    public boolean checkisCHCBlank() {

        String chc = lsrDataBean.getCoHotCut();
        Log.write("LSRBaseValidator checkisCHCBlank chc " + chc);
        boolean flag = false;
        if (chc == null) {
            flag = true;
        }
        fillterSerTypeRejCode("100012-checkisCHCBlank", flag, "NPV", !flag);
        fillterSerTypeRejCode("100013-checkisCHCBlank", flag, "UNEPV", !flag);
        fillterSerTypeRejCode("50013-checkisCHCBlank", flag, "UNEPD", false);
        fillterSerTypeRejCode("40013-checkisCHCBlank", flag, "RED", false);
        fillterSerTypeRejCode("30013-checkisCHCBlank", flag, "DAD", false);
        fillterSerTypeRejCode("80013-checkisCHCBlank", flag, "UNEPS", false);
        fillterSerTypeRejCode("60014-checkisCHCBlank", flag, "RES", false);
        return flag;
    }

    /*isExitOCN_Trait_CAMS method used for checking
     *19 V
     *OCN Trait exists in CAMS
     * If OCN trait RESOLD-LSP-ID or UNEP-ID exists,
     * then the AN and passcode fields will be ignored, (ignore all values for AN & PC fields)
     * then send to manual review after validation completes.
     * If no OCN trait exists AN must be populated and validataed against CAMS ID.
     * send to Manual Review(only SP,UPC,NP)
     * Reject Box without further validation
     */
    public boolean isExitOCN_Trait_CAMS() {
        boolean flag = false;
        boolean manflag = false;
        boolean ANCAMSID_PC_ANEAN = false;//
        Ctrt ctrtvalue[] = validationData.getCustTraitList();
        String an = lsrDataBean.getAccountNo();
        String camsID = validationData.getCamsId();
        String traitReUn = null;//
        Log.write("LSRBaseValidator isExitOCN_Trait_CAMS ctrtvalue-- " + ctrtvalue + ": an " + an + ": camsID " + camsID);
        boolean resold_flag = false;
        boolean unep_flag = false;
        boolean iclec_flag = false;
        boolean wireless_flag = false;
        
        if (ctrtvalue != null) {
            for (int i = 0; i < ctrtvalue.length; i++) {
                Ctrt ctrt = ctrtvalue[i];

                TnTrait traitTn[] = ctrt.getTnTrait();

                for (int j = 0; j < traitTn.length; j++) {
                    TnTrait traitvalue = traitTn[j];
                    String trName = traitvalue.getTraitName();
                    String trValue = traitvalue.getTraitValue();

                    Log.write("LBV BW isExitOCN_Trait_CAMS trName " +
                            trName + " trValue " + trValue);
                    if (trValue != null) {
                        if (trName.trim().equalsIgnoreCase("RESOLD-LSP-ID")) {
                            resold_flag = true;
                        } else if (trName.trim().equalsIgnoreCase("UNEP-ID")) {
                            unep_flag = true;
                        } else if (trName.trim().equalsIgnoreCase("ICLEC-LSP-ID")) {
                            iclec_flag = true;
                        } else if (trName.trim().equalsIgnoreCase("WIRELESS-LSP-ID")) {
                            wireless_flag = true;
                        }
                    }
                }
            }
        }    
         
        if ("NPV".equals(serType)) {
            if(iclec_flag || wireless_flag) {
                //Reject without further validation"TN not eligible to port"
                flag = false;
                manflag = false;
                fillterSerTypeRejCode("10006-isExitOCN_Trait_CAMS :BW", flag, "NPV", manflag);
                return false;
            } else if(resold_flag) {
                //Manual Review "TN has RESOLD trait.  Does not qualify for automation."
                flag = false;
                manflag = true;
                fillterSerTypeRejCode("90009-isExitOCN_Trait_CAMS :BW", flag, "NPV", manflag);
		//Do not do validations pertaining to passcode or AN in the flow.
                setDoPasscodeValidation(false);
                return true; //as not fatal error
            } else if(unep_flag) {
                //Manual Review "TN has UNEP-ID trait.  Does not qualify for automation."
                flag = false;
                manflag = true;
                fillterSerTypeRejCode("90008-isExitOCN_Trait_CAMS :BW", flag, "NPV", manflag);
		//Do not do validations pertaining to passcode or AN in the flow.
                setDoPasscodeValidation(false);
                return true;//as not fatal error
            } else {
          
                //if (AN is null or blank) or AN does not match CAMSID from CAMS
		//Reject without further validation "Invalid/Missing Account Number"
		//else 
		//Passed validation "V4"
                flag = matchAN_CAMSID();
                
                if(flag)
                    setDoPasscodeValidation(true);// so that PC and EAN validations will be done
                
                manflag = false;
                fillterSerTypeRejCode("10009-isExitOCN_Trait_CAMS :BW", flag, "NPV", manflag);
                return flag;
            }
        }
        
        if("SP".equals(serType)) {
            if(iclec_flag || wireless_flag) {
                //Reject without further validation"TN not eligible to port"
                flag = false;
                manflag = false;
                fillterSerTypeRejCode("20006-isExitOCN_Trait_CAMS :BW", flag, "SP", manflag);
                return false;
            } else if(resold_flag) {
                //Reject  "TN does not qualify for simple port"
                flag = false;
                manflag = false;
                fillterSerTypeRejCode("20007-isExitOCN_Trait_CAMS :BW", flag, "SP", manflag);
		//Do not do validations pertaining to passcode or AN in the flow. (passcode and SPAN fields ignored)  Proceed with validation
                setDoPasscodeValidation(false);
                return true; //as not fatal error
            } else if(unep_flag) {
                //Reject  "TN does not qualify for simple port"
                flag = false;
                manflag = false;
                fillterSerTypeRejCode("20007-isExitOCN_Trait_CAMS :BW", flag, "SP", manflag);
		//Do not do validations pertaining to passcode or AN in the flow. (passcode and SPAN fields ignored)  Proceed with validation
                setDoPasscodeValidation(false);
                return true; //as not fatal error
            } else {
                //if SPAN does not match CAMSID from CAMS or SPAN is blank (when no ICLEC, WIRELESS, RESOLD, or UNE-P trait exists)
		//		Reject without further validation "Invalid/Missing SPAN"
		//	else 
		//		Passed validation "V4"
                flag = matchAN_CAMSID();
                
                if(flag)
                    setDoPasscodeValidation(true);// so that PC validation will be done
                
                manflag = false;
                fillterSerTypeRejCode("20010-isExitOCN_Trait_CAMS :BW", flag, "SP", manflag);
                return flag;
            }
        }
        
        if("UNEPV".equals(serType)) {
            if(iclec_flag) {
                //Manual Review  "TN has ICLEC-LSP-ID trait.  Does not qualify for automation."  where "x" is the name of the trait on account.
                flag = false;
                manflag = true;
                fillterSerTypeRejCode("100034-isExitOCN_Trait_CAMS :BW", flag, "UNEPV", manflag);
		//Do not do validations pertaining to passcode or AN in the flow. (passcode and AN fields ignored)  Proceed with validation
                setDoPasscodeValidation(false);
                return true; //as not fatal error
                
            } else if(wireless_flag) {
                //Manual Review  "TN has WIRELESS-LSP-ID trait.  Does not qualify for automation."  where "x" is the name of the trait on account.
                flag = false;
                manflag = true;
                fillterSerTypeRejCode("100035-isExitOCN_Trait_CAMS :BW", flag, "UNEPV", manflag);
		//Do not do validations pertaining to passcode or AN in the flow. (passcode and AN fields ignored)  Proceed with validation
                setDoPasscodeValidation(false);
                return true; //as not fatal error
            } else if(resold_flag) {
                //Manual Review  "TN has RESOLD-LSP-ID trait.  Does not qualify for automation."  where "x" is the name of the trait on account.
                flag = false;
                manflag = true;
                fillterSerTypeRejCode("100036-isExitOCN_Trait_CAMS :BW", flag, "UNEPV", manflag);
		//Do not do validations pertaining to passcode or AN in the flow. (passcode and AN fields ignored)  Proceed with validation
                setDoPasscodeValidation(false);
                return true; //as not fatal error
            } else if(unep_flag) {
                //Manual Review  "TN has UNEP-ID trait.  Does not qualify for automation."  where "x" is the name of the trait on account.
                flag = false;
                manflag = true;
                fillterSerTypeRejCode("100037-isExitOCN_Trait_CAMS :BW", flag, "UNEPV", manflag);
		//Do not do validations pertaining to passcode or AN in the flow. (passcode and AN fields ignored)  Proceed with validation
                setDoPasscodeValidation(false);
                return true; //as not fatal error
            } else {
                //if (AN is blank or null) or AN does not match CAMSID from CAMS 
		//Reject without further validation "Invalid/Missing Account Number"
        	//else 	
		//Passed validation "V4"
                flag = matchAN_CAMSID();
                
                if(flag)
                    setDoPasscodeValidation(true);// so that PC and EAN validations will be done
                
                manflag = false;
                fillterSerTypeRejCode("70008-isExitOCN_Trait_CAMS :BW", flag, "UNEPV", manflag);
                return flag;
            }
        }

            /*
            if (re_un_flag) {
                //manual review as we found UNEP trait or RESOLD trait
                manflag = true;

            } else if (ic_wi_flag) {
                // rejected
                flag = false;//send to reject for "TN not eligible to port"
            } else {
                //sucess: calling two methods
                //code change for bug 1446 call each method below sequentially only if previous method passed
                // as per order AN/CAMSID, PC/PASSWDPIN and then EAN/AN
                if ("NPV".equals(serType)) {
                    flag = matchAN_CAMSID();
                                        
                    if(flag)
                        flag = matchPasscode();
                    
                    //if(flag) -- this should not be here as it is not a fatal error validation - bug 305 fix
                      //  flag = matchEAN_AN();
                    
                    //if failed due to the above 3 checks then do not reject for "TN not eligible to port"
                    if(!flag)
                        ANCAMSID_PC_ANEAN = true;
                    
                } else if ("SP".equals(serType)) {
                    flag = matchAN_CAMSID();
                    
                    if(flag)
                        flag = matchPasscode();
                    
                    //if failed due to the above 2 checks then do not reject for "TN not eligible to port"
                    if(!flag)
                        ANCAMSID_PC_ANEAN = true;
                }
            }
        } else {
            return flag;
        }
        Log.write("LBV  BW isExitOCN traitReUn " + traitReUn);
        if (manflag && traitReUn != null && traitReUn.equalsIgnoreCase("UNEP-ID")) {
            fillterSerTypeRejCode("90008-isExitOCN_Trait_CAMS :BW", flag, "NPV", manflag);
            //fillterSerTypeRejCode("90008-isExitOCN_Trait_CAMS :BW", flag, "SP", manflag); -- code change for bug 1416
            //send to REJECT for simple port and not to MR
            fillterSerTypeRejCode("20007-isExitOCN_Trait_CAMS :BW", false, "SP", false); 
            
        } else if (manflag && traitReUn != null && traitReUn.equalsIgnoreCase("RESOLD-LSP-ID")) {
            fillterSerTypeRejCode("90009-isExitOCN_Trait_CAMS :BW", flag, "NPV", manflag);
            //fillterSerTypeRejCode("90009-isExitOCN_Trait_CAMS :BW", flag, "SP", manflag); -- code change for bug 1416
            //send to REJECT for simple port and not to MR
            fillterSerTypeRejCode("20007-isExitOCN_Trait_CAMS :BW", false, "SP", false); 
            
        } else {
            if(ANCAMSID_PC_ANEAN) {
                //do not add rejection codes as they were already added in the above 3/2 methods
            } else {//add reject code for "TN not eligible to port"
                fillterSerTypeRejCode("10006-isExitOCN_Trait_CAMS :BW", flag, "NPV", manflag);
                fillterSerTypeRejCode("20007-isExitOCN_Trait_CAMS :BW", flag, "SP", manflag);
            }
        }
        if (manflag) {
            return manflag;
        }
        */
            
        return flag;

    }

    /*matchOCN_Trait_CC method used for checking
     *OCN trait value matches CC field in LSR
     */
    public boolean matchOCN_Trait_CC() {
        boolean flag = false;

        String cmpnCode = lsrDataBean.getCompanyCode();
        Ctrt ctrtvalue[] = validationData.getCustTraitList();
        Log.write("LSRBaseValidator matchOCN_Trait_CC ctrtvalue " + ctrtvalue + ": cmpnCode " + cmpnCode);
        if (ctrtvalue != null) {
            for (int i = 0; i < ctrtvalue.length; i++) {
                Ctrt ctrt = ctrtvalue[i];
                TnTrait traitTn[] = ctrt.getTnTrait();
                for (int j = 0; j < traitTn.length; j++) {
                    TnTrait traitvalue = traitTn[j];
                    String trName = traitvalue.getTraitName();
                    String trValue = traitvalue.getTraitValue();
                    Log.write("LBV  BW matchOCN_Trait_CC trName " +
                            trName + " trValue " + trValue);
                    if (trName != null && trValue != null &&
                            cmpnCode != null && (trName.trim().equalsIgnoreCase("RESOLD-LSP-ID")) && (trValue.trim().equalsIgnoreCase(cmpnCode))) {
                        flag = true;
                        break;
                    }
                }
            }
        } else {
            return flag;
        }

        fillterSerTypeRejCode("50003-matchOCN_Trait_CC :BW", flag, "UNEPD", false);
        fillterSerTypeRejCode("40003-matchOCN_Trait_CC :BW", flag, "RED", false);
        fillterSerTypeRejCode("30004-matchOCN_Trait_CC :BW", flag, "DAD", false);
        fillterSerTypeRejCode("80003-matchOCN_Trait_CC :BW", flag, "UNEPS", false);
        fillterSerTypeRejCode("60003-matchOCN_Trait_CC :BW", flag, "RES", false);
        return flag;
    }

    /* matchEAN_AN method used for checking
     *3a PQ  EAN field on the EU form matches value in
     * AN field (CAMS ID) on LSR form
     */
    public boolean matchEAN_AN() {
        String an = lsrDataBean.getAccountNo();
        String ean = lsrDataBean.getExitingAccountNo();
        String camsID = validationData.getCamsId();
        Log.write("LSRBaseValidator matchEAN_AN AN " + an + " : ean " + ean);
        boolean flag = false;
        if (ean != null && an != null && camsID != null && ean.trim().equals(an.trim())) {
            flag = true;
        }
        fillterSerTypeRejCode("10023-matchEAN_AN :BW", flag, "NPV", false);
        fillterSerTypeRejCode("70021-matchEAN_AN :BW", flag, "UNEPV", false);
        return flag;
    }

    /* checkLACT method used for checking
     * 3b PQ LACT on the DL form must be a D when the actyP on the LSR form is
     * E/M and ACT on the LSR form is D for Resale DISC / UNEP DISC
     * Later we can check below method
     */
    public boolean checkLACT() {
        String lact = lsrDataBean.getListActivity();
        String act = lsrDataBean.getActivity();
        String serviceTy = lsrDataBean.getSerRequestType();
        Log.write("LSRBaseValidator checkLACT lact " + lact + ": act " + act + ":serviceTy " + serviceTy);
        boolean flag = false;
        if (lact != null && act != null && serviceTy != null &&
                act.equals("D") && lact.equals("D") &&
                (serviceTy.equals("E") || serviceTy.equals("M"))) {
            flag = true;
        }
        return flag;
    }

    /* checkIsembargoed method used for checking
     * Provider is not Embargoed on Vendor Table -
     *if they are embargoed LSR should reject without any  
     * more validation (move this to SP and UNEP conversion)
     */
    public boolean checkIsembargoed() {
        String embargoed = vendorBean.getIsEmbargoed();
        boolean flag = false;
        Log.write("LSRBaseValidator checkIsembargoed embargoed " + embargoed);
        if (embargoed != null && embargoed.equals("N")) {
            flag = true;
        }
        fillterSerTypeRejCode("10004-checkIsembargoed", flag, "NPV", false);
        fillterSerTypeRejCode("20004-checkIsembargoed", flag, "SP", false);
        fillterSerTypeRejCode("70004-checkIsembargoed", flag, "UNEPV", false);
        fillterSerTypeRejCode("Rejected no further validate UPD checkIsembargoed", flag, "UNEPD", false);

        fillterSerTypeRejCode("Rejected no further validate RSD checkIsembargoed", flag, "RED", false);
        fillterSerTypeRejCode("30003-checkIsembargoed", flag, "DAD", false);
        fillterSerTypeRejCode("Rejected no further validate UNEPS checkIsembargoed", flag, "UNEPS", false);
        fillterSerTypeRejCode("Rejected no further validate RSS checkIsembargoed", flag, "RES", false);
        return flag;
    }

    /* matchAN_CAMSID method used for checking
     *10a V LSR ACCT#SPSR SPAN# matches CAMS ID
     * ( ACCT means account number other form ecxcept SP)
     */
    public boolean matchAN_CAMSID() {
        String span = lsrDataBean.getAccountNo();
        String camsId = validationData.getCamsId();
        Log.write("LSRBaseValidator matchAN_CAMSID span: " + span + " camsId: " + camsId);
        boolean flag = false;
        if (span != null && camsId != null && span.trim().equals(camsId.trim())) {
            flag = true;
        }
        //Code Change for avoiding validation with Cams for Resale Account - Saravanan
        boolean reselFlag = checkTraitNameisResold();
        if(reselFlag)
        {
      	  fillterSerTypeRejCode("10009-matchAN_CAMSID :BW",true, "NPV", false);
      	  fillterSerTypeRejCode("20010-matchAN_CAMSID :BW", true, "SP", false);
        }
        else {
      	  fillterSerTypeRejCode("10009-matchAN_CAMSID :BW", flag, "NPV", false);
      	  fillterSerTypeRejCode("20010-matchAN_CAMSID :BW", flag, "SP", false);
        }
        fillterSerTypeRejCode("70008-matchAN_CAMSID :BW", flag, "UNEPV", false);

        return flag;
    }

    /* matchNNSP_SOA method used for checking
     * 12a V NNSP value is valid in SOA (Call SOA webservice if NNSP is valid)
	 * SOA validation method called from SOAHelper
     */
    public boolean matchNNSP_SOA() {
        String nnsp = lsrDataBean.getNewNetwork();
        boolean flag = false;
        boolean manFlag = false;

        Log.write("LSRBaseValidator matchNNSP_SOA NNSP: " + nnsp);
        try {
            if (nnsp != null) {
                if(lsrDao.checkNNSP(nnsp)) {
                    flag = true;
                }
            }
        } catch(Exception e) {
            Log.write("Exception thrown in CheckNNSP method in SOA : "+e.getMessage());
            manFlag = true;
        }
        
        if(manFlag)
            fillterSerTypeRejCode("100004-matchNNSP_SOA :BW", flag, "NPV", manFlag);
        else
        {
            Log.write("NNSP valid : "+flag);
        
            fillterSerTypeRejCode("10013-matchNNSP_SOA :BW", flag, "NPV", false);
            //Added Reason Code for Simple Ports NNSP reject 20015
            fillterSerTypeRejCode("20015-matchNNSP_SOA :BW", flag, "SP", false);
        }
        return flag;
    }

    /*iseligibleforPortNumber method used for checking
     *14a V Check in vendor table if Provider is eligible to port numbers in the specific exchange
     *
     */
    public boolean iseligibleforPortNumber() {
        int isPortableAreaFlag = lsrDataBean.isPorNumberFlag();
        boolean flag = false;
        
        if(isPortableAreaFlag == 0)
            flag = false;
        else if(isPortableAreaFlag == 1)
            flag = true;
        
        if(isPortableAreaFlag == 0 || isPortableAreaFlag == 1) {
            Log.write("LSRBaseValidator iseligibleforPortNumber flag: " + flag);
            //fix for JIRA issue ISSASOI-14 made by Antony on 05/09/2010
            fillterSerTypeRejCode("10005-iseligibleforPortNumber:LERG", flag, "NPV", false);
            fillterSerTypeRejCode("20005-iseligibleforPortNumber:LERG", flag, "SP", false);
            fillterSerTypeRejCode("70005-iseligibleforPortNumber:LERG", flag, "UNEPV", false);
            
        } else if(isPortableAreaFlag == 2) {
            flag = false;
            Log.write("LSRBaseValidator iseligibleforPortNumber flag: Exception thrown while connecting to LERG. Sending to MR.");
            fillterSerTypeRejCode("100007-iseligibleforPortNumber:LERG", flag, "NPV", true);//add new MR seq no
            fillterSerTypeRejCode("100008-iseligibleforPortNumber:LERG", flag, "SP", true);//add new MR seq no
            fillterSerTypeRejCode("100009-iseligibleforPortNumber:LERG", flag, "UNEPV", true);//add new MR seq no
         
        }

        return flag;
    }

    /* iseligibledeleteDirectory method used for checking
     *14b V if ERL=N and LACT=D on EU form check vendor
     * table to see if vendor is eligible to delete directory
     */
    public boolean iseligibledeleteDirectory() {
        String isdelete = vendorBean.getIsEligibleToDeleteDir();
        boolean flag = false;
        Log.write("LSRBaseValidator iseligibledeleteDirectory isdelete: " + isdelete);
        if (isdelete != null && isdelete.equals("Y")) {
            flag = true;
        }
        fillterSerTypeRejCode("10029-iseligibledeleteDirectory", flag, "NPV", false);
        fillterSerTypeRejCode("70024-iseligibledeleteDirectory", flag, "UNEPV", false);
        return flag;
    }

    /*matchPasscode method used for checking
     *Pass Code - If pass code is in CAMS must match (9 characters)
     */
    public boolean matchPasscode() {
        boolean passcodeExistsFlag = false;
        boolean flag = false;
        String passcode = null;
        Ctrt ctrtvalue[] = validationData.getCustTraitList();
        Log.write("LBV  BW matchPasscode ctrtvalue " + ctrtvalue);
        if (ctrtvalue != null) {
            for (int i = 0; i < ctrtvalue.length; i++) {
                Ctrt ctrt = ctrtvalue[i];
                TnTrait traitTn[] = ctrt.getTnTrait();
                for (int j = 0; j < traitTn.length; j++) {
                    TnTrait traitvalue = traitTn[j];
                    String trName = traitvalue.getTraitName();
                    String trValue = traitvalue.getTraitValue();

                    Log.write("LBV  BW matchPasscode trName " + trName + " trValue  " + trValue);
                    if (trName != null && trName.trim().equalsIgnoreCase("PASSCODE-CAPP")) {
                        passcode = trValue.trim();
                        passcodeExistsFlag = true;
                        Log.write("LBV  BW matchPasscode passcode " + passcode);
                        break;
                    }
                }
            }
        }
        String passcodelsr = lsrDataBean.getPasscodeLSR();

        Log.write("LBV  BW matchPasscode after for loop : passcode " + passcode + " passcodelsr " + passcodelsr);
        
        //Fix for passcode issue - Antony - 07/07/2010
        // if passcode trait exists for customer and if customer has entered a non-blank value for passcode in LSR
        // as per Jon's correction the length check for psswd_pin should be removed
        if(passcodeExistsFlag) { 
            if (passcode != null && passcodelsr != null) {
                passcodelsr = passcodelsr.trim();
                passcode = passcode.trim();
                if (passcodelsr.equalsIgnoreCase(passcode)) {
                    flag = true;
                }

            } else if (passcode == null && passcodelsr == null) {
                flag = true;
            }
        } else {
            flag = true;
        }
        
        //Code Change for avoiding validation with Cams for Resale Account - Saravanan
    	boolean reselFlag = checkTraitNameisResold();
        if(reselFlag)
        {
            fillterSerTypeRejCode("10017-matchPasscode :BW", true, "NPV", false);
            fillterSerTypeRejCode("20020-matchPasscode :BW", true, "SP", false);
        }
        else
        {
            fillterSerTypeRejCode("10017-matchPasscode :BW", flag, "NPV", false);
            fillterSerTypeRejCode("20020-matchPasscode :BW", flag, "SP", false);
        }
        fillterSerTypeRejCode("70015-matchPasscode :BW", flag, "UNEPV", false);
        return flag;

    }

    /*checkLock_CAMS method used for checking
     *PLOCK/LLOCK Traits equal A,E, or B not active trait in CAMS
     *IF RESOLD-LSP-ID or UNEP-ID and PLOCK/LLOCK exist then send to Manual Review
     * Move to Reject Box when  Plock/Lock is active & no resold or unep trait on acct
     */
    public boolean checkLock_CAMS() {
        Log.write("LSRBaseValidator checkLock_CAMS calling :: ");
        boolean flag = false;
        boolean manflag = false;
        Ctrt ctrtvalue[] = validationData.getCustTraitList();
        boolean activeFlag = false;
        boolean re_un_Flag = false;
        Log.write("LBV  BW checkLock_CAMS ctrtvalue " + ctrtvalue);
        PndngOrder pndgOrderlist[] = validationData.getCustPndgOrderList();
        Log.write("LSRBaseValidator checkLock_CAMS pndgOrderlist " + pndgOrderlist);
        if (ctrtvalue != null) {
            for (int i = 0; i < ctrtvalue.length; i++) {
                Ctrt ctrt = ctrtvalue[i];
                TnTrait traitTn[] = ctrt.getTnTrait();
                for (int j = 0; j < traitTn.length; j++) {
                    TnTrait traitvalue = traitTn[j];
                    String trName = traitvalue.getTraitName();
                    String trValue = traitvalue.getTraitValue();

                    Log.write("LBV  BW checkLock_CAMS trName  " + trName + "  trValue " + trValue);
                    if (trName != null && trValue != null &&
                            (trValue.trim().equalsIgnoreCase("AW") || trValue.trim().equalsIgnoreCase("BW") ||
                            trValue.trim().equalsIgnoreCase("EW") || trValue.trim().equalsIgnoreCase("A") ||
                            trValue.trim().equalsIgnoreCase("B") || trValue.trim().equalsIgnoreCase("E"))) {

                       if (trName.trim().equalsIgnoreCase("PIC-LOCK")) {
                          //activeFlag = true;
                          //change made by Antony to make sure if there was a PLOCK and pending order list is empty
                          //so that it goes to reject; will be set to false if there was a pending order found to remove in below
                          //for loop
                          if (pndgOrderlist != null) {
                             for (int k = 0; k < pndgOrderlist.length; k++) {
                                 PndngOrder pndgOrderValue = pndgOrderlist[k];
                                 Log.write("LBV  BW checkLock_CAMS pndgOrderValue " + pndgOrderValue);
                                 if (pndgOrderValue != null) {
									String sts =pndgOrderValue.getOrderStatus();
                                    OrderSegment orderSegment[] = pndgOrderValue.getOrderSegmentList();

                                    Log.write("LBV  BW checkLock_CAMS status: " + sts);
                                    Log.write("LBV  BW checkLock_CAMS orderSegment: " + orderSegment);

                                    if (sts != null && (sts.equalsIgnoreCase("50") || sts.equalsIgnoreCase("A0") || sts.equalsIgnoreCase("B0"))) {
										break;
								    }
                                    if (orderSegment != null) {
                                       for (int l = 0; l < orderSegment.length; l++) {
                                           SneData sneDataList[] = orderSegment[l].getSegSneList();
                                           if (sneDataList != null) {
                                              for (int m = 0; m < sneDataList.length; m++) {
                                                  SneData sneData = sneDataList[m];
                                                  String sneAct = sneData.getSneAction();
                                                  String sneCode = sneData.getSneCode();
                                                  String sneRemarks = sneData.getSneRemarks();
                                                  if (sneAct != null && sneCode != null && sneRemarks != null) {
                                                     if(sneAct.trim().equalsIgnoreCase("I") &&
                                                        sneCode.trim().equalsIgnoreCase("PLOCK") &&
                                                        sneRemarks.trim().equalsIgnoreCase("N")) {

                                                         activeFlag = false;

												     }
                                                  }
                                              }
                                           }
                                       }
                                    }
                                 }
                             }
                          }
                       } else if (trName.trim().equalsIgnoreCase("LOCAL-CARR-LOCK")) {
                                activeFlag = true;
                                //change made by Antony to make sure if there was a LLOCK and pending order list is empty
                                //so that it goes to reject; will be set to false if there was a pending order found to remove in below
                                //for loop
                                 if (pndgOrderlist != null) {
				                    for (int k = 0; k < pndgOrderlist.length; k++) {
				                        PndngOrder pndgOrderValue = pndgOrderlist[k];
				                        Log.write("LBV  BW checkLock_CAMS pndgOrderValue " + pndgOrderValue);
				                        if (pndgOrderValue != null) {
										   String sts =pndgOrderValue.getOrderStatus();
				                           OrderSegment orderSegment[] = pndgOrderValue.getOrderSegmentList();

				                           Log.write("LBV  BW checkLock_CAMS status: " + sts);
                                           Log.write("LBV  BW checkLock_CAMS orderSegment: " + orderSegment);

                                           if (sts != null && (sts.equalsIgnoreCase("50") || sts.equalsIgnoreCase("A0") || sts.equalsIgnoreCase("B0"))) {
										   	   break;
								           }

				                           if (orderSegment != null) {
				                              for (int l = 0; l < orderSegment.length; l++) {
				                                  SneData sneDataList[] = orderSegment[l].getSegSneList();
				                                  if (sneDataList != null) {
				                                     for (int m = 0; m < sneDataList.length; m++) {
				                                         SneData sneData = sneDataList[m];
				                                         String sneAct = sneData.getSneAction();
				                                         String sneCode = sneData.getSneCode();
				                                         String sneRemarks = sneData.getSneRemarks();
				                                         if (sneAct != null && sneCode != null && sneRemarks != null) {
				                                            if(sneAct.trim().equalsIgnoreCase("I") &&
				                                               sneCode.trim().equalsIgnoreCase("LLOCK") &&
				                                               sneRemarks.trim().equalsIgnoreCase("N")) {

				                                                activeFlag = false;

				                                            }
													     }
				                                     }
				                                  }
				                              }
				                           }
				                        }
				                    }
                                 }
                       }
                    }

                    if (trName != null && (trName.trim().equalsIgnoreCase("RESOLD-LSP-ID") ||
                            trName.trim().equalsIgnoreCase("UNEP-ID"))) {
                        re_un_Flag = true;
                    }
                }
            }
        }
        Log.write("LSRBaseValidator checkLock_CAMS activeFlag : " + activeFlag + " re_un_Flag " + re_un_Flag);
        if (re_un_Flag && activeFlag) {
            // manual review
            manflag = true;
        } else if (activeFlag) {
            //reject for this
            flag = false;
        } else if (!activeFlag) {
            // sucess view go to next step
            flag = true;
        } else {
            return flag;
        }

        if (manflag) {
            fillterSerTypeRejCode("90017-checkLock_CAMS :BW", flag, "NPV", manflag);
            fillterSerTypeRejCode("100030-checkLock_CAMS :BW", flag, "SP", manflag);
            fillterSerTypeRejCode("100031-checkLock_CAMS :BW", flag, "UNEPV", manflag);
	} else {
            fillterSerTypeRejCode("10003-checkLock_CAMS :BW", flag, "NPV", manflag);
            fillterSerTypeRejCode("20003-checkLock_CAMS :BW", flag, "SP", false);
            fillterSerTypeRejCode("70003-checkLock_CAMS :BW", flag, "UNEPV", manflag);
	}

        return flag;
    }
    /*checkDIDP_Asoc method used for checking
     *DIDP ASOC does not appear on Customer acct (all lines)
     */

    public boolean checkDIDP_Asoc() {
        Log.write("LSRBaseValidator checkDIDP_Asoc calling ");
        boolean flag = true;
        
        /*
        Gtnl [] gtnlList = validationData.getGtnlTnList();
        
        for (int g = 0; g < gtnlList.length; g++) {
            //loop here for each gtnl -> get asoc list for each gtnl -> check for DIDP
            Asoc [] asoclist = gtnlList[0].getGtnlAsocList();
        */    
            Asoc asoclist[] = validationData.getCustAsocList();
            Log.write("LSRBaseValidator checkDIDP_Asoc BW  asoclist" + asoclist);
            if (asoclist != null) {
                for (int i = 0; i < asoclist.length; i++) {
                    Asoc asoc = asoclist[i];
                    String asocName = asoc.getAsocName();
                    Log.write("LSRBaseValidator checkDIDP_Asoc BW  asocName" + asocName);
                    if (asocName != null && asocName.trim().equalsIgnoreCase("DIDP")) {
                        flag = false;
                        break;
                    }
                }
            }
            
        /*
            if(!flag)
                break;
        }
        */
            
        fillterSerTypeRejCode("90005-checkDIDP_Asoc:BW", flag, "NPV", !flag);
        fillterSerTypeRejCode("20007-checkDIDP_Asoc :BW", flag, "SP", false);
        fillterSerTypeRejCode("100014-checkDIDP_Asoc:BW", flag, "UNEPV", !flag);

        return flag;
    }

    /* matchEach_QuantityTN method used for checking
     *15e V (this is combined with 15d On NP / PS / SPSR form PORTED_NBR/TNS/SPPTN field matches
     * TNs'/TN on GTNL in CAMS: Match each of the TNs / TN value
     *On NP form the TNS in PORTED_NBR field match TN's on GTNL in CAMS if 50 or > project field required
     *TN's quantity not matched or is equal to or greater then 50 TNs then send manual review
     * TN's quantity matches, but value is mismatched reject
     */
    public boolean matchEach_QuantityTN() {
        Log.write("LSRBaseValidator matchEach_QuantityTN calling ");
        boolean flag = false;
        boolean manflag = false;
        List lsrvalues = null;
        List checkFlag = new ArrayList();
        List gtnlsList = new ArrayList();
        List values = new ArrayList();
        HashMap sectionMap = new HashMap();
        String projectField = lsrDataBean.getProject();
        if (lsrDataBean.getPortedNBR() != null && lsrDataBean.getPortedNBR().size() > 0) {
            Log.write("LSRBaseValidator matchEach_QuantityTN lsrvalues  " + lsrvalues);
            lsrvalues = lsrDataBean.getPortedNBR();
        } else if (lsrDataBean.getTeleNumbs() != null && lsrDataBean.getTeleNumbs().size() > 0) {
            lsrvalues = lsrDataBean.getTeleNumbs();
        } else if (lsrDataBean.getProviderTN() != null && lsrDataBean.getProviderTN().size() > 0) {
            lsrvalues = lsrDataBean.getProviderTN();
        }

        Log.write("LSRBaseValidator matchEach_QuantityTN lsrvalues  " + lsrvalues);
        
        Gtnl gtnllist[] = validationData.getGtnlTnList();
        Log.write("LSRBaseValidator matchEach_QuantityTN lsrvalues:  " + lsrvalues);
        Log.write("LSRBaseValidator matchEach_QuantityTN lsrvalues: " + lsrvalues + " gtnllist:" + gtnllist);
        if (gtnllist != null) {
            this.gtnlLenth = gtnllist.length;
            for (int i = 0; i < gtnllist.length; i++) {
                String tncams = gtnllist[i].getGtnlTn();
                if (tncams != null) {
                    tncams = tncams.trim();
                }
                if (tncams != null && tncams.length() > 11) {
                    String str[] = tncams.split("[-]");
                    if (str != null && str.length == 4) {
                        int size1 = Integer.parseInt(str[2]);
                        int size2 = Integer.parseInt(str[3]);
                        for (; size1 <= size2; size1++) {
                            gtnlsList.add(str[0] + str[1] + String.valueOf(size1));
                        }
                    }
                } else {
                    Log.write("  before tncams " + tncams);
                    if (tncams != null) {
                        Log.write("  before tncams " + tncams);
                    }
                    tncams = tncams.replaceAll("-","");
                    gtnlsList.add(tncams);
                }
            }
        }
        if (lsrvalues != null) {
            for (int i = 0; i < lsrvalues.size(); i++) {
                String ptnbrs = (String) lsrvalues.get(i);
                if (ptnbrs != null) {
                    ptnbrs = ptnbrs.trim();
                }
                if (ptnbrs != null && ptnbrs.length() > 12) {
                    String str[] = ptnbrs.split("[-]");
                    if (str != null && str.length == 4) {
                        int size1 = Integer.parseInt(str[2]);
                        int size2 = Integer.parseInt(str[3]);
                        for (; size1 <= size2; size1++) {
                            
                            String tnInRange = String.valueOf(size1);
                            if(tnInRange.trim().length() < 4) {
                                if(tnInRange.trim().length() == 1)
                                    tnInRange = "000"+tnInRange;
                                else if(tnInRange.trim().length() == 2)
                                    tnInRange = "00"+tnInRange;
                                else if(tnInRange.trim().length() == 3)
                                    tnInRange = "0"+tnInRange;
                            }
                                                        
                            String strPtn = str[0] + str[1] + tnInRange;
                            strPtn = strPtn.replaceAll("-","");
                            Log.write("LSRBaseValidator  strPtn " + strPtn);
                            sectionMap.put(strPtn, String.valueOf(i + 1));
                            values.add(strPtn);
                        }
                    }
                } else {
                    Log.write("LSRBaseValidator  ptnbrs " + ptnbrs);
                    if (ptnbrs != null) {
                        Log.write("  before ptnbrs " + ptnbrs);
                        ptnbrs = ptnbrs.replaceAll("-", "");
                        sectionMap.put(ptnbrs.replaceAll("-", ""), String.valueOf(i + 1));
                    }
                    values.add(ptnbrs);
                }
            }
        }
        
        int pnmbrSize = 0;
                
        int gtnlSize = 0;
        if (gtnllist != null && values != null) {
            gtnlSize = gtnlsList.size();
            pnmbrSize = values.size();
            
            Log.write("==gtnlSize =" + gtnlSize + " pnmbrSize " + pnmbrSize);
            Log.write("LSRBaseValidator matchEach_QuantityTN gtnlSize " + gtnlSize + " pnmbrSize:" + pnmbrSize);
            Log.write("LSRBaseValidator matchEach_QuantityTN projectField " + projectField);
            if ((gtnlSize == pnmbrSize) && pnmbrSize <= 999 /*&& projectField != null*/) {
                // sorting Arrays as well as List
                Collections.sort(values);
                Collections.sort(gtnlsList);

                for (int i = 0; i < gtnlSize; i++) {
                    String tncams = (String) gtnlsList.get(i);
                    String tn = (String) values.get(i);

                    Log.write(" LSR tncams =" + tncams + " BW tn " + tn);

                    if (tncams != null && tn != null) {
                        if (tncams.trim().replaceAll("-", "").equals(tn.trim().replaceAll("-", ""))) {
                            checkFlag.add(tncams);
                        } else {
                            String sectionNumber = (String) sectionMap.get(tn.replaceAll("-", ""));
                            Log.write(" LSR tncams = else sectionNumber" + sectionNumber + " BW tn " + tn);
                            if (sectionNumber != null) {
                                gtnlNumbrs = sectionNumber;
                            } else {
                                gtnlNumbrs = tn;
                            }
                            break;
                        }
                    }
                }
                
                if (checkFlag.size() == gtnlSize) {
                    flag = true;
                }

            //} else if ((!(gtnlSize == pnmbrSize) || ((gtnlSize == pnmbrSize) && gtnlSize >= 50))) {
            //bug fix for PI issue 31
            } else if ((!(gtnlSize == pnmbrSize) || ((gtnlSize == pnmbrSize) && pnmbrSize >= 50))) {
                manflag = true;
            }


        } else {
            manflag = true;
        }
        Log.write("===flag= " + flag + "  manflag  " + manflag + " gtnlSize " + gtnlSize);

        Log.write("===projectField outside ----> " + projectField);

       
        boolean projectFlag = (projectField == null || projectField.equals("null"));
        boolean pnmbrSizeFlag = pnmbrSize >= 50;
        
        //Code Change for avoiding validation with Cams for Resale Account - Saravanan
        boolean resold_flag = checkTraitNameisResold();

         Log.write("===projectField projectFlag --LL--> " + projectFlag +" pnmbrSizeFlag  "+pnmbrSizeFlag);
        if (projectFlag && pnmbrSizeFlag) {
            Log.write("===projectField inside -pnmbrSizeFlag--kk>10016 " + projectField);
            manflag = true;
            if(resold_flag)
            	fillterSerTypeRejCode("10016-matchQua:BW", true, "NPV", false);
            else 
            	fillterSerTypeRejCode("10016-matchQua:BW", false, "NPV", true);
            fillterSerTypeRejCode("10016-matchQua:BW", false, "UNEPV", true);
        }

        if (pnmbrSize >= 50) {
            flag = false;
            Log.write("===pnmbrSize= if >= 50");
             if(resold_flag)
            	fillterSerTypeRejCode("90011-matchQualityTN :BW", true, "NPV", false);
            else 
            	fillterSerTypeRejCode("90011-matchQualityTN :BW", false, "NPV", true);
            fillterSerTypeRejCode("100016-matchQualityTN :BW", false, "UNEPV", true);
        } else if (manflag) {
            gtnlNumbrs = null;
            if(resold_flag)
            	fillterSerTypeRejCode("90006-matchQualityTN :BW", true, "NPV", false);
            else 
            	fillterSerTypeRejCode("90006-matchQualityTN :BW", flag, "NPV", manflag);
            fillterSerTypeRejCode("100015-matchQualityTN :BW", flag, "UNEPV", manflag);
        } else {
            if(resold_flag)
       		 fillterSerTypeRejCode("10027-matchQualityTN :BW", true, "NPV", false);
            else 
           	 fillterSerTypeRejCode("10027-matchQualityTN :BW", flag, "NPV", manflag);
            fillterSerTypeRejCode("70027-matchQualityTN :BW", flag, "UNEPV", manflag);
        }

        return flag;

    }

    /*isExit_AcessLine method used for checking
     * In CAMS, verify access line exist on all tn levels 
     */
    public boolean isExit_AcessLine() {
        boolean flag = false;
        Log.write("LSRBaseValidator isExit_AcessLine calling: ");
        Gtnl gtnl[] = validationData.getGtnlTnList();
        Log.write("LSRBaseValidator isExit_AcessLine gtnl: " + gtnl);
        if (gtnl != null) {
            for (int i = 0; i < gtnl.length; i++) {
                Gtnl gtnlvalue = gtnl[i];
                String lineId = gtnlvalue.getGtnlAccessLineInd();
                Log.write(" isExit_AcessLine  lineId " + lineId);
                if (lineId != null && (lineId.trim().equalsIgnoreCase("Y") || lineId.trim().equalsIgnoreCase("R"))) {//added R for Ring Plus
                    flag = true;
                } else {
                    flag = false;
                    break;
                }
            }
        }
        
        if (flag) {
            fillterSerTypeRejCode("10006-isExit_AcessLine :BW", flag, "NPV", !flag);
            fillterSerTypeRejCode("70006-isExit_AcessLine :BW", flag, "UNEPV", !flag);
        } else {
            fillterSerTypeRejCode("90010-isExit_AcessLine :BW", flag, "NPV", !flag);
            fillterSerTypeRejCode("100024-isExit_AcessLine :BW", flag, "UNEPV", !flag);
        }
        return flag;
    }

    
    /* checkProjectfield method used for checking
     * Project field on LSR form is not populated
     *
     */
    public boolean checkProjectfield() {
        String project = lsrDataBean.getProject();
        Log.write("LSRBaseValidator checkProjectfield project: " + project);
        boolean flag = false;
        if (project == null) {
            flag = true;
        }

        fillterSerTypeRejCode("90012-checkProjectfield", flag, "NPV", !flag);
        fillterSerTypeRejCode("100026-checkProjectfield", flag, "UNEPV", !flag);
        return flag;

    }

    /* isTCOPT method used for checking
     * If TCOPT field is blank or N
     *
     */
    public boolean isTCOPT() {

        String tcopt = lsrDataBean.getTransCallOption();
        Log.write("LSRBaseValidator isTCOPT tcopt: " + tcopt);
        boolean flag = false;
        if (tcopt == null || tcopt.equals("N")) {
            flag = true;
        }
        fillterSerTypeRejCode("100027-isTCOPT", flag, "UNEPD", !flag);
        fillterSerTypeRejCode("100028-isTCOPT", flag, "RED", !flag);
        fillterSerTypeRejCode("UNEPS isTCOPT", flag, "UNEPS", false);
        fillterSerTypeRejCode("RSS isTCOPT", flag, "RES", false);
        return flag;
    }

    /* checkReqTypeRes method used for checking
     *V 23 Buisness Flow
     * Check if value in RTR field on LSR form is C
     *
     */
    public boolean checkReqTypeRes() {
        String rtr = lsrDataBean.getResTypeReq();
        boolean flag = false;
        Log.write("LSRBaseValidator checkReqTypeRes rtr: " + rtr);
        if (rtr != null && rtr.equals("C")) {
            flag = true;
        }
        fillterSerTypeRejCode("10018-checkReqTypeRes", flag, "NPV", false);
        fillterSerTypeRejCode("50011-checkReqTypeRes", flag, "UNEPD", false);
        fillterSerTypeRejCode("70016-checkReqTypeRes", flag, "UNEPV", false);
        fillterSerTypeRejCode("40011-checkReqTypeRes", flag, "RED", false);
        fillterSerTypeRejCode("80011-checkReqTypeRes", flag, "UNEPS", false);
        fillterSerTypeRejCode("60012-checkReqTypeRes", flag, "RES", false);
        return flag;
    }

    /*checkCAMS_TN_CAMPRD3 method used for checking
     *End User Customer is not in OOT CAMS Database (CAMPRD3)
     *TN not found in CAMPRD 3 or CAMFFC
     *
     */
    public boolean checkCAMS_TN_CAMPRD3() {
        String db = validationData.getCustDatabase();
        Log.write("LSB checkCAMS_TN_CAMPRD3 db " + db);
        boolean flag = false;
        if (db != null &&
                (!db.trim().equalsIgnoreCase("CAMPRD3") &&
                !db.trim().equalsIgnoreCase("CAMFFC"))) {
            flag = true;
        }
        fillterSerTypeRejCode("100023-checkCAMS_TN_CAMPRD3 :BW", flag, "NPV", !flag);//send to MR not reject
        fillterSerTypeRejCode("100023-checkCAMS_TN_CAMPRD3 :BW", flag, "SP", !flag);//send to MR not reject
        return flag;
    }

    /*
     * dateToString(yyyyMMdd hhmmss) method used for date object to String object
     */
    public String dateToString(Date date) {
        Log.write("LSRBaseValidator dateToString date: " + date);
        String DATE_FORMAT = "yyyyMMdd hhmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Log.write("Today is " + sdf.format(date));
        return sdf.format(date);
    }

    /*
     * dateToString(MM-dd-yyyy) method used for String object to date object
     */
    public Date dateToString(String strdate) {
        Log.write("LSRBaseValidator dateToString strdate: " + strdate);
        String DATE_FORMAT = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {

            Log.write("Today is " + sdf.parse(strdate));
            return sdf.parse(strdate);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /*
     * dateToString1(yyyyMMdd hhmmss) method used for String object to date object
     */
    public Date dateToString1(String strdate) {
        Log.write("LSRBaseValidator dateToString1 strdate: " + strdate);
        String DATE_FORMAT = "yyyyMMdd hhmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {

            Log.write("Today is " + sdf.parse(strdate));
            return sdf.parse(strdate);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /*
     * dateToString1(MM-dd-yyyy) method used for date object to String object
     */
    public String dateToString1(Date date) {
        Log.write("LSRBaseValidator dateToString1 date: " + date);
        String DATE_FORMAT = "MM-dd-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Log.write("Today is " + sdf.format(date));
        return sdf.format(date);
    }

    public String dateToStringYYMMDD(Date date) {
        Log.write("LSRBaseValidator dateToString1 date: " + date);
        String DATE_FORMAT = "yyyyMMdd";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Log.write("Today is " + sdf.format(date));
        return sdf.format(date);
    }

    public VendorTableDataBean getVendorTableDataBean() {
        return vendorBean;
    }

    public LSRDataBean getLSRDataBean() {
        return lsrDataBean;
    }

    public void setLSRDataBean(LSRDataBean lsrDataBean) {
        this.lsrDataBean = lsrDataBean;
    }

    public ValidationDataBean getValidationDataBean() {
        return validationData;
    }

    public String getStatusManual() {
        return statusManual;
    }

    public void setStatusManual(String statusManual) {
        this.statusManual = statusManual;
    }

    public Vector getHoldayVector() {
        return holdayVector;
    }

    public void setHoldayVector(Vector holdayVector) {
        this.holdayVector = holdayVector;
    }
    
    public Map getStreetAddrsMap() {
        return streetAddrsMap;
    }

    public void setStreetAddrsMap(Map streetAddrsMap) {
        this.streetAddrsMap = streetAddrsMap;
    }

    public int getGtnlLenth() {
        return gtnlLenth;
    }

    public void setGtnlLenth(int gtnlLenth) {
        this.gtnlLenth = gtnlLenth;
    }

    /**
     * @return the gtnlNumbrs
     */
    public String getGtnlNumbrs() {
        return gtnlNumbrs;
    }

    /**
     * @param gtnlNumbrs the gtnlNumbrs to set
     */
    public void setGtnlNumbrs(String gtnlNumbrs) {
        this.gtnlNumbrs = gtnlNumbrs;
    }

    /**
     * @return the unepAsocValue
     */
    public String getUnepAsocValue() {
        return unepAsocValue;
    }

    /**
     * @param unepAsocValue the unepAsocValue to set
     */
    public void setUnepAsocValue(String unepAsocValue) {
        this.unepAsocValue = unepAsocValue;
    }
    
     /**
     * @return boolean flag if to perform Passcode validation
     */
    public boolean getDoPasscodeValidation() {
        return doPasscodeValidation;
    }

    /**
     * @param set boolean flag if to perform Passcode validation
     */
    public void setDoPasscodeValidation(boolean doPasscodeValidation) {
        this.doPasscodeValidation = doPasscodeValidation;
    }
    
    public void updateSLATimeforSP() {

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
            
            /*
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
             */


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
        }
    }
    
    /* method to check SV status in SOA for SUPP 2&3 Q&V  - Antony - 05/02/2011*/
     
    public Hashtable checkSVStatusInSOA(String checkStatus) 
    {
        Hashtable htResult = new Hashtable();
        
        boolean result = false; 
        
        boolean proceedWithValidation = false;
        
        //String atn = lsrDataBean.getAccountTelephoneNo(); -- will be blank as atn on LSR form is empty for Simple Orders
        
        List tnList = lsrDataBean.getPortedNBR();
        
        String atn = (String) tnList.get(0);
        
        Log.write("ATN from tnList :"+atn);
        
        LSRdao lsrDao = new LSRdao();


	//code change for getONSP -- start
	 String lsrONSP = "";
        
	//first obtain ONSP from SOA table

        lsrONSP = lsrDao.getONSP(lsrDataBean.getReqstNmbr(),atn);
        
        Log.write("value of lsrONSP after getONSP : "+lsrONSP);

	try {//if still null get from WIN SPIDS table
        
                //second fix to get WCN from request_t table as this method is called only for SUPP2/3's
                //and there may not be a value returned by getWCNCheckFlag sometimes 
                //and to get the state code from address map as getstatecd() returns full state name - start
                //- start
                // Antony - 02122013
                
                String strWCN = lsrDao.getWCN(lsrDataBean.getReqstNmbr());
                
                Log.write("Value of strWCN returned by getWCN in LSRBaseValidator:checkSVStatusInSOA : "+strWCN);
                
        if(lsrONSP == null || lsrONSP.length() == 0) {

                        Map mapAddress = lsrDataBean.getAddressMap();
                        AddressBean addr = (AddressBean) mapAddress.get("EU_LA");
                        Log.write("LSRBaseValidator setting state value with EU state value :" + addr.getState());
                                    
	 		lsrONSP = lsrDao.getWINSPID(addr.getState(),strWCN);
		Log.write("value of lsrONSP after getWINSPID : "+lsrONSP);
	 }
	} catch(Exception ex) {
		  	Log.write("DB Exception while calling getWINSPID method :"+ex.getMessage());
	}

	//if still null send to Manual Review

	if(lsrONSP == null || lsrONSP.length() == 0) {
		//send to MR -- "lsrONSP is null."
		try
		{
		    lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"110010","ONSP retrieved is null. Unable to do SOA status check for SUPP.");
		    return null;//return null hashtable
		}
		catch(Exception ex)
		{
		  Log.write("DB Exception while calling MR status update procedure:"+ex.getMessage());
		}
	}

	//code change for getONSP fix -- end

        String[] respArray = null;
        
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
                             NNSPValueInSV.equals("1482") ||
                             NNSPValueInSV.equals("7002")
                            )) {//add method call here to pass NNSP in SV to db method and check win_spids table for presence of this SPID value
                            //validation is a pass -- add to passed array here
                            Log.write("SUPP1 PON passed SOA SV status validation!");
                            result = true;
                    
                            proceedWithValidation = true;
                            
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:nonWINSPID", result, "NPV", false); //add reason code sqnc nmbr
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:nonWINSPID", result, "SP", false); //add reason code sqnc nmbr
                            
                            if(svStatus.equals("canceled") || svStatus.equals("cancel-pending")) {
                                Log.write("SV status is canceled or cancel-pending so skip V6 & V9");
                                result = false;
                                proceedWithValidation = true;
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
                            
                            proceedWithValidation = true;
                            
                        }
                    } else if(svStatus.equals("active")) {
                        
                        Log.write("SV status is active.");
                        
                        if(NNSPValueInSV.equals("1180") ||
                             NNSPValueInSV.equals("4263") ||
                             NNSPValueInSV.equals("8334") ||
                             NNSPValueInSV.equals("0999") ||
                             NNSPValueInSV.equals("2147") ||
                             NNSPValueInSV.equals("1226") ||
                             NNSPValueInSV.equals("938D") ||
                             NNSPValueInSV.equals("7815") ||
                             NNSPValueInSV.equals("1482") ||
                             NNSPValueInSV.equals("7002")
                          ) {
                        
                            Log.write("NNSP is a windstream SPID so V4 is a pass.Skip V6&V9");
                            
                            result = true;
                            
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:WINDSPID", result, "NPV", false); //add reason code sqnc nmbr
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:WINDSPID", result, "SP", false); //add reason code sqnc nmbr
                            
                            //make result false as we need to skip V6&V9
                            result = false;
                            
                            proceedWithValidation = true;
                            
                        } else {
                            Log.write("SV is a non-windstream SPID so V4 is a reject.Fatal reject.Skip V5,V6&V9");
                            
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:nonWINDSPID", result, "NPV", false); //add reason code sqnc nmbr
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:nonWINDSPID", result, "SP", false); //add reason code sqnc nmbr
                            
                            //make result false as we need to skip V6&V9
                            result = false;
                            
                            proceedWithValidation = false;
                            
                        }
                        
                    } else {
                        //reject and proceed to V5-- this method should be called in processInitialValidations
                        //add to reject/error array with error message "SV status in SOA is not valid for sending a CancelSV request."
                        fillterSerTypeRejCode("100039-checkSVStatusInSOAPending:invalidstatus", result, "SP", false); //add reason code sqnc nmbr
                        fillterSerTypeRejCode("100039-checkSVStatusInSOAPendinginvalidstatus", result, "NPV", false); //add reason code sqnc nmbr
                        Log.write("SV status in SOA is not valid for sending a CancelSV request.So skip V6&V9");
                        
                        result = false;
                        
                        proceedWithValidation = true;
                            
                    }
                } else {
                    
                    // this else block will never be executed as a separate method with no params in the SLATimer is the one
                    // which is used for checking if SV went to Cancelled when SLA timer fires
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
                
                proceedWithValidation = true;
                            
            }
            
                
	    
        }
        catch (java.net.MalformedURLException e)
        {
            e.printStackTrace();        
            Log.write("MalformedURLException thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
 
            proceedWithValidation = true;
                            
        }
        catch (java.rmi.RemoteException e)
        {

            e.printStackTrace();        
            Log.write("RemoteException thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
            
            proceedWithValidation = true;
            
        }
        catch (Exception e)
        {
            e.printStackTrace();        
            Log.write("Exception thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
            
            proceedWithValidation = true;
            
        }
        
        if(respArray[1].indexOf("SvStatus value=\"pending\"") > 0 || 
           respArray[1].indexOf("SvStatus value=\"disconnect-pending\"") > 0 || 
           respArray[1].indexOf("SvStatus value=\"conflict\"") > 0) {
            
            Log.write("SOA cancel valid status found among multiple SVs");
            htResult.put("skipV6V9","true");
            htResult.put("proceedToV5","true");
        } else {
            htResult.put("skipV6V9",String.valueOf(result));
            htResult.put("proceedToV5",String.valueOf(proceedWithValidation));
        }
        
        return htResult;
    }

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
    
    /* method to check SV status in SOA for SUPP 1 Q&V  - Antony - 01/06/2012*/
     
    public Hashtable checkSVStatusInSOASUPP1(String checkStatus,String atn) 
    {
        Hashtable htResult = new Hashtable();
        
        boolean result = false; 
        
        boolean proceedWithValidation = false;
        
        String svStatus = "";
        
        String svActiveStatus = "non-active";
        
        //String atn = lsrDataBean.getAccountTelephoneNo(); -- will be blank as atn on LSR form is empty for Simple Orders
        
        /*
        List tnList = lsrDataBean.getPortedNBR();
        
        String atn = (String) tnList.get(0);
        */
        
        Log.write("ATN from tnList :"+atn);

        LSRdao lsrDao = new LSRdao();

        //code change for getONSP -- start
	String lsrONSP = "";
        
	//first obtain ONSP from SOA table
        
        lsrONSP = lsrDao.getONSP(lsrDataBean.getReqstNmbr(),atn);
        
        Log.write("value of lsrONSP after getONSP : "+lsrONSP);

	try {//if still null get from WIN SPIDS table
        
                //second fix to get WCN from request_t table as this method is called only for SUPP1's
                //and there may not be a value returned by getWCNCheckFlag sometimes 
                // and to get the state code from address map as getstatecd() returns full state name - start
                //- start
                // Antony - 02122013
        
                String strWCN = lsrDao.getWCN(lsrDataBean.getReqstNmbr());
                
                Log.write("Value of strWCN returned by getWCN in LSRBaseValidator:checkSVStatusInSOASUPP1 : "+strWCN);
                
                if(lsrONSP == null || lsrONSP.length() == 0) {

                        Map mapAddress = lsrDataBean.getAddressMap();
                        AddressBean addr = (AddressBean) mapAddress.get("EU_LA");
                        Log.write("LSRBaseValidator setting state value with EU state value :" + addr.getState());
                                    
	 		lsrONSP = lsrDao.getWINSPID(addr.getState(),strWCN);
			Log.write("value of lsrONSP after getWINSPID : "+lsrONSP);
	 	}
	} catch(Exception ex) {
		  	Log.write("DB Exception while calling getWINSPID method :"+ex.getMessage());
	}
        
	//if still null send to Manual Review
        
	if(lsrONSP == null || lsrONSP.length() == 0) {
		//send to MR -- "lsrONSP is null."
		try
		{
		    lsrDao.callMRStatusUpdateProc(lsrDataBean.getReqstPon(),lsrDataBean.getReqstVer(),lsrDataBean.getOCNcd(),"110010","ONSP retrieved is null. Unable to do SOA status check for SUPP.");
		    return null;//return null hashtable
		}
		catch(Exception ex)
		{
		  Log.write("DB Exception while calling MR status update procedure:"+ex.getMessage());
		}
	}
        
	//code change for getONSP fix -- end

        String[] respArray = null;
        
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

                String NNSPValueInSV = "";

                /*
                 *<SvStatus value="active"/>
                 *<OldSP value="1180"/>
                 */
                
                startIndex = soapSyncResponse.lastIndexOf("SvStatus value=");
                startIndex = startIndex + 16;
                endIndex = soapSyncResponse.indexOf("/>",startIndex) - 1;
                
                svStatus = soapSyncResponse.substring(startIndex,endIndex);
                

                Log.write("svStatus value in query response : "+svStatus);
                
                startIndex = soapSyncResponse.lastIndexOf("NewSP value=");
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
                             NNSPValueInSV.equals("1482") ||
                             NNSPValueInSV.equals("7002")
                            )) {//add method call here to pass NNSP in SV to db method and check win_spids table for presence of this SPID value
                            //validation is a pass -- add to passed array here
                            Log.write("SUPP1 PON passed SOA SV status validation!");
                            result = true;
                    
                            proceedWithValidation = true;
                            
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:nonWINSPID", result, "NPV", false); //add reason code sqnc nmbr
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:nonWINSPID", result, "SP", false); //add reason code sqnc nmbr
                            
                            if(svStatus.equals("canceled") || svStatus.equals("cancel-pending")) {
                                Log.write("SV status is canceled or cancel-pending so skip V6 & V9");
                                result = false;
                                proceedWithValidation = true;
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
                            
                            proceedWithValidation = true;
                            
                        }
                    } else if(svStatus.equals("active")) {
                        
                        Log.write("SV status is active.");
                        
                        if(NNSPValueInSV.equals("1180") ||
                             NNSPValueInSV.equals("4263") ||
                             NNSPValueInSV.equals("8334") ||
                             NNSPValueInSV.equals("0999") ||
                             NNSPValueInSV.equals("2147") ||
                             NNSPValueInSV.equals("1226") ||
                             NNSPValueInSV.equals("938D") ||
                             NNSPValueInSV.equals("7815") ||
                             NNSPValueInSV.equals("1482") ||
                             NNSPValueInSV.equals("7002")
                          ) {
                        
                            Log.write("NNSP is a windstream SPID so V4 is a pass.Skip V6&V9");
                            
                            result = true;
                            
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:WINDSPID", result, "NPV", false); //add reason code sqnc nmbr
                            fillterSerTypeRejCode("100038-checkSVStatusInSOAPending:active:WINDSPID", result, "SP", false); //add reason code sqnc nmbr
                            
                            //make result false as we need to skip V6&V9
                            result = false;
                            
                            proceedWithValidation = true;
                            
                        } else {
                            Log.write("SV is a non-windstream SPID so V4 is a reject.Fatal reject.Skip V5,V6&V9");
                            
                            fillterSerTypeRejCode("10007-checkSVStatusInSOAPending:active:nonWINDSPID", result, "NPV", false); //add reason code sqnc nmbr
                            fillterSerTypeRejCode("20008-checkSVStatusInSOAPending:active:nonWINDSPID", result, "SP", false); //add reason code sqnc nmbr
                
                            //set active indicator to retain in NP form - Antony - 01/11/2012
                            svActiveStatus = "active";
                            
                            //make result false as we need to skip V6&V9
                            result = false;
                            
                                    proceedWithValidation = false;
                            
                        }
                        
                    } else {
                        //reject and proceed to V5-- this method should be called in processInitialValidations
                        //add to reject/error array with error message "SV status in SOA is not valid for sending a CancelSV request."
                        fillterSerTypeRejCode("100039-checkSVStatusInSOAPending:invalidstatus", result, "SP", false); //add reason code sqnc nmbr
                        fillterSerTypeRejCode("100039-checkSVStatusInSOAPendinginvalidstatus", result, "NPV", false); //add reason code sqnc nmbr
                        Log.write("SV status in SOA is not valid for sending a CancelSV request.So skip V6&V9");
                        
                        result = false;
                        
                        proceedWithValidation = true;
                            
                    }
                } else {
                    
                    // this else block will never be executed as a separate method with no params in the SLATimer is the one
                    // which is used for checking if SV went to Cancelled when SLA timer fires
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
                
                proceedWithValidation = true;
                            
            }
            
                
	    
        }
        catch (java.net.MalformedURLException e)
        {
            e.printStackTrace();        
            Log.write("MalformedURLException thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
 
            proceedWithValidation = true;
                            
        }
        catch (java.rmi.RemoteException e)
        {

            e.printStackTrace();        
            Log.write("RemoteException thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
            
            proceedWithValidation = true;
            
        }
        catch (Exception e)
        {
            e.printStackTrace();        
            Log.write("Exception thrown in checksvstatus method : "+e.getMessage());
	    //add manual error code to manual error Array -- "Unable to check status in SOA.Error while calling SOA API."
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "SP", true); //add reason code sqnc nmbr
            fillterSerTypeRejCode("100041-checkSVStatusInSOACanceled", result, "NPV", true); //add reason code sqnc nmbr
            
            proceedWithValidation = true;
            
        }
        
        if(respArray[1].indexOf("SvStatus value=\"pending\"") > 0 || 
           respArray[1].indexOf("SvStatus value=\"disconnect-pending\"") > 0 || 
           respArray[1].indexOf("SvStatus value=\"conflict\"") > 0) {
            
            Log.write("SOA cancel valid status found among multiple SVs");
            htResult.put("skipV6V9","true");
            htResult.put("proceedToV5","true");
            htResult.put("svStatus",svActiveStatus);
        } else {
            htResult.put("skipV6V9",String.valueOf(result));
            htResult.put("proceedToV5",String.valueOf(proceedWithValidation));
            htResult.put("svStatus",svActiveStatus);
        }
        
        return htResult;
    }

    /* method to send cancel request for SV in SOA for SUPP 1 Q&V  - Antony - 01/06/2012*/
     
    public boolean cancelSVInSOASUPP1(String atn) 
    {
        boolean result = false; 
        
        LSRdao lsrDao = new LSRdao();
        
        //String atn = lsrDataBean.getAccountTelephoneNo(); -- will be blank as atn on LSR form is empty for Simple Orders
        //String lsrONSP = "1180"; //get using state cd,atn using lsrdao method -- todo
        
        /*
        List tnList = lsrDataBean.getPortedNBR();
        
        String atn = (String) tnList.get(0);
        */
        
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
    
    
    /*method to send cancel dcris order to BW WS -- Antony - 05/05/2011*/
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
                
                //String OLDSPValue = "1180";//todo -- get from win spids table for given state and npa
                
                String OLDSPValue = lsrDao.getONSP(lsrDataBean.getReqstNmbr(),atn);
        
                Log.write("value of OLDSPValue : "+OLDSPValue);
        
                
                String strVersion = lsrDataBean.getReqstVer();
                int reqVersion = Integer.parseInt(strVersion);
     
                reqVersion = reqVersion - 1;
     
                strVersion = String.valueOf(reqVersion);
     
                
                Hashtable htDCRISOrderParams = lsrDao.retrieveDCRISOrderParams(atn,OLDSPValue,lsrDataBean.getReqstNmbr(),strVersion);
            
                //if hashtable returned is null then it means no order found for previous version of PON
                //if so send true as we need to proceed to next step and not send to MR
                if(htDCRISOrderParams == null) {
                    Log.write("No order found for previous version of PON with ATN: "+atn);
                    return true;
                }
            

                rqstSqncNmbr = (String) htDCRISOrderParams.get("RQST_SQNC_NMBR");
                rqstVersion = (String) htDCRISOrderParams.get("RQST_VRSN");
                rqstPON = (String) htDCRISOrderParams.get("RQST_PON");
                String strBOID = "";//get BOID BEX value after splitting order number
                String strBEX = "";
                String strOrderNumber = (String) htDCRISOrderParams.get("ORDER_NO");
                strOCNcd = (String) htDCRISOrderParams.get("OCN_CD");
                String strSPFlag = (String) htDCRISOrderParams.get("SP_FLAG");
                String strCLECInd = (String) htDCRISOrderParams.get("CLEC_IND");
                String strFOCDueDate = (String) htDCRISOrderParams.get("FOC_DDD");
             
                  //create SimpleDateFormat object with source string date format
                SimpleDateFormat sdfSource = new SimpleDateFormat("MM-dd-yyyy");
        
            
                try {
                    Date date = sdfSource.parse(strFOCDueDate);
                    //create SimpleDateFormat object with desired date format
                    SimpleDateFormat sdfDestination = new SimpleDateFormat("yyMMdd");

                    //parse the date into another format
                    strFOCDueDate = sdfDestination.format(date);  

                } catch (ParseException ex) {
                    Log.write("Error while parsing FOC due date. Error Message : "+ex.getMessage());
                    ex.printStackTrace();
                }
                
                
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
                        orderData[i-1].setOrderDueDate(strFOCDueDate);//lr_t    
                        
                        System.out.print("orderData order number : "+orderData[i-1].getOrderNumber());
                        System.out.print("orderData order duedate : "+orderData[i-1].getOrderDueDate());
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
    
    /* checkSUPP method used for checking
     * When version of PON is a 0 SUPP value in LSR should be blank
     * This validation was moved from NPValidator to LSRBaseValidator 
     * as that is common to both simple and complex NP PONs
     * Antony 09-08-2011
     */
    public boolean checkSUPP() {
        String supp = lsrDataBean.getSupplementalType();
        String reqVrsn = lsrDataBean.getReqstVer();
        boolean flag = true;
        
        if(reqVrsn != null && reqVrsn.trim().equals("0") && supp != null)
         flag = false;
        else if(reqVrsn != null && !reqVrsn.trim().equals("0") && (supp == null || supp.length() == 0))
         flag = false;
        
        Log.write("NPValidator checkSUPP validation result: "+flag+" for SUPP value of: "+supp);
        
        fillterSerTypeRejCode("10014-checkSUPP", flag, "NPV", false);
        fillterSerTypeRejCode("10014-checkSUPP", flag, "SP", false);
        
        return flag;
    }
    
    
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
     } else if (focdueDate.equals(" 21:00:00")) {//we don't have foc date in lr_t but 21:00 string has been added to an empty string
//         focdueDate = focdueDate + " 21:00:00";
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
            
     
     if(focdateflag)
         Log.write("FOC due date not past yet.");
     else
         Log.write("FOC due date is past.");
     
     //fix for Sup after FOC date change - Antony - 10/30/2012
     Log.write("PON SUPP type: "+lsrDataBean.getSupplementalType());
     if(lsrDataBean.getSupplementalType() != null && lsrDataBean.getSupplementalType().equals("1")) {
        focdateflag = true;
     }
     
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
    
    /*method to check for SUPP field to be blank if version is 0
     *Fix for SPIRA incident 3752 -- Antony -- 06/29/2011
     *20016	R	2A	Invalid SUP value	28-JAN-11	e0034987	Simple Port Service Request	Simple Port
      50009	R	2A	Invalid SUP value	23-AUG-09	korchnak	Port/Loop Service and Unbundled Local Switching	Disconnection
      80009	R	2A	Invalid SUP value	02-SEP-09	korchnak	Port/Loop Service and Unbundled Local Switching	Suspend
      60009	R	2A	Invalid SUP value	25-AUG-09	korchnak	Resale	Suspend
      70012	R	2A	Invalid SUP value	25-AUG-09	korchnak	Port/Loop Service and Unbundled Local Switching	Conversion of Service to new LSP
      10014	R	2A	Invalid SUP value	31-JUL-09	korchnak	Number Portability	Conversion of Service to new LSP
      40009	R	2A	Invalid SUP value	20-AUG-09	korchnak	Resale	Disconnection
     */
    
    public String RemoveSpecial(String addressStr) {

        String pattern = "[^A-Za-z0-9]";

        Log.write("Original String is:         "+addressStr);
        
        String removedString = addressStr.replaceAll(pattern, "");
        
        Log.write("After Replacing Characters: "+removedString);

        return removedString;
    }

    /*method to check if all SVs for a goto PON are in pending status - Antony - 02/13/2012*/
    public boolean checkIfAllSVsInPendingStatus(List tnList,String reqNo,String reqVer) {
        boolean isSVInPendingStatus = true;
        
        String svStatus = "";
        
        for(int i = 0; i < tnList.size(); i++) {
        
            String atn = (String) tnList.get(i);
        
            Log.write("checkIfAllSVsInPendingStatus:ATN from tnList :"+atn);

            LSRdao lsrDao = new LSRdao();

            //String lsrONSP = lsrDao.getONSP(reqNo,atn);
            
            String lsrONSP = "";
            
            try {
            
                //second fix to get WCN from request_t table as this method is called only for SUPPs
                // and to get the state code from address map as getstatecd() returns full state name - start
                // Antony - 02082013
                
                String strWCN = lsrDao.getWCN(reqNo);
                
                Log.write("Value of strWCN returned by getWCN in LSRBaseValidator:checkIfAllSVsInPendingStatus : "+strWCN);
                
                Map mapAddress = lsrDataBean.getAddressMap();
                AddressBean addr = (AddressBean) mapAddress.get("EU_LA");
                Log.write("LSRBaseValidator setting state value with EU state value :" + addr.getState());
                
                //second fix -- end
                
                lsrONSP = lsrDao.getWINSPID(addr.getState(),strWCN);
                
                Log.write("Value of lsrONSP returned by getWINSPID in LSRBaseValidator:checkIfAllSVsInPendingStatus : "+lsrONSP);

            } catch(Exception e) {
                Log.write("Exception in LSRBaseValidator:checkIfAllSVsInPendingStatus : "+e.getMessage());
            }
            
            Log.write("checkIfAllSVsInPendingStatus:value of lsrONSP : "+lsrONSP);

            String[] respArray = null;

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
              Log.write("checkIfAllSVsInPendingStatus:Date value sent in svQueryRequest xml:"+strCurrentDate);


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

                Log.write( "checkIfAllSVsInPendingStatus:processSync called...");
                Log.write( "contacting server [" + soapURL +"]\n" + 
                            "with soapParam1 ["+header+"]\n" + "and soapParam2 ["+body+"]\n");

                respArray = stub.processSync(header,body);
                Log.write("checkIfAllSVsInPendingStatus:processSync returned: soapParam1=" + respArray[0] + " =" + respArray[1] );


                if(respArray[1].indexOf("SOARequestStatus value=\"success\"") > 0) {

                    String soapSyncResponse = respArray[1]; 

                    int startIndex = 0;
                    int endIndex = 0;

                    String NNSPValueInSV = "";

                    /*
                     *<SvStatus value="active"/>
                     *<OldSP value="1180"/>
                     */

                    startIndex = soapSyncResponse.lastIndexOf("SvStatus value=");
                    startIndex = startIndex + 16;
                    endIndex = soapSyncResponse.indexOf("/>",startIndex) - 1;

                    svStatus = soapSyncResponse.substring(startIndex,endIndex);


                    Log.write("checkIfAllSVsInPendingStatus:svStatus value in query response : "+svStatus);

                    startIndex = soapSyncResponse.lastIndexOf("NewSP value=");
                    startIndex = startIndex + 13;
                    endIndex = startIndex + 4;

                    NNSPValueInSV = soapSyncResponse.substring(startIndex,endIndex);

                    Log.write("checkIfAllSVsInPendingStatus:NNSP value in query response : "+NNSPValueInSV);

                    if(!svStatus.equals("pending")) {
                        Log.write("checkIfAllSVsInPendingStatus:non-pending status SV found for reqNo/reqVer : "+reqNo+"/"+reqVer);
                        return false;
                    }
                }
            } catch (Exception e)     {
                Log.write("checkIfAllSVsInPendingStatus: Exception caught for reqNo:"+reqNo+"/reqVer:"+reqVer+":"+e.getMessage());
                e.printStackTrace();
                return false;
            }
        }//end for loop
        
        return isSVInPendingStatus;
    }

    /**
    * @param camsAddress
    * @param euAddress
    * @param strAddrsMap
    * @return boolean - if both the street address matched: return true, else false.
    */
    private boolean srvAddrsStrtTypValidation(String camsAddress, String euAddress, Map<String, List<String>> strAddrsMap) {
     Log.write("** srvAddrsStrtTypValidation()");
     String tempCamsAddrsArr[] = camsAddress.trim().split(" ");
     Set<String> dataCombinations = new HashSet<String>();
     Set<String> tmpDataCombinations = new HashSet<String>();
     boolean flag = false;

     dataCombinations.add(camsAddress.toUpperCase());

      for (int addIndex = 0; addIndex < tempCamsAddrsArr.length; addIndex++) {
            List<String> strAddrsList = strAddrsMap.get(tempCamsAddrsArr[addIndex].toUpperCase());
            if (strAddrsList != null) {
                  for (String comb : dataCombinations) {
                         for (String str : strAddrsList) {
                        	 tmpDataCombinations.add(comb.replaceAll(tempCamsAddrsArr[addIndex].toUpperCase(), str));
                         }
                  }
                  dataCombinations.addAll(tmpDataCombinations);
                  tmpDataCombinations.clear();
            }

     }

     for (String comb : dataCombinations) {

            Log.write(Log.DEBUG_VERBOSE,"LSRBaseValidator ** before removing the special characters updAddrs:" + comb);
            String strAddrsType = RemoveSpecial(comb);
            Log.write(Log.DEBUG_VERBOSE, "LSRBaseValidator service address compare euAddress: " + euAddress + " strAdds: " + strAddrsType);
            if (euAddress != null && euAddress.equalsIgnoreCase(strAddrsType)) {
                  Log.write(Log.DEBUG_VERBOSE, "LSRBaseValidator common service address street suffix matched");
                  flag = true;
                  break;
            }

     }
     
     Log.write("LSRBaseValidator common service address street suffix match flag: " + flag);
     return flag;
    }
    
    //Check TraitName is Resold
    public boolean checkTraitNameisResold(){
  	  Ctrt ctrtvalue[] = validationData.getCustTraitList(); 
  	   if (ctrtvalue != null) {
  	            for (int i = 0; i < ctrtvalue.length; i++) {
  	                Ctrt ctrt = ctrtvalue[i];

  	                TnTrait traitTn[] = ctrt.getTnTrait();

  	                for (int j = 0; j < traitTn.length; j++) {
  	                    TnTrait traitvalue = traitTn[j];
  	                    String trName = traitvalue.getTraitName();
  	                    if (trName.trim().equalsIgnoreCase("RESOLD-LSP-ID")) {
                              return true;
                          }
  	               }			
                    }
                }
		return false;
        }
}
