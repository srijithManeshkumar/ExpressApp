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

	String strPost = request.getParameter("mrpt");
	Log.write(Log.DEBUG_VERBOSE, "MthReport parm mrpt=" + strPost);
%>
<script type="text/javascript">

    function OnSubmitForm()
    {
        if(document.pressed == 'Submit')
        {
            document.WNPDateMonthReportView.action ="<%=strPost%>";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            document.WNPDateMonthReportView.action ="WNPSummaryExcelReport.jsp";
        }
        return true;
    }


</script>
        <FORM NAME="WNPDateMonthReportView" METHOD=POST onsubmit="return OnSubmitForm();">

<TABLE width="70%" align=center cellspacing=0 cellpadding=0 border=0>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD align=center colspan=10>
<SPAN CLASS="header1"> W&nbsp;N&nbsp;P&nbsp;&nbsp;&nbsp;S&nbsp;u&nbsp;m&nbsp;m&nbsp;a&nbsp;r&nbsp;y&nbsp;&nbsp;&nbsp;R&nbsp;e&nbsp;p&nbsp;o&nbsp;r&nbsp;t&nbsp;</SPAN></TD>
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
<TR><TD colspan=2>&nbsp;</TD></TR>
<TR>

	<TD colspan=8 align=center>State:&nbsp;
	<SELECT name="state_list">
			<OPTION SELECTED value="all_states">--All--
	
<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;
try {
	con = DatabaseManager.getConnection();
	stmt = con.createStatement();

	rs = stmt.executeQuery("SELECT S.STT_CD, S.STT_CD||' '||S.STT_NM AS NAME FROM STATE_T S ORDER BY S.STT_CD");

	while (rs.next() == true)
	{
	%>
		<OPTION value=<%= rs.getString(1) %>><%= rs.getString(2) %>
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
	</SELECT>
	</TD>
</TR>

<TR><TD colspan=2>&nbsp;</TD></TR>
<TR>

	<TD colspan=8 align=center>OCN:&nbsp;
	<SELECT name="ocn_list">
			<OPTION SELECTED value="all_ocns">--All--
	
<%
try {
	con = DatabaseManager.getConnection();
	stmt = con.createStatement();

	String strOCNQuery = "";
	if (strPost != null && strPost.equals("WNPSummaryReport.jsp"))
	{	//weed em out if WNP report
		strOCNQuery = "SELECT DISTINCT O.OCN_CD, O.OCN_NM FROM OCN_T O, COMPANY_T C " +
			" WHERE O.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP in ('R','C','L') AND C.CMPNY_NM NOT LIKE 'TSI T%' ORDER BY O.OCN_CD";
	}
	else
	{
		strOCNQuery = "SELECT DISTINCT O.OCN_CD, O.OCN_NM FROM OCN_T O, COMPANY_T C " +
			" WHERE O.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP in ('R','C') ORDER BY O.OCN_CD";
	}
	rs = stmt.executeQuery(strOCNQuery);

	while (rs.next() == true)
	{
	%>
		<OPTION value=<%= rs.getString(1) %>><%= rs.getString(1) %> - <%= rs.getString(2) %>
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
	</SELECT>
	</TD>
</TR>
<TR><TD colspan=2>&nbsp;</TD></TR>
    <TR>
  <TD colspan = 4 align=center>Order&nbsp;Type :
  <SELECT NAME="orderFlag">
  <option value="ALL">ALL </OPTION>
  <option value="N">CAMS</OPTION>
  <option value="Y">ICARE</OPTION>
 </SELECT></TD>
  </TR>


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
	<%= (String) request.getAttribute("slastat") %>
	</TD>
</TR>
 
</TABLE>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

