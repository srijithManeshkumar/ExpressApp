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
 * MODULE:	UserOptions.jsp	
 * 
 * DESCRIPTION: JSP View used to provide User specific options
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *	05/01/2002	dmartz	initial
 *	05/31/2002	psedlak	Added security objects on Default values choice
 *	01/27/2004 	psedlak allow defaults for DSL users
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%
	final String CONTROLLER = "UserInfoCtlr";
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to change user options on JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
%>


<BR>
<TABLE width=50% align=center cellspacing=0 cellpadding=0 border=0>
	<TR>
		<TD align=center width=50%><SPAN CLASS="header1">USER OPTIONS</SPAN></TD>
</TABLE>
<BR CLEAR=ALL>
<BR>
<BR>
<TABLE align=center cellspacing=0 cellpadding=0 border=0>
  <TR><TD align=left><SPAN CLASS="smallstyle1">>&nbsp;<a href="UserInfoCtlr?action=get&USERID=<%=sdm.getUser()%>">User Information</a>&nbsp;</SPAN></TD></TR>
  <TR><TD >&nbsp;</TD></TR>
<%
	//Only show the Default User values option if the user has ability to enter Order or Preorders
	if (sdm.isAuthorized("CREATE_REQUESTS") || sdm.isAuthorized("CREATE_PREORDERS")
		|| sdm.isAuthorized("CREATE_DSLS"))
	{
%>
		<TR><TD align=left><SPAN CLASS="smallstyle1">>&nbsp;<a href=DefaultValuesView.jsp>Default User Values</a>&nbsp;</SPAN></TD></TR>
<%
	}
	else if (sdm.isAuthorized("CREATE_DWOS") || sdm.isAuthorized("CREATE_DSTICKETS"))
	{
%>
  		<TR><TD align=left><SPAN CLASS="smallstyle1">>&nbsp;<a href=DefaultValuesView.jsp>User Profile</a>&nbsp;</SPAN></TD></TR>
<%	}
%>
  <TR><TD >&nbsp;</TD></TR>
  <TR><TD align=left><SPAN CLASS="smallstyle1">&nbsp;</SPAN></TD></TR>
</TABLE>
</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/UserOptions.jsv  $
/*
/*   Rev 1.5   May 31 2002 10:11:20   sedlak
/*Wrapped Default Values with Security Objects
/*
/*   Rev 1.4   May 31 2002 09:16:08   sedlak
/*Chgd Security Object in JSP
/*
/*   Rev 1.3   09 Apr 2002 15:30:20   dmartz
/*User Info Screen
/*
/*   Rev 1.2   31 Jan 2002 13:32:52   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 06:59:38   sedlak
/* 
/*
/*   Rev 1.0   31 Jan 2002 06:46:18   psedlak
/*Initial Checkin
*/

/* $Revision:   1.5  $
*/
%>
