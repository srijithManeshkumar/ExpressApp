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

             * MODULE:	RejDetailRpt.jsp
             *
             * DESCRIPTION:
             *
             * AUTHOR:      Satish Talluri
             *
             * DATE:        May 17, 2011, 3:04:14 PM
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
<%@page  import="com.automation.reports.bean.RejectionDetailBean"  %>
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
<jsp:forward page="RejectionDetailDateSelect.jsp"/>;
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
<jsp:forward page="RejectionDetailDateSelect.jsp"/>;
<%
                return;
            }
            String strEndDate = strEndYr + strEndMth + strEndDay;
            if (strStartDate.compareTo(strEndDate) > 0) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' must be less than or equal to 'To Date'!");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport: Invalid to date");
%>
<jsp:forward page="RejectionDetailDateSelect.jsp"/>;
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
<jsp:forward page="RejectionDetailDateSelect.jsp"/>;
<%
                return;
            }
            calTemp.set(Integer.parseInt(strEndYr), Integer.parseInt(strEndMth) - 1, 1, 0, 0, 0);
            iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);

            if (Integer.parseInt(strEndDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("reportstat", "'To Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "DailyReport Invalid to date");
%>
<jsp:forward page="RejectionDetailDateSelect.jsp"/>;
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
            Statement stmtRej = null;
            ResultSet rsRej = null;
            stmtRej = con.createStatement();
            Statement stmtci = null;
            ResultSet rsci = null;
            stmtci = con.createStatement();

            boolean bSpecificOCN = false;
            boolean bSpecificState = false;
            boolean bSpecificVendor = false;
            boolean bSpecificSrvcTypCd = false;
            boolean bSpecificActvtyTypCd = false;
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
                        strRejectioncd += "B.RSN_CD_SQNC_NMBR LIKE '%" + strRejectioncds[i] + "%'";

                    }
                }
            }
            Log.write(Log.DEBUG_VERBOSE, "strRejectioncd=[" + strRejectioncd + "]");
            if (bSpecificSrvcTypCd) {
                strRejectioncodeWhere = " AND " + strRejectioncd + "";
                String strQryRejection = "SELECT RSN_CD_DSCRPTN FROM REASON_CODE_T B WHERE " + strRejectioncd;
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
        <SPAN CLASS="header1">R&nbsp;E&nbsp;J&nbsp;E&nbsp;C&nbsp;T&nbsp;I&nbsp;O&nbsp;N&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;E&nbsp;T&nbsp;A&nbsp;I&nbsp;L&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN><br>
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
 <table width="64%"  align=center cellspacing=0 cellpadding=2>

     <td width="54%" align="right"><a href=RejDetExcelRpt.jsp>EXCEL</a></td>
     <td width="10%" align="right"><a href=RejDetCSVRpt.jsp>CSV</a></td>
    </tr>
 </table>
<table width=90% id="rejdetTable" class="tablesorter" border=1 align=center cellspacing=0 cellpadding=1>
    <thead>
    <tr>
        <th align=center width=10% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		PON</font></th>

        <th align=center width=8% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		TN</font></th>

        <th align=center width=12% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		VendorName</font></th>

        <th align=center width=4% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		OCN</font></th>

        <th align=center width=4% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		State</th>

        <th align=center width=12% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Internal Rejection Reason Code</th>

        <th align=center width=18% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Reason (RDET)</th>

        <th align=center width=10% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		CI Drop Down Reason</th>
        <th align=center width=10% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Submitted Date</th>
        <th align=center width=10%% bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Rejection Date</th>

    </tr>
        </thead>
        <tbody>

    <%
		/* Modified for rejection reports fix -- To add the MANUAL-REJECT 
		( REJECTED records from REQUEST_T R ) records , UNION query added with 
		R.INN_STTS = 'MANUAL-REJECT' and A.INTERNAL_STATUS ='PRE-REJECT' 
		and CI_T table removed from the previous query ,CI.BB (CI DROP DOWN REASON) value added in the code.*/
		
                String strQry = "SELECT DISTINCT A.RQST_PON AS PON,R.ATN AS TN,R.OCN_CD AS OCN,C.CMPNY_NM AS Company,R.OCN_STT AS State,"
                        + "SUBSTR(B.RSN_CD_SQNC_NMBR,1,INSTR(B.RSN_CD_SQNC_NMBR,'-')) AS REASONCODE,TO_CHAR(H.RQST_HSTRY_DT_OUT, 'MM-DD-YYYY HH12:MI:SS AM') AS SubmittedDate,"
                        + "TO_CHAR(A.MDFD_DT, 'MM-DD-YYYY HH12:MI:SS AM') AS REJECTEDDATE,r.SIMPLE_PORT_FLAG as simpleflag ,A.RQST_SQNC_NMBR AS RQSTSQNCNMBR "
                        + "FROM AUTOMATION_STATUSES_T A, AUTOMATION_RESULTS_T B, REQUEST_T R, REQUEST_HISTORY_T H, COMPANY_T C"
                        + " WHERE A.INTERNAL_STATUS = 'REJECTED' AND "
                        + "A.MDFD_DT BETWEEN TO_DATE('" + strStartDate + "00:00:01','YYYYMMDD HH24:MI:SS') "
                        + "AND TO_DATE('" + strEndDate + "23:59:59','YYYYMMDD HH24:MI:SS') "
                        + "AND A.RQST_SQNC_NMBR=B.RQST_SQNC_NMBR AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                        + "AND A.RQST_VRSN = R.RQST_VRSN "
                        + "AND A.RQST_VRSN = B.RQST_VER "
                        + "AND B.VALIDATION_RESULT='N' AND R.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR "
                        + "AND A.RQST_SQNC_NMBR = H.RQST_SQNC_NMBR AND A.RQST_VRSN = H.RQST_VRSN "
                        + "AND H.RQST_HSTRY_SQNC_NMBR = (SELECT MIN(Q.RQST_HSTRY_SQNC_NMBR) FROM REQUEST_HISTORY_T Q "
                        + "WHERE H.RQST_SQNC_NMBR = Q.RQST_SQNC_NMBR AND H.RQST_VRSN = Q.RQST_VRSN "
                        + "AND Q.RQST_STTS_CD_OUT = 'REJECTED') "
                        + strOCNWhere + strStateWhere + strVendorWhere + strRejectioncodeWhere + strOrderFlagWhere
                        + " UNION "
                        + "SELECT DISTINCT A.RQST_PON AS PON,R.ATN AS TN,R.OCN_CD AS OCN,C.CMPNY_NM AS Company,R.OCN_STT AS State,"
                        + "SUBSTR(B.RSN_CD_SQNC_NMBR,1,INSTR(B.RSN_CD_SQNC_NMBR,'-')) AS REASONCODE,TO_CHAR(H.RQST_HSTRY_DT_OUT, 'MM-DD-YYYY HH12:MI:SS AM') AS SubmittedDate,"
                        + "TO_CHAR(A.MDFD_DT, 'MM-DD-YYYY HH12:MI:SS AM') AS REJECTEDDATE,r.SIMPLE_PORT_FLAG as simpleflag ,A.RQST_SQNC_NMBR AS RQSTSQNCNMBR "
                        + "FROM AUTOMATION_STATUSES_T A, AUTOMATION_RESULTS_T B, REQUEST_T R, REQUEST_HISTORY_T H, COMPANY_T C "
                        + "WHERE R.INN_STTS = 'MANUAL-REJECT' and A.INTERNAL_STATUS ='PRE-REJECT' AND "
                        + "A.MDFD_DT BETWEEN TO_DATE('" + strStartDate + "00:00:01','YYYYMMDD HH24:MI:SS') "
                        + "AND TO_DATE('" + strEndDate + "23:59:59','YYYYMMDD HH24:MI:SS') "
                        + "AND A.RQST_SQNC_NMBR=B.RQST_SQNC_NMBR AND A.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR "
                        + "AND A.RQST_VRSN = R.RQST_VRSN "
                        + "AND A.RQST_VRSN = B.RQST_VER  "
                        + "AND B.VALIDATION_RESULT in ('N','M')  AND R.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR "
                        + "AND A.RQST_SQNC_NMBR = H.RQST_SQNC_NMBR AND A.RQST_VRSN = H.RQST_VRSN "
                        + "AND H.RQST_HSTRY_SQNC_NMBR = (SELECT MIN(Q.RQST_HSTRY_SQNC_NMBR) FROM REQUEST_HISTORY_T Q "
                        + "WHERE H.RQST_SQNC_NMBR = Q.RQST_SQNC_NMBR AND H.RQST_VRSN = Q.RQST_VRSN "
                        + "AND Q.RQST_STTS_CD_OUT = 'REJECTED') "
                        + strOCNWhere + strStateWhere + strVendorWhere + strRejectioncodeWhere + strOrderFlagWhere
                        + " ORDER BY PON";
		
			
                
                rs = stmt.executeQuery(strQry);
                String strPON = "";
                String strCC = "";
                String strCompany = "";
                String strSt = "";
                String strTN = "";
                String strTNforSimple = "";
                String strRsncdsqno = "";
                String strReasonCode = "";
                String strReasondesc = "";
                String strciReason = "";
                String strSubmittedDate = "";
                String strFRMDate = "";
                String strSimpleFlag = "";
                String Rqstsqncnmbr = "";
                List detailList = new ArrayList();
                while (rs.next()) {
                    RejectionDetailBean rejectionDetailBean = new RejectionDetailBean();
                    strPON = rs.getString("PON");
                    strCC = rs.getString("OCN");
                    strCompany = rs.getString("Company");
                    strSt = rs.getString("State");
                    strTN = rs.getString("TN");
                    rejectionDetailBean.setTn(strTN);
                    strRsncdsqno = rs.getString("REASONCODE");
                    Rqstsqncnmbr = rs.getString("RQSTSQNCNMBR");
                    String rejectionSeno = strRsncdsqno.substring(0, strRsncdsqno.indexOf("-"));
                    String internalRejQuery = "SELECT RSN_CD_TYP,RSN_CD_DSCRPTN FROM REASON_CODE_T WHERE RSN_CD_SQNC_NMBR='" + rejectionSeno + "'";
                    rsRej = stmtRej.executeQuery(internalRejQuery);
                    if (rsRej.next()) {
                        strReasonCode = rsRej.getString("RSN_CD_TYP");
                        strReasondesc = rsRej.getString("RSN_CD_DSCRPTN");
                    }
                    rsRej.close();
                    rsRej = null;
                    strSubmittedDate = rs.getString("SubmittedDate");
                    String internalCIQuery = "SELECT CI_BB FROM CI_T WHERE RQST_SQNC_NMBR ='" + Rqstsqncnmbr + "'";
		      	
                    rsci = stmtci.executeQuery(internalCIQuery);
                    if(rsci.next())
                        {
                        strciReason = rsci.getString("CI_BB");
                        }
                    rsci.close();
                    rsci=null;
                    strFRMDate = rs.getString("REJECTEDDATE");                    
                    strSimpleFlag = rs.getString("simpleflag");
                    if (strSimpleFlag != null && strSimpleFlag.equals("Y")) {
                        String strQueryforTN = "SELECT N.NP_SD_PORTEDNBR FROM NP_SD_T N,REQUEST_T R WHERE "
                                + "R.RQST_PON='" + strPON + "' AND R.RQST_SQNC_NMBR=N.RQST_SQNC_NMBR";
				
                        rsSimple = stmtforSimple.executeQuery(strQueryforTN);
                        if (rsSimple.next()) {
                            strTNforSimple = rsSimple.getString("NP_SD_PORTEDNBR");
                            rejectionDetailBean.setTn(strTNforSimple);
                        }
                        rsSimple.close();
                        rsSimple = null;
                    }
                    rejectionDetailBean.setPon(strPON);
                    rejectionDetailBean.setVendorname(strCompany);
                    rejectionDetailBean.setOcn(strCC);
                    rejectionDetailBean.setState(strSt);
                    rejectionDetailBean.setRejreasoncode(strReasonCode);
                    rejectionDetailBean.setRejdet(strReasondesc);
                    rejectionDetailBean.setCireason(strciReason);
                    rejectionDetailBean.setSubmitteddate(strSubmittedDate);
                    rejectionDetailBean.setRejectiondate(strFRMDate);
                    detailList.add(rejectionDetailBean);
    %>
    <tr>

        <td align=left><%=strPON%></td>
        <% if (strSimpleFlag != null && strSimpleFlag.equals("N")) {
        %>
        <td align=left><%=strTN%></td>
        <%} else if (strSimpleFlag != null && strSimpleFlag.equals("Y")) {%>
        <td align=left><%=strTNforSimple%></td>
        <%} else {%>
        <td align=left>UNKNOWN</td>
        <%}%>
        <td align=left><%=strCompany%></td>
        <td align=left><%=strCC%></td>
        <td align=left><%=strSt%></td>
        <td align=left><%=strReasonCode%></td>
        <td align=left><%=strReasondesc%></td>
        <td align=left><%=strciReason%></td>
        <td align=left><%=strSubmittedDate%></td>
        <td align=left><%=strFRMDate%></td>
    </tr>
    
    <%
                }
                session.setAttribute("detailList", detailList);
                stmtRej.close();
                stmtRej = null;
                stmtforSimple.close();
                stmtforSimple = null;
                rs.close();
                rs = null;
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
        $("#rejdetTable").tablesorter();
    }
);
</script>
</BODY>
</HTML>
