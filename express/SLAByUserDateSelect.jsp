<%
            /**
             * NOTICE:
             *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
             *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
             *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
             *
             *                      COPYRIGHT (C) 2005
             *                              BY
             *                      Windstream COMMUNICATIONS INC.
             */
            /**
             * MODULE:      SLAByUserDateSelect.jsp
             * DESCRIPTION: SLA report by user selection criteria page
             * AUTHOR:      psedlak
             * DATE:        5-15-2005
             *
             * HISTORY:
             */
%>
<%@ include file="i_header.jsp" %>

<%            final String SECURITY_OBJECT = "PROV_REPORTS";
            if (!sdm.isAuthorized(SECURITY_OBJECT)) {
                Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
                alltelResponse.sendRedirect(SECURITY_URL);
            }
%>
<script type="text/javascript">

    function OnSubmitForm()
    {


        if(document.pressed == 'Submit')
        {

            document.SLAReportByUserView.action ="SLAReportByUser.jsp";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            document.SLAReportByUserView.action ="SLAReportByUserinEXCEL.jsp";
        }else{
            document.SLAReportByUserView.action ="SLAReportByUserinCSV.jsp";
        }
        return true;
    }


</script>
<FORM NAME="SLAReportByUserView" method='POST' onsubmit="return OnSubmitForm();">

    <TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
        <TR><TD colspan=4>&nbsp;</TD></TR>
        <TR><TD align=center colspan=10>
                <SPAN CLASS="header1"> SLA&nbsp;Report&nbsp;By&nbsp;Group&nbsp;or&nbsp;Userid </SPAN></TD>
        </TR>
        <TR><TD colspan=2>&nbsp;</TD></TR>
        <TR><TD colspan=8>&nbsp;</TD></TR>
        <TR>
            <TD align=right>From Date:&nbsp;</TD>
            <TD><SELECT name="from_due_date_mnth">
                    <%
                                String y;
                                Calendar cal = Calendar.getInstance();
                                int iMth = cal.get(Calendar.MONTH) + 1;
                                int iDay = cal.get(Calendar.DAY_OF_MONTH);
                                int iYear = cal.get(Calendar.YEAR);
                                for (int x = 1; x < 13; x++) {
                                    y = "" + x;
                                    if (y.length() == 1) {
                                        y = "0" + x;
                                    }
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
                                for (int x = 1; x < 32; x++) {
                                    y = "" + x;
                                    if (y.length() == 1) {
                                        y = "0" + x;
                                    }
                                    if (x == iDay) {
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
                                for (int x = 2001; x <= iYear; x++) {
                                    if (x == iYear) {
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
            <TD align=right>To Date:&nbsp;</TD>
            <TD><SELECT name="to_due_date_mnth">
                    <%
                                for (int x = 1; x < 13; x++) {
                                    y = "" + x;
                                    if (y.length() == 1) {
                                        y = "0" + x;
                                    }
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
                                for (int x = 1; x < 32; x++) {
                                    y = "" + x;
                                    if (y.length() == 1) {
                                        y = "0" + x;
                                    }
                                    if (x == iDay) {
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
                                for (int x = 2001; x <= iYear ; x++) {
                                    if (x == iYear) {
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
        <TR><TD colspan=8>&nbsp;</TD></TR>
    </TABLE><br>
    <TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>

        <%
                    Connection con = null;
                    Statement stmt = null;
                    ResultSet rs = null;
                    PreparedStatement pstmt = null;

                    try {
                        con = DatabaseManager.getConnection();
                        stmt = con.createStatement();

                        String strQryGrps = " Select USRGRP_EMP_SQNC_NMBR, USERGROUP_NAME, "
                                + " USR_GRP_DSCRPTN  FROM USERGROUP_EMP_T uel "
                                + " where exists (select USRGRP_EMP_SQNC_NMBR "
                                + " from USR_USRGRP_LINK_T where "
                                + " USRGRP_EMP_SQNC_NMBR =  uel.USRGRP_EMP_SQNC_NMBR)"
                                + " ORDER BY USERGROUP_NAME ";

                        StringBuffer sbUsrSection = new StringBuffer(256);

                        sbUsrSection.append(" <select NAME=\"USERID\" MULTIPLE SIZE=5>"
                                + " <option value=\"ALL\">--Report on all Userids--  ");

                        StringBuffer sbGrpSection = new StringBuffer(256);

                        sbGrpSection.append(" <select NAME=\"groupids\" MULTIPLE SIZE=5>"
                                + " <option value=\"ALL\" >--Report on all group id--  ");

                        pstmt = con.prepareStatement(strQryGrps);
                        pstmt.clearParameters();
                        rs = pstmt.executeQuery();
                        while (rs.next() == true) {
                            sbGrpSection.append("<option value=\"" + rs.getString(1) + "\">" + rs.getString(2) + "</option>");
                        }
                        sbGrpSection.append("</select></td></tr>");

                        //Only include users that have the ability to work on orders/LSRs
                        rs = stmt.executeQuery("SELECT U.USERID, U.LST_NM||', '||U.FRST_NM||' ('||U.USERID||') '"
                                + " FROM USERID_T U, COMPANY_T C, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA "
                                + " WHERE U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP = 'P' "
                                + " AND UGA.USERID=U.USERID AND SGA.SCRTY_GRP_CD=UGA.SCRTY_GRP_CD "
                                + " AND SGA.SCRTY_OBJCT_CD='PROV_RQST_ACTIONS'  "
                                + " ORDER BY U.LST_NM ASC");
                        while (rs.next() == true) {
                            sbUsrSection.append("<option value=\"" + rs.getString(1) + "\">");
                            sbUsrSection.append(rs.getString(2) + "</option> ");
                        }
                        sbUsrSection.append("</select></td></tr>");
                        rs.close();
                        pstmt.close();
                        pstmt = null;
        %>
        <tr><td align=right><b>Employee Group(s)&nbsp;*&nbsp;:&nbsp;</b></td><td>
                <%=sbGrpSection.toString()%></td></tr>
        <TR><TD colspan=4>&nbsp;</TD></TR>

        <tr><td align=right ><b>Userid(s):</b>&nbsp;*&nbsp;:&nbsp;</b></td><td>
                <%=sbUsrSection.toString()%></td></tr>

        <tr><td colspan=2>&nbsp;</td></tr>
        <tr>
            <td align=right>OCN&nbsp;Code&nbsp;*&nbsp;:&nbsp;</td><td>
                <select MULTIPLE size=5 NAME="OCN_CD">
                    <option value=ALL>Report on all OCNs
                        <%

                                                rs = stmt.executeQuery("SELECT DISTINCT O.OCN_CD, O.OCN_NM, C.CMPNY_SQNC_NMBR FROM OCN_T O, COMPANY_T C "
                                                        + " WHERE O.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP IN ('R','C','L') ORDER BY OCN_CD ASC");

                                                while (rs.next() == true) {
                        %>
                    <option value=<%= rs.getString("OCN_CD")%>-<%= rs.getString("CMPNY_SQNC_NMBR")%>><%= rs.getString("OCN_CD")%>&nbsp;-&nbsp;<%= rs.getString("OCN_NM")%>
                        <%
                                                }
                                                rs.close();
                        %>
                        </td>
                    <TR><TD colspan=2>&nbsp;</TD></TR>
                    <tr>
                        <td align=right >State&nbsp;Code&nbsp;*&nbsp;:&nbsp;</td><td>
                            <select MULTIPLE NAME="STATE_CD">
                                <option value=ALL>Report on all States

                                    <%
                                                            rs = stmt.executeQuery("SELECT DISTINCT STT_CD, STT_NM FROM STATE_T ORDER BY STT_CD ASC");
                                                            while (rs.next() == true) {
                                    %>
                                <option value=<%= rs.getString("STT_CD")%> ><%= rs.getString("STT_CD")%>&nbsp;-&nbsp;<%= rs.getString("STT_NM")%>
                                    <%
                                                            }
                                                            rs.close();
                                    %>
                                    </td>
                                <TR><TD colspan=2>&nbsp;</TD></TR>
                                <tr>
                                    <td align=right >Vendor&nbsp;*&nbsp;:&nbsp;</td><td>
                                        <select MULTIPLE NAME="VENDOR">
                                            <option value=ALL>Report on all Vendors

                                                <%
                                                                        rs = stmt.executeQuery("SELECT DISTINCT CMPNY_SQNC_NMBR, CMPNY_NM FROM COMPANY_T WHERE CMPNY_TYP IN ('R','C','L') ORDER BY CMPNY_NM");
                                                                        while (rs.next() == true) {
                                                %>
                                            <option value=<%= rs.getString("CMPNY_SQNC_NMBR")%> ><%= rs.getString("CMPNY_NM")%>
                                                <%
                                                                        }
                                                                        rs.close();
                                                %>
                                                </td>
                                            <TR><TD colspan=2>&nbsp;</TD></TR>
                                            <tr>
                                                <td align=right >Service Type&nbsp;*&nbsp;:&nbsp;</td><td>
                                                    <select MULTIPLE NAME="SRVC_TYP_CD">
                                                        <option value=ALL>Report on all Service Types

                                                            <%
                                                                                    rs = stmt.executeQuery("SELECT DISTINCT SRVC_TYP_CD, SRVC_TYP_DSCRPTN FROM SERVICE_TYPE_T WHERE TYP_IND ='R' ORDER BY SRVC_TYP_DSCRPTN");
                                                                                    while (rs.next() == true) {
                                                            %>
                                                        <option value=<%= rs.getString("SRVC_TYP_CD")%> ><%= rs.getString("SRVC_TYP_DSCRPTN")%>
                                                            <%
                                                                                    }
                                                                                    rs.close();
                                                                                    rs = null;
                                                            %>
                                                            </td>
                                                            </tr>

                                                        <TR><TD colspan=2>&nbsp;</TD></TR>
                                                        <tr>
                                                            <td align=right >Activity Type&nbsp;*&nbsp;:&nbsp;</td><td>
                                                                <select MULTIPLE NAME="ACTVTY_TYP_CD">
                                                                    <option value=ALL>Report on all Activity Types
                                                                    <option value=BDS>Suspends/Restores/Disconnects

                                                                        <%
                                                                                                rs = stmt.executeQuery("SELECT DISTINCT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T WHERE TYP_IND ='R' ORDER BY ACTVTY_TYP_DSCRPTN");
                                                                                                while (rs.next() == true) {
                                                                        %>
                                                                    <option value=<%= rs.getString("ACTVTY_TYP_CD")%> ><%= rs.getString("ACTVTY_TYP_DSCRPTN")%>
                                                                        <%
                                                                                                }
                                                                                                rs.close();
                                                                                                rs = null;
                                                                        %>
                                                                        </td>
                                                                        </tr>

										<TR><TD colspan=2>&nbsp;</TD></TR>
                                                                    <TR>
                                                                        <TD align=right>&nbsp;Order&nbsp;Type :&nbsp;</TD>
                                                                        <TD align=left><SELECT NAME="orderFlag">
                                                                                <option value="ALL">ALL </OPTION>
                                                                                <option value="N">CAMS</OPTION>
                                                                                <option value="Y">ICARE</OPTION>
                                                                            </SELECT></TD>
                                                                    </TR>

                                                                    <TR><TD colspan=8 align=center><br>&nbsp;*&nbsp;- use Ctrl key to make multiple selections</TD></TR>

                                                                    <%
                                                                                } //try
                                                                                catch (Exception e) {
                                                                                    rs.close();
                                                                                    rs = null;
                                                                                    Log.write(Log.ERROR, "SLAByUserDateSelect.jsp : Exception caught [" + e + "]");
                                                                                    stmt.close();
                                                                                    stmt = null;
                                                                                } finally {
                                                                                    DatabaseManager.releaseConnection(con);
                                                                                }
                                                                    %>

                                                                    <TR><TD colspan=8>&nbsp;</TD></TR>
                                                                    <TR><TD colspan=8>&nbsp;</TD></TR>

                                                                    <TR>
                                                                        <TD align='center' colspan=2>
                                                                            <INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit" onclick="document.pressed=this.value">
											&nbsp;	&nbsp;
                                                                            <INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="EXCEL" onclick="document.pressed=this.value"></TD>

                                                                    </TR>
                                                                    <TR><TD colspan=2>&nbsp;</TD></TR>
                                                                    <TR><TD colspan=2>&nbsp;</TD></TR>
                                                                    <TR>
                                                                        <TD align=center colspan=4>
                                                                            <%= (String) request.getAttribute("slastat")%>
                                                                        </TD>
                                                                    </TR>

                                                                    </TABLE>
                                                                    </FORM>
                                                                    <jsp:include page="i_footer.htm" flush="true" />
                                                                    </body>
                                                                    </html>

                                                                    <%
                                                                                /* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/SLAByUserDateSelect.jsv  $
                                                                                /*
                                                                                /*Initial Checkin
                                                                                 */

                                                                                /* $Revision:   .  $
                                                                                 */
                                                                    %>
