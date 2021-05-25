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
 * MODULE:	TicketNotesView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/20/2002 Initial Check-in
 *	09/29/2003 psedlak made generic
 *
 */
%>

<%@ include file="i_TicketHeader.jsp" %>
<%@ page import ="com.alltel.lsr.common.objects.*" %>

<%
String UPDATE_NOTES_TAG = "TCKT_NOTES_UPDATE";
TicketOrder myorder = TicketOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "TicketNotesView.jsp --- ");
%>

<%@ include file="ExpressNotesView.jsp" %>


</FORM>
<jsp:include page="i_footer.htm" flush="true" />

</BODY>
</HTML>

