<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "SecurityGroupCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>


<jsp:useBean id="securitygroupbean" scope="request" class="com.alltel.lsr.common.objects.SecurityGroupBean" />

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">Security&nbsp;Group&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="SecurityGroupCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= securitygroupbean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<%	if (securitygroupbean.getDbAction().equals("new") ||
	    securitygroupbean.getDbAction().equals("InsertRow"))
	{
%>
		<tr>
		<td align=right><%= securitygroupbean.getScrtyGrpCdSPANStart() %>Security&nbsp;Group&nbsp;Code:<%= securitygroupbean.getScrtyGrpCdSPANEnd() %></td> 
		<td align=left><input type="TEXT" size=16 maxLength=15 NAME="SCRTY_GRP_CD" VALUE="<%= securitygroupbean.getScrtyGrpCd() %>"></td>
		</tr>
<%	}
	else if (securitygroupbean.getDbAction().equals("get") ||
		 securitygroupbean.getDbAction().equals("UpdateRow") ||
		 securitygroupbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Security&nbsp;Group&nbsp;Code:</td> 
		<td align=left><%= securitygroupbean.getScrtyGrpCd() %>
		<input type="hidden" size=16 maxLength=15 NAME="SCRTY_GRP_CD" VALUE="<%= securitygroupbean.getScrtyGrpCd() %>"></td>
		</tr>
<%	}
%>

<tr>
<td align=right><%= securitygroupbean.getScrtyGrpDscrptnSPANStart() %>Security&nbsp;Group&nbsp;Description:<%= securitygroupbean.getScrtyGrpDscrptnSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="SCRTY_GRP_DSCRPTN" VALUE="<%= securitygroupbean.getScrtyGrpDscrptn() %>"></td>
</tr>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= securitygroupbean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= securitygroupbean.getMdfdDt() %>">

<%	if (securitygroupbean.getDbAction().equals("get") ||
	    securitygroupbean.getDbAction().equals("UpdateRow") ||
	    securitygroupbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= securitygroupbean.getMdfdDt() %></td>
		</tr>

		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= securitygroupbean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<% 	if (securitygroupbean.getDbAction().equals("get"))
	{
%>
		<tr><td align=center colspan=2><A HREF="TableAdminCtlr?tblnmbr=9&rstrctsrch=yes&srchctgry=0&srchvl=<%= securitygroupbean.getScrtyGrpCd() %>">&nbsp;Security&nbsp;Group&nbsp;Assignment&nbsp;Table</A></td></tr>
<%	}
%>

<tr>
<td colspan=2 align=center>

<%	if (securitygroupbean.getDbAction().equals("new") ||
	    securitygroupbean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%	}
	else if (securitygroupbean.getDbAction().equals("get") ||
		 securitygroupbean.getDbAction().equals("UpdateRow") ||
		 securitygroupbean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(securitygroupbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(securitygroupbean.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(securitygroupbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
<%
		}
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%	}
%>

</td>
</tr>

</table>

</form>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/SecurityGroupView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:49:52   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:42   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

%>
