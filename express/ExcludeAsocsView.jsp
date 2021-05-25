<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2002
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	ExcludeAsocs.jsp	
 * 
 * DESCRIPTION: JSP View used to maintain ASOCs to be excluded from preorders
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        6-1-2002
 * 
 * HISTORY:
 *	06/01/2002 dmartz	initial
 *	06/03/2002 psedlak	chgd to use inclusion
 *
 */

%>

<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "ExcludeAsocsCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="exasocbean" scope="request" class="com.alltel.lsr.common.objects.ExcludeAsocsBean" />

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">Included&nbsp;ASOCs&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form action="ExcludeAsocsCtlr" method="POST">

<table align=center border=0 width=500>

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= exasocbean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<tr>
<td align=right><%= exasocbean.getExcldCtgrySPANStart() %>Included&nbsp;Category&nbsp;<%= exasocbean.getExcldCtgrySPANEnd() %></td> 
<td align=left><input type="TEXT" size=10 maxLength=8 NAME="EXCLD_CTGRY" VALUE="<%= exasocbean.getExcldCtgry() %>"></td>
</tr>

<tr>
<td align=right><%= exasocbean.getNpaSPANStart() %>NPA&nbsp;<%= exasocbean.getNpaSPANEnd() %></td> 
<td align=left><input type="TEXT" size=4 maxLength=3 NAME="NPA" VALUE="<%= exasocbean.getNpa() %>"></td>
</tr>

<tr>
<td align=right><%= exasocbean.getNxxSPANStart() %>NXX&nbsp;<%= exasocbean.getNxxSPANEnd() %></td> 
<td align=left><input type="TEXT" size=4 maxLength=3 NAME="NXX" VALUE="<%= exasocbean.getNxx() %>"></td>
</tr>

<tr>
<td align=right><%= exasocbean.getAsocCodeSPANStart() %>ASOC&nbsp;Code&nbsp;<%= exasocbean.getAsocCodeSPANEnd() %></td> 
<td align=left><input type="TEXT" size=6 maxLength=5 NAME="ASOC_CODE" VALUE="<%= exasocbean.getAsocCode() %>"></td>
</tr>

</td>
</tr>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= exasocbean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= exasocbean.getMdfdDt() %>">
<input type="hidden" NAME="SQNC_NMBR" VALUE="<%= exasocbean.getSqncNmbr() %>">

<%      if (exasocbean.getDbAction().equals("get") ||
	    exasocbean.getDbAction().equals("UpdateRow") ||
	    exasocbean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td>
		<td align=left><%= exasocbean.getMdfdDt() %></td>
		</tr>

		<tr>
		<td align=right>Modified&nbsp;Userid:</td>
		<td align=left><%= exasocbean.getMdfdUserid() %></td>
		</tr>
<%      }
%>

<tr>
<td align=center colspan=2>
<%      if (exasocbean.getDbAction().equals("new") ||
	    exasocbean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%      }
	else if (exasocbean.getDbAction().equals("get") ||
	         exasocbean.getDbAction().equals("UpdateRow") ||
	         exasocbean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(exasocbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(exasocbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(exasocbean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Reset">
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
