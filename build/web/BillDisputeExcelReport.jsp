<%-- 
    Document   : BillDisputeExcelReport
    Created on : May 24, 2011, 1:11:33 PM
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
            response.setHeader("Content-Disposition", "attachment; filename=" + "BillDisputeReport" + ".xls");
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

	final long DAY_IN_SEC = (long) 86400;
	final long HOUR_IN_SEC = (long) 3600;
	final long MIN_IN_SEC = (long) 60;
	final String SECURITY_OBJECT = "PROV_REPORTS";

	Connection con = null;
	Statement  stmt = null;
	int     iCmpnyCount = 0;
	long    lIntervalTotals = 0;


	

	// Did they cancel?
	if (alltelRequest.getParameter("SUBMITBUTTON").equals("Cancel"))
	{
%>
                <jsp:forward page="Reports.jsp"/>;
<%
		return;
	}

try {
	String strStartYr = alltelRequest.getParameter("from_due_date_yr");
	String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
	String strStartDay = alltelRequest.getParameter("from_due_date_dy");
	if ((strStartYr.length() == 0) || (strStartMth.length()==0) || (strStartDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("dslstat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "Dispute Invalid from date");
%>
		<jsp:forward page="BillDisputeDateSelect.jsp"/>;
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
		Log.write(Log.DEBUG_VERBOSE, "Dispute Invalid to date");
%>
		<jsp:forward page="BillDisputeDateSelect.jsp"/>;
<%
		return;
	}
	String strEndDate = strEndYr + strEndMth + strEndDay;
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("dslstat", "'From Date' must be less than or equal to 'To Date'!");
		Log.write(Log.DEBUG_VERBOSE, "Dispute Invalid to date");
%>
		<jsp:forward page="BillDisputeDateSelect.jsp"/>;
<%
		return;
	}
        //Check days of month and adjust if necessary ...
	Calendar calTemp = Calendar.getInstance();
	calTemp.set(Integer.parseInt(strStartYr),  Integer.parseInt(strStartMth) - 1,  1, 0, 0, 0);
        int iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (Integer.parseInt(strStartDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("dslstat", "'From Date' - invalid day of month selected");
		Log.write(Log.DEBUG_VERBOSE, "Dispute Invalid from date");
%>
		<jsp:forward page="BillDisputeDateSelect.jsp"/>;
<%
		return;
        }
	calTemp.set(Integer.parseInt(strEndYr),  Integer.parseInt(strEndMth) - 1,  1, 0, 0, 0);
        iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (Integer.parseInt(strEndDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("dslstat", "'To Date' - invalid day of month selected");
		Log.write(Log.DEBUG_VERBOSE, "Dispute Invalid to date");
%>
		<jsp:forward page="BillDisputeDateSelect.jsp"/>;
<%
		return;
        }

        String strCmp = "";
	String[] strCmps = alltelRequest.getAttributeValue("COMPANY_SEL");
//        <option value=ALL SELECTED>--Report on all Companies--
        if (strCmps!= null)
        {       for (int i=0;i<strCmps.length;i++)
                {       if (strCmps[i].equals("ALL"))
                        {       strCmp="";
                                break;
                        }
                        else
                        {       if(strCmp.length()>0)
				{	strCmp += ",";				}
				else
				{	strCmp = "  U.CMPNY_SQNC_NMBR IN (";	}
                                strCmp += strCmps[i];
                        }
                }
        }
	if (strCmp.length()>0) strCmp += " ) AND  ";

	DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
 	NumberFormat moneyFmt = NumberFormat.getCurrencyInstance(java.util.Locale.US);

	con = DatabaseManager.getConnection();
	Statement stmt2 = con.createStatement();

	//Only report on companies that have the ability to create Disputes
	String strQuery1 = "SELECT DISTINCT C.CMPNY_SQNC_NMBR, C.CMPNY_NM" +
		" FROM USERID_T U, COMPANY_T C, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA " +
		" WHERE  " + strCmp +
		" U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND UGA.USERID=U.USERID  " +
		" AND SGA.SCRTY_GRP_CD=UGA.SCRTY_GRP_CD AND SGA.SCRTY_OBJCT_CD='CREATE_DISPUTES' " +
		 "  ORDER BY C.CMPNY_SQNC_NMBR ";
//	Log.write(Log.DEBUG_VERBOSE, "query1 = [" + strQuery1 + "]");
	stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(strQuery1);
%>

<br><center>
<SPAN CLASS="header1"> B&nbsp;i&nbsp;l&nbsp;l&nbsp;i&nbsp;n&nbsp;g&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;i&nbsp;s&nbsp;p&nbsp;u&nbsp;t&nbsp;e&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T</SPAN>
<br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartDay%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndDay%>/<%=strEndYr%></b><br>
Effective:&nbsp;<%= dFmt.format(new java.util.Date()) %><br></center><br>
<table border=1 align=center cellspacing=0 cellpadding=1>
<tr>
	<th align=center>Company</th>
	<th align=center>&nbsp;Total<br>Disputes<br>&nbsp;Submitted&nbsp;</th>
	<th align=center>&nbsp;Total&nbsp;Detail<br>Items<br>&nbsp;Submitted&nbsp;</th>
	<th align=center>&nbsp;Amount<br>&nbsp;Disputed&nbsp;</th>
	<th align=center>&nbsp;Total&nbsp;Disputes<br>Resolved&nbsp;</th>
	<th align=center>&nbsp;Total&nbsp;Detail<br>&nbsp;Items&nbsp;Resolved&nbsp;</th>
	<th align=center>&nbsp;Amount<br>&nbsp;Credited&nbsp;</th>
	<th align=center>&nbsp;Avg<br>Completion<br>Interval&nbsp;</th>
</tr>
<%
	long 	lPrevDisputeSeqNo = 0;
	long 	lDisputeSeqNo = 0;
	long	lIntervalAverage = 0;
	long    lDay = 0;
	long    lHour = 0;
	long    lMin = 0;
	long    lSec = 0;
	float	fTotalComplaintAmount = 0;
	float	fTotalCreditedAmount = 0;
        int     iTotalCompleted = 0;
        int     iTotalSubmitted = 0;
        long	lTotalCompletedLineItems = 0;
	long	lTotalSubmittedLineItems = 0;

	PreparedStatement pStmt1 = con.prepareStatement("SELECT COUNT(*) FROM DSPT_DETAIL_T D " +
		" WHERE D.DSPT_SQNC_NMBR=? AND D.DSPT_VRSN=? AND LENGTH(D.DSPT_AMNT) > 0 ");
	PreparedStatement pStmt2 = con.prepareStatement("SELECT COUNT(*) FROM DSPT_RSPNS_DETAIL_T DR " +
		" WHERE DR.DSPT_SQNC_NMBR=? AND DR.DSPT_VRSN=? AND LENGTH(DR.DSPT_ADJSTD_AMNT) > 0 ");
	ResultSet rs3 = null;

	while(rs.next()==true)
	{
		iCmpnyCount++;
		String	strCmpnyNm = rs.getString("CMPNY_NM");
		String	strCmpnySqncNmbr = rs.getString("CMPNY_SQNC_NMBR");
		int 	iCompleted = 0;
		long 	lCompletedLineItems = 0;
		int 	iSubmitted = 0;
		long 	lSubmittedLineItems = 0;
		float	fComplaintAmount = 0;
		float	fCreditedAmount = 0;
		long	lIntervalAccumulation = 0;      //this is total seconds
                long	lInterval = 0;
		String	strIntervalEndDTS = "";
		String	strIntervalBeginDTS = "";

		// Start building stats for this OCN-State
		String strCmpnyQuery =
		  "SELECT DH.DSPT_SQNC_NMBR, DH.DSPT_VRSN, DH.STTS_CD_IN, " +
		  " TO_CHAR(DH.HSTRY_DT_IN,'YYYYMMDD HH24MISS'), D.TTL_DSPTD_AMNT, D.TTL_CRDTTD_AMNT " +
		  " FROM DISPUTE_T D, DISPUTE_HISTORY_T DH " +
		  " WHERE D.CMPNY_SQNC_NMBR = " + strCmpnySqncNmbr +
		  " AND D.DSPT_SQNC_NMBR = DH.DSPT_SQNC_NMBR " +
		  " AND DH.STTS_CD_IN <> DH.STTS_CD_OUT " +
		  " AND EXISTS (SELECT DH2.DSPT_SQNC_NMBR FROM DISPUTE_HISTORY_T DH2 " +
		  		" WHERE DH2.DSPT_SQNC_NMBR = D.DSPT_SQNC_NMBR " +
				" AND DH2.HSTRY_DT_IN BETWEEN " +
				" TO_DATE('" + strStartDate + " 00:00:00', 'YYYYMMDD HH24:MI:SS') AND " +
				" TO_DATE('" + strEndDate + " 23:59:59', 'YYYYMMDD HH24:MI:SS') ) " +
		  " AND DH.STTS_CD_IN in ('SUBMITTED','RESOLVED') " +
		  " ORDER BY DH.DSPT_SQNC_NMBR, DH.HSTRY_DT_IN DESC ";
//		Log.write(Log.DEBUG_VERBOSE, "hist query=["+strCmpnyQuery+"]");
		ResultSet rs2 = stmt2.executeQuery(strCmpnyQuery);

		String strDisputeSqncNmbr = "";
		String strPrevDisputeSqncNmbr = "";
		int iVersion=0;
		boolean bCompleted = false;

		while(rs2.next()==true)
		{
			String strStatus = rs2.getString("STTS_CD_IN");
			strDisputeSqncNmbr = rs2.getString("DSPT_SQNC_NMBR");
			iVersion = rs2.getInt("DSPT_VRSN");

			if ( strStatus.equals("RESOLVED") )
			{
				Log.write(Log.DEBUG_VERBOSE, "Resolved++ request " + strDisputeSqncNmbr);
				iCompleted++;
				bCompleted = true;
				strIntervalEndDTS = rs2.getString(4);
				lPrevDisputeSeqNo = rs2.getInt("DSPT_SQNC_NMBR");
				fCreditedAmount += rs2.getFloat("TTL_CRDTTD_AMNT");
				pStmt2.setLong(1, lPrevDisputeSeqNo);
				pStmt2.setInt(2, iVersion);
				rs3 = pStmt2.executeQuery();
				rs3.next();
				lCompletedLineItems += rs3.getInt(1);
				rs3.close();
				rs3=null;
			}
			if ( strStatus.equals("SUBMITTED") )
			{
				if ((bCompleted == false) && !strDisputeSqncNmbr.equals(strPrevDisputeSqncNmbr))
				{
				Log.write(Log.DEBUG_VERBOSE, "Submitted++ for non-resolved request " + strDisputeSqncNmbr);
					iSubmitted++;
					pStmt1.setLong(1, lPrevDisputeSeqNo);
					pStmt1.setInt(2, iVersion);
					rs3 = pStmt1.executeQuery();
					rs3.next();
					lSubmittedLineItems += rs3.getInt(1);
					rs3.close();
					rs3=null;
				}
				else if (bCompleted == true)
				{
					lDisputeSeqNo = rs2.getInt("DSPT_SQNC_NMBR");
					fComplaintAmount += rs2.getFloat("TTL_DSPTD_AMNT");
					if (lDisputeSeqNo == lPrevDisputeSeqNo)
					{
						iSubmitted++;
						strIntervalBeginDTS = rs2.getString(4);
						//Calculate Interval
						strIntervalBeginDTS = SLATools.getSLAStartDateTime(strIntervalBeginDTS.substring(0,8), strIntervalBeginDTS.substring(9,15));
Log.write(Log.DEBUG_VERBOSE, "Submitted++ for resolved request " + strDisputeSqncNmbr+" "+strIntervalBeginDTS+" - " +strIntervalEndDTS);
						lInterval = SLATools.calculateSLA(strIntervalBeginDTS, strIntervalEndDTS);
						lIntervalAccumulation = lIntervalAccumulation + lInterval;
						Log.write(Log.DEBUG_VERBOSE, ">>Interval for request " + lDisputeSeqNo + " = " + lInterval + " seconds");
						pStmt1.setLong(1, lPrevDisputeSeqNo);
						pStmt1.setInt(2, iVersion);
						rs3 = pStmt1.executeQuery();
						rs3.next();
						lSubmittedLineItems += rs3.getInt(1);
						rs3.close();
						rs3=null;
					}
					bCompleted = false;
				}
			}

			strPrevDisputeSqncNmbr = strDisputeSqncNmbr;
                }   //while()
		rs2.close();
		rs2 = null;

                iTotalCompleted += iCompleted;
                iTotalSubmitted += iSubmitted;
                lTotalCompletedLineItems += lCompletedLineItems;
                lTotalSubmittedLineItems += lSubmittedLineItems;
		fTotalCreditedAmount += fCreditedAmount;
		fTotalComplaintAmount += fComplaintAmount;
%>
	        <tr>

       		<td><%=strCmpnyNm%></td>
               	<td align=right><%=iSubmitted%></td>
               	<td align=right><%=lSubmittedLineItems%></td>
               	<td align=right><%=moneyFmt.format(fComplaintAmount)%></td>
               	<td align=right><%=iCompleted%></td>
               	<td align=right><%=lCompletedLineItems%></td>
               	<td align=right><%=moneyFmt.format(fCreditedAmount)%></td>

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

	try {
		pStmt1.close();
		pStmt2.close();
		pStmt1=null;
		pStmt2=null;
	}
	catch (Exception eeee) {};
%>

<tr>
<td><b>TOTALS</b></td>
<td align=right><b><%=iTotalSubmitted%></b></td>
<td align=right><b><%=lTotalSubmittedLineItems%></b></td>
<td align=right><%=moneyFmt.format(fTotalComplaintAmount)%></td>
<td align=right><b><%=iTotalCompleted%></b></td>
<td align=right><b><%=lTotalCompletedLineItems%></b></td>
<td align=right><%=moneyFmt.format(fTotalCreditedAmount)%></td>
<td align=right><b><%=lDay%>d&nbsp;<%=lHour%>h&nbsp;<%=lMin%>m&nbsp;<%=lSec%>s</b></td>
<tr>
</table>

<%

}//main try
catch (Exception e) {
	Log.write(Log.ERROR, "BillDisputeReport.jsp e=["+e+"]");
}
finally {
	DatabaseManager.releaseConnection(con);
}

%>

</UL>
<BR>
<BR>
</FORM>

</BODY>
</HTML>
