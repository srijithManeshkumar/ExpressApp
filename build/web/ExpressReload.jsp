<%@ include file="i_header.jsp"%>

<%@ page import ="java.util.*" %> 
<%@ page import ="java.sql.*" %> 
<%@ page import ="javax.sql.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<FORM NAME="CamsTest" METHOD=POST ACTION="cams.jsp">
<table align=center width="95%">
  <tr>
    <TH align=center bgcolor="#336699">
	<SPAN class="barheader">Express Singleton reload.</SPAN>
    </th>
  </tr>

<%
	
	final String SECURITY_OBJECT = "TABLE_ADMIN_UPDATE";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		 Log.write(Log.WARNING, sdm.getUser() + "\t\t  This is an administrative action!");
		 alltelResponse.sendRedirect(SECURITY_URL);
	}
	FormFields.reload();
	FieldValues.reload();
	Actions.reload();
	RemedyValues.reload();
%>
<tr><td>
Reloading Form Fields .........
<br>
Reloading Form Values .........
<br>
Reloading Actions .........
<br>
Reloading RemedyValues ....
</td></tr>
</TABLE>
<BR CLEAR=ALL><BR>
</form>
</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
