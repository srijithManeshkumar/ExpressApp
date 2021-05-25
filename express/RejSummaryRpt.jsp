<%
            /** NOTICE:
             *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO WINDSTREAM COMMUNICATIONS
             *		INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
             *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
             *
             *			COPYRIGHT (C) 2009
             *				BY
             *			WINDSTREAM COMMUNICATIONS INC.
             */
            /**

             * MODULE:	RejSummaryRpt.jsp
             *
             * DESCRIPTION:
             *
             * AUTHOR:      Satish Talluri
             *
             * DATE:        April 30, 2011, 2:55:14 PM
             *
             * HISTORY:
             *
             */
%>
<%@ include file="i_header.jsp" %>
<head>
    <%            String path = request.getContextPath();
    %>
    <script type='text/javascript' src='<%=path%>/jquery.js'></script>
    <script type='text/javascript' src='<%=path%>/tablesorter.js'></script>
</head>
<%@page  import="com.automation.reports.bean.RejectionBean"  %>
<%            final String SECURITY_OBJECT = "PROV_REPORTS";
            if (!sdm.isAuthorized(SECURITY_OBJECT)) {
                Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
                alltelResponse.sendRedirect(SECURITY_URL);
            }

            Connection con = null;


            Vector vFocRej;

            String strFromMMDDYYYY;
            String strToMMDDYYYY;

            String strStartYr = alltelRequest.getParameter("from_due_date_yr");
            String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
            String strStartDay = alltelRequest.getParameter("from_due_date_dy");
            if ((strStartYr.length() == 0) || (strStartMth.length() == 0) || (strStartDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid from date");
%>
<jsp:forward page="RejSummaryDateSelect.jsp"/>;
<%
                return;
            }
            String strStartDate = strStartYr + strStartMth + strStartDay;

            String strEndYr = alltelRequest.getParameter("to_due_date_yr");
            String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
            String strEndDay = alltelRequest.getParameter("to_due_date_dy");
            if ((strEndYr.length() == 0) || (strEndMth.length() == 0) || (strEndDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid to date");
%>
<jsp:forward page="RejSummaryDateSelect.jsp"/>;
<%
                return;
            }
            String strEndDate = strEndYr + strEndMth + strEndDay;
            if (strStartDate.compareTo(strEndDate) > 0) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' must be less than or equal to 'To Date'!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid to date");
%>
<jsp:forward page="RejSummaryDateSelect.jsp"/>;
<%
                return;
            }

            //Check days of month and adjust if necessary

            Calendar calTemp = Calendar.getInstance();
            calTemp.set(Integer.parseInt(strStartYr), Integer.parseInt(strStartMth) - 1, 1, 0, 0, 0);
            int iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);

            if (Integer.parseInt(strStartDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport Invalid from date");
%>
<jsp:forward page="RejSummaryDateSelect.jsp"/>;
<%
                return;
            }
            calTemp.set(Integer.parseInt(strEndYr), Integer.parseInt(strEndMth) - 1, 1, 0, 0, 0);
            iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);

            if (Integer.parseInt(strEndDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'To Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport Invalid to date");
%>
<jsp:forward page="RejSummaryDateSelect.jsp"/>;
<%
                return;
            }

            DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);

            Log.write(Log.DEBUG_VERBOSE, "DailyReport: Date:" + strStartDate + " Date:" + strEndDate);

            //Build from and to calendar ranges
            Calendar calFrom = Calendar.getInstance();
            calFrom.set(Integer.parseInt(strStartYr), Integer.parseInt(strStartMth) - 1, Integer.parseInt(strStartDay), 0, 0, 0);

            Calendar calTo = Calendar.getInstance();
            calTo.set(Integer.parseInt(strEndYr), Integer.parseInt(strEndMth) - 1, Integer.parseInt(strEndDay), 23, 59, 59);

            Calendar calTempFrom = Calendar.getInstance();
            calTempFrom.set(Integer.parseInt(strStartYr), Integer.parseInt(strStartMth) - 1, Integer.parseInt(strStartDay), 0, 0, 0);

            Log.write(Log.DEBUG_VERBOSE, "DailyReport: From:" + calFrom.getTime() + " To:" + calTo.getTime());

            con = DatabaseManager.getConnection();
            Statement stmt = null;
            ResultSet rs = null;
            stmt = con.createStatement();
            Statement stmtRej = null;
            ResultSet rsRej = null;
            stmtRej = con.createStatement();

            boolean bSpecificOCN = false;
            boolean bSpecificState = false;
            boolean bSpecificVendor = false;
            boolean bSpecificSrvcTypCd = false;
            boolean bSpecificOrder = false;

            String strOCN = "";
            String strOCNDis = "";
            String strOCNWhere = "";
            String[] strOCNs = alltelRequest.getAttributeValue("OCN_CD");
            if (strOCNs != null) {
                for (int i = 0; i < strOCNs.length; i++) {
                    if (strOCNs[i].equals("ALL")) {
                        strOCN = "ALL";
                        break;
                    } else {
                        bSpecificOCN = true;
                        if (strOCN.length() > 0) {
                            strOCN += ",";
                        }
                        strOCN += "'" + strOCNs[i] + "'";
                        strOCNDis += strOCNs[i];
                        strOCNDis += "&nbsp;&nbsp;&nbsp;";
                    }
                }
            }
            Log.write(Log.DEBUG_VERBOSE, "strOCN=[" + strOCN + "]");
            if (bSpecificOCN) {
                strOCNWhere = " AND R.OCN_CD IN (" + strOCN + ") ";
            }

            String strState = "";
            String strStateDis = "";
            String strStateWhere = "";
            String[] strStates = alltelRequest.getAttributeValue("STATE_CD");
            if (strStates != null) {
                for (int i = 0; i < strStates.length; i++) {
                    if (strStates[i].equals("ALL")) {
                        strState = "ALL";
                        break;
                    } else {
                        bSpecificState = true;
                        if (strState.length() > 0) {
                            strState += ",";
                        }
                        strState += "'" + strStates[i] + "'";
                        strStateDis += strStates[i];
                        strStateDis += "&nbsp;&nbsp;&nbsp;";
                    }
                }
            }
            Log.write(Log.DEBUG_VERBOSE, "strState=[" + strState + "]");
            if (bSpecificState) {
                strStateWhere = " AND R.OCN_STT IN (" + strState + ") ";
            }

            String strVendor = "";
            String strCmpnyDis = "";
            String strVendorWhere = "";
            String[] strVendors = alltelRequest.getAttributeValue("VENDOR");
            if (strVendors != null) {
                for (int i = 0; i < strVendors.length; i++) {
                    if (strVendors[i].equals("ALL")) {
                        strVendor = "ALL";
                        break;
                    } else {
                        bSpecificVendor = true;
                        if (strVendor.length() > 0) {
                            strVendor += ",";
                        }
                        strVendor += "'" + strVendors[i] + "'";
                    }
                }
            }
            Log.write(Log.DEBUG_VERBOSE, "strVendor=[" + strVendor + "]");
            if (bSpecificVendor) {
                strVendorWhere = " AND R.CMPNY_SQNC_NMBR IN (" + strVendor + ") ";

                String strQryCmpny = "SELECT CMPNY_NM FROM COMPANY_T WHERE CMPNY_SQNC_NMBR IN (" + strVendor + ")";

                rs = stmt.executeQuery(strQryCmpny);

                while (rs.next()) {
                    strCmpnyDis += rs.getString("CMPNY_NM");
                    strCmpnyDis += "&nbsp;&nbsp;&nbsp;";
                }

                rs.close();
                rs = null;
            }

            String strRejectioncd = "";
            String strRejectionCodeis = "";
            String strRejectioncodeWhere = "";
            String[] strRejectioncds = alltelRequest.getAttributeValue("REJECTION_CODE");
            if (strRejectioncds != null) {
                for (int i = 0; i < strRejectioncds.length; i++) {
                    if (strRejectioncds[i].equals("ALL")) {
                        strRejectioncd = "ALL";
                        break;
                    } else {
                        if (strRejectioncd.length() > 1) {
                            strRejectioncd += " OR ";
                        }
                        bSpecificSrvcTypCd = true;
                        strRejectioncd += "E.RSN_CD_SQNC_NMBR LIKE '%" + strRejectioncds[i] + "%'";

                    }
                }
            }
            Log.write(Log.DEBUG_VERBOSE, "strRejectioncd=[" + strRejectioncd + "]");
            if (bSpecificSrvcTypCd) {
                //strRejectioncodeWhere = " AND B.RSN_CD_SQNC_NMBR IN (" + strRejectioncd + ") ";
                strRejectioncodeWhere = " AND " + strRejectioncd + "";
                String strQryRejection = "SELECT RSN_CD_DSCRPTN FROM REASON_CODE_T E WHERE " + strRejectioncd;
                rs = stmt.executeQuery(strQryRejection);
                while (rs.next()) {
                    strRejectionCodeis += rs.getString("RSN_CD_DSCRPTN");
                    strRejectionCodeis += "&nbsp;&nbsp;&nbsp;";
                }

                rs.close();
                rs = null;

            }
            String strOrderFlag = alltelRequest.getParameter("orderFlag");
            String strOrderFlagWhere = "";

            Log.write(Log.DEBUG_VERBOSE, "strOrderFlag=[" + strOrderFlag + "]");

            if (strOrderFlag.equals("ALL")) {
                strOrderFlag = "ALL";
            } else {
                bSpecificOrder = true;
            }
            if (bSpecificOrder) {
                strOrderFlagWhere = " AND R.ICARE='" + strOrderFlag + "'";
            }



