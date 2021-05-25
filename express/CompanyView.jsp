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
 * MODULE:	CompanyView.jsp	
 * 
 * DESCRIPTION: JSP View used to maintain the COMPANY_T table
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *	01/01/2002 dmartz	initial
 *	05/29/2002 psedlak	Added cols to Company_t
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%
	final String CONTROLLER = "CompanyCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<jsp:useBean id="cmpnybean" scope="request" class="com.alltel.lsr.common.objects.CompanyBean" />

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">Company&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<FORM action="CompanyCtlr" method="POST">

<table align=center border=0>
<input type="hidden" size=17 maxLength=15 NAME="CMPNY_SQNC_NMBR" VALUE="<%= cmpnybean.getCmpnySqncNmbr() %>">

<tr><td colspan=2>&nbsp;</td></tr>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= cmpnybean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>

<tr>
<td align=right><%= cmpnybean.getCmpnyTypSPANStart() %>Company&nbsp;Type:<%= cmpnybean.getCmpnyTypSPANEnd() %></td> 
<td align=left><select NAME="CMPNY_TYP">
<%	// Get possible company types
        Connection con = DatabaseManager.getConnection();
        Statement stmt = con.createStatement();

        // Get the Company Type
        ResultSet rs = stmt.executeQuery("SELECT CMPNY_TYP, CMPNY_TYP_DSCRPTN FROM COMPANY_TYPE_T ORDER BY CMPNY_TYP");
        while (rs.next() == true)
        {
%>
		<option value="<%=rs.getString("CMPNY_TYP")%>" <% if (cmpnybean.getCmpnyTyp().equals(rs.getString("CMPNY_TYP"))){%> SELECTED <%}%>><%=rs.getString("CMPNY_TYP")%> - <%=rs.getString("CMPNY_TYP_DSCRPTN")%>
<%
        } //while
	rs.close();
	stmt.close();
        DatabaseManager.releaseConnection(con);
%>
</select>
</td>
</tr>

<tr>
<td align=right><%= cmpnybean.getCmpnyNmSPANStart() %>Company&nbsp;Name:<%= cmpnybean.getCmpnyNmSPANEnd() %></td> 
<td align=left><input type="TEXT" size=52 maxLength=50 NAME="CMPNY_NM" VALUE="<%= cmpnybean.getCmpnyNm() %>"></td>
</tr>

<tr>
<td align=right><%= cmpnybean.getTargusUseridSPANStart() %>Targus&nbsp;Userid:<%= cmpnybean.getTargusUseridSPANEnd() %></td> 
<td align=left><input type="TEXT" size=15 maxLength=15 NAME="TARGUS_USERID" VALUE="<%= cmpnybean.getTargusUserid() %>"></td>
</tr>

<tr>
<td align=right><%= cmpnybean.getTargusPsswrdSPANStart() %>Targus&nbsp;Password:<%= cmpnybean.getTargusPsswrdSPANEnd() %></td> 
<td align=left><input type="TEXT" size=15 maxLength=15 NAME="TARGUS_PSSWRD" VALUE="<%= cmpnybean.getTargusPsswrd() %>"></td>
</tr>

<input type="hidden" NAME="MDFD_USERID" VALUE="<%= cmpnybean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= cmpnybean.getMdfdDt() %>">

<%	if (cmpnybean.getDbAction().equals("get") ||
	    cmpnybean.getDbAction().equals("UpdateRow") ||
	    cmpnybean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= cmpnybean.getMdfdDt() %></td>
		</tr>
		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= cmpnybean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td colspan=2 align=center>

<%	if (cmpnybean.getDbAction().equals("new") ||
	    cmpnybean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%	}
	else if (cmpnybean.getDbAction().equals("get") ||
		 cmpnybean.getDbAction().equals("UpdateRow") ||
		 cmpnybean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(cmpnybean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(cmpnybean.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(cmpnybean.getTblAdmnScrtyTgMod()) )
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

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/CompanyView.jsv  $
/*
/*   Rev 1.3   May 30 2002 07:48:36   sedlak
/*Add targus userid/psswrd to Company table.
/*
/*   Rev 1.1   31 Jan 2002 14:17:04   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:34   wwoods
/*Initial Checkin
*/

/* $Revision:   1.3  $
*/
%>
