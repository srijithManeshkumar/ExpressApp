/*
 * DirAssDirDISCValidator.java
 *
 * Created on June 22, 2009, 3:04 PM
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
import com.windstream.winexpcustprof.AddLn;
import com.windstream.winexpcustprof.Addr;
import com.windstream.winexpcustprof.Ctrt;
import com.windstream.winexpcustprof.TnTrait;
import java.util.Map;

/**
 *
 * @author kumar.k
 */
public class DirAssDirDISCValidator extends LSRBaseValidator {

    private VendorTableDataBean vendorBean;
    private ValidationDataBean validationData;
    private LSRDataBean lsrDataBean;

    /** Creates a new instance of DirAssDirDISCValidator */
    public DirAssDirDISCValidator(LSRDataBean lsrDataBean, VendorTableDataBean vendorBean,
            ValidationDataBean validationData) {
        super(lsrDataBean, vendorBean, validationData);
        this.vendorBean = vendorBean;
        this.validationData = validationData;
        this.lsrDataBean = lsrDataBean;

    }

    /* isWindstreamNativeNumber method used for checking(LERG DB)
     * 9d V TN is not Windstream Native number
     */
    public boolean isWindstreamNativeNumber() {
        // lerg db input
        int isWinNativeflag = lsrDataBean.isNativeNumberFlag();
        
        boolean flag = false;
        
        if(isWinNativeflag == 0)
            flag = false;
        else if(isWinNativeflag == 1)
            flag = true;
        
        Log.write("Value of Native flag returned: "+isWinNativeflag);
        
        if(isWinNativeflag == 0 || isWinNativeflag == 1) {
            Log.write("LSRBaseValidator isWinNative flag: " + flag);
        
            //manual review
            Log.write("DirAssDirDISCValidator isWindstreamNativeNumber flag " + flag);
            super.fillterSerTypeRejCode("90003-isWindstreamNativeNumber", !flag, "DAD", flag);
            
        } else if(isWinNativeflag == 2) {
            flag = true;
            Log.write("LSRBaseValidator isWinNative flag: Exception thrown while connecting to LERG. Sending to MR.");
            //manual review
            Log.write("DirAssDirDISCValidator isWindstreamNativeNumber flag " + flag);
            super.fillterSerTypeRejCode("100010-isWindstreamNativeNumber", !flag, "DAD", flag);//create new MR and add seq no
         
        }
        
        return !flag;
    }
    /* isRecordType method used for checking
     * V RTY is not an F
     */

