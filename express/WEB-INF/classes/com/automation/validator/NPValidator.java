/*
 * NPValidator.java
 *
 * Created on June 22, 2009, 2:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.automation.validator;

import com.alltel.lsr.common.util.Log;
import com.automation.bean.LSRDataBean;
import com.automation.bean.ValidationDataBean;
import com.automation.bean.VendorTableDataBean;

import java.util.ArrayList;

/**
 *
 * @author kumar.k
 */
public class NPValidator extends LSRBaseValidator {

    private VendorTableDataBean vendorBean;
    private ValidationDataBean validationData;
    private LSRDataBean lsrDataBean;

    /** Creates a new instance of NPValidator */
    public NPValidator(LSRDataBean lsrDataBean, VendorTableDataBean vendorBean,
            ValidationDataBean validationData) {
        super(lsrDataBean, vendorBean, validationData);
        this.vendorBean = vendorBean;
        this.validationData = validationData;
        this.lsrDataBean = lsrDataBean;

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
        String atn = lsrDataBean.getAccountTelephoneNo();
        Log.write("LSRBaseValidator isStatus SPilotNumber atn " + atn);
        String pilotNo = validationData.getPilotNo();
        Log.write("LBV  BW isStatus pilotNo " + pilotNo);

        boolean flag = false;
        boolean manflag = false;
        if (actsts != null) {
            actsts = actsts.trim();
            if (actsts.equalsIgnoreCase("active") && atn != null &&
                    pilotNo != null && pilotNo.trim().equals(atn.trim())) {
                //active valid
                flag = true;
            } else if (actsts.equalsIgnoreCase("disconnected")) {
                //Inactive
                flag = false;
            } else if (actsts.equalsIgnoreCase("Invalid")) {
                flag = false;
                manflag = true;
            } else{
                  flag = false;
            }
        }
        //Code Change for avoiding validation with Cams for Resale Account - Saravanan 
        boolean reselFlag = checkTraitNameisResold();
        if(manflag)
        {
            if(reselFlag)
            	fillterSerTypeRejCode("100017-isStatus", true, "NPV", false);//100017
            else
            	fillterSerTypeRejCode("100017-isStatus", flag, "NPV", manflag);//100017      
           
        }
        else
        {
        	if(reselFlag)
        		fillterSerTypeRejCode("10010-isStatus", true, "NPV", manflag);//10010
            else
            	fillterSerTypeRejCode("10010-isStatus", flag, "NPV", manflag);//10010
        	
        }
        
        return flag;
    }

