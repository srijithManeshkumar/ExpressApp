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
 * MODULE:	PreorderFormView.jsp	
 * 
 * DESCRIPTION: Display Preorder forms/sections/fields.
 * 
 * AUTHOR:      
 * 
 * DATE:        03-02-2002
 * 
 * HISTORY:
 *	03/02/2002 dmartz/psedlak Release 2.0
 *	09/29/2003 psedlak made generic
 *
 */
%>

<%@ include file="i_PreorderHeader.jsp" %>

<%

PreorderBean myorderBean = new PreorderBean();
Log.write(Log.DEBUG_VERBOSE, "PreorderFormView.jsp --- ");

%>

<%@ include file="ExpressFormView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
