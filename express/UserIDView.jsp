<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "UserIDCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="useridbean" scope="request" class="com.alltel.lsr.common.objects.UserIDBean" />

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">User&nbsp;Id&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="UserIDCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= useridbean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<% 	if (useridbean.getDbAction().equals("new") ||
	    useridbean.getDbAction().equals("InsertRow"))
	{
%>
		<tr>
		<td align=right><%= useridbean.getUserIDSPANStart() %>User&nbsp;Id:<%= useridbean.getUserIDSPANEnd() %></td> 
		<td align=left><input type="TEXT" size=16 maxLength=15 NAME="USERID" VALUE="<%= useridbean.getUserID() %>"></td>
		</tr>
<%	}
	else if (useridbean.getDbAction().equals("get") ||
		 useridbean.getDbAction().equals("UpdateRow") ||
		 useridbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>User&nbsp;Id:</td> 
		<td align=left><%= useridbean.getUserID() %></td>
		<input type="hidden" size=16 maxLength=15 NAME="USERID" VALUE="<%= useridbean.getUserID() %>">
		</tr>
<%	}
%>

<tr>
<td align=right><%= useridbean.getFrstNmSPANStart() %>First&nbsp;Name:<%= useridbean.getFrstNmSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="FRST_NM" VALUE="<%= useridbean.getFrstNm() %>"></td>
</tr>

<tr>
<td align=right><%= useridbean.getLstNmSPANStart() %>Last&nbsp;Name:<%= useridbean.getLstNmSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="LST_NM" VALUE="<%= useridbean.getLstNm() %>"></td>
</tr>

<tr>
<td align=right><%= useridbean.getEmlIdSPANStart() %>Email&nbsp;Address:<%= useridbean.getEmlIdSPANEnd() %></td> 
<td align=left><input type="EMAIL" size=52 maxLength=50 NAME="EMAIL" VALUE="<%= useridbean.getEmlId() %>"></td>
</tr>

<tr>
<% 	if (useridbean.getDbAction().equals("new") ||
	    useridbean.getDbAction().equals("InsertRow"))
	{
%>
		<td align=right><%= useridbean.getEncrptdPsswdSPANStart() %>Password:<%= useridbean.getEncrptdPsswdSPANEnd() %></td> 
		<td align=left><input type="PASSWORD" size=22 maxLength=20 NAME="ENCRPTD_PSSWD" VALUE=""></td>
		<input type="hidden" NAME="CHNG_PSSWD" VALUE="yes">
		<input type="hidden" NAME="FRC_PSSWD_CHNG" VALUE="N">
		<input type="hidden" NAME="ENABLE_PSSWD" VALUE="yes">
<%	}
	else
	{
%>
		<td align=right><%= useridbean.getEncrptdPsswdSPANStart() %>Password:<%= useridbean.getEncrptdPsswdSPANEnd() %></td> 
		<td align=left><input type="PASSWORD" size=22 maxLength=20 NAME="ENCRPTD_PSSWD" VALUE="">
		&nbsp;&nbsp;&nbsp;Change&nbsp;Password:
		<input type="checkbox" NAME="CHNG_PSSWD" VALUE="yes">
		</td>
</tr>
<tr>
		<td align=right>Force&nbsp;Password&nbsp;Change&nbsp;on&nbsp;next&nbsp;logon:</td>
		<td align=left><input type="checkbox" NAME="FRC_PSSWD_CHNG" VALUE="yes">
		</td>
<%	}
%>
</tr>

<tr>
<td align=right><%= useridbean.getPsswdRcvrQstnSPANStart() %>Password&nbsp;Recovery&nbsp;Question:<%= useridbean.getPsswdRcvrQstnSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="PSSWD_RCVR_QSTN" VALUE="<%= useridbean.getPsswdRcvrQstn() %>"></td>
</tr>

<tr>
<td align=right><%= useridbean.getPsswdRcvrNswrSPANStart() %>Password&nbsp;Recovery&nbsp;Answer:<%= useridbean.getPsswdRcvrNswrSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="PSSWD_RCVR_NSWR" VALUE="<%= useridbean.getPsswdRcvrNswr() %>"></td>
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
	if (useridbean.getCmpnySqncNmbr().equals(rs.getString("CMPNY_SQNC_NMBR")))
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

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= useridbean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= useridbean.getMdfdDt() %>">
<input type="hidden" NAME="DSBLD_USERID" VALUE="<%= useridbean.getDsbldUserID() %>">

<%      if (useridbean.getDbAction().equals("get") ||
	    useridbean.getDbAction().equals("UpdateRow") ||
	    useridbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Last&nbsp;successful&nbsp;login:</td>
		<td align=left><%= useridbean.getLstLgnDt() %></td>
		</tr>

		<tr>
		<td align=right>User&nbsp;login&nbsp;disabled?:</td>
		<td align=left><%= useridbean.getDsbldUserID() %>
<%		  
		if ( useridbean.getDsbldUserID().equals("Y") )
		{
%>
			&nbsp;&nbsp;&nbsp;Enable&nbsp;Password:
			<input type="checkbox" NAME="ENABLE_PSSWD" VALUE="yes">
<%		  
		}
		else
		{
%>
			&nbsp;&nbsp;&nbsp;Disable&nbsp;Password:
			<input type="checkbox" NAME="ENABLE_PSSWD" VALUE="yes">
<%		  
		}
%>
		</td>
		</tr>

		<tr>
		<td align=right>Modified&nbsp;Date:</td>
		<td align=left><%= useridbean.getMdfdDt() %></td>
		</tr>

		<tr>
		<td align=right>Modified&nbsp;Userid:</td>
		<td align=left><%= useridbean.getMdfdUserid() %></td>
		</tr>
<%      }
%>

<% 	if (useridbean.getDbAction().equals("get"))
	{
%>
		<TR><TD align=center colspan=2><A HREF="TableAdminCtlr?tblnmbr=4&rstrctsrch=yes&srchctgry=0&srchvl=<%= useridbean.getUserID() %>">&nbsp;User&nbsp;Group&nbsp;Assignment&nbsp;Table</A></TD></TR>
<%	}
%>

<tr>
<td align=center colspan=2>
<%      if (useridbean.getDbAction().equals("new") ||
	    useridbean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (useridbean.getDbAction().equals("get") ||
	         useridbean.getDbAction().equals("UpdateRow") ||
	         useridbean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(useridbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(useridbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(useridbean.getTblAdmnScrtyTgMod()) )
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
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/UserIDView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:46:32   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:07:00   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

%>
