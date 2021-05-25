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
            document.SLAReportView.action ="<%=strPost%>";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            if("<%=strPost%>" =='MonthlyReport.jsp'){
            document.SLAReportView.action ="MonthlyExcelReport.jsp";
            }else{
             document.SLAReportView.action ="MonthlyExcelOcnReport.jsp";
            }
        }
        return true;
    }


</script>

        <FORM NAME="SLAReportView" METHOD=POST onsubmit="return OnSubmitForm();">

<TABLE width="70%" align=center cellspacing=0 cellpadding=0 border=0>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD align=center colspan=10>
<SPAN CLASS="header1"> M&nbsp;O&nbsp;N&nbsp;T&nbsp;H&nbsp;L&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN></TD>
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

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/MthDateSelect.jsv  $
/*
/*   Rev 1.2   18 Feb 2002 12:16:22   sedlak
/* 
/*
/*   Rev 1.0   31 Jan 2002 07:01:24   sedlak
/*Initial revision
/*
/*   Rev 1.0   31 Jan 2002 06:46:18   psedlak
/*Initial Checkin
*/

/* $Revision:   1.2  $
*/
%>
