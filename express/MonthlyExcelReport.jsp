<%-- 
    Document   : MonthlyExcelReport
    Created on : May 24, 2011, 12:49:41 PM
    Author     : satish.t
--%>


<%@ page language="java"
         import = "java.util.*, java.text.*,
		  java.sql.*,
		  javax.sql.*,
		  com.alltel.lsr.common.objects.*,
		  com.alltel.lsr.common.util.*"
         %>

<%
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "MonthlyReportLSR" + ".xls");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");

            AlltelRequest alltelRequest = null;
            AlltelResponse alltelResponse = null;
            SessionDataManager sdm = null;
            try {
                alltelRequest = new AlltelRequest(request);
                alltelResponse = new AlltelResponse(response);
                sdm = alltelRequest.getSessionDataManager();
                if ((sdm == null) || (!sdm.isUserLoggedIn())) {
                    alltelResponse.sendRedirect("LoginCtlr");
                    return;
                }
            } catch (Exception e) {
                Log.write(Log.ERROR, e.getMessage());
                Log.write(Log.ERROR, "Trapped in i_header.jsp");
            }
            final String SECURITY_OBJECT = "PROV_REPORTS";


            Connection con = null;
            PreparedStatement pstmtLSR = null;
            PreparedStatement pstmtSRD = null;
            PreparedStatement pstmtSRDMulti = null;
            PreparedStatement pstmtPorts = null;

            PreparedStatement pstmtSimplePorts = null;

            PreparedStatement pstmtLoop = null;
            PreparedStatement pstmtDir = null;
            PreparedStatement pstmtOther = null;
            PreparedStatement pstmtUniqueVendors = null;
            ResultSet rs = null;

            int iOCNCount = 0;
            int iWkLSR = 0;
            int iWkSRD = 0;
            int iWkCLEC = 0;

            int iWkSPorts = 0;

            int iWkLoops = 0;
            int iWkDirs = 0;
            int iWkOther = 0;
            int iWkUniqueVendors = 0;

            int iMthLSR = 0;
            int iMthSRD = 0;
            int iMthCLEC = 0;
            int iMthSPorts = 0;
            int iMthLoops = 0;
            int iMthDirs = 0;
            int iMthOther = 0;
            int iMthUniqueVendor = 0;
            int iVendors = 0;

            int iCurrentWeek = 0;
            int iPrevWeek = 0;
            int iMth = 0;
            int iPrevMth = 0;

            Vector vFocRej;

            String strFromMMDDYYYY;
            String strMMDDYYYY_mthstart;
            String strToMMDDYYYY;

            String strStartYr = alltelRequest.getParameter("from_due_date_yr");
            String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
            if ((strStartYr.length() == 0) || (strStartMth.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("slastat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "MthRpt: Invalid from date");
%>
<jsp:forward page="MthDateSelect.jsp"/>;
<%
                return;
            }
            String strStartDate = strStartYr + strStartMth + "01";

            String strEndYr = alltelRequest.getParameter("to_due_date_yr");
            String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
            if ((strEndYr.length() == 0) || (strEndMth.length() == 0)) {
                alltelRequest.getHttpRequest().setAttribute("slastat", "INVALID Date entered!");
                Log.write(Log.DEBUG_VERBOSE, "MthRpt: Invalid to date");
%>
<jsp:forward page="MthDateSelect.jsp"/>;
<%
                return;
            }
            String strEndDate = strEndYr + strEndMth + "01";
            if (strStartDate.compareTo(strEndDate) > 0) {
                alltelRequest.getHttpRequest().setAttribute("slastat", "'From Date' must be less than or equal to 'To Date'!");
                Log.write(Log.DEBUG_VERBOSE, "MontlyReport: Invalid to date");
%>
<jsp:forward page="MthDateSelect.jsp"/>;
<%
                return;
            }
            DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);

            Log.write(Log.DEBUG_VERBOSE, "MthRpt: Date:" + strStartDate + " Date:" + strEndDate);

            //Build from and to calendar ranges
            Calendar calFrom = Calendar.getInstance();
            calFrom.set(Integer.parseInt(strStartDate.substring(0, 4)),
                    Integer.parseInt(strStartDate.substring(4, 6)) - 1,
                    1, 0, 0, 0);
            iPrevWeek = calFrom.get(Calendar.WEEK_OF_MONTH);
            iCurrentWeek = iPrevWeek;
            iMth = calFrom.get(Calendar.MONTH);
            iPrevMth = iMth;

            Calendar calTo = Calendar.getInstance();
            calTo.set(Integer.parseInt(strEndDate.substring(0, 4)),
                    Integer.parseInt(strEndDate.substring(4, 6)) - 1,
                    1, 23, 59, 59);
            int iMaxDays = calTo.getActualMaximum(Calendar.DAY_OF_MONTH);
            calTo.set(Calendar.DAY_OF_MONTH, iMaxDays);

            Log.write(Log.DEBUG_VERBOSE, "MthRpt: From:" + calFrom.getTime() + " To:" + calTo.getTime());

            try {
                con = DatabaseManager.getConnection();
                pstmtLSR = con.prepareStatement("SELECT COUNT(*) FROM REQUEST_T R, REQUEST_HISTORY_T RH "
                        + " WHERE R.SRVC_TYP_CD='E' AND R.ACTVTY_TYP_CD IN ('N', 'C') "
                        + " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' "
                        + " AND RH.RQST_HSTRY_DT_IN > TO_DATE(?,'MMDDYYYY') "
                        + " AND RH.RQST_HSTRY_DT_IN < TO_DATE(?,'MMDDYYYY')");
                pstmtSRD = con.prepareStatement("SELECT COUNT(*) FROM REQUEST_T R, REQUEST_HISTORY_T RH "
                        + " WHERE R.SRVC_TYP_CD='E' AND R.ACTVTY_TYP_CD IN ('S', 'B', 'D') "
                        + " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' "
                        + " AND RH.RQST_HSTRY_DT_IN > TO_DATE(?,'MMDDYYYY') "
                        + " AND RH.RQST_HSTRY_DT_IN < TO_DATE(?,'MMDDYYYY')");
                pstmtSRDMulti = con.prepareStatement("SELECT R.RQST_SQNC_NMBR FROM REQUEST_T R, REQUEST_HISTORY_T RH "
                        + " WHERE R.SRVC_TYP_CD='1' AND R.ACTVTY_TYP_CD IN ('S', 'B', 'D') "
                        + " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' "
                        + " AND RH.RQST_HSTRY_DT_IN > TO_DATE(?,'MMDDYYYY') "
                        + " AND RH.RQST_HSTRY_DT_IN < TO_DATE(?,'MMDDYYYY')");
                pstmtPorts = con.prepareStatement("SELECT COUNT(*) FROM REQUEST_T R, REQUEST_HISTORY_T RH "
                        + " WHERE R.SRVC_TYP_CD='C' "
                        + " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' "
                        + " AND RH.RQST_HSTRY_DT_IN > TO_DATE(?,'MMDDYYYY') "
                        + " AND RH.RQST_HSTRY_DT_IN < TO_DATE(?,'MMDDYYYY')");

                pstmtSimplePorts = con.prepareStatement("SELECT COUNT(*) FROM REQUEST_T R, REQUEST_HISTORY_T RH "
                        + " WHERE R.SRVC_TYP_CD='S' "
                        + " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' "
                        + " AND RH.RQST_HSTRY_DT_IN > TO_DATE(?,'MMDDYYYY') "
                        + " AND RH.RQST_HSTRY_DT_IN < TO_DATE(?,'MMDDYYYY')");

                pstmtLoop = con.prepareStatement("SELECT COUNT(*) FROM REQUEST_T R, REQUEST_HISTORY_T RH "
                        + " WHERE R.SRVC_TYP_CD IN ('A','B') "
                        + " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' "
                        + " AND RH.RQST_HSTRY_DT_IN > TO_DATE(?,'MMDDYYYY') "
                        + " AND RH.RQST_HSTRY_DT_IN < TO_DATE(?,'MMDDYYYY')");
                pstmtDir = con.prepareStatement("SELECT COUNT(*) FROM REQUEST_T R, REQUEST_HISTORY_T RH "
                        + " WHERE R.SRVC_TYP_CD IN ('G','H', 'J') "
                        + " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' "
                        + " AND RH.RQST_HSTRY_DT_IN > TO_DATE(?,'MMDDYYYY') "
                        + " AND RH.RQST_HSTRY_DT_IN < TO_DATE(?,'MMDDYYYY')");
                pstmtOther = con.prepareStatement("SELECT COUNT(*) FROM REQUEST_T R, REQUEST_HISTORY_T RH "
                        + " WHERE R.SRVC_TYP_CD IN ('D','F','K','L','M','N','P') "
                        + " AND R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' "
                        + " AND RH.RQST_HSTRY_DT_IN > TO_DATE(?,'MMDDYYYY') "
                        + " AND RH.RQST_HSTRY_DT_IN < TO_DATE(?,'MMDDYYYY')");
                pstmtUniqueVendors = con.prepareStatement("SELECT COUNT(*) FROM ("
                        + "SELECT DISTINCT R.CMPNY_SQNC_NMBR FROM REQUEST_T R, REQUEST_HISTORY_T RH "
                        + " WHERE R.RQST_SQNC_NMBR=RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN = 'SUBMITTED' "
                        + " AND RH.RQST_HSTRY_DT_IN > TO_DATE(?,'MMDDYYYY') "
                        + " AND RH.RQST_HSTRY_DT_IN < TO_DATE(?,'MMDDYYYY') ) ");

            } catch (Exception e) {
                alltelRequest.getHttpRequest().setAttribute("slastat", "ERROR! Database connection unavailable!");
                Log.write(Log.ERROR, "MthRpt: getConnecton() failed");
%>
<jsp:forward page="MthDateSelect.jsp"/>;
<%
                return;
            }
            //Get the number of Vendors (CLEC, Reseller only)
            try {
                Statement stmt = con.createStatement();
                rs = stmt.executeQuery("SELECT COUNT(*) FROM COMPANY_T C WHERE C.CMPNY_TYP IN ('R','C') ");
                rs.next();
                iVendors = rs.getInt(1);
                rs.close();
            } catch (Exception e) {
                alltelRequest.getHttpRequest().setAttribute("slastat", "ERROR! Reading Vendor information");
                Log.write(Log.ERROR, "MthRpt: failure querying company table");
%>
<jsp:forward page="MthDateSelect.jsp"/>;
<%
                return;
            }
%>


<br><center>
    <SPAN CLASS="header1">M&nbsp;O&nbsp;N&nbsp;T&nbsp;H&nbsp;L&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN><br>
    <SPAN CLASS="header1">S&nbsp;u&nbsp;b&nbsp;m&nbsp;i&nbsp;t&nbsp;t&nbsp;e&nbsp;d&nbsp;&nbsp;&nbsp;&nbsp;L&nbsp;S&nbsp;R&nbsp;'&nbsp;s&nbsp;</SPAN>
    <br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/01/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=iMaxDays%>/<%=strEndYr%></b><br>
    Effective:&nbsp;<%= dFmt.format(new java.util.Date())%><br></center><br><br>

<%
            //Loop month by month and create this table
            DecimalFormat df = new DecimalFormat("##0.00");
            DateFormat mthFmt = new SimpleDateFormat("MMM yyyy");
            DateFormat weFmt = new SimpleDateFormat("dd-MMM");
            DateFormat dateRangeFmt = new SimpleDateFormat("MMddyyyy");

            strFromMMDDYYYY = dateRangeFmt.format(calFrom.getTime());
            strMMDDYYYY_mthstart = strFromMMDDYYYY;

            while (calFrom.before(calTo)) {
                //Log.write(Log.DEBUG_VERBOSE, "MthRpt: New month " + (calFrom.get(Calendar.MONTH)+1) );
%>
<center><font size="+1"><b><%=mthFmt.format(calFrom.getTime())%></b></font></center>
<table width="90%" border=1 align=center cellspacing=0 cellpadding=1>
    <tr>
        <th align=left >&nbsp;Week Ending</th>
        <th align=center bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();"
                                                  ONMOUSEOVER="showpopupmsg('Resale|REQTYP=E and |Activity Type = N or C');">
			LSR</font></th>

        <th align=center bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();"
                                                 ONMOUSEOVER="showpopupmsg('Suspend/Restore/Disconnects|REQTYP=E and |Activity Type = S, B, or D|and Multi Orders');">
			S/R/D</th>

        <th align=center bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();"
                                                 ONMOUSEOVER="showpopupmsg('Number Portabilities| REQTYP = C');">
			Number Ports</th>

        <th align=center bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();"
                                                 ONMOUSEOVER="showpopupmsg('Simple Ports| REQTYP = S');">
			Simple Ports</th>

        <th align=center bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();"
                                                 ONMOUSEOVER="showpopupmsg('Loop | REQTYP = A or B');">
			Loops</th>

        <th align=center bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();"
                                                 ONMOUSEOVER="showpopupmsg('Directory | REQTYP = G, H or J');">
			Directory</th>

        <th align=center bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();"
                                                 ONMOUSEOVER="showpopupmsg('Other REQTYPs | (D, F, K, L, M, N, P)');">
			Other</th>

        <th align=center bgcolor="#3366cc"><font color="#ffffff">TOTALS</font></th>

        <th align=center bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();"
                                                 ONMOUSEOVER="showpopupmsg('Total Vendors | in Express');">
			Vendors</th>

        <th align=center bgcolor="#3366cc"><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();"
                                                 ONMOUSEOVER="showpopupmsg('Total unique| vendors submitting| orders');">
			Vendors<br>Submitting<br>LSRs</th>

                </tr>
                <%
                            while (iMth == iPrevMth) {
                                //Gather statistics for this day
                                iOCNCount = 0;

                                String strWkEnding = weFmt.format(calFrom.getTime());

                                calFrom.add(Calendar.DATE, 1);
                                iCurrentWeek = calFrom.get(Calendar.WEEK_OF_MONTH);

                                //If new week, then Spit out stats for the week we just completed
                                if (iCurrentWeek != iPrevWeek) {
                                    strToMMDDYYYY = dateRangeFmt.format(calFrom.getTime());
                                    //Log.write("dates; " + strFromMMDDYYYY + " to " + strToMMDDYYYY);

                                    //End of the week, so gather our stats
                                    pstmtLSR.setString(1, strFromMMDDYYYY);
                                    pstmtLSR.setString(2, strToMMDDYYYY);
                                    rs = pstmtLSR.executeQuery();
                                    rs.next();
                                    iWkLSR = rs.getInt(1);
                                    rs.close();

                                    pstmtSRD.setString(1, strFromMMDDYYYY);
                                    pstmtSRD.setString(2, strToMMDDYYYY);
                                    rs = pstmtSRD.executeQuery();
                                    rs.next();
                                    iWkSRD = rs.getInt(1);
                                    rs.close();

                                    //Also need to SRD's for MultiOrders
                                    pstmtSRDMulti.setString(1, strFromMMDDYYYY);
                                    pstmtSRDMulti.setString(2, strToMMDDYYYY);
                                    rs = pstmtSRDMulti.executeQuery();
                                    while (rs.next()) {
                                        vFocRej = SLATools.getMultiFocRej(rs.getString(1), "SUBMITTED");
                                        iWkSRD += ((Integer) vFocRej.elementAt(0)).intValue();
                                        iWkSRD += ((Integer) vFocRej.elementAt(1)).intValue();
                                    }//while
                                    rs.close();

                                    pstmtPorts.setString(1, strFromMMDDYYYY);
                                    pstmtPorts.setString(2, strToMMDDYYYY);
                                    rs = pstmtPorts.executeQuery();
                                    rs.next();
                                    iWkCLEC = rs.getInt(1);
                                    rs.close();

                                    pstmtSimplePorts.setString(1, strFromMMDDYYYY);
                                    pstmtSimplePorts.setString(2, strToMMDDYYYY);
                                    rs = pstmtSimplePorts.executeQuery();
                                    rs.next();
                                    iWkSPorts = rs.getInt(1);
                                    rs.close();

                                    pstmtLoop.setString(1, strFromMMDDYYYY);
                                    pstmtLoop.setString(2, strToMMDDYYYY);
                                    rs = pstmtLoop.executeQuery();
                                    rs.next();
                                    iWkLoops = rs.getInt(1);
                                    rs.close();

                                    pstmtDir.setString(1, strFromMMDDYYYY);
                                    pstmtDir.setString(2, strToMMDDYYYY);
                                    rs = pstmtDir.executeQuery();
                                    rs.next();
                                    iWkDirs = rs.getInt(1);
                                    rs.close();

                                    pstmtOther.setString(1, strFromMMDDYYYY);
                                    pstmtOther.setString(2, strToMMDDYYYY);
                                    rs = pstmtOther.executeQuery();
                                    rs.next();
                                    iWkOther = rs.getInt(1);
                                    rs.close();

                                    pstmtUniqueVendors.setString(1, strFromMMDDYYYY);
                                    pstmtUniqueVendors.setString(2, strToMMDDYYYY);
                                    rs = pstmtUniqueVendors.executeQuery();
                                    rs.next();
                                    iWkUniqueVendors = rs.getInt(1);
                                    rs.close();
                %>
                <tr>
                    <td align=right><b><%=strWkEnding%></b></td>
                    <td align=right><%=iWkLSR%></td>
                    <td align=right><%=iWkSRD%></td>
                    <td align=right><%=iWkCLEC%></td>
                    <td align=right><%=iWkSPorts%></td>
                    <td align=right><%=iWkLoops%></td>
                    <td align=right><%=iWkDirs%></td>
                    <td align=right><%=iWkOther%></td>
                    <td align=right><%=iWkLSR + iWkSRD + iWkCLEC + iWkSPorts + iWkLoops + iWkDirs + iWkOther%></td>
                    <td align=right><%=iVendors%></td>
                    <td align=right><%=iWkUniqueVendors%></td>
                <tr>
                    <%
                                                //Add weekly total to MTD
                                                iMthLSR += iWkLSR;
                                                iMthSRD += iWkSRD;
                                                iMthCLEC += iWkCLEC;

                                                iMthSPorts += iWkSPorts;

                                                iMthLoops += iWkLoops;
                                                iMthDirs += iWkDirs;
                                                iMthOther += iWkOther;
                                                //reset weekly counters

                                                iWkLSR = iWkSRD = iWkCLEC = iWkSPorts = iWkLoops = iWkDirs = iWkOther = iWkUniqueVendors = 0;

                                                iPrevWeek = iCurrentWeek;

                                                iMth = calFrom.get(Calendar.MONTH);
                                                if (iMth != iPrevMth) {
                                                    Log.write("Mth end dates; " + strMMDDYYYY_mthstart + " to " + strToMMDDYYYY);
                                                    pstmtUniqueVendors.setString(1, strMMDDYYYY_mthstart);
                                                    pstmtUniqueVendors.setString(2, strToMMDDYYYY);
                                                    rs = pstmtUniqueVendors.executeQuery();
                                                    rs.next();
                                                    iWkUniqueVendors = rs.getInt(1);
                                                    rs.close();
                    %>
                <tr>
                    <td><b>MTD TOT'S</b></td>
                    <td align=right><b><%=iMthLSR%></b></td>
                    <td align=right><b><%=iMthSRD%></b></td>
                    <td align=right><b><%=iMthCLEC%></b></td>

                    <td align=right><b><%=iMthSPorts%></b></td>

                    <td align=right><b><%=iMthLoops%></b></td>
                    <td align=right><b><%=iMthDirs%></b></td>
                    <td align=right><b><%=iMthOther%></b></td>
                    <td align=right><b><%=iMthLSR + iMthSRD + iMthCLEC + iMthSPorts + iMthLoops + iMthDirs + iMthOther%></b></td>
                    <td align=right><b><%=iVendors%></b></td>
                    <td align=right><b><%=iWkUniqueVendors%></b></td>
                <tr>
                </table><br><br>
                <%

                                            iMthLSR = iMthSRD = iMthCLEC = iMthSPorts = iMthLoops = iMthDirs = iMthOther = 0;

                                            iWkUniqueVendors = 0;
                                            strMMDDYYYY_mthstart = strToMMDDYYYY;
                                        }
                                        strFromMMDDYYYY = dateRangeFmt.format(calFrom.getTime());
                                    }

                                } //while month the same
                                iPrevMth = iMth;

                            } //while()
                            DatabaseManager.releaseConnection(con);
                %>

                </UL>
                <BR>
                <BR>

                </FORM>

                </BODY>
                </HTML>
