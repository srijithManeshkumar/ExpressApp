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
 * MODULE:	DwoFormView.jsp	
 * 
 * DESCRIPTION: Display Dwo forms/sections/fields.
 * 
 * AUTHOR:      
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *
 */
%>

<%@ include file="i_DwoHeader.jsp" %>

<%

DwoBean myorderBean = new DwoBean(m_strTypInd);
Log.write(Log.DEBUG_VERBOSE, "DwoFormView.jsp --- ");

%>

<%@ include file="ExpressFormView.jsp" %>

</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>