    /*checkREQTYP_LNA method used for checking
     *When REQTYP in LSR form is C and ACT on LSR form is V
     *the LNA on the NP form must be a V
     */
    public boolean checkREQTYP_LNA() {
        ArrayList lnaList = lsrDataBean.getNPLNAList();
        
        boolean flag = false;
        String lna = null;
        int i = 0;
        
        for(i = 0; i < lnaList.size(); i++) {
            lna = (String) lnaList.get(i);
            
            //reset to false
            flag = false;
        
            Log.write("NPValidator LNA=V validation: LNA value : " + lna);
        
            if (lna != null && lna.equals("V")) {
                flag = true;
            }
            
            if(!flag)
                break;
        }
        
        //pass i value here for section number
        super.fillterSerTypeRejCode("10028-checkREQTYP_LNA", flag, "NPV", false);
        return flag;
    }
    
        
    /* matchERL_LACT() method used for checking
     * If ERL on EU form =Y and LACT is Z or blank move to next step.
     * If ERL on EU form =N and LACT on DL form = "O", "I", or "N" send to manual review.
     * If ERL on EU form =N and LACT on DL form "Z" if it is "Z" move to Reject box.
     *
     */
/* Comment per Christie due to rewrite of method matchERLLACT

    public String matchERL_LACT() {
        String erl = lsrDataBean.getEURetainingList();
        String lact = lsrDataBean.getListActivity();
        Log.write("NPValidator checkREQTYP_LNA erl " + erl + " lact" +
                lact);
        String checkstr = "";

        if (erl != null) {
            Log.write("=matchERL_LACT=method:" + erl + " lact " + lact);
            if (erl.equals("Y") && (lact == null || lact.equals("Z"))) {
                checkstr = "Pass";
            } else if (lact != null && erl.equals("N") && (lact.equals("O") || lact.equals("I") || lact.equals("N"))) {

                //Code fix for bug 1400
                //start
                if(!lact.equals("O") && !matchERL_LACT_delete()) {
                    checkstr = "Fail";
                } else {
                    checkstr = "Manual";
                }
                //end
            } else if (lact != null && erl.equals("N") && lact.equals("Z")) {
                checkstr = "Fail";
            } else if (lact != null && erl.equals("Y") && lact.equals("D")) {
                checkstr = "Fail";
            } else {
                checkstr = "Pass";//fix for bug 1400 changed "No" to "Pass"
            }
        } else {
            checkstr = "No";
        }


        return checkstr;

    }

    /* matchERL_LACT_delete() method used for checking
     * If ERL=Y or if ERL is N and LACT is I or N, Reference Directory Column on Vendor Table,
     * If "Y" vendor is eligible to request directory listing.
     *
     */
     /* Comment per Christie due to rewrite of method matchERLLACT

    public boolean matchERL_LACT_delete() {
        String erl = lsrDataBean.getEURetainingList();
        String lact = lsrDataBean.getListActivity();
        String direct = vendorBean.getIsDirectory();
        Log.write("NPValidator matchERL_LACT_delete erl " + erl + " lact" +
                lact + " getIsDirectory" + direct);
        boolean flag = false;
        if (erl != null && lact != null && direct != null) {
            if ((erl.equals("Y") || erl.equals("N")) && (lact.equals("I") || lact.equals("N")) && direct.equals("Y")) {
                flag = true;
            }
        }
        return flag;

    }

    /* matchERL_LACT_N_delete() method used for checking
     * ERL=N & LACT=D then check vendor table
     * for Delete Directory Eligiblity.
     *
     */
     /* Comment per Christie due to rewrite of method matchERLLACT

    public boolean matchERL_LACT_N_delete() {
        boolean flag = false;
        String erl = lsrDataBean.getEURetainingList();
        String lact = lsrDataBean.getListActivity();
        String isdelete = vendorBean.getIsEligibleToDeleteDir();
        Log.write("NPValidator matchERL_LACT_delete erl " + erl + " lact" +
                lact + " isdelete" + isdelete);
        if (erl != null && lact != null && erl.equals("N") &&
                lact.equals("D") && isdelete != null && isdelete.equals("Y")) {
            flag = true;
        }

        return flag;

    }

    public boolean matchERLLACT() {
        Log.write("NPValidator  matchERLLACT  ");
        boolean flag = false;
        boolean manflag = false;

        if (matchERL_LACT_delete()) {
            flag = true;
        } else if (matchERL_LACT_N_delete()) {
            flag = true;
        }
        if (matchERL_LACT().equals("Pass")) {
            flag = true;
        } else if (matchERL_LACT().equals("Manual")) {
            manflag = true;
        }else{
             flag = false;
        }
        Log.write("NPValidator matchERLLACT manflag " + manflag + " flag " + flag);

        fillterSerTypeRejCode("10022-matchERLLACT", flag, "NPV", manflag);
        return flag;
    }
    */
    
    public boolean matchERLLACT() {
        Log.write("NPValidator  matchERLLACT  ");
        String erl = lsrDataBean.getEURetainingList();
        if(erl == null) erl = "";
        String lact = lsrDataBean.getListActivity();
        if(lact == null) lact = "";
        String isdelete = vendorBean.getIsEligibleToDeleteDir();
        if(isdelete == null) isdelete = "";
        String isdirect = vendorBean.getIsDirectory();
        if(isdirect == null) isdirect = "";
        boolean flag = false;
        boolean manflag = false;
        boolean manflagValiderl = false;
        
        if(erl != null && lact != null && isdelete != null && isdirect != null) {
                if (erl.equals("N")) {
                        if((lact.equals("I") || lact.equals("N")) && isdirect.equals("N")){
                                flag = false; // Reject
                        } else if(lact.equals("D") && isdelete.equals("N")){
                                flag = false; // Reject
                        } else if (lact.equals("Z")) {
                                flag = false; //Reject
                        } else if(lact.equals("I") || lact.equals("N") || lact.equals("O")) {
                                manflagValiderl = true; //Manual Review with different message
                        } else {
                                flag = true; // Pass validation
                        }
                } else if (erl.equals("Y")) {
                        if (isdirect.equals("N")) {
                                flag = false; //reject
                        } else if (!lact.equals("Z") && !lact.equals(" ") && !lact.equals("")) {
                                //manflag = true; // Manual Review -- send to Reject as per Theresa -- fix for bug 299
                                flag = false;
                        } else {
                                flag = true; // Pass validation
                        }

                }
        }

        Log.write("NPValidator matchERLLACT manflag " + manflag + " flag " + flag + " manflagValiderl "+manflagValiderl);
	
        if (manflagValiderl){
            fillterSerTypeRejCode("100006-matchERLLACT", flag, "NPV", manflagValiderl); //MR for "ERL is N and LACT is I,O or N. Does not qualify for automation."
	} else {
	    fillterSerTypeRejCode("10022-matchERLLACT", flag, "NPV", manflag);
	}

        return flag;
    }


}
