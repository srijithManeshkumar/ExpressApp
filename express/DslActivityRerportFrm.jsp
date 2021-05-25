<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2002
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	DslActivityReport	
 * 
 * DESCRIPTION: JSP View used to select dates for the DSL Completed Order Report
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-25-2005
 * 
 * HISTORY:
 *
 */

%>
<%@ include file="ExpressUtil.jsp" %>
<%@ include file="i_header.jsp" %>
<script type="text/javascript">

    function OnSubmitForm()
    {
        if(document.pressed == 'Submit')
        {
            document.DslActivityRptFrm.action ="DslReportBystate.jsp";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            document.DslActivityRptFrm.action ="DslExcelReportBystate.jsp";
        }
        return true;
    }

</script>
<%
	final String SECURITY_OBJECT = "PROV_REPORTS";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		 alltelResponse.sendRedirect(SECURITY_URL);
	}
	String strArrStt[] = {"__" };
	Connection conn = null;
	PreparedStatement pstmt =  null;
	String srvQry = " select  distinct srv.SRVC_TYP_CD, srv.SRVC_TYP_DSCRPTN " 
			 	+ " from SERVICE_TYPE_T srv, DSL_t dsv "
			 	+ " where dsv.SRVC_TYP_CD =  srv.SRVC_TYP_CD ";	
	ResultSet rset = null;
	String htmlSelectBox = "";
	htmlSelectBox = "<select name=\"srvtype\" size=5  MULTIPLE>\n";			
	htmlSelectBox += "<option value=\"0\" selected>Select All</option>\n";
	try {
		conn  = DatabaseManager.getConnection();
		pstmt = conn .prepareStatement( srvQry );		
		rset = pstmt.executeQuery( );		
		while( rset.next() ) 
		{	
		htmlSelectBox += "<option value=\"" 
			+ rset.getInt(1) + "\">" +  rset.getString(2) +  "</option>\n";
		}

	} 	catch(Exception e) {
		Log.write(Log.DEBUG_VERBOSE, "DSL Activity Report: Caught exception e=[" + e + "]");
	}
	finally {
		try {
			rset.close(); rset=null;
			pstmt.close(); pstmt=null;
		} catch (Exception eee) {}
		DatabaseManager.releaseConnection(conn);
	}	
	htmlSelectBox += "</select>\n";
%>
<table align=center width="100%" cellspacing=0 cellpadding=0>
  <tr>
    <TH width="100%" align=center bgcolor="#7AABDE"><SPAN class="barheader">
B&nbsp;R&nbsp;O&nbsp;A&nbsp;D&nbsp;B&nbsp;A&nbsp;N&nbsp;D&nbsp;&nbsp;&nbsp;&nbsp;A&nbsp;C&nbsp;T&nbsp;I&nbsp;V&nbsp;I&nbsp;T&nbsp;Y&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;&nbsp;&nbsp;&nbsp; F&nbsp;O&nbsp;R&nbsp;M</SPAN> </th>
    </th>
  </tr>
</table>
<FORM NAME="DslActivityRptFrm" METHOD=POST onsubmit="return OnSubmitForm();">
<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR>
	<TD   align=right>From Date:&nbsp;</TD>
	<TD><SELECT name="from_due_date_mnth">
<%
		String y;
		Calendar cal = Calendar.getInstance();
		
		int iMth = cal.get(Calendar.MONTH) + 1;
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
	<TD   align=right>To Date:&nbsp;</TD>
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
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR>
<td colspan=4  align=center>Select a State:<br><span class=smallNote>(<i>Hold down Control Key to select more than one state</i>)</span></td>
</TR>
<TR>
<td colspan=4  align=center>		
	<%=printSelectBoxStates_ALLOp("state", strArrStt, 6 )%>
</td>		
</tr>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR>
<td  colspan=4 align=center>Select Agent(s):<br><span class=smallNote>(<i>Hold down Control Key to select more than one state</i>)</span></td>
</TR>
<TR>
<td colspan=4  align=center>		
	<%=htmlSelectBox%>
</td>		
</tr>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR>
	<TD align=center colspan=4>
	<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit" onclick="document.pressed=this.value">
        &nbsp;&nbsp;
	<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="EXCEL"onclick="document.pressed=this.value" >
        </TD>

</TR>
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
