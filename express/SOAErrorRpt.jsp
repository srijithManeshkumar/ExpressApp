<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO WINDSTREAM COMMUNICATIONS
 *		INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2010
 *				BY
 *			WINDSTREAM COMMUNICATIONS INC.
 */
/** 
 * MODULE:	SOAErrorRpt.jsp	
 * 
 * DESCRIPTION: SOA Error Report
 * 
 * AUTHOR:      Andy Wei
 * 
 * DATE:        03-30-2010
 * 
 * HISTORY:
 *
 */

%>
<%@ include file="i_header.jsp" %>
<%            String path = request.getContextPath();
%>
<script type='text/javascript' src='<%=path%>/jquery.js'></script>
<script type='text/javascript' src='<%=path%>/tablesorter.js'></script>
<% 
	final String SECURITY_OBJECT = "PROV_REPORTS";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		 alltelResponse.sendRedirect(SECURITY_URL);
	}

	Connection con = null;
	
        con = DatabaseManager.getConnection();
	Statement stmt = null;
	ResultSet rs= null;
	stmt = con.createStatement();
%>

<table id="SOATable" class="tablesorter" width=1300 border=1 align=center cellspacing=0 cellpadding=1>

<br><center>
<SPAN CLASS="header1">S&nbsp;O&nbsp;A&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;E&nbsp;R&nbsp;R&nbsp;O&nbsp;R&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;</SPAN><br>
<br>
	<thead>
		<th align=center width=100 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Request Number</font></th>
		
		<th align=center width=40 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		Version</font></th>
		
		<th align=center width=40 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		PON</font></th>
		
		<th align=center width=110 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		SOA Transaction ID</font></th>
		
		<th align=center width=170 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		SOA Response Received Date</font></th>
	
		<th align=center width=110 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		SOA Reason Code</th>
	
		<th align=center width=500 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		SOA Reason</th>
		
		<th align=center width=100 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		SOA Status</th>
		
		<th align=center width=100 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		ONSP</th>
		
		<th align=center width=60 bgcolor="#3366cc" ><font color="#ffffff" STYLE="cursor:hand" ONMOUSEOUT="hidepopupmsg();">
		ATN</th>
	 </thead>
         <tbody>

                    
<%
	
	String strQry = "SELECT R.RQST_SQNC_NMBR AS NMBR, R.RQST_VRSN AS VRSN, R.RQST_PON AS PON, SOA_TXN_ID AS TXNID, SOA_RESP_RECD_DT AS ReceivedDate, SOA_RSN_CD AS ReasonCode, " +
	                "SOA_RSN_STRING AS Reason, SOA_STATUS AS Status, ONSP AS ONSP, R.ATN AS ATN " +
		        "FROM SOA_TXN_RESPONSE_T S,REQUEST_T R " +
                        "WHERE R.RQST_SQNC_NMBR = S.RQST_SQNC_NMBR and R.RQST_VRSN = S.RQST_VRSN " +
                        "ORDER BY ReceivedDate ";

       rs = stmt.executeQuery(strQry);
       
       String strNMBR = "";
       String strVRSN = "";
       String strPON = "";
       String strTXNID = "";
       String strReceivedDate = "";
       String strReasonCode = "";
       String strReason = "";
       String strStatus = "";
       String strONSP = "";
       String strATN = "";
       
       
       while(rs.next())
       {
       		strNMBR = rs.getString("NMBR");
       		strVRSN = rs.getString("VRSN");
                strPON = rs.getString("PON");
       		strTXNID = rs.getString("TXNID");
			if(strTXNID == null) strTXNID = "&nbsp";
       		strReceivedDate = rs.getString("ReceivedDate");
            if (strReceivedDate == null) {strReceivedDate = "&nbsp";}
       		strReasonCode = rs.getString("ReasonCode");
       	    if (strReasonCode == null) {strReasonCode = "&nbsp";}
       		strReason = rs.getString("Reason");
       	    if (strReason == null) {strReason = "&nbsp";}
       		strStatus = rs.getString("Status");
       	    if (strStatus == null) {strStatus = "&nbsp";}
       		strONSP = rs.getString("ONSP");
       		strATN = rs.getString("ATN");
            if (strATN == null) {strATN = "&nbsp";}
%>
       		<tr>
       		<td align=left><%=strNMBR%></td>
       		<td align=left><%=strVRSN%></td>
                <td align=left><%=strPON%></td>
       		<td align=left><%=strTXNID%></td>
       		<td align=left><%=strReceivedDate%></td>
       		<td align=left><%=strReasonCode%></td>
       		<td align=left><%=strReason%></td>
       		<td align=left><%=strStatus%></td>
       		<td align=left><%=strONSP%></td>
       		<td align=left><%=strATN%></td>
       		</tr>
<%       			
       }
       	
rs.close();
rs = null;
DatabaseManager.releaseConnection(con);
%>
</tbody>
</table>
</UL>
<BR>
<BR>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
<script language="javascript">
    $(document).ready(function()
    {
        $("#SOATable").tablesorter();
    }
);
</script>
</BODY>
</HTML>
