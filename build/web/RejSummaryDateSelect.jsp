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

 * MODULE:	RejSummaryDateSelect.jsp
 *
 * DESCRIPTION:
 *
 * AUTHOR:      Satish Talluri
 *
 * DATE:        Mar 30, 2011, 11:31:09 AM
 *
 * HISTORY:
 *
 */
%>

<%@ include file="i_header.jsp" %>

<%
	final String SECURITY_OBJECT = "PROV_REPORTS";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		 alltelResponse.sendRedirect(SECURITY_URL);
	}
	DateFormat df = new SimpleDateFormat("MMM");
	Calendar cal = Calendar.getInstance();
	Calendar cal2 = Calendar.getInstance();
	cal.setTime(new java.util.Date());

	String strPost = request.getParameter("rpt");
	Log.write(Log.DEBUG_VERBOSE, "DailySummaryReport parm rpt=" + strPost);
%>

<FORM NAME="RejSumReportView" METHOD=POST ACTION="<%=strPost%>">

<TABLE width="70%" align=center cellspacing=0 cellpadding=0 border=0>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD align=center colspan=10>
<SPAN CLASS="header1">R&nbsp;E&nbsp;J&nbsp;E&nbsp;C&nbsp;T&nbsp;I&nbsp;O&nbsp;N&nbsp;&nbsp;&nbsp;&nbsp;S&nbsp;U&nbsp;M&nbsp;M&nbsp;A&nbsp;R&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN></TD>
</TR>
<TR><TD colspan=2>&nbsp;</TD></TR>
<TR><TD colspan=8>&nbsp;</TD></TR>
<TR><TD colspan=8>&nbsp;</TD></TR>
<TR>
	<TD align=right>Submitted From Date:&nbsp;</TD>
	<TD><SELECT name="from_due_date_mnth">
<%
		String y;
		int iMth = cal.get(Calendar.MONTH)+1;
		int iDay = cal.get(Calendar.DAY_OF_MONTH);
		int iYear = cal.get(Calendar.YEAR);
		for (int x = 1; x < 13 ; x++)
		{	y = "" + x;
			if (y.length()==1) y="0"+x;
			cal2.set(Calendar.MONTH, x-1);
			if (x == iMth) {
%>
				<OPTION SELECTED value="<%=y%>"><%=df.format(cal2.getTime())%>
<%
			} else {
%>
				<OPTION value="<%=y%>"><%=df.format(cal2.getTime())%> <%
			}
		}
%>
	</SELECT>
	<SELECT name="from_due_date_dy">
	<%
			for (int x = 1; x < 32 ; x++)
			{	y = "" + x;
				if (y.length()==1) y="0"+x;
				if (x==iDay) {
	%>
					<OPTION SELECTED value="<%=y%>"><%=y%>
	<%
				} else {
	%>
					<OPTION value="<%=y%>"><%=y%>
	<%
				}
			}
	%>
	</SELECT>
	<SELECT name="from_due_date_yr" >
<%
		for (int x = 2001; x <= iYear ; x++)
		{
			if (x==iYear) {
%>
				<OPTION SELECTED value="<%=x%>"><%=x%>
<%
			} else {
%>
				<OPTION value="<%=x%>"><%=x%>
<%
			}
		}
%>
	</SELECT>
	</TD>
	<TD align=right>To Date:&nbsp;</TD>
	<TD><SELECT name="to_due_date_mnth">
<%
		for (int x = 1; x < 13 ; x++)
		{
			y = "" + x;
			if (y.length()==1) y="0"+x;
			cal2.set(Calendar.MONTH, x-1);
			if (x == iMth) {
%>
				<OPTION SELECTED value="<%=y%>"><%=df.format(cal2.getTime())%>
<%
			} else {
%>
				<OPTION value="<%=y%>"><%=df.format(cal2.getTime())%>
<%
			}
		}
%>
	</SELECT>
	<SELECT name="to_due_date_dy">
	<%
			for (int x = 1; x < 32 ; x++)
			{	y = "" + x;
				if (y.length()==1) y="0"+x;
				if (x==iDay) {
	%>
				<OPTION SELECTED value="<%=y%>"><%=y%>
	<%
				} else {
	%>
				<OPTION value="<%=y%>"><%=y%>
	<%
				}
			}
	%>
	</SELECT>
	<SELECT name="to_due_date_yr">
