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
 * MODULE:	expressAlert.jsp
 * 
 * DESCRIPTION: JSP View used to display a generic alert box
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        6-1-2002
 * 
 * HISTORY:
 *
 */

%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<LINK rel=stylesheet type="text/css" HREF="application.css">
<head><title>Express Session Timeout Warning !!!!</title>
</head>
<body bgcolor="#C0C0C0">
<center>
<BR>
<% String strMsg = (String) request.getParameter("msg"); %>
<%= strMsg %>
<BR>
<button onClick="window.close()">Close</button>
</center>
</body>
</html>
