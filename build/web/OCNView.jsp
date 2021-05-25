<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "OCNCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="ocnbean" scope="request" class="com.alltel.lsr.common.objects.OCNBean" />

<%@ page import ="java.util.*" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>

</head>

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">OCN&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="OCNCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= ocnbean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<%      if (ocnbean.getDbAction().equals("new") ||
	    ocnbean.getDbAction().equals("InsertRow"))
	{
%>
		<tr>
		<td align=right><%= ocnbean.getOcnCdSPANStart() %>OCN&nbsp;Code:<%= ocnbean.getOcnCdSPANEnd() %></td> 
		<td align=left><input type="TEXT" size=5 maxLength=4 NAME="OCN_CD" VALUE="<%= ocnbean.getOcnCd() %>"></td>
		</tr>
<%      }
	else if (ocnbean.getDbAction().equals("get") ||
	         ocnbean.getDbAction().equals("UpdateRow") ||
	         ocnbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>OCN&nbsp;Code:</td> 
		<td align=left><%= ocnbean.getOcnCd() %></td>
		<input type="hidden" size=5 maxLength=4 NAME="OCN_CD" VALUE="<%= ocnbean.getOcnCd() %>">
		</tr>
<%      }
%>

<tr>
<td align=right>Company:</td> 
<td align=left><select NAME="CMPNY_SQNC_NMBR" onchange="myOcnFunction()">

<%
Connection con = DatabaseManager.getConnection();
Statement stmt = con.createStatement();
ResultSet rs = stmt.executeQuery("SELECT CMPNY_SQNC_NMBR, CMPNY_NM FROM COMPANY_T ORDER BY CMPNY_NM ASC");

while (rs.next() == true)
{
	if (ocnbean.getCmpnySqncNmbr().equals(rs.getString("CMPNY_SQNC_NMBR")))
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
<td align=right><%= ocnbean.getOcnNmSPANStart() %>OCN&nbsp;Description:<%= ocnbean.getOcnNmSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="OCN_NM" VALUE=""></td>
</tr>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= ocnbean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= ocnbean.getMdfdDt() %>">

<% 	if (ocnbean.getDbAction().equals("get") ||
            ocnbean.getDbAction().equals("UpdateRow") ||
            ocnbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= ocnbean.getMdfdDt() %></td>
		</tr>
		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= ocnbean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td align=center colspan=2>
<%      if (ocnbean.getDbAction().equals("new") ||
	    ocnbean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (ocnbean.getDbAction().equals("get") ||
	         ocnbean.getDbAction().equals("UpdateRow") ||
	         ocnbean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(ocnbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(ocnbean.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(ocnbean.getTblAdmnScrtyTgMod()) )
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
<script>
function myOcnFunction() {
	var elem = document.getElementsByName('CMPNY_SQNC_NMBR')[0];
        document.getElementsByName('OCN_NM')[0].value = elem.options[elem.selectedIndex].text;
}
myOcnFunction();
</script>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/OCNView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:11:32   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:10   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
%>
