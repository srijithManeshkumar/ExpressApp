/*
 * LSRDataBean.java
 *
 * Created on April 21, 2009, 9:27 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bean;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 *
 * @author kumar.k
 */

public class LSRDataBean extends UserBean{
    
    // common validation bean values
    private String desiedDueDate;//LSR
    private String accountNo;//LSR
    private String accountTelephoneNo;//LSR
    private String exitingActTeleNo;//EU
    private String serRequestType;//LSR
    private String activity;//LSR
    private String supplementalType;//LSR
    private String accountStatus;//cams BW
    private String oCNcd ;//LSR OCNcd is already taking companyCode object
    private String stateCD;//EU_LA
    private String typeOfService;//LSR
    private String euName;//EU
    private String CamsName;//cams
    private String CamsAtn;//cams
    private String eXPedite;//LSR
    private String coHotCut;//LSR
    private String companyCode;//LSR
    private AddressBean address;//LSR
    
    // few common validation bean values
    private String exitingAccountNo;//EU
    private String simpleportAccountNo;//SP_AN
    private String newNetwork;//LST_NNSP
    private String newNetworkSP;//SP_NNSP
    private String listActivity;//DL_LD_LACT
    private String eURetainingList;//EU_LA__ERL
    private String passCode;//VENDOR TABLE
    private List portedNBR;//NP_SD_T PORTEDNBR
    private List teleNumbs;//PS_SD_T TNS
    private List providerTN;//SP_T SP_SPPTN
    private String asoc;//CAMS
    private String project;//LSR
    private String transCallOption;//EU_DD__TCOPT
    
    // NP validation bean values
    private ArrayList lineActivityNPList;//NP_SD__LNA
    
    // SPSR validation bean values
    private String TGUID;//CAMS BW SPSR
    private String goto1;//CAMS BW SPSR
    private String simpleportDDD;//SP_DDD
    private String simpleportZIP;//SP_ZIP
    private String eUListTreatment;//SP_ELT
    
    // UNEP validation bean values
    private String lineActivityLSUNEP;//LS_SD_LNA PS_SD_LNA
    private String lineActivityPSUNEP;//LS_SD_LNA PS_SD_LNA
    private String exchangeCCType;//LS_SD_ECCKT
    
    // Resale_DISC validation bean values
    private String lineActivityResale;//RS_SD_LNA
    private String resTypeReq;//LSR_RTR
    
    // Dir_Ass_Dir_DISC validation bean values
    private String recordType;//DL_LD_RTY_1
    
    // new Added Variables
    
    private String purchaseON;
    private String purVerNum;
    private String reqType;//SPSR
    private String dsrName;//SPSR
    private String cmpnySeqNmbr;//company name
    
    private String reqstNmbr;
    private String reqstPon;
    private String reqstVer;
    private String eumi;
    private List asocListPS;
    private String dadt;//DAD
    
    private int porNumberFlag;//lerg
    private int nativeNumberFlag; //lerg
    private String histRqstNo;
    private  String euAddressTrim;
    
    private Map addressMap;
    private String typeOfService2;
    private String wcnCheckFlag;
    private boolean atnNPA;
    private boolean btnNPA;
    private String vendorautoVal;
    private List asocSPList;
    private List asocUNPList;
    private String billname;
    private String passcodeLSR;
    private List asocSGreenfieldList;
	private List asocSUnepIWList;
    //added new attribute for SLA Time for Simple Port LSRs
    public String SLATimeForSP;
               
    
    /**
     * Creates a new instance of LSRDataBean
     */
    public LSRDataBean() {
    }
    
    public String getDesiedDueDate() {
        return desiedDueDate;
    }
    
    public void setDesiedDueDate(String desiedDueDate) {
        this.desiedDueDate = desiedDueDate;
    }
    
    public String getAccountNo() {
        return accountNo;
    }
    
    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
    
    public String getAccountTelephoneNo() {
        return accountTelephoneNo;
    }
    
    public void setAccountTelephoneNo(String accountTelephoneNo) {
        this.accountTelephoneNo = accountTelephoneNo;
    }
    
    public String getExitingActTeleNo() {
        return exitingActTeleNo;
    }
    
