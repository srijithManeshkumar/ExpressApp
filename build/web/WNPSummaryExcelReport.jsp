<%-- 
    Document   : WNPSummaryExcelReport
    Created on : May 24, 2011, 3:15:17 PM
    Author     : satish.t
--%>

<%@ page import ="java.util.*, com.alltel.lsr.common.batch.WNPReportInfo" %>

<%@ page language="java"
         import = "java.util.*, java.text.*,
		  java.sql.*,
		  javax.sql.*,
		  com.alltel.lsr.common.objects.*,
		  com.alltel.lsr.common.util.*"
         %>

<%
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + "WNPSummaryReport" + ".xls");
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


	
	// Did they cancel?
	if (alltelRequest.getParameter("SUBMITBUTTON").equals("Cancel"))
	{
%>
                <jsp:forward page="Reports.jsp"/>;
<%		return;
	}

	Connection con = null;
	Statement  stmt = null;
	ResultSet rs = null;
	long    lIntervalTotals = 0;
	boolean	bSpecificState = false;
	boolean	bSpecificOcn = false;
	boolean bSpecificOrder = false;
	Hashtable m_hashOCNs;
	Vector m_vSortedOCNs = new Vector();	//use this to retreive hash in same ascending order every time
	DateFormat dateFmt = new SimpleDateFormat("MMM yyyy");

	String strStartYr = alltelRequest.getParameter("from_due_date_yr");
	String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
	String strStartDay = alltelRequest.getParameter("from_due_date_dy");
	strStartDay="01";
	if ((strStartYr.length() == 0) || (strStartMth.length()==0) || (strStartDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "WNP Rpt Invalid from date");
%>
		<jsp:forward page="MonthStateSelect.jsp"/>;
<%		return;
	}
	String strStartDate = strStartYr + strStartMth + strStartDay;

	String strEndYr = alltelRequest.getParameter("to_due_date_yr");
	String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
	String strEndDay = alltelRequest.getParameter("to_due_date_dy");
	strEndDay = "02";
	if ((strEndYr.length() == 0) || (strEndMth.length()==0) || (strEndDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("reportstat", "INVALID Date entered!");
		Log.write(Log.DEBUG_VERBOSE, "WNP Summary Rpt Invalid to date");
%>
		<jsp:forward page="MonthStateSelect.jsp"/>;
<%		return;
	}
	String strEndDate = strEndYr + strEndMth + strEndDay;
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' must be less than or equal to 'To Date'!");
		Log.write(Log.DEBUG_VERBOSE, "WNP Summary Rpt Invalid to date");
%>
		<jsp:forward page="MonthStateSelect.jsp"/>;
<%		return;
	}
        //Check days of month and adjust if necessary ...
	Calendar calStart = Calendar.getInstance();
	calStart.set(Integer.parseInt(strStartYr),  Integer.parseInt(strStartMth) - 1,  1, 0, 0, 0);
        int iMaxDays = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (Integer.parseInt(strStartDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("reportstat", "'From Date' - invalid day of month selected");
		Log.write(Log.DEBUG_VERBOSE, "WNP SUmmary Rpt Invalid from date");
%>
		<jsp:forward page="MonthStateSelect.jsp"/>;
<%		return;
        }
	calStart.set(Calendar.DAY_OF_MONTH, Integer.parseInt(strStartDay) );
	Calendar calStartSave = Calendar.getInstance();
	calStartSave = (Calendar)calStart.clone();

	Calendar calEnd = Calendar.getInstance();
	calEnd.set(Integer.parseInt(strEndYr),  Integer.parseInt(strEndMth) - 1,  1, 0, 0, 0);
        iMaxDays = calEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (Integer.parseInt(strEndDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("reportstat", "'To Date' - invalid day of month selected");
		Log.write(Log.DEBUG_VERBOSE, "Wnp Summary Rpt Invalid to date");
%>
		<jsp:forward page="MonthStateSelect.jsp"/>;
<%		return;
        }
	calEnd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(strEndDay) );
	calEnd.set(Calendar.HOUR_OF_DAY, 23);

	DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
	DateFormat dowFmt = new SimpleDateFormat("MM/dd - EE");

	String strState = "";
	String strStateWhere = "";
	String[] strStates = alltelRequest.getAttributeValue("state_list");
	if (strStates != null)
	{	for (int i=0;i<strStates.length;i++)
		{	if (strStates[i].equals("all_states"))
			{	strState ="ALL";
				break;
			}
			else
			{	bSpecificState = true;
				if(strState.length()>0)  strState += ",";
				strState += "'"+strStates[i]+"'";
			}
		}
	}
	Log.write(Log.DEBUG_VERBOSE, "strState=["+ strState + "]");
	if (bSpecificState)
	{	strStateWhere = " AND R.OCN_STT IN (" + strState + ") ";
	}

	String strOCN = "";
	String[] strOCNs = alltelRequest.getAttributeValue("ocn_list");
	if (strOCNs != null)
	{	for (int i=0;i<strOCNs.length;i++)
		{	if (strOCNs[i].equals("all_ocns"))
			{	strOCN ="ALL";
				break;
			}
			else
			{	if(strOCN.length()>0)  strOCN += ",";
				strOCN += "'"+strOCNs[i]+"'";
			}
		}
	}
	Log.write(Log.DEBUG_VERBOSE, "strOCN=["+ strOCN + "]");

	String strOrderFlag = alltelRequest.getParameter("orderFlag");
            String strOrderFlagWhere = "";

            Log.write(Log.DEBUG_VERBOSE, "strOrderFlag=[" + strOrderFlag + "]");

            if (strOrderFlag.equals("ALL")) {
                strOrderFlag = "ALL";
            } else {
                bSpecificOrder = true;
            }
            if (bSpecificOrder) {
                strOrderFlagWhere = " AND R.ICARE='" + strOrderFlag + "'";
            }

	String strOCNWhere = "";
	String strOCNQuery= "";
try {
	con = DatabaseManager.getConnection();
	if (strOCN.equals("ALL"))
	{
		strOCNWhere = "";
		//This will give us the complete list of OCNs that had these types of WNP Orders
	//	strOCNQuery = "SELECT DISTINCT R.OCN_CD, O.OCN_NM FROM REQUEST_T R,  LR_T L, OCN_T O " +
	//		" WHERE R.SRVC_TYP_CD='C' AND R.RQST_STTS_CD != 'INITIAL' AND L.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND L.RQST_VRSN=R.RQST_VRSN " +
	//		" AND L.LR_TYPE1 IN ('N','Y') AND R.OCN_CD = O.OCN_CD AND EXISTS " +
	//		"	(SELECT * FROM REQUEST_HISTORY_T RH WHERE RH.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND RH.RQST_VRSN=R.RQST_VRSN " +
	//		"	 AND RH.RQST_STTS_CD_IN='COMPLETED') ORDER BY R.OCN_CD ";
	// EXCLUDE TSI from report...they'll get reprted under owner of OCN (probably the wireless carrier)
		strOCNQuery = "SELECT DISTINCT R.OCN_CD, OO.OCN_NM FROM REQUEST_T R,  LR_T L, OCN_T O, OCN_T OO " +
			" WHERE R.SRVC_TYP_CD='C' AND R.RQST_STTS_CD != 'INITIAL' AND L.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND L.RQST_VRSN=R.RQST_VRSN " +
			" AND L.LR_TYPE1 IN ('N','Y') AND R.OCN_CD = O.OCN_CD AND O.OCN_CD=OO.OCN_CD and OO.OCN_NM NOT LIKE 'TSI %' AND EXISTS " +
			"	(SELECT * FROM REQUEST_HISTORY_T RH WHERE RH.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND RH.RQST_VRSN=R.RQST_VRSN " +
			"	 AND RH.RQST_STTS_CD_IN='COMPLETED') ORDER BY R.OCN_CD ";

	}
	else
	{
		bSpecificOcn = true;
		//Only build list of selected OCN(s)
		strOCNWhere = " AND R.OCN_CD IN (" + strOCN +  ") ";
		strOCNQuery = " SELECT DISTINCT O.OCN_CD, O.OCN_NM FROM OCN_T O WHERE O.OCN_CD IN (" + strOCN +  ") ORDER BY O.OCN_CD ";
	}
	Log.write(Log.DEBUG_VERBOSE, "strOCNQuery=[" + strOCNQuery +  "]");
	Log.write(Log.DEBUG_VERBOSE, "strOCNWhere=[" + strOCNWhere +  "]");

	m_hashOCNs = new Hashtable();

	stmt = con.createStatement();
	rs = stmt.executeQuery(strOCNQuery);
	String strTemp;
	String prevOCN=null;
	//Load hash table with possible OCNs
	while (rs.next())
	{
		strTemp = rs.getString(1);
		if (!strTemp.equals(prevOCN) || (prevOCN==null))
		{
			WNPReportInfo objURI = new WNPReportInfo( strTemp, rs.getString(2) );
			m_hashOCNs.put(strTemp, objURI);
			m_vSortedOCNs.addElement(strTemp);
			prevOCN = strTemp;
			Log.write(Log.DEBUG_VERBOSE, "Load OCN: " + strTemp + " " + rs.getString(2));
		}
	}

        if (m_hashOCNs.size()<1)
        {       alltelRequest.getHttpRequest().setAttribute("reportstat", "Invalid userid selected. Choose another");
		Log.write(Log.DEBUG_VERBOSE, "WNPRpt: Hash tbl empty ");
%>
		<jsp:forward page="MonthStateSelect.jsp"/>;
<%		return;
        }
%>

<STYLE TYPE="text/css">
.break { page-break-before: always; }
</STYLE>

<br><center>
<SPAN CLASS="header1">W&nbsp;N&nbsp;P&nbsp;&nbsp;S&nbsp;u&nbsp;m&nbsp;m&nbsp;a&nbsp;r&nbsp;y&nbsp;&nbsp;&nbsp;R&nbsp;e&nbsp;p&nbsp;o&nbsp;r&nbsp;t&nbsp;&nbsp;</SPAN><br>
<br><b>Date&nbsp;Range:&nbsp;<%=strStartMth%>/<%=strStartYr%>&nbsp;-&nbsp;<%=strEndMth%>/<%=strEndYr%></b><br>
Effective:&nbsp;<%= dFmt.format(new java.util.Date()) %><br></center><br>

<%
	if (bSpecificState)
	{
%>
<center><b>Specific States: &nbsp;&nbsp;
<%		if (strStates != null)
		{       for (int i=0;i<strStates.length;i++)
			{
%>				<%= strStates[i] %>&nbsp;&nbsp;
<%
			}
		}
%></b></center>
<%	}
%>
<br>
<table border=1 align=center cellspacing=0 cellpadding=1><caption>Wireline to Wireless Ports (Type 1 = N)</caption>
<tr bgcolor="#DBDBDB">
<th align=center colspan=<%= m_vSortedOCNs.size()+2 %>>Wireline to Wireless Ports</th>
<tr bgcolor="#DBDBDB">
	<th align=center>&nbsp;MONTH&nbsp;</th>
<%
	//Spin thru OCNs to create header
	for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
	{
		WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
%>
	<th align=center>&nbsp;<%=objURI.getOCN()%><br><%=objURI.getOCNName()%></th>
<%
	}
%>
	<th align=center>&nbsp;TOTALs&nbsp;</th>
</tr>

<%
	int 	iCount = 0;
	int 	iMthCount = 0;
	boolean bMore = true;

	//Build query string to get our stats
	String strStatsQuery = "SELECT TO_CHAR(the_date,'YYYYMM') AS YYYYMM, OCN_CD,  COUNT(*) AS Tots FROM " +
		" ( SELECT R.OCN_CD, R.OCN_STT, to_date(LSRCM.LSRCM_CD,'MM-DD-YYYY') AS the_date " +
		"   FROM REQUEST_T R, LSRCM_T LSRCM, LR_T L  WHERE R.SRVC_TYP_CD='C' AND R.RQST_STTS_CD != 'INITIAL' " +
		strOCNWhere + strStateWhere + strOrderFlagWhere +
		"   AND LSRCM.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND LSRCM.RQST_VRSN=R.RQST_VRSN AND LSRCM.LSRCM_CD IS NOT NULL  " +
		"   AND L.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND L.RQST_VRSN=R.RQST_VRSN  " +
		"   AND L.LR_TYPE1 = 'N' " +
		"   AND  EXISTS (SELECT * FROM REQUEST_HISTORY_T RH WHERE RH.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND RH.RQST_VRSN=R.RQST_VRSN " +
		"		 AND RH.RQST_STTS_CD_IN='COMPLETED') " +
		" ) GROUP BY TO_CHAR(the_date,'YYYYMM'), OCN_CD  ORDER BY 1, 2 ";


	Log.write("Query=[" + strStatsQuery + "]");
	rs = stmt.executeQuery(strStatsQuery);
	if (!rs.next())
	{	bMore = false;
	} else
	{
		prevOCN = rs.getString("OCN_CD");
	}
	//Get priming date and OCN
	String prevYYYYMM = strStartDate;
	String tempOCN = "";
	String strYYYYMM ="";
	Calendar calTemp = Calendar.getInstance();
	calTemp = (Calendar)calStart.clone();
	int iMth = calStart.get(Calendar.MONTH);
	int iPrevMth = iMth;

	while ( (calStart.before(calEnd)) && bMore)
	{
		strYYYYMM = rs.getString("YYYYMM")+"01";
		calTemp.set(Calendar.YEAR, Integer.parseInt(strYYYYMM.substring(0, 4)));
		calTemp.set(Calendar.MONTH, Integer.parseInt(strYYYYMM.substring(4, 6)) -1 );
//		Log.write(Log.DEBUG_VERBOSE, "calTemp =["+ calTemp.toString() +"]");

		if (strYYYYMM.compareTo(strStartDate) < 0 )
		{	if (!rs.next()) bMore=false;
			Log.write(Log.DEBUG_VERBOSE, "too early, skip");
			continue;
		}
		if ( (strYYYYMM.compareTo(strStartDate) > 0 ) && (calTemp.after(calStart)) )
		{
			while ( (calTemp.after(calStart)) && (calStart.before(calEnd)) )
			{
				Log.write(Log.DEBUG_VERBOSE, "filling in missing mths until calTemp=calStart");
				iMth= calStart.get(Calendar.MONTH);
%>
				<tr><td align=center bgcolor="#DBEAF5">&nbsp;<%= dateFmt.format(calStart.getTime()) %>&nbsp;</td>
<%
				//fill in months
				for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
				{	WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
					iMthCount+= objURI.getMonthlyType1N();
%>
					<td align=center>&nbsp;<%=objURI.getMonthlyType1N()%>&nbsp;</td>
<%				}
%>
				<td bgcolor="#FFFFF0" align=right>&nbsp;<%=iMthCount%>&nbsp;</td></tr>
<%
				iMthCount=0;
				for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
				{
					WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
					objURI.resetMonthlyCounts();
				}
				calStart.add(Calendar.MONTH, 1);
			}
			prevYYYYMM = strYYYYMM;
		}
		if (strYYYYMM.compareTo(prevYYYYMM) > 0 )
		{
			Log.write(Log.DEBUG_VERBOSE, "filling in missing mths");
			//spit out prev month numbers
			iMthCount=0;
%>
			<tr><td align=center bgcolor="#DBEAF5">&nbsp;<%= prevYYYYMM %>&nbsp;</td>
<%
			prevYYYYMM = strYYYYMM;
			for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
			{	WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
				iMthCount+= objURI.getMonthlyType1N();
%>
				<td align=center>&nbsp;<%=objURI.getMonthlyType1N()%>&nbsp;</td>
<%			}
%>
			<td bgcolor="#FFFFF0" align=right>&nbsp;<%=iMthCount%>&nbsp;</td></tr>
<%

			iMthCount=0;
			for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
			{
				WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
				objURI.resetMonthlyCounts();
			}
		}

		if (strYYYYMM.compareTo(strEndDate) > 0)
		{	bMore=false;
			Log.write(Log.DEBUG_VERBOSE, " past date..ending");
			continue;
		}
		tempOCN = rs.getString("OCN_CD");
		iCount = rs.getInt("Tots");
		Log.write(Log.DEBUG_VERBOSE, "rs: "+strYYYYMM+" "+ tempOCN + " " + iCount);

		if (m_hashOCNs.containsKey(tempOCN))
		{	WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get(tempOCN);
			objURI.addType1N(iCount);
			//iMthCount+= iCount;
		}
		else {
			Log.write(Log.ERROR, "WNPSummaryReport: DB synch problem ocn not found = " + tempOCN);
		}
		if (!rs.next()) bMore = false;

	}

	while (calTemp.before(calEnd))
	{
		Log.write(Log.DEBUG_VERBOSE, "filling in remaining mths");
		iMth= calTemp.get(Calendar.MONTH);
%>
		<tr><td align=center bgcolor="#DBEAF5">&nbsp;<%= dateFmt.format(calTemp.getTime()) %>&nbsp;</td>
<%
		//fill in months
		for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
		{	WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
			iMthCount+= objURI.getMonthlyType1N();
%>
			<td align=center>&nbsp;<%=objURI.getMonthlyType1N()%>&nbsp;</td>
<%		}
%>
		<td bgcolor="#FFFFF0" align=right>&nbsp;<%=iMthCount%>&nbsp;</td></tr>
<%
		iMthCount=0;
		for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
		{
			WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
			objURI.resetMonthlyCounts();
		}
		calTemp.add(Calendar.MONTH, 1);
	}
%>

<tr>
<tr  bgcolor="#FFFFF0">
<td align=center  bgcolor="#3366cc"><font color="#ffffff">&nbsp;Totals&nbsp;</font></td>
<%
	for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
	{
		WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
%>
		<td align=center>&nbsp;<%=objURI.getTotalType1N()%>&nbsp;</td>
<%		iMthCount+=objURI.getTotalType1N();
	}
%>
<td align=right>&nbsp;<%=iMthCount%>&nbsp;</td>
</tr>
</table>
<br><br>
<%	//NOW REPEAT for Type1 = Y
	iMthCount=0;
%>

<table border=1 align=center cellspacing=0 cellpadding=1><caption>Type 1 = Y</caption>
<tr bgcolor="#DBDBDB">
<th align=center colspan=<%= m_vSortedOCNs.size()+2 %>>Type 1 Numbers</th>
<tr bgcolor="#DBDBDB">
	<th align=center>&nbsp;MONTH&nbsp;</th>
<%
	//Spin thru OCNs to create header
	for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
	{
		WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
%>
	<th align=center>&nbsp;<%=objURI.getOCN()%><br><%=objURI.getOCNName()%></th>
<%
	}
%>
	<th align=center>&nbsp;TOTALs&nbsp;</th>
</tr>

<%
	bMore = true;
	strStatsQuery = "SELECT TO_CHAR(the_date,'YYYYMM') AS YYYYMM, OCN_CD,  COUNT(*) AS Tots FROM " +
		" ( SELECT R.OCN_CD, R.OCN_STT, to_date(LSRCM.LSRCM_CD,'MM-DD-YYYY') AS the_date " +
		"   FROM REQUEST_T R, LSRCM_T LSRCM, LR_T L  WHERE R.SRVC_TYP_CD='C' AND R.RQST_STTS_CD != 'INITIAL' " +
		strOCNWhere + strStateWhere +
		"   AND LSRCM.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND LSRCM.RQST_VRSN=R.RQST_VRSN AND LSRCM.LSRCM_CD IS NOT NULL  " +
		"   AND L.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND L.RQST_VRSN=R.RQST_VRSN  " +
		"   AND L.LR_TYPE1 = 'Y' " +
		"   AND  EXISTS (SELECT * FROM REQUEST_HISTORY_T RH WHERE RH.RQST_SQNC_NMBR=R.RQST_SQNC_NMBR AND RH.RQST_VRSN=R.RQST_VRSN " +
		"		 AND RH.RQST_STTS_CD_IN='COMPLETED') " +
		" ) GROUP BY TO_CHAR(the_date,'YYYYMM'), OCN_CD  ORDER BY 1, 2 ";

	rs = stmt.executeQuery(strStatsQuery);
	if (!rs.next())
	{	bMore = false;
	} else
	{
		prevOCN = rs.getString("OCN_CD");
	}
	//Get priming date and OCN
	prevYYYYMM = strStartDate;
	tempOCN = "";
	strYYYYMM ="";
	calTemp = (Calendar)calStartSave.clone();
	iMth = calStartSave.get(Calendar.MONTH);
	iPrevMth = iMth;
	calStart = calStartSave;	//returning value

	while ( (calStart.before(calEnd)) && bMore)
	{
		strYYYYMM = rs.getString("YYYYMM")+"01";
		calTemp.set(Calendar.YEAR, Integer.parseInt(strYYYYMM.substring(0, 4)));
		calTemp.set(Calendar.MONTH, Integer.parseInt(strYYYYMM.substring(4, 6)) -1 );
//		Log.write(Log.DEBUG_VERBOSE, "calTemp =["+ calTemp.toString() +"]");

		if (strYYYYMM.compareTo(strStartDate) < 0 )
		{	if (!rs.next()) bMore=false;
			Log.write(Log.DEBUG_VERBOSE, "too early, skip");
			continue;
		}
		if ( (strYYYYMM.compareTo(strStartDate) > 0 ) && (calTemp.after(calStart)) )
		{
			while ( (calTemp.after(calStart)) && (calStart.before(calEnd)) )
			{
				Log.write(Log.DEBUG_VERBOSE, "filling in missing mths");
				iMth= calStart.get(Calendar.MONTH);
%>
				<tr><td align=center bgcolor="#DBEAF5">&nbsp;<%= dateFmt.format(calStart.getTime()) %>&nbsp;</td>
<%
				//fill in months
				for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
				{	WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
					iMthCount+= objURI.getMonthlyType1Y();
%>
					<td align=center>&nbsp;<%=objURI.getMonthlyType1Y()%>&nbsp;</td>
<%				}
%>
				<td bgcolor="#FFFFF0" align=right>&nbsp;<%=iMthCount%>&nbsp;</td></tr>
<%
				iMthCount=0;
				for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
				{
					WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
					objURI.resetMonthlyCounts();
				}
				calStart.add(Calendar.MONTH, 1);
			}
			prevYYYYMM = strYYYYMM;
		}
		if (strYYYYMM.compareTo(prevYYYYMM) > 0 )
		{
			Log.write(Log.DEBUG_VERBOSE, "filling in missing mths");
			//spit out prev month numbers
			iMthCount=0;
%>
			<tr><td align=center bgcolor="#DBEAF5">&nbsp;<%= prevYYYYMM %>&nbsp;</td>
<%
			prevYYYYMM = strYYYYMM;
			for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
			{	WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
				iMthCount+= objURI.getMonthlyType1Y();
%>
				<td align=center>&nbsp;<%=objURI.getMonthlyType1Y()%>&nbsp;</td>
<%			}
%>
			<td bgcolor="#FFFFF0" align=right>&nbsp;<%=iMthCount%>&nbsp;</td></tr>
<%

			iMthCount=0;
			for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
			{
				WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
				objURI.resetMonthlyCounts();
			}
		}

		if (strYYYYMM.compareTo(strEndDate) > 0)
		{	bMore=false;
			Log.write(Log.DEBUG_VERBOSE, " past date..ending");
			continue;
		}
		tempOCN = rs.getString("OCN_CD");
		iCount = rs.getInt("Tots");
		Log.write(Log.DEBUG_VERBOSE, "rs: "+strYYYYMM+" "+ tempOCN + " " + iCount);

		if (m_hashOCNs.containsKey(tempOCN))
		{	WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get(tempOCN);
			objURI.addType1Y(iCount);
			//iMthCount+= iCount;
		}
		else {
			Log.write(Log.ERROR, "WNPSummaryReport: DB synch problem ocn not found = " + tempOCN);
		}
		if (!rs.next()) bMore = false;

	}

	while (calTemp.before(calEnd))
	{
		if (calTemp.before(calStart))
		{	calTemp = calStart;
		}
		Log.write(Log.DEBUG_VERBOSE, "filling in remaining mths");
		iMth= calTemp.get(Calendar.MONTH);
%>
		<tr><td align=center bgcolor="#DBEAF5">&nbsp;<%= dateFmt.format(calTemp.getTime()) %>&nbsp;</td>
<%
		//fill in months
		for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
		{	WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
			iMthCount+= objURI.getMonthlyType1Y();
%>
			<td align=center>&nbsp;<%=objURI.getMonthlyType1Y()%>&nbsp;</td>
<%		}
%>
		<td bgcolor="#FFFFF0" align=right>&nbsp;<%=iMthCount%>&nbsp;</td></tr>
<%
		iMthCount=0;
		for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
		{
			WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
			objURI.resetMonthlyCounts();
		}
		calTemp.add(Calendar.MONTH, 1);
	}
%>

<tr>
<tr  bgcolor="#FFFFF0">
<td align=center  bgcolor="#3366cc"><font color="#ffffff">&nbsp;Totals&nbsp;</font></td>
<%
	for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
	{
		WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
%>
		<td align=center>&nbsp;<%=objURI.getTotalType1Y()%>&nbsp;</td>
<%		iMthCount+=objURI.getTotalType1Y();
	}
%>
<td align=right>&nbsp;<%=iMthCount%>&nbsp;</td>
</tr>
</table>

<%
	iMthCount=0;
%>

<br><br>
<table border=1 align=center cellspacing=0 cellpadding=1>
<tr bgcolor="#DBDBDB">
<th align=center colspan=<%= m_vSortedOCNs.size()+2 %>>Grand Totals</th>
<tr bgcolor="#DBDBDB">
	<th align=center>&nbsp;MONTH&nbsp;</th>
<%
	//Spin thru OCNs to create header
	for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
	{
		WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
%>
	<th align=center>&nbsp;<%=objURI.getOCN()%><br><%=objURI.getOCNName()%></th>
<%
	}
%>
	<th align=center>&nbsp;TOTALs&nbsp;</th>
</tr>

<tr  bgcolor="#FFFFF0">
<td align=center  bgcolor="#3366cc"><font color="#ffffff">&nbsp;&nbsp;&nbsp;&nbsp;Totals&nbsp;&nbsp;&nbsp;&nbsp;</font></td>
<%
	for (Iterator it = m_vSortedOCNs.iterator(); it.hasNext(); )
	{
		WNPReportInfo objURI = (WNPReportInfo)m_hashOCNs.get((String)it.next());
%>
		<td align=center>&nbsp;<%=objURI.getTotalType1Y() + objURI.getTotalType1N() %>&nbsp;</td>
<%		iMthCount+=objURI.getTotalType1Y() + objURI.getTotalType1N();
	}
%>
<td align=right>&nbsp;<%=iMthCount%>&nbsp;</td>
</tr>
</table>
<%
}
catch(Exception e) {
	Log.write(Log.DEBUG_VERBOSE, "Caught Exception in main block. e=["+e+"]");
}
finally {
	try {	rs.close();
		rs=null;
	} catch (Exception ee) {}
	DatabaseManager.releaseConnection(con);
}

%>

<%!
	private void dumpTotals(String strHdr, String strType)
	{
		Log.write(Log.DEBUG_VERBOSE, "WnpSummaryReport() dumpTotals()");
	}
%>

<BR>
<BR>
</FORM>

</BODY>
</HTML>