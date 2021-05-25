<%@ include file="i_header.jsp" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>

<%
	final String SECURITY_OBJECT = "PROV_REPORTS";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		 alltelResponse.sendRedirect(SECURITY_URL);
	}
%>
<script type="text/javascript">

    function OnSubmitForm()
    {
        if(document.pressed == 'Submit')
        {
            document.PreorderReportView.action ="PreorderReport.jsp";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            document.PreorderReportView.action ="PreorderExcelReport.jsp";
        }
        return true;
    }


</script>

<FORM NAME="PreorderReportView" METHOD=POST onsubmit="return OnSubmitForm();">

<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD align=center colspan=10>
<SPAN CLASS="header1"> P&nbsp;R&nbsp;E&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN></TD>
</TR>
<TR><TD colspan=2>&nbsp;</TD></TR>
<TR><TD colspan=8>&nbsp;</TD></TR>
<TR><TD colspan=8>&nbsp;</TD></TR>
<TR>
	<TD align=right>From Date:&nbsp;</TD>
	<TD><SELECT name="from_due_date_mnth">
<%
		String y;
		Calendar cal = Calendar.getInstance();
		int iMth = cal.get(Calendar.MONTH)+1;
		int iDay = cal.get(Calendar.DAY_OF_MONTH);
		int iYear = cal.get(Calendar.YEAR);
		for (int x = 1; x < 13 ; x++)
		{	y = "" + x;
			if (y.length()==1) y="0"+x;
			if (x == iMth) {
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
	<SELECT name="from_due_date_yr">
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
			if (x == iMth) {
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

<tr>
<td align=center colspan=8>OCN&nbsp;Code:
<select NAME="OCN_CD">
	<option value=ALL>Report on all OCNs

<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;

con = DatabaseManager.getConnection();
stmt = con.createStatement();
rs = stmt.executeQuery("SELECT OCN_CD, OCN_NM FROM OCN_T ORDER BY OCN_CD ASC");

while (rs.next() == true)
{
%>
	<option value=<%= rs.getString("OCN_CD") %>><%= rs.getString("OCN_CD") %>&nbsp;-&nbsp;<%= rs.getString("OCN_NM") %>
<%
}
DatabaseManager.releaseConnection(con);
%>
</td>
</tr>

<TR><TD colspan=8>&nbsp;</TD></TR>
<TR><TD colspan=8>&nbsp;</TD></TR>
<TR>
	<TD align=center colspan=8>
	<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit" onclick="document.pressed=this.value">
        &nbsp;&nbsp;
	<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="EXCEL"onclick="document.pressed=this.value" >
        </TD>
</TR>
<TR><TD colspan=2>&nbsp;</TD></TR>
<TR><TD colspan=2>&nbsp;</TD></TR>
<TR>
	<TD align=center colspan=4>
	<%= (String) request.getAttribute("preorderstat") %>
	</TD>
</TR>
 
</TABLE>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
