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
 * MODULE:	BillDisputeFormView.jsp	
 * 
 * DESCRIPTION: Display Dispute forms/sections/fields.
 * 
 * AUTHOR: Vince Pavill     
 * 
 * DATE:        01-21-2003
 * 
 * HISTORY:
 *	03/15/2003 Initial Check-in
 *      09/29/2003 psedlak made generic
 *
 */
%>

<%@ include file="i_BillDisputeHeader.jsp" %>

<%

BillDisputeBean myorderBean = new BillDisputeBean();
Log.write(Log.DEBUG_VERBOSE, "BillDisputeFormView.jsp --- ");

%>

<%@ include file="ExpressFormView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
