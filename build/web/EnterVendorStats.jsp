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
 * MODULE:	EnterVendorStats.jsp	
 * 
 * DESCRIPTION: JSP View used to display list of vendor reports
 * 
 * AUTHOR:      
 * 
 * DATE:        
 * 
 * HISTORY:
 *	xx/dd/2002  
 *
 */

%>

<%@ include file="i_header.jsp" %>

<%
	final String CONTROLLER = "PROV_REPORTS";
	final int PRIMER = -1;
	if (!sdm.isAuthorized(CONTROLLER))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + CONTROLLER);
		alltelResponse.sendRedirect(SECURITY_URL);
	}
	Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        Log.write(Log.DEBUG_VERBOSE, "EnterVendorStats()");
%>


<BR>
<TABLE width=50% align=center cellspacing=0 cellpadding=0 border=0>
	<TR>
		<TD><HR></TD>
		<TD align=center width=50%><SPAN CLASS="header1">Enter Statistics for Vendor Reports</SPAN></TD>
		<TD><HR></TD>
</TABLE>
<BR CLEAR=ALL>
<BR>
<BR>
<table width=45% align="center" border=0>
<tr><td>
<ul>
<%
try {
        con = DatabaseManager.getConnection();
        stmt = con.createStatement();

        rs = stmt.executeQuery("SELECT VR.RPRT_SQNC_NMBR, VR.RPRT_DSC, TO_CHAR(VRA.RPRT_RUN_DT,'YYYYMMDD'),  " +
                " TO_CHAR(VRA.RPRT_BGN_DT,'MON YYYY'), VR.RPRT_PRCSSR,  "+
		" TO_CHAR(VRA.RPRT_BGN_DT,'YYYYMMDD') " +
                " FROM VENDOR_REPORT_T VR, VENDOR_REPORT_ACTVTY_T VRA " +
		" WHERE VR.RPRT_SQNC_NMBR=VRA.RPRT_SQNC_NMBR(+) " +
                " AND VR.RPRT_SRC_IND='M' ORDER BY 1,3 DESC");
        int iPrev=PRIMER;
        while(rs.next()==true)
        {
		int iNum = rs.getInt(1);
                String strDesc = rs.getString(2);
                String strRunDate = rs.getString(3);
                String strRange = rs.getString(4);
                String strProc = rs.getString(5);
                String strBeginDate = rs.getString(6);
                if (iNum != iPrev) 
		{
			if (iPrev != PRIMER) //close last list
			{
%>			
			</ul></li></ul>
<%
			}
%>
			<tr><td>
			<ul><li><strong><%=strDesc%></strong>
				<ul>
				<li><SPAN CLASS="smallstyle1"><a href="<%=strProc%>?seq=<%=iNum%>&mth=new">New Report</a></SPAN></li>
<%	
		}
		//Now start new list
		iPrev = iNum;
		if (strRunDate != null)
		{
%>
				<li><SPAN CLASS="smallstyle1">
				<a href="<%=strProc%>?seq=<%=iNum%>&mth=<%=strRunDate%>&beg=<%=strBeginDate%>"><%=strRange%></a>
				</SPAN>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp
				Delete this report:<input type="checkbox" name=x value="Delete report">
				</li>
				
<%
		}
        } //while()

}
catch (Exception e) {
        Log.write(Log.DEBUG_VERBOSE, "EnterVendorStats() Exception=["+e+"] caught");
}
finally {
        Log.write(Log.DEBUG_VERBOSE, "EnterVendorStats() finally");
        DatabaseManager.releaseConnection(con);
}

%>

</ul></li>
</ul>
</td></tr>
</table>

</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
