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
 * MODULE:	COReport.jsp	
 * 
 * DESCRIPTION: HD: 1918479 Please Create a Monthly Report for Rachelle Summers (Data Engineering).   
 *		This report will need to pull the same information as the KPEN& Business Data Products Complete Report.
 *  	The only change is the Status at which the information is pulled will need to be the DE-Complete Status for all our product types.  
 *		This report can be named: KPEN &  Business Data Products DE-Complete Report.. 
 *		1918479 User picks a date range from DCBReportForm.jsp.
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        3/2006
 * 
 * HISTORY: EK 3/2006
 *	
 */
%>

<%-- VALIDATE SECURITY HERE AND SEND USER TO LOGIN PAGE IF NO VALID LOGIN EXISTS FOR SESSION --%>
<%@ include file="ExpressUtil.jsp" %>
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
	LoginProfileBean lpbean = sdm.getLoginProfileBean();
	MenuVector mvec = lpbean.getMenu(0); //0 is main menu
	Vector mivec = mvec.getMenuItemVector();
	MenuItem mitem;

        //Rel 1.1 If any requests are locked then unlock here -since user navigated to a new page.
        //If page is one of the exceptions defined in applications properties file, then don't unlock.
        if ( PropertiesManager.getProperty("lsr.keeplocks.view", "unlockAll").indexOf(alltelRequest.getURLNoBackSlash()) < 0 )
        {  	//need to unlock requests, etc.
		sdm.removeLocks();
        }
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<%@ page contentType = "application/vnd.ms-excel" %>
<% 
	//alltelResponse.setContentType("application/vnd.ms-excel");
	alltelResponse.getHttpResponse().setHeader("Content-Disposition", ("attachment; filename = KPENBDP_PREPORT_" + getCurrentDate_Simpe() + ".xls" ));
	final long DAY_IN_SEC = (long) 86400;
	final long HOUR_IN_SEC = (long) 3600;
	final long MIN_IN_SEC = (long) 60;
	final String SECURITY_OBJECT = "PROV_REPORTS";
	
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		alltelResponse.getHttpResponse().setHeader( "Content_Type","text/html");
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
	int     iOCNCount = 0;
	
	String strStartYr = alltelRequest.getParameter("from_due_date_yr");
	String strStartMth = alltelRequest.getParameter("from_due_date_mnth");
	String strStartDay = alltelRequest.getParameter("from_due_date_dy");
	if ((strStartYr.length() == 0) || (strStartMth.length()==0) || (strStartDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("costat", "INVALID Date entered!");	
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid from date");
%>
		<jsp:forward page="CBReportForm.jsp"/>;
<%
		return;
	}
	String strStartDate = strStartYr + strStartMth + strStartDay;
	
	String strEndYr = alltelRequest.getParameter("to_due_date_yr");
	String strEndMth = alltelRequest.getParameter("to_due_date_mnth");
	String strEndDay = alltelRequest.getParameter("to_due_date_dy");
	if ((strEndYr.length() == 0) || (strEndMth.length()==0) || (strEndDay.length()==0))
	{
		alltelRequest.getHttpRequest().setAttribute("costat", "INVALID Date entered!");	
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid to date");
%>
		<jsp:forward page="CBReportForm.jsp"/>;
<%
		return;
	}
	String strEndDate = strEndYr + strEndMth + strEndDay;
	if ( strStartDate.compareTo(strEndDate) > 0 )
	{
		alltelRequest.getHttpRequest().setAttribute("costat", "'From Date' must be less than or equal to 'To Date'!");	
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid to date");
%>
		<jsp:forward page="CBReportForm.jsp"/>;
<%
		return;
	}
        //Check days of month and adjust if necessary ...
	Calendar calTemp = Calendar.getInstance();
	calTemp.set(Integer.parseInt(strStartYr),  Integer.parseInt(strStartMth) - 1,  1, 0, 0, 0);
        int iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
Log.write("iMaxDays ToDate=" + iMaxDays + "  Startdate=" + strStartYr + strStartMth + strStartDay);
        if (Integer.parseInt(strStartDay)  > iMaxDays)
        {       
        alltelRequest.getHttpRequest().setAttribute("costat", "'From Date' - invalid day of month selected");	
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid from date");
%>
		<jsp:forward page="CBReportForm.jsp"/>;
<%
		return;
        }
	calTemp.set(Integer.parseInt(strEndYr),  Integer.parseInt(strEndMth) - 1,  1, 0, 0, 0);
        iMaxDays = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH);
