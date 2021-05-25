/*
 * UNEPDisValidator.java
 *
 * Created on June 23, 2009, 4:52 PM
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
public class UNEPDisValidator extends LSRBaseValidator{
    
    private VendorTableDataBean vendorBean;
    private ValidationDataBean validationData;
    private LSRDataBean lsrDataBean;
    
    /** Creates a new instance of UNEPDisValidator */
    public UNEPDisValidator( LSRDataBean lsrDataBean,VendorTableDataBean vendorBean,
            ValidationDataBean validationData) {
        super(lsrDataBean,vendorBean,validationData);
        this.vendorBean=vendorBean;
        this.validationData=validationData;
        this.lsrDataBean=lsrDataBean;
        
    }
    
    
     /*matchOCN_Trait_CC method used for checking
      *2nd Validation point: CC on LSR matches OCN trait name
      *UNEP-ID and OCN trait value and
      *corresponds with each TN on the LSR.
      */
    
    
    public boolean matchOCN_Trait_CC(){
        boolean flag =false;
         boolean reflag =false;
        String cmpnCode= lsrDataBean.getCompanyCode();
        Ctrt ctrtvalue[]= validationData.getCustTraitList();
        Log.write("UNEPDisValidator matchOCN_Trait_CC cmpnCode "+cmpnCode+" ctrtvalue"
                +ctrtvalue);
        if(ctrtvalue!=null){
            for(int i=0;i<ctrtvalue.length;i++){
                Ctrt ctrt=  ctrtvalue[i];
                TnTrait traitTn[]=ctrt.getTnTrait();
                for(int j=0;j<traitTn.length;j++){
                    TnTrait traitvalue=  traitTn[j];
                    String trName=traitvalue.getTraitName();
                    String trValue=traitvalue.getTraitValue();
                    Log.write("UNEPD matchOCN_Trait_CC trValue "+trValue +" trName "
                            +trName);
                    if(trName!=null && trValue!=null && cmpnCode!=null)
                    {
                        if((trName.trim().equalsIgnoreCase("UNEP-ID"))
                         &&(trValue.trim().equalsIgnoreCase(cmpnCode))){
                        flag= true;
                        break;
                        }else if((trName.trim().equalsIgnoreCase("RESOLD-LSP-ID"))
                        && (trValue.trim().equalsIgnoreCase(cmpnCode))){
                            reflag=true;
                        }
                    }
                }
            }
        }else{
            return flag;
        }
         Log.write("UNEPD matchOCN_Trait_CC flag "+flag +" reflag "+reflag); 
		if(flag){
		fillterSerTypeRejCode("50003-matchOCN_Trait_CC :BW",flag,"UNEPD",false);
		}else if(reflag){
		fillterSerTypeRejCode("50026-matchOCN_Trait_CC :BW",flag,"UNEPD",false);
		}else{
		fillterSerTypeRejCode("50003-matchOCN_Trait_CC :BW",flag,"UNEPD",false);
		}
        return flag;
    }
    
     /*checkREQTYP_LACT_DL method used for checking
      *3c PQ
      *LACT on the DL form must be a D when the REQTYP on the LSR form is E/M
      *and ACT on the LSR form is D for Resale DISC / UNEP DISC
      */
    
    public boolean checkREQTYP_LACT_DL(){
        String reqtype =lsrDataBean.getSerRequestType();
        String act =lsrDataBean.getActivity();
        String lact =lsrDataBean.getListActivity();
        Log.write("UNEPDisValidator matchERL_LACT act "+act+" lact "+lact
                +"reqtype"+reqtype);
        boolean flag=false;
        if(reqtype!=null && act!=null &&
                (reqtype.equals("M") || reqtype.equals("E")) && act.equals("D")
                &&(lact==null || lact.equals("D")  )){
            
            flag= true;
        }
        super.fillterSerTypeRejCode("50025-checkREQTYP_LACT_DL",flag,"UNEPD",false);
        return flag;
    }
    
    
    /*checkREQTYP_LNA_PS method used for checking
     *3d PQ
     *Check if LNA value on the PS and LS forms must be a D when the
     *REQTYP on the LSR form is M and ACT on the LSR form is D
     */
    
    public boolean checkREQTYP_LNA_PS(){
        String reqtype =lsrDataBean.getSerRequestType();
        String act =lsrDataBean.getActivity();
        String lnaPS =lsrDataBean.getLineActivityPSUNEP();
        Log.write("UNEPDisValidator checkREQTYP_LNA_PS act "+act+" lnaPS "+lnaPS
                +"reqtype"+reqtype);
        boolean flag=false;
        if(lnaPS!=null && lnaPS.equals("D") ){
            
            flag= true;
        }
        super.fillterSerTypeRejCode("50022-checkREQTYP_LNA_PS",flag,"UNEPD",false);
        return flag;
        
    }
    
      /*
       *V 32 Buisness Flow ,checkREQTYP_LNA_LS() method used for checking
       *LNA on the LS form is "D"
       *
       */
    
    public boolean checkREQTYP_LNA_LS(){
        String reqtype =lsrDataBean.getSerRequestType();
        String act =lsrDataBean.getActivity();
        String lnaLS =lsrDataBean.getLineActivityLSUNEP();
        Log.write("UNEPDisValidator checkREQTYP_LNA_PS act "+act+" lnaLS "+lnaLS
                +"reqtype"+reqtype);
        boolean flag=false;
        if(lnaLS!=null && lnaLS.equals("D") ){
            
            flag= true;
        }
        super.fillterSerTypeRejCode("50024-checkREQTYP_LNA_LS",flag,"UNEPD",false);
        return flag;
        
    }
    
    
      /* checkReqTypeRes method used for checking
       * V 23 Buisness Flow
       * Check if value in RTR field on LSR form is C
       *
       */
    
    public boolean checkReqTypeRes(){
        String rtr =lsrDataBean.getResTypeReq();
        Log.write("UNEPDisValidator checkReqTypeRes rtr "+rtr);
        boolean flag= false;
        if(rtr!=null && rtr.equals("C")){
            flag= true;
        }
        super.fillterSerTypeRejCode("50011-checkReqTypeRes",flag,"UNEPD",false);
        return flag;
    }
    
}
