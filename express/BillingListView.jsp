<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				Windstream COMMUNICATIONS, INC.
 */
/** 
 * MODULE:		BillingListView.jsp
 * 
 * DESCRIPTION: Displays a list of BANs for this user to view
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-01-2002
 * 
 * HISTORY:
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 
<%
	final String SECURITY_OBJECT = "VIEW_BILLING";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		 alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<table align=center width="100%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">View&nbsp;Bills</SPAN>
    </th>
  </tr>
</table>
<BR CLEAR=ALL>

<%-- Commented out for now
<table align=center border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=2>
	<FORM NAME="BillingListView" METHOD="POST" ACTION="BillingCtlr?search=ban_srch">
	<tr>
		<td>&nbsp;BAN&nbsp;Search&nbsp;>>>&nbsp;</td>
		<td><input type=text maxLength=13 size=15 name=ban_srch_value></td>
		<td><INPUT class=appButton TYPE="SUBMIT" name="submitbutton" value="GO"></td>
	</tr>
	</FORM>
</table>
%>

<TABLE align=center border=1 cellspacing=0 cellpadding=0>
<TH align=center>BAN&nbsp;Code</TH>
<TH align=center>BAN&nbsp;Description</TH>
<TH align=center>Bill&nbsp;Load&nbsp;Date</TH>

<%
	// Retrieve Parameter
	String strQuery = (String) request.getAttribute("banlistquery");

	Connection con = DatabaseManager.getConnection();
	Statement stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(strQuery);
	while (rs.next() == true)
	{
%>
		<TR>
<%		if (rs.getString("LD_DT") != null && rs.getString("LD_DT").length() > 0)
		{
%>
			<TD width=100 align=left>&nbsp;<A HREF="BillingCtlr?ban=<%=rs.getString("BAN")%>&ld_dt=<%=rs.getString("LD_DT")%>" target=_blank><%=rs.getString("BAN")%></A></TD>
			<TD width=300 align=left>&nbsp;<%= rs.getString("BAN_DSCRPTN") %></TD>
			<TD width=120 align=left>&nbsp;<%= rs.getString("LD_DT") %></TD>
<%		}
		else
		{
%>
			<TD width=100 align=left>&nbsp;<%= rs.getString("BAN") %></TD>
			<TD width=300 align=left>&nbsp;<%= rs.getString("BAN_DSCRPTN") %></TD>
			<TD width=120 align=left>&nbsp;No Bill Images Available</TD>
<%		}
%>			
		</TR>
<%
	} //while
	DatabaseManager.releaseConnection(con);
%>
</TABLE>
<BR CLEAR=ALL><BR>
<BR>

</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
