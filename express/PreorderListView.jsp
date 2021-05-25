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
 * MODULE:	PreorderListView.jsp	
 * 
 * DESCRIPTION: JSP View used to create new Preorders
 * 
 * AUTHOR:      Express Development Team
 * 
 * DATE:        2-1-2002
 * 
 * HISTORY:
 *	pjs     7/25/2002 Correct date/time display after Oracle driver change.
 *	pjs     8/29/2002 Emer CC30028/HD99494 catch exception and close db conn.
 *
 */

%>

<%@ include file="i_header.jsp" %>
<jsp:useBean id="preorderlistbean" scope="request" class="com.alltel.lsr.common.objects.PreorderListBean" />
<% QueueCriteria queuebean = sdm.getPreorderQueueCriteria(); %>

<% Log.write("Query = " + queuebean.getFullQuery()); %>

<%

DateFormat AppDateFormat = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
Timestamp dtsFromDB = new Timestamp(System.currentTimeMillis());

int iRecordCount = 0;
Connection con = null;
Statement stmt = null;

try {

	con = DatabaseManager.getConnection();
	stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery(queuebean.getFullQuery());
%>
	
<table width=800 align=left>
  <tr>
    <td width=100>
	<%	if ( sdm.isAuthorized(preorderlistbean.getTblAdmnScrtyTgAdd()) )
		{
	%>
			<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="PreorderCtlr?precreate=view">&nbsp;NEW&nbsp;PREORDER&nbsp;</A>
			</tr></td>
			</table>
	<%	}
	%>

	</td>
	<td width=40></td>
	<td>	  
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="PreorderListView" METHOD="POST" ACTION="PreorderListCtlr?presrch=queue">
			<tr>
			  <td>
				&nbsp;
<%--			dont show the QUEUE until it is implemented

				&nbsp;Select&nbsp;Queue&nbsp;>>>&nbsp;
			  </td>
			  <td>
				<SELECT NAME="srchtyp">
					<OPTION VALUE="" SELECTED>...Select...</OPTION>
					<OPTION VALUE="1">INITIAL</OPTION>
					<OPTION VALUE="2">SUBMITTED</OPTION>
					<OPTION VALUE="3">IN-REVIEW</OPTION>
					<OPTION VALUE="4">FOC</OPTION>
					<OPTION VALUE="5">REJECTED</OPTION>
					<OPTION VALUE="6">JEPOARDY</OPTION>
					<OPTION VALUE="7">CANCELLED</OPTION>
					<OPTION VALUE="8">COMPLETED</OPTION>
				</SELECT>
			  </td>
			  <td>
			  <INPUT class=appButton TYPE="SUBMIT" name="queuebutton" value="GO">
--%>
			  </td>
		  </tr>
		</FORM>
		</table>			
	</td>
  </tr>
  <tr>
	<td>
		<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="PreorderListCtlr?presrch=advanced">&nbsp;ADVANCED&nbsp;SEARCH&nbsp;</A>
			</td></tr>
		</table>
	</td>
	<td></td>
	<td>
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="PreorderListView" METHOD="POST" ACTION="PreorderListCtlr?presrch=quicksrch">
			<tr>
			  <td>
				&nbsp;Quick&nbsp;Search&nbsp;>>>&nbsp;
			  </td>
			  <td>
				<SELECT NAME="srchctgry">
					<OPTION VALUE="" SELECTED>...Category...</OPTION>
					<%  for (int x = 0; x < preorderlistbean.getTblAdmnClmns() ; x++) { %>
					<OPTION VALUE="<%=x%>"><%=preorderlistbean.getTblAdmnClmnDscrptn(x)%></OPTION>
					<% } %>
				</SELECT>

				&nbsp;Value:
				<input type=text maxLength=30 size=32 name=srchvl>
			  </td>
			  <td>
			  <INPUT class=appButton TYPE="SUBMIT" name="submitbutton" value="GO">
			</td></tr>
		</FORM>
		</table>
	</td>
  </tr>
</table>
<BR CLEAR=ALL>

<table width=1000 align=left border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=2>
<tr>
<% 	for (int x = 0; x < preorderlistbean.getTblAdmnClmns() ; x++) 
	{ 
%>
		<th align=left valign=top width=<%=preorderlistbean.getTblAdmnClmnWdth(x)%>><%=preorderlistbean.getTblAdmnClmnDscrptn(x)%>
		<BR>
		<a href="/PreorderListCtlr?tblnmbr=<%=preorderlistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=preorderlistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=ASC">
		<IMG NAME="Sort Ascending" SRC="images/arrow_asc.gif" BORDER=0>
		</a>
		<a href="/PreorderListCtlr?tblnmbr=<%=preorderlistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=preorderlistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=DESC">
		<IMG NAME="Sort Descending" SRC="images/arrow_desc.gif" BORDER=0>
		</a>
		</th>
<% 	} 
%>
</tr>

<% while(rs.next()==true) 
   {
	iRecordCount++; 
%>
	<tr>
	
<% 	for (int x = 0; x < preorderlistbean.getTblAdmnClmns() ; x++) 
	{ 
		int tmpIndex = preorderlistbean.getTblAdmnClmnDbNm(x).lastIndexOf("."); 
		if (x == 0)
		{
%>
			<td align=left valign=top>
			<A HREF="<%=preorderlistbean.getTblAdmnCtlr()%>?seqget=<%=rs.getString(preorderlistbean.getTblAdmnCtlrIdx())%>">&nbsp;<%=rs.getString(preorderlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1))%>&nbsp;</A>
			</td>
<%		}
		else
		{
%>
			<td align=left valign=top>
<%
			if ((preorderlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("MDFD_DT"))
			{
				dtsFromDB = Timestamp.valueOf(rs.getString(preorderlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)));
%>
				<%=AppDateFormat.format(dtsFromDB)%>
<%			}
			else
			{
%>
				<%=rs.getString(preorderlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) %>
<%
			}
%>
			&nbsp;</td>
<%		}
%>
	

<% 	} 
%>
	</tr>
<% }

}//end of try
catch(Exception e) {
        Log.write(Log.DEBUG_VERBOSE, "PreorderListView: Caught exception e=[" + e + "]");
}
finally {
        DatabaseManager.releaseConnection(con);
}

%>

</table>
<br clear=all><br>&nbsp;Records displayed: <b><%=iRecordCount%></b><br>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

