/*
 * SLASubmittedDateDataBean.java
 *
 * Created on June 16, 2009, 5:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bean;

import org.omg.SendingContext.RunTime;

/**
 *
 * @author kumar.k
 */
public class SLASubmittedDateDataBean {
    
    /** Creates a new instance of SLASubmittedDateDataBean */
    private String submittedDate;
    private String versionNumber;
    private String SLAtime;
    
    public SLASubmittedDateDataBean() {
    }
    
    public String getSubmittedDate() {
        return submittedDate;
    }
    
    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }
    
    public String getVersionNumber() {
        return versionNumber;
    }
    
    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
    
    public String getSLAtime() {
        return SLAtime;
    }
    
    public void setSLAtime(String SLAtime) {
        this.SLAtime = SLAtime;
    }
    
}
