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
 * MODULE:	TicketListView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        03-20-2002
 * 
 * HISTORY:
 *	03/20/2002 Initial Check-in
 *	7/25/2002 Correct date/time display after Oracle driver change.
 *
 */

%>

<%@ include file="i_header.jsp" %>
<jsp:useBean id="ticketlistbean" scope="request" class="com.alltel.lsr.common.objects.TicketListBean" />
<% QueueCriteria queuebean = sdm.getTicketQueueCriteria(); %>

<% Log.write("Query = " + queuebean.getFullQuery()); %>

<%

//DateFormat AppDateFormat = new SimpleDateFormat("MM-dd-yyyy @ hh:mm:ss a");
DateFormat AppDateFormat = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
Timestamp dtsFromDB = new Timestamp(System.currentTimeMillis());

int iRecordCount = 0;
Connection con = null;
Statement stmt = null;

con = DatabaseManager.getConnection();
stmt = con.createStatement();
ResultSet rs = stmt.executeQuery(queuebean.getFullQuery());
%>
	
<table width=800 align=left>
  <tr>
    <td width=100>
	<%	if ( sdm.isAuthorized(ticketlistbean.getTblAdmnScrtyTgAdd()) )
		{
	%>
			<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="TicketCtlr?tcktcreate=view">&nbsp;NEW&nbsp;TICKET&nbsp;</A>
			</tr></td>
			</table>
	<%	}
	%>

	</td>
	<td width=40></td>
	<td>	  
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="TicketListView" METHOD="POST" ACTION="TicketListCtlr?tcktsrch=queue">
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
				<A HREF="TicketListCtlr?tcktsrch=advanced">&nbsp;ADVANCED&nbsp;SEARCH&nbsp;</A>
			</td></tr>
		</table>
	</td>
	<td></td>
	<td>
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="TicketListView" METHOD="POST" ACTION="TicketListCtlr?tcktsrch=quicksrch">
			<tr>
			  <td>
				&nbsp;Quick&nbsp;Search&nbsp;>>>&nbsp;
			  </td>
			  <td>
				<SELECT NAME="srchctgry">
					<OPTION VALUE="" SELECTED>...Category...</OPTION>
					<%  for (int x = 0; x < ticketlistbean.getTblAdmnClmns() ; x++) { %>
					<OPTION VALUE="<%=x%>"><%=ticketlistbean.getTblAdmnClmnDscrptn(x)%></OPTION>
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
<% 	for (int x = 0; x < ticketlistbean.getTblAdmnClmns() ; x++) 
	{ 
%>
		<th align=left valign=top width=<%=ticketlistbean.getTblAdmnClmnWdth(x)%>><%=ticketlistbean.getTblAdmnClmnDscrptn(x)%>
		<BR>
		<a href="/TicketListCtlr?tblnmbr=<%=ticketlistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=ticketlistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=ASC">
		<IMG NAME="Sort Ascending" SRC="images/arrow_asc.gif" BORDER=0>
		</a>
		<a href="/TicketListCtlr?tblnmbr=<%=ticketlistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=ticketlistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=DESC">
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
	
<% 	for (int x = 0; x < ticketlistbean.getTblAdmnClmns() ; x++) 
	{ 
		int tmpIndex = ticketlistbean.getTblAdmnClmnDbNm(x).lastIndexOf("."); 
		if (x == 0)
		{
%>
			<td align=left valign=top>
			<A HREF="<%=ticketlistbean.getTblAdmnCtlr()%>?seqget=<%=rs.getString(ticketlistbean.getTblAdmnCtlrIdx())%>">&nbsp;<%=rs.getString(ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1))%>&nbsp;</A>
			</td>
<%		}
		else
		{
%>
			<td align=left valign=top>
<%
			if ((ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("MDFD_DT"))
			{
				dtsFromDB = Timestamp.valueOf(rs.getString(ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)));
%>
				<%=AppDateFormat.format(dtsFromDB)%>
<%			}
			else
			{
%>
				<%=rs.getString(ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) %>
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
DatabaseManager.releaseConnection(con);
%>

</table>
<br clear=all><br>&nbsp;Records displayed: <b><%=iRecordCount%></b><br>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>


