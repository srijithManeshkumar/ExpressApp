<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	DwoSearchView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 * 	pjs 5-13-2005 added Order Type, fixed Site list
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%

HttpSession objSession = alltelRequest.getSession();
String strPJVN = (String)objSession.getAttribute("DwOcHoIcE");
Log.write(Log.DEBUG_VERBOSE, "DwoSearchView - strPJVN: " + strPJVN);
String m_strTypInd = "";
if (strPJVN == null) {  strPJVN=""; }
if ( strPJVN.equals("Bdp") )
{
        m_strTypInd = "X";
}
else
        m_strTypInd = "W";

Connection conSrch = null;
Statement stmtSrch = null;
ResultSet rsSrch = null;

try {

conSrch = DatabaseManager.getConnection();
stmtSrch = conSrch.createStatement();
%>

<FORM NAME="DwoSearchView" METHOD=POST ACTION="DwoListCtlr?dwosrch=advsrch">

<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD colspan=2>&nbsp;</TD></TR>

<% if (m_strTypInd.equals("X")) // BDP
{ %>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">S&nbsp;E&nbsp;A&nbsp;R&nbsp;C&nbsp;H&nbsp;&nbsp;&nbsp;&nbsp;B&nbsp;U&nbsp;S&nbsp;I&nbsp;N&nbsp;E&nbsp;S&nbsp;S&nbsp;&nbsp;&nbsp;D&nbsp;A&nbsp;T&nbsp;A&nbsp;&nbsp;&nbsp;P&nbsp;R&nbsp;O&nbsp;D&nbsp;U&nbsp;C&nbsp;T&nbsp;S</SPAN></TD></TR>
<% } else { %>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">S&nbsp;E&nbsp;A&nbsp;R&nbsp;C&nbsp;H&nbsp;&nbsp;&nbsp;&nbsp;K&nbsp;P&nbsp;E&nbsp;N&nbsp;&nbsp;&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R&nbsp;S</SPAN></TD></TR>
<% } %>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=right valign=top>Order&nbsp;Number:&nbsp;</TD>
	<TD align=left><input type=text maxLength=16 size=25 name=dwo_nbr><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>Order&nbsp;Status:&nbsp;</TD>
	<TD align=left>
	<input type="checkbox" name="dwo_status" value="">ALL
	<br>
	<%
	if (m_strTypInd.equals("X")) // BDP
	{
		rsSrch = stmtSrch.executeQuery("SELECT STTS_CD FROM STATUS_T WHERE TYP_IND = 'X' ORDER BY STTS_CD ASC");
	}
	else // KPEN
	{
		rsSrch = stmtSrch.executeQuery("SELECT STTS_CD FROM STATUS_T WHERE TYP_IND = 'W' ORDER BY STTS_CD ASC");
	}

	while (rsSrch.next() == true)
	{
	%>
		<input type="checkbox" name="dwo_status" value="<%= rsSrch.getString("STTS_CD") %>"><%= rsSrch.getString("STTS_CD") %>
		<br>
	<%
	}
	rsSrch.close();
	%>
	</SELECT><BR><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>Order&nbsp;Type:&nbsp;</TD>
	<TD align=left>
	<input type="checkbox" name="dwo_ordertype" value="">ALL
	<br>
	<%
	if (m_strTypInd.equals("X")) // BDP
	{
		rsSrch = stmtSrch.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T WHERE TYP_IND = 'X' ORDER BY SRVC_TYP_CD ASC");
	}
	else   // KPEN
	{
		rsSrch = stmtSrch.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T WHERE TYP_IND = 'W' ORDER BY SRVC_TYP_CD ASC");
	}

	String strTmp="";
	while (rsSrch.next() == true)
	{	strTmp = rsSrch.getString("SRVC_TYP_CD");
		if (  strTmp.equals("A") || strTmp.equals("C") || strTmp.equals("E") )
		{} else {
	%>
		<input type="checkbox" name="dwo_ordertype" value="<%= rsSrch.getString("SRVC_TYP_CD") %>"><%= rsSrch.getString("SRVC_TYP_DSCRPTN") %>
		<br>
	<%	}
	}
	rsSrch.close();
	%>
	</SELECT><BR></TD>
  </TR>
  <TR>
	<TD align=right valign=top>Product&nbsp;Type:&nbsp;</TD>
	<TD align=left>
	<input type="checkbox" name="dwo_producttype" value="">ALL
	<br>
	<%
	if (m_strTypInd.equals("X")) // BDP
	{
		rsSrch = stmtSrch.executeQuery("SELECT PRDCT_TYP_CD, PRDCT_DSCRPTN FROM PRODUCT_T WHERE TYP_IND = 'X' ORDER BY PRDCT_TYP_CD ASC");
	}
	else   // KPEN
	{
		rsSrch = stmtSrch.executeQuery("SELECT PRDCT_TYP_CD, PRDCT_DSCRPTN FROM PRODUCT_T WHERE TYP_IND = 'W' ORDER BY PRDCT_TYP_CD ASC");
	}

	while (rsSrch.next() == true)
	{	
	%>
		<input type="checkbox" name="dwo_producttype" value="<%= rsSrch.getString("PRDCT_TYP_CD") %>"><%= rsSrch.getString("PRDCT_DSCRPTN") %>
		<br>
	<%	
	}
	rsSrch.close();
	%>
	</SELECT><BR></TD>
  </TR>
  <TR>
        <TD align=right valign=top>State:<br><span class=smallnote><i>(Multiple selections<br>can be made by holding<br>down the 'Ctrl' key)</i></span></TD>
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
        %>
        </SELECT><BR><BR></TD>
  </TR>

<%
if (m_strTypInd.equals("W")) // KPEN
{
%>
  <TR>
	<TD align=right valign=top>Site:</TD>
	<TD align=left><select MULTIPLE size=3 NAME="ocn">
	<option value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp&nbsp&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...ALL...&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

	<%
	if (sdm.getLoginProfileBean().getUserBean().getCmpnyTyp().equals("P"))
	{
		if (m_strTypInd.equals("X"))  // BDP
		{
			rsSrch = stmtSrch.executeQuery("SELECT O.OCN_CD, O.OCN_NM FROM OCN_T O, COMPANY_T C " +
				" WHERE O.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP='X' ORDER BY OCN_NM");
		}
		else   // KPEN
		{
			rsSrch = stmtSrch.executeQuery("SELECT O.OCN_CD, O.OCN_NM FROM OCN_T O, COMPANY_T C " +
				" WHERE O.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP='W' ORDER BY OCN_NM");
		}

		while (rsSrch.next() == true)
		{
			%>
			<option value=<%= rsSrch.getString("OCN_CD") %>><%= rsSrch.getString("OCN_CD") %> - <%= rsSrch.getString("OCN_NM") %>
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
		{	String strSubQuery ="";
			if (rsSrch.getString("OCN_CD").equals("*"))
			{
				strSubQuery = "SELECT OCN_CD, OCN_NM FROM OCN_T WHERE CMPNY_SQNC_NMBR = " + 
					rsSrch.getInt("CMPNY_SQNC_NMBR") + " ORDER BY OCN_NM ";
			}
			else
			{
				strSubQuery = "SELECT OCN_CD, OCN_NM FROM OCN_T WHERE OCN_CD = '" + rsSrch.getString("OCN_CD") + "' ";
			}
			Statement substmt = conSrch.createStatement();
			ResultSet subrs = substmt.executeQuery(strSubQuery);
			while (subrs.next() == true)
			{
				%>
				<option value=<%= subrs.getString("OCN_CD") %>><%= subrs.getString("OCN_CD") %> - <%= subrs.getString("OCN_NM") %>
				<%
			}
			subrs.close();
		}
	}
	%>
        </SELECT><BR><BR></TD>
  </TR>
<% 
}
%>

<%
if (m_strTypInd.equals("X")) // BDP
{
%>
  <TR>
	<TD align=right>Location&nbsp;Name:</TD>
	<TD align=left><input type=text maxLength=60 size=25 name=locname></TD>
  </TR>
  <tr><td>&nbsp;</td></tr>
  <TR>
	<TD align=right>Main&nbsp;Business&nbsp;Name:</TD>
	<TD align=left><input type=text maxLength=60 size=25 name=businame></TD>
  </TR>
<% 
}
%>

  <tr><td>&nbsp;</td></tr>
  <TR>
	<TD align=right>User&nbsp;ID:</TD>
	<TD align=left><input type=text maxLength=25 size=25 name=userid></TD>
  </TR>
  <tr><td>&nbsp;</td></tr>
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
<%

} //try
catch (Exception e) {
    
        Log.write("Exception in DwoSearchView.jsp. Error Message :"+e.getMessage());
    
        rsSrch.close();
        rsSrch=null;
        stmtSrch.close();
        stmtSrch = null;
}
finally {
    
        Log.write("Inside finally block in DwoSearchView.jsp ! Releasing connection object :"+conSrch.toString());
    
        DatabaseManager.releaseConnection(conSrch);
        
        Log.write("Inside finally block in DwoSearchView.jsp ! Released connection object.");
}
%>

</TABLE>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
