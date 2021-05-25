<%
/**
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                      COPYRIGHT (C) 2003
 *                             BY
 *                      ALLTEL COMMUNICATIONS INC.
 */
/**
 * MODULE:      LoginView.jsp
 *
 * DESCRIPTION: Login page
 *
 * AUTHOR:
 *
 * DATE:        01-02-2002
 *
 * HISTORY:
 *      04/22/2003  psedlak Add lookup to customer serv nbr.
 *
 */
%>

<%@ page
        language="java"
        import = "com.alltel.lsr.common.util.*"
%>

<%@ include file="i_header_logo.htm" %>

<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>

<% String evnt =  request.getAttribute("levent").toString(); %>

<% if(evnt.equals("pchange")) { %>
   <FORM NAME="LoginView" ACTION="LoginCtlr" METHOD="POST">
        <TR><TD colspan=2>&nbsp;</TD></TR>
    <TR><TD align=center colspan=2><SPAN CLASS="header1">Login&nbsp;/&nbsp;Change&nbsp;Password</SPAN></TD></TR>
    <TR><TD colspan=2>&nbsp;</TD></TR>
    <TR>
	<TD width=50% align=right>User ID:&nbsp;</TD>
	<TD align=left><input type=text maxLength=15 size=14 name=userid></TD>
    </TR>
    <TR>
	<TD width=50% align=right>Old&nbsp;Password:&nbsp;</TD>
	<TD align=left><input type=PASSWORD maxLength=12 size=14 name=oldpassword></TD>
    </TR>
    <TR>
	<TD width=50% align=right>New&nbsp;Password:&nbsp;</TD>
	<TD align=left><input type=PASSWORD maxLength=12 size=14 name=newpassword1></TD>
    </TR>
    <TR>
	<TD width=50% align=right>Verify&nbsp;New&nbsp;Password:&nbsp;</TD>
	<TD align=left><input type=PASSWORD maxLength=12 size=14 name=newpassword2></TD>
    </TR>
    <TR><TD colspan=2>&nbsp;</TD></TR>
    <TR><TD colspan=2>&nbsp;</TD></TR>
    <TR>
    </TR>

  <TR><TD align=center colspan=2>
     <%= (String)request.getAttribute("loginstat")  %>  
   </TD></TR>
    
    <TR><TD colspan=2>&nbsp;</TD></TR>
    <TR><TD colspan=2>&nbsp;</TD></TR>
	
  <TR><TD colspan=2>&nbsp;
	<table width=100% cellspacing=4>
	<TD width=50% align=right> <INPUT class=appButton TYPE="RESET" VALUE="Reset"> </TD>
	<TD nowrap align=left><INPUT class=appButton  TYPE="SUBMIT" NAME="sevent" VALUE="ChangeLogin"></TD>
  </table> 
  </TR>  

  </FORM>
  
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><A class=loginlink HREF="LoginCtlr?sevent=login">
   Click HERE to Login.
  </A></TD></TR>


<% } else if (evnt.equals("forget")) { %>
   <FORM NAME="LoginView" ACTION="LoginCtlr" METHOD="POST">
    
    <TR><TD colspan=2>&nbsp;</TD></TR>
    <TR><TD align=center colspan=2><SPAN CLASS="header1">LOGIN&nbsp;/&nbsp;FORGOT&nbsp;PASSWORD</SPAN></TD></TR>
    <TR><TD colspan=2>&nbsp;</TD></TR>
    <TR>
	<TD width=50% align=right>User ID:&nbsp;</TD>

	 <TD align=left><input type=text maxLength=15 size=14 name=userid READONLY value=<%= request.getAttribute("theUser").toString() %> ></TD> 

    </TR>
    <TR>
	<TD width=50% align=right>Secret&nbsp;Question:&nbsp;</TD>
	<TD align=left><input type=text maxLength=50 size=50 name=secretquestion READONLY value="<%= request.getAttribute("theQuestion").toString()  %>" ></TD> 
    </TR>
    <TR>
	<TD width=50% align=right>Answer:&nbsp;</TD>
	<TD align=left><input type=text maxLength=12 size=14 name=answer></TD>
    </TR>
    <TR>
	<TD width=50% align=right>New&nbsp;Password:&nbsp;</TD>
	<TD align=left><input type=PASSWORD maxLength=12 size=14 name=newpassword1></TD>
    </TR>
    <TR>
	<TD width=50% align=right>Verify&nbsp;New&nbsp;Password:&nbsp;</TD>
	<TD align=left><input type=PASSWORD maxLength=12 size=14 name=newpassword2></TD>
    </TR>

  
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2>
     <%= (String)request.getAttribute("loginstat")  %>  
   </TD></TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>

  <TR><TD colspan=2>&nbsp;
	<table width=100% cellspacing=4>
	<TD width=50% align=right><INPUT class=appButton TYPE="RESET" VALUE="Reset"></TD>
	<TD align=left><INPUT class=appButton  TYPE="SUBMIT" NAME="sevent" VALUE="ResetLogin"></TD>
  </table>  
  </TR>
  

  </FORM>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><A HREF="LoginCtlr?sevent=login">
  Click&nbsp;here&nbsp;to&nbsp;return&nbsp;to&nbsp;Main&nbsp;Login&nbsp;Screen.
  </A></TD></TR>

<% } else { %>
   <FORM NAME="LoginView" ACTION="LoginCtlr" METHOD="POST">

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2><SPAN CLASS="header1">Login&nbsp;&nbsp;Required</SPAN></TD></TR>

  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR>
	<TD width=50% align=right>User ID:&nbsp;</TD>
	<TD align=left><input type=text maxLength=15 size=14 name=userid></TD>
  </TR>
  <TR>
	<TD width=50% align=right>Password:&nbsp;</TD>
	<TD align=left><input type=PASSWORD maxLength=12 size=14 name=password></TD>
  </TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  
  <TR><TD colspan=2>&nbsp;
	<table width=100% cellspacing=4>
	<TD width=50% valign=top align=right> <INPUT class=appButton TYPE="RESET" VALUE="Reset"> </TD>
	<TD nowrap valign=top align=left><INPUT class=appButton  TYPE="SUBMIT" NAME="sevent" VALUE="Login"></TD>
  </table>  
  </TR>
  
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2>
     <%= (String)request.getAttribute("loginstat")  %>  
   </TD></TR>

  
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2> 
	Forget&nbsp;your&nbsp;password?	Enter&nbsp;your&nbsp;User ID&nbsp;above&nbsp;and&nbsp;then&nbsp;Click&nbsp;HERE-><INPUT class=appButton  TYPE="SUBMIT" NAME="sevent" VALUE="ForgetPassword">
	</A></TD></TR>
  <TR><TD colspan=2>&nbsp;</TD></TR>
  <TR><TD align=center colspan=2>
	Want&nbsp;to&nbsp;change&nbsp;your&nbsp;password?&nbsp;&nbsp;Click&nbsp;HERE--><INPUT class=appButton  TYPE="SUBMIT" NAME="sevent" VALUE="ChangePassword"> 
	</A></TD></TR>
  </FORM>
</TABLE>

<% }

	// Add help desk number. pjs 4/22/03
	String str800Nbr =  PropertiesManager.getProperty("lsr.helpdesk.number", "1-800-615-6227");
	String str800Message =  PropertiesManager.getProperty("lsr.helpdesk.message", "Help desk:");
%>
	<br><br><br><br>
	<center><%=str800Message%></center><br>
	<center><%=str800Nbr%></center><br>

<SCRIPT LANGUAGE="JavaScript">
	document.LoginView.userid.focus();
</SCRIPT>


<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/LoginView.jsv  $
/*
/*   Rev 1.2   May 31 2002 15:40:34   dmartz
/* 
/*
/*   Rev 1.1   31 Jan 2002 14:33:20   sedlak
/* 
/*
/*   Rev 1.0   23 Jan 2002 11:05:56   wwoods
/*Initial Checkin
*/

/* $Revision:   1.2  $
*/
%>
