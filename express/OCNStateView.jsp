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
 * MODULE:	OCNStateView.jsp	
 * 
 * DESCRIPTION: JSP View used to maintain the OCN_STATE_T table.
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
	final String CONTROLLER = "OCNStateCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="ocnstatebean" scope="request" class="com.alltel.lsr.common.objects.OCNStateBean" />

<%@ page import ="java.util.*" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>
<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;
%>

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">OCN&nbsp;State&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="OCNStateCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= ocnstatebean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<input type="hidden" size=17 maxLength=15 NAME="OCN_STT_SQNC_NMBR" VALUE="<%= ocnstatebean.getOcnSttSqncNmbr() %>">

<tr>
<td align=right>OCN&nbsp;Code:</td> 
<td align=left><select NAME="OCN_CD">

<%
con = DatabaseManager.getConnection();
stmt = con.createStatement();
rs = stmt.executeQuery("SELECT OCN_CD FROM OCN_T ORDER BY OCN_CD ASC");

while (rs.next() == true)
{
	if (ocnstatebean.getOcnCd().equals(rs.getString("OCN_CD")))
	{
%>
		<option value=<%= rs.getString("OCN_CD") %> SELECTED><%= rs.getString("OCN_CD") %>
<%	}
	else
	{
%>
		<option value=<%= rs.getString("OCN_CD") %>><%= rs.getString("OCN_CD") %>
<%
	}
}
DatabaseManager.releaseConnection(con);
%>
</td>
</tr>

<tr>
<td align=right>State&nbsp;Code:</td> 
<td align=left><select NAME="STT_CD">

<%
con = DatabaseManager.getConnection();
stmt = con.createStatement();
rs = stmt.executeQuery("SELECT STT_CD, STT_NM FROM STATE_T ORDER BY STT_NM ASC");

while (rs.next() == true)
{
	if (ocnstatebean.getSttCd().equals(rs.getString("STT_CD")))
	{
%>
		<option value=<%= rs.getString("STT_CD") %> SELECTED><%= rs.getString("STT_NM") %>
<%	}
	else
	{
%>
		<option value=<%= rs.getString("STT_CD") %>><%= rs.getString("STT_NM") %>
<%
	}
}
DatabaseManager.releaseConnection(con);
%>
</td>
</tr>

<tr>
<td align=right><%= ocnstatebean.getOcnSttSlaDysSPANStart() %>OCN&nbsp;State&nbsp;SLA&nbsp;Days:<%= ocnstatebean.getOcnSttSlaDysSPANEnd() %></td> 
<td align=left><input type="TEXT" size=5 maxLength=3 NAME="OCN_STT_SLA_DYS" VALUE="<%= ocnstatebean.getOcnSttSlaDys() %>"></td>
</tr>

<tr>
<td align=right><%= ocnstatebean.getOcnSttCntrctPrcntgSPANStart() %>OCN&nbsp;State&nbsp;Contract&nbsp;Percentage:<%= ocnstatebean.getOcnSttCntrctPrcntgSPANEnd() %></td> 
<td align=left><input type="TEXT" size=5 maxLength=3 NAME="OCN_STT_CNTRCT_PRCNTG" VALUE="<%= ocnstatebean.getOcnSttCntrctPrcntg() %>"></td>
</tr>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= ocnstatebean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= ocnstatebean.getMdfdDt() %>">

<%	if (ocnstatebean.getDbAction().equals("get") ||
	    ocnstatebean.getDbAction().equals("UpdateRow") ||
	    ocnstatebean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= ocnstatebean.getMdfdDt() %></td>
		</tr>

		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= ocnstatebean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td align=center colspan=2>
<%      if (ocnstatebean.getDbAction().equals("new") ||
	    ocnstatebean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (ocnstatebean.getDbAction().equals("get") ||
	         ocnstatebean.getDbAction().equals("UpdateRow") ||
	         ocnstatebean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(ocnstatebean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(ocnstatebean.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(ocnstatebean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
<%
		}
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
%>

</td>
</tr>

</table>

</form>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
