<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2007
 *				BY
 *			WINDSTREAM COMMUNICATIONS INC.
 * MODULE:	WGIntrvlWS.jsp	
 * 
 * DESCRIPTION: Business Data Product, work group interval worksheet
 * 
 * AUTHOR:      Steve Korchnak
 * 
 * DATE:        01/02/2006
 * 
 * HISTORY:
 */
%>

<%@ include file="ExpressUtil.jsp" %>
<%-- VALIDATE SECURITY HERE AND SEND USER TO LOGIN PAGE IF NO VALID LOGIN EXISTS FOR SESSION --%>

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
	alltelResponse.getHttpResponse().setHeader("Content-Disposition", ("attachment; filename = TEST_" + getCurrentDate_Simpe() + ".xls" ));

	String strStartYr = alltelRequest.getParameter("startYr");
	String strStartMth = alltelRequest.getParameter("startMn");
	String strStartDay = alltelRequest.getParameter("startDy");
	String strStartDate = strStartYr + strStartMth + strStartDay;
	
	String strEndYr = alltelRequest.getParameter("endYr");
	String strEndMth = alltelRequest.getParameter("endMn");
	String strEndDay = alltelRequest.getParameter("endDy");
	String strEndDate = strEndYr + strEndMth + strEndDay;
	
	String[] strWGReportType = alltelRequest.getAttributeValue("reportType");
	
	Log.write(Log.DEBUG_VERBOSE, "WG Interval Worksheet: Date:" + strStartDate + " Date:" + strEndDate);
	BDPReportBean Report = new BDPReportBean();
	Report.setStartYr( strStartYr ) ;
	Report.setStartMth( strStartMth );
	Report.setStartDay( strStartDay );
	Report.setEndYr( strEndYr ) ;
	Report.setEndMth( strEndMth ) ;
	Report.setEndDay( strEndDay ) ;
	Report.setReportType(Integer.parseInt(strWGReportType[0]));
	String[] strProductType = alltelRequest.getAttributeValue("dwonew_prdcttyp");	//allow multiple
	String[] strSTATE_CDs = alltelRequest.getAttributeValue("state");
	String[] strOrderType = alltelRequest.getAttributeValue("dwonew_srvctyp");
	String[] strChangeType = alltelRequest.getAttributeValue("dwonew_acttyp");
	String[] strSubChangeType =  alltelRequest.getAttributeValue("dwonew_actsubtyp");
	
	Report.setProductTypes( strProductType );
	Report.setOrderTypes( strOrderType );
	Report.setChangeTypes( strChangeType );
	Report.setChangeSubTypes( strSubChangeType);
	Report.setSTATE_CDs( strSTATE_CDs );	
	Connection conRC = null;
	String strArrStt[] = {"__" };
	try { 
		conRC = DatabaseManager.getConnection();	
		String strReport =Report.runReport( conRC );
%>	
		<%=strReport %>
<%
	} // try 
	catch (Exception e) {
		e.printStackTrace();
		Log.write(Log.DEBUG_VERBOSE, e.toString() );
	}
	finally {
		DatabaseManager.releaseConnection(conRC);
	}
%>
</UL>
</HTML>
