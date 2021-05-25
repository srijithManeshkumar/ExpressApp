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
 * MODULE:	PreorderNotesView.jsp	
 * 
 * DESCRIPTION: JSP View used to display Preorder Notes
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *	09/29/2003 psedlak made generic
 *
 */

%>

<%@ include file="i_PreorderHeader.jsp" %>
<%@ page import ="com.alltel.lsr.common.objects.*" %>

<%
String UPDATE_NOTES_TAG = "PRE_NOTES_UPDATE";
PreorderOrder myorder = PreorderOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "PreorderNotesView.jsp --- ");
%>

<%@ include file="ExpressNotesView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />

</BODY>
</HTML>
