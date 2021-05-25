/*
 * VendoPortableAreaBean.java
 *
 * Created on June 12, 2009, 9:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bean;

/**
 *
 * @author kumar.k
 */
public class VendoPortableAreaBean  extends VendorTableDataBean {
    
    /** Creates a new instance of VendoPortableAreaBean */
    public VendoPortableAreaBean() {
    }
    private String portableAreaSqncNumber = " ";
    private String portableAreaNameSqncNo = " ";
    
    public String getPortableAreaSqncNumber() {
        return portableAreaSqncNumber;
    }
    
    public void setPortableAreaSqncNumber(String portableAreaSqncNumber) {
        this.portableAreaSqncNumber = portableAreaSqncNumber;
    }
    
    public String getPortableAreaNameSqncNo() {
        return portableAreaNameSqncNo;
    }
    
    public void setPortableAreaNameSqncNo(String portableAreaNameSqncNo) {
        this.portableAreaNameSqncNo = portableAreaNameSqncNo;
    }
}
