<%
/**
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                      COPYRIGHT (C) 2002
 *                              BY
 *                      Windstream COMMUNICATIONS INC.
 */
/**
 * MODULE:      HomePageView.jsp
 *
 * DESCRIPTION: JSP View used to maintain Home Page Notes
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        6-1-2002
 *
 * HISTORY:
 *
 */
/** $Log:    $
*/
/* $Revision:    $
*/
%>

<%@ include file="i_header.jsp" %> 
<%@ include file="ExpressUtil.jsp" %>
<%
	final String CONTROLLER = "HomePageCtlr";
	String arrNoticeTypes_values[] = { "-1", "0", "1", "2"};
	String arrNoticeTypes_choices[] = {"Select Notice Type", "Product and Service Notice", "Process Notice", "Express Enhancement Notice"};

	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
	
%>

<jsp:useBean id="homepagebean" scope="request" class="com.alltel.lsr.common.objects.HomePageBean" />

<table width="100%" align=left>
  <tr>
    <TH align=center bgcolor="#7AABDE" >
	<SPAN class="barheader">Home&nbsp;Page&nbsp;Table&nbsp;Maintenance</SPAN>
    </th>
  </tr>
</table>

<br clear=ALL>

<form name="HomePageView" action="HomePageCtlr" method="POST">
<table align=center border=0 width=500>
<tr><td colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%= homepagebean.getErrMsg() %>&nbsp;</SPAN></td></tr>
<tr><td colspan=2>&nbsp;</td></tr>


<tr>
<td align=right><%= homepagebean.getNoteStrtDtSPANStart() %>Note&nbsp;Start&nbsp;Date&nbsp;<i>(MM-DD-YYYY)</i>:<%= homepagebean.getNoteStrtDtSPANEnd() %></td> 
<td align=left><input type="TEXT" size=12 maxLength=10 NAME="NOTE_STRT_DT" VALUE="<%= homepagebean.getNoteStrtDt() %>"></td>
</tr>

<tr>
<td align=right><%= homepagebean.getNoteEndDtSPANStart() %>Note&nbsp;End&nbsp;Date&nbsp;<i>(MM-DD-YYYY)</i>:<%= homepagebean.getNoteEndDtSPANEnd() %></td> 
<td align=left><input type="TEXT" size=12 maxLength=10 NAME="NOTE_END_DT" VALUE="<%= homepagebean.getNoteEndDt() %>"></td>
</tr>

<tr>
<td align=right><%= homepagebean.getNoteTitleSPANStart() %>Note Title<%= homepagebean.getNoteTitleSPANEnd() %></td> 
<td align=left><TEXTAREA  maxLength=50 cols=50 rows=2 wrap NAME="NT_TITLE"><%= homepagebean.getNoteTitle() %> </TEXTAREA></td>
</tr>

<tr>
<td align=right valign=top><%= homepagebean.getNoteMsgSPANStart() %>Note&nbsp;Message:<br><i><font size=-3>(&nbsp;enter &lt;BR&gt; to <br> represent a <br>carriage return&nbsp;)</font></i><%= homepagebean.getNoteMsgSPANEnd() %></td> 
<td align=left>
<TEXTAREA NAME="NOTE_MSG" cols=50 rows=6 wrap onChange="maxCheck('HomePageView', 'Note Message', 'NOTE_MSG', <%=homepagebean.getColumnSize("NOTE_MSG")%>);">
<%= homepagebean.getNoteMsg() %>
</TEXTAREA>
</td>
</tr>

<tr>
	<td align=right><%= homepagebean.getNoteStatesSPANStart() %>State(s):<br>(<i>Hold down Control Key to select more than one state</i>):<%= homepagebean.getNoteStatesSPANEnd() %></td> 
	<td align=left><%= printSelectBoxStates("NT_STTS", homepagebean.getNoteStates(), 10 )%> </td>
</tr>
<tr>
	<td align=right><%= homepagebean.getNoteTypSPANStart() %>Select Note Type</i>:<%= homepagebean.getNoteTypSPANEnd() %></td> 
	<td align=left><%=printSelectBox("NT_TYP", 1, arrNoticeTypes_values, arrNoticeTypes_choices,  homepagebean.getNoteTyp()== null? "0" : homepagebean.getNoteTyp()  )%>
	<%= homepagebean.getNoteTypSPANEnd() %> </td>
</tr>

<tr>
<td align=right valign=top><%= homepagebean.getNoteMsgSPANStart() %>Display&nbsp;Note&nbsp;To:<%= homepagebean.getNoteMsgSPANEnd() %></td> 
<td align=left>
<%
// Get company types
String strCmpnyTypes = homepagebean.getCmpnyTypList();
if (strCmpnyTypes == null)
	strCmpnyTypes = "";

// Build a list of Company types to choose from
Connection con = DatabaseManager.getConnection();
Statement stmt = con.createStatement();

ResultSet rs = stmt.executeQuery("SELECT CMPNY_TYP, CMPNY_TYP_DSCRPTN FROM COMPANY_TYPE_T ORDER BY CMPNY_TYP_DSCRPTN");

while (rs.next() == true)
{
%>
	<input type="checkbox" name="cmpny_typ" value=<%= rs.getString("CMPNY_TYP") %> <%if (strCmpnyTypes.indexOf(rs.getString("CMPNY_TYP")) >= 0){%>checked<%}%>><%= rs.getString("CMPNY_TYP_DSCRPTN") %>&nbsp(<%=rs.getString("CMPNY_TYP")%>)
	<br>
<%
}
rs.close();
stmt.close();
DatabaseManager.releaseConnection(con);
%>
<br></td>
</tr>

<input type="hidden" NAME="NOTE_SQNC_NMBR" VALUE="<%= homepagebean.getNoteSqncNmbr() %>">
<input type="hidden" NAME="MDFD_USERID" VALUE="<%= homepagebean.getMdfdUserid() %>">
<input type="hidden" NAME="MDFD_DT" VALUE="<%= homepagebean.getMdfdDt() %>">

<%	if (homepagebean.getDbAction().equals("get") ||
	    homepagebean.getDbAction().equals("UpdateRow") ||
	    homepagebean.getDbAction().equals("DeleteRow"))
	{
%>
		<tr>
		<td align=right>Modified&nbsp;Date:</td> 
		<td align=left><%= homepagebean.getMdfdDt() %></td>
		</tr>

		<tr>
		<td align=right>Modified&nbsp;Userid:</td> 
		<td align=left><%= homepagebean.getMdfdUserid() %></td>
		</tr>
<%	}
%>

<tr>
<td colspan=2 align=center>

<%	if (homepagebean.getDbAction().equals("new") ||
	    homepagebean.getDbAction().equals("InsertRow"))
	{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="InsertRow">
		<INPUT class=appButton TYPE="RESET" name="action" value="Reset">
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="Cancel">
<%	}
	else if (homepagebean.getDbAction().equals("get") ||
		 homepagebean.getDbAction().equals("UpdateRow") ||
		 homepagebean.getDbAction().equals("DeleteRow"))
	{
		if (sdm.isAuthorized(homepagebean.getTblAdmnScrtyTgMod()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="UpdateRow">
<%
		}
		if (sdm.isAuthorized(homepagebean.getTblAdmnScrtyTgDel()) )
		{
%>
		<INPUT class=appButton TYPE="SUBMIT" name="action" value="DeleteRow">
<%
		}
		if (sdm.isAuthorized(homepagebean.getTblAdmnScrtyTgMod()) )
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

