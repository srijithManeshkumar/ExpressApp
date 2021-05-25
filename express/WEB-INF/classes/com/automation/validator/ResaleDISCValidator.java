/*
 * ResaleDISCValidator.java
 *
 * Created on June 22, 2009, 3:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.automation.validator;

import com.alltel.lsr.common.util.Log;
import com.automation.bean.LSRDataBean;
import com.automation.bean.ValidationDataBean;
import com.automation.bean.VendorTableDataBean;
import com.windstream.winexpcustprof.Ctrt;
import com.windstream.winexpcustprof.TnTrait;

/**
 *
 * @author kumar.k
 */
public class ResaleDISCValidator extends LSRBaseValidator {

    private VendorTableDataBean vendorBean;
    private ValidationDataBean validationData;
    private LSRDataBean lsrDataBean;

    /** Creates a new instance of ResaleDISCValidator */
    public ResaleDISCValidator(LSRDataBean lsrDataBean, VendorTableDataBean vendorBean,
            ValidationDataBean validationData) {
        super(lsrDataBean, vendorBean, validationData);
        this.vendorBean = vendorBean;
        this.validationData = validationData;
        this.lsrDataBean = lsrDataBean;

    }

    /*matchOCN_Trait_CC method used for checking
     * 2 V 2nd Validation Point:
     * CC on LSR matches OCN trait Name RESOLD-LSP-ID and OCN Trait
     * CC Value with value of 1234, must
     * match CAMS RESOLD-LSP-ID with value of 1234
     *
     */
    
    public boolean matchOCN_Trait_CC() {
        boolean flag = false;
        boolean upflag = false;
        String cmpnCode = lsrDataBean.getCompanyCode();
        Ctrt ctrtvalue[] = validationData.getCustTraitList();
        Log.write("ResaleDISCValidator matchOCN_Trait_CC cmpnCode " + cmpnCode + " ctrtvalue" +
                ctrtvalue);
        if (ctrtvalue != null) {
            for (int i = 0; i < ctrtvalue.length; i++) {
                Ctrt ctrt = ctrtvalue[i];
                TnTrait traitTn[] = ctrt.getTnTrait();
                for (int j = 0; j < traitTn.length; j++) {
                    TnTrait traitvalue = traitTn[j];
                    String trName = traitvalue.getTraitName();
                    String trValue = traitvalue.getTraitValue();
                    Log.write("ResaleDISCValidator matchOCN_Trait_CC trValue " + trValue + " trName " + trName);
                    if (trName != null && trValue != null && cmpnCode != null) {
                        if ((trName.trim().equalsIgnoreCase("RESOLD-LSP-ID")) && (trValue.trim().equalsIgnoreCase(cmpnCode))) {
                            flag = true;
                            break;
                        } else if ((trName.trim().equalsIgnoreCase("UNEP-ID")) && (trValue.trim().equalsIgnoreCase(cmpnCode))) {
                            upflag = true;
                        }
                    }
                }
            }
        } else {
            Log.write("ResaleDISCValidator matchOCN_Trait_CC it will not return any value");
            return flag;
        }
        Log.write("RED matchOCN_Trait_CC flag " + flag + " upflag " + upflag);
        if (flag) {
            fillterSerTypeRejCode("40003-matchOCN_Trait_CC :BW", flag, "RED", false);
        } else if (upflag) {
            fillterSerTypeRejCode("40023-matchOCN_Trait_CC :BW", flag, "RED", false);
        } else {
            fillterSerTypeRejCode("40003-matchOCN_Trait_CC :BW", flag, "RED", false);
        }
        return flag;
    }

    /*checkREQTYP_RSform method used for checking
     *This method we need check later
     *Check if RS form is prohibited (means the user should not be able
     *to enter any value which means this field should be blank ?) when the REQTYP on
     *the LSR form is E and ACT on the LSR form is D.
     *(RS form is NOT prohibited when the REQTYP is E and the ACT is D)
     */
    
    public boolean checkREQTYP_RSform() {
        String reqtype = lsrDataBean.getSerRequestType();
        String act = lsrDataBean.getActivity();
        String lnaRS = lsrDataBean.getLineActivityResale();
        Log.write("ResaleDISCValidator checkREQTYP_RSform reqtype " + reqtype + " act" +
                act + " lnaRS " + lnaRS);
        boolean flag = false;
        if (reqtype != null && act != null && lnaRS != null &&
                reqtype.equals("E") && act.equals("ED")) {
            flag = true;
        }
        return flag;
    }

    /*checkREQTYP_LNA_RS method used for checking
     *Check if LNA value on the RS form must be a D
     *when the REQTYP on the LSR form is E and ACT on the LSR form is D
     */
    
    public boolean checkREQTYP_LNA_RS() {
        String reqtype = lsrDataBean.getSerRequestType();
        String act = lsrDataBean.getActivity();
        String lnaRS = lsrDataBean.getLineActivityResale();
        Log.write("ResaleDISCValidator checkREQTYP_LNA_RS reqtype " + reqtype + " act" +
                act + " lnaRS " + lnaRS);
        boolean flag = false;
        if (reqtype != null && act != null && lnaRS != null &&
                reqtype.equals("E") && act.equals("D") && lnaRS.equals("D")) {

            flag = true;
        }
        super.fillterSerTypeRejCode("40020-checkREQTYP_LNA_RS", flag, "RED", false);
        return flag;
    }

    /* checkREQTYP_LACT_DL() method used for checking
     * LACT on the DL form is "D" or Blank
     */
    
    public boolean checkREQTYP_LACT_DL() {

        String lact = lsrDataBean.getListActivity();
        Log.write("ResaleDISCValidator checkREQTYP_LACT_DL lact " + lact);
        boolean flag = false;
        if (lact == null || lact.equals("D")) {
            flag = true;
        }
        super.fillterSerTypeRejCode("40022-checkREQTYP_LACT_DL", flag, "RED", false);
        return flag;
    }

    /* checkReqTypeRes method used for checking
     * 3i PQ
     * Check if value in RTR field on LSR form is C
     *
     */
    public boolean checkReqTypeRes() {
        String rtr = lsrDataBean.getResTypeReq();
        Log.write("ResaleDISCValidator checkReqTypeRes rtr " + rtr);
        boolean flag = false;
        if (rtr != null && rtr.equals("C")) {
            flag = true;
        }
        super.fillterSerTypeRejCode("40011-checkReqTypeRes", flag, "RED", false);
        return flag;
    }
}
