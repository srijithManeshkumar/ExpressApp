/*
 * UserBean.java
 *
 * Created on June 12, 2009, 5:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.automation.bean;

/**
 *
 * @author kumar.k
 */
public class UserBean {
    
    private String mdfdDt = " ";
    private String mdfdUserid = " ";
    /**
     * Creates a new instance of UserBean
     */
    public UserBean() {
    }

    public String getMdfdDt() {
        return mdfdDt;
    }

    public void setMdfdDt(String mdfdDt) {
        this.mdfdDt = mdfdDt;
    }

    public String getMdfdUserid() {
        return mdfdUserid;
    }

    public void setMdfdUserid(String mdfdUserid) {
        this.mdfdUserid = mdfdUserid;
    }
    
}
