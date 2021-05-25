<%
            /**
             * NOTICE:
             *		THIS MATERIAL CONTAINS TRADE SECRETS THAT BELONGS TO Windstream INFORMATION
             *		SERVICES, INC. AND IS LICENSED BY AN AGREEMENT. ANY UNAUTHORIZED ACCESS,
             *		USE, DUPLICATION, OR DISCLOSURE IS UNLAWFUL.
             *
             *			COPYRIGHT (C) 2005
             *				BY
             *			Windstream COMMUNICATIONS INC.
             */
            /**
             * AUTHOR:      Express Development Team
             *
             * DATE:        11-2-2005
             *
             */
%>

<%@ include file="i_header.jsp" %>
<%@ include file="ExpressUtil.jsp" %>
<%@ page import ="java.util.*" %>
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="com.alltel.lsr.common.util.*" %>
<%            final String SECURITY_OBJECT = "PROV_REPORTS";
            if (!sdm.isAuthorized(SECURITY_OBJECT)) {
                Log.write(Log.WARNING, sdm.getUser() + " is trying to access JSP assoc with " + SECURITY_OBJECT);
                alltelResponse.sendRedirect(SECURITY_URL);
            }
            final String EXPRESS_FUNCTION = "CREATE_BDPS";
            final String BDP_QC = "Bdp";
            Connection conRC = null;
            Statement stmtRC = null;
            ResultSet rsRC = null;
            boolean bKpen = true;
            String strArrStt[] = {"__"};
            try {
                conRC = DatabaseManager.getConnection();
                stmtRC = conRC.createStatement();

                HttpSession objSession = alltelRequest.getSession();
                String strSearchSeqNum = request.getParameter("seqnum");
%>

<SCRIPT LANGUAGE="JavaScript">

    var arrProductTypCd = new Array();
    var arrProductTypMOXIDX = new Array();
    var arrOrdrTypCd = new Array();
    var arrChgTypDesc = new Array();
    var arrChgTypMOXIDX = new Array();
    var arrChgSubTypMOXIDX = new Array();

    var arrActTypCd = new Array();
    var arrActTypDesc = new Array();

    var arrSubActTypCd = new Array();
    var arrSubActTypDesc = new Array();

    <%
    rsRC = stmtRC.executeQuery("SELECT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN, SUB_ACTVTY_TYP_MOX_IDX FROM ACTIVITY_TYPE_T WHERE TYP_IND = 'X' ORDER BY ACTVTY_TYP_CD ASC");
    int iAct = 0;
    while (rsRC.next() == true) {
    %>
    arrActTypCd[<%=iAct%>] = "<%=rsRC.getString("ACTVTY_TYP_CD")%>";
    arrActTypDesc[<%=iAct%>] = "<%=rsRC.getString("ACTVTY_TYP_DSCRPTN")%>";
    arrChgSubTypMOXIDX[<%=iAct%>] = "<%=rsRC.getString("SUB_ACTVTY_TYP_MOX_IDX")%>";
    <%
        iAct++;
    }
    rsRC.close();

    rsRC = stmtRC.executeQuery("SELECT SUB_ACTVTY_TYP_CD, SUB_ACTVTY_TYP_DSCRPTN FROM SUB_ACTIVITY_TYPE_T WHERE TYP_IND = 'X' ORDER BY SUB_ACTVTY_TYP_CD ASC");
    int iSubAct = 0;
    while (rsRC.next() == true) {
    %>
    arrSubActTypCd[<%=iSubAct%>] = "<%=rsRC.getString("SUB_ACTVTY_TYP_CD")%>";
    arrSubActTypDesc[<%=iSubAct%>] = "<%=rsRC.getString("SUB_ACTVTY_TYP_DSCRPTN")%>";
    <%
        iSubAct++;
    }
    rsRC.close();
    %>

    function setOrderTypSelect(RqstTypControl, OrderTypControl)
    {
        // Clear the options in the "Order Type" dropdown --- drive by MOX_IDX in PRODUCT_T table...
        for (var z=OrderTypControl.options.length ; z >= 1 ; z--)
            OrderTypControl.options[z]=null;
  	
        var OrderTypeOption ;
        OrderTypeOption = document.createElement("option") ;
        OrderTypeOption.value = "ALL" ;
        OrderTypeOption.text = "All Order Types";
        OrderTypControl.add(OrderTypeOption) ;
        for (var x = 0 ; x < arrProductTypCd.length  ; x++ )
        {
            if ( arrProductTypCd[x] == RqstTypControl.value )
            {
                for (var y = 0 ; y < arrOrdrTypCd.length  ; y++ )
                {
                    if (arrProductTypMOXIDX[x].indexOf(arrOrdrTypCd[y]) >= 0)
                    {
                        OrderTypeOption = document.createElement("option") ;
                        OrderTypeOption.value = arrOrdrTypCd[y] ;
                        OrderTypeOption.text = arrChgTypDesc[y];
                        OrderTypControl.add(OrderTypeOption) ;
                    }
                }
            }
        }
    }

    function setChgTypSelect(RqstTypControl, SrvcTypControl)
    {
        // Clear the options in the "Change Type" dropdown
        for (var z=SrvcTypControl.options.length ; z >= 1 ; z--)
            SrvcTypControl.options[z]=null;
        var SrvcTypeOption ;
        SrvcTypeOption = document.createElement("option") ;
        SrvcTypeOption.value =  "ALL" ;
        SrvcTypeOption.text = "All Change Types";
        SrvcTypControl.add(SrvcTypeOption) ;
	
        for (var x = 0 ; x < arrOrdrTypCd.length  ; x++ )
        {
            if ( arrOrdrTypCd[x] == RqstTypControl.value )
            {
                for (var y = 0 ; y < arrActTypCd.length  ; y++ )
                {
                    if (arrChgTypMOXIDX[x].indexOf(arrActTypCd[y]) >= 0)
                    {
                        SrvcTypeOption = document.createElement("option") ;
                        SrvcTypeOption.value = arrActTypCd[y] ;
                        SrvcTypeOption.text = arrActTypDesc[y];
                        SrvcTypControl.add(SrvcTypeOption) ;
                    }
                }
            }
        }
    }

    function setChgSubTypSelect(RqstTypControl, ActvtyTypControl)
    {
        // Clear the options in the Activity Type Select Control
        for (var z=ActvtyTypControl.options.length ; z >= 1 ; z--) ActvtyTypControl.options[z]=null;

        var ActvtyTypeOption ;
        ActvtyTypeOption = document.createElement("option") ;
        ActvtyTypeOption.value = "ALL" ;
        ActvtyTypeOption.text = "All Change Sub Type";
        ActvtyTypControl.add(ActvtyTypeOption) ;
        for (var x = 0 ; x < arrActTypCd.length  ; x++ )
        {
            if ( arrActTypCd[x] == RqstTypControl.value )
            {
                for (var y = 0 ; y < arrSubActTypCd.length  ; y++ )
                {
                    if (arrChgSubTypMOXIDX[x].indexOf(arrSubActTypCd[y]) >= 0)
                    {
                        ActvtyTypeOption = document.createElement("option") ;
                        ActvtyTypeOption.value = arrSubActTypCd[y] ;
                        ActvtyTypeOption.text = arrSubActTypDesc[y];
                        ActvtyTypControl.add(ActvtyTypeOption) ;
                    }
                }
            }
        }
    }

    function checkValues()
    {
        // some form edits
        if(document.pressed == 'Submit')
        {
            document.FFReportForm.action ="BDPActivityReport.jsp";
        }
        else
            if(document.pressed == 'EXCEL')
        {
            document.FFReportForm.action ="BDPActivityExcelReport.jsp";
        }else{
            document.FFReportForm.action ="BDPActivityReport.jsp";
        }
        var opt_val = window.document.FFReportForm.dwonew_srvctyp.options[window.document.FFReportForm.dwonew_srvctyp.selectedIndex].value;
        if (window.document.FFReportForm.dwonew_ocncd.value == "New")
        {	//if new, then new order type
            if ( opt_val != "D" )
            {
                alert("Order type must be New for an Add New Site order");
                return false;
            }
        }
        else if (window.document.FFReportForm.dwonew_ocncd.value == "")
        {	// Site must be picked
            alert("Site must be selected");
            return false;
        }
        if (opt_val == "")
        {	// Order type must be picked
            alert("Order Type must be selected");
            return false;
        }
        else if (window.document.FFReportForm.dwonew_ocncd.value == "")
        {	// must pick a site for non-New order
            alert("Site must be selected for this Order Type");
            return false;
        }
        return true;
    }

</SCRIPT>
<FORM NAME="FFReportForm" METHOD=POST  onSubmit="return checkValues();">
    <body onLoad="setOrderTypSelect(FFReportForm.dwonew_prdcttyp, FFReportForm.dwonew_srvctyp);">
        <TABLE width=70% align=center cellspacing=0 cellpadding=0 border=0>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <TR><TD align=center colspan=4>
                    <SPAN CLASS="header1"> B&nbsp;D&nbsp;P &nbsp;&nbsp;S&nbsp;A&nbsp;L&nbsp;E&nbsp;S &nbsp;&nbsp; A&nbsp;C&nbsp;T&nbsp;I&nbsp;V&nbsp;I&nbsp;T&nbsp;Y&nbsp;&nbsp; R&nbsp;E&nbsp;P&nbsp;O&nbsp;R&nbsp;T</SPAN></TD>
            </TR>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <TR><TD colspan=4 align=center>&nbsp;<SPAN CLASS="errormsg"><%= (String) request.getAttribute("slastat")%></span></TD></TR>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <tr>
                <TD colspan=4 align=center>From Date:<%=printDTSelect("startMn", "startDy", "startYr")%>
                    &nbsp;&nbsp;&nbsp;To&nbsp;Date:<%=printDTSelect("endMn", "endDy", "endYr")%><br>
                </TD>
            </tr>
            <tr>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <TR><td  width=40% colspan=2 valign=top align=right>
                    <b>States:&nbsp;*&nbsp;</b></TD>
                <TD colspan=2 align=left>
                    <%=printSelectBoxStates_ALLOp("state", strArrStt, 3)%>
                </TD>

            </tr>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <%
                Statement stmt = null;
                ResultSet rs = null;
                PreparedStatement pstmt = null;
                stmt = conRC.createStatement();
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
                pstmt = conRC.prepareStatement(strQryGrps);
                pstmt.clearParameters();
                rs = pstmt.executeQuery();
                while (rs.next() == true) {
                    sbGrpSection.append("<option value=\"" + rs.getString(1) + "\">" + rs.getString(2) + "</option>");
                }
                sbGrpSection.append("</select></td></tr>");

                //Only include users that have the ability to work on orders/LSRs
                rs = stmt.executeQuery("SELECT U.USERID, U.LST_NM||', '||U.FRST_NM||' ('||U.USERID||') '"
                        + " FROM USERID_T U, COMPANY_T C, USER_GROUP_ASSIGNMENT_T UGA, SECURITY_GROUP_ASSIGNMENT_T SGA "
                        + " WHERE U.CMPNY_SQNC_NMBR=C.CMPNY_SQNC_NMBR AND ( C.CMPNY_TYP = 'P' or C.CMPNY_TYP = 'W')  "
                        + " AND UGA.USERID=U.USERID AND SGA.SCRTY_GRP_CD=UGA.SCRTY_GRP_CD "
                        + " AND SGA.SCRTY_OBJCT_CD='PROV_BDP_ACTIONS'  "
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
            <tr><td colspan=2 align=right><b>Employee Group(s)&nbsp;*&nbsp;:&nbsp;</b></td><td>
                    <%=sbGrpSection.toString()%></td></tr>
            <TR><TD colspan=4>&nbsp;</TD></TR>

            <tr><td colspan=2 align=right ><b>Userid(s):</b>&nbsp;*&nbsp;:&nbsp;</b></td>
                <td colspan=2td>
                    <%=sbUsrSection.toString()%></td></tr>

            <tr><td colspan=2>&nbsp;</td></tr>
            <TR>
                <TD width=40% colspan=2 align=right><b>Product:&nbsp;</b></TD>
                <TD colspan=2 align=left>
                    <SELECT id="dwonew_prdcttyp" NAME="dwonew_prdcttyp" onchange="setOrderTypSelect(this, FFReportForm.dwonew_srvctyp);">
                        <option value="none">... Select a Product ...</option>
                        <%
                            rsRC = stmtRC.executeQuery("SELECT PRDCT_TYP_CD, PRDCT_DSCRPTN, SCRTY_OJBCT_CD, ORDR_TYP_MOX_IDX FROM PRODUCT_T WHERE TYP_IND = 'X' ORDER BY PRDCT_DSCRPTN ");
                            int pt = 0;
                            while (rsRC.next() == true) //{	if (sdm.isAuthorized(rsRC.getString("SCRTY_OJBCT_CD")))
                            {
                                if (EXPRESS_FUNCTION.equals((rsRC.getString("SCRTY_OJBCT_CD")))) //load only BDP choices -security was verified up top
                                {
                                    Log.write(Log.DEBUG_VERBOSE, " --loading product =" + rsRC.getString("PRDCT_DSCRPTN") + " mox:" + rsRC.getString("ORDR_TYP_MOX_IDX"));
                        %>
                        <option value=<%= rsRC.getString("PRDCT_TYP_CD")%> <% if (pt == 0) {%> SELECTED <% }%>>&nbsp; <%= rsRC.getString("PRDCT_DSCRPTN")%></option>
                        <SCRIPT LANGUAGE="JavaScript">
                            arrProductTypCd[<%=pt%>] = "<%=rsRC.getString("PRDCT_TYP_CD")%>";
                            arrProductTypMOXIDX[<%=pt%>] = "<%=rsRC.getString("ORDR_TYP_MOX_IDX")%>";
                        </SCRIPT>

                        <%
                                    pt++;
                                }
                            }
                            rsRC.close();
                        %>
                    </SELECT>
                </TD>
            </TR>

            <TR><TD colspan=4>&nbsp;</TD></TR>
            <TR>
                <TD  width=40% colspan=2 align=right><b>Order&nbsp;Type:&nbsp;</b></TD>
                <TD colspan=2 align=left>
                    <SELECT id="dwonew_srvctyp" NAME="dwonew_srvctyp" onchange="setChgTypSelect(this, FFReportForm.dwonew_acttyp);">
                        <option value="ALL">... Select an Order Type ...</option>
                        <%
                            rsRC = stmtRC.executeQuery("SELECT SRVC_TYP_CD, SRVC_TYP_DSCRPTN, ACTVTY_TYP_MOX_IDX FROM SERVICE_TYPE_T WHERE TYP_IND = 'X' ORDER BY SRVC_TYP_CD ASC");
                            int st = 0;
                            while (rsRC.next() == true) {
                        %>
                        <option value=<%= rsRC.getString("SRVC_TYP_CD")%>><%= rsRC.getString("SRVC_TYP_CD")%> - <%= rsRC.getString("SRVC_TYP_DSCRPTN")%></option>
                        <SCRIPT LANGUAGE="JavaScript">
                    arrOrdrTypCd[<%=st%>] = "<%=rsRC.getString("SRVC_TYP_CD")%>";
                    arrChgTypDesc[<%=st%>] = "<%=rsRC.getString("SRVC_TYP_DSCRPTN")%>";
                    arrChgTypMOXIDX[<%=st%>] = "<%=rsRC.getString("ACTVTY_TYP_MOX_IDX")%>";
                        </SCRIPT>

                        <%
                                st++;
                            }
                            rsRC.close();
                        %>
                    </SELECT>
                </TD>
            </TR>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <TR>
                <TD  width=40% colspan=2 align=right><b>Change&nbsp;Type:&nbsp;</b></TD>
                <TD colspan=2 align=left>
                    <SELECT id="dwonew_acttyp" NAME="dwonew_acttyp" onchange="setChgSubTypSelect(this, FFReportForm.dwonew_actsubtyp);">
                        <option value="ALL">... Select a Change Type ...</option>
                        <%
                            rsRC = stmtRC.executeQuery("SELECT ACTVTY_TYP_CD, ACTVTY_TYP_DSCRPTN FROM ACTIVITY_TYPE_T WHERE TYP_IND = 'X' ORDER BY ACTVTY_TYP_CD ASC");
                            iAct = 0;
                            while (rsRC.next() == true) {
                        %>
                        <option value=<%= rsRC.getString("ACTVTY_TYP_CD")%>><%= rsRC.getString("ACTVTY_TYP_DSCRPTN")%></option>

                        <%
                                iAct++;
                            }
                            rsRC.close();
                        %>
                    </SELECT>
                </TD>
            </TR>

            <TR><TD colspan=4>&nbsp;</TD></TR>
            <TR>
                <TD colspan=2 width=40% align=right><b>Change&nbsp;Sub&nbsp;Type:&nbsp;
                    </b></TD><TD colspan=2 align=left>
                    <SELECT id="dwonew_actsubtyp" NAME="dwonew_actsubtyp">
                        <option value="ALL">... Select a Change Sub-Type ...</option>
                    </SELECT>
                </TD>
            </TR>
            <TR><TD colspan=4>&nbsp;</TD></TR><TR><TD colspan=4>&nbsp;</TD></TR>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <TR>
                <TD align=center colspan=4>
                    <INPUT class=appButton TYPE="RESET" VALUE="Reset">&nbsp;&nbsp;
                    <INPUT class=appButton TYPE="SUBMIT" NAME="submit" VALUE="Submit" onclick="document.pressed=this.value">&nbsp;&nbsp;
                    <INPUT class=appButton TYPE="SUBMIT" NAME="excel" VALUE="EXCEL" onclick="document.pressed=this.value">&nbsp;&nbsp;
                    <INPUT class=appButton TYPE="SUBMIT" NAME="cancel" VALUE="Cancel" onclick="document.pressed=this.value">&nbsp;&nbsp;
                </TD>
            </TR>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <TR><TD colspan=4>&nbsp;</TD></TR>
            <tr><td colspan=4 align=center><span class=smallNote>
                        (<i>Hold down Control Key to select more than one items</i>)</span>
                </td></tr>
            <BR>
        </TABLE>

</FORM>
<%
            } // try
            catch (Exception e) {
            } finally {
                stmtRC.close();
                stmtRC = null;
                DatabaseManager.releaseConnection(conRC);
            }
%>
<jsp:include page="i_footer.htm" flush="true" />
</body>
</html>

<%
            /* $Log:   $
            /*
            /*Initial Checkin
             */

            /* $Revision:     $
             */
%>
