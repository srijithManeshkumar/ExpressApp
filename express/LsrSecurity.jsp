<%-- User didnt have security privileges to access a page, servlet, or jsp and got sent here! --%>
<%-- If the user isnt logged in, then the i_header will direct to login page, unless we send to menu --%>
<%-- added this line via exceed ftp cached edit --%>

<%@ include file="i_header.jsp" %>

<script language = "JavaScript">
<!-- hide me
var my_continue = setTimeout("goToMenu();", 9000);
function goToMenu()
{
	window.location = "ExpressHome.jsp";
}
// show me -->
</script>

<BR CLEAR=ALL>
<center>
<b>You are not authorized to this <i>Express</i> function or page.<br></b>
<p>You will be redirected to the home page or select the continue button to continue. </p>
</center>
<form action="ExpressHome.jsp" method="POST">
<center>
<INPUT class=appButton TYPE="SUBMIT" name="submitbutton" value="Continue">

<input type="HIDDEN" name="originating" value="lsrsecurity">
</body>
<jsp:include page="i_footer.htm" flush="true" />
</form>

</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/LsrSecurity.jsv  $
/*
/*   Rev 1.1   31 Jan 2002 14:29:56   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:58   wwoods
/*Initial Checkin
*/

/* $Revision:   1.1  $
*/
%>
