<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Frameset//EN">


<HTML>
<head>
<LINK rel=stylesheet type="text/css" HREF="application.css">
</head>

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
Log.write("got in dslresults.jsp");
	AlltelRequest alltelRequest = null;
	SessionDataManager sdm = null;
	try {
		alltelRequest = new AlltelRequest(request);
		sdm =  alltelRequest.getSessionDataManager();
		if ( (sdm==null) || (!sdm.isUserLoggedIn()))
		{	Log.write(Log.DEBUG_VERBOSE, "user not logged on dsl page");
			AlltelResponse alltelResponse = null;
			alltelResponse.sendRedirect("LoginCtlr");
			return;
		}
	}
	catch (Exception e) {}
	String strURL = PropertiesManager.getProperty("lsr.bpqlookup.url", "LsrErr.jsp");
Log.write("dsl url=" + strURL);
	Connection conn = null;
	Statement stmt = null;
	
Log.write("before conneciton");

	conn = DatabaseManager.getConnection();
	stmt = conn.createStatement();
	ResultSet rs = stmt.executeQuery("SELECT TARGUS_USERID, TARGUS_PSSWRD FROM USERID_T U, COMPANY_T C WHERE U.USERID='" + sdm.getUser() + "' AND C.CMPNY_SQNC_NMBR=U.CMPNY_SQNC_NMBR");

	rs.next();
	String strUser=rs.getString(1);
	String strPwd=rs.getString(2);
	rs.close();
	rs=null;
	stmt.close();
	stmt=null;
	DatabaseManager.releaseConnection(conn);
%>

<form method="POST" action="<%=strURL%>">
<BR CLEAR=ALL>
<center>
<b>Enter Qualification information.<br><br>

<font face="verdana" size=2><b>Phone number:</b></font>
<input type="text" name="skey" size="10"><br>

<input type="hidden" name="uname" size="10" value="<%=strUser%>"><br>
<input type="hidden" name="pword" size="10" value="<%=strPwd%>"><br>

<INPUT class=appButton TYPE="SUBMIT" value="Submit Inquiry" name="B1">
<input type="reset" value="Reset" name="B2">
<input type="hidden" value="phone" name="func">
<BR CLEAR=ALL>

</form>

<jsp:include page="i_footer.htm" flush="true" />
</html>
