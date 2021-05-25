/*
 * ValidationDataBean.java
 *
 * Created on June 11, 2009, 8:25 PM
 *
 * Adding this line to check in same file version into clear case to apply
 * same label - Antony - 02/28/2014
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bean;

import com.windstream.winexpcustprof.Addr;
import com.windstream.winexpcustprof.Asoc;
import com.windstream.winexpcustprof.Gtnl;
import com.windstream.winexpcustprof.ImpctdApp;
import com.windstream.winexpcustprof.PndngOrder;
import com.windstream.winexpcustprof.Ctrt;
import com.windstream.winexpcustprof.ErrorInfo;


//import com.windstream.winexpcustprof.*;



/**
 *
 * @author kumar.k
 */
public class ValidationDataBean extends UserBean{
    
    //StatusDataResponse
    
  /*  private String custStatus;
    private CustTraits custTraitList;
    private String camsId;
    private String custType;
    private String custName;
    private String custAddress;
    private String custTaxJuris;
    private String custOrgId;
    private String custDatabase;
   
   
    //ServiceDataResponse
   
    private String custSag;
    private ImpctdApps impctdAppList;
    private PndgOrders custPndgOrderList;
    private AsocData custAsocList;
    private GtnlData gtnlTnList;
    private ErrorInfo errorInfo;
   */
    
    
    private String custStatus;
    private Ctrt custTraitList[];
    private String camsId;
    private String custType;
    private String custName;
    private Addr custAddress;
    private String custTaxJuris;
    private String custOrgId;
    private String custDatabase;
    
    
    //ServiceDataResponse
    
    private String custSag;
    private String complex;
    private String giftService;
    private ImpctdApp impctdAppList[];
    private PndngOrder custPndgOrderList[];
    private Asoc custAsocList[];
    private Gtnl gtnlTnList[];
    private ErrorInfo errorInfo;
    
    
    // newly
    
    private String pilotNo;
    private String videoType;
        
    /** Creates a new instance of ValidationDataBean */
    public ValidationDataBean() {
    }

    public String getCustStatus() {
        return custStatus;
    }

    public void setCustStatus(String custStatus) {
        this.custStatus = custStatus;
    }

    public Ctrt[] getCustTraitList() {
        return custTraitList;
    }

    public void setCustTraitList(Ctrt[] custTraitList) {
        this.custTraitList = custTraitList;
    }

    public String getCamsId() {
        return camsId;
    }

    public void setCamsId(String camsId) {
        this.camsId = camsId;
    }

    public String getCustType() {
        return custType;
    }

    public void setCustType(String custType) {
        this.custType = custType;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public Addr getCustAddress() {
        return custAddress;
    }

    public void setCustAddress(Addr custAddress) {
        this.custAddress = custAddress;
    }

    public String getCustTaxJuris() {
        return custTaxJuris;
    }

    public void setCustTaxJuris(String custTaxJuris) {
        this.custTaxJuris = custTaxJuris;
    }

    public String getCustOrgId() {
        return custOrgId;
    }

    public void setCustOrgId(String custOrgId) {
        this.custOrgId = custOrgId;
    }

    public String getCustDatabase() {
        return custDatabase;
    }

    public void setCustDatabase(String custDatabase) {
        this.custDatabase = custDatabase;
    }

    public String getCustSag() {
        return custSag;
    }

    public void setCustSag(String custSag) {
        this.custSag = custSag;
    }

    public String getComplex() {
	        return complex;
    }
    public void setComplex(String complex) {
	        this.complex = complex;
    }

    public String getGiftService() {
	        return giftService;
    }

    public void setGiftService(String giftService) {
	        this.giftService = giftService;
    }

    public ImpctdApp[] getImpctdAppList() {
        return impctdAppList;
    }

    public void setImpctdAppList(ImpctdApp[] impctdAppList) {
        this.impctdAppList = impctdAppList;
    }

    public PndngOrder[] getCustPndgOrderList() {
        return custPndgOrderList;
    }

    public void setCustPndgOrderList(PndngOrder[] custPndgOrderList) {
        this.custPndgOrderList = custPndgOrderList;
    }

    public Asoc[] getCustAsocList() {
        return custAsocList;
    }

    public void setCustAsocList(Asoc[] custAsocList) {
        this.custAsocList = custAsocList;
    }

    public Gtnl[] getGtnlTnList() {
        return gtnlTnList;
    }

    public void setGtnlTnList(Gtnl[] gtnlTnList) {
        this.gtnlTnList = gtnlTnList;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(ErrorInfo errorInfo) {
        this.errorInfo = errorInfo;
    }

     public String getPilotNo() {
        return pilotNo;
    }

    public void setPilotNo(String pilotNo) {
        this.pilotNo = pilotNo;
    }
   
    /* Added new getter setter method for videoType field from BW to be pre-populated in CI form Video field -- 12/19/2013 */
    
    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }
    /* Antony -- end of code changes -- 12/19/2013 */
    
}
