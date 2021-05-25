<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "SecurityGroupAssignmentCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="securitygroupassignmentbean" scope="request" class="com.alltel.lsr.common.objects.SecurityGroupAssignmentBean" />

<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;
%>

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">Security&nbsp;Group&nbsp;Assignment&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="SecurityGroupAssignmentCtlr" method="POST">

<input type="hidden" NAME="SCRTY_GRP_ASSGNMNT_SQNC_NMBR" VALUE="<%= securitygroupassignmentbean.getScrtyGrpAssgnmntSqncNmbr() %>">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= securitygroupassignmentbean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<tr>
<td align=right>Security&nbsp;Group&nbsp;Code:</td> 
<td align=left><%= session.getAttribute("SecGrp_scrtygrpcd") %></td>
<input type="hidden" size=14 maxLength=12 NAME="SCRTY_GRP_CD" VALUE="<%= session.getAttribute("SecGrp_scrtygrpcd") %>">
</tr>

<tr>
<td align=right>Security&nbsp;Object&nbsp;Code:</td> 
<td align=left><select NAME="SCRTY_OBJCT_CD">

<%
con = DatabaseManager.getConnection();
stmt = con.createStatement();
rs = stmt.executeQuery("SELECT SCRTY_OBJCT_CD, SCRTY_OBJCT_DSCRPTN FROM SECURITY_OBJECT_T ORDER BY SCRTY_OBJCT_DSCRPTN ASC");

while (rs.next() == true)
{
	if (securitygroupassignmentbean.getScrtyObjctCd().equals(rs.getString("SCRTY_OBJCT_CD")))
	{
%>
		<option value=<%= rs.getString("SCRTY_OBJCT_CD") %> SELECTED><%= rs.getString("SCRTY_OBJCT_CD") %>:&nbsp;&nbsp;<%= rs.getString("SCRTY_OBJCT_DSCRPTN") %>
<%	}
	else
	{
%>
		<option value=<%= rs.getString("SCRTY_OBJCT_CD") %>><%= rs.getString("SCRTY_OBJCT_CD") %>:&nbsp;&nbsp;<%= rs.getString("SCRTY_OBJCT_DSCRPTN") %>
<%
	}
}
DatabaseManager.releaseConnection(con);
%>

</td>
</tr>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= securitygroupassignmentbean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= securitygroupassignmentbean.getMdfdDt() %>">

<%	if (securitygroupassignmentbean.getDbAction().equals("get") ||
	    securitygroupassignmentbean.getDbAction().equals("UpdateRow") ||
	    securitygroupassignmentbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= securitygroupassignmentbean.getMdfdDt() %></td>
		</tr>

		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= securitygroupassignmentbean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td align=center colspan=2>
<%      if (securitygroupassignmentbean.getDbAction().equals("new") ||
	    securitygroupassignmentbean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (securitygroupassignmentbean.getDbAction().equals("get") ||
	         securitygroupassignmentbean.getDbAction().equals("UpdateRow") ||
	         securitygroupassignmentbean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(securitygroupassignmentbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(securitygroupassignmentbean.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(securitygroupassignmentbean.getTblAdmnScrtyTgMod()) )
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

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SecurityGroupAssignmentView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:50:32   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:40   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

%>
