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
             * MODULE:	AutoDetailRpt.jsp
             *
             * DESCRIPTION: Daily Detail Report for the Automated Activity
             *
             * AUTHOR:      Andy Wei
             *
             * DATE:        10-05-2009
             *
             * HISTORY:
             *
             */

%>

<%@ include file="i_header.jsp" %>
<head>
    <%            
    String path = request.getContextPath();
    out.println(path);
    %>
    <script type='text/javascript' src='<%=path%>/jquery.js'></script>
    <script type='text/javascript' src='<%=path%>/tablesorter.js'></script>
</head>
<%@page  import="com.automation.reports.bean.AutoDetailBean" %>
<%            final String SECURITY_OBJECT = "PROV_REPORTS";
            if (!sdm.isAuthorized(SECURITY_OBJECT)) {
                Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
                alltelResponse.sendRedirect(SECURITY_URL);
            }

            Connection con = null;

            String strStartYr = alltelRequest.getParameter("from_due_date_yr");
            String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
            String strStartDay = alltelRequest.getParameter("from_due_date_dy");
            if ((strStartYr.length() == 0) || (strStartMth.length() == 0) || (strStartDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid from date");
%>
<jsp:forward page="AutoDetailDateSelect.jsp"/>;
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
<jsp:forward page="AutoDetailDateSelect.jsp"/>;
<%
                return;
            }
            String strEndDate = strEndYr + strEndMth + strEndDay;
            if (strStartDate.compareTo(strEndDate) > 0) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' must be less than or equal to 'To Date'!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid to date");
%>
<jsp:forward page="AutoDetailDateSelect.jsp"/>;
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
<jsp:forward page="AutoDetailDateSelect.jsp"/>;
<%
                return;
            }
            calTemp.set(Integer.parseInt(strEndYr), Integer.parseInt(strEndMth) - 1, 1, 0, 0, 0);
            iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);

            if (Integer.parseInt(strEndDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'To Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport Invalid to date");
%>
<jsp:forward page="AutoDetailDateSelect.jsp"/>;
<%
                return;
            }

            DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);

            Log.write(Log.DEBUG_VERBOSE, "DailyReport: Date:" + strStartDate + " Date:" + strEndDate);

            con = DatabaseManager.getConnection();
            Statement stmt = null;
            Statement stmtforSimple = null;
            ResultSet rs = null;
            ResultSet rsSimple = null;
            stmt = con.createStatement();
            stmtforSimple = con.createStatement();

            boolean bSpecificOCN = false;
            boolean bSpecificState = false;
            boolean bSpecificVendor = false;
            boolean bSpecificSrvcTypCd = false;
            boolean bSpecificActvtyTypCd = false;

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

            String strSimplePortFlag = alltelRequest.getParameter("spFlag");
            String strSimplePortFlagWhere = "";

            Log.write(Log.DEBUG_VERBOSE, "strSimplePortFlag=[" + strSimplePortFlag + "]");
            if (!strSimplePortFlag.equals("NULL")) {
                strSimplePortFlagWhere = " AND R.SIMPLE_PORT_FLAG='" + strSimplePortFlag + "'";
            }

%>

<table width=800 align=center>

    <br><center>
        <SPAN CLASS="header1">A&nbsp;U&nbsp;T&nbsp;O&nbsp;M&nbsp;A&nbsp;T&nbsp;E&nbsp;D&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;A&nbsp;C&nbsp;T&nbsp;I&nbsp;V&nbsp;I&nbsp;T&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;A&nbsp;I&nbsp;L&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;E&nbsp;T&nbsp;A&nbsp;I&nbsp;L&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN><br>
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
<table width="64%"  align=center cellspacing=0 cellpadding=2>

    <td width="54%" align="right"><a href=AutoDetExcelRpt.jsp>EXCEL</a></td>
    <td width="10%" align="right"><a href=AutoDetCSVRpt.jsp>CSV</a></td>
