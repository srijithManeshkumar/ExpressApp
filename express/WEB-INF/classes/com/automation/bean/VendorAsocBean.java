/*
 * VendorAsocBean.java
 *
 * Created on June 12, 2009, 9:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bean;

/**
 *
 * @author kumar.k
 */
public class VendorAsocBean extends VendorTableDataBean {
    
    private String vendorAsocConfigSeqNo= " ";
    private String asocTypeConfigSeqNo = " ";
    private String asocTypeCode = " ";
    private String hwAsocFeeApplies = " ";
    private String asocFeeRate = " ";
    
    
    
    /** Creates a new instance of VendorAsocBean */
    public VendorAsocBean() {
    }
    
    public String getVendorAsocConfigSeqNo() {
        return vendorAsocConfigSeqNo;
    }
    
    public void setVendorAsocConfigSeqNo(String vendorAsocConfigSeqNo) {
        this.vendorAsocConfigSeqNo = vendorAsocConfigSeqNo;
    }
    
    public String getAsocTypeConfigSeqNo() {
        return asocTypeConfigSeqNo;
    }
    
    public void setAsocTypeConfigSeqNo(String asocTypeConfigSeqNo) {
        this.asocTypeConfigSeqNo = asocTypeConfigSeqNo;
    }
    
    public String getAsocTypeCode() {
        return asocTypeCode;
    }
    
    public void setAsocTypeCode(String asocTypeCode) {
        this.asocTypeCode = asocTypeCode;
    }
    
    public String getHwAsocFeeApplies() {
        return hwAsocFeeApplies;
    }
    
    public void setHwAsocFeeApplies(String hwAsocFeeApplies) {
        this.hwAsocFeeApplies = hwAsocFeeApplies;
    }
    
    public String getAsocFeeRate() {
        return asocFeeRate;
    }
    
    public void setAsocFeeRate(String asocFeeRate) {
        this.asocFeeRate = asocFeeRate;
    }
    
    
}
