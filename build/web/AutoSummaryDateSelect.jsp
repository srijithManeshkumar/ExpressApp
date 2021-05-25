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
 * MODULE:	AutoSummaryDateSelect.jsp	
 * 
 * DESCRIPTION: JSP View used to select dates for the Automated Activity Daily Summary Report
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

<FORM NAME="DSReportView" METHOD=POST ACTION="<%=strPost%>">

<TABLE width="70%" align=center cellspacing=0 cellpadding=0 border=0>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD align=center colspan=10>
<SPAN CLASS="header1">A&nbsp;U&nbsp;T&nbsp;O&nbsp;M&nbsp;A&nbsp;T&nbsp;E&nbsp;D&nbsp;&nbsp;&nbsp;A&nbsp;C&nbsp;T&nbsp;I&nbsp;V&nbsp;I&nbsp;T&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;A&nbsp;I&nbsp;L&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S&nbsp;U&nbsp;M&nbsp;M&nbsp;A&nbsp;R&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN></TD>
</TR>
<TR><TD colspan=2>&nbsp;</TD></TR>
<TR><TD colspan=8>&nbsp;</TD></TR>
<TR><TD colspan=8>&nbsp;</TD></TR>
<TR>
	<TD align=right>From Date:&nbsp;</TD>
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
</TABLE><br>
<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>

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
<td align=right >Service Type&nbsp;*&nbsp;:&nbsp;</td><td>
<select MULTIPLE NAME="SRVC_TYP_CD">
	<option value=ALL>Report on all Service Types 

<% 
        
	rs = stmt.executeQuery("SELECT DISTINCT S.SRVC_TYP_CD, S.SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T S, VENDOR_TABLE_CONFIG_T V " +
	     "WHERE S.SRVC_TYP_CD = SUBSTR(V.SRVC_TYP_CD,1,1) AND S.TYP_IND = 'R' ORDER BY SRVC_TYP_DSCRPTN");
	while (rs.next() == true)
	{
%>
	<option value=<%= rs.getString("SRVC_TYP_CD") %> ><%= rs.getString("SRVC_TYP_DSCRPTN") %>
<%
	}
	rs.close();
	rs=null;
%>
</td>
</tr>

<TR><TD colspan=2>&nbsp;</TD></TR>
<tr>
<td align=right >Activity Type&nbsp;*&nbsp;:&nbsp;</td><td>
<select MULTIPLE NAME="ACTVTY_TYP_CD">
	<option value=ALL>Report on all Activity Types 

<%
	rs = stmt.executeQuery("SELECT DISTINCT A.ACTVTY_TYP_CD, A.ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T A, VENDOR_TABLE_CONFIG_T V " +
	     "WHERE A.ACTVTY_TYP_CD = SUBSTR(V.ACTVTY_TYP_CD,1,1) AND A.TYP_IND ='R' ORDER BY ACTVTY_TYP_DSCRPTN");
	while (rs.next() == true)
	{
%>
	<option value=<%= rs.getString("ACTVTY_TYP_CD") %> ><%= rs.getString("ACTVTY_TYP_DSCRPTN") %>
<%
	}
	rs.close();
	rs=null;
%>
</td>
</tr>

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

<%
/** $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/AutoSummaryDateSelect.jsv  $

   Rev 1.0   Sep 15 2009 10:14:00 e0059725
   IDEA 5072
/*
*/

/* $Revision:   1.0  $
*/
%>

