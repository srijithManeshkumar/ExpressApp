<%
/*
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                              COPYRIGHT (C) 2004
 *                                      BY
 *                              Alltel Communications Inc.
 */
/*
 * MODULE:      ExpressHome.jsp
 *
 * DESCRIPTION: Home page
 *
 * AUTHOR:      Express devel team
 *
 * DATE:        01/01/2002
 *
 * HISTORY:
 *      pjs     4/16/2004 chgd to yank cmp type from session, not db
 *      pjs     12/28/2004 added link to archived msgs page
 * 		EK     05/20/2006 added windstream Rebranding
 * Steve Korchnak   04/21/2007 - HD0000002325972 Modified 'order by' in select statment 
 *                  to descending order
 */
%>

<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<table align=center width="100%" cellspacing=0 cellpadding=0>
  <tr> 
    <TH width="70%">
    <SPAN CLASS="header1">Express&nbsp;Home</SPAN>  </th>
  </tr>
</table>
<BR CLEAR=ALL>


<%
	// Get the userid 
	String strUserid = sdm.getUser();

	// Get a Connection
	Connection con = DatabaseManager.getConnection();
	Statement stmt = con.createStatement();

	// Get the Company Type from session objects
	String strCmpnyTyp = sdm.getLoginProfileBean().getUserBean().getCmpnyTyp();
	//Log.write(Log.DEBUG_VERBOSE,"######################################################         ExpressHome typ="+strCmpnyTyp);

	// Get the notes for this Company Type
	ResultSet rs = stmt.executeQuery("SELECT NOTE_TITLE, NOTE_MSG FROM HOME_PAGE_NOTES_T WHERE SYSDATE BETWEEN NOTE_STRT_DT AND NOTE_END_DT AND CMPNY_TYP_LIST LIKE '%" + strCmpnyTyp + "%' ORDER BY NOTE_SQNC_NMBR DESC");
	while (rs.next() == true)
	{
%>
	<TABLE width=75% align=center border=1 cellspacing=0 cellpadding=0>
		<TR><TD align=center>
		<span class=notetitle><%= rs.getString( "NOTE_TITLE" ) %></span><BR> <BR><%= rs.getString("NOTE_MSG") %><BR></TD></TR>
	</TABLE>
	<BR CLEAR=ALL>
<%
	} //while
	DatabaseManager.releaseConnection(con);
%>

<BR>
<table align=center width="100%">
<tr>
<td align=center><a href="http://www.windstream.com">Visit&nbsp;Windstream's&nbsp;HomePage!</a></td>
<tr>
</table>

</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>

<%
/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/ExpressHome.jsv  $
/*
/*   Rev 1.4   Jun 04 2004 09:02:58   e0069884
/* 
/*
/*   Rev 1.1   31 Jan 2002 14:36:32   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:36   wwoods
/*Initial Checkin
*/

/* $Revision:   1.4  $
*/
%>

