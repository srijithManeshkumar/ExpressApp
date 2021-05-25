<%
            /**
             * NOTICE:
             *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
             *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
             *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
             *
             *			COPYRIGHT (C) 2002
             *				BY
             *			Windstream COMMUNICATIONS INC.
             */
            /**
             * MODULE:	DslReport.jsp
             *
             * DESCRIPTION: Dsls report by Company. User picks a date range from DslDateSelect.jsp.
             *
             * AUTHOR:      Express Development Team
             *
             * DATE:        06-12-2002
             *
             * HISTORY:
             * EK: MOD_DATE:   02-02-2005
             * MOD_DESC: The DSL Orders Report is not in synch with the DSL Orders
             */

%>

<%@ include file="ExpressUtil.jsp" %>
<%@ include file="i_header.jsp" %>
<%            String path = request.getContextPath();
%>
<script type='text/javascript' src='<%=path%>/jquery.js'></script>
<script type='text/javascript' src='<%=path%>/tablesorter.js'></script>
<%
            final long DAY_IN_SEC = (long) 86400;
            final long HOUR_IN_SEC = (long) 3600;
            final long MIN_IN_SEC = (long) 60;
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

            Connection con = null;
            PreparedStatement stmt = null;
            int iCmpnyCount = 0;
            long lIntervalTotals = 0;

            String strStartYr = alltelRequest.getParameter("from_due_date_yr");
            String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
            String strStartDay = alltelRequest.getParameter("from_due_date_dy");
            String[] strStates = alltelRequest.getAttributeValue("state");
            String[] srvtypes = alltelRequest.getAttributeValue("srvtype");
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

            if ((strStartYr.length() == 0) || (strStartMth.length() == 0) || (strStartDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("dslstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid from date");
%>
<jsp:forward page="DslDateSelect.jsp"/>;
<%
                return;
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
            int iAllTotalSubmitted = 0;
            DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
            String strDateFormat = "YYYYMMDD HH24:MI:SS";
            String strQuery = " SELECT "
                    + " DSL_SQNC_NMBR, DSL_STTS_CD_IN,"
                    + " TO_CHAR(DSL_HSTRY_DT_IN,'YYYYMMDD HH24MISS'), cp.CMPNY_NM  "
                    + " FROM "
                    + " DSL_REPORT_V dv, COMPANY_T cp  "
                    + " WHERE  dv.CMPNY_SQNC_NMBR = cp.CMPNY_SQNC_NMBR "
                    + " AND cp.CMPNY_TYP = ? "
                    + " AND DSL_STTS_CD_IN <> DSL_STTS_CD_OUT "
                    + " AND DSL_HSTRY_DT_IN < TO_DATE( ?, ?) "
                    + " AND DSL_STTS_CD_IN in ( ?, ? ) "
                    + (allStates ? " " : " AND DSL_RQST_STATE IN " + StatesClause)
                    + (allSrvces ? " " : " AND  SRVC_TYP_CD IN " + SrvcClause)
                    + "  AND "
                    + " EXISTS "
                    + " (SELECT DH2.DSL_SQNC_NMBR FROM DSL_REPORT_V DH2 "
                    + " WHERE DH2.DSL_SQNC_NMBR  = dv.DSL_SQNC_NMBR "
                    + " AND DH2.DSL_HSTRY_DT_IN BETWEEN "
                    + " TO_DATE( ?, ?) AND "
                    + " TO_DATE( ?, ?) "
                    + " AND DH2.DSL_STTS_CD_IN in (?,? ) ) "
                    + " ORDER BY cp.CMPNY_NM, DSL_SQNC_NMBR, DSL_STTS_CD_IN, DSL_HSTRY_DT_IN desc ";

            /***** This section gets the names of services types and display them back on users screen ******/
            String strHTMLsrcTypes = "";
            PreparedStatement pstmt = null;
            ResultSet rset1 = null;
            String strSrvcTypNames = "	select SRVC_TYP_DSCRPTN "
                    + " from SERVICE_TYPE_T  "
                    + " where SRVC_TYP_CD " + (allSrvces ? " > ? " : " IN " + SrvcClause);

            int n = 0;
            if (!allSrvces) {

                try {
                    con = DatabaseManager.getConnection();
                    pstmt = con.prepareStatement(strSrvcTypNames);
                    pstmt.clearParameters();
                    for (n = 0; n < srvtypes.length; n++) {
                        pstmt.setString(n + 1, srvtypes[n]);

                    }
                    rset1 = pstmt.executeQuery();
                    while (rset1.next()) {
                        strHTMLsrcTypes += rset1.getString(1) + "&nbsp&nbsp&nbsp";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.write(Log.DEBUG_VERBOSE, "DSL Activity Report 001: Caught exception e=[" + e.toString() + "]");
                    //DatabaseManager.releaseConnection(conn);
                } finally { // keep connection open for next try
                    try {
                        pstmt.close();
                        pstmt = null;
                        rset1.close();
                        rset1 = null;
                    } catch (Exception eee) {
                    }
                }
            }
            strHTMLsrcTypes = strHTMLsrcTypes.equals("") ? "All Services" : strHTMLsrcTypes;


%>

<br><center>
    <SPAN CLASS="header1"> D&nbsp;S&nbsp;L&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T</SPAN>
    <br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%></b><br>
    Effective:&nbsp;<%= dFmt.format(new java.util.Date())%><br>
    States:&nbsp; <%=getStateFullNames(strStates)%><br>
    Service Types:&nbsp;<%=strHTMLsrcTypes%><BR>
</center><br>
<table border=1 id="DSLRTable" class="tablesorter"  align=center cellspacing=0 cellpadding=1>
    <thead>
        <tr>
            <th align=center>Company</th>
            <th align=center>&nbsp;Total<br>Submitted&nbsp;</th>
            <th align=center>&nbsp;Total<br>Completed&nbsp;</th>
            <th align=center>&nbsp;Completed<br>Interval&nbsp;</th>
        </tr>
    </thead>
    <tbody>
        <%
                    long lPrevDslSeqNo = 0;
                    long lDslSeqNo = 0;
                    long lIntervalAverage = 0;
                    long lDay = 0;
                    long lHour = 0;
                    long lMin = 0;
                    long lSec = 0;
                    int iTotalCompleted = 0;
                    int iTotalSubmitted = 0;
                    String strTempEdate = strEndDate + " 23:59:59";
                    String strTempSdate = strStartDate + " 00:00:00";
                    String strCmpnyNm = "";
                    String strCmpnySqncNmbr = "";
                    int iCompleted = 0;
                    int iSubmitted = 0;
                    ResultSet rs2 = null;
                    PreparedStatement stmt2 = null;
                    String strDslSqncNmbr = "";
                    String strPrevDslSqncNmbr = "";
                    boolean bCompleted = false;
                    ResultSet rs = null;
                    long lIntervalAccumulation = 0;
                    long lInterval = 0;
                    String strIntervalEndDTS = "";
                    String strIntervalBeginDTS = "";
                    String strStatus = "";
                    String strPrevCompany = "";
                    try {
                        iCompleted = 0;
                        iSubmitted = 0;
                        if (allSrvces) {
                            // note, if all services was selected,
                            //connection is not opened yet at this point.
                            con = DatabaseManager.getConnection();
                        }
                        stmt2 = con.prepareStatement(strQuery);
                        stmt2.clearParameters();
                        stmt2.setString(1, "D");
                        stmt2.setString(2, strTempEdate);
                        stmt2.setString(3, strDateFormat);
                        stmt2.setString(4, "SUBMITTED");
                        stmt2.setString(5, "COMPLETED");
                        int j = 6;
                        int i;
                        int k = 0;
                        if (!allStates) {
                            for (i = 0; i < strStates.length; i++) {
                                stmt2.setString(j, strStates[i]);
                                j++;
                            }
                        }

                        if (!allSrvces) {
                            for (; k < srvtypes.length; k++) {
                                stmt2.setString(j, srvtypes[k]);
                                j++;
                            }
                        }
                        stmt2.setString(j, strTempSdate);
                        j += 1;
                        stmt2.setString(j, strDateFormat);
                        j += 1;
                        stmt2.setString(j, strTempEdate);
                        j += 1;
                        stmt2.setString(j, strDateFormat);
                        j += 1;
                        stmt2.setString(j, "SUBMITTED");
                        j += 1;
                        stmt2.setString(j, "COMPLETED");

                        rs2 = stmt2.executeQuery();
                        while (rs2.next() == true) {

                            strCmpnyNm = rs2.getString("CMPNY_NM");
                            if (!strPrevCompany.equals(strCmpnyNm) && !strPrevCompany.equals("")) {
                                iTotalCompleted += iCompleted;
                                iTotalSubmitted += iSubmitted;
                                Log.write(Log.DEBUG_VERBOSE, "DslReport total submission for:\t " + strCmpnyNm
                                        + ":\t" + strDslSqncNmbr + "\t:" + iAllTotalSubmitted + "\t:" + strIntervalEndDTS
                                        + "\t" + strIntervalBeginDTS);
                                iAllTotalSubmitted = 0;
        %>
        <tr>
            <td><%=strPrevCompany%></td>
            <td align=right><%=iSubmitted%></td>
            <td align=right><%=iCompleted%></td>
            <td align=right>
                <%
                                            if (iCompleted > 0) {
                                                lIntervalAverage = lIntervalAccumulation / iCompleted;

                                                //put in xd xh xm xs format
                                                lDay = lIntervalAverage / DAY_IN_SEC;
                                                lIntervalAverage %= DAY_IN_SEC;
                                                lHour = lIntervalAverage / HOUR_IN_SEC;
                                                lIntervalAverage %= HOUR_IN_SEC;
                                                lMin = lIntervalAverage / MIN_IN_SEC;
                                                lIntervalAverage %= MIN_IN_SEC;
                                                lSec = lIntervalAverage;
                                                Log.write(Log.DEBUG_VERBOSE, "*************" + SLATools.getSLAExpectedEndDateTime("20050523 110630", 10));
                %>
                &nbsp;<%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;<%=lSec%>s&nbsp;</td>
                <%

                                                        } else {
                %>
            &nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;0s&nbsp;</td>
            <%                    }
                                        lIntervalTotals += lIntervalAccumulation;
                                        iSubmitted = 0;
                                        iCompleted = 0;
                                        lIntervalAccumulation = 0;
                                    }// end if
                                    strPrevCompany = strCmpnyNm;

                                    strStatus = rs2.getString("DSL_STTS_CD_IN");
                                    strDslSqncNmbr = rs2.getString("DSL_SQNC_NMBR");

                                    if (strStatus.equals("COMPLETED")) {
                                        Log.write(Log.DEBUG_VERBOSE, "Counting 1 for request " + strDslSqncNmbr);
                                        iCompleted++;
                                        bCompleted = true;
                                        strIntervalEndDTS = rs2.getString(3);
                                        lPrevDslSeqNo = rs2.getInt("DSL_SQNC_NMBR");
                                    }
                                    if (strStatus.equals("SUBMITTED")
                                            && strStartDate.compareTo(rs2.getString(3)) <= 0) {
                                        lDslSeqNo = rs2.getInt("DSL_SQNC_NMBR");
                                        iAllTotalSubmitted++;
                                        // submitted and completed
                                        if (bCompleted && (lDslSeqNo == lPrevDslSeqNo)) {
                                            iSubmitted++;
                                            strIntervalBeginDTS = rs2.getString(3);
                                            //Calculate Interval
                                            strIntervalBeginDTS = SLATools.getSLAStartDateTime(strIntervalBeginDTS.substring(0, 8), strIntervalBeginDTS.substring(9, 15));
                                            lInterval = SLATools.calculateSLA(strIntervalBeginDTS, strIntervalEndDTS);
                                            lIntervalAccumulation = lIntervalAccumulation + lInterval;
                                            Log.write(Log.DEBUG_VERBOSE, ">>Interval for request " + lDslSeqNo + " = " + lInterval + " seconds");
                                            bCompleted = false;
                                        } // submitted and not completed
                                        else if (!strPrevDslSqncNmbr.equals(strDslSqncNmbr)) {
                                            iSubmitted++;
                                            bCompleted = false;
                                        }
                                    } else {
                                        if (strStatus.equals("SUBMITTED")) {
                                            iAllTotalSubmitted++;
                                            lDslSeqNo = rs2.getInt("DSL_SQNC_NMBR");
                                            if (bCompleted && (lDslSeqNo == lPrevDslSeqNo)) {
                                                strIntervalBeginDTS = rs2.getString(3);
                                                //Calculate Interval
                                                strIntervalBeginDTS = SLATools.getSLAStartDateTime(strIntervalBeginDTS.substring(0, 8), strIntervalBeginDTS.substring(9, 15));
                                                lInterval = SLATools.calculateSLA(strIntervalBeginDTS, strIntervalEndDTS);
                                                lIntervalAccumulation = lIntervalAccumulation + lInterval;
                                                Log.write(Log.DEBUG_VERBOSE, ">>Interval for request " + lDslSeqNo + " = " + lInterval + " seconds");
                                                bCompleted = false;
                                            }
                                        }
                                    }
                                    strPrevDslSqncNmbr = strDslSqncNmbr;

                                } // end recordset navigation

                                // should put this in a function
                                if (strPrevCompany.equals(strCmpnyNm) && !strPrevCompany.equals("")) {
                                    Log.write(Log.DEBUG_VERBOSE, "DslReport total submission for:\t " + strCmpnyNm
                                            + ":\t" + strDslSqncNmbr + "\t:" + iAllTotalSubmitted + "\t:" + strIntervalEndDTS
                                            + "\t" + strIntervalBeginDTS);
                                    iAllTotalSubmitted = 0;
                                    iTotalCompleted += iCompleted;
                                    iTotalSubmitted += iSubmitted;


            %>
        <tr>

            <td><%=strPrevCompany%></td>
            <td align=right><%=iSubmitted%></td>
            <td align=right><%=iCompleted%></td>

            <td align=right>
                <%

                                        if (iCompleted > 0) {
                                            lIntervalAverage = lIntervalAccumulation / iCompleted;

                                            //put in xd xh xm xs format
                                            lDay = lIntervalAverage / DAY_IN_SEC;
                                            lIntervalAverage %= DAY_IN_SEC;
                                            lHour = lIntervalAverage / HOUR_IN_SEC;
                                            lIntervalAverage %= HOUR_IN_SEC;
                                            lMin = lIntervalAverage / MIN_IN_SEC;
                                            lIntervalAverage %= MIN_IN_SEC;
                                            lSec = lIntervalAverage;
                                            Log.write(Log.DEBUG_VERBOSE, "*************" + SLATools.getSLAExpectedEndDateTime("20050523 110630", 10));
                %>
                &nbsp;<%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;<%=lSec%>s&nbsp;</td>
                <%

                                                } else {
                %>
            &nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;0s&nbsp;</td>
            <%                }

                                    lIntervalTotals += lIntervalAccumulation;
                                    iSubmitted = 0;
                                    iCompleted = 0;
                                    lIntervalAccumulation = 0;
                                }// end if
                                if (iTotalCompleted > 0) {
                                    lIntervalAverage = lIntervalTotals / iTotalCompleted;
                                } else {
                                    lIntervalAverage = 0;
                                }

                                lDay = lIntervalAverage / DAY_IN_SEC;
                                lIntervalAverage %= DAY_IN_SEC;
                                lHour = lIntervalAverage / HOUR_IN_SEC;
                                lIntervalAverage %= HOUR_IN_SEC;
                                lMin = lIntervalAverage / MIN_IN_SEC;
                                lIntervalAverage %= MIN_IN_SEC;
                                lSec = lIntervalAverage;

            %>
    </tbody>
    <tr>
        <td><b>TOTALS</b></td>
        <td align=right><b><%=iTotalSubmitted%></b></td>
        <td align=right><b><%=iTotalCompleted%></b></td>
        <td align=right><b><%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;<%=lSec%>s</b></td>
    <tr>
</table>

<%
            } catch (Exception e) {
                e.printStackTrace();
                Log.write(Log.DEBUG_VERBOSE, "ExpressHomeArchive: Caught exception e=[" + e.toString() + "]");
            } finally {
                try {
                    rs2.close();
                    rs2 = null;
                    stmt.close();
                    stmt = null;
                } catch (Exception eee) {
                }
                DatabaseManager.releaseConnection(con);
            }

%>

</UL>
<BR>
<BR>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
<script language="javascript">
    $(document).ready(function()
    {
        $("#DSLRTable").tablesorter();
    }
);
</script>
</BODY>
</HTML>
