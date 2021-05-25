/*
 * UNEPConverValidator.java
 *
 * Created on June 22, 2009, 3:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.automation.validator;

import com.alltel.lsr.common.util.Log;

import com.automation.bean.AddressBean;
import com.automation.bean.LSRDataBean;
import com.automation.bean.ValidationDataBean;
import com.automation.bean.VendorTableDataBean;
import com.automation.dao.LSRdao;

import com.windstream.winexpcustprof.Ctrt;
import com.windstream.winexpcustprof.TnTrait;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author kumar.k
 */
public class UNEPConverValidator extends LSRBaseValidator {

    private VendorTableDataBean vendorBean;
    private ValidationDataBean validationData;
    private LSRDataBean lsrDataBean;

    /** Creates a new instance of UNEPConverValidator */
    public UNEPConverValidator(LSRDataBean lsrDataBean, VendorTableDataBean vendorBean,
            ValidationDataBean validationData) {
        super(lsrDataBean, vendorBean, validationData);
        this.vendorBean = vendorBean;
        this.validationData = validationData;
        this.lsrDataBean = lsrDataBean;

    }

    /*checkREQTYP_LNA_PS method used for checking
     *When REQTYP (in LSR form?) is M and ACT (on LSR form) is V
     *the LNA on the LS and PS forms must be a V
     *cahnged as well as BF
     *
     */
    public boolean checkREQTYP_LNA_PS() {

        String lnaPS = lsrDataBean.getLineActivityPSUNEP();
        Log.write("UNEPConverValidator checkREQTYP_LNA_PS lnaPS " + lnaPS);
        boolean flag = false;
        if (lnaPS != null && lnaPS.equals("V")) {

            flag = true;
        }
        super.fillterSerTypeRejCode("70026-checkREQTYP_LNA_PS", flag, "UNEPV", false);
        return flag;
    }

    /*
     *checkREQTYP_LNA_LS method used for checking
     * LNA on the LS form is "V"
     */
    public boolean checkREQTYP_LNA_LS() {

        String lnaLS = lsrDataBean.getLineActivityLSUNEP();
        Log.write("UNEPConverValidator checkREQTYP_LNA_LS lnaLS " + lnaLS);
        boolean flag = false;
        if (lnaLS != null && lnaLS.equals("V")) {

            flag = true;
        }
        super.fillterSerTypeRejCode("70029-checkREQTYP_LNA_LS", flag, "UNEPV", false);
        return flag;
    }

    /*checkREQTYP_LACT_DL method used for checking
     *When REQTYP (in LSR form?) is M and ACT (on LSR form) is V
     *the LACT field on the DL form must not be a D
     */
    public boolean checkREQTYP_LACT_DL() {
        String reqtype = lsrDataBean.getSerRequestType();
        String act = lsrDataBean.getActivity();
        String lact = lsrDataBean.getListActivity();
        Log.write("UNEPConverValidator checkREQTYP_LACT_DL reqtype " + reqtype + " act" + act + " lact " + lact);
        boolean flag = false;
        if (reqtype != null && act != null && lact != null &&
                reqtype.equals("M") && act.equals("V") && !lact.equals("D")) {
            flag = true;
        }
        super.fillterSerTypeRejCode("70030-checkREQTYP_LACT_DL", flag, "UNEPV", false);
        return flag;
    }

    /*
     *V 11 Buisness Flow ,checkEUMI() method used for checking
     *if E UMI field is populated "Y"
     *
     */
    public boolean checkEUMI() {
        String eumi = lsrDataBean.getEumi();
        Log.write("UNEPConverValidator checkEUMI eumi " + eumi);
        boolean flag = false;
        boolean manflag = false;
        if (eumi != null && eumi.equals("Y")) {
            flag = false;
            manflag = true;
        } else {
            flag = true;
            manflag = false;
        }
        super.fillterSerTypeRejCode("100029-checkEUMI", flag, "UNEPV", manflag);
        return flag;

    }

    /*checkReqTypeRes method used for checking
     *V 23 Buisness Flow
     * Check if value in RTR field on LSR form is C
     *
     */
    public boolean checkReqTypeRes() {
        String rtr = lsrDataBean.getResTypeReq();
        Log.write("UNEPConverValidator checkReqTypeRes rtr " + rtr);
        boolean flag = false;
        if (rtr != null && rtr.equals("C")) {
            flag = true;
        }
        super.fillterSerTypeRejCode("70016-checkReqTypeRes", flag, "UNEPV", false);
        return flag;
    }

