<%@ include file="i_header.jsp" %>
<% try{
	final String CONTROLLER = "VNTCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="beanVNT1" scope="request" class="com.alltel.lsr.common.objects.VNTBean" />

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">User&nbsp;Id&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="VNTCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= beanVNT1.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<% 	if (beanVNT1.getDbAction().equals("new") ||
	    beanVNT1.getDbAction().equals("InsertRow"))
	{
%>
		<tr>
		<td align=right><%= beanVNT1.getIDSPANStart() %>Id:<%= beanVNT1.getIDSPANEnd() %></td> 
		<td align=left><input type="TEXT" size=16 maxLength=15 NAME="ID" VALUE="<%= beanVNT1.getID() %>"></td>
		</tr>
<%	}
	else if (beanVNT1.getDbAction().equals("get") ||
		 beanVNT1.getDbAction().equals("UpdateRow") ||
		 beanVNT1.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Id:</td> 
		<td align=left><%= beanVNT1.getID() %></td>
		<input type="hidden" size=16 maxLength=15 NAME="ID" VALUE="<%= beanVNT1.getID() %>">
		</tr>
<%	}
%>

<tr>
<td align=right><%= beanVNT1.getNameSPANStart() %>Name:<%= beanVNT1.getNameSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="NAME" VALUE="<%= beanVNT1.getName() %>"></td>
</tr>

<tr>
<td align=right><%= beanVNT1.getAgeSPANStart() %>Age:<%= beanVNT1.getAgeSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="AGE" VALUE="<%= beanVNT1.getAge() %>"></td>
</tr>

<tr>
<td align=right>Company:</td> 
<td align=left><select NAME="CMPNY_SQNC_NMBR">

<%
Connection con = DatabaseManager.getConnection();
Statement stmt = con.createStatement();
ResultSet rs = stmt.executeQuery("SELECT CMPNY_SQNC_NMBR, CMPNY_NM FROM COMPANY_T ORDER BY CMPNY_NM ASC");

while (rs.next() == true)
{
	if (beanVNT1.getCmpnySqncNmbr().equals(rs.getString("CMPNY_SQNC_NMBR")))
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



<%      if (beanVNT1.getDbAction().equals("get") ||
	    beanVNT1.getDbAction().equals("UpdateRow") ||
	    beanVNT1.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Userid:</td>
		<td align=left><%= beanVNT1.getMdfdUserid() %></td>
		</tr>
<%      }
%>

<% 	if (beanVNT1.getDbAction().equals("get"))
	{
%>
		<TR><TD align=center colspan=2><A HREF="TableAdminCtlr?tblnmbr=2124&rstrctsrch=yes&srchctgry=0&srchvl=<%= beanVNT1.getID() %>">&nbsp;CHILD&nbsp;Table</A></TD></TR>
<%	}
%>

<tr>
<td align=center colspan=2>
<%      if (beanVNT1.getDbAction().equals("new") ||
	    beanVNT1.getDbAction().equals("InsertRow"))
	{
%>
<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (beanVNT1.getDbAction().equals("get") ||
	         beanVNT1.getDbAction().equals("UpdateRow") ||
	         beanVNT1.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(beanVNT1.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(beanVNT1.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(beanVNT1.getTblAdmnScrtyTgMod()) )
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

<%}catch(Exception e){
    e.printStackTrace();
}

%>
