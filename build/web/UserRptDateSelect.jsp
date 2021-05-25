<%
/**
 * NOTICE:
 *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO ALLTEL INFORMATION
 *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
 *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
 *
 *                      COPYRIGHT (C) 2003
 *                              BY
 *                      ALLTEL COMMUNICATIONS INC.
 */
/**
 * MODULE:      UserRptDateSelect.jsp
 *
 * DESCRIPTION: View to define user report criteria (date/user(s)).
 *
 * AUTHOR:      pjs
 *
 * DATE:        02-21-2003
 *
 * HISTORY:
 *	05/29/2003 pjs 	SER20476 Added another user rpt for BillDisputes -and they pick date/users here...
 */

%>

<%@ include file="i_header.jsp" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>

<%
	final String SECURITY_OBJECT = "PROV_REPORTS";
	if (!sdm.isAuthorized(SECURITY_OBJECT))
	{
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
		alltelResponse.sendRedirect(SECURITY_URL);
		return;
	}
        String strPost = request.getParameter("rpt");
        Log.write(Log.DEBUG_VERBOSE, "User Report parm rpt=" + strPost);
	if ( (strPost == null) || (strPost.length() < 2) )
	{	//navigation error...user tried to get to URL w/o proper parms...
		Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP directly w/o parms " + SECURITY_OBJECT);
		alltelResponse.sendRedirect(SECURITY_URL);
		return;
	}
%>
<script type="text/javascript">

    function OnSubmitForm()
    {
        if(document.pressed == 'Submit')
        {
            document.UserReportForm.action ="<%=strPost%>";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            if("<%=strPost%>" =='UserReport.jsp'){
            document.UserReportForm.action ="UserExcelReport.jsp";
            }else{
             document.UserReportForm.action ="UserExcelReportDisputes.jsp";
            }
        }
        return true;
    }


</script>
            <FORM NAME="UserReportForm" METHOD=POST onsubmit="return OnSubmitForm();">

<TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD align=center colspan=4>
<SPAN CLASS="header1"> U&nbsp;s&nbsp;e&nbsp;r&nbsp;&nbsp;&nbsp;S&nbsp;t&nbsp;a&nbsp;t&nbsp;i&nbsp;s&nbsp;t&nbsp;i&nbsp;c&nbsp;s&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;e&nbsp;p&nbsp;o&nbsp;r&nbsp;t&nbsp;&nbsp;&nbsp;&nbsp;D&nbsp;a&nbsp;t&nbsp;e&nbsp;&nbsp;&nbsp;S&nbsp;e&nbsp;l&nbsp;e&nbsp;c&nbsp;t&nbsp;i&nbsp;o&nbsp;n</SPAN></TD>
</TR>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR>
	<TD align=right>From Date:&nbsp;</TD>
	<TD  nowrap><SELECT name="from_due_date_mnth">
<%
		String y;
		Calendar cal = Calendar.getInstance();
		int iMth = cal.get(Calendar.MONTH)+1;
		int iDay = cal.get(Calendar.DAY_OF_MONTH);
		int iYear = cal.get(Calendar.YEAR);
		for (int x = 1; x < 13 ; x++)
		{	y = "" + x;
			if (y.length()==1) y="0"+x;
			if (x == iMth) {
%>
				<OPTION SELECTED value="<%=y%>"><%=y%>
<%
			} else {
%>
				<OPTION value="<%=y%>"><%=y%>
<%
			}
		}
%>
	</SELECT>
	<SELECT name="from_due_date_dy">
<%
		for (int x = 1; x < 32 ; x++)
		{	y = "" + x;
			if (y.length()==1) y="0"+x;
			if (x==iDay) {
%>
				<OPTION SELECTED value="<%=y%>"><%=y%>
<%
			} else {
%>
				<OPTION value="<%=y%>"><%=y%>
<%
			}
		}
%>
	</SELECT>
	<SELECT name="from_due_date_yr">
<%
		for (int x = 2001; x <= iYear ; x++)
		{
			if (x==iYear) {
%>
				<OPTION SELECTED value="<%=x%>"><%=x%>
<%
			} else {
%>
				<OPTION value="<%=x%>"><%=x%>
<%
			}
		}
%>
	</SELECT>
	</TD>
	<TD  align=right>To Date:&nbsp;</TD>
	<TD nowrap><SELECT name="to_due_date_mnth">
<%
		for (int x = 1; x < 13 ; x++)
		{
			y = "" + x;
			if (y.length()==1) y="0"+x;
			if (x == iMth) {
%>
			<OPTION SELECTED value="<%=y%>"><%=y%>
<%
			} else {
%>
			<OPTION value="<%=y%>"><%=y%>
<%
			}
		}
%>
	</SELECT>
	<SELECT name="to_due_date_dy">
<%
		for (int x = 1; x < 32 ; x++)
		{	y = "" + x;
			if (y.length()==1) y="0"+x;
			if (x==iDay) {
%>
			<OPTION SELECTED value="<%=y%>"><%=y%>
<%
			} else {
%>
			<OPTION value="<%=y%>"><%=y%>
<%
			}
		}
%>
	</SELECT>
	<SELECT name="to_due_date_yr">
<%
		for (int x = 2001; x <= iYear ; x++)
		{
			if (x==iYear) {
%>
				<OPTION SELECTED value="<%=x%>"><%=x%>
<%
			} else {
%>
				<OPTION value="<%=x%>"><%=x%>
<%
			}
		}
%>
	</SELECT>
	</TD>
</TR>

