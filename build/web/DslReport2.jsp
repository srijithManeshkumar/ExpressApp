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
 * MODULE:	DslReport.jsp	
 * 
 * DESCRIPTION: Dsls report by Company. User picks a date range from DslDateSelect.jsp.
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        06-12-2002
 * 
 * HISTORY:
 *
 */

%>

<%@ include file="i_header.jsp" %>
<%
	final long DAY_IN_SEC = (long) 86400;
	final long HOUR_IN_SEC = (long) 3600;
	final long MIN_IN_SEC = (long) 60;
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

	Connection con = null;
	Statement  stmt = null;
	int     iCmpnyCount = 0;
	long    lIntervalTotals = 0;
	
	String strStartYr = alltelRequest.getParameter("from_due_date_yr");
	String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
	String strStartDay = alltelRequest.getParameter("from_due_date_dy");
	if ((strStartYr.length() == 0) || (strStartMth.length()==0) || (strStartDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("dslstat", "INVALID Date entered!");	
		Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid from date");
%>
		<jsp:forward page="DslDateSelect.jsp"/>;
<%
		return;
	}
	String strStartDate = strStartYr + strStartMth + strStartDay;
	
	String strEndYr = alltelRequest.getParameter("to_due_date_yr");
	String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
	String strEndDay = alltelRequest.getParameter("to_due_date_dy");
	if ((strEndYr.length() == 0) || (strEndMth.length()==0) || (strEndDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("dslstat", "INVALID Date entered!");	
		Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid to date");
%>
		<jsp:forward page="DslDateSelect.jsp"/>;
<%
		return;
	}
	String strEndDate = strEndYr + strEndMth + strEndDay;
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("dslstat", "'From Date' must be less than or equal to 'To Date'!");	
		Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid to date");
%>
		<jsp:forward page="DslDateSelect.jsp"/>;
<%
		return;
	}
        //Check days of month and adjust if necessary ...
	Calendar calTemp = Calendar.getInstance();
	calTemp.set(Integer.parseInt(strStartYr),  Integer.parseInt(strStartMth) - 1,  1, 0, 0, 0);
        int iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (Integer.parseInt(strStartDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("dslstat", "'From Date' - invalid day of month selected");	
		Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid from date");
%>
		<jsp:forward page="DslDateSelect.jsp"/>;
<%
		return;
        }
	calTemp.set(Integer.parseInt(strEndYr),  Integer.parseInt(strEndMth) - 1,  1, 0, 0, 0);
        iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (Integer.parseInt(strEndDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("dslstat", "'To Date' - invalid day of month selected");	
		Log.write(Log.DEBUG_VERBOSE, "Dsl Invalid to date");
%>
		<jsp:forward page="DslDateSelect.jsp"/>;
<%
		return;
        }
	DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
	Log.write(Log.DEBUG_VERBOSE, ">>>>Dsl rpt User: " + sdm.getUser() + " Range:" + strStartDate + " to " + strEndDate + " at " + dFmt.format(new java.util.Date()) );

	con = DatabaseManager.getConnection();
	Statement stmt2 = con.createStatement();

	String strQuery1 = "SELECT CMPNY_SQNC_NMBR, CMPNY_NM " +
		" FROM COMPANY_T WHERE CMPNY_TYP = 'D' " +
		" ORDER BY CMPNY_NM";

	stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(strQuery1);
%>

<br><center>
<SPAN CLASS="header1"> D&nbsp;S&nbsp;L&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T</SPAN>
<br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%></b><br>
Effective:&nbsp;<%= dFmt.format(new java.util.Date()) %><br></center><br>
<table border=1 align=center cellspacing=0 cellpadding=1>
<tr>
	<th align=center>Company</th>
	<th align=center>&nbsp;Total<br>Submitted&nbsp;</th>
	<th align=center>&nbsp;Total<br>Completed&nbsp;</th>
	<th align=center>&nbsp;Completed<br>Interval&nbsp;</th>
</tr>
<%
	long 	lPrevDslSeqNo = 0;
	long 	lDslSeqNo = 0;
	long	lIntervalAverage = 0;
	long    lDay = 0;
	long    lHour = 0;
	long    lMin = 0;
	long    lSec = 0;
        int     iTotalCompleted = 0;
        int     iTotalSubmitted = 0;
	while(rs.next()==true) 
	{
		iCmpnyCount++;
		String strCmpnyNm = rs.getString("CMPNY_NM");
		String strCmpnySqncNmbr = rs.getString("CMPNY_SQNC_NMBR");
		int iCompleted = 0;
		int iSubmitted = 0;
		long lIntervalAccumulation = 0;      //this is total seconds
                long lInterval = 0;
		String strIntervalEndDTS = "";
		String strIntervalBeginDTS = "";

		Log.write(Log.DEBUG_VERBOSE, ">>>>Dsl rpt: company=" + strCmpnySqncNmbr);
		// Start building stats for this Company
		String strCmpnyQuery =
		  "SELECT DH.DSL_SQNC_NMBR, DH.DSL_STTS_CD_IN, " +
		  " TO_CHAR(DH.DSL_HSTRY_DT_IN,'YYYYMMDD HH24MISS') " +
		  " FROM DSL_T D, DSL_HISTORY_T DH " +
		  " WHERE D.CMPNY_SQNC_NMBR = " + strCmpnySqncNmbr +
		  " AND D.DSL_SQNC_NMBR = DH.DSL_SQNC_NMBR " +
		  " AND DH.DSL_STTS_CD_IN <> DH.DSL_STTS_CD_OUT " +
		  " AND EXISTS (SELECT DH2.DSL_SQNC_NMBR FROM DSL_HISTORY_T DH2 " +
		  		" WHERE DH2.DSL_SQNC_NMBR = D.DSL_SQNC_NMBR " +
				" AND DH2.DSL_HSTRY_DT_IN BETWEEN " +
				" TO_DATE('" + strStartDate + " 00:00:00', 'YYYYMMDD HH24:MI:SS') AND " + 
				" TO_DATE('" + strEndDate + " 23:59:59', 'YYYYMMDD HH24:MI:SS') " +
		  		" AND DH2.DSL_STTS_CD_IN in ('SUBMITTED','COMPLETED') ) " +
		  " AND DH.DSL_STTS_CD_IN in ('SUBMITTED','COMPLETED') " +
		  " ORDER BY DH.DSL_SQNC_NMBR, DSL_STTS_CD_IN";

		ResultSet rs2 = stmt2.executeQuery(strCmpnyQuery);

		String strDslSqncNmbr = "";
		String strPrevDslSqncNmbr = "";

		boolean bCompleted = false;

		while(rs2.next()==true)
		{
			String strStatus = rs2.getString("DSL_STTS_CD_IN");
			strDslSqncNmbr = rs2.getString("DSL_SQNC_NMBR");

			if ( strStatus.equals("COMPLETED") )
			{
				Log.write(Log.DEBUG_VERBOSE, "cmplt " + strDslSqncNmbr);
				iCompleted++;
				bCompleted = true;
				strIntervalEndDTS = rs2.getString(3);
				lPrevDslSeqNo = rs2.getInt("DSL_SQNC_NMBR");
			}
			if ( strStatus.equals("SUBMITTED") )
			{
				if ((bCompleted == false) && !strDslSqncNmbr.equals(strPrevDslSqncNmbr))
				{
					iSubmitted++;
					Log.write(Log.DEBUG_VERBOSE, "subm " + strDslSqncNmbr);
				}
				else
				{
					lDslSeqNo = rs2.getInt("DSL_SQNC_NMBR");
					if (lDslSeqNo == lPrevDslSeqNo)
					{
						strIntervalBeginDTS = rs2.getString(3);
						//Calculate Interval
						strIntervalBeginDTS = SLATools.getSLAStartDateTime(strIntervalBeginDTS.substring(0,8), strIntervalBeginDTS.substring(9,15));
						lInterval = SLATools.calculateSLA(strIntervalBeginDTS, strIntervalEndDTS);
						lIntervalAccumulation = lIntervalAccumulation + lInterval;
						Log.write(Log.DEBUG_VERBOSE, ">>Interval for request " + lDslSeqNo + " = " + lInterval + " seconds");
					}
					iSubmitted++;
					Log.write(Log.DEBUG_VERBOSE, "subm " + strDslSqncNmbr);
					bCompleted = false;
				}
			}

			strPrevDslSqncNmbr = strDslSqncNmbr;
                }   //while()
		rs2.close();
		rs2 = null;

                iTotalCompleted += iCompleted;
                iTotalSubmitted += iSubmitted;
		
%>
	        <tr>

       		<td><%=strCmpnyNm%></td>
               	<td align=right><%=iSubmitted%></td>
               	<td align=right><%=iCompleted%></td>

               	<td align=right>
<%		if (iCompleted > 0) 
		{
			lIntervalAverage = lIntervalAccumulation/iCompleted;

                       	//put in xd xh xm xs format
                       	lDay = lIntervalAverage / DAY_IN_SEC;
                       	lIntervalAverage %= DAY_IN_SEC;
                       	lHour = lIntervalAverage / HOUR_IN_SEC;
                       	lIntervalAverage %= HOUR_IN_SEC;
                       	lMin = lIntervalAverage / MIN_IN_SEC;
		        lIntervalAverage %= MIN_IN_SEC;
			lSec = lIntervalAverage;
%>
			&nbsp;<%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;<%=lSec%>s&nbsp;</td>
<%
		}
               	else 	
               	{
%>
			&nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;0s&nbsp;</td>
<%	
		}
		lIntervalTotals += lIntervalAccumulation;
	} //while()

	if (iTotalCompleted > 0) 
	{	lIntervalAverage =  lIntervalTotals/iTotalCompleted;
	}
	else	
        {	lIntervalAverage=0;
        }

	lDay = lIntervalAverage / DAY_IN_SEC;
	lIntervalAverage %= DAY_IN_SEC;
	lHour = lIntervalAverage / HOUR_IN_SEC;
	lIntervalAverage %= HOUR_IN_SEC;
	lMin = lIntervalAverage / MIN_IN_SEC;
	lIntervalAverage %= MIN_IN_SEC;
	lSec = lIntervalAverage;
		
%>

<tr>
<td><b>TOTALS</b></td>
<td align=right><b><%=iTotalSubmitted%></b></td>
<td align=right><b><%=iTotalCompleted%></b></td>
<td align=right><b><%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;<%=lSec%>s</b></td>
<tr>
</table>

<%
	DatabaseManager.releaseConnection(con);
%>

</UL>
<BR>
<BR>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />

</BODY>
</HTML>