%>

<table width=800 align=center>

    <br><center>
        <SPAN CLASS="header1">R&nbsp;E&nbsp;J&nbsp;E&nbsp;C&nbsp;T&nbsp;I&nbsp;O&nbsp;N&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S&nbsp;U&nbsp;M&nbsp;M&nbsp;A&nbsp;R&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN><br>
        <br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%></b><br>
        Effective:&nbsp;<%= dFmt.format(new java.util.Date())%><br><br>
        <%
                    if (bSpecificOCN) {
        %>
        <b><font size="2">OCN:&nbsp;</font></b><%=strOCNDis%>
        <%
                    }
        %>

        <%
                    if (bSpecificState) {
        %>
        <b><font size="2">State:&nbsp;</font></b><%=strStateDis%>
        <%
                    }
        %>

        <%
                    if (bSpecificVendor) {
        %>
        <b><font size="2">Company:&nbsp;</font></b><%=strCmpnyDis%>
        <%
                    }
        %>

        <%
                    if (bSpecificSrvcTypCd) {
        %>
        <b><font size="2">RejectionCode:&nbsp;</font></b><%=strRejectionCodeis%>
        <%
                    }
        %>



    </center><br><br></table>

<%
            //Loop day by day and create this table
            DateFormat dyFmt = new SimpleDateFormat("dd-MMM");
            DateFormat dateRangeFmt = new SimpleDateFormat("MMddyyyy");
            //strFromMMDDYYYY = dateRangeFmt.format(calTempFrom.getTime());
            //strToMMDDYYYY = dateRangeFmt.format(calTempFrom.getTime());
            strFromMMDDYYYY = dateRangeFmt.format(calFrom.getTime());
            calTo.add(Calendar.DATE, 1);
            strToMMDDYYYY = dateRangeFmt.format(calTo.getTime());

            DecimalFormat f = new DecimalFormat("00");
