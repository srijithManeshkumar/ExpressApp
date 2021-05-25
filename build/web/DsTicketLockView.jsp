<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2004
 *						BY
 *				Windstream Communications Inc
 */
/** 
 * MODULE:	DsTicketLockView.jsp
 * 
 * DESCRIPTION: Displays a list of locked tickets and provides a link to unlock it.
 * 
 * AUTHOR:      Paul Sedlak
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	03/20/2004 pjs init
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 
<%
	final String SECURITY_OBJECT = "UNLOCK_DSTICKETS";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		 alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<table align=center width="100%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">Ticket&nbsp;Locks</SPAN>
    </th>
  </tr>
</table>
<BR CLEAR=ALL>

<TABLE width="75%" align=center border=1 cellspacing=0 cellpadding=0>
<TH align=center>Ticket&nbsp;Number</TH>
<TH align=center>Userid</TH>
<TH align=center>User&nbsp;Name</TH>
<TH align=center>OCN&nbsp;Code,&nbsp;State&nbsp;and&nbsp;Description</TH>
<TH align=center>Lock&nbsp;Date/Time</TH>
<TH align=center>UNLOCK</TH>

<%
	String strQuery = "SELECT TL.SQNC_NMBR, TL.USERID,  " +
		" U.FRST_NM, U.LST_NM, T.OCN_CD, T.OCN_STT, O.OCN_NM, TO_CHAR(TL.LCK_DT, 'MM/DD/YYYY @ HH24:MI:SS') " +
		" FROM LOCK_T TL, USERID_T u, DSTICKET_T T, ocn_t o " +
		" WHERE TL.USERID = U.USERID AND TL.SQNC_NMBR = T.TCKT_SQNC_NMBR AND TL.TYP_IND = 'S'" +
		" AND T.OCN_CD = O.OCN_CD " +
		" ORDER BY T.TCKT_SQNC_NMBR ASC";
	Connection con = DatabaseManager.getConnection();
	Statement stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(strQuery);
	while (rs.next() == true)
	{
%>
		<TR>
			<TD align=left>&nbsp;<%= rs.getString(1) %></TD>
			<TD align=left>&nbsp;<%= rs.getString(2) %></TD>
			<TD align=left>&nbsp;<%= rs.getString(4) %>,&nbsp;<%= rs.getString(3) %></TD>
			<TD align=center><%=rs.getString(5)%>-<%=rs.getString(6)%>&nbsp;&nbsp;<%= rs.getString(7)%></TD>
			<TD align=center><%= rs.getString(8) %></TD>
			<TD align=center><A HREF="DsTicketUnlockCtlr?tckt=<%=rs.getString(1)%>">&nbsp;Unlock&nbsp;Ticket&nbsp;</A></TD>
			
		</TR>
<%
	} //while
	DatabaseManager.releaseConnection(con);
%>
</TABLE>
<BR CLEAR=ALL><BR>
<BR>

</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
