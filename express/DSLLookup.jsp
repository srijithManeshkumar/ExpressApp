<%
/**
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                      COPYRIGHT (C) 2004
 *                              BY
 *                      Windstream COMMUNICATIONS INC.
 */
/**
 * MODULE:      DSLLookup.jsp
 *
 * DESCRIPTION: Page to display common Express header and wrap Targus lookup site in an internal frame.
 *
 * AUTHOR:	psedlak
 *		
 * DATE:        06-03-2002
 *
 * HISTORY:
 *	9-27-2004 pjs - Not using Targus anymore...Use DSL Qual web from ACI
 *	5-04-2006 gao - Change DSL to Broadband naming 
 * 01/11/2008 Steve Korchnak 
 * Idea5052   Modified usage of properties files from lsr.dsllookup to lsr.bpqlookup
 *
 */

/** $Log:    $
/*
/* $Revision:   1.10  $
*/

%>
<%@ include file="i_header.jsp" %>

<script language = "JavaScript">
<!-- hide me
function phoneEdit(formName, elementDesc, element, maxlen)
{
        var q = eval("window.document." + formName + "." + element + ".value.length");
        var x = eval("window.document." + formName + "." + element + ".value");
        if (q != maxlen) 
        {
                var msg="Telephone number field must be " + maxlen + " digits ";
                alert(msg);
                return false;
        }
        if (isNaN(x))
        {
                var msg="Telephone number field must be numeric only";
                alert(msg);
                return false;
        }
        return true;
}
function clearTN()
{
	var msg2="";
        window.document.DSLLookup.skey.value=msg2;
        window.document.DSLLookup._DSL_QUAL_RESULTS_.value=msg2;
	return false;
}
// show me -->
</script>

<% 
	/*************************** ********************************
	 *	SEE Dslbean for storing dsl lookups 					*
	 *															*
	 * 	THIS  SECTION OF CODE IS OBSOLETE						*
	 *  NEW CODE IS DslBean.dbInsert().....						*
	 *  EK: FEB 23, 2005.										*
	 ************************************************************
	
	
	//Every time we take a user here, we write a record to an activity table TARGUS_ACTIVITY_T.
	//This gives us a kind of cross-check to what Targus will bill us/vendors per lookup.
	//Although this violates the MVC view concept, this view is a 'hybrid' since it also
		//acts as a controller and can be navigated to via multiple ways.
	Connection conn = null;
	Statement stmt = null;
	try {
	
		conn = DatabaseManager.getConnection();
		stmt = conn.createStatement();
		stmt.executeUpdate("INSERT INTO TARGUS_ACTIVITY_T (CMPNY_SQNC_NMBR, USERID, TARGUS_USERID, ACTIVITY_DTS) " +
			" SELECT C.CMPNY_SQNC_NMBR, '" + sdm.getUser() + "', C.TARGUS_USERID, SYSDATE FROM USERID_T U, " +
			" COMPANY_T C WHERE U.USERID='" + sdm.getUser() + "' AND C.CMPNY_SQNC_NMBR=U.CMPNY_SQNC_NMBR");
	}
	catch (Exception e) {
		Log.write(Log.ERROR, "DSLLookup.jsp Exception insert targus activity record for user=["+sdm.getUser()+"]");
	}
	finally {
	        stmt.close();
	        stmt=null;
	        DatabaseManager.releaseConnection(conn);
	}
*/
%>

<table align=center width="100%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
        <SPAN class="barheader">Broadband&nbsp;Qualification</SPAN>
    </th>
  </tr>
</table>
<BR CLEAR=ALL>
<center><a href="DSLLookup.jsp"><font size=+1>Perform New Lookup</font></a>
</b></center>
<br><center>
<%= PropertiesManager.getProperty("lsr.bpqlookup.fyi", "NOTE: There will be a fee charged per lookup") %>
</center>

<%
	// Log.write("got in dslresults.jsp");
	String m_strTN = (String) request.getAttribute("skey");
	if ((m_strTN == null) || (m_strTN.length() == 0))
	{
		m_strTN="";
	}
	String m_strResults = (String) request.getAttribute("_DSL_QUAL_RESULTS_");
	if ((m_strResults == null) || (m_strResults.length() == 0))
	{
		m_strResults="";
	}

	//code change to replace if message contains "Qualified for Internet Access" -- Antony -- 0802013

	if (m_strResults.indexOf("Qualified for Internet Access") > 0)
//		m_strResults="Found string \"Qualified for Internet Access\". This has to be replaced with the message given by Mike: Failed Qualificaton\n Provided wire center not equipped for Broadband service.";
		m_strResults="<font color=\"red\">Failed Qualification"+"<br>"+"Provided wire center not equipped for Broadband service.</font>";

	String strURL = PropertiesManager.getProperty("lsr.bpqlookup.processor", "LsrErr.jsp");
//Log.write("dsl url=" + strURL);
//Log.write("before conneciton");
/*
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
	*/
%>

<form name="DSLLookup" method="POST" action="<%=strURL%>" >
<BR CLEAR=ALL>
<center>
<b>The service is only applicable for ordering UNE-P Loop services.<br><br>
<b>Enter Qualification information.<br><br>

<font face="verdana" size=2><b>Phone number:</b></font>
<input type="text" name="skey" value="<%=m_strTN%>" size="10" onChange="phoneEdit('DSLLookup','Telephone Number', 'skey', 10);"><br><br>

<INPUT class=appButton TYPE="SUBMIT" value="Submit Inquiry" name="action" onClick="return phoneEdit('DSLLookup','Telephone Number', 'skey', 10);">
<INPUT class=appButton TYPE="SUBMIT" value="Reset" name="action" onClick="return clearTN();">
<input type="hidden" value="origin" name="DSLLookup">

<BR CLEAR=ALL>
</form>

<% if (m_strResults.length() > 0 )
{

%>
	<br>
	<hr><br><font size=+1>
	<center><%= m_strResults %></center></font><br>
<% }
%>

<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>