    public void setExitingActTeleNo(String exitingActTeleNo) {
        this.exitingActTeleNo = exitingActTeleNo;
    }
    
    public String getSerRequestType() {
        return serRequestType;
    }
    
    public void setSerRequestType(String serRequestType) {
        this.serRequestType = serRequestType;
    }
    
    public String getActivity() {
        return activity;
    }
    
    public void setActivity(String activity) {
        this.activity = activity;
    }
    
    public String getAccountStatus() {
        return accountStatus;
    }
    
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
    
    public String getOCNcd() {
        return oCNcd;
    }
    
    public void setOCNcd(String oCNcd) {
        this.oCNcd = oCNcd;
    }
    
    public String getStateCD() {
        return stateCD;
    }
    
    public void setStateCD(String stateCD) {
        this.stateCD = stateCD;
    }
    
    public String getTypeOfService() {
        return typeOfService;
    }
    
    public void setTypeOfService(String typeOfService) {
        this.typeOfService = typeOfService;
    }
    
    public String getEuName() {
        return euName;
    }
    
    public void setEuName(String euName) {
        this.euName = euName;
    }
    
    public String getCamsName() {
        return CamsName;
    }
    
    public void setCamsName(String CamsName) {
        this.CamsName = CamsName;
    }
    
    public String getCamsAtn() {
        return CamsAtn;
    }
    
    public void setCamsAtn(String CamsAtn) {
        this.CamsAtn = CamsAtn;
    }
    
    public String getEXPedite() {
        return eXPedite;
    }
    
    public void setEXPedite(String eXPedite) {
        this.eXPedite = eXPedite;
    }
    
    public String getCoHotCut() {
        return coHotCut;
    }
    
    public void setCoHotCut(String coHotCut) {
        this.coHotCut = coHotCut;
    }
    
    public String getCompanyCode() {
        return companyCode;
    }
    
    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
    
    public AddressBean getAddress() {
        return address;
    }
    
    public void setAddress(AddressBean address) {
        this.address = address;
    }
    
    public String getExitingAccountNo() {
        return exitingAccountNo;
    }
    
    public void setExitingAccountNo(String exitingAccountNo) {
        this.exitingAccountNo = exitingAccountNo;
    }
    
    public String getSimpleportAccountNo() {
        return simpleportAccountNo;
    }
    
    public void setSimpleportAccountNo(String simpleportAccountNo) {
        this.simpleportAccountNo = simpleportAccountNo;
    }
    
    public String getNewNetworkSP() {
        return newNetworkSP;
    }
    
    public void setNewNetworkSP(String newNetworkSP) {
        this.newNetworkSP = newNetworkSP;
    }
    
    public String getListActivity() {
        return listActivity;
    }
    
    public void setListActivity(String listActivity) {
        this.listActivity = listActivity;
    }
    
    public String getEURetainingList() {
        return eURetainingList;
    }
    
    public void setEURetainingList(String eURetainingList) {
        this.eURetainingList = eURetainingList;
    }
    
    public String getPassCode() {
        return passCode;
    }
    
    public void setPassCode(String passCode) {
        this.passCode = passCode;
    }
    
    public List getPortedNBR() {
        return portedNBR;
    }
    
    public void setPortedNBR(List portedNBR) {
        this.portedNBR = portedNBR;
    }
    
    public List getTeleNumbs() {
        return teleNumbs;
    }
    
    public void setTeleNumbs(List teleNumbs) {
        this.teleNumbs = teleNumbs;
    }
    
    public List getProviderTN() {
        return providerTN;
    }
    
    public void setProviderTN(List providerTN) {
        this.providerTN = providerTN;
    }
    
    public String getAsoc() {
        return asoc;
    }
    
    public void setAsoc(String asoc) {
        this.asoc = asoc;
    }
    
    public String getProject() {
        return project;
    }
    
    public void setProject(String project) {
        this.project = project;
    }
    
    public String getTransCallOption() {
        return transCallOption;
    }
    
    public void setTransCallOption(String transCallOption) {
        this.transCallOption = transCallOption;
    }
    
    public ArrayList getNPLNAList() {
        return lineActivityNPList;
    }
    
