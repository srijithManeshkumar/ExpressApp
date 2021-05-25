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
 */
/** 
 * MODULE:	SLAReportByUser.jsp	
 * 
 * DESCRIPTION: SLA report by userid
 * 
 * AUTHOR:      psedlak
 * 
 * DATE:        5.15.2005
 * 
 * HISTORY:
 */


/* $Log:   //10.33.3.28/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/SLAReportByUser.jsv  $
/*
/*Initial Checkin
*/

/* $Revision:   1.7  $
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
		<jsp:forward page="SLAByUserDateSelect.jsp"/>;
<%
		return;
	}
	String strStartDate = strStartYr + strStartMth + strStartDay;
	
	String strEndYr = alltelRequest.getParameter("to_due_date_yr");
	String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
	String strEndDay = alltelRequest.getParameter("to_due_date_dy");
	if ((strEndYr.length() == 0) || (strEndMth.length()==0) || (strEndDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "INVALID Date entered!");	
		Log.write(Log.DEBUG_VERBOSE, "SLA Invalid to date");
%>
		<jsp:forward page="SLAByUserDateSelect.jsp"/>;
<%
		return;
	}
	String strEndDate = strEndYr + strEndMth + strEndDay;
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("slastat", "'From Date' must be less than or equal to 'To Date'!");	
		Log.write(Log.DEBUG_VERBOSE, "SLA Invalid to date");
%>
		<jsp:forward page="SLAByUserDateSelect.jsp"/>;
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
		<jsp:forward page="SLAByUserDateSelect.jsp"/>;
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
		<jsp:forward page="SLAByUserDateSelect.jsp"/>;
<%
		return;
        }
	Log.write(Log.DEBUG_VERBOSE, "SLA Date:" + strStartDate + " Date:" + strEndDate);

	SLAReportByUserBean slaReport = new SLAReportByUserBean();
	slaReport.setStartYr( strStartYr ) ;
	slaReport.setStartMth( strStartMth );
	slaReport.setStartDay( strStartDay );
	slaReport.setEndYr( strEndYr ) ;
	slaReport.setEndMth( strEndMth ) ;
	slaReport.setEndDay( strEndDay ) ;

	Log.write(Log.DEBUG_VERBOSE, "SLAReportByUser user running=["+ sdm.getUser() + "] *********BEGIN********");

	// HDR 1071942 -additional criteria
	String[] strOCN_CDs= alltelRequest.getAttributeValue("OCN_CD");	//allow multiple
	String[] strSTATE_CDs = alltelRequest.getAttributeValue("STATE_CD");
	String[] strVENDORs = alltelRequest.getAttributeValue("VENDOR");
	String[] strSRVC_TYP_CDs = alltelRequest.getAttributeValue("SRVC_TYP_CD");
	String[] strACTVTY_TYP_CDs = alltelRequest.getAttributeValue("ACTVTY_TYP_CD");
	String strOrderflag = alltelRequest.getParameter("orderFlag");
         Log.write(Log.DEBUG_VERBOSE, "strOrderFlag" + strOrderflag );
	// if picked the Susp/rest/Disc, then expand...
	if ( strACTVTY_TYP_CDs!= null)
	{	if ( isElementOf(strACTVTY_TYP_CDs, "BDS") )
		{	
			for (int i=0;i<strACTVTY_TYP_CDs.length;i++)
			{ if (strACTVTY_TYP_CDs[i].equals("BDS"))
					strACTVTY_TYP_CDs[i]="B";
			}
			int iSize=strACTVTY_TYP_CDs.length;
			String[] strATs = (String[]) strACTVTY_TYP_CDs.clone();
			strACTVTY_TYP_CDs=null;
			strACTVTY_TYP_CDs = new String[iSize+2];
			for (int i=0;i<strATs.length;i++) strACTVTY_TYP_CDs[i]=strATs[i];
			iSize=strACTVTY_TYP_CDs.length;
			strACTVTY_TYP_CDs[iSize-2]="S";
			strACTVTY_TYP_CDs[iSize-1]="D";
		}
	}
	
	slaReport.setOCNs( strOCN_CDs );
	slaReport.setSTATE_CDs( strSTATE_CDs);
	slaReport.setVENDORs( strVENDORs );
	slaReport.setSRVC_TYP_CDs( strSRVC_TYP_CDs );
	slaReport.setACTVTY_TYP_CDs( strACTVTY_TYP_CDs );
	slaReport.setOrderFlag( strOrderflag );

	String[] strUserids = alltelRequest.getAttributeValue("USERID");
	slaReport.setUserids( strUserids );
	String[] strEmployeeGroups = alltelRequest.getAttributeValue("groupids");
	boolean egFlag = false;
	if( strEmployeeGroups != null )
	{
		if( isElementOf( strEmployeeGroups, "ALL" ) ){
			egFlag = true;
      		}
	}
	Log.write(Log.DEBUG_VERBOSE, "*********Append users from groups********");
	slaReport.extractEmployeeGroups( strEmployeeGroups, egFlag );
	
	String strReport = slaReport.runReport();

%>	
	<%= strReport %>
<%
	Log.write(Log.DEBUG_VERBOSE, "SLAReportByUser user running=["+ sdm.getUser() + "] *********END********");
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
