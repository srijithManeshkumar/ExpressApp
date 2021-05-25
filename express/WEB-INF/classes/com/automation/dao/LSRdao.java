/*
 * LSRdao.java
 *
 * Created on April 21, 2009, 4:44 PM
 *
 * Adding this line to check in same file version into clear case to apply
 * same label - Antony - 02/28/2014
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.automation.dao;

import com.alltel.lsr.common.util.PropertiesManager;
import com.alltel.lsr.common.error.objects.ExceptionHandler;
import com.alltel.lsr.common.util.DatabaseManager;
import com.alltel.lsr.common.util.Log;

import com.automation.bean.AddressBean;
import com.automation.bean.HolidayTableDataBean;
import com.automation.bean.LSRDataBean;
import com.automation.bean.VendorTableDataBean;
import com.automation.bean.ReasonCodeBean;
import com.automation.bean.ValidationDataBean;

import com.windstream.winexpcustprof.Asoc;
import com.windstream.winexpcustprof.ImpctdApp;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Hashtable;

/**
 *
 * @author kumar.k
 */
public class LSRdao {

    /** Creates a new instance of LSRdao */
    public LSRdao() {
    }

    /*
     * getConnection() method used for getting
     * connection from LERG DataBase.
     */
    public Connection getConnection() {
        Connection conn = null;
        try {    //oracle.jdbc.OracleDriver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn =
                    DriverManager.getConnection("jdbc:oracle:thin:@sun105.windstream.com:1521:exp00t10",
                    "elsr", "elsr");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return conn;
    }

    /*
     * loadLSRData method used for loading LsrDatabean and getting
     * input parameter of Request Number
     * Added reqVersion parameter to get the most recent version's data for Q&V - Antony - 05/20/2011
     */
    public LSRDataBean loadLSRData(String reqSeqNmbr,String reqVer) throws Exception {
        Log.write("LSRdao loadLSRData reqSeqNmbr  " + reqSeqNmbr+"reqVer "+reqVer);
        LSRDataBean objLSRDataBean = new LSRDataBean();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        Map adressmap = new HashMap();
        List upclisttos = new ArrayList();
        List nptns = new ArrayList();
        ArrayList npLNAList = new ArrayList();
        List splist = new ArrayList();
        List asocSPList = new ArrayList();
        List asoclist = new ArrayList();
        // Get DB Connection
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            //LSR Table Values starting

            //add if condition to change query for version > 0

            if(reqVer.equals("0"))
                strQuery = "select * from request_t where RQST_SQNC_NMBR ='" +
                    reqSeqNmbr + "' and rqst_stts_cd='SUBMITTED'";
            else
                strQuery = "select * from request_t where RQST_SQNC_NMBR ='" +
                    reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";

            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                objLSRDataBean.setReqstNmbr(rs.getString("RQST_SQNC_NMBR"));
                objLSRDataBean.setHistRqstNo(rs.getString("RQST_HSTRY_SQNC_NMBR"));
                objLSRDataBean.setReqstPon(rs.getString("RQST_PON"));
                objLSRDataBean.setReqstVer(rs.getString("RQST_VRSN"));
                objLSRDataBean.setOCNcd(rs.getString("OCN_CD"));
                objLSRDataBean.setCmpnySeqNmbr(rs.getString("CMPNY_SQNC_NMBR"));
                objLSRDataBean.setSerRequestType(rs.getString("SRVC_TYP_CD"));
                objLSRDataBean.setReqType(rs.getString("RQST_TYP_CD"));
                objLSRDataBean.setActivity(rs.getString("ACTVTY_TYP_CD"));
                objLSRDataBean.setAccountNo(rs.getString("AN"));
                String atn = rs.getString("ATN");
                Log.write("=strnpa==" + atn);
                if (atn != null && atn.length() == 12) {
                    objLSRDataBean.setAccountTelephoneNo(atn.replaceAll("-", ""));
                }

                objLSRDataBean.setMdfdDt(rs.getString("MDFD_DT"));
                objLSRDataBean.setMdfdUserid(rs.getString("MDFD_USERID"));

            } else {
                return null;
            }

            strQuery = "select STT_NM from STATE_T where " +
                    "STT_CD=(select OCN_STT from request_t where RQST_SQNC_NMBR ='" +
                    reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"')";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                objLSRDataBean.setStateCD(rs.getString("STT_NM"));
            }
            strQuery = "select * from LSR_T where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add version number here and at all places for supps
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                objLSRDataBean.setCoHotCut(rs.getString("LSR_CHC"));
                String cc = rs.getString("LSR_CC");
                if (cc != null) {
                    objLSRDataBean.setCompanyCode(cc);
                }
                Log.write("DDD kk " + rs.getString("LSR_DDD"));
                objLSRDataBean.setDesiedDueDate(rs.getString("LSR_DDD"));
                objLSRDataBean.setEXPedite(rs.getString("LSR_EXP"));
                objLSRDataBean.setTypeOfService(rs.getString("LSR_TOS_1"));
                objLSRDataBean.setTypeOfService2(rs.getString("LSR_TOS_2"));
                objLSRDataBean.setProject(rs.getString("LSR_Project"));
                objLSRDataBean.setResTypeReq(rs.getString("LSR_RTR"));
                //Code change to pass the validation for NNSP with lower case and to avoid page break for null value - Saravanan
                    //objLSRDataBean.setNewNetwork(rs.getString("LSR_NNSP"));
                String nnsp_value = rs.getString("LSR_NNSP");
                if (nnsp_value != null && nnsp_value.length() > 0) {
                	
                	objLSRDataBean.setNewNetwork(nnsp_value.toUpperCase());
                }
                else 
                {
                	objLSRDataBean.setNewNetwork(nnsp_value);
                }
                
                objLSRDataBean.setSupplementalType(rs.getString("LSR_SUP"));

