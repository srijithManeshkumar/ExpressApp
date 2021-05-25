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
 * MODULE:	DslNotesView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
 *	09/29/2003 psedlak made generic
 */

%>

<%@ include file="i_DslHeader.jsp" %>
<%@ page import ="com.alltel.lsr.common.objects.*" %>

<%
String UPDATE_NOTES_TAG = "DSL_NOTES_UPDATE";
DslOrder myorder = DslOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "DslNotesView.jsp --- ");
%>

<%@ include file="ExpressNotesView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />

</BODY>
</HTML>

