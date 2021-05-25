/*
 * ReasonCodeBean.java
 *
 * Created on August 11, 2009, 5:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bean;

/**
 *
 * @author kumar.k
 */
public class ReasonCodeBean {
   
    private String ReaCodeType;
    private String ReaCodeDscr;
    private String ReaCodeSqnc;
    
    /** Creates a new instance of ReasonCodeBean */
    public ReasonCodeBean() {
    }

    public String getReaCodeType() {
        return ReaCodeType;
    }

    public void setReaCodeType(String ReaCodeType) {
        this.ReaCodeType = ReaCodeType;
    }

    public String getReaCodeDscr() {
        return ReaCodeDscr;
    }

    public void setReaCodeDscr(String ReaCodeDscr) {
        this.ReaCodeDscr = ReaCodeDscr;
    }

    public String getReaCodeSqnc() {
        return ReaCodeSqnc;
    }

    public void setReaCodeSqnc(String ReaCodeSqnc) {
        this.ReaCodeSqnc = ReaCodeSqnc;
    }
    
}
