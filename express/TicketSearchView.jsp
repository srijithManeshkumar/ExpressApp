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
 * MODULE:	TicketSearchView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/20/2002 Initial Check-in
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%
Connection conSrch = null;
Statement stmtSrch = null;
ResultSet rsSrch = null;

conSrch = DatabaseManager.getConnection();
stmtSrch = conSrch.createStatement();
%>

<FORM NAME="TicketSearchView" METHOD=POST ACTION="TicketListCtlr?tcktsrch=advsrch">

<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">S&nbsp;E&nbsp;A&nbsp;R&nbsp;C&nbsp;H&nbsp;&nbsp;&nbsp;&nbsp;T&nbsp;I&nbsp;C&nbsp;K&nbsp;E&nbsp;T&nbsp;S</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=right valign=top>Ticket&nbsp;Number:&nbsp;</TD>
	<TD align=left><input type=text maxLength=16 size=25 name=ttn><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>Telephone&nbsp;Number:</TD>
	<TD align=left><input type=text maxLength=17 size=17 name=teleno><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>Ticket Status:&nbsp;</TD>
	<TD align=left>
	<input type="checkbox" name="ticket_status" value="">ALL
	<br>
	<%
	rsSrch = stmtSrch.executeQuery("SELECT STTS_CD FROM STATUS_T WHERE TYP_IND = 'T' ORDER BY STTS_CD ASC");

	while (rsSrch.next() == true)
	{
	%>
		<input type="checkbox" name="ticket_status" value=<%= rsSrch.getString("STTS_CD") %>><%= rsSrch.getString("STTS_CD") %>
		<br>
	<%
	}
	%>
	</SELECT><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>Company:</TD>
	<TD align=left><select MULTIPLE size=3 NAME="company">
	<option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...ALL...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

	<%
	rsSrch = stmtSrch.executeQuery("SELECT CMPNY_TYP FROM COMPANY_T C, USERID_T U WHERE " +
		"C.CMPNY_SQNC_NMBR = U.CMPNY_SQNC_NMBR AND USERID = '" + sdm.getUser() + "'");

	if (rsSrch.next())
	{
		if (rsSrch.getString("CMPNY_TYP").equals("P"))
		{
			rsSrch = stmtSrch.executeQuery("SELECT CMPNY_SQNC_NMBR, CMPNY_NM FROM COMPANY_T ORDER BY CMPNY_NM");
		}
		else
		{
			rsSrch = stmtSrch.executeQuery("SELECT C.CMPNY_SQNC_NMBR CMPNY_SQNC_NMBR, CMPNY_NM " +
				"FROM COMPANY_T C, USERID_T U WHERE  C.CMPNY_SQNC_NMBR = U.CMPNY_SQNC_NMBR " +
				"AND USERID = '" + sdm.getUser() + "' ORDER BY CMPNY_NM ASC");
		}
	}

	while (rsSrch.next() == true)
	{
	%>
		<option value=<%= rsSrch.getString("CMPNY_SQNC_NMBR") %>><%= rsSrch.getString("CMPNY_NM") %>
	<%
	}
	%>
	</SELECT><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>OCN:</TD>
	<TD align=left><select MULTIPLE size=3 NAME="ocn">
	<option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...ALL...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

	<%
	rsSrch = stmtSrch.executeQuery("SELECT CMPNY_TYP FROM COMPANY_T C, USERID_T U WHERE " +
		"C.CMPNY_SQNC_NMBR = U.CMPNY_SQNC_NMBR AND USERID = '" + sdm.getUser() + "'");

	if (rsSrch.next())
	{
		if (rsSrch.getString("CMPNY_TYP").equals("P"))
		{
			rsSrch = stmtSrch.executeQuery("SELECT OCN_CD FROM OCN_T ORDER BY OCN_CD");
			while (rsSrch.next() == true)
			{
				%>
				<option value=<%= rsSrch.getString("OCN_CD") %>><%= rsSrch.getString("OCN_CD") %>
				<%
			}
		}
		else
		{

			// Get All OCN Codes for this users user groups
			String strQuery = "SELECT OCN_CD, CMPNY_SQNC_NMBR FROM USER_GROUP_T WHERE USR_GRP_CD IN " +
				"(SELECT DISTINCT USR_GRP_CD FROM USER_GROUP_ASSIGNMENT_T WHERE USERID = '" + sdm.getUser() + "')";
			rsSrch = stmtSrch.executeQuery(strQuery);

			String strInClause = "";
			while (rsSrch.next() == true)
			{
				if (rsSrch.getString("OCN_CD").equals("*"))
				{
					String strSubQuery = "SELECT OCN_CD FROM OCN_T WHERE CMPNY_SQNC_NMBR = " + 
						rsSrch.getInt("CMPNY_SQNC_NMBR") + " ORDER BY OCN_CD";
					Statement substmt = null;
					substmt = conSrch.createStatement();
					ResultSet subrs = substmt.executeQuery(strSubQuery);
					while (subrs.next() == true)
					{
						%>
						<option value=<%= subrs.getString("OCN_CD") %>><%= subrs.getString("OCN_CD") %>
						<%
					}
				}
				else
				{
					%>	
					<option value=<%= rsSrch.getString("OCN_CD") %>><%= rsSrch.getString("OCN_CD") %>
					<%
				}
			}
		}
	}
	%>
        </SELECT><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>State:</TD>
	<TD align=left><select MULTIPLE size=3 NAME="state">
	<option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...ALL...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

	<%
	rsSrch = stmtSrch.executeQuery("SELECT STT_CD, STT_NM FROM STATE_T ORDER BY STT_NM ASC");

	while (rsSrch.next() == true)
	{
	%>
		<option value=<%= rsSrch.getString("STT_CD") %>><%= rsSrch.getString("STT_NM") %>
	<%
	}
	DatabaseManager.releaseConnection(conSrch);
	%>
	</SELECT><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right>User&nbsp;ID:</TD>
	<TD align=left><input type=text maxLength=15 size=25 name=userid></TD>
  </TR>
  <TR>
	<TD align=right>From&nbsp;Last&nbsp;Mod&nbsp;Date&nbsp;<i>(MM/DD/YYYY)</i>&nbsp;:</TD>
	<TD align=left>
	<input type="TEXT" size=3 maxLength=2 NAME="from_lst_mdfd_mnth" VALUE="">
	/
	<input type="TEXT" size=3 maxLength=2 NAME="from_lst_mdfd_dy" VALUE="">
	/
	<input type="TEXT" size=5 maxLength=4 NAME="from_lst_mdfd_yr" VALUE="">
	&nbsp;&nbsp;&nbsp;To&nbsp;Last&nbsp;Mod&nbsp;Date&nbsp;<i>(MM/DD/YYYY)</i>&nbsp;:
	<input type="TEXT" size=3 maxLength=2 NAME="to_lst_mdfd_mnth" VALUE="">
	/
	<input type="TEXT" size=3 maxLength=2 NAME="to_lst_mdfd_dy" VALUE="">
	/
	<input type="TEXT" size=5 maxLength=4 NAME="to_lst_mdfd_yr" VALUE="">
	</TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=center colspan=2><INPUT class=appButton TYPE="RESET" VALUE="Reset">&nbsp;
	&nbsp;<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit">&nbsp;
	&nbsp;<INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Cancel"></TD>
  </TR>
 
</TABLE>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

