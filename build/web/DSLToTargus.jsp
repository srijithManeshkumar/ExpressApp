<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Frameset//EN">
<HTML>
<%@ page
        language="java"
        import = "java.util.*, java.text.*, com.alltel.lsr.common.util.*"
        session="true"
%>


<%@ include file="i_header.jsp" %>
<body>
Hello
<%
	Log.write("In DSLtoTargus");
	alltelResponse.sendRedirect("http://webapp.targusinfo.com/dslqualifier/dsl_query.asp");
	Log.write("In DSLtoTargus after sendRedirect() ");
%>

</body>
</HTML>