<%
		for (int x = 2001; x <= iYear ; x++)
		{
			if (x==iYear) {
%>
				<OPTION SELECTED value="<%=x%>"><%=x%>
<%
			} else {
%>
				<OPTION value="<%=x%>"><%=x%>
<%
			}
		}
%>
	</SELECT>
	</TD>
</TR>

<TR><TD colspan=8>&nbsp;</TD></TR>
<TR>
	<TD align=right>Rejected From Date:&nbsp;</TD>
	<TD><SELECT name="from_due_date_mnth">
<%

		for (int x = 1; x < 13 ; x++)
		{	y = "" + x;
			if (y.length()==1) y="0"+x;
			cal2.set(Calendar.MONTH, x-1);
			if (x == iMth) {
%>
				<OPTION SELECTED value="<%=y%>"><%=df.format(cal2.getTime())%>
<%
			} else {
%>
				<OPTION value="<%=y%>"><%=df.format(cal2.getTime())%> <%
			}
		}
%>
	</SELECT>
	<SELECT name="from_due_date_dy">
	<%
			for (int x = 1; x < 32 ; x++)
			{	y = "" + x;
				if (y.length()==1) y="0"+x;
				if (x==iDay) {
	%>
					<OPTION SELECTED value="<%=y%>"><%=y%>
	<%
				} else {
	%>
					<OPTION value="<%=y%>"><%=y%>
	<%
				}
			}
	%>
	</SELECT>
	<SELECT name="from_due_date_yr" >
<%
		for (int x = 2001; x <= iYear ; x++)
		{
			if (x==iYear) {
%>
				<OPTION SELECTED value="<%=x%>"><%=x%>
<%
			} else {
%>
				<OPTION value="<%=x%>"><%=x%>
<%
			}
		}
%>
	</SELECT>
	</TD>
	<TD align=right>To Date:&nbsp;</TD>
	<TD><SELECT name="to_due_date_mnth">
<%
		for (int x = 1; x < 13 ; x++)
		{
			y = "" + x;
			if (y.length()==1) y="0"+x;
			cal2.set(Calendar.MONTH, x-1);
			if (x == iMth) {
%>
				<OPTION SELECTED value="<%=y%>"><%=df.format(cal2.getTime())%>
<%
			} else {
%>
				<OPTION value="<%=y%>"><%=df.format(cal2.getTime())%>
<%
			}
		}
%>
	</SELECT>
	<SELECT name="to_due_date_dy">
	<%
			for (int x = 1; x < 32 ; x++)
			{	y = "" + x;
				if (y.length()==1) y="0"+x;
				if (x==iDay) {
	%>
				<OPTION SELECTED value="<%=y%>"><%=y%>
	<%
				} else {
	%>
				<OPTION value="<%=y%>"><%=y%>
	<%
				}
			}
	%>
	</SELECT>
	<SELECT name="to_due_date_yr">
<%
		for (int x = 2001; x <= iYear ; x++)
		{
			if (x==iYear) {
%>
				<OPTION SELECTED value="<%=x%>"><%=x%>
<%
			} else {
%>
				<OPTION value="<%=x%>"><%=x%>
<%
			}
		}
%>
	</SELECT>
	</TD>
</TR>


</TABLE><br>
<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>

<TR><TD colspan=2>&nbsp;</TD></TR>
<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;

