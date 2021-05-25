<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2004
 *				BY
 *			ALLTEL COMMUNICATIONS INC.
 */
/** 
 * MODULE:	DsTicketErrorView2.jsp
 * 
 * DESCRIPTION: Displays ticket errors
 * 
 * AUTHOR:      
 * 
 * DATE:        04-29-2004
 * 
 * HISTORY:
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%
Log.write(Log.DEBUG_VERBOSE, "DsTicketErrorView2.jsp --- ");
%>
<BR>
<BR>
<P align=center>A system error has occurred with the Ticket you created.</P>
<BR>
<p align=center>To ensure the ticket gets properly reported and worked as soon as possible, please<BR>
call the DS TAC at: 1-866-990-DATA (3282).</center><br>
</p>
<BR>
<BR>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

