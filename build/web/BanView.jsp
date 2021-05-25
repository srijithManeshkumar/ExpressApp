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
 * MODULE:	BanView.jsp	
 * 
 * DESCRIPTION: JSP View used to maintain BANs
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        6-1-2002
 * 
 * HISTORY:
 *	xx/dd/2002  
 *
 */

%>

<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "BanCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="banbean" scope="request" class="com.alltel.lsr.common.objects.BanBean" />

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">BAN&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="BanCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= banbean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<% 	if (banbean.getDbAction().equals("new") ||
	    banbean.getDbAction().equals("InsertRow"))
	{
%>
		<tr>
		<td align=right><%= banbean.getBanSPANStart() %>BAN&nbsp;<i><font size=-2>(Please use "_" for spaces)</font></i>&nbsp;:<%= banbean.getBanSPANEnd() %></td> 
		<td align=left><input type="TEXT" size=15 maxLength=13 NAME="BAN" VALUE="<%= banbean.getBan() %>"></td>
		</tr>
<%	}
	else if (banbean.getDbAction().equals("get") ||
		 banbean.getDbAction().equals("UpdateRow") ||
		 banbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>BAN:</td> 
		<td align=left><%= banbean.getBan() %></td>
		<input type="hidden" NAME="BAN" VALUE="<%= banbean.getBan() %>">
		</tr>
<%	}
%>

<tr>
<td align=right><%= banbean.getBanDscrptnSPANStart() %>BAN&nbsp;Description:<%= banbean.getBanDscrptnSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="BAN_DSCRPTN" VALUE="<%= banbean.getBanDscrptn() %>"></td>
</tr>

<tr>
<td align=right>Company:</td> 
<td align=left><select NAME="CMPNY_SQNC_NMBR">

<%
Connection con = DatabaseManager.getConnection();
Statement stmt = con.createStatement();
ResultSet rs = stmt.executeQuery("SELECT CMPNY_SQNC_NMBR, CMPNY_NM FROM COMPANY_T WHERE CMPNY_TYP = 'I' ORDER BY CMPNY_NM ASC");

while (rs.next() == true)
{
	if (banbean.getCmpnySqncNmbr().equals(rs.getString("CMPNY_SQNC_NMBR")))
	{
%>
		<option value=<%= rs.getString("CMPNY_SQNC_NMBR") %> SELECTED><%= rs.getString("CMPNY_NM") %>
<%	}
	else
	{
%>
		<option value=<%= rs.getString("CMPNY_SQNC_NMBR") %>><%= rs.getString("CMPNY_NM") %>
<%
	}
}
DatabaseManager.releaseConnection(con);
%>

</td>
</tr>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= banbean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= banbean.getMdfdDt() %>">

<%      if (banbean.getDbAction().equals("get") ||
	    banbean.getDbAction().equals("UpdateRow") ||
	    banbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td>
		<td align=left><%= banbean.getMdfdDt() %></td>
		</tr>

		<tr>
		<td align=right>Modified&nbsp;Userid:</td>
		<td align=left><%= banbean.getMdfdUserid() %></td>
		</tr>
<%      }
%>

<tr>
<td align=center colspan=2>
<%      if (banbean.getDbAction().equals("new") ||
	    banbean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (banbean.getDbAction().equals("get") ||
	         banbean.getDbAction().equals("UpdateRow") ||
	         banbean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(banbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(banbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(banbean.getTblAdmnScrtyTgMod()) )
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
