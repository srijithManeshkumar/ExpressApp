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
 * MODULE:	BillDisputeValidationView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:  Vince Pavill      
 * 
 * DATE:        01-21-2003
 * 
 * HISTORY:
 *	03/15/2003 Initial Check-in
 *	10/04/2003 psedlak use generic
 *
 */

%>

<%@ include file="i_BillDisputeHeader.jsp" %>

<%
Log.write(Log.DEBUG_VERBOSE, "BillDisputeValidationView.jsp --- ");
%>

<%@ include file="ExpressValidationView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

