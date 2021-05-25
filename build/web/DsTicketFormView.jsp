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
 * MODULE:	DsTicketFormView.jsp
 * 
 * DESCRIPTION: Display Ticket forms/sections/fields.
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	03/20/2004 pjs init
 *
 */
%>

<%@ include file="i_DsTicketHeader.jsp" %>

<%

DsTicketBean myorderBean = new DsTicketBean();
Log.write(Log.DEBUG_VERBOSE, "DsTicketFormView.jsp --- ");

%>

<%@ include file="ExpressFormView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
