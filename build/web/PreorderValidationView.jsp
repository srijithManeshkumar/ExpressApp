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
 * MODULE:	RequestValidationView.jsp	
 * 
 * DESCRIPTION: JSP View used to display Validation Errors
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *	10/04/2003 psedlak use generic
 *
 */

%>

<%@ include file="i_PreorderHeader.jsp" %>

<%
Log.write(Log.DEBUG_VERBOSE, "PreorderValidationView.jsp --- ");
%>

<%@ include file="ExpressValidationView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