%>
<table width="64%"  align=center cellspacing=0 cellpadding=2>

    <td width="54%" align="right"><a href=RejSumExcelRpt.jsp>EXCEL</a></td>
    <td width="10%" align="right"><a href=RejSumCSVRpt.jsp>CSV</a></td>
</tr>
</table>

<table width="64%" id="rejsummaryTable" class="tablesorter" border=1 align=center cellspacing=0 cellpadding=1>
    <thead>
        <tr>
            <th align=center width="8%" bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Internal Rejection Code</font></th>

            <th align=center width="8%" bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Total</font></th>

            <th align=center width="8%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		%</th>

        </tr>
    </thead>
    <tbody>

        <%
                    Log.write("dates; " + strFromMMDDYYYY + " to " + strToMMDDYYYY);
                    String rejectiondesc = "";
                    float rejcount = 0;
                    int rejcountTot = 0;
                    double rejPer = 0;
                    double rejPerTot = 0;
                    /*
                    String strQryRej = "SELECT RSN_CD_SQNC_NMBR REJDETAIL,COUNT(RSN_CD_SQNC_NMBR) REJCOUNT FROM AUTOMATION_STATUSES_T A,"
                    + "AUTOMATION_RESULTS_T B,REQUEST_HISTORY_T H,REQUEST_T R WHERE A.EXTERNAL_STATUS='REJECTED' AND "
                    + "B.VALIDATION_RESULT='N' AND A.RQST_SQNC_NMBR=B.RQST_SQNC_NMBR AND "
                    + "R.SRVC_TYP_CD='C' AND R.ACTVTY_TYP_CD='V' AND "
                    + "B.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND R.RQST_SQNC_NMBR=H.RQST_SQNC_NMBR AND "
                    + "H.RQST_HSTRY_DT_OUT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                    + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') AND "
                    + "H.RQST_STTS_CD_OUT='REJECTED' "
                    + strOCNWhere + strStateWhere + strVendorWhere + strRejectioncodeWhere + " GROUP BY RSN_CD_SQNC_NMBR";

                    String strQryRej="SELECT DISTINCT SUBSTR(E.RSN_CD_SQNC_NMBR,1,INSTR(E.RSN_CD_SQNC_NMBR,'-')) REJDETAIL, COUNT(E.RSN_CD_SQNC_NMBR) REJCOUNT FROM AUTOMATION_STATUSES_T A, "
                    + "REQUEST_T R, REQUEST_HISTORY_T H, AUTOMATION_RESULTS_T E, COMPANY_T C, CI_T CI WHERE A.EXTERNAL_STATUS = 'REJECTED' "
                    + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                    + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') AND A.RQST_SQNC_NMBR=E.RQST_SQNC_NMBR AND "
                    + "A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR AND A.RQST_VRSN = R.RQST_VRSN AND A.RQST_SQNC_NMBR= CI.RQST_SQNC_NMBR AND A.RQST_VRSN= CI.RQST_VRSN AND "
                    + "A.RQST_VRSN = E.RQST_VER AND R.SRVC_TYP_CD = 'C' AND R.ACTVTY_TYP_CD = 'V' AND "
                    + "E.VALIDATION_RESULT='N' AND R.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND A.RQST_SQNC_NMBR = H.RQST_SQNC_NMBR "
                    + "AND A.RQST_VRSN = H.RQST_VRSN "
                    + "AND H.RQST_HSTRY_SQNC_NMBR = (SELECT MIN(Q.RQST_HSTRY_SQNC_NMBR) FROM "
                    + "REQUEST_HISTORY_T Q WHERE H.RQST_SQNC_NMBR = Q.RQST_SQNC_NMBR AND "
                    + "H.RQST_VRSN = Q.RQST_VRSN AND Q.RQST_STTS_CD_OUT = 'REJECTED') "
                    + strOCNWhere + strStateWhere + strVendorWhere + strRejectioncodeWhere
                    + "GROUP BY RSN_CD_SQNC_NMBR";
                     */
                    //modified on Aug19th
                 /*   String strQryRej="SELECT REJDETAIL,COUNT(*) REJCOUNT FROM(SELECT DISTINCT R.RQST_PON,SUBSTR(E.RSN_CD_SQNC_NMBR,1,INSTR(E.RSN_CD_SQNC_NMBR,'-')) REJDETAIL FROM AUTOMATION_STATUSES_T A, "
                    + "REQUEST_T R, REQUEST_HISTORY_T H, AUTOMATION_RESULTS_T E, COMPANY_T C, CI_T CI WHERE A.EXTERNAL_STATUS = 'REJECTED' "
                    + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                    + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') AND A.RQST_SQNC_NMBR=E.RQST_SQNC_NMBR AND "
                    + "A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR AND A.RQST_VRSN = R.RQST_VRSN AND A.RQST_SQNC_NMBR= CI.RQST_SQNC_NMBR AND A.RQST_VRSN= CI.RQST_VRSN AND "
                    + "A.RQST_VRSN = E.RQST_VER AND R.SRVC_TYP_CD = 'C' AND R.ACTVTY_TYP_CD = 'V' AND "
                    + "E.VALIDATION_RESULT='N' AND R.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND A.RQST_SQNC_NMBR = H.RQST_SQNC_NMBR "
                    + "AND A.RQST_VRSN = H.RQST_VRSN "
                    + "AND H.RQST_HSTRY_SQNC_NMBR = (SELECT MIN(Q.RQST_HSTRY_SQNC_NMBR) FROM "
                    + "REQUEST_HISTORY_T Q WHERE H.RQST_SQNC_NMBR = Q.RQST_SQNC_NMBR AND "
                    + "H.RQST_VRSN = Q.RQST_VRSN AND Q.RQST_STTS_CD_OUT = 'REJECTED') "
                    + strOCNWhere + strStateWhere + strVendorWhere + strRejectioncodeWhere
                    + ")GROUP BY REJDETAIL"; */

                    /*  Modified for rejection reports fix -- To add the MANUAL-REJECT
                    ( REJECTED records from REQUEST_T R ) records , UNION query added with
                    R.INN_STTS = 'MANUAL-REJECT' and A.INTERNAL_STATUS ='PRE-REJECT' AND E.VALIDATION_RESULT in ('N','M') . */

                    String strQryRej = "SELECT REJDETAIL,COUNT(*) REJCOUNT FROM(SELECT DISTINCT R.RQST_PON,SUBSTR(E.RSN_CD_SQNC_NMBR,1,INSTR(E.RSN_CD_SQNC_NMBR,'-')) REJDETAIL FROM AUTOMATION_STATUSES_T A, "
                            + "REQUEST_T R, REQUEST_HISTORY_T H, AUTOMATION_RESULTS_T E, COMPANY_T C WHERE A.EXTERNAL_STATUS = 'REJECTED' "
                            + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                            + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') AND A.RQST_SQNC_NMBR=E.RQST_SQNC_NMBR AND "
                            + "A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR AND A.RQST_VRSN = R.RQST_VRSN AND "
                            + "A.RQST_VRSN = E.RQST_VER AND "
                            + "E.VALIDATION_RESULT='N' AND R.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND A.RQST_SQNC_NMBR = H.RQST_SQNC_NMBR "
                            + "AND A.RQST_VRSN = H.RQST_VRSN "
                            + "AND H.RQST_HSTRY_SQNC_NMBR = (SELECT MIN(Q.RQST_HSTRY_SQNC_NMBR) FROM "
                            + "REQUEST_HISTORY_T Q WHERE H.RQST_SQNC_NMBR = Q.RQST_SQNC_NMBR AND "
                            + "H.RQST_VRSN = Q.RQST_VRSN AND Q.RQST_STTS_CD_OUT = 'REJECTED') "
                            + strOCNWhere + strStateWhere + strVendorWhere + strRejectioncodeWhere + strOrderFlagWhere
                            + " UNION "
                            + "SELECT DISTINCT R.RQST_PON,SUBSTR(E.RSN_CD_SQNC_NMBR,1,INSTR(E.RSN_CD_SQNC_NMBR,'-')) REJDETAIL FROM AUTOMATION_STATUSES_T A, "
                            + "REQUEST_T R, REQUEST_HISTORY_T H, AUTOMATION_RESULTS_T E, COMPANY_T C WHERE R.INN_STTS = 'MANUAL-REJECT' and A.INTERNAL_STATUS ='PRE-REJECT'  "
                            + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                            + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') AND A.RQST_SQNC_NMBR=E.RQST_SQNC_NMBR AND "
                            + "A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR AND A.RQST_VRSN = R.RQST_VRSN AND "
                            + "A.RQST_VRSN = E.RQST_VER  "
                            + "AND E.VALIDATION_RESULT IN ('N','M') AND R.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR AND A.RQST_SQNC_NMBR = H.RQST_SQNC_NMBR  "
                            + "AND A.RQST_VRSN = H.RQST_VRSN "
                            + "AND H.RQST_HSTRY_SQNC_NMBR = (SELECT MIN(Q.RQST_HSTRY_SQNC_NMBR) FROM "
                            + "REQUEST_HISTORY_T Q WHERE H.RQST_SQNC_NMBR = Q.RQST_SQNC_NMBR AND "
                            + "H.RQST_VRSN = Q.RQST_VRSN AND Q.RQST_STTS_CD_OUT = 'REJECTED') "
                            + strOCNWhere + strStateWhere + strVendorWhere + strRejectioncodeWhere + strOrderFlagWhere
                            + ")GROUP BY REJDETAIL ";


                    rs = stmt.executeQuery(strQryRej);
                    List rejList = new ArrayList();
                    List rejrptList = new ArrayList();
                    Map rjCodeMap = new HashMap();
                    int rejCount = 0;
                    Object rejkeyval = "";
                    int rejvalue = 0;

                    
                    RejectionBean rejectionBean = new RejectionBean();
                    while (rs.next()) {
                        rejectiondesc = rs.getString("REJDETAIL");
                        rejectionBean.setRejcount(rs.getInt("REJCOUNT"));
                        rejCount = rs.getInt("REJCOUNT");
                        String rejectionSeno = rejectiondesc.substring(0, rejectiondesc.indexOf("-"));
                        String internalRejQuery = "SELECT RSN_CD_TYP,RSN_CD_DSCRPTN FROM REASON_CODE_T WHERE RSN_CD_SQNC_NMBR='" + rejectionSeno + "'";
                        rsRej = stmtRej.executeQuery(internalRejQuery);
                        if (rsRej.next()) {
                            if (!rjCodeMap.containsKey(rsRej.getString("RSN_CD_TYP") + "-" + rsRej.getString("RSN_CD_DSCRPTN"))) {
                                rjCodeMap.put(rsRej.getString("RSN_CD_TYP") + "-" + rsRej.getString("RSN_CD_DSCRPTN"), rs.getString("REJCOUNT"));
                            } else {

                                rejkeyval = rjCodeMap.get(rsRej.getString("RSN_CD_TYP") + "-" + rsRej.getString("RSN_CD_DSCRPTN"));
                                rejvalue = Integer.parseInt(rejkeyval.toString());
                                rejCount = rejCount + rejvalue;
                                rjCodeMap.put(rsRej.getString("RSN_CD_TYP") + "-" + rsRej.getString("RSN_CD_DSCRPTN"), new Integer(rejCount));

                            }
                        }
                        rsRej.close();
                    }
                    stmtRej.close();
                    rs.close();

                    Set set = rjCodeMap.entrySet();
                    Iterator i = set.iterator();
                    while (i.hasNext()) {
                        RejectionBean _rejectionBean = new RejectionBean();
                        Map.Entry me = (Map.Entry) i.next();
                        _rejectionBean.setRejDesc(me.getKey().toString());
                        int k = Integer.parseInt(me.getValue().toString());
                        _rejectionBean.setRejcount(k);
                        rejList.add(_rejectionBean);
                        rejcountTot = rejcountTot + _rejectionBean.getRejcount();
                    }
                    if (rejList != null) {
                        Iterator iterator = rejList.iterator();
                        while (iterator.hasNext()) {
                            RejectionBean rejectionrptBean = new RejectionBean();
                            RejectionBean rejectionBeanfinal = (RejectionBean) iterator.next();
                            rejectiondesc = rejectionBeanfinal.getRejDesc();
                            rejcount = rejectionBeanfinal.getRejcount();
        %>

        <tr>
            <% if (rejectiondesc != null) {
            %>
            <td align=left><b><%=rejectiondesc%></b></td>
            <td align=center><%=(int) rejcount%></td>
            <% if (rejcountTot > 0) {
                     //rejPer = ((rejcount * 100) / rejcountTot-0.15));
                     rejPer = ((rejcount / rejcountTot) * 100);
                     rejPerTot = rejPerTot + rejPer;
            %>
            <td align="center"><%=Math.round(rejPer)%></td>
            <%
                                    }
                                    rejectionrptBean.setRejDesc(rejectiondesc);
                                    rejectionrptBean.setRejcount((int) rejcount);
                                    rejectionrptBean.setRejPer(Math.round(rejPer));
                                    rejrptList.add(rejectionrptBean);
                                }
                            }
                            RejectionBean totRejectionBean = new RejectionBean();
                            totRejectionBean.setRejTot(rejcountTot);
                            totRejectionBean.setRejPerTot(Math.round(rejPerTot));
                            session.setAttribute("total", totRejectionBean);
                            session.setAttribute("sumList", rejrptList);
                        }

            %>
    </tbody>
    <tr>
        <td align=center><b>Total</b></td>
        <td align=center><b><%=(int) rejcountTot%></b></td>
        <td align=center><b><%=Math.round(rejPerTot)%></b></td>
    </tr>

</table><br><br>
<%

            DatabaseManager.releaseConnection(con);
%>

</UL>
<BR>
<BR>
<center><a href=javascript:this.history.back()>GO BACK</a></center>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
<script language="javascript">
    $(document).ready(function()
    {
        $("#rejsummaryTable").tablesorter();
    }
);
</script>
</BODY>
</HTML>


