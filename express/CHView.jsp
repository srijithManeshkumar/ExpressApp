<%try{
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
	final String CONTROLLER = "CHCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="beanCH1" scope="request" class="com.alltel.lsr.common.objects.CHBean" />

<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;
%>

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">CHILD&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="CHCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= beanCH1.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<input type="hidden" size=17 maxLength=15 NAME="sequence_nbr" VALUE="<%= beanCH1.getUsrGrpAssgnmntSqncNmbr() %>">

<tr>
<td align=right>User&nbsp;Id:</td>
<td align=left><%= session.getAttribute("UserID_userid") %></td>
<input type="hidden" size=16 maxLength=15 NAME="ID" VALUE="<%= session.getAttribute("UserID_userid") %>">
</tr>

<tr>
<td align=right>NAME1:</td> 
<td align=left>

<%
con = DatabaseManager.getConnection();
stmt = con.createStatement();
rs = stmt.executeQuery("SELECT * FROM CH_T " +
			"WHERE SEQUENCE_NBR = '" + beanCH1.getUsrGrpAssgnmntSqncNmbr() + 
			"' ORDER BY NAME1 ASC");

if (rs.next())
{
	 
%>
		<input type=text NAME="NAME1"  value="<%= rs.getString("NAME1") %>"  >
      
</td>
</tr>
<tr>
<td align=right>NAME2</td> 
<td align=left>	<input type=text NAME="NAME2"  value="<%= rs.getString("NAME2") %>"  >
</tr>		 
<%
}else{%>
    <input type=text NAME="NAME1"    >
      
</td>
</tr>
<tr>
<td align=right>NAME2</td> 
<td align=left>	<input type=text NAME="NAME2">
</tr>		
<% }
DatabaseManager.releaseConnection(con);
%>


 


<input type="hidden" NAME="MDFD_USERID" VALUE="<%= beanCH1.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= beanCH1.getMdfdDt() %>">

<%	if (beanCH1.getDbAction().equals("get") ||
	    beanCH1.getDbAction().equals("UpdateRow") ||
	    beanCH1.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= beanCH1.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td align=center colspan=2>
<%      if (beanCH1.getDbAction().equals("new") ||
	    beanCH1.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (beanCH1.getDbAction().equals("get") ||
		beanCH1.getDbAction().equals("UpdateRow") ||
		beanCH1.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(beanCH1.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(beanCH1.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(beanCH1.getTblAdmnScrtyTgMod()) )
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
} catch(Exception e){
    System.out.println("=-========="+e);
    e.printStackTrace();
}
%>
