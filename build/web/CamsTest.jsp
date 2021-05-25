<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<table align=center width="100%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">TESTING DB CONNECTION POOLS</SPAN>
    </th>
  </tr>
</table>
<BR CLEAR=ALL>
<center> Express connection to CAMS </center><br>
<%
	Log.write("querying CAMS DB2 data warehouse now ...");
	Connection con = DatabaseManager.getConnection(DatabaseManager.CAMSP_CONNECTION);
	Statement stmt = con.createStatement();
	ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM DB2.CAMS_TRAITT");
	while (rs.next() == true)
	{
%>
	<TABLE width=75% align=center border=1 cellspacing=0 cellpadding=0>
		<TR><TD align=center>CAMS ASOCT Table entries = <BR><%= rs.getInt(1) %><BR><BR></TD></TR>
	</TABLE>
	<BR CLEAR=ALL><BR>
<%
	} //while
	DatabaseManager.releaseConnection(con, DatabaseManager.CAMSP_CONNECTION);
	//con.close();
	//con=null;
	Log.write("Done querying CAMS DB2 data warehouse now ...");
%>

<center> Express connection to FrontWare </center><br>
<%
	Log.write("querying Frontware now ...");
	Connection con2 = DatabaseManager.getConnection(DatabaseManager.FWP_CONNECTION);
	Statement stmt2 = con2.createStatement();
	ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM fw.Community");
	while (rs2.next() == true)
	{
%>
	<TABLE width=75% align=center border=1 cellspacing=0 cellpadding=0>
		<TR><TD align=center>FrontWare Community Table entries = <BR><%= rs2.getInt(1) %><BR><BR></TD></TR>
	</TABLE>
	<BR CLEAR=ALL><BR>
<%
	} //while
	DatabaseManager.releaseConnection(con2, DatabaseManager.FWP_CONNECTION);
	//con2.close();
	//con2=null;
	Log.write("Done querying Frontware now ...");
%>

<BR>

</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/ExpressHome.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:36:32   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:36   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
%>

