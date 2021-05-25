<%-- 
    Document   : FulFillmntExcelReport
    Created on : May 24, 2011, 4:33:26 PM
    Author     : satish.t
--%>

<%@ include file="ExpressUtil.jsp" %>
<%@ page language="java"
         import = "java.util.*, java.text.*,
		  java.sql.*,
		  javax.sql.*,
		  com.alltel.lsr.common.objects.*,
		  com.alltel.lsr.common.util.*"
         %>

<%
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "FulFillmntReport" + ".xls");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");

            AlltelRequest alltelRequest = null;
            AlltelResponse alltelResponse = null;
            SessionDataManager sdm = null;
            try {
                alltelRequest = new AlltelRequest(request);
                alltelResponse = new AlltelResponse(response);
                sdm = alltelRequest.getSessionDataManager();
                if ((sdm == null) || (!sdm.isUserLoggedIn())) {
                    alltelResponse.sendRedirect("LoginCtlr");
                    return;
                }
            } catch (Exception e) {
                Log.write(Log.ERROR, e.getMessage());
                Log.write(Log.ERROR, "Trapped in i_header.jsp");
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
</FORM>

</BODY>
</HTML>
