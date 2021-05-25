<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>

<head>
<title>Top Frame</title>
<LINK rel=stylesheet type="text/css" HREF="application.css">
</head>
<body bgcolor="white" LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0>
<table width=100% align=left border=0 cellspacing=0 cellpadding=1>

<% String menuscope = request.getAttribute("accesslevel").toString(); %>

<% if(menuscope.equals("all") ) { %>

<tr>
<!--	<td width=40% align=left><img src="lsr.gif"></td> --> 
	<td width=20% valign=top>
		<table border=0 cellspacing=0 cellpadding=5>
		<tr><td><A HREF="MenuCtrl?sevent=rqst" target="fmain">Work&nbsp;With&nbsp;Requests</A></td></tr>
		<tr><td><A HREF="MenuCtrl?sevent=rept" target="fmain">Work&nbsp;With&nbsp;Reports</A></td></tr>
		</table>
	</td>
	<td width=20% valign=top>
		<table border=0 cellspacing=0 cellpadding=5>
		<tr><td><A HREF="MenuCtrl?sevent=tadmin" target="fmain">Table&nbsp;Admin</A></td></tr>
		<tr><td><A HREF="MenuCtrl?sevent=uadmin" target="fmain">User&nbsp;Admin</A></td></tr>
		</table>
	</td>
	<td width=20% valign=top>
		<table border=0 cellspacing=0 cellpadding=5>
		<tr><td><A HREF="MenuCtrl?sevent=hlp" target="fmain">Help</A></td></tr>
		<tr><td><A HREF="MenuCtrl?sevent=logout" target="fmain">Logout</A></td></tr>
		</table>
	</td>
</tr>
<tr><td colspan=4><HR></td></tr>
<h1> WELCOME <%= (String)request.getAttribute("who") %> TO LSR MAIN MENU</h1>
</table>

<% } else { %>

    <h1> WELCOME <%= (String)request.getAttribute("who") %> TO LSR MAIN MENU (LIMITED ACCESS!) </h1>
<% }  %>

</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/MainMenu.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:28:12   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:58   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
%>
