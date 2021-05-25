<%
            /**
             * NOTICE:
             *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
             *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
             *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
             *
             *			COPYRIGHT (C) 2005
             *				BY
             *			Windstream COMMUNICATIONS INC.
             */
            /**
             * MODULE:	DslReportByState.jsp
             *
             * DESCRIPTION: Dsls orders and statses by states. User picks a date range from DslDateSelect.jsp.
             *
             * AUTHOR:      Express Development Team WB
             *
             * DATE:        02-25-2005
             *
             * HISTORY:
             * EK: MOD_DATE:   02-25-2005
             */

%>

<%@ include file="i_header.jsp" %>
<%@ include file="ExpressUtil.jsp" %>
<%            String path = request.getContextPath();
%>
<script type='text/javascript' src='<%=path%>/jquery.js'></script>
<script type='text/javascript' src='<%=path%>/tablesorter.js'></script>
<%
            final String SECURITY_OBJECT = "PROV_REPORTS";
            if (!sdm.isAuthorized(SECURITY_OBJECT)) {
                Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
                alltelResponse.sendRedirect(SECURITY_URL);
            }

            // Did they cancel?
            if (alltelRequest.getParameter("SUBMITBUTTON").equals("Cancel")) {
%>
<jsp:forward page="Reports.jsp"/>;
<%
                return;
            }

            PreparedStatement stmt = null;
            int iCmpnyCount = 0;
            long lIntervalTotals = 0;
            String[] srvtypes = alltelRequest.getAttributeValue("srvtype");
            String strStartYr = alltelRequest.getParameter("from_due_date_yr");
            String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
            String strStartDay = alltelRequest.getParameter("from_due_date_dy");
            if ((strStartYr.length() == 0) || (strStartMth.length() == 0) || (strStartDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("dslstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid from date");
%>
<jsp:forward page="DslDateSelect.jsp"/>;
<%
                return;
            }
            //String strState = alltelRequest.getParameter("state" ).trim();
            String[] strStates = alltelRequest.getAttributeValue("state");
            String StatesClause = " ( ? ";
            boolean allStates = false;
            if (strStates[0].equals("__")) {
                allStates = true;
            }
            if (!allStates) {
                for (int j = 1; j < strStates.length; j++) {
                    StatesClause += ",? ";
                    if (strStates[j].equals("__")) {
                        allStates = true;
                        break;
                    }
                }
                StatesClause += " )";
            }



            // build types clause
            String SrvcClause = " ( ? ";
            boolean allSrvces = false;
            if (srvtypes[0].equals("0")) {
                allSrvces = true;
            }
            if (!allSrvces) {
                for (int j = 1; j < srvtypes.length; j++) {
                    SrvcClause += ",? ";
                    if (srvtypes[j].equals("0")) {
                        allSrvces = true;
                        break;
                    }
                }
                SrvcClause += " )";
            }

            String strStartDate = strStartYr + strStartMth + strStartDay;
            String strEndYr = alltelRequest.getParameter("to_due_date_yr");
            String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
            String strEndDay = alltelRequest.getParameter("to_due_date_dy");
            if ((strEndYr.length() == 0) || (strEndMth.length() == 0) || (strEndDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("dslstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid to date");
%>
<jsp:forward page="DslDateSelect.jsp"/>;
<%
                return;
            }
            String strEndDate = strEndYr + strEndMth + strEndDay;
            if (strStartDate.compareTo(strEndDate) > 0) {
                alltelRequest.getHttpRequest().setAttribute("dslstat", "'From Date' must be less than or equal to 'To Date'!");
                Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid to date");
%>
<jsp:forward page="DslDateSelect.jsp"/>;
<%
                return;
            }
            //Check days of month and adjust if necessary ...
            Calendar calTemp = Calendar.getInstance();
            calTemp.set(Integer.parseInt(strStartYr), Integer.parseInt(strStartMth) - 1, 1, 0, 0, 0);
            int iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (Integer.parseInt(strStartDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("dslstat", "'From Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid from date");
%>
<jsp:forward page="DslDateSelect.jsp"/>;
<%
                return;
            }
            calTemp.set(Integer.parseInt(strEndYr), Integer.parseInt(strEndMth) - 1, 1, 0, 0, 0);
            iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (Integer.parseInt(strEndDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("dslstat", "'To Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid to date");
%>
<jsp:forward page="DslDateSelect.jsp"/>;
<%
                return;
            }


            Connection conn = null;
            ResultSet rs = null;
            ResultSet rset1 = null;
            PreparedStatement pstmt2 = null;
            String strHTMLsrcTypes = "";

            /***** This section gets the names of services types and display them back on users screen ******/
            String strSrvcTypNames = "	select SRVC_TYP_DSCRPTN "
                    + " from SERVICE_TYPE_T  "
                    + " where SRVC_TYP_CD " + (allSrvces ? " > ? " : " IN " + SrvcClause);

            int n = 0;
            if (!allSrvces) {

                try {
                    conn = DatabaseManager.getConnection();
                    pstmt2 = conn.prepareStatement(strSrvcTypNames);
                    pstmt2.clearParameters();
                    for (n = 0; n < srvtypes.length; n++) {
                        pstmt2.setString(n + 1, srvtypes[n]);

                    }
                    rset1 = pstmt2.executeQuery();
                    while (rset1.next()) {
                        strHTMLsrcTypes += rset1.getString(1) + "&nbsp&nbsp&nbsp";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.write(Log.DEBUG_VERBOSE, "DSL Activity Report 001: Caught exception e=[" + e.toString() + "]");
                    //DatabaseManager.releaseConnection(conn);
                } finally { // keep connection open for next try
                    try {
                        pstmt2.close();
                        pstmt2 = null;
                        rset1.close();
                        rset1 = null;
                    } catch (Exception eee) {
                    }
                }
            }
            strHTMLsrcTypes = strHTMLsrcTypes.equals("") ? "All Services" : strHTMLsrcTypes;

            String strDateFormat = "YYYYMMDD HH24:MI:SS";
            DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
            PreparedStatement pstmt = null;
            Hashtable statuses = new Hashtable(3);
            statuses.put("COMPLETED", "0");
            statuses.put("DISQUALIFIED", "0");
            statuses.put("SUBMITTED", "0");
            String strQuery1 = "SELECT count( DSL_STTS_CD_IN ), DSL_RQST_STATE,  DSL_STTS_CD_IN "
                    + " FROM  DSL_REPORT_V DRQ  WHERE "
                    + "  DSL_STTS_CD_IN <> DSL_STTS_CD_OUT "
                    + " AND DSL_HSTRY_DT_IN BETWEEN TO_DATE( ?, ? )"
                    + " AND TO_DATE( ?, ?) "
                    + " AND DSL_STTS_CD_IN IN ( ?, ?, ?) "
                    + " AND " + (allSrvces ? " 1 = ? " : "  SRVC_TYP_CD IN " + SrvcClause)
                    + " AND DSL_RQST_STATE " + (allStates ? " like ? " : " IN " + StatesClause)
                    + " GROUP BY DSL_RQST_STATE, DSL_STTS_CD_IN ORDER BY DSL_RQST_STATE, DSL_STTS_CD_IN ";


%>
<center>
    <table align=center width="100%" cellspacing=0 cellpadding=0>
        <tr>
            <TH width="100%" align=center bgcolor="#7AABDE"><SPAN class="barheader">
                    D&nbsp;S&nbsp;L&nbsp;&nbsp;&nbsp;&nbsp;A&nbsp;C&nbsp;T&nbsp;I&nbsp;V&nbsp;I&nbsp;T&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;&nbsp;&nbsp;&nbsp;</SPAN> </th>
            </th>
        </tr>
    </table>
    <p>
        <span class=rowheader>Date&nbsp;Range:</span>&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%><br>
        <span class=rowheader>States:</span> <%=getStateFullNames(strStates)%><br>
        <span class=rowheader>Service Types:</span> <%=strHTMLsrcTypes%><BR>
        <span class=rowheader>Effective:</span>&nbsp;<%= dFmt.format(new java.util.Date())%>
    </p>
</center>

<table id="DSLRSTable" class="tablesorter"  width=50% border=0 align=center cellspacing=1 cellpadding=1>
    <thead>
        <tr class=tHeader2 style="color: #777">
            <th align=center>State</th>
            <th align=center>&nbsp;Total<br>Submitted&nbsp;</th>
            <th align=center>&nbsp;Total<br>Disqualified&nbsp;</th>
            <th align=center%>&nbsp;Total<br>Completed&nbsp;</th>

        </tr>
    </thead>
    <tbody>
        <%

                    String strCurrent_ST = "";
                    int iTotal_com = 0;
                    int iTotal_disq = 0;
                    int iTotal_sub = 0;
                    String strTemp1 = "";
                    String strTemp2 = "";
                    String strTemp3 = "";
                    int counter = 0, istCouter = 0;

                    StringBuffer sb = new StringBuffer(100);
                    String strRowClass = "rowodd";
                    String strTempEdate = strEndDate + " 23:59:59";
                    String strTempSdate = strStartDate + " 00:00:00";
                    Log.write(Log.DEBUG_VERBOSE, strQuery1);
                    try {

                        if (allSrvces) {
                            // note, if all services was selected,
                            //connection is not opened yet at this point.
                            conn = DatabaseManager.getConnection();
                        }
                        pstmt = conn.prepareStatement(strQuery1);
                        pstmt.clearParameters();
                        pstmt.setString(1, strTempSdate);
                        pstmt.setString(2, strDateFormat);
                        pstmt.setString(3, strTempEdate);
                        pstmt.setString(4, strDateFormat);
                        pstmt.setString(5, "COMPLETED");
                        pstmt.setString(6, "DISQUALIFIED");
                        pstmt.setString(7, "SUBMITTED");
                        int k = 0;
                        int j = 8;

                        if (allSrvces) {
                            pstmt.setInt(8, 1);
                            j++;
                        } else {
                            for (; k < srvtypes.length; k++) {
                                pstmt.setString(8 + k, srvtypes[k]);
                                j++;
                            }
                        }

                        if (allStates) {
                            pstmt.setString(j, "__");

                        } else {
                            for (int i = 0; i < strStates.length; i++) {
                                pstmt.setString(j + i, strStates[i]);
                            }
                        }

                        rs = pstmt.executeQuery();
                        while (rs.next()) {


                            if (counter > 0 && !strCurrent_ST.equals(rs.getString(2))) {

                                if (istCouter % 2 == 0) {
                                    strRowClass = "roweven";
                                } else {
                                    strRowClass = "rowodd";
                                }
                                sb.append("<tr class=" + strRowClass + "><td align=center>");
                                sb.append(getStateFullName(strCurrent_ST));
                                sb.append("<td align=center> " + (strTemp1 = ((String) statuses.get("SUBMITTED") != null ? (String) statuses.get("SUBMITTED") : "0")) + " </td> ");
                                sb.append("<td align=center> " + (strTemp3 = ((String) statuses.get("DISQUALIFIED") != null ? (String) statuses.get("DISQUALIFIED") : "0")) + " </td> ");
                                sb.append("<td align=center> " + (strTemp2 = ((String) statuses.get("COMPLETED") != null ? (String) statuses.get("COMPLETED") : "0")) + " </td> </tr>");
                                iTotal_sub += Integer.parseInt(strTemp1);
                                iTotal_com += Integer.parseInt(strTemp2);
                                iTotal_disq += Integer.parseInt(strTemp3);
                                istCouter++;
                                statuses.clear();
                            }
                            statuses.put(rs.getString(3), rs.getString(1));
                            counter++;
                            strCurrent_ST = rs.getString(2);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.write(Log.DEBUG_VERBOSE, "DSL Activity Report 002: Caught exception e=[" + e.toString() + "]");
                    } finally {
                        try {
                            rs.close();
                            rs = null;
                            pstmt.close();
                            pstmt = null;
                        } catch (Exception eee) {
                        }
                        DatabaseManager.releaseConnection(conn);
                    }
                    // if only 1 state was  selected:
                    if (statuses.size() > 0) {
                        if (istCouter % 2 == 0) {
                            strRowClass = "roweven";
                        } else {
                            strRowClass = "rowodd";
                        }
                        sb.append("<tr class=" + strRowClass + "><td align=center widtd=26% >");
                        sb.append(getStateFullName((strCurrent_ST.equals("") ? strStates[0] : strCurrent_ST)));
                        sb.append("<td align=center> " + (strTemp1 = ((String) statuses.get("SUBMITTED") != null ? (String) statuses.get("SUBMITTED") : "0")) + " </td> ");
                        sb.append("<td align=center> " + (strTemp3 = ((String) statuses.get("DISQUALIFIED") != null ? (String) statuses.get("DISQUALIFIED") : "0")) + " </td> ");
                        sb.append("<td align=center> " + (strTemp2 = ((String) statuses.get("COMPLETED") != null ? (String) statuses.get("COMPLETED") : "0")) + " </td> </tr>");
                        iTotal_sub += Integer.parseInt(strTemp1);
                        iTotal_com += Integer.parseInt(strTemp2);
                        iTotal_disq += Integer.parseInt(strTemp3);
                    }


        %>
        <%=sb.toString()%>
    </tbody>
    <tr class=tFooter>
        <th align=center>TOTALS</th>
        <td align=center><%=iTotal_sub%></td>
        <td align=center><%=iTotal_disq%></td>
        <td align=center><%=iTotal_com%></td>
    </tr></table>
<BR>
<jsp:include page="i_footer.htm" flush="true" />
<script language="javascript">
    $(document).ready(function()
    {
        $("#DSLRSTable").tablesorter();
    }
);
</script>
</BODY>
</HTML>