try {
	con = DatabaseManager.getConnection();
	stmt = con.createStatement();
%>
	<tr>
	<td align=right>OCN&nbsp;Code&nbsp;*&nbsp;:&nbsp;</td><td>
	<select MULTIPLE size=5 NAME="OCN_CD">
		<option value=ALL>Report on all OCNs
<%

	rs = stmt.executeQuery("SELECT DISTINCT O.OCN_CD, O.OCN_NM, C.CMPNY_SQNC_NMBR FROM OCN_T O, COMPANY_T C, VENDOR_TABLE_CONFIG_T V " +
	     "WHERE V.CMPNY_SQNC_NMBR = O.CMPNY_SQNC_NMBR AND V.OCN_CD = O.OCN_CD AND O.CMPNY_SQNC_NMBR = C.CMPNY_SQNC_NMBR ORDER BY OCN_CD ASC");

	while (rs.next() == true)
	{
%>
	<option value=<%= rs.getString("OCN_CD") %> ><%= rs.getString("OCN_CD") %>&nbsp;-&nbsp;<%= rs.getString("OCN_NM") %>
<%
	}
	rs.close();
	rs=null;
%>
</td>
</tr>

<TR><TD colspan=2>&nbsp;</TD></TR>
<tr>
<td align=right >State&nbsp;Code&nbsp;*&nbsp;:&nbsp;</td><td>
<select MULTIPLE NAME="STATE_CD">
	<option value=ALL>Report on all States

<%
	rs = stmt.executeQuery("SELECT DISTINCT S.STT_CD, S.STT_NM FROM STATE_T S, VENDOR_TABLE_CONFIG_T V " +
	     "WHERE S.STT_NM = V.STT_CD ORDER BY STT_CD ASC");
	while (rs.next() == true)
	{
%>
	<option value=<%= rs.getString("STT_CD") %> ><%= rs.getString("STT_CD") %>&nbsp;-&nbsp;<%= rs.getString("STT_NM") %>
<%
	}
	rs.close();
	rs=null;
%>
</td>
</tr>

<TR><TD colspan=2>&nbsp;</TD></TR>
<tr>
<td align=right >Company&nbsp;*&nbsp;:&nbsp;</td><td>
<select MULTIPLE NAME="VENDOR">
	<option value=ALL>Report on all Companys

<%
	rs = stmt.executeQuery("SELECT DISTINCT C.CMPNY_SQNC_NMBR, C.CMPNY_NM FROM COMPANY_T C, VENDOR_TABLE_CONFIG_T V " +
	     "WHERE C.CMPNY_SQNC_NMBR = V.CMPNY_SQNC_NMBR ORDER BY CMPNY_NM");
	while (rs.next() == true)
	{
%>
	<option value=<%= rs.getString("CMPNY_SQNC_NMBR") %> ><%= rs.getString("CMPNY_NM") %>
<%
	}
	rs.close();
	rs=null;
%>
</td>
</tr>

<TR><TD colspan=2>&nbsp;</TD></TR>
<tr>
<td align=right >RejectionCode&nbsp;*&nbsp;:&nbsp;</td><td>
<select MULTIPLE NAME="REJECTION_CODE">
	<option value=ALL>Report on all Rejection Codes

<%

	rs = stmt.executeQuery("SELECT RSN_CD_SQNC_NMBR,RSN_CD_TYP,RSN_CD_DSCRPTN FROM REASON_CODE_T WHERE SRVC_TYP_CD='C' AND ACTVTY_TYP_CD ='V'");
	while (rs.next() == true)
	{
%>
        <option value=<%= rs.getString("RSN_CD_SQNC_NMBR") %> ><%= rs.getString("RSN_CD_TYP") %>&nbsp;-&nbsp;<%= rs.getString("RSN_CD_DSCRPTN") %>
<%
	}
	rs.close();
	rs=null;
%>
</td>
</tr>

<TR><TD colspan=2>&nbsp;</TD></TR>
    <TR>
  <TD align=right>&nbsp;Order&nbsp;Type :&nbsp;</TD>
  <TD align=left><SELECT NAME="orderFlag">
  <option value="ALL">ALL </OPTION>
  <option value="N">CAMS</OPTION>
  <option value="Y">ICARE</OPTION>
 </SELECT></TD>
    </TR>

<TR><TD colspan=2>&nbsp;</TD></TR>

<TR><TD colspan=8 align=center><br>&nbsp;*&nbsp;- use Ctrl key to make multiple selections</TD></TR>

<%
} //try
catch (Exception e) {
        rs.close();
        rs=null;
        Log.write(Log.ERROR, "AutoSummaryDateSelect.jsp : Exception caught ["+ e + "]");
        stmt.close();
        stmt= null;
}
finally {
        DatabaseManager.releaseConnection(con);
}
%>


<TR><TD colspan=8>&nbsp;</TD></TR>
<TR><TD colspan=8>&nbsp;</TD></TR>
<TR><TD colspan=8>&nbsp;</TD></TR>
<TR>
	<TD align=center colspan=8>
	<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit"></TD>
</TR>
<TR><TD colspan=2>&nbsp;</TD></TR>
<TR><TD colspan=2>&nbsp;</TD></TR>
<TR>
	<TD align=center colspan=4>
	<%= (String) request.getAttribute("reportstat") %>
	</TD>
</TR>

</TABLE>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
