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
 * MODULE:	DsTicketNotesView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	03/20/2004 psedlak init
 *
 */
%>

<%@ include file="i_DsTicketHeader.jsp" %>
<%@ page import ="com.alltel.lsr.common.objects.*" %>

<%
String UPDATE_NOTES_TAG = "DSTCKT_NOTES_UPDATE";
DsTicketOrder myorder = DsTicketOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "DsTicketNotesView.jsp --- ");
%>

<%@ include file="ExpressNotesView.jsp" %>


</FORM>
<jsp:include page="i_footer.htm" flush="true" />

</BODY>
</HTML>

