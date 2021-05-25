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
 * MODULE:	TicketHistoryView.jsp	
 * 
 * DESCRIPTION: JSP View used to display TT History
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/20/2002 Initial Check-in
 *	07/23/2002 psedlak Correct time display
 *	10/15/2002 psedlak show userid in history view
 *	09/23/2003 psedlak make generic -show detail history
 */
%>

<%@ include file="i_TicketHeader.jsp" %>
<%
TicketOrder thisOrder = TicketOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "TicketHistoryView.jsp --- ");
%>

<%@ include file="HistoryView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

<%
/* $Log:   $
/*
/* $Revision:    $
*/
%>
