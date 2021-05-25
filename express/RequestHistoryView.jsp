<%
/**
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                      COPYRIGHT (C) 2003
 *                              BY
 *                      Windstream COMMUNICATIONS INC.
 */
/**
 * MODULE:      RequestHistoryView.jsp
 *
 * DESCRIPTION: JSP View used to display LSR/Order History
 *
 * AUTHOR:      Express Development Team
 *
 * DATE:        1-1-2002
 *
 * HISTORY:
 *      07/23/2002 psedlak Correct time display
 *      10/15/2002 psedlak Show userid (HDR 165254)
 *	09/23/2003 psedlak make generic -show detail history
 *
 */
%>

<%@ include file="i_RequestHeader.jsp" %>
<%
RequestOrder thisOrder = RequestOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "RequestHistoryView.jsp --- ");
%>

<%@ include file="HistoryView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/RequestHistoryView.jsv  $
/*
/*   Rev 1.3   Jul 23 2002 13:37:58   sedlak
/*Fix date/time display in History Views 
/*after Oracle driver change
/*
/*   Rev 1.2   31 Jan 2002 14:53:44   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 08:20:40   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:26   wwoods
/*Initial Checkin
*/

/* $Revision:   1.3  $
*/

%>
