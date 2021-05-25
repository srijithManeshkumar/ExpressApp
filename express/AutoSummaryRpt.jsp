<%
            /**
             * NOTICE:
             *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO WINDSTREAM COMMUNICATIONS
             *		INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
             *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
             *
             *			COPYRIGHT (C) 2009
             *				BY
             *			WINDSTREAM COMMUNICATIONS INC.
             */
            /**
             * MODULE:	AutoSummaryRpt.jsp
             *
             * DESCRIPTION: Daily Summary Report for the Automated Activity
             *
             * AUTHOR:      Andy Wei
             *
             * DATE:        09-15-2009
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

<%@page  import="com.automation.reports.bean.AutoSummaryBean"  %>
<%            final String SECURITY_OBJECT = "PROV_REPORTS";
            if (!sdm.isAuthorized(SECURITY_OBJECT)) {
                Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
                alltelResponse.sendRedirect(SECURITY_URL);
            }

            Connection con = null;

            double iDyMAN = 0.00;
            double iDyREJ = 0.00;
            double iDyREJAuto = 0.00;
            double iDyREJMan = 0.00;
            double iDyFOCMan = 0.00;
            double iDyFOCAuto = 0.00;
            double iDyFOC = 0.00;
            double iDyTOT = 0.00;

            double iTotMAN = 0.00;
            double iTotREJ = 0.00;
            double iTotREJAuto = 0.00;
            double iTotREJMan = 0.00;
            double iTotFOC = 0.00;
            double iTotFOCAuto = 0.00;
            double iTotFOCMan = 0.00;
            double iGrandTOT = 0.00;

            int iPerDyMAN = 0;
            int iPerDyREJ = 0;
            int iPerDyFOC = 0;
            int iPerDyTemp = 0;

            int iPerTotMAN = 0;
            int iPerTotREJ = 0;
            int iPerTotFOC = 0;
            int iPerTotTemp = 0;
            int iTotSimple = 0;
            int iTotComplex = 0;
            double iDySimple = 0;
            double iDyComplex = 0;
            String iDyResponseTime = "0d 00:00:00";
            String iDyResponseTimeAuto = "0d 00:00:00";
            String iDyResponseTimeMan = "0d 00:00:00";
            String iDyResponseTimeTotal = "0d 00:00:00";
            String iDyResponseTimeAutoTotal = "0d 00:00:00";
            String iDyResponseTimeManTotal = "0d 00:00:00";




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
<jsp:forward page="AutoSummaryDateSelect.jsp"/>;
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
<jsp:forward page="AutoSummaryDateSelect.jsp"/>;
<%
                return;
            }
            String strEndDate = strEndYr + strEndMth + strEndDay;
            if (strStartDate.compareTo(strEndDate) > 0) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' must be less than or equal to 'To Date'!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid to date");
%>
<jsp:forward page="AutoSummaryDateSelect.jsp"/>;
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
<jsp:forward page="AutoSummaryDateSelect.jsp"/>;
<%
                return;
            }
            calTemp.set(Integer.parseInt(strEndYr), Integer.parseInt(strEndMth) - 1, 1, 0, 0, 0);
            iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);

            if (Integer.parseInt(strEndDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'To Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport Invalid to date");
%>
<jsp:forward page="AutoSummaryDateSelect.jsp"/>;
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

            boolean bSpecificOCN = false;
            boolean bSpecificState = false;
            boolean bSpecificVendor = false;
            boolean bSpecificSrvcTypCd = false;
            boolean bSpecificActvtyTypCd = false;
            boolean bSpecificPort = false;

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

            String strSrvcTypCd = "";
            String strSrvcTypCdDis = "";
            String strSrvcTypCdWhere = "";
            String[] strSrvcTypCds = alltelRequest.getAttributeValue("SRVC_TYP_CD");
            if (strSrvcTypCds != null) {
                for (int i = 0; i < strSrvcTypCds.length; i++) {
                    if (strSrvcTypCds[i].equals("ALL")) {
                        strSrvcTypCd = "ALL";
                        break;
                    } else {
                        bSpecificSrvcTypCd = true;
                        if (strSrvcTypCd.length() > 0) {
                            strSrvcTypCd += ",";
                        }
                        strSrvcTypCd += "'" + strSrvcTypCds[i] + "'";
                    }
                }
            }
            Log.write(Log.DEBUG_VERBOSE, "strSrvcTypCd=[" + strSrvcTypCd + "]");
            if (bSpecificSrvcTypCd) {
                strSrvcTypCdWhere = " AND R.SRVC_TYP_CD IN (" + strSrvcTypCd + ") ";

                String strQrySrvcTypCd = "SELECT SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T WHERE TYP_IND = 'R' AND SRVC_TYP_CD IN (" + strSrvcTypCd + ")";

                rs = stmt.executeQuery(strQrySrvcTypCd);

                while (rs.next()) {
                    strSrvcTypCdDis += rs.getString("SRVC_TYP_DSCRPTN");
                    strSrvcTypCdDis += "&nbsp;&nbsp;&nbsp;";
                }

                rs.close();
                rs = null;
            }

            String strActvtyTypCd = "";
            String strActvtyTypCdDis = "";
            String strActvtyTypCdWhere = "";
            String[] strActvtyTypCds = alltelRequest.getAttributeValue("ACTVTY_TYP_CD");
            if (strActvtyTypCds != null) {
                for (int i = 0; i < strActvtyTypCds.length; i++) {
                    if (strActvtyTypCds[i].equals("ALL")) {
                        strActvtyTypCd = "ALL";
                        break;
                    } else {
                        bSpecificActvtyTypCd = true;
                        if (strActvtyTypCd.length() > 0) {
                            strActvtyTypCd += ",";
                        }
                        strActvtyTypCd += "'" + strActvtyTypCds[i] + "'";
                    }
                }
            }
            Log.write(Log.DEBUG_VERBOSE, "strActvtyTypCd=[" + strActvtyTypCd + "]");
            if (bSpecificActvtyTypCd) {
                strActvtyTypCdWhere = " AND R.ACTVTY_TYP_CD IN (" + strActvtyTypCd + ") ";

                String strQryActvtyTypCd = "SELECT ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T WHERE TYP_IND = 'R' AND ACTVTY_TYP_CD IN (" + strActvtyTypCd + ")";

                rs = stmt.executeQuery(strQryActvtyTypCd);

                while (rs.next()) {
                    strActvtyTypCdDis += rs.getString("ACTVTY_TYP_DSCRPTN");
                    strActvtyTypCdDis += "&nbsp;&nbsp;&nbsp;";
                }

                rs.close();
                rs = null;
            }

%>

<table width=800 align=center>

    <br><center>
        <SPAN CLASS="header1">A&nbsp;U&nbsp;T&nbsp;O&nbsp;M&nbsp;A&nbsp;T&nbsp;E&nbsp;D&nbsp;&nbsp;&nbsp;A&nbsp;C&nbsp;T&nbsp;I&nbsp;V&nbsp;I&nbsp;T&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;A&nbsp;I&nbsp;L&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S&nbsp;U&nbsp;M&nbsp;M&nbsp;A&nbsp;R&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN><br>
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
        <b><font size="2">Service Type:&nbsp;</font></b><%=strSrvcTypCdDis%>
        <%
                    }
        %>

        <%
                    if (bSpecificActvtyTypCd) {
        %>
        <b><font size="2">Activity Type:&nbsp;</font></b><%=strActvtyTypCdDis%>
        <%
                    }
        %>

    </center><br><br></table>

<%
            //Loop day by day and create this table
            DateFormat dyFmt = new SimpleDateFormat("dd-MMM");
            DateFormat dateRangeFmt = new SimpleDateFormat("MMddyyyy");
            strFromMMDDYYYY = dateRangeFmt.format(calTempFrom.getTime());

            DecimalFormat f = new DecimalFormat("00");
%>
<table width="64%"  align=center cellspacing=0 cellpadding=2>

    <td width="54%" align="right"><a href=AutoSumExcelRpt.jsp>EXCEL</a></td>
    <td width="10%" align="right"><a href=AutoSumCSVRpt.jsp>CSV</a></td>
</tr>
</table>
<table width="90%" id="summaryTable" class="tablesorter" border=1 align=center cellspacing=0 cellpadding=1>
    <thead>
        <tr>
            <th align=center width="5%" bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Day</font></th>

            <th align=center width="5%" bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Manual Review</font></th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Manual Review %</th>

            <th align=center width="5%" bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Rejected, Auto</font></th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Rejected, Manual</th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Reject Total</th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Reject %</th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		FOC, Auto</th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		FOC, Manual</th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		FOC Total</th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		FOC %</th>
            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Simple</th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Non-Simple</th>
            <th align=center width="9%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Avg Response Time, Auto</th>
            <th align=center width="9%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Avg Response Time, Manual</th>

            <th align=center width="9%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Avg Response Time</th>

            <th align=center width="5%" bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Total</th>

        </tr>
    </thead>
    <tbody >
        <%
                    List sumBeanList = new ArrayList();
                    int count = 0;
                    while (calTempFrom.before(calTo)) {
                        //Gather statistics for this day
                        AutoSummaryBean autoSummaryBean = new AutoSummaryBean();
                        String strDyEnding = dyFmt.format(calTempFrom.getTime());

                        calTempFrom.add(Calendar.DATE, 1);

                        strToMMDDYYYY = dateRangeFmt.format(calTempFrom.getTime());
                        Log.write("dates; " + strFromMMDDYYYY + " to " + strToMMDDYYYY);

                        String strQryMAN = "SELECT COUNT(*) FROM AUTOMATION_STATUSES_T A, REQUEST_T R "
                                + "WHERE A.INTERNAL_STATUS = 'MANUAL-REVIEW' AND R.ICARE = 'N' "
                                + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                                + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') "
                                + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                                + "AND A.RQST_VRSN = R.RQST_VRSN "
                                + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere;

                        rs = stmt.executeQuery(strQryMAN);
                        rs.next();
                        iDyMAN = rs.getDouble(1);
                        rs.close();

                        /*                  String strQryREJ = "SELECT COUNT(*) FROM AUTOMATION_STATUSES_T A, REQUEST_T R "
                        + "WHERE A.INTERNAL_STATUS = 'REJECTED' "
                        + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                        + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') "
                        + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                        + "AND A.RQST_VRSN = R.RQST_VRSN "
                        + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere ;


                        rs = stmt.executeQuery(strQryREJ);
                        rs.next();
                        iDyREJ = rs.getDouble(1);
                        rs.close();*/

                        String strQryREJAuto = "SELECT COUNT(*) FROM AUTOMATION_STATUSES_T A, REQUEST_T R "
                                + "WHERE A.INTERNAL_STATUS = 'REJECTED' AND R.ICARE = 'N' "
                                + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                                + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') "
                                + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                                + "AND A.RQST_VRSN = R.RQST_VRSN "
                                + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere;


                        rs = stmt.executeQuery(strQryREJAuto);
                        rs.next();
                        iDyREJAuto = rs.getDouble(1);
                        rs.close();

                        String strQryREJMan = "SELECT COUNT(*) FROM AUTOMATION_STATUSES_T A, REQUEST_T R "
                                + "WHERE R.INN_STTS = 'MANUAL-REJECT' AND R.ICARE = 'N' "
                                + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                                + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') "
                                + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                                + "AND A.RQST_VRSN = R.RQST_VRSN "
                                + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere;


                        rs = stmt.executeQuery(strQryREJMan);
                        rs.next();
                        iDyREJMan = rs.getDouble(1);
                        rs.close();
                        /*
                        String strQryFOC = "SELECT COUNT(*) FROM AUTOMATION_STATUSES_T A, REQUEST_T R "
                        + "WHERE A.INTERNAL_STATUS = 'FOC' "
                        + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                        + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') "
                        + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                        + "AND A.RQST_VRSN = R.RQST_VRSN "
                        + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere ;

                        rs = stmt.executeQuery(strQryFOC);
                        rs.next();
                        iDyFOC = rs.getDouble(1);
                        rs.close();
                         */
                        String strQryFOCAuto = "SELECT COUNT(*) FROM AUTOMATION_STATUSES_T A, REQUEST_T R "
                                + "WHERE A.INTERNAL_STATUS = 'FOC' AND R.ICARE = 'N' "
                                + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                                + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') "
                                + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                                + "AND A.RQST_VRSN = R.RQST_VRSN "
                                + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere;


                        rs = stmt.executeQuery(strQryFOCAuto);
                        rs.next();
                        iDyFOCAuto = rs.getDouble(1);
                        rs.close();

                        String strQryFOCMan = "SELECT COUNT(*) FROM AUTOMATION_STATUSES_T A, REQUEST_T R "
                                + "WHERE R.INN_STTS = 'MANUAL-FOC' AND R.ICARE = 'N' "
                                + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                                + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') "
                                + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                                + "AND A.RQST_VRSN = R.RQST_VRSN "
                                + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere;


                        rs = stmt.executeQuery(strQryFOCMan);
                        rs.next();
                        iDyFOCMan = rs.getDouble(1);
                        rs.close();
                        iDyREJ = iDyREJAuto + iDyREJMan;
                        iDyFOC = iDyFOCAuto + iDyFOCMan;
                        iDyTOT = iDyMAN + iDyREJ + iDyFOC;

                        String strQrySimplePort = "SELECT COUNT(*) FROM AUTOMATION_STATUSES_T A, REQUEST_T R "
                                + "WHERE R.SIMPLE_PORT_FLAG = 'Y' AND R.ICARE = 'N' "
				     + "AND A.INTERNAL_STATUS IN ('FOC','REJECTED','MANUAL-REVIEW') "	
                                + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                                + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') "
                                + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                                + "AND A.RQST_VRSN = R.RQST_VRSN "
                                + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere;
				//out.println(strQrySimplePort);
                        rs = stmt.executeQuery(strQrySimplePort);
                        rs.next();
                        iDySimple = rs.getDouble(1);
                        rs.close();

                        String strQryComplexPort = "SELECT COUNT(*) FROM AUTOMATION_STATUSES_T A, REQUEST_T R "
                                + "WHERE R.SIMPLE_PORT_FLAG = 'N' AND R.ICARE = 'N' "
                                + "AND A.INTERNAL_STATUS IN ('FOC','REJECTED','MANUAL-REVIEW') "

                                // + "AND A.INTERNAL_STATUS = 'MANUAL-REVIEW'"
                                + "AND A.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                                + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') "
                                + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                                + "AND A.RQST_VRSN = R.RQST_VRSN "
                                + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere;

                        rs = stmt.executeQuery(strQryComplexPort);
                        rs.next();
                        iDyComplex = rs.getDouble(1);
                        rs.close();


                        String strAvgResponseTimeAuto = "select floor((AVG((B.RQST_HSTRY_DT_IN-A.RQST_HSTRY_DT_IN)*24*60*60))/86400) || 'd ' ||"
                                + "to_char(to_date(mod(trunc(avg((b.RQST_HSTRY_DT_IN-a.RQST_HSTRY_DT_IN)*24*60*60)),86400),'sssss'),'hh24:mi:ss') AvgResponseTime "
                                + "from request_history_t A, request_history_t B,request_t C,"
                                + "automation_statuses_t D where D.rqst_sqnc_nmbr=C.rqst_sqnc_nmbr and "
                                + "C.rqst_sqnc_nmbr=B.rqst_sqnc_nmbr and A.rqst_sqnc_nmbr=B.rqst_sqnc_nmbr  AND C.ICARE = 'N' "
                                + "and  A.RQST_VRSN=B.RQST_VRSN and A.RQST_VRSN=0 and A.RQST_STTS_CD_IN='SUBMITTED' "
                                + "and B.RQST_STTS_CD_IN in ('REJECTED','FOC') "
                                + "AND D.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                                + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') ";
                        //  + "AND D.MDFD_DT BETWEEN TO_DATE('02022011','MMDDYYYY') AND  TO_DATE('02032011','MMDDYYYY')";

                        rs = stmt.executeQuery(strAvgResponseTimeAuto);
                        rs.next();
                        iDyResponseTimeAuto = rs.getString(1);
                        rs.close();
                        String strAvgResponseTimeMan = "select floor((AVG((B.RQST_HSTRY_DT_IN-A.RQST_HSTRY_DT_IN)*24*60*60))/86400) || 'd ' ||"
                                + "to_char(to_date(mod(trunc(avg((b.RQST_HSTRY_DT_IN-a.RQST_HSTRY_DT_IN)*24*60*60)),86400),'sssss'),'hh24:mi:ss') AvgResponseTime "
                                + "from request_history_t A, request_history_t B,request_t C,"
                                + "automation_statuses_t D where D.rqst_sqnc_nmbr=C.rqst_sqnc_nmbr and "
                                + "C.rqst_sqnc_nmbr=B.rqst_sqnc_nmbr and A.rqst_sqnc_nmbr=B.rqst_sqnc_nmbr  AND C.ICARE = 'N' "
                                + "and  A.RQST_VRSN=B.RQST_VRSN and A.RQST_VRSN=0 and A.RQST_STTS_CD_IN='SUBMITTED' "
                                + "and B.RQST_STTS_CD_IN ='MANUAL-REVIEW' "
                                + "AND D.MDFD_DT BETWEEN TO_DATE('" + strFromMMDDYYYY + "','MMDDYYYY') "
                                + "AND TO_DATE('" + strToMMDDYYYY + "','MMDDYYYY') ";
                        //  + "AND D.MDFD_DT BETWEEN TO_DATE('02022011','MMDDYYYY') AND  TO_DATE('02032011','MMDDYYYY')";

                        rs = stmt.executeQuery(strAvgResponseTimeMan);
                        rs.next();
                        iDyResponseTimeMan = rs.getString(1);
                        rs.close();
                        if (!iDyResponseTimeAuto.equals("d ") && iDyResponseTimeMan.equals("d ")) {
                            iDyResponseTimeMan = "0d 00:00:00";
                            iDyResponseTime = addTimes(iDyResponseTimeAuto, iDyResponseTimeMan);
                        } else if (iDyResponseTimeAuto.equals("d ") && !iDyResponseTimeMan.equals("d ")) {
                            iDyResponseTime = addTimes("0d 00:00:00", iDyResponseTimeMan);
                        } else if (iDyResponseTimeMan.equals("d ") && !iDyResponseTimeAuto.equals("d ")) {
                            iDyResponseTime = addTimes("0d 00:00:00", iDyResponseTimeAuto);
                        } else {
                            iDyResponseTime = addTimes("0d 00:00:00", "0d 00:00:00");
                        }

        %>
        <%! String SPLITER = ":";
            String SPLITED = "d ";

            public String addTimes(String first, String second) {

                int firstSeconds = splitSeconds(first);
                int firstMinutes = splitMinutes(first);
                int firstHours = splitHours(first);
                int firstDays = splitDays(first);
                int secondSeconds = splitSeconds(second);
                int secondMinutes = splitMinutes(second);

                int secondHours = splitHours(second);
                int secondDays = splitDays(second);

                int finalSeconds = 0;
                int finalMinutes = 0;
                int finalHours = 0;
                int finalDays = 0;

                finalSeconds = firstSeconds + secondSeconds;
                finalMinutes = firstMinutes + secondMinutes;
                finalHours = firstHours + secondHours;
                finalDays = firstDays + secondDays;

                if (finalSeconds / 60 > 0) {
                    finalSeconds = finalSeconds - 60;
                    finalMinutes = finalMinutes + 1;
                }
                if (finalMinutes / 60 > 0) {
                    finalMinutes = finalMinutes - 60;
                    finalHours = finalHours + 1;

                }
                if (finalHours / 24 > 0) {
                    finalHours = finalHours - 24;
                    finalDays = finalDays + 1;

                }

                return finalDays + "d " + finalHours + ":" + finalMinutes + ":" + finalSeconds;
            }

            public int splitSeconds(String time) {
                return Integer.parseInt(time.substring(time.lastIndexOf(SPLITER) + 1, time.length()));
            }

            public int splitMinutes(String time) {
                time = time.substring(time.indexOf(SPLITER) + 1, time.length());
                int index = time.indexOf(SPLITER) + 1;
                return Integer.parseInt(time.substring(0, time.indexOf(SPLITER)));
            }

            public int splitHours(String time) {
                time = time.substring((time.indexOf(SPLITED) + 2), time.length());
                return Integer.parseInt(time.substring(0, time.indexOf(SPLITER)));
            }

            public int splitDays(String time) {
                return Integer.parseInt(time.substring(0, time.indexOf(SPLITED)));
            }

            public String formatHHMMSS(long secondsCount) {
                //Calculate the seconds to display:
                int seconds = (int) (secondsCount % 60);
                secondsCount -= seconds;
                //Calculate the minutes:
                long minutesCount = secondsCount / 60;
                long minutes = minutesCount % 60;
                minutesCount -= minutes;
                //Calculate the hours:
                long hoursCount = minutesCount / 60;
                long hours = hoursCount % 24;
                hoursCount -= hours;
                //Calculate the days
                long days = hoursCount / 24;
                return days + "d " + hours + ":" + minutes + ":" + seconds;
            }
        %>
       <tr>

            <td align=center><b><%=strDyEnding%></b></td>

            <td align=center><%=(int) iDyMAN%></td>
            <%
                                if (iDyTOT > 0) {
                                    iPerDyMAN = (int) ((iDyMAN * 100) / iDyTOT + 0.5);
            %>
            <td align=center><%=iPerDyMAN%></td>
            <%
                                        } else {
            %>
            <td align=center>0</td>
            <%            }
            %>
            <td align="center"><%=(int) iDyREJAuto%></td>
            <td align="center"><%=(int) iDyREJMan%></td>
            <td align=center><%=(int) iDyREJ%></td>
            <%
                                if (iDyTOT > 0) {
                                    iPerDyREJ = (int) ((iDyREJ * 100) / iDyTOT + 0.5);
            %>
            <td align=center><%=iPerDyREJ%></td>
            <%
                                        } else {
            %>
            <td align=center>0</td>
            <%            }
            %>
            <td align="center"><%=(int) iDyFOCAuto%></td>
            <td align="center"><%=(int) iDyFOCMan%></td>
            <td align=center><%=(int) iDyFOC%></td>
            <%
                                //iPerDyTemp = iPerDyMAN + iPerDyREJ;
                                //if (iPerDyTemp > 0) {
                                //     iPerDyFOC = 100 - iPerDyTemp;
                                //  } else {
                                //       iPerDyFOC = 0;
                                //   }
                                iPerDyFOC = (int) ((iDyFOC * 100) / iDyTOT + 0.5);

                                if (iPerDyFOC > 0) {
            %>
            <td align=center><%=iPerDyFOC%></td>
            <%
                                        } else {
            %>
            <td align=center>0</td>
            <%            }
            %>
            <td align="center"><%=(int) iDySimple%></td>
            <td align="center"><%=(int) iDyComplex%></td>
            <%
                                if (!iDyResponseTimeAuto.equals("d ")) {
            %>
            <td align=center><%=iDyResponseTimeAuto%></td>
            <%
                                        } else {
            %>
            <td align=center>0d 00:00:00</td>
            <%            }
            %>
            <%
                                if (!iDyResponseTimeMan.equals("d ")) {
            %>
            <td align=center><%=iDyResponseTimeMan%></td>
            <%
                                        } else {
            %>
            <td align=center>0d 00:00:00</td>
            <%            }
            %>
            <td align=center><%=iDyResponseTime%></td>
            <td align=center><%=(int) iDyTOT%></td>
    
            <%
                            iTotMAN += iDyMAN;
                            iTotREJ += iDyREJ;
                            iTotFOC += iDyFOC;
                            iTotREJAuto += iDyREJAuto;
                            iTotREJMan += iDyREJMan;
                            iTotFOCAuto += iDyFOCAuto;
                            iTotFOCMan += iDyFOCMan;
                            iTotSimple += iDySimple;
                            iTotComplex += iDyComplex;




                            if (!iDyResponseTimeMan.equals("d ")) {
                                iDyResponseTimeAutoTotal = addTimes(iDyResponseTimeAutoTotal, iDyResponseTimeAuto);
                            } else {
                                iDyResponseTimeAutoTotal = addTimes(iDyResponseTimeAutoTotal, "0d 00:00:00");
                            }
                            if (!iDyResponseTimeMan.equals("d ")) {
                                iDyResponseTimeManTotal = addTimes(iDyResponseTimeManTotal, iDyResponseTimeMan);
                            } else {
                                iDyResponseTimeManTotal = addTimes(iDyResponseTimeManTotal, "0d 00:00:00");
                            }
                            if (iDyResponseTime != null) {
                                iDyResponseTimeTotal = addTimes(iDyResponseTimeTotal, iDyResponseTime);
                            } else {
                                iDyResponseTimeTotal = addTimes(iDyResponseTimeTotal, "0d 00:00:00");
                            }

                            strFromMMDDYYYY = dateRangeFmt.format(calTempFrom.getTime());
                            count++;
                            autoSummaryBean.setDate(strDyEnding);
                            autoSummaryBean.setManRev((int) iDyMAN);
                            autoSummaryBean.setPerManrev(iPerDyMAN);
                            autoSummaryBean.setRejMan((int) iDyREJMan);
                            autoSummaryBean.setRejAuto((int) iDyREJAuto);
                            autoSummaryBean.setRej((int) iDyREJ);
                            autoSummaryBean.setPerRej(iPerDyREJ);
                            autoSummaryBean.setFocMan((int) iDyFOCMan);
                            autoSummaryBean.setFocAuto((int) iDyFOCAuto);
                            autoSummaryBean.setFoc((int) iDyFOC);
                            autoSummaryBean.setPerFoc(iPerDyFOC);
                            autoSummaryBean.setSimple((int) iDySimple);
                            autoSummaryBean.setComplex((int) iDyComplex);
                            autoSummaryBean.setAvgRespMan(iDyResponseTimeMan);
                            autoSummaryBean.setAvgRespAuto(iDyResponseTimeAuto);
                            autoSummaryBean.setAvgResp(iDyResponseTime);
                            autoSummaryBean.setHorTot((int) iDyTOT);
                            sumBeanList.add(autoSummaryBean);
                        }

                        long responseTimeAuto = splitDays(iDyResponseTimeAutoTotal) * 24 * 60 * 60 + splitHours(iDyResponseTimeAutoTotal) * 60 * 60 + splitMinutes(iDyResponseTimeAutoTotal) * 60 + splitSeconds(iDyResponseTimeAutoTotal);
                        long responseTimeMan = splitDays(iDyResponseTimeManTotal) * 24 * 60 * 60 + splitHours(iDyResponseTimeManTotal) * 60 * 60 + splitMinutes(iDyResponseTimeManTotal) * 60 + splitSeconds(iDyResponseTimeManTotal);
                        long responseTime = splitDays(iDyResponseTimeTotal) * 24 * 60 * 60 + splitHours(iDyResponseTimeTotal) * 60 * 60 + splitMinutes(iDyResponseTimeTotal) * 60 + splitSeconds(iDyResponseTimeTotal);
                        iDyResponseTimeAutoTotal = formatHHMMSS(responseTimeAuto / count);
                        iDyResponseTimeManTotal = formatHHMMSS(responseTimeMan / count);
                        iDyResponseTimeTotal = formatHHMMSS(responseTime / count);

                        session.setAttribute("TableData", sumBeanList);
                        iDyMAN = iDyREJ = iDyFOC = iDyTOT = 0;
                        iGrandTOT = iTotMAN + iTotREJ + iTotFOC;



            %>
       </tr>
    </tbody>
    <tr>
        <td><b>Total</b></td>
        <td align=center><b><%=(int) iTotMAN%></b></td>
        <%
                    if (iGrandTOT > 0) {
                        iPerTotMAN = (int) ((iTotMAN * 100) / iGrandTOT + 0.5);
        %>
        <td align=center><b><%=iPerTotMAN%></b></td>
        <%
                            } else {
        %>
        <td align=center><b>0</b></td>
        <%            }
        %>
        <%if (iTotREJAuto > 0) {
        %>
        <td align=center><b><%=(int) iTotREJAuto%></b></td>
        <%
        } else {
        %>
        <td align=center><b>0</b></td>
        <%            }
        %>
        <%if (iTotREJMan > 0) {
        %>
        <td align=center><b><%=(int) iTotREJMan%></b></td>
        <%
        } else {
        %>
        <td align=center><b>0</b></td>
        <%            }
        %>


        <td align=center><b><%=(int) iTotREJ%></b></td>
        <%
                    if (iGrandTOT > 0) {
                        iPerTotREJ = (int) ((iTotREJ * 100) / iGrandTOT + 0.5);
        %>
        <td align=center><b><%=iPerTotREJ%></b></td>
        <%
                            } else {
        %>
        <td align=center><b>0</b></td>
        <%            }
        %>
        <%if (iTotFOCAuto > 0) {
        %>
        <td align=center><b><%=(int) iTotFOCAuto%></b></td>
        <%
        } else {
        %>
        <td align=center><b>0</b></td>
        <%            }
        %>
        <%if (iTotFOCMan > 0) {
        %>
        <td align=center><b><%=(int) iTotFOCMan%></b></td>
        <%
        } else {
        %>
        <td align=center><b>0</b></td>
        <%            }
        %>


        <td align=center><b><%=(int) iTotFOC%></b></td>
        <%
                    iPerTotTemp = iPerTotMAN + iPerTotREJ;
                    if (iPerTotTemp > 0) {
                        iPerTotFOC = 100 - iPerTotTemp;
                    } else {
                        iPerTotFOC = 0;
                    }
                    if (iPerTotFOC > 0) {
        %>
        <td align=center><b><%=iPerTotFOC%></b></td>
        <%
                            } else {
        %>
        <td align=center><b>0</b></td>
        <%            }
        %>
        <%if (iTotSimple > 0) {
        %>
        <td align=center><b><%= iTotSimple%></b></td>
        <%
        } else {
        %>
        <td align=center><b>0</b></td>
        <%            }
        %>
        <%if (iTotComplex > 0) {
        %>
        <td align=center><b><%= iTotComplex%></b></td>
        <%
        } else {
        %>
        <td align=center><b>0</b></td>
        <%            }
        %>
        <td align=center><b><%=iDyResponseTimeAutoTotal%></b></td>
        <td align=center><b><%=iDyResponseTimeManTotal%></b></td>
        <td align=center><b><%=iDyResponseTimeTotal%></b></td>

        <td align=center><b><%=(int) iGrandTOT%></b></td>
    </tr>

