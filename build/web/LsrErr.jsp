<%@ include file="i_header.jsp" %>
<%@ page isErrorPage="true" %>

<script language = "JavaScript">
<!-- hide me
var my_continue = setTimeout("goToLoginPage();", 5000);
function goToLoginPage()
{
	window.location = "ExpressHome.jsp";
}
// show me -->
</script>

<form action="ExpressHome.jsp" method="POST">
<CENTER>
<br><br>
<font size=5><b>An <i>Express</i> Application error has occurred and been logged!</b></font>
<br><br><br>
<font size=3>You will be redirected to the home page or select the continue button to continue.</font>
<br><br>
<INPUT class=appButton TYPE="SUBMIT" name="submitbutton" value="Continue">
<input type="HIDDEN" name="originating" value="LsrErr">
</CENTER><br>
</form>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/LsrErr.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:31:52   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:56   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
%>
