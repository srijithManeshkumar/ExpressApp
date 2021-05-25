<%
/**
 * NOTICE:
 *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
 *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS, 
 *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *			COPYRIGHT (C) 2005
 *				BY
 *			Windstream COMMUNICATIONS INC.
 */
/** 
 * MODULE:	DwoListView.jsp	
 * 
 * DESCRIPTION: 
 * 
 * AUTHOR:      Dan Martz
 * 
 * DATE:        03-20-2004
 * 
 * HISTORY:
 * 	pjs 4-25-2005 Logical Chg Order - column size changes
 *
 */

%>

<%@ include file="i_header.jsp" %>
<jsp:useBean id="dwolistbean" scope="request" class="com.alltel.lsr.common.objects.DwoListBean" />
<% QueueCriteria queuebean = sdm.getDwoQueueCriteria(); %>

<% Log.write(Log.DEBUG_VERBOSE, "Query = " + queuebean.getFullQuery()); %>

<%

//DateFormat AppDateFormat = new SimpleDateFormat("MM-dd-yyyy @ hh:mm:ss a");
DateFormat AppDateFormat = new SimpleDateFormat(Toolkit.REPORT_DATE_FORMAT);
Timestamp dtsFromDB = new Timestamp(System.currentTimeMillis());

int iRecordCount = 0;
Connection con = null;
Statement stmt = null;
ResultSet rs = null;

//fix for connection pool leak -- Antony -- 06102010

try {//enclosed whole jsp content inside a try

con = DatabaseManager.getConnection();
stmt = con.createStatement();
try
{
rs = stmt.executeQuery(queuebean.getFullQuery());
}
catch (Exception e)
{
e.printStackTrace();
//added extra log statements here -- Antony -- 06102010
Log.write("Exception in executing query in DwoListView.jsp. Error from Exception :"+e.getMessage());
}
%>
	
<table width=800 align=left>
  <tr>
    <td width=100>
	<%	
		Log.write(Log.DEBUG_VERBOSE, "DwoListView.jsp Add tag="+ dwolistbean.getTblAdmnScrtyTgAdd());
		if ( sdm.isAuthorized(dwolistbean.getTblAdmnScrtyTgAdd()) )
		{
	%>
			<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="DwoCtlr?dwocreate=view">&nbsp;Create&nbsp;Order&nbsp;</A>
			</tr></td>
			</table>
	<%	}
	%>

	</td>
	<td width=40></td>
	<td>	  
	</td>
  </tr>
  <tr>
	<td>
		<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr><td>
				<A HREF="DwoListCtlr?dwosrch=advanced">&nbsp;ADVANCED&nbsp;SEARCH&nbsp;</A>
			</td></tr>
		</table>
	</td>
	<td>
		<table border=1 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
			<tr>
			<td>  
				<A HREF="ExpressSISCtlr?ExpressSISAction=0">&nbsp;Customer&nbsp;Search&nbsp;</A>
			</td>
			</tr>
		</table>
	</td>	
	<td></td>
	<td>
		<table border=0 bordercolor="#7AABDE" cellspacing=0 cellpadding=0>
		<FORM NAME="DwoListView" METHOD="POST" ACTION="DwoListCtlr?dwosrch=quicksrch">
			<tr>
			  <td>
				&nbsp;Quick&nbsp;Search&nbsp;>>>&nbsp;
			  </td>
			  <td>
				<SELECT NAME="srchctgry">
					<OPTION VALUE="" SELECTED>...Category...</OPTION>
					<%  for (int x = 0; x < dwolistbean.getTblAdmnClmns() ; x++) { %>
					<OPTION VALUE="<%=x%>"><%=dwolistbean.getTblAdmnClmnDscrptn(x)%></OPTION>
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
<% 	for (int x = 0; x < dwolistbean.getTblAdmnClmns() ; x++) 
	{ 
%>
		<th align=left valign=top width=<%=dwolistbean.getTblAdmnClmnWdth(x)%>><%=dwolistbean.getTblAdmnClmnDscrptn(x)%>
		<BR>
		<a href="/DwoListCtlr?tblnmbr=<%=dwolistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=dwolistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=ASC">
		<IMG NAME="Sort Ascending" SRC="images/arrow_asc.gif" BORDER=0>
		</a>
		<a href="/DwoListCtlr?tblnmbr=<%=dwolistbean.getTblAdmnSqncNmbr()%>&amp;srtby=<%=dwolistbean.getTblAdmnClmnDbNm(x)%>&amp;srtsqnc=DESC">
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
	
<% 	for (int x = 0; x < dwolistbean.getTblAdmnClmns() ; x++) 
	{ 
		int tmpIndex = dwolistbean.getTblAdmnClmnDbNm(x).lastIndexOf("."); 
		if (x == 0)
		{
%>
			<td align=left valign=top>
			<A HREF="<%=dwolistbean.getTblAdmnCtlr()%>?seqget=<%=rs.getString(dwolistbean.getTblAdmnCtlrIdx())%>">&nbsp;<%=rs.getString(dwolistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1))%>&nbsp;</A>
			</td>
<%		}
		else
		{
%>
			<td align=left valign=top>
<%
			if ((dwolistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)).equals("MDFD_DT"))
			{
				dtsFromDB = Timestamp.valueOf(rs.getString(dwolistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)));
%>
				<%=AppDateFormat.format(dtsFromDB)%>
<%			}
			else
			{
%>
				<%=rs.getString(dwolistbean.getTblAdmnClmnDbNm(x).substring(tmpIndex+1)) %>
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
   //Added rs and stmt close statements as they were never there before -- Antony -- 06102010
   rs.close(); rs = null;
   stmt.close(); stmt = null;
}//end of first try -- Added catch and finally blocks below -- Antony -- 06102010
catch(Exception e) {
    Log.write("Exception in DwoListView.jsp. Error Message :"+e.getMessage());
    rs.close();rs = null;//we need to close here as because of the exception the close statement in code above may have been skipped
    stmt.close();stmt = null;
} finally {
        Log.write("Inside finally block in DwoListView.jsp ! Releasing connection object: "+con.toString());
        DatabaseManager.releaseConnection(con);
}

%>

</table>
<br clear=all><br>&nbsp;Records displayed: <b><%=iRecordCount%></b><br>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>


