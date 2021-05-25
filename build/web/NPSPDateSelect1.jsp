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
 * MODULE:	NPSPDateSelect.jsp	
 * 
 * DESCRIPTION: JSP View used to select dates for the Number Portability and SPSRs Daily Report
 * 
 * AUTHOR:      Andy Wei
 * 
 * DATE:        10-19-2009
 * 
 * HISTORY:
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%
	final String SECURITY_OBJECT = "SPECIAL_REPORTS";
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
	Log.write(Log.DEBUG_VERBOSE, "NPSPReport parm rpt=" + strPost);
%>
<script type="text/javascript">

    function OnSubmitForm()
    {
        if(document.pressed == 'Submit')
        {
            document.NPSPReportView.action ="<%=strPost%>";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            document.NPSPReportView.action ="NPSPDailyExcelReport.jsp";
        }
        return true;
    }


</script>

        <FORM NAME="NPSPReportView" METHOD=POST onsubmit="return OnSubmitForm();">

<TABLE width="70%" align=center cellspacing=0 cellpadding=0 border=0>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD align=center colspan=10>
<SPAN CLASS="header1"> N&nbsp;P&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;a&nbsp;n&nbsp;d&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;S&nbsp;P&nbsp;S&nbsp;R&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;A&nbsp;I&nbsp;L&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN></TD></TR>
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
<td align=right >State&nbsp;Code&nbsp;*&nbsp;:&nbsp;</td><td>
<select MULTIPLE NAME="STATE_CD">
	<option value=ALL>Report on all States

<%
	rs = stmt.executeQuery("SELECT STT_CD, STT_NM FROM STATE_T ORDER BY STT_CD ASC");
	
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

<TR><TD colspan=8>&nbsp;</TD></TR>

<tr>
<td align=right >Customer&nbsp;Type&nbsp;&nbsp;:&nbsp;</td><td>
<select NAME="CUST_TYP">
        <option value=ALL>All Customer Types
	<option value=BUS>Business
	<option value=RES>Residence
</td>
</tr>

<TR><TD colspan=8>&nbsp;</TD></TR>

<tr>
<td align=right >Broadband&nbsp;&nbsp;:&nbsp;</td><td>
<select NAME="BB">
        <option value=ALL>All
	<option value=Yes>Yes
</td>
</tr>

<TR><TD colspan=8>&nbsp;</TD></TR>

<tr>
<td align=right >Video&nbsp;&nbsp;:&nbsp;</td><td>
<select NAME="VI">
        <option value=ALL>All
	<option value=Cable>Cable
	<option value=Dish>Dish
        <option value=IPTV>IPTV
</td>
</tr>

<TR><TD colspan=8>&nbsp;</TD></TR>

<tr>
<td align=right >Greenfield&nbsp;&nbsp;:&nbsp;</td><td>
<select NAME="GF">
        <option value=ALL>All
	<option value=Yes>Yes
</td>
</tr>

<TR><TD colspan=8>&nbsp;</TD></TR>

<tr>
<td align=right >CLEC&nbsp;&nbsp;:&nbsp;</td><td>
<select NAME="CLEC">
        <option value=ALL>All
	<option value=Yes>Yes
</td>
</tr>

<TR><TD colspan=8>&nbsp;</TD></TR>

<tr>
<td align=right >Partial&nbsp;&nbsp;:&nbsp;</td><td>
<select NAME="PL">
        <option value=ALL>All
	<option value=Yes>Yes
</td>
</tr>

<TR><TD colspan=8>&nbsp;</TD></TR>

<tr>
<td align=right >Circuit&nbsp;&nbsp;:&nbsp;</td><td>
<select NAME="CT">
        <option value=ALL>All
	<option value=Yes>Yes
</td>
</tr>

<TR><TD colspan=8>&nbsp;</TD></TR>

<tr>
<td align=right >CHC&nbsp;&nbsp;:&nbsp;</td><td>
<select NAME="CHC">
        <option value=ALL>All
	<option value=Yes>Yes
</td>
</tr>

<TR><TD colspan=8 align=center><br>&nbsp;*&nbsp;- use Ctrl key to make multiple selections</TD></TR>

<%
} //try
catch (Exception e) {
        rs.close();
        rs=null;
        Log.write(Log.ERROR, "NPSPDateSelect.jsp : Exception caught ["+ e + "]");
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
	<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit" onclick="document.pressed=this.value">
        &nbsp;&nbsp;
	<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="EXCEL"onclick="document.pressed=this.value" >
        </TD>

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
/* $Log:   $
/*

/* $Revision:     $
*/
%>
