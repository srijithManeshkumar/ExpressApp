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
 * MODULE:      RequestValidationView.jsp
 *
 * DESCRIPTION: JSP View used to display Validation Errors. 
 *
 * AUTHOR:      
 *
 * DATE:        9-29-2003
 *
 * HISTORY:
 *	10/04/2003 psedlak use generic
 */
%>

<%@ include file="i_RequestHeader.jsp" %>

<%
Log.write(Log.DEBUG_VERBOSE, "RequestValidationView.jsp --- ");
%>

<%@ include file="ExpressValidationView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/RequestValidationView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:51:52   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:32   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

%>
