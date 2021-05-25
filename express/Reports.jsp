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
 * MODULE:	Reports.jsp	
 * 
 * DESCRIPTION: JSP View used to display Express Reports
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *	xx/dd/2002  
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%
	final String CONTROLLER = "PROV_REPORTS";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>


<BR>
<TABLE width=50% align=center cellspacing=0 cellpadding=0 border=0>
	<TR>
		<TD><HR></TD>
		<TD align=center width=50%><SPAN CLASS="header1">REPORTS</SPAN></TD>
		<TD><HR></TD>
</TABLE>
<BR CLEAR=ALL>
<BR>
<BR>
<TABLE align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD align=left><SPAN CLASS="smallstyle1">>&nbsp;<a href=SLADateSelect.jsp>SLA Report - Orders</a>&nbsp;</SPAN></TD></TR>
  <TR><TD >&nbsp;</TD></TR>
  <TR><TD align=left><SPAN CLASS="smallstyle1">>&nbsp;<a href=PreorderDateSelect.jsp>Preorder Report</a>&nbsp;</SPAN></TD></TR>
  <TR><TD >&nbsp;</TD></TR>
  <TR><TD align=left><SPAN CLASS="smallstyle1">>&nbsp;<a href=CODateSelect.jsp>Completed Orders Report</a>&nbsp;</SPAN></TD></TR>
  <TR><TD >&nbsp;</TD></TR>
  <TR><TD align=left><SPAN CLASS="smallstyle1">>&nbsp;<a href=DslDateSelect.jsp>DSL Completed Orders Report</a>&nbsp;</SPAN></TD></TR>
  <TR><TD >&nbsp;</TD></TR>
  <TR><TD align=left><SPAN CLASS="smallstyle1">>&nbsp;<a href=MthDateSelect.jsp?mrpt=MonthlyReport.jsp>Monthly Statistics</a></SPAN></TD></TR>
  <TR><TD >&nbsp;</TD></TR>
  <TR><TD align=left><SPAN CLASS="smallstyle1">>&nbsp;<a href=MthDateSelect.jsp?mrpt=MonthlyOcnReport.jsp>Monthly Statistics by Vendor</a></SPAN></TD></TR>
</TABLE>
</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
