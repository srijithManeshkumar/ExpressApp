/*
 * VendorTableDataBean.java
 *
 * Created on June 11, 2009, 8:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bean;

/**
 *
 * @author kumar.k
 */
public class VendorTableDataBean extends UserBean{
    
    private String vendorConfigSqncNumber ="";
    private String compSqncNumber="";
    private String stateCode ="";
    private String oCN ="";
    private String bTN ="";
    private String wCN ="";
    private String isEmbargoed ="";
    private String tXJUR ="";
    private String serviceType="";
    private String ativityType="";
    private String isDirectory="";
    private String isEligibleToDeleteDir="";
    private String contactNo;
    
    // date values
    private String validTimeOfDayDDD = "";
    private String dueDateLowerLimit= "";
    private String dueDateUpperLimit= "";
    private String sLAWaitTime=  "";
    
    // Automate flag values
    private String vedorAutomateFlag=  "";
    private String ocnAutomateFlag=  "";
    private String stateAutomateFlag=  "";
    private String srvtypeAutomateFlag=  "";
    private String acttypeAutomateFlag=  "";
    
    
    
    /** Creates a new instance of VendorTableDataBean */
    public VendorTableDataBean() {
    }
    
    public String getVendorConfigSqncNumber() {
        return vendorConfigSqncNumber;
    }
    
    public void setVendorConfigSqncNumber(String vendorConfigSqncNumber) {
        this.vendorConfigSqncNumber = vendorConfigSqncNumber;
    }
    
    public String getCompSqncNumber() {
        return compSqncNumber;
    }
    
    public void setCompSqncNumber(String compSqncNumber) {
        this.compSqncNumber = compSqncNumber;
    }
    
    public String getStateCode() {
        return stateCode;
    }
    
    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }
    
    public String getOCN() {
        return oCN;
    }
    
    public void setOCN(String oCN) {
        this.oCN = oCN;
    }
    
    public String getBTN() {
        return bTN;
    }
    
    public void setBTN(String bTN) {
        this.bTN = bTN;
    }
    
    public String getWCN() {
        return wCN;
    }
    
    public void setWCN(String wCN) {
        this.wCN = wCN;
    }
    
    public String getIsEmbargoed() {
        return isEmbargoed;
    }
    
    public void setIsEmbargoed(String isEmbargoed) {
        this.isEmbargoed = isEmbargoed;
    }
    
    public String getTXJUR() {
        return tXJUR;
    }
    
    public void setTXJUR(String tXJUR) {
        this.tXJUR = tXJUR;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getAtivityType() {
        return ativityType;
    }
    
    public void setAtivityType(String ativityType) {
        this.ativityType = ativityType;
    }
    
    public String getIsDirectory() {
        return isDirectory;
    }
    
    public void setIsDirectory(String isDirectory) {
        this.isDirectory = isDirectory;
    }
    
    public String getIsEligibleToDeleteDir() {
        return isEligibleToDeleteDir;
    }
    
    public void setIsEligibleToDeleteDir(String isEligibleToDeleteDir) {
        this.isEligibleToDeleteDir = isEligibleToDeleteDir;
    }
    
    public String getContactNo() {
        return contactNo;
    }
    
    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }
    
    public String getValidTimeOfDayDDD() {
        return validTimeOfDayDDD;
    }
    
    public void setValidTimeOfDayDDD(String validTimeOfDayDDD) {
        this.validTimeOfDayDDD = validTimeOfDayDDD;
    }
    
    public String getDueDateLowerLimit() {
        return dueDateLowerLimit;
    }
    
    public void setDueDateLowerLimit(String dueDateLowerLimit) {
        this.dueDateLowerLimit = dueDateLowerLimit;
    }
    
    public String getDueDateUpperLimit() {
        return dueDateUpperLimit;
    }
    
    public void setDueDateUpperLimit(String dueDateUpperLimit) {
        this.dueDateUpperLimit = dueDateUpperLimit;
    }
    
    public String getSLAWaitTime() {
        return sLAWaitTime;
    }
    
    public void setSLAWaitTime(String sLAWaitTime) {
        this.sLAWaitTime = sLAWaitTime;
    }
    
    public String getVedorAutomateFlag() {
        return vedorAutomateFlag;
    }
    
    public void setVedorAutomateFlag(String vedorAutomateFlag) {
        this.vedorAutomateFlag = vedorAutomateFlag;
    }
    
    public String getOcnAutomateFlag() {
        return ocnAutomateFlag;
    }
    
    public void setOcnAutomateFlag(String ocnAutomateFlag) {
        this.ocnAutomateFlag = ocnAutomateFlag;
    }
    
    public String getStateAutomateFlag() {
        return stateAutomateFlag;
    }
    
    public void setStateAutomateFlag(String stateAutomateFlag) {
        this.stateAutomateFlag = stateAutomateFlag;
    }
    
    public String getSrvtypeAutomateFlag() {
        return srvtypeAutomateFlag;
    }
    
    public void setSrvtypeAutomateFlag(String srvtypeAutomateFlag) {
        this.srvtypeAutomateFlag = srvtypeAutomateFlag;
    }
    
    public String getActtypeAutomateFlag() {
        return acttypeAutomateFlag;
    }
    
    public void setActtypeAutomateFlag(String acttypeAutomateFlag) {
        this.acttypeAutomateFlag = acttypeAutomateFlag;
    }
    
    
}