    /*
     *V 31 Buisness Flow ,matchERL_LACT() method used for checking
     *ERL on EU form  =Y and LACT on DL form is Z or blank move to next step.
     *If ERL on EU form ="N" and LACT on DL form = "O", "I", "N" or is Blank send for manual review
     * If ERL on eU Form is "n", LACT on DL form "Z".  If it is move to reject box
     */
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
                checkstr = "Manual";
            } else if (lact != null && erl.equals("N") && lact.equals("Z")) {
                checkstr = "Fail";
            } else {
                checkstr = "No";
            }
        } else {
            checkstr = "No";
        }
        return checkstr;
    }

    /*
     *V 32 Buisness Flow ,matchERL_LACT_N_delete() method used for checking
     *If ERL on EU form=N and LACT=D, check Vendor Table to see
     *if eligible to delete directory, if yes proceed to next step
     */
    public boolean matchERL_LACT_N_delete() {
        boolean flag = false;
        String erl = lsrDataBean.getEURetainingList();
        String lact = lsrDataBean.getListActivity();
        String isdelete = vendorBean.getIsEligibleToDeleteDir();
        Log.write("UNEPConverValidator matchERL_LACT_N_delete erl " + erl + " lact " + lact + "isdelete" + isdelete);
        if (erl != null && lact != null && erl.equals("N") &&
                lact.equals("D") && isdelete != null && isdelete.equals("Y")) {
            flag = true;
        }
        return flag;

    }

    /*
     * matchERLLACT method used combined two methods of
     * matchERL_LACTmatch and ERL_LACT_N_delete
     */
    public boolean matchERLLACT() {
        Log.write("UNEPConverValidator  matchERLLACT  ");
        boolean flag = false;
        boolean manflag = false;

        if (matchERL_LACT_N_delete()) {
            flag = true;
        } else if (matchERL_LACT().equals("Pass")) {
            flag = true;
        } else if (matchERL_LACT().equals("Manual")) {
            manflag = true;
        }
        Log.write("UNEPConverValidator matchERLLACT manflag " + manflag + " flag " + flag);

        if (manflag)
            fillterSerTypeRejCode("100025-matchERLLACT", flag, "UNEPV", manflag);
        else
            fillterSerTypeRejCode("70024-matchERLLACT", flag, "UNEPV", manflag);
        
        return flag;
    }

    /*
     * CHANGE CONTROL MSAG Valid Address. If address values
     * don't match MSAG valid address - Manual Review
     * If County, City (community), or HSNBR (SANO) field is blank then reject
     *
     */
    
    public boolean matchEU_Bill_Address() {
        boolean flag = false;
        boolean msagValid = false;
        Map mapAddr = lsrDataBean.getAddressMap();
        AddressBean addBean = (AddressBean) mapAddr.get("EU_LA");
        
        if (addBean != null) {
            String sano = addBean.getSano();
            String county = addBean.getCounty();
            String city = addBean.getCity();
            String street = addBean.getMsagStreet();
            String state = addBean.getState();
            String preDir = addBean.getMsagPreDir();
        
            if (sano != null && county != null && city != null) {
                flag = true;
            }
                                
            if(flag) {
                street = street.replaceAll(sano.trim(),"").trim();
                
                try {
                    LSRdao lsrDao = new LSRdao();

                    Log.write("Calling LSRdao.validateMSAG() method...State: "+state);
                    Log.write("Calling LSRdao.validateMSAG() method...county: "+county);
                    Log.write("Calling LSRdao.validateMSAG() method...City: "+city);
                    Log.write("Calling LSRdao.validateMSAG() method...SANO: "+sano);
                    Log.write("Calling LSRdao.validateMSAG() method...Street: "+street);
                    
                    HashMap msagResult = lsrDao.validateMSAG(state,county,city,sano,street,preDir);
                    Log.write("Value returned by SLA Procedure in SLATimer from validateMSAG:"+(String)msagResult.get("p_valid"));
                    String isMSAGValid = (String)msagResult.get("p_valid");
                    Log.write("Value returned by SLA Procedure in SLATimer from validateMSAG:"+(String)msagResult.get("p_fieldInError"));
                    Log.write("Value returned by SLA Procedure in SLATimer from validateMSAG:"+(String)msagResult.get("p_errorCode"));
                    Log.write("Value returned by SLA Procedure in SLATimer from validateMSAG:"+(String)msagResult.get("p_errorInfo"));
                    
                    if(isMSAGValid.trim().equals("TRUE"))
                        msagValid = true;
                    
                } catch(Exception e) {
                    Log.write("Exception while calling MSAG Validation stored proc: "+e.getMessage());
                    e.printStackTrace();
                }
                
            }
        }
        
        if(flag && msagValid) {
            fillterSerTypeRejCode("70019-matchEU_Bill_Address:UNEPV ", flag, "UNEPV", false);
            //adding rejection code and for pass case here - 100003
            fillterSerTypeRejCode("100003-validateMSAG:UNEPV ", msagValid, "UNEPV", false);
        } else if (!flag)
            fillterSerTypeRejCode("70019-matchEU_Bill_Address:UNEPV ", flag, "UNEPV", false);
        else if (!msagValid )
            fillterSerTypeRejCode("100003-validateMSAG:UNEPV ", flag, "UNEPV", true);
            //added new MSAG fail rej code seq no here
        return msagValid;
    }
    
    public boolean checkUNEPAsocList() {
        boolean flag = false;
        boolean manflag = false;
        List ascoUPList = lsrDataBean.getAsocUNPList();
        Log.write("UPC checkUNEPAsocList ascoUPList-- " + ascoUPList);
        List ascoUPIWList = lsrDataBean.getAsocSUnepIWList();
        Log.write("UPC checkUNEPAsocList ascoUPIWList-- " + ascoUPIWList);
        List ascoPSList = lsrDataBean.getAsocListPS();
        Log.write("UPC checkUNEPAsocList ascoPSList " + ascoPSList);
        if (ascoPSList.size() > 0) {
            for (int i = 0; i < ascoPSList.size(); i++) {
                String asoc = (String) ascoPSList.get(i);
                Log.write("UPC checkAsocList asoc " + asoc);
                if (asoc != null) {
                    asoc = asoc.trim().toUpperCase();
                }
                boolean asocFlag = ascoUPIWList.contains(asoc);
                Log.write("UPC IW checkAsocList asocFlag " + asocFlag);
                if (asocFlag) {
		    manflag = true; // Manual Review Inside Work Required
                }
                asocFlag = ascoUPList.contains(asoc);
                Log.write("UPC checkAsocList asocFlag " + asocFlag);
                if (asocFlag) {
                    flag = true;
                } else {
                    flag = false;
                    setUnepAsocValue((i + 1) + " (" + asoc + ") ");
                    break;
                }
            }
        } else {
            flag = true;
        }
	if (!flag){
            super.fillterSerTypeRejCode("70028-checkUNEPAsocList", flag, "UNEPV", false);
	} else if(manflag) {
            super.fillterSerTypeRejCode("100032-checkUNEPAsocList", true, "UNEPV", manflag);
        }
        return flag;
    }

    /*isExitOCN_Trait_CAMS method used for checking
     *19 V
     *OCN Trait exists in CAMS
     *6) If no LSP ID (RESOLD, UNEP, Wireless, & ICLEC)
     *is on PON then validate LSR AN = ACCT# matches CAMs ID RQ005
     *17)Pass Code is a match  9 characters -
     *Don't validate if RESOLD. Wireless, ECLEC, or UNEP LSP ID trait exists RQ005
     *27) If Resold , ICLEC, Wireless, and UNEP-LSP-ID exist must go to manual review
     */
    public boolean isExitOCN_Trait_CAMS() {
        boolean flag = false;
        boolean manflag = false;
        Ctrt ctrtvalue[] = validationData.getCustTraitList();
        String an = lsrDataBean.getAccountNo();
        String camsID = validationData.getCamsId();
        String traitReUn = null;
        Log.write("LSRBaseValidator isExitOCN_Trait_CAMS ctrtvalue-- " + ctrtvalue + ": an " + an + ": camsID " + camsID);
        boolean re_un_flag = false;
        boolean ic_wi_flag = false;
        if (ctrtvalue != null) {
            for (int i = 0; i < ctrtvalue.length; i++) {
                Ctrt ctrt = ctrtvalue[i];

                TnTrait traitTn[] = ctrt.getTnTrait();

                for (int j = 0; j < traitTn.length; j++) {
                    TnTrait traitvalue = traitTn[j];
                    String trName = traitvalue.getTraitName();
                    String trValue = traitvalue.getTraitValue();

                    Log.write("LBV  BW isExitOCN_Trait_CAMS trName " +
                            trName + " trValue " + trValue);
                    if (trValue != null) {
                        if ((trName.trim().equalsIgnoreCase("RESOLD-LSP-ID") ||
                                trName.trim().equalsIgnoreCase("UNEP-ID")) ||
                                trName.trim().equalsIgnoreCase("ICLEC-LSP-ID") ||
                                trName.trim().equalsIgnoreCase("WIRELESS-LSP-ID")) {
                            traitReUn = trName.trim();
                            re_un_flag = true;
                        }
                    }
                }
            }

            if (re_un_flag) {
                //manual review
                manflag = true;

            } else {
                //sucess: calling two methods
                flag = true;
                matchPasscode();
                matchEAN_AN();
                matchAN_CAMSID();
            }
        } else {
            return flag;
        }
        Log.write("LBV  BW isExitOCN traitReUn " + traitReUn);
        if (manflag && traitReUn != null && traitReUn.equalsIgnoreCase("UNEP-ID")) {
            fillterSerTypeRejCode("90013-isExitOCN_Trait_CAMS", flag, "UNEPV", manflag);
        } else if (manflag && traitReUn != null && traitReUn.equalsIgnoreCase("RESOLD-LSP-ID")) {
            fillterSerTypeRejCode("90013-isExitOCN_Trait_CAMS", flag, "UNEPV", manflag);
        } else if (manflag && traitReUn != null && traitReUn.equalsIgnoreCase("ICLEC-LSP-ID")) {
            fillterSerTypeRejCode("90013-isExitOCN_Trait_CAMS", flag, "UNEPV", manflag);
        } else if (manflag && traitReUn != null && traitReUn.equalsIgnoreCase("WIRELESS-LSP-ID")) {
            fillterSerTypeRejCode("90013-isExitOCN_Trait_CAMS", flag, "UNEPV", manflag);
        } else {
            fillterSerTypeRejCode("70010-isExitOCN_Trait_CAMS", flag, "UNEPV", manflag);
        }
        if (manflag) {
            return manflag;
        }
        return flag;

    }
}
