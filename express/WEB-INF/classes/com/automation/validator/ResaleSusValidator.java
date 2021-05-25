/*
 * ResaleSusValidator.java
 *
 * Created on June 24, 2009, 12:41 PM
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
public class ResaleSusValidator extends LSRBaseValidator {

    private VendorTableDataBean vendorBean;
    private ValidationDataBean validationData;
    private LSRDataBean lsrDataBean;

    /** Creates a new instance of ResaleSusValidator */
    public ResaleSusValidator(LSRDataBean lsrDataBean, VendorTableDataBean vendorBean,
            ValidationDataBean validationData) {
        super(lsrDataBean, vendorBean, validationData);
        this.vendorBean = vendorBean;
        this.validationData = validationData;
        this.lsrDataBean = lsrDataBean;

    }

    /* matchOCN_Trait_CC method used for checking
     * 2nd Validation Point:
     *  CC on LSR form matches OCN trait name RESOLD-LSP-ID or UNEP-LSP-ID
     *  and OCN trait value and corresponds with each TN on the LSR
     *
     */
    public boolean matchOCN_Trait_CC() {
        boolean flag = false;
        boolean invServTypeflag = false;
        String cmpnCode = lsrDataBean.getCompanyCode();
        String serviceType = lsrDataBean.getSerRequestType();
        String activityType = lsrDataBean.getActivity();
        
        Ctrt ctrtvalue[] = validationData.getCustTraitList();
        Log.write("ResaleSusValidator matchOCN_Trait_CC cmpnCode " + cmpnCode + " ctrtvalue" + ctrtvalue);


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
                        if ((serviceType.trim().equalsIgnoreCase("E")) && 
                            (trName.trim().equalsIgnoreCase("RESOLD-LSP-ID")) && 
                            (trValue.trim().equalsIgnoreCase(cmpnCode))) {
                            flag = true;
                            break;
                        } else if ((serviceType.trim().equalsIgnoreCase("M")) && 
                            (trName.trim().equalsIgnoreCase("UNEP-ID")) && 
                            (trValue.trim().equalsIgnoreCase(cmpnCode))) {
                            flag = true;
                            break;
                        } else if ((serviceType.trim().equalsIgnoreCase("E")) && 
                                   (trName.trim().equalsIgnoreCase("UNEP-ID")) && 
                                   (trValue.trim().equalsIgnoreCase(cmpnCode))) {
                            invServTypeflag = true;
                        } else if ((serviceType.trim().equalsIgnoreCase("M")) && 
                                   (trName.trim().equalsIgnoreCase("RESOLD-LSP-ID")) && 
                                   (trValue.trim().equalsIgnoreCase(cmpnCode))) {
                            invServTypeflag = true;
                        }
                    }
                }
            }
        } else {
            return flag;
        }
        Log.write("UNEPS/RES matchOCN_Trait_CC flag " + flag + " upflag " + invServTypeflag);
        if (flag) {
            fillterSerTypeRejCode("80003-matchOCN_Trait_CC :BW", flag, "UNEPS", false);
            fillterSerTypeRejCode("60003-matchOCN_Trait_CC :BW", flag, "RES", false);
        } else if (invServTypeflag) {
            fillterSerTypeRejCode("80024-matchOCN_Trait_CC :BW", flag, "UNEPS", false);
            fillterSerTypeRejCode("60024-matchOCN_Trait_CC :BW", flag, "RES", false);
        } else {
            fillterSerTypeRejCode("80003-matchOCN_Trait_CC :BW", flag, "UNEPS", false);
            fillterSerTypeRejCode("60003-matchOCN_Trait_CC :BW", flag, "RES", false);
        }

        return flag;
    }

    /* checkREQTYP_LNA_RS method used for checking
     *Check if LNA value on the RS form must be a S when the
     *SRVTYP on the LSR form is E and ACT on the LSR form is S
     */
    public boolean checkREQTYP_LNA_RS() {
        String servType = lsrDataBean.getSerRequestType();
        String act = lsrDataBean.getActivity();
        String lnaRS = lsrDataBean.getLineActivityResale();
        String lnaPS = lsrDataBean.getLineActivityPSUNEP();
        
        Log.write("ResaleSusValidator checkREQTYP_LNA_RS reqtype " + servType + " act" + act +
                "lnaRS" + lnaRS);
        boolean flag = false;
        if (servType != null && act != null && lnaRS != null &&
                servType.equals("E") && act.equals("S") && lnaRS.equals("S")) {
            flag = true;
        } else if (servType != null && act != null && lnaPS != null &&
                servType.equals("M") && act.equals("S") && lnaPS.equals("S")) {
            flag = true;
        }
                
        super.fillterSerTypeRejCode("80022-checkREQTYP_LNA_RS", flag, "UNEPS", false);
        super.fillterSerTypeRejCode("60021-checkREQTYP_LNA_RS", flag, "RES", false);
        return flag;
    }
}
