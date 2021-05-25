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
 * MODULE:	RequestAtnPrintView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        02-15-2002
 * 
 * HISTORY:
 *	xx/dd/2002  
 *
 */

/** $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/RequestAtnPrintView.jsv  $
/*
/*   Rev 1.2   09 Apr 2002 15:52:16   dmartz
/* 
/*
/*   Rev 1.1   21 Feb 2002 12:29:22   sedlak
/* 
/*
/*   Rev 1.0   13 Feb 2002 14:20:32   dmartz
/*Release 1.1
/*
*/
/** $Revision:   1.2  $
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
	Log.write(Log.ERROR, "Trapped in RequestAtnPrintView.jsp");
}
%>

<jsp:useBean id="requestBean" scope="request" class="com.alltel.lsr.common.objects.RequestBean" />

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>Windstream Express</title>
<LINK rel=stylesheet type="text/css" HREF="application.css">
</head>
<body bgcolor="#ffffff" LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0>

<%--  Top Header --%>

<%


Connection conPrint = null;
Statement stmtPrint = null;

conPrint = DatabaseManager.getConnection();
requestBean.getConnection();
requestBean.setUserid(sdm.getUser());
stmtPrint = conPrint.createStatement();

// Retrieve Parameters
String m_strRqstSqncNmbr = (String) request.getAttribute("RQST_SQNC_NMBR");
int m_iRqstSqncNmbr = Integer.parseInt(m_strRqstSqncNmbr);
String m_strRqstVrsn = (String) request.getAttribute("RQST_VRSN");
int m_iRqstVrsn = Integer.parseInt(m_strRqstVrsn);

// Verify user has access to view this form
if (! requestBean.hasAccessTo(m_iRqstSqncNmbr))
{
	alltelResponse.sendRedirect("LsrSecurity.jsp");
	return;
}

String m_strRqstHdrQry = "SELECT A.RQST_SQNC_NMBR, A.RQST_STTS_CD, A.RQST_PON, A.SRVC_TYP_CD, A.ACTVTY_TYP_CD, A.RQST_VRSN, A.RQST_TYP_CD, B.SRVC_TYP_DSCRPTN, C.ACTVTY_TYP_DSCRPTN FROM REQUEST_T A, SERVICE_TYPE_T B, ACTIVITY_TYPE_T C WHERE A.RQST_SQNC_NMBR = " + m_iRqstSqncNmbr + " AND A.SRVC_TYP_CD = B.SRVC_TYP_CD AND A.ACTVTY_TYP_CD = C.ACTVTY_TYP_CD AND B.TYP_IND = 'R' AND C.TYP_IND = 'R'";

ResultSet rsRqstHdr = stmtPrint.executeQuery(m_strRqstHdrQry);
rsRqstHdr.next();
%>

<table align=left border=0 cellspacing=1 cellpadding=0>
<tr>
	<th width=10>&nbsp;</th>
	<th align=left width=80>PON</th>
	<th align=left width=100>Status</th>
	<th align=left width=130>Service&nbsp;Type</th>
	<th align=left width=130>Activity</th>
	<th align=left width=130>Version</th>
</tr>
<tr>
	<td>&nbsp;</td>
	<td align=left><%=rsRqstHdr.getString("RQST_PON")%></td>
	<td align=left><%=rsRqstHdr.getString("RQST_STTS_CD")%></td>
	<td align=left><%=rsRqstHdr.getString("SRVC_TYP_DSCRPTN")%></td>
	<td align=left><%=rsRqstHdr.getString("ACTVTY_TYP_DSCRPTN")%></td>
	<td align=left><%=rsRqstHdr.getInt("RQST_VRSN")%></td>
</tr>
</table>

<BR CLEAR=ALL>
<HR>
<BR>
<%
rsRqstHdr.close();

Log.write(Log.DEBUG_VERBOSE, "RequestAtnPrintView.jsp --- ");

// Obtain all ATNs for this request
String strQuery = "SELECT RS_MULTI_DETAIL_ATN FROM RS_MULTI_DETAIL_T WHERE RQST_SQNC_NMBR = " + m_iRqstSqncNmbr;
ResultSet rs = stmtPrint.executeQuery(strQuery);

while (rs.next())
{
	String strAtn = rs.getString("RS_MULTI_DETAIL_ATN");

	if (strAtn != null && strAtn.length() > 0)
	{
%>
		(<%= strAtn.substring(0,3) %>)&nbsp;<%= strAtn.substring(4,7) %>-<%= strAtn.substring(8) %>
		<BR>
<%	}
}

rs.close();
stmtPrint.close();
requestBean.closeConnection();
DatabaseManager.releaseConnection(conPrint);
%>

</TABLE>
<BR>
<BR>
<BR>
<jsp:include page="i_footer.htm" flush="true" />
</BODY>
</HTML>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/RequestAtnPrintView.jsv  $
/*
/*   Rev 1.2   31 Jan 2002 06:51:34   sedlak
/* 
/*
/*   Rev 1.1   31 Jan 2002 06:46:14   sedlak
/* 
/*
/*   Rev 1.0   31 Jan 2002 06:46:18   psedlak
/*Initial Checkin
*/

/* $Revision:   1.2  $
*/

%>
