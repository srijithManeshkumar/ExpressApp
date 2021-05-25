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
 * MODULE:	BillingView.jsp	
 * 
 * DESCRIPTION: JSP View used to display Bill Reports
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        6-1-2002
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
	Log.write(Log.ERROR, "Trapped in BillingView.jsp");
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title>Windstream Express</title>
<STYLE TYPE="text/css">
.break { page-break-before: always; }
</STYLE>
</head>
<body bgcolor="#ffffff" LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0>

<%

// Verify the user has proper security
final String CONTROLLER = "BillingCtlr";
if (!sdm.isAuthorized(CONTROLLER))
{
	Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
	alltelResponse.sendRedirect("LsrSecurity.jsp");
}

Connection con = null;
Statement stmt = null;
ResultSet rs = null;

con = DatabaseManager.getConnection();
stmt = con.createStatement();

// Retrieve Parameters
String m_strBan = (String) request.getAttribute("ban");
String m_strLdDt = (String) request.getAttribute("ld_dt");

// Verify user has access to view this BAN 
String m_strVerifyAccess = "SELECT BAN" + 
		" FROM USERID_T U, BAN_T B" +
		" WHERE U.USERID = '" + sdm.getUser() + "'" +
		" AND U.CMPNY_SQNC_NMBR = B.CMPNY_SQNC_NMBR" +
		" AND B.BAN = '" + m_strBan + "'";

rs = stmt.executeQuery(m_strVerifyAccess);
if (!rs.next())
{
	Log.write(Log.WARNING, sdm.getUser() + " does not have access to BAN: " + m_strBan);
        alltelResponse.sendRedirect("LsrSecurity.jsp");
	rs.close();
        return;
}
rs.close();

// Display the Bill for this BAN
String m_strBillDetail = "SELECT BAN_DETAIL_ROW" +
		" FROM BAN_REPORT_T" +
		" WHERE BAN = '" + m_strBan + "'" +
		" AND LD_DT = '" + m_strLdDt + "'" +
		" ORDER BY BAN_DETAIL_SRT_SQNC";

rs = stmt.executeQuery(m_strBillDetail);
%>

<PRE>
<%
while (rs.next())
{
	out.println(rs.getString("BAN_DETAIL_ROW"));
}
%>
</PRE>

<%
rs.close();
stmt.close();
DatabaseManager.releaseConnection(con);
%>

</body>
</html>

