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
 * MODULE:	SLAReport.jsp	
 * 
 * DESCRIPTION: SLA report by OCN/state. User picks a date range from SLADateSelect.jsp.
 * 
 * AUTHOR:      psedlak
 * 
 * DATE:        01-02-2002
 * 
 * HISTORY:
 *	02/11/2002  psedlak Release 1.1 Revised per client requirements.
 *	03/11/2002  psedlak Fixed column heading and query.
 *	12/20/2002  psedlak Chg OCN to alphanumeric (HD 227319)
 *	08/30/2004  Psedlak HD 1028048 OCN reported on multiple times (WNP related). Also added
 *		ability to select a single OCN.
 *	10/30/2004  Psedlak HDR 1071942 Add aditional report selectio criteria
 *	2-21-2005   psedlak - changed to use SLAReportBean
 */


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/SLAReport.jsv  $
/*
/*   Rev 1.7   Jun 05 2002 11:28:20   sedlak
/*Fix total line
/*
/*   Rev 1.6   May 28 2002 13:15:18   sedlak
/* 
/*
/*   Rev 1.5   21 Feb 2002 12:34:42   sedlak
/* 
/*
/*   Rev 1.4   14 Feb 2002 11:19:18   sedlak
/*release 1.1
/*
/*   Rev 1.3   31 Jan 2002 13:35:04   sedlak
/* 
/*
/*   Rev 1.2   31 Jan 2002 07:08:30   sedlak
/* 
/*
/*   Rev 1.1   30 Jan 2002 14:49:10   sedlak
/*rel 1.0 base
/*
/*   Rev 1.0   23 Jan 2002 11:06:34   wwoods
/*Initial Checkin
*/

/* $Revision:   1.7  $
*/
%>

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
<%
	Log.write(Log.DEBUG_VERBOSE, "SLAReport user running=["+ sdm.getUser() + "] *********END********");
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
