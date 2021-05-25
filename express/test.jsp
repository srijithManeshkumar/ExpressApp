<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<FORM NAME="PaulsTest" METHOD=POST ACTION="test.jsp">
<table align=center width="95%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">Query Tool</SPAN>
    </th>
  </tr>

<%
	String strQuery = (String) request.getParameter("query1");
	Log.write("strQuery = " + strQuery);
%>

<tr><td>
<TEXTAREA NAME=query1 ROWS=10 COLS=140 WRAP ><%=strQuery%></TEXTAREA>
</td></tr>
</table>

<center><INPUT class=appButton TYPE="SUBMIT" name="button1" value="Run Query"></center>
<hr>
<TABLE width=95% align=center border=1 cellspacing=0 cellpadding=0>
<CAPTION><font size=+2><b>Query results</b></font></CAPTION>

<%
	Connection con=null;
	String strHereBefore = (String) request.getParameter("button1");
	if (strHereBefore == null || strHereBefore.length() == 0 || strQuery == null || strQuery.length()==0)
	{
		//empty query or first time here
		Log.write("First time in or empty query");
			
	}
	else
	{
try {
		Log.write("querying CAMS DB2 data warehouse now ...");
		con = DatabaseManager.getConnection();
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
		
		ResultSet rs2= stmt.executeQuery("select rqst_sqnc_nmbr, mdfd_dt, to_char(mdfd_dt) from request_t where rqst_sqnc_nmbr=100175");
		while (rs2.next()) {
%>
			<tr>
			<td align=left><%=rs2.getString("rqst_sqnc_nmbr")%></td>
			<td align=left><%=rs2.getString("mdfd_dt")%></td>
			</tr>
<%

		}	



}//try
catch(SQLException se) {
}
catch(Exception e) {
}
finally {
		DatabaseManager.releaseConnection(con);
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
