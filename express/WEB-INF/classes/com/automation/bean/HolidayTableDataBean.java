/*
 * HolidayTableDataBean.java
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
public class HolidayTableDataBean extends UserBean{
    
    private String hldyDate = "";
    private String hldyDscrptn = " ";
    /** Creates a new instance of HolidayTableDataBean */
    public HolidayTableDataBean() {
    }
    
    public String getHldyDate() {
        return hldyDate;
    }
    
    public void setHldyDate(String hldyDate) {
        this.hldyDate = hldyDate;
    }
    
    public String getHldyDscrptn() {
        return hldyDscrptn;
    }
    
    public void setHldyDscrptn(String hldyDscrptn) {
        this.hldyDscrptn = hldyDscrptn;
    }
    
}