    public void setNPLNAList(ArrayList lineActivityNPList) {
        this.lineActivityNPList = lineActivityNPList;
    }
    
    public String getTGUID() {
        return TGUID;
    }
    
    public void setTGUID(String TGUID) {
        this.TGUID = TGUID;
    }
    
    public String getGoto1() {
        return goto1;
    }
    
    public void setGoto1(String goto1) {
        this.goto1 = goto1;
    }
    
    public String getSimpleportDDD() {
        return simpleportDDD;
    }
    
    public void setSimpleportDDD(String simpleportDDD) {
        this.simpleportDDD = simpleportDDD;
    }
    
    public String getSimpleportZIP() {
        return simpleportZIP;
    }
    
    public void setSimpleportZIP(String simpleportZIP) {
        this.simpleportZIP = simpleportZIP;
    }
    
    public String getEUListTreatment() {
        return eUListTreatment;
    }
    
    public void setEUListTreatment(String eUListTreatment) {
        this.eUListTreatment = eUListTreatment;
    }
    
    
    
    public String getExchangeCCType() {
        return exchangeCCType;
    }
    
    public void setExchangeCCType(String exchangeCCType) {
        this.exchangeCCType = exchangeCCType;
    }
    
    public String getLineActivityResale() {
        return lineActivityResale;
    }
    
    public void setLineActivityResale(String lineActivityResale) {
        this.lineActivityResale = lineActivityResale;
    }
    
    public String getResTypeReq() {
        return resTypeReq;
    }
    
    public void setResTypeReq(String resTypeReq) {
        this.resTypeReq = resTypeReq;
    }
    
