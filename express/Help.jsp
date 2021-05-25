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
 * MODULE:	Help.jsp	
 * 
 * DESCRIPTION: Main help page/links for Express.
 * 
 * AUTHOR:      psedlak
 * 
 * DATE:        01-01-2002
 * 
 * HISTORY:
 *	02/08/2002	psedlak	Modifed for rel 1.1 - Include link to Status workflow page.
 *	06/16/2004	psedlak	Driven by company type (like homepage notes)
 *
 */
%>
<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/Help.jsv  $
/*
/*   Rev 1.2   13 Feb 2002 14:38:38   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 14:35:30   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:46   wwoods
/*Initial Checkin
*/

/* $Revision:   1.2  $
*/
%>

<%@ include file="i_header.jsp" %>
<SCRIPT LANGUAGE = "JavaScript">
<!--hide
function OpenPageNew( dUrl) 
{ 
window.open( dUrl, 'Test','width=700,height=450,resizable=yes'); 
//--> 
} 
</script>
<BR>
<TABLE width="50%" align=center cellspacing=0 cellpadding=0 border=0>
	<TR>
		<TD align=center width="50%"><SPAN CLASS="header1">H&nbsp;E&nbsp;L&nbsp;P&nbsp;&nbsp;&nbsp;T&nbsp;O&nbsp;P&nbsp;I&nbsp;C&nbsp;S&nbsp;</SPAN></TD>
</TABLE>
<BR CLEAR=ALL>
<BR>
<BR>
<TABLE align=center cellspacing=0 cellpadding=0 border=0>
<%
	Connection con = null;
	// Get the userid
        String strUserid = sdm.getUser();
        // Get a Connection
	try {
		con = DatabaseManager.getConnection();
		Statement stmt = con.createStatement();

		// Get the Company Type from session objects
		String strCmpnyTyp = sdm.getLoginProfileBean().getUserBean().getCmpnyTyp();

		// Get the notes for this Company Type
		ResultSet rs = stmt.executeQuery("SELECT MSG FROM HELP_T WHERE SYSDATE BETWEEN STRT_DT AND END_DT AND CMPNY_TYP_LIST LIKE '%"
			+ strCmpnyTyp + "%' ORDER BY SQNC_NMBR");
		while (rs.next() == true)
		{
%>
			
			<TR><TD align=left><img src="images/bluebullet.gif"  border=0>&nbsp;&nbsp;<%= rs.getString("MSG") %></TD></TR>
			<TR><TD align=left>&nbsp;</TD></TR>

<%
		} //while
		DatabaseManager.releaseConnection(con);
	} //try
	catch (Exception e) {
		Log.write(Log.ERROR,"Help.jsp caught error !");
        	DatabaseManager.releaseConnection(con);
	}
%>

</TABLE><BR CLEAR=ALL>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>
