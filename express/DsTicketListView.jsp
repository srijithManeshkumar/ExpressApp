<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2004
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	DsTicketListView.jsp
 * DESCRIPTION: 
 * AUTHOR:      
 * DATE:        03-20-2004
 * 
 * HISTORY:
 *	03/20/2004 init
 *	7/30/2004 pjs look for entry msg
 *
 */

%>

<%@ include file="i_header.jsp" %>
<jsp:useBean id="ticketlistbean" scope="request" class="com.alltel.lsr.common.objects.DsTicketListBean" />
<% QueueCriteria queuebean = sdm.getDsTicketQueueCriteria(); %>

<% Log.write("Query = " + queuebean.getFullQuery()); %>

<%

//DateFormat AppDateFormat = new SimpleDateFormat("MM-dd-yyyy @ hh:mm:ss a");
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
	
<table width=1285 align=left>
  <tr>
    <td width=100>
	<%	if ( sdm.isAuthorized(ticketlistbean.getTblAdmnScrtyTgAdd()) )
		{
	%>
			<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="DsTicketCtlr?tcktcreate=view">&nbsp;NEW&nbsp;TICKET&nbsp;</A>
			</tr></td>
			</table>
	<%	}
	%>

	</td>
	<td width=40></td>
	<td>	  
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="DsTicketListView" METHOD="POST" ACTION="DsTicketListCtlr?tcktsrch=queue">
			<tr>
			  <td>
				&nbsp;
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
				<A HREF="DsTicketListCtlr?tcktsrch=advanced">&nbsp;ADVANCED&nbsp;SEARCH&nbsp;</A>
			</td></tr>
		</table>
	</td>
	<td></td>
	<td>
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="DsTicketListView" METHOD="POST" ACTION="DsTicketListCtlr?tcktsrch=quicksrch">
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
				<input type=text maxLength=60 size=32 name=srchvl>
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
		<a href="/DsTicketListCtlr?tblnmbr=<%=ticketlistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=ticketlistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=ASC">
		<IMG NAME="Sort Ascending" SRC="images/arrow_asc.gif" BORDER=0>
		</a>
		<a href="/DsTicketListCtlr?tblnmbr=<%=ticketlistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=ticketlistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=DESC">
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
			Log.write("Col = " + ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1) );
			//if ((ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("MDFD_DT"))
			if ((ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).endsWith("_DT"))
			{
				if ( (rs.getString(ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1))) != null)
				{	dtsFromDB = Timestamp.valueOf(rs.getString(ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)));
%>
				<%=AppDateFormat.format(dtsFromDB)%>
<%				}
				else {
%>				<%=rs.getString(ticketlistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) %>
<%				}
			}
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
}
catch(Exception e)
{
	e.printStackTrace();
	Log.write(Log.DEBUG_VERBOSE,"DsTicketListView.jsp exception ["+e+"]");
	try {
		DatabaseManager.releaseConnection(con);
	}	catch(Exception ee) {}
}

%>

</table>
<br clear=all><br>&nbsp;Records displayed: <b><%=iRecordCount%></b><br>
<jsp:include page="i_footer.htm" flush="true" />
</body>

<%
//Adding entry message alert box...(value is set in Controller) 
//This was created for giving user trouble ticket #,but can be for other uses as well....
String strEntryMessage = (String) request.getAttribute("_entry_msg_");
if ( (strEntryMessage != null) && (strEntryMessage.length() > 1) && ( sdm.getLoginProfileBean().getUserBean().getCmpnyTyp().equals("P")) )
{
%>
	<script language = "JavaScript">
	<!-- hide me
		var entrymsg="<%= strEntryMessage %>";
		alert(entrymsg);
	// show me -->
	</script>
<%
} %>
</html>


