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
 * MODULE:	BillDisputeNotesView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:	Vince Pavill      
 * 
 * DATE:        01-21-2003
 * 
 * HISTORY:
 *	 03/15/2003 Initial Check-in
 *	 09/22/2003 psedlak fix sec object and made generic
 */

%>

<%@ include file="i_BillDisputeHeader.jsp" %>
<%@ page import ="com.alltel.lsr.common.objects.*" %>

<%
String UPDATE_NOTES_TAG = "DISPUTE_NOTES_UPDATE";
BillDisputeOrder myorder = BillDisputeOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "BillDisputeNotesView.jsp --- ");
%>

<%@ include file="ExpressNotesView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

