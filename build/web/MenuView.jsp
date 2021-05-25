<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<%
	String m_strRqstMnNmbr = request.getParameter("menunmbr");
	if ((m_strRqstMnNmbr == null) || (m_strRqstMnNmbr.length() == 0))
	{
		Log.write(Log.ERROR, "Invalid menu nmbr passed");
%>
		<jsp:forward page="NavigationErrorView.jsp"/>;
<%
		return;
	}

	int m_iRqstMnNmbr = Integer.parseInt(m_strRqstMnNmbr);
	LoginProfileBean lpb = sdm.getLoginProfileBean();
	MenuVector mv = lpb.getMenu(m_iRqstMnNmbr);
	if (mv == null)
	{
		Log.write(Log.ERROR, "This menu " + m_iRqstMnNmbr + "  doesnt exist for this user " +
			  lpb.getUser());
%>
		<jsp:forward page="NavigationErrorView.jsp"/>;
<%
		return;
	}
%>
	
<table align=center width="100%">
  <tr>
    <TH align=center>
	<SPAN class="header1"><%=mv.getMenuDescription()%></SPAN>
    </th>
  </tr>
</table>
<BR CLEAR=ALL>
<table align=center border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=1>
<% 
	Vector miv = mv.getMenuItemVector();
	for(int i = 0; i < miv.size(); i++)
	{
		MenuItem mi = (MenuItem)miv.elementAt(i);
%>
<tr>
	<td align=left>
	<A HREF="<%=mi.getHyperlink()%>">&nbsp;<%=mi.getMenuItemDescription()%>&nbsp;</A>
	</td>
	<TR><TD colspan=2>&nbsp;</TD></TR>
</tr>

<% 
	} //for
	
%>
<%
if( m_iRqstMnNmbr == 2 )
{
%>
<tr>
	<td align=center bgcolor="#7AABDE">
	<SPAN class="barheader"> Reports Tools </span>
	</td>
	<TR><TD colspan=2>&nbsp;</TD></TR>
</tr>
<tr>
	<td align=left>
	<A HREF="/EmployeeGroups.jsp">&nbsp;Employee Group&nbsp;</A>
	</td>
	<TR><TD colspan=2>&nbsp;</TD></TR>
</tr>
<%
}
%>
</table>
</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/MenuView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:28:24   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:04   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
%>
