<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			Windstream COMMUNICATIONS INC.
 * MODULE:	FulfillmntReport.jsp	
 * 
 * DESCRIPTION: Business Data Product, fulfillment report
 * 
 * AUTHOR:      Edris Kalibala
 * 
 * DATE:        11-10-2005
 * 
 * HISTORY:
 */
%>

<%@ include file="ExpressUtil.jsp" %>
<%@ include file="i_header.jsp" %>
<%
	final String SECURITY_OBJECT = "PROV_REPORTS";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		 alltelResponse.sendRedirect(SECURITY_URL);
	}

	// Did they cancel?
	if (alltelRequest.getParameter("cancel") != null)
		if (alltelRequest.getParameter("cancel").equals("Cancel"))
		{
		%>
		                <jsp:forward page="Reports.jsp"/>;
		<%
				return;
	}

	String strStartYr = alltelRequest.getParameter("startYr");
	String strStartMth = alltelRequest.getParameter("startMn");
	String strStartDay = alltelRequest.getParameter("startDy");
	if ((strStartYr.length() == 0) || (strStartMth.length()==0) || (strStartDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "INVALID Date entered!");	
		Log.write(Log.DEBUG_VERBOSE, "Fulfillment Report: Invalid from date");
%>
		<jsp:forward page="BDPDateSelect.jsp"/>;
<%
		return;
	}
	String strStartDate = strStartYr + strStartMth + strStartDay;
	
	String strEndYr = alltelRequest.getParameter("endYr");
	String strEndMth = alltelRequest.getParameter("endMn");
	String strEndDay = alltelRequest.getParameter("endDy");
	if ((strEndYr.length() == 0) || (strEndMth.length()==0) || (strEndDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "INVALID Date entered!");	
		Log.write(Log.DEBUG_VERBOSE, "Fulfillment Report: Invalid to date");
%>
		<jsp:forward page="BDPDateSelect.jsp"/>;
<%
		return;
	}
	String strEndDate = strEndYr + strEndMth + strEndDay;
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "'From Date' must be less than or equal to 'To Date'!");	
		Log.write(Log.DEBUG_VERBOSE, "Fulfillment Report: Invalid to date");
%>
		<jsp:forward page="BDPDateSelect.jsp"/>;
<%
		return;
	}
        //Check days of month and adjust if necessary ...
	Calendar calTemp = Calendar.getInstance();
	calTemp.set(Integer.parseInt(strStartYr),  Integer.parseInt(strStartMth) - 1,  1, 0, 0, 0);
        int iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
Log.write("iMaxDays ToDate=" + iMaxDays + "  Startdate=" + strStartYr + strStartMth + strStartDay);
        if (Integer.parseInt(strStartDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("slastat", "'From Date' - invalid day of month selected");	
		Log.write(Log.DEBUG_VERBOSE, "Fulfillment Report: Invalid from date");
%>
		<jsp:forward page="BDPDateSelect.jsp"/>;
<%
		return;
        }
	calTemp.set(Integer.parseInt(strEndYr),  Integer.parseInt(strEndMth) - 1,  1, 0, 0, 0);
        iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
Log.write("iMaxDays FromDate=" + iMaxDays + "  Enddate=" + strEndYr + strEndMth + strEndDay);
        if (Integer.parseInt(strEndDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("slastat", "'To Date' - invalid day of month selected");	
		Log.write(Log.DEBUG_VERBOSE, "Fulfillment Report: Invalid to date");
%>
		<jsp:forward page="BDPDateSelect.jsp"/>;
<%
		return;
        }
	Log.write(Log.DEBUG_VERBOSE, "Fulfillment Report: Date:" + strStartDate + " Date:" + strEndDate);

	BDPReportBean Report = new BDPReportBean();
	Report.setStartYr( strStartYr ) ;
	Report.setStartMth( strStartMth );
	Report.setStartDay( strStartDay );
	Report.setEndYr( strEndYr ) ;
	Report.setEndMth( strEndMth ) ;
	Report.setEndDay( strEndDay ) ;
	Report.setReportType( 1 ) ;
	String[] strProductType = alltelRequest.getAttributeValue("dwonew_prdcttyp");	//allow multiple
	String[] strSTATE_CDs = alltelRequest.getAttributeValue("state");
	String[] strOrderType = alltelRequest.getAttributeValue("dwonew_srvctyp");
	String[] strChangeType = alltelRequest.getAttributeValue("dwonew_acttyp");
	String[] strSubChangeType =  alltelRequest.getAttributeValue("dwonew_actsubtyp");
	
	Report.setProductTypes( strProductType );
	Report.setOrderTypes( strOrderType );
	Report.setChangeTypes( strChangeType );
	Report.setChangeSubTypes( strSubChangeType);
	Report.setSTATE_CDs( strSTATE_CDs );	
	Connection conRC = null;
	String strArrStt[] = {"__" };
	try { 
		conRC = DatabaseManager.getConnection();	
		String strReport =Report.runReport( conRC );
	
	%>	
		<%=strReport %>
	<%
	} // try 
	catch (Exception e) {
		e.printStackTrace();
		Log.write(Log.DEBUG_VERBOSE, e.toString() );
	}
	finally {
		DatabaseManager.releaseConnection(conRC);
	}

%>

</UL>
<BR>
<BR>
<BR>
<center><a href=javascript:this.history.back()>GO BACK</a></center>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />

</BODY>
</HTML>
