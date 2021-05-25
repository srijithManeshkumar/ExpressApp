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
 * MODULE:	VendorReport.jsp	
 * 
 * DESCRIPTION: Vendor reports view. The user selects a report from VendorReportSelection
 *		and they get sent here.  If the user is a provider, they see all rows -
 *		otherwise the user only sees generic rows (COMPANY_SEQ_NMBR = 0) and rows
 *		for their company.
 * 
 * AUTHOR:      Express Development TEam
 * 
 * DATE:        08-30-2002
 * 
 * HISTORY:
 *
 */
%>

<%@ page
        language="java"
        import = "java.util.*, java.text.*,
                  java.sql.*,
                  javax.sql.*,
                  com.alltel.lsr.common.objects.*,
                  com.alltel.lsr.common.util.*"
        session="true"
%>
<%
        final String SECURITY_URL = "LsrSecurity.jsp";
        AlltelRequest alltelRequest = null;
        AlltelResponse alltelResponse = null;
        SessionDataManager sdm = null;
        try
        {
                alltelRequest = new AlltelRequest(request);
                alltelResponse = new AlltelResponse(response);
                sdm = alltelRequest.getSessionDataManager();
                if ( (sdm == null) || (!sdm.isUserLoggedIn()) )
                {
                        alltelResponse.sendRedirect("LoginCtlr");
                        return;
                }
        }
        catch (Exception e)
        {
                Log.write(Log.ERROR, e.getMessage());
                Log.write(Log.ERROR, "Trapped in i_header.jsp");
        }

	final String SECURITY_OBJECT = "VENDOR_REPORTS";
	final String SECURITY_OBJECT2 = "PROV_REPORTS";
	
	Log.write(Log.DEBUG_VERBOSE,"VendorReport()");
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		if (!sdm.isAuthorized(SECURITY_OBJECT2))
		{
			Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
			alltelResponse.sendRedirect(SECURITY_URL);
			return;
		}
	}

	Connection con = null;
	Statement  stmt = null;
	String strCmpTyp = "";
	int iCmp = 0;
	int iRpt = 0;
	int iLineCount = 0;
	String strCmp = "";
	
	String strReportDate = alltelRequest.getParameter("rptdt");
	String strReportId = alltelRequest.getParameter("rptid");
	if ( (strReportId == null) || (strReportId.length() < 1) )
	{
                alltelRequest.getHttpRequest().setAttribute("vendorrptmsg", "INVALID vendor report selection!");
                Log.write(Log.WARNING, "Vendor reports - invalid selection made or " + sdm.getUser() + " trying to get more info...");
%>
                <jsp:forward page="VendorReportSelection.jsp"/>;
<%
                return;
        }
	else {
		iRpt = Integer.parseInt(strReportId);
	}

	if ( (strReportDate == null) || (strReportDate.length() < 1) )
	{
                alltelRequest.getHttpRequest().setAttribute("vendorrptmsg", "INVALID vendor report selection!");
                Log.write(Log.WARNING, "Vendor reports - invalid rpt date or " + sdm.getUser() + " trying to get more info...");
%>
                <jsp:forward page="VendorReportSelection.jsp"/>;
<%
                return;
        }
	Log.write(Log.DEBUG_VERBOSE,"VendorReport: "+sdm.getUser()+" viewing rpt["+iRpt+"] for ["+strReportDate+"]");

	try {	
		con = DatabaseManager.getConnection();
		stmt = con.createStatement();

		//Get users company and double check they have access to report they're requesting. To make it 
		//here, they had to pick a report choice from VendorReportSelection.jsp, where the rptid and
		//rptdt were fixed.

		ResultSet rs = stmt.executeQuery("SELECT CMPNY_TYP, C.CMPNY_SQNC_NMBR, C.CMPNY_NM FROM COMPANY_T C, USERID_T U "+
				" WHERE C.CMPNY_SQNC_NMBR = U.CMPNY_SQNC_NMBR AND USERID = '" + sdm.getUser() + "'");
		if (rs.next())
		{	strCmpTyp = rs.getString(1);
			iCmp = rs.getInt(2);
			strCmp = rs.getString(3);
			Log.write(Log.DEBUG_VERBOSE, "VendorReport: cmp type="+ strCmpTyp + " seq=" +  iCmp);
		}
		else
		{       Log.write(Log.WARNING, "VendorReport: " + sdm.getUser() + " has no valid userid/company type! ");
			alltelResponse.sendRedirect(SECURITY_URL);
			return;
		}
		rs = stmt.executeQuery("SELECT COUNT(*) FROM VENDOR_REPORT_T WHERE RPRT_SQNC_NMBR="+iRpt+" AND " +
				" CMPNY_TYPS LIKE '%"+strCmpTyp+"%'");
		rs.next();
		//Provider user can see all report data !
		if (rs.getInt(1)==0 && !strCmpTyp.equals("P"))
		{	Log.write(Log.WARNING, "VendorReport: " + sdm.getUser() + " does have access to this report !");
			alltelResponse.sendRedirect(SECURITY_URL);
			return;
		}
	
		//Get Heading
		rs = stmt.executeQuery("SELECT RPRT_DSC, TO_CHAR(RPRT_RUN_DT,'MM/DD/YYYY'), " +
			" TO_CHAR(RPRT_BGN_DT,'" +PropertiesManager.getProperty("lsr.historyview.datefmt", "MM-DD-YYYY HH:MI:SS AM") + "')," +
			" TO_CHAR(RPRT_END_DT,'" +PropertiesManager.getProperty("lsr.historyview.datefmt", "MM-DD-YYYY HH:MI:SS AM") + "')," +
			" STYL_SHT " +
			" FROM VENDOR_REPORT_T V, VENDOR_REPORT_ACTVTY_T VA " +
			"WHERE V.RPRT_SQNC_NMBR="+iRpt+" AND VA.RPRT_SQNC_NMBR=V.RPRT_SQNC_NMBR " +
			" AND RPRT_RUN_DT=TO_DATE('"+strReportDate+"','YYYYMMDD') ");
		rs.next();
		String strHeading = rs.getString(1);
		String strDate = rs.getString(2);
		String strBeginDate = rs.getString(3);
		String strEndDate = rs.getString(4);
		String strStyleSheet = rs.getString(5);
		Log.write(Log.DEBUG_VERBOSE, "VendorReport() Heading=["+strHeading+"] Date=["+strDate+"] StyleSheet =["+strStyleSheet+"]");

		//Get report contents
		String strQuery = "SELECT DTL_LN FROM VENDOR_REPORT_DETAIL_T WHERE RPRT_SQNC_NMBR="+iRpt+
				  " AND RPRT_RUN_DT=TO_DATE('" + strReportDate + "','YYYYMMDD')  ";
		if (!strCmpTyp.equals("P"))	//limit users if not Provider
		{	strQuery += "  AND (CMPNY_SQNC_NMBR=0 OR CMPNY_SQNC_NMBR="+iCmp+ ") ";
		}
		strQuery += " ORDER BY DTL_LN_NMBR ";
		rs = stmt.executeQuery(strQuery);
%>

<html>
<head>
<LINK rel=stylesheet type="text/css" HREF="application.css">
<title><%=strHeading%></title>
<STYLE type="text/css">
	th { background: yellow; }
	caption{ text-align: center; }
</STYLE>
</head>
<body>
<H1><%=strHeading%></H1>
<font size=-1><center>Report date range:<font color=red><b> <%=strBeginDate%></b></font>&nbsp; to &nbsp;<font color=red><b><%=strEndDate%></b></font></center></font><br>
<font size=-1><i><center>Report created on <%=strDate%></center></i></font>
<br><br><font size=+1><b><%=strCmp%></b></font>
<%
		if (strCmpTyp.equals("P"))
		{
%>
			&nbsp;&nbsp;<i>(Provider view - all companies displayed)&nbsp;</i>
<%
		}	
%>
<br>
<vendor-report-<%=iRpt%>>
<table class="prov_reports" align=center border=1 >
<%
		while(rs.next()==true) 
		{
			iLineCount++;
			String strReportLine = rs.getString(1);
			//Log.write("VendorReport() line="+strReportLine);
%>
			<tr class="prov_reports"><%=strReportLine%></tr>
<%
		} //while()
		if (iLineCount<2) //If no data has been reported for a particular OCN, then they'll see this 
		{
%>
			<tr class="prov_reports"><td>No report data available for this OCN</td></tr>
<%
		}

	} //try()
	catch (Exception e) {
		Log.write("VendorReport() Exception caught=["+e+"]");
	}
	finally {
		Log.write("VendorReport() finally() closing connection");
		DatabaseManager.releaseConnection(con);
	}
%>
</table>
<br></br><center>-------------------End-of-Report-----------------</center>
</vendor-report-<%=iRpt%>>
</body>
</html>

