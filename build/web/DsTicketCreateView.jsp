<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2004
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	DsTicketCreateView.jsp
 * 
 * DESCRIPTION: JSP View used to create new Tickets
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        3-20-2004
 * 
 * HISTORY:
 *	11/20/2004 psedlak init
 *
 */
%>

<%@ include file="i_header.jsp" %>
<%@ page import ="java.util.*" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>
<%

//Does user belong here?
final String EXPRESS_FUNCTION = "CREATE_DSTICKETS";
if (!sdm.isAuthorized(EXPRESS_FUNCTION))
{
	Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + EXPRESS_FUNCTION);
	alltelResponse.sendRedirect(SECURITY_URL);
}

Connection conTC = null;
Statement stmtTC = null;
ResultSet rsTC = null;

try {
conTC = DatabaseManager.getConnection();
stmtTC = conTC.createStatement();


String m_strTcktNewErrorMsg = (String) request.getAttribute("tcktnew_errormsg");
if ((m_strTcktNewErrorMsg == null) || (m_strTcktNewErrorMsg.length() == 0))
{
	m_strTcktNewErrorMsg = "";
}

String m_strOCNCd= request.getParameter("tcktnew_ocncd");
if ((m_strOCNCd == null) || (m_strOCNCd.length() == 0))
{
	m_strOCNCd = "";
}

%>

<FORM NAME="DsTicketCreateView" METHOD=POST ACTION="DsTicketCtlr">

<TABLE width=30% align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">N&nbsp;E&nbsp;W&nbsp;&nbsp;&nbsp;&nbsp;T&nbsp;I&nbsp;C&nbsp;K&nbsp;E&nbsp;T</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2 align=center>&nbsp;<SPAN class="errormsg"><%=m_strTcktNewErrorMsg%>&nbsp;</SPAN></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=right>Site:&nbsp;</TD>
	<TD align=left><SELECT NAME="tcktnew_ocncd">
		<option value="">... Select a Site...</OPTION>
	<%

	// Get All OCN Codes for this users user groups
	String strQuery = "SELECT OCN_CD, CMPNY_SQNC_NMBR FROM USER_GROUP_T WHERE USR_GRP_CD IN " +
		"(SELECT DISTINCT USR_GRP_CD FROM USER_GROUP_ASSIGNMENT_T WHERE USERID = '" + sdm.getUser() + "')";
Log.write(Log.DEBUG_VERBOSE, "DsTicketCreateView() strQuery=["+strQuery+"]");
	rsTC = stmtTC.executeQuery(strQuery);

	String strInClause = "";
	boolean bPickFirst=false;
	while (rsTC.next() == true)
	{
		if (rsTC.getString("OCN_CD").equals("*"))
		{
			String strSubQuery = "SELECT OCN_CD FROM OCN_T WHERE CMPNY_SQNC_NMBR = " + rsTC.getInt("CMPNY_SQNC_NMBR");
			Statement stmtTC2 = null;
			stmtTC2 = conTC.createStatement();
			ResultSet rsTC2 = stmtTC2.executeQuery(strSubQuery);
			while (rsTC2.next() == true)
			{
				strInClause = strInClause + "'" + rsTC2.getString("OCN_CD") + "',";
			}
			rsTC2.close();
		}
		else
		{
			strInClause = strInClause + "'" + rsTC.getString("OCN_CD") + "',";
		}
	}
	rsTC.close();
Log.write(Log.DEBUG_VERBOSE, sdm.getUser() + " inClause =["+strInClause+"]");

	// strip off last comma
	if (strInClause.endsWith(","))  
		strInClause = strInClause.substring(0,strInClause.length()-1);

	if (strInClause.length() > 0)
	{
		if (strInClause.length() < 8)
			bPickFirst=true;
		strQuery = "SELECT OCN_CD, OCN_NM FROM OCN_T WHERE OCN_CD IN (" + strInClause + ") AND CMPNY_SQNC_NMBR = " +
			sdm.getLoginProfileBean().getUserBean().getCmpnySqncNmbr()+ " ORDER BY OCN_CD ";
Log.write(Log.DEBUG_VERBOSE, sdm.getUser() + " strQuery =["+strQuery+"]");
		
		int i=0;
		rsTC = stmtTC.executeQuery(strQuery);
		while (rsTC.next() == true)
		{
			i++;
	%>
			<option value=<%= rsTC.getString("OCN_CD") %> <% if (bPickFirst && i==1) { %> SELECTED <% } %> ><%= rsTC.getString("OCN_CD") %> - <%= rsTC.getString("OCN_NM") %></OPTION>
	<%
		}
		rsTC.close();
	}

	%>
	</SELECT>
	</TD>
  </TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD align=center colspan=2>
	<INPUT class=appButton TYPE="RESET" VALUE="Reset">&nbsp;&nbsp;
	<INPUT class=appButton TYPE="SUBMIT" NAME="tcktnew" VALUE="Submit">&nbsp;&nbsp;
	<INPUT class=appButton TYPE="SUBMIT" NAME="tcktnew" VALUE="Cancel">
	</TD>
  </TR>
 
</TABLE>

</FORM>


<%
}//try 
catch(Exception e)
{	Log.write(Log.WARNING, sdm.getUser() + " Caught exception in DsTicketCreateView.jsp");
}
finally {
	DatabaseManager.releaseConnection(conTC);
}

%>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
