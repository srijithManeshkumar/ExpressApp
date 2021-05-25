

package com.alltel.lsr.common.objects;

import com.alltel.lsr.common.error.objects.ExceptionHandler;
import java.util.*;
import java.sql.*;
import com.alltel.lsr.common.util.*;

public class VendorBean extends AlltelCtlrBean {
    
    final private String m_strTableName = "VENDOR_TABLE_CONFIG_T";
    
    
    private String strCompSqncNumber="";
    private String strStateCode ="";
    private String strOCN ="";
    //private String strCompanyType ="";
    private String strBTN ="";
    private String strWCN ="";
    private String isEmbargoed ="";
    private String strTXJUR ="";
    private String strServiceType="";
    private String strActivityType="";
    private String isDirectory="";
    private String isEligibleToDeleteDir="";
    
    private String validTimeOfDayDDD = "";
    private String dueDateLowerLimit= "";
    private String dueDateUpperLimit= "";
    private String sLAWaitTime=  "";
    
    private String vedorAutomateFlag=  "";
    private String ocnAutomateFlag=  "";
    private String stateAutomateFlag=  "";
    private String srvtypeAutomateFlag=  "";
    private String acttypeAutomateFlag=  "";
    
    private String contactNo;
    
    private String strBTNSPANStart = " ";
    private String strBTNSPANEnd = " ";
    
    private String strWCNSPANStart = " ";
    private String strWCNSPANEnd = " ";
    
    private String strTXJURSPANStart = " ";
    private String strTXJURSPANEnd = " ";
    
    public String getBTNSPANStart() { return strBTNSPANStart; }
    public String getBTNSPANEnd() { return strBTNSPANEnd; }
    
    public String getWCNSPANStart() { return strWCNSPANStart; }
    public String getWCNSPANEnd() { return strWCNSPANEnd; }
    
    public String getTXJURSPANStart() { return strTXJURSPANStart; }
    public String getTXJURSPANEnd() { return strTXJURSPANEnd; }
    
    private int    m_iLgnAttmpts = 0;
    private String m_strLstLgnDt = " ";
    
    public VendorBean() {
        setSecurityTags(getM_strTableName());
    }
    
    public int    getLgnAttmpts() { return m_iLgnAttmpts; }
    public String getLstLgnDt() { return m_strLstLgnDt; }
    
    public void setLgnAttmpts(int iAttempt) { this.m_iLgnAttmpts = iAttempt; }
    public void setLstLgnDt() { this.m_strLstLgnDt = Toolkit.getDateTime(); }
    
