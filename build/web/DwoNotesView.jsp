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
 * MODULE:	DwoNotesView.jsp	
 * 
 * DESCRIPTION: 
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
<%@ page import ="com.alltel.lsr.common.objects.*" %>

<%

String UPDATE_NOTES_TAG = "DWO_NOTES_UPDATE";
DwoOrder myorder = DwoOrder.getInstance(m_strTypInd);
Log.write(Log.DEBUG_VERBOSE, "DwoNotesView.jsp --- ");
%>

<%@ include file="ExpressNotesView.jsp" %>


</FORM>
<jsp:include page="i_footer.htm" flush="true" />

</BODY>
</HTML>

