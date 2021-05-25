<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2003
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	BillDisputeListView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Vince Pavill
 * 
 * DATE:        01-21-2003
 * 
 * HISTORY:
 *	03/15/2003 Initial Check-in
 *
 */

%>

<%@ include file="i_header.jsp" %>
<jsp:useBean id="billdisputelistbean" scope="request" class="com.alltel.lsr.common.objects.BillDisputeListBean" />
<% QueueCriteria queuebean = sdm.getDisputeQueueCriteria(); %>

<% Log.write("Query = " + queuebean.getFullQuery()); %>

<%

//DateFormat AppDateFormat = new SimpleDateFormat("MM-dd-yyyy @ hh:mm:ss a");
DateFormat AppDateFormat = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
NumberFormat moneyFmt = NumberFormat.getCurrencyInstance(java.util.Locale.US);
Timestamp dtsFromDB = new Timestamp(System.currentTimeMillis());
float fMoneyFromDB = 0;

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
	<%	if ( sdm.isAuthorized(billdisputelistbean.getTblAdmnScrtyTgAdd()) )
		{
	%>
			<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="BillDisputeCtlr?dsptcreate=view">&nbsp;NEW&nbsp;DISPUTE&nbsp;</A>
			</tr></td>
			</table>
	<%	}
	%>

	</td>
	<td width=40></td>
	<td>	  
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="BillDisputeListView" METHOD="POST" ACTION="BillDisputeListCtlr?dsptsrch=queue">
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
				<A HREF="BillDisputeListCtlr?dsptsrch=advanced">&nbsp;ADVANCED&nbsp;SEARCH&nbsp;</A>
			</td></tr>
		</table>
	</td>
	<td></td>
	<td>
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="BillDisputeListView" METHOD="POST" ACTION="BillDisputeListCtlr?dsptsrch=quicksrch">
			<tr>
			  <td>
				&nbsp;Quick&nbsp;Search&nbsp;>>>&nbsp;
			  </td>
			  <td>
				<SELECT NAME="srchctgry">
					<OPTION VALUE="" SELECTED>...Category...</OPTION>
					<%  for (int x = 0; x < billdisputelistbean.getTblAdmnClmns() ; x++) { %>
					<OPTION VALUE="<%=x%>"><%=billdisputelistbean.getTblAdmnClmnDscrptn(x)%></OPTION>
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

<table width=1100 align=left border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=2>
<tr>
<% 	for (int x = 0; x < billdisputelistbean.getTblAdmnClmns() ; x++) 
	{ 
%> 

		<th align=left valign=top width=<%=billdisputelistbean.getTblAdmnClmnWdth(x)%>><%=billdisputelistbean.getTblAdmnClmnDscrptn(x)%>
		<BR>
		<a href="/BillDisputeListCtlr?tblnmbr=<%=billdisputelistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=billdisputelistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=ASC">
		<IMG NAME="Sort Ascending" SRC="images/arrow_asc.gif" BORDER=0>
		</a>
		<a href="/BillDisputeListCtlr?tblnmbr=<%=billdisputelistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=billdisputelistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=DESC">
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
	
<% 	for (int x = 0; x < billdisputelistbean.getTblAdmnClmns() ; x++) 
	{ 
		int tmpIndex = billdisputelistbean.getTblAdmnClmnDbNm(x).lastIndexOf("."); 
		if (x == 0)
		{
%>
			<td align=left valign=top>
			<A HREF="<%=billdisputelistbean.getTblAdmnCtlr()%>?seqget=<%=rs.getString(billdisputelistbean.getTblAdmnCtlrIdx())%>">&nbsp;<%=rs.getString(billdisputelistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1))%>&nbsp;</A>
			</td>
<%		}
		else
		{
%>
			<td align=left valign=top>
<%
			if ((billdisputelistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("MDFD_DT"))
			{
				dtsFromDB = Timestamp.valueOf(rs.getString(billdisputelistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)));
%>
				<%=AppDateFormat.format(dtsFromDB)%>
<%			}
			else
			if ((billdisputelistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).toLowerCase().indexOf("amnt") >= 0)
			{	//Money...
				fMoneyFromDB = rs.getFloat(billdisputelistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1));
				//Log.write("money = " + fMoneyFromDB); 
%>
				<%=moneyFmt.format(fMoneyFromDB)%>
<%			}
			else
			{
%>
				<%=rs.getString(billdisputelistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) %>
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


