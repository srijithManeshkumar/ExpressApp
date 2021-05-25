<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2003
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	BillDisputeDateSelect.jsp	
 * 
 * DESCRIPTION: JSP View used to select dates for the DSL Completed Order Report
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        4-10-2003
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
%>
<script type="text/javascript">

    function OnSubmitForm()
    {
        if(document.pressed == 'Submit')
        {
            document.BillDisputeReportView.action ="BillDisputeReport.jsp";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            document.BillDisputeReportView.action ="BillDisputeExcelReport.jsp";
        }
        return true;
    }


</script>
<FORM NAME="BillDisputeReportView" METHOD=POST onsubmit="return OnSubmitForm();">

<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD align=center colspan=10>
<SPAN CLASS="header1"> B&nbsp;i&nbsp;l&nbsp;l&nbsp;&nbsp;&nbsp;D&nbsp;i&nbsp;s&nbsp;p&nbsp;u&nbsp;t&nbsp;e&nbsp;&nbsp&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN></TD>
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

<tr>
<td align=center colspan=8 VALIGN="center">Company&nbsp;:
<select NAME="COMPANY_SEL" MULTIPLE SIZE=5>
        <option value=ALL SELECTED>--Report on all Companies--

<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;
try {
        con = DatabaseManager.getConnection();
        stmt = con.createStatement();

        //Only include users that have the ability to work on orders/LSRs
        rs = stmt.executeQuery("SELECT DISTINCT C.CMPNY_SQNC_NMBR, C.CMPNY_NM" +
                " FROM USERID_T U, COMPANY_T C, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA " +
                " WHERE U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND UGA.USERID=U.USERID  " +
                " AND SGA.SCRTY_GRP_CD=UGA.SCRTY_GRP_CD AND SGA.SCRTY_OBJCT_CD='CREATE_DISPUTES' " +
                 "  ORDER BY C.CMPNY_SQNC_NMBR ");
        while (rs.next() == true)
        {
%>
		<option value=<%= rs.getString(1) %>><%= rs.getString(2) %>
<%
        }
}
catch (Exception e) {}
finally {
        rs.close();
        rs=null;
        DatabaseManager.releaseConnection(con);
}
%>
</td>
</tr>

<TR><TD colspan=8>&nbsp;</TD></TR>
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
	<%= (String) request.getAttribute("costat") %>
	</TD>
</TR>
 
</TABLE>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
