<%-- 
    Document   : UserExcelReport
    Created on : May 24, 2011, 2:32:45 PM
    Author     : satish.t
--%>

<%@ include file="ExpressUtil.jsp" %>
<%@ page import ="java.util.*, com.alltel.lsr.common.batch.UserReportInfo" %>

<%@ page language="java"
         import = "java.util.*, java.text.*,
		  java.sql.*,
		  javax.sql.*,
		  com.alltel.lsr.common.objects.*,
		  com.alltel.lsr.common.util.*"
         %>

<%
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "UserReport" + ".xls");
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
<%		return;
	}

	boolean	bSpecificUserids = false;
	Hashtable m_hashUserids;
	Vector m_vSortedUsers = new Vector();	//use this to retreive hash in same ascending order every time

	String strCheckBox = alltelRequest.getParameter("keep_weekends");
	boolean bKeepWeekends = false;
	if ( (strCheckBox == null) || (strCheckBox.length() <1))
	{}
	else	bKeepWeekends = true;
	strCheckBox = alltelRequest.getParameter("count_weekends");
	boolean bCountWeekends = false;
	if ( (strCheckBox == null) || (strCheckBox.length() <1))
	{}
	else	bCountWeekends = true;
	Log.write(Log.DEBUG_VERBOSE, "UserReport() Weekend options = " + bKeepWeekends + " " + bCountWeekends);

	String strStartYr = alltelRequest.getParameter("from_due_date_yr");
	String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
	String strStartDay = alltelRequest.getParameter("from_due_date_dy");
	if ((strStartYr.length() == 0) || (strStartMth.length()==0) || (strStartDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "UserRpt Invalid from date");
%>
		<jsp:forward page="UserRptDateSelect.jsp?rpt=UserReport.jsp"/>;
<%		return;
	}
	String strStartDate = strStartYr + strStartMth + strStartDay;

	String strEndYr = alltelRequest.getParameter("to_due_date_yr");
	String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
	String strEndDay = alltelRequest.getParameter("to_due_date_dy");
	if ((strEndYr.length() == 0) || (strEndMth.length()==0) || (strEndDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "User Rpt Invalid to date");
%>
		<jsp:forward page="UserRptDateSelect.jsp?rpt=UserReport.jsp"/>;
<%		return;
	}
	String strEndDate = strEndYr + strEndMth + strEndDay;
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' must be less than or equal to 'To Date'!");
		Log.write(Log.DEBUG_VERBOSE, "User Rpt Invalid to date");
%>
		<jsp:forward page="UserRptDateSelect.jsp?rpt=UserReport.jsp"/>;
<%		return;
	}
        //Check days of month and adjust if necessary ...
	Calendar calStart = Calendar.getInstance();
	calStart.set(Integer.parseInt(strStartYr),  Integer.parseInt(strStartMth) - 1,  1, 0, 0, 0);
        int iMaxDays = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (Integer.parseInt(strStartDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' - invalid day of month selected");
		Log.write(Log.DEBUG_VERBOSE, "User Rpt Invalid from date");
%>
		<jsp:forward page="UserRptDateSelect.jsp?rpt=UserReport.jsp"/>;
<%		return;
        }
	calStart.set(Calendar.DAY_OF_MONTH, Integer.parseInt(strStartDay) );

	Calendar calEnd = Calendar.getInstance();
	calEnd.set(Integer.parseInt(strEndYr),  Integer.parseInt(strEndMth) - 1,  1, 0, 0, 0);
        iMaxDays = calEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (Integer.parseInt(strEndDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("reportstat", "'To Date' - invalid day of month selected");
		Log.write(Log.DEBUG_VERBOSE, "User Rpt Invalid to date");
%>
		<jsp:forward page="UserRptDateSelect.jsp?rpt=UserReport.jsp"/>;
<%		return;
        }
	calEnd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(strEndDay) );
	calEnd.set(Calendar.HOUR_OF_DAY, 23);

	UserReportBean userStatsReport = new UserReportBean();
        userStatsReport.setStartYr( strStartYr ) ;
        userStatsReport.setStartMth( strStartMth );
        userStatsReport.setStartDay( strStartDay );
        userStatsReport.setEndYr( strEndYr ) ;
        userStatsReport.setEndMth( strEndMth ) ;
        userStatsReport.setEndDay( strEndDay ) ;
	userStatsReport.setKeepWeekends(bKeepWeekends);
	userStatsReport.setCountWeekends(bCountWeekends);

        Log.write(Log.DEBUG_VERBOSE, "UserStatsReport user running=["+ sdm.getUser() + "] *********BEGIN********");

	String[] strUserids = alltelRequest.getAttributeValue("USERID");
        userStatsReport.setUserids( strUserids );
	String[] strEmployeeGroups = alltelRequest.getAttributeValue("groupids");
       boolean egFlag = false;
       if( strEmployeeGroups != null )
       {

       	if( isElementOf( strEmployeeGroups, "ALL" ) ){
       		egFlag = true;
      	}

       }
         Log.write(Log.DEBUG_VERBOSE, "*********Append users from groups********");

       userStatsReport.extractEmployeeGroups( strEmployeeGroups, egFlag );
        String strReport = userStatsReport.runReport();

%>
        <%= strReport %>
<%
        Log.write(Log.DEBUG_VERBOSE, "UserStatsReport user running=["+ sdm.getUser() + "] *********END********");
%>

</UL>
<BR>
<BR>
<BR>
</FORM>

</BODY>
</HTML>