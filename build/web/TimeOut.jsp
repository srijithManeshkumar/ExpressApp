<%@ include file="i_header_logo.htm" %>

<%-- User exceeded the inactivity period defined in AlltelSecurityManager --%>

<script language = "JavaScript">
<!-- hide me
var my_continue = setTimeout("goToMenu();", 15000);
function goToMenu()
{
	window.location = "LoginCtlr";
}
// show me -->
</script>


<BR CLEAR=ALL>
<center>
<b>Your <i>Express</i> session has timed out due to inactivity.<br></b>
<p>You will be redirected to the login page or select the continue button to continue. </p>
</center>
<form action="LoginCtlr" method="POST">
<center>
<INPUT class=appButton TYPE="SUBMIT" name="submitbutton" value="Continue">

<input type="HIDDEN" name="originating" value="timeout">
</form>

</body>
<jsp:include page="i_footer.htm" flush="true" />
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/TimeOut.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:48:24   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:06:50   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/

%>
