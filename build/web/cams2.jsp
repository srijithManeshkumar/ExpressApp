<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<FORM NAME="CamsTest" METHOD=POST ACTION="cams2.jsp">

<BR CLEAR=ALL>
<table align=center width="95%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">Testing CAMS Queries</SPAN>
    </th>
  </tr>

<%
	String strQuery = (String) request.getParameter("query1");
	Log.write("strQuery = " + strQuery);
%>
<tr><td>
select  a.cust_phone, b.org_npa, b.org_nxx  from db2.cams_addresst a, db2.CAMS_CUSTOMERT b
where a.cust_phone = '4024896354' and a.addr_id ='SAD1'  AND a.addr_eff_date = (select max(a.addr_eff_date)
from db2.cams_addresst a where a.cust_phone = '4024896354'  and a.addr_id='SAD1')  and b.cust_phone = a.cust_phone
</td></tr>

<tr><td>
<TEXTAREA NAME=query1 ROWS=8 COLS=140 WRAP ><%=strQuery%></TEXTAREA>
</td></tr>
</table>

&nbsp;Field1<select name="field1" size="1"><option value="S" selected>String</option><option value="I" >Int</option><option value="D" >Date</option>
</select>
&nbsp;Field2<select name="field2" size="1"><option value="S" selected>String</option><option value="I" >Int</option><option value="D" >Date</option><option value="N">None</option>
</select>
&nbsp;Field3<select name="field3" size="1"><option value="S" selected>String</option><option value="I" >Int</option><option value="D" >Date</option><option value="N" >None</option>
</select>
&nbsp;Field4<select name="field4" size="1"><option value="S" >String</option><option value="I" >Int</option><option value="D" >Date</option><option value="N" selected>None</option>
</select>
&nbsp;Field5<select name="field5" size="1"><option value="S" >String</option><option value="I" >Int</option><option value="D" >Date</option><option value="N" selected>None</option>
</select>
&nbsp;Field6<select name="field6" size="1"><option value="S" >String</option><option value="I" >Int</option><option value="D" >Date</option><option value="N" selected>None</option>
</select>

<center><INPUT class=appButton TYPE="SUBMIT" name="button1" value="Run Query"></center>
<hr>
<center><b><font size=+3>Query Results</font></b></center>
<TABLE width=95% align=center border=1 cellspacing=0 cellpadding=0>
<TR><th>Field1</th><th>Field2</th><th>Field3</th><th>Field4</th><th>Field5</th><th>Field6</th></tr>

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
		con = DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
		//con = DriverManager.getConnection( "jdbc:edbc://sun30:ED7", "atlxdev", "reset1$1" );
		Log.write("querying CAMS DB2 data warehouse now .000000..");
		
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(strQuery);
		while (rs.next() == true)
		{
%>
		<TR><TD align=left> 
<% 		String str1 = request.getParameter("field1");
		if (str1.equals("S")) {
%>
			<%=rs.getString(1)%>
<%
		} else if (str1.equals("D")) {
%>			
			<%=rs.getDate(1)%>
<%
		} else if (str1.equals("I")) {
%>
			<%=rs.getInt(1)%>
<%
		} else if (str1.equals("N")) {
%>		
		-n/a-
<%
		}
%>
		</TD>
		<TD align=left>
<% 		str1 = request.getParameter("field2");
		if (str1.equals("S")) {
%>
			<%=rs.getString(2)%>
<%
		} else if (str1.equals("D")) {
%>			
			<%=rs.getDate(2)%>
<%
		} else if (str1.equals("I")) {
%>
			<%=rs.getInt(2)%>
<%
		} else if (str1.equals("N")) {
%>		
		-n/a-
<%
		}
%>
		</TD>
		<TD align=left>
<% 		str1 = request.getParameter("field3");
		if (str1.equals("S")) {
%>
			<%=rs.getString(3)%>
<%
		} else if (str1.equals("D")) {
%>			
			<%=rs.getDate(3)%>
<%
		} else if (str1.equals("I")) {
%>
			<%=rs.getInt(3)%>
<%
		} else if (str1.equals("N")) {
%>		
		-n/a-
<%
		}
%>
		</TD>
		<TD align=left>
<% 		str1 = request.getParameter("field4");
		if (str1.equals("S")) {
%>
			<%=rs.getString(4)%>
<%
		} else if (str1.equals("D")) {
%>			
			<%=rs.getDate(4)%>
<%
		} else if (str1.equals("I")) {
%>
			<%=rs.getInt(4)%>
<%
		} else if (str1.equals("N")) {
%>		
		-n/a-
<%
		}
%>
		</TD>
		<TD align=left>
<% 		str1 = request.getParameter("field5");
		if (str1.equals("S")) {
%>
			<%=rs.getString(5)%>
<%
		} else if (str1.equals("D")) {
%>			
			<%=rs.getDate(5)%>
<%
		} else if (str1.equals("I")) {
%>
			<%=rs.getInt(5)%>
<%
		} else if (str1.equals("N")) {
%>		
		-n/a-
<%
		}
%>
		</TD>
		<TD align=left>
<% 		str1 = request.getParameter("field6");
		if (str1.equals("S")) {
%>
			<%=rs.getString(6)%>
<%
		} else if (str1.equals("D")) {
%>			
			<%=rs.getDate(6)%>
<%
		} else if (str1.equals("I")) {
%>
			<%=rs.getInt(6)%>
<%
		} else if (str1.equals("N")) {
%>		
		-n/a-
<%
		}
%>

		</TD></TR>
<%
		} //while
}//try
catch(SQLException se) {
 throw new Exception( se.toString()  );
}
catch(Exception e) {
 throw new Exception( e.toString() );
}
finally {
		//DatabaseManager.releaseConnection(con, DatabaseManager.CAMSP_CONNECTION);
		try{
			con.close();
			con = null;
			System.err.println("Done querying CAMS DB2 data warehouse now ...");
		}
		catch( Exception e) {
		 throw new Exception( "Error closing db connection 1 " );
		}
		System.err.println("Done querying CAMS DB2 data warehouse now ...");

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
