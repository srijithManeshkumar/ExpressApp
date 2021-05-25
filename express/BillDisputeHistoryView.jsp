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
 * MODULE:	BillDisputeHistoryView.jsp	
 * 
 * DESCRIPTION: JSP View used to display BillDispute History
 * 
 * AUTHOR:  Vince Pavill      
 * 
 * DATE:        01-21-2003
 * 
 * HISTORY:
 *	03/15/2003 Initial Check-in
 *	09/23/2003 psedlak make generic -show detail history
 */
%>

<%@ include file="i_BillDisputeHeader.jsp" %>
<%
BillDisputeOrder thisOrder = BillDisputeOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "BillDisputeHistoryView.jsp --- ");
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
