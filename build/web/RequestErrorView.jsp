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
 * MODULE:	RequestErrorView.jsp	
 * 
 * DESCRIPTION: Displays order errors
 * 
 * AUTHOR:      
 * 
 * DATE:        
 * 
 * HISTORY:
 *
 */
%>

<%@ include file="i_header.jsp" %>

<%
Log.write(Log.DEBUG_VERBOSE, "RequestErrorView.jsp --- ");
%>
<BR>
<BR>
<P align=center>An Error has occurred with the Transaction you attempted.</P>
<BR>
<P align=center>The Request may have been updated by another user.<BR><center>OR<br>
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

<%

/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/RequestErrorView.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:56:34   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:24   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

%>
