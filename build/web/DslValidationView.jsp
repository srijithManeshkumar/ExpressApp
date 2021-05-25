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
 * MODULE:	DslValidationView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
 *	10/04/2003 psedlak use generic
 *
 */

%>

<%@ include file="i_DslHeader.jsp" %>

<%
Log.write(Log.DEBUG_VERBOSE, "DslValidationView.jsp --- ");
%>

<%@ include file="ExpressValidationView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

