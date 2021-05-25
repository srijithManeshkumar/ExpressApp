<%-- 
    Document   : COExcelReport
    Created on : May 24, 2011, 12:16:55 PM
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
            response.setHeader("Content-Disposition", "attachment; filename=" + "COReport" + ".xls");
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
	final long DAY_IN_SEC = (long) 86400;
	final long HOUR_IN_SEC = (long) 3600;
	final long MIN_IN_SEC = (long) 60;
	final String SECURITY_OBJECT = "PROV_REPORTS";

	
	// Did they cancel?
	if (alltelRequest.getParameter("SUBMITBUTTON").equals("Cancel"))
	{
%>
                <jsp:forward page="Reports.jsp"/>;
<%
		return;
	}

	Connection con = null;
	Statement  stmt = null;
	int     iOCNCount = 0;

	String strStartYr = alltelRequest.getParameter("from_due_date_yr");
	String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
	String strStartDay = alltelRequest.getParameter("from_due_date_dy");
	if ((strStartYr.length() == 0) || (strStartMth.length()==0) || (strStartDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("costat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid from date");
%>
		<jsp:forward page="CODateSelect.jsp"/>;
<%
		return;
	}
	String strStartDate = strStartYr + strStartMth + strStartDay;

	String strEndYr = alltelRequest.getParameter("to_due_date_yr");
	String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
	String strEndDay = alltelRequest.getParameter("to_due_date_dy");
	if ((strEndYr.length() == 0) || (strEndMth.length()==0) || (strEndDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("costat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid to date");
%>
		<jsp:forward page="CODateSelect.jsp"/>;
<%
		return;
	}
	String strEndDate = strEndYr + strEndMth + strEndDay;
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("costat", "'From Date' must be less than or equal to 'To Date'!");
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid to date");
%>
		<jsp:forward page="CODateSelect.jsp"/>;
<%
		return;
	}
        //Check days of month and adjust if necessary ...
	Calendar calTemp = Calendar.getInstance();
	calTemp.set(Integer.parseInt(strStartYr),  Integer.parseInt(strStartMth) - 1,  1, 0, 0, 0);
        int iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
Log.write("iMaxDays ToDate=" + iMaxDays + "  Startdate=" + strStartYr + strStartMth + strStartDay);
        if (Integer.parseInt(strStartDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("costat", "'From Date' - invalid day of month selected");
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid from date");
%>
		<jsp:forward page="CODateSelect.jsp"/>;
<%
		return;
        }
	calTemp.set(Integer.parseInt(strEndYr),  Integer.parseInt(strEndMth) - 1,  1, 0, 0, 0);
        iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
Log.write("iMaxDays FromDate=" + iMaxDays + "  Enddate=" + strEndYr + strEndMth + strEndDay);
        if (Integer.parseInt(strEndDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("costat", "'To Date' - invalid day of month selected");
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid to date");
%>
		<jsp:forward page="CODateSelect.jsp"/>;
<%
		return;
        }
	DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
	Log.write(Log.DEBUG_VERBOSE, "CO Date:" + strStartDate + " Date:" + strEndDate);

	con = DatabaseManager.getConnection();
	Statement stmt2 = con.createStatement();

	String strQuery1 = "SELECT O.OCN_CD, OS.STT_CD, O.OCN_NM " +
		" FROM OCN_T O, OCN_STATE_T OS WHERE O.OCN_CD = OS.OCN_CD " +
		" ORDER BY OS.STT_CD, O.OCN_CD ";
	stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(strQuery1);
%>


<br><center>
<SPAN CLASS="header1"> C&nbsp;O&nbsp;M&nbsp;P&nbsp;L&nbsp;E&nbsp;T&nbsp;E&nbsp;D&nbsp;&nbsp;&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R&nbsp;S&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN>
<br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%></b><br>
Effective:&nbsp;<%= dFmt.format(new java.util.Date()) %><br></center><br>
<table border=1 align=center cellspacing=0 cellpadding=1>
<tr>
	<th align=center>&nbsp;OCN&nbsp;</th>
	<th align=center>VENDOR</th>
	<th align=center>&nbsp;Orders<br>Completed&nbsp;</th>
	<th align=center>&nbsp;Completion&nbsp;Interval<br>( days )&nbsp;</th>
	<th align=center>&nbsp;Due&nbsp;Date<br>Missed&nbsp;</th>
	<th align=center>&nbsp;%&nbsp;Due&nbsp;Date<br>Missed&nbsp;</th>
</tr>
<%
        int     iTotal = 0;
        int     iTotalMissed = 0;
	double	fTotalCmpltnIntrvl = 0.0;
	DecimalFormat OCNfmt = new DecimalFormat("0000");
	DecimalFormat Intrvlfmt = new DecimalFormat("0.00");
	while(rs.next()==true)
	{
		iOCNCount++;
		//int iOCN = rs.getInt("OCN_CD");
		String strOCN = rs.getString("OCN_CD");
		String strSt = rs.getString("STT_CD");
		String strNm = rs.getString("OCN_NM");
		int iCompleted = 0;
		int iMissed = 0;
		double fCmpltnIntrvl = 0.0;

		String strDueDt = "";
		String strCmpltdDt = "";

		Log.write(Log.DEBUG_VERBOSE, "CO OCN=" + strOCN + " St:"+strSt+ " Dt1:" +strStartDate+" Dt2:"+strEndDate+"-------");

		// Start building stats for this OCN-State
		String strOCNQuery =
			"SELECT distinct R.RQST_SQNC_NMBR, R.RQST_STTS_CD, R.DUE_DT, L.LSRCM_CD " +
			" FROM REQUEST_T R, LSRCM_T L " +
			" WHERE R.RQST_SQNC_NMBR = L.RQST_SQNC_NMBR " +
			" AND R.OCN_CD = '" + strOCN + "' " +
			" AND R.OCN_STT = '" + strSt + "' " +
			" AND R.RQST_STTS_CD IN ('CLOSED','COMPLETED') " +
			" AND EXISTS (SELECT RH2.RQST_SQNC_NMBR FROM REQUEST_HISTORY_T RH2 " +
			"            WHERE RH2.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR " +
			"            AND RH2.RQST_STTS_CD_OUT = 'COMPLETED' " +
			"            AND RH2.RQST_HSTRY_DT_IN BETWEEN " +
			"            TO_DATE('" + strStartDate + "', 'YYYYMMDD HH24:MI:SS') AND " +
			"            TO_DATE('" + strEndDate + "', 'YYYYMMDD HH24:MI:SS') ) " +
			" ORDER BY R.RQST_SQNC_NMBR";

		ResultSet rs2 = stmt2.executeQuery(strOCNQuery);

		while(rs2.next()==true)
		{
			strDueDt = rs2.getString("DUE_DT");
			strCmpltdDt = rs2.getString("LSRCM_CD");

			// weed out the freaks, shouldn't be any...
			if (strDueDt == null || strDueDt.length() != 10 ||
			    strCmpltdDt == null || strCmpltdDt.length() != 10)
			{
				// ignore this order, it's a freak of nature...
				continue;
			}

			// Increment total completed for this OCN
			iCompleted++;

			// Increment total missed Due Date for this OCN
			strDueDt = strDueDt.substring(6) + strDueDt.substring(0,2) + strDueDt.substring(3,5);
			strCmpltdDt = strCmpltdDt.substring(6) + strCmpltdDt.substring(0,2) + strCmpltdDt.substring(3,5);
			if (Integer.parseInt(strCmpltdDt) > Integer.parseInt(strDueDt))
			{
				iMissed++;
			}

			// Calculate completion interval for this order
			//   and increment total days accordingly
			fCmpltnIntrvl += SLATools.calculateSLADays(strDueDt, strCmpltdDt);
                }
		iTotal += iCompleted;
		iTotalMissed += iMissed;
		fTotalCmpltnIntrvl += fCmpltnIntrvl;
		rs2.close();
		rs2 = null;

%>
                <tr>
                <td><%=strOCN%>-<%=strSt%></td>
                <td><%=strNm%></td>
                <td align=right><%=iCompleted%></td>
<%		if (iCompleted > 0)
		{
%>
			<td align=right><%= Intrvlfmt.format(fCmpltnIntrvl/iCompleted) %></td>
<%		}
		else
		{
%>
                	<td align=right>0</td>
<%		}
%>
                <td align=right><%=iMissed%></td>
<%		if (iCompleted > 0)
		{
%>
			<td align=right><%= (iMissed*100)/iCompleted %></td>
			</tr>
<%		}
		else
		{
%>
                	<td align=right>0</td>
			</tr>
<%		}
	}
%>

<tr>
<td><b>TOTALS</b></td>
<td align=center><b><%=iOCNCount%>&nbsp;VENDORS</b></td>
<td align=right><b><%=iTotal%></b></td>
<%	if (iTotal > 0)
	{
%>
		<td align=right><%= Intrvlfmt.format(fTotalCmpltnIntrvl/iTotal) %></td>
<%	}
	else
	{
%>
               	<td align=right>0</td>
<%	}
%>
<td align=right><b><%=iTotalMissed%></b></td>
<%	if (iTotal > 0)
	{
%>
		<td align=right><b>&nbsp;<%= (iTotalMissed*100)/iTotal %>&nbsp;</b></td>
		</tr>
<%	}
	else
	{
%>
               	<td align=right>0</td>
		</tr>
<%	}
rs.close();
rs = null;
DatabaseManager.releaseConnection(con);
%>

</table>
</UL>
<BR>
<BR>
</FORM>


</BODY>
</HTML>