    public boolean isRecordType() {

        String rty = lsrDataBean.getRecordType();
        Log.write("DirAssDirDISCValidator isRecordType rty " + rty);
        boolean flag = false;
        if (rty == null || !rty.equals("F")) {
            flag = true;
        }
        super.fillterSerTypeRejCode("90004-isRecordType", flag, "DAD", !flag);
        return flag;
    }

  
    /* matchNameAddressCams method used for checking
     * Name and address in Service Section of
     * DSR form  match name and address in CAMS
     */
    public boolean matchNameAddressCams() {
        boolean flag = false;
        boolean finalflag = false;
        boolean billflag = false;
        String dsrname = lsrDataBean.getDsrName();
        String camsName = validationData.getCustName();
        String billName = lsrDataBean.getBillname();
        
        //fix for bug 298
        if(dsrname != null)
            dsrname = dsrname.replaceAll(" ","");
        if(billName != null)
            billName = billName.replaceAll(" ","");
        if(camsName != null)
            camsName = camsName.replaceAll(" ","");
        
        Map mapAddress = lsrDataBean.getAddressMap();
        Addr camsAddress = validationData.getCustAddress();
        AddressBean addbean = (AddressBean) mapAddress.get("DSR");
        Log.write("DirAssDirDISCValidator matchNameAddressCams dsrname " + dsrname + " camsName " + camsName + "  addbean " + addbean + "  billName " + billName);

        String euAddress = lsrDataBean.getEuAddressTrim();

        Log.write("DAD matchEU_Bill_Address euAddress " + euAddress + " camsAddress " + camsAddress);
        
        //fix for PI issue no 47
        /*
        if (billName != null && camsName != null) {
            if (billName.trim().equalsIgnoreCase(camsName.trim())) {
                billflag = true;
            } else {
                super.fillterSerTypeRejCode("30018-matchNameAddressCams :BW", finalflag, "DAD", false);
                return flag;
            }
        } else if (billName == null) {
            billflag = true;
        }
         */
        
        //fix to separate bill name and address validation - Antony - 06/24/2010
        //billflag = true;
        //fix for issue# 54
        
        if (camsName != null && dsrname != null) {
            if (camsName.trim().equalsIgnoreCase(dsrname.trim())) {
                billflag = true;
            } else {
                Log.write("CAMS Name and DSR Service Section Name fields don't match !!!");
                Log.write("CAMS Name :"+camsName+" and Service Section Name :"+dsrname);
                
                super.fillterSerTypeRejCode("30016-matchNameAddressCams :BW", false, "DAD", false);
                //fix to separate bill name and address validation - Antony - 06/24/2010
                //return false;
            }
        }
        
        Log.write("DAD matchEU_Bill_Address billflag " + billflag);
        
        String catStr = "";
        if (camsAddress != null && euAddress != null) {
                AddLn addrLine[] = camsAddress.getAddrLine();
                if (addrLine != null) {
                    for (int i = 0; i < addrLine.length; i++) {
                        AddLn addrLineValue = addrLine[i];
                        Log.write("DAD matchNameAddressCams BW addrLineValue " + addrLineValue);
                        if (addrLineValue != null) {
                            String strAdds = addrLineValue.getAddrLn();
                            Log.write("DAD BW strAdds" + strAdds);
                            Log.write("DAD BW catStr " + catStr);
                            if (strAdds != null) {
                                if (i < 2) {
                                    catStr = catStr + strAdds.replaceAll(" ", "");
                                }
                                
                                //code inserted to fix bug 1450
                                //start
                                euAddress = euAddress.replaceAll(";","");
                                strAdds = strAdds.replaceAll(";","");
                                catStr = catStr.replaceAll(";","");
                                //end
                                
                                if (euAddress.equalsIgnoreCase(strAdds.replaceAll(" ", ""))) {
                                    flag = true;
                                    break;
                                } else if (euAddress.equalsIgnoreCase(catStr)) {
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }
            } 
        }


        if (camsAddress != null && addbean != null) {
            String city = camsAddress.getCity();
            String state = camsAddress.getState();
            String zip = camsAddress.getZipCd();
            if (zip!=null && zip.length() > 5) {
                String zips[] = zip.split("[-]");
                Log.write("DAD matchEU_Bill_Address zips" + zips);
                zip = zips[0];
                Log.write("DAD matchEU_Bill_Address zip " + zip);
            }
            String citylsr = addbean.getCity();
            String statelsr = addbean.getState();
            String ziplsr = addbean.getZip();
            Log.write("DAD matchNameAddressCams flag  " + flag + " city " + city + " citylsr " + citylsr + " state " + state + " zip " + zip + " statelsr" + statelsr + " ziplsr " + ziplsr);

            if (flag && city != null && citylsr != null && city.trim().equalsIgnoreCase(citylsr.trim()) &&
                state != null && statelsr != null && state.trim().equalsIgnoreCase(statelsr.trim()) &&
                zip != null && ziplsr != null && zip.trim().equalsIgnoreCase(ziplsr.trim())) {
                finalflag = true;
            }
        }

        super.fillterSerTypeRejCode("30017-matchNameAddressCams :BW", finalflag, "DAD", false);
        return flag;
    }

    /* notEmptyDADT() method used for checking
     * DADT field is blank
     *
     */
    public boolean notEmptyDADT() {
        boolean flag = false;
        String dadt = lsrDataBean.getDadt();
        Log.write("DirAssDirDISCValidator notEmptyDADT dadt " + dadt);
        if (dadt == null || dadt.trim().length() == 0) {
            flag = true;
        }
        super.fillterSerTypeRejCode("30015-notEmptyDADT", flag, "DAD", false);
        return flag;
    }

    /*
     *checkisEXPNotY method used for checking
     * Expedite field does not contain a "Y"
     */
    public boolean checkisEXPNotY() {

        String exp = lsrDataBean.getEXPedite();
        Log.write("DirAssDirDISCValidator checkisEXPNotY exp " + exp);
        boolean flag = false;
        if (exp == null || !exp.equalsIgnoreCase("Y")) {
            flag = true;
        }

        fillterSerTypeRejCode("30012-checkisEXPNotY", flag, "DAD", false);
        return flag;
    }

    /* checkREQTYP_LACT_DL() method used for checking
     *    LACT field on DL form = "D"
     *
     */
    public boolean checkREQTYP_LACT_DL() {

        String lact = lsrDataBean.getListActivity();
        Log.write("DirAssDirDISCValidator checkREQTYP_LACT_DL lact " + lact);
        boolean flag = false;
        if (lact != null && lact.equals("D")) {
            flag = true;
        }
        super.fillterSerTypeRejCode("30019-checkREQTYP_LACT_DL", flag, "DAD", false);
        return flag;
    }

    /*matchOCN_Trait_CC method used for checking
     *CC on DSR matches OCN trait name ICLEC-LSP-ID in CAMS
     *Move to Reject Box without further validation
     */
    public boolean matchOCN_Trait_CC() {
        boolean flag = false;

        String cmpnCode = lsrDataBean.getCompanyCode();
        Ctrt ctrtvalue[] = validationData.getCustTraitList();
        Log.write("DirAssDirDISCValidator matchOCN_Trait_CC BW cmpnCode " + cmpnCode + " ctrtvalue" +
                ctrtvalue);
        if (ctrtvalue != null) {
            for (int i = 0; i < ctrtvalue.length; i++) {
                Ctrt ctrt = ctrtvalue[i];
                TnTrait traitTn[] = ctrt.getTnTrait();
                for (int j = 0; j < traitTn.length; j++) {
                    TnTrait traitvalue = traitTn[j];
                    String trName = traitvalue.getTraitName();
                    String trValue = traitvalue.getTraitValue();
                    Log.write("DAD BW trName  " + trName + "  trValue " + trValue + " cmpnCode " + cmpnCode);
                    if (trName != null && trValue != null &&
                            cmpnCode != null && (trName.trim().equalsIgnoreCase("ICLEC-LSP-ID")) && (trValue.trim().equalsIgnoreCase(cmpnCode))) {
                        flag = true;
                        break;
                    }
                }
            }
        } else {
            return flag;
        }
        fillterSerTypeRejCode("30004-matchOCN_Trait_CC :BW", flag, "DAD", false);
        return flag;
    }
}
