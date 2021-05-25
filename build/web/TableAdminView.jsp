<%@ include file="i_header.jsp" %>
<%
	final String CONTROLLER = "TableAdminCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}

	DateFormat HolidayDateFormat = new SimpleDateFormat("MM-dd-yyyy");
%>

<jsp:useBean id="tableadminbean" scope="request" class="com.alltel.lsr.common.objects.TableAdminBean" />

<%
	Connection con = null;
	Statement stmt = null;
        ResultSet rs   = null;
        
        try{
	con = DatabaseManager.getConnection();
	stmt = con.createStatement();
	if ( tableadminbean.getQueryString().length() == 0 )
	{
%>
		<jsp:forward page="NavigationErrorView.jsp"/>;
<%
		return;
	}
	rs = stmt.executeQuery(tableadminbean.getQueryString());
%>
	
<FORM METHOD="POST" ACTION="TableAdminCtlr">

<input type="hidden" NAME="rstrctsrch" VALUE="<%= tableadminbean.getRstrctSrch() %>">
<input type="hidden" NAME="rstrctsrchctgry" VALUE="<%= tableadminbean.getRstrctSrchCtgry() %>">
<input type="hidden" NAME="rstrctsrchvl" VALUE="<%= tableadminbean.getRstrctSrchVl() %>">

<TABLE width=50% align=center cellspacing=0 cellpadding=0 border=0>
	<TR>
		<TD align=center width=50%><SPAN class="header1"><%=tableadminbean.getTblAdmnDscrptn()%>&nbsp;&nbsp;Table</SPAN></TD>
</TABLE>
<BR CLEAR=ALL>
<BR>
<table align=center width=100%>
  <tr>
<%
if ( sdm.isAuthorized(tableadminbean.getTblAdmnScrtyTgAdd()) )
{
%>
    <td width=150>
	<table border=1 bordercolor="#7AABDE" cellspacing=1 cellpadding=2>
	  <tr><td align=center>
		<A HREF="./<%=tableadminbean.getTblAdmnCtlr()%>?action=new">&nbsp;Add&nbsp;NEW&nbsp;<%=tableadminbean.getTblAdmnDscrptn()%>&nbsp;</A>
	  </td></tr>
	</table>
    </td>
<%
}
%>
    <td>
	&nbsp;
    </td>
    <td>
    <table border=0 cellspacing=0 cellpadding=0>
     <tr><td align=center>
	&nbsp;Search&nbsp;>>>&nbsp;
    <SELECT NAME="srchctgry">
	<OPTION VALUE="" SELECTED>...Category...</OPTION>
	<%  for (int x = 0; x < tableadminbean.getTblAdmnClmns() ; x++) { %>
		<OPTION VALUE="<%=x%>"><%=tableadminbean.getTblAdmnClmnDscrptn(x)%></OPTION>
	<% } %>
	
	</SELECT>
	</td>
	<td align=center>
	&nbsp;Value:
	<input type=text maxLength=16 size=18 name=srchvl>
     </td>
     <td>
     <INPUT class=appButton TYPE="SUBMIT" name="submitbutton" value="GO">
     </td>
     </tr>
	</table>
    </td>
  </tr>
</table>
<input type=hidden name="tblnmbr" value="<%=tableadminbean.getTblAdmnSqncNmbr()%>">
</FORM>

<BR CLEAR=ALL>
<table align=center border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=1>
<tr>
	<th width=50>
	&nbsp;
    </th>
    <% for (int x = 0; x < tableadminbean.getTblAdmnClmns() ; x++) { %>

    <th align=left width=<%=tableadminbean.getTblAdmnClmnWdth(x)%>><%=tableadminbean.getTblAdmnClmnDscrptn(x)%>
	<BR>
<%   int TblAdmnSqncNmbr = tableadminbean.getTblAdmnSqncNmbr(); %>
<%	if (TblAdmnSqncNmbr != 29 && TblAdmnSqncNmbr !=30)
	{%>
	<a href="/TableAdminCtlr?tblnmbr=<%=tableadminbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=tableadminbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=ASC">
		<IMG NAME="Sort Ascending" SRC="images/arrow_asc.gif" BORDER=0>
     <% } %>
	</a>
<%	if (TblAdmnSqncNmbr != 29 && TblAdmnSqncNmbr !=30)
	{%>
	<a href="/TableAdminCtlr?tblnmbr=<%=tableadminbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=tableadminbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=DESC">
		<IMG NAME="Sort Descending" SRC="images/arrow_desc.gif" BORDER=0>
     <% } %>
	</a>
    </th>
    <% } %>
</tr>

<% while(rs.next()==true) { %>

<tr>
	<td align=center>
	<A HREF="<%=tableadminbean.getTblAdmnCtlr()%>?action=get&amp;<%=tableadminbean.getTblAdmnCtlrIdx()%>=

<%	if (tableadminbean.getTblAdmnCtlrIdx().equals("HLDY_DT") || 
	    tableadminbean.getTblAdmnCtlrIdx().equals("NOTE_STRT_DT") || 
	    tableadminbean.getTblAdmnCtlrIdx().equals("NOTE_END_DT"))
	{
%>
		<%=HolidayDateFormat.format(rs.getDate(tableadminbean.getTblAdmnCtlrIdx()))%>">&nbsp;SELECT&nbsp;</A>
<%	}
	else
	{
%>
		<%=rs.getString(tableadminbean.getTblAdmnCtlrIdx())%>">&nbsp;SELECT&nbsp;</A>
<%	}
%>

    </td>
	
    <% for (int x = 0; x < tableadminbean.getTblAdmnClmns() ; x++) { %>

	<td align=left>
<% 		int tmpIndex = tableadminbean.getTblAdmnClmnDbNm(x).lastIndexOf("."); 

		if ((tableadminbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("HLDY_DT") ||
		    (tableadminbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("NOTE_STRT_DT") ||
		    (tableadminbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("NOTE_END_DT"))
		{
%>
			<%=HolidayDateFormat.format(rs.getDate(tableadminbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)))%>
<%		}
		else
		{
%>
			<%= rs.getString(tableadminbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) %>
<%		}
%>
	</td>
	
    <% } %>
	
</tr>

<% }
} //end of try
catch(Exception e) {
    Log.write(Log.DEBUG_VERBOSE, "TableAdminView: Caught exception e=[" + e + "]");
}
finally {
   try {
        rs.close(); rs=null;
        stmt.close(); stmt=null;
   } catch (Exception ex){}
   DatabaseManager.releaseConnection(con);         
}      
%>
</table>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/TableAdminView.jsv  $
/*
/*   Rev 1.5   06 Mar 2002 11:38:48   dmartz
/*Home Page Maintenance
/*
/*   Rev 1.3   31 Jan 2002 14:49:12   sedlak
/* 
/*
/*   Rev 1.2   31 Jan 2002 13:34:02   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 08:28:40   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:48   wwoods
/*Initial Checkin
*/

/* $Revision:   1.5  $
*/

%>
