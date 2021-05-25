<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 
<%@ page import ="com.alltel.lsr.common.objects.*" %> 

<FORM NAME="CamsTest" METHOD=POST ACTION="camsEK.jsp">

<BR CLEAR=ALL>
<table align=center width="95%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">Test DSL PROM </SPAN>
    </th>
  </tr>

<%
	String strPhone  = (String) request.getParameter("phone");
	Log.write("strQuery = " + strPhone);
%>
<tr><td>
Please enter a phone number i.e: '4024896354' 
</td></tr>
<tr><td>
<input NAME=phone size=20 ></TEXTAREA>
</td></tr>
</table>
<center><INPUT class=appButton TYPE="SUBMIT" name="button1" value="Run Query"></center>
<hr>
<center>
<p> <%=strPhone %> <br>
<%
	if( strPhone != null ){
		Connection con=null;
		Hashtable hCamsInfo = new Hashtable( 20);
		DslPromoBean bean = new DslPromoBean(strPhone);	
		Class.forName("ca.edbc.jdbc.EdbcDriver");	
		try {
			System.err.println("querying CAMS DB2 data warehouse now ...");
			con = DriverManager.getConnection( "jdbc:edbc://sun30:ED7", "atlxdev", "reset1$1" );
			//con = DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
			hCamsInfo = bean.getCAMPhoneInfo( con);
		System.err.println( "-------------"  );
		%> 
		<%=hCamsInfo.toString()%>
		<%
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
				if( con != null ){
					con.close();
				}
				con = null;
				System.err.println("Done querying CAMS DB2 data warehouse now ...");
			}
			catch( Exception e) {
			 throw new Exception( "Error closing db connection 1 " + e.toString() );
			}
			System.err.println("Done querying CAMS DB2 data warehouse now ...");

		}
		%>
		<%=hCamsInfo.toString()%>
		<%
	}
	%>
</p>	
</form>
</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
