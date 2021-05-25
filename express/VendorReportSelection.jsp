<%
/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2002
 *                                        BY
 *                              Windstream INFORMATION SERVICES
 */
/*
 * MODULE:      VendorReportSelection.jsp
 *
 * DESCRIPTION: Displays list of reports available for vendors to view.
 *
 * AUTHOR:      pjs
 *
 * DATE:        08/30/2002
 *
 * HISTORY:
 *
 */
%>
<%@ include file="i_header.jsp" %>

<%
        final String CONTROLLER = "VENDOR_REPORTS";
        final String CONTROLLER2 = "PROV_REPORTS";	//let providers in
        if (!sdm.isAuthorized(CONTROLLER))
        {
		if (!sdm.isAuthorized(CONTROLLER2))
		{
			Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
			alltelResponse.sendRedirect(SECURITY_URL);
		}
        }

	Connection con = null;
	Statement stmt = null;
	ResultSet rs = null;
	Log.write(Log.DEBUG_VERBOSE, "VendorReportSelection()");
%>

<BR>
<TABLE width=50% align=center cellspacing=0 cellpadding=0 border=0>
	<TR>
		<TD><HR></TD>
		<TD align=center width=50%><SPAN CLASS="header1">VENDOR&nbsp;REPORTS</SPAN></TD>
		<TD><HR></TD>
</TABLE>
<BR CLEAR=ALL>
<BR>
<TABLE align=center cellspacing=0 cellpadding=5 border=1>
<tr>
        <th align=center width=210 height=15>Report</th>
        <th align=center width=45>Run Date</th>
        <th align=center width=150 >Date Range Reported</th>
</tr>

<%
try {
	con = DatabaseManager.getConnection();
        stmt = con.createStatement();
	String strCmpTyp = " ";
	rs = stmt.executeQuery("SELECT CMPNY_TYP FROM COMPANY_T C, USERID_T U WHERE C.CMPNY_SQNC_NMBR = U.CMPNY_SQNC_NMBR " +
			" AND USERID = '" + sdm.getUser() + "'");
	if (rs.next()) 
	{	strCmpTyp = rs.getString(1);
	}
	else
	{	Log.write(Log.WARNING, "VendorReportSelection: " + sdm.getUser() + " has no valid userid/company type! ");
                alltelResponse.sendRedirect(SECURITY_URL);
	}
	
        rs = stmt.executeQuery("SELECT VR.RPRT_SQNC_NMBR, VR.RPRT_DSC, TO_CHAR(VRA.RPRT_RUN_DT,'YYYYMMDD'),  " +
		" TO_CHAR(VRA.RPRT_RUN_DT,'MM-DD-YYYY'), " +
		" TO_CHAR(VRA.RPRT_BGN_DT,'MM-DD-YYYY HH24:MI'), TO_CHAR(VRA.RPRT_END_DT,'MM-DD-YYYY HH24:MI') " +
		" FROM VENDOR_REPORT_T VR, VENDOR_REPORT_ACTVTY_T VRA WHERE VRA.RPRT_SQNC_NMBR=VR.RPRT_SQNC_NMBR " +
		" AND VR.CMPNY_TYPS LIKE '%" + strCmpTyp + "%' ORDER BY 1,3 DESC");
	int iPrev=1;
	while(rs.next()==true)
	{
		int iNum = rs.getInt(1);
		String strDesc = rs.getString(2);
		String strDate = rs.getString(3);
		String strDate2 = rs.getString(4);
		String strBDate = rs.getString(5);
		String strEDate = rs.getString(6);
		if (iNum != iPrev) {
%>
			<TR STYLE="background: purple"><TD height=1>&nbsp;</TD><TD>&nbsp;</TD><TD>&nbsp;</TD></TR>
<%
			iPrev = iNum;
		}
%>
		<TR>
		<TD align=left height=5><SPAN CLASS="smallstyle1">
			<a onMouseOver="self.status='<%=strDesc%>'; return true;" onMouseOut="self.status=''"; return=true;" 
			href="VendorReport.jsp?rptid=<%=iNum%>&amp;rptdt=<%=strDate%>" target=_blank><%=strDesc%></a>&nbsp;</SPAN></TD>
		<TD align=left>&nbsp;<%=strDate2%>&nbsp;</TD>
		<TD align=left>&nbsp;<%=strBDate%>&nbsp;&nbsp;-&nbsp;&nbsp;<%=strEDate%></TD>
		</TR>
<%
	} //while()
}
catch (Exception e) {
	Log.write(Log.DEBUG_VERBOSE, "VendorReportSelection() Exception=["+e+"] caught");
}
finally {
	Log.write(Log.DEBUG_VERBOSE, "VendorReportSelection() finally");
	DatabaseManager.releaseConnection(con);
}
%>
  
</TABLE>
<br><br><%= (String) request.getAttribute("vendorrptmsg") %><br>
</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
