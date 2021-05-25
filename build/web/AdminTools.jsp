<%@ include file="i_header.jsp" %>

<%@ page import ="java.util.*" %> 
<%@ page import ="com.alltel.lsr.common.util.*" %> 

<FORM NAME="AdminTools" METHOD=POST ACTION="AdminTools.jsp">
<table align=center width="95%">
  <tr>
    <TH align=center bgcolor="#7AABDE">
	<SPAN class="barheader">Admin Tools</SPAN>
    </th>
  </tr>

</td></tr>
</table>

<center><INPUT class=appButton TYPE="SUBMIT" name="button1" value="Garbage Collection"></center>

<%
	String strHereBefore = (String) request.getParameter("button1");
	if (strHereBefore == null || strHereBefore.length() == 0 )
	{
		//empty query or first time here
		Log.write("First time in to AdminTools");
	}
	else
	{
		if (strHereBefore.equals("Garbage Collection") )
		{
			Log.write("AdminTools user is forcing Garbage collection");
			System.gc();
		}
	}
%>
<BR CLEAR=ALL><BR>
</form>
</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>
