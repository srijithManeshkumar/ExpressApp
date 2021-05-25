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
             * MODULE:	PreorderReport.jsp
             *
             * DESCRIPTION: Preorders report by OCN/state. User picks a date range from PreorderDateSelect.jsp.
             *
             * AUTHOR:      Express Development TEam
             *
             * DATE:        01-02-2002
             *
             * HISTORY:
             *	12/20/2002	pjs	Chg OCN to alphanumeric (HD 227319)
             */

%>

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
            Statement stmt = null;
            int iOCNCount = 0;
            long lIntervalTotals = 0;

            String strStartYr = alltelRequest.getParameter("from_due_date_yr");
            String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
            String strStartDay = alltelRequest.getParameter("from_due_date_dy");
            if ((strStartYr.length() == 0) || (strStartMth.length() == 0) || (strStartDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("preorderstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "Preorder Invalid from date");
%>
<jsp:forward page="PreorderDateSelect.jsp"/>;
<%
                return;
            }
            String strStartDate = strStartYr + strStartMth + strStartDay;

            String strEndYr = alltelRequest.getParameter("to_due_date_yr");
            String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
            String strEndDay = alltelRequest.getParameter("to_due_date_dy");
            if ((strEndYr.length() == 0) || (strEndMth.length() == 0) || (strEndDay.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("preorderstat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "Preorder Invalid to date");
%>
<jsp:forward page="PreorderDateSelect.jsp"/>;
<%
                return;
            }
            String strEndDate = strEndYr + strEndMth + strEndDay;
            if (strStartDate.compareTo(strEndDate) > 0) {
                alltelRequest.getHttpRequest().setAttribute("preorderstat", "'From Date' must be less than or equal to 'To Date'!");
                Log.write(Log.DEBUG_VERBOSE, "Preorder Invalid to date");
%>
<jsp:forward page="PreorderDateSelect.jsp"/>;
<%
                return;
            }
            //Check days of month and adjust if necessary ...
            Calendar calTemp = Calendar.getInstance();
            calTemp.set(Integer.parseInt(strStartYr), Integer.parseInt(strStartMth) - 1, 1, 0, 0, 0);
            int iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (Integer.parseInt(strStartDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("preorderstat", "'From Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "Preorder Invalid from date");
%>
<jsp:forward page="PreorderDateSelect.jsp"/>;
<%
                return;
            }
            calTemp.set(Integer.parseInt(strEndYr), Integer.parseInt(strEndMth) - 1, 1, 0, 0, 0);
            iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (Integer.parseInt(strEndDay) > iMaxDays) {
                alltelRequest.getHttpRequest().setAttribute("preorderstat", "'To Date' - invalid day of month selected");
                Log.write(Log.DEBUG_VERBOSE, "Preorder Invalid to date");
%>
<jsp:forward page="PreorderDateSelect.jsp"/>;
<%
                return;
            }
            DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);

            con = DatabaseManager.getConnection();
            Statement stmt2 = con.createStatement();

            String strOcnCd = alltelRequest.getParameter("OCN_CD");
            String strQuery1 = "";
            if (strOcnCd.equals("ALL")) {
                strQuery1 = "SELECT O.OCN_CD, OS.STT_CD, O.OCN_NM "
                        + " FROM OCN_T O, OCN_STATE_T OS "
                        + " WHERE O.OCN_CD = OS.OCN_CD "
                        + " ORDER BY OS.STT_CD, O.OCN_CD ";
            } else {
                strQuery1 = "SELECT O.OCN_CD, OS.STT_CD, O.OCN_NM "
                        + " FROM OCN_T O, OCN_STATE_T OS "
                        + " WHERE O.OCN_CD = OS.OCN_CD "
                        + " AND O.OCN_CD = '" + strOcnCd + "' "
                        + " ORDER BY OS.STT_CD, O.OCN_CD ";
            }
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery1);
%>


<br><center>
    <SPAN CLASS="header1"> P&nbsp;R&nbsp;E&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T</SPAN>
    <br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%></b><br>
    Effective:&nbsp;<%= dFmt.format(new java.util.Date())%><br></center><br>
<table id="PRTable" class="tablesorter" border=1 align=center cellspacing=0 cellpadding=1>
    <thead>
        <tr>
            <th align=center>&nbsp;OCN&nbsp;</th>
            <th align=center>VENDOR</th>
            <th align=center>Transaction<br>Type</th>
            <th align=center>&nbsp;Total<br>Completed&nbsp;</th>
            <th align=center>&nbsp;Completed<br>Interval&nbsp;</th>
        </tr>
    </thead>
    <tbody>
        <%
                    long lPrevSeqNmbr = 0;
                    long lSeqNmbr = 0;
                    long lIntervalAverage = 0;
                    long lDay = 0;
                    long lHour = 0;
                    long lMin = 0;
                    long lSec = 0;
                    int iTotal = 0;
                    DecimalFormat OCNfmt = new DecimalFormat("0000");
                    while (rs.next() == true) {
                        iOCNCount++;
                        //int iOCN = rs.getInt("OCN_CD");
                        String strOCN = rs.getString("OCN_CD");
                        String strSt = rs.getString("STT_CD");
                        String strNm = rs.getString("OCN_NM");
                        int iCompleted = 0;
                        long lIntervalAccumulation = 0;      //this is total seconds
                        long lInterval = 0;
                        String strIntervalEndDTS = "";
                        String strIntervalBeginDTS = "";

                        // Start building stats for this OCN-State
                        String strOCNQuery =
                                "SELECT PH.PRE_ORDR_SQNC_NMBR, PH.PRE_ORDR_STTS_CD_IN, TO_CHAR(PH.PRE_ORDR_HSTRY_DT_IN,'YYYYMMDD HH24MISS'), P.SRVC_TYP_CD "
                                + " FROM PREORDER_T P, PREORDER_HISTORY_T PH WHERE P.OCN_CD = '" + strOCN + "' AND P.OCN_STT = '" + strSt + "' AND "
                                + " P.PRE_ORDR_SQNC_NMBR = PH.PRE_ORDR_SQNC_NMBR AND PH.PRE_ORDR_STTS_CD_IN <> PH.PRE_ORDR_STTS_CD_OUT "
                                + " AND EXISTS (SELECT PH2.PRE_ORDR_SQNC_NMBR FROM PREORDER_HISTORY_T PH2 "
                                + " WHERE PH2.PRE_ORDR_SQNC_NMBR = P.PRE_ORDR_SQNC_NMBR AND P.ICARE = 'N' "
                                + " AND PH2.PRE_ORDR_STTS_CD_IN = 'COMPLETED' "
                                + " AND PH2.PRE_ORDR_HSTRY_DT_IN BETWEEN "
                                + " TO_DATE('" + strStartDate + " 00:00:00', 'YYYYMMDD HH24:MI:SS') AND "
                                + " TO_DATE('" + strEndDate + " 23:59:59', 'YYYYMMDD HH24:MI:SS') ) "
                                + " AND PH.PRE_ORDR_STTS_CD_IN in ('SUBMITTED','COMPLETED') "
                                + " ORDER BY P.SRVC_TYP_CD, PH.PRE_ORDR_SQNC_NMBR, PRE_ORDR_STTS_CD_IN";

                        ResultSet rs2 = stmt2.executeQuery(strOCNQuery);

                        String strPreOrdrSqncNmbr = "";
                        String strPrevPreOrdrSqncNmbr = "";

                        String strTransType = "&nbsp;";
                        String strPrevTransType = "&nbsp;";

                        boolean bFirstTime = true;
                        boolean bFirstHeader = true;

                        while (rs2.next() == true) {
                            String strStatus = rs2.getString("PRE_ORDR_STTS_CD_IN");
                            strPreOrdrSqncNmbr = rs2.getString("PRE_ORDR_SQNC_NMBR");
                            strTransType = rs2.getString("SRVC_TYP_CD");

                            // Check for changing Transaction Type
                            if (!strTransType.equals(strPrevTransType) && bFirstTime == false) {
                                iTotal += iCompleted;
        %>
        <tr>
            <%				if (bFirstHeader == true) {
            %>
            <td><%=strOCN%>-<%=strSt%></td>
            <td><%=strNm%></td>
            <%					bFirstHeader = false;
                                            } else {
            %>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <%				}
            %>
            <td><%=strPrevTransType%></td>
            <td align=right><%=iCompleted%></td>

            <td align=right>
                <%				if (iCompleted > 0) {
                                                lIntervalAverage = lIntervalAccumulation / iCompleted;

                                                //put in xd xh xm xs format
                                                lDay = lIntervalAverage / DAY_IN_SEC;
                                                lIntervalAverage %= DAY_IN_SEC;
                                                lHour = lIntervalAverage / HOUR_IN_SEC;
                                                lIntervalAverage %= HOUR_IN_SEC;
                                                lMin = lIntervalAverage / MIN_IN_SEC;
                                                lIntervalAverage %= MIN_IN_SEC;
                                                lSec = lIntervalAverage;
                %>
                &nbsp;<%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;<%=lSec%>s&nbsp;</td>
                <%
                                                } else {
                %>
            &nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;0s&nbsp;</td>
            <%                    }
                                lIntervalTotals += lIntervalAccumulation;
                                iCompleted = 0;
                                lIntervalAccumulation = 0;
                            }

                            if (strStatus.equals("COMPLETED")) {
                                Log.write(Log.DEBUG_VERBOSE, "Counting 1 for request " + strPreOrdrSqncNmbr);
                                iCompleted++;
                                strIntervalEndDTS = rs2.getString(3);
                                lPrevSeqNmbr = rs2.getInt("PRE_ORDR_SQNC_NMBR");
                            }
                            if (strStatus.equals("SUBMITTED")) {
                                lSeqNmbr = rs2.getInt("PRE_ORDR_SQNC_NMBR");
                                if (lSeqNmbr == lPrevSeqNmbr) {
                                    strIntervalBeginDTS = rs2.getString(3);
                                    //Calculate Interval
                                    strIntervalBeginDTS = SLATools.getSLAStartDateTime(strIntervalBeginDTS.substring(0, 8), strIntervalBeginDTS.substring(9, 15));
                                    lInterval = SLATools.calculateSLA(strIntervalBeginDTS, strIntervalEndDTS);
                                    lIntervalAccumulation = lIntervalAccumulation + lInterval;
                                    Log.write(Log.DEBUG_VERBOSE, ">>Interval for request " + lSeqNmbr + " = " + lInterval + " seconds");
                                }
                            }

                            strPrevTransType = strTransType;
                            bFirstTime = false;
                        }   //while()
                        rs2.close();
                        rs2 = null;

                        iTotal += iCompleted;

            %>
        <tr>

            <%		if (bFirstHeader == true) {
            %>
            <td><%=strOCN%>-<%=strSt%></td>
            <td><%=strNm%></td>
            <%		} else {
            %>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <%		}
            %>
            <td><%=strTransType%></td>
            <td align=right><%=iCompleted%></td>

            <td align=right>
                <%		if (iCompleted > 0) {
                                lIntervalAverage = lIntervalAccumulation / iCompleted;

                                //put in xd xh xm xs format
                                lDay = lIntervalAverage / DAY_IN_SEC;
                                lIntervalAverage %= DAY_IN_SEC;
                                lHour = lIntervalAverage / HOUR_IN_SEC;
                                lIntervalAverage %= HOUR_IN_SEC;
                                lMin = lIntervalAverage / MIN_IN_SEC;
                                lIntervalAverage %= MIN_IN_SEC;
                                lSec = lIntervalAverage;
                %>
                &nbsp;<%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;<%=lSec%>s&nbsp;</td>
                <%
                                } else {
                %>
            &nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;0s&nbsp;</td>
            <%                }
                            lIntervalTotals += lIntervalAccumulation;
                        } //while()

                        if (iTotal > 0) {
                            lIntervalAverage = lIntervalTotals / iTotal;
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
        <td align=center><b><%=iOCNCount%>&nbsp;VENDORS</b></td>
        <td align=right><b>&nbsp;</b></td>
        <td align=right><b><%=iTotal%></b></td>
        <td align=right><b><%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;<%=lSec%>s</b></td>
    <tr>
</table>

<%
            DatabaseManager.releaseConnection(con);
%>

</UL>
<BR>
<BR>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
<script language="javascript">
    $(document).ready(function()
    {
        $("#PRTable").tablesorter();
    }
);
</script>
</BODY>
</HTML>
