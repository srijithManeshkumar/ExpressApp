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
 * MODULE:	DsTicketHistoryView.jsp	
 * DESCRIPTION: JSP View used to display TT History
 * AUTHOR:      
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	3-17-2004 pjs init
 */
%>

<%@ include file="i_DsTicketHeader.jsp" %>
<%
DsTicketOrder thisOrder = DsTicketOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "DsTicketHistoryView.jsp --- ");
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
