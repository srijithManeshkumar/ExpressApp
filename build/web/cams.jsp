<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<FORM NAME="CamsTest" METHOD=POST ACTION="cams.jsp">
<table align=center width="95%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">DB2 Query Tool</SPAN>
    </th>
  </tr>

<%
	String strQuery = (String) request.getParameter("query1");
	System.err.println("strQuery = " + strQuery);
	//String strQuery = (String) request.getParameter("query1");
	Log.write("strQuery = " + strQuery);
%>
<tr><td>
select  s.cust_phone, s.cust_insrv_date FROM DB2.CAMS_SENTT S where s.org_region='098' and s.org_state='NE'
AND s.org_comp='150' and s.org_dist='150' and s.org_busoff='LNCL' and s.cust_phone='4024896354'
</td></tr>

<tr><td>
<TEXTAREA NAME=query1 ROWS=10 COLS=140 WRAP ><%=strQuery%></TEXTAREA>
</td></tr>
</table>

<center><INPUT class=appButton TYPE="SUBMIT" name="button1" value="Run Query"></center>
<hr>
<TABLE width=95% align=center border=1 cellspacing=0 cellpadding=0>
<CAPTION><font size=+2><b>DB2 Warehouse Query results</b></font></CAPTION>

<%
	Connection con=null;
	String strHereBefore = (String) request.getParameter("button1");
	if (strHereBefore == null || strHereBefore.length() == 0 || strQuery == null || strQuery.length()==0)
	{
		//empty query or first time here
		System.err.println("First time in or empty query");
			
	}
	else
	{
try {
		System.err.println("querying CAMS DB2 data warehouse now ...");
		con = DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
		Statement stmt = con.createStatement();

		ResultSet rs = stmt.executeQuery(strQuery);
		
		//Get column name and count
		ResultSetMetaData rsMeta = rs.getMetaData();
		int iColCount = rsMeta.getColumnCount();
		Log.write("Col count=" + iColCount);
%>		
		<TR>
<%
		for (int i = 1;i<iColCount+1; i++) {
%>
			<TH><%=rsMeta.getColumnName(i)%></TH>
<%
		}
%>
		</TR>
<%
		while (rs.next() == true)
		{
%>
			<TR>
<%
			for (int i=1;i<iColCount+1;i++) {
%>
				<TD align=left><%=rs.getString(i)%></TD>
<%			}
%>
			</TR>
<%
		} //while
}//try
catch(SQLException se) {
	se.printStackTrace();
	System.err.println( se.toString() );
}
catch(Exception e) {
e.printStackTrace();
	System.err.println( e.toString() );
}
finally {
		DatabaseManager.releaseConnection(con, DatabaseManager.CAMSP_CONNECTION);
		Log.write("Done querying CAMS DB2 data warehouse now ...");
}
	}
%>
	<tr><td align=center bgcolor="#ff99cc">End of Data</td></tr>
	</TABLE>
	<BR CLEAR=ALL><BR>

</form>
</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