Log.write("iMaxDays FromDate=" + iMaxDays + "  Enddate=" + strEndYr + strEndMth + strEndDay);
        if (Integer.parseInt(strEndDay)  > iMaxDays)
        {       alltelRequest.getHttpRequest().setAttribute("costat", "'To Date' - invalid day of month selected");	
		Log.write(Log.DEBUG_VERBOSE, "CO Invalid to date");
%>
		<jsp:forward page="CBReportForm.jsp"/>;
<%
		return;
        }
	DateFormat dFmt = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
	Log.write(Log.DEBUG_VERBOSE, "CO Date:" + strStartDate + " Date:" + strEndDate);
%>
<table  border=1>
<tr bgcolor=#E5F1CC>
	<td nowrap>Order Number</td>
	<td nowrap>Order Status</td>
	<td nowrap>Status Date</td>
	<td nowrap>User Id</td>
	<td nowrap>First Name</td>
	<td nowrap>Last Name</td>
	<td nowrap>Client</td>
	<td nowrap>Product Type</td>
	<td nowrap>Order Type</td>
	<td nowrap>Change Type</td>
	<td nowrap>Business Name</td>
</tr>
<%
  // user prepared statement later. This query runs once a month...    
	Statement stmt = null;
	ResultSet rs= null;
	StringBuffer sb= new StringBuffer( 256 );
	String strGrpBy   = " Group by d.dwo_sqnc_nmbr, h.DWO_STTS_CD_IN, "
			+ " h.mdfd_userid, u.FRST_NM, u.LST_NM, p.PRDCT_DSCRPTN, s.SRVC_TYP_DSCRPTN, "
			+ " ACTVTY_TYP_DSCRPTN, d.BSNSS_NM ";
	
	String strGrpBy2   = " Group by d.dwo_sqnc_nmbr, h.DWO_STTS_CD_IN, "
			+ " h.mdfd_userid, u.FRST_NM, u.LST_NM, p.PRDCT_DSCRPTN, " 
			+ " s.SRVC_TYP_DSCRPTN, d.BSNSS_NM ";
	
	try{
		String strQry = "select  d.dwo_sqnc_nmbr as OrderNumber, h.DWO_STTS_CD_IN as OrderStatus, max( to_char(h.DWO_HSTRY_DT_IN, 'mm-dd-yyyy HH24:MI:SS Am') ) as  statusdate, "
			+ 	" h.mdfd_userid as UserId, u.FRST_NM as FirstName, u.LST_NM as LastName, "
			+ 	"'KPEN' as  Client,  p.PRDCT_DSCRPTN AS PRODUCTName, s.SRVC_TYP_DSCRPTN "
			+ 	"as OrderType, ACTVTY_TYP_DSCRPTN as ChangeType, d.BSNSS_NM as businessname from dwo_t d, dwo_history_t h, "
			+ 	"service_type_t s, activity_type_t a, userid_t u, PRODUCT_T P "
			+ 	"where d.DWO_SQNC_NMBR = h.DWO_SQNC_NMBR and s.SRVC_TYP_CD = d.srvc_typ_cd "
			+ 	"and a.ACTVTY_TYP_CD = d.ACTVTY_TYP_CD and u.USERID = h.MDFD_USERID "
			+ 	"and P.PRDCT_TYP_CD = D.PRDCT_TYP_CD and h.DWO_STTS_CD_IN = 'IN-REVIEW' "
			+ 	"and h.DWO_HSTRY_DT_IN BETWEEN to_date('" + strStartDate + "','YYYYMMDD') "
			+ 	"AND to_date('" + strEndDate + "','YYYYMMDD') and s.typ_ind = 'W' "
			+ 	"and a.TYP_IND = 'W'  " + strGrpBy 
			+ 	"	union "
			+ 	"select distinct d.dwo_sqnc_nmbr as OrderNumber, h.DWO_STTS_CD_IN as OrderStatus, max( to_char(h.DWO_HSTRY_DT_IN, 'mm-dd-yyyy HH24:MI:SS Am') ) as  statusdate, "
			+ 	" h.mdfd_userid as UserId, u.FRST_NM as FirstName, "
			+ 	"u.LST_NM as LastName,  'BDP' as Client, p.PRDCT_DSCRPTN AS PRODUCTName, "
			+ 	"s.SRVC_TYP_DSCRPTN as OrderType,  'N/A' as ChangeType, d.BSNSS_NM as businessname "
			+ 	"from dwo_t d, dwo_history_t h, service_type_t s, "
			+ 	"userid_t u , PRODUCT_T P where d.DWO_SQNC_NMBR = h.DWO_SQNC_NMBR "
			+ 	"and s.SRVC_TYP_CD = d.srvc_typ_cd and u.USERID = h.MDFD_USERID "
			+ 	"and P.PRDCT_TYP_CD = D.PRDCT_TYP_CD and h.DWO_STTS_CD_IN ='DE-COMPLETE' "
			+ 	"and h.DWO_HSTRY_DT_IN BETWEEN to_date('" + strStartDate + "','YYYYMMDD') "
			+ 	"AND to_date('" + strEndDate + "','YYYYMMDD') and s.typ_ind = 'X' "
			+ 	"and p.TYP_IND = 'X' AND  D.PRDCT_TYP_CD IN ( 'A', 'D', 'S', 'T') " + strGrpBy2
			+ 	" union "
			+ 	"select distinct d.dwo_sqnc_nmbr as OrderNumber, "
			+ 	"h.DWO_STTS_CD_IN as OrderStatus, max(to_char(h.DWO_HSTRY_DT_IN, 'mm-dd-yyyy HH24:MI:SS Am')) as  statusdate, h.mdfd_userid as UserId, "
			+ 	"u.FRST_NM as FirstName, u.LST_NM as LastName, "
			+ 	"'BDP' as Client, p.PRDCT_DSCRPTN AS PRODUCTName, "
			+ 	"s.SRVC_TYP_DSCRPTN as OrderType, ACTVTY_TYP_DSCRPTN as ChangeType, d.BSNSS_NM as businessname "
			+ 	"from dwo_t d, dwo_history_t h, service_type_t s, activity_type_t a, "
			+ 	"userid_t u , PRODUCT_T P where d.DWO_SQNC_NMBR = h.DWO_SQNC_NMBR "
			+ 	"and s.SRVC_TYP_CD = d.srvc_typ_cd and a.ACTVTY_TYP_CD(+) = d.ACTVTY_TYP_CD "
			+ 	"and u.USERID = h.MDFD_USERID and P.PRDCT_TYP_CD = D.PRDCT_TYP_CD "
			+ 	"and h.DWO_STTS_CD_IN ='DE-COMPLETE' "
			+ 	"and h.DWO_HSTRY_DT_IN BETWEEN to_date('" + strStartDate + "','YYYYMMDD') "
			+ 	"AND to_date('" + strEndDate + "','YYYYMMDD') "
			+ 	"and s.typ_ind = 'X' and p.TYP_IND = 'X' "
			// gao, 08/25/2006, RIS 1449, add type 'L'
			+ 	"and a.TYP_IND(+) = 'X' AND  D.PRDCT_TYP_CD IN ( 'M', 'I', 'P', 'E', 'L', 'B', 'X' ) "
			+ 	strGrpBy + " order by   Client, PRODUCTName, UserId ";     
      	Log.write(Log.DEBUG_VERBOSE, "\n " + strQry );
      	con = DatabaseManager.getConnection();
		stmt = con.createStatement(  );
		rs = stmt.executeQuery( strQry );
		
		while(rs.next())
		{
			sb.append( "<tr>");
			sb.append( "<td>&nbsp;" + fixNullStr( rs.getString(1) )  + "</td>\n");
			sb.append( "<td>&nbsp;" +  fixNullStr( rs.getString(2) ) + "</td>\n");
			sb.append( "<td>&nbsp;" +  fixNullStr( rs.getString(3) )  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  fixNullStr( rs.getString(4) )  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  fixNullStr( rs.getString(5) ) + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  fixNullStr( rs.getString(6))  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  fixNullStr( rs.getString(7))  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  fixNullStr( rs.getString(8))  + "</td>\n");
			sb.append( "<td nowrap>&nbsp;" +  fixNullStr( rs.getString(9))  + "</td>\n");
			sb.append( "<td>&nbsp;" +  fixNullStr( rs.getString(10))  + "</td>\n");
			sb.append( "<td>&nbsp;" +  fixNullStr( rs.getString(11))  + "</td>\n");
			sb.append( "<\tr>");
		}
	
	} catch(Exception e) {
		 	e.printStackTrace();
			Log.write(Log.DEBUG_VERBOSE, "CBREPORT  Report 001: Caught exception e=[" + e.toString() + "]");
		}
		finally { // keep connection open for next try
			DatabaseManager.releaseConnection(con);
			try {
				stmt.close(); stmt= null;
				rs.close(); rs = null;
			} catch (Exception eee) {}
		}	
	sb.append( "<tr>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<\tr>");		
	sb.append( "<tr>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<\tr>");	
	sb.append( "<tr>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<\tr>");	
	sb.append( "<tr>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<\tr>");		
	sb.append( "<tr>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<td>&nbsp;</td>");
	sb.append( "<\tr>");	
%>
<%=sb.toString()%>


