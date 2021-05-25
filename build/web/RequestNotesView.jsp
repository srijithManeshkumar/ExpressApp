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
 * MODULE:      RequestNotesView.jsp
 *
 * DESCRIPTION:
 *
 * AUTHOR:
 *
 * DATE:        06-05-2002
 *
 * HISTORY:
 *      09/29/2003 psedlak made generic
 */
%>

<%@ include file="i_RequestHeader.jsp" %>
<%@ page import ="com.alltel.lsr.common.objects.*" %>

<%
String UPDATE_NOTES_TAG = "RQST_NOTES_UPDATE";
RequestOrder myorder = RequestOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "RequestNotesView.jsp --- ");
%>

<%@ include file="ExpressNotesView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />

</BODY>
</HTML>
<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/RequestNotesView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:53:06   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:30   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

%>
