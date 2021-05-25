<%
            /**
             * NOTICE:
             *              THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
             *              SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
             *              USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
             *
             *                      COPYRIGHT (C) 2004
             *                              BY
             *                      Windstream COMMUNICATIONS INC.
             */
            /**
             * MODULE:      SLADateSelect.jsp
             * DESCRIPTION: SLA report selection criteria page
             * AUTHOR:      psedlak
             * DATE:        1/1/2002
             *
             * HISTORY:
             *      08/30/2004	psedlak added OCN selection
             *      10/30/2004	psedlak added state
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
            document.SLAReportView.action ="SLAReport.jsp";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            document.SLAReportView.action ="SLAExcelReport.jsp";
        }
        return true;
    }


</script>
<FORM NAME="SLAReportView" METHOD=POST onsubmit="return OnSubmitForm();">

    <TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
        <TR><TD colspan=4>&nbsp;</TD></TR>
        <TR><TD align=center colspan=10>
                <SPAN CLASS="header1"> S&nbsp;L&nbsp;A&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;O&nbsp;R&nbsp;D&nbsp;E&nbsp;R&nbsp;S&nbsp;</SPAN></TD>
        </TR>
        <TR><TD colspan=2>&nbsp;</TD></TR>
        <TR><TD colspan=8>&nbsp;</TD></TR>
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
        <tr>
            <td align=right>OCN&nbsp;Code&nbsp;*&nbsp;:&nbsp;</td><td>
                <select MULTIPLE size=5 NAME="OCN_CD">
                    <option value=ALL>Report on all OCNs

                        <%
                                    Connection con = null;
                                    Statement stmt = null;
                                    ResultSet rs = null;

                                    try {
                                        con = DatabaseManager.getConnection();
                                        stmt = con.createStatement();
                                        rs = stmt.executeQuery("SELECT DISTINCT O.OCN_CD, O.OCN_NM, C.CMPNY_SQNC_NMBR FROM OCN_T O, COMPANY_T C "
                                                + " WHERE O.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND C.CMPNY_TYP IN ('R','C','L') ORDER BY OCN_CD ASC");

                                        while (rs.next() == true) {
                        %>
                    <option value=<%= rs.getString("OCN_CD")%>-<%= rs.getString("CMPNY_SQNC_NMBR")%>><%= rs.getString("OCN_CD")%>&nbsp;-&nbsp;<%= rs.getString("OCN_NM")%>
                        <%
                            }
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


                                                                DatabaseManager.releaseConnection(con);
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
                                                                        Log.write(Log.ERROR, "SLADateSelect.jsp : Exception caught [" + e + "]");
                                                                        stmt.close();
                                                                        stmt = null;
                                                                    } finally {
                                                                        DatabaseManager.releaseConnection(con);
                                                                    }
                                                        %>

                                                        <TR><TD colspan=8>&nbsp;</TD></TR>
                                                        <TR><TD colspan=8>&nbsp;</TD></TR>
                                                        <TR><TD colspan=8>&nbsp;</TD></TR>
                                                        <TR>
                                                            <TD align=center colspan=8>
                                                                <INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="Submit" onclick="document.pressed=this.value">
                                                                &nbsp;&nbsp;
                                                                <INPUT class=appButton TYPE="SUBMIT" NAME="SUBMITBUTTON" VALUE="EXCEL"onclick="document.pressed=this.value" >
                                                            </TD>

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
                                                                    /* $Log:   //ACI-AIS-PVCS/SCM2PVCS/PVCS/ARCHIVES/express/JAVA/JSP/SLADateSelect.jsv  $
                                                                    /*
                                                                    /*   Rev 1.4   May 30 2002 09:01:14   sedlak
                                                                    /*
                                                                    /*
                                                                    /*   Rev 1.3   22 May 2002 06:39:48   dmartz
                                                                    /*
                                                                    /*
                                                                    /*   Rev 1.2   31 Jan 2002 13:35:54   sedlak
                                                                    /*
                                                                    /*
                                                                    /*   Rev 1.1   31 Jan 2002 08:11:50   sedlak
                                                                    /*
                                                                    /*
                                                                    /*   Rev 1.0   23 Jan 2002 11:06:32   wwoods
                                                                    /*Initial Checkin
                                                                     */

                                                                    /* $Revision:   1.4  $
                                                                     */
                                                        %>