    public String getRecordType() {
        return recordType;
    }
    
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }
    
    public String getPurchaseON() {
        return purchaseON;
    }
    
    public void setPurchaseON(String purchaseON) {
        this.purchaseON = purchaseON;
    }
    
    public String getPurVerNum() {
        return purVerNum;
    }
    
    public void setPurVerNum(String purVerNum) {
        this.purVerNum = purVerNum;
    }
    
    
    public String getNewNetwork() {
        return newNetwork;
    }
    
    public void setNewNetwork(String newNetwork) {
        this.newNetwork = newNetwork;
    }
    
    
    public String getDsrName() {
        return dsrName;
    }
    
    public void setDsrName(String dsrName) {
        this.dsrName = dsrName;
    }
    
    public String getLineActivityLSUNEP() {
        return lineActivityLSUNEP;
    }
    
    public void setLineActivityLSUNEP(String lineActivityLSUNEP) {
        this.lineActivityLSUNEP = lineActivityLSUNEP;
    }
    
    public String getLineActivityPSUNEP() {
        return lineActivityPSUNEP;
    }
    
    public void setLineActivityPSUNEP(String lineActivityPSUNEP) {
        this.lineActivityPSUNEP = lineActivityPSUNEP;
    }
    
    public String getCmpnySeqNmbr() {
        return cmpnySeqNmbr;
    }
    
    public void setCmpnySeqNmbr(String cmpnySeqNmbr) {
        this.cmpnySeqNmbr = cmpnySeqNmbr;
    }
    
    public String getReqstNmbr() {
        return reqstNmbr;
    }
    
    public void setReqstNmbr(String reqstNmbr) {
        this.reqstNmbr = reqstNmbr;
    }
    
    public String getReqstPon() {
        return reqstPon;
    }
    
    public void setReqstPon(String reqstPon) {
        this.reqstPon = reqstPon;
    }
    
    public String getReqstVer() {
        return reqstVer;
    }
    
    public void setReqstVer(String reqstVer) {
        this.reqstVer = reqstVer;
    }
    
    public String getReqType() {
        return reqType;
    }
    
    public void setReqType(String reqType) {
        this.reqType = reqType;
    }
    
    public String getEumi() {
        return eumi;
    }
    
    public void setEumi(String eumi) {
        this.eumi = eumi;
    }
    
    public List getAsocListPS() {
        return asocListPS;
    }
    
    public void setAsocListPS(List asocListPS) {
        this.asocListPS = asocListPS;
    }
    
    public String getDadt() {
        return dadt;
    }
    
    public void setDadt(String dadt) {
        this.dadt = dadt;
    }
    
    public int isPorNumberFlag() {
        return porNumberFlag;
    }
    
    public void setPorNumberFlag(int porNumberFlag) {
        this.porNumberFlag = porNumberFlag;
    }
    
    public int isNativeNumberFlag() {
        return nativeNumberFlag;
    }
    
    public void setNativeNumberFlag(int nativeNumberFlag) {
        this.nativeNumberFlag = nativeNumberFlag;
    }
    
    public String getHistRqstNo() {
        return histRqstNo;
    }
    
    public void setHistRqstNo(String histRqstNo) {
        this.histRqstNo = histRqstNo;
    }
    
    public String getEuAddressTrim() {
        return euAddressTrim;
    }
    
    public void setEuAddressTrim(String euAddressTrim) {
        this.euAddressTrim = euAddressTrim;
    }
    
    public Map getAddressMap() {
        return addressMap;
    }
    
    public void setAddressMap(Map addressMap) {
        this.addressMap = addressMap;
    }
    
    public String getTypeOfService2() {
        return typeOfService2;
    }
    
    public void setTypeOfService2(String typeOfService2) {
        this.typeOfService2 = typeOfService2;
    }
    
    
    public String getWcnCheckFlag() {
        return wcnCheckFlag;
    }
    
    public void setWcnCheckFlag(String wcnCheckFlag) {
        this.wcnCheckFlag = wcnCheckFlag;
    }
    
    public boolean isAtnNPA() {
        return atnNPA;
    }
    
    public void setAtnNPA(boolean atnNPA) {
        this.atnNPA = atnNPA;
    }
    
    public boolean isBtnNPA() {
        return btnNPA;
    }
    
    public void setBtnNPA(boolean btnNPA) {
        this.btnNPA = btnNPA;
    }
    
    public String getVendorautoVal() {
        return vendorautoVal;
    }
    
    public void setVendorautoVal(String vendorautoVal) {
        this.vendorautoVal = vendorautoVal;
    }
    public List getAsocSPList() {
        return asocSPList;
    }
    
    public void setAsocSPList(List asocSPList) {
        this.asocSPList = asocSPList;
    }
    
    public List getAsocUNPList() {
        return asocUNPList;
    }
    
    public void setAsocUNPList(List asocUNPList) {
        this.asocUNPList = asocUNPList;
    }
    
     public String getBillname() {
        return billname;
    }

    public void setBillname(String billname) {
        this.billname = billname;
    }

    /**
     * @return the passcodeLSR
     */
    public String getPasscodeLSR() {
        return passcodeLSR;
    }

    /**
     * @param passcodeLSR the passcodeLSR to set
     */
    public void setPasscodeLSR(String passcodeLSR) {
        this.passcodeLSR = passcodeLSR;
    }
	
    /**
     * @return supplementalType
     */
    public String getSupplementalType() {
        return supplementalType;
    }

    /**
     * @param supplementalType to set
     */
    public void setSupplementalType(String supplementalType) {
        this.supplementalType = supplementalType;
    }
        
	/**
     * @return the asocSGreenfieldList
     */
    public List getAsocSGreenfieldList() {
        return asocSGreenfieldList;
    }

    /**
     * @param asocSGreenfieldList the asocSGreenfieldList to set
     */
    public void setAsocSGreenfieldList(List asocSGreenfieldList) {
        this.asocSGreenfieldList = asocSGreenfieldList;
    }
	/**
     * @return the asocSUnepIWList
     */
    public List getAsocSUnepIWList() {
        return asocSUnepIWList;
    }

    /**
     * @param asocSUnepIWList the asocSUnepIWList to set
     */
    public void setAsocSUnepIWList(List asocSUnepIWList) {
        this.asocSUnepIWList = asocSUnepIWList;
    }
    
    	/**
     * @return SLA time for Simple Port
     */
    public String getSLATimeForSP() {
        return SLATimeForSP;
    }

    /**
     * @param asocSUnepIWList the asocSUnepIWList to set
     */
    public void setSLATimeForSP(String SLATimeforSP) {
        this.SLATimeForSP = SLATimeForSP;
    }
}
