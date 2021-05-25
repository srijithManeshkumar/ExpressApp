/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.automation.reports.bean;

/**
 *
 * @author satish.t
 */
public class RejectionBean {

    private String rejDesc;
    private int rejcount;
    private int rejTot;
    private double rejPer;
    private double rejPerTot;

    /**
     * @return the rejDesc
     */
    public String getRejDesc() {
        return rejDesc;
    }

    /**
     * @param rejDesc the rejDesc to set
     */
    public void setRejDesc(String rejDesc) {
        this.rejDesc = rejDesc;
    }

    /**
     * @return the rejcount
     */
    public int getRejcount() {
        return rejcount;
    }

    /**
     * @param rejcount the rejcount to set
     */
    public void setRejcount(int rejcount) {
        this.rejcount = rejcount;
    }

    /**
     * @return the rejTot
     */
    public int getRejTot() {
        return rejTot;
    }

    /**
     * @param rejTot the rejTot to set
     */
    public void setRejTot(int rejTot) {
        this.rejTot = rejTot;
    }

    /**
     * @return the rejPer
     */
    public double getRejPer() {
        return rejPer;
    }

    /**
     * @param rejPer the rejPer to set
     */
    public void setRejPer(double rejPer) {
        this.rejPer = rejPer;
    }

    /**
     * @return the rejPerTot
     */
    public double getRejPerTot() {
        return rejPerTot;
    }

    /**
     * @param rejPerTot the rejPerTot to set
     */
    public void setRejPerTot(double rejPerTot) {
        this.rejPerTot = rejPerTot;
    }

}
