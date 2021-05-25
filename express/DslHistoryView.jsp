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
 * MODULE:	DslHistoryView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      
 * 
 * DATE:        06-05-2002
 * 
 * HISTORY:
 *		07/23/2002 psedlak Correct time display
 *		10/15/2002 psedlak show userid on history
 *		09/23/2003 psedlak make generic and show detail
 */
%>

<%@ include file="i_DslHeader.jsp" %>
<%
Log.write(Log.DEBUG_VERBOSE, "DslHistoryView.jsp --- ");
DslOrder thisOrder = DslOrder.getInstance();
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
