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
 *
 */


/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/SLAReport.jsv  $
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

/* $Revision:   1.5  $
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

	Connection con = null;
	Statement  stmt = null;
	int     iOCNCount = 0;
	int     iCompletedTotals = 0;
	int 	iRejectedTotals = 0;
	int     iWithinFOCTotals = 0;
        int     iPastFOCTotals = 0;
        long 	lSLATotals = 0;
	long    lSLAAverage = 0;        
	
	String strOCNQuery =
		  "SELECT RH.RQST_SQNC_NMBR, RH.RQST_STTS_CD_IN, TO_CHAR(RH.RQST_HSTRY_DT_IN,'YYYYMMDD HH24MISS') " +
		  " FROM REQUEST_T R, REQUEST_HISTORY_T RH WHERE R.OCN_CD = ? AND R.OCN_STT=? AND " +
		  " R.RQST_SQNC_NMBR = RH.RQST_SQNC_NMBR AND RH.RQST_STTS_CD_IN <> RH.RQST_STTS_CD_OUT " +
		  " AND EXISTS (SELECT RH2.RQST_SQNC_NMBR FROM REQUEST_HISTORY_T RH2 " +
		  		" WHERE RH2.RQST_SQNC_NMBR = R.RQST_SQNC_NMBR " +
		  		" AND RH2.RQST_STTS_CD_IN IN ('FOC', 'REJECTED') " +
				" AND RH2.RQST_HSTRY_DT_IN BETWEEN " +
				" TO_DATE(?, 'YYYYMMDD HH24:MI:SS') AND " + 
				" TO_DATE(?, 'YYYYMMDD HH24:MI:SS') ) " +
		  " ORDER BY RH.RQST_SQNC_NMBR, RH.RQST_HSTRY_DT_IN DESC";

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
	DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
	Log.write(Log.DEBUG_VERBOSE, "SLA Date:" + strStartDate + " Date:" + strEndDate);

	con = DatabaseManager.getConnection();
	PreparedStatement pstmt = con.prepareStatement(strOCNQuery);

	String strQuery1 = "SELECT O.OCN_CD, OS.STT_CD, O.OCN_NM, OS.OCN_STT_SLA_DYS " +
		" FROM OCN_T O, OCN_STATE_T OS WHERE O.OCN_CD = OS.OCN_CD " +
		" ORDER BY OS.STT_CD, O.OCN_CD ";
	stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(strQuery1);
%>


<br><center>
<SPAN CLASS="header1"> S&nbsp;L&nbsp;A&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN>
<br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%></b><br>
Effective:&nbsp;<%= dFmt.format(new java.util.Date()) %><br></center><br>
<table border=1 align=center cellspacing=0 cellpadding=1>
<tr>
	<th align=center>&nbsp;OCN&nbsp;</th>
	<th align=center>VENDOR</th>
	<th align=center>&nbsp;FOCed&nbsp;</th>
	<th align=center>&nbsp;REJECTED&nbsp;</th>
	<th align=center>&nbsp;TOTAL&nbsp;</th>
	<th align=center>&nbsp;FOC&nbsp;INT&nbsp;</th>
	<th align=center>&nbsp;FOC<br>&nbsp;SLA&nbsp;</th>
	<th align=center>&nbsp;Within<br>&nbsp;FOC&nbsp;SLA&nbsp;</th>
	<th align=center>&nbsp;Past<br>&nbsp;FOC&nbsp;SLA&nbsp;</th>
	<th align=center>&nbsp;%&nbsp;within<br>&nbsp;FOC&nbsp;SLA&nbsp;</th>
</tr>
<%
	long lPrevSeqNmbr = 0;
	long lSeqNmbr = 0;
	long    lDay = 0;
	long    lHour = 0;
	long    lMin = 0;
        int     iTotal = 0;
	DecimalFormat OCNfmt = new DecimalFormat("0000");
	while(rs.next()==true) 
	{
		iOCNCount++;
		int iOCN = rs.getInt("OCN_CD");
		String strSt = rs.getString("STT_CD");
		String strNm = rs.getString("OCN_NM");
		int iSLA = rs.getInt("OCN_STT_SLA_DYS");
                long lSLAInSeconds = iSLA * DAY_IN_SEC;
		int iCompleted = 0;
		int iRejected = 0;
                int iWithinFOC = 0;
                int iPastFOC = 0;
		long lSLAAccumulation = 0;      //this is total seconds
                long lSLA = 0;
		int iState = 0;
		String strSLAEndDTS = "";
		String strSLABeginDTS = "";

		Log.write(Log.DEBUG_VERBOSE, "SLA OCN=" + iOCN + " St:"+strSt+ " Dt1:" +strStartDate+" Dt2:"+strEndDate+"-------");
		// Start building stats for this OCN-State
		pstmt.setInt(1, iOCN);
		pstmt.setString(2, strSt);
		pstmt.setString(3, strStartDate + " 00:00:00");
		pstmt.setString(4, strEndDate + " 23:59:59");
		ResultSet rs2 = pstmt.executeQuery();
		while(rs2.next()==true)
		{
			String strStatus = rs2.getString("RQST_STTS_CD_IN");
			Log.write(Log.DEBUG_VERBOSE, "SLA REQ: " + rs2.getInt("RQST_SQNC_NMBR") + "  STATUS=" + strStatus);
			if ( strStatus.equals("FOC") || strStatus.equals("REJECTED") )
			{
				strSLAEndDTS = rs2.getString(3);
				iState = 1;
				lPrevSeqNmbr = rs2.getInt("RQST_SQNC_NMBR");
				if ( strStatus.equals("FOC") )
				{	iCompleted++;
        Log.write(Log.DEBUG_VERBOSE, "++FOC Count : " + lPrevSeqNmbr);
				}
				if ( strStatus.equals("REJECTED") )
				{	iRejected++;
        Log.write(Log.DEBUG_VERBOSE, "++Reject Count : " + lPrevSeqNmbr);
				}
			}
			if ( strStatus.equals("SUBMITTED") && iState == 1 )
			{
				lSeqNmbr = rs2.getInt("RQST_SQNC_NMBR");
				if (lSeqNmbr == lPrevSeqNmbr)
				{
					strSLABeginDTS = rs2.getString(3);
					iState = 0;
					//Calculate SLA
					String strSLA = SLATools.getSLAStartDateTime(strSLABeginDTS.substring(0,8), strSLABeginDTS.substring(9,15));
					strSLABeginDTS = strSLA;
					lSLA = SLATools.calculateSLA(strSLABeginDTS, strSLAEndDTS);
					lSLAAccumulation = lSLAAccumulation + lSLA;
					Log.write(Log.DEBUG_VERBOSE, ">>SLA for request " + lSeqNmbr + " = " + lSLA + " seconds");
					//Log.write(Log.DEBUG_VERBOSE, "SLA running total:" + lSLAAccumulation);
                                        if (lSLA <= lSLAInSeconds) {
                                            iWithinFOC++; }
                                        else {
                                            iPastFOC++;
                                         }
				}
				else
				{	iState = 0;	//this should never happen
				}
			}
                }   //while()
                iTotal = iCompleted + iRejected;
%>
                <tr>
                <td><%=OCNfmt.format(iOCN)%>-<%=strSt%></td>
                <td><%=strNm%></td>
                <td align=right><%=iCompleted%></td>
                <td align=right><%=iRejected%></td>
                <td align=right><%=iTotal%></td>
                <td align=right>
<%
		if (iTotal > 0) 
		{
			lSLAAverage = lSLAAccumulation/iTotal;
			Log.write(Log.DEBUG, "SLAReport: SLA Average= " + lSLAAccumulation + "/" + iTotal +  " = " + lSLAAverage);
                        //put in xd xh xm format
                        lDay = lSLAAverage / DAY_IN_SEC;
                        lSLAAverage %= DAY_IN_SEC;
                        lHour = lSLAAverage / HOUR_IN_SEC;
                        lSLAAverage %= HOUR_IN_SEC;
                        lMin = lSLAAverage / MIN_IN_SEC;
%>
			&nbsp;<%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;
<%
		}
                else 	
                {
%>
			&nbsp;0d&nbsp;&nbsp;0h&nbsp;&nbsp;0m&nbsp;
<%	
		}
%>
                </td>
        	<td align=center><%=iSLA%></td>
        	<td align=right><%=iWithinFOC%></td>
        	<td align=right><%=iPastFOC%></td>
<%
                if (iTotal > 0)
                {
%>
                    <td align=right>&nbsp;<%= (iWithinFOC*100)/iTotal %>&nbsp;</td>
                   <tr>
<%
                }
                else
                {
%>
                    <td align=right>&nbsp;</td>
                    <tr>
<%
                }
		iCompletedTotals += iCompleted;
		iRejectedTotals += iRejected;
		lSLATotals += lSLAAccumulation;
                iWithinFOCTotals += iWithinFOC;
                iPastFOCTotals += iPastFOC;

	} //while()
	iTotal = iCompletedTotals+iRejectedTotals;
	if (iTotal > 0) 
	{	lSLAAverage =  lSLATotals/iTotal;
	}
	else	
        {	lSLAAverage=0;
        }

	lDay = lSLAAverage / DAY_IN_SEC;
	lSLAAverage %= DAY_IN_SEC;
	lHour = lSLAAverage / HOUR_IN_SEC;
	lSLAAverage %= HOUR_IN_SEC;
	lMin = lSLAAverage / MIN_IN_SEC;
		
%>

<tr>
<td><b>TOTALS</b></td>
<td align=center><b><%=iOCNCount%>&nbsp;VENDORS</b></td>
<td align=right><b><%=iCompletedTotals%></b></td>
<td align=right><b><%=iRejectedTotals%></b></td>
<td align=right><b><%=iTotal%></b></td>
<td align=right><b><%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m</b></td>
<td align=center>&nbsp;</td>
<td align=right><b><%=iWithinFOCTotals%></b></td>
<td align=right><b><%=iPastFOCTotals%></b></td>
<%
        if (iTotal > 0)
        {
%>
        <td align=right><b>&nbsp;<%= (iWithinFOCTotals*100)/iTotal %>&nbsp;</b></td>
<%
        }
        else
        {
%>
        <td align=right>&nbsp;</td>
<%
        }
%>
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
