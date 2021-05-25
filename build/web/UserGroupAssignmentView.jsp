<%
/**
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                      COPYRIGHT (C) 2003
 *                              BY
 *                      Windstream COMMUNICATIONS INC.
 */
/**
 * MODULE:      UserGroupAssignmentView.jsp
 *
 * DESCRIPTION: 
 *
 * AUTHOR:      Express dev group
 *
 * DATE:        01-02-2002
 *
 * HISTORY:
 *      02/25/2003  psedlak Wrap user group in quotes to allow spaces in field
 *
 */
%>

<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "UserGroupAssignmentCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="usergroupassignmentbean" scope="request" class="com.alltel.lsr.common.objects.UserGroupAssignmentBean" />

<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;
%>

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">User&nbsp;Group&nbsp;Assignment&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="UserGroupAssignmentCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= usergroupassignmentbean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<input type="hidden" size=17 maxLength=15 NAME="USR_GRP_ASSGNMNT_SQNC_NMBR" VALUE="<%= usergroupassignmentbean.getUsrGrpAssgnmntSqncNmbr() %>">

<tr>
<td align=right>User&nbsp;Id:</td>
<td align=left><%= session.getAttribute("UserID_userid") %></td>
<input type="hidden" size=16 maxLength=15 NAME="USERID" VALUE="<%= session.getAttribute("UserID_userid") %>">
</tr>

<tr>
<td align=right>User&nbsp;Group&nbsp;Code:</td> 
<td align=left><select NAME="USR_GRP_CD">

<%
con = DatabaseManager.getConnection();
stmt = con.createStatement();
rs = stmt.executeQuery("SELECT USR_GRP_CD, USR_GRP_DSCRPTN FROM USER_GROUP_T UG, USERID_T U " +
			"WHERE U.CMPNY_SQNC_NMBR = UG.CMPNY_SQNC_NMBR AND USERID = '" + session.getAttribute("UserID_userid") + 
			"' ORDER BY USR_GRP_DSCRPTN ASC");

while (rs.next() == true)
{
	if (usergroupassignmentbean.getUsrGrpCd().equals(rs.getString("USR_GRP_CD")))
	{
%>
		<option value="<%= rs.getString("USR_GRP_CD") %>" SELECTED><%= rs.getString("USR_GRP_DSCRPTN") %>
<%      }
	else
	{
%>
		<option value="<%= rs.getString("USR_GRP_CD") %>"><%= rs.getString("USR_GRP_DSCRPTN") %>
<%
	}
}
DatabaseManager.releaseConnection(con);
%>

<tr>
<td align=right>Security&nbsp;Group&nbsp;Code:</td> 
<td align=left><select NAME="SCRTY_GRP_CD">

<%
con = DatabaseManager.getConnection();
stmt = con.createStatement();
rs = stmt.executeQuery("SELECT SCRTY_GRP_CD, SCRTY_GRP_DSCRPTN FROM SECURITY_GROUP_T ORDER BY SCRTY_GRP_DSCRPTN ASC");

while (rs.next() == true)
{
	if (usergroupassignmentbean.getUsrGrpCd().equals(rs.getString("SCRTY_GRP_CD")))
	{
%>
		<option value=<%= rs.getString("SCRTY_GRP_CD") %> SELECTED><%= rs.getString("SCRTY_GRP_DSCRPTN") %>
<%      }
	else
	{
%>
		<option value=<%= rs.getString("SCRTY_GRP_CD") %>><%= rs.getString("SCRTY_GRP_DSCRPTN") %>
<%
	}
}
DatabaseManager.releaseConnection(con);
%>


<input type="hidden" NAME="MDFD_USERID" VALUE="<%= usergroupassignmentbean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= usergroupassignmentbean.getMdfdDt() %>">

<%	if (usergroupassignmentbean.getDbAction().equals("get") ||
	    usergroupassignmentbean.getDbAction().equals("UpdateRow") ||
	    usergroupassignmentbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= usergroupassignmentbean.getMdfdDt() %></td>
		</tr>
		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= usergroupassignmentbean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td align=center colspan=2>
<%      if (usergroupassignmentbean.getDbAction().equals("new") ||
	    usergroupassignmentbean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (usergroupassignmentbean.getDbAction().equals("get") ||
		usergroupassignmentbean.getDbAction().equals("UpdateRow") ||
		usergroupassignmentbean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(usergroupassignmentbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(usergroupassignmentbean.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(usergroupassignmentbean.getTblAdmnScrtyTgMod()) )
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
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/UserGroupAssignmentView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:47:50   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:54   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
%>
