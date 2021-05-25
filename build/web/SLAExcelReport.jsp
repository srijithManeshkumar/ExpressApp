<%-- 
    Document   : SLAExcelReport
    Created on : May 24, 2011, 5:05:10 PM
    Author     : satish.t
--%>

<%@ page language="java"
         import = "java.util.*, java.text.*,
		  java.sql.*,
		  javax.sql.*,
		  com.alltel.lsr.common.objects.*,
		  com.alltel.lsr.common.util.*"
         %>

<%
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "SLAReport" + ".xls");
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
	if (alltelRequest.getParameter("SUBMITBUTTON").equals("Cancel"))
	{
%>
                <jsp:forward page="Reports.jsp"/>;
<%
		return;
	}



	String strStartYr = alltelRequest.getParameter("from_due_date_yr");
	String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
	String strStartDay = alltelRequest.getParameter("from_due_date_dy");
	if ((strStartYr.length() == 0) || (strStartMth.length()==0) || (strStartDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "SLA Invalid from date");
%>
		<jsp:forward page="SLADateSelect.jsp"/>;
<%
		return;
	}
	String strStartDate = strStartYr + strStartMth + strStartDay;

	String strEndYr = alltelRequest.getParameter("to_due_date_yr");
	String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
	String strEndDay = alltelRequest.getParameter("to_due_date_dy");
	String strOrderflag = alltelRequest.getParameter("orderFlag");
         Log.write(Log.DEBUG_VERBOSE, "strOrderFlag" + strOrderflag );
	


	if ((strEndYr.length() == 0) || (strEndMth.length()==0) || (strEndDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "SLA Invalid to date");
%>
		<jsp:forward page="SLADateSelect.jsp"/>;
<%
		return;
	}
	String strEndDate = strEndYr + strEndMth + strEndDay;
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "'From Date' must be less than or equal to 'To Date'!");
		Log.write(Log.DEBUG_VERBOSE, "SLA Invalid to date");
%>
		<jsp:forward page="SLADateSelect.jsp"/>;
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
		Log.write(Log.DEBUG_VERBOSE, "SLA Invalid from date");
%>
		<jsp:forward page="SLADateSelect.jsp"/>;
<%
		return;
        }
	calTemp.set(Integer.parseInt(strEndYr),  Integer.parseInt(strEndMth) - 1,  1, 0, 0, 0);
        iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
Log.write("iMaxDays FromDate=" + iMaxDays + "  Enddate=" + strEndYr + strEndMth + strEndDay);
        if (Integer.parseInt(strEndDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("slastat", "'To Date' - invalid day of month selected");
		Log.write(Log.DEBUG_VERBOSE, "SLA Invalid to date");
%>
		<jsp:forward page="SLADateSelect.jsp"/>;
<%
		return;
        }
	Log.write(Log.DEBUG_VERBOSE, "SLA Date:" + strStartDate + " Date:" + strEndDate);

	SLAReportBean slaReport = new SLAReportBean();
	slaReport.setStartYr( strStartYr ) ;
	slaReport.setStartMth( strStartMth );
	slaReport.setStartDay( strStartDay );
	slaReport.setEndYr( strEndYr ) ;
	slaReport.setEndMth( strEndMth ) ;
	slaReport.setEndDay( strEndDay ) ;
	slaReport.setOrderFlag( strOrderflag );

	Log.write(Log.DEBUG_VERBOSE, "SLAReport user running=["+ sdm.getUser() + "] *********BEGIN********");

	// HDR 1071942 -additional criteria
	String[] strOCN_CDs= alltelRequest.getAttributeValue("OCN_CD");	//allow multiple
	String[] strSTATE_CDs = alltelRequest.getAttributeValue("STATE_CD");
	String[] strVENDORs = alltelRequest.getAttributeValue("VENDOR");
	String[] strSRVC_TYP_CDs = alltelRequest.getAttributeValue("SRVC_TYP_CD");

	slaReport.setOCNs( strOCN_CDs );
	slaReport.setSTATE_CDs( strSTATE_CDs);
	slaReport.setVENDORs( strVENDORs );
	slaReport.setSRVC_TYP_CDs( strSRVC_TYP_CDs );


	String strReport = slaReport.runReport();

%>
	<%= strReport %>


</UL>
<BR>
<BR>
<BR>
</FORM>

</BODY>
</HTML>