    public int deleteVendorBeanFromDB() {
        Connection con = null;
        Statement stmt = null;
        int a=0;
        // Get DB Connection
        try {
            // Build DELETE SQL statement
            String strQuery = "DELETE VENDOR_TABLE_CONFIG_T WHERE Vendor_Config_sqnc_nmbr = '" + Toolkit.replaceSingleQwithDoubleQ(strVendorConfigSqncNumber)
            + "'";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            Log.write("deleteVendorBeanFromDB strQuery "+strQuery);
            a = stmt.executeUpdate(strQuery);
        } catch(SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public int retrieveVendorBeanFromDB(String configNumber) {
        System.out.println("==retrieveVendorBeanFromDB=");
        Log.write(Log.DEBUG_VERBOSE, "retrieveVendorBeanFromDB kk");
        // Clear out beans prior contents...
        Log.write(Log.DEBUG, "VendorBean() retrieve() user = " + configNumber);
        setStrVendorConfigSqncNumber(configNumber);
        setStrCompSqncNumber("");
        setStrStateCode("");
        setStrOCN("");
        setStrBTN("");
        setStrWCN("");
        setIsEmbargoed("");
        setStrTXJUR("");
        setStrServiceType("");
        setStrActivityType("");
        setIsDirectory("");
        setIsEligibleToDeleteDir("");
        setValidTimeOfDayDDD("");
        setDueDateLowerLimit("");
        setDueDateUpperLimit("");
        setSLAWaitTime("");
        return retrieveVendorBeanFromDB();
    }
    
    public int retrieveVendorBeanFromDB() {
        Log.write("retrieveVendorBeanFromDB inside kk");
        Connection con = null;
        Statement stmt = null;
        
        // Get DB Connection
        try {
            String strQuery = "SELECT * from VENDOR_TABLE_CONFIG_T " +
                    " where VENDOR_CONFIG_SQNC_NMBR='"+strVendorConfigSqncNumber+"'";
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            Log.write(Log.DEBUG_VERBOSE, "LPB() rs kk"+rs);
            
            if (rs.next()) {
                System.out.println(" retrieve VendorBeanFromDB ");
                Log.write(Log.DEBUG, " VendorBean() if got user ");
                this.strCompSqncNumber = rs.getString("CMPNY_SQNC_NMBR");
                this.strStateCode = rs.getString("STT_CD");
                this.strOCN = rs.getString("OCN_CD");
                this.strBTN = rs.getString("BTN");
                this.strWCN = rs.getString("WCN");
                this.isEmbargoed = rs.getString("IS_EMBARGOED");
                this.strTXJUR = rs.getString("TaxExemptions");
                this.strServiceType = rs.getString("SRVC_TYP_CD");
                
                this.strActivityType = rs.getString("ACTVTY_TYP_CD");
                this.isDirectory = rs.getString("IS_DIRECTORY");
                this.isEligibleToDeleteDir = rs.getString("IS_ELIGIBLE_TO_DIR_DELETE");
                
                this.setValidTimeOfDayDDD(rs.getString("VALID_TIME_OF_DAY_FOR_DDD"));
                this.setDueDateLowerLimit(rs.getString("DDD_INTERVAL_LOWER_LIMIT"));
                this.setDueDateUpperLimit(rs.getString("DDD_INTERVAL_UPPER_LIMIT"));
                this.setSLAWaitTime(rs.getString("SLA_WAIT_TIME"));
                
                this.contactNo=rs.getString("CONTACTNUMBER");
                this.m_strMdfdDt=rs.getString("MDFD_DT");
                this.m_strMdfdUserid = rs.getString("MDFD_USERID");
            }else {
                System.out.println("==retrieveVendorBeanFromDB= else");
                Log.write(Log.DEBUG, "VendorBean() else user ");
                return 1;
            }
        } catch(SQLException sqle) {
            
            Log.write(1,sqle.toString());
            sqle.printStackTrace();
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            Log.write(1,e.toString());
            e.printStackTrace();
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public int updateVendorBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        String strQuery = null;
        
        // Get DB Connection
        try {
            // Build UPDATE SQL statement
            // int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);
            
            
            strQuery = "UPDATE VENDOR_TABLE_CONFIG_T SET stt_cd = '" + Toolkit.replaceSingleQwithDoubleQ(strStateCode) + "', ocn_cd = '" +
                    Toolkit.replaceSingleQwithDoubleQ(strOCN) + "', btn = '" +
                    Toolkit.replaceSingleQwithDoubleQ(strBTN) + "', " +
                    "wcn='" + strWCN + "', is_embargoed='" + isEmbargoed +
                    "', TaxExemptions='" + strTXJUR + "' " +", srvc_typ_cd='" + strServiceType + "' " +
                    ", actvty_typ_cd='" + strActivityType + "'" +", is_directory='" + isDirectory +"',IS_ELIGIBLE_TO_DIR_DELETE='"+
                    isEligibleToDeleteDir+  "',VALID_TIME_OF_DAY_FOR_DDD='" +
                    validTimeOfDayDDD+"',DDD_INTERVAL_LOWER_LIMIT='"+dueDateLowerLimit+"',DDD_INTERVAL_UPPER_LIMIT='"+dueDateUpperLimit+
                    "',SLA_WAIT_TIME='"+sLAWaitTime+   "',CONTACTNUMBER='"+contactNo+ "',MDFD_DT=sysdate,MDFD_USERID='" +
                    Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid) +
                    "' WHERE Vendor_Config_sqnc_nmbr = '" + Toolkit.replaceSingleQwithDoubleQ(strVendorConfigSqncNumber) + "'";
            
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            Log.write("strQuery ==kk"+strQuery);
            if (stmt.executeUpdate(strQuery) <= 0) {
                throw new SQLException(null,null,100);
            }
        } catch(SQLException sqle) {
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public int saveVendorBeanToDB() {
        Connection con = null;
        Statement stmt = null;
        String strQuery = "";
        String strQuery1 = "";
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            //   int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);
            String sericeType1=null;
            int len = strServiceType.lastIndexOf("^");
            if(len!=-1){
                sericeType1 =strServiceType.substring(0,len);
            }else{
                sericeType1 =strServiceType;
            }
            Log.write("====len=1=="+sericeType1);
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            strQuery1= "select VENDOR_CONFIG_SEQ.nextval as VensequnceNumber from dual";
            ResultSet rs1= stmt.executeQuery(strQuery1);
            if(rs1.next()){
                this.strVendorConfigSqncNumber = rs1.getString("VensequnceNumber");
            }
            Log.write("=values="+strVendorConfigSqncNumber+strCompSqncNumber+strStateCode+
                    strOCN+strBTN+strWCN+isEmbargoed+strTXJUR+sericeType1+strActivityType
                    +isDirectory+isEligibleToDeleteDir+validTimeOfDayDDD+dueDateLowerLimit
                    +dueDateUpperLimit+sLAWaitTime+contactNo);
            strQuery = "INSERT INTO VENDOR_TABLE_CONFIG_T " +
                    "values('"+this.strVendorConfigSqncNumber+"','"+Toolkit.replaceSingleQwithDoubleQ(strCompSqncNumber)+"','"
                    +Toolkit.replaceSingleQwithDoubleQ(strStateCode)+"','"+Toolkit.replaceSingleQwithDoubleQ(strOCN)+
                    "','"+Toolkit.replaceSingleQwithDoubleQ(strBTN)+"','" +
                    Toolkit.replaceSingleQwithDoubleQ(strWCN)+"','" +isEmbargoed+
                    "','"+Toolkit.replaceSingleQwithDoubleQ(strTXJUR)+"','" +sericeType1+"','" +strActivityType+"','" +isDirectory+"','" +
                    isEligibleToDeleteDir+"','"+validTimeOfDayDDD+"','"+dueDateLowerLimit+"','"+dueDateUpperLimit+"','"+
                    sLAWaitTime+"',sysdate,'"+ Toolkit.replaceSingleQwithDoubleQ(m_strMdfdUserid)+"','N','N','N','N','N','"+contactNo+"')";
            Log.write("=strQuery =saveVendorBeanToDB "+strQuery);
            stmt.executeUpdate(strQuery);
        } catch(SQLException sqle) {
            Log.write("====saveVendorBeanToDB==="+sqle.getMessage());
            Log.write(sqle.toString());
            return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            ExceptionHandler.handleException("kk exception",e);
            Log.write(e.toString());
            return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return 0;
    }
    
    public boolean validateVendorBean() {
        try{
            boolean rc = true;
            if(rc) return true;
            strBTNSPANStart = getSPANStart();
            strBTNSPANEnd = getSPANEnd();
            strWCNSPANStart = getSPANStart();
            strWCNSPANEnd = getSPANEnd();
            strTXJURSPANStart = getSPANStart();
            strTXJURSPANEnd = getSPANEnd();
            
            // Validate User ID
            if ((strBTN == null) || (strBTN.length() == 0)) {
                strBTNSPANStart = getErrSPANStart();
                strBTNSPANEnd = getErrSPANEnd();
                rc = false;
            } else if (! Validate.containsSpecialChars(strBTN)) {
                strBTNSPANStart = getErrSPANStart();
                strBTNSPANEnd = getErrSPANEnd();
                rc = false;
            }
            
            // Validate WCN
            if ((strWCN == null) || (strWCN.length() == 0)) {
                strWCNSPANStart = getErrSPANStart();
                strWCNSPANEnd = getErrSPANEnd();
                rc = false;
            } else if (Validate.containsSpecialChars(strWCN)) {
                strWCNSPANStart = getErrSPANStart();
                strWCNSPANEnd = getErrSPANEnd();
                rc = false;
            }
            
            // Validate TXJUR
            if ((strTXJUR == null) || (strTXJUR.length() == 0)) {
                strTXJURSPANStart = getErrSPANStart();
                strTXJURSPANEnd = getErrSPANEnd();
                rc = false;
            } else if (Validate.containsSpecialChars(strTXJUR)) {
                strTXJURSPANStart = getErrSPANStart();
                strTXJURSPANEnd = getErrSPANEnd();
                rc = false;
            }
            
            if (rc == false)
                m_strErrMsg = "ERROR:  Please review the data";
            
            return rc;
            
        }catch(Exception e) {
            Log.write("====saveVendorBeanToDB==="+e.getMessage());
            Log.write(e.toString());
        }
        return true;
    }
    
    public boolean validateMdfdDt() {
        Connection con = null;
        Statement stmt = null;
        String strMdfdDt = null;
        
        // Get DB Connection
        try {
            // Build SQL statement
            //kumar hot code temp
            if(true)	 return true;
            String strQuery = "SELECT MDFD_DT FROM VENDOR_TABLE_CONFIG_T ";
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            if (rs.next()) {
                strMdfdDt = rs.getString("MDFD_DT");
            } else {
                throw new SQLException(null,null,100);
            }
        } catch(SQLException sqle) {
            handleSQLError(sqle.getErrorCode());
            return false;
        } catch(Exception e) {
            return false;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        // As long as the dates are equal, all is well in the world and no one has changed the record
        if (strMdfdDt.equals(m_strMdfdDt)) {
            return true;
        } else {
            m_strErrMsg = "ERROR:  This row has been modified since you retrieved it. " +
                    "Please CANCEL and retrieve the row again.";
            return false;
        }
    }
    
    public List getOCN(String cmpnySqnc){
        
        List listOcn = new ArrayList();
        Connection con = null;
        Statement stmt = null;
        String strQuery = "";
        // Get DB Connection
        try {
            // Build INSERT SQL statement
            //   int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            strQuery = "SELECT OCN_T.OCN_CD,COMPANY_T.CMPNY_NM" +
                    " FROM OCN_T,COMPANY_T where OCN_T.CMPNY_SQNC_NMBR=COMPANY_T.CMPNY_SQNC_NMBR and " +
                    "OCN_T.CMPNY_SQNC_NMBR='"+cmpnySqnc+"'"+
                    " ORDER BY OCN_CD ASC";
            Log.write("====strQuery==ocn="+strQuery);
            ResultSet rs =stmt.executeQuery(strQuery);
            while(rs.next()==true){
                listOcn.add(rs.getString("OCN_CD"));
            }
        } catch(SQLException sqle) {
            Log.write("====saveVendorBeanToDB==="+sqle.getMessage());
            Log.write(sqle.toString());
            //    return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            Log.write("====saveVendorBeanToDB==="+e.getMessage());
            Log.write(e.toString());
            //   return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return listOcn;
    }
    
    
    public HashMap getState(String stateSqnc){
        
        
        HashMap hs = new HashMap();
        List listStCd = new ArrayList();
        List listStnm = new ArrayList();
        Connection con = null;
        Statement stmt = null;
        String strQuery = "";
        // Get DB Connection
        try {
            if(stateSqnc!=null){
                stateSqnc= stateSqnc.trim().substring(0,4);
            }
            // Build INSERT SQL statement
            //   int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);
            //   Log.write("====stateSqnc==="+stateSqnc);
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            strQuery = "select * from state_t  where state_t.stt_cd in" +
                    " (select stt_cd from  ocn_state_t where OCN_CD='"+stateSqnc+"')" +
                    " ORDER BY STT_NM ASC";
            Log.write("====strQuery=kk=="+strQuery);
            ResultSet rs =stmt.executeQuery(strQuery);
            while(rs.next()==true){
                listStCd.add(rs.getString("STT_CD"));
                listStnm.add(rs.getString("STT_NM"));
            }
            hs.put("listStCd",listStCd);
            hs.put("listStnm",listStnm);
        } catch(SQLException sqle) {
            Log.write("====saveVendorBeanToDB==="+sqle.getMessage());
            Log.write(sqle.toString());
            //    return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            Log.write("====saveVendorBeanToDB==="+e.getMessage());
            Log.write(e.toString());
            //   return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return hs;
    }
    
    public static void main(String[] args) {
        String str = "CSI including Listings (E and T)(M-P)^Ahh";
        
        int len = str.lastIndexOf("^");
        System.out.println("===="+len);
        String str1 =null;
        String str2 =null;
        str1=str.substring(len+1,str.length());
        System.out.println("=str1=="+str1);
        if(str1!=null && str1.trim().length()>0){
            System.out.println("=str1=="+str1.charAt(0));
            str2="in('"+str1.charAt(0)+"'";
            for(int i=1;i<str1.length();i++){
                str2 =str2+",'"+str1.charAt(i)+"'";
            }
            str2 =str2+")";
        }
        System.out.println("==str2=="+str2);
        
        
    }
    public List getActvity(String sericeType1){
        
        List listAct = new ArrayList();
        Connection con = null;
        Statement stmt = null;
        String strQuery = "";
        // Get DB Connection
        try {
            String strSer= null;
            Log.write("====sericeType1==="+sericeType1);
            if(sericeType1!=null && sericeType1.trim().length()>0){
                strSer="in('"+sericeType1.charAt(0)+"'";
                for(int i=1;i<sericeType1.length();i++){
                    strSer =strSer+",'"+sericeType1.charAt(i)+"'";
                }
                strSer =strSer+")";
            }
            // Build INSERT SQL statement
            //   int iCmpnySqncNmbr = Integer.parseInt(m_strCmpnySqncNmbr);
            
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            
            if(strSer==null) return listAct;
            
            strQuery = "SELECT * FROM activity_type_t where typ_ind='R' and actvty_typ_cd "+strSer+" ORDER BY " +
                    " ACTVTY_TYP_CD ASC";
            Log.write("====strQuery==ocn="+strQuery);
            ResultSet rs =stmt.executeQuery(strQuery);
            String actStr ="" ;
            while(rs.next()==true){
                actStr=rs.getString("ACTVTY_TYP_CD")+"-"+rs.getString("ACTVTY_TYP_DSCRPTN");
                listAct.add(actStr);
            }
        } catch(SQLException sqle) {
            Log.write("====saveVendorBeanToDB==="+sqle.getMessage());
            Log.write(sqle.toString());
            //    return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            Log.write("====saveVendorBeanToDB==="+e.getMessage());
            Log.write(e.toString());
            //   return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return listAct;
    }
    
    public String queryBuilder(String[] cmpny,String[] state,String[] ocn,String[] service,String[] activity ) {
        
        Log.write("kk Inside the query Builder ===kk===>" + strCompSqncNumber + "-->" + strStateCode
                + "-->" + strOCN + "--->" + strServiceType);
        
        String selectQuery = "SELECT VENDOR_CONFIG_SQNC_NMBR,CPY.CMPNY_NM,STT_CD,OCN_CD," +
                "SRVC_TYP_CD,ACTVTY_TYP_CD,VEDOR_AUTOMATE_FLAG,OCN_AUTOMATE_FLAG," +
                "STATE_AUTOMATE_FLAG,SRVTYPE_AUTOMATE_FLAG,ACTTYPE_AUTOMATE_FLAG,WCN " +
                "FROM VENDOR_TABLE_CONFIG_T VEN ,COMPANY_T CPY WHERE VEN.CMPNY_SQNC_NMBR=CPY.CMPNY_SQNC_NMBR ";
        
        if(cmpny==null && state==null && ocn==null &&
                service==null && activity==null ){
            return selectQuery;
        }else{
            selectQuery=selectQuery+ " AND ";
        }
        boolean andFlag = false;
        // boolean mulFlag = false;
        if (cmpny != null && cmpny.length>0) {
            selectQuery=selectQuery+"VEN.CMPNY_SQNC_NMBR IN('"+cmpny[0]+"'";
            for(int i=1;i<cmpny.length;i++){
                selectQuery = selectQuery.concat(",'"+ cmpny[i] + "'");
                Log.write("Query ======>" + selectQuery);
                
                
            }
            selectQuery= selectQuery+") ";
            andFlag = true;
        }
        
        if (state != null &&  state.length>0) {
            if (andFlag == true) {
                selectQuery = selectQuery.concat(" and ");
            }
            selectQuery=selectQuery+"STT_CD IN('"+state[0]+"'";
            for(int i=1;i<state.length;i++){
                selectQuery = selectQuery.concat(",'"+ state[i] + "'");
                Log.write("Query ======>" + selectQuery);
                
                
            }
            selectQuery=selectQuery+")";
            andFlag = true;
        }
        
        if (ocn != null &&  ocn.length>0) {
            if (andFlag == true) {
                selectQuery = selectQuery.concat(" and ");
            }
            selectQuery=selectQuery+"OCN_CD IN('"+ocn[0]+"'";
            for(int i=1;i<ocn.length;i++){
                selectQuery = selectQuery.concat(",'"+ ocn[i] + "'");
                Log.write("Query ======>" + selectQuery);
                
            }
            selectQuery=selectQuery+")";
            andFlag = true;
        }
        if (service != null &&  service.length>0) {
            if (andFlag == true) {
                selectQuery = selectQuery.concat(" and ");
            }
            selectQuery=selectQuery+"SRVC_TYP_CD IN('"+service[0]+"'";
            for(int i=1;i<service.length;i++){
                selectQuery = selectQuery.concat(",'"+ service[i] + "'");
                Log.write("Query ======>" + selectQuery);
                
            }
            andFlag = true;
            selectQuery=selectQuery+")";
        }
        
        if (activity != null &&  activity.length>0) {
            if (andFlag == true) {
                selectQuery = selectQuery.concat(" and ");
                
            }
            selectQuery=selectQuery+"ACTVTY_TYP_CD IN('"+activity[0]+"'";
            for(int i=1;i<activity.length;i++){
                selectQuery = selectQuery.concat(",'"+ activity[i] + "'");
                Log.write("Query ======>" + selectQuery);
                
            }
            andFlag = true;
            selectQuery=selectQuery+")";
        }
        
        
        Log.write("From Query Builder--11-----" + selectQuery);
        
        return selectQuery;
    }
    
    
    public List retrieveVendorAutomationBeanFromDB(String strQuery){
        List listAuto = new ArrayList();
        Log.write("=kk1=strQuery="+strQuery);
        Connection con = null;
        Statement stmt = null;
        ResultSet rs =null;
        try {
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(strQuery);
            Log.write(Log.DEBUG_VERBOSE, "LPB() rs kk"+rs);
            
            
            while (rs.next()) {
                VendorBean objVendorBean = new VendorBean();
                objVendorBean.strVendorConfigSqncNumber=rs.getString("VENDOR_CONFIG_SQNC_NMBR");
                objVendorBean.strCompSqncNumber = rs.getString("CMPNY_NM");
                objVendorBean.strStateCode = rs.getString("STT_CD");
                objVendorBean.strOCN = rs.getString("OCN_CD");
                objVendorBean.strServiceType = rs.getString("SRVC_TYP_CD");
                objVendorBean.strActivityType = rs.getString("ACTVTY_TYP_CD");
                //shifted the WCN to below line from the last getString line - fix for ISSASOI-2 - Antony - 05/26/2010
                objVendorBean.strWCN = rs.getString("WCN");
                Log.write("WCN VALUE :"+objVendorBean.getStrWCN());
                objVendorBean.vedorAutomateFlag = rs.getString("VEDOR_AUTOMATE_FLAG");
                Log.write("=vedorAutomateFlag==="+objVendorBean.vedorAutomateFlag );
                objVendorBean.ocnAutomateFlag = rs.getString("OCN_AUTOMATE_FLAG");
                objVendorBean.stateAutomateFlag = rs.getString("STATE_AUTOMATE_FLAG");
                objVendorBean.srvtypeAutomateFlag = rs.getString("SRVTYPE_AUTOMATE_FLAG");
                objVendorBean.acttypeAutomateFlag = rs.getString("ACTTYPE_AUTOMATE_FLAG");
                listAuto.add(objVendorBean);
            }
            
        } catch(SQLException sqle) {
            
            Log.write(1,sqle.toString());
            sqle.printStackTrace();
            handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            Log.write(1,e.toString());
            e.printStackTrace();
            //  return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return listAuto;
    }
    
    public int updateVendorAutomationBeanToDB(List listFlag){
        List listAuto = new ArrayList();
        Connection con = null;
        Statement stmt = null;
        int value=0;
        try {
            
            
            String[] allFlag =(String[])listFlag.get(0);
            String[] strVendorConfigSqncNumber1 =(String[]) listFlag.get(1);
            String flag =(String) listFlag.get(2);
            
            Log.write("flag  kk"+flag);
            String[] vedorAutomateFlag =(String[]) listFlag.get(3);
            String[] ocnAutomateFlag =(String[]) listFlag.get(4);
            String[] stateAutomateFlag =(String[]) listFlag.get(5);
            String[] srvtypeAutomateFlag =(String[]) listFlag.get(6);
            String[] acttypeAutomateFlag =(String[]) listFlag.get(7);
            con = DatabaseManager.getConnection();
            stmt = con.createStatement();
            String strQuery = "";
            for(int i=0;i<strVendorConfigSqncNumber1.length;i++){
                if(flag!=null){
                    //  Log.write("flag  if "+flag);
                    strQuery= "UPDATE VENDOR_TABLE_CONFIG_T SET VEDOR_AUTOMATE_FLAG='"+flag
                            +"',OCN_AUTOMATE_FLAG='"+flag+"',STATE_AUTOMATE_FLAG='"+flag
                            +"',SRVTYPE_AUTOMATE_FLAG='"+flag+"',ACTTYPE_AUTOMATE_FLAG='"+
                            flag+"' WHERE VENDOR_CONFIG_SQNC_NMBR='"+strVendorConfigSqncNumber1[i]+"'";
                }else if(allFlag[i].trim().equals("")){
                    //Log.write("flag else if "+flag);
                    strQuery= "UPDATE VENDOR_TABLE_CONFIG_T SET VEDOR_AUTOMATE_FLAG='"+vedorAutomateFlag[i]
                            +"',OCN_AUTOMATE_FLAG='"+ocnAutomateFlag[i]+"',STATE_AUTOMATE_FLAG='"+stateAutomateFlag[i]
                            +"',SRVTYPE_AUTOMATE_FLAG='"+srvtypeAutomateFlag[i]+"',ACTTYPE_AUTOMATE_FLAG='"+
                            acttypeAutomateFlag[i]+"' WHERE VENDOR_CONFIG_SQNC_NMBR='"+strVendorConfigSqncNumber1[i]+"'";
                }else{
                    //  Log.write("flag else  ");
                    strQuery= "UPDATE VENDOR_TABLE_CONFIG_T SET VEDOR_AUTOMATE_FLAG='"+allFlag[i]
                            +"',OCN_AUTOMATE_FLAG='"+allFlag[i]+"',STATE_AUTOMATE_FLAG='"+allFlag[i]
                            +"',SRVTYPE_AUTOMATE_FLAG='"+allFlag[i]+"',ACTTYPE_AUTOMATE_FLAG='"+
                            allFlag[i]+"' WHERE VENDOR_CONFIG_SQNC_NMBR='"+strVendorConfigSqncNumber1[i]+"'";
                }
                Log.write("=strQuery =updateVendorAutomationBeanToDB "+strQuery);
                value = stmt.executeUpdate(strQuery);
            }
            
            
            con.commit();
            //  Log.write("=strQuery =updateVendorAutomationBeanToDB "+strQuery);
            
            Log.write(Log.DEBUG_VERBOSE, "LPB() value kk"+value);
            
        } catch(SQLException sqle) {
            
            Log.write(1,sqle.toString());
            sqle.printStackTrace();
            //return handleSQLError(sqle.getErrorCode());
        } catch(Exception e) {
            Log.write(1,e.toString());
            e.printStackTrace();
            //  return 1;
        } finally {
            DatabaseManager.releaseConnection(con);
        }
        
        return value;
    }
    
    
    
    
    public String getM_strTableName() {
        return m_strTableName;
    }
    
    
    
    public String getStrCompSqncNumber() {
        return strCompSqncNumber;
    }
    
    public void setStrCompSqncNumber(String strCompSqncNumber) {
        this.strCompSqncNumber = strCompSqncNumber;
    }
    
    public String getStrStateCode() {
        return strStateCode;
    }
    
    public void setStrStateCode(String strStateCode) {
        this.strStateCode = strStateCode;
    }
    
    public String getStrOCN() {
        return strOCN;
    }
    
    public void setStrOCN(String strOCN) {
        this.strOCN = strOCN;
    }
    
//    public String getStrCompanyType() {
//        return strCompanyType;
//    }
//
//    public void setStrCompanyType(String strCompanyType) {
//        this.strCompanyType = strCompanyType;
//    }
    
    public String getStrBTN() {
        return strBTN;
    }
    
    public void setStrBTN(String strBTN) {
        this.strBTN = strBTN;
    }
    
    public String getStrWCN() {
        return strWCN;
    }
    
    public void setStrWCN(String strWCN) {
        this.strWCN = strWCN;
    }
    
    public String getIsEmbargoed() {
        return isEmbargoed;
    }
    
    public void setIsEmbargoed(String isEmbargoed) {
        this.isEmbargoed = isEmbargoed;
    }
    
    public String getStrTXJUR() {
        return strTXJUR;
    }
    
    public void setStrTXJUR(String strTXJUR) {
        this.strTXJUR = strTXJUR;
    }
    
    public String getStrServiceType() {
        return strServiceType;
    }
    
    public void setStrServiceType(String strServiceType) {
        this.strServiceType = strServiceType;
    }
    
    public String getStrActivityType() {
        return strActivityType;
    }
    
    public void setStrActivityType(String strActivityType) {
        this.strActivityType = strActivityType;
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
    
    public String getContactNo() {
        return contactNo;
    }
    
    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }
    
    
}// end of VendorBean()