<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD colspan=4>&nbsp;</TD></TR>

<%
Connection con = null;
Statement stmt = null;
ResultSet rs = null;
PreparedStatement pstmt =  null;

String strQryGrps = " Select USRGRP_EMP_SQNC_NMBR, USERGROUP_NAME, "
			+ " USR_GRP_DSCRPTN  FROM USERGROUP_EMP_T uel "
			+ " where exists (select USRGRP_EMP_SQNC_NMBR "
			+ " from USR_USRGRP_LINK_T where "
			+ " USRGRP_EMP_SQNC_NMBR =  uel.USRGRP_EMP_SQNC_NMBR)"
			+ " ORDER BY USERGROUP_NAME ";
			
ResultSet rset = null;
StringBuffer  sbUsrSection = new StringBuffer(256); 

sbUsrSection.append( "<tr><td align=center  colspan=4 valign=\"top\">"
	 + " <select NAME=\"USERID\" MULTIPLE SIZE=5>"
	 + " <option value=\"ALL\">--Report on all Userids--  ");

StringBuffer  sbGrpSection = new StringBuffer(256); 

sbGrpSection.append( "<tr><td align=center colspan=4 valign=\"top\">"
	 + " <select NAME=\"groupids\" MULTIPLE SIZE=5>"
	 + " <option value=\"ALL\" >--Report on all group id--  " );

try {
	con = DatabaseManager.getConnection();
	stmt = con.createStatement();
	pstmt = con.prepareStatement( strQryGrps  );
	pstmt.clearParameters();
	rset = pstmt.executeQuery( );	
	while (rset.next() == true)
	{
	sbGrpSection.append( "<option value=\"" + rset.getString(1) +"\">"+ rset .getString(2) + "</option>" );
	}
	sbGrpSection.append("</select></td></tr>" );


	

	//If report is for Billing Disputes, then only show users that have that ability...
	if ( (strPost.toLowerCase()).indexOf("disputes") >= 0)
	{
		//Only include users that have the ability to work on orders/LSRs
		rs = stmt.executeQuery("SELECT DISTINCT U.USERID, U.LST_NM||', '||U.FRST_NM||' ('||U.USERID||') '" +
			" FROM USERID_T U, COMPANY_T C, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA "+
			" WHERE U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP = 'P' " +
			" AND UGA.USERID=U.USERID AND SGA.SCRTY_GRP_CD=UGA.SCRTY_GRP_CD " +
			" AND SGA.SCRTY_OBJCT_CD='PROV_DISPUTE_ACTIONS'  "+
			" ORDER BY 2 ASC");
	} 
	else
	{
		//Only include users that have the ability to work on orders/LSRs
		rs = stmt.executeQuery("SELECT U.USERID, U.LST_NM||', '||U.FRST_NM||' ('||U.USERID||') '" +
			" FROM USERID_T U, COMPANY_T C, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA "+
			" WHERE U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP = 'P' " +
			" AND UGA.USERID=U.USERID AND SGA.SCRTY_GRP_CD=UGA.SCRTY_GRP_CD " +
			" AND SGA.SCRTY_OBJCT_CD='PROV_RQST_ACTIONS'  "+
			" ORDER BY U.LST_NM ASC");
	}

	while (rs.next() == true)
	{
		sbUsrSection.append( "<option value=\"" + rs.getString(1)+ "\">" );
		sbUsrSection.append( rs.getString(2) + "</option> ");
	}
	sbUsrSection.append("</select></td></tr>" );
}
catch (Exception e) {}
finally {
	rs.close();
	rs=null;
	pstmt.close();  pstmt = null;
	rset.close();
	rset=null;
	DatabaseManager.releaseConnection(con);
}
%>
<tr><td align=center colspan=4>Select <b>Employee Group(s):</b><span class=smalenote>
 Note, Multiple group selections can be made by holding down the 'Ctrl' key</span></td></tr>
<%=sbGrpSection.toString() %>
<TR><TD colspan=4>&nbsp;</TD></TR>
<tr><td align=center colspan=4>Select <b>Userid(s):</b><span class=smalenote>
 Note, Multiple userid selections can be made by holding down the 'Ctrl' key</span></td></tr>
<%=sbUsrSection.toString() %>

<TR><TD colspan=4>&nbsp;</TD></TR>
<tr>
	<td align=center colspan=4>Display weekend details (Sat/Sun) on report ?
	<input name="keep_weekends" type="CHECKBOX" CHECKED value="keep_weekends"></td>
</tr>
<TR><TD colspan=4>&nbsp;</TD></TR>
<tr>
	<td align=center colspan=4>Count weekends (Sat/Sun) as working days (used to calculate daily response average) ?
	<input name="count_weekends" type="CHECKBOX" value="count_weekends"></td>
</tr>

<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR>
	<TD align=center colspan=4>
	<INPUT  TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit" onclick="document.pressed=this.value">
        &nbsp;&nbsp;
	<INPUT  TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="EXCEL"onclick="document.pressed=this.value" >
        </TD>
</TR>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR><TD colspan=4>&nbsp;</TD></TR>
<TR>
	<TD align=center colspan=4><font color="red"><b>
	<%= (String) request.getAttribute("reportstat") %></b></font>
	</TD>
</TR>
 
</TABLE>
</FORM>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
/** $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/UserRptDateSelect.jsv  $
   
      Rev 1.0   Feb 26 2003 12:47:14   e0069884
   SER 20477
/*
*/

/* $Revision:   1.0  $
*/
%>
