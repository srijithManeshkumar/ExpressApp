<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2003
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	TicketFormView.jsp	
 * 
 * DESCRIPTION: Display Ticket forms/sections/fields.
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/20/2002 Initial Check-in
 *	09/19/2003 psedlak made	generic
 *
 */
%>

<%@ include file="i_TicketHeader.jsp" %>

<%

TicketBean myorderBean = new TicketBean();
Log.write(Log.DEBUG_VERBOSE, "TicketFormView.jsp --- ");

%>

<%@ include file="ExpressFormView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
