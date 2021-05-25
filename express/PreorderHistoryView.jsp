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
 * MODULE:	PreorderHistoryView.jsp	
 * 
 * DESCRIPTION: JSP View used to display Preorder History
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *	07/23/2002 psedlak Correct time display
 *	10/15/2002 psedlak Show userid (HDR 165254)
 *	09/23/2003 psedlak make generic -show detail history
 */
%>

<%@ include file="i_PreorderHeader.jsp" %>
<%
PreorderOrder thisOrder = PreorderOrder.getInstance();
Log.write(Log.DEBUG_VERBOSE, "PreorderHistoryView.jsp --- ");
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
