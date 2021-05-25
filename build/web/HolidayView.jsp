<%@ include file="i_header.jsp" %> 
<%
	final String CONTROLLER = "HolidayCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="holidaybean" scope="request" class="com.alltel.lsr.common.objects.HolidayBean" />

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">Holiday&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="HolidayCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= holidaybean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<%	if (holidaybean.getDbAction().equals("new") ||
	    holidaybean.getDbAction().equals("InsertRow"))
	{
%>
		<tr>
		<td align=right><%= holidaybean.getHldyDtSPANStart() %>Holiday&nbsp;Date:<%= holidaybean.getHldyDtSPANEnd() %></td> 
		<td align=left>
		<%	if (holidaybean.getHldyDt().length() < 10)
			{
		%>
				<input type="TEXT" size=3 maxLength=2 NAME="HLDY_MNTH" VALUE="">
				/
				<input type="TEXT" size=3 maxLength=2 NAME="HLDY_DY" VALUE="">
				/
				<input type="TEXT" size=5 maxLength=4 NAME="HLDY_YR" VALUE=""></td>
		<%	}
			else
			{
		%>
				<input type="TEXT" size=3 maxLength=2 NAME="HLDY_MNTH" VALUE="<%= holidaybean.getHldyDt().substring(0,2) %>">
				/
				<input type="TEXT" size=3 maxLength=2 NAME="HLDY_DY" VALUE="<%= holidaybean.getHldyDt().substring(3,5) %>">
				/
				<input type="TEXT" size=5 maxLength=4 NAME="HLDY_YR" VALUE="<%= holidaybean.getHldyDt().substring(6) %>"></td>
		<%	}
		%>
		</tr>
<%	}
	else if (holidaybean.getDbAction().equals("get") ||
		 holidaybean.getDbAction().equals("UpdateRow") ||
		 holidaybean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Holiday&nbsp;Date:</td> 
		<td align=left><%= holidaybean.getHldyDt() %>
		<input type="hidden" size=14 maxLength=12 NAME="HLDY_DT" VALUE="<%= holidaybean.getHldyDt() %>"></td>
		</tr>
<%	}
%>

<tr>
<td align=right><%= holidaybean.getHldyDscrptnSPANStart() %>Holiday&nbsp;Description:<%= holidaybean.getHldyDscrptnSPANEnd() %></td> 
<td align=left><input type="TEXT" size=32 maxLength=30 NAME="HLDY_DSCRPTN" VALUE="<%= holidaybean.getHldyDscrptn() %>"></td>
</tr>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= holidaybean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= holidaybean.getMdfdDt() %>">

<%	if (holidaybean.getDbAction().equals("get") ||
	    holidaybean.getDbAction().equals("UpdateRow") ||
	    holidaybean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= holidaybean.getMdfdDt() %></td>
		</tr>

		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= holidaybean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td colspan=2 align=center>

<%	if (holidaybean.getDbAction().equals("new") ||
	    holidaybean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%	}
	else if (holidaybean.getDbAction().equals("get") ||
		 holidaybean.getDbAction().equals("UpdateRow") ||
		 holidaybean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(holidaybean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(holidaybean.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(holidaybean.getTblAdmnScrtyTgMod()) )
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
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/HolidayView.jsv  $
/*
/*   Rev 1.2   31 Jan 2002 14:34:24   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 08:23:12   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:48   wwoods
/*Initial Checkin
*/

/* $Revision:   1.2  $
*/
%>
