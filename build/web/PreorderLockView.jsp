<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *				COPYRIGHT (C) 2002
 *						BY
 *				Windstream INFORMATION SERVICES
 */
/** 
 * MODULE:		PreorderLockView.jsp
 * 
 * DESCRIPTION: Displays a list of locked preorders and provides a link to unlock it.
 * 
 * AUTHOR:      Paul Sedlak
 * 
 * DATE:        02-01-2002
 * 
 * HISTORY:
 *	xx/xx/2002  initial check-in.
 *
 */
%>

<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 
<%
	final String SECURITY_OBJECT = "UNLOCK_PREORDERS";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		 alltelResponse.sendRedirect(SECURITY_URL);
	}
%>

<table align=center width="100%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">Preorder&nbsp;Locks</SPAN>
    </th>
  </tr>
</table>
<BR CLEAR=ALL>

<TABLE width="75%" align=center border=1 cellspacing=0 cellpadding=0>
<TH align=center>TXNUM</TH>
<TH align=center>Userid</TH>
<TH align=center>User&nbsp;Name</TH>
<TH align=center>OCN&nbsp;Code,&nbsp;State&nbsp;and&nbsp;Description</TH>
<TH align=center>Lock&nbsp;Date/Time</TH>
<TH align=center>UNLOCK</TH>

<%
	String strQuery = "SELECT L.SQNC_NMBR, P.PRE_ORDR_SQNC_NMBR, L.USERID,  " +
		" U.FRST_NM, U.LST_NM, P.OCN_CD, P.OCN_STT, O.OCN_NM, TO_CHAR(L.LCK_DT, 'MM/DD/YYYY @ HH24:MI:SS') " +
		" FROM LOCK_T l, USERID_T u, PREORDER_T P, ocn_t o " +
		" WHERE L.USERID = U.USERID AND L.SQNC_NMBR = P.PRE_ORDR_SQNC_NMBR AND L.TYP_IND = 'P'" +
		" AND P.OCN_CD = O.OCN_CD " +
		" ORDER BY P.PRE_ORDR_SQNC_NMBR ASC";
	Connection con = DatabaseManager.getConnection();
	Statement stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(strQuery);
	while (rs.next() == true)
	{
%>
		<TR>
			<TD align=left>&nbsp;<%= rs.getString(2) %></TD>
			<TD align=left>&nbsp;<%= rs.getString(3) %></TD>
			<TD align=left>&nbsp;<%= rs.getString(5) %>,&nbsp;<%= rs.getString(4) %></TD>
			<TD align=center><%=rs.getString(6)%>-<%=rs.getString(7)%>&nbsp;&nbsp;<%= rs.getString(8)%></TD>
			<TD align=center><%= rs.getString(9) %></TD>
			<TD align=center><A HREF="PreorderUnlockCtlr?pre=<%=rs.getString(1)%>">&nbsp;Unlock&nbsp;Preorder&nbsp;</A></TD>
			
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