                String atn = rs.getString("LSR_TELNO_INIT");
                if (objLSRDataBean.getAccountTelephoneNo() == null && atn != null) {
                    objLSRDataBean.setAccountTelephoneNo(atn.replaceAll("-", ""));
                }
                objLSRDataBean.setPasscodeLSR(rs.getString("LSR_PSWD"));
            }

            strQuery = "select * from DSR_SA_T where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            String trimAddress[] = new String[13];
            if (rs.next()) {

                if (rs.getString("DSR_SA_NAME") != null) {
                    objLSRDataBean.setDsrName(rs.getString("DSR_SA_NAME"));
                }

                AddressBean addbean = new AddressBean();

                addbean.setCity(rs.getString("DSR_SA_CITY"));
                addbean.setState(rs.getString("DSR_SA_SAST"));
                addbean.setStreet(rs.getString("DSR_SA_SASN"));
                addbean.setZip(rs.getString("DSR_SA_ZIP"));
                adressmap.put("DSR", addbean);
                objLSRDataBean.setAddressMap(adressmap);

                trimAddress[0] = rs.getString("DSR_SA_SAPR");
                trimAddress[1] = rs.getString("DSR_SA_sano");
                trimAddress[2] = rs.getString("DSR_SA_SASF");
                trimAddress[3] = rs.getString("DSR_SA_sasd");
                trimAddress[4] = rs.getString("DSR_SA_sasn");
                trimAddress[5] = rs.getString("DSR_SA_sath");
                trimAddress[6] = rs.getString("DSR_SA_sass");
                trimAddress[7] = rs.getString("DSR_SA_ld1");
                trimAddress[8] = rs.getString("DSR_SA_lv1");
                trimAddress[9] = rs.getString("DSR_SA_ld2");
                trimAddress[10] = rs.getString("DSR_SA_lv2");
                trimAddress[11] = rs.getString("DSR_SA_ld3");
                trimAddress[12] = rs.getString("DSR_SA_lv3");
                String trmAddr = trimAddress(trimAddress);
                if (trmAddr.length() > 0) {
                    objLSRDataBean.setEuAddressTrim(trmAddr);
                }

            }

            strQuery = "select * from EU_T where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                objLSRDataBean.setExitingAccountNo(rs.getString("EU_EAN"));
                objLSRDataBean.setBillname(rs.getString("EU_BILLNM"));
                String eatn = rs.getString("EU_EATN");
                if (eatn != null && eatn.length() == 12) {
                    objLSRDataBean.setExitingActTeleNo(eatn.replaceAll("-", ""));
                }
                AddressBean addbean = new AddressBean();
                addbean.setBillcon(rs.getString("EU_BILLCON"));
                addbean.setCity(rs.getString("EU_CITY"));
                addbean.setFloor(rs.getString("EU_FLOOR"));
                addbean.setRoomMailStop(rs.getString("EU_ROOM_MAILSTOP"));
                addbean.setState(rs.getString("EU_STATE"));
                addbean.setStreet(rs.getString("EU_STREET"));
                addbean.setZip(rs.getString("EU_ZIP"));
                objLSRDataBean.setAddress(addbean);
            }

            strQuery = "select * from DSR_T where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                String dcc = rs.getString("DSR_CC");
                if (dcc != null) {
                    objLSRDataBean.setCompanyCode(dcc);
                }
                String edd = rs.getString("DSR_EDD");
                Log.write("EDD kk " + edd);
                if (edd != null && edd.trim().length() > 0 &&
                   (objLSRDataBean.getSerRequestType().trim().equals("G") ||
                    objLSRDataBean.getSerRequestType().trim().equals("H") ||
                    objLSRDataBean.getSerRequestType().trim().equals("J"))) {
                    objLSRDataBean.setDesiedDueDate(edd);
                }

                String exp = rs.getString("DSR_EXP");
                if (exp != null) {
                    objLSRDataBean.setEXPedite(exp);
                }
                String dbillnm = rs.getString("DSR_DBILLNM");
                if (dbillnm != null) {
                    objLSRDataBean.setBillname(dbillnm);
                }

                // Fix for PI Issue 25 - Antony - 06/22/2010
                if (objLSRDataBean.getSerRequestType().trim().equals("G") ||
                    objLSRDataBean.getSerRequestType().trim().equals("H") ||
                    objLSRDataBean.getSerRequestType().trim().equals("J")) {

                    String tos1 = rs.getString("DSR_TOS_1");
                    if (tos1 != null) {
                        objLSRDataBean.setTypeOfService(tos1);
                    }
                    String tos2 = rs.getString("DSR_TOS_2");
                    if (tos2 != null) {
                        objLSRDataBean.setTypeOfService2(tos2);
                    }
                }

                if (rs.getString("DSR_Project") != null) {
                    objLSRDataBean.setProject(rs.getString("DSR_Project"));
                }
                if (rs.getString("DSR_RTR") != null) {
                    objLSRDataBean.setResTypeReq(rs.getString("DSR_RTR"));
                }
                if (rs.getString("DSR_DCHC") != null) {
                    objLSRDataBean.setCoHotCut(rs.getString("DSR_DCHC"));
                }

                objLSRDataBean.setDadt(rs.getString("DSR_DADT"));

                //Fix for EATN from DSR issue - Antony - 07/03/2010
                if (objLSRDataBean.getSerRequestType().trim().equals("G") ||
                    objLSRDataBean.getSerRequestType().trim().equals("H") ||
                    objLSRDataBean.getSerRequestType().trim().equals("J")) {

                String eatn = rs.getString("DSR_EATN");
                if (eatn != null && eatn.length() == 12) {
                    objLSRDataBean.setExitingActTeleNo(eatn.replaceAll("-", ""));
                }
                }
            }

            strQuery = "select * from EU_LA_T where RQST_SQNC_NMBR= '" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                objLSRDataBean.setEuName(rs.getString("EU_LA_NAME"));
                objLSRDataBean.setEURetainingList(rs.getString("EU_LA_ERL"));
                objLSRDataBean.setEumi(rs.getString("eu_la_eumi"));
                trimAddress[0] = rs.getString("eu_la_SAPR");
                trimAddress[1] = rs.getString("eu_la_sano");
                trimAddress[2] = rs.getString("eu_la_SASF");
                trimAddress[3] = rs.getString("eu_la_sasd");
                trimAddress[4] = rs.getString("eu_la_sasn");
                trimAddress[5] = rs.getString("eu_la_sath");
                trimAddress[6] = rs.getString("eu_la_sass");
                trimAddress[7] = rs.getString("eu_la_ld1");
                trimAddress[8] = rs.getString("eu_la_lv1");
                trimAddress[9] = rs.getString("eu_la_ld2");
                trimAddress[10] = rs.getString("eu_la_lv2");
                trimAddress[11] = rs.getString("eu_la_ld3");
                trimAddress[12] = rs.getString("eu_la_lv3");
                AddressBean addbean = new AddressBean();
                addbean.setCity(rs.getString("eu_la_city"));
                addbean.setCounty(rs.getString("eu_la_lcon"));
                addbean.setSano(trimAddress[1]);
                addbean.setZip(rs.getString("eu_la_zip"));
                addbean.setState(rs.getString("eu_la_state"));
                addbean.setStreet(getStreetAddress(trimAddress));
                addbean.setMsagStreet(getMsagStreetAddress(trimAddress));

                String preDir = trimAddress[3];
                if (preDir != null) {
                    preDir = preDir.trim();
                } else {
                    preDir = "";
                }

                addbean.setMsagPreDir(preDir);
                adressmap.put("EU_LA", addbean);
                objLSRDataBean.setAddressMap(adressmap);
                String trmAddr = trimAddress(trimAddress);
                if (trmAddr.length() > 0) {
                    objLSRDataBean.setEuAddressTrim(trmAddr);
                }
            }

            strQuery = "select * from rs_sd_t where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                if (rs.getString("rs_sd_TCOPT") != null) {
                    objLSRDataBean.setTransCallOption(rs.getString("rs_sd_TCOPT"));
                }
                objLSRDataBean.setLineActivityResale(rs.getString("rs_sd_lna"));
            }

            strQuery = "select * from NP_SD_T where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";;//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            while (rs.next()) {
                String nptnstr = rs.getString("NP_SD_PORTEDNBR");
                if (nptnstr != null) {
                    nptns.add(nptnstr);
                }
                //objLSRDataBean.setLineActivityNP(rs.getString("NP_SD_LNA")); -- code change to send LNA as a list
                String lnastr = rs.getString("NP_SD_LNA");
                if (lnastr != null) {
                    npLNAList.add(lnastr);
                }
            }
            objLSRDataBean.setPortedNBR(nptns);
            objLSRDataBean.setNPLNAList(npLNAList);

            strQuery = "select * from SP_T where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            while (rs.next()) {
                objLSRDataBean.setCompanyCode(rs.getString("SP_CC"));
                objLSRDataBean.setSimpleportAccountNo(rs.getString("SP_AN"));
                objLSRDataBean.setNewNetworkSP(rs.getString("SP_NNSP"));
                objLSRDataBean.setDesiedDueDate(rs.getString("SP_DDD"));
                objLSRDataBean.setSimpleportDDD(rs.getString("SP_DDD"));
                objLSRDataBean.setSimpleportZIP(rs.getString("SP_ZIP"));
                objLSRDataBean.setEUListTreatment(rs.getString("SP_ELT"));
                splist.add(rs.getString("SP_PTN"));
                String atn = rs.getString("sp_telno");
                if (rs.getString("sp_pwd") != null) {
                    objLSRDataBean.setPasscodeLSR(rs.getString("sp_pwd"));
                }
                if (objLSRDataBean.getAccountTelephoneNo() == null && atn != null) {
                    objLSRDataBean.setAccountTelephoneNo(atn.replaceAll("-", ""));
                }

            }
            objLSRDataBean.setProviderTN(splist);
            strQuery = "select * from DL_LD_T where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                objLSRDataBean.setListActivity(rs.getString("DL_LD_LACT"));
                objLSRDataBean.setRecordType(rs.getString("DL_LD_RTY_1"));
            }
            strQuery = "select * from LS_SD_T where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                objLSRDataBean.setLineActivityLSUNEP(rs.getString("LS_SD_LNA"));
                objLSRDataBean.setExchangeCCType(rs.getString("LS_SD_ECCKT"));
            }
            strQuery = "select * from PS_SD_T where RQST_SQNC_NMBR ='" + reqSeqNmbr + "' and RQST_VRSN='"+reqVer+"'";//add reqver here
            Log.write("strQuery : "+strQuery);
            rs = stmt.executeQuery(strQuery);

            while (rs.next()) {
                if (rs.getString("PS_SD_FEATURE_1") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_1"));
                }
                if (rs.getString("PS_SD_FEATURE_2") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_2"));
                }
                if (rs.getString("PS_SD_FEATURE_3") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_3"));
                }
                if (rs.getString("PS_SD_FEATURE_4") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_4"));
                }
                if (rs.getString("PS_SD_FEATURE_5") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_5"));
                }
                if (rs.getString("PS_SD_FEATURE_6") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_6"));
                }
                if (rs.getString("PS_SD_FEATURE_7") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_7"));
                }
                if (rs.getString("PS_SD_FEATURE_8") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_8"));
                }
                if (rs.getString("PS_SD_FEATURE_9") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_9"));
                }
                if (rs.getString("PS_SD_FEATURE_12") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_12"));
                }
                if (rs.getString("PS_SD_FEATURE_13") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_13"));
                }
                if (rs.getString("PS_SD_FEATURE_14") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_14"));
                }
                if (rs.getString("PS_SD_FEATURE_15") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_15"));
                }
                if (rs.getString("PS_SD_FEATURE_16") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_16"));
                }
                if (rs.getString("PS_SD_FEATURE_17") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_17"));
                }
                if (rs.getString("PS_SD_FEATURE_18") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_18"));
                }
                if (rs.getString("PS_SD_FEATURE_19") != null) {
                    asoclist.add(rs.getString("PS_SD_FEATURE_19"));
                }
                objLSRDataBean.setLineActivityPSUNEP(rs.getString("PS_SD_LNA"));
                if (rs.getString("PS_SD_TCOPT") != null) {
                    objLSRDataBean.setTransCallOption(rs.getString("PS_SD_TCOPT"));
                }
                upclisttos.add(rs.getString("PS_SD_TNS"));
            }
            objLSRDataBean.setTeleNumbs(upclisttos);
            objLSRDataBean.setAsocListPS(asoclist);
            //Log.write("complex_asoc_t ZIP()-: " + objLSRDataBean.getSimpleportZIP());
            Log.write("unep_asocs_t asoclist.size() " + asoclist.size());

            String spFlag = retrieveSPFlag(objLSRDataBean.getReqstNmbr(),objLSRDataBean.getReqstVer());

            Log.write("Is this a Simple Port NP? "+spFlag);

            //if (objLSRDataBean.getSimpleportZIP() != null) {
            if (spFlag.equals("Y")) {
                strQuery = "select * from complex_asocs_t";
                Log.write("Asoc Query-: " + strQuery);
                rs = stmt.executeQuery(strQuery);
                String insts = null;
                while (rs.next()) {
                    asocSPList.add(rs.getString("Asoc_cd"));
                }
                objLSRDataBean.setAsocSPList(asocSPList);
            }
            if (asoclist.size() > 0) {
                strQuery = "select * from unep_asocs_t";
                Log.write("unep_asocs_t Query-: " + strQuery);
                rs = stmt.executeQuery(strQuery);
                String insts = null;
                ArrayList upcAsocList = new ArrayList();
                while (rs.next()) {
                    upcAsocList.add(rs.getString("Asoc_cd"));
                }
                objLSRDataBean.setAsocUNPList(upcAsocList);

                //Set Asoc list for unep asocs that require IW work - need to go to Manual Review
                strQuery = "select * from unep_asocs_t where iwflag = 'Y'";
                Log.write("unep_asocs_t IW Query-: " + strQuery);
                rs = stmt.executeQuery(strQuery);
                insts = null;
                ArrayList upcIWAsocList = new ArrayList();
                while (rs.next()) {
                    upcIWAsocList.add(rs.getString("Asoc_cd"));
                }
                objLSRDataBean.setAsocSUnepIWList(upcIWAsocList);
            }
			//Satish code for greenfieldasoclist
            strQuery="select * from greenfield_asocs_t";
            Log.write("strQuery : "+strQuery);
            rs=stmt.executeQuery(strQuery);
            ArrayList greenfieldAsocList=new ArrayList();
            while(rs.next()){
                greenfieldAsocList.add(rs.getString("Asoc_cd"));
            }
            objLSRDataBean.setAsocSGreenfieldList(greenfieldAsocList);

        } catch (SQLException sqle) {
            Log.write("sql exception : "+sqle.getMessage());
            ExceptionHandler.handleException("Sql Exception ", sqle);
            throw sqle;
        } catch (Exception e) {
            Log.write("exception : "+e.getMessage());
            ExceptionHandler.handleException("Exception ", e);
            throw e;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return objLSRDataBean;
    }

    public boolean checkNPAOCNState(String reqSeqNmbr, String npa) throws Exception {
        Log.write("LSRdao checkNPAOCNState reqSeqNmbr  " + reqSeqNmbr + " npa " + npa);
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String btn = "";
        boolean flag = false;
        // Get DB Connection
        try {

            if (npa.length() >= 3) {
                btn = npa.substring(0, 3);
            }
            Log.write("=checkNPAOCNState=btn= " + btn);
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            //LSR Table Values starting

            String stateValue = null;
            String btnValue = null;

            strQuery = "select STT_CD from STATE_T where STT_CD=(select OCN_STT from request_t where RQST_SQNC_NMBR ='" + reqSeqNmbr + "')";
            Log.write("=checkNPAOCNState=strQuery= " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                stateValue = rs.getString("STT_CD");
            }

            strQuery = " select STT_CD from STATE_T where STT_CD=(select STT_CD from NPA_T where NPA ='" + btn +
                    "')";

            Log.write("=checkNPAOCNState=strQuery= " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                btnValue = rs.getString("STT_CD");
            }
            Log.write("=checkNPAOCNState=stateValue= " + stateValue + " btnValue " + btnValue);
            if (stateValue != null && btnValue != null && stateValue.equals(btnValue)) {
                flag = true;
            }
            Log.write("=checkNPAOCNState=flag" + flag);
        } catch (SQLException sqle) {
            ExceptionHandler.handleException("Sql Exception ", sqle);
            throw sqle;
        } catch (Exception e) {
            ExceptionHandler.handleException("Exception ", e);
            throw e;
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return flag;
    }

    /*
     * loadVendorTable method used for loading all the
     * VendorTableDataBean from DataBase and to storing
     * Vector object
     */
    public Vector loadVendorTable() throws Exception {
        Log.write("LSRdao loadVendorTable calling  ");
        VendorTableDataBean objVendorBean = null;
        Vector objVector = new Vector();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "SELECT * from VENDOR_TABLE_CONFIG_T ";


        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);
			//removed the word kk from below log message as it is not needed -- Antony - 05/26/2010
            Log.write(Log.DEBUG_VERBOSE, "LPB() rs " + rs);
            while (rs.next()) {
                objVendorBean = new VendorTableDataBean();
                objVendorBean.setVendorConfigSqncNumber(rs.getString("VENDOR_CONFIG_SQNC_NMBR"));
                objVendorBean.setCompSqncNumber(rs.getString("CMPNY_SQNC_NMBR"));
                objVendorBean.setStateCode(rs.getString("STT_CD"));
                objVendorBean.setOCN(rs.getString("OCN_CD"));
                String btn = rs.getString("BTN");
                objVendorBean.setBTN(btn);
                objVendorBean.setWCN(rs.getString("WCN"));
                objVendorBean.setIsEmbargoed(rs.getString("IS_EMBARGOED"));
                objVendorBean.setTXJUR(rs.getString("TaxExemptions"));
                objVendorBean.setServiceType(rs.getString("SRVC_TYP_CD"));
                objVendorBean.setContactNo(rs.getString("CONTACTNUMBER"));
                objVendorBean.setAtivityType(rs.getString("ACTVTY_TYP_CD"));
                objVendorBean.setIsDirectory(rs.getString("IS_DIRECTORY"));
                objVendorBean.setIsEligibleToDeleteDir(rs.getString("IS_ELIGIBLE_TO_DIR_DELETE"));

                objVendorBean.setValidTimeOfDayDDD(rs.getString("VALID_TIME_OF_DAY_FOR_DDD"));
                objVendorBean.setDueDateLowerLimit(rs.getString("DDD_INTERVAL_LOWER_LIMIT"));
                objVendorBean.setDueDateUpperLimit(rs.getString("DDD_INTERVAL_UPPER_LIMIT"));
                objVendorBean.setSLAWaitTime(rs.getString("SLA_WAIT_TIME"));


                objVendorBean.setOcnAutomateFlag(rs.getString("OCN_AUTOMATE_FLAG"));
                objVendorBean.setStateAutomateFlag(rs.getString("STATE_AUTOMATE_FLAG"));
                objVendorBean.setSrvtypeAutomateFlag(rs.getString("SRVTYPE_AUTOMATE_FLAG"));
                objVendorBean.setVedorAutomateFlag(rs.getString("VEDOR_AUTOMATE_FLAG"));
                objVendorBean.setActtypeAutomateFlag(rs.getString("ACTTYPE_AUTOMATE_FLAG"));

                objVector.add(objVendorBean);
            }
        } catch (SQLException sqle) {

            Log.write(1, sqle.toString());
            sqle.printStackTrace();
        } catch (Exception e) {
            Log.write(1, e.toString());
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return objVector;
    }

    /*
     * loadHolidayTable method used for loading all the
     * HolidayTableDataBean from DataBase and to storing
     * Vector object
     */
    public Vector loadHolidayTable() throws Exception {
        Log.write("LSRdao loadHolidayTable calling===  ");
        Vector objVector = new Vector();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "SELECT DISTINCT TO_CHAR(HLDY_DT, 'YYYYMMDD') as HLDY_DT, HLDY_DSCRPTN " +
                " FROM HOLIDAY_T ORDER BY 1 ";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);
            Log.write(Log.DEBUG_VERBOSE, "LSR " + rs);
            while (rs.next()) {
                objVector.add(rs.getString("HLDY_DT"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return objVector;
    }

    /*
     * loadReasonCodeTable method used for loading all the
     * ReasonCodeTableBean from DataBase and to storing
     * Vector object
     */
    public Map loadReasonCodeTable() throws Exception {
        Log.write("LSRdao loadReasonCodeTable calling  ");
        Map rsCodeMap = new HashMap();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "select * from reason_code_t";
        HolidayTableDataBean objholTDB = null;
        ReasonCodeBean reasonCodeBean = null;
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            while (rs.next()) {
                reasonCodeBean = new ReasonCodeBean();
                reasonCodeBean.setReaCodeDscr(rs.getString("RSN_CD_DSCRPTN"));
                reasonCodeBean.setReaCodeSqnc(rs.getString("RSN_CD_SQNC_NMBR"));
                reasonCodeBean.setReaCodeType(rs.getString("RSN_CD_TYP"));
                rsCodeMap.put(rs.getString("RSN_CD_SQNC_NMBR"), reasonCodeBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return rsCodeMap;
    }

    public String getSLACompletedRequests() throws Exception {
        Log.write("Calling LSRdao:getSLACompletedRequests()");
        Connection con = null;
        CallableStatement stmt = null;
        String noOfRequestsCompSLA = "";
        String strQuery = "{ call ? := sp_get_sla_requests() }";

        try {
            con = DatabaseManager.getConnection();
            Log.write(" getSLACompletedRequests conn: " + con + "strQuery " + strQuery);
            stmt = con.prepareCall(strQuery);
            stmt.registerOutParameter(1, java.sql.Types.VARCHAR);
            stmt.execute();

            noOfRequestsCompSLA = stmt.getObject(1).toString();
            Log.write("Value of string returned by SLA stored procedure:" + noOfRequestsCompSLA);

        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.handleException("Exception 11: ", e);
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return noOfRequestsCompSLA;
    }

    public String callStatusUpdateProc(String reqNo,String reqVrsn,String resultCode,String errorMessage) throws Exception {
        Log.write("Calling LSRdao:callStatusUpdateProc()");
        Connection con = null;
        CallableStatement stmt = null;
        String result = "ERROR";
        String strQuery = "{ call LR_UPDATE_SUSPEND_PROC(?,?,?,?,?) }";

        try {
            con = DatabaseManager.getConnection();
            Log.write(" LR Suspend Update conn: " + con + "strQuery " + strQuery);
            stmt = con.prepareCall(strQuery);
            stmt.setString(1,reqNo);
            stmt.setString(2,reqVrsn);
            stmt.setString(3,resultCode);
            stmt.setString(4,errorMessage);
            stmt.registerOutParameter(5, java.sql.Types.INTEGER);
            stmt.execute();

            result = String.valueOf(stmt.getInt(5));

            Log.write("Value of string returned by SLA stored procedure:" + result);

        } catch (Exception e) {
            e.printStackTrace();
            result = "ERROR";
            ExceptionHandler.handleException("Exception 11: ", e);
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return result;
    }

	public String callMRStatusUpdateProc(String reqPON,String reqVrsn,String ocn,String errorCode,
                                         String errorMessage) throws Exception {
        Log.write("Calling LSRdao:MRStatusUpdateProc() after SOA/Order failure ....");
        Connection con = null;
        CallableStatement stmt = null;
        String result = "ERROR";
        String strQuery = "{ call LR_FORM_UPDATE_PROC(?,?,?,?,?,?,?,?,?,?,?) }";

        try {
            con = DatabaseManager.getConnection();
            Log.write(" MR Status Update conn: " + con + "strQuery " + strQuery);
            stmt = con.prepareCall(strQuery);
            stmt.setString(1,ocn);
            stmt.setString(2,reqPON);
            stmt.setString(3,reqVrsn);
            stmt.setString(4," ");
            stmt.setString(5,errorCode);
            stmt.setString(6,errorMessage);
            stmt.setString(7," ");
            stmt.setString(8," ");
            stmt.registerOutParameter(9,java.sql.Types.INTEGER);
            stmt.registerOutParameter(10,java.sql.Types.VARCHAR);
            stmt.registerOutParameter(11,java.sql.Types.VARCHAR);
            stmt.execute();

            result = String.valueOf(stmt.getInt(9));

            Log.write("Value of string returned by MR status update proc:" + result);

        } catch (Exception e) {
            e.printStackTrace();
            result = "ERROR";
            ExceptionHandler.handleException("Exception 11: ", e);
        } finally {

            DatabaseManager.releaseConnection(con);
        }

        return result;
    }

    public String sendArchitelPOSTRequest(String TN) throws Exception {
    Log.write("Calling LSRdao:sendArchitelPOSTRequest()");
    Connection con = null;
    CallableStatement stmt = null;
    String strQuery = "{ call ? := sp_http_post_func(?) }";
    boolean returnFlag = false;
    String result = "";
    String param1 = "PhoneNumber1="+TN.trim()+"&action1=D&PhoneNumber2=&PhoneNumber3=&" +
                    "PhoneNumber4=&PhoneNumber5=&PhoneNumber6=&PhoneNumber7=&PhoneNumber8=&" +
                    "PhoneNumber9=&PhoneNumber10=&basket=LSPAC&UpdateBTN=Submit&userid=e005072A";

    Log.write("Value of param1: "+param1);

    try {
        con = DatabaseManager.getConnection();
        Log.write(" sendArchitelPOSTRequest conn: " + con + "strQuery " + strQuery);
        stmt = con.prepareCall(strQuery);
        stmt.registerOutParameter(1, java.sql.Types.VARCHAR);
        stmt.setString(2,param1);

        stmt.execute();

        result = stmt.getObject(1).toString();

        Log.write("Value of string returned by Architel Stored Proc:" + result);

        } catch (Exception e) {
        e.printStackTrace();
        ExceptionHandler.handleException("Exception 11: ", e);
        } finally {
        DatabaseManager.releaseConnection(con);
        }

    return result;
    }

    public Vector getPortedTNs(String reqNo,String reqVrsn,String portedNmbrTable,String portedNmbrField) throws Exception {
    Log.write("LSRdao getPorted TNs method ===  ");
    Vector tnList = new Vector();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs;
    String strQuery = "select * from "+portedNmbrTable.trim()+" where RQST_SQNC_NMBR ='"+
                       reqNo.trim()+"' and RQST_VRSN ='"+reqVrsn.trim()+"'";
Log.write("LSRdao getPorted TNs method find portedNmbrField ===  " + portedNmbrField);
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
			String atn = "";
            rs = stmt.executeQuery(strQuery);
            while (rs.next()) {

                if(portedNmbrTable.trim().equals("PS_SD_T")) {
                    tnList.add(rs.getString(portedNmbrField.trim())+"/"+rs.getString("PS_SD_PIC")
                               +"/"+rs.getString("PS_SD_IPIC")+"/"+rs.getString("PS_SD_FPI"));
                } else {
                    //atn = rs.getString(portedNmbrField.trim());
                     atn = rs.getString(portedNmbrField) == null ? "" : rs.getString(portedNmbrField).trim();
          Log.write("LSRdao getPorted TNs method find atn ===  " + atn);
                    if(atn.trim().length()==17) {
                        Log.write("here is a range TN.....: "+atn);

                        String [] stringArray = atn.split("-");
                        String firstTN = stringArray[0]+"-"+stringArray[1]+"-"+stringArray[2];
                        tnList.add(firstTN);

                        int startTNLine = Integer.parseInt(stringArray[2]);
                        int endTNLine = Integer.parseInt(stringArray[3]);

                        String tnInRange = "";

                        while(!(startTNLine == endTNLine)) {
                            startTNLine++;
                            tnInRange = String.valueOf(startTNLine);
                            if(tnInRange.trim().length() < 4) {
                                if(tnInRange.trim().length() == 1)
                                    tnInRange = "000"+tnInRange;
                                else if(tnInRange.trim().length() == 2)
                                    tnInRange = "00"+tnInRange;
                                else if(tnInRange.trim().length() == 3)
                                    tnInRange = "0"+tnInRange;
                            }
                            tnList.add(stringArray[0]+"-"+stringArray[1]+"-"+tnInRange);
                        }
                    } else {
                        atn = rs.getString(portedNmbrField.trim());

                        if(atn != null && atn.length() > 0)
                            tnList.add(atn);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    return tnList;
    }

    /*
     * get ONSP SPID value from WIN_SPIDS_T table
     *
     */

    public String getWINSPID(String tnState,String tnWCN) throws Exception {
    Log.write("LSRdao getWINSPID for TN State: "+tnState+" and TN WCN: "+tnWCN);

    String onspSPID = "";
    String userID = "";
    String password = "";
    String spidTable = PropertiesManager.getProperty("lsr.SOA.SPIDTableName","");
    Connection con = null;
    Statement stmt = null;
    ResultSet rs;
    String strQuery = "select onsp,userid,passwd from "+spidTable+" where tn_state ='"+
                       tnState.trim()+"' and wcn ='"+tnWCN.trim()+"'";
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            while (rs.next()) {
                onspSPID = rs.getString("ONSP");

                Log.write("ONSP SPID : "+onspSPID);

                if(onspSPID == null) onspSPID = "";

                userID = rs.getString("USERID");

                if (userID == null) userID = "";

                password = rs.getString("PASSWD");

                if (password == null) password = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    return onspSPID;
    }

    /*
     * check if NNSP is available in the NPAC_SPIDS_T table
     *
     */

    public boolean checkNNSP(String nnsp) throws Exception {
        Log.write("LSRdao checking NNSP SPID in NPAC_SPIDS table for NNSP: "+nnsp);

        boolean result = false;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "select 1 from npac_spids_t where nnsp='"+nnsp.trim()+"'";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            if(rs.next()) {
                result = true;
            }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DatabaseManager.releaseConnection(con);
            }

        return result;
    }

    /*
     * get ONSP SPID value from WIN_SPIDS_T table
     *
     */

    public Vector getWINSPIDList() throws Exception {
    Log.write("In LSRdao getWINSPIDList");

    Vector spidList = new Vector();
    String onspSPID = "";
    String userID = "";
    String password = "";
    String spidTable = PropertiesManager.getProperty("lsr.SOA.SPIDTableName","");
    Connection con = null;
    Statement stmt = null;
    ResultSet rs;
    String strQuery = "select distinct onsp,userid,passwd from "+spidTable.trim();

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            while (rs.next()) {
                    onspSPID = rs.getString("ONSP");

                    Log.write("ONSP SPID: "+onspSPID);

                    if(onspSPID == null) onspSPID = "";

                    userID = rs.getString("USERID");

                    if (userID == null) userID = "";

                    password = rs.getString("PASSWD");

                    if (password == null) password = "";

                    Hashtable htONSPSPID = new Hashtable();

                    htONSPSPID.put("ONSP",onspSPID);
                    htONSPSPID.put("USERID",userID);
                    htONSPSPID.put("PASSWD",password);

                    spidList.add(htONSPSPID);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    return spidList;
    }


    /*
     * get data from request_t for Order processing and other FOC calls
     *
     */


public Hashtable getRequestData(String reqNo,String reqVrsn) throws Exception {
    Log.write("LSRdao getRequestData method ===  ");
    Hashtable reqData = new Hashtable();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs;
    String strQuery = "select cus_wcn,cus_taxjur,cmpny_sqnc_nmbr,cus_typ,broadband_cus," +
                      "greenfield_cus from request_t where RQST_SQNC_NMBR ='"+
                       reqNo.trim()+"' and RQST_VRSN ='"+reqVrsn.trim()+"'";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            while (rs.next()) {
                reqData.put("CUST_WCN",rs.getString("CUS_WCN"));
                reqData.put("CUST_TAXJUR",rs.getString("CUS_TAXJUR"));
                reqData.put("COMP_SEQ_NO",rs.getString("CMPNY_SQNC_NMBR"));
                reqData.put("CUST_TYPE",rs.getString("CUS_TYP"));
                reqData.put("BROADBAND_CUST",rs.getString("BROADBAND_CUS"));
                reqData.put("GREENFIELD_CUST",rs.getString("GREENFIELD_CUS"));

                Log.write("Request table data WCN: "+(String) reqData.get("CUST_WCN"));
                Log.write("Request table data TAXJUR: "+(String) reqData.get("CUST_TAXJUR"));
                Log.write("Request table data Company Seq No: "+(String) reqData.get("COMP_SEQ_NO"));
                Log.write("Request table data broadband customer: "+(String) reqData.get("BROADBAND_CUST"));
                Log.write("Request table data greenfield customer: "+(String) reqData.get("GREENFIELD_CUST"));
            }

            stmt.close();
            //get company type and company name from company table
            strQuery = "select cmpny_typ,cmpny_nm from company_t where cmpny_sqnc_nmbr='"+(String)reqData.get("COMP_SEQ_NO")+"'";

            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            while (rs.next()) {
                reqData.put("COMP_TYPE",rs.getString("CMPNY_TYP"));
                reqData.put("COMP_NAME",rs.getString("CMPNY_NM"));
                Log.write("Request table data Company Type: "+(String) reqData.get("COMP_NAME"));
            }

            String compType = "";
            compType = (String) reqData.get("COMP_TYPE");

            if(compType != null && compType.equals("L")) {
                reqData.put("WIRELESS_IND","Y");
            } else {
                reqData.put("WIRELESS_IND","N");
            }

            stmt.close();

            String taxExemptCD = "";
            int i = 0;
            String origTaxJUR = (String) reqData.get("CUST_TAXJUR");
            String result;

            StringTokenizer taxJurArray = new StringTokenizer((String)reqData.get("CUST_TAXJUR"),"/");


            taxExemptCD = taxJurArray.nextToken();
            Log.write("Tax Exempt Code: "+taxExemptCD);

            taxExemptCD = taxJurArray.nextToken();
            Log.write("Tax Exempt Code: "+taxExemptCD);

            Log.write("Tax Exempt Code Array size: "+String.valueOf(taxJurArray.countTokens()));

            while(taxJurArray.hasMoreTokens()) {
                taxExemptCD = taxJurArray.nextToken();
                strQuery = "select 1 RESULT from tax_exempt_t where trim(tax_exempt_cd)='"+taxExemptCD.trim()+"'";

                Log.write("Tax Exempt Code: "+taxExemptCD);
                Log.write("strQuery: "+strQuery);
                stmt = con.createStatement();
                rs = stmt.executeQuery(strQuery);

                while (rs.next()) {
                    result = rs.getString("RESULT");

                    if(result != null && result.trim().equals("1")) {
                        origTaxJUR = origTaxJUR.replaceAll("/"+taxExemptCD.trim(),"");
                        Log.write("Inside RESULT = 1 if block: "+origTaxJUR);
                    }
                }
            }

            reqData.put("CUST_TAXJUR",origTaxJUR);
            stmt.close();
            //get pic and Ipic from ps_sd_t table
            strQuery = "select ps_sd_pic,ps_sd_ipic from ps_sd_t where rqst_sqnc_nmbr='"+reqNo
                        +"' and rqst_vrsn='"+reqVrsn+"'";

            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);
            boolean firstRecord = true;

            while (rs.next()) {
                if(firstRecord) {
                    reqData.put("PIC",rs.getString("PS_SD_PIC"));
                    reqData.put("IPIC",rs.getString("PS_SD_IPIC"));
                    Log.write("PS SD table data PIC: "+(String) reqData.get("PIC"));
                    Log.write("PS SD table data IPIC: "+(String) reqData.get("IPIC"));
                    firstRecord = false;
                } else {
                    break;
                }
            }

            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    return reqData;
    }

public Vector getVendorTableASOCs(String vendorTableSeqNo,Hashtable reqData) throws Exception {
    Log.write("LSRdao getVendorTableASOCs method ===  ");

    Vector asocsVector = new Vector();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs;
    String reqBusResInd = (String) reqData.get("CUST_TYPE");

    if(reqBusResInd == null)
        reqBusResInd = "ALL";

    String strQuery = "select asoc_cd,how_asoc_fee_applies," +
                            " asoc_fee_rate,bus_res_ind from vendor_asoc_config_t" +
                            " where (asoc_type_config_sqnc_nmbr='2' or asoc_type_config_sqnc_nmbr='19') " +
                            "and (bus_res_ind = '" +reqBusResInd.trim()+ "' or bus_res_ind = 'ALL')"+
                            "and vendor_config_sqnc_nmbr='"+vendorTableSeqNo.trim()+"'";

    Log.write("strquery :"+strQuery);
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            String asocCD;
            String howAsocFeeApplies;
            float asocFeeRateFloat;
            String asocFeeRate;
            String busResInd;

            while (rs.next()) {
                Hashtable asocData = new Hashtable();

                asocCD = rs.getString("ASOC_CD");

                if(asocCD == null) asocCD = "";

                howAsocFeeApplies = rs.getString("HOW_ASOC_FEE_APPLIES");

                if(howAsocFeeApplies == null) howAsocFeeApplies = "";

                asocFeeRateFloat = rs.getFloat("ASOC_FEE_RATE");
                DecimalFormat df= new DecimalFormat("0.00");
		asocFeeRate=df.format(asocFeeRateFloat);

                if(asocFeeRate == null) asocFeeRate = "0.00";

                busResInd = rs.getString("BUS_RES_IND");

                if(busResInd == null) busResInd = "";

                asocData.put("ASOC_CD",asocCD);
                asocData.put("HOW_ASOC_FEE_APPLIES",howAsocFeeApplies);
                asocData.put("ASOC_FEE_RATE", asocFeeRate);
                asocData.put("BUS_RES_IND", busResInd);

                Log.write("Asoc table data ASOC_CD: "+(String) asocData.get("ASOC_CD"));
                Log.write("Asoc table data HOW_ASOC_FEE_APPLIES: "+(String) asocData.get("HOW_ASOC_FEE_APPLIES"));
                Log.write("Asoc table data ASOC_FEE_RATE: "+(String) asocData.get("ASOC_FEE_RATE"));
                Log.write("Asoc table data BUS_RES_IND: "+(String) asocData.get("BUS_RES_IND"));

                asocsVector.add(asocData);
            }

            stmt.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    return asocsVector;
}

public Vector getSuppFeeAsocs(String vendorTableSeqNo,String reqBusResInd, String reqSupTyp,String focStatus) throws Exception {
    Log.write("LSRdao getSuppFeeAsocs method ===  ");

    Vector asocsVector = new Vector();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs;
    String asocTypSeqNo = "";

    if(reqSupTyp.equals("1")){
        asocTypSeqNo = "4";
    }else if(reqSupTyp.equals("2")){
        asocTypSeqNo = "15";
    }else{
        asocTypSeqNo = "20";
    }

    if(reqBusResInd == null)
        reqBusResInd = "ALL";

    String strQuery = "select asoc_cd,how_asoc_fee_applies," +
                            " asoc_fee_rate,bus_res_ind from vendor_asoc_config_t" +
                            " where asoc_type_config_sqnc_nmbr='"+asocTypSeqNo.trim()+"'"+
                            " and bus_res_ind = '" +reqBusResInd.trim()+ "'"+
                            "and vendor_config_sqnc_nmbr='"+vendorTableSeqNo.trim()+"'";

    Log.write("strquery :"+strQuery);
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            String asocCD;
            String howAsocFeeApplies;
            float asocFeeRateFloat;
            String asocFeeRate;
            String busResInd;

            Hashtable afterFOCFee = null;
            Hashtable eachVersionFee = null;

            while (rs.next()) {

                asocCD = rs.getString("ASOC_CD");

                if(asocCD == null) asocCD = "";

                howAsocFeeApplies = rs.getString("HOW_ASOC_FEE_APPLIES");

                if(howAsocFeeApplies == null) howAsocFeeApplies = "";

                asocFeeRateFloat = rs.getFloat("ASOC_FEE_RATE");
                DecimalFormat df= new DecimalFormat("0.00");
		asocFeeRate=df.format(asocFeeRateFloat);

                if(asocFeeRate == null) asocFeeRate = "0.00";

                busResInd = rs.getString("BUS_RES_IND");

                if(busResInd == null) busResInd = "";

                //if PON was never sent to FOC before and current version's internal status is Pre-FOC
                //and afterFOC HAFA exists for this vendor then apply afterFOC fee
                if(focStatus == null && howAsocFeeApplies.equals("After FOC")) {

                    afterFOCFee = new Hashtable();

                    afterFOCFee.put("ASOC_CD",asocCD);
                    afterFOCFee.put("HOW_ASOC_FEE_APPLIES",howAsocFeeApplies);
                    afterFOCFee.put("ASOC_FEE_RATE", asocFeeRate);
                    afterFOCFee.put("BUS_RES_IND", busResInd);

                    Log.write("HAFA value of After FOC found.");

                    Log.write("Asoc table data ASOC_CD: "+(String) afterFOCFee.get("ASOC_CD"));
                    Log.write("Asoc table data HOW_ASOC_FEE_APPLIES: "+(String) afterFOCFee.get("HOW_ASOC_FEE_APPLIES"));
                    Log.write("Asoc table data ASOC_FEE_RATE: "+(String) afterFOCFee.get("ASOC_FEE_RATE"));
                    Log.write("Asoc table data BUS_RES_IND: "+(String) afterFOCFee.get("BUS_RES_IND"));

                } else if(howAsocFeeApplies.equals("Each Version")) {

                    //if vendor has HAFA value of each version then apply eachversion fee for every version
                    //regardless of previous or current status

                    eachVersionFee = new Hashtable();

                    eachVersionFee.put("ASOC_CD",asocCD);
                    eachVersionFee.put("HOW_ASOC_FEE_APPLIES",howAsocFeeApplies);
                    eachVersionFee.put("ASOC_FEE_RATE", asocFeeRate);
                    eachVersionFee.put("BUS_RES_IND", busResInd);

                    Log.write("HAFA value of Each Version found.");

                    Log.write("Asoc table data ASOC_CD: "+(String) eachVersionFee.get("ASOC_CD"));
                    Log.write("Asoc table data HOW_ASOC_FEE_APPLIES: "+(String) eachVersionFee.get("HOW_ASOC_FEE_APPLIES"));
                    Log.write("Asoc table data ASOC_FEE_RATE: "+(String) eachVersionFee.get("ASOC_FEE_RATE"));
                    Log.write("Asoc table data BUS_RES_IND: "+(String) eachVersionFee.get("BUS_RES_IND"));
                }

            }

            if(afterFOCFee != null)
                asocsVector.add(afterFOCFee);
            else if(eachVersionFee != null)
                asocsVector.add(eachVersionFee);

            stmt.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    return asocsVector;
}

public Vector getVTNPubNListASOCs(String vendorTableSeqNo,Hashtable reqData) throws Exception {
    Log.write("LSRdao getVendorTableASOCs method ===  ");

    Vector asocsVector = new Vector();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs;
    String reqBusResInd = (String) reqData.get("CUST_TYPE");

    if(reqBusResInd == null)
        reqBusResInd = "ALL";

    String strQuery = "select asoc_cd,asoc_type_config_sqnc_nmbr,how_asoc_fee_applies," +
                            " asoc_fee_rate,bus_res_ind from vendor_asoc_config_t" +
                            " where (asoc_type_config_sqnc_nmbr='17' or asoc_type_config_sqnc_nmbr='18') " +
                            "and (bus_res_ind = '" +reqBusResInd.trim()+ "' or bus_res_ind = 'ALL')"+
                            "and vendor_config_sqnc_nmbr='"+vendorTableSeqNo.trim()+"'";

    Log.write("strquery :"+strQuery);
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            String asocCD;
            String howAsocFeeApplies;
			float asocFeeRateFloat;
            String asocFeeRate;
            String busResInd;
            String asocType;

            while (rs.next()) {
                Hashtable asocData = new Hashtable();

                asocCD = rs.getString("ASOC_CD");

                if(asocCD == null) asocCD = "";

                howAsocFeeApplies = rs.getString("HOW_ASOC_FEE_APPLIES");

                if(howAsocFeeApplies == null) howAsocFeeApplies = "";

                asocFeeRateFloat = rs.getFloat("ASOC_FEE_RATE");
                DecimalFormat df= new DecimalFormat("0.##");
		asocFeeRate=df.format(asocFeeRateFloat);

                if(asocFeeRate == null) asocFeeRate = "0.00";

                busResInd = rs.getString("BUS_RES_IND");

                if(busResInd == null) busResInd = "";

                asocType = rs.getString("asoc_type_config_sqnc_nmbr");

                if(asocType == null) asocType = "";


                asocData.put("ASOC_CD",asocCD);
                asocData.put("HOW_ASOC_FEE_APPLIES",howAsocFeeApplies);
                asocData.put("ASOC_FEE_RATE", asocFeeRate);
                asocData.put("BUS_RES_IND", busResInd);
                asocData.put("ASOC_TYPE",asocType);

                Log.write("Asoc table data ASOC_CD: "+(String) asocData.get("ASOC_CD"));
                Log.write("Asoc table data HOW_ASOC_FEE_APPLIES: "+(String) asocData.get("HOW_ASOC_FEE_APPLIES"));
                Log.write("Asoc table data ASOC_FEE_RATE: "+(String) asocData.get("ASOC_FEE_RATE"));
                Log.write("Asoc table data BUS_RES_IND: "+(String) asocData.get("BUS_RES_IND"));
                Log.write("Asoc table data ASOC_TYPE: "+(String) asocData.get("ASOC_TYPE"));

                asocsVector.add(asocData);
            }

            stmt.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    return asocsVector;
    }

    //satish code for broadband field update
    // Method  for broadband field updation -- start
     public void updateBroadbandReqTable(String reqNo, ValidationDataBean validationData,LSRDataBean objLSRDataBean) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String broadFieldValue="N";
        ImpctdApp ImpctdApp[] = validationData.getImpctdAppList();
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
              for (int i = 0; i < ImpctdApp.length; i++) {
                   String impctAppcd = ImpctdApp[i].getImpctdAppCd();
                   if(impctAppcd.indexOf("DSL")!=-1){
                      broadFieldValue="Y";
                      break;
                   }
              }
            strQuery = "update request_t set BROADBAND_CUS='" + broadFieldValue + "'where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + objLSRDataBean.getReqstVer() + "'";
            Log.write("updateReqTable broadband strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            // Add update to CI table as well.. This is needed for "NP and SPSR Daily Report"
            if ((objLSRDataBean.getSerRequestType().equals("C") && objLSRDataBean.getActivity().equals("V")) ||
                 (objLSRDataBean.getSerRequestType().equals("S") && objLSRDataBean.getActivity().equals("F"))) {
                  if (broadFieldValue.equals("Y")) {
                          strQuery = "update ci_t set CI_BB='Yes'where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + objLSRDataBean.getReqstVer() + "'";
                          Log.write("updateCITable broadband strQuery-: " + strQuery);
                          rs = stmt.executeQuery(strQuery);
                  }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    }
    // Method  for greenfield field updation -- start
    //satish code for greenfield update
    public void updateGreenfieldReqTable(String reqNo, ValidationDataBean validationData,LSRDataBean objLSRDataBean) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String greenFieldValue="N";
        List ascoGreenList = objLSRDataBean.getAsocSGreenfieldList();
        Asoc asocArray[] = validationData.getCustAsocList();

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
              for (int i = 0; i < asocArray.length; i++) {
                   Asoc asoc = asocArray[i];
                   String asocName=asoc.getAsocName();
                   if(ascoGreenList.contains(asocName)){
                      greenFieldValue="Y";
                      break;
                   }
              }
            strQuery = "update request_t set GREENFIELD_CUS='" + greenFieldValue + "'where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + objLSRDataBean.getReqstVer() + "'";
            Log.write("updateReqTable greenfield strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            // Add update to CI table as well.. This is needed for "NP and SPSR Daily Report"
            if ( (objLSRDataBean.getSerRequestType().equals("C") && objLSRDataBean.getActivity().equals("V")) ||
	         (objLSRDataBean.getSerRequestType().equals("S") && objLSRDataBean.getActivity().equals("F"))) {
                  if(greenFieldValue.equals("Y")) {
                          strQuery = "update ci_t set CI_GF='Yes'where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + objLSRDataBean.getReqstVer() + "'";
                          Log.write("updateCITable greenfield strQuery-: " + strQuery);
                          rs = stmt.executeQuery(strQuery);
                  }
	    }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    }

    //method to update simplePortFlag field in request_t table
     public void updateSimpleOrderFlag(String reqNo, String reqVer,String spFlag) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "update request_t set SIMPLE_PORT_FLAG ='"+spFlag+"' where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + reqVer + "'";
            Log.write("updateReqTable simple port flag strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            /*should we do this for CI table too
            // Add update to CI table as well.. This is needed for "NP and SPSR Daily Report"
            if ((objLSRDataBean.getSerRequestType().equals("C") && objLSRDataBean.getActivity().equals("V")) ||
                 (objLSRDataBean.getSerRequestType().equals("S") && objLSRDataBean.getActivity().equals("F"))) {
                  if (broadFieldValue.equals("Y")) {
                          strQuery = "update ci_t set CI_BB='Yes'where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + objLSRDataBean.getReqstVer() + "'";
                          Log.write("updateCITable broadband strQuery-: " + strQuery);
                          rs = stmt.executeQuery(strQuery);
                  }
            }
             */

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    }

    //method to update simplePort sla time field in request_t table
     public void updateSLATimeForSP(String reqNo, String reqVer,String slaTime) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "update request_t set SP_SLA_TIME ='"+slaTime+"' where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + reqVer + "'";
            Log.write("updateReqTable simple port sla time strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    }

     /*
     * Retrieve Simple Port Flag from request_t table
     *
     */

    public String getSPSLATime(String reqNo,String reqVer) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String spSLATime = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select sp_sla_time from request_t where rqst_sqnc_nmbr='"+reqNo+"' and rqst_vrsn='"+reqVer+"'";

            Log.write("retrieveSPSLATime strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                spSLATime = rs.getString("sp_sla_time");
            }

            if(spSLATime == null)
                spSLATime = "";

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return spSLATime;
    }


    // Method to validate MSAG address

    public HashMap validateMSAG(String state,String county,String city,String sano,String street,String preDir) throws Exception {
        Connection con = null;
        CallableStatement cstmt = null;
        HashMap hashMap = new HashMap();
        String strQuery = "{ call fw.msag_pkg.valAddress(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
        try {
            String fwr_connectionpool_number = PropertiesManager.getProperty("lsr.FWR.connectionpool","");
            con = DatabaseManager.getConnection(Integer.parseInt(fwr_connectionpool_number.trim()));
            cstmt = con.prepareCall(strQuery);
            Log.write("LSRdao getMsagValues strQuery  " + strQuery);
            cstmt.setString(1, state.trim());
            cstmt.setString(2, county.trim());
            cstmt.setString(3, city.trim());
            cstmt.setString(4, sano.trim());
            cstmt.setString(5, preDir.trim());
            cstmt.setString(6, street.trim());
            cstmt.registerOutParameter(7, Types.VARCHAR);
            cstmt.registerOutParameter(8, Types.INTEGER);
            cstmt.registerOutParameter(9, Types.INTEGER);
            cstmt.registerOutParameter(10, Types.VARCHAR);
            cstmt.execute();
            hashMap.put("p_valid", cstmt.getString(7));
            hashMap.put("p_fieldInError",new String(""+cstmt.getInt(8)));
            hashMap.put("p_errorCode",new String(""+cstmt.getInt(9)));
            hashMap.put("p_errorInfo",cstmt.getString(10));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con, 4);
        }

        return hashMap;
    }


    /*
     * insertRejCode method used to storing  Rejection code .
     *
     */
    public int insertRejCode(Map mapRejCode, LSRDataBean lsrBean, String user)
            throws Exception {
        Log.write("LSRdao insertRejCode calling  ");
        Vector passVect = (Vector) mapRejCode.get("Pass");
        Vector failVect = (Vector) mapRejCode.get("Fail");
        Vector mailVect = (Vector) mapRejCode.get("Manal");
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String reqNmbr = lsrBean.getReqstNmbr();
        String reqPon = lsrBean.getReqstPon();
        String reqVer = lsrBean.getReqstVer();
        // Get DB Connection
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            for (int i = 0; i < failVect.size(); i++) {
                String str = (String) failVect.get(i);
                strQuery = "INSERT INTO AUTOMATION_RESULTS_T " +
                        "values(AUTOMATION_RESULTS_SEQ.nextval,'" + reqNmbr + "','" +
                        reqPon + "','" + reqVer + "',sysdate,'submitted','" + str + "','N',sysdate,'" + user + "')";
                Log.write(" strQuery " + strQuery);
                rs = stmt.executeQuery(strQuery);
            }
            for (int i = 0; i < passVect.size(); i++) {
                String str = (String) passVect.get(i);
                strQuery = "INSERT INTO AUTOMATION_RESULTS_T " +
                        "values(AUTOMATION_RESULTS_SEQ.nextval,'" + reqNmbr + "','" +
                        reqPon + "','" + reqVer + "',sysdate,'submitted','" + str + "','Y',sysdate,'" + user + "')";
                Log.write(" strQuery " + strQuery);
                rs = stmt.executeQuery(strQuery);
            }

            for (int i = 0; i < mailVect.size(); i++) {
                String str = (String) mailVect.get(i);
                strQuery = "INSERT INTO AUTOMATION_RESULTS_T " +
                        "values(AUTOMATION_RESULTS_SEQ.nextval,'" + reqNmbr + "','" +
                        reqPon + "','" + reqVer + "',sysdate,'submitted','" + str + "','M',sysdate,'" + user + "')";
                Log.write(" strQuery " + strQuery);
                rs = stmt.executeQuery(strQuery);
            }

            con.commit();

        } catch (Exception e) {
            Log.write(1, e.toString());
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return 0;
    }

    /*
     * insertRejDescrpttoLR method used to
     * storing LR_T and AE_T tables.
     *
     */
    public int insertRejDescrpttoLR(String rsDescr[], LSRDataBean lsrBean, String user) throws Exception {
        Log.write("LSRdao insertRejDescrpttoLR rsDescr  " + rsDescr + " user" + user);
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String reqNmbr = lsrBean.getReqstNmbr();
        String reqPon = lsrBean.getReqstPon();
        String reqVer = lsrBean.getReqstVer();

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            strQuery = "INSERT INTO AUTOMATION_STATUSES_T " +
                    "values(AUTOMATION_STATUS_SEQ.nextval,'" + reqNmbr + "','" + reqPon + "','" + reqVer + "','SUBMITTED'," +
                    "'SUBMITTED',SYSDATE,'" + user + "','" + rsDescr[0] + "','" + rsDescr[1] + "','" + rsDescr[2] + "')";
            rs = stmt.executeQuery(strQuery);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    /*
     *  Lerg DB-getWindstreamNativeNumberLerg
     *
     *
     */
    public int getWindstreamNativeNumberLerg(String atn) throws Exception {
        Log.write("LSRdao getWindstreamNativeNumberLerg atn  " + atn);
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String companyName = "";
        String strQuery = "select * from lerg_1 where ocn in (select ocn from lerg_6 where npa='" + atn.substring(0, 3) + "' and nxx='" + atn.substring(3, 6) + "' "+
                                                                               "and block_id='"+atn.substring(6,7)+"') ";
                                                //+
                                                //"and (upper(ABBRE_OCN_NAME) like  " +
                                                //"'%WINDSTREAM%' or upper(ABBRE_OCN_NAME) like '%VALOR TELECOMMUNICATIONS%' or upper(ABBRE_OCN_NAME) like " +
                                                //"'%KCC TELCOM%' or upper(ABBRE_OCN_NAME) like  '%CONCORD TELEPHONE COMPANY%' " +
                                                //               "or upper(ABBRE_OCN_NAME) like  '%CTC EXCHANGE SERVICES%')";
        try {
            con = DatabaseManager.getConnection(3);
            stmt = con.createStatement();
            Log.write("LSRdao getWindstreamNativeNumberLerg strQuery  " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                companyName = rs.getString("ABBRE_OCN_NAME");

            } else {
                strQuery = "select * from lerg_1 where ocn in (select ocn from lerg_6 where npa='" + atn.substring(0, 3) + "' and nxx='" + atn.substring(3, 6) + "' "+
                                                                               "and block_id='A') ";
                                                //+
                                                //"and (upper(ABBRE_OCN_NAME) like  " +
                                                //"'%WINDSTREAM%' or upper(ABBRE_OCN_NAME) like '%VALOR TELECOMMUNICATIONS%' or upper(ABBRE_OCN_NAME) like " +
                                                //"'%KCC TELCOM%' or upper(ABBRE_OCN_NAME) like  '%CONCORD TELEPHONE COMPANY%' " +
                                                //               "or upper(ABBRE_OCN_NAME) like  '%CTC EXCHANGE SERVICES%')";
                stmt.close();
                rs.close();
                stmt = con.createStatement();
                Log.write("LSRdao getWindstreamNativeNumberLerg strQuery  " + strQuery);
                rs = stmt.executeQuery(strQuery);

                if (rs.next()) {
                    companyName = rs.getString("ABBRE_OCN_NAME");
                }
            }

            companyName = companyName.toUpperCase();

            Log.write("value of company name: "+companyName);

            if(companyName.indexOf("WINDSTREAM") >= 0 ||
               companyName.indexOf("VALOR TELECOMMUNICATIONS") >= 0 ||
               companyName.indexOf("KCC TELCOM") >= 0 ||
               companyName.indexOf("CONCORD TELEPHONE COMPANY") >= 0 ||
               companyName.indexOf("CTC EXCHANGE SERVICES") >= 0) {
                Log.write("Native validation 1 returned.");
                return 1;
            } else if(companyName.length() == 0) {
                Log.write("Native validation 2 returned.");
                return 2;//company does not exist in LERG so send to MR
            } else {
                Log.write("Native validation 0 returned.");
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 2;//send to Manual Review with message - Unable to connect to LERG
        } finally {
            DatabaseManager.releaseConnection(con, 3);
        }

        //return 0;
    }

    /*
     * getPortNumberLerg Method used for conjnecting Lerg DB and
     * get the output of RC_ABBRE
     */
    public String getPortNumberLerg(String atn) throws Exception {
        Log.write("LSRdao getPortNumberLerg atn  " + atn);
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String rcabbr = "";
        try {
            if (atn != null && atn.length() > 6) {

                strQuery = "select * from lerg_6 where npa='" + atn.substring(0, 3) + "' and nxx='" + atn.substring(3, 6) + "' and block_id='" + atn.substring(6, 7) +
                        "'";
                Log.write("LSRdao getPortNumberLerg strQuery  " + strQuery);


                con = DatabaseManager.getConnection(3);

                if(con == null)
                    Log.write("Connection from lerg is null !!!!");

                stmt = con.createStatement();


                rs = stmt.executeQuery(strQuery);
                boolean flag = rs.next();
                if (flag) {
                    rcabbr = rs.getString("RC_ABBRE");
                    Log.write("LSRdao getPortNumberLerg if Line Actual rcabbr  " + rcabbr);

                } else {
                    strQuery = "select * from lerg_6 where npa='" + atn.substring(0, 3) + "' and nxx='" + atn.substring(3, 6) +
                            "' and block_id='A'";
                    Log.write("LSRdao getPortNumberLerg else strQuery  " + strQuery);
                    rs = stmt.executeQuery(strQuery);
                    if (rs.next()) {
                        rcabbr = rs.getString("RC_ABBRE");
                        Log.write("LSRdao getPortNumberLerg else if rcabbr Line=A " + rcabbr);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.write("Exception in LERG validation. DB Error : "+e.getMessage());
            throw new Exception();
        } finally {
            DatabaseManager.releaseConnection(con, 3);
        }

        return rcabbr;
    }

    /*
     * checkVendorPortable_PortNumber Method used for checking  portable number
     * from the PORTABLE_AREA_NAME_T table
     */
    public int checkVendorPortable_PortNumber(String rcabbr,
            VendorTableDataBean objVendorBean) throws Exception {
        Log.write("LSRdao checkVendorPortable_PortNumber rcabbr  " + rcabbr);
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String pName = "";
        boolean result = false;
        int resultInt = 0;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            strQuery = "select * from PORTABLE_AREA_NAME_T where PORTABLE_AREA_NAME_SQNC_NMBR in" +
                    "(select distinct(PORTABLE_AREA_NAME_SQNC_NMBR)" +
                    " from PORTABLE_AREA_T where VENDOR_CONFIG_SQNC_NMBR='" + objVendorBean.getVendorConfigSqncNumber() + "')";
            rs = stmt.executeQuery(strQuery);
            Log.write("LSRdao checkVendorPortable_PortNumber strQuery  " + strQuery);

            while (rs.next()) {
                pName = rs.getString("PORTABLE_AREA_NAME");
                if (pName.trim().equalsIgnoreCase(rcabbr.trim())) {
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 2;
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        if (result)
            resultInt = 1;
        else
            resultInt = 0;

        return resultInt;
    }

    public int inserRejectCodeLR_REJTABLE(String[] values) throws Exception {
        Log.write("LSRdao inserRejectCodeLR_REJTABLE values  " + values);
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String pName = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "update LR_REJ_T set reject_value='" + values[2] +
                    "' where rqst_sqnc_nmbr='" + values[0] + "' and frm_sctn_occ='" + values[1] + "'";
            Log.write("==strQuery kk=" + strQuery);

            //fix for fetch out of sequence exception - Antony - 06162010
            Log.write("Check for out of sequence exception here !");
            i = stmt.executeUpdate(strQuery);

            /*while (rs.next()) {
                i++;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    public int inserRejectCodeLR_RRC_T(String[] values) throws Exception {
        Log.write("LSRdao inserRejectCodeLR_RRC_T values  " + values);
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String pName = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "update LR_RRC_T set" +
                    " RCODE1='" + values[2] + "',RDET1='" + values[3] + "',RCODE2='" + values[4] + "',RDET2='" + values[5] +
                    "',RCODE3='" + values[6] + "',RDET3='" + values[7] + "',RCODE4='" + values[8] + "',RDET4='" + values[9] +
                    "'  where rqst_sqnc_nmbr='" + values[0] + "' and frm_sctn_occ='" + values[1] + "'";

            strQuery = " update LR_RRC_T set " + getDynamicQuery(values, "LR");

            Log.write("==strQuery LR_RRC_T : =" + strQuery);

            //fix for fetch out of sequence exception - Antony - 06162010
            Log.write("Check for out of sequence exception here !");
            i = stmt.executeUpdate(strQuery);

            /*while (rs.next()) {
                i++;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    public int inserRejectCodeLR_T(String[] values) throws Exception {
        Log.write("LSRdao inserRejectCodeLR_RRC_T values  " + values);
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String pName = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = " update LR_T set " + getDynamicQuery(values, "LR");

            Log.write("==strQuery LR_T : =" + strQuery);

            //fix for fetch out of sequence exception - Antony - 06162010
            Log.write("Check for out of sequence exception here !");
            i = stmt.executeUpdate(strQuery);

            /*while (rs.next()) {
                i++;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    public int inserRejectCodeAE_RRC_T(String[] values)
            throws Exception {
        Log.write("LSRdao inserRejectCodeLR_RRC_T values  " + values);
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String pName = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            /*
             *--erase rejects from previous version if any -- antony -- 05/15/2011
                      update lr_t set lr_rcode = '', lr_rdet = '',rcode1 = '', rdet1 = '',
                                  rcode2 = '', rdet2 = '',rcode3 = '', rdet3 = '',mdfd_dt=sysdate,mdfd_userid='e005072A'
                                  where rqst_sqnc_nmbr=req_no and rqst_vrsn=req_vrsn;
             *
             *select rcode,rdet,rcode1,rdet1,rcode2,rdet2,rcode3,rdet3
                      into rej_code,rej_det,rej_code1,rej_det1,rej_code2,rej_det2,rej_code3,rej_det3
                      from ae_t where rqst_sqnc_nmbr=req_no and rqst_vrsn=req_vrsn;
             *
             */

                //wipe out LR table rejects from previous version -- antony -- 05/15/2011
            strQuery = "update lr_t set lr_rcode = '', lr_rdet = '',rcode1 = '', rdet1 = '',"+
                                  "rcode2 = '', rdet2 = '',rcode3 = '', rdet3 = '',mdfd_dt=sysdate,mdfd_userid='e005072A' "+
                                  "where rqst_sqnc_nmbr='"+values[6]+"' and rqst_vrsn='"+values[5]+"'";

            Log.write("=inserRejectCode AE_RRC_T lr table erase strQuery: " + strQuery);

            i = stmt.executeUpdate(strQuery);

            //wipe out AE table rejects from previous version -- antony -- 05/15/2011
            strQuery = "update ae_t set rcode = '', rdet = '',rcode1 = '', rdet1 = '',"+
                                  "rcode2 = '', rdet2 = '',rcode3 = '', rdet3 = '',"+
                                  "m_rcode1 = '', m_rdet1 = '',m_rcode2 = '', m_rdet2 = '',"+
                                  "m_rcode3 = '', m_rdet3 = '',m_rcode4 = '', m_rdet4 = '',"+
                                  "mdfd_dt=sysdate,mdfd_userid='e005072A' "+
                                  "where rqst_sqnc_nmbr='"+values[6]+"' and rqst_vrsn='"+values[5]+"'";

            Log.write("=inserRejectCode AE_RRC_T ae table erase strQuery: " + strQuery);

            i = stmt.executeUpdate(strQuery);

            strQuery = "update AE_T set " + getDynamicQuery(values, "AE");
            Log.write("=inserRejectCode AE_RRC_T strQuery: " + strQuery);
            //fix for fetch out of sequence exception - Antony - 06162010
            Log.write("Check for out of sequence exception here !");
            i = stmt.executeUpdate(strQuery);

            /*while (rs.next()) {
                i++;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    public int manualRejectCodeAE_RRC_T(String[] values)
            throws Exception {
        Log.write("LSRdao manualRejectCodeAE_RRC_T values  " + values);
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String pName = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            strQuery = "update AE_T set " + getDynamicQueryMal(values, "AE");
            Log.write("=manualRejectCodeAE_RRC_T  strQuery: " + strQuery);
            //fix for fetch out of sequence exception - Antony - 06162010
            Log.write("Check for out of sequence exception here !");
            i = stmt.executeUpdate(strQuery);

            /*while (rs.next()) {
                i++;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    public String getDynamicQuery(String[] values, String lrae) {

        String strQuery = " ";
        String strUserID = "";
        boolean checkFlag = false;
        if (values[0].length() > 0) {
            checkFlag = true;
            if (lrae.equals("LR")) {
                strQuery = strQuery + " LR_RCODE='2A',LR_RDET='" + values[0] + "'";
            } else {
                strQuery = strQuery + " RCODE='2A',RDET='" + values[0] + "'";
            }

        }
        if (values[1].length() > 0) {
            if (checkFlag) {
                strQuery = strQuery + ",RCODE1='1T',RDET1='" + values[1] + "'";
            } else {
                strQuery = strQuery + " RCODE1='1T',RDET1='" + values[1] + "'";
                checkFlag = true;
            }

        }
        if (values[2].length() > 0) {
            if (checkFlag) {
                strQuery = strQuery + ",RCODE2='1X',RDET2='" + values[2] + "'";
            } else {
                strQuery = strQuery + " RCODE2='1X',RDET2='" + values[2] + "'";
                checkFlag = true;
            }
        }
        if (values[3].length() > 0) {
            if (checkFlag) {
                strQuery = strQuery + ",RCODE3='1P',RDET3='" + values[3] + "'";
            } else {
                strQuery = strQuery + " RCODE3='1P',RDET3='" + values[3] + "'";
                checkFlag = true;
            }
        }


        if(values[4] == null || values[4].length() == 0)
            strUserID = "e005072A";
        else
            strUserID = values[4];

        /*fix for SPIRA issue #3521  -- reason codes not showing up on AE form*/

        String rqst_vrsn = values[5];

        if(rqst_vrsn.equals("0")) {
            if (checkFlag) {
                strQuery = strQuery + ",MDFD_DT=sysdate,mdfd_userid='" + strUserID + "',rqst_vrsn='" + values[5] +
                        "'";

            } else {
                strQuery = strQuery + " MDFD_DT=sysdate,mdfd_userid='" + strUserID + "',rqst_vrsn='" + values[5] + "'";

            }
            strQuery = strQuery + "  where rqst_sqnc_nmbr='" + values[6] + "'";
        } else {
            if (checkFlag) {
                strQuery = strQuery + ",MDFD_DT=sysdate,mdfd_userid='" + strUserID +
                        "'";

            } else {
                strQuery = strQuery + " MDFD_DT=sysdate,mdfd_userid='" + strUserID + "'";

            }
            strQuery = strQuery + "  where rqst_sqnc_nmbr='" + values[6] + "' and rqst_vrsn='" + values[5] + "'";
        }

        return strQuery;

    }

    public String getDynamicQueryMal(String[] values, String lrae) {

        String strQuery = " ";
        String strUserID = "";
        boolean checkFlag = false;
        if (values[0].length() > 0) {
            checkFlag = true;

            strQuery = strQuery + " M_RCODE1='M0',M_RDET1='" + values[0] + "'";
            Log.write("getDynamicQueryMal1 Method"+ strQuery);

        }
        if (values[1].length() > 0) {
            if (checkFlag) {
                strQuery = strQuery + ",M_RCODE2='M1',M_RDET2='" + values[1] + "'";
                 Log.write("getDynamicQueryMal2 Method"+ strQuery);
            } else {
                strQuery = strQuery + " M_RCODE2='M1',M_RDET2='" + values[1] + "'";
                 Log.write("getDynamicQueryMal3 Method"+ strQuery);
                checkFlag = true;
            }

        }
        if (values[2].length() > 0) {
            if (checkFlag) {
                strQuery = strQuery + ",M_RCODE3='M2',M_RDET3='" + values[2] + "'";
                 Log.write("getDynamicQueryMal4 Method"+ strQuery);
            } else {
                strQuery = strQuery + " M_RCODE3='M2',M_RDET3='" + values[2] + "'";
                Log.write("getDynamicQueryMal5 Method"+ strQuery);
                checkFlag = true;
            }
        }
        if (values[3].length() > 0) {
            if (checkFlag) {
                strQuery = strQuery + ",M_RCODE4='M3',M_RDET4='" + values[3] + "'";
                Log.write("getDynamicQueryMal6 Method"+ strQuery);
            } else {
                strQuery = strQuery + " M_RCODE4='M3',M_RDET4='" + values[3] + "'";
                 Log.write("getDynamicQueryMal7 Method"+ strQuery);
                checkFlag = true;
            }
        }

        if(values[4] == null || values[4].length() == 0)
            strUserID = "e005072A";
        else
            strUserID = values[4];

        /*fix for SPIRA issue #3521  -- reason codes not showing up on AE form*/

        String rqst_vrsn = values[5];

        if(rqst_vrsn.equals("0")) {
            if (checkFlag) {
                strQuery = strQuery + ",MDFD_DT=sysdate,mdfd_userid='" + strUserID + "',rqst_vrsn='" + values[5] +
                        "'";
                Log.write("getDynamicQueryMal Method"+ strQuery);

            } else {
                strQuery = strQuery + " MDFD_DT=sysdate,mdfd_userid='" + strUserID + "',rqst_vrsn='" + values[5] + "'";
                Log.write("getDynamicQueryMal Method"+ strQuery);

            }
            strQuery = strQuery + "  where rqst_sqnc_nmbr='" + values[6] + "'";
            Log.write("getDynamicQueryMal Method"+ strQuery);
        } else {
            if (checkFlag) {
                strQuery = strQuery + ",MDFD_DT=sysdate,mdfd_userid='" + strUserID +
                        "'";
                Log.write("getDynamicQueryMal Method"+ strQuery);

            } else {
                strQuery = strQuery + " MDFD_DT=sysdate,mdfd_userid='" + strUserID + "'";
                Log.write("getDynamicQueryMal Method"+ strQuery);

            }
            strQuery = strQuery + "  where rqst_sqnc_nmbr='" + values[6] + "' and rqst_vrsn='" + values[5] + "'";
        }

        Log.write(" getDynamicQueryMal strQuery: " + strQuery);
        return strQuery;

    }

    public int insertSLATimerQueueTable(LSRDataBean lsrBean, String slatime) {

        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String pName = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            if(slatime.equals("12"))

                /*
                strQuery = "INSERT INTO SLA_TIMER_QUEUE_T values(SLA_TIMER_QUEUE_SEQ.nextval,'" + lsrBean.getReqstNmbr() + "','" + lsrBean.getHistRqstNo() + "','" + lsrBean.getReqstPon() + "','" + lsrBean.getReqstVer() +
                    "','13-JAN-11 08:00 AM','SUBMITTED','4',sysdate,'" + lsrBean.getMdfdUserid() + "')";
                 */


                strQuery = "INSERT INTO SLA_TIMER_QUEUE_T values(SLA_TIMER_QUEUE_SEQ.nextval,'" + lsrBean.getReqstNmbr() + "','" + lsrBean.getHistRqstNo() + "','" + lsrBean.getReqstPon() + "','" + lsrBean.getReqstVer() +
                    "',to_date(sysdate+1||' 08:00:00', 'DD-MON-YY hh24:mi:ss'),'SUBMITTED','4',sysdate,'" + lsrBean.getMdfdUserid() + "')";
            else
            strQuery = "INSERT INTO SLA_TIMER_QUEUE_T values(SLA_TIMER_QUEUE_SEQ.nextval,'" + lsrBean.getReqstNmbr() + "','" + lsrBean.getHistRqstNo() + "','" + lsrBean.getReqstPon() + "','" + lsrBean.getReqstVer() +
                    "',sysdate,'SUBMITTED','" + slatime + "',sysdate,'" + lsrBean.getMdfdUserid() + "')";

            Log.write("insertSLATimerQueueTable strQuery: " + strQuery);
            //fix for fetch out of sequence exception - Antony - 06162010
            Log.write("Check for out of sequence exception here !");
            i = stmt.executeUpdate(strQuery);

            /*while (rs.next()) {
                i++;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    public int updateStatus(String status,
            String rqstNo, String rqstVer, boolean manFlag) {

        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String pName = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "update request_t set INN_STTS='" + status + "' where rqst_sqnc_nmbr='" + rqstNo + "' and rqst_vrsn='" + rqstVer + "'";

            Log.write("request_t strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            // no manual review
            if (manFlag) {
                strQuery = "update SLA_TIMER_QUEUE_T set INTERNAL_STATUS='" + status + "' where rqst_sqnc_nmbr='" + rqstNo + "' and rqst_vrsn='" + rqstVer + "'";
            }

            Log.write("SLA_TIMER_QUEUE_T strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);


            strQuery = "update AUTOMATION_STATUSES_T set INTERNAL_STATUS='" + status + "' where rqst_sqnc_nmbr='" + rqstNo + "' and rqst_vrsn='" + rqstVer + "'";

            Log.write("AUTOMATION_STATUSES_T strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            con.commit();

        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    public int updateEx_IN_Status(String user, LSRDataBean lsrBean,
            boolean rejflag, String aeStr) {
        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String pName = "";
        Log.write("updateEx_IN_Status aeStr " + aeStr);
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            if (rejflag) {
                strQuery = "update request_t set INN_STTS='MANUAL-REVIEW',MDFD_DT=sysdate,mdfd_userid='" + user + "'" +
                        " where rqst_sqnc_nmbr='" + lsrBean.getReqstNmbr() + "'";
                rs = stmt.executeQuery(strQuery);
            }
            Log.write("updateEx_IN_Status request_t strQuery:-- " + strQuery);

            if (aeStr.equals("MR0")) {

                strQuery = "INSERT INTO AUTOMATION_STATUSES_T " +
                        "values(AUTOMATION_STATUS_SEQ.nextval,'" + lsrBean.getReqstNmbr() + "','" + lsrBean.getReqstPon() + "','" + lsrBean.getReqstVer() + "','MANUAL-REVIEW'," +
                        "'SUBMITTED',SYSDATE,'" + user + "','','Internal Error Businessware Down','')";

            } else if (aeStr.equals("VEN") || aeStr.equals("WCN") || aeStr.equals("autoff")) {
                strQuery = "INSERT INTO AUTOMATION_STATUSES_T " +
                        "values(AUTOMATION_STATUS_SEQ.nextval,'" + lsrBean.getReqstNmbr() + "','" + lsrBean.getReqstPon() + "','" + lsrBean.getReqstVer() + "','MANUAL-REVIEW'," +
                        "'SUBMITTED',SYSDATE,'" + user + "','','Request not picked up for automation as not matching Vendor table data','')";
            } else if (aeStr.equals("CKT") || aeStr.equals("REIT")) {
                //PON contains CKT/REIT ASOC - Added a new record with CKT/REIT messege.
                strQuery = "INSERT INTO AUTOMATION_STATUSES_T " +
                        "values(AUTOMATION_STATUS_SEQ.nextval,'" + lsrBean.getReqstNmbr() + "','" + lsrBean.getReqstPon() + "','" + lsrBean.getReqstVer() + "','MANUAL-REVIEW'," +
                        "'SUBMITTED',SYSDATE,'" + user + "','','Request not picked up for automation as the PON matching "+aeStr+" ASOC','')";

            } else {

                strQuery = "update AUTOMATION_STATUSES_T set INTERNAL_STATUS='REJECT_NO_QV',MDFD_DT=sysdate,mdfd_userid='" + user + "'" +
                        " where rqst_sqnc_nmbr='" + lsrBean.getReqstNmbr() + "'";

            }
            Log.write("updateEx_IN_Status AUTOMATION_STATUSES_T strQuery:kk  " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (aeStr.equals("MR1")) {
                strQuery = "update AE_T set M_RDET1='Invalid Customer',M_RCODE1='M1',MDFD_DT=sysdate,mdfd_userid='" + user + "'" +
                        " where rqst_sqnc_nmbr='" + lsrBean.getReqstNmbr() + "'";
            } else if (aeStr.equals("MR0")) {
                strQuery = "update AE_T set M_RDET1='Internal Error Businessware Down',M_RCODE1='M0',MDFD_DT=sysdate,mdfd_userid='" + user + "'" +
                        " where rqst_sqnc_nmbr='" + lsrBean.getReqstNmbr() + "'";
            } else if (aeStr.equals("VEN")) {
                strQuery = "update AE_T set M_RDET1='Request not picked up for automation as not matching Vendor table data for this CAMS WCN " +
                        lsrBean.getWcnCheckFlag() + "',M_RCODE1='M2',MDFD_DT=sysdate,mdfd_userid='" + user + "'" +
                        " where rqst_sqnc_nmbr='" + lsrBean.getReqstNmbr() + "'";
            } else if (aeStr.equals("autoff")) {
                strQuery = "update AE_T set M_RDET1='Request not picked up for automation as not matching Vendor table data',M_RCODE1='M2',MDFD_DT=sysdate,mdfd_userid='" + user + "'" +
                        " where rqst_sqnc_nmbr='" + lsrBean.getReqstNmbr() + "'";
            } else if (aeStr.equals("CKT") || aeStr.equals("REIT")) {
                //PON contains CKT/REIT ASOC - Updated AE_T table with CKT/REIT messege.
                strQuery = "update AE_T set M_RDET1='Request not picked up for automation as the PON matching "+aeStr+" ASOC',M_RCODE1='M2',MDFD_DT=sysdate,mdfd_userid='" + user + "'" +
                        " where rqst_sqnc_nmbr='" + lsrBean.getReqstNmbr() + "'";
            } else {
                return i;
            }

            Log.write("updateEx_IN_Status AE_T strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    public int updateInternalStatus(String reqNO, String reqVer, String user) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        int i = 0;
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            int m_iReqVer = 0;
            if ((reqVer == null) || (reqVer.length() == 0))
	    {}
	    else
	    {
		m_iReqVer = Integer.parseInt(reqVer);
	    }
            if (m_iReqVer == 0){
               strQuery = "select * from request_t where RQST_SQNC_NMBR ='" +
                    reqNO + "' and rqst_stts_cd='SUBMITTED'";
            }else{
            strQuery = "select * from request_t where RQST_SQNC_NMBR ='" +
                    reqNO + "'";
            }
            Log.write("updateInternalStatus strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            String insts = null;
            if (rs.next()) {
                insts = rs.getString("INN_STTS");
            }
            Log.write("updateInternalStatus insts-: " + insts);
            if (insts == null || insts.equals("null")) {
                strQuery = "update request_t set INN_STTS='SUBMITTED',MDFD_DT=sysdate,mdfd_userid='" + user + "'" +
                        " where rqst_sqnc_nmbr='" + reqNO + "'";
                Log.write("updateInternalStatus insts-: " + strQuery);
                i = stmt.executeUpdate(strQuery);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return i;
    }
    
    /*
     * updateCustDBReqTable used update custsag from BW(getCustSag)
     *
     */

    public int updateCustDBReqTable(String reqNO, ValidationDataBean validationDataBean) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String custOrgID = "";
        StringTokenizer paramList;
        String custWCN = "";
        String custTAXJUR = "";

        int i = 0;
        int j = 0;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            custTAXJUR = validationDataBean.getCustTaxJuris();
            custOrgID =  validationDataBean.getCustOrgId();
            paramList = new StringTokenizer(custOrgID,"/");

            while(j < 4) {
                custWCN = paramList.nextToken();
                j++;
            }

            strQuery = "update request_t set CUSTSAG='"+validationDataBean.getCustSag()+
                                          "',CUS_TYP='"+validationDataBean.getCustType() +
                                          "',CUS_WCN='"+custWCN +
                                          "',CUS_TAXJUR='"+custTAXJUR +
                                          "' where RQST_SQNC_NMBR ='" + reqNO + "'";
            Log.write("updateCustDBReqTable strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return i;
    }

    /*
     * updateCIFormVideoTypeField method added by Antony to update videoType value obtained from BW
     * in Video field in the CI_T (CI Form) table for given request number and request version -- 12/19/2013
     *
     */

    public int updateCIFormVideoTypeField(String reqNO, String reqVer, String videoType) {
        Connection con = null;
        Statement stmt = null;
        String strQuery = "";
        int result = 0;
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "update CI_T set ci_video='"+videoType+
                       "' where RQST_SQNC_NMBR =" + reqNO + " and rqst_vrsn="+reqVer;
            Log.write("updateCIFormVideoTypeField strQuery-: " + strQuery);
            result = stmt.executeUpdate(strQuery);
            
        } catch (Exception e) {
            e.printStackTrace();
            result = 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return result;
    }
    
    /*
     * Insert record with SOA transaction ID obtained from SOA in soa_txn_response_t table
     *
     */

    public int updateSOATXNID(String reqNO, String vrsnNo,String soaTxnID,String respRecdDate,
                              String soaRsnCD,String soaRsnString,String soaStatus,String onsp,String atn) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        int i = 0;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "insert into soa_txn_response_t values ('"+
                                                reqNO+"','"+
                                                vrsnNo+"','"+
                                                soaTxnID+"','"+
                                                respRecdDate+"','"+
                                                soaRsnCD+"','"+
                                                soaRsnString+"','"+
                                                soaStatus+"','"+
                                                onsp+"','"+
                                                atn+"','')";

            Log.write("updateSOATXNID strQuery-: " + strQuery);
            i = stmt.executeUpdate(strQuery);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return i;
    }

    /*
     * Update SOA response data for given SOA transaction ID obtained from SOA in soa_txn_response_t table
     *
     */

    public int updateSOATXNResponse(String soaTxnID,String respRecdDate,
                              String soaRsnCD,String soaRsnString,String soaStatus,String onsp) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        int i = 0;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "update soa_txn_response_t set soa_resp_recd_dt='"+respRecdDate+"',"+
                                                     "soa_rsn_cd='"+soaRsnCD+"',"+
                                                     "soa_rsn_string='"+soaRsnString+"',"+
                                                     "soa_status='"+soaStatus+"' "+
                                                     "where soa_txn_id='"+soaTxnID.trim()+"' and "+
                                                           "onsp='"+onsp+"'";

            Log.write("updateSOATXNID strQuery-: " + strQuery);
            i = stmt.executeUpdate(strQuery);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return i;
    }

    /*
     * Retrieve soa txn ID from soa_txn_response_t table for records with empty
     * values of response data
     *
     */

    public List retrieveSOATxnIDs() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        List soaTxnIDList = new ArrayList();
        String strQuery = "";
        String soaTxnID = "";
        String onsp = "";
        int i = 0;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select soa_txn_id,onsp from soa_txn_response_t where soa_txn_id is not null and "+
                                                                       "soa_txn_id <> 'ERR' and "+
                                                                       "soa_resp_recd_dt is null and "+
                                                                       "soa_rsn_cd is null and "+
                                                                       "soa_rsn_string is null and "+
                                                                       "soa_status is null ";

            Log.write("updateSOATXNID strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                soaTxnID = rs.getString("soa_txn_id");
                onsp = rs.getString("onsp");

                Hashtable htSOAID = new Hashtable();

                if (soaTxnID != null && soaTxnID.length() != 0)
                    htSOAID.put("SOA_TXN_ID",soaTxnID);

                if (onsp != null && onsp.length() != 0)
                    htSOAID.put("ONSP",onsp);

                soaTxnIDList.add(htSOAID);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return soaTxnIDList;
    }

    /*
     * Retrieve Simple Port Flag from request_t table
     *
     */

    public String retrieveSPFlag(String reqNo,String reqVer) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String spFlag = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select simple_port_flag from request_t where rqst_sqnc_nmbr='"+reqNo+"' and rqst_vrsn='"+reqVer+"'";

            Log.write("retrieveSPFlag strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                spFlag = rs.getString("simple_port_flag");
            }

            if(spFlag == null)
                spFlag = "";

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return spFlag;
    }

    /*
     * Retrieve WCN from request_t table 
     * New method to retrieve WCN as lsrDataBean.getWCNFlag returns null - Antony
     * 02082013
     *
     */
    
    public String getWCN(String reqNo) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String strWCN = "";
            
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
                 
            strQuery = "select cus_wcn from request_t where rqst_sqnc_nmbr='"+reqNo+"'";
            
            Log.write("getWCN strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            
            while(rs.next()) {
                strWCN = rs.getString("cus_wcn");
            }
            
            if(strWCN == null)
                strWCN = "";
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return strWCN;
    }
    
    
    public String getServiceType(String reqNo) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String serviceType = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select srvc_typ_cd from request_t where rqst_sqnc_nmbr='"+reqNo+"'";

            Log.write("getST strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                serviceType = rs.getString("srvc_typ_cd");
            }

            if(serviceType == null)
                serviceType = "";

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return serviceType;
    }

    public String getActivityType(String reqNo) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String activityType = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select actvty_typ_cd from request_t where rqst_sqnc_nmbr='"+reqNo+"'";

            Log.write("getAT strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                activityType = rs.getString("actvty_typ_cd");
            }

            if(activityType == null)
                activityType = "";

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return activityType;
    }

    public String getSUPPType(String reqNo,String reqVer) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String suppType = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select lsr_sup from lsr_t where rqst_sqnc_nmbr='"+reqNo+"' and rqst_vrsn='"+reqVer+"'";

            Log.write("getSuppType strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                suppType = rs.getString("lsr_sup");
            }

            if(suppType == null)
                suppType = "0";

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return suppType;
    }

    public String getCurrentExtStatus(String reqNo) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String extStatus = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select rqst_stts_cd from request_t where rqst_sqnc_nmbr='"+reqNo+"'";

            Log.write("retrievecurrentstatus strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                extStatus = rs.getString("rqst_stts_cd");
            }

            if(extStatus == null)
                extStatus = "";

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return extStatus;
    }



    /*
     * Retrieve Time of the day limit for given state and timezone
     *
     */

    public String getTODLimit(String state) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String todLimit = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select tod_limit from time_zone_tod_limit_t where stt_cd='"+state+"'";

            Log.write("getTODLimit strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                todLimit = rs.getString("tod_limit");
            }

            if(todLimit == null)
                todLimit = "";

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return todLimit;
    }

    /*
     * dateToString method used for geting Date format in
     * the input of String
     */
    public String dateToString(Date date) {
        //   String DATE_FORMAT = "dd-MM-yyyy";
        Log.write("LSRdao dateToString date  " + date);
        String DATE_FORMAT = "YYYYMMDD hsm";
        //Create object of SimpleDateFormat and pass the desired date format.
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        Log.write("Today is " + sdf.format(date));
        return sdf.format(date);
    }

    public Date stringTodate(String strDate) throws Exception {
        Log.write("LSRdao stringTodate strDate  " + strDate);
        String DATE_FORMAT = "yyyy/MM/DD h/s/m";
        //Create object of SimpleDateFormat and pass the desired date format.
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        Log.write("Today is " + sdf.parse(strDate));
        return sdf.parse(strDate);
    }

    public String trimAddress(String strAddr[]) {
        String trimAddr = "";
        if (strAddr[0] != null) {
            trimAddr = trimAddr + strAddr[0].replaceAll(" ", "");
        }
        if (strAddr[1] != null) {
            trimAddr = trimAddr + strAddr[1].replaceAll(" ", "");
        }
        if (strAddr[2] != null) {
            trimAddr = trimAddr + strAddr[2].replaceAll(" ", "");
        }
        if (strAddr[3] != null) {
            trimAddr = trimAddr + strAddr[3].replaceAll(" ", "");
        }
        if (strAddr[4] != null) {
            trimAddr = trimAddr + strAddr[4].replaceAll(" ", "");
        }
        if (strAddr[5] != null) {
            trimAddr = trimAddr + strAddr[5].replaceAll(" ", "");
        }
        if (strAddr[6] != null) {
            trimAddr = trimAddr + strAddr[6].replaceAll(" ", "");
        }
        if (strAddr[7] != null) {
            trimAddr = trimAddr + strAddr[7].replaceAll(" ", "");
        }
        if (strAddr[8] != null) {
            trimAddr = trimAddr + strAddr[8].replaceAll(" ", "");
        }
        if (strAddr[9] != null) {
            trimAddr = trimAddr + strAddr[9].replaceAll(" ", "");
        }
        if (strAddr[10] != null) {
            trimAddr = trimAddr + strAddr[10].replaceAll(" ", "");
        }
        if (strAddr[11] != null) {
            trimAddr = trimAddr + strAddr[11].replaceAll(" ", "");
        }
        if (strAddr[12] != null) {
            trimAddr = trimAddr + strAddr[12].replaceAll(" ", "");
        }
        Log.write(" Tirm Address for EU " + trimAddr);
        Log.write(" Tirm Address for EU " + trimAddr);
        return trimAddr;
    }


    public String getStreetAddress(String strAddr[]) {
        String trimAddr = "";
        if (strAddr[0] != null) {
            trimAddr = trimAddr +" "+ strAddr[0];
        }
        if (strAddr[1] != null) {
            trimAddr = trimAddr +" "+ strAddr[1];
        }
        if (strAddr[2] != null) {
            trimAddr = trimAddr +" "+ strAddr[2];
        }
        if (strAddr[3] != null) {
            trimAddr = trimAddr +" "+ strAddr[3];
        }
        if (strAddr[4] != null) {
            trimAddr = trimAddr +" "+ strAddr[4];
        }
        if (strAddr[5] != null) {
            trimAddr = trimAddr +" "+ strAddr[5];
        }
        if (strAddr[6] != null) {
            trimAddr = trimAddr +" "+ strAddr[6];
        }
        if (strAddr[7] != null) {
            trimAddr = trimAddr +" "+ strAddr[7];
        }
        if (strAddr[8] != null) {
            trimAddr = trimAddr +" "+ strAddr[8];
        }
        if (strAddr[9] != null) {
            trimAddr = trimAddr +" "+ strAddr[9];
        }
        if (strAddr[10] != null) {
            trimAddr = trimAddr +" "+ strAddr[10];
        }
        if (strAddr[11] != null) {
            trimAddr = trimAddr +" "+ strAddr[11];
        }
        if (strAddr[12] != null) {
            trimAddr = trimAddr +" "+ strAddr[12];
        }
        Log.write(" Street Address from EU LA fields: " + trimAddr);
        return trimAddr;
    }

    public String getMsagStreetAddress(String strAddr[]) {
        String trimAddr = "";
        if (strAddr[0] != null) {
            trimAddr = trimAddr +" "+ strAddr[0];
        }
        if (strAddr[1] != null) {
            trimAddr = trimAddr +" "+ strAddr[1];
        }
        if (strAddr[2] != null) {
            trimAddr = trimAddr +" "+ strAddr[2];
        }
        /* // pre dir not required for MSAG validation in street field
        if (strAddr[3] != null) {
            trimAddr = trimAddr +" "+ strAddr[3];
        }
         */
        if (strAddr[4] != null) {
            trimAddr = trimAddr +" "+ strAddr[4];
        }
        if (strAddr[5] != null) {
            trimAddr = trimAddr +" "+ strAddr[5];
        }
        if (strAddr[6] != null) {
            trimAddr = trimAddr +" "+ strAddr[6];
        }

        /*ld,lv field pairs not needed for MSAG validation - fix for bug 296
        if (strAddr[7] != null) {
            trimAddr = trimAddr +" "+ strAddr[7];
        }
        if (strAddr[8] != null) {
            trimAddr = trimAddr +" "+ strAddr[8];
        }
        if (strAddr[9] != null) {
            trimAddr = trimAddr +" "+ strAddr[9];
        }
        if (strAddr[10] != null) {
            trimAddr = trimAddr +" "+ strAddr[10];
        }
        if (strAddr[11] != null) {
            trimAddr = trimAddr +" "+ strAddr[11];
        }
        if (strAddr[12] != null) {
            trimAddr = trimAddr +" "+ strAddr[12];
        }
        */


        Log.write(" Street Address from EU LA fields: " + trimAddr);
        return trimAddr;
    }

    public Hashtable retrieveDCRISOrderParams(String atn, String onsp,String reqNo,String reqVer) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        Hashtable dcrisOrderParams = new Hashtable();
        String strQuery = "";
        String rqstSqncNmbr = "";
        String rqstVersion = "";
        String companyType = "";
        String ocn = "";
        String rqstPON = "";
        String spFlag = "";
        String orderNumber = "";
        String focDDD = "";
        String [] boidBexArray;
        boolean noOrderFound = true;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select s.rqst_sqnc_nmbr,s.rqst_vrsn,r.cmpny_sqnc_nmbr,c.cmpny_typ,r.ocn_cd,"+
                       "r.rqst_pon,r.simple_port_flag,l.lr_ord,s.dcris_dd lr_dd "+
                       "from soa_txn_response_t s,request_t r,lr_t l,company_t c where "+
                       "s.atn='"+atn+"' and s.onsp='"+onsp+"' and s.soa_status='pending' and "+
                       "s.rqst_sqnc_nmbr = r.rqst_sqnc_nmbr and "+
                       "s.rqst_sqnc_nmbr = l.rqst_sqnc_nmbr and s.rqst_vrsn = l.rqst_vrsn and "+
                       "r.cmpny_sqnc_nmbr = c.cmpny_sqnc_nmbr and l.RQST_SQNC_NMBR='"+reqNo+"' and l.RQST_VRSN='"+reqVer+"'";

            Log.write("retrieveDCRISCallDataforSOA strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            while(rs.next()) {
                noOrderFound = false;

                rqstSqncNmbr = rs.getString("rqst_sqnc_nmbr");
                rqstVersion = rs.getString("rqst_vrsn");
                companyType = rs.getString("cmpny_typ");
                ocn = rs.getString("ocn_cd");
                rqstPON = rs.getString("rqst_pon");
                spFlag = rs.getString("simple_port_flag");
                orderNumber = rs.getString("lr_ord");
                focDDD = rs.getString("lr_dd");

                if(orderNumber != null) {
                    dcrisOrderParams.put("ORDER_NO",orderNumber);
                }
                //send whole order number and split into individual orders in send order method
                /*
                    boidBexArray = orderNumber.split(" ");
                    dcrisOrderParams.put("BEX",boidBexArray[0]);
                    dcrisOrderParams.put("ORDER_NO",boidBexArray[1]);
                    dcrisOrderParams.put("BOID",boidBexArray[2]);
                } else {
                    dcrisOrderParams.put("BEX","");
                    dcrisOrderParams.put("ORDER_NO","");
                    dcrisOrderParams.put("BOID","");
                }
                */

                if(companyType != null && companyType.equals("C"))
                    dcrisOrderParams.put("CLEC_IND","Y");
                else
                    dcrisOrderParams.put("CLEC_IND","N");

                if(ocn != null)
                    dcrisOrderParams.put("OCN_CD",ocn);
                else
                    dcrisOrderParams.put("OCN_CD","");

                if(rqstPON != null)
                    dcrisOrderParams.put("RQST_PON",rqstPON);
                else
                    dcrisOrderParams.put("RQST_PON","");

                if(spFlag != null)
                    dcrisOrderParams.put("SP_FLAG",spFlag);
                else
                    dcrisOrderParams.put("SP_FLAG","");

                if (rqstSqncNmbr != null && rqstSqncNmbr.length() != 0)
                    dcrisOrderParams.put("RQST_SQNC_NMBR",rqstSqncNmbr);

                if (rqstVersion != null && rqstVersion.length() != 0)
                    dcrisOrderParams.put("RQST_VRSN",rqstVersion);

                if (focDDD != null && focDDD.length() != 0)
                    dcrisOrderParams.put("FOC_DDD",focDDD);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        if(noOrderFound) {
            Log.write("No order found for previous version of PON with ATN : "+atn);
            return null;
        }

        return dcrisOrderParams;
    }

        /**
     *This will validate the FOC DD is after the submitted date or not. i.e
     * Get the foc dd from LR table by mapping with soa tx table and check foc dd is after the
     * current date or not.if it is after then this method returns true else false.
     * @param lSRDataBean
     * @return true or false.
     */
    public String getFOCDDD(String reqNo,String reqVer) {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String focdueDate = null;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select lr_dd||' 21:00:00' foc_ddd from lr_t where rqst_sqnc_nmbr="+reqNo+" and rqst_vrsn="+reqVer;
            Log.write("getFOCDDforSOA strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                if (rs.getString("foc_ddd") != null) {

                    Log.write("date value from db :"+rs.getString("foc_ddd"));

                    //focdueDate = dateToString(rs.getString("LR_DD")+" 21:00:00", "MM-dd-yyyy hh24:mi:ss");
                    focdueDate = rs.getString("foc_ddd");
                    Log.write("focduedate :"+focdueDate);
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return focdueDate;
    }

     /**
     * Date:26/04/2011-V7 Author: Satish Talluri
     * This method will update the SLATimerque table for the supplemental requests.
     * This method will update the Internal Status to canceled for all type of requests
     * i.e SUP1,SUP2 and SUP3
     * @param lsrBean
     * @return integer
     */
    //public int updateSLATimerQueueforSUP(LSRDataBean lsrBean) {
    public int updateSLATimerQueueforSUP(String reqNo,String reqVer) {

        int i = 0;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";

        String strVersion = reqVer;
        int reqVersion = Integer.parseInt(strVersion);

        reqVersion = reqVersion - 1;

        strVersion = String.valueOf(reqVersion);

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            strQuery = "UPDATE SLA_TIMER_QUEUE_T SET INTERNAL_STATUS='CANCELLED',"
                    + "MDFD_USERID='" + "e005072a" + "',MDFD_DT =sysdate "
                    + "where rqst_sqnc_nmbr='" + reqNo + "' AND RQST_VRSN='" + strVersion + "'";
            Log.write("updateSLATimerQueueforSUP strQuery: " + strQuery);
            i = stmt.executeUpdate(strQuery);

        } catch (Exception e) {
            e.printStackTrace();
            Log.write("Unable to update previous version of PON in SLA queue to CANCELLED!");
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return i;
    }

    /* New method added for updating CANCELLED status when the PON fires --  Antony -- 07/17/2011*/

    public String callAutomationStatusUpdateProc(String reqNo, String reqVrsn) throws Exception {
        Log.write("Calling LSRdao:callAutomationStatusUpdateProc");
        Connection con = null;
        CallableStatement stmt = null;
        String result = "ERROR";
        String strQuery = "{ call AUTOMATION_STATUS_PROC(?,?,?) }";

        try {
            con = DatabaseManager.getConnection();
            Log.write(" Automation status update " + con + "strQuery " + strQuery);
            stmt = con.prepareCall(strQuery);
            stmt.setString(1, reqNo);
            stmt.setString(2, reqVrsn);
            stmt.registerOutParameter(3, java.sql.Types.INTEGER);
            stmt.execute();

            result = String.valueOf(stmt.getInt(3));

            Log.write("Value of string returned by automation status stored procedure:" + result);

        } catch (Exception e) {
            e.printStackTrace();
            result = "ERROR";
            ExceptionHandler.handleException("Exception 11: ", e);
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return result;
    }

    /* New method added for updating CANCELLED status for a partial SUPP1 PON --  Antony -- 02/02/2012*/

    public String updateSUPP1PartialToCancelled(String reqNo, String reqVrsn) throws Exception {
        Log.write("Calling LSRdao:callAutomationStatusUpdateProc");
        Connection con = null;
        CallableStatement stmt = null;
        String result = "ERROR";
        String strQuery = "{ call SP_SUPP1_PARTIAL_CANCEL(?,?,?) }";

        try {
            con = DatabaseManager.getConnection();
            Log.write(" Automation status update " + con + "strQuery " + strQuery);
            stmt = con.prepareCall(strQuery);
            stmt.setString(1, reqNo);
            stmt.setString(2, reqVrsn);
            stmt.registerOutParameter(3, java.sql.Types.INTEGER);
            stmt.execute();

            result = String.valueOf(stmt.getInt(3));

            Log.write("Value of string returned by automation status stored procedure:" + result);

        } catch (Exception e) {
            e.printStackTrace();
            result = "ERROR";
            ExceptionHandler.handleException("Exception 11: ", e);
        } finally {
            DatabaseManager.releaseConnection(con);
        }

        return result;
    }

    /*method to retrieve ONSP for SOA port-out sent for given TN and req no -- Antony -- 07/26/2011*/

    public String getONSP(String reqNo,String atn) {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String onspInSV = null;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select onsp from soa_txn_response_t where rqst_sqnc_nmbr="+reqNo+" and atn='"+atn+"'";
            Log.write("getONSP strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                if (rs.getString("onsp") != null) {

                    Log.write("onsp value from db :"+rs.getString("onsp"));

                    //focdueDate = dateToString(rs.getString("LR_DD")+" 21:00:00", "MM-dd-yyyy hh24:mi:ss");
                    onspInSV = rs.getString("onsp");
                    Log.write("ONSP in SV :"+onspInSV);
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return onspInSV;
    }

    public String getFOCStatus(String reqNo,String reqVer) {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String focStatus = null;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select distinct rqst_stts_cd_in from request_history_t "
                    + "where rqst_sqnc_nmbr="+reqNo+" and rqst_vrsn<"+reqVer+" and rqst_stts_cd_in='FOC'";
            Log.write("getFOCStatus strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                if (rs.getString("rqst_stts_cd_in") != null) {

                    Log.write("FOC Status From DB :"+rs.getString("rqst_stts_cd_in"));

                    focStatus = rs.getString("rqst_stts_cd_in");
                    Log.write("focStatus :"+focStatus);
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return focStatus;
    }

    /*method to check if SOA SV exists for previous version of PON -- Antony -- 08/12/2011*/

    public boolean checkSVExistsInSOA(String reqNo,String reqVer) {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        boolean svExists = false;

        String strVersion = "";

        if(!reqVer.equals("0")) {
            strVersion = reqVer;
            int reqVersion = Integer.parseInt(strVersion);

            reqVersion = reqVersion - 1;

            reqVer = String.valueOf(reqVersion);
        } else {
            Log.write("SV check cannot be done in SOA for previous version for req no:"+reqNo+" version:"+reqVer);
            return false;
        }

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select * from soa_txn_response_t where soa_status='pending' and rqst_sqnc_nmbr='"+reqNo+"' and rqst_vrsn = '"+reqVer+"'";
            Log.write("checkSVExists strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                svExists = true;
                Log.write("SV exists in SOA for previous version for req no:"+reqNo+" version:"+reqVer);
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return svExists;
    }
    
    /*method to check if any Previous SOA Transactions exists for the PON  -- Saravanan - 10/30/2018 */

    public boolean checkPrevSoaTxn (String reqNo) {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        boolean soaTransactionExists = false;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select * from soa_txn_response_t where  rqst_sqnc_nmbr='"+reqNo+"'";
            Log.write("checkPrevSoaTxn strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
            	soaTransactionExists = true;
                Log.write("Soa Transactions Exist for req no:"+reqNo);
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return soaTransactionExists;
    }
    
    
     /*method to check if any Previous version is FOCed manually*/

     public boolean checkPrevVerNotAuto (String reqNo,String reqVer) {

    	Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        boolean PrevVerNotAuto = false;

        String strVersion = "";

        if(!reqVer.equals("0")) {
            strVersion = reqVer;
            int reqVersion = Integer.parseInt(strVersion);

            reqVersion = reqVersion - 1;

            reqVer = String.valueOf(reqVersion);
        }
        else {
            Log.write("Previous Version cannot be checked "+reqNo+" version:"+reqVer);
            return false;
        }

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select * from request_history_t where RQST_SQNC_NMBR= '"+reqNo+"' and RQST_STTS_CD_OUT ='FOC' and MDFD_USERID != 'e005072A'";
            Log.write("checkPrevVerNotAuto strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
            	PrevVerNotAuto = true;
                Log.write("Previous Version of this Pon is not submitted through automation -"+reqNo );
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return PrevVerNotAuto;
    }
     
     
     

    /*method to delete records from NP_SD_T table for which the TNs were not activated in SOA - Antony - 01/26/2012*/

    public void deleteNPSectionForPortedNbr(String reqNo, String reqVer,String strAtn) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "delete from np_sd_t where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + reqVer + "' and np_sd_portednbr='"+strAtn+"'";
            Log.write("delete from np table strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            
            strQuery = "delete from lr_cd_t where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + reqVer + "' and lr_cd_portednbr='"+strAtn+"'";
            Log.write("delete from lr_cd table strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    }

    //PON is a partial port.
    /*method to check if this is a partial port PON by checking AE table for error message -- Antony -- 02/02/2011*/

    public boolean checkAEForPartial(String reqNo,String reqVer) {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        boolean ponIsPartial = false;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            strQuery = "select * from ae_t where m_rdet4='PON is a partial port.' and rqst_sqnc_nmbr='"+reqNo+"' and rqst_vrsn = '"+reqVer+"'";
            Log.write("checkAEForPartial strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                ponIsPartial = true;
                Log.write("This PON is a partial for req no:"+reqNo+" version:"+reqVer);
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return ponIsPartial;
    }

    public String getATN(String reqNo,String reqVer,String SrvcTypCd,String ActvtyTypCd) {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";
        String atn = null;

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

            if(SrvcTypCd.equals("C") && ActvtyTypCd.equals("V")){

            strQuery = "select replace(nvl(t1.lsr_atn, t2.np_sd_portednbr),'-','') atn from lsr_t t1, np_sd_t t2 "
                    + "where t1.rqst_sqnc_nmbr = t2.rqst_sqnc_nmbr and t1.rqst_vrsn = t2.rqst_vrsn "
                    + "and t1.rqst_sqnc_nmbr="+reqNo+" and t1.rqst_vrsn="+reqVer;

		    }else if(SrvcTypCd.equals("G") || SrvcTypCd.equals("H") || SrvcTypCd.equals("J")){

			   strQuery = "select replace(dsr_atn,'-','') atn from dsr_t "
			           + "where rqst_sqnc_nmbr="+reqNo+" and rqst_vrsn="+reqVer;

            }else{

			   strQuery = "select replace(lsr_atn,'-','') atn from lsr_t "
			           + "where rqst_sqnc_nmbr="+reqNo+" and rqst_vrsn="+reqVer;
		    }

            Log.write("getATN strQuery: " + strQuery);
            rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                if (rs.getString("atn") != null) {

                    Log.write("ATN From DB :"+rs.getString("atn"));

                    atn = rs.getString("atn");
                    Log.write("atn :"+atn);
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return atn;
    }

    //Updated the query due to Eliminate nuvox_tn_t project.
    public String checkNuvoxTNStatus(String tn) {
        Connection connCAMS = null;
        Statement stmtCAMS = null;
        
        ResultSet rs;
        String strQuery = "";
        // Non-Windstream customer, returning value to update PON as ICARE
        String sent_tn = "Non-Windstream";

        try {
            connCAMS = DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
            stmtCAMS = connCAMS.createStatement();
            //con = DatabaseManager.getConnection();
            //stmt = con.createStatement();
            strQuery = "select sent_phone from kash.cams_sentt t where sent_phone='"+tn+"'";

            Log.write("checkNuvoxTN strQuery: " + strQuery);
            rs = stmtCAMS.executeQuery(strQuery);
          
                        Log.write("RS value!!!!!!!!!!!!" + rs);
            if(rs.next()){
              
                Log.write("Result set value" +rs.getString("sent_phone"));
            if(rs.getString("sent_phone") != null){
                // Windstream customer, returning null to not update PON as ICARE
                
                sent_tn = null;
                
                Log.write("Sent tn value..........." + sent_tn);
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.write("Exception returned"+e);
        } finally {
            DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
        }
        return sent_tn;
    }
    
    //Check the TN is Active(Connected) or InActive(Disconnected) in Windstream.
    public String checkTNStatus(String tn) {
        Connection connCAMS = null;
        Statement stmtCAMS = null;
        
        ResultSet rs;
        String strQuery = "";
        String sent_discdate = null;

        try {
            connCAMS = DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
            stmtCAMS = connCAMS.createStatement();
           
            strQuery = "select sent_dcnct_date from kash.cams_sentt t where sent_phone='"+tn+"' order by sent_insrv_date desc";

            Log.write("checkTNStatus strQuery: " + strQuery);
            rs = stmtCAMS.executeQuery(strQuery);
          
                        Log.write("RS value!!!!!!!!!!!!" + rs);
            if(rs.next()){
              
                Log.write("Result set value" +rs.getString("sent_dcnct_date"));
            if(rs.getString("sent_dcnct_date") != null){
                
            	sent_discdate = "InActive and Disconnected";
                
                Log.write("Sent tn value..........." + sent_discdate);
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.write("Exception returned"+e);
        } finally {
            DatabaseManager.releaseConnection(connCAMS, DatabaseManager.CAMSP_CONNECTION);
        }
        return sent_discdate;
    }

     public void updateNuvox(String reqNo, String reqVer, String sent_tn) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

               strQuery = "update request_t set INN_STTS = 'MANUAL-REVIEW', ICARE ='Y' where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + reqVer + "'";

            Log.write("updateReqTable for Nuvox TN strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

               strQuery = "update ae_t set m_rcode1='M0', m_rdet1='The TN is a: " + sent_tn + "' where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + reqVer + "'";

            Log.write("updateAETable for M_RDET1 strQuery-: " + strQuery);
            rs = stmt.executeQuery(strQuery);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    }

     public void clearLRFormForAllPendingInSOA(String reqNo, String reqVer) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs;
        String strQuery = "";

        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            
            strQuery = "UPDATE lr_t SET lr_ord = '',lr_rcode = '',lr_dd = '',lr_type1 = '',lr_rdet = '',rcode1 = '',rdet1 = '',rcode2 = '',rdet2 = '',"+
                        "rcode3 = '',rdet3 = '',mdfd_dt = SYSDATE,mdfd_userid = 'e005072A' where RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + reqVer + "'";

            Log.write("update LR table for all pending TNs in SOA: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            strQuery = "   UPDATE lr_cd_t SET lr_cd_portednbr = '' where FRM_SCTN_OCC = '1' and RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + reqVer + "'";

            Log.write("Clear up LR_CD_T table for all pending TNs in SOA: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            strQuery = "   delete from lr_cd_t where FRM_SCTN_OCC > 1 and RQST_SQNC_NMBR ='" +reqNo + "' and RQST_VRSN ='" + reqVer + "'";

            Log.write("Clear up LR_CD_T table for all pending TNs in SOA: " + strQuery);
            rs = stmt.executeQuery(strQuery);

            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }

    }

      //Update submitted date in request_t table - Vijay - 04-06-12
    public int updateSubmittedDate(String reqNO, String user) {
        Connection con = null;
        Statement stmt = null;
        String strQuery = "";
        int i = 0;
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();

                strQuery = "update request_t set RQST_SUBMIT_DATE=sysdate,MDFD_DT=sysdate,mdfd_userid='" + user + "'" +
                        " where rqst_sqnc_nmbr='" + reqNO + "'";
                Log.write("updateInternalStatus insts-: " + strQuery);
                i = stmt.executeUpdate(strQuery);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        return i;
    }
    
    /*
     * loadCommonAddrsStrtTypes method used for loading all the
     * Common Service Address Street Types from DataBase and 
     * to storing Map object
     */
    public Map<String, List<String>> loadCommonAddrsStrtTypes(){
        Log.write("LSRdao loadCommonAddrsStrtTypes calling ** ");
        Map<String, List<String>> streetAddrsMap = new HashMap<String, List<String>>();
        Connection con = null;
        Statement stmt = null;
        String key = null;
        List<String> dataList = null;
        ResultSet rs;
        String strQuery = "select * from srv_address_street_types_t order by primary_street_sufx";
        
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);

            while (rs.next()) {
                String primaryStreetSufx = rs.getString("PRIMARY_STREET_SUFX");
		String commonStreetSufxAbbr = rs.getString("COMMON_STREET_SUFX_ABBR");
                
            	if (key == null || !key.equalsIgnoreCase(primaryStreetSufx)) {
                    dataList = new ArrayList<String>();
                    dataList.add(commonStreetSufxAbbr);
		} else {
                    dataList.add(commonStreetSufxAbbr);
		}
		streetAddrsMap.put(commonStreetSufxAbbr.toUpperCase(), dataList);
		key = primaryStreetSufx;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.write(Log.ERROR, "LSRdao.loadCommonAddrsStrtTypes() : DB Exception on Query : " + strQuery);
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return streetAddrsMap;
    }
    
    // update the tables when the previous PON version is not submitted by auto id
	public void updateStatusMR( String reqstPon,String reqstNum, String reqstVer, String user) {
		
	        Connection con = null;
	        Statement stmt = null;
	        ResultSet rs;
	        String strQuery = "";
	        
	         int rqst_vsrn =  Integer.parseInt(reqstVer);

	        try {
	            con = DatabaseManager.getConnection();
	            stmt = con.createStatement();
	            
	            strQuery = "update ae_t set m_rcode4 = 'MR', m_rdet4 = 'PREVIOUS_VERSION_FOCed_MANUALLY', mdfd_dt = sysdate,mdfd_userid='" + user + "' where rqst_sqnc_nmbr = '"+ reqstNum +"' and rqst_vrsn='"+ rqst_vsrn +"'" ;       
	            
	            stmt.executeUpdate(strQuery);
	            
	            strQuery = "update request_t set inn_stts = 'MANUAL-REVIEW', mdfd_dt = sysdate,mdfd_userid='" + user + "' where rqst_sqnc_nmbr ='"+ reqstNum +"' and rqst_vrsn = '"+ rqst_vsrn +"'";
	            
	            stmt.executeUpdate(strQuery);
                    
                     strQuery = "INSERT INTO AUTOMATION_STATUSES_T " +
                        "values(AUTOMATION_STATUS_SEQ.nextval,'" + reqstNum + "','" + reqstPon + "','" + rqst_vsrn + "','MANUAL-REVIEW'," +
                        "'SUBMITTED',SYSDATE,'" + user + "','','','')";
	            
	            stmt.executeUpdate(strQuery);
                    
//	            strQuery = "update AUTOMATION_STATUSES_T set INTERNAL_STATUS='MANUAL-REVIEW',MDFD_DT=sysdate,mdfd_userid='" + user + "' where rqst_sqnc_nmbr = '"+ reqstNum +"' and rqst_vrsn = '"+ rqst_vsrn +"'"; 
//	            stmt.executeUpdate(strQuery);
//	            
//	            strQuery = "update sla_timer_queue_t set rqst_vrsn='"+ reqstVer +"',internal_status = 'MANUAL-REVIEW', mdfd_dt = sysdate, mdfd_userid = '"+ user +"' where rqst_sqnc_nmbr = '"+ reqstNum +"'";  
//	            rs = stmt.executeQuery(strQuery);
//	            
                    Log.write(Log.ERROR, "Manual Review status updated in tables when any previous version is FOCed manually");
	        } catch (Exception e) {
	            e.printStackTrace();
	            Log.write(Log.ERROR, "DB Exception on Query : " + strQuery);
	        } finally {
	            DatabaseManager.releaseConnection(con);
	        }
		
	}
}