</tr>
</table>
<table width=1540 id="summaryTable" class="tablesorter" border=1 align=center cellspacing=0 cellpadding=1>
    <thead>
        <tr>
            <th align=center width=200 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		PON</font></th>

            <th align=center width=30 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		OCN</font></th>

            <th align=center width=300 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Company</font></th>

            <th align=center width=65 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		External Status</font></th>

            <th align=center width=40 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Version</th>

            <th align=center width=25 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		State</th>

            <th align=center width=80 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		TN</th>

            <th align=center width=260 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Service Type</th>

            <th align=center width=180 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Activity Type</th>

            <th align=center width=130 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Submitted Date</th>

            <th align=center width=130 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		FOC/Reject/Manual Review Date</th>

            <th align=center width=100 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Automation Status</font></th>

            <th align=center width=100 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Simple/Non-Simple</font></th>

            <th align=center width=100 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Actual Response Time</font></th>


        </tr>
    </thead>
    <tbody>
        <%
                    String strQry = "SELECT A.RQST_PON AS PON, R.OCN_CD AS OCN, C.CMPNY_NM AS Company, R.RQST_STTS_CD AS eStatus, A.RQST_VRSN AS Ver, "
                            + "R.OCN_STT AS State, R.ATN AS TN, S.SRVC_TYP_DSCRPTN AS ServiceType, "
                            + "T.ACTVTY_TYP_DSCRPTN AS ActivityType, TO_CHAR(H.RQST_HSTRY_DT_OUT, 'MM-DD-YYYY HH12:MI:SS AM') AS SubmittedDate, "
                            + "TO_CHAR(A.MDFD_DT, 'MM-DD-YYYY HH12:MI:SS AM') AS FRMDate, A.INTERNAL_STATUS AS iStatus,"
                            + "floor(((a.mdfd_dt-h.rqst_hstry_dt_out)*24*60*60)/86400) || 'd ' ||to_char(to_date(mod(trunc((a.mdfd_dt-h.rqst_hstry_dt_out)*24*60*60),86400),'sssss'),'hh24:mi:ss') as responseTime,r.SIMPLE_PORT_FLAG as simpleflag "
                            + "FROM AUTOMATION_STATUSES_T A, REQUEST_T R, SERVICE_TYPE_T S, ACTIVITY_TYPE_T T, REQUEST_HISTORY_T H, COMPANY_T C "
                            + "WHERE A.INTERNAL_STATUS IN ('FOC', 'REJECTED', 'MANUAL-REVIEW') "
                            + "AND A.MDFD_DT BETWEEN TO_DATE('" + strStartDate + "00:00:01','YYYYMMDD HH24:MI:SS') "
                            + "AND TO_DATE('" + strEndDate + "23:59:59','YYYYMMDD HH24:MI:SS') "
                            + "AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                            + "AND A.RQST_VRSN = R.RQST_VRSN "
                            + "AND R.SRVC_TYP_CD = S.SRVC_TYP_CD "
                            + "AND S.TYP_IND = 'R' "
                            + "AND R.ACTVTY_TYP_CD = T.ACTVTY_TYP_CD "
                            + "AND T.TYP_IND = 'R' "
                            + "AND R.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR "
                            + "AND A.RQST_SQNC_NMBR = H.RQST_SQNC_NMBR "
                            + "AND A.RQST_VRSN = H.RQST_VRSN AND R.ICARE = 'N' "
                            + "AND H.RQST_HSTRY_SQNC_NMBR = (SELECT MIN(Q.RQST_HSTRY_SQNC_NMBR) FROM REQUEST_HISTORY_T Q "
                            + "WHERE H.RQST_SQNC_NMBR = Q.RQST_SQNC_NMBR AND H.RQST_VRSN = Q.RQST_VRSN AND Q.RQST_STTS_CD_OUT = 'SUBMITTED') "
                            + strOCNWhere + strStateWhere + strVendorWhere + strSrvcTypCdWhere + strActvtyTypCdWhere + strSimplePortFlagWhere
                            + "ORDER BY eStatus, State";


                    rs = stmt.executeQuery(strQry);
                    String strPON = "";
                    String strCC = "";
                    String strCompany = "";
                    String strExternalStatus = "";
                    String strVer = "";
                    String strSt = "";
                    String strTN = "";
                    String strTNforSimple = "";
                    String strServiceType = "";
                    String strActivityType = "";
                    String strSubmittedDate = "";
                    String strFRMDate = "";
                    String strInternalStatus = "";
                    String strSimpleFlag = "";
                    String strActualResponseTime = "0d 00:00:00";
                    List detBeanList = new ArrayList();
                    if (!rs.next()) {
                                    AutoDetailBean autoDetailBean = new AutoDetailBean();
                                    autoDetailBean.setPon("");
                                    autoDetailBean.setOcn("");
                                    autoDetailBean.setCompany("");
                                    autoDetailBean.setExternalstatus("");
                                    autoDetailBean.setVersion("");
                                    autoDetailBean.setState("");
                                    autoDetailBean.setServicetype("");
                                    autoDetailBean.setActivitytype("");
                                    autoDetailBean.setSubmitteddate("");
                                    autoDetailBean.setFocrejMandate("");
                                    autoDetailBean.setAutoStatus("");
                                    autoDetailBean.setSimpleflag("");
                                    autoDetailBean.setActResponseTime("");
                                    autoDetailBean.setTn("");
                                    detBeanList.add(autoDetailBean);
                                    session.setAttribute("detailList", detBeanList);
                                }
                    while (rs.next()) {
                        AutoDetailBean autoDetailBean = new AutoDetailBean();
                        strPON = rs.getString("PON");
                        strCC = rs.getString("OCN");
                        strCompany = rs.getString("Company");
                        strExternalStatus = rs.getString("eStatus");
                        strVer = rs.getString("Ver");
                        strSt = rs.getString("State");
                        strTN = rs.getString("TN");
                        autoDetailBean.setTn(strTN);
                        strServiceType = rs.getString("ServiceType");
                        strActivityType = rs.getString("ActivityType");
                        strSubmittedDate = rs.getString("SubmittedDate");
                        strFRMDate = rs.getString("FRMDate");
                        strInternalStatus = rs.getString("iStatus");
                        strSimpleFlag = rs.getString("simpleflag");
                        strActualResponseTime = rs.getString("responseTime");
                        if (strSimpleFlag != null &&( strSimpleFlag.equals("N") ||strSimpleFlag.equals("Y") || strSimpleFlag.equals("N/A")) ){
                            String strQueryforTN = "SELECT N.NP_SD_PORTEDNBR FROM NP_SD_T N,REQUEST_T R WHERE "
                                    + "R.RQST_PON='" + strPON + "' AND R.RQST_SQNC_NMBR=N.RQST_SQNC_NMBR";
                            rsSimple = stmtforSimple.executeQuery(strQueryforTN);
                            if (rsSimple.next()) {
                                strTNforSimple = rsSimple.getString("NP_SD_PORTEDNBR");
                                autoDetailBean.setTn(strTNforSimple);
                            }
                            rsSimple.close();
                            rsSimple = null;
                            autoDetailBean.setPon(strPON);
                            autoDetailBean.setOcn(strCC);
                            autoDetailBean.setCompany(strCompany);
                            autoDetailBean.setExternalstatus(strExternalStatus);
                            autoDetailBean.setVersion(strVer);
                            autoDetailBean.setState(strSt);
                            autoDetailBean.setServicetype(strServiceType);
                            autoDetailBean.setActivitytype(strActivityType);
                            autoDetailBean.setSubmitteddate(strSubmittedDate);
                            autoDetailBean.setFocrejMandate(strFRMDate);
                            autoDetailBean.setAutoStatus(strInternalStatus);
                            autoDetailBean.setSimpleflag(strSimpleFlag);
                            autoDetailBean.setActResponseTime(strActualResponseTime);
                            detBeanList.add(autoDetailBean);
                        }
                        session.setAttribute("detailList", detBeanList);
        %>

        <tr>

            <td align=left><%=strPON%></td>
            <td align=left><%=strCC%></td>
            <td align=left><%=strCompany%></td>
            <td align=left><%=strExternalStatus%></td>
            <td align=left><%=strVer%></td>
            <td align=left><%=strSt%></td>
            <% if (strSimpleFlag != null && strSimpleFlag.equals("N")) {
            %>
            <td align=left><%=strTN%></td>
            <%} else if (strSimpleFlag != null && strSimpleFlag.equals("Y")) {%>
            <td align=left><%=strTNforSimple%></td>
            <%} else {%>
            <td align=left>UNKNOWN</td>
            <%}%>

            <td align=left><%=strServiceType%></td>
            <td align=left><%=strActivityType%></td>
            <td align=left><%=strSubmittedDate%></td>
            <td align=left><%=strFRMDate%></td>
            <td align=left><%=strInternalStatus%></td>
            <% if (strSimpleFlag != null && strSimpleFlag.equals("N")) {
            %>
            <td align=left>Complex</td>
            <%} else if (strSimpleFlag != null && strSimpleFlag.equals("Y")) {%>
            <td align=left>Simple</td>
            <%} else {%>
            <td align=left>UNKNOWN</td>
            <%}%>
            <td align=left><%=strActualResponseTime%></td>
        </tr>
        <%
                    }

                    rs.close();
                    rs = null;
                    stmtforSimple.close();
                    stmtforSimple = null;
                    DatabaseManager.releaseConnection(con);
        %>
    </tbody>
</table>
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
