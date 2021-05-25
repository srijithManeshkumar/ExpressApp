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
 * MODULE:	PreorderErrorView.jsp	
 * 
 * DESCRIPTION: JSP View used to create new Preorders
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *	xx/dd/2002  
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%
Log.write(Log.DEBUG_VERBOSE, "PreorderErrorView.jsp --- ");
%>
<BR>
<BR>
<P align=center>An Error has occurred with the Transaction you attempted.<BR><center>OR<br>
After performing an action, the browser <b>back button</b> was used, data editted, then the action was attempted again.</center><br>
</p>
<BR>
<P align=center>This message will be updated to include additional details.</P>
<BR>
<BR>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