</table><br><br>
<%
            AutoSummaryBean totautoSummaryBean = new AutoSummaryBean();

            totautoSummaryBean.setManRevTot((int) iTotMAN);
            totautoSummaryBean.setPerManrevTot(iPerTotMAN);
            totautoSummaryBean.setRejManTot((int) iTotREJMan);
            totautoSummaryBean.setRejAutoTot((int) iTotREJAuto);
            totautoSummaryBean.setRejTot((int) iTotREJ);
            totautoSummaryBean.setPerRejTot(iPerTotREJ);
            totautoSummaryBean.setFocManTot((int) iTotFOCMan);
            totautoSummaryBean.setFocAutoTot((int) iTotFOCAuto);
            totautoSummaryBean.setFocTot((int) iTotFOC);
            totautoSummaryBean.setPerFocTot(iPerTotFOC);
            totautoSummaryBean.setSimpleTot((int) iTotSimple);
            totautoSummaryBean.setComplexTot((int) iTotComplex);
            totautoSummaryBean.setAvgRespManTot(iDyResponseTimeManTotal);
            totautoSummaryBean.setAvgRespAutoTot(iDyResponseTimeAutoTotal);
            totautoSummaryBean.setAvgRespTot(iDyResponseTimeTotal);
            totautoSummaryBean.setVerTot((int) iGrandTOT);
            session.setAttribute("totals", totautoSummaryBean);

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
        $("#summaryTable").tablesorter();
    }
);
</script>
</BODY>
</HTML>

