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
 * MODULE:	DslSearchView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
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

<FORM NAME="DslSearchView" METHOD=POST ACTION="DslListCtlr?dslsrch=advsrch">

<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">S&nbsp;e&nbsp;a&nbsp;r&nbsp;c&nbsp;h&nbsp;&nbsp;&nbsp;&nbsp;C&nbsp;r&nbsp;i&nbsp;t&nbsp;e&nbsp;r&nbsp;i&nbsp;a</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=right valign=top>Broadband&nbsp;Number:&nbsp;</TD>
	<TD align=left><input type=text maxLength=16 size=25 name=dsl_sqnc_nmbr><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>Broadband&nbsp;Status:&nbsp;</TD>
	<TD align=left>
	<input type="checkbox" name="dsl_status" value="">ALL
	<br>
	<%
	rsSrch = stmtSrch.executeQuery("SELECT STTS_CD FROM STATUS_T WHERE TYP_IND = 'D' ORDER BY STTS_CD ASC");

	while (rsSrch.next() == true)
	{
	%>
		<input type="checkbox" name="dsl_status" value=<%= rsSrch.getString("STTS_CD") %>><%= rsSrch.getString("STTS_CD") %>
		<br>
	<%
	}
	%>
	</SELECT><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>Request&nbsp;Type:</TD>
	<TD align=left><select MULTIPLE size=3 NAME="dsl_rqst_typ">
	<option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...ALL...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

	<%
	rsSrch = stmtSrch.executeQuery("SELECT VLD_FLD_VL FROM FIELD_VALUES_T WHERE FLD_VLS_SQNC_NMBR = 116 ORDER BY FLD_SRT_ORDR ASC");

	while (rsSrch.next() == true)
	{
	%>
		<option value=<%= rsSrch.getString("VLD_FLD_VL") %>><%= rsSrch.getString("VLD_FLD_VL") %>
	<%
	}
	%>
	</SELECT><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>ISP:</TD>
	<TD align=left><select MULTIPLE size=3 NAME="isp">
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
	<TD align=right>City:</TD>
	<TD align=left><input type=text maxLength=32 size=32 name=city></TD>
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
	<TD align=right>Service&nbsp;Order:</TD>
	<TD align=left><input type=text maxLength=15 size=25 name=so></TD>
  </TR>
  <TR>
	<TD align=right>User&nbsp;ID:</TD>
	<TD align=left><input type=text maxLength=15 size=25 name=userid></TD>
  </TR>
  <TR>
	<TD align=right>DSL&nbsp;Service&nbsp;Telno:</TD>
	<TD align=left><input type=text maxLength=12 size=25 name=dsltelno></TD>
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

