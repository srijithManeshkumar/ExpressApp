<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2002
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	DslFormView.jsp	
 * 
 * DESCRIPTION: Display Dsl forms/sections/fields.
 * 
 * AUTHOR:      
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
 *	09-29-2003 psedlak made generic
 *
 */
%>

<%@ include file="i_DslHeader.jsp" %>

<%
Log.write(Log.DEBUG_VERBOSE, "DslFormView.jsp --- entry ");

DslBean myorderBean = new DslBean();
Log.write(Log.DEBUG_VERBOSE, "DslFormView.jsp --- ");

%>

<%@ include file="ExpressFormView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
