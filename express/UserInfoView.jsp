<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2003
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	UserInfoView.jsp
 * 
 * DESCRIPTION: JSP View used to allow the user to modify their specific information
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 **************************************************************************************
 *	HDR : 278175  
 * DESCRIPTION: Add Print all fields checkbox
 * 
 * AUTHOR:      Dave Zasada - Offshift Application Support
 *
 * DATE:        2-13-2003
 *
 **************************************************************************************
 */


%>

<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "UserInfoCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="userinfobean" scope="request" class="com.alltel.lsr.common.objects.UserInfoBean" />

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">User&nbsp;Information&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="UserInfoCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= userinfobean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<tr>
<td align=right>User&nbsp;Id:</td> 
<td align=left><%=sdm.getUser()%></td>
<input type="hidden" NAME="USERID" VALUE="<%=sdm.getUser()%>">
</tr>

<tr>
<td align=right><%= userinfobean.getEmlAddrssSPANStart() %>Email&nbsp;Address:<%= userinfobean.getEmlAddrssSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=128 NAME="EML_ADDRSS" VALUE="<%= userinfobean.getEmlAddrss() %>"></td>
</tr>

<tr>
<td align=right>Receive&nbsp;Email&nbsp;Notifications?&nbsp;:</td>
<%	if (userinfobean.getRcvEmlNtfctns().equals("Y"))
	{
%>
		<td align=left><input type="checkbox" NAME="RCV_EML_NTFCTNS" VALUE="yes" checked>
<%	}
	else
	{
%>
		<td align=left><input type="checkbox" NAME="RCV_EML_NTFCTNS" VALUE="yes">
<%	}
%>

	Print&nbsp;all&nbsp;Fields&nbsp;(including&nbsp;fields&nbsp;with&nbsp;no
	value)&nbsp;?&nbsp;:&nbsp;
<%	if (userinfobean.getPrintInd().equals("Y"))
	{
%>
		<input type="checkbox" NAME="PRINT_IND" VALUE="yes" checked>
<%	}
	else
	{
%>
		<input type="checkbox" NAME="PRINT_IND" VALUE="yes">
<%	}
%>
</td>
</tr>

<tr>
<td align=right><%= userinfobean.getPsswdRcvrQstnSPANStart() %>Password&nbsp;Recovery&nbsp;Question:<%= userinfobean.getPsswdRcvrQstnSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=128 NAME="PSSWD_RCVR_QSTN" VALUE="<%= userinfobean.getPsswdRcvrQstn() %>"></td>
</tr>

<tr>
<td align=right><%= userinfobean.getPsswdRcvrNswrSPANStart() %>Password&nbsp;Recovery&nbsp;Answer:<%= userinfobean.getPsswdRcvrNswrSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=128 NAME="PSSWD_RCVR_NSWR" VALUE="<%= userinfobean.getPsswdRcvrNswr() %>"></td>
</tr>

<input type="hidden" NAME="MDFD_DT" VALUE="<%= userinfobean.getMdfdDt() %>">

<tr>
<td align=center colspan=2>
<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
</td>
</tr>

</table>

</form>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
