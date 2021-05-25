<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "UserGroupCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="usergroupbean" scope="request" class="com.alltel.lsr.common.objects.UserGroupBean" />

<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;
%>

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">User&nbsp;Group&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="UserGroupCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= usergroupbean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<%      if (usergroupbean.getDbAction().equals("new") ||
	    usergroupbean.getDbAction().equals("InsertRow"))
	{
%>
		<tr>
		<td align=right><%= usergroupbean.getUsrGrpCdSPANStart() %>User&nbsp;Group&nbsp;Code:<%= usergroupbean.getUsrGrpCdSPANStart() %></td> 
		<td align=left><input type="TEXT" size=16 maxlength=15 NAME="USR_GRP_CD" VALUE="<%= usergroupbean.getUsrGrpCd() %>"></td>
		</tr>
<%      }
	else if (usergroupbean.getDbAction().equals("get") ||
		 usergroupbean.getDbAction().equals("UpdateRow") ||
		 usergroupbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>User&nbsp;Group&nbsp;Code:</td> 
		<td align=left><%= usergroupbean.getUsrGrpCd() %></td>
		<input type="hidden" size=16 maxLength=15 NAME="USR_GRP_CD" VALUE="<%= usergroupbean.getUsrGrpCd() %>">
		</tr>
<%      }
%>

<tr>
<td align=right><%= usergroupbean.getUsrGrpDscrptnSPANStart() %>User&nbsp;Group&nbsp;Description:<%= usergroupbean.getUsrGrpDscrptnSPANStart() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="USR_GRP_DSCRPTN" VALUE="<%= usergroupbean.getUsrGrpDscrptn() %>"></td>
</tr>

<tr>
<td align=right>Company:</td> 
<td align=left><select NAME="CMPNY_SQNC_NMBR">

<%
con = DatabaseManager.getConnection();
stmt = con.createStatement();
rs = stmt.executeQuery("SELECT CMPNY_SQNC_NMBR, CMPNY_NM FROM COMPANY_T ORDER BY CMPNY_NM ASC");

while (rs.next() == true)
{
	if (usergroupbean.getCmpnySqncNmbr().equals(rs.getString("CMPNY_SQNC_NMBR")))
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

<tr>
<td align=right><%= usergroupbean.getOcnCdSPANStart() %>OCN:<%= usergroupbean.getOcnCdSPANStart() %></td> 
<td align=left><select NAME="OCN_CD">
<option value=*>*</OPTION>
<%
con = DatabaseManager.getConnection();
stmt = con.createStatement();
rs = stmt.executeQuery("SELECT OCN_CD FROM OCN_T ORDER BY OCN_CD ASC");

while (rs.next() == true)
{
	if (usergroupbean.getOcnCd().equals(rs.getString("OCN_CD")))
	{
%>
		<option value=<%= rs.getString("OCN_CD") %> SELECTED><%= rs.getString("OCN_CD") %></OPTION>
<%	}
	else
	{
%>
		<option value=<%= rs.getString("OCN_CD") %>><%= rs.getString("OCN_CD") %></OPTION>
<%
	}
}
DatabaseManager.releaseConnection(con);
%>

</td>
</tr>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= usergroupbean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= usergroupbean.getMdfdDt() %>">

<%	if (usergroupbean.getDbAction().equals("get") ||
	    usergroupbean.getDbAction().equals("UpdateRow") ||
	    usergroupbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= usergroupbean.getMdfdDt() %></td>
		</tr>
		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= usergroupbean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td align=center colspan=2>
<%      if (usergroupbean.getDbAction().equals("new") ||
	    usergroupbean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (usergroupbean.getDbAction().equals("get") ||
	         usergroupbean.getDbAction().equals("UpdateRow") ||
	         usergroupbean.getDbAction().equals("DeleteRow"))
	{
		
		if (sdm.isAuthorized(usergroupbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(usergroupbean.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(usergroupbean.getTblAdmnScrtyTgMod()) )
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
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/UserGroupView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:47:12   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:58   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
%>